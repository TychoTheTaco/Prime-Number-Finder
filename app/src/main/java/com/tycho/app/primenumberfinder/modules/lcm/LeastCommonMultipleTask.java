package com.tycho.app.primenumberfinder.modules.lcm;

import android.util.Log;

import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import easytasks.Task;

public class LeastCommonMultipleTask extends Task {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LeastCommonMultipleTask.class.getSimpleName();

    private final List<Long> numbers = new ArrayList<>();

    private long lcm;

    public LeastCommonMultipleTask(final List<Long> numbers){
        this.numbers.addAll(numbers);
    }

    public LeastCommonMultipleTask(final long... numbers){
        for (long l : numbers){
            this.numbers.add(l);
        }
    }

    @Override
    protected void run() {
        final Map<Long, Integer> occurrences = new TreeMap<>();
        for (Long number : numbers){
            final PrimeFactorizationTask primeFactorizationTask = new PrimeFactorizationTask(new PrimeFactorizationTask.SearchOptions(number));
            primeFactorizationTask.start();
            final Map<Long, Integer> treeMap = primeFactorizationTask.getPrimeFactors();
            for (Long l : treeMap.keySet()){
                if (!occurrences.containsKey(l) || occurrences.get(l) < treeMap.get(l)){
                    occurrences.put(l, treeMap.get(l));
                }
            }
        }

        lcm = 1;
        for (Long l : occurrences.keySet()){
            lcm *= l;
        }
    }

    public long getLcm() {
        return lcm;
    }

    public List<Long> getNumbers() {
        return numbers;
    }
}
