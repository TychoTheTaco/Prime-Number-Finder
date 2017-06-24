package com.tycho.app.primenumberfinder.tasks;

import com.tycho.app.primenumberfinder.utils.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class FindFactorsTask extends Task{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsTask";

    private final long number;

    private final List<Long> factors = new ArrayList<>();

    private final List<EventListener> eventListeners = new ArrayList<>();

    public FindFactorsTask(final long number){
        this.number = number;
    }

    @Override
    public void run(){
        dispatchStarted();

        float a;

        //Check each number below the number
        for (long i = 1; i <= number / 2; i++){

            if (number % i == 0){
                factors.add(i);
                sendOnFactorFound(i);
            }

            if (requestPause){
                pauseThread();
            }

            if (requestStop){
                dispatchStopped();
                return;
            }

            a = i / (number / 2);
            //setProgress((float) i / (number / 2));
            //sendOnProgressChanged(getProgress());
        }

        factors.add(number);
        sendOnFactorFound(number);

        dispatchFinished();
    }

    public void addEventListener(final EventListener eventListener){
        if (!eventListeners.contains(eventListener)) eventListeners.add(eventListener);
    }

    private void sendOnFactorFound(final long factor){
        for (EventListener eventListener : eventListeners){
            eventListener.onFactorFound(factor);
        }
    }

    public interface EventListener{
        void onFactorFound(final long factor);
    }

    public List<Long> getFactors(){
        return factors;
    }

    public static class SearchOptions{

        private long numberToFactor;

        public enum MonitorType{
            NONE,
            SIMPLE,
            ADVANCED
        }

        private MonitorType monitorType;

        public SearchOptions(){}

        public SearchOptions(long numberToFactor, MonitorType monitorType){
            this.numberToFactor = numberToFactor;
            this.monitorType = monitorType;
        }

        public long getNumberToFactor(){
            return numberToFactor;
        }

        public void setNumberToFactor(long numberToFactor){
            this.numberToFactor = numberToFactor;
        }

        public MonitorType getMonitorType(){
            return monitorType;
        }

        public void setMonitorType(MonitorType monitorType){
            this.monitorType = monitorType;
        }
    }
}
