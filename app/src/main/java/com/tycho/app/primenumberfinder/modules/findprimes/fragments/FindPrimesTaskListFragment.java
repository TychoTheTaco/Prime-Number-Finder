package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
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

public class FindPrimesTaskListFragment extends Fragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesTaskListFragment.class.getSimpleName();

    private FindPrimesTaskListAdapter taskListAdapter;

    private TextView textViewNoTasks;

    private RecyclerView recyclerView;

    private final List<AbstractTaskListAdapter.EventListener> eventListeners = new ArrayList<>();
    private final List<ActionViewListener> actionViewListeners = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        taskListAdapter = new FindPrimesTaskListAdapter(context);
        for (AbstractTaskListAdapter.EventListener eventListener : eventListeners){
            taskListAdapter.addEventListener(eventListener);
        }
        for (ActionViewListener actionViewListener : actionViewListeners){
            taskListAdapter.addActionViewListener(actionViewListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_task_list_fragment, container, false);

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
                taskListAdapter.addTask(task);
            }
        }
        taskListAdapter.sortByTimeCreated();

        //Select correct task
        if (savedInstanceState != null){
            taskListAdapter.setSelected(savedInstanceState.getInt("selectedItemPosition"));
        }else{
            taskListAdapter.setSelected(PrimeNumberFinder.getTaskManager().findTaskById((UUID) getActivity().getIntent().getSerializableExtra("taskId")));
        }

        update();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedItemPosition", taskListAdapter.getSelectedItemPosition());
    }

    public void addTask(final Task task) {
        if (task instanceof FindPrimesTask){
            ((FindPrimesTask) task).addSavableCallbacks(new Savable.SavableCallbacks() {
                @Override
                public void onSaved() {
                    taskListAdapter.setSaved(task);
                }

                @Override
                public void onError() {

                }
            });
        }
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

    public void addEventListener(final AbstractTaskListAdapter.EventListener eventListener) {
        if (taskListAdapter == null) {
            eventListeners.add(eventListener);
        } else {
            taskListAdapter.addEventListener(eventListener);
        }
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        if (taskListAdapter == null) {
            actionViewListeners.add(actionViewListener);
        } else {
            taskListAdapter.addActionViewListener(actionViewListener);
        }
    }
}
