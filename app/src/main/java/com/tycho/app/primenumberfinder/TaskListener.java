package com.tycho.app.primenumberfinder;

/**
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public interface TaskListener{

    void onTaskStarted();

    void onTaskPaused();

    void onTaskResumed();

    void onTaskStopped();

    void onTaskFinished();

    void onProgressChanged(final float percent);
}
