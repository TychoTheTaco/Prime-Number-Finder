package com.tycho.app.primenumberfinder;

import java.util.UUID;

import easytasks.Task;
import easytasks.TaskListener;

public interface ITask {

    UUID getId();

    void start();
    Thread startOnNewThread();
    void pause();
    void pauseAndWait() throws InterruptedException;
    void resume();
    void resumeAndWait() throws InterruptedException;
    void stop();
    void stopAndWait() throws InterruptedException;

    float getProgress();
    long getElapsedTime();
    long getEstimatedTimeRemaining();

    long getStartTime();
    long getEndTime();

    void addTaskListener(TaskListener listener);
    boolean removeTaskListener(TaskListener listener);

    Task.State getState();
}
