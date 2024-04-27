package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.SharedAssetContainer;

public class MenuItem {

    private final TextButton label;
    private final Menu submenu;
    private final MenuContainer container;

    /**
     * Initializes a MenuItem with a listener.
     *
     * @param text  Text for the label.
     * @param sep   Separation between items in percentage over 1.
     * @param index Index of this item in the menu.
     * @param len   Length of the menu.
     * @param l     Listener to run on click.
     * @param ctr   Container of the menu.
     * @param c     Game canvas.
     */
    public MenuItem(String text,
                    float sep,
                    int index,
                    int len,
                    ClickListener l,
                    MenuContainer ctr,
                    GameCanvas c) {
        this(text, sep, index, len, l, null, ctr, c, 0, 0, Align.center);
    }

    /**
     * Initializes a MenuItem.
     *
     * @param text    Text for the label.
     * @param sep     Separation between items in percentage over 1.
     * @param index   Index of this item in the menu.
     * @param len     Length of the menu.
     * @param l       Listener to run on click.
     * @param sm      Submenu to switch to on click.
     * @param ctr     Container of the menu.
     * @param c       Game canvas.
     * @param xOffset Menu x offset in percentage over 1.
     * @param yOffset Menu y offset in percentage over 1.
     * @param align   Alignment of label.
     */
    private MenuItem(String text,
                     float sep,
                     int index,
                     int len,
                     ClickListener l,
                     Menu sm,
                     MenuContainer ctr,
                     GameCanvas c,
                     float xOffset,
                     float yOffset,
                     int align) {
        container = ctr;
        submenu = sm;
        BitmapFont font = SharedAssetContainer.getInstance().uiFont;
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.overFontColor = new Color(0.7f, 0.7f, 0.7f, 1);

        label = new TextButton(text, buttonStyle);
        label.getLabel().setAlignment(align);
        float ww = c.getTextViewport().getWorldWidth();
        float wh = c.getTextViewport().getWorldHeight();
        float xPos = ww / 2f - label.getWidth() / 2f + ww * xOffset;
        float yPos = wh / 2f + (len - 1f) * sep * wh / 2f - index * sep * wh -
                label.getHeight() / 2f + wh * yOffset;
        label.setPosition(xPos, yPos);
        label.addListener(l);
    }

    /**
     * Initializes a MenuItem with a submenu and offset.
     *
     * @param text    Text for the label.
     * @param sep     Separation between items in percentage over 1.
     * @param index   Index of this item in the menu.
     * @param len     Length of the menu.
     * @param sm      Submenu to switch to on click.
     * @param ctr     Container of the menu.
     * @param c       Game canvas.
     * @param xOffset Menu x offset in percentage over 1.
     * @param yOffset Menu y offset in percentage over 1.
     * @param align   Alignment of label.
     */
    public MenuItem(String text,
                    float sep,
                    int index,
                    int len,
                    Menu sm,
                    MenuContainer ctr,
                    GameCanvas c,
                    float xOffset,
                    float yOffset,
                    int align) {
        this(text, sep, index, len, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ctr.setMenu(sm);
            }
        }, sm, ctr, c, xOffset, yOffset, align);
    }

    /**
     * Initializes a MenuItem with a listener and offset.
     *
     * @param text    Text for the label.
     * @param sep     Separation between items in percentage over 1.
     * @param index   Index of this item in the menu.
     * @param len     Length of the menu.
     * @param l       Listener to run on click.
     * @param ctr     Container of the menu.
     * @param c       Game canvas.
     * @param xOffset Menu x offset in percentage over 1.
     * @param yOffset Menu y offset in percentage over 1.
     * @param align   Alignment of label.
     */
    public MenuItem(String text,
                    float sep,
                    int index,
                    int len,
                    ClickListener l,
                    MenuContainer ctr,
                    GameCanvas c,
                    float xOffset,
                    float yOffset,
                    int align) {
        this(text, sep, index, len, l, null, ctr, c, xOffset, yOffset, align);
    }

    /**
     * Initializes a MenuItem with a submenu.
     *
     * @param text  Text for the label.
     * @param sep   Separation between items in percentage over 1.
     * @param index Index of this item in the menu.
     * @param len   Length of the menu.
     * @param sm    Submenu to switch to on click.
     * @param ctr   Container of the menu.
     * @param c     Game canvas.
     */
    public MenuItem(String text,
                    float sep,
                    int index,
                    int len,
                    Menu sm,
                    MenuContainer ctr,
                    GameCanvas c) {
        this(text, sep, index, len, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ctr.setMenu(sm);
            }
        }, sm, ctr, c, 0, 0, Align.center);
    }

    /**
     * Returns this item's label.
     */
    public TextButton getLabel() {
        return label;
    }

}
