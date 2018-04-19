package com.tycho.app.primenumberfinder.modules.findfactors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import easytasks.Task;

/**
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class FindFactorsTask extends Task {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsTask";

    /**
     * The number we are finding factors of.
     */
    private final long number;

    /**
     * Lis of all the factors found.
     */
    private final List<Long> factors = new LinkedList<>();

    /**
     * This list holds all of the opposite factors found. For example, for the number 20, we will
     * find that 2 x 10 = 20, which means 2 is a factor. At this point, 2 is added to
     * {@linkplain FindFactorsTask#factors} and 10 is added to {@linkplain FindFactorsTask#inverse}.
     * This allows much faster factorization because it prevents us from having to search past the
     * sqrt(20). All contents of this list will be added to {@linkplain FindFactorsTask#factors}
     * after all factors below sqrt(20) have been found.
     */
    private final List<Long> inverse = new LinkedList<>();

    public boolean didFinish = false;

    private final SearchOptions searchOptions;

    private long i;

    final int sqrtMax;

    public FindFactorsTask(final SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
        this.number = searchOptions.getNumberToFactor();
         sqrtMax = (int) Math.sqrt(number);
    }

    @Override
    public void run() {

        for (i = 1; i <= sqrtMax; i++) {

            //Check if the number divides perfectly
            if (number % i == 0) {
                factors.add(i);
                if ((number / i) != i) {
                    inverse.add(0, number / i);
                }
            }

            tryPause();
            if (shouldStop()) {
                return;
            }

            //setProgress((float) i / sqrtMax);
        }

        if (!shouldStop()){
            for (Long n : inverse) {
                factors.add(n);
            }

            didFinish = true;
        }
    }

    @Override
    public float getProgress() {
        setProgress((float) i / sqrtMax);
        return super.getProgress();
    }

    public long getMaxValue(){
        return (long) Math.sqrt(number);
    }

    public long getCurrentValue(){
        return i;
    }

    public List<Long> getFactors() {
        return factors;
    }

    public static class SearchOptions {

        private long number;

        public enum MonitorType {
            NONE,
            SIMPLE,
            ADVANCED
        }

        private MonitorType monitorType;

        public SearchOptions() {
        }

        public SearchOptions(long number, MonitorType monitorType) {
            this.number = number;
            this.monitorType = monitorType;
        }

        public long getNumberToFactor() {
            return number;
        }

        public void setNumber(long number) {
            this.number = number;
        }

        public MonitorType getMonitorType() {
            return monitorType;
        }

        public void setMonitorType(MonitorType monitorType) {
            this.monitorType = monitorType;
        }
    }

    public long getNumber() {
        return number;
    }
}
