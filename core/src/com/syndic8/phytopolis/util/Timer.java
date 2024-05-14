package com.syndic8.phytopolis.util;

public class Timer {

    //    /**
    //     * Max number of points that can be acquired
    //     */
    //    public int numStars;
    //    /**
    //     * Number of points acquired
    //     */
    //    public int acquiredStars;
    //    /**
    //     * Max time passed to get the max number of points
    //     */
    //    public int starTime;
    /**
     * Starting time for this Timer
     */
    private final float initialTime;
    /**
     * The time in seconds granted by collecting sun
     */
    public float sunTime;
    /**
     * The time in seconds that this timer has left
     */
    public float time;
    /**
     * Whether the timer is running
     */
    public boolean running;

    /**
     * Creates a new timer representing the amount of
     * seconds.
     * <p>
     *
     * @param initialTime The initial time represented in seconds
     */
    public Timer(float initialTime) {
        this(initialTime, 10);
    }

    public Timer(float initialTime, float sunTime) {
        this.initialTime = initialTime;
        time = initialTime;
        this.sunTime = sunTime;
    }

    public void reset(float initialTime) {
        time = initialTime;
    }

    /**
     * Starts the Timer by getting the current system time
     * and setting running to true
     */
    public void start() {
        running = true;
    }

    //    /**
    //     * Sets the max points in the timer
    //     *
    //     * @param stars
    //     */
    //    public void setStars(int stars) {
    //        numStars = stars;
    //    }

    //    /**
    //     * @return the acquired points in stars
    //     */
    //    public int getAcquiredStars() {
    //        if (!running) {
    //            int elapsedSeconds = (startSeconds + startMinutes * 60) -
    //                    (seconds + minutes * 60);
    //
    //            acquiredStars = numStars - (elapsedSeconds / starTime);
    //
    //            return Math.max(acquiredStars, 0);
    //        }
    //        return 0;
    //    }

    /**
     * Updates the timer by displaying the time left
     */
    public void updateTime(float dt) {
        if (running) {
            time -= dt;
            if (time <= 0) {
                running = false;
                time = 0;
            }
        }

    }

    public void addTime() {
        addTime(sunTime);
    }

    public void addTime(float t) {
        time += t;
    }

    public String toString() {
        return String.format("%02d:%02d", getMinutes(), getSeconds());
    }

    public int getMinutes() {
        return (int) (time / 60);
    }

    public int getSeconds() {
        return (int) (time % 60);
    }

    public boolean isOver() {
        return !running;
    }

    public void pause() {
        running = false;
    }

}