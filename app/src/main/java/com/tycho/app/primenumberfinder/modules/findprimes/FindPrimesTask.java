package com.tycho.app.primenumberfinder.modules.findprimes;

import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.MultithreadedTask;
import easytasks.Task;
import easytasks.TaskAdapter;
import simpletrees.Tree;

public class FindPrimesTask extends MultithreadedTask {

    public static final int INFINITY = -1;

    /**
     * The starting value of the search. (inclusive).
     */
    private final long startValue;

    /**
     * The ending value of the search. (inclusive).
     */
    private long endValue;

    /**
     * The number of threads to use. This must be at least 1.
     */
    private int threadCount;

    public enum SearchMethod {
        BRUTE_FORCE,
        SIEVE_OF_ERATOSTHENES
    }

    private SearchMethod searchMethod;

    private long[][] statistics = new long[2][3];
    private static final int NUMBERS_PER_SECOND = 0;
    private static final int PRIMES_PER_SECOND = 1;
    private static final int CURRENT_VALUE = 0;
    private static final int SINCE_LAST = 1;
    private static final int LAST_UPDATE_TIME = 2;

    private final List<Long> primes = new ArrayList<>();

    private SearchOptions searchOptions;

    /**
     * Create a new {@linkplain FindPrimesTask}.
     *
     * @param startValue  The start value (inclusive).
     * @param endValue    The end value (inclusive).
     * @param threadCount The number of threads to use.
     */
    public FindPrimesTask(final long startValue, final long endValue, final int threadCount, final SearchMethod searchMethod) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.threadCount = threadCount;
        this.searchMethod = searchMethod;
    }

    public FindPrimesTask(final SearchOptions searchOptions) {
        this(searchOptions.getStartValue(), searchOptions.getEndValue(), searchOptions.getThreadCount(), searchOptions.getSearchMethod());
        this.searchOptions = searchOptions;
    }

    public SearchOptions getSearchOptions(){
        return this.searchOptions;
    }

    @Override
    protected void run() {

        //Create worker tasks
        for (int i = 0; i < threadCount; i++) {
            long s = startValue + (2 * i + 1);
            if (s % 2 == 0)
                s -= 1;
            final BruteForceTask task = new BruteForceTask(s, endValue, threadCount * 2);
            task.addTaskListener(new TaskAdapter() {
                @Override
                public void onTaskStarted() {
                    System.out.println("Thread " + task.startValue + " started.");
                }

                @Override
                public void onTaskPaused() {
                    System.out.println("Thread " + task.startValue + " paused.");
                }

                @Override
                public void onTaskResumed() {
                    System.out.println("Thread " + task.startValue + " resumed.");
                }

                @Override
                public void onTaskStopped() {
                    System.out.println("Thread " + task.startValue + " finished.");
                }
            });
            addTask(task);
        }

        executeTasks();

        System.out.println("All threads stopped.");
        long time = 0;
        for (Task task : getTasks()) {
            time += task.getElapsedTime();
            System.out.println("Task " + ((BruteForceTask) task).startValue + "\n    " /*+ ((BruteForceTask) task).primes.size() + " primes\n    "*/
                    + task.getElapsedTime() + " milliseconds\n    " + ((BruteForceTask) task).totalDistance + " distance");
        }
        System.out.println("Average time: " + (time / getTasks().size()) + " milliseconds.");
        System.out.println("Elapsed time: " + getElapsedTime() + " milliseconds.");
    }

    /**
     * @return the endValue
     */
    public long getEndValue() {
        return endValue;
    }

    /**
     * @return the threadCount
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * @return the startValue
     */
    public long getStartValue() {
        return startValue;
    }

    private int lastSortIndex = 0;

    private final Object SORT_LOCK = new Object();

    public List<Long> getSortedPrimes(){
        Log.d("FPT", "Beginning sort at index " + lastSortIndex);

        final int size = primes.size();
        for (int i = lastSortIndex; i < size; i++) {
            int swapIndex = -1;
            long smallest = primes.get(i);
            for (int k = i + 1; k < size; k++) {
                final long item = primes.get(k);
                if (item < smallest) {
                    smallest = item;
                    swapIndex = k;
                }
            }

            if (swapIndex != -1) {
                final long temp = primes.get(i);
                primes.set(i, smallest);
                primes.set(swapIndex, temp);
            }
        }
        lastSortIndex = size;

        Log.d("FPT", "Finished sort until index " + size);

        return primes;
    }

    public List<Long> getPrimes() {
        return this.primes;
    }

    /**
     * Finds and returns the lowest number that is currently being checked or has already been checked for primality. If there is more than one thread, this will return the lowest {@linkplain BruteForceTask#currentNumber} out of all threads.
     *
     * @return The lowest {@linkplain BruteForceTask#currentNumber}.
     */
    public long getCurrentValue() {
        if (getTasks().size() == 0) return 0;
        long lowest = ((BruteForceTask) getTasks().get(0)).getCurrentValue();
        for (Task task : getTasks()) {
            if (((BruteForceTask) task).getCurrentValue() < lowest) {
                lowest = ((BruteForceTask) task).getCurrentValue();
            }
        }
        return lowest;
    }

    public void setOptions(final SearchOptions searchOptions){
        this.searchOptions = searchOptions;
    }

    private class BruteForceTask extends MultithreadedTask.SubTask {

        /**
         * The starting value of the search. (inclusive).
         */
        private final long startValue;

        /**
         * The ending value of the search. (inclusive).
         */
        private long endValue;

        private long currentNumber;

        private final int increment;

        //public final List<Long> primes = new ArrayList<>();

        private int totalDistance = 0;

        public BruteForceTask(final long startValue, final long endValue, final int increment) {
            this.startValue = startValue;
            this.endValue = endValue;
            this.increment = increment;
        }

        @Override
        protected void run() {

            currentNumber = startValue;
            if (startValue < 3) {
                if (endValue == INFINITY || endValue >= 2) {
                    dispatchPrimeFound(2);
                }
                currentNumber += increment;
            }

            int sqrtMax;
            boolean isPrime;

            boolean running = true;

            while (running) {

                // Check if the end value has been reached
                if (currentNumber <= endValue || endValue == -1) {

                    // Check if the number is divisible by 2
                    if (currentNumber % 2 != 0) {

						/*
                         * Get the square root of the number. We only need to calculate up to the square
						 * root to determine if the number is prime. The square root of a long will
						 * always fit inside the value range of an int.
						 */
                        sqrtMax = (int) Math.sqrt(currentNumber);

                        // Assume the number is prime
                        isPrime = true;

						/*
                         * Check if the number is divisible by every odd number below it's square root.
						 */
                        for (int i = 3; i <= sqrtMax; i += 2) {

                            // Check if the number divides perfectly
                            if (currentNumber % i == 0) {
                                isPrime = false;
                                totalDistance += i;
                                break;
                            }

                            // Check if we should pause
                            tryPause();
                            if (shouldStop()) {
                                running = false;
                                break;
                            }
                        }

                        // Check if the number was prime
                        if (isPrime) {
                            totalDistance += sqrtMax;
                            dispatchPrimeFound(currentNumber);
                        }
                    }

                    currentNumber += increment;

                    // Calculate total progress
                    if (endValue != -1) {
                        setProgress(((float) (currentNumber - startValue) / (endValue - startValue)));
                    }

                } else {
                    currentNumber = endValue;
                    setProgress(1);
                    break;
                }
            }
        }

        public long getCurrentValue() {
            return this.currentNumber;
        }

        private void dispatchPrimeFound(final long number) {
            synchronized (SORT_LOCK) {
                primes.add(number);
            }
        }

       /* public List<Long> getPrimes() {
            return this.primes;
        }*/

    }

    // Android


    public long getPrimesPerSecond() {
        return 0;
    }

    public long getNumbersPerSecond() {
        return 0;
    }

    public static class SearchOptions implements Parcelable {

        /**
         * The value to start the search from. Inclusive.
         */
        private long startValue;

        /**
         * The value to stop the search on. Inclusive.
         */
        private long endValue;

        /**
         * The search method to use.
         */
        private SearchMethod searchMethod;

        /**
         * The number of threads to use. This is ignored if the search method is {@linkplain SearchMethod#SIEVE_OF_ERATOSTHENES}.
         */
        private int threadCount;

        /**
         * Show a notification when the task is finished.
         */
        public boolean notifyWhenFinished;

        /**
         * Automatically save the results of this task.
         */
        public boolean autoSave;

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final int threadCount, final boolean notifyWhenFinished, final boolean autoSave) {
            this.startValue = startValue;
            this.endValue = endValue;
            this.searchMethod = searchMethod;
            this.threadCount = threadCount;
            this.notifyWhenFinished = notifyWhenFinished;
            this.autoSave = autoSave;
        }

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final int threadCount) {
            this(startValue, endValue, searchMethod, threadCount, false, false);
        }

        public SearchOptions(final long startValue, final long endValue){
            this(startValue, endValue, SearchMethod.BRUTE_FORCE, 1, false, false);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.startValue);
            dest.writeLong(this.endValue);
            dest.writeSerializable(this.searchMethod);
            dest.writeInt(this.threadCount);
            dest.writeInt(this.notifyWhenFinished ? 1 : 0);
            dest.writeInt(this.autoSave ? 1 : 0);
        }

        public static final Parcelable.Creator<SearchOptions> CREATOR  = new Parcelable.Creator<SearchOptions>() {

            @Override
            public SearchOptions createFromParcel(Parcel in) {
                return new SearchOptions(in);
            }

            @Override
            public SearchOptions[] newArray(int size) {
                return new SearchOptions[size];
            }
        };

        private SearchOptions(final Parcel parcel) {
            this.startValue = parcel.readLong();
            this.endValue = parcel.readLong();
            this.searchMethod = (SearchMethod) parcel.readSerializable();
            this.threadCount = parcel.readInt();
            this.notifyWhenFinished = parcel.readInt() == 1;
            this.autoSave = parcel.readInt() == 1;
        }

        public long getStartValue() {
            return startValue;
        }

        public void setStartValue(long startValue) {
            this.startValue = startValue;
        }

        public long getEndValue() {
            return endValue;
        }

        public void setEndValue(long endValue) {
            this.endValue = endValue;
        }

        public SearchMethod getSearchMethod() {
            return searchMethod;
        }

        public void setSearchMethod(SearchMethod searchMethod) {
            this.searchMethod = searchMethod;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }
    }
}
