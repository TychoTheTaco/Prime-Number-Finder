package com.tycho.app.primenumberfinder.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class GeneralSearchOptions implements Parcelable, Cloneable {

    /**
     * The number of threads to use.
     */
    private int threadCount;

    /**
     * Show a notification when the task is finished.
     */
    private boolean notifyWhenFinished;

    /**
     * Automatically save the results of this task.
     */
    private boolean autoSave;

    protected GeneralSearchOptions(int threadCount, boolean notifyWhenFinished, boolean autoSave){
        this.threadCount = threadCount;
        this.notifyWhenFinished = notifyWhenFinished;
        this.autoSave = autoSave;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.threadCount);
        dest.writeInt(this.notifyWhenFinished ? 1 : 0);
        dest.writeInt(this.autoSave ? 1 : 0);
    }

    public static final Parcelable.Creator<GeneralSearchOptions> CREATOR = new Parcelable.Creator<GeneralSearchOptions>() {

        @Override
        public GeneralSearchOptions createFromParcel(Parcel in) {
            return new GeneralSearchOptions(in);
        }

        @Override
        public GeneralSearchOptions[] newArray(int size) {
            return new GeneralSearchOptions[size];
        }
    };

    protected GeneralSearchOptions(final Parcel parcel) {
        this.threadCount = parcel.readInt();
        this.notifyWhenFinished = parcel.readInt() == 1;
        this.autoSave = parcel.readInt() == 1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isNotifyWhenFinished() {
        return notifyWhenFinished;
    }

    public void setNotifyWhenFinished(boolean notifyWhenFinished) {
        this.notifyWhenFinished = notifyWhenFinished;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }
}
