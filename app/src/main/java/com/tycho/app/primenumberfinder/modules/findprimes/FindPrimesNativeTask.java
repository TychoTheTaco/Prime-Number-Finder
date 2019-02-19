package com.tycho.app.primenumberfinder.modules.findprimes;

import com.tycho.app.primenumberfinder.FPT;
import com.tycho.app.primenumberfinder.NativeTask;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;

public class FindPrimesNativeTask extends NativeTask implements FPT, SearchOptions, Savable {

    private FPT.SearchOptions searchOptions;

    public FindPrimesNativeTask(final SearchOptions searchOptions){
        this.searchOptions = searchOptions;
        init(initNativeTask());
    }

    private long initNativeTask() {
        return nativeInit(searchOptions.getStartValue(), searchOptions.getEndValue(), searchOptions.getSearchMethod(), searchOptions.getThreadCount(), searchOptions.getCacheDirectory().getAbsolutePath());
    }

    @Override
    public void saveToFile(final File file) {
        nativeSaveToFile(native_task_pointer, file.getAbsolutePath());
    }

    @Override
    public boolean save() {
        saveToFile(new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "Prime numbers from " + getStartValue() + " to " + (getEndValue() == FindPrimesTask.INFINITY ? "INF" : getEndValue())));
        return true;
    }

    @Override
    public long getStartValue() {
        return nativeGetStartValue(native_task_pointer);
    }

    @Override
    public long getEndValue() {
        return nativeGetEndValue(native_task_pointer);
    }

    @Override
    public int getCurrentFactor() {
        return nativeGetCurrentFactor(native_task_pointer);
    }

    @Override
    public int getPrimeCount() {
        return nativeGetPrimeCount(native_task_pointer);
    }

    @Override
    public String getStatus() {
        return nativeGetStatus(native_task_pointer);
    }

    @Override
    public FPT.SearchOptions getSearchOptions() {
        return searchOptions;
    }

    @Override
    public boolean isSaved() {
        return false;
    }

    private native long nativeInit(final long startValue, final long endValue, final SearchOptions.SearchMethod searchMethod, final int threadCount, final String cacheDirectory);
    private native void nativeSaveToFile(final long native_task_pointer, String filePath);
    private native long nativeGetStartValue(final long native_task_pointer);
    private native long nativeGetEndValue(final long native_task_pointer);
    private native int nativeGetCurrentFactor(final long native_task_pointer);
    private native int nativeGetPrimeCount(final long native_task_pointer);
    private native String nativeGetStatus(final long native_task_pointer);
}
