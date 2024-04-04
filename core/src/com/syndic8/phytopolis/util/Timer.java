package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.syndic8.phytopolis.GameCanvas;

public class Timer {
    /**
     * The current number of minutes left
     */
    private int minutes;
    /**
     * The starting amount of seconds after the minutes
     */
    private int startSeconds;
    /**
     * The starting amount of minutes
     */
    private int startMinutes;
    /**
     * The seconds remaining after the minutes
     */
    private int seconds;
    /**
     * The system time at which the timer started
     */
    private long startTime;
    /**
     * Whether the timer is running
     */
    private boolean running;
    /**
     * Max number of points that can be acquired
     */
    public int numStars;
    /**
     * Number of points acquired
     */
    public int acquiredStars;

    /**
     * Max time passed to get the max number of points
     */
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