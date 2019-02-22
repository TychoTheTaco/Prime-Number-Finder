package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.util.Log;

import com.tycho.app.primenumberfinder.ITask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import simpletrees.Tokenizer;
import simpletrees.Tree;

/**
 * @author Tycho Bellers
 * Date Created: 10/24/2016
 */

public final class FileManager {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FileManager.class.getSimpleName();

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

    private final WeakReference<Context> context;

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

        this.context = new WeakReference<>(context);

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

    public File getTaskCacheDirectory(final ITask task) {
        final File cacheDirectory = new File(context.get().getFilesDir() + File.separator + "cache" + File.separator + task.getId() + File.separator);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
        }
        return cacheDirectory;
    }

    public boolean savePrimes(final long startValue, final long endValue, final List<Long> primes) {
        return writeNumbersQuick(primes, new File(savedPrimesDirectory.getAbsolutePath() + File.separator + "Prime numbers from " + startValue + " to " + endValue + EXTENSION), false);
    }

    public void savePrimes(final List<Long> primes, final File file) {
        writeNumbersQuick(primes, file, false);
    }

    public boolean saveFactors(final List<Long> factors, final long number) {
        return writeNumbersQuick(factors, new File(savedFactorsDirectory.getAbsolutePath() + File.separator + "Factors of " + number + EXTENSION), false);
    }

    public boolean saveFactors(final List<Long> factors, final File file) {
        return writeNumbersQuick(factors, file, false);
    }

    public boolean saveTree(final Tree<?> tree) {
        return saveTree(tree, new File(savedTreesDirectory.getAbsolutePath() + File.separator + "Factor tree of " + tree.getValue() + TREE_EXTENSION));
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

    public boolean writeNumbersQuick(final List<Long> numbers, final File file, final boolean append) {

        try {
            final DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, append)));

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

    public static List<Long> readNumbers(final File file) {
        final List<Long> numbers = new ArrayList<>();

        try {
            final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

            try {
                while (true) {
                    numbers.add(dataInputStream.readLong());
                }
            } catch (EOFException e) {

            } finally {
                dataInputStream.close();
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return numbers;
    }

    /**
     * Reads numbers from a file and adds them to the given list. Returns true if the end of file was reached.
     *
     * @param file
     * @param numbers
     * @param startIndex
     * @param count
     * @return
     */
    public static boolean readNumbers(final File file, final List<Long> numbers, final int startIndex, final int count) {
        boolean endOfFile = false;
        try {
            final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

            //Read header
            final int version = dataInputStream.readUnsignedByte();
            final int headerLength = dataInputStream.readUnsignedByte();
            final int numberSize = dataInputStream.readUnsignedByte();

            //Skip numbers
            dataInputStream.skipBytes(startIndex * numberSize);

            try {
                for (int i = 0; i < count; i++) {
                    numbers.add(dataInputStream.readLong());
                }
            } catch (EOFException e) {
                endOfFile = true;
            } finally {
                dataInputStream.close();
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return endOfFile;
    }

    public static List<Long> readNumbers(final File file, final int startIndex, final int count) {
        final List<Long> numbers = new ArrayList<>();
        readNumbers(file, numbers, startIndex, count);
        return numbers;
    }

    public static int countTotalNumbersQuick(final File file) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        //Read header
        final int version = dataInputStream.readUnsignedByte();
        final int headerLength = dataInputStream.readUnsignedByte();
        final int numberSize = dataInputStream.readUnsignedByte();

        return (int) (file.length() - headerLength) / numberSize;
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
                    final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
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

    public void upgradeTo1_3_0() {
        if (PreferenceManager.getInt(PreferenceManager.Preference.FILE_VERSION) == 0) {

            final List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(savedPrimesDirectory.listFiles()));
            files.addAll(Arrays.asList(savedFactorsDirectory.listFiles()));

            //Update saved primes
            for (File file : files) {

                //Read old file
                final List<Long> numbers = new ArrayList<>();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    final StringBuilder stringBuilder = new StringBuilder("");

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();

                    final List<String> stringNumbers = Arrays.asList(stringBuilder.toString().split(","));
                    for (String string : stringNumbers) {
                        numbers.add(Long.valueOf(string));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Save in new format
                writeNumbersQuick(numbers, file, false);
            }

            PreferenceManager.set(PreferenceManager.Preference.FILE_VERSION, 1);
        }

    }

    public File export(final File file, final String fileName, final String itemSeparator, final NumberFormat numberFormat) {
        final List<Long> items = readNumbers(file);

        final File output = new File(getExportCacheDirectory() + File.separator + fileName);

        try {

            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));

            final long lastItem = items.get(items.size() - 1);

            for (long number : items) {
                if (numberFormat != null) {
                    bufferedWriter.write(numberFormat.format(number));
                } else {
                    bufferedWriter.write(String.valueOf(number));
                }

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
        return new File(context.get().getFilesDir() + File.separator + "export" + File.separator);
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

    public static int numbersWithNDigits(final int n) {
        return (int) (Math.pow(10, n) - Math.pow(10, n - 1));
    }

    public static long[] getPrimesRangeFromTitle(final File file) {
        final long[] range = new long[2];

        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(file.getName());
        for (int i = 0; i < 2; i++) {
            if (matcher.find()) {
                range[i] = Long.valueOf(matcher.group());
            }
        }

        return range;
    }

    public static long getNumberFromTitle(final File file) {
        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(file.getName());
        if (matcher.find()) {
            return Long.valueOf(matcher.group());
        }
        return 0;
    }

    public static FileType getFileType(final File directory) {
        if (directory.equals(getInstance().getSavedPrimesDirectory())) {
            return FileType.PRIMES;
        } else if (directory.equals(getInstance().getSavedFactorsDirectory())) {
            return FileType.FACTORS;
        } else if (directory.equals(getInstance().getSavedTreesDirectory())) {
            return FileType.TREE;
        }
        return FileType.UNKNOWN;
    }
}
