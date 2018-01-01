package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

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
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FindFactorsTaskListAdapter;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskListAdapter = new FindFactorsTaskListAdapter(getActivity());
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

        rootView.post(new Runnable() {
            @Override
            public void run() {
                for (Task task : PrimeNumberFinder.getTaskManager().getTasks()){
                    if (task instanceof FindFactorsTask){
                        getAdapter().addTask(task);
                    }
                }
                if (getAdapter().getItemCount() > 0){
                    getAdapter().setSelected(0);
                }

                update();
            }
        });

        return rootView;
    }

    public FindFactorsTaskListAdapter getAdapter(){
        return taskListAdapter;
    }

    public void update(){
        textViewNoTasks.setVisibility(taskListAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void scrollToBottom(){
        recyclerView.scrollToPosition(taskListAdapter.getItemCount() - 1);
    }
}
