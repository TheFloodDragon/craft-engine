package net.momirealms.craftengine.core.pack.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.item.setting.EquipmentData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class EquipmentGeneration implements Supplier<JsonObject> {
    private final List<Layer> humanoid;
    private final List<Layer> humanoidLeggings;
    private final List<Layer> llamaBody;
    private final List<Layer> horseBody;
    private final List<Layer> wolfBody;
    private final List<Layer> wings;

    private final EquipmentData modernData;
    private final int trim;

    public EquipmentGeneration(List<Layer> humanoid,
                               List<Layer> humanoidLeggings,
                               List<Layer> llamaBody,
                               List<Layer> horseBody,
                               List<Layer> wolfBody,
                               List<Layer> wings,
                               EquipmentData modernData,
                               int trim) {
        this.humanoid = humanoid;
        this.humanoidLeggings = humanoidLeggings;
        this.llamaBody = llamaBody;
        this.horseBody = horseBody;
        this.wolfBody = wolfBody;
        this.wings = wings;
        this.trim = trim;
        this.modernData = modernData;
    }

    public EquipmentData modernData() {
        return modernData;
    }

    public int trim() {
        return trim;
    }

    public List<Layer> humanoid() {
        return humanoid;
    }

    public List<Layer> humanoidLeggings() {
        return humanoidLeggings;
    }

    public List<Layer> llamaBody() {
        return llamaBody;
    }

    public List<Layer> horseBody() {
        return horseBody;
    }

    public List<Layer> wolfBody() {
        return wolfBody;
    }

    public List<Layer> wings() {
        return wings;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EquipmentGeneration that)) return false;
        return trim == that.trim && Objects.equals(humanoid, that.humanoid) && Objects.equals(humanoidLeggings, that.humanoidLeggings) && Objects.equals(llamaBody, that.llamaBody) && Objects.equals(horseBody, that.horseBody) && Objects.equals(wolfBody, that.wolfBody) && Objects.equals(wings, that.wings) && Objects.equals(modernData, that.modernData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(humanoid, humanoidLeggings, llamaBody, horseBody, wolfBody, wings, modernData, trim);
    }

    @Override
    public JsonObject get() {
        JsonObject jsonObject = new JsonObject();
        JsonObject layersJson = new JsonObject();
        jsonObject.add("layers", layersJson);
        setLayerJson(layersJson, humanoid(), "humanoid");
        setLayerJson(layersJson, humanoidLeggings(), "humanoid_leggings");
        setLayerJson(layersJson, llamaBody(), "llama_body");
        setLayerJson(layersJson, horseBody(), "horse_body");
        setLayerJson(layersJson, wolfBody(), "wolf_body");
        setLayerJson(layersJson, wings(), "wings");
        return jsonObject;
    }

    private void setLayerJson(JsonObject layersJson, List<Layer> layers, String key) {
        if (layers.isEmpty()) return;
        JsonArray layersArray = new JsonArray();
        for (Layer layer : layers) {
            layersArray.add(layer.get());
        }
        layersJson.add(key, layersArray);
    }

    public record Layer(String texture, boolean dyeable) implements Supplier<JsonObject> {

        @NotNull
        public static List<Layer> fromConfig(Object obj) {
            if (obj instanceof String texture) {
                return List.of(new Layer(texture, false));
            } else if (obj instanceof Map<?, ?> map) {
                String texture = map.get("texture").toString();
                return List.of(new Layer(texture, map.containsKey("dyeable")));
            } else if (obj instanceof List<?> list) {
                List<Layer> layers = new ArrayList<>();
                for (Object inner : list) {
                    layers.addAll(fromConfig(inner));
                }
                return layers;
            } else {
                return List.of();
            }
        }

        @Override
        public JsonObject get() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("texture", texture);
            if (dyeable) {
                jsonObject.add("dyeable", new JsonObject());
            }
            return jsonObject;
        }
    }
}
