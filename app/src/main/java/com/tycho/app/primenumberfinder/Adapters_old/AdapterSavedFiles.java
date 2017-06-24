package com.tycho.app.primenumberfinder.Adapters_old;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SavedFileType;
import com.tycho.app.primenumberfinder.activities.DisplayFileContentsActivity;
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

public class AdapterSavedFiles extends RecyclerView.Adapter<AdapterSavedFiles.ViewHolderSavedFiles>{

    protected final ArrayList<File> files = new ArrayList<>();

    private final SavedFileType fileType;

    private Context context;

    public AdapterSavedFiles(final Context context, final SavedFileType fileType){
        this.fileType = fileType;

        this.context = context;

        switch (fileType){
            case PRIMES:
                files.addAll(Arrays.asList(FileManager.getInstance(context).getDirectorySavedPrimes().listFiles()));
                break;
            case FACTORS:
                files.addAll(Arrays.asList(FileManager.getInstance(context).getDirectorySavedFactors().listFiles()));
                break;
        }
    }

    @Override
    public ViewHolderSavedFiles onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_saved_file, parent, false);
        return new AdapterSavedFiles.ViewHolderSavedFiles(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderSavedFiles holder, int position){

        final File file = files.get(position);

        final String fileName = file.getName().replace(".txt", "");

        holder.fileName.setText(formatTitle(fileName));


        switch (fileType){

            case PRIMES:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple)));
                holder.icon.setText("P");
                break;

            case FACTORS:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange)));
                holder.icon.setText("F");
                break;

            case FACTOR_TREE:
                holder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.green)));
                holder.icon.setText("T");
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(context, DisplayFileContentsActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                context.startActivity(intent);
            }
        });
    }

    private String formatTitle(final String string){

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
    }

    @Override
    public int getItemCount(){
        return files.size();
    }

    public void sortByDate(){
        Collections.sort(files, new Comparator<File>(){
            @Override
            public int compare(File file0, File file1){
                return (int) (file1.lastModified() - file0.lastModified());
            }
        });
    }

    public ArrayList<File> getFiles(){
        return files;
    }

    public SavedFileType getFileType(){
        return fileType;
    }

    protected class ViewHolderSavedFiles extends RecyclerView.ViewHolder{

        protected final TextView fileName;
        protected final TextView dateCreated;
        protected final TextView icon;

        public ViewHolderSavedFiles(final View itemView){
            super(itemView);
            icon = (TextView) itemView.findViewById(R.id.icon);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            dateCreated = (TextView) itemView.findViewById(R.id.textView_dateCreated);
        }
    }
}
