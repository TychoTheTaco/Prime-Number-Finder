package com.tycho.app.primenumberfinder.modules.findprimes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.MultithreadedTask;
import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.BRUTE_FORCE;
import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.SIEVE_OF_ERATOSTHENES;
import static com.tycho.app.primenumberfinder.utils.FileManager.EXTENSION;

public class FindPrimesTask extends MultithreadedTask implements Savable, SearchOptions {

    static {
        System.loadLibrary("native-utils");
    }

    public native int nativeSieve(final long start, final long end);

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesTask.class.getSimpleName();

    /**
     * Used to represent infinity (typically as an end value).
     */
    public static final int INFINITY = -1;

    /**
     * Search options that specify task parameters.
     */
    private SearchOptions options;

    /**
     * Create a new FindPrimesTask with the specified search options
     *
     * @param options
     */
    public FindPrimesTask(final SearchOptions options) {
        this.options = options;
    }

    @Override
    protected void run() {
        switch (options.getSearchMethod()) {
            case BRUTE_FORCE:
                searchBruteForce();
                break;

            case SIEVE_OF_ERATOSTHENES:
                searchSieve();
                break;
        }
    }

    /**
     * Search for primes using the brute force method.
     */
    private void searchBruteForce() {
        //Determine best search mode to use
        if (options.getThreadCount() % 2 == 0) {
            options.searchMode = SearchOptions.SearchMode.ALTERNATE;
        } else {
            options.searchMode = SearchOptions.SearchMode.PACKET;
        }

        /*Vector v = new Vector();
        while (true) {
            byte b[] = new byte[1048576];
            v.add(b);
            Runtime rt = Runtime.getRuntime();
            System.out.println("free memory: " + rt.freeMemory());
        }*/

        /*switch (options.searchMode) {
            case PARTITION:
                preparePartitionMode();
                executeTasks();
                break;

            case ALTERNATE:
                prepareAlternateMode();
                executeTasks();
                break;

            case PACKET:
                //The optimal packet size is roughly 10% of each thread's total workload
                preparePacketMode((long) ((getRange() / options.getThreadCount()) * 0.1));
                executeThreadPool();
                break;
        }*/
    }

    /**
     * This method prepares the task for searching using the partition mode. The partition mode divides the search range into even partitions based on the
     * number of threads available. Each thread then is responsible for searching through a single partition.
     */
    private void preparePartitionMode() {
        final long partitionSize = getRange() / options.getThreadCount();
        System.out.println("partition size: " + partitionSize);
        for (int i = 0; i < options.getThreadCount(); i++) {
            long start = options.startValue + (i * partitionSize + 1);
            if (start % 2 == 0) start++;
            final BruteForceTask task = new BruteForceTask(start, options.startValue + (i + 1) * partitionSize, 2);
            System.out.println("task " + start + " " + task.endValue);
            debugTaskListener(task);
            addTask(task);
        }
    }

    /**
     * This method prepares the task for searching using the alternate mode. The alternate mode ???
     */
    private void prepareAlternateMode() {
        final long[] startValues = new long[options.getThreadCount()];
        int increment = options.getThreadCount() * 2;
        startValues[0] = (options.startValue % 2 == 0) ? (options.startValue + 1) : options.startValue;
        for (int i = 0; i < startValues.length; i++) {
            long s = i == 0 ? startValues[0] : startValues[i - 1] + 2;
            if (s % 2 == 0) {
                s -= 1;
            }
            startValues[i] = s;
            final BruteForceTask task = new BruteForceTask(s, options.endValue, increment);
            debugTaskListener(task);
            addTask(task);
        }
    }

    /**
     * This method prepares the task for searching using the packet mode. The packet mode divides the search range into "packets" of a specified size. Each
     * packet is added to a queue, where it is consumed by the next available thread in the pool.
     * <p>
     * This is typically much faster than the partition mode and only slightly slower than the alternate mode.
     *
     * @param packetSize The maximum size of a packet.
     */
    private void preparePacketMode(final long packetSize) {
        for (int i = 0; i < Math.ceil((double) getRange() / packetSize); i++) {
            long start = options.startValue + (i * packetSize + 1);
            if (start % 2 == 0) start++;
            final BruteForceTask task = new BruteForceTask(start, Math.min(options.startValue + (i + 1) * packetSize, options.endValue), 2);
            System.out.println("task " + start + " " + task.endValue);
            debugTaskListener(task);
            addTask(task);
        }
    }

    int count = 0;
    final Object LOCK = new Object();

    private void executeThreadPool() {
        final List<Thread> pool = new ArrayList<>();
        final Queue<Task> pending = new ArrayDeque<>(getTasks());
        while (!pending.isEmpty()) {
            final Task task = pending.poll();
            task.addTaskListener(new TaskAdapter() {
                @Override
                public void onTaskStopped() {
                    synchronized (LOCK) {
                        count--;
                        LOCK.notifyAll();
                    }
                }
            });

            synchronized (LOCK) {
                while (count >= options.getThreadCount()) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                count++;
                pool.add(task.startOnNewThread());
            }
        }

        for (Thread thread : pool) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void debugTaskListener(BruteForceTask task) {
        task.addTaskListener(new TaskListener() {
            @Override
            public void onTaskStarted() {
                System.out.println("STARTED: " + task.startValue);
            }

            @Override
            public void onTaskPausing() {

            }

            @Override
            public void onTaskPaused() {

            }

            @Override
            public void onTaskResuming() {

            }

            @Override
            public void onTaskResumed() {

            }

            @Override
            public void onTaskStopping() {

            }

            @Override
            public void onTaskStopped() {
                System.out.println("STOPPED: " + task.startValue + " in " + task.getElapsedTime() + " ms.");
            }
        });
    }

    public File saveToFile() {

        final File largeCache = new File(FileManager.getInstance().getTaskCacheDirectory(this) + File.separator + "primes");
        if (options.searchMethod == BRUTE_FORCE) {
            //sortCache(getState() == State.STOPPED);
            sortCache(false);
        } else {
            if (getState() != State.STOPPED) return largeCache;
            FileManager.getInstance().writeNumbersQuick(((SieveTask) getTasks().get(0)).primes, largeCache, false);
        }
        return largeCache;
    }

    private long getRange() {
        return options.endValue - options.startValue;
    }

    private void sortCache(final boolean delete) {
        try {

            //Main cache file
            final File cache = new File(FileManager.getInstance().getTaskCacheDirectory(this) + File.separator + "primes");
            final DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cache, false)));

            //Create readers for each sub-task cache file
            final List<DataInputStream> dataInputStreams = new ArrayList<>();
            for (File file : FileManager.getInstance().getTaskCacheDirectory(this).listFiles()) {
                if (!file.getAbsolutePath().contains("primes")) {
                    dataInputStreams.add(new DataInputStream(new FileInputStream(file)));
                }
            }

            final List<Long> numbers = new ArrayList<>();

            //Read one number from each cache file
            for (DataInputStream dataInputStream : dataInputStreams) {
                try {
                    numbers.add(dataInputStream.readLong());
                } catch (EOFException e) {
                    Log.d(TAG, "End of file reached!");
                }
            }

            while (numbers.size() > 0) {

                //Find the smallest number
                int smallestIndex = 0;
                for (int i = 0; i < numbers.size(); i++) {
                    if (numbers.get(i) < numbers.get(smallestIndex)) {
                        smallestIndex = i;
                    }
                }

                //Write smallest number to large cache
                dataOutputStream.writeLong(numbers.get(smallestIndex));

                //Refill numbers
                try {
                    numbers.remove(smallestIndex);
                    numbers.add(smallestIndex, dataInputStreams.get(smallestIndex).readLong());
                } catch (EOFException e) {
                    //Log.d(TAG, "End of file reached!");
                }
            }

            //Close readers
            for (DataInputStream dataInputStream : dataInputStreams) {
                dataInputStream.close();
            }

            //SAVE from lists
            final List<List<Long>> lists = new ArrayList<>();
            for (Task task : getTasks()) {
                lists.add(((BruteForceTask) task).primes);
            }

            final int[] indexes = new int[lists.size()];

            for (List<Long> list : lists) {
                if (list.size() > 0) {
                    numbers.add(list.get(0));
                    indexes[lists.indexOf(list)] = 1;
                }
            }

            while (numbers.size() > 0) {

                //Find the smallest number
                int smallestIndex = 0;
                for (int i = 0; i < numbers.size(); i++) {
                    if (numbers.get(i) < numbers.get(smallestIndex)) {
                        smallestIndex = i;
                    }
                }

                //Write smallest number to large cache
                dataOutputStream.writeLong(numbers.get(smallestIndex));

                //Refill numbers
                try {
                    numbers.remove(smallestIndex);
                    numbers.add(smallestIndex, lists.get(smallestIndex).get(indexes[smallestIndex]));
                    indexes[smallestIndex]++;
                } catch (IndexOutOfBoundsException e) {
                    //Log.d(TAG, "End of list reached!");
                }
            }

            dataOutputStream.close();

            //DELETE files and clear lists
            if (delete) {
                for (File file : FileManager.getInstance().getTaskCacheDirectory(this).listFiles()) {
                    if (!file.getAbsolutePath().contains("primes")) {
                        file.delete();
                    }
                }

                for (List<Long> list : lists) {
                    list.clear();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private volatile boolean takeFlag = false;

    public SearchOptions getSearchOptions() {
        return this.options;
    }

    /**
     * @return the endValue
     */
    public long getEndValue() {
        return options.endValue;
    }

    /**
     * @return the threadCount
     */
    public int getThreadCount() {
        return options.getThreadCount();
    }

    /**
     * @return the startValue
     */
    public long getStartValue() {
        return options.startValue;
    }

    public int getPrimeCount() {
        long total = 0;
        for (Task task : getTasks()) {
            if (task instanceof BruteForceTask) {
                total += ((BruteForceTask) task).primeCount;
            } else if (task instanceof SieveTask) {
                total += ((SieveTask) task).primeCount;
            }
        }
        return (int) total;
    }

    /*public List<Long> getSortedPrimes() {

        final List<Long> primes = new ArrayList<>(primeCount);

        switch (options.searchMethod) {
            case BRUTE_FORCE:
                final int[] index = new int[getTasks().size()];
                for (int i = 0; i < index.length; i++) {
                    index[i] = 0;
                }

                int li = 0;
                long lowest;
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
                primes.addAll(((SieveTask) getTasks().get(0)).primes);
                break;
        }

        return primes;
    }*/

    /**
     * Finds and returns the lowest number that is currently being checked or has already been checked for primality. If there is more than one thread, this will return the lowest {@linkplain BruteForceTask#currentNumber} out of all threads.
     *
     * @return The lowest {@linkplain BruteForceTask#currentNumber}.
     */
    public long getCurrentValue() {
        if (getTasks().size() == 0) return 0;
        if (options.searchMethod == SIEVE_OF_ERATOSTHENES) {
            if (getState() == State.STOPPED) {
                return options.endValue;
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
        this.options = searchOptions;
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

        private final Object QUEUE_LOCK = new Object();

        private int bufferSize = -1;

        private long primeCount = 0;

        private BruteForceTask(final long startValue, final long endValue, final int increment) {
            this.startValue = startValue;
            this.endValue = endValue;
            this.increment = increment;
            this.currentNumber = startValue;
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

            boolean running = true;

            while (running && (currentNumber <= endValue || endValue == INFINITY)) {

                /*
                Get the square root of the number. We only need to calculate up to the square root
                to determine if the number is prime. The square root of a long will always fit
                inside the value range of an int.
                 */
                final int sqrtMax = (int) Math.sqrt(currentNumber);

                // Assume the number is prime
                boolean isPrime = true;

                // Check if the number is divisible by every odd number below it's square root.
                for (int i = 3; i <= sqrtMax; i += 2) {

                    /*
                    TODO: Optimization
                    Ideally, this check should go after the check for primality so it does not get
                    called every iteration. For now, this will remain here in case a thread never
                    finds a prime number.
                     */
                    tryPause();
                    if (shouldStop()) {
                        running = false;
                        break;
                    }

                    // Check if the number divides perfectly
                    if (currentNumber % i == 0) {
                        isPrime = false;
                        break;
                    }
                }

                // Check if the number was prime
                if (isPrime) {
                    dispatchPrimeFound(currentNumber);
                }

                currentNumber += increment;
            }
        }

        @Override
        public float getProgress() {
            if (endValue == INFINITY) return 0;
            if (getState() != State.STOPPED) {
                setProgress((float) (currentNumber - startValue) / (endValue - startValue));
            }
            return super.getProgress();
        }

        public long getCurrentValue() {
            return this.currentNumber;
        }

        private void dispatchPrimeFound(final long number) {
            try {
                primes.add(number);
                primeCount++;
            }catch (OutOfMemoryError e){
                System.out.println("[ERROR] Out of memory with " + primes.size() + " primes!");
                //Write all to cache
            }


            if (bufferSize != -1 && primes.size() >= bufferSize) {
                Log.d(TAG, "Swapping to disk! Size: " + primes.size());

                //Swap memory to disk
                if (!taskDirectory.exists()) {
                    taskDirectory.mkdirs();
                }

                //Write to 1 file with no commas use byte offset for quick reads
                FileManager.getInstance().writeNumbersQuick(primes, new File(FileManager.getInstance().getTaskCacheDirectory(FindPrimesTask.this) + File.separator + getId() + ".cache"), true);
                primes.clear();
            }
        }

    }

    private class SieveTask extends MultithreadedTask.SubTask {

        private String status = "searching";

        /**
         * Prime numbers are added to this queue. We are using an {@link ArrayDeque} here because it
         * has the best performance for inserting elements at the end of the collection.
         */
        private final Queue<Long> primes = new ArrayDeque<>();

        private final int sqrtMax = (int) Math.sqrt(options.endValue);
        private int factor;
        private long counter;

        private long primeCount = 0;

        @Override
        protected void run() {
            //Assume all numbers are prime
            final BitSet bitSet = new BitSet((int) (options.endValue + 1));
            bitSet.set(0, bitSet.size() - 1, true);

            // mark non-primes <= n using Sieve of Eratosthenes
            for (factor = 2; factor <= sqrtMax; factor++) {

                // if factor is prime, then mark multiples of factor as nonprime
                // suffices to consider mutiples factor, factor+1, ...,  n/factor
                if (bitSet.get(factor)) {
                    for (int j = factor; factor * j <= options.endValue; j++) {
                        bitSet.set(factor * j, false);
                    }
                }

                tryPause();
            }

            if (shouldStop()) {
                return;
            }

            status = "counting";

            //Count primes
            for (counter = (options.startValue > 2 ? options.startValue : 2); counter <= options.endValue; counter++) {
                if (bitSet.get((int) counter)) {
                    primes.add(counter);
                    primeCount++;
                }
                tryPause();
            }

            status = String.valueOf(getState());
        }

        @Override
        public float getProgress() {
            switch (status) {
                case "searching":
                    setProgress(((float) factor / sqrtMax) / 2);
                    break;

                case "counting":
                    setProgress(0.5f + (((float) counter / options.endValue) / 2));
                    break;
            }
            return super.getProgress();
        }

        public int getFactor() {
            return factor;
        }
    }

    public int getCurrentFactor() {
        return ((SieveTask) getTasks().get(0)).getFactor();
    }

    private void searchSieve() {
        addTask(new SieveTask());
        executeTasks();

        long start = System.currentTimeMillis();
        System.out.println("Version 0: " + version_0() + "\t" + (System.currentTimeMillis() - start) + " ms.");
        start = System.currentTimeMillis();
        System.out.println("Version N: " + nativeSieve(options.startValue, options.endValue) + "\t" + (System.currentTimeMillis() - start) + " ms.");
    }

    private int version_0(){
        final boolean[] array = new boolean[(int) options.endValue + 1];
        for (int i = 0; i < array.length; ++i){
            array[i] = true;
        }
        final int sqrtMax = (int) Math.sqrt(options.endValue);
        int primeCount = 0;

        // mark non-primes <= n using Sieve of Eratosthenes
        for (int factor = 2; factor <= sqrtMax; factor++) {

            // if factor is prime, then mark multiples of factor as nonprime
            // suffices to consider mutiples factor, factor+1, ...,  n/factor
            if (array[factor]) {
                for (long j = factor; factor * j <= options.endValue; j++) {
                    final long number = factor * j;
                    array[(int) number] = false;
                }
            }
        }

        //Count primes
        for (int counter = (int) (options.startValue > 2 ? options.startValue : 2); counter <= options.endValue; counter++) {
            if (array[counter]) {
                primeCount++;
            }
        }

        return primeCount;
    }

    public String getStatus() {
        switch (options.searchMethod) {
            case BRUTE_FORCE:
                break;

            case SIEVE_OF_ERATOSTHENES:
                if (getTasks().size() > 0) {
                    return ((SieveTask) getTasks().get(0)).status;
                }
                break;
        }
        return String.valueOf(getState());
    }

    public static class SearchOptions extends GeneralSearchOptions {

        /**
         * The value to start the search from. Inclusive.
         */
        private long startValue;

        /**
         * The value to stop the search on. Inclusive.
         */
        private long endValue;

        public enum SearchMethod {
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

        private enum SearchMode {
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

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final int threadCount, final boolean notifyWhenFinished, final boolean autoSave) {
            super(threadCount, notifyWhenFinished, autoSave);
            this.startValue = startValue;
            this.endValue = endValue;
            this.searchMethod = searchMethod;
        }

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final int threadCount) {
            this(startValue, endValue, searchMethod, threadCount, false, false);
        }

        public SearchOptions(final long startValue, final long endValue) {
            this(startValue, endValue, BRUTE_FORCE, 1, false, false);
        }

        private SearchOptions(final Parcel parcel) {
            super(parcel);
            this.startValue = parcel.readLong();
            this.endValue = parcel.readLong();
            this.searchMethod = (SearchMethod) parcel.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.startValue);
            dest.writeLong(this.endValue);
            dest.writeSerializable(this.searchMethod);
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
    }

    private boolean saved = false;

    @Override
    public boolean save() {
        try {
            FileManager.copy(saveToFile(), new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "Prime numbers from " + getStartValue() + " to " + (getEndValue() == FindPrimesTask.INFINITY ? getCurrentValue() : getEndValue()) + EXTENSION));
            saved = true;
            sendOnSaved();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendOnError();
        return false;
    }

    private CopyOnWriteArrayList<SaveListener> saveListeners = new CopyOnWriteArrayList<>();

    public void addSaveListener(final SaveListener listener) {
        saveListeners.add(listener);
    }

    public void removeSaveListener(final SaveListener listener) {
        saveListeners.remove(listener);
    }

    private void sendOnSaved() {
        for (SaveListener listener : saveListeners) {
            listener.onSaved();
        }
    }

    private void sendOnError() {
        for (SaveListener listener : saveListeners) {
            listener.onError();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
