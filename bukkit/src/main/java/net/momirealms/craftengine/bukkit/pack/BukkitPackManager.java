package net.momirealms.craftengine.bukkit.pack;

import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.api.event.AsyncResourcePackGenerateEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.bukkit.util.PlayerUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.pack.PackManager;
import net.momirealms.craftengine.core.pack.PackManagerImpl;
import net.momirealms.craftengine.core.pack.host.AbstractPackHost;
import net.momirealms.craftengine.core.pack.host.ExternalPackHost;
import net.momirealms.craftengine.core.pack.host.LocalPackHost;
import net.momirealms.craftengine.core.pack.host.PackHostFactory;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.ZipUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.floor;

public class BukkitPackManager implements PackManager, Listener {
	public static final NamespacedKey PACK_GAMEMODE_KEY = Objects.requireNonNull(NamespacedKey.fromString("craftengine:pack_gamemode"));
	private final PackManagerImpl packManager;
	private final BukkitCraftEngine plugin;
	private String packHash;
	private final List<AbstractPackHost> hosts = new ArrayList<>();
	private final Map<UUID, Integer> cameraEntities = new ConcurrentHashMap<>();

	public BukkitPackManager(BukkitCraftEngine plugin) {
		this.plugin = plugin;
		this.packManager = new PackManagerImpl(plugin, (rf, zp) -> {
			AsyncResourcePackGenerateEvent endEvent = new AsyncResourcePackGenerateEvent(rf, zp);
			EventUtils.fireAndForget(endEvent);
		});
		this.enable();
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (!VersionHelper.isVersionNewerThan1_20_2()) return;
		BukkitNetworkManager networkManager = plugin.networkManager();
		BukkitServerPlayer user = (BukkitServerPlayer) networkManager.getUser(networkManager.getChannel(event.getPlayer(), true));
		if (user != null) {
			user.setPlayer(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if ((!VersionHelper.isVersionNewerThan1_20_2() && ConfigManager.enableHost()) || (ConfigManager.enableHost() && !ConfigManager.beforeJoin())) {
			this.sendResourcePack(plugin.networkManager().getUser(event.getPlayer()));
		}
		if (event.getPlayer().getGameMode() != GameMode.SPECTATOR) return;
		endReceivingPack(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		cameraEntities.remove(event.getPlayer().getUniqueId());
	}

	@Override
	public void enable() {
		calculateHash();
		PackHostFactory.registerHost("local", true, LocalPackHost::new);
		PackHostFactory.registerHost("external", false, ExternalPackHost::new);
	}

	@Override
	public void load() {
		packManager.load();
		this.loadHosts();
		Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
	}

	@Override
	public void unload() {
		packManager.unload();
		for (AbstractPackHost host : this.hosts) {
			if (!host.isSingleton()) host.disable();
		}
		this.hosts.clear();
		HandlerList.unregisterAll(this);
	}

	@Override
	public void disable() {
		PackHostFactory.unregisterAll(hosts);
		unload();
		packManager.disable();
	}

	@Override
	public @NotNull Collection<Pack> loadedPacks() {
		return packManager.loadedPacks();
	}

	@Override
	public boolean registerConfigSectionParser(ConfigSectionParser parser) {
		return packManager.registerConfigSectionParser(parser);
	}

	@Override
	public boolean unregisterConfigSectionParser(String id) {
		return packManager.unregisterConfigSectionParser(id);
	}

	@Override
	public void generateResourcePack() {
		packManager.generateResourcePack();
		calculateHash();
		sendAllPlayersResourcePack();
	}

	private void loadHosts() {
		if (!ConfigManager.enableHost()) return;
		Path packPath = plugin.dataFolderPath().resolve("generated/resource_pack.zip");

		List<String> singletons = new ArrayList<>();

		for (Map<String, Object> method : ConfigManager.resourcePack$methods()) {
			String type = (String) method.get("type");
			if (singletons.contains(type)) {
				CraftEngine.instance().logger().warn(type + " is singleton");
				continue;
			}
			AbstractPackHost host = PackHostFactory.getOrCreateHost(type);
			if (host == null) continue;
			if (host.isSingleton()) singletons.add(type);
			host.setConfig(packPath, method);
			host.reload();
			if (!host.isEnable()) continue;
			hosts.add(host);
		}
		if (hosts.isEmpty()) {
			CraftEngine.instance().logger().warn("No available resource pack hosts");
		}
	}

	private void calculateHash() {
		this.plugin.logger().info("Calculating resource pack hash...");
		long start = System.currentTimeMillis();
		Path zipFile = this.plugin.dataFolderPath()
					.resolve("generated")
					.resolve("resource_pack.zip");
		if (Files.exists(zipFile)) {
			try {
				this.packHash = ZipUtils.computeSHA1(zipFile);
			} catch (IOException | NoSuchAlgorithmException e) {
				this.plugin.logger().severe("Error calculating resource pack hash", e);
			}
		} else {
			this.plugin.logger().warn(zipFile + " does not exist, make sure to generate the resource pack first");
			this.packHash = "0000000000000000000000000000000000000000";
		}
		long end = System.currentTimeMillis();
		this.plugin.logger().info("Finished calculating resource pack hash in " + (end - start) + "ms");
	}

	private void sendAllPlayersResourcePack() {
		if (ConfigManager.enableHost()) {
			BukkitNetworkManager manger = plugin.networkManager();
			plugin.scheduler().executeAsync(() -> {
				for (Player player : Bukkit.getOnlinePlayers()) {
					BukkitServerPlayer bukkitServerPlayer = (BukkitServerPlayer) manger.getUser(player);
					if (bukkitServerPlayer == null) return;
					bukkitServerPlayer.setPack();
				}
			});
		}
	}

	public void startReceivingPack(BukkitServerPlayer user) {
		Player player = user.platformPlayer();
		if (player == null) return;
		try {
			List<Object> packets = new ArrayList<>();
			int entityId = Reflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
			cameraEntities.put(player.getUniqueId(), entityId);

			Location eye = player.getEyeLocation();
			Object addEntityPacket = Reflections.constructor$ClientboundAddEntityPacket.newInstance(
					entityId, UUID.randomUUID(), eye.getX(), eye.getY(), eye.getZ(), 0, 0,
					Reflections.instance$EntityType$ITEM_DISPLAY, 0, Reflections.instance$Vec3$Zero, 0
			);

			FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
			byteBuf.writeVarInt(entityId);
			Object mcByteBuf = Reflections.constructor$FriendlyByteBuf.newInstance(byteBuf.asByteBuf());
			Object setCameraPacket = Reflections.constructor$ClientboundSetCameraPacket.newInstance(mcByteBuf);
			Object rotPacket = Reflections.constructor$ClientboundMoveEntityPacket$Rot.newInstance(entityId, (byte)floor(user.getXRot() * 256.0F / 360.0F), (byte)floor(user.getYRot() * 256.0F / 360.0F), false);
			packets.add(addEntityPacket);
			packets.add(rotPacket);
			packets.add(setCameraPacket);
			user.sendPacket(Reflections.constructor$ClientboundBundlePacket.newInstance(packets), false);
			plugin.scheduler().executeSync(() -> {
				player.getPersistentDataContainer().set(BukkitPackManager.PACK_GAMEMODE_KEY, PersistentDataType.STRING, player.getGameMode().name());
				player.setGameMode(GameMode.SPECTATOR);
			});
		} catch (Exception e) {
			plugin.logger().severe("Failed to start receiving pack" + e);

		}
	}

	public void endReceivingPack(Player player) {
		plugin.scheduler().executeSync(() -> {
			PersistentDataContainer container = player.getPersistentDataContainer();
			String gameMode = container.get(PACK_GAMEMODE_KEY, PersistentDataType.STRING);
			if (gameMode == null) return;
			player.setGameMode(GameMode.valueOf(gameMode));
			container.remove(PACK_GAMEMODE_KEY);
		});
	}
	public void endReceivingPack(BukkitServerPlayer user) {
		Player player = user.platformPlayer();
		if (player == null) return;
		try {
			Object object = new int[]{this.cameraEntities.get(player.getUniqueId())};
			Object removeEntityPacket = Reflections.constructor$ClientboundRemoveEntitiesPacket.newInstance(object);
			this.cameraEntities.remove(player.getUniqueId());

			FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
			byteBuf.writeVarInt(user.entityID());
			Object mcByteBuf = Reflections.constructor$FriendlyByteBuf.newInstance(byteBuf.asByteBuf());
			Object setCameraPacket = Reflections.constructor$ClientboundSetCameraPacket.newInstance(mcByteBuf);

			user.sendPacket(removeEntityPacket, false);
			user.sendPacket(setCameraPacket, false);
		} catch (Exception e) {
			plugin.logger().warn("Failed to end receiving pack", e);
		}
		endReceivingPack(player);
	}

	public void sendResourcePack(NetWorkUser user) {
		BukkitServerPlayer player = (BukkitServerPlayer) user;
		if (hosts.isEmpty()) {
			if (player.isJoining() && !ConfigManager.kickOnDeclined()) returnToWorld(player);
			else PlayerUtils.kickPlayer(user, "multiplayer.requiredTexturePrompt.disconnect", PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION);
			plugin.logger().warn("No available resource pack hosts");
			return;
		}
		String url = this.hosts().get(player.packIndex()).url();
		String packHash = this.packHash();
		Object packPrompt = ComponentUtils.adventureToMinecraft(ConfigManager.resourcePack$prompt());
		try {
			Object packPacket;
			if (VersionHelper.isVersionNewerThan1_20_5()) {
				packPacket = Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(
						ConfigManager.uuid(), url, packHash, ConfigManager.kickOnDeclined(), Optional.of(packPrompt));
			} else if (VersionHelper.isVersionNewerThan1_20_3()) {
				packPacket = Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(
						ConfigManager.uuid(), url, packHash, ConfigManager.kickOnDeclined(), packPrompt);
			} else {
				packPacket = Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(
						url, packHash, ConfigManager.kickOnDeclined(), packPrompt);
			}
			if (player.isJoining()) PlayerUtils.sendPacketBeforeJoin(user, packPacket);
			else user.sendPacket(packPacket, true);
		} catch (Exception e) {
			CraftEngine.instance().logger().warn("Failed to sent resource pack", e);
		}
	}

	public void returnToWorld(BukkitServerPlayer player) {
		try {
			if (VersionHelper.isVersionNewerThan1_20_2() && player.isJoining()) {
				player.setJoining(false);
				if (VersionHelper.isVersionNewerThan1_20_5()) PlayerUtils.sendPacketBeforeJoin(player, Reflections.field$ClientboundFinishConfigurationPacket$INSTANCE.get(null));
				else PlayerUtils.sendPacketBeforeJoin(player, Reflections.constructor$ClientboundFinishConfigurationPacket.newInstance());
			} else {
				BukkitCraftEngine.instance().packManager().endReceivingPack(player);
			};
			player.setPackIndex(0);
		} catch (Exception e) {
			CraftEngine.instance().logger().warn("Failed to return player to world", e);
		}
	}

	public List<AbstractPackHost> hosts() {
		return this.hosts;
	}

	public String packHash() {
		return this.packHash;
	}

	public boolean isPlayerFrozen(UUID uuid) {
		return cameraEntities.containsKey(uuid);
	}
}
