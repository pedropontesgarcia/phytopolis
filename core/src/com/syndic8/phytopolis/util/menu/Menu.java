package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.util.PooledList;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    public static final float DEFAULT_WIDTH = 400f;
    private final PooledList<MenuItem> items;
    private final int length;
    private final float separation;
    private final float xOffset;
    private final float yOffset;
    private final int alignment;
    private final float fontScale;
    private final float width;

    public Menu(int len, float sep) {
        this(len, sep, 0, 0, 1, Align.center, DEFAULT_WIDTH);
    }

    public Menu(int len,
                float sep,
                float xOff,
                float yOff,
                float scl,
                int align,
                float w) {
        length = len;
        separation = sep;
        xOffset = xOff;
        yOffset = yOff;
        fontScale = scl;
        alignment = align;
        width = w;
        items = new PooledList<>();
    }

    public Menu(int len, float sep, float scl) {
        this(len, sep, 0, 0, scl, Align.center, DEFAULT_WIDTH);
    }

    public PooledList<MenuItem> getItems() {
        return items;
    }

    public void addItem(MenuItem item) {
        items.add(item);
    }

    public List<TextButton> gatherLabels() {
        List<TextButton> labels = new ArrayList<>();
        for (MenuItem item : items) {
            labels.add(item.getLabel());
        }
        return labels;
    }

    public int getLength() {
        return length;
    }

    public float getSeparation() {
        return separation;
    }

    public float getXOffset() {
        return xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public int getAlignment() {
        return alignment;
    }

    public float getFontScale() {
        return fontScale;
    }

    public float getWidth() {
        return width;
    }

}
