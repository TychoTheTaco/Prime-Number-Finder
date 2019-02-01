package com.tycho.app.primenumberfinder;

import easytasks.Task;

public interface ITask {

    void start();
    void startOnNewThread();
    void pause();
    void pauseAndWait();
    void resume();
    void resumeAndWait();
    void stop();
    void stopAndWait();

    long getElapsedTime();

    Task.State getState();
}
