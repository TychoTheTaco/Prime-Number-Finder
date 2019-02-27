package com.tycho.app.primenumberfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import easytasks.Task;
import easytasks.TaskListener;

public abstract class NativeTask implements NativeTaskInterface {

    static {
        System.loadLibrary("native-utils");
    }

    private Map<TaskListener, UUID> taskListenerMap = new HashMap<>();

    @Override
    public UUID getId() {
        return UUID.randomUUID();
    }

    @Override
    public float getProgress() {
        return nativeGetProgress(native_task_pointer);
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public long getEndTime() {
        return 0;
    }

    @Override
    public long getEstimatedTimeRemaining() {
        return 0;
    }

    @Override
    public void addTaskListener(TaskListener listener) {
        final UUID id = UUID.randomUUID();
        taskListenerMap.put(listener, id);
        nativeAddTaskListener(native_task_pointer, listener, id.toString());
    }

    @Override
    public boolean removeTaskListener(TaskListener listener) {
        final UUID id = taskListenerMap.get(listener);
        if (id != null){
            taskListenerMap.remove(listener);
            return nativeRemoveTaskListener(native_task_pointer, listener, id.toString());
        }
        return false;
    }

    protected long native_task_pointer;

    protected void init(final long native_task_pointer){
        this.native_task_pointer = native_task_pointer;
    }

    @Override
    public void start() {
        nativeStart(native_task_pointer);
    }

    @Override
    public Thread startOnNewThread() {
        nativeStartOnNewThread(native_task_pointer);
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

    @Override
    public Task.State getState() {
        return Task.State.values()[nativeGetState(native_task_pointer)];
    }

    @Override
    public long getElapsedTime() {
        return nativeGetElapsedTime(native_task_pointer);
    }

    private native void nativeStart(long native_task_pointer);
    private native void nativeStartOnNewThread(long native_task_pointer);
    private native void nativePause(long native_task_pointer);
    private native void nativePauseAndWait(long native_task_pointer);
    private native void nativeResume(long native_task_pointer);
    private native void nativeResumeAndWait(long native_task_pointer);
    private native void nativeStop(long native_task_pointer);
    private native void nativeStopAndWait(long native_task_pointer);

    private native float nativeGetProgress(long native_task_pointer);

    private native int nativeGetState(long native_task_pointer);
    private native long nativeGetElapsedTime(long native_task_pointer);

    private native void nativeAddTaskListener(long native_task_pointer, TaskListener listener, String id);
    private native boolean nativeRemoveTaskListener(long native_task_pointer, TaskListener listener, String id);
}
