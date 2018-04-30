package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.IntentReceiver;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.adapters.PrimeFactorizationTaskListAdapter;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class PrimeFactorizationTaskListFragment extends Fragment implements IntentReceiver{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationTaskListFragment.class.getSimpleName();

    private RecyclerView recyclerView;

    private PrimeFactorizationTaskListAdapter taskListAdapter;

    private TextView textViewNoTasks;

    private final Queue<AbstractTaskListAdapter.EventListener> eventListenerQueue = new LinkedBlockingQueue<>(5);
    private final Queue<ActionViewListener> actionViewListenerQueue = new LinkedBlockingQueue<>(5);

    private Intent intent;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        taskListAdapter = new PrimeFactorizationTaskListAdapter(context);
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
        final View rootView = inflater.inflate(R.layout.prime_factorization_task_list_fragment, container, false);

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
            if (task instanceof PrimeFactorizationTask) {
                taskListAdapter.addTask(task);
            }
        }
        taskListAdapter.sortByTimeCreated();

        //Select correct task
        if (intent == null || taskListAdapter.getItemCount() > 0) {
            taskListAdapter.setSelected(0);
        }else{
            taskListAdapter.setSelected(PrimeNumberFinder.getTaskManager().findTaskById((UUID) intent.getSerializableExtra("taskId")));
        }

        update();

        //Reset intent
        this.intent = null;

        return rootView;
    }

    @Override
    public void giveIntent(Intent intent) {
        this.intent = intent;
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

    public void addEventListener(final AbstractTaskListAdapter.EventListener eventListener) {
        if (taskListAdapter == null) {
            eventListenerQueue.add(eventListener);
        } else {
            taskListAdapter.addEventListener(eventListener);
        }
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
