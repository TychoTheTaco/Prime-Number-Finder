package com.tycho.app.primenumberfinder;

import easytasks.Task;

public abstract class NativeTask implements ITask {

    static {
        System.loadLibrary("native-utils");
    }

    private final long native_task_pointer;

    public NativeTask(){
        this.native_task_pointer = initNativeTask();
    }

    @Override
    public void start() {
        nativeStart(native_task_pointer);
    }

    @Override
    public void startOnNewThread() {
        nativeStartOnNewThread(native_task_pointer);
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

    protected abstract long initNativeTask();
    private native void nativeStart(long native_task_pointer);
    private native void nativeStartOnNewThread(long native_task_pointer);
    private native void nativePause(long native_task_pointer);
    private native void nativePauseAndWait(long native_task_pointer);
    private native void nativeResume(long native_task_pointer);
    private native void nativeResumeAndWait(long native_task_pointer);
    private native void nativeStop(long native_task_pointer);
    private native void nativeStopAndWait(long native_task_pointer);

    private native int nativeGetState(long native_task_pointer);
    private native long nativeGetElapsedTime(long native_task_pointer);
}
