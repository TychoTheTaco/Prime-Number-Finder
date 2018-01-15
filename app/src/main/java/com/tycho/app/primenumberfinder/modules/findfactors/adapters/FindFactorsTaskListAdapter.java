package com.tycho.app.primenumberfinder.modules.findfactors.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindFactorsTaskListAdapter extends AbstractTaskListAdapter<AbstractTaskListAdapter.AbstractTaskListItemViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsTskLstAdptr";

    private final DecimalFormat decimalFormat = new DecimalFormat("##0.00");

    private final Context context;

    public FindFactorsTaskListAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public AbstractTaskListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_factors_task_list_item, parent, false);
        return new AbstractTaskListItemViewHolder(view);
    }

    @Override
    protected void doOnBindViewHolder(RecyclerView.ViewHolder h, int position) {

        final AbstractTaskListItemViewHolder holder = (AbstractTaskListItemViewHolder) h;

        //Get the current task
        final FindFactorsTask task = (FindFactorsTask) tasks.get(position);

        //Set title
        holder.title.setText(context.getString(R.string.find_factors_task_list_item_title, NumberFormat.getInstance(Locale.getDefault()).format(task.getNumber())));

        switch (task.getState()) {
            case RUNNING:
                holder.state.setText(context.getString(R.string.status_searching));
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.deleteButton.setEnabled(false);

                //Set progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, decimalFormat.format(task.getProgress() * 100)));
                break;

            case PAUSED:
                holder.state.setText(context.getString(R.string.status_paused));
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                holder.deleteButton.setEnabled(true);

                //Set progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, decimalFormat.format(task.getProgress() * 100)));
                break;

            case STOPPED:
                holder.state.setText(context.getString(R.string.status_finished));
                holder.pauseButton.setVisibility(View.GONE);
                holder.deleteButton.setEnabled(true);

                //Set progress
                holder.progress.setVisibility(View.GONE);
                break;
        }

        holder.root.setSelected(holder.getAdapterPosition() == getSelectedItemPosition());
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}