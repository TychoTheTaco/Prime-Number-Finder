package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.util.Log;

import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import easytasks.Task;
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

    private static final char LIST_ITEM_SEPARATOR = '\n';
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

        if (!getExportCacheDirectory().exists()) {
            getExportCacheDirectory().mkdirs();
        }

        //Clear export cache
        deleteDirectory(getExportCacheDirectory(), false);

        //Clear cache directory //TODO: Dont do this if there are running tasks
        deleteDirectory(new File(context.getFilesDir() + File.separator + "cache" + File.separator), false);
    }

    private static void deleteDirectory(final File directory, final boolean deleteRoot) {
        if (directory != null && directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file, true);
                } else {
                    file.delete();
                }
            }
            if (deleteRoot) {
                directory.delete();
            }
        }
    }

    public Context getContext() {
        return this.context;
    }

    public File getTaskCacheDirectory(final Task task) {
        final File cacheDirectory = new File(FileManager.getInstance().getContext().getFilesDir() + File.separator + "cache" + File.separator + task.getId() + File.separator);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        return cacheDirectory;
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

    public boolean saveTree(final Tree<?> tree, final File file) {
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

    public void writeToCache(final List<Long> numbers, final UUID id, final boolean append) {
        writeToCache(numbers, new File(FileManager.getInstance().getContext().getFilesDir() + File.separator + "cache" + File.separator + id + File.separator + "cache"), append);
    }

    public static void writeCompact(final List<Long> numbers, final File file, final boolean append) {
        try {
            final DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file, append));

            final List<Integer> cache = new ArrayList<>();

            //Check if last value is already separator
            if (file.exists() && append && file.length() > 0) {
                final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                Log.d(TAG, "Skipping: " + (file.length() - 1) + " bytes");
                dataInputStream.skip(file.length() - 1);
                if ((dataInputStream.readUnsignedByte() & 0x0F) == 0xF) {
                    //Already has separator
                } else {
                    cache.add(0xFF);
                }
            }

            for (long number : numbers) {
                /*final String string = String.valueOf(number);
                final char[] chars = string.toCharArray();
                for (int i = 0; i < chars.length; i++){
                    cache.add(Character.digit(chars[i], 10));
                }*/
                while (number > 0) {
                    long d = number / 10;
                    int k = (int) (number - d * 10);
                    number = d;
                    cache.add(k);
                }
                cache.add(0xFF);
            }
            cache.remove(cache.size() - 1);

            //Log.d(TAG, "Final Cache: " + cache);

            for (int i = 0; i < cache.size(); i += 8) {
                int data = 0;
                for (int a = 0; a < 8; a++) {
                    final int value;
                    if (i + a < cache.size()) {
                        value = cache.get(i + a);
                    } else {
                        value = 0xF;
                    }
                    data |= ((value << ((a) * 4)) & (0xF << ((a) * 4)));
                }
                //Log.d(TAG, "Write: " + data);
                dataOutputStream.writeInt(data);
            }

            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Long> readCompat(final File file) {
        final List<Long> numbers = new ArrayList<>();

        try {
            final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            final List<Integer> digits = new ArrayList<>();
            try {

                while (true) {
                    final int data = dataInputStream.readInt();
                    final int[] split = new int[8];
                    //Log.d(TAG, "Read: " + data);
                    for (int i = 0; i < 8; i++) {
                        split[i] = ((data >> ((i) * 4)) & 0xF);
                        //Log.d(TAG, "split[" + i + "] = " + split[i]);
                    }
                    //Log.d(TAG, "Read: " + (data & 0xFF));
                    for (int i = 0; i < 8; i++) {
                        if (split[i] != 0xF) {
                            digits.add(split[i]);
                        } else {
                            String number = "";
                            Collections.reverse(digits);
                            for (Integer integer : digits) {
                                number += integer;
                            }
                            //Log.d(TAG, "Adding: " + number);
                            if (number.length() > 0) {
                                numbers.add(Long.valueOf(number));
                                digits.clear();
                            }
                        }
                    }
                }
            } catch (EOFException e) {
                dataInputStream.close();
                String number = "";
                for (Integer integer : digits) {
                    number += integer;
                }
                //Log.d(TAG, "Adding: " + number);
                if (number.length() > 0) {
                    numbers.add(Long.valueOf(number));
                }
                digits.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return numbers;
    }

    public void writeToCache(final List<Long> numbers, final File file, final boolean append) {
        try {
            final DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file, append));

            for (long number : numbers) {
                dataOutputStream.writeLong(number);
            }

            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeNumbers(final List<Long> numbers, final File file) {

        try {

            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            final long lastNumber = numbers.get(numbers.size() - 1);

            for (long number : numbers) {
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

    public boolean writeNumbersNew(final List<Long> numbers, final File file) {

        try {
            final DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));

            for (long number : numbers) {
                dataOutputStream.writeLong(number);
            }

            dataOutputStream.close();

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

            while ((line = bufferedReader.readLine()) != null) {
                numbers.add(Long.valueOf(line));
            }

            bufferedReader.close();

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return numbers;
    }

    public List<Long> readNumbers(final File file, final int startIndex, final int endIndex) {
        final List<Long> numbers = new ArrayList<>();

        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            //Slip lines until startIndex
            for (int i = 0; i < startIndex; i++) {
                bufferedReader.readLine();
            }

            for (int i = 0; i < (endIndex - startIndex); i++) {
                numbers.add(Long.valueOf(bufferedReader.readLine()));
            }

            bufferedReader.close();

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return numbers;
    }

    public static void saveDebugFile(final File file){
        try {

            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            final int MAX = 1_000;

            for (int i = 0; i < MAX; i++){
                bufferedWriter.write(String.valueOf(i));
                if (i != MAX - 1) {
                    bufferedWriter.write(LIST_ITEM_SEPARATOR);
                }
            }

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int countTotalNumbers(final File file) {

        int count = 0;

        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while (bufferedReader.readLine() != null) {
                count++;
            }

            bufferedReader.close();

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return count;
    }

    public List<Long> readNumbersNew(final File file) {

        final List<Long> numbers = new ArrayList<>();

        try {
            final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));

            try {
                while (true) {
                    numbers.add(dataInputStream.readLong());
                }
            } catch (EOFException e) {
                dataInputStream.close();
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

    /**
     * Update the file system used in versions prior to 1.2.0
     *
     * @param context
     */
    public void updateFileSystem(final Context context) {

        //Check for primes directory
        final File primesDirectory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "Prime numbers");
        if (primesDirectory.exists()) {
            for (File file : primesDirectory.listFiles()) {
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
        if (factorsDirectory.exists()) {
            for (File file : factorsDirectory.listFiles()) {
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

    public File convert(final File file, final String fileName, final String itemSeparator) {
        final List<Long> items = readNumbers(file);

        final File output = new File(getExportCacheDirectory() + File.separator + fileName);

        try {

            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));

            final long lastItem = items.get(items.size() - 1);

            for (long number : items) {
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

    public File getExportCacheDirectory() {
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
