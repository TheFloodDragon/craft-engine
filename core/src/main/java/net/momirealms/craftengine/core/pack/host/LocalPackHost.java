package net.momirealms.craftengine.core.pack.host;

import com.sun.net.httpserver.HttpServer;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class LocalPackHost extends AbstractPackHost {
	private HttpServer server;
	private String ip;
	private int port;

	@Override
	public void enable() {
		try {
			this.server = HttpServer.create(new InetSocketAddress(ip, port), 0);
			this.server.createContext("/", exchange -> {
				if (!packPath.toFile().exists()) {
					exchange.sendResponseHeaders(404, -1);
					return;
				}

				exchange.sendResponseHeaders(200, packPath.toFile().length());

				try (OutputStream os = exchange.getResponseBody();
					 InputStream is = Files.newInputStream(packPath)) {

					byte[] buffer = new byte[8192];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
				}
			});
			this.url = "http://" + ip + ":" + port + "/";
			this.server.start();
		} catch (IOException e) {
			CraftEngine.instance().logger().warn("Could not start resource pack local host server", e);
		}
	}

	@Override
	public void reload() {
		boolean oldEnable = this.enable;
		String oldIp = this.ip;
		int oldPort = this.port;

		this.enable = (boolean) properties.getOrDefault("enable", false);
		this.ip = (String) properties.getOrDefault("ip", "127.0.0.1");
		this.port = (int) properties.getOrDefault("port", 12345);

		if (this.port == CraftEngine.instance().serverPort()) {
			CraftEngine.instance().logger().warn(this.port + "is same as your server port");
			this.enable = false;
			return;
		}

		if (!this.enable)
			disable();
		else if (!oldEnable)
			enable();
		else if (!oldIp.equals(this.ip) || oldPort != this.port) {
			disable();
			enable();
		}
	}

	@Override
	public void disable() {
		if (this.server != null) {
			this.server.stop(0);
			this.server = null;
		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}