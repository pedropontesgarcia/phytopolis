package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.util.PooledList;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic menu, to be used with a MenuContainer and MenuItems.
 */
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

    /**
     * Creates a menu with a given length and separation between items. The
     * other parameters are set to the following defaults:
     * <ul>
     *      <li>Font scale: 1</li>
     *      <li>Text alignment: centered</li>
     *      <li>Width: DEFAULT_WIDTH</li>
     *      <li>x offset: 0</li>
     *      <li>y offset: 0</li>
     * </ul>
     *
     * @param len length of the menu.
     * @param sep separation between items.
     */
    public Menu(int len, float sep) {
        this(len, sep, 0, 0, 1, Align.center, DEFAULT_WIDTH);
    }

    /**
     * Creates a menu with the given parameters.
     *
     * @param len   length of the menu.
     * @param sep   separation between items.
     * @param xOff  x offset.
     * @param yOff  y offset.
     * @param scl   font scale.
     * @param align text alignment
     * @param w     width.
     */
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

    /**
     * Creates a menu with a given length, separation between items, and font
     * scale. The other parameters are set to the following defaults:
     * <ul>
     *      <li>Text alignment: centered</li>
     *      <li>Width: DEFAULT_WIDTH</li>
     *      <li>x offset: 0</li>
     *      <li>y offset: 0</li>
     * </ul>
     *
     * @param len length of the menu.
     * @param sep separation between items.
     */
    public Menu(int len, float sep, float scl) {
        this(len, sep, 0, 0, scl, Align.center, DEFAULT_WIDTH);
    }

    /**
     * @return the items of the menu, as a list.
     */
    public PooledList<MenuItem> getItems() {
        return items;
    }

    /**
     * Adds item to menu.
     *
     * @param item the item to add.
     */
    public void addItem(MenuItem item) {
        items.add(item);
    }

    /**
     * @return the labels from the menu items.
     */
    public List<TextButton> gatherLabels() {
        List<TextButton> labels = new ArrayList<>();
        for (MenuItem item : items) {
            labels.add(item.getLabel());
        }
        return labels;
    }

    /**
     * @return the number of items in the menu.
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the separation between items in the menu.
     */
    public float getSeparation() {
        return separation;
    }

    /**
     * @return the x offset of the menu.
     */
    public float getXOffset() {
        return xOffset;
    }

    /**
     * @return the y offset of the menu.
     */
    public float getYOffset() {
        return yOffset;
    }

    /**
     * @return the alignment of the menu.
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * @return the font scale of the menu.
     */
    public float getFontScale() {
        return fontScale;
    }

    /**
     * @return the width of the menu.
     */
    public float getWidth() {
        return width;
    }

}
