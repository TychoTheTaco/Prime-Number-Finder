package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 11/3/2016
 */

public class SavedFilesSmallListAdapter extends RecyclerView.Adapter<SavedFilesSmallListAdapter.ViewHolder>{

    private final List<File> files = new ArrayList<>();

    private final FileType fileType;

    private Context context;

    public SavedFilesSmallListAdapter(final Context context, final FileType fileType){
        this.fileType = fileType;

        this.context = context;

        switch (fileType){
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_saved_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){

        final File file = files.get(position);

        final String fileName = file.getName().replace((fileType == FileType.TREE ? FileManager.TREE_EXTENSION : FileManager.EXTENSION), "");

        holder.fileName.setText(formatTitle(fileName));


        switch (fileType){

            case PRIMES:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple)));
                holder.icon.setText("P");
                holder.itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Intent intent = new Intent(context, DisplayPrimesActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
                        intent.putExtra("title", true);
                        context.startActivity(intent);
                    }
                });
                break;

            case FACTORS:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange)));
                holder.icon.setText("F");
                holder.itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Intent intent = new Intent(context, DisplayFactorsActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
                        intent.putExtra("title", true);
                        context.startActivity(intent);
                    }
                });
                break;

            case TREE:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.green)));
                holder.icon.setText("T");
                holder.itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Intent intent = new Intent(context, DisplayPrimeFactorizationActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
                        intent.putExtra("title", true);
                        context.startActivity(intent);
                    }
                });
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
        switch (fileType){
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
        notifyDataSetChanged();
    }

    public void sortByDate(){
        Collections.sort(files, new Comparator<File>(){
            @Override
            public int compare(File file0, File file1){
                return (int) (file1.lastModified() - file0.lastModified());
            }
        });
    }

    public List<File> getFiles(){
        return files;
    }

    public FileType getFileType(){
        return fileType;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView fileName;
        private final TextView dateCreated;
        private final TextView icon;

        ViewHolder(final View itemView){
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            fileName = itemView.findViewById(R.id.file_name);
            dateCreated =  itemView.findViewById(R.id.textView_dateCreated);
        }
    }
}
