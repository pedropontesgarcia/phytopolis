package com.syndic8.phytopolis.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.syndic8.phytopolis.GameCanvas;

public class Timer {
    private int minutes;
    private int seconds;
    private int elapsedTime;

    private int startTime;

    private String timeDisplay;
    public Timer(int second){
        seconds = second % 60;
        minutes = second / 60;
    }

    public void startTimer(){
        startTime = (int) TimeUtils.millis();
    }

    public void updateTime(){

    }

    public void displayTime(GameCanvas canvas,
                            BitmapFont font, Color color,
                            float x, float y){

    }
    public void displayTime(GameCanvas canvas,
                            BitmapFont font, Color color,
                            float x, float y,
                            Vector2 scale){
        font.getData().setScale(scale.x, scale.y);
        displayTime(canvas, font, color, x, y);
    }


}