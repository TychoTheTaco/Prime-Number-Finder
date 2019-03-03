package com.tycho.app.primenumberfinder.modules.findprimes;

import com.tycho.app.primenumberfinder.FindPrimesTask;
import com.tycho.app.primenumberfinder.NativeTask;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;

public class FindPrimesNativeTask extends NativeTask implements FindPrimesTask, SearchOptions, Savable {

    private FindPrimesTask.SearchOptions searchOptions;

    public FindPrimesNativeTask(final SearchOptions searchOptions){
        super(nativeInit(searchOptions.getStartValue(), searchOptions.getEndValue(), searchOptions.getSearchMethod(), searchOptions.getThreadCount(), searchOptions.getCacheDirectory().getAbsolutePath()));
        this.searchOptions = searchOptions;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public long getStartValue() {
        return nativeGetStartValue(native_task_pointer);
    }

    @Override
    public long getEndValue() {
        return nativeGetEndValue(native_task_pointer);
    }

    @Override
    public int getThreadCount() {
        return nativeGetThreadCount(native_task_pointer);
    }

    @Override
    public int getPrimeCount() {
        return nativeGetPrimeCount(native_task_pointer);
    }

    @Override
    public int getCurrentFactor() {
        return nativeGetCurrentFactor(native_task_pointer);
    }

    @Override
    public String getStatus() {
        return nativeGetStatus(native_task_pointer);
    }

    @Override
    public FindPrimesTask.SearchOptions getSearchOptions() {
        return searchOptions;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setSearchOptions(SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Misc
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isEndless() {
        return nativeIsEndless(native_task_pointer);
    }

    @Override
    public void saveToFile(final File file) {
        nativeSaveToFile(native_task_pointer, file.getAbsolutePath());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // [Savable]
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addSaveListener(SaveListener listener) {

    }

    @Override
    public void removeSaveListener(SaveListener listener) {

    }

    @Override
    public boolean save() {
        saveToFile(new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "Prime numbers from " + getStartValue() + " to " + (isEndless() ? "INF" : getEndValue())));
        return true;
    }

    @Override
    public boolean isSaved() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Native methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static native long nativeInit(final long startValue, final long endValue, final SearchOptions.SearchMethod searchMethod, final int threadCount, final String cacheDirectory);

    private native long nativeGetStartValue(final long native_task_pointer);
    private native long nativeGetEndValue(final long native_task_pointer);
    private native int nativeGetThreadCount(final long native_task_pointer);
    private native int nativeGetPrimeCount(final long native_task_pointer);
    private native int nativeGetCurrentFactor(final long native_task_pointer);
    private native String nativeGetStatus(final long native_task_pointer);

    private native boolean nativeIsEndless(final long native_task_pointer);

    private native void nativeSaveToFile(final long native_task_pointer, String filePath);
}
