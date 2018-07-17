package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.TaskListFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import easytasks.Task;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 11/12/2016
 */
public class FindPrimesFragment extends ModuleHostFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesFragment.class.getSimpleName();

    //Views
    private ValidEditText editTextPrimalityInput;
    private ValidEditText editTextSearchRangeStart;
    private ValidEditText editTextSearchRangeEnd;

    //Fragments in the main adapter.
    //private GeneralResultsFragment generalResultsFragment;

    //Results fragments
    private final FindPrimesResultsFragment findPrimesResultsFragment = new FindPrimesResultsFragment();
    private final CheckPrimalityResultsFragment checkPrimalityResultsFragment = new CheckPrimalityResultsFragment();

    /**
     * Search options used for starting tasks.
     */
    private final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, FindPrimesTask.INFINITY, FindPrimesTask.SearchMethod.BRUTE_FORCE, 1);

    /**
     * Request code for starting a new task.
     */
    private static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                final Task task = new CheckPrimalityTask(getPrimalityInput().longValue());
                taskListFragment.addTask(task);
                PrimeNumberFinder.getTaskManager().registerTask(task);

                //Start the task
                task.startOnNewThread();
                taskListFragment.setSelected(task);

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

                Crashlytics.log("Editable: '" + editable + "'");

                //Check if infinity
                if (!editable.toString().equals(getString(R.string.infinity_text))) {

                    //Format text
                    final String formatted = NUMBER_FORMAT.format(getEndValue());
                    if (editable.length() > 0 && !editable.toString().equals(formatted)) {
                        Crashlytics.log("Setting text: '" + formatted + "'");
                        editTextSearchRangeEnd.setText(formatted, formatted.length() > 1);
                    } else if (editable.toString().equals(NUMBER_FORMAT.format(0))) {
                        editTextSearchRangeEnd.getText().clear();
                    }
                }

                searchOptions.setEndValue(getEndValue().longValue());

                Crashlytics.log("Checking valid...");

                //Check if the number is valid
                editTextSearchRangeStart.setValid(editTextSearchRangeStart.length() > 0);
                editTextSearchRangeEnd.setValid(Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod()) && editTextSearchRangeEnd.length() != 0);
            }
        });
        editTextSearchRangeEnd.overrideDefaultTextWatcher();

        //Set up infinity button
        final ImageButton infinityButton = rootView.findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(v -> editTextSearchRangeEnd.setText(getString(R.string.infinity_text), false));

        //Set up find primes button
        final Button buttonFindPrimes = rootView.findViewById(R.id.button_find_primes);
        buttonFindPrimes.setOnClickListener(v -> {

            //Determine best search method
            searchOptions.setSearchMethod(determineBestSearchMethod());

            //Check if the range is valid
            if (Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod())) {

                //Create a new task
                searchOptions.setThreadCount(1);
                try {
                    startTask((FindPrimesTask.SearchOptions) searchOptions.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                //Reset search options
                searchOptions.setSearchMethod(FindPrimesTask.SearchMethod.BRUTE_FORCE);

                hideKeyboard(getActivity());

            } else {
                Toast.makeText(getActivity(), getString(R.string.error_invalid_range), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    protected void loadFragments() {
        taskListFragment = (TaskListFragment) addFragment("Tasks", TaskListFragment.class);
        resultsFragment = (GeneralResultsFragment) addFragment("Results", GeneralResultsFragment.class);
    }

    @Override
    protected void afterLoadFragments() {
        //Set up Task list fragment
        taskListFragment.setAdapter(new FindPrimesTaskListAdapter(getContext()));
        taskListFragment.whitelist(FindPrimesTask.class, CheckPrimalityTask.class);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final FindPrimesTask task = (FindPrimesTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    final FindPrimesTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    if (searchOptions != null) {
                        if (task == null) {
                            startTask(searchOptions);
                        } else {
                            task.setOptions(searchOptions);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), FindPrimesConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    private FindPrimesTask.SearchMethod determineBestSearchMethod() {

        //Check if end value is infinity or is greater than int range
        if (getEndValue().longValue() == FindPrimesTask.INFINITY || getEndValue().compareTo(BigInteger.valueOf(Integer.MAX_VALUE - 1)) > 0) {
            return FindPrimesTask.SearchMethod.BRUTE_FORCE;
        }

        //Make sure we have enough heap memory to use the sieve
        if (getStartValue().compareTo(BigInteger.ZERO) >= 0 && hasEnoughMemoryForSieve()) {
            return FindPrimesTask.SearchMethod.SIEVE_OF_ERATOSTHENES;
        }

        return FindPrimesTask.SearchMethod.BRUTE_FORCE;
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
        Crashlytics.setLong("requiredMB", requiredMB);
        Crashlytics.setLong("availableHeapMB", availHeapSizeInMB);

        return requiredMB <= availHeapSizeInMB;
    }

    private void startTask(final FindPrimesTask.SearchOptions searchOptions) {
        final FindPrimesTask task = new FindPrimesTask(searchOptions);
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped() {
                if (task.getSearchOptions().isAutoSave()) {
                    new Thread(() -> {
                        final boolean success = FileManager.getInstance().savePrimes(task.getStartValue(), task.getEndValue(), task.getSortedPrimes());
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show());
                    }).start();
                }

                if (task.getSearchOptions().isNotifyWhenFinished()) {
                    com.tycho.app.primenumberfinder.utils.NotificationManager.displayNotification(getActivity(), "default", task, com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_PRIMES, "Task \"Primes from " + NUMBER_FORMAT.format(task.getStartValue()) + " to " + NUMBER_FORMAT.format(task.getEndValue()) + "\" finished.");
                }
                task.removeTaskListener(this);
            }
        });
        taskListFragment.addTask(task);
        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();
        Utils.logTaskStarted(getContext(), task);

        taskListFragment.setSelected(task);
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

    @Override
    public void onTaskSelected(Task task) {
        if (task instanceof FindPrimesTask) {
            findPrimesResultsFragment.setTask(task);
            checkPrimalityResultsFragment.setTask(null);
            ((GeneralResultsFragment) resultsFragment).setContent(findPrimesResultsFragment);
        } else if (task instanceof CheckPrimalityTask) {
            findPrimesResultsFragment.setTask(null);
            checkPrimalityResultsFragment.setTask(task);
            ((GeneralResultsFragment) resultsFragment).setContent(checkPrimalityResultsFragment);
        } else {
            ((GeneralResultsFragment) resultsFragment).setContent(null);
            findPrimesResultsFragment.setTask(null);
            checkPrimalityResultsFragment.setTask(null);
        }
    }

    @Override
    public void onPausePressed(Task task) {

    }

    @Override
    public void onTaskRemoved(Task task) {
        if (findPrimesResultsFragment.getTask() == task) {
            findPrimesResultsFragment.setTask(null);
        }
        if (checkPrimalityResultsFragment.getTask() == task) {
            checkPrimalityResultsFragment.setTask(null);
        }

        taskListFragment.update();
    }

    @Override
    public void onEditPressed(Task task) {
        final Intent intent = new Intent(getActivity(), FindPrimesConfigurationActivity.class);
        intent.putExtra("searchOptions", ((FindPrimesTask) task).getSearchOptions());
        intent.putExtra("taskId", task.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    public void onSavePressed(Task task) {
        if (task instanceof FindPrimesTask) {
            Crashlytics.log(Log.DEBUG, TAG, "Save button clicked\nActivity: " + getActivity() + "\nView: " + taskListFragment.getView());
            findPrimesResultsFragment.saveTask((FindPrimesTask) task, getActivity());
        }
    }
}
