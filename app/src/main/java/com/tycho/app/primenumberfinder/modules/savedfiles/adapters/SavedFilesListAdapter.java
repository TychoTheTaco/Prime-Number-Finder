package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 11/8/2016
 */

public class SavedFilesListAdapter extends SelectableAdapter<SavedFilesListAdapter.ViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesListAdapter.class.getSimpleName();

    private final List<File> files = new ArrayList<>();

    private Context context;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    private final File directory;

    private ColorStateList iconBackgroundTintList;
    private String iconText;

    public SavedFilesListAdapter(final Context context, final File directory) {
        this.context = context;
        this.directory = directory;

        switch (FileManager.getFileType(directory)) {
            default:
                iconText = "?";
                break;

            case PRIMES:
                iconBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple));
                iconText = "P";
                break;

            case FACTORS:
                iconBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange));
                iconText = "F";
                break;

            case TREE:
                iconBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green));
                iconText = "T";
                break;
        }

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

        //Format icon
        holder.icon.setText(iconText);
        holder.icon.setBackgroundTintList(iconBackgroundTintList);
        holder.iconImage.setVisibility(holder.isSelected() ? View.VISIBLE : View.GONE);

        //Set file name
        holder.fileName.setText(file.getName());

        if (holder.isSelected()){
            holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent)));
            holder.icon.setText("");
            holder.fileSize.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
        }else{
            holder.fileSize.setTextColor(Color.parseColor("#bebebe"));
        }

        holder.dateCreated.setText(simpleDateFormat.format(new Date(file.lastModified())));
        holder.fileSize.setText(Utils.humanReadableByteCount(file.length(), true));

        holder.itemView.setSelected(holder.isSelected());
    }

    public void refresh(){
        files.clear();
        files.addAll(Arrays.asList(directory.listFiles()));
        Utils.sortByDate(files, false);
        notifyDataSetChanged();
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
        protected final TextView icon;
        private final ImageView iconImage;

        private final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.25f, 1.0f, 1.25f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        ViewHolder(final View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            iconImage = itemView.findViewById(R.id.icon_image);
            fileName = itemView.findViewById(R.id.file_name);
            dateCreated =  itemView.findViewById(R.id.textView_dateCreated);
            fileSize = itemView.findViewById(R.id.file_size);

            iconImage.setImageResource(R.drawable.round_check_24);
            iconImage.setVisibility(View.GONE);

            scaleAnimation.setDuration(75);
            scaleAnimation.setRepeatCount(1);
            scaleAnimation.setRepeatMode(Animation.REVERSE);

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon.startAnimation(scaleAnimation);
                    iconImage.setVisibility(addToSelection(true) ? View.VISIBLE : View.GONE);
                }
            });
        }

        @Override
        protected void onClick(View view) {
            if (!isSelectionMode()){
                final FileType fileType = FileManager.getFileType(directory);
                if (fileType.getOpeningClass() != null){
                    final Intent intent = new Intent(context, fileType.getOpeningClass());
                    intent.putExtra("filePath", files.get(getAdapterPosition()).getAbsolutePath());
                    intent.putExtra("allowExport", true);
                    intent.putExtra("enableSearch", true);
                    intent.putExtra("allowDelete", true);
                    intent.putExtra("title", true);
                    context.startActivity(intent);
                }
            }
        }
    }
}
