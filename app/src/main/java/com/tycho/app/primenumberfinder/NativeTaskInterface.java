package com.tycho.app.primenumberfinder;

import java.util.UUID;

import easytasks.Task;
import easytasks.TaskListener;

/**
 * This is used as an interface between Java tasks and tasks written in native code.
 */
public interface NativeTaskInterface {

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
