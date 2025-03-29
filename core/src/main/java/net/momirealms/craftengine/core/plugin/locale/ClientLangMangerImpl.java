package net.momirealms.craftengine.core.plugin.locale;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ClientLangMangerImpl implements ClientLangManager {
    private final Plugin plugin;
    private final Map<String, I18NData> i18nData = new HashMap<>();

    public ClientLangMangerImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        this.i18nData.clear();
    }

    @Override
    public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
        String langId = id.value().toLowerCase(Locale.ROOT);

        Map<String, String> sectionData = section.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));

        addTranslation(langId, sectionData);
    }

    @Override
    public Map<String, I18NData> langData() {
        return Collections.unmodifiableMap(this.i18nData);
    }

    @Override
    public void addTranslation(String langId, Map<String, String> translations) {
        if ("all".equals(langId)) {
            ALL_LANG.forEach(lang -> this.i18nData.computeIfAbsent(lang, k -> new I18NData())
                    .addTranslations(translations));
            return;
        }

        if (ALL_LANG.contains(langId)) {
            this.i18nData.computeIfAbsent(langId, k -> new I18NData())
                    .addTranslations(translations);
            return;
        }

        List<String> langCountries = LOCALE_2_COUNTRIES.getOrDefault(langId, Collections.emptyList());
        for (String lang : langCountries) {
            this.i18nData.computeIfAbsent(langId + "_" + lang, k -> new I18NData())
                    .addTranslations(translations);
        }
    }
}
