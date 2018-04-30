package com.tycho.app.primenumberfinder.modules;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import easytasks.Task;
import easytasks.TaskAdapter;


/**
 * Created by tycho on 12/12/2017.
 */

public abstract class AbstractTaskListAdapter<T extends AbstractTaskListAdapter.ViewHolder> extends RecyclerView.Adapter<T> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "AbstractTaskListAdapter";

    /**
     * List of tasks in the adapter.
     */
    protected final List<Task> tasks = new ArrayList<>();

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

    @Override
    public void onBindViewHolder(final AbstractTaskListAdapter.ViewHolder holder, int position) {
        final Task task = tasks.get(position);
        customEventListeners.get(task).setViewHolder(holder);
        if (holder.uiUpdater.getState() == Task.State.NOT_STARTED) {
            holder.uiUpdater.startOnNewThread();

            if (task.getState() == Task.State.PAUSED || task.getState() == Task.State.NOT_STARTED || task.getState() == Task.State.STOPPED){
                holder.uiUpdater.pause(false);
            }
        }
        doOnBindViewHolder(holder, position);
    }

    protected abstract void doOnBindViewHolder(RecyclerView.ViewHolder holder, int position);

    public void addTask(final Task task) {
        if (!tasks.contains(task)) {
            final CustomTaskEventListener customTaskEventListener = new CustomTaskEventListener() {
                @Override
                public void onTaskStarted() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (holder != null) {
                                holder.uiUpdater.resume(false);
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
                                holder.uiUpdater.pause(false);
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
                                holder.uiUpdater.resume(false);
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
                                holder.uiUpdater.pause(false);
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        }
                    });
                }
            };
            task.addTaskListener(customTaskEventListener);
            task.addTaskListener(taskAdapter);
            this.tasks.add(task);
            notifyItemInserted(getItemCount());
            customEventListeners.put(task, customTaskEventListener);
        }
    }

    public void setSelected(int index) {
        if (index < getItemCount()){
            final int changed = selectedItemPosition;
            selectedItemPosition = index;
            notifyItemChanged(selectedItemPosition);
            if (changed != -1) notifyItemChanged(changed);

            if (selectedItemPosition == -1) {
                sendOnTaskSelected(null);
            } else {
                sendOnTaskSelected(tasks.get(selectedItemPosition));
            }
        }
    }

    public void setSelected(final Task task) {
        setSelected(tasks.indexOf(task));
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void sortByTimeCreated(){
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task task0, Task task1) {
                return Long.compare(task0.getStartTime(), task1.getStartTime());
            }
        });
        notifyDataSetChanged();
    }

    public interface EventListener {
        void onTaskSelected(final Task task);

        void onPausePressed(final Task task);

        void onTaskRemoved(final Task task);

        void onEditPressed(final Task task);
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

    private void sendOnEditClicked(final Task task){
        for (EventListener eventListener : eventListeners){
            eventListener.onEditPressed(task);
        }
    }

    private void sendOnDeletePressed(final Task task) {
        for (EventListener eventListener : eventListeners) {
            eventListener.onTaskRemoved(task);
        }
    }

    protected abstract class CustomTaskEventListener extends TaskAdapter {
        protected AbstractTaskListAdapter.ViewHolder holder;

        private void setViewHolder(final AbstractTaskListAdapter.ViewHolder holder) {
            this.holder = holder;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final ViewGroup root;
        public final TextView title;
        public final TextView state;
        public final TextView progress;
        public final ImageButton pauseButton;
        public final ImageButton editButton;
        public final ImageButton deleteButton;

        private final UiUpdater uiUpdater = new UiUpdater(this);

        public ViewHolder(View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            title = itemView.findViewById(R.id.title);
            state = itemView.findViewById(R.id.state);
            progress = itemView.findViewById(R.id.textView_search_progress);
            pauseButton = itemView.findViewById(R.id.pause_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

            root.setOnClickListener(new View.OnClickListener() {
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
                    switch (tasks.get(getAdapterPosition()).getState()) {

                        case PAUSED:
                            tasks.get(getAdapterPosition()).resume(false);
                            break;

                        case RUNNING:
                            tasks.get(getAdapterPosition()).pause(false);
                            break;
                    }

                    sendOnPausePressed(tasks.get(getAdapterPosition()));
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendOnEditClicked(tasks.get(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Pause the UI updater. It will be re-used by other ViewHolders
                    tasks.get(getAdapterPosition()).pause(false);

                    if (getAdapterPosition() < selectedItemPosition) {
                        selectedItemPosition--;
                    } else if (getAdapterPosition() == selectedItemPosition) {
                        selectedItemPosition = -1;
                        sendOnTaskSelected(null);
                    }

                    //Remove the task from the list
                    final Task task = tasks.get(getAdapterPosition());
                    customEventListeners.remove(task);
                    tasks.remove(task);
                    notifyItemRemoved(getAdapterPosition());

                    PrimeNumberFinder.getTaskManager().unregisterTask(task);

                    //Notify listeners
                    sendOnDeletePressed(task);
                }
            });
        }
    }

    protected void onUpdate(final ViewHolder viewHolder) {
        notifyItemChanged(viewHolder.getAdapterPosition());
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
                if (shouldStop()){
                    break;
                }
            }
        }
    }

    public void addActionViewListener(final ActionViewListener actionViewListener) {
        if (!actionViewListeners.contains(actionViewListener)){
            this.actionViewListeners.add(actionViewListener);
        }
    }

    public boolean removeActionViewListener(final ActionViewListener actionViewListener) {
        return this.actionViewListeners.remove(actionViewListener);
    }

    private void sendTaskStatesChanged() {
        boolean active = false;
        for (Task task : this.tasks) {
            if (task.getState() == Task.State.RUNNING) {
                active = true;
                break;
            }
        }

        for (ActionViewListener actionViewListener : this.actionViewListeners) {
            actionViewListener.onTaskStatesChanged(active);
        }
    }
}
