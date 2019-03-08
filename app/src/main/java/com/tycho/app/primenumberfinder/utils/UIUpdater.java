package com.tycho.app.primenumberfinder.utils;

import android.os.Handler;

import easytasks.Task;

public abstract class UIUpdater extends Task {

    private final Handler handler;

    private int refreshRate = 30;

    public UIUpdater(final Handler handler){
        this.handler = handler;
    }

    @Override
    protected void run() {
        while (isRunning()) {

            handler.post(this::update);

            try {
                Thread.sleep(1000 / refreshRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    protected abstract void update();

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }
}
