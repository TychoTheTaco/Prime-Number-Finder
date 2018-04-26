package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
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
import android.widget.Toast;

import com.tycho.app.primenumberfinder.FabAnimator;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.IntentReceiver;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.ValidEditText;
import com.tycho.app.primenumberfinder.activities.MainActivity;
import com.tycho.app.primenumberfinder.adapters.FragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FindFactorsTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * This {@linkplain Fragment} allows the user to input a number they want to factor. This fragment
 * will display the progress and statistics of the factorization, along with a list of factors that
 * are found.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindFactorsFragment extends Fragment implements FloatingActionButtonListener, IntentReceiver{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsFragment";

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    /**
     * All views
     */
    private ValidEditText editTextNumberToFactor;

    private final FindFactorsTask.SearchOptions searchOptions = new FindFactorsTask.SearchOptions(0);

    private final FindFactorsTaskListFragment taskListFragment = new FindFactorsTaskListFragment();
    private final FindFactorsResultsFragment resultsFragment = new FindFactorsResultsFragment();

    private ViewPager viewPager;
    private FabAnimator fabAnimator;

    private Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_fragment, viewGroup, false);

        //Set up tab layout for results and statistics
        final FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager());
        viewPager = rootView.findViewById(R.id.view_pager);
        fragmentAdapter.add("Tasks", taskListFragment);
        taskListFragment.addEventListener(new FindFactorsTaskListAdapter.EventListener() {
            @Override
            public void onTaskSelected(Task task) {
                resultsFragment.setTask(task);
            }

            @Override
            public void onPausePressed(Task task) {

            }

            @Override
            public void onTaskRemoved(Task task) {
                if (resultsFragment.getTask() == task) {
                    resultsFragment.setTask(null);
                }

                taskListFragment.update();
            }

            @Override
            public void onEditPressed(Task task) {
                final Intent intent = new Intent(getActivity(), FindFactorsConfigurationActivity.class);
                intent.putExtra("searchOptions", ((FindFactorsTask) task).getSearchOptions());
                intent.putExtra("taskId", task.getId());
                startActivityForResult(intent, 0);
            }
        });
        fragmentAdapter.add("Results", resultsFragment);
        viewPager.setAdapter(fragmentAdapter);
        fabAnimator = new FabAnimator(((FloatingActionButtonHost) getActivity()).getFab(0));
        viewPager.addOnPageChangeListener(fabAnimator);
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
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
                //Format the number
                final String formattedText = NUMBER_FORMAT.format(getNumberToFactor());
                if (!editable.toString().equals(formattedText)) {
                    editTextNumberToFactor.setText(formattedText);
                }
                editTextNumberToFactor.setSelection(formattedText.length());

                //Check if the number is valid
                editTextNumberToFactor.setValid(Validator.isValidFactorInput(getNumberToFactor()));
            }
        });

        //Set up start button
        final Button buttonFindFactors = rootView.findViewById(R.id.button_find_factors);
        buttonFindFactors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (Validator.isValidFactorInput(getNumberToFactor())) {

                    //Create a new task
                    searchOptions.setNumber(getNumberToFactor().longValue());
                    try {
                        startTask((FindFactorsTask.SearchOptions) searchOptions.clone());
                    }catch (CloneNotSupportedException e){
                        e.printStackTrace();
                    }

                    hideKeyboard(getActivity());
                    taskListFragment.scrollToBottom();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Give the root view focus to prevent EditTexts from initially getting focus
        rootView.requestFocus();

        //Scroll to Results fragment if started from a notification
        if (intent.getSerializableExtra("taskId") != null){
            viewPager.setCurrentItem(1);
        }

        //Reset intent
        this.intent = null;

        return rootView;
    }

    private static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), FindFactorsConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void initFab(View view) {
        if (fabAnimator != null){
            fabAnimator.onPageScrolled(viewPager.getCurrentItem(), 0, 0);
        }
    }

    @Override
    public void giveIntent(Intent intent) {
        this.intent = intent;
        taskListFragment.giveIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final FindFactorsTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final FindFactorsTask task = (FindFactorsTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(searchOptions);
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
    }

    private void startTask(final FindFactorsTask.SearchOptions searchOptions){
        final FindFactorsTask task = new FindFactorsTask(searchOptions);
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped() {

                //Auto-save
                if (task.getSearchOptions().isAutoSave()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean success = FileManager.getInstance().saveFactors(task.getFactors(), task.getNumber());
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }

                //Notify when finished
                if (task.getSearchOptions().isNotifyWhenFinished()) {
                    com.tycho.app.primenumberfinder.utils.NotificationManager.displayNotification(getActivity(), "default", task, com.tycho.app.primenumberfinder.utils.NotificationManager.REQUEST_CODE_FIND_FACTORS, "Task \"Factors of " + NUMBER_FORMAT.format(task.getNumber()) + "\" finished.");
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

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextNumberToFactor.getText().toString());
    }

    public void addActionViewListener(final ActionViewListener actionViewListener){
        taskListFragment.addActionViewListener(actionViewListener);
    }
}
