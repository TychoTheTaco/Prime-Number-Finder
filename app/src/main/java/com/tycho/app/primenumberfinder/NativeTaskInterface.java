package com.tycho.app.primenumberfinder;

import java.util.UUID;

import easytasks.Task;
import easytasks.TaskListener;

/**
 * This is used as an interface between Java tasks and tasks written in native code.
 */
public interface NativeTaskInterface {

    UUID getId();

    // Lifecycle methods
    void start();
    Thread startOnNewThread();
    void pause();
    void pauseAndWait() throws InterruptedException;
    void resume();
    void resumeAndWait() throws InterruptedException;
    void stop();
    void stopAndWait() throws InterruptedException;
    //void finish();

    // Listeners
    void addTaskListener(final TaskListener listener);
    boolean removeTaskListener(final TaskListener listener);

    // Time methods
    long getStartTime();
    long getEndTime();
    long getElapsedTime();
    long getEstimatedTimeRemaining();

    // State methods
    Task.State getState();
    float getProgress();
}
