package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.concurrent.atomic.AtomicInteger;

import easytasks.ITask;
import easytasks.TaskAdapter;

public abstract class ModuleHostFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ModuleHostFragment.class.getSimpleName();

    protected ActionViewListener actionViewListener;

    protected static final int REQUEST_CODE_NEW_TASK = 0;

    private ResultsFragment resultsFragment;

    private Class cls;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ActionViewListener){
            actionViewListener = (ActionViewListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.module_host_fragment, container, false);

        //Give the root view focus to prevent EditTexts from initially getting focus
        //rootView.requestFocus();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView advanced = view.findViewById(R.id.advanced_search);
        if (advanced != null){
            advanced.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(getActivity(), cls);
                    startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
                }
            });
        }
    }

    protected void setConfigurationClass(final Class cls){
        this.cls = cls;
    }

    protected void setConfigurationContainer(final Fragment fragment){
        getChildFragmentManager().beginTransaction().replace(R.id.configuration_container, fragment).commit();
    }

    protected void inflate(final int id){
        getLayoutInflater().inflate(id, requireView().findViewById(R.id.configuration_container));
    }

    protected void setResultsFragment(final ResultsFragment fragment){
        this.resultsFragment = fragment;
        getChildFragmentManager().beginTransaction().replace(R.id.results_container, fragment).commit();
    }

    private int getTaskType(ITask task){
        if (task instanceof FindPrimesTask || task instanceof CheckPrimalityTask){
            return 0;
        }else if (task instanceof FindFactorsTask){
            return 1;
        }else if (task instanceof PrimeFactorizationTask){
            return 2;
        }else if (task instanceof LeastCommonMultipleTask){
            return 3;
        }else if (task instanceof GreatestCommonFactorTask){
            return 4;
        }
        return -1;
    }

    private AtomicInteger taskCount = new AtomicInteger(0);

    protected void startTask(final ITask task){
        final ITask previousTask = this.resultsFragment.getTask();
        if (previousTask != null){
            previousTask.stop();
            PrimeNumberFinder.getTaskManager().unregisterTask(previousTask);
        }
        this.resultsFragment.setTask(task);

        task.addTaskListener(new TaskAdapter(){
            @Override
            public void onTaskStarted(ITask task) {
                actionViewListener.onTaskStatesChanged(getTaskType(task), true);
                taskCount.getAndIncrement();
            }

            @Override
            public void onTaskPaused(ITask task) {
                actionViewListener.onTaskStatesChanged(getTaskType(task), false);
            }

            @Override
            public void onTaskResumed(ITask task) {
                actionViewListener.onTaskStatesChanged(getTaskType(task), true);
            }

            @Override
            public void onTaskStopped(ITask task) {
                final Bundle bundle = new Bundle();
                bundle.putString("task_type", task.getClass().getSimpleName());
                bundle.putLong("duration", task.getElapsedTime());
                FirebaseAnalytics.getInstance(requireContext()).logEvent("task_finished", bundle);
                actionViewListener.onTaskStatesChanged(getTaskType(task), false);
                Log.e(TAG, "ACTIVE TASKS: " + taskCount.decrementAndGet());
            }
        });

        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();
        Utils.logTaskStarted(getContext(), task);
    }

    protected int getTheme(){
        return PreferenceManager.getInt(PreferenceManager.Preference.THEME);
    }
}
