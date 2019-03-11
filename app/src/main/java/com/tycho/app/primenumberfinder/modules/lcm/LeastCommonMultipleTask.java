package com.tycho.app.primenumberfinder.modules.lcm;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import easytasks.MultithreadedTask;
import easytasks.Task;

public class LeastCommonMultipleTask extends MultithreadedTask implements SearchOptions {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LeastCommonMultipleTask.class.getSimpleName();

    /**
     * List of numbers of which we are finding the least common multiple.
     */
    private final List<Long> numbers = new ArrayList<>();

    /**
     * The resulting least common multiple. This will be equal to 0 if the task has not finished.
     */
    private BigInteger lcm = BigInteger.ZERO;

    private SearchOptions searchOptions;

    public LeastCommonMultipleTask(final SearchOptions searchOptions){
        this.searchOptions = searchOptions;
        this.numbers.addAll(searchOptions.getNumbers());
    }

    public SearchOptions getSearchOptions() {
        return searchOptions;
    }

    @Override
    protected void run() {
        //Find prime factors of each number
        for (long number : numbers){
            addTask(new PrimeFactorizationTask(new PrimeFactorizationTask.SearchOptions(number)));
        }
        executeTasks();

        //Find most occurrences of each prime factor
        final Map<Long, Integer> occurrences = new TreeMap<>();
        for (Task task : getTasks()){
            final Map<Long, Integer> treeMap = ((PrimeFactorizationTask) task).getPrimeFactors();
            for (long factor : treeMap.keySet()){
                if (!occurrences.containsKey(factor) || occurrences.get(factor) < treeMap.get(factor)){
                    occurrences.put(factor, treeMap.get(factor));
                }
            }
        }

        //Multiply highest occurrences
        lcm = BigInteger.ONE;
        for (long number : occurrences.keySet()){
            lcm = lcm.multiply(BigInteger.valueOf((long) Math.pow(number, occurrences.get(number))));
        }
    }

    public BigInteger getLcm() {
        return lcm;
    }

    public List<Long> getNumbers() {
        return numbers;
    }

    public static class SearchOptions extends GeneralSearchOptions{

        private final List<Long> numbers = new ArrayList<>();

        public SearchOptions(final List<Long> numbers){
            super(1, false, false);
            this.numbers.addAll(numbers);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeList(numbers);
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

        private SearchOptions(final Parcel parcel) {
            super(parcel);
            parcel.readList(numbers, ArrayList.class.getClassLoader());
        }

        public List<Long> getNumbers() {
            return numbers;
        }
    }

    public void setSearchOptions(final SearchOptions searchOptions){
        this.searchOptions = searchOptions;
    }
}
