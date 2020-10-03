package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesNativeTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;
import com.tycho.app.primenumberfinder.utils.NotificationManager;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import easytasks.ITask;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.BRUTE_FORCE;
import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.SIEVE_OF_ERATOSTHENES;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_FACTORS;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_PRIMES;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_GCF;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_LCM;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_PRIME_FACTORIZATION;
import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 11/12/2016
 */
public class FindPrimesFragment extends Fragment implements AbstractTaskListAdapter.EventListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesFragment.class.getSimpleName();

    //Views
    private ValidEditText editTextPrimalityInput;
    private ValidEditText editTextSearchRangeStart;
    private ValidEditText editTextSearchRangeEnd;

    //Results fragments
    private final FindPrimesResultsFragment findPrimesResultsFragment = new FindPrimesResultsFragment();
    private final CheckPrimalityResultsFragment checkPrimalityResultsFragment = new CheckPrimalityResultsFragment();

    /**
     * Search options used for starting tasks.
     */
    private final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, FindPrimesTask.INFINITY, BRUTE_FORCE, 1);

    /**
     * Request code for starting a new task.
     */
    private static final int REQUEST_CODE_NEW_TASK = 0;

    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    @Override
    public void onPausePressed(ITask task) {

    }

    @Override
    public void onSavePressed(ITask task) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_fragment, container, false);

        //Set up number input
        editTextPrimalityInput = rootView.findViewById(R.id.primality_input);
        editTextPrimalityInput.setHint(NUMBER_FORMAT.format(new Random().nextInt(1_000_000)));
        editTextPrimalityInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Check if the number is valid
                editTextPrimalityInput.setValid(Validator.isPrimalityInputValid(getPrimalityInput()));
            }
        });

        //Set up check primality button
        final Button buttonCheckPrimality = rootView.findViewById(R.id.check_primality_button);
        buttonCheckPrimality.setOnClickListener(v -> {

            //Check if the number is valid
            if (Validator.isPrimalityInputValid(getPrimalityInput())) {

                //Create a new task
                final ITask task = new CheckPrimalityTask(getPrimalityInput().longValue());
                startTask(task);
                hideKeyboard(getActivity());

            } else {
                Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
            }
        });

        //Set up range start input
        editTextSearchRangeStart = rootView.findViewById(R.id.search_range_start);
        editTextSearchRangeStart.setHint(NUMBER_FORMAT.format(0));
        editTextSearchRangeStart.setAllowZeroInput(true);
        editTextSearchRangeStart.setText(NUMBER_FORMAT.format(searchOptions.getStartValue()));
        editTextSearchRangeStart.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchOptions.setStartValue(getStartValue().longValue());

                //Check if the number is valid
                editTextSearchRangeStart.setValid((Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod()) && editTextSearchRangeStart.length() > 0) || (editTextSearchRangeStart.length() != 0 && editTextSearchRangeEnd.length() == 0));
                editTextSearchRangeEnd.setValid(editTextSearchRangeEnd.length() > 0);
            }
        });

        //Set up range end input
        editTextSearchRangeEnd = rootView.findViewById(R.id.search_range_end);
        editTextSearchRangeEnd.setHint(NUMBER_FORMAT.format(new Random().nextInt(1_000_000)));
        editTextSearchRangeEnd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                FirebaseCrashlytics.getInstance().log("Editable: '" + editable + "'");

                //Check if infinity
                if (!editable.toString().equals(getString(R.string.infinity_text))) {

                    //Format text
                    final String formatted = NUMBER_FORMAT.format(getEndValue());
                    if (editable.length() > 0 && !editable.toString().equals(formatted)) {
                        FirebaseCrashlytics.getInstance().log("Setting text: '" + formatted + "'");
                        editTextSearchRangeEnd.setText(formatted, formatted.length() > 1);
                    } else if (editable.toString().equals(NUMBER_FORMAT.format(0))) {
                        editTextSearchRangeEnd.getText().clear();
                    }
                }

                searchOptions.setEndValue(getEndValue().longValue());

                FirebaseCrashlytics.getInstance().log("Checking valid...");

                //Check if the number is valid
                editTextSearchRangeStart.setValid(editTextSearchRangeStart.length() > 0);
                editTextSearchRangeEnd.setValid(Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod()) && editTextSearchRangeEnd.length() != 0);
            }
        });
        editTextSearchRangeEnd.overrideDefaultTextWatcher();

        //Set up infinity button
        final ImageButton infinityButton = rootView.findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(v -> {
            editTextSearchRangeEnd.setText(getString(R.string.infinity_text), false);
        });

        //Set up find primes button
        final Button buttonFindPrimes = rootView.findViewById(R.id.button_find_primes);
        buttonFindPrimes.setOnClickListener(v -> {

            //Determine best search method
            searchOptions.setSearchMethod(determineBestSearchMethod());

            //Check if the range is valid
            if (Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod())) {

                //Create a new task
                searchOptions.setThreadCount(1);

                searchOptions.setCacheDirectory(getActivity().getCacheDir());

                try {
                    startTask(new FindPrimesNativeTask((FindPrimesTask.SearchOptions) searchOptions.clone()));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                //Reset search options
                searchOptions.setSearchMethod(BRUTE_FORCE);

                hideKeyboard(getActivity());

            } else {
                Toast.makeText(getActivity(), getString(R.string.error_invalid_range), Toast.LENGTH_SHORT).show();
            }
        });

        fragment = findPrimesResultsFragment;
        getChildFragmentManager().beginTransaction().add(R.id.container, fragment).commit();

        advanced = rootView.findViewById(R.id.advanced_search);
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getActivity(), FindPrimesConfigurationActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
            }
        });

        return rootView;
    }

    private View advanced;

    private ResultsFragment fragment;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final FindPrimesTask task = (FindPrimesTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    final FindPrimesTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    if (searchOptions != null) {
                        if (task == null) {
                            startTask(new FindPrimesNativeTask(searchOptions));
                        } else {
                            task.setSearchOptions(searchOptions);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onTaskSelected(ITask task) {

    }

    @Override
    public void onTaskRemoved(ITask task) {
        if (findPrimesResultsFragment.getTask() == task) {
            findPrimesResultsFragment.setTask(null);
        }
        if (checkPrimalityResultsFragment.getTask() == task) {
            checkPrimalityResultsFragment.setTask(null);
        }
    }

    private FindPrimesTask.SearchOptions.SearchMethod determineBestSearchMethod() {

        //Check if end value is infinity or is greater than int range
        if (getEndValue().longValue() == FindPrimesTask.INFINITY || getEndValue().compareTo(BigInteger.valueOf(Integer.MAX_VALUE - 1)) > 0) {
            return BRUTE_FORCE;
        }

        //Make sure we have enough heap memory to use the sieve
        if (getStartValue().compareTo(BigInteger.ZERO) >= 0 && hasEnoughMemoryForSieve()) {
            return SIEVE_OF_ERATOSTHENES;
        }

        return BRUTE_FORCE;
    }

    private boolean hasEnoughMemoryForSieve() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;

        final long bits = searchOptions.getEndValue() + 1;
        final int longCount = (int) (bits / 8) + 1;
        final int arraySize = longCount * 8;
        final long estimatedPrimeCount = (long) ((searchOptions.getEndValue() / Math.log(searchOptions.getEndValue())) - (searchOptions.getStartValue() / Math.log(searchOptions.getStartValue())));
        final long primesSize = estimatedPrimeCount * 8;
        final long totalBytes = arraySize + primesSize;

        //Add an extra 13% due to inaccurate estimate of prime count
        final int requiredMB = (int) ((totalBytes * 1.13f) / (1024 * 1024));

        //Log.d(TAG, "RAM: " + usedMemInMB + " / " + maxHeapSizeInMB);
        //Log.d(TAG, "Avail: " + availHeapSizeInMB);
        //Log.d(TAG, "Req: " + requiredMB);
        FirebaseCrashlytics.getInstance().setCustomKey("requiredMB", requiredMB);
        FirebaseCrashlytics.getInstance().setCustomKey("availableHeapMB", availHeapSizeInMB);

        return requiredMB <= availHeapSizeInMB;
    }

    private BigInteger getPrimalityInput() {
        return Utils.textToNumber(editTextPrimalityInput.getText().toString());
    }

    private BigInteger getStartValue() {
        return Utils.textToNumber(editTextSearchRangeStart.getText().toString());
    }

    private BigInteger getEndValue() {
        final String input = editTextSearchRangeEnd.getText().toString().trim();

        //Check for infinity
        if (input.equals(getString(R.string.infinity_text))) {
            return BigInteger.valueOf(FindPrimesTask.INFINITY);
        }

        return Utils.textToNumber(input);
    }

    protected void startTask(final ITask task){
        if (task instanceof CheckPrimalityTask){
            fragment = checkPrimalityResultsFragment;
        }else{
            fragment = findPrimesResultsFragment;
        }
        getChildFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        fragment.setTask(task);

        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped(final ITask task) {

                final GeneralSearchOptions searchOptions;
                if (task instanceof SearchOptions){
                    searchOptions = ((SearchOptions) task).getSearchOptions();
                }else{
                    searchOptions = null;
                }

                if (searchOptions != null){
                    //Auto-save
                    if (task instanceof Savable && searchOptions.isAutoSave()){
                        new Thread(() -> {
                            final boolean success = ((Savable) task).save();
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show());
                        }).start();
                    }

                    //Notify when finished
                    if (searchOptions.isNotifyWhenFinished()) {
                        final String content;
                        final int taskType;
                        final int smallIconDrawable;
                        if (task instanceof FindPrimesTask){
                            taskType = TASK_TYPE_FIND_PRIMES;
                            smallIconDrawable = R.drawable.find_primes_icon;
                            content = "Task \"Primes from " + NUMBER_FORMAT.format(((FindPrimesTask) task).getStartValue()) + " to " + NUMBER_FORMAT.format(((FindPrimesTask) task).getEndValue()) + "\" finished.";
                        }else if (task instanceof FindFactorsTask){
                            taskType = TASK_TYPE_FIND_FACTORS;
                            smallIconDrawable = R.drawable.find_factors_icon;
                            content = "Task \"Factors of " + NUMBER_FORMAT.format(((FindFactorsTask) task).getNumber()) + "\" finished.";
                        }else if (task instanceof PrimeFactorizationTask){
                            taskType = TASK_TYPE_PRIME_FACTORIZATION;
                            smallIconDrawable = R.drawable.prime_factorization_icon;
                            content = "Task \"Prime factorization of " + NUMBER_FORMAT.format(((PrimeFactorizationTask) task).getNumber()) + "\" finished.";
                        }else if (task instanceof LeastCommonMultipleTask){
                            taskType = TASK_TYPE_LCM;
                            smallIconDrawable = R.drawable.lcm_icon;
                            content = "Task \"LCM of " + Utils.formatNumberList(((LeastCommonMultipleTask) task).getNumbers(), NUMBER_FORMAT, ",") + "\" finished.";
                        }else if (task instanceof GreatestCommonFactorTask){
                            taskType = TASK_TYPE_GCF;
                            smallIconDrawable = R.drawable.gcf_icon;
                            content = "Task \"GCF of " + Utils.formatNumberList(((GreatestCommonFactorTask) task).getNumbers(), NUMBER_FORMAT, ",") + "\" finished.";
                        } else{
                            return;
                        }
                        NotificationManager.displayNotification(getActivity(), "default", task, taskType, content, smallIconDrawable);
                    }
                }

                task.removeTaskListener(this);
            }
        });
        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();
        Utils.logTaskStarted(getContext(), task);
    }
}
