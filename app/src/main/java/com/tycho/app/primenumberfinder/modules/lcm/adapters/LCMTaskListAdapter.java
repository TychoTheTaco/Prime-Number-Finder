package com.tycho.app.primenumberfinder.modules.lcm.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_FACTORS;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_LCM;

/**
 * Created by tycho on 11/16/2017.
 */

public class LCMTaskListAdapter extends AbstractTaskListAdapter<LCMTaskListAdapter.Dummy> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LCMTaskListAdapter.class.getSimpleName();

    public LCMTaskListAdapter(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    public Dummy onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_primes_task_list_item, parent, false);
        return new Dummy(view);
    }

    @Override
    protected void doOnBindViewHolder(Dummy holder, int position) {

        //Get the current task
        final LeastCommonMultipleTask task = (LeastCommonMultipleTask) getTask(position);

        //Set title
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < task.getNumbers().size(); i++){
            stringBuilder.append(NUMBER_FORMAT.format(task.getNumbers().get(i)));
            if (i == task.getNumbers().size() - 2){
                stringBuilder.append("; and ");
            }else if (i != task.getNumbers().size() - 1){
                stringBuilder.append("; ");
            }
        }
        holder.title.setText(context.getString(R.string.lcm_task_list_item_title, stringBuilder.toString()));

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
                spannableStringBuilder.append(context.getString(R.string.lcm_result, NUMBER_FORMAT.format((task.getLcm()))));
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent_dark)), context.getString(R.string.status_finished).length() + 2, spannableStringBuilder.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                holder.state.setText(spannableStringBuilder);

                //Progress
                holder.progress.setVisibility(View.GONE);
                break;
        }

        //Show saved
        if (isSaved(task)){
            holder.saveButton.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress.setText(context.getString(R.string.saved));
        }
    }

    @Override
    protected int getTaskType() {
        return TASK_TYPE_LCM;
    }

    class Dummy extends AbstractTaskListAdapter.ViewHolder{
        Dummy(View itemView){
            super(itemView);
        }
    }
}