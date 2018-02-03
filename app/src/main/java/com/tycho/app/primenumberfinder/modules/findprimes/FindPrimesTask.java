package com.tycho.app.primenumberfinder.modules.findprimes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.MultithreadedTask;
import easytasks.Task;
import easytasks.TaskAdapter;

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
    }

    @Override
    protected void run() {

        //Start worker threads
        System.out.println("Starting " + threadCount + " threads...");

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

                @Override
                public void onProgressChanged(float v) {

                }
            });
            addTask(task);
        }

        executeTasks();

        System.out.println("All threads stopped.");
        long time = 0;
        for (Task task : getTasks()) {
            time += task.getElapsedTime();
            System.out.println("Task " + ((BruteForceTask) task).startValue + "\n    " + ((BruteForceTask) task).primes.size() + " primes\n    "
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

    //final List<Long> primes = new ArrayList<>();

    public List<Long> getPrimes() {
        final List<Long> primes = new ArrayList<>();
        for (Task task : getTasks()) {
            primes.addAll(((BruteForceTask) task).getPrimes());
        }
        return primes;
    }

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

        public final List<Long> primes = new ArrayList<>();

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
            primes.add(number);
            sendOnPrimeFound(number);
        }

        public List<Long> getPrimes() {
            return this.primes;
        }

    }

    // Android

    private final List<EventListener> eventListeners = new CopyOnWriteArrayList<>();

    public interface EventListener {
        void onPrimeFound(final long prime);

        void onErrorOccurred(final Object error);
    }

    private void sendOnPrimeFound(final long prime) {
        for (int i = 0; i < eventListeners.size(); i++) {
            eventListeners.get(i).onPrimeFound(prime);
        }
    }

    public long getPrimesPerSecond() {
        return 0;
    }

    public long getNumbersPerSecond() {
        return 0;
    }

    public void addEventListener(final EventListener eventListener) {
        if (!eventListeners.contains(eventListener)) eventListeners.add(eventListener);
    }

    public boolean removeEventListener(final EventListener eventListener) {
        return eventListeners.remove(eventListener);
    }

    public static class SearchOptions {

        /**
         * The value to start the search from. Inclusive.
         */
        private long startValue;

        /**
         * The value to stop the search on. Inclusive.
         */
        private long endValue;

        public enum MonitorType {
            NONE,
            SIMPLE,
            ADVANCED
        }

        /**
         * The search method to use.
         */
        private SearchMethod searchMethod;

        private MonitorType monitorType;

        private int threadCount;

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final MonitorType monitorType) {
            this.startValue = startValue;
            this.endValue = endValue;
            this.searchMethod = searchMethod;
            this.monitorType = monitorType;
        }

        public SearchOptions(final long startValue, final long endValue, final SearchMethod searchMethod, final MonitorType monitorType, final int threadCount) {
            this(startValue, endValue, searchMethod, monitorType);
            this.threadCount = threadCount;
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

        public MonitorType getMonitorType() {
            return monitorType;
        }

        public void setMonitorType(MonitorType monitorType) {
            this.monitorType = monitorType;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }
    }
}
