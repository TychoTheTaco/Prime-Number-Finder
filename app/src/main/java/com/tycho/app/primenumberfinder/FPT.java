package com.tycho.app.primenumberfinder;

import android.os.Parcel;
import android.os.Parcelable;

import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.io.File;

public interface FPT extends ITask {

    File saveToFile();
    boolean save();

    long getStartValue();
    long getEndValue();
    int getCurrentFactor();

    int getPrimeCount();

    String getStatus();

    FindPrimesTask.SearchOptions getSearchOptions();

    class SearchOptions extends GeneralSearchOptions {

        /**
         * The value to start the search from. Inclusive.
         */
        private long startValue;

        /**
         * The value to stop the search on. Inclusive.
         */
        private long endValue;

        public enum SearchMethod{
            /**
             * Using brute force will loop over every number in the search range and check if it is divisible by numbers below its square root. This is
             * typically much slower, but more memory efficient.
             */
            BRUTE_FORCE,

            /**
             * Using the Sieve or Eratosthenes is a very quick way to check primality for each number in a specified range. The only drawbacks are that the
             * range needs to start at 0 and this method also requires a significant amount of memory.
             */
            SIEVE_OF_ERATOSTHENES
        }

        /**
         * The search method to use.
         */
        private SearchMethod searchMethod;

        public enum SearchMode{
            PARTITION,
            ALTERNATE,
            PACKET
        }

        private SearchMode searchMode = SearchMode.PACKET;

        /**
         * Directory used to cache task data. This is used to keep memory usage low. If no cache directory is specified, the task will not use caching and risks
         * running out of memory.
         */
        private File cacheDirectory;

        /**
         * The maximum size of prime numbers in memory. If more numbers are found, the entire list will be saved onto the disk first.
         */
        private int bufferSize = 1_000_000;

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final int threadCount, final boolean notifyWhenFinished, final boolean autoSave){
            super(threadCount, notifyWhenFinished, autoSave);
            this.startValue = startValue;
            this.endValue = endValue;
            this.searchMethod = searchMethod;
        }

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final int threadCount){
            this(startValue, endValue, searchMethod, threadCount, false, false);
        }

        public SearchOptions(final long startValue, final long endValue){
            this(startValue, endValue, SearchMethod.BRUTE_FORCE, 1, false, false);
        }

        private SearchOptions(final Parcel parcel){
            super(parcel);
            this.startValue = parcel.readLong();
            this.endValue = parcel.readLong();
            this.searchMethod = (SearchMethod) parcel.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            super.writeToParcel(dest, flags);
            dest.writeLong(this.startValue);
            dest.writeLong(this.endValue);
            dest.writeSerializable(this.searchMethod);
        }

        public static final Parcelable.Creator<SearchOptions> CREATOR = new Parcelable.Creator<SearchOptions>(){

            @Override
            public SearchOptions createFromParcel(Parcel in){
                return new SearchOptions(in);
            }

            @Override
            public SearchOptions[] newArray(int size){
                return new SearchOptions[size];
            }
        };

        public long getStartValue(){
            return startValue;
        }

        public void setStartValue(long startValue){
            this.startValue = startValue;
        }

        public long getEndValue(){
            return endValue;
        }

        public void setEndValue(long endValue){
            this.endValue = endValue;
        }

        public SearchMethod getSearchMethod(){
            return searchMethod;
        }

        public SearchMode getSearchMode() {
            return searchMode;
        }

        public void setSearchMode(SearchMode searchMode) {
            this.searchMode = searchMode;
        }

        public void setSearchMethod(SearchMethod searchMethod){
            this.searchMethod = searchMethod;
        }
    }
}