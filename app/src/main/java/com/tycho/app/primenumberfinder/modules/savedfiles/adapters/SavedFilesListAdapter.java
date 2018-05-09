package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.SavedFilesListActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 11/8/2016
 */

public class SavedFilesListAdapter extends RecyclerView.Adapter<SavedFilesListAdapter.ViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesListAdapter.class.getSimpleName();

    private final List<File> files = new ArrayList<>();
    private final SparseBooleanArray selectedPositions = new SparseBooleanArray();
    private int selectedCount = 0;
    private boolean isSelecting = false;

    private Context context;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    private final File directory;

    public SavedFilesListAdapter(final Context context, final File directory) {
        this.context = context;
        this.directory = directory;
        refresh();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_file_list_item_large, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final File file = files.get(position);

        holder.fileName.setText(file.getName());

        switch (FileManager.getFileType(directory)) {
            default:
                holder.icon.setText("?");
                break;

            case PRIMES:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple)));
                holder.icon.setText("P");
                break;

            case FACTORS:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange)));
                holder.icon.setText("F");
                break;

            case TREE:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.green)));
                holder.icon.setText("T");
                break;
        }

        if (selectedPositions.get(position)){
            holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent)));
            holder.icon.setText("");
            holder.fileSize.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
        }else{
            holder.fileSize.setTextColor(Color.parseColor("#bebebe"));
        }

        holder.dateCreated.setText(simpleDateFormat.format(new Date(file.lastModified())));

        holder.fileSize.setText(humanReadableByteCount(file.length(), true));

        holder.itemView.setSelected(selectedPositions.get(holder.getAdapterPosition(), false));
    }

    public void refresh(){
        files.clear();
        files.addAll(Arrays.asList(directory.listFiles()));
        Utils.sortByDate(files, false);
        notifyDataSetChanged();
    }

    public void setSelecting(boolean selecting) {
        isSelecting = selecting;
        if (!selecting) {
            selectedPositions.clear();
            selectedCount = 0;
        }
        SavedFilesListActivity.setDeleteVisibility(selecting);
    }

    public boolean isSelecting() {
        return isSelecting;
    }

    public void deleteSelected(){
        if (isSelecting){
            final Iterator<File> iterator = files.iterator();
            int index = 1;
            while (iterator.hasNext()){
                final File file = iterator.next();
                if (selectedPositions.get(index)){
                    if (!file.delete()){
                        Log.w(TAG, "Failed to delete file: " + file);
                    }
                    iterator.remove();
                }
                index++;
            }
            setSelecting(false);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView fileName;
        private final TextView dateCreated;
        private final TextView fileSize;
        protected final TextView icon;

        ViewHolder(final View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            fileName = itemView.findViewById(R.id.file_name);
            dateCreated =  itemView.findViewById(R.id.textView_dateCreated);
            fileSize = itemView.findViewById(R.id.file_size);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isSelecting) {
                        selectedPositions.put(getAdapterPosition(), !selectedPositions.get(getAdapterPosition(), false));
                        if (selectedPositions.get(getAdapterPosition(), false)) {
                            itemView.setSelected(true);
                            selectedCount++;
                        } else {
                            itemView.setSelected(false);
                            selectedCount--;
                        }
                        if (selectedCount == 0) {
                            setSelecting(false);
                        }
                        notifyItemChanged(0);
                        notifyItemChanged(getAdapterPosition());
                    } else {
                        Intent intent;
                        switch (FileManager.getFileType(directory)) {
                            case PRIMES:
                                intent = new Intent(context, DisplayPrimesActivity.class);
                                intent.putExtra("filePath", files.get(getAdapterPosition()).getAbsolutePath());
                                intent.putExtra("allowExport", true);
                                intent.putExtra("enableSearch", true);
                                intent.putExtra("allowDelete", true);
                                intent.putExtra("title", true);
                                context.startActivity(intent);
                                break;

                            case FACTORS:
                                intent = new Intent(context, DisplayFactorsActivity.class);
                                intent.putExtra("filePath", files.get(getAdapterPosition()).getAbsolutePath());
                                intent.putExtra("allowExport", true);
                                intent.putExtra("allowDelete", true);
                                intent.putExtra("title", true);
                                context.startActivity(intent);
                                break;

                            case TREE:
                                intent = new Intent(context, DisplayPrimeFactorizationActivity.class);
                                intent.putExtra("filePath", files.get(getAdapterPosition()).getAbsolutePath());
                                intent.putExtra("allowExport", true);
                                intent.putExtra("allowDelete", true);
                                intent.putExtra("title", true);
                                context.startActivity(intent);
                                break;
                        }
                    }

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isSelecting) {
                        selectedPositions.put(getAdapterPosition(), true);
                        itemView.setSelected(true);
                        selectedCount++;
                        notifyItemChanged(0);
                        notifyItemChanged(getAdapterPosition());
                        setSelecting(true);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public interface OnSelectionStateChangedListener{

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
         * Called when the user has stopped selecting items.
         */
        void onStopSelection();
    }
}
