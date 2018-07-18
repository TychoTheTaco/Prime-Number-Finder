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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class AbstractTaskListAdapter<T extends Task> extends RecyclerView.Adapter<AbstractTaskListAdapter.ViewHolder> implements TaskListener{

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

    protected final Context context;

    protected class Item {
        private final T task;
        private boolean saved;

        public Item(final T task) {
            this.task = task;
        }

        public T getTask() {
            return task;
        }

        public void setSaved(boolean saved) {
            this.saved = saved;
        }

        public boolean isSaved() {
            return saved;
        }
    }

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
        final Item item = items.get(position);
        final Task task = item.getTask();
        customEventListeners.get(task).setViewHolder(holder);

        //Start the UI updater if it hasn't been started yet
        if (holder.uiUpdater.getState() == Task.State.NOT_STARTED) {
            //holder.uiUpdater.addTaskListener(getUiUpdaterDebugListener(holder));
            holder.uiUpdater.startOnNewThread();
            if (task.getState() == Task.State.PAUSED || task.getState() == Task.State.NOT_STARTED || task.getState() == Task.State.STOPPED) {
                holder.uiUpdater.pause();
            }
        }

        //Check if this item should be selected
        holder.itemView.setSelected(holder.getAdapterPosition() == getSelectedItemPosition());

        holder.title.setText(getTitle(item));
        holder.subtitle.setText(getSubtitle(item));
        holder.progress.setText(context.getString(R.string.task_progress, DECIMAL_FORMAT.format(getProgress(item) * 100)));

        //Manage button visibility
        switch (task.getState()) {
            case RUNNING:
                //Pause button
                if (holder.pauseButton != null){
                    holder.pauseButton.setEnabled(true);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }

                //Edit button
                if (holder.editButton != null){
                    holder.editButton.setEnabled(false);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null){
                    holder.deleteButton.setEnabled(false);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null){
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case PAUSING:
                //Pause button
                if (holder.pauseButton != null){
                    holder.pauseButton.setEnabled(false);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }

                //Edit button
                if (holder.editButton != null){
                    holder.editButton.setEnabled(false);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null){
                    holder.deleteButton.setEnabled(false);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null){
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case PAUSED:
                //Pause button
                if (holder.pauseButton != null){
                    holder.pauseButton.setEnabled(true);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                }

                //Edit button
                if (holder.editButton != null){
                    holder.editButton.setEnabled(true);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null){
                    holder.deleteButton.setEnabled(true);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null){
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case RESUMING:
                //Pause button
                if (holder.pauseButton != null){
                    holder.pauseButton.setEnabled(false);
                    holder.pauseButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }

                //Edit button
                if (holder.editButton != null){
                    holder.editButton.setEnabled(false);
                    holder.editButton.setVisibility(View.VISIBLE);
                }

                //Delete button
                if (holder.deleteButton != null){
                    holder.deleteButton.setEnabled(false);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null){
                    holder.saveButton.setVisibility(View.GONE);
                }
                break;

            case STOPPED:
                //Pause button
                if (holder.pauseButton != null){
                    holder.pauseButton.setVisibility(View.GONE);
                }

                //Edit button
                if (holder.editButton != null){
                    holder.editButton.setVisibility(View.GONE);
                }

                //Delete button
                if (holder.deleteButton != null){
                    holder.deleteButton.setEnabled(true);
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }

                //Save button
                if (holder.saveButton != null){
                    holder.saveButton.setEnabled(true);
                    holder.saveButton.setVisibility(View.VISIBLE);
                }

                //Hide progress if task is complete
                if (task.getProgress() == 1){
                    holder.progress.setVisibility(View.GONE);
                }
                break;
        }

        //Show saved
        if (isSaved(task)){
            if (holder.saveButton != null) holder.saveButton.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress.setText(context.getString(R.string.saved));
        }
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

    @Override
    public void onTaskStarted() {
        sendTaskStatesChanged();
    }

    @Override
    public void onTaskPausing() {

    }

    @Override
    public void onTaskPaused() {
        sendTaskStatesChanged();
    }

    @Override
    public void onTaskResuming() {

    }

    @Override
    public void onTaskResumed() {
        sendTaskStatesChanged();
    }

    @Override
    public void onTaskStopping() {

    }

    @Override
    public void onTaskStopped() {
        sendTaskStatesChanged();
    }

    protected CharSequence getTitle(final Item item){
        return getTitle(item.getTask());
    }

    protected CharSequence getTitle(final T task){
        return task.getClass().getSimpleName();
    }

    protected CharSequence getSubtitle(final Item item){
        return getSubtitle(item.getTask());
    }

    protected CharSequence getSubtitle(final T task){
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

    protected float getProgress(final T task){
        return task.getProgress();
    }

    protected float getProgress(final Item item){
        return getProgress(item.getTask());
    }

    public void addTask(final T task) {
        final CustomTaskEventListener customTaskEventListener = new CustomTaskEventListener() {
            @Override
            public void onTaskStarted() {
                handler.post(() -> {
                    if (holder != null) {
                        holder.uiUpdater.resume();
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                });
            }

            @Override
            public void onTaskPausing() {
                handler.post(() -> {
                    if (holder != null) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                });
            }

            @Override
            public void onTaskPaused() {
                handler.post(() -> {
                    if (holder != null) {
                        holder.uiUpdater.pause();
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                });
            }

            @Override
            public void onTaskResuming() {
                handler.post(() -> {
                    if (holder != null) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                });
            }

            @Override
            public void onTaskResumed() {
                handler.post(() -> {
                    if (holder != null) {
                        holder.uiUpdater.resume();
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                });
            }

            @Override
            public void onTaskStopping() {
                handler.post(() -> {
                    if (holder != null) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                });
            }

            @Override
            public void onTaskStopped() {
                handler.post(() -> {
                    if (holder != null) {
                        holder.uiUpdater.pause();
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                });
            }
        };
        task.addTaskListener(customTaskEventListener);
        task.addTaskListener(this);
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
        Collections.sort(items, (item0, item1) -> Long.compare(item0.getTask().getStartTime(), item1.getTask().getStartTime()));
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView title;
        public final TextView subtitle;
        public final TextView progress;
        public ImageButton pauseButton;
        public ImageButton editButton;
        public ImageButton deleteButton;
        public ImageButton saveButton;

        protected final UiUpdater uiUpdater = new UiUpdater(this);

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

            if (!buttons.contains(Button.PAUSE)){
                pauseButton.setVisibility(View.GONE);
                pauseButton = null;
            }
            if (!buttons.contains(Button.EDIT)){
                editButton.setVisibility(View.GONE);
                editButton = null;
            }
            if (!buttons.contains(Button.DELETE)){
                deleteButton.setVisibility(View.GONE);
                deleteButton = null;
            }
            if (!buttons.contains(Button.SAVE)){
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
            if (pauseButton != null){
                pauseButton.setOnClickListener(v -> {
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
                });
            }

            if (editButton != null){
                editButton.setOnClickListener(v -> sendOnEditClicked(getTask(getAdapterPosition())));
            }

            if (deleteButton != null){
                deleteButton.setOnClickListener(v -> {

                    //PAUSE the UI updater. It will be re-used by other ViewHolders
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
                });
            }

            if (saveButton != null){
                saveButton.setOnClickListener(v -> {
                    saveButton.setEnabled(false);
                    sendOnSavePressed(getTask(getAdapterPosition()));
                });
            }
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
        handler.post(() -> setSaved(task, isSaved));
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

                handler.post(() -> {
                    //Make sure the view holder is still visible
                    if (viewHolder.getAdapterPosition() != -1) {
                        onUpdate(viewHolder);
                        notifyItemChanged(viewHolder.getAdapterPosition());
                    }else{
                        Log.e(TAG, "Posted an invalid update on " + viewHolder);
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

    protected int getTaskType(){
        return -1;
    }

    public enum Button{
        SAVE,
        PAUSE,
        EDIT,
        DELETE
    }
}
