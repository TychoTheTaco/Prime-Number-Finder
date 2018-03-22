package com.tycho.app.primenumberfinder.modules.findprimes;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import easytasks.MultithreadedTask;
import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;
import simpletrees.Tree;

import static com.tycho.app.primenumberfinder.utils.FileManager.EXTENSION;

public class FindPrimesTask extends MultithreadedTask {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesTask";

    public static final int INFINITY = -1;

    /**
     * The starting value of the search range. (inclusive).
     */
    private final long startValue;

    /**
     * The ending value of the search range. (inclusive).
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

    //private List<Long> primes = new ArrayList<>();

    private static final Object COUNTER_SYNC = new Object();

    private int primeCount = 0;

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

    public FindPrimesTask(final SearchOptions searchOptions, final Context context) {
        this(searchOptions.getStartValue(), searchOptions.getEndValue(), searchOptions.getThreadCount(), searchOptions.getSearchMethod());
        this.searchOptions = searchOptions;
    }

    public SearchOptions getSearchOptions() {
        return this.searchOptions;
    }

    @Override
    protected void run() {

        switch (searchMethod) {
            case BRUTE_FORCE:
                searchBruteForce();
                break;

            case SIEVE_OF_ERATOSTHENES:
                searchSieve();
                break;
        }
    }

    private void searchBruteForce() {
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
            task.bufferSize = searchOptions.bufferSize / threadCount;
            addTask(task);
        }

        executeTasks();

        //Execute all tasks
/*        final List<Thread> threads = new ArrayList<>();
        for (Task task : getTasks()) {
            threads.add(task.startOnNewThread());
        }

        final List<Long> sorted = new ArrayList<>();

        if (threadCount > 1){
            final List<Long> heads = new ArrayList<>();
            for (int i = 0; i < getTasks().size(); i++) {
                if (!(getTasks().get(i).getState() == State.STOPPED && ((BruteForceTask) getTasks().get(i)).queue.isEmpty())) {
                    try {
                        heads.add(((BruteForceTask) getTasks().get(i)).queue.take());
                    } catch (InterruptedException e) {
                        Log.wtf(TAG, "Sorting was interrupted!");
                    }
                }
            }

            //Log.d(TAG, "Initial heads: " + heads);

            while (!areSubTasksDone()){
                takeFlag = false;
                int lowestIndex = 0;
                for (int i = 1; i < heads.size(); i++) {
                    if (heads.get(i) != -1 && heads.get(i) < heads.get(lowestIndex)) {
                        lowestIndex = i;
                    }
                }

                if (lowestIndex == -1){
                    Log.wtf(TAG, "lowestIndex was -1!");
                }

                *//*for (long number : heads){
                    Log.w(TAG, "Head: " + number);
                }
                Log.d(TAG, "Lowest index was " + lowestIndex);
                for (Task task : getTasks()){
                    Log.w(TAG, "Queue: " + ((BruteForceTask) task).queue);
                }*//*

                sorted.add(heads.get(lowestIndex));
                if (!(getTasks().get(lowestIndex).getState() == State.STOPPED && ((BruteForceTask) getTasks().get(lowestIndex)).queue.isEmpty())) {
                    try {
                        if (((BruteForceTask) getTasks().get(lowestIndex)).queue.isEmpty()){
                            //Wait until item added or task finished
                            synchronized (((BruteForceTask) getTasks().get(lowestIndex)).QUEUE_LOCK){
                                while (((BruteForceTask) getTasks().get(lowestIndex)).queue.isEmpty() && getTasks().get(lowestIndex).getState() != State.STOPPED){
                                    //Log.d(TAG, "Waiting...");
                                    ((BruteForceTask) getTasks().get(lowestIndex)).QUEUE_LOCK.wait();
                                    //Log.d(TAG, "Notified: " + ((BruteForceTask) getTasks().get(lowestIndex)).queue.size());
                                }
                            }
                        }
                        if (((BruteForceTask) getTasks().get(lowestIndex)).queue.isEmpty()){
                            //Log.d(TAG, "Index " + lowestIndex + " still empty. state: " + getTasks().get(lowestIndex).getState());
                            heads.set(lowestIndex, -1L);
                        }else{
                            //Log.d(TAG, "Taking from " + ((BruteForceTask) getTasks().get(lowestIndex)).queue);
                            heads.set(lowestIndex, ((BruteForceTask) getTasks().get(lowestIndex)).queue.poll());
                            //Log.d(TAG, "Took " + heads.get(lowestIndex));
                            takeFlag = true;
                        }
                    } catch (InterruptedException e) {
                        Log.wtf(TAG, "Sorting was interrupted!");
                    }
                }else{
                    //Log.d(TAG, "Index " + lowestIndex + " is done");
                    heads.set(lowestIndex, -1L);
                }
            }
        }else{
            sorted.addAll(((BruteForceTask) getTasks().get(0)).queue);
        }

        Log.d(TAG, "Sorted: " + sorted);

        for (Thread thread : threads){
            try {
                thread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }*/

        System.out.println("All threads stopped.");
        long time = 0;
        for (Task task : getTasks()) {
            time += task.getElapsedTime();
            System.out.println("Task " + ((BruteForceTask) task).startValue + "\n    " /*+ ((BruteForceTask) task).primes.size() + " primes\n    "*/
                    + task.getElapsedTime() + " milliseconds\n    " + ((BruteForceTask) task).totalDistance + " distance");
        }
        System.out.println("Average time: " + (time / getTasks().size()) + " milliseconds.");
        System.out.println("Elapsed time: " + getElapsedTime() + " milliseconds.");

        //final long sortStart = System.currentTimeMillis();
        //System.out.println("Merging cache...");

        //mergeCache();

        //System.out.println("Finished merge in " + (System.currentTimeMillis() - sortStart) + " ms.");
    }

    public File saveToFile() throws IOException {

        final File largeCache = new File(FileManager.getInstance().getTaskCacheDirectory(this) + File.separator + "primes");
        if (searchMethod == SearchMethod.BRUTE_FORCE) {
            mergeCache();
        } else {
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(largeCache));
            final int lastNumber = ((SieveTask) getTasks().get(0)).primes.get(((SieveTask) getTasks().get(0)).primes.size() - 1);
            for (int number : ((SieveTask) getTasks().get(0)).primes) {
                bufferedWriter.write(String.valueOf(number));
                if (number != lastNumber) {
                    bufferedWriter.write('\n');
                }
            }
            bufferedWriter.close();
        }
        return largeCache;
    }

    private void mergeCache() {

        final long sortStart = System.currentTimeMillis();
        System.out.println("Merging cache...");

        try {
            final File largeCache = new File(FileManager.getInstance().getTaskCacheDirectory(this) + File.separator + "primes");
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(largeCache));

            //Read each cache file
            for (File file : FileManager.getInstance().getTaskCacheDirectory(this).listFiles()) {
                Log.d(TAG, "Reading from: " + file.getAbsolutePath());
                if (file.getAbsolutePath().contains("primes")) break;
                final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                //final List<Long> numbers = new ArrayList<>();
                //final int BUFFER_SIZE = 100_000;
                try {
                    while (true) {
                            /*numbers.add(dataInputStream.readLong());
                            if (numbers.size() >= BUFFER_SIZE){
                                FileManager.writeCompact(numbers, largeCache, true);
                                numbers.clear();
                            }*/
                        bufferedWriter.write(String.valueOf(dataInputStream.readLong()));
                        bufferedWriter.write('\n');
                    }
                } catch (EOFException e) {
                    dataInputStream.close();
                        /*if (numbers.size() >= BUFFER_SIZE){
                            FileManager.writeCompact(numbers, largeCache, true);
                            numbers.clear();
                        }*/
                }
            }

            final List<Long> primes = getSortedPrimes();
            Log.d(TAG, "Writing remaining: " + primes.size());
            //Debug.startMethodTracing("trace4");
            //FileManager.writeCompact(primes, largeCache, true);
            //Debug.stopMethodTracing();
            if (primes.size() > 0) {
                final long last = primes.get(primes.size() - 1);
                for (long number : primes) {
                    bufferedWriter.write(String.valueOf(number));
                    if (number != last) {
                        bufferedWriter.write('\n');
                    }
                }
            }

            //Delete old cache
            for (File file : FileManager.getInstance().getTaskCacheDirectory(this).listFiles()) {
                if (!file.getAbsolutePath().contains("primes")) {
                    file.delete();
                }
            }

            //Clear memory
            for (Task task : getTasks()) {
                ((BruteForceTask) task).primes.clear();
            }

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Finished merge in " + (System.currentTimeMillis() - sortStart) + " ms.");
    }

    private volatile boolean takeFlag = false;

    private boolean areSubTasksDone() {
        if (takeFlag) return false;
        for (Task task : getTasks()) {
            if (!(task.getState() == State.STOPPED && ((BruteForceTask) task).queue.isEmpty()))
                return false;
        }
        return true;
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

    public int getPrimeCount() {
        return primeCount;
    }

    public List<Long> getSortedPrimes() {

        final List<Long> primes = new ArrayList<>(primeCount);

        switch (searchMethod) {
            case BRUTE_FORCE:
                final int[] index = new int[getTasks().size()];
                for (int i = 0; i < index.length; i++) {
                    index[i] = 0;
                }

                int li = 0;
                long lowest = 0;
                do {
                    lowest = 0;
                    for (int i = 0; i < getTasks().size(); i++) {
                        if (index[i] < ((BruteForceTask) getTasks().get(i)).primes.size()) {
                            if (((BruteForceTask) getTasks().get(i)).primes.get(index[i]) < lowest || lowest == 0) {
                                lowest = ((BruteForceTask) getTasks().get(i)).primes.get(index[i]);
                                li = i;
                            }
                        }
                    }
                    index[li]++;
                    primes.add(lowest);
                } while (lowest != 0);
                primes.remove(primes.size() - 1);

                break;

            case SIEVE_OF_ERATOSTHENES:
                for (int i = 0; i < primeCount - 1; i++) {
                    primes.add(Long.valueOf(((SieveTask) getTasks().get(0)).primes.get(i)));
                }
                break;
        }

        return primes;
    }

    /**
     * Finds and returns the lowest number that is currently being checked or has already been checked for primality. If there is more than one thread, this will return the lowest {@linkplain BruteForceTask#currentNumber} out of all threads.
     *
     * @return The lowest {@linkplain BruteForceTask#currentNumber}.
     */
    public long getCurrentValue() {
        if (getTasks().size() == 0) return 0;
        if (searchMethod == SearchMethod.SIEVE_OF_ERATOSTHENES) {
            if (getState() == State.STOPPED) {
                return endValue;
            } else {
                return 0;
            }
        }
        long lowest = ((BruteForceTask) getTasks().get(0)).getCurrentValue();
        for (Task task : getTasks()) {
            if (((BruteForceTask) task).getCurrentValue() < lowest) {
                lowest = ((BruteForceTask) task).getCurrentValue();
            }
        }
        return lowest;
    }

    public void setOptions(final SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
    }

    private final File taskDirectory = FileManager.getInstance().getTaskCacheDirectory(this);

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

        private final List<Long> primes = new ArrayList<>();
        private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();

        private int totalDistance = 0;

        private final Object QUEUE_LOCK = new Object();

        private int bufferSize;

        private BruteForceTask(final long startValue, final long endValue, final int increment) {
            this.startValue = startValue;
            this.endValue = endValue;
            this.increment = increment;
            /*addTaskListener(new TaskAdapter(){
                @Override
                public void onTaskStopped() {
                    synchronized (QUEUE_LOCK){
                        QUEUE_LOCK.notify();
                    }
                    Log.d(TAG, "Thread took " + getElapsedTime() + " ms.");
                }
            });*/
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
                        dispatchPrimeFound(currentNumber);
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
            primes.add(number);
            /*queue.add(number);
            synchronized (QUEUE_LOCK){
                //Log.d(TAG, "Added " + number + " to " + queue);
                QUEUE_LOCK.notify();
            }*/
            synchronized (COUNTER_SYNC) {
                primeCount++;
            }

            if (primes.size() >= bufferSize) {
                Log.d(TAG, "Swapping to disk! Size: " + primes.size());

                //Swap memory to disk
                if (!taskDirectory.exists()) {
                    taskDirectory.mkdirs();
                }

                //Write to 1 file with no commas use byte offset for quick reads
                FileManager.getInstance().writeToCache(primes, new File(FileManager.getInstance().getContext().getFilesDir() + File.separator + "cache" + File.separator + FindPrimesTask.this.getId() + File.separator + getId() + ".cache"), true);
                primes.clear();
            }
        }

       /* public List<Long> getPrimes() {
            return this.primes;
        }*/

    }

    private class SieveTask extends MultithreadedTask.SubTask {

        private final List<Integer> primes = new ArrayList<>();

        @Override
        protected void run() {
            //Assume all numbers are prime
            final BitSet bitSet = new BitSet((int) (endValue + 1));
            bitSet.set(0, bitSet.size() - 1, true);

            final int sqrtMax = (int) Math.sqrt(endValue);

            // mark non-primes <= n using Sieve of Eratosthenes
            for (int factor = 2; factor <= sqrtMax; factor++) {

                // if factor is prime, then mark multiples of factor as nonprime
                // suffices to consider mutiples factor, factor+1, ...,  n/factor
                if (bitSet.get(factor)) {
                    for (int j = factor; factor * j <= endValue; j++) {
                        bitSet.set(factor * j, false);
                        //tryPause();
                    }
                }

                setProgress(((float) factor / sqrtMax) / 2);
                tryPause();
            }

            for (int i = 2; i < endValue; i++) {
                if (bitSet.get(i)) {
                    primes.add(i);
                    primeCount++;
                }
                //setProgress(0.5f + (((float) i / endValue) / 2));
            }
            setProgress(1);
        }
    }

    private void searchSieve() {
        addTask(new SieveTask());
        executeTasks();
        /*final List<Integer> numbers = new ArrayList<>();
        for (int i : ((SieveTask) getTasks().get(0)).primes){
            numbers.add(i);
        }*/
        //Debug.startMethodTracing("run0");
        //FileManager.writeCompact(numbers, new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "large"), false);
        //Debug.stopMethodTracing();

        /*for (int x = 0, y = 0; x < 10 && y < 10; x++, y += ((x == 10) ? 1 : 0), x = (x == 10 ? 0 : x)){
            Log.d(TAG, "(" + x + ", " + y + ")");
        }*/
    }

    // Android

    public static class SearchOptions implements Parcelable, Cloneable {

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
         * The maximum size of prime numbers in memory. If more numbers are found, the entire list will be saved onto the disk first.
         */
        private int bufferSize = 100_000;

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

        public SearchOptions(final long startValue, final long endValue) {
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

        public static final Parcelable.Creator<SearchOptions> CREATOR = new Parcelable.Creator<SearchOptions>() {

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

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
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
