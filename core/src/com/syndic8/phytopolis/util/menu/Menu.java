package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private final List<MenuItem> items;
    private final int length;
    private final float separation;

    public Menu(int len, float sep) {
        items = new ArrayList<>();
        length = len;
        separation = sep;
    }

    public void addItem(MenuItem item) {
        items.add(item);
    }

    public List<TextButton> gatherLabels() {
        List<TextButton> labels = new ArrayList<>();
        for (MenuItem item : items) {
            labels.addAll(item.gatherLabels());
        }
        return labels;
    }

    public int getLength() {
        return length;
    }

    public float getSeparation() {
        return separation;
    }

}
