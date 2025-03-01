package net.momirealms.craftengine.core.pack.host;

import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PackHostFactory {
	private static final Map<String, Supplier<AbstractPackHost>> hostSuppliers = new HashMap<>();
	private static final Map<String, AbstractPackHost> singletonHosts = new HashMap<>();

	public static void registerHost(String type, boolean isSingleton, Supplier<AbstractPackHost> supplier) {
		if (hostSuppliers.containsKey(type)) {
			CraftEngine.instance().logger().warn("PackHost type '" + type + "' already registered, skipping.");
			return;
		}
		hostSuppliers.put(type, supplier);
		if (isSingleton) {
			singletonHosts.put(type, null);
		}
	}

	public static AbstractPackHost getOrCreateHost(String type) {
		if (!hostSuppliers.containsKey(type)) {
			CraftEngine.instance().logger().warn("Unknown PackHost type: " + type);
			return null;
		}

		if (singletonHosts.containsKey(type)) {
			if (singletonHosts.get(type) == null) {
				singletonHosts.put(type, hostSuppliers.get(type).get());
			}
			return singletonHosts.get(type);
		}

		return singletonHosts.containsKey(type) ? singletonHosts.get(type) : hostSuppliers.get(type).get();
	}

	public static void unregisterAll(List<AbstractPackHost> hosts) {
		for (AbstractPackHost host : hosts) {
			if (!host.isSingleton()) host.disable();
		}
		for (AbstractPackHost host : singletonHosts.values()) {
			if (host != null) host.disable();
		}
		hostSuppliers.clear();
		singletonHosts.clear();
	}
}
