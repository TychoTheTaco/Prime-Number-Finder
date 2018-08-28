package com.tycho.app.primenumberfinder.modules.gcf;

import android.os.Parcel;

import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import easytasks.Task;

public class GreatestCommonFactorTask extends Task implements SearchOptions {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GreatestCommonFactorTask.class.getSimpleName();

    private final List<Long> numbers = new ArrayList<>();

    private long gcf;

    private SearchOptions searchOptions;

    public GreatestCommonFactorTask(final SearchOptions searchOptions){
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
            final FindFactorsTask findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number));
            findFactorsTask.start();
            for (Long f : findFactorsTask.getFactors()){
                if (occurrences.containsKey(f)){
                    occurrences.put(f, occurrences.get(f) + 1);
                }else{
                    occurrences.put(f, 1);
                }
            }
        }

        final List<Long> keys = new ArrayList<>(occurrences.keySet());
        Collections.reverse(keys);
        for (Long number : keys){
            if (occurrences.get(number) == numbers.size()){
                gcf = number;
                break;
            }
        }
    }

    public long getGcf() {
        return gcf;
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

        public static final Creator<SearchOptions> CREATOR = new Creator<SearchOptions>() {

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
