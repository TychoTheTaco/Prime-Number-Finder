package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import easytasks.ITask;
import easytasks.TaskAdapter;

public abstract class ModuleHostFragment extends Fragment implements AbstractTaskListAdapter.EventListener {

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
        rootView.requestFocus();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View advanced = view.findViewById(R.id.advanced_search);
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

    @Override
    public void onTaskSelected(ITask task) {

    }

    @Override
    public void onPausePressed(ITask task) {

    }

    @Override
    public void onTaskRemoved(ITask task) {

    }

    @Override
    public void onSavePressed(ITask task){

    }

    protected void startTask(final ITask task){
        final ITask previousTask = this.resultsFragment.getTask();
        if (previousTask != null){
            previousTask.stop();
            PrimeNumberFinder.getTaskManager().unregisterTask(previousTask);
        }
        this.resultsFragment.setTask(task);

        task.addTaskListener(new TaskAdapter(){
            @Override
            public void onTaskStopped(ITask task) {
                final Bundle bundle = new Bundle();
                bundle.putString("task_type", task.getClass().getSimpleName());
                bundle.putLong("duration", task.getElapsedTime());
                FirebaseAnalytics.getInstance(requireContext()).logEvent("task_finished", bundle);
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
