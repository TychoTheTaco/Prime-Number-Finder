package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.app.Fragment;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
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
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.adapters.PrimeFactorizationTaskListAdapter;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 *         Date Created: 3/2/2017
 */

public class PrimeFactorizationFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeFactorizationFragment";

    /**
     * All UI updates are posted to this {@link Handler} on the main thread.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    private EditText editTextInput;

    private Button buttonFactorize;

    private final PrimeFactorizationTaskListFragment taskListFragment = new PrimeFactorizationTaskListFragment();
    private final PrimeFactorizationResultsFragment resultsFragment = new PrimeFactorizationResultsFragment();
    private final PrimeFactorizationStatisticsFragment statisticsFragment = new PrimeFactorizationStatisticsFragment();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        final View rootView = inflater.inflate(R.layout.prime_factorization_fragment, container, false);

        //Set up tab layout for results and statistics
        final FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager());
        final ViewPager viewPager = rootView.findViewById(R.id.view_pager);
        fragmentAdapter.add("Tasks", taskListFragment);
        rootView.post(new Runnable() {
            @Override
            public void run() {
                taskListFragment.getAdapter().addEventListener(new PrimeFactorizationTaskListAdapter.EventListener() {
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
        editTextInput = (EditText) rootView.findViewById(R.id.editText_input_number);
        editTextInput.addTextChangedListener(new TextWatcher() {
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
                    editTextInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));

                    final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getNumberToFactor());
                    if (!editable.toString().equals(formattedText)) {
                        editTextInput.setText(formattedText);
                    }
                    editTextInput.setSelection(formattedText.length());

                } else {
                    editTextInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.red)));
                }
            }
        });
        editTextInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextInput.getText().clear();
                return false;
            }

        });

        //Set up start button
        buttonFactorize = rootView.findViewById(R.id.button_generate_factor_tree);
        buttonFactorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (isNumberValid()) {

                    //Create a new task
                    final Task task = new PrimeFactorizationTask(getNumberToFactor());
                    fragmentAdapter.notifyDataSetChanged();
                    taskListFragment.getAdapter().addTask(task);
                    taskListFragment.update();
                    PrimeNumberFinder.getTaskManager().registerTask(task);

                    //Start the task
                    task.startOnNewThread();
                    taskListFragment.getAdapter().setSelected(task);

                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), "Invalid number", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return rootView;
    }

    private boolean isNumberValid() {

        try {

            final long number = getNumberToFactor();

            //The number must be greater than 0
            if (number <= 0) {
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private long getNumberToFactor() {
        final BigInteger number = new BigInteger(editTextInput.getText().toString().replace(",", ""));

        if (number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
            return -1;
        }

        return number.longValue();
    }

    private void applyDefaults() {
        editTextInput.setEnabled(true);
        editTextInput.setText("");
        buttonFactorize.setEnabled(true);
    }
}
