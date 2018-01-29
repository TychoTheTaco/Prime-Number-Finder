package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.util.Log;

import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import simpletrees.Tokenizer;
import simpletrees.Tree;

/**
 * @author Tycho Bellers
 *         Date Created: 10/24/2016
 */

public final class FileManager {

    /**
     * Tag used for logging and debugging
     */
    private static final String TAG = "FileManager";

    /**
     * The current FileManager instance. There should only be 1 at a time.
     */
    private static FileManager instance = null;

    /**
     * File directories.
     */
    private final File savedPrimesDirectory;
    private final File savedFactorsDirectory;
    private final File savedTreesDirectory;

    private static final char LIST_ITEM_SEPARATOR = ',';
    public static final String EXTENSION = ".txt";
    public static final String TREE_EXTENSION = ".tree";

    private final Context context;

    /**
     * Initialize the file manager. This will create 1 instance that will be used throughout the
     * lifetime of the application.
     *
     * @param context Context used for getting the files directory.
     */
    public static void init(final Context context) {
        if (instance == null) {
            instance = new FileManager(context);
        }
    }

    public static FileManager getInstance() {
        if (instance == null) throw new RuntimeException("FileManager must be initialized first!");
        return instance;
    }

    /**
     * Private constructor. Variables are initialized here.
     *
     * @param context The context to use when initializing variables.
     */
    private FileManager(final Context context) {

        this.context = context;

        //Initialize save directories
        savedPrimesDirectory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "primes");
        if (!savedPrimesDirectory.exists()) {
            if (!savedPrimesDirectory.mkdirs()) {
                Log.e(TAG, "Failed to create save directory at " + savedPrimesDirectory);
            }
        }

        savedFactorsDirectory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "factors");
        if (!savedFactorsDirectory.exists()) {
            if (!savedFactorsDirectory.mkdirs()) {
                Log.e(TAG, "Failed to create save directory at " + savedFactorsDirectory);
            }
        }

        savedTreesDirectory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "trees");
        if (!savedTreesDirectory.exists()) {
            if (!savedTreesDirectory.mkdirs()) {
                Log.e(TAG, "Failed to create save directory at " + savedTreesDirectory);
            }
        }

        if (!getExportCacheDirectory().exists()){
            getExportCacheDirectory().mkdirs();
        }
    }

    public boolean savePrimes(final long startValue, final long endValue, final List<Long> primes) {
        final File file = new File(savedPrimesDirectory.getAbsolutePath() + File.separator + "Prime numbers from " + startValue + " to " + endValue + EXTENSION);
        return writeNumbers(primes, file);
    }

    public boolean savePrimes(final List<Long> primes, final File file) {
        return writeNumbers(primes, file);
    }

    public boolean saveFactors(final List<Long> factors, final long number) {
        final File file = new File(savedFactorsDirectory.getAbsolutePath() + File.separator + "Factors of " + number + EXTENSION);
        return writeNumbers(factors, file);
    }

    public boolean saveFactors(final List<Long> factors, final File file) {
        return writeNumbers(factors, file);
    }

    public boolean saveFactors(final List<Long> factors) {
        return saveFactors(factors, factors.get(factors.size() - 1));
    }

    public boolean saveTree(final Tree<?> tree) {
        final File file = new File(savedTreesDirectory.getAbsolutePath() + File.separator + "Factor tree of " + tree.getValue() + TREE_EXTENSION);
        return saveTree(tree, file);
    }

    public boolean saveTree(final Tree<?> tree, final File file){
        try {

            final PrintWriter printWriter = new PrintWriter(file);
            printWriter.write(tree.toString());
            printWriter.flush();
            printWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean writeNumbers(final List<Long> numbers, final File file) {

        try {

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            final long lastNumber = numbers.get(numbers.size() - 1);

            for (long number : numbers){
                bufferedWriter.write(String.valueOf(number));

                if (number != lastNumber) {
                    bufferedWriter.write(LIST_ITEM_SEPARATOR);
                }
            }

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //Read methods

    public List<Long> readNumbers(final File file) {

        final List<Long> numbers = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String line;

            String output = "";

            while ((line = bufferedReader.readLine()) != null) {
                output += line;
            }

            bufferedReader.close();

            final List<String> stringNumbers = Arrays.asList(output.split(","));

            for (String string : stringNumbers) {
                numbers.add(Long.valueOf(string));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return numbers;
    }

    public Tree<Long> readTree(final File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            StringBuilder stringBuilder = new StringBuilder();

            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }

            return Tree.parse(stringBuilder.toString()).toLongTree();
        } catch (IOException | Tokenizer.InvalidTokenException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateFileSystem(final Context context){

        //Check for primes directory
        final File primesDirectory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "Prime numbers");
        if (primesDirectory.exists()){
            for (File file : primesDirectory.listFiles()){
                final List<Long> numbers = new ArrayList<>();

                //Read old file
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        numbers.add(Long.valueOf(line));
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Save as new file
                savePrimes(numbers, new File(getSavedPrimesDirectory() + File.separator + file.getName()));
                file.delete();
            }
            primesDirectory.delete();
        }

        //Check for factors directory
        final File factorsDirectory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "Factors");
        if (factorsDirectory.exists()){
            for (File file : factorsDirectory.listFiles()){
                final List<Long> numbers = new ArrayList<>();

                //Read old file
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        numbers.add(Long.valueOf(line));
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Save as new file
                saveFactors(numbers, new File(getSavedFactorsDirectory() + File.separator + file.getName()));
                file.delete();
            }
            factorsDirectory.delete();
        }
    }

    public File convert(final File file, final String fileName, final String itemSeparator){
        final List<Long> items = readNumbers(file);

        final File output = new File(getExportCacheDirectory() + File.separator + fileName);

        try {

            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));

            final long lastItem = items.get(items.size() - 1);

            for (long number : items){
                bufferedWriter.write(String.valueOf(number));

                if (number != lastItem) {
                    bufferedWriter.write(itemSeparator);
                }
            }

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return output;
    }

    public File getSavedPrimesDirectory() {
        return savedPrimesDirectory;
    }

    public File getSavedFactorsDirectory() {
        return savedFactorsDirectory;
    }

    public File getSavedTreesDirectory() {
        return savedTreesDirectory;
    }

    public File getExportCacheDirectory(){
        return new File(context.getFilesDir() + File.separator + "export" + File.separator);
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
