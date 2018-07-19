package com.tycho.app.primenumberfinder.modules.primefactorization;

import android.os.Parcel;
import android.os.Parcelable;

import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;
import simpletrees.Tree;

/**
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class PrimeFactorizationTask extends Task implements Savable, SearchOptions {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationTask.class.getSimpleName();

    private final long number;

    private Tree<Long> factorTree;

    /**
     * Map<Number, Occurrences>.
     */
    private Map<Long, Integer> primeFactors = new TreeMap<>();

    private SearchOptions searchOptions;

    //final FindFactorsTask findFactorsTask;

    public PrimeFactorizationTask(final SearchOptions searchOptions){
        this.number = searchOptions.getNumber();
        this.searchOptions = searchOptions;
        //findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number));
    }

    @Override
    public void run() {
        this.factorTree = generateTree(number);
    }

    private FindFactorsTask findFactorsTask;

    @Override
    public float getProgress() {
        //TODO: Dont do this
        if (findFactorsTask != null){
            setProgress(findFactorsTask.getProgress());
        }
        return super.getProgress();
    }

    private Tree<Long> generateTree(long number) {

        final Tree<Long> tree = new Tree<>(number);

        findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number));
        findFactorsTask.start();

        final int size = findFactorsTask.getFactors().size();

        setProgress((float) getNumber() / number);

        if (size == 1){
            primeFactors.put(1L, 1);
        }else if (size == 2) {
            //This number is prime, end of branch
            if (!primeFactors.containsKey(findFactorsTask.getNumber())){
                primeFactors.put(findFactorsTask.getNumber(), 1);
            }else{
                primeFactors.put(findFactorsTask.getNumber(), primeFactors.get(findFactorsTask.getNumber()) + 1);
            }
        } else if (size == 3) {
            //This is a perfect square
            tree.addNode(findFactorsTask.getFactors().get(1));
            tree.addNode(findFactorsTask.getFactors().get(1));

            if (!primeFactors.containsKey(findFactorsTask.getFactors().get(1))){
                primeFactors.put(findFactorsTask.getFactors().get(1), 2);
            }else{
                primeFactors.put(findFactorsTask.getFactors().get(1), primeFactors.get(findFactorsTask.getFactors().get(1)) + 2);
            }
        } else {

            long number1;
            long number2;

            if (size % 2 == 0) {
                number1 = findFactorsTask.getFactors().get((size / 2) - 1);
                number2 = findFactorsTask.getFactors().get((size / 2));
            } else {
                number1 = findFactorsTask.getFactors().get((size / 2));
                number2 = findFactorsTask.getFactors().get((size / 2));
            }

            tree.addNode(generateTree(number1));
            tree.addNode(generateTree(number2));
        }

        return tree;
    }

    public Tree<Long> getFactorTree() {
        return factorTree;
    }

    public Map<Long, Integer> getPrimeFactors() {
        return primeFactors;
    }

    public long getNumber() {
        return number;
    }

    public SearchOptions getSearchOptions() {
        return searchOptions;
    }

    public void setSearchOptions(SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
    }

    public static class SearchOptions extends GeneralSearchOptions {

        /**
         * The number we are finding factors of.
         */
        private long number;

        public SearchOptions(final long number, final int threadCount, final boolean notifyWhenFinished, final boolean autoSave) {
            super(threadCount, notifyWhenFinished, autoSave);
            this.number = number;
        }

        public SearchOptions(final long number) {
            this(number, 1, false, false);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.number);
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
            this.number = parcel.readLong();
        }

        public void setNumber(long number) {
            this.number = number;
        }

        public long getNumber() {
            return number;
        }
    }

    private boolean saved;

    @Override
    public boolean save() {
        saved = FileManager.getInstance().saveTree(getFactorTree());
        if (saved){
            sendOnSaved();
        }else{
            sendOnError();
        }
        return saved;
    }

    private CopyOnWriteArrayList<SaveListener> saveListeners = new CopyOnWriteArrayList<>();

    public void addSaveListener(final SaveListener listener){
        saveListeners.add(listener);
    }

    public void removeSaveListener(final SaveListener listener){
        saveListeners.remove(listener);
    }

    private void sendOnSaved(){
        for (SaveListener listener : saveListeners){
            listener.onSaved();
        }
    }

    private void sendOnError(){
        for (SaveListener listener : saveListeners){
            listener.onError();
        }
    }

    public boolean isSaved(){
        return saved;
    }
}
