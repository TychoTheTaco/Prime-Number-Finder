package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilesListAdapter extends RecyclerView.Adapter<FilesListAdapter.ViewHolder>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FilesListAdapter.class.getSimpleName();

    protected final File directory;

    protected final List<File> files = new ArrayList<>();

    protected final Context context;

    public FilesListAdapter(final File directory, final Context context){
        this.directory = directory;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_saved_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final File file = files.get(position);

        holder.fileName.setText(Utils.formatTitle(file));

        switch (FileManager.getFileType(directory)){
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
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    /**
     * Refresh the data in this adapter. This will clear the current files list and add all files
     * from the current directory.
     */
    public synchronized void refresh(){
        files.clear();
        files.addAll(Arrays.asList(directory.listFiles()));
        Utils.sortByDate(files, false);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView icon;
        private final TextView fileName;

        ViewHolder(final View itemView){
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            fileName = itemView.findViewById(R.id.file_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
            });
        }
    }
}
