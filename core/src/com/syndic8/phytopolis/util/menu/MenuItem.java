package com.syndic8.phytopolis.util.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.syndic8.phytopolis.GameCanvas;
import com.syndic8.phytopolis.util.SharedAssetContainer;

public class MenuItem {

    private final TextButton label;

    /**
     * Initializes a MenuItem with a submenu.
     *
     * @param text  Text for the label.
     * @param index Index of this item in the menu.
     * @param sm    Submenu to switch to on click.
     * @param m     Menu containing this item.
     * @param ctr   Container containing the menu.
     * @param c     Game canvas.
     * @param align Alignment for this item.
     * @param scl   Font scale for this item.
     */
    public MenuItem(String text,
                    int index,
                    Menu sm,
                    Menu m,
                    MenuContainer ctr,
                    GameCanvas c,
                    int align,
                    float scl) {
        this(text, index, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ctr.setMenu(sm);
            }
        }, m, ctr, c, align, scl);
    }

    /**
     * Initializes a MenuItem with a submenu.
     *
     * @param text  Text for the label.
     * @param index Index of this item in the menu.
     * @param l     Listener to run on click.
     * @param m     Menu containing this item.
     * @param ctr   Container containing the menu.
     * @param c     Game canvas.
     * @param align Alignment for this item.
     * @param scl   Font scale for this item.
     */
    public MenuItem(String text,
                    int index,
                    ClickListener l,
                    Menu m,
                    MenuContainer ctr,
                    GameCanvas c,
                    int align,
                    float scl) {
        BitmapFont font = SharedAssetContainer.getInstance().getUIFont(scl);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.overFontColor = new Color(0.7f, 0.7f, 0.7f, 1);

        label = new TextButton(text, buttonStyle);
        label.setSize(400, label.getMaxHeight());
        label.getLabel().setAlignment(align);
        float ww = c.getTextViewport().getWorldWidth();
        float wh = c.getTextViewport().getWorldHeight();
        float xPos = ww / 2f - label.getWidth() / 2f + ww * m.getXOffset();
        float yPos =
                wh / 2f + (m.getLength() - 1f) * m.getSeparation() * wh / 2f -
                        index * m.getSeparation() * wh -
                        label.getHeight() / 2f + wh * m.getYOffset();
        label.setPosition(xPos, yPos);
        label.addListener(l);
    }

    /**
     * Initializes a MenuItem with a submenu.
     *
     * @param text  Text for the label.
     * @param index Index of this item in the menu.
     * @param sm    Submenu to switch to on click.
     * @param m     Menu containing this item.
     * @param ctr   Container containing the menu.
     * @param c     Game canvas.
     */
    public MenuItem(String text,
                    int index,
                    Menu sm,
                    Menu m,
                    MenuContainer ctr,
                    GameCanvas c) {
        this(text, index, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ctr.setMenu(sm);
            }
        }, m, ctr, c);
    }

    /**
     * Initializes a MenuItem with a submenu.
     *
     * @param text  Text for the label.
     * @param index Index of this item in the menu.
     * @param l     Listener to run on click.
     * @param m     Menu containing this item.
     * @param ctr   Container containing the menu.
     * @param c     Game canvas.
     */
    public MenuItem(String text,
                    int index,
                    ClickListener l,
                    Menu m,
                    MenuContainer ctr,
                    GameCanvas c) {
        this(text, index, l, m, ctr, c, m.getAlignment(), m.getFontScale());
    }

    /**
     * Returns this item's label.
     */
    public TextButton getLabel() {
        return label;
    }

}
