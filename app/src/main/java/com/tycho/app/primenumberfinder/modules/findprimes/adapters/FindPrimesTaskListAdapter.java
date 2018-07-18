/*
package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

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
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_PRIMES;

*/
/**
 * Created by tycho on 11/16/2017.
 *//*


public class FindPrimesTaskListAdapter extends AbstractTaskListAdapter<FindPrimesTaskListAdapter.Dummy>{

    */
/**
     * Tag used for logging and debugging.
     *//*

    private static final String TAG = FindPrimesTaskListAdapter.class.getSimpleName();

    public FindPrimesTaskListAdapter(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    public FindPrimesTaskListAdapter.Dummy onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_primes_task_list_item, parent, false);
        return new FindPrimesTaskListAdapter.Dummy(view);
    }

    @Override
    protected void doOnBindViewHolder(FindPrimesTaskListAdapter.Dummy holder, int position) {

        //Get the current task
        final Task task = getTask(position);

        //Set title
        if (task instanceof FindPrimesTask) {
            final long endValue = ((FindPrimesTask) task).getEndValue();
            holder.title.setText(context.getString(R.string.find_primes_task_list_item_title, NumberFormat.getInstance(Locale.getDefault()).format(((FindPrimesTask) task).getStartValue()), endValue == FindPrimesTask.INFINITY ? context.getString(R.string.infinity_text) : NumberFormat.getInstance(Locale.getDefault()).format(endValue)));
        } else if (task instanceof CheckPrimalityTask) {
            holder.title.setText(context.getString(R.string.check_primality_task_list_title, NumberFormat.getInstance(Locale.getDefault()).format(((CheckPrimalityTask) task).getNumber())));
        }

        manageStandardViews(task, holder);

        if (task instanceof CheckPrimalityTask){
            holder.saveButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
        }

        if (task.getState() == Task.State.STOPPED){
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
            holder.subtitle.setText(spannableStringBuilder);
        }

        onUpdate(holder);
    }

    @Override
    protected void onUpdate(AbstractTaskListAdapter.ViewHolder holder) {
        final Task task = getTask(holder.getAdapterPosition());

        //Set progress
        if (holder.getAdapterPosition() != -1){
            if (task.getState() == Task.State.STOPPED || task instanceof FindPrimesTask && ((FindPrimesTask) task).getEndValue() == FindPrimesTask.INFINITY){
                holder.progress.setVisibility(View.GONE);
            }else if (task.getState() != Task.State.STOPPED){
                holder.progress.setVisibility(View.VISIBLE);
                holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));
            }
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
        return TASK_TYPE_FIND_PRIMES;
    }

    class Dummy extends AbstractTaskListAdapter.ViewHolder{
        Dummy(View itemView){
            super(itemView);
        }

        @Override
        protected void onPausePressed() {
            final Task task = getTask(getAdapterPosition());
            if (task instanceof FindPrimesTask && ((FindPrimesTask) task).getEndValue() == FindPrimesTask.INFINITY){
                setSaved(task, false);
            }
        }
    }
}*/
