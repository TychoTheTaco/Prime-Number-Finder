package com.tycho.app.primenumberfinder.modules.findfactors;

import android.os.Parcel;
import android.os.Parcelable;

import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;

/**
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class FindFactorsTask extends Task implements Savable{

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

    private SearchOptions searchOptions;

    private long i;

    final int sqrtMax;

    public FindFactorsTask(final SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
        this.number = searchOptions.getNumber();
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
    //TODO: The downside of this is that getEstimatedTimeRemaining() will be inaccurate if getProgress() is never called because the progress is never set until this is called.
    public float getProgress() {
        if (getState() != State.STOPPED){
            setProgress((float) i / sqrtMax);
        }
        return super.getProgress();
    }

    public void setSearchOptions(SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
    }

    public SearchOptions getSearchOptions() {
        return searchOptions;
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

    public long getNumber() {
        return number;
    }

    private boolean saved;

    @Override
    public boolean save() {
        saved = FileManager.getInstance().saveFactors(getFactors(), getNumber());
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
