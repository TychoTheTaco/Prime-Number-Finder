package com.tycho.app.primenumberfinder;

/**
 * @author Tycho Bellers
 *         Date Created: 5/19/2017
 */

public enum Statistic{
    TIME_ELAPSED("timeElapsed"),
    NUMBERS_PER_SECOND("numbersPerSecond"),
    PRIMES_PER_SECOND("primesPerSecond"),
    FACTORS_PER_SECOND("factorsPerSecond"),
    ESTIMATED_TIME_REMAINING("estimatedTimeRemaining");

    private final String key;

    Statistic(final String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }
}
