package com.tycho.app.primenumberfinder.modules.primefactorization.adapters;

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
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_PRIME_FACTORIZATION;

/**
 * Created by tycho on 11/16/2017.
 */

public class PrimeFactorizationTaskListAdapter extends AbstractTaskListAdapter<PrimeFactorizationTaskListAdapter.Dummy> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationTaskListAdapter.class.getSimpleName();

    public PrimeFactorizationTaskListAdapter(final Context context) {
       super(context);
    }

    @NonNull
    @Override
    public Dummy onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prime_factorization_task_list_item, parent, false);
        return new PrimeFactorizationTaskListAdapter.Dummy(view);
    }

    @Override
    protected void doOnBindViewHolder(PrimeFactorizationTaskListAdapter.Dummy holder, int position) {

        //Get the current task
        final PrimeFactorizationTask task = (PrimeFactorizationTask) tasks.get(holder.getAdapterPosition());

        //Set title
        holder.title.setText(context.getString(R.string.prime_factorization_task_list_item_title, NUMBER_FORMAT.format(task.getNumber())));

        manageStandardViews(task, holder);

        //Set state and buttons
        switch (task.getState()) {
            case RUNNING:
                /*holder.state.setText(context.getString(R.string.status_searching));
                holder.pauseButton.setEnabled(true);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.editButton.setVisibility(View.VISIBLE);
                holder.editButton.setEnabled(false);
                holder.deleteButton.setEnabled(false);*/

                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case PAUSING:
                /*holder.state.setText(context.getString(R.string.state_pausing));
                holder.pauseButton.setEnabled(false);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.editButton.setEnabled(false);
                holder.deleteButton.setEnabled(false);*/

                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case PAUSED:
                /*holder.state.setText(context.getString(R.string.status_paused));
                holder.pauseButton.setEnabled(true);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                holder.editButton.setEnabled(true);
                holder.deleteButton.setEnabled(true);*/

                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case RESUMING:
                /*holder.state.setText(context.getString(R.string.state_resuming));
                holder.pauseButton.setEnabled(false);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.editButton.setEnabled(false);
                holder.deleteButton.setEnabled(false);*/

                //Progress
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
                break;

            case STOPPED:
                final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(context.getString(R.string.status_finished));
                spannableStringBuilder.append(": ");
                spannableStringBuilder.append(context.getString(R.string.prime_factorization_result, NUMBER_FORMAT.format((task.getPrimeFactors().size()))));
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent_dark)), context.getString(R.string.status_finished).length() + 2, spannableStringBuilder.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                holder.state.setText(spannableStringBuilder);
                /*holder.pauseButton.setVisibility(View.GONE);
                holder.editButton.setVisibility(View.GONE);
                holder.deleteButton.setEnabled(true);*/

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
        return TASK_TYPE_PRIME_FACTORIZATION;
    }

    class Dummy extends AbstractTaskListAdapter.ViewHolder{
        Dummy(View itemView){
            super(itemView);
        }
    }
}