package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;

import easytasks.Task;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesTaskListFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesTaskListFgmnt";

    private FindPrimesTaskListAdapter taskListAdapter;

    private TextView textViewNoTasks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        taskListAdapter = new FindPrimesTaskListAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_task_list_fragment, container, false);

        //Set up the task list
        final RecyclerView recyclerView = rootView.findViewById(R.id.task_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(taskListAdapter);
        recyclerView.setItemAnimator(null);

        //Set up "no tasks" message
        textViewNoTasks = rootView.findViewById(R.id.empty_message);

        //Restore tasks if fragment was destroyed
        rootView.post(new Runnable() {
            @Override
            public void run() {
                for (Task task : PrimeNumberFinder.getTaskManager().getTasks()){
                    if (task instanceof FindPrimesTask || task instanceof CheckPrimalityTask){
                        taskListAdapter.addTask(task);
                    }
                }
                if (taskListAdapter.getItemCount() > 0){
                    taskListAdapter.setSelected(0);
                }

                update();
            }
        });

        return rootView;
    }

    public void addTask(final Task task){
        taskListAdapter.addTask(task);
        update();
    }

    public void setSelected(final int index){
        taskListAdapter.setSelected(index);
    }

    public void setSelected(final Task task){
        taskListAdapter.setSelected(task);
    }

    public void addEventListener(final AbstractTaskListAdapter.EventListener eventListener){
        taskListAdapter.addEventListener(eventListener);
    }

    public void update(){
        textViewNoTasks.setVisibility(taskListAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }
}
