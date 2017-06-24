package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.util.Log;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 10/24/2016
 */

public class FileManager{

    /**
     * Tag used for logging and debugging
     */
    private static final String TAG = "FileManager";

    /**
     * The current FileManager instance. There should only be 1 at a time.
     */
    private static FileManager instance = null;

    /**
     * Save directories.
     */
    private final File directorySavedPrimes;
    private final File directorySavedFactors;

    private static final char SEPARATOR = ',';
    private static final String EXTENSION = ".txt";

    /**
     * Gets the current FileManager instance.
     *
     * @param context The context to use to create the FileManager instance if it did not exist yet.
     * @return FileManager instance.
     */
    public static FileManager getInstance(final Context context){

        if (instance == null){
            instance = new FileManager(context);
        }

        return instance;
    }

    public static FileManager getInstance(){
        if (instance == null) throw new RuntimeException("FileManager must be initialized first!");
        return instance;
    }

    /**
     * Private constructor. Variables are initialized here.
     *
     * @param context The context to use when initializing variables.
     */
    private FileManager(final Context context){

        //Initialize save directories
        directorySavedPrimes = new File(context.getFilesDir().getAbsolutePath() + File.separator + "savedPrimes");
        if (!directorySavedPrimes.exists()){
            if (!directorySavedPrimes.mkdirs()){
                if (PrimeNumberFinder.DEBUG)
                    Log.e(TAG, "Failed to create save directory at " + directorySavedPrimes);
            }
        }

        directorySavedFactors = new File(context.getFilesDir().getAbsolutePath() + File.separator + "savedFactors");
        if (!directorySavedFactors.exists()){
            if (!directorySavedFactors.mkdirs()){
                if (PrimeNumberFinder.DEBUG)
                    Log.e(TAG, "Failed to create save directory at " + directorySavedFactors);
            }
        }
    }

    //Write methods

    public boolean savePrimes(final long startValue, final long endValue, final List<Long> primes){

        final File file = new File(directorySavedPrimes.getAbsolutePath() + File.separator + "Prime numbers from " + startValue + " to " + endValue + EXTENSION);

        return writeNumbers(primes, file);
    }

    public boolean saveFactors(final List<Long> factors){

        //Create new file
        final File file = new File(directorySavedFactors.getAbsolutePath() + File.separator + "Factors of " + factors.get(factors.size() - 1) + EXTENSION);

        return writeNumbers(factors, file);
    }

    private boolean writeNumbers(final List<Long> numbers, final File file){

        try{

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            final long lastNumber = numbers.get(numbers.size() - 1);

            for (long number : numbers){
                bufferedWriter.write(String.valueOf(number));

                if (number != lastNumber){
                    bufferedWriter.write(SEPARATOR);
                }

            }

            bufferedWriter.flush();
            bufferedWriter.close();

        }catch (IOException e){
            return false;
        }

        return true;
    }

    //Read methods

    public List<Long> readNumbers(final File file){

        try{
            final List<Long> numbers = new ArrayList<>();

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String line;

            String output = "";

            while ((line = bufferedReader.readLine()) != null){
                output += line;
            }

            bufferedReader.close();

            final List<String> stringNumbers = Arrays.asList(output.split(","));

            for (String string : stringNumbers){
                numbers.add(Long.valueOf(string));
            }

            return numbers;
        }catch (IOException e){
            return null;
        }


    }

    //Getters

    public File getDirectorySavedPrimes(){
        return directorySavedPrimes;
    }

    public File getDirectorySavedFactors(){
        return directorySavedFactors;
    }
}
