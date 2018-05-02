package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.tycho.app.primenumberfinder.IntentReceiver;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import easytasks.Task;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesTaskListFragment extends Fragment implements IntentReceiver {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesTaskListFgmnt";

    private FindPrimesTaskListAdapter taskListAdapter;

    private TextView textViewNoTasks;

    private RecyclerView recyclerView;

    private final Queue<AbstractTaskListAdapter.EventListener> eventListenerQueue = new LinkedBlockingQueue<>(5);
    private final Queue<ActionViewListener> actionViewListenerQueue = new LinkedBlockingQueue<>(5);

    private Intent intent;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        taskListAdapter = new FindPrimesTaskListAdapter(context);
        Crashlytics.log(Log.DEBUG, TAG, "onAttach()");
        Crashlytics.log(Log.DEBUG, TAG, "Adding " + eventListenerQueue.size() + " event listeners.");
        while (!eventListenerQueue.isEmpty()) {
            taskListAdapter.addEventListener(eventListenerQueue.poll());
        }
        Crashlytics.log(Log.DEBUG, TAG, "Adding " + actionViewListenerQueue.size() + " action view listeners.");
        while (!actionViewListenerQueue.isEmpty()) {
            taskListAdapter.addActionViewListener(actionViewListenerQueue.poll());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_task_list_fragment, container, false);

        Crashlytics.log(Log.DEBUG, TAG, "onCreateView: " + taskListAdapter);

        //Set up the task list
        recyclerView = rootView.findViewById(R.id.task_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //taskListAdapter = new FindPrimesTaskListAdapter(getActivity());
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
        if (intent == null || intent.getSerializableExtra("taskId") == null || taskListAdapter.getItemCount() == 0) {
            taskListAdapter.setSelected(0);
        } else {
            taskListAdapter.setSelected(PrimeNumberFinder.getTaskManager().findTaskById((UUID) intent.getSerializableExtra("taskId")));
        }

        update();

        //Reset intent
        this.intent = null;

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Crashlytics.log(Log.DEBUG, TAG, "onDestroyView()");
        if (taskListAdapter != null){
            Crashlytics.setString("taskListAdapter", taskListAdapter.toString());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Crashlytics.log(Log.DEBUG, TAG, "onDetach()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Crashlytics.log(Log.DEBUG, TAG, "onDestroy()");
    }

    @Override
    public void giveIntent(final Intent intent) {
        this.intent = intent;
    }

    public void addTask(final Task task) {
        Crashlytics.log(Log.DEBUG, TAG, "isAdded: " + isAdded());
        Crashlytics.log(Log.DEBUG, TAG, "isDetached: " + isDetached());
        Crashlytics.log(Log.DEBUG, TAG, "getView: " + getView());
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
            eventListenerQueue.add(eventListener);
        } else {
            taskListAdapter.addEventListener(eventListener);
        }
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        if (taskListAdapter == null) {
            actionViewListenerQueue.add(actionViewListener);
        } else {
            taskListAdapter.addActionViewListener(actionViewListener);
        }
    }
}
