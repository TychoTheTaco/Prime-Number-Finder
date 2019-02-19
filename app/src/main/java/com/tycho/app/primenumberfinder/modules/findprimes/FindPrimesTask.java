package com.tycho.app.primenumberfinder.modules.findprimes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tycho.app.primenumberfinder.FPT;
import com.tycho.app.primenumberfinder.ITask;
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

public class FindPrimesTask extends MultithreadedTask implements FPT {

    static {
        System.loadLibrary("native-utils");
        System.loadLibrary("Taskr");
    }

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
        if (options.getThreadCount() == 1){
            options.setSearchMode(SearchOptions.SearchMode.PARTITION);
        }else if (options.getThreadCount() % 2 == 0) {
            options.setSearchMode(SearchOptions.SearchMode.ALTERNATE);
        } else {
            options.setSearchMode(SearchOptions.SearchMode.PACKET);
        }

        switch (options.getSearchMode()) {
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
        }
    }

    /**
     * This method prepares the task for searching using the partition mode. The partition mode divides the search range into even partitions based on the
     * number of threads available. Each thread then is responsible for searching through a single partition.
     */
    private void preparePartitionMode() {
        final long partitionSize = getRange() / options.getThreadCount();
        System.out.println("partition size: " + partitionSize);
        for (int i = 0; i < options.getThreadCount(); i++) {
            long start = options.getStartValue() + (i * partitionSize + 1);
            if (start % 2 == 0) start++;
            final BruteForceTask task = new BruteForceTask(start, options.getStartValue() + (i + 1) * partitionSize, 2);
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
        startValues[0] = (options.getStartValue() % 2 == 0) ? (options.getStartValue() + 1) : options.getStartValue();
        for (int i = 0; i < startValues.length; i++) {
            long s = i == 0 ? startValues[0] : startValues[i - 1] + 2;
            if (s % 2 == 0) {
                s -= 1;
            }
            startValues[i] = s;
            final BruteForceTask task = new BruteForceTask(s, options.getEndValue(), increment);
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
            long start = options.getStartValue() + (i * packetSize + 1);
            if (start % 2 == 0) start++;
            final BruteForceTask task = new BruteForceTask(start, Math.min(options.getStartValue() + (i + 1) * packetSize, options.getEndValue()), 2);
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

    public void saveToFile(final File file) {
        /*final File largeCache = new File(FileManager.getInstance().getTaskCacheDirectory(this) + File.separator + "primes");
        if (options.getSearchMethod() == SearchOptions.SearchMethod.BRUTE_FORCE) {
            //sortCache(getState() == State.STOPPED);
            sortCache(false);
        } else {
            if (getState() != State.STOPPED) return largeCache;
            FileManager.getInstance().writeNumbersQuick(((SieveTask) getTasks().get(0)).primes, largeCache, false);
        }
        return largeCache;*/
    }

    private long getRange() {
        return options.getEndValue() - options.getStartValue();
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

    public SearchOptions getSearchOptions() {
        return this.options;
    }

    /**
     * @return the endValue
     */
    public long getEndValue() {
        return options.getEndValue();
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
        return options.getStartValue();
    }

    public int getPrimeCount() {
        long total = 0;
        //TODO: Potential concurrent modification exception if we are removing tasks as they finish. We need to synchronize
        for (Task task : getTasks()) {
            if (task instanceof BruteForceTask) {
                total += ((BruteForceTask) task).primeCount;
            } else if (task instanceof SieveTask) {
                total += ((SieveTask) task).primeCount;
            }
        }
        return (int) total;
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

            while ((currentNumber <= endValue || endValue == INFINITY)) {

                tryPause();
                if (shouldStop()) return;

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

        private void dispatchPrimeFound(final long number) {
            try {
                primes.add(number);
                primeCount++;
            } catch (OutOfMemoryError e) {
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

        private final int sqrtMax = (int) Math.sqrt(options.getEndValue());
        private int factor;
        private long counter;

        private long primeCount = 0;

        @Override
        protected void run() {
            //Assume all numbers are prime
            final BitSet bitSet = new BitSet((int) (options.getEndValue() + 1));
            bitSet.set(0, bitSet.size() - 1, true);

            // mark non-primes <= n using Sieve of Eratosthenes
            for (factor = 2; factor <= sqrtMax; factor++) {

                // if factor is prime, then mark multiples of factor as nonprime
                // suffices to consider mutiples factor, factor+1, ...,  n/factor
                if (bitSet.get(factor)) {
                    for (int j = factor; factor * j <= options.getEndValue(); j++) {
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
            for (counter = (options.getStartValue() > 2 ? options.getStartValue() : 2); counter <= options.getEndValue(); counter++) {
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
                    setProgress(0.5f + (((float) counter / options.getEndValue()) / 2));
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
    }

    public String getStatus() {
        switch (options.getSearchMethod()) {
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


    private boolean saved = false;

    @Override
    public boolean save() {
        /*try{
            FileManager.copy(saveToFile(), new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "Prime numbers from " + getStartValue() + " to " + (getEndValue() == FindPrimesTask.INFINITY ? getCurrentValue() : getEndValue()) + EXTENSION));
            saved = true;
            sendOnSaved();
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }*/
        sendOnError();
        return false;
    }

    private CopyOnWriteArrayList<Savable.SaveListener> saveListeners = new CopyOnWriteArrayList<>();

    public void addSaveListener(final Savable.SaveListener listener) {
        saveListeners.add(listener);
    }

    public void removeSaveListener(final Savable.SaveListener listener) {
        saveListeners.remove(listener);
    }

    private void sendOnSaved() {
        for (Savable.SaveListener listener : saveListeners) {
            listener.onSaved();
        }
    }

    private void sendOnError() {
        for (Savable.SaveListener listener : saveListeners) {
            listener.onError();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
