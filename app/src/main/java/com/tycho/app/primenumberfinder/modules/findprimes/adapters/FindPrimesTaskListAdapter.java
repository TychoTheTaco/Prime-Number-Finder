package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.TaskListAdapterCallbacks;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesFragment;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesTaskListAdapter extends AbstractTaskListAdapter<FindPrimesTaskListAdapter.ViewHolder>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesTaskListAdptr";

    private final DecimalFormat decimalFormat = new DecimalFormat("##0.00");

    private final Context context;

    private final CopyOnWriteArrayList<TaskListAdapterCallbacks> taskListAdapterCallbacks = new CopyOnWriteArrayList<>();

    public FindPrimesTaskListAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_primes_task_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void doOnBindViewHolder(RecyclerView.ViewHolder h, int position) {

        final ViewHolder holder = (ViewHolder) h;

        //Get the current task
        final Task task = tasks.get(holder.getAdapterPosition());

        //Set title
        if (task instanceof FindPrimesTask) {
            final long endValue = ((FindPrimesTask) task).getEndValue();
            holder.title.setText(context.getString(R.string.find_primes_task_list_item_title, NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) task).getStartValue()), endValue == FindPrimesTask.INFINITY ? context.getString(R.string.infinity_text) : NumberFormat.getInstance(Locale.getDefault()).format(endValue)));
        } else if (task instanceof CheckPrimalityTask) {
            holder.title.setText(context.getString(R.string.check_primality_task_status, NumberFormat.getInstance(Locale.getDefault()).format(((CheckPrimalityTask) task).getNumber())));
        }

        //Set state and buttons
        switch (task.getState()) {
            case RUNNING:
                holder.state.setText(context.getString(R.string.status_searching));
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.editButton.setEnabled(false);
                holder.deleteButton.setEnabled(false);
                break;

            case PAUSED:
                holder.state.setText(context.getString(R.string.status_paused));
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                holder.editButton.setEnabled(true);
                holder.deleteButton.setEnabled(true);
                break;

            case STOPPED:
                holder.state.setText(context.getString(R.string.status_finished));
                holder.pauseButton.setVisibility(View.GONE);
                holder.editButton.setVisibility(View.GONE);
                holder.deleteButton.setEnabled(true);
                break;
        }

        //Set progress
        if (task.getState() == Task.State.STOPPED || task instanceof FindPrimesTask && ((FindPrimesTask) task).getEndValue() == FindPrimesTask.INFINITY){
            holder.progress.setVisibility(View.GONE);
        }else if (task.getState() != Task.State.STOPPED){
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress.setText(context.getString(R.string.task_progress, decimalFormat.format(task.getProgress() * 100)));
        }

        holder.root.setSelected(holder.getAdapterPosition() == getSelectedItemPosition());
    }

    @Override
    protected void onUpdate(AbstractTaskListItemViewHolder viewHolder) {
        //Set progress
        final Task task = tasks.get(viewHolder.getAdapterPosition());
        if (task.getState() == Task.State.STOPPED || task instanceof FindPrimesTask && ((FindPrimesTask) task).getEndValue() == FindPrimesTask.INFINITY){
            viewHolder.progress.setVisibility(View.GONE);
        }else if (task.getState() != Task.State.STOPPED){
            viewHolder.progress.setVisibility(View.VISIBLE);
            viewHolder.progress.setText(context.getString(R.string.task_progress, decimalFormat.format(task.getProgress() * 100)));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class ViewHolder extends AbstractTaskListAdapter.AbstractTaskListItemViewHolder{

        private final ImageButton editButton;

        public ViewHolder(View itemView){
            super(itemView);
            editButton = itemView.findViewById(R.id.edit_button);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendOnEditClicked(tasks.get(getAdapterPosition()));
                }
            });
        }
    }

    public void addTaskListAdapterCallbacks(final TaskListAdapterCallbacks taskListAdapterCallbacks){
        this.taskListAdapterCallbacks.add(taskListAdapterCallbacks);
    }

    public void removeTaskListAdapterCallbacks(final TaskListAdapterCallbacks taskListAdapterCallbacks){
        this.taskListAdapterCallbacks.remove(taskListAdapterCallbacks);
    }

    private void sendOnEditClicked(final Task task){
        for (TaskListAdapterCallbacks taskListAdapterCallbacks : taskListAdapterCallbacks){
            taskListAdapterCallbacks.onEditClicked(task);
        }
    }
}