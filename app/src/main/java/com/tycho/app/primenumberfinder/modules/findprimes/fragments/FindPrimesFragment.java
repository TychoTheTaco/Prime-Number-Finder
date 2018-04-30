package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FabAnimator;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.IntentReceiver;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SimpleFragmentAdapter;
import com.tycho.app.primenumberfinder.ValidEditText;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import easytasks.Task;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 11/12/2016
 */
public class FindPrimesFragment extends Fragment implements FloatingActionButtonListener, IntentReceiver {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesFragment.class.getSimpleName();

    //Views
    private ValidEditText editTextPrimalityInput;
    private ValidEditText editTextSearchRangeStart;
    private ValidEditText editTextSearchRangeEnd;

    //Fragments in the main adapter.
    private final FindPrimesTaskListFragment taskListFragment = new FindPrimesTaskListFragment();
    private final GeneralResultsFragment generalResultsFragment = new GeneralResultsFragment();

    //Results fragments
    private final FindPrimesResultsFragment findPrimesResultsFragment = new FindPrimesResultsFragment();
    private final CheckPrimalityResultsFragment checkPrimalityResultsFragment = new CheckPrimalityResultsFragment();

    /**
     * {@linkplain NumberFormat} instance used to format numbers with commas.
     */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    /**
     * Search options used for starting tasks.
     */
    private final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, FindPrimesTask.INFINITY, FindPrimesTask.SearchMethod.BRUTE_FORCE, 1);

    /**
     * Request code for starting a new task.
     */
    private static final int REQUEST_CODE_NEW_TASK = 0;

    private ViewPager viewPager;
    private FabAnimator fabAnimator;

    private Intent intent;

    private FloatingActionButtonHost floatingActionButtonHost;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FloatingActionButtonHost) {
            floatingActionButtonHost = (FloatingActionButtonHost) context;
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_fragment, viewGroup, false);

        //Set up tab layout for results and statistics
        final SimpleFragmentAdapter simpleFragmentAdapter = new SimpleFragmentAdapter(getChildFragmentManager());
        viewPager = rootView.findViewById(R.id.view_pager);
        simpleFragmentAdapter.add(taskListFragment, "Tasks");
        taskListFragment.addEventListener(new FindPrimesTaskListAdapter.EventListener() {
            @Override
            public void onTaskSelected(Task task) {
                if (task instanceof FindPrimesTask) {
                    findPrimesResultsFragment.setTask(task);
                    checkPrimalityResultsFragment.setTask(null);
                    generalResultsFragment.setContent(findPrimesResultsFragment);
                } else if (task instanceof CheckPrimalityTask) {
                    findPrimesResultsFragment.setTask(null);
                    checkPrimalityResultsFragment.setTask(task);
                    generalResultsFragment.setContent(checkPrimalityResultsFragment);
                } else {
                    //generalResultsFragment.setContent(null);
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
        });
        simpleFragmentAdapter.add(generalResultsFragment, "Results");
        generalResultsFragment.setContent(findPrimesResultsFragment);
        viewPager.setAdapter(simpleFragmentAdapter);
        fabAnimator = new FabAnimator(floatingActionButtonHost.getFab(0));
        viewPager.addOnPageChangeListener(fabAnimator);
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

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
                //Format the number
                final String formattedText = NUMBER_FORMAT.format(getPrimalityInput());
                if (!editable.toString().equals(formattedText)) {
                    editTextPrimalityInput.setText(formattedText);
                }
                editTextPrimalityInput.setSelection(formattedText.length());

                //Check if the number is valid
                editTextPrimalityInput.setValid(Validator.isPrimalityInputValid(getPrimalityInput()));
            }
        });

        //Set up check primality button
        final Button buttonCheckPrimality = rootView.findViewById(R.id.check_primality_button);
        buttonCheckPrimality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        //Set up range start input
        editTextSearchRangeStart = rootView.findViewById(R.id.search_range_start);
        editTextSearchRangeStart.setHint(NUMBER_FORMAT.format(0));
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

                //Check if infinity
                if (!editable.toString().equals(getString(R.string.infinity_text))) {

                    //Format text
                    final String formatted = NUMBER_FORMAT.format(getStartValue());
                    if (!editable.toString().equals(formatted)) {
                        editTextSearchRangeStart.setText(formatted);
                        editTextSearchRangeStart.setSelection(formatted.length());
                    }
                }

                //Check if the number is valid
                if (Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod())) {
                    editTextSearchRangeStart.setValid(true);
                    editTextSearchRangeEnd.setValid(true);
                } else if (editTextSearchRangeStart.hasFocus()) {
                    editTextSearchRangeStart.setValid(editTextSearchRangeEnd.getText().length() == 0);
                }
            }
        });

        //Set up range end input
        editTextSearchRangeEnd = rootView.findViewById(R.id.search_range_end);
        editTextSearchRangeEnd.setHint(NUMBER_FORMAT.format(Integer.valueOf(editTextSearchRangeStart.getHint().toString().replace(",", "")) + new Random().nextInt(1_000_000)));
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
                    if (!editable.toString().equals(formatted)) {
                        Crashlytics.log("Setting text: '" + formatted + "'");
                        editTextSearchRangeEnd.setText(formatted);
                        editTextSearchRangeEnd.setSelection(formatted.length());
                    }
                }

                Crashlytics.log("Checking valid...");

                //Check if the number is valid
                if (Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod())) {
                    editTextSearchRangeStart.setValid(true);
                    editTextSearchRangeEnd.setValid(true);
                } else if (editTextSearchRangeEnd.hasFocus()) {
                    editTextSearchRangeEnd.setValid(editTextSearchRangeStart.getText().length() == 0);
                }
            }
        });

        //Set up infinity button
        final ImageButton infinityButton = rootView.findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearchRangeEnd.setText(getString(R.string.infinity_text));
                editTextSearchRangeEnd.setSelection(getString(R.string.infinity_text).length());
            }
        });

        //Set up find primes button
        final Button buttonFindPrimes = rootView.findViewById(R.id.button_find_primes);
        buttonFindPrimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Determine best search method
                if (getStartValue().compareTo(BigInteger.ZERO) > 0 || getEndValue().longValue() == FindPrimesTask.INFINITY || getEndValue().compareTo(BigInteger.valueOf(Integer.MAX_VALUE - 1)) > 0) {
                    searchOptions.setSearchMethod(FindPrimesTask.SearchMethod.BRUTE_FORCE);
                } else {
                    searchOptions.setSearchMethod(FindPrimesTask.SearchMethod.SIEVE_OF_ERATOSTHENES);
                }

                //Check if the range is valid
                if (Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod())) {

                    //Create a new task
                    searchOptions.setStartValue(getStartValue().longValue());
                    searchOptions.setEndValue(getEndValue().longValue());
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
            }
        });

        //Give the root view focus to prevent EditTexts from initially getting focus
        rootView.requestFocus();

        //Scroll to Results fragment if started from a notification
        if (intent.getSerializableExtra("taskId") != null) {
            viewPager.setCurrentItem(1);
        }

        //Reset intent
        this.intent = null;

        return rootView;
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

    @Override
    public void initFab(View view) {
        if (fabAnimator != null) {
            fabAnimator.onPageScrolled(viewPager.getCurrentItem(), 0, 0);
        }
    }

    @Override
    public void giveIntent(Intent intent) {
        this.intent = intent;
        taskListFragment.giveIntent(intent);
    }

    private void startTask(final FindPrimesTask.SearchOptions searchOptions) {

        boolean valid;

        //Make sure there is enough memory
        if (searchOptions.getSearchMethod() == FindPrimesTask.SearchMethod.SIEVE_OF_ERATOSTHENES) {

            final Runtime runtime = Runtime.getRuntime();
            final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
            final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
            final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;

            final long requiredMB = ((searchOptions.getEndValue() / 8) + (searchOptions.getEndValue() * 2)) / (1024 * 1024);

            Log.d(TAG, "RAM: " + usedMemInMB + " / " + maxHeapSizeInMB);
            Log.d(TAG, "Avail: " + availHeapSizeInMB);
            Log.d(TAG, "Req: " + requiredMB);

            if (requiredMB <= availHeapSizeInMB * 0.9f) {
                valid = true;
            } else {
                valid = false;
                Toast.makeText(getActivity(), "Not enough RAM to start task! Use Brute Force instead!", Toast.LENGTH_LONG).show();
            }
        } else {
            valid = true;
        }

        if (valid) {
            final FindPrimesTask task = new FindPrimesTask(searchOptions, getActivity());
            task.addTaskListener(new TaskAdapter() {

                @Override
                public void onTaskStopped() {
                    if (task.getSearchOptions().isAutoSave()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final boolean success = FileManager.getInstance().savePrimes(task.getStartValue(), task.getEndValue(), task.getSortedPrimes());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).start();
                    }

                    if (task.getSearchOptions().isNotifyWhenFinished()) {
                        com.tycho.app.primenumberfinder.utils.NotificationManager.displayNotification(getActivity(), "default", task, com.tycho.app.primenumberfinder.utils.NotificationManager.REQUEST_CODE_FIND_PRIMES, "Task \"Primes from " + NUMBER_FORMAT.format(task.getStartValue()) + " to " + NUMBER_FORMAT.format(task.getEndValue()) + "\" finished.");
                    }
                    task.removeTaskListener(this);
                }
            });
            taskListFragment.addTask(task);
            PrimeNumberFinder.getTaskManager().registerTask(task);

            //Start the task
            task.startOnNewThread();

            //Post to a handler because "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState"
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    taskListFragment.setSelected(task);
                }
            });
        }
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

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        taskListFragment.addActionViewListener(actionViewListener);
    }
}
