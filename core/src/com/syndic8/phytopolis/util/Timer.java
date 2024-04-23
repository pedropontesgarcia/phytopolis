package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.syndic8.phytopolis.GameCanvas;

public class Timer {
    private SpriteBatch batch;

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

    /**
     * Creates a new timer representing the amount of
     * seconds passed in minutes and seconds.
     * <p>
     * @param second The total time represented in seconds
     */
    public Timer(int second){
        startSeconds = second % 60;
        startMinutes = second / 60;
        seconds = startSeconds;
        minutes = startMinutes;
    }
    /**
     * Creates a new timer representing the amount of
     * seconds passed in minutes and seconds.
     * <p>
     * @param second The total time represented in seconds
     * @param stars  The max number of points to acquire
     * @param starTime Max time passed to get the max number of points
     */
    public Timer(int second, int stars, int starTime){
        startSeconds = second % 60;
        startMinutes = second / 60;
        seconds = startSeconds;
        minutes = startMinutes;
        numStars = stars;
        this.starTime = starTime;
        batch = new SpriteBatch();


    }

    /**
     * Starts the Timer by getting the current system time
     * and setting running to true
     */
    public void startTimer(){
        startTime = TimeUtils.millis();
        running = true;
    }

    /**
     * Sets the max points in the timer
     * @param stars
     */
    public void setStars(int stars){
        numStars = stars;
    }

    /**
     * @return the acquired points in stars
     */
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

    /**
     * Updates the timer by displaying the time left
     */
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

    /**
     *
     * @param canvas The canvas to draw with
     * @param font The font to use
     * @param color The color to render the time with
     * @param x The x-coordinate of the lower-left corner
     * @param y The y-coordinate of the lower-left corner
     */
    public void displayTime(GameCanvas canvas,
                            BitmapFont font, Color color,
                            float x, float y){
        String timeDisplay = String.format("%02d:%02d", minutes, seconds);
        //String timeDisplay = "" + minutes + " : " + seconds;
//        font.setColor(color);
//        canvas.drawText(timeDisplay, font, x, y);
        canvas.drawTime(font, timeDisplay, color, x, y);
    }

    /**
     *
     * @param canvas The canvas to draw with
     * @param font The font to use
     * @param color The color to render the time with
     * @param x The x-coordinate of the lower-left corner
     * @param y The y-coordinate of the lower-left corner
     * @param scale The scale at which to render the text at
     */
    public void displayTime(GameCanvas canvas,
                            BitmapFont font, Color color,
                            float x, float y,
                            Vector2 scale){
        font.getData().setScale(scale.x, scale.y);
        displayTime(canvas, font, color, x, y);
    }
    public int getMinutes(){
        return minutes;
    }
    public int getSeconds(){
        return seconds;
    }

    public boolean isRunning() {
        return running;
    }
}