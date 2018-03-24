package com.tycho.app.primenumberfinder.modules.primefactorization.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by tycho on 11/16/2017.
 */

public class PrimeFactorizationTaskListAdapter extends AbstractTaskListAdapter<AbstractTaskListAdapter.ViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrmFctrztnTskListAdptr";

    private final DecimalFormat decimalFormat = new DecimalFormat("##0.00");

    private final Context context;

    public PrimeFactorizationTaskListAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prime_factorization_task_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void doOnBindViewHolder(RecyclerView.ViewHolder h, int position) {

        final ViewHolder holder = (ViewHolder) h;

        //Get the current task
        final PrimeFactorizationTask task = (PrimeFactorizationTask) tasks.get(holder.getAdapterPosition());

        //Set title
        holder.title.setText(context.getString(R.string.prime_factorization_task_list_item_title, NumberFormat.getInstance(Locale.getDefault()).format(task.getNumber())));

        //Set state and buttons
        switch (task.getState()) {
            case RUNNING:
                holder.state.setText(context.getString(R.string.status_searching));
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.deleteButton.setEnabled(false);
                break;

            case PAUSED:
                holder.state.setText(context.getString(R.string.status_paused));
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                holder.deleteButton.setEnabled(true);
                break;

            case STOPPED:
                holder.state.setText(context.getString(R.string.status_finished));
                holder.pauseButton.setVisibility(View.GONE);
                holder.deleteButton.setEnabled(true);
                break;
        }

        //Set progress
        /*if (task.getState() != Task.State.STOPPED){
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
        }else{
            holder.progress.setVisibility(View.GONE);
        }*/
        holder.progress.setVisibility(View.GONE);

        holder.root.setSelected(holder.getAdapterPosition() == getSelectedItemPosition());
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}