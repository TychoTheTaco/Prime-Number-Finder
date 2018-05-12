package com.tycho.app.primenumberfinder.modules.findfactors.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_FACTORS;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindFactorsTaskListAdapter extends AbstractTaskListAdapter<FindFactorsTaskListAdapter.Dummy> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindFactorsTaskListAdapter.class.getSimpleName();

    public FindFactorsTaskListAdapter(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    public Dummy onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_factors_task_list_item, parent, false);
        return new FindFactorsTaskListAdapter.Dummy(view);
    }

    @Override
    protected void doOnBindViewHolder(FindFactorsTaskListAdapter.Dummy holder, int position) {

        //Get the current task
        final FindFactorsTask task = (FindFactorsTask) tasks.get(position);

        //Set title
        holder.title.setText(context.getString(R.string.find_factors_task_list_item_title, NUMBER_FORMAT.format(task.getNumber())));

        manageStandardViews(task, holder);

        //Set state and buttons
        switch (task.getState()) {
            case RUNNING:
                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case PAUSING:
                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case PAUSED:
                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case RESUMING:
                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case STOPPED:
                final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(context.getString(R.string.status_finished));
                spannableStringBuilder.append(": ");
                spannableStringBuilder.append(context.getString(R.string.find_factors_result, NUMBER_FORMAT.format((task.getFactors().size()))));
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent_dark)), context.getString(R.string.status_finished).length() + 2, spannableStringBuilder.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                holder.state.setText(spannableStringBuilder);

                //Progress
                holder.progress.setVisibility(View.GONE);
                break;
        }

        //Show saved
        if (holder.isSaved()){
            holder.saveButton.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress.setText("Saved");
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    protected int getTaskType() {
        return TASK_TYPE_FIND_FACTORS;
    }

    class Dummy extends AbstractTaskListAdapter.ViewHolder{
        Dummy(View itemView){
            super(itemView);
        }
    }
}