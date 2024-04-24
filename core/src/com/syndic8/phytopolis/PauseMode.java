package com.syndic8.phytopolis;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.syndic8.phytopolis.util.FadingScreen;
import com.syndic8.phytopolis.util.ScreenListener;

public class PauseMode extends FadingScreen implements Screen {

    private final Rectangle bounds;
    private ScreenListener listener;
    private boolean ready;
    private boolean active;
    private GameCanvas canvas;

    public PauseMode() {
        ready = false;
        bounds = new Rectangle(0, 0, 16, 9);
    }

    public void setCanvas(GameCanvas c) {
        canvas = c;
    }

    @Override
    public void show() {
        active = true;
        ready = false;
        fadeIn(0.1f);
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (listener != null && ready && isFadeDone()) {
                listener.exitScreen(this, 0);
            }
        }

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void update(float delta) {
        super.update(delta);
        InputController.getInstance().readInput(bounds, Vector2.Zero.add(1, 1));
        if (InputController.getInstance().didExit() && !ready) {
            ready = true;
            fadeOut(0.1f);
        }
    }

    public void draw() {
        canvas.clear();
        canvas.begin();
        canvas.end();
        super.draw(canvas);
    }

    public void setScreenListener(ScreenListener l) {
        listener = l;
    }

}
