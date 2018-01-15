package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.app.Fragment;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
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

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.adapters.FragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindPrimesFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesFragment";

    /*
     * Views.
     */
    private EditText editTextPrimalityInput;
    private EditText editTextSearchRangeStart;
    private EditText editTextSearchRangeEnd;
    private Button buttonCheckPrimality;
    private Button buttonFindPrimes;
    private ImageButton infinityButton;

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


    private final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, FindPrimesTask.END_VALUE_INFINITY, FindPrimesTask.SearchOptions.Method.BRUTE_FORCE, FindPrimesTask.SearchOptions.MonitorType.SIMPLE);

    /*private BottomSheetBehavior bottomSheetBehavior;
    private RadioGroup radioGroupSearchMethod;*/

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
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //TODO: We might not want to do this because the last fragment doesn't get even spacing. Either center it or allow scrolling.
                /**
                 * This empty touch listener is to prevent the scrolling behaviour of this
                 * TabLayout's parent HorizontalScrollView.
                 */
                return true;
            }
        });
        tabLayout.setupWithViewPager(viewPager);

        //Set up number input
        editTextPrimalityInput = rootView.findViewById(R.id.editText_primality_input);
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
        buttonCheckPrimality = rootView.findViewById(R.id.button_check_primality);
        buttonCheckPrimality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (isNumberValid()) {

                    //Create a new task
                    final Task task = new CheckPrimalityTask(getPrimalityInput());
                    /*task.addTaskListener(new TaskListener() {
                        @Override
                        public void onTaskStarted() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    //Update bottom sheet
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                }
                            });
                        }

                        @Override
                        public void onTaskPaused() {

                        }

                        @Override
                        public void onTaskResumed() {

                        }

                        @Override
                        public void onTaskStopped() {

                        }

                        @Override
                        public void onProgressChanged(float v) {

                        }
                    });*/
                    taskListFragment.addTask(task);
                    PrimeNumberFinder.getTaskManager().registerTask(task);

                    //Start the task
                    task.startOnNewThread();
                    taskListFragment.setSelected(task);

                    //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Set up range start input
        editTextSearchRangeStart = rootView.findViewById(R.id.search_range_start);
        editTextSearchRangeStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //Check if the number is valid
                if (isRangeValid()) {
                    editTextSearchRangeStart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
                    editTextSearchRangeEnd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getStartValue());
                    if (!editable.toString().equals(formattedText)) {
                        editTextSearchRangeStart.setText(formattedText);
                    }
                    editTextSearchRangeStart.setSelection(formattedText.length());

                } else {
                    editTextSearchRangeStart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
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
        editTextSearchRangeEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //Check if the number is valid
                if (isRangeValid()) {
                    editTextSearchRangeStart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
                    editTextSearchRangeEnd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));

                    if (getEndValue() == FindPrimesTask.END_VALUE_INFINITY) {
                        if (!editable.toString().equals(getString(R.string.infinity_text))) {
                            editTextSearchRangeEnd.setText(getString(R.string.infinity_text));
                        }
                        editTextSearchRangeEnd.setSelection(editTextSearchRangeEnd.getText().length());
                    } else {
                        final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getEndValue());
                        if (!editable.toString().equals(formattedText)) {
                            editTextSearchRangeEnd.setText(formattedText);
                        }
                        editTextSearchRangeEnd.setSelection(formattedText.length());
                    }
                } else {
                    editTextSearchRangeEnd.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
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
        infinityButton = rootView.findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearchRangeEnd.setText(getString(R.string.infinity_text));
            }
        });

        //Set up find primes button
        buttonFindPrimes = rootView.findViewById(R.id.button_find_primes);
        buttonFindPrimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the range is valid
                if (isRangeValid()) {

                    //Create a new task
                    searchOptions.setStartValue(getStartValue());
                    searchOptions.setEndValue(getEndValue());
                    //searchOptions.setThreadCount(2);
                    final Task task = new FindPrimesTask(searchOptions);
                    taskListFragment.addTask(task);
                    PrimeNumberFinder.getTaskManager().registerTask(task);

                    //Start the task
                    task.startOnNewThread();
                    taskListFragment.setSelected(task);

                    //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_range), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Set up screen-dim view
        /*final View screenDim = rootView.findViewById(R.id.screenDim);
        screenDim.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (screenDim.getAlpha() > 0) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //editTextPrimalityInput.clearFocus();
                        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        MainActivity.hideKeyboard(getActivity());
                    }
                    return true;
                }
                return false;
            }
        });*/

        //Set up bottom sheet
        /*final View bottomSheet = rootView.findViewById(R.id.bottom_sheet);
        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight((int) getResources().getDimension(R.dimen.bottom_sheet_peek_height));
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                screenDim.setAlpha(Utils.map(slideOffset, 0, 1, 0, 0.7f));
            }
        });*/

        //Set up search method
        /*radioGroupSearchMethod = rootView.findViewById(R.id.radio_group_search_method);
        radioGroupSearchMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {

                    case R.id.brute_force:
                        searchOptions.setSearchMethod(FindPrimesTask.SearchOptions.Method.BRUTE_FORCE);
                        editTextSearchRangeStart.setEnabled(true);
                        infinityButton.setEnabled(true);
                        infinityButton.setAlpha(1f);
                        break;

                    case R.id.sieve_of_eratosthenes:
                        searchOptions.setSearchMethod(FindPrimesTask.SearchOptions.Method.SIEVE_OF_ERATOSTHENES);
                        editTextSearchRangeStart.setText("0");
                        editTextSearchRangeStart.setEnabled(false);
                        if (getEndValue() < 0) {
                            editTextSearchRangeEnd.setText(NumberFormat.getInstance().format(1000000));
                        }
                        infinityButton.setEnabled(false);
                        infinityButton.setAlpha(0.3f);
                        break;

                }

                editTextSearchRangeStart.setText(editTextSearchRangeStart.getText());
                editTextSearchRangeEnd.setText(editTextSearchRangeEnd.getText());
            }
        });*/

        //Apply defaults
        applyDefaults();

        return rootView;
    }

    //Utility methods

    private void applyDefaults() {

        //Reset search options
        searchOptions.setStartValue(0);
        searchOptions.setEndValue(1000000);
        searchOptions.setSearchMethod(FindPrimesTask.SearchOptions.Method.BRUTE_FORCE);
        searchOptions.setMonitorType(FindPrimesTask.SearchOptions.MonitorType.SIMPLE);

        //Reset view states
        buttonCheckPrimality.setEnabled(true);
        buttonFindPrimes.setEnabled(true);
        editTextPrimalityInput.setText("");
        editTextPrimalityInput.setEnabled(true);
        editTextSearchRangeStart.setText(NumberFormat.getInstance().format(searchOptions.getStartValue()));
        editTextSearchRangeEnd.setEnabled(true);

        switch (searchOptions.getSearchMethod()) {

            case BRUTE_FORCE:
                //radioGroupSearchMethod.check(R.id.brute_force);
                editTextSearchRangeEnd.setText(getText(R.string.infinity_text));
                editTextSearchRangeStart.setEnabled(true);
                break;

            /*case SIEVE_OF_ERATOSTHENES:
                radioGroupSearchMethod.check(R.id.sieve_of_eratosthenes);
                editTextSearchRangeEnd.setText(NumberFormat.getInstance().format(searchOptions.getEndValue()));
                break;*/
        }
    }

    //Get inputs

    private long getPrimalityInput() {
        final String input = editTextPrimalityInput.getText().toString().trim();
        if (input.length() > 0) {
            final BigInteger number = new BigInteger(editTextPrimalityInput.getText().toString().replace(",", ""));
            return number.longValue();
        } else {
            return 0;
        }
    }

    private long getStartValue() {
        final String input = editTextSearchRangeStart.getText().toString().trim();
        if (input.length() > 0) {
            final BigInteger number = new BigInteger(editTextSearchRangeStart.getText().toString().replace(",", ""));
            return number.longValue();
        } else {
            return 0;
        }
    }

    private long getEndValue() {
        final String input = editTextSearchRangeEnd.getText().toString().trim();

        if (input.length() > 0) {
            if (input.equals("infinity")) {
                return FindPrimesTask.END_VALUE_INFINITY;
            }

            final BigInteger endValue = new BigInteger(editTextSearchRangeEnd.getText().toString().replace(",", ""));
            return endValue.longValue();
        } else {
            return 0;
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
                endValue = BigInteger.valueOf(FindPrimesTask.END_VALUE_INFINITY);
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
                    if (endValue.compareTo(BigInteger.valueOf(FindPrimesTask.END_VALUE_INFINITY)) == 0) {
                        return false;
                    }

                    break;

                case BRUTE_FORCE:
                    break;
            }

            if (endValue.compareTo(BigInteger.valueOf(FindPrimesTask.END_VALUE_INFINITY)) != 0) {

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
}
