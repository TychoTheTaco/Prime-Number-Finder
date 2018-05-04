package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_PRIMES;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesTaskListAdapter extends AbstractTaskListAdapter<AbstractTaskListAdapter.ViewHolder>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesTaskListAdapter.class.getSimpleName();

    private final Context context;

    public FindPrimesTaskListAdapter(final Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
            holder.title.setText(context.getString(R.string.check_primality_subtitle_searching, NumberFormat.getInstance(Locale.getDefault()).format(((CheckPrimalityTask) task).getNumber())));
        }

        //Set state and buttons
        switch (task.getState()) {
            case RUNNING:
                holder.state.setText(context.getString(R.string.status_searching));
                holder.pauseButton.setEnabled(true);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.editButton.setVisibility(View.VISIBLE);
                holder.editButton.setEnabled(false);
                holder.deleteButton.setEnabled(false);
                break;

            case PAUSING:
                holder.state.setText(context.getString(R.string.state_pausing));
                holder.pauseButton.setEnabled(false);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.editButton.setEnabled(false);
                holder.deleteButton.setEnabled(false);
                break;

            case PAUSED:
                holder.state.setText(context.getString(R.string.status_paused));
                holder.pauseButton.setEnabled(true);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                holder.editButton.setEnabled(true);
                holder.deleteButton.setEnabled(true);
                break;

            case RESUMING:
                holder.state.setText(context.getString(R.string.state_resuming));
                holder.pauseButton.setEnabled(false);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                holder.editButton.setEnabled(false);
                holder.deleteButton.setEnabled(false);
                break;

            case STOPPED:
                holder.state.setText(context.getString(R.string.status_finished));
                final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(context.getString(R.string.status_finished));
                spannableStringBuilder.append(": ");
                if (task instanceof FindPrimesTask){
                    spannableStringBuilder.append(context.getString(R.string.find_primes_result, NUMBER_FORMAT.format(((FindPrimesTask) task).getPrimeCount())));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent_dark)), context.getString(R.string.status_finished).length() + 2, spannableStringBuilder.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }else if (task instanceof CheckPrimalityTask){
                    spannableStringBuilder.append(context.getString(R.string.check_primality_result, NUMBER_FORMAT.format(((CheckPrimalityTask) task).getNumber()), ((CheckPrimalityTask) task).isPrime() ? "prime" : "not prime"));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent_dark)), context.getString(R.string.status_finished).length() + 2, spannableStringBuilder.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                holder.state.setText(spannableStringBuilder);
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
            holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
        }

        holder.root.setSelected(holder.getAdapterPosition() == getSelectedItemPosition());
    }

    @Override
    protected void onUpdate(AbstractTaskListAdapter.ViewHolder viewHolder) {
        //Set progress
        if (viewHolder.getAdapterPosition() != -1){
            final Task task = tasks.get(viewHolder.getAdapterPosition());
            if (task.getState() == Task.State.STOPPED || task instanceof FindPrimesTask && ((FindPrimesTask) task).getEndValue() == FindPrimesTask.INFINITY){
                viewHolder.progress.setVisibility(View.GONE);
            }else if (task.getState() != Task.State.STOPPED){
                viewHolder.progress.setVisibility(View.VISIBLE);
                viewHolder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
            }
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    protected int getTaskType() {
        return TASK_TYPE_FIND_PRIMES;
    }
}