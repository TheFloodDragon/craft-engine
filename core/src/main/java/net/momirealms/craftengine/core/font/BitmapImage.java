package net.momirealms.craftengine.core.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.util.CharacterUtils;
import net.momirealms.craftengine.core.util.FormatUtils;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Supplier;

public class BitmapImage implements Supplier<JsonObject> {
    private final Key id;
    private final Key font;
    private final int height;
    private final int ascent;
    private final String file;
    private final int[][] codepointGrid;

    public BitmapImage(Key id, Key font, int height, int ascent, String file, int[][] codepointGrid) {
        this.id = id;
        this.font = font;
        this.height = height;
        this.ascent = ascent;
        this.file = file;
        this.codepointGrid = codepointGrid;
    }

    public String miniMessageAt(int row, int col) {
        int codepoint = codepointGrid[row][col];
        return FormatUtils.miniMessageFont(new String(Character.toChars(codepoint)), this.font.toString());
    }

    public int height() {
        return height;
    }

    public int ascent() {
        return ascent;
    }

    public String file() {
        return file;
    }

    public Key font() {
        return font;
    }

    public Key id() {
        return id;
    }

    public int[][] codepointGrid() {
        return codepointGrid.clone();
    }

    public int rows() {
        return codepointGrid.length;
    }

    public int columns() {
        return codepointGrid[0].length;
    }

    public int codepointAt(int row, int column) {
        if (!isValidCoordinate(row, column)) {
            throw new IndexOutOfBoundsException("Invalid index: (" + row + ", " + column + ") for image " + id());
        }
        return codepointGrid[row][column];
    }

    @SuppressWarnings("all")
    public Component componentAt(int row, int column) {
        int codepoint = codepointAt(row, column);
        return Component.text(new String(Character.toChars(codepoint))).font(net.kyori.adventure.key.Key.key(font().toString()));
    }

    public boolean isValidCoordinate(int row, int column) {
        return row >= 0 && row < codepointGrid.length && column >= 0 && column < codepointGrid[row].length;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BitmapImage image = (BitmapImage) object;
        return id.equals(image.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public JsonObject get() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "bitmap");
        jsonObject.addProperty("height", height);
        jsonObject.addProperty("ascent", ascent);
        jsonObject.addProperty("file", file);
        JsonArray charArray = new JsonArray();
        jsonObject.add("chars", charArray);
        for (int[] codepoints : codepointGrid) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int codepoint : codepoints) {
                stringBuilder.append(CharacterUtils.encodeCharsToUnicode(Character.toChars(codepoint)));
            }
            // to deceive Gson
            charArray.add(stringBuilder.toString());
        }
        return jsonObject;
    }
}
