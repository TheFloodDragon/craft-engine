package net.momirealms.craftengine.core.pack.host;

import net.momirealms.craftengine.core.plugin.Reloadable;

import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractPackHost implements Reloadable {
	protected boolean enable;
	protected String url;
	protected Path packPath;
	protected Map<String, Object> properties;

	public void setConfig(Path packPath, Map<String, Object> properties) {
		this.packPath = packPath;
		this.properties = properties;
	}

	public boolean isEnable() {
		return enable;
	}

	public String url() {
		return url;
	};

	public abstract boolean isSingleton();
}
