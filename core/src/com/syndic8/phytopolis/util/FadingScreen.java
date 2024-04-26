package com.syndic8.phytopolis.util;

import com.badlogic.gdx.math.Interpolation;
import com.syndic8.phytopolis.GameCanvas;

public abstract class FadingScreen {

    private float tmr;
    private float fadeDuration;
    private float alpha;
    private Fade fadeState;
    private boolean done;
    private float volume;

    public FadingScreen() {
        tmr = 0;
        fadeState = Fade.HIDDEN;
        done = false;
        volume = 1;
    }

    protected void update(float deltaTime) {
        // Clamp between 0 and 1
        float linearAlpha = Math.max(Math.min(tmr / fadeDuration, 1), 0);
        float interpolatedAlpha = Interpolation.fade.apply(0, 1, linearAlpha);
        switch (fadeState) {
            case FADE_IN:
                alpha = 1 - interpolatedAlpha;
                if (alpha == 0) {
                    done = true;
                    fadeState = Fade.SHOWN;
                }
                break;
            case FADE_OUT:
                alpha = interpolatedAlpha;
                if (alpha == 1) {
                    done = true;
                    fadeState = Fade.HIDDEN;
                }
                break;
            default:
                alpha = 0;
                break;
        }
        volume = Math.max(Math.min(1 - alpha, 1), 0);
        if (fadeState != Fade.HIDDEN && fadeState != Fade.SHOWN) {
            tmr += deltaTime;
        }
    }

    protected void draw(GameCanvas c) {
        c.beginShape();
        c.getShapeRenderer().setColor(0, 0, 0, alpha);
        c.getShapeRenderer().rect(0, 0, c.getWidth(), c.getHeight());
        c.endShape();
    }

    public void fadeIn(float seconds) {
        fadeDuration = seconds;
        fadeState = Fade.FADE_IN;
        tmr = 0;
        done = false;
    }

    public void fadeOut(float seconds) {
        fadeDuration = seconds;
        fadeState = Fade.FADE_OUT;
        tmr = 0;
        done = false;
    }

    public float getVolume() {
        return volume;
    }

    protected boolean isFadeDone() {
        return done;
    }

    public Fade getFadeState() {
        return fadeState;
    }

    public enum Fade {HIDDEN, FADE_IN, SHOWN, FADE_OUT}

}
