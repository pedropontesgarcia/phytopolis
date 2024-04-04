package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.syndic8.phytopolis.GameCanvas;

public class Timer {
    private int minutes;

    private int startSeconds;
    private int startMinutes;
    private int seconds;

    private long startTime;

    private boolean running;
    public int numStars;

    public int acquiredStars;

    public int starTime;
    public Timer(int second){
        startSeconds = second % 60;
        startMinutes = second / 60;
        seconds = startSeconds;
        minutes = startMinutes;
    }
    public Timer(int second, int stars, int starTime){
        new Timer(second);
        numStars = stars;
        this.starTime = starTime;

    }
    public void startTimer(){
        startTime = TimeUtils.millis();
        running = true;
    }

    public void setStars(int stars){
        numStars = stars;
    }

    public int getAcquiredStars(){
        if (!running){
            int elapsedSeconds =
                    (startSeconds + startMinutes * 60) - (seconds + minutes * 60);

            acquiredStars =
                    numStars - (elapsedSeconds/starTime);

            return Math.max(acquiredStars, 0);
        }

        return 0;

    }

    public void updateTime(){
        if (running) {
            long elapsedTime = TimeUtils.timeSinceMillis(startTime);
            int totalSeconds = (int) (elapsedTime / 1000);
            int remainingSeconds =
                    startSeconds + startMinutes * 60 - totalSeconds;
            if (remainingSeconds <= 0) {
                running = false;
                remainingSeconds = 0;
            }
            minutes = remainingSeconds / 60;
            seconds = remainingSeconds % 60;
        }
    }

    public void displayTime(GameCanvas canvas,
                            BitmapFont font, Color color,
                            float x, float y){
        String timeDisplay = String.format("%02d:%02d", minutes, seconds);
        font.setColor(color);
        canvas.drawText(timeDisplay, font, x, y);
    }
    public void displayTime(GameCanvas canvas,
                            BitmapFont font, Color color,
                            float x, float y,
                            Vector2 scale){
        font.getData().setScale(scale.x, scale.y);
        displayTime(canvas, font, color, x, y);
    }

}