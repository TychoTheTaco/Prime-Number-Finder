package com.tycho.app.primenumberfinder;

import android.util.Log;

import com.tycho.app.primenumberfinder.utils.OneToOneMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskListener;

public abstract class NativeTask implements ITask {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = NativeTask.class.getSimpleName();

    // Load native library
    static {
        System.loadLibrary("native-utils");
    }

    /**
     * Pointer to the native Task object.
     */
    protected final long native_task_pointer;

    public NativeTask(final long native_task_pointer){
        this.native_task_pointer = native_task_pointer;
    }

    @Override
    public UUID getId() {
        return UUID.randomUUID();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Life-cycle methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void start() {
        nativeStart(native_task_pointer);
    }

    @Override
    public Thread startOnNewThread() {
        nativeStartOnNewThread(native_task_pointer);
        //TODO: Don't just return a new thread.
        return new Thread();
    }

    @Override
    public void pause() {
        nativePause(native_task_pointer);
    }

    @Override
    public void pauseAndWait() {
        nativePauseAndWait(native_task_pointer);
    }

    @Override
    public void resume() {
        nativeResume(native_task_pointer);
    }

    @Override
    public void resumeAndWait() {
        nativeResumeAndWait(native_task_pointer);
    }

    @Override
    public void stop() {
        nativeStop(native_task_pointer);
    }

    @Override
    public void stopAndWait() {
        nativeStopAndWait(native_task_pointer);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Task listeners
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This map stores each {@link TaskListener} with a unique ID which is passed to the native add/remove listener functions. This allows us to uniquely
     * identify the same object between Java and native code.
     */
    private OneToOneMap<TaskListener, UUID> taskListenerMap = new OneToOneMap<>();

    @Override
    public void addTaskListener(final TaskListener listener) {
        final UUID id = UUID.randomUUID();
        taskListenerMap.put(listener, id);
        nativeAddTaskListener(native_task_pointer, listener, id.toString());
    }

    @Override
    public boolean removeTaskListener(final TaskListener listener) {
        final UUID id = taskListenerMap.get(listener);
        if (id != null){
            taskListenerMap.remove(listener);
            return nativeRemoveTaskListener(native_task_pointer, id.toString());
        }
        return false;
    }

    private void sendOnTaskStarted(final String listenerId){
        taskListenerMap.getKey(UUID.fromString(listenerId)).onTaskStarted(this);
    }

    private void sendOnTaskPausing(final String listenerId){
        taskListenerMap.getKey(UUID.fromString(listenerId)).onTaskPausing(this);
    }

    private void sendOnTaskPaused(final String listenerId){
        taskListenerMap.getKey(UUID.fromString(listenerId)).onTaskPaused(this);
    }

    private void sendOnTaskResuming(final String listenerId){
        taskListenerMap.getKey(UUID.fromString(listenerId)).onTaskResuming(this);
    }

    private void sendOnTaskResumed(final String listenerId){
        taskListenerMap.getKey(UUID.fromString(listenerId)).onTaskResumed(this);
    }

    private void sendOnTaskStopping(final String listenerId){
        taskListenerMap.getKey(UUID.fromString(listenerId)).onTaskStopping(this);
    }

    private void sendOnTaskStopped(final String listenerId){
        if (taskListenerMap.getKey(UUID.fromString(listenerId)) == null){
            //TODO: Sometimes this happens
            Log.w(TAG, "Listener was null!");
            return;
        }
        taskListenerMap.getKey(UUID.fromString(listenerId)).onTaskStopped(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Time methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public long getStartTime() {
        return nativeGetStartTime(native_task_pointer);
    }

    @Override
    public long getEndTime() {
        return nativeGetEndTime(native_task_pointer);
    }

    @Override
    public long getElapsedTime() {
        return nativeGetElapsedTime(native_task_pointer);
    }

    @Override
    public long getEstimatedTimeRemaining() {
        return nativeGetEstimatedTimeRemaining(native_task_pointer);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // State methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Task.State getState() {
        return Task.State.values()[nativeGetState(native_task_pointer)];
    }

    @Override
    public float getProgress() {
        return nativeGetProgress(native_task_pointer);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Native methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private native void nativeStart(long native_task_pointer);
    private native void nativeStartOnNewThread(long native_task_pointer);
    private native void nativePause(long native_task_pointer);
    private native void nativePauseAndWait(long native_task_pointer);
    private native void nativeResume(long native_task_pointer);
    private native void nativeResumeAndWait(long native_task_pointer);
    private native void nativeStop(long native_task_pointer);
    private native void nativeStopAndWait(long native_task_pointer);

    private native void nativeAddTaskListener(long native_task_pointer, TaskListener listener, String id);
    private native boolean nativeRemoveTaskListener(long native_task_pointer, String id);

    private native long nativeGetStartTime(long native_task_pointer);
    private native long nativeGetEndTime(long native_task_pointer);
    private native long nativeGetElapsedTime(long native_task_pointer);
    private native long nativeGetEstimatedTimeRemaining(long native_task_pointer);

    private native int nativeGetState(long native_task_pointer);
    private native float nativeGetProgress(long native_task_pointer);
}
