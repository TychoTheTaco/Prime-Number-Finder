package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;


/**
 * Created by tycho on 12/12/2017.
 */

public abstract class AbstractTaskListAdapter<H extends AbstractTaskListAdapter.ViewHolder> extends RecyclerView.Adapter<H> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = AbstractTaskListAdapter.class.getSimpleName();

    /**
     * List of tasks in the adapter.
     */
    protected final List<Item> items = new ArrayList<>();

    /**
     * The currently selected item position. This will be -1 if nothing is selected.
     */
    private int selectedItemPosition;

    /**
     * Event listeners.
     */
    private final List<EventListener> eventListeners = new ArrayList<>();

    /**
     * Custom task event listeners that update the view holder when the task is paused / resumed / stopped.
     */
    private Map<Task, CustomTaskEventListener> customEventListeners = new HashMap<>();

    /**
     * Handler for posting to UI thread.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Action listeners for button presses. (delete / edit / pause).
     */
    private final CopyOnWriteArrayList<ActionViewListener> actionViewListeners = new CopyOnWriteArrayList<>();

    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.00");
    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private final TaskAdapter taskAdapter = new TaskAdapter() {
        @Override
        public void onTaskStarted() {
            sendTaskStatesChanged();
        }

        @Override
        public void onTaskPaused() {
            sendTaskStatesChanged();
        }

        @Override
        public void onTaskResumed() {
            sendTaskStatesChanged();
        }

        @Override
        public void onTaskStopped() {
            sendTaskStatesChanged();
        }
    };

    protected final Context context;

    private class Item {
        private final Task task;
        private boolean saved;

        public Item(final Task task) {
            this.task = task;
        }

        public Task getTask() {
            return task;
        }

        public void setSaved(boolean saved) {
            this.saved = saved;
        }

        public boolean isSaved() {
            return saved;
        }
    }

    public AbstractTaskListAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public void onBindViewHolder(@NonNull H holder, int position) {
        final Task task = items.get(position).getTask();
        customEventListeners.get(task).setViewHolder(holder);

        //Start the UI updater if it hasn't been started yet
        if (holder.uiUpdater.getState() == Task.State.NOT_STARTED) {
            //holder.uiUpdater.addTaskListener(getUiUpdaterDebugListener(holder));
            holder.uiUpdater.startOnNewThread();
            if (task.getState() == Task.State.PAUSED || task.getState() == Task.State.NOT_STARTED || task.getState() == Task.State.STOPPED) {
                holder.uiUpdater.pause();
            }
        }

        doOnBindViewHolder(holder, position);
    }

    private TaskListener getUiUpdaterDebugListener(final ViewHolder holder){
        return new TaskListener() {
            @Override
            public void onTaskStarted() {
                Log.w(TAG, "UI Updater started(): " + holder);
            }

            @Override
            public void onTaskPausing() {
                Log.w(TAG, "UI Updater pausing(): " + holder);
            }

            @Override
            public void onTaskPaused() {
                Log.w(TAG, "UI Updater paused(): " + holder);
            }

            @Override
            public void onTaskResuming() {
                Log.w(TAG, "UI Updater resuming(): " + holder);
            }

            @Override
            public void onTaskResumed() {
                Log.w(TAG, "UI Updater resumed(): " + holder);
            }

            @Override
            public void onTaskStopping() {
                Log.w(TAG, "UI Updater stopping(): " + holder);
            }

            @Override
            public void onTaskStopped() {
                Log.w(TAG, "UI Updater stopped(): " + holder);
            }
        };
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected void manageStandardViews(final Task task, final H holder) {

        //Check if this item should be selected
        holder.itemView.setSelected(holder.getAdapterPosition() == getSelectedItemPosition());

        //Manage button visibility
        switch (task.getState()) {
            case RUNNING:
                //State
                holder.state.setText(context.getString(R.string.status_searching));

                //Pause button
                holder.pauseButton.setEnabled(true);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);

                //Edit button
                holder.editButton.setVisibility(View.VISIBLE);
                holder.editButton.setEnabled(false);

                //Delete button
                holder.deleteButton.setEnabled(false);

                //Save button
                holder.saveButton.setVisibility(View.GONE);
                break;

            case PAUSING:
                //State
                holder.state.setText(context.getString(R.string.state_pausing));

                //Pause button
                holder.pauseButton.setEnabled(false);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);

                //Edit button
                holder.editButton.setEnabled(false);

                //Delete button
                holder.deleteButton.setEnabled(false);

                //Save button
                holder.saveButton.setVisibility(View.GONE);
                break;

            case PAUSED:
                //State
                holder.state.setText(context.getString(R.string.status_paused));

                //Pause button
                holder.pauseButton.setEnabled(true);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);

                //Edit button
                holder.editButton.setEnabled(true);

                //Delete button
                holder.deleteButton.setEnabled(true);

                //Save button
                holder.saveButton.setVisibility(View.GONE);
                break;

            case RESUMING:
                //State
                holder.state.setText(context.getString(R.string.state_resuming));

                //Pause button
                holder.pauseButton.setEnabled(false);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);

                //Edit button
                holder.editButton.setEnabled(false);

                //Delete button
                holder.deleteButton.setEnabled(false);

                //Save button
                holder.saveButton.setVisibility(View.GONE);
                break;

            case STOPPED:
                //State
                holder.state.setText(context.getString(R.string.status_finished));

                //Pause button
                holder.pauseButton.setVisibility(View.GONE);

                //Edit button
                holder.editButton.setVisibility(View.GONE);

                //Delete button
                holder.deleteButton.setEnabled(true);

                //Save button
                holder.saveButton.setEnabled(true);
                holder.saveButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    protected abstract void doOnBindViewHolder(H holder, int position);

    public void addTask(final Task task) {
        final CustomTaskEventListener customTaskEventListener = new CustomTaskEventListener() {
            @Override
            public void onTaskStarted() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            holder.uiUpdater.resume();
                            notifyItemChanged(holder.getAdapterPosition());
                        }

                    }
                });
            }

            @Override
            public void onTaskPausing() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            notifyItemChanged(holder.getAdapterPosition());
                        }

                    }
                });
            }

            @Override
            public void onTaskPaused() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            holder.uiUpdater.pause();
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    }
                });
            }

            @Override
            public void onTaskResuming() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            notifyItemChanged(holder.getAdapterPosition());
                        }

                    }
                });
            }

            @Override
            public void onTaskResumed() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            holder.uiUpdater.resume();
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    }
                });
            }

            @Override
            public void onTaskStopping() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            notifyItemChanged(holder.getAdapterPosition());
                        }

                    }
                });
            }

            @Override
            public void onTaskStopped() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            holder.uiUpdater.pause();
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    }
                });
            }
        };
        task.addTaskListener(customTaskEventListener);
        task.addTaskListener(taskAdapter);
        items.add(new Item(task));
        notifyItemInserted(getItemCount());
        customEventListeners.put(task, customTaskEventListener);
        sendTaskStatesChanged();
    }

    public void setSelected(int index) {
        if (index < getItemCount()) {
            final int changed = selectedItemPosition;
            selectedItemPosition = index;
            notifyItemChanged(selectedItemPosition);
            if (changed != -1) notifyItemChanged(changed);

            if (selectedItemPosition == -1) {
                sendOnTaskSelected(null);
            } else {
                sendOnTaskSelected(getTask(selectedItemPosition));
            }
        }
    }

    public void setSelected(final Task task) {
        setSelected(items.indexOf(getItem(task)));
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void sortByTimeCreated() {
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item item0, Item item1) {
                return Long.compare(item0.getTask().getStartTime(), item1.getTask().getStartTime());
            }
        });
        notifyDataSetChanged();
    }

    public interface EventListener {
        void onTaskSelected(final Task task);

        void onPausePressed(final Task task);

        void onTaskRemoved(final Task task);

        void onEditPressed(final Task task);

        void onSavePressed(final Task task);
    }

    public void addEventListener(final EventListener eventListener) {
        if (!eventListeners.contains(eventListener)) eventListeners.add(eventListener);
    }

    public boolean removeEventListener(final EventListener eventListener) {
        return eventListeners.remove(eventListener);
    }

    private void sendOnTaskSelected(final Task task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onTaskSelected(task);
        }
    }

    private void sendOnPausePressed(final Task task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onPausePressed(task);
        }
    }

    private void sendOnEditClicked(final Task task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onEditPressed(task);
        }
    }

    private void sendOnDeletePressed(final Task task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onTaskRemoved(task);
        }
    }

    private void sendOnSavePressed(final Task task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onSavePressed(task);
        }
    }

    protected abstract class CustomTaskEventListener extends TaskAdapter {
        protected AbstractTaskListAdapter.ViewHolder holder;

        private void setViewHolder(final AbstractTaskListAdapter.ViewHolder holder) {
            this.holder = holder;
        }
    }

    protected Task getTask(final int position) {
        return items.get(position).getTask();
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView title;
        public final TextView state;
        public final TextView progress;
        public final ImageButton pauseButton;
        public final ImageButton editButton;
        public final ImageButton deleteButton;
        public final ImageButton saveButton;

        protected final UiUpdater uiUpdater = new UiUpdater(this);

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            state = itemView.findViewById(R.id.state);
            progress = itemView.findViewById(R.id.textView_search_progress);
            pauseButton = itemView.findViewById(R.id.pause_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            saveButton = itemView.findViewById(R.id.save_button);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedItemPosition == getAdapterPosition()) {
                        setSelected(null);
                    } else {
                        setSelected(getAdapterPosition());
                    }
                }
            });

            //Set button listeners
            pauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (items.get(getAdapterPosition()).getTask().getState()) {

                        case PAUSED:
                            getTask(getAdapterPosition()).resume();
                            break;

                        case RUNNING:
                            getTask(getAdapterPosition()).pause();
                            break;
                    }

                    onPausePressed();
                    sendOnPausePressed(getTask(getAdapterPosition()));
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendOnEditClicked(getTask(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Pause the UI updater. It will be re-used by other ViewHolders
                    getTask(getAdapterPosition()).pause();

                    if (getAdapterPosition() < selectedItemPosition) {
                        selectedItemPosition--;
                    } else if (getAdapterPosition() == selectedItemPosition) {
                        selectedItemPosition = -1;
                        sendOnTaskSelected(null);
                    }

                    //Remove the task from the list
                    final int position = getAdapterPosition();
                    final Task task = getTask(position);
                    customEventListeners.remove(task);
                    items.remove(position);
                    notifyItemRemoved(position);

                    PrimeNumberFinder.getTaskManager().unregisterTask(task);

                    //Notify listeners
                    onDeletePressed();
                    sendOnDeletePressed(task);
                }
            });

            //Save button
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveButton.setEnabled(false);
                    sendOnSavePressed(getTask(getAdapterPosition()));
                }
            });
        }

        protected void onPausePressed(){}

        protected void onDeletePressed(){}
    }

    public void setSaved(final int index, boolean isSaved){
        final Item item = items.get(index);
        if (item != null){
            item.setSaved(isSaved);
            notifyItemChanged(index);
        }
    }

    public void setSaved(final Task task, boolean isSaved) {
        final Item item = getItem(task);
        final int index = items.indexOf(item);
        if (item != null){
            item.setSaved(isSaved);
            notifyItemChanged(index);
        }
    }

    public void postSetSaved(final Task task, final boolean isSaved){
        handler.post(new Runnable() {
            @Override
            public void run() {
                setSaved(task, isSaved);
            }
        });
    }

    protected boolean isSaved(Task task){
        return getItem(task).isSaved();
    }

    public List<Task> getSavedItems(){
        final List<Task> savedItems = new ArrayList<>();
        for (Item item : items){
            if (item.isSaved()){
                savedItems.add(item.getTask());
            }
        }
        return savedItems;
    }

    public int indexOf(final Task task){
        return items.indexOf(getItem(task));
    }

    protected void onUpdate(final ViewHolder viewHolder) {
        notifyItemChanged(viewHolder.getAdapterPosition());
    }

    protected Item getItem(final Task task) {
        for (Item item : items) {
            if (item.getTask() == task) {
                return item;
            }
        }
        return null;
    }

    private class UiUpdater extends Task {

        private final ViewHolder viewHolder;

        UiUpdater(final ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        protected void run() {

            while (true) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Make sure the view holder is still visible
                        if (viewHolder.getAdapterPosition() != -1) {
                            onUpdate(viewHolder);
                        }else{
                            Log.e(TAG, "Posted an invalid update on " + viewHolder);
                        }
                    }
                });

                try {
                    Thread.sleep(1000 / 25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                tryPause();
                if (shouldStop()) {
                    break;
                }
            }
        }
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        if (!actionViewListeners.contains(actionViewListener)) {
            this.actionViewListeners.add(actionViewListener);
        }
    }

    public boolean removeActionViewListener(final ActionViewListener actionViewListener) {
        return this.actionViewListeners.remove(actionViewListener);
    }

    private void sendTaskStatesChanged() {
        boolean active = false;
        for (Item item : items) {
            if (item.getTask().getState() == Task.State.RUNNING) {
                active = true;
                break;
            }
        }

        for (ActionViewListener actionViewListener : this.actionViewListeners) {
            actionViewListener.onTaskStatesChanged(getTaskType(), active);
        }
    }

    protected abstract int getTaskType();
}
