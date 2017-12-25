package com.tycho.app.primenumberfinder.modules.findprimes;

import android.util.Log;

import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;

import net.jodah.expiringmap.ExpiringMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import easytasks.Task;


/**
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */

public class FindPrimesTask extends Task {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesTask";

    /**
     * List of all prime numbers found.
     */
    private final List<Long> primes = new LinkedList<>();

    /*
     * Start and end values. The end value can be modified after the task has started if the user
     * pauses it first. If the end value is set to {@link #END_VALUE_INFINITY}, then the task will run
     * forever until stopped by the user or interrupted.
     */
    private final long startValue;
    private long endValue;

    /**
     * If the end value is set to this, then the task will run forever until stopped by the
     * user or interrupted.
     */
    public static final int END_VALUE_INFINITY = -1;

    /**
     * The current number we are checking.
     */
    private long currentNumber;

    private boolean finished = false;

    private final SearchOptions.Method searchMethod;
    private SearchOptions.MonitorType monitorType;

    private final List<EventListener> eventListeners = new CopyOnWriteArrayList<>();

    private long[][] statistics = new long[2][3];
    private static final int NUMBERS_PER_SECOND = 0;
    private static final int PRIMES_PER_SECOND = 1;
    private static final int CURRENT_VALUE = 0;
    private static final int SINCE_LAST = 1;
    private static final int LAST_UPDATE_TIME = 2;

    public FindPrimesTask(SearchOptions searchOptions){
        this.startValue = searchOptions.getStartValue();
        this.endValue = searchOptions.getEndValue();
        this.searchMethod = searchOptions.getSearchMethod();
        this.monitorType = searchOptions.getMonitorType();

        statistics[NUMBERS_PER_SECOND][CURRENT_VALUE] = -1;
        statistics[PRIMES_PER_SECOND][CURRENT_VALUE] = -1;
    }

    @Override
    public void run(){

        switch (searchMethod){

            case BRUTE_FORCE:
                searchBruteForce();
                break;

            case SIEVE_OF_ERATOSTHENES:
                try {
                    searchSieveOfEratosthenes();
                }catch (OutOfMemoryError e){
                    sendOnExceptionOccurred(e);
                }
                break;
        }

        //The task has finished
        if (currentNumber == endValue){
            finished = true;
        }

        Log.d(TAG, "Stopped with : " + statistics[NUMBERS_PER_SECOND][CURRENT_VALUE] + " " + statistics[NUMBERS_PER_SECOND][SINCE_LAST]);

    }

    private void searchBruteForce(){

        statistics[NUMBERS_PER_SECOND][LAST_UPDATE_TIME] = System.currentTimeMillis();
        statistics[PRIMES_PER_SECOND][LAST_UPDATE_TIME] = System.currentTimeMillis();

        /*
         * Set the current number to the start value. If the start value is less then 2, then set
         * it to 2 because that is the lowest prime number.
         */
        if (startValue < 3){
            if (endValue >= 2){
                sendOnPrimeFound(2);
                primes.add(2L);
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
            if (currentNumber <= endValue || endValue == END_VALUE_INFINITY){

                //Check if the number is divisible by 2
                if (currentNumber % 2 != 0){

                    /*
                     * Get the square root of the number. We only need to calculate up to the square
                     * root to determine if the number is prime. The square root of a long will
                     * always fit inside the value range of an int.
                     */
                    sqrtMax = (int) Math.sqrt(currentNumber);

                    //Assume the number is prime
                    isPrime = true;

                    /*
                     * Check if the number is divisible by every odd number below it's square root.
                     */
                    for (int i = 3; i <= sqrtMax; i += 2){

                        //Check if the number divides perfectly
                        if (currentNumber % i == 0){
                            isPrime = false;
                            break;
                        }

                        //Check if we should pause
                        tryPause();
                        if (/*shouldStop()*/requestStop){
                            running = false;
                            break;
                        }
                    }

                    //Check if the number was prime
                    if (isPrime){
                        statistics[PRIMES_PER_SECOND][SINCE_LAST]++;
                        synchronized (LOCK){
                            primes.add(currentNumber);
                            sendOnPrimeFound(currentNumber);
                        }
                    }
                }

                currentNumber++;
                statistics[NUMBERS_PER_SECOND][SINCE_LAST] += 2;

                //Update statistics
                if (monitorType != SearchOptions.MonitorType.NONE) {

                    final long currentTime = System.currentTimeMillis();

                    if (currentTime - statistics[NUMBERS_PER_SECOND][LAST_UPDATE_TIME] >= 1000){
                        statistics[NUMBERS_PER_SECOND][CURRENT_VALUE] = statistics[NUMBERS_PER_SECOND][SINCE_LAST];
                        statistics[NUMBERS_PER_SECOND][SINCE_LAST] = 0;
                        statistics[NUMBERS_PER_SECOND][LAST_UPDATE_TIME] = currentTime;
                    }

                    if (currentTime - statistics[PRIMES_PER_SECOND][LAST_UPDATE_TIME] >= 1000){
                        statistics[PRIMES_PER_SECOND][CURRENT_VALUE] = statistics[PRIMES_PER_SECOND][SINCE_LAST];
                        statistics[PRIMES_PER_SECOND][SINCE_LAST] = 0;
                        statistics[PRIMES_PER_SECOND][LAST_UPDATE_TIME] = currentTime;
                    }
                }

                //Calculate total progress
                if (endValue != END_VALUE_INFINITY){
                    setProgress(((float) (currentNumber - startValue) / (endValue - startValue)));
                }

            }else{
                currentNumber = endValue;
                setProgress(1);
                break;
            }
        }
    }

    private void searchSieveOfEratosthenes(){

        // initially assume all integers are prime
        boolean[] isPrime = new boolean[(int) (endValue + 1)];
        //final List<Boolean> isPrime = new ArrayList();
        for (int i = (int) startValue; i <= endValue; i++) {
            isPrime[i] = true;
            //isPrime.add(i, true);
        }

        // mark non-primes <= n using Sieve of Eratosthenes
        for (int factor = 2; factor*factor <= endValue; factor++) {

            currentNumber = factor;
            // if factor is prime, then mark multiples of factor as nonprime
            // suffices to consider mutiples factor, factor+1, ...,  n/factor
            if (isPrime[factor] /*isPrime.get(factor)*/) {
                for (int j = factor; factor*j <= endValue; j++) {
                    isPrime[factor*j] = false;
                    //isPrime.set(factor * j, false);
                }
            }

            setProgress((float) ((factor * factor) / 2) / endValue);
        }

        // count primes
        int primes = 0;
        for (int i = 2; i <= endValue; i++) {
            if (isPrime[i]){
                this.primes.add((long) i);
                sendOnPrimeFound(i);
                tryPause();
                primes++;
            }
            //setProgress((float) ((i / 2) + (endValue / 2)) / endValue);
        }
        System.out.println("The number of primes <= " + endValue + " is " + primes);

        currentNumber = endValue;
    }

    public void setEndValue(long endValue){
        this.endValue = endValue;
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
        if (statistics[NUMBERS_PER_SECOND][CURRENT_VALUE] == -1){
            return statistics[NUMBERS_PER_SECOND][SINCE_LAST];
        }
        return statistics[NUMBERS_PER_SECOND][CURRENT_VALUE];
    }

    public List<Long> getPrimes(){
        return primes;
    }

    public long getPrimesPerSecond(){
        if (statistics[PRIMES_PER_SECOND][CURRENT_VALUE] == -1){
            return statistics[PRIMES_PER_SECOND][SINCE_LAST];
        }
        return statistics[PRIMES_PER_SECOND][CURRENT_VALUE];
    }

    public void addEventListener(final EventListener eventListener){
        if (!eventListeners.contains(eventListener)) eventListeners.add(eventListener);
    }

    public boolean removeEventListener(final EventListener eventListener){
        return eventListeners.remove(eventListener);
    }

    public interface EventListener{
        void onPrimeFound(final long prime);
        void onErrorOccurred(final Object error);
    }

    private void sendOnPrimeFound(final long prime){
        for (int i = 0; i < eventListeners.size(); i++){
            eventListeners.get(i).onPrimeFound(prime);
        }
    }

    private void sendOnExceptionOccurred(final Object error){
        for (int i = 0; i < eventListeners.size(); i++){
            eventListeners.get(i).onErrorOccurred(error);
        }
    }

    public boolean isFinished(){
        return this.finished;
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
