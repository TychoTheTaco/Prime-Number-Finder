package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SelectableAdapter<H extends SelectableAdapter.ViewHolder> extends RecyclerView.Adapter<H> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SelectableAdapter.class.getSimpleName();

    /**
     * When {@code true}, this adapter is in selection mode. This means that an item will be selected
     * simply by tapping it, as opposed to long pressing it.
     */
    private boolean selectionMode = false;

    /**
     * This keeps track of which indexes have been selected.
     */
    private final SparseBooleanArray selectedItemIndexes = new SparseBooleanArray();

    /**
     * The number of items currently selected.
     */
    private int selectedItemCount = 0;

    public interface OnSelectionStateChangedListener {

        /**
         * Called when the user has started to select multiple items.
         */
        void onStartSelection();

        /**
         * Called when an item is selected.
         */
        void onItemSelected();

        /**
         * Called when an item is deselected.
         */
        void onItemDeselected();

        /**
         * Called when the user has stopped selectionMode items.
         */
        void onStopSelection();
    }

    private final CopyOnWriteArrayList<OnSelectionStateChangedListener> onSelectionStateChangedListeners = new CopyOnWriteArrayList<>();

    @NonNull
    public abstract H onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(@NonNull H holder, int position);

    public abstract int getItemCount();

    public int getSelectedItemCount() {
        return selectedItemCount;
    }

    public synchronized void setSelectionMode(boolean selectionMode) {
        final boolean prevState = this.selectionMode;
        this.selectionMode = selectionMode;

        if (!prevState && selectionMode) {
            sendOnStartSelection();
        }else if (prevState && !selectionMode){
            //Update all selected items
            for (int i = 0; i < getItemCount(); i++){
                if (selectedItemIndexes.get(i)){
                    selectedItemIndexes.put(i, false);
                    notifyItemChanged(i);
                }
            }
            selectedItemCount = 0;
            sendOnStopSelection();
        }
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public int[] getSelectedItemIndexes(){
        final int[] indexes = new int[selectedItemCount];
        int position = 0;
        for (int i = 0; i < getItemCount(); i++){
            if (selectedItemIndexes.get(i)){
                indexes[position++] = i;
            }
        }
        return indexes;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder.this.onClick(v);
                    if (selectionMode) {
                        setSelected(!isSelected());
                        if (selectedItemCount == 0) {
                            setSelectionMode(false);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!selectionMode) {
                        setSelectionMode(true);
                        setSelected(true);
                        return true;
                    }
                    return ViewHolder.this.onLongClick(v);
                }
            });
        }

        protected void onClick(View view){}

        protected boolean onLongClick(View view){
            return false;
        }

        private void setSelected(final boolean selected) {
            if (selected && !isSelected()) {
                selectedItemCount++;
                sendOnItemSelected();
            } else if (!selected && isSelected()) {
                selectedItemCount--;
                sendOnItemDeselected();
            }
            selectedItemIndexes.put(getAdapterPosition(), selected);
            notifyItemChanged(getAdapterPosition());
        }

        public boolean isSelected() {
            return selectedItemIndexes.get(getAdapterPosition());
        }
    }

    public void addOnSelectionStateChangedListener(final OnSelectionStateChangedListener listener){
        if (!onSelectionStateChangedListeners.contains(listener)){
            onSelectionStateChangedListeners.add(listener);
        }
    }

    private void sendOnStartSelection(){
        for (OnSelectionStateChangedListener listener : onSelectionStateChangedListeners){
            listener.onStartSelection();
        }
    }

    private void sendOnItemSelected(){
        for (OnSelectionStateChangedListener listener : onSelectionStateChangedListeners){
            listener.onItemSelected();
        }
    }

    private void sendOnItemDeselected(){
        for (OnSelectionStateChangedListener listener : onSelectionStateChangedListeners){
            listener.onItemDeselected();
        }
    }

    private void sendOnStopSelection(){
        for (OnSelectionStateChangedListener listener : onSelectionStateChangedListeners){
            listener.onStopSelection();
        }
    }
}
