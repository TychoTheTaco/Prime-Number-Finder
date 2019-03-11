package com.tycho.app.primenumberfinder.modules.gcf;

import android.os.Parcel;
import android.util.ArraySet;

import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import easytasks.MultithreadedTask;
import easytasks.Task;

public class GreatestCommonFactorTask extends MultithreadedTask implements SearchOptions {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GreatestCommonFactorTask.class.getSimpleName();

    /**
     * The list of numbers of which we are finding the greatest common factor.
     */
    private final List<Long> numbers = new ArrayList<>();

    /**
     * The greatest common factor.
     */
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
        //Find factors of each number
        for (long number : numbers){
            addTask(new FindFactorsTask(new FindFactorsTask.SearchOptions(number)));
        }
        executeTasks();

        //Find common factors
        final Set<Long> set = new HashSet<>(((FindFactorsTask) getTasks().get(0)).getFactors());
        for (int i = 1; i < getTasks().size(); ++i){
            set.retainAll(((FindFactorsTask) getTasks().get(i)).getFactors());
        }

        //Find largest factor
        gcf = Collections.max(set);
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
