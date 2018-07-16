package com.tycho.app.primenumberfinder.modules.findprimes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tycho.app.primenumberfinder.Savable;
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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import easytasks.MultithreadedTask;
import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.FileManager.EXTENSION;

public class FindPrimesTask extends MultithreadedTask implements Savable {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesTask.class.getSimpleName();

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

    public FindPrimesTask(final SearchOptions searchOptions) {
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
            if (s % 2 == 0) {
                s -= 1;
            }

            //If 's' is divisible by 'threadCount', increment it by 2 to avoid a thread with all non-primes
            /*Log.e(TAG, "s: " + s + " " + (s % threadCount));
            if (threadCount != 1 && s % threadCount == 0){
                s += 2;
            }*/

            final BruteForceTask task = new BruteForceTask(s, endValue, threadCount * 2);
            task.bufferSize = searchOptions.bufferSize / threadCount;
            addTask(task);
        }

        /*
        1 7  13
        3 9  15
        5 11 17

        if first number is multiple of threadcount, redo it

        29  35  41
        31  37  43
        33  39  45

        29  37
        31  39
        35  41
         */

        final long[] startValues = new long[threadCount];
        for (int i = 0; i < threadCount; i++){
            long s = startValue + (2 * i + 1);
            if (s % 2 == 0) {
                s -= 1;
            }
            startValues[i] = s;
        }

        /*final long partition = (endValue - startValue) / threadCount;
        for (int i = 0; i < threadCount; i++){
            long start = partition * i;
            if (start % 2 == 0){
                start++;
            }
            addTask(new BruteForceTask(start, partition * (i + 1), 2));
        }*/

        executeTasks();

        /*
        NEW VERSION
        10,000,000 - 4 threads: 65 sec
        10,000,000 - 3 threads: 84 sec
        1,000,000 - 4 threads: 2.7 sec
        1,000,000 - 3 threads: 3.1 sec

        OLD VERSION
        10,000,000 - 4 threads: 55 sec
        10,000,000 - 3 threads: 98 sec
        1,000,000 - 4 threads: 2.6 sec
        1,000,000 - 3 threads: 3.7 sec
         */

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

        //final long sortStart = System.currentTimeMillis();
        //System.out.println("Merging cache...");

        //mergeCache();

        //System.out.println("Finished merge in " + (System.currentTimeMillis() - sortStart) + " ms.");
    }

    public File saveToFile(){

        final File largeCache = new File(FileManager.getInstance().getTaskCacheDirectory(this) + File.separator + "primes");
        if (searchMethod == SearchMethod.BRUTE_FORCE) {
            //sortCache(getState() == State.STOPPED);
            sortCache(false);
        } else {
            if (getState() != State.STOPPED) return largeCache;
            FileManager.getInstance().writeNumbersQuick(((SieveTask) getTasks().get(0)).primes, largeCache, false);
        }
        return largeCache;
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

            //Save from lists
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

            //Delete files and clear lists
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
                    primes.add(((SieveTask) getTasks().get(0)).primes.get(i));
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

    /*public long getNumbersPerSecond(){
        long total = 0;
        for (Task task : getTasks()) {
            total += ((BruteForceTask) task).getCurrentValue();
        }
        return total;
    }*/

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

        private int bufferSize = -1;

        private BruteForceTask(final long startValue, final long endValue, final int increment) {
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

            boolean running = true;

            while (running && (currentNumber <= endValue || endValue == -1)) {

                /*
                 * Get the square root of the number. We only need to calculate up to the square
                 * root to determine if the number is prime. The square root of a long will
                 * always fit inside the value range of an int.
                 */
                final int sqrtMax = (int) Math.sqrt(currentNumber);

                // Assume the number is prime
                boolean isPrime = true;

                /*
                 * Check if the number is divisible by every odd number below it's square root.
                 */
                for (int i = 3; i <= sqrtMax; i += 2) {

                    // Check if we should pause
                    // Ideally, this check should go after the check for primality so it does not get
                    // called every iteration. For now, this will remain here in case a thread
                    // never finds a prime number.
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

            //Log.e(TAG, "SubTask: " + this + " finished. (" + startValue + ", " + endValue + ")");
        }

        @Override
        public float getProgress() {
            if (endValue == -1) return 0;
            if (getState() != State.STOPPED){
                setProgress((float) (currentNumber - startValue) / (endValue - startValue));
            }
            return super.getProgress();
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

       /* public List<Long> getPrimes() {
            return this.primes;
        }*/

    }

    private class SieveTask extends MultithreadedTask.SubTask {

        private String status = "searching";

        private final List<Long> primes = new ArrayList<>();

        private final int sqrtMax = (int) Math.sqrt(endValue);
        private int factor;
        private long counter;

        @Override
        protected void run() {
            //Assume all numbers are prime
            final BitSet bitSet = new BitSet((int) (endValue + 1));
            bitSet.set(0, bitSet.size() - 1, true);

            //final int sqrtMax = (int) Math.sqrt(endValue);

            // mark non-primes <= n using Sieve of Eratosthenes
            for (factor = 2; factor <= sqrtMax; factor++) {

                // if factor is prime, then mark multiples of factor as nonprime
                // suffices to consider mutiples factor, factor+1, ...,  n/factor
                if (bitSet.get(factor)) {
                    for (int j = factor; factor * j <= endValue; j++) {
                        bitSet.set(factor * j, false);
                    }
                }

                tryPause();
            }

            if (shouldStop()){
                return;
            }

            status = "counting";

            //Count primes
            for (counter = (startValue > 2 ? startValue : 2); counter <= endValue; counter++) {
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
            switch (status){
                case "searching":
                    setProgress(((float) factor / sqrtMax) / 2);
                    break;

                case "counting":
                    setProgress(0.5f + (((float) counter / endValue) / 2));
                    break;
            }
            return super.getProgress();
        }
    }

    private void searchSieve() {
        addTask(new SieveTask());
        executeTasks();
    }

    public String getStatus(){
        switch (searchMethod){
            case BRUTE_FORCE:
                break;

            case SIEVE_OF_ERATOSTHENES:
                if (getTasks().size() > 0){
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

        /**
         * The search method to use.
         */
        private SearchMethod searchMethod;

        /**
         * The maximum size of prime numbers in memory. If more numbers are found, the entire list will be saved onto the disk first.
         */
        private int bufferSize = 100_000;

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
            this(startValue, endValue, SearchMethod.BRUTE_FORCE, 1, false, false);
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

    public void addSaveListener(final SaveListener listener){
        saveListeners.add(listener);
    }

    public void removeSaveListener(final SaveListener listener){
        saveListeners.remove(listener);
    }

    private void sendOnSaved(){
        for (SaveListener listener : saveListeners){
            listener.onSaved();
        }
    }

    private void sendOnError(){
        for (SaveListener listener : saveListeners){
            listener.onError();
        }
    }

    public boolean isSaved(){
        return saved;
    }
}
