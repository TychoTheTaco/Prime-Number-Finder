package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.tycho.app.primenumberfinder.ValidEditText;
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

public class PrimeFactorizationFragment extends Fragment implements FloatingActionButtonListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationFragment.class.getSimpleName();

    private ValidEditText editTextInput;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private PrimeFactorizationTaskListFragment taskListFragment;
    private PrimeFactorizationResultsFragment resultsFragment;

    private final PrimeFactorizationTask.SearchOptions searchOptions = new PrimeFactorizationTask.SearchOptions(0);

    private ViewPager viewPager;
    private FabAnimator fabAnimator;

    private FloatingActionButtonHost floatingActionButtonHost;

    private ActionViewListener actionViewListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FloatingActionButtonHost) {
            floatingActionButtonHost = (FloatingActionButtonHost) context;
        }

        if (context instanceof ActionViewListener){
            actionViewListener = (ActionViewListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_fragment, container, false);

        //Set fragment adapter
        final SimpleFragmentAdapter simpleFragmentAdapter = new SimpleFragmentAdapter(getChildFragmentManager(), getContext());
        viewPager = rootView.findViewById(R.id.view_pager);

        //Add fragments to adapter
        simpleFragmentAdapter.add(PrimeFactorizationTaskListFragment.class.getName(), "Tasks");
        simpleFragmentAdapter.add(PrimeFactorizationResultsFragment.class.getName(), "Results");

        //Instantiate fragments now to save a reference to them
        simpleFragmentAdapter.startUpdate(viewPager);
        taskListFragment = (PrimeFactorizationTaskListFragment) simpleFragmentAdapter.instantiateItem(viewPager, 0);
        resultsFragment = (PrimeFactorizationResultsFragment) simpleFragmentAdapter.instantiateItem(viewPager, 1);
        simpleFragmentAdapter.finishUpdate(viewPager);

        //Set up Task list fragment
        taskListFragment.addActionViewListener(actionViewListener);
        taskListFragment.addEventListener(new PrimeFactorizationTaskListAdapter.EventListener() {
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
                final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
                intent.putExtra("searchOptions", ((PrimeFactorizationTask) task).getSearchOptions());
                intent.putExtra("taskId", task.getId());
                startActivityForResult(intent, 0);
            }
        });

        //Set up view pager
        viewPager.setAdapter(simpleFragmentAdapter);
        fabAnimator = new FabAnimator(floatingActionButtonHost.getFab(0));
        viewPager.addOnPageChangeListener(fabAnimator);
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

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
                    }catch (CloneNotSupportedException e){}

                    hideKeyboard(getActivity());
                    taskListFragment.scrollToBottom();

                } else {
                    Toast.makeText(getActivity(), "Invalid number", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Give the root view focus to prevent EditTexts from initially getting focus
        rootView.requestFocus();

        //Scroll to Results fragment if started from a notification
        if (getActivity().getIntent().getSerializableExtra("taskId") != null){
            viewPager.setCurrentItem(1);
        }

        return rootView;
    }

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextInput.getText().toString());
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        taskListFragment.addActionViewListener(actionViewListener);
    }

    private static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void initFab(View view) {
        if (fabAnimator != null){
            fabAnimator.onPageScrolled(viewPager.getCurrentItem(), 0, 0);
        }
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

    private void startTask(final PrimeFactorizationTask.SearchOptions searchOptions){
        final PrimeFactorizationTask task = new PrimeFactorizationTask(searchOptions);
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped() {

                //Auto-save
                if (task.getSearchOptions().isAutoSave()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean success = FileManager.getInstance().saveTree(task.getFactorTree());
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
                    com.tycho.app.primenumberfinder.utils.NotificationManager.displayNotification(getActivity(), "default", task, com.tycho.app.primenumberfinder.utils.NotificationManager.REQUEST_CODE_PRIME_FACTORIZATION, "Task \"Prime factorization of " + NUMBER_FORMAT.format(task.getNumber()) + "\" finished.");
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
