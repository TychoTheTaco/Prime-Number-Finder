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

import easytasks.Task;

public class LeastCommonMultipleTask extends Task implements SearchOptions {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LeastCommonMultipleTask.class.getSimpleName();

    private final List<Long> numbers = new ArrayList<>();

    private BigInteger lcm;

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
        final Map<Long, Integer> occurrences = new TreeMap<>();
        for (Long number : numbers){
            //TODO: To support BigInteger input, PrimeFactorizationTask also needs to accept BigInteger input
            final PrimeFactorizationTask primeFactorizationTask = new PrimeFactorizationTask(new PrimeFactorizationTask.SearchOptions(number));
            primeFactorizationTask.start();
            final Map<Long, Integer> treeMap = primeFactorizationTask.getPrimeFactors();
            for (Long l : treeMap.keySet()){
                if (!occurrences.containsKey(l) || occurrences.get(l) < treeMap.get(l)){
                    occurrences.put(l, treeMap.get(l));
                }
            }
            Log.d(TAG, "setProgress: " + ((float) numbers.indexOf(number) / numbers.size()));
            setProgress((float) numbers.indexOf(number) / numbers.size());
        }


        lcm = BigInteger.ONE;
        Log.e(TAG, "Occurences: " + occurrences);
        for (Long number : occurrences.keySet()){
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
