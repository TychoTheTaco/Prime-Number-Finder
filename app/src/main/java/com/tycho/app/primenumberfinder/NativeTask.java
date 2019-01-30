package com.tycho.app.primenumberfinder;

import easytasks.Task;

public class NativeTask extends Task {

    private final long native_task_pointer;

    public NativeTask(final long native_task_pointer){
        //this.native_task_pointer = native_task_pointer;
        this.native_task_pointer = nativeInit();
    }

    private native long nativeInit();

    @Override
    protected void run() {
        nativeRun(native_task_pointer);
    }

    private native void nativeRun(long task_pointer);

    @Override
    public void pause() {
        super.pause();
    }

    private native void nativePause(long task_pointer);

    @Override
    public void resume() {
        super.resume();
    }

    private native void nativeResume(long task_pointer);

    @Override
    public void stop() {
        super.stop();
    }

    private native void nativeStop(long task_pointer);
}
