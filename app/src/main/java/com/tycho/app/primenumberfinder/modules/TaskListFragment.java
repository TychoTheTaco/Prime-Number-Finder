package com.tycho.app.primenumberfinder.modules;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.VerticalItemDecoration;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import easytasks.ITask;

public class TaskListFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = TaskListFragment.class.getSimpleName();

    /**
     * Adapter used to display task list.
     */
    private AbstractTaskListAdapter taskListAdapter;

    /**
     * Displayed when the task list is empty.
     */
    private TextView textViewNoTasks;

    private RecyclerView recyclerView;

    private final List<Class<? extends ITask>> whitelist = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.task_list_fragment, container, false);

        //Set up the task list
        recyclerView = rootView.findViewById(R.id.task_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new VerticalItemDecoration(Utils.dpToPx(getContext(), 8)));
        recyclerView.setAdapter(taskListAdapter);
        recyclerView.setItemAnimator(null);

        //Set up "no tasks" message
        textViewNoTasks = rootView.findViewById(R.id.empty_message);

        //Restore tasks if fragment was destroyed
        for (ITask task : PrimeNumberFinder.getTaskManager().getTasks()) {
            if (whitelist.contains(task.getClass())){
                addTask(task);
            }
        }
        taskListAdapter.sortByTimeCreated();

        //Restore saved instance
        if (savedInstanceState != null) {
            taskListAdapter.setSelected(savedInstanceState.getInt("selectedItemPosition"));
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
        final List<ITask> savedItems = taskListAdapter.getSavedItems();
        final ArrayList<Integer> savedItemPositions = new ArrayList<>();
        for (ITask task : savedItems) {
            savedItemPositions.add(taskListAdapter.indexOf(task));
        }
        outState.putIntegerArrayList("savedItemPositions", savedItemPositions);
    }

    public void setAdapter(final AbstractTaskListAdapter adapter){
        this.taskListAdapter = adapter;
    }

    public void addEventListener(final AbstractTaskListAdapter.EventListener eventListener) {
        taskListAdapter.addEventListener(eventListener);
    }

    public void addTask(final ITask task) {
        if (task instanceof Savable){
            ((Savable) task).addSaveListener(new Savable.SaveListener() {
                @Override
                public void onSaved() {
                    taskListAdapter.postSetSaved(task);
                }

                @Override
                public void onError() {
                    Log.e(TAG, "Failed to save task!");
                }
            });
        }
        taskListAdapter.addTask(task);
        update();
    }

    public void setSelected(final ITask task) {
        taskListAdapter.setSelected(task);
    }

    public void update() {
        textViewNoTasks.setVisibility(taskListAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void scrollToBottom() {
        recyclerView.scrollToPosition(taskListAdapter.getItemCount() - 1);
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        taskListAdapter.addActionViewListener(actionViewListener);
    }

    public void whitelist(final Class<? extends ITask>... classes){
        whitelist.addAll(Arrays.asList(classes));
    }
}
