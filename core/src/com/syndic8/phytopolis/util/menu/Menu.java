package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private final List<MenuItem> items;
    private final int length;
    private final float separation;
    private final float xOffset;
    private final float yOffset;
    private final int alignment;

    public Menu(int len, float sep) {
        this(len, sep, 0, 0, Align.center);
    }

    public Menu(int len, float sep, float xOff, float yOff, int align) {
        xOffset = xOff;
        yOffset = yOff;
        items = new ArrayList<>();
        length = len;
        separation = sep;
        alignment = align;
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

}
