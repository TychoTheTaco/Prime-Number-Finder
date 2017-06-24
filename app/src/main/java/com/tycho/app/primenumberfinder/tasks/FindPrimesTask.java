package com.tycho.app.primenumberfinder.tasks;

import com.tycho.app.primenumberfinder.utils.Task;

import net.jodah.expiringmap.ExpiringMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */

public class FindPrimesTask extends Task{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesTask";

    /**
     * List of all prime numbers found.
     */
    private final List<Long> primeNumbers = new ArrayList<>();

    /**
     * Start and end values. The end value can be modified after the task has started if the user
     * pauses it first. If the end value is set to {@link #LIMIT_NO_LIMIT}, then the task will run
     * forever until stopped by the user or interrupted.
     */
    private final long startValue;
    private long endValue;

    /**
     * If the end value is set to this, then the runnable will run forever until stopped by the
     * user or interrupted.
     */
    public static final int LIMIT_NO_LIMIT = -1;

    /**
     * The current number we are checking.
     */
    private long currentNumber;

    /**
     * The search progress on the current number as a decimal between 0 and 1.
     */
    private float currentProgress = 0;

    private final SearchOptions.Method searchMethod;
    private SearchOptions.MonitorType monitorType;


    private long totalCheckTime;
    private int numbersChecked;

    private final List<EventListener> eventListeners = new ArrayList<>();


    private long[] lastUpdateTimes = new long[2];


    Map<Long, Long> map;
    Map<Long, Long> primesMap;

    public FindPrimesTask(SearchOptions searchOptions){
        this.startValue = searchOptions.getStartValue();
        this.endValue = searchOptions.getEndValue();
        this.searchMethod = searchOptions.getSearchMethod();
        this.monitorType = searchOptions.getMonitorType();

        map = ExpiringMap.builder().expiration(1000, TimeUnit.MILLISECONDS).build();
        primesMap = ExpiringMap.builder().expiration(1000, TimeUnit.MILLISECONDS).build();
    }

    @Override
    public void run(){

        //The task has started
        dispatchStarted();

        switch (searchMethod){

            case BRUTE_FORCE:
                searchBruteForce();
                break;

            case SIEVE_OF_ERATOSTHENES:
                searchSieveOfEratosthenes();
                break;
        }

        dispatchStopped();

        //The task has finished
        if (currentNumber == endValue){
            dispatchFinished();
        }

    }

    private void searchBruteForce(){
        /**
         * Set the current number to the start value. If the start value is less then 2, then set
         * it to 2 because that is the lowest prime number.
         */
        if (startValue < 3){
            if (endValue >= 2){
                sendOnPrimeFound(2);
                primeNumbers.add(2L);
            }
            currentNumber = 3;
        }else{
            currentNumber = startValue;
        }

        int sqrtMax;
        boolean isPrime;

        boolean running = true;

        //Loop forever
        while (running){

            //Check if the end value has been reached
            if (endValue == LIMIT_NO_LIMIT || currentNumber <= endValue){

                //Check if the number is divisible by 2
                if (currentNumber % 2 != 0){

                    /**
                     * Get the square root of the number. We only need to calculate up to the square
                     * root to determine if the number is prime. The square root of a long will
                     * always fit inside the value range of an int.
                     */
                    sqrtMax = (int) Math.sqrt(currentNumber);

                    //Assume the number is prime
                    isPrime = true;

                    final long checkStartTime = System.nanoTime();

                    /**
                     * Check if the number is divisible by every odd number below it's square root.
                     */
                    for (int i = 3; i <= sqrtMax; i += 2){

                        //Check if the number divides perfectly
                        if (currentNumber % i == 0){
                            isPrime = false;
                            break;
                        }

                        /*if (monitorType == SearchOptions.MonitorType.SIMPLE){
                            //Calculate current progress
                            currentProgress = (float) i / sqrtMax;
                            //if ((getAverageCheckTime() / 1000000) >= 16){
                                sendOnProgressChanged(currentProgress);
                            //}
                        }*/

                        //Check if we should pause
                        //tryPause();
                        if (requestPause){
                            pauseThread();
                        }
                        if (/*shouldStop()*/requestStop){
                            running = false;
                            break;
                        }
                    }

                    numbersChecked++;
                    totalCheckTime += (System.nanoTime() - checkStartTime);

                    //Check if the number was prime
                    if (isPrime){
                        synchronized (LOCK){
                            primeNumbers.add(currentNumber);
                            sendOnPrimeFound(currentNumber);
                            //primesMap.put(currentNumber, currentNumber);
                        }

                        //sendOnPrimeFound(currentNumber);
                    }
                }

               /* if ((getAverageCheckTime() / 1000000) < 16){
                    sendOnProgressChanged(currentProgress);
                }*/

                //Increase currentNumber
                currentNumber++;
                //map.put(currentNumber, currentNumber);

                //Calculate total progress
                /*if (monitorType == SearchOptions.MonitorType.SIMPLE){
                    if (endValue != LIMIT_NO_LIMIT){
                        setProgress((float) (currentNumber - startValue) / (endValue - startValue));
                    }
                }*/


            }else{
                currentNumber = endValue;
                currentProgress = 1f;
                setProgress(1);
                //isRunning = false;
                break;
            }
        }
    }

    private void searchSieveOfEratosthenes(){
        //int n = Integer.parseInt(args[0]);
        long n = endValue;

        // initially assume all integers are prime
        boolean[] isPrime = new boolean[(int) (n+1)];
        //final List<Boolean> isPrime = new ArrayList();
        for (int i = (int) startValue; i <= n; i++) {
            isPrime[i] = true;
            //isPrime.add(i, true);
        }

        // mark non-primes <= n using Sieve of Eratosthenes
        for (int factor = 2; factor*factor <= n; factor++) {

            currentNumber = factor;
            // if factor is prime, then mark multiples of factor as nonprime
            // suffices to consider mutiples factor, factor+1, ...,  n/factor
            if (isPrime[factor] /*isPrime.get(factor)*/) {
                for (int j = factor; factor*j <= n; j++) {
                    isPrime[factor*j] = false;
                    //isPrime.set(factor * j, false);
                }
            }
        }

        // count primes
        int primes = 0;
        for (int i = 2; i <= n; i++) {
            if (isPrime[i]){
                primeNumbers.add((long) i);
                sendOnPrimeFound(i);
                primes++;
            }
        }
        System.out.println("The number of primes <= " + n + " is " + primes);

        currentNumber = endValue;
    }

    public long getAverageCheckTime(){
        if (numbersChecked < 1) return 0;
        return (totalCheckTime - getTotalPauseTime()) / numbersChecked;
    }

    public void setEndValue(long endValue){
        this.endValue = endValue;
    }

    public float getCurrentProgress(){
        return currentProgress;
    }

    public long getCurrentNumber(){
        return currentNumber;
    }

    public long getEndValue(){
        return endValue;
    }

    public long getStartValue(){
        return startValue;
    }

    public long getNumbersPerSecond(){
        return map.size();
    }

    public List<Long> getPrimeNumbers(){
        return primeNumbers;
    }

    public long getPrimesPerSecond(){
        return primesMap.size();
    }

    public void addEventListener(final EventListener eventListener){
        if (!eventListeners.contains(eventListener)) eventListeners.add(eventListener);
    }

    public interface EventListener{
        void onPrimeFound(final long prime);
    }

    private void sendOnPrimeFound(final long prime){
        /*for (EventListener eventListener : eventListeners){
            eventListener.onPrimeFound(prime);
        }*/
        for (int i = 0; i < eventListeners.size(); i++){
            eventListeners.get(i).onPrimeFound(prime);
        }
    }

    public static class SearchOptions{

        private long startValue;
        private long endValue;

        public enum Method{
            BRUTE_FORCE,
            SIEVE_OF_ERATOSTHENES
        }

        public enum MonitorType{
            NONE,
            SIMPLE,
            ADVANCED
        }

        private Method searchMethod;
        private MonitorType monitorType;

        public SearchOptions(){}

        public SearchOptions(long startValue, long endValue, Method searchMethod, MonitorType monitorType){
            this.startValue = startValue;
            this.endValue = endValue;
            this.searchMethod = searchMethod;
            this.monitorType = monitorType;
        }

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

        public Method getSearchMethod(){
            return searchMethod;
        }

        public void setSearchMethod(Method searchMethod){
            this.searchMethod = searchMethod;
        }

        public MonitorType getMonitorType(){
            return monitorType;
        }

        public void setMonitorType(MonitorType monitorType){
            this.monitorType = monitorType;
        }
    }
}
