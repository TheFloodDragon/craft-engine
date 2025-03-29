package net.momirealms.craftengine.core.plugin.locale;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class I18NData {
    public final Map<String, String> translations = new HashMap<>();

    public void addTranslations(Map<String, String> data) {
        translations.putAll(data);
    }

    public void addTranslation(String key, String value) {
        this.translations.put(key, value);
    }

    @Nullable
    public String translate(String key) {
        return this.translations.get(key);
    }

    @Override
    public String toString() {
        return "I18NData{" + translations + "}";
    }

    public static void merge(Map<String, I18NData> target, Map<String, I18NData> source) {
        source.forEach((key, value) -> {
            I18NData copy = new I18NData();
            copy.addTranslations(value.translations);
            target.merge(key, copy, (existing, newData) -> {
                existing.addTranslations(newData.translations);
                return existing;
            });
        });
    }
}