package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.util.Log;

import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.modules.savedfiles.DataFile;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import easytasks.ITask;
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
        //deleteDirectory(new File(context.getFilesDir() + File.separator + "cache" + File.separator), false);
    }

    public static File buildFile(final ITask task) {
        if (task instanceof FindPrimesTask) {
            return new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + ((FindPrimesTask) task).getStartValue() + "-" + (((FindPrimesTask) task).isEndless() ? "INF" : ((FindPrimesTask) task).getEndValue()) + ".primes");
        }else if (task instanceof FindFactorsTask){
            return new File(FileManager.getInstance().getSavedFactorsDirectory() + File.separator + ((FindFactorsTask) task).getNumber() + ".factors");
        }else if (task instanceof PrimeFactorizationTask){
            return new File(FileManager.getInstance().getSavedTreesDirectory() + File.separator + ((PrimeFactorizationTask) task).getNumber() + ".tree");
        }
        return new File(FileManager.getInstance() + File.separator + task.getId().toString() + ".unknown");
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

    private static long bytesToNumber(final byte[] bytes) {
        long number = 0;
        for (int i = 0, offset = 8 * 8 - 8; i < 8; ++i, offset -= 8) {
            long n = bytes[i] & 0xFF;
            number |= (n << offset);
        }
        return number;
    }

    private static abstract class NumbersFile extends DataFile {

        protected final int numberSize;

        protected final int totalNumbers;

        public NumbersFile(final File file) throws IOException{
            super(file, true);

            final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            dataInputStream.skipBytes(2);
            numberSize = dataInputStream.readUnsignedByte();
            dataInputStream.close();

            totalNumbers = (int) (file.length() - headerLength) / numberSize;
        }

        public List<Long> readNumbers(final int startIndex, final int count) throws IOException{
            final List<Long> numbers = new ArrayList<>();
            try (final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                dataInputStream.skipBytes(headerLength + (startIndex * numberSize));
                for (int i = 0; i < count || count == -1; ++i) {
                    numbers.add(dataInputStream.readLong());
                }
            } catch (EOFException e) {
                //Ignore
            }
            return numbers;
        }

        public File export(final String fileName, final String itemSeparator, final NumberFormat numberFormat) {
            final int BUFFER_SIZE = 1024;
            final File output = new File(FileManager.getInstance().getExportCacheDirectory() + File.separator + fileName);

            try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output))) {
                for (int i = 0; i < totalNumbers; i += BUFFER_SIZE) {
                    final List<Long> numbers = readNumbers(i, BUFFER_SIZE);
                    final long lastItem = numbers.get(numbers.size() - 1);
                    for (long number : numbers) {
                        bufferedWriter.write(numberFormat == null ? String.valueOf(number) : numberFormat.format(number));
                        if (i + BUFFER_SIZE < totalNumbers || number != lastItem) {
                            bufferedWriter.write(itemSeparator);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        public String getTitle(){
            return file.getName();
        }

        public File getFile() {
            return file;
        }

        public int getTotalNumbers() {
            return totalNumbers;
        }
    }

    public static class PrimesFile extends NumbersFile {
        private final long startValue;
        private final long endValue;

        public PrimesFile(final File file) throws IOException {
            super(file);

            //Read header
            final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            dataInputStream.skipBytes(2 + 1);
            final byte[] buffer = new byte[numberSize];
            dataInputStream.readFully(buffer);
            startValue = bytesToNumber(buffer);
            dataInputStream.readFully(buffer);
            endValue = bytesToNumber(buffer);
            dataInputStream.close();
        }

        public long getStartValue() {
            return startValue;
        }

        public long getEndValue() {
            return endValue;
        }

        @Override
        public String getTitle() {
            final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
            return "Primes from " +  numberFormat.format(startValue) + " to " + numberFormat.format(endValue);
        }
    }

    public static void generateDebugFileWithHeader(final File file, final int count){
        try(final DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, false)))){
            //Write header
            dataOutputStream.writeByte(1); //Version
            dataOutputStream.writeByte(3 + 8 * 2); //Header size
            dataOutputStream.writeByte(8); //Number size
            dataOutputStream.writeLong(0); //Start number
            dataOutputStream.writeLong(count); //End number

            //Write data
            for (int i = 0; i < count; ++i){
                dataOutputStream.writeLong((long) i);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static class FactorsFile extends NumbersFile {

        private final long number;

        public FactorsFile(final File file) throws IOException{
            super(file);

            //Read header
            final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            dataInputStream.skipBytes(2 + 1);
            final byte[] buffer = new byte[numberSize];
            dataInputStream.readFully(buffer);
            number = bytesToNumber(buffer);
            dataInputStream.close();
        }

        public long getNumber() {
            return number;
        }

        @Override
        public String getTitle() {
            final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
            return "Factors of " +  numberFormat.format(number);
        }
    }

    public static class TreeFile extends DataFile{

        private final long number;

        private final Tree<Long> tree;

        public TreeFile(final File file) throws IOException{
            super(file, false);

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = bufferedReader.readLine();
            StringBuilder stringBuilder = new StringBuilder();

            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }

            Tree<Long> t;
            try {
                t = Tree.parse(stringBuilder.toString()).toLongTree();
            }catch (Tokenizer.InvalidTokenException e){
                e.printStackTrace();
                t = null;
            }
            this.tree = t;
            number = tree.getValue();
        }

        public long getNumber(){
            return number;
        }

        @Override
        public String getTitle() {
            final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
            return "Factor tree of " +  numberFormat.format(number);
        }
    }

    public static byte[] numberToBytes(final long number) {
        final byte[] bytes = new byte[8];
        for (int i = 0, offset = 8 * 8 - 8; i < 8; ++i, offset -= 8) {
            bytes[i] = (byte) ((number >> offset) & 0xFF);
        }
        return bytes;
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

    public static void upgradeFileSystem_1_4() {
        //Upgrade saved primes
        File[] files = FileManager.getInstance().getSavedPrimesDirectory().listFiles();
        for (File file : files) {
            Log.d(TAG, "Upgrading: " + file);
            final int BUFFER_SIZE = 1024 * 128;
            final List<Long> primes = new ArrayList<>(BUFFER_SIZE);
            final long[] range = getPrimesRangeFromTitle(file);
            final File dest = new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + range[0] + "-" + range[1] + ".primes");

            try (final DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dest)))) {
                //Write header
                dataOutputStream.writeByte(1); //Version
                dataOutputStream.writeByte(3 + 8 * 2); //Header length
                dataOutputStream.writeByte(8); //Number size
                dataOutputStream.writeLong(range[0]); //Start value
                dataOutputStream.writeLong(range[1]); //End value

                final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                boolean endOfFile = false;
                while (!endOfFile) {
                    try {
                        //Read from source
                        for (int i = 0; i < BUFFER_SIZE; ++i) {
                            primes.add(dataInputStream.readLong());
                        }
                    } catch (EOFException e) {
                        endOfFile = true;
                        dataInputStream.close();
                    }

                    //Write to destination
                    for (long number : primes) {
                        dataOutputStream.writeLong(number);
                    }
                    primes.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (File file : files) {
            file.delete();
        }

        //Upgrade saved factors
        files = FileManager.getInstance().getSavedFactorsDirectory().listFiles();
        for (File file : files) {
            Log.d(TAG, "Upgrading: " + file);
            final int BUFFER_SIZE = 1024 * 128;
            final List<Long> factors = new ArrayList<>(BUFFER_SIZE);
            final long number = getNumberFromTitle(file);
            final File dest = new File(FileManager.getInstance().getSavedFactorsDirectory() + File.separator + number + ".primes");

            try (final DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dest)))) {
                //Write header
                dataOutputStream.writeByte(1); //Version
                dataOutputStream.writeByte(3 + 8); //Header length
                dataOutputStream.writeByte(8); //Number size
                dataOutputStream.writeLong(number); //Number

                final DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                boolean endOfFile = false;
                while (!endOfFile) {
                    try {
                        //Read from source
                        for (int i = 0; i < BUFFER_SIZE; ++i) {
                            factors.add(dataInputStream.readLong());
                        }
                    } catch (EOFException e) {
                        endOfFile = true;
                        dataInputStream.close();
                    }

                    //Write to destination
                    for (long n : factors) {
                        dataOutputStream.writeLong(n);
                    }
                    factors.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (File file : files) {
            file.delete();
        }
        PreferenceManager.set(PreferenceManager.Preference.FILE_VERSION, 2);
        Log.d(TAG, "Finished upgrading!");
    }
}
