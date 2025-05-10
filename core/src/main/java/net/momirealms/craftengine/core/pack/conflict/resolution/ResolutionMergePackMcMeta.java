package net.momirealms.craftengine.core.pack.conflict.resolution;

import com.google.gson.*;
import net.momirealms.craftengine.core.pack.conflict.PathContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Key;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ResolutionMergePackMcMeta implements Resolution {
    public static final Factory FACTORY = new Factory();
    private final String description;

    public ResolutionMergePackMcMeta(String description) {
        this.description = description;
    }

    private static class MinMax {
        int min;
        int max;

        MinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    public static void mergeMcMeta(Path file1, Path file2, JsonElement customDescription) throws IOException {
        JsonElement elem1 = GsonHelper.readJsonFile(file1);
        JsonElement elem2 = GsonHelper.readJsonFile(file2);

        JsonObject merged = mergeValues(elem1.getAsJsonObject(), elem2.getAsJsonObject())
                .getAsJsonObject();

        if (merged.has("pack")) {
            JsonObject pack = merged.getAsJsonObject("pack");

            int pf1 = elem1.getAsJsonObject().getAsJsonObject("pack")
                    .getAsJsonPrimitive("pack_format").getAsInt();
            int pf2 = elem2.getAsJsonObject().getAsJsonObject("pack")
                    .getAsJsonPrimitive("pack_format").getAsInt();
            pack.addProperty("pack_format", Math.max(pf1, pf2));

            JsonElement sf1 = elem1.getAsJsonObject().getAsJsonObject("pack")
                    .get("supported_formats");
            JsonElement sf2 = elem2.getAsJsonObject().getAsJsonObject("pack")
                    .get("supported_formats");

            if (sf1 != null || sf2 != null) {
                MinMax mergedMinMax = getMergedMinMax(sf1, sf2, pf1, pf2);

                JsonElement mergedSf = createSupportedFormatsElement(
                        sf1 != null ? sf1 : sf2,
                        mergedMinMax.min,
                        mergedMinMax.max
                );

                pack.add("supported_formats", mergedSf);
            }

            if (customDescription != null) {
                pack.add("description", customDescription);
            } else {
                JsonPrimitive desc1 = elem1.getAsJsonObject().getAsJsonObject("pack")
                        .getAsJsonPrimitive("description");
                JsonPrimitive desc2 = elem2.getAsJsonObject().getAsJsonObject("pack")
                        .getAsJsonPrimitive("description");

                String mergedDesc = (desc1 != null ? desc1.getAsString() : "")
                        + (desc1 != null && desc2 != null ? "\n" : "")
                        + (desc2 != null ? desc2.getAsString() : "");

                if (!mergedDesc.isEmpty()) {
                    pack.addProperty("description", mergedDesc);
                }
            }
        }

        GsonHelper.writeJsonFile(merged, file1);
    }

    private static MinMax getMergedMinMax(JsonElement sf1, JsonElement sf2, int pf1, int pf2) {
        MinMax mm1 = parseSupportedFormats(sf1);
        MinMax mm2 = parseSupportedFormats(sf2);

        int finalMin = Math.min(mm1.min, mm2.min);
        int finalMax = Math.max(mm1.max, mm2.max);
        int pfMin = Math.min(pf1, pf2);
        int pfMax = Math.max(pf1, pf2);
        finalMin = Math.min(pfMin, finalMin);
        finalMax = Math.max(pfMax, finalMax);

        return new MinMax(finalMin, finalMax);
    }

    private static MinMax parseSupportedFormats(JsonElement supported) {
        if (supported == null || supported.isJsonNull()) {
            return new MinMax(Integer.MAX_VALUE, Integer.MIN_VALUE);
        }

        if (supported.isJsonPrimitive()) {
            int value = supported.getAsInt();
            return new MinMax(value, value);
        }

        if (supported.isJsonArray()) {
            JsonArray arr = supported.getAsJsonArray();
            int min = arr.get(0).getAsInt();
            int max = arr.get(arr.size()-1).getAsInt();
            return new MinMax(min, max);
        }

        JsonObject obj = supported.getAsJsonObject();
        int min, max;

        if (obj.has("min_inclusive")) {
            min = obj.get("min_inclusive").getAsInt();
        } else if (obj.has("max_inclusive")) {
            min = obj.get("max_inclusive").getAsInt();
        } else {
            throw new IllegalArgumentException("Invalid supported_formats format");
        }

        if (obj.has("max_inclusive")) {
            max = obj.get("max_inclusive").getAsInt();
        } else {
            max = min;
        }

        return new MinMax(min, max);
    }

    private static JsonElement createSupportedFormatsElement(
            JsonElement originalFormat,
            int min,
            int max) {

        if (originalFormat.isJsonPrimitive()) {
            return new JsonPrimitive(Math.max(min, max));

        } else if (originalFormat.isJsonArray()) {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(min));
            array.add(new JsonPrimitive(max));
            return array;

        } else if (originalFormat.isJsonObject()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("min_inclusive", min);
            obj.addProperty("max_inclusive", max);
            return obj;
        }

        return JsonNull.INSTANCE;
    }

    private static JsonElement mergeValues(JsonElement v1, JsonElement v2) {
        if (v1.isJsonObject() && v2.isJsonObject()) {
            JsonObject obj1 = v1.getAsJsonObject();
            JsonObject obj2 = v2.getAsJsonObject();
            JsonObject merged = new JsonObject();

            for (String key : obj1.keySet()) {
                if (obj2.has(key)) {
                    merged.add(key, mergeValues(obj1.get(key), obj2.get(key)));
                } else {
                    merged.add(key, obj1.get(key));
                }
            }
            for (String key : obj2.keySet()) {
                if (!merged.has(key)) {
                    merged.add(key, obj2.get(key));
                }
            }
            return merged;
        }

        if (v1.isJsonArray() && v2.isJsonArray()) {
            JsonArray arr1 = v1.getAsJsonArray();
            JsonArray arr2 = v2.getAsJsonArray();
            JsonArray merged = new JsonArray();
            merged.addAll(arr2);
            merged.addAll(arr1);
            return merged;
        }

        return v2.isJsonNull() ? v1 : v2;
    }

    @Override
    public void run(PathContext existing, PathContext conflict) {
        try {
            mergeMcMeta(existing.path(), conflict.path(), AdventureHelper.componentToJsonElement(AdventureHelper.miniMessage().deserialize(this.description)));
        } catch (IOException e) {
            CraftEngine.instance().logger().severe("Failed to merge pack.mcmeta when resolving file conflicts", e);
        }
    }

    @Override
    public Key type() {
        return Resolutions.MERGE_PACK_MCMETA;
    }

    public static class Factory implements ResolutionFactory {
        @Override
        public Resolution create(Map<String, Object> arguments) {
            String description = arguments.getOrDefault("description", "<gray>CraftEngine ResourcePack</gray>").toString();
            return new ResolutionMergePackMcMeta(description);
        }
    }
}
