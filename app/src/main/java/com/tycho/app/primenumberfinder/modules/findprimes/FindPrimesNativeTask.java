package com.tycho.app.primenumberfinder.modules.findprimes;

import com.tycho.app.primenumberfinder.NativeTask;

public class FindPrimesNativeTask extends NativeTask {

    @Override
    protected long initNativeTask() {
        return nativeInit();
    }

    private native long nativeInit();
}
