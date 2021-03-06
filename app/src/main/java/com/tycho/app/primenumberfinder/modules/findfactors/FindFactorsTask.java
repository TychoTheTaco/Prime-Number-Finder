package com.tycho.app.primenumberfinder.modules.findfactors;

import android.os.Parcel;
import android.os.Parcelable;

import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;

/**
 * @author Tycho Bellers
 * Date Created: 3/3/2017
 */

public class FindFactorsTask extends Task implements Savable, SearchOptions {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindFactorsTask.class.getSimpleName();

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

    private SearchOptions searchOptions;

    private long i;

    private final int sqrtMax;

    public FindFactorsTask(final SearchOptions searchOptions){
        this.searchOptions = searchOptions;
        this.number = searchOptions.getNumber();
        sqrtMax = (int) Math.sqrt(number);
    }

    @Override
    protected void run(){
        for (i = 1; isRunning() && i <= sqrtMax; i++){

            //Check if the number divides perfectly
            if (number % i == 0){
                factors.add(i);
                final long inv = number / i;
                if (inv != i){
                    inverse.add(0, inv);
                }
            }
        }

        factors.addAll(inverse);
        inverse.clear();
    }

    @Override
    public float getProgress(){
        if (getState() != State.STOPPED){
            return (float) i / sqrtMax;
        }
        return super.getProgress();
    }

    public void setSearchOptions(SearchOptions searchOptions){
        this.searchOptions = searchOptions;
    }

    public SearchOptions getSearchOptions(){
        return searchOptions;
    }

    public List<Long> getFactors(){
        return factors;
    }

    public static class SearchOptions extends GeneralSearchOptions{

        /**
         * The number we are finding factors of.
         */
        private long number;

        public SearchOptions(final long number, final int threadCount){
            super(threadCount);
            this.number = number;
        }

        public SearchOptions(final long number){
            this(number, 1);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            super.writeToParcel(dest, flags);
            dest.writeLong(this.number);
        }

        public static final Parcelable.Creator<SearchOptions> CREATOR = new Parcelable.Creator<SearchOptions>(){

            @Override
            public SearchOptions createFromParcel(Parcel in){
                return new SearchOptions(in);
            }

            @Override
            public SearchOptions[] newArray(int size){
                return new SearchOptions[size];
            }
        };

        private SearchOptions(final Parcel parcel){
            super(parcel);
            this.number = parcel.readLong();
        }

        public void setNumber(long number){
            this.number = number;
        }

        public long getNumber(){
            return number;
        }
    }

    public long getNumber(){
        return number;
    }

    public void saveToFile(final File file) throws IOException{
        final DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)));
        //Header format
        // [1 byte] Version
        // [1 byte] Header length
        // [1 byte] Number size (in bytes)
        // [Variable] Number being factored

        //Write header
        dataOutputStream.writeByte(1);
        dataOutputStream.writeByte(3 + 8);
        dataOutputStream.writeByte(8);
        dataOutputStream.write(FileManager.numberToBytes(number));

        //Write data
        for (long factor : factors){
            dataOutputStream.write(FileManager.numberToBytes(factor));
        }
        for (long factor : inverse){
            dataOutputStream.write(FileManager.numberToBytes(factor));
        }

        dataOutputStream.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // [Savable]
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean saved;

    private final CopyOnWriteArrayList<SaveListener> saveListeners = new CopyOnWriteArrayList<>();

    @Override
    public boolean save(){
        try{
            saveToFile(FileManager.buildFile(this));
            sendOnSaved();
            saved = true;
        }catch (IOException e){
            e.printStackTrace();
            sendOnError();
            saved = false;
        }
        return saved;
    }

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
