package com.tycho.app.primenumberfinder.modules;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import easytasks.Task;
import easytasks.TaskListener;


/**
 * Created by tycho on 12/12/2017.
 */

public abstract class AbstractTaskListAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "AbstractTaskListAdapter";

    protected final List<Task> tasks = new ArrayList<>();

    private int selectedItemPosition;

    /**
     * Event listeners.
     */
    private final List<EventListener> eventListeners = new ArrayList<>();

    private Map<Task, CustomTaskEventListener> customEventListeners = new HashMap<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private long lastUiUpdateTime = 0;

    @Override
    public void onBindViewHolder(T holder, int position){
        final Task task = tasks.get(position);
        customEventListeners.get(task).setViewHolder(holder);
        doOnBindViewHolder(holder, position);
    }

    protected abstract void doOnBindViewHolder(RecyclerView.ViewHolder holder, int position);

    protected abstract class CustomTaskEventListener implements TaskListener{
        protected T holder;

        private void setViewHolder(final T holder){
            this.holder = holder;
        }
    }

    public void addTask(final Task task){
        if (!tasks.contains(task)){
            final CustomTaskEventListener customTaskEventListener = new CustomTaskEventListener(){
                @Override
                public void onTaskStarted(){

                }

                @Override
                public void onTaskPaused(){
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            if (holder != null){
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        }
                    });
                }

                @Override
                public void onTaskResumed(){
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            if (holder != null){
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        }
                    });
                }

                @Override
                public void onTaskStopped(){
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            if (holder != null){
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        }
                    });
                }

                @Override
                public void onProgressChanged(float v){
                    if (System.currentTimeMillis() - lastUiUpdateTime >= PrimeNumberFinder.UPDATE_LIMIT_MS * 2){
                        handler.post(new Runnable(){
                            @Override
                            public void run(){
                                if (holder != null){
                                    notifyItemChanged(holder.getAdapterPosition());
                                }
                            }
                        });
                        lastUiUpdateTime = System.currentTimeMillis();
                    }
                }
            };
            task.addTaskListener(customTaskEventListener);
            this.tasks.add(task);
            notifyItemInserted(getItemCount());
            customEventListeners.put(task, customTaskEventListener);
        }
    }

    public void setSelected(int index){
        final int changed = selectedItemPosition;
        selectedItemPosition = index;
        notifyItemChanged(selectedItemPosition);
        if (changed != -1) notifyItemChanged(changed);

        if (selectedItemPosition == -1){
            sendOnTaskSelected(null);
        }else{
            sendOnTaskSelected(tasks.get(selectedItemPosition));
        }
    }

    public void setSelected(final Task task){
        setSelected(tasks.indexOf(task));
    }

    public int getSelectedItemPosition(){
        return selectedItemPosition;
    }

    public interface EventListener{
        void onTaskSelected(final Task task);

        void onPausePressed(final Task task);

        void onTaskRemoved(final Task task);
    }

    public void addEventListener(final EventListener eventListener){
        if (!eventListeners.contains(eventListener)) eventListeners.add(eventListener);
    }

    public boolean removeEventListener(final EventListener eventListener){
        return eventListeners.remove(eventListener);
    }

    protected void sendOnTaskSelected(final Task task){
        for (EventListener eventListener : eventListeners){
            eventListener.onTaskSelected(task);
        }
    }

    protected void sendOnPausePressed(final Task task){
        for (EventListener eventListener : eventListeners){
            eventListener.onPausePressed(task);
        }
    }

    protected void sendOnDeletePressed(final Task task){
        for (EventListener eventListener : eventListeners){
            eventListener.onTaskRemoved(task);
        }
    }

    public class AbstractTaskListItemViewHolder extends RecyclerView.ViewHolder{

        public final ViewGroup root;
        public final TextView title;
        public final TextView state;
        public final TextView progress;
        public final ImageButton pauseButton;
        public final ImageButton deleteButton;

        public AbstractTaskListItemViewHolder(View itemView){
            super(itemView);

            root = itemView.findViewById(R.id.root);
            title = itemView.findViewById(R.id.title);
            state = itemView.findViewById(R.id.state);
            progress = itemView.findViewById(R.id.textView_search_progress);
            pauseButton = itemView.findViewById(R.id.pause_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

            root.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    if (selectedItemPosition == getAdapterPosition()){
                        setSelected(null);
                    }else{
                        setSelected(getAdapterPosition());
                    }
                }
            });

            //Set button listeners
            pauseButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    switch (tasks.get(getAdapterPosition()).getState()){

                        case PAUSED:
                            tasks.get(getAdapterPosition()).resume();
                            break;

                        case RUNNING:
                            tasks.get(getAdapterPosition()).pause();
                            break;
                    }

                    sendOnPausePressed(tasks.get(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    tasks.get(getAdapterPosition()).stop();

                    if (getAdapterPosition() < selectedItemPosition){
                        selectedItemPosition--;
                    }else if (getAdapterPosition() == selectedItemPosition){
                        selectedItemPosition = -1;
                        sendOnTaskSelected(null);
                    }

                    //Remove the task from the list
                    final Task task = tasks.get(getAdapterPosition());
                    customEventListeners.remove(task);
                    tasks.remove(task);
                    notifyItemRemoved(getAdapterPosition());

                    //Notify listeners
                    sendOnDeletePressed(task);
                }
            });
        }
    }
}