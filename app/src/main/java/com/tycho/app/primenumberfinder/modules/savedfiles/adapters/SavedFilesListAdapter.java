package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.SavedFilesListActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 11/8/2016
 */

public class SavedFilesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "SavedFilesFrgmntAdapter";

    private final List<File> files = new ArrayList<>();
    private final SparseBooleanArray selectedPositions = new SparseBooleanArray();
    private int selectedCount = 0;
    private boolean isSelecting = false;

    private final FileType fileType;

    private Context context;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    public SavedFilesListAdapter(final Context context, final FileType fileType) {
        this.fileType = fileType;

        this.context = context;

        switch (fileType) {
            case PRIMES:
                files.addAll(Arrays.asList(FileManager.getInstance().getSavedPrimesDirectory().listFiles()));
                break;
            case FACTORS:
                files.addAll(Arrays.asList(FileManager.getInstance().getSavedFactorsDirectory().listFiles()));
                break;
            case TREE:
                files.addAll(Arrays.asList(FileManager.getInstance().getSavedTreesDirectory().listFiles()));
                break;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_files_header, parent, false));
            case 1:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_saved_file_large, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            bindHeader((HeaderViewHolder) holder, position);
        } else if (holder instanceof ItemViewHolder) {
            bindItem((ItemViewHolder) holder, position);
        }
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

    private void bindHeader(final HeaderViewHolder viewHolder, final int position) {
        switch (fileType) {

            case PRIMES:
                viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.purple_dark));
                viewHolder.subTitle.setTextColor(ContextCompat.getColor(context, R.color.purple_dark));
                viewHolder.title.setText("Prime numbers");
                break;

            case FACTORS:
                viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.orange_dark));
                viewHolder.subTitle.setTextColor(ContextCompat.getColor(context, R.color.orange_dark));
                viewHolder.title.setText("Factors");
                break;

            case TREE:
                viewHolder.title.setTextColor(ContextCompat.getColor(context, R.color.green_dark));
                viewHolder.subTitle.setTextColor(ContextCompat.getColor(context, R.color.green_dark));
                viewHolder.title.setText("Factor Trees");
                break;
        }

        viewHolder.subTitle.setText("You have " + files.size() + " saved files." + (isSelecting ? (" Selected " + selectedCount + " items.") : ""));
    }

    private void bindItem(final ItemViewHolder viewHolder, final int position) {
        final File file = files.get(position - 1);
        final String fileName = file.getName().replace((fileType == FileType.TREE ? FileManager.TREE_EXTENSION : FileManager.EXTENSION), "");

        viewHolder.fileName.setText(formatTitle(fileName));

        switch (fileType) {

            case PRIMES:
                viewHolder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.purple)));
                viewHolder.icon.setText("P");
                break;

            case FACTORS:
                viewHolder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.orange)));
                viewHolder.icon.setText("F");
                break;

            case TREE:
                viewHolder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.green)));
                viewHolder.icon.setText("T");
                break;
        }

        if (selectedPositions.get(position)){
            viewHolder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.accent)));
            viewHolder.icon.setText("");
            viewHolder.fileSize.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
        }else{
            viewHolder.fileSize.setTextColor(Color.parseColor("#bebebe"));
        }

        viewHolder.dateCreated.setText(simpleDateFormat.format(new Date(file.lastModified())));

        viewHolder.fileSize.setText(humanReadableByteCount(file.length(), true));

        viewHolder.itemView.setSelected(selectedPositions.get(viewHolder.getAdapterPosition(), false));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSelecting) {
                    selectedPositions.put(viewHolder.getAdapterPosition(), !selectedPositions.get(viewHolder.getAdapterPosition(), false));
                    if (selectedPositions.get(viewHolder.getAdapterPosition(), false)) {
                        viewHolder.itemView.setSelected(true);
                        selectedCount++;
                    } else {
                        viewHolder.itemView.setSelected(false);
                        selectedCount--;
                    }
                    if (selectedCount == 0) {
                        setSelecting(false);
                    }
                    notifyItemChanged(0);
                    notifyItemChanged(viewHolder.getAdapterPosition());
                } else {
                    Intent intent;
                    switch (fileType) {
                        case PRIMES:
                            intent = new Intent(context, DisplayPrimesActivity.class);
                            intent.putExtra("filePath", file.getAbsolutePath());
                            intent.putExtra("title", true);
                            context.startActivity(intent);
                            break;

                        case FACTORS:
                            intent = new Intent(context, DisplayFactorsActivity.class);
                            intent.putExtra("filePath", file.getAbsolutePath());
                            intent.putExtra("title", true);
                            context.startActivity(intent);
                            break;

                        case TREE:
                            intent = new Intent(context, DisplayPrimeFactorizationActivity.class);
                            intent.putExtra("filePath", file.getAbsolutePath());
                            intent.putExtra("title", true);
                            context.startActivity(intent);
                            break;
                    }
                }

            }
        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isSelecting) {
                    selectedPositions.put(viewHolder.getAdapterPosition(), true);
                    viewHolder.itemView.setSelected(true);
                    selectedCount++;
                    notifyItemChanged(0);
                    notifyItemChanged(viewHolder.getAdapterPosition());
                    setSelecting(true);
                    return true;
                }
                return false;
            }
        });
    }

    public void sortByDate() {
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file0, File file1) {
                return (int) (file1.lastModified() - file0.lastModified());
            }
        });
    }

    private String formatTitle(final String string) {

        //Replace all the numbers
        String replaceNumbers = string.replaceAll("[0-9]+", "<number>");

        //Replace all the text
        String onlyNumbers = string.replaceAll("[^0-9]+", "<text>");

        //Get all numbers from the string
        try {
            String numbers[] = onlyNumbers.trim().split("<text>");
            final List<Long> formattedNumbers = new ArrayList<>();
            for (String numberString : numbers) {
                if (!numberString.equals("")) {
                    formattedNumbers.add(Long.valueOf(numberString));
                }
            }

            //Replace all place holders with formatted numbers
            String title = replaceNumbers;
            for (int i = 0; i < formattedNumbers.size(); i++) {
                title = title.replaceFirst("<number>", NumberFormat.getInstance().format(formattedNumbers.get(i)));
            }
            return title;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return string;
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
        return files.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView fileName;
        private final TextView dateCreated;
        private final TextView fileSize;
        protected final TextView icon;

        ItemViewHolder(final View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            fileName = itemView.findViewById(R.id.file_name);
            dateCreated =  itemView.findViewById(R.id.textView_dateCreated);
            fileSize = itemView.findViewById(R.id.file_size);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subTitle;

        HeaderViewHolder(final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subtitle);
        }
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
