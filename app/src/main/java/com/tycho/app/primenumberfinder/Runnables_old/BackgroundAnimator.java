package com.tycho.app.primenumberfinder.Runnables_old;

import android.view.View;

import java.util.Random;

public class BackgroundAnimator implements Runnable{
    public boolean threadRunning = false;
    public boolean active = false;

    public View view;

    public boolean stopped = true;

    private float COLOR_INCREMENT;
    private float MAX_COLOR;
    private float MIN_COLOR;

    public int UPDATE_SPEED_FPS = 60;

    public float r, g, b;
    public boolean r0, g0, b0;

    public long lastUpdateTime;

    /*public static float mainBackgroundOffset
    public static boolean animateMainBackground = true;
    TODO: seperate rgb for each view, make custom object to make easier
    public static boolean animateCurrentTextView = true;*/

    public BackgroundAnimator(View view, float incr, float min, float max){
        this.view = view;
        this.COLOR_INCREMENT = incr;
        this.MIN_COLOR = min;
        this.MAX_COLOR = max;
    }

    @Override
    public void run(){
        while (threadRunning){
            while (active){
                stopped = false;

               /* if (System.currentTimeMillis() - lastUpdateTime >= (1000 / UPDATE_SPEED_FPS)){
                    lastUpdateTime = System.currentTimeMillis();*/

                if (r < MAX_COLOR && b0){
                    r += COLOR_INCREMENT;
                    g -= COLOR_INCREMENT;
                    b -= COLOR_INCREMENT;
                    if (r >= MAX_COLOR){
                        r0 = true;
                        b0 = false;
                        r = MAX_COLOR;
                        g = MIN_COLOR;
                        b = MIN_COLOR;
                    }
                }
                if (g < MAX_COLOR && r0){
                    r -= COLOR_INCREMENT;
                    g += COLOR_INCREMENT;
                    b -= COLOR_INCREMENT;
                    if (g >= MAX_COLOR){
                        g0 = true;
                        r0 = false;
                        r = MIN_COLOR;
                        g = MAX_COLOR;
                        b = MIN_COLOR;
                    }
                }
                if (b < MAX_COLOR && g0){
                    r -= COLOR_INCREMENT;
                    g -= COLOR_INCREMENT;
                    b += COLOR_INCREMENT;
                    if (b >= MAX_COLOR){
                        b0 = true;
                        g0 = false;
                        r = MIN_COLOR;
                        g = MIN_COLOR;
                        b = MAX_COLOR;
                    }
                }

                if (r < MIN_COLOR){
                    r = MIN_COLOR;
                }
                if (g < MIN_COLOR){
                    g = MIN_COLOR;
                }
                if (b < MIN_COLOR){
                    b = MIN_COLOR;
                }

                sendUpdateMessage();

                try{
                    Thread.sleep(32);
                }catch (Exception e){

                }
                //}
            }
            stopped = true;
        }
    }

    public void setRGB(float r, float g, float b){
        this.r = r;
        this.g = g;
        this.b = b;

        if (r >= MAX_COLOR){
            this.r0 = true;
        }
        if (g >= MAX_COLOR){
            this.g0 = true;
        }
        if (b >= MAX_COLOR){
            this.b0 = true;
        }

        //If all are false, pick a random one to make true
        if (!r0 && !g0 && !b0){
            Random random = new Random();
            switch (random.nextInt(2)){
                case 0:
                    r0 = true;
                    break;

                case 1:
                    g0 = true;
                    break;

                case 2:
                    b0 = true;
                    break;
            }
        }
    }

    private void sendUpdateMessage(){
        if (!stopped){
           /* Message message = MainActivity.backgroundAnimatorHandler.obtainMessage();
            message.obj = this;
            MainActivity.backgroundAnimatorHandler.sendMessage(message);*/
        }
    }
}
