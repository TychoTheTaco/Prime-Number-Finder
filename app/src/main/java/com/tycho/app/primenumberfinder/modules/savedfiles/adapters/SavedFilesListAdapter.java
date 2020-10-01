package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tycho Bellers
 *         Date Created: 11/8/2016
 */

public class SavedFilesListAdapter extends SelectableAdapter<SavedFilesListAdapter.ViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesListAdapter.class.getSimpleName();

    /**
     * The current directory whose files are listed in the adapter.
     */
    protected final File directory;

    /**
     * List of data files currently in the adapter.
     */
    protected final List<File> files = new ArrayList<>();

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    public SavedFilesListAdapter(final Context context, final File directory) {
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

        //Set file name
        switch (FileManager.getFileType(directory)) {
            default:
                holder.fileName.setText(file.getName());
                break;

            case PRIMES:
                try {
                    final FileManager.PrimesFile primesFile = new FileManager.PrimesFile(file);
                    holder.fileName.setText(primesFile.getTitle());
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;

            case FACTORS:
                try {
                    final FileManager.FactorsFile factorsFile = new FileManager.FactorsFile(file);
                    holder.fileName.setText(factorsFile.getTitle());
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;

            case TREE:
                try {
                    final FileManager.TreeFile treeFile = new FileManager.TreeFile(file);
                    holder.fileName.setText(treeFile.getTitle());
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
        }

        //Format icon
        if (!holder.isSelected()){
            switch (FileManager.getFileType(directory)) {
                case PRIMES:
                    holder.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple)));
                    holder.icon.setImageResource(R.drawable.find_primes_icon);
                    break;

                case FACTORS:
                    holder.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange)));
                    holder.icon.setImageResource(R.drawable.find_factors_icon);
                    break;

                case TREE:
                    holder.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.green)));
                    holder.icon.setImageResource(R.drawable.prime_factorization_icon);
                    break;
            }
        }

        if (holder.isSelected()){
            holder.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent)));
            holder.fileSize.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_text));
        }else{
            holder.fileSize.setTextColor(Color.parseColor("#bebebe"));
        }

        holder.dateCreated.setText(simpleDateFormat.format(new Date(file.lastModified())));
        holder.fileSize.setText(Utils.humanReadableByteCount(file.length(), true).replaceAll("\\.\\d+", ""));

        holder.itemView.setSelected(holder.isSelected());
    }

    public long getTotalStorage(){
        long size = 0;
        for (File file : files){
            size += file.length();
        }
        return size;
    }

    public long getSelectedSize(){
        long size = 0;
        for (int i : getSelectedItemIndexes()){
            size += files.get(i).length();
        }
        return size;
    }

    public void refresh(){
        files.clear();
        files.addAll(Arrays.asList(directory.listFiles()));
        Utils.sortByDate(files, false);
        notifyDataSetChanged();
    }

    public void sortDate(final boolean ascending){
        Utils.sortByDate(files, ascending);
        notifyDataSetChanged();
    }

    public void sortSize(final boolean ascending){
        Utils.sortBySize(files, ascending);
        notifyDataSetChanged();
    }

    public void sortSearchRange(final boolean ascending){
        Collections.sort(files, (file0, file1) -> (ascending ? 1 : -1) * Long.compare(getRangeFromFileName(file0)[0], getRangeFromFileName(file1)[0]));
        notifyDataSetChanged();
    }

    public void sortNumber(final boolean ascending){
        Collections.sort(files, (file0, file1) -> (ascending ? 1 : -1) * Long.compare(getNumberFromFileName(file0), getNumberFromFileName(file1)));
        notifyDataSetChanged();
    }

    private long[] getRangeFromFileName(final File file){
        final long[] range = new long[2];

        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(file.getName());
        for (int i = 0; i < range.length; i++){
            matcher.find();
            range[i] = Long.valueOf(matcher.group());
        }

        return range;
    }

    private long getNumberFromFileName(final File file){
        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(file.getName());
        matcher.find();
        return Long.valueOf(matcher.group());
    }

    public List<File> getFiles() {
        return files;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends SelectableAdapter.ViewHolder {

        private final TextView fileName;
        private final TextView dateCreated;
        private final TextView fileSize;
        protected final ImageView icon;

        private final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.25f, 1.0f, 1.25f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        ViewHolder(final View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            fileName = itemView.findViewById(R.id.file_name);
            dateCreated =  itemView.findViewById(R.id.textView_dateCreated);
            fileSize = itemView.findViewById(R.id.file_size);

            scaleAnimation.setDuration(75);
            scaleAnimation.setRepeatCount(1);
            scaleAnimation.setRepeatMode(Animation.REVERSE);

            icon.setOnClickListener(v -> {
                icon.startAnimation(scaleAnimation);
                if (addToSelection(true)){
                    icon.setImageResource(R.drawable.round_check_24);
                }
            });
        }

        @Override
        protected void onClick(View view) {
            if (!isSelectionMode()){
                final FileType fileType = FileManager.getFileType(directory);
                if (fileType.getOpeningClass() != null){
                    final Context context = itemView.getContext();
                    final Intent intent = new Intent(context, fileType.getOpeningClass());
                    intent.putExtra("filePath", files.get(getAdapterPosition()).getAbsolutePath());
                    intent.putExtra("allowExport", true);
                    intent.putExtra("enableSearch", true);
                    intent.putExtra("allowDelete", true);
                    context.startActivity(intent);
                }
            }else{
                icon.startAnimation(scaleAnimation);
                if (!isSelected()){
                    icon.setImageResource(R.drawable.round_check_24);
                }
            }
        }

        @Override
        protected boolean onLongClick(View view) {
            icon.startAnimation(scaleAnimation);
            if (isSelected()){
                icon.setImageResource(R.drawable.round_check_24);
            }
            return super.onLongClick(view);
        }
    }
}
