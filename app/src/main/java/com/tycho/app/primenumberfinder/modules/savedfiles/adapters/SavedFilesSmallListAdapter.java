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
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 11/3/2016
 */

public class SavedFilesSmallListAdapter extends RecyclerView.Adapter<SavedFilesSmallListAdapter.ViewHolder>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesSmallListAdapter.class.getSimpleName();

    private final File directory;

    private final List<File> files = new ArrayList<>();

    private Context context;

    public SavedFilesSmallListAdapter(final Context context, final File directory){
        this.context = context;
        this.directory = directory;
        refresh();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_saved_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final File file = files.get(position);

        holder.fileName.setText(file.getName());

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

    private String formatTitle(final String string){

        try{
            //Replace all the numbers
            String replaceNumbers = string.replaceAll("[0-9]+", "<number>");

            //Replace all the text
            String onlyNumbers = string.replaceAll("[^0-9]+", "<text>");

            //Get all numbers from the string
            String numbers[] = onlyNumbers.trim().split("<text>");
            final List<Long> formattedNumbers = new ArrayList<>();
            for (String numberString : numbers){
                if (!numberString.equals("")){
                    formattedNumbers.add(Long.valueOf(numberString));
                }
            }

            //Replace all place holders with formatted numbers
            String title = replaceNumbers;
            for (int i = 0; i < formattedNumbers.size(); i++){
                title = title.replaceFirst("<number>", NumberFormat.getInstance().format(formattedNumbers.get(i)));
            }

            return title;
        }catch (Exception e){}

        return string;
    }

    @Override
    public int getItemCount(){
        return files.size();
    }

    public void refresh(){
        files.clear();
        files.addAll(Arrays.asList(directory.listFiles()));
        Utils.sortByDate(files, false);
        notifyDataSetChanged();
    }

    public List<File> getFiles(){
        return files;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView fileName;
        private final TextView icon;

        ViewHolder(final View itemView){
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            fileName = itemView.findViewById(R.id.file_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent;
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
            });
        }
    }
}
