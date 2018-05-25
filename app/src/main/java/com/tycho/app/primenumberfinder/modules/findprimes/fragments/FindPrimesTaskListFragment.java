package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import easytasks.Task;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesTaskListFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesTaskListFragment.class.getSimpleName();

    private FindPrimesTaskListAdapter taskListAdapter;

    private TextView textViewNoTasks;

    private RecyclerView recyclerView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "onAttach(): " + context);
        taskListAdapter = new FindPrimesTaskListAdapter(context);

        if (context instanceof ActionViewListener) {
            taskListAdapter.addActionViewListener((ActionViewListener) context);
        }

        if (getParentFragment() instanceof AbstractTaskListAdapter.EventListener) {
            taskListAdapter.addEventListener((AbstractTaskListAdapter.EventListener) getParentFragment());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.task_list_fragment, container, false);

        //Set up the task list
        recyclerView = rootView.findViewById(R.id.task_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(taskListAdapter);
        recyclerView.setItemAnimator(null);

        //Set up "no tasks" message
        textViewNoTasks = rootView.findViewById(R.id.empty_message);

        //Restore tasks if fragment was destroyed
        for (Task task : PrimeNumberFinder.getTaskManager().getTasks()) {
            if (task instanceof FindPrimesTask || task instanceof CheckPrimalityTask) {
                addTask(task);
            }
        }
        taskListAdapter.sortByTimeCreated();

        //Select correct task
        if (savedInstanceState != null) {
            taskListAdapter.setSelected(savedInstanceState.getInt("selectedItemPosition"));

            //Restore saved state
            final ArrayList<Integer> savedItemPositions = savedInstanceState.getIntegerArrayList("savedItemPositions");
            Crashlytics.log("Tasks: " + PrimeNumberFinder.getTaskManager().getTasks());
            Crashlytics.log("Saved positions: " + savedItemPositions);
            if (savedItemPositions != null) {
                for (int i : savedItemPositions) {
                    taskListAdapter.setSaved(i, true);
                }
            }
        } else {
            taskListAdapter.setSelected(PrimeNumberFinder.getTaskManager().findTaskById((UUID) getActivity().getIntent().getSerializableExtra("taskId")));
        }

        update();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedItemPosition", taskListAdapter.getSelectedItemPosition());

        //Store the saved item positions
        final List<Task> savedItems = taskListAdapter.getSavedItems();
        final ArrayList<Integer> savedItemPositions = new ArrayList<>();
        for (Task task : savedItems) {
            savedItemPositions.add(taskListAdapter.indexOf(task));
        }
        outState.putIntegerArrayList("savedItemPositions", savedItemPositions);
    }

    public void addTask(final Task task) {
        /*if (task instanceof FindPrimesTask) {
            ((FindPrimesTask) task).addSavableCallbacks(new Savable.SavableCallbacks() {
                @Override
                public void onSaved() {
                    taskListAdapter.postSetSaved(task, true);
                }

                @Override
                public void onError() {

                }
            });
        }*/
        taskListAdapter.addTask(task);
        recyclerView.scrollToPosition(taskListAdapter.getItemCount() - 1);
        update();
    }

    public void update() {
        textViewNoTasks.setVisibility(taskListAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void setSelected(final int index) {
        taskListAdapter.setSelected(index);
    }

    public void setSelected(final Task task) {
        taskListAdapter.setSelected(task);
    }
}
