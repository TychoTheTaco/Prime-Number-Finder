package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.utils.UIUpdater;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskListener;


/**
 * Created by tycho on 12/12/2017.
 */

public class AbstractTaskListAdapter<T extends ITask> extends RecyclerView.Adapter<AbstractTaskListAdapter.ViewHolder> implements TaskListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = AbstractTaskListAdapter.class.getSimpleName();

    /**
     * List of items in the adapter.
     */
    protected final List<T> tasks = new ArrayList<>();

    /**
     * The currently selected item position. This will be -1 if nothing is selected.
     */
    private int selectedItemPosition;

    /**
     * Event listeners.
     */
    private final List<EventListener> eventListeners = new ArrayList<>();

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

    protected final Context context;

    private final List<Button> buttons = new ArrayList<>();

    public AbstractTaskListAdapter(final Context context) {
        this.context = context;
    }

    public AbstractTaskListAdapter(final Context context, Button... buttons) {
        this(context);
        this.buttons.addAll(Arrays.asList(buttons));
    }

    @NonNull
    @Override
    public AbstractTaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AbstractTaskListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_item, parent, false), buttons);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractTaskListAdapter.ViewHolder holder, int position) {
        final T task = tasks.get(position);
        holder.setTask(task);

        //Check if this item should be selected
        holder.itemView.setSelected(holder.getAdapterPosition() == getSelectedItemPosition());

        holder.title.setText(getTitle(task));
        holder.subtitle.setText(getSubtitle(task));
        holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(task.getProgress() * 100)));

        //Manage button visibility
        switch (task.getState()) {
            case RUNNING:
                //Pause button
                if (holder.pauseButton != null) {
                    holder.pauseButton.setEnabled(true);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }

                //Edit button
                if (holder.editButton != null) {
                    holder.editButton.setEnabled(false);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null) {
                    holder.deleteButton.setEnabled(false);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null) {
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case PAUSING:
                //Pause button
                if (holder.pauseButton != null) {
                    holder.pauseButton.setEnabled(false);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }

                //Edit button
                if (holder.editButton != null) {
                    holder.editButton.setEnabled(false);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null) {
                    holder.deleteButton.setEnabled(false);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null) {
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case PAUSED:
                //Pause button
                if (holder.pauseButton != null) {
                    holder.pauseButton.setEnabled(true);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                }

                //Edit button
                if (holder.editButton != null) {
                    holder.editButton.setEnabled(true);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null) {
                    holder.deleteButton.setEnabled(true);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null) {
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case RESUMING:
                //Pause button
                if (holder.pauseButton != null) {
                    holder.pauseButton.setEnabled(false);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }

                //Edit button
                if (holder.editButton != null) {
                    holder.editButton.setEnabled(false);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null) {
                    holder.deleteButton.setEnabled(false);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null) {
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case STOPPED:
                //Pause button
                if (holder.pauseButton != null) {
                    holder.pauseButton.setVisibility(View.GONE);
                }

                //Edit button
                if (holder.editButton != null) {
                    holder.editButton.setVisibility(View.GONE);
                }

                //Delete button
                if (holder.deleteButton != null) {
                    holder.deleteButton.setEnabled(true);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null) {
                    holder.saveButton.setEnabled(true);
                    holder.saveButton.setVisibility(View.VISIBLE);
                }

                //Hide progress if task is complete
                if (task.getProgress() == 1) {
                    holder.progress.setVisibility(View.GONE);
                }
                break;
        }

        onUpdate(holder);

        //Show saved
        if (isSaved(task)) {
            if (holder.saveButton != null) holder.saveButton.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress.setText(context.getString(R.string.saved));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public void onTaskStarted(final ITask task) {
        sendTaskStatesChanged();
    }

    @Override
    public void onTaskPausing(final ITask task) {

    }

    @Override
    public void onTaskPaused(final ITask task) {
        sendTaskStatesChanged();
    }

    @Override
    public void onTaskResuming(final ITask task) {

    }

    @Override
    public void onTaskResumed(final ITask task) {
        sendTaskStatesChanged();
    }

    @Override
    public void onTaskStopping(final ITask task) {

    }

    @Override
    public void onTaskStopped(final ITask task) {
        sendTaskStatesChanged();
    }

    protected CharSequence getTitle(final T task) {
        return task.getClass().getSimpleName();
    }

    protected CharSequence getSubtitle(final T task) {
        switch (task.getState()) {
            default:
                return "";

            case RUNNING:
                return context.getString(R.string.status_searching);

            case PAUSING:
                return context.getString(R.string.state_pausing);

            case PAUSED:
                return context.getString(R.string.status_paused);

            case RESUMING:
                return context.getString(R.string.state_resuming);

            case STOPPED:
                return context.getString(R.string.status_finished);
        }
    }

    public void addTask(final T task) {
        task.addTaskListener(this);
        tasks.add(task);
        notifyItemInserted(getItemCount());
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

    public void setSelected(final ITask task) {
        setSelected(tasks.indexOf(getItem(task)));
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void sortByTimeCreated() {
        Collections.sort(tasks, (item0, item1) -> Long.compare(item0.getStartTime(), item1.getStartTime()));
        notifyDataSetChanged();
    }

    public interface EventListener {
        void onTaskSelected(final ITask task);

        void onPausePressed(final ITask task);

        void onTaskRemoved(final ITask task);

        void onEditPressed(final ITask task);

        void onSavePressed(final ITask task);
    }

    public void addEventListener(final EventListener eventListener) {
        if (!eventListeners.contains(eventListener)) eventListeners.add(eventListener);
    }

    public boolean removeEventListener(final EventListener eventListener) {
        return eventListeners.remove(eventListener);
    }

    private void sendOnTaskSelected(final ITask task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onTaskSelected(task);
        }
    }

    private void sendOnPausePressed(final ITask task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onPausePressed(task);
        }
    }

    private void sendOnEditClicked(final ITask task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onEditPressed(task);
        }
    }

    private void sendOnDeletePressed(final ITask task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onTaskRemoved(task);
        }
    }

    private void sendOnSavePressed(final ITask task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onSavePressed(task);
        }
    }

    protected ITask getTask(final int position) {
        return tasks.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements TaskListener {

        public final TextView title;
        public final TextView subtitle;
        public final TextView progress;
        public ImageButton pauseButton;
        public ImageButton editButton;
        public ImageButton deleteButton;
        public ImageButton saveButton;

        private ITask task;

        private final UIUpdater uiUpdater = new UIUpdater(handler) {
            @Override
            protected void update() {
                //Make sure the view holder is still visible
                if (getAdapterPosition() != -1) {
                    onUpdate(ViewHolder.this);
                    notifyItemChanged(getAdapterPosition());
                } else {
                    Log.w(TAG, "Posted an invalid update on " + ViewHolder.this);
                }
            }
        };

        private final List<Button> buttons = new ArrayList<>();

        public ViewHolder(View itemView, final List<Button> buttons) {
            super(itemView);
            this.buttons.clear();
            this.buttons.addAll(buttons);

            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.task_state);
            progress = itemView.findViewById(R.id.search_progress);
            pauseButton = itemView.findViewById(R.id.pause_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            saveButton = itemView.findViewById(R.id.save_button);

            if (!buttons.contains(Button.PAUSE)) {
                pauseButton.setVisibility(View.GONE);
                pauseButton = null;
            }
            if (!buttons.contains(Button.EDIT)) {
                editButton.setVisibility(View.GONE);
                editButton = null;
            }
            if (!buttons.contains(Button.DELETE)) {
                deleteButton.setVisibility(View.GONE);
                deleteButton = null;
            }
            if (!buttons.contains(Button.SAVE)) {
                saveButton.setVisibility(View.GONE);
                saveButton = null;
            }

            itemView.setOnClickListener(v -> {
                if (selectedItemPosition == getAdapterPosition()) {
                    setSelected(null);
                } else {
                    setSelected(getAdapterPosition());
                }
            });

            //Set button listeners
            if (pauseButton != null) {
                pauseButton.setOnClickListener(v -> {
                    switch (tasks.get(getAdapterPosition()).getState()) {

                        case PAUSED:
                            getTask(getAdapterPosition()).resume();
                            break;

                        case RUNNING:
                            getTask(getAdapterPosition()).pause();
                            break;
                    }

                    onPausePressed();
                    sendOnPausePressed(getTask(getAdapterPosition()));
                });
            }

            if (editButton != null) {
                editButton.setOnClickListener(v -> sendOnEditClicked(getTask(getAdapterPosition())));
            }

            if (deleteButton != null) {
                deleteButton.setOnClickListener(v -> {

                    getTask(getAdapterPosition()).pause();

                    if (getAdapterPosition() < selectedItemPosition) {
                        selectedItemPosition--;
                    } else if (getAdapterPosition() == selectedItemPosition) {
                        selectedItemPosition = -1;
                        sendOnTaskSelected(null);
                    }

                    //Remove the task from the list
                    final int position = getAdapterPosition();
                    final ITask task = getTask(position);
                    tasks.remove(position);
                    notifyItemRemoved(position);

                    PrimeNumberFinder.getTaskManager().unregisterTask(task);

                    //Notify listeners
                    onDeletePressed();
                    sendOnDeletePressed(task);
                });
            }

            if (saveButton != null) {
                saveButton.setOnClickListener(v -> {
                    //Save the task if it is savable
                    final ITask task = getTask(getAdapterPosition());
                    if (task instanceof Savable) {
                        Utils.save((Savable) task, context);
                    }

                    saveButton.setEnabled(false);
                    sendOnSavePressed(getTask(getAdapterPosition()));
                });
            }
        }

        public void setTask(ITask task) {
            //Remove task listener from previous task
            if (this.task != null) {
                if (!this.task.removeTaskListener(this)) {
                    Log.w(TAG, "Failed to remove task listener from " + this.task);
                }
            }

            this.task = task;

            //Add task listener to new task
            if (task != null) {

                //Start the UI updater if it hasn't been started yet
                if (uiUpdater.getState() == Task.State.NOT_STARTED) {
                    //holder.uiUpdater.addTaskListener(getUiUpdaterDebugListener(holder));
                    uiUpdater.startOnNewThread();
                    if (task.getState() == Task.State.PAUSED || task.getState() == Task.State.NOT_STARTED || task.getState() == Task.State.STOPPED) {
                        uiUpdater.pause();
                    }
                }

                task.addTaskListener(this);
            }
        }

        protected void onPausePressed() {
        }

        protected void onDeletePressed() {
        }

        @Override
        public void onTaskStarted(final ITask task) {
            handler.post(() -> {
                uiUpdater.resume();
                notifyItemChanged(getAdapterPosition());
            });
        }

        @Override
        public void onTaskPausing(final ITask task) {
            handler.post(() -> {
                notifyItemChanged(getAdapterPosition());
            });
        }

        @Override
        public void onTaskPaused(final ITask task) {
            handler.post(() -> {
                uiUpdater.pause();
                notifyItemChanged(getAdapterPosition());
            });
        }

        @Override
        public void onTaskResuming(final ITask task) {
            handler.post(() -> {
                notifyItemChanged(getAdapterPosition());
            });
        }

        @Override
        public void onTaskResumed(final ITask task) {
            handler.post(() -> {
                uiUpdater.resume();
                notifyItemChanged(getAdapterPosition());
            });
        }

        @Override
        public void onTaskStopping(final ITask task) {
            handler.post(() -> {
                notifyItemChanged(getAdapterPosition());
            });
        }

        @Override
        public void onTaskStopped(final ITask task) {
            handler.post(() -> {
                uiUpdater.pause();
                notifyItemChanged(getAdapterPosition());
            });
        }
    }

    public void setSaved(final ITask task) {
        final int index = tasks.indexOf(task);
        notifyItemChanged(index);
    }

    public void postSetSaved(final ITask task) {
        handler.post(() -> setSaved(task));
    }

    protected boolean isSaved(ITask task) {
        return task instanceof Savable ? ((Savable) task).isSaved() : false;
    }

    public List<ITask> getSavedItems() {
        final List<ITask> savedItems = new ArrayList<>();
        for (T item : tasks) {
            if (isSaved(item)) {
                savedItems.add(item);
            }
        }
        return savedItems;
    }

    public int indexOf(final ITask task) {
        return tasks.indexOf(getItem(task));
    }

    protected void onUpdate(final ViewHolder viewHolder) {

    }

    protected T getItem(final ITask task) {
        for (T item : tasks) {
            if (item == task) {
                return item;
            }
        }
        return null;
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
        for (T task : tasks) {
            if (task.getState() == Task.State.RUNNING) {
                active = true;
                break;
            }
        }

        for (ActionViewListener actionViewListener : this.actionViewListeners) {
            actionViewListener.onTaskStatesChanged(getTaskType(), active);
        }
    }

    protected int getTaskType() {
        return -1;
    }

    public enum Button {
        SAVE,
        PAUSE,
        EDIT,
        DELETE
    }
}
