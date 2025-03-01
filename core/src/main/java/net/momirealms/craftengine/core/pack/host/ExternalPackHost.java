package net.momirealms.craftengine.core.pack.host;

public class ExternalPackHost extends AbstractPackHost {

	@Override
	public void load() {
		this.enable = (boolean) properties.getOrDefault("enable", false);
		url = (String) properties.get("url");
		if (url == null) {
			enable = false;
		}
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
