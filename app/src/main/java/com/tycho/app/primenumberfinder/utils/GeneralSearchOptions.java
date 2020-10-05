package com.tycho.app.primenumberfinder.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class GeneralSearchOptions implements Parcelable, Cloneable {

    /**
     * The number of threads to use.
     */
    private int threadCount;

    protected GeneralSearchOptions(int threadCount){
        this.threadCount = threadCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.threadCount);
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
}
