package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FabAnimator;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SimpleFragmentAdapter;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.TaskListFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.GeneralResultsFragment;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.adapters.PrimeFactorizationTaskListAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import easytasks.Task;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 3/2/2017
 */

public class PrimeFactorizationFragment extends ModuleHostFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationFragment.class.getSimpleName();

    private ValidEditText editTextInput;

    private final PrimeFactorizationTask.SearchOptions searchOptions = new PrimeFactorizationTask.SearchOptions(0);

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_fragment, container, false);

        //Set up factor input
        editTextInput = rootView.findViewById(R.id.editText_input_number);
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
                editTextInput.setValid(Validator.isValidFactorInput(getNumberToFactor()));
            }
        });

        //Set up start button
        rootView.findViewById(R.id.button_generate_factor_tree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the number is valid
                if (Validator.isValidFactorInput(getNumberToFactor())) {

                    //Create a new task
                    searchOptions.setNumber(getNumberToFactor().longValue());
                    try {
                        startTask((PrimeFactorizationTask.SearchOptions) searchOptions.clone());
                    } catch (CloneNotSupportedException e) {
                    }

                    hideKeyboard(getActivity());
                    taskListFragment.scrollToBottom();

                } else {
                    Toast.makeText(getActivity(), "Invalid number", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return rootView;
    }

    @Override
    protected void loadFragments() {
        taskListFragment = (TaskListFragment) addFragment("Tasks", TaskListFragment.class);
        resultsFragment = (PrimeFactorizationResultsFragment) addFragment("Results", PrimeFactorizationResultsFragment.class);
    }

    @Override
    protected void afterLoadFragments() {
        //Set up Task list fragment
        taskListFragment.setAdapter(new PrimeFactorizationTaskListAdapter(getContext()));
        taskListFragment.whitelist(PrimeFactorizationTask.class);
    }

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextInput.getText().toString());
    }

    private static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final PrimeFactorizationTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final PrimeFactorizationTask task = (PrimeFactorizationTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(searchOptions);
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
    }

    @Override
    public void onEditPressed(Task task) {
        final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
        intent.putExtra("searchOptions", ((PrimeFactorizationTask) task).getSearchOptions());
        intent.putExtra("taskId", task.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    public void onSavePressed(Task task) {
        ((PrimeFactorizationResultsFragment) resultsFragment).saveTask((PrimeFactorizationTask) task, getActivity());
    }

    private void startTask(final PrimeFactorizationTask.SearchOptions searchOptions) {
        final PrimeFactorizationTask task = new PrimeFactorizationTask(searchOptions);
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped() {

                //Auto-save
                if (task.getSearchOptions().isAutoSave()) {
                    new Thread(() -> {
                        final boolean success = FileManager.getInstance().saveTree(task.getFactorTree());
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show());
                    }).start();
                }

                //Notify when finished
                if (task.getSearchOptions().isNotifyWhenFinished()) {
                    com.tycho.app.primenumberfinder.utils.NotificationManager.displayNotification(getActivity(), "default", task, com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_PRIME_FACTORIZATION, "Task \"Prime factorization of " + NUMBER_FORMAT.format(task.getNumber()) + "\" finished.");
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
}
