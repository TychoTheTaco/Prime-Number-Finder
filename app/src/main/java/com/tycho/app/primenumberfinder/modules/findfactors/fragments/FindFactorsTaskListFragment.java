package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FindFactorsTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class FindFactorsTaskListFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsTskListFgmnt";

    private FindFactorsTaskListAdapter taskListAdapter;

    private TextView textViewNoTasks;

    private RecyclerView recyclerView;

    private Queue<AbstractTaskListAdapter.EventListener> eventListenerQueue = new LinkedBlockingQueue<>(5);
    private Queue<ActionViewListener> actionViewListenerQueue = new LinkedBlockingQueue<>(5);

    /**
     * This deprecated version of {@linkplain Fragment#onAttach(Activity)} is needed in API < 22.
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        taskListAdapter = new FindFactorsTaskListAdapter(activity);
        while (!eventListenerQueue.isEmpty()) {
            taskListAdapter.addEventListener(eventListenerQueue.poll());
        }
        while (!actionViewListenerQueue.isEmpty()) {
            taskListAdapter.addActionViewListener(actionViewListenerQueue.poll());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_task_list_fragment, container, false);

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
            if (task instanceof FindFactorsTask) {
                taskListAdapter.addTask(task);
            }
        }
        if (taskListAdapter.getItemCount() > 0) {
            taskListAdapter.setSelected(0);
        }

        update();

        return rootView;
    }

    public void addEventListener(final AbstractTaskListAdapter.EventListener eventListener) {
        if (taskListAdapter == null) {
            eventListenerQueue.add(eventListener);
        } else {
            taskListAdapter.addEventListener(eventListener);
        }
    }

    public void addTask(final Task task) {
        taskListAdapter.addTask(task);
        update();
    }

    public void setSelected(final int index) {
        taskListAdapter.setSelected(index);
    }

    public void setSelected(final Task task) {
        taskListAdapter.setSelected(task);
    }

    public void update() {
        textViewNoTasks.setVisibility(taskListAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void scrollToBottom() {
        recyclerView.scrollToPosition(taskListAdapter.getItemCount() - 1);
    }

    public void addActionViewListener(final ActionViewListener actionViewListener){
        if (taskListAdapter == null) {
            actionViewListenerQueue.add(actionViewListener);
        } else {
            taskListAdapter.addActionViewListener(actionViewListener);
        }
    }
}
