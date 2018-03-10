package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ValidEditText;
import com.tycho.app.primenumberfinder.activities.MainActivity;
import com.tycho.app.primenumberfinder.adapters.FragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindPrimesFragment extends Fragment implements FloatingActionButtonListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesFragment";

    /*
     * Views.
     */
    private EditText editTextPrimalityInput;
    private ValidEditText editTextSearchRangeStart;
    private ValidEditText editTextSearchRangeEnd;

    /*
    Fragments in the main adapter.
     */
    private final FindPrimesTaskListFragment taskListFragment = new FindPrimesTaskListFragment();
    private final GeneralResultsFragment generalResultsFragment = new GeneralResultsFragment();
    private final GeneralStatisticsFragment generalStatisticsFragment = new GeneralStatisticsFragment();

    /*
    Find primes fragments
     */
    private final FindPrimesResultsFragment findPrimesResultsFragment = new FindPrimesResultsFragment();
    private final FindPrimesStatisticsFragment findPrimesStatisticsFragment = new FindPrimesStatisticsFragment();

    /*
    Check primality fragments
     */
    private final CheckPrimalityResultsFragment checkPrimalityResultsFragment = new CheckPrimalityResultsFragment();
    private final CheckPrimalityStatisticsFragment checkPrimalityStatisticsFragment = new CheckPrimalityStatisticsFragment();

    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());


    private final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, FindPrimesTask.INFINITY, FindPrimesTask.SearchMethod.BRUTE_FORCE, 1);

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_fragment, viewGroup, false);

        //Set up tab layout for results and statistics
        final FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager());
        final ViewPager viewPager = rootView.findViewById(R.id.view_pager);
        fragmentAdapter.add("Tasks", taskListFragment);
        taskListFragment.addEventListener(new FindPrimesTaskListAdapter.EventListener() {
            @Override
            public void onTaskSelected(Task task) {
                if (task instanceof FindPrimesTask) {
                    findPrimesResultsFragment.setTask(task);
                    checkPrimalityResultsFragment.setTask(null);
                    generalResultsFragment.setContent(findPrimesResultsFragment);

                    findPrimesStatisticsFragment.setTask(task);
                    checkPrimalityStatisticsFragment.setTask(null);
                    generalStatisticsFragment.setContent(findPrimesStatisticsFragment);
                } else if (task instanceof CheckPrimalityTask) {
                    findPrimesResultsFragment.setTask(null);
                    checkPrimalityResultsFragment.setTask(task);
                    generalResultsFragment.setContent(checkPrimalityResultsFragment);

                    findPrimesStatisticsFragment.setTask(null);
                    checkPrimalityStatisticsFragment.setTask(task);
                    generalStatisticsFragment.setContent(checkPrimalityStatisticsFragment);
                } else {
                    //generalResultsFragment.setContent(null);
                    findPrimesResultsFragment.setTask(null);
                    checkPrimalityResultsFragment.setTask(null);

                    //generalStatisticsFragment.setContent(null);
                    findPrimesStatisticsFragment.setTask(null);
                    checkPrimalityStatisticsFragment.setTask(null);
                }
            }

            @Override
            public void onPausePressed(Task task) {

            }

            @Override
            public void onTaskRemoved(Task task) {
                if (findPrimesResultsFragment.getTask() == task) {
                    findPrimesResultsFragment.setTask(null);
                    findPrimesStatisticsFragment.setTask(null);
                }
                if (checkPrimalityResultsFragment.getTask() == task) {
                    checkPrimalityResultsFragment.setTask(null);
                    checkPrimalityResultsFragment.setTask(null);
                }

                taskListFragment.update();
            }
        });
        fragmentAdapter.add("Results", generalResultsFragment);
        fragmentAdapter.add("Statistics", generalStatisticsFragment);
        generalResultsFragment.setContent(findPrimesResultsFragment);
        generalStatisticsFragment.setContent(findPrimesStatisticsFragment);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private final FloatingActionButton fab = ((MainActivity) getActivity()).getFab();
            private int diameter;
            private double circumference;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                diameter = fab.getWidth();
                circumference = (2f * Math.PI * ((float) diameter / 2));
                switch (position) {
                    case 0:
                        fab.setVisibility(View.VISIBLE);
                        fab.setTranslationX(-positionOffsetPixels);
                        if (circumference > 0) {
                            fab.setRotation(-360 * positionOffset * ((float) (rootView.getWidth() / circumference)));
                        }
                        break;

                    default:
                        fab.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //TODO: We might not want to do this because the last fragment doesn't get even spacing. Either center it or allow scrolling.
                /*
                 * This empty touch listener is to prevent the scrolling behaviour of this
                 * TabLayout's parent HorizontalScrollView.
                 */
                return true;
            }
        });
        tabLayout.setupWithViewPager(viewPager);

        //Set up number input
        editTextPrimalityInput = rootView.findViewById(R.id.primality_input);
        editTextPrimalityInput.setHint(numberFormat.format(new Random().nextInt(1000000)));
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
                if (isNumberValid()) {
                    editTextPrimalityInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getPrimalityInput());
                    if (!editable.toString().equals(formattedText)) {
                        editTextPrimalityInput.setText(formattedText);
                    }
                    editTextPrimalityInput.setSelection(formattedText.length());

                } else {
                    editTextPrimalityInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
            }
        });
        editTextPrimalityInput.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Clear text on touch
                editTextPrimalityInput.getText().clear();
                return false;
            }
        });

        //Set up check primality button
        final Button buttonCheckPrimality = rootView.findViewById(R.id.check_primality_button);
        buttonCheckPrimality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (isNumberValid()) {

                    //Create a new task
                    final Task task = new CheckPrimalityTask(getPrimalityInput());
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
        editTextSearchRangeStart.setText(numberFormat.format(searchOptions.getStartValue()));
        editTextSearchRangeStart.setHint(numberFormat.format(0));
        editTextSearchRangeStart.addTextChangedListener(new TextWatcher() {

            private boolean isDirty = true;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (isDirty) {

                    //Format text
                    if (editable.length() > 0) {
                        final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getStartValue());
                        if (!editable.toString().equals(formattedText)) {
                            isDirty = false;
                            editTextSearchRangeStart.setText(formattedText);
                        }
                        editTextSearchRangeStart.setSelection(formattedText.length());
                    }

                    //Check if the number is valid
                    if (isRangeValid()){
                        editTextSearchRangeStart.setValid(true);
                        editTextSearchRangeEnd.setValid(true);
                    }else if (editTextSearchRangeStart.hasFocus()){
                        editTextSearchRangeStart.setValid(editTextSearchRangeEnd.getText().length() == 0);
                    }

                }
                isDirty = true;
            }
        });
        editTextSearchRangeStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Clear text on touch
                editTextSearchRangeStart.getText().clear();
                return false;
            }
        });

        //Set up range end input
        editTextSearchRangeEnd = rootView.findViewById(R.id.search_range_end);
        editTextSearchRangeEnd.setHint(numberFormat.format(Integer.valueOf(editTextSearchRangeStart.getHint().toString().replace(",", "")) + new Random().nextInt(1000000)));
        editTextSearchRangeEnd.addTextChangedListener(new TextWatcher() {

            private boolean isDirty = true;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (isDirty) {

                    //Format text
                    if (editable.length() > 0) {
                        if (getEndValue().compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) == 0) {
                            if (!editable.toString().equals(getString(R.string.infinity_text))) {
                                editTextSearchRangeEnd.setText(getString(R.string.infinity_text));
                            }
                            editTextSearchRangeEnd.setSelection(editTextSearchRangeEnd.getText().length());
                        } else {
                            final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getEndValue());
                            if (!editable.toString().equals(formattedText)) {
                                isDirty = false;
                                editTextSearchRangeEnd.setText(formattedText);
                            }
                            editTextSearchRangeEnd.setSelection(formattedText.length());
                        }
                    }

                    //Check if the number is valid
                    if (isRangeValid()){
                        editTextSearchRangeStart.setValid(true);
                        editTextSearchRangeEnd.setValid(true);
                    }else if (editTextSearchRangeEnd.hasFocus()){
                        editTextSearchRangeEnd.setValid(editTextSearchRangeStart.getText().length() == 0);
                    }

                }
                isDirty = true;
            }
        });
        editTextSearchRangeEnd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextSearchRangeEnd.getText().clear();
                return false;
            }
        });

        //Set up infinity button
        final ImageButton infinityButton = rootView.findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearchRangeEnd.setText(getString(R.string.infinity_text));
            }
        });

        //Set up find primes button
        final Button buttonFindPrimes = rootView.findViewById(R.id.button_find_primes);
        buttonFindPrimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getStartValue().compareTo(BigInteger.ZERO) == 1 ||getEndValue().longValue() == FindPrimesTask.INFINITY || getEndValue().compareTo(BigInteger.valueOf(Integer.MAX_VALUE - 1)) == 1){
                    searchOptions.setSearchMethod(FindPrimesTask.SearchMethod.BRUTE_FORCE);
                }else{
                    searchOptions.setSearchMethod(FindPrimesTask.SearchMethod.SIEVE_OF_ERATOSTHENES);
                }

                //Check if the range is valid
                if (isRangeValid()) {

                    //Create a new task
                    searchOptions.setStartValue(getStartValue().longValue());
                    searchOptions.setEndValue(getEndValue().longValue());
                    searchOptions.setThreadCount(1);
                    startTask(searchOptions);
                    searchOptions.setSearchMethod(FindPrimesTask.SearchMethod.BRUTE_FORCE);

                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_range), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private long getPrimalityInput() {
        final String input = editTextPrimalityInput.getText().toString().trim();
        if (input.length() > 0) {
            final BigInteger number = new BigInteger(input.replace(",", ""));
            return number.longValue();
        } else {
            return 0;
        }
    }

    private BigInteger getStartValue() {
        final String input = editTextSearchRangeStart.getText().toString().trim();
        if (input.length() > 0) {
            return new BigInteger(input.replace(",", ""));
        } else {
            return BigInteger.ZERO;
        }
    }

    private BigInteger getEndValue() {
        final String input = editTextSearchRangeEnd.getText().toString().trim();

        if (input.length() > 0) {
            if (input.equals("infinity")) {
                return BigInteger.valueOf(FindPrimesTask.INFINITY);
            }
            return new BigInteger(input.replace(",", ""));
        } else {
            return BigInteger.ZERO;
        }
    }

    //Validation

    /**
     * Check if the primality input number is valid.
     *
     * @return <code>true</code> if valid. <code>false</code> if invalid.
     */
    private boolean isNumberValid() {
        try {
            final BigInteger number = new BigInteger(editTextPrimalityInput.getText().toString().replace(",", ""));

            //Check if number is greater than long range
            if (number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
                return false;
            }

            //Check if number is less than 1
            if (number.compareTo(BigInteger.ONE) == -1) {
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isRangeValid() {

        //Validate the start value
        try {
            final BigInteger startValue = new BigInteger(editTextSearchRangeStart.getText().toString().replace(",", ""));
            BigInteger endValue;
            final String searchRangeEndText = editTextSearchRangeEnd.getText().toString();
            if (searchRangeEndText.equals(getString(R.string.infinity_text))) {
                endValue = BigInteger.valueOf(FindPrimesTask.INFINITY);
            } else {
                endValue = new BigInteger(searchRangeEndText.replace(",", ""));
            }

            //Check if numbers are greater than long range
            if (startValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1 || endValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
                return false;
            }

            //The start value must be at least 0
            if (startValue.compareTo(BigInteger.ZERO) == -1) {
                return false;
            }

            //Depends on search method
            switch (searchOptions.getSearchMethod()) {
                case SIEVE_OF_ERATOSTHENES:

                    //The start value must equal to 0
                    if (startValue.compareTo(BigInteger.ZERO) != 0) {
                        return false;
                    }

                    //The end value cannot be infinity
                    if (endValue.compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) == 0) {
                        return false;
                    }

                    break;

                case BRUTE_FORCE:
                    break;
            }

            if (endValue.compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) != 0) {

                //End value must be greater than start value
                if (startValue.compareTo(endValue) >= 0) {
                    return false;
                }

                //End value must be greater than current number
                /*if (taskFragment != null && taskFragment.getTask() != null && taskFragment.getTask() instanceof FindPrimesTask) {
                    if (endValue.compareTo(BigInteger.valueOf(((FindPrimesTask) taskFragment.getTask()).getCurrentNumber())) <= 0) {
                        return false;
                    }
                }*/
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        taskListFragment.addActionViewListener(actionViewListener);
    }

    private static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), FindPrimesConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final FindPrimesTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final FindPrimesTask task = (FindPrimesTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    Log.d(TAG, "Task: " + task);
                    if (task == null) {
                        startTask(searchOptions);
                    } else {
                        task.setOptions(searchOptions);
                    }
                }
                break;
        }
    }

    private void startTask(final FindPrimesTask.SearchOptions searchOptions) {
        final FindPrimesTask task = new FindPrimesTask(searchOptions, getActivity());
        task.addTaskListener(new TaskAdapter() {
            @Override
            public void onTaskStopped() {
                if (task.getSearchOptions().autoSave) {
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

                if (task.getSearchOptions().notifyWhenFinished) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                            .setSmallIcon(R.drawable.circle_white)
                            .setContentTitle("Task Finished")
                            .setContentText("Task \"Primes from " + task.getStartValue() + " to " + task.getEndValue() + "\" finished.");
                    final NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                }
            }
        });
        taskListFragment.addTask(task);
        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                taskListFragment.setSelected(task);
            }
        });

    }
}
