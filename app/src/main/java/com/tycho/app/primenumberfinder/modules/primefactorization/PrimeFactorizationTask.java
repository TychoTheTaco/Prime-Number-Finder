package com.tycho.app.primenumberfinder.modules.primefactorization;

import android.os.Parcel;
import android.os.Parcelable;

import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;
import simpletrees.Tree;

/**
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class PrimeFactorizationTask extends Task implements Savable{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeFactorizationTask";

    private final long number;

    private Tree<Long> factorTree;

    private Map<Long, Long> primeFactors = new TreeMap<>();

    private int total;

    private String status;

    private SearchOptions searchOptions;

    final FindFactorsTask findFactorsTask;

    public PrimeFactorizationTask(final SearchOptions searchOptions){
        this.number = searchOptions.getNumber();
        this.searchOptions = searchOptions;
        findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number));
    }

    @Override
    public void run() {

        status = "findFactors";
        //final FindFactorsTask findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number));
        findFactorsTask.start();
        final List<Long> factors = findFactorsTask.getFactors();

        status = "checkPrimality";

        for (long n : factors){
            final CheckPrimalityTask checkPrimalityTask = new CheckPrimalityTask(n);
            checkPrimalityTask.start();
            if (checkPrimalityTask.isPrime()){
                total++;
            }
            tryPause();
            if (shouldStop()){
                return;
            }
        }
        status = "generatingTree";
        this.factorTree = generateTree(number);
        status = "";
    }

    @Override
    public void pause() {
        synchronized (STATE_LOCK){
            if (findFactorsTask.getState() != State.STOPPED){
                try {
                    findFactorsTask.pauseAndWait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                dispatchPaused();
            }else{
                super.pause();
            }
        }
    }

    @Override
    public void resume() {
        synchronized (STATE_LOCK){
            if (findFactorsTask.getState() == State.PAUSED){
                try {
                    findFactorsTask.resumeAndWait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                dispatchResumed();
            }else{
                super.resume();
            }
        }
    }

    @Override
    public void stop() {
        findFactorsTask.stop();
        super.stop();
    }

    @Override
    public float getProgress() {
        switch (status){
            case "findFactors":
                setProgress(0.33f * (findFactorsTask.getProgress()));
                break;

            case "checkPrimality":
                setProgress(0.33f);
                break;

            case "generatingTree":
                setProgress(0.67f);
                break;
        }
        return super.getProgress();
    }

    private Tree<Long> generateTree(long number) {

        final Tree<Long> tree = new Tree<>(number);

        final FindFactorsTask findFactorsTask = new FindFactorsTask(new FindFactorsTask.SearchOptions(number));
        findFactorsTask.start();

        final int size = findFactorsTask.getFactors().size();

        if (size == 1){
            primeFactors.put(1L, 1L);
        }else if (size == 2) {
            //This number is prime, end of branch
            if (!primeFactors.containsKey(findFactorsTask.getNumber())){
                primeFactors.put(findFactorsTask.getNumber(), 1L);
            }else{
                primeFactors.put(findFactorsTask.getNumber(), primeFactors.get(findFactorsTask.getNumber()) + 1);
            }
        } else if (size == 3) {
            //This is a perfect square
            tree.addNode(findFactorsTask.getFactors().get(1));
            tree.addNode(findFactorsTask.getFactors().get(1));

            if (!primeFactors.containsKey(findFactorsTask.getFactors().get(1))){
                primeFactors.put(findFactorsTask.getFactors().get(1), 2L);
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

            //setProgress(primeFactors.size() / total);

            tree.addNode(generateTree(number1));
            tree.addNode(generateTree(number2));
        }

        return tree;
    }

    public Tree<Long> getFactorTree() {
        return factorTree;
    }

    public Map<Long, Long> getPrimeFactors() {
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

    @Override
    public boolean save() {
       return FileManager.getInstance().saveTree(getFactorTree());
    }
}
