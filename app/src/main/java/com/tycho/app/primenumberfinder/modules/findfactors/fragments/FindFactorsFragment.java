package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.app.Fragment;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.adapters.FragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FindFactorsTaskListAdapter;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * This {@linkplain Fragment} allows the user to input a number they want to factor. This fragment
 * will display the progress and statistics of the factorization, along with a list of factors that
 * are found.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindFactorsFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsFragment";

    /**
     * All UI updates are posted to this {@link Handler} on the main thread.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * All views
     */
    private EditText editTextNumberToFactor;
    private Button buttonFindFactors;

    private final FindFactorsTask.SearchOptions searchOptions = new FindFactorsTask.SearchOptions(0, FindFactorsTask.SearchOptions.MonitorType.SIMPLE);

    private final FindFactorsTaskListFragment taskListFragment = new FindFactorsTaskListFragment();
    private final FindFactorsResultsFragment resultsFragment = new FindFactorsResultsFragment();
    private final FindFactorsStatisticsFragment statisticsFragment = new FindFactorsStatisticsFragment();

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_fragment, viewGroup, false);

        //Set up tab layout for results and statistics
        final FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager());
        final ViewPager viewPager = rootView.findViewById(R.id.view_pager);
        fragmentAdapter.add("Tasks", taskListFragment);
        rootView.post(new Runnable() {
            @Override
            public void run() {
                taskListFragment.getAdapter().addEventListener(new FindFactorsTaskListAdapter.EventListener() {
                    @Override
                    public void onTaskSelected(Task task) {
                        resultsFragment.setTask(task);
                        statisticsFragment.setTask(task);
                    }

                    @Override
                    public void onPausePressed(Task task) {

                    }

                    @Override
                    public void onTaskRemoved(Task task) {
                        if (resultsFragment.getTask() == task){
                            resultsFragment.setTask(null);
                            statisticsFragment.setTask(null);
                        }

                        taskListFragment.update();
                    }
                });
            }
        });

        fragmentAdapter.add("Results", resultsFragment);
        fragmentAdapter.add("Statistics", statisticsFragment);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2);
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //TODO: We might not want to do this because the last fragment doesnt get even spacing. Either center it or allow scrolling.
                /**
                 * This empty touch listener is to prevent the scrolling behaviour of this
                 * TabLayout's parent HorizontalScrollView.
                 */
                return true;
            }
        });
        tabLayout.setupWithViewPager(viewPager);

        //Set up factor input
        editTextNumberToFactor = rootView.findViewById(R.id.editText_input_number);
        editTextNumberToFactor.addTextChangedListener(new TextWatcher() {
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
                    editTextNumberToFactor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getNumberToFactor());
                    if (!editable.toString().equals(formattedText)) {
                        editTextNumberToFactor.setText(formattedText);
                    }
                    editTextNumberToFactor.setSelection(formattedText.length());

                } else {
                    editTextNumberToFactor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
            }
        });
        editTextNumberToFactor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextNumberToFactor.getText().clear();
                return false;
            }
        });

        //Set up start button
        buttonFindFactors = rootView.findViewById(R.id.button_find_factors);
        buttonFindFactors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (isNumberValid()) {

                    //Create a new task
                    searchOptions.setNumber(getNumberToFactor());
                    final Task task = new FindFactorsTask(searchOptions);
                    taskListFragment.getAdapter().addTask(task);
                    taskListFragment.update();
                    PrimeNumberFinder.getTaskManager().registerTask(task);

                    //Start the task
                    task.startOnNewThread();
                    taskListFragment.getAdapter().setSelected(task);

                    hideKeyboard(getActivity());
                    taskListFragment.scrollToBottom();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    public void restoreTask(final Task task){
        taskListFragment.getAdapter().addTask(task);
        taskListFragment.update();
    }

    //Get inputs

    private long getNumberToFactor() {
        final BigInteger number = new BigInteger(editTextNumberToFactor.getText().toString().replace(",", ""));
        return number.longValue();
    }

    //Validation

    private boolean isNumberValid() {

        try {

            final BigInteger number = new BigInteger(editTextNumberToFactor.getText().toString().replace(",", ""));

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
}
