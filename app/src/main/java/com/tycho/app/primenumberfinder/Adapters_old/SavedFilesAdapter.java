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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 11/8/2016
 */

public class SavedFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final ArrayList<File> files = new ArrayList<>();

    private final SavedFileType fileType;

    private Context context;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    public SavedFilesAdapter(final Context context, final SavedFileType fileType){
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        switch (viewType){
            case 0:
                return new ViewHolderHeader(LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_files_header, parent, false));
            case 1:
                return new ViewHolderSavedFiles(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_saved_file_large, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){

        if (holder instanceof ViewHolderHeader){
            bindHeader((ViewHolderHeader) holder, position);
        }else if (holder instanceof ViewHolderSavedFiles){
            bindItem((ViewHolderSavedFiles) holder, position);
        }
    }

    private void bindHeader(final ViewHolderHeader viewHolder, final int position){
        switch (fileType){

            case PRIMES:
                viewHolder.title.setText("Prime numbers");
                break;

            case FACTORS:
                viewHolder.title.setText("Factors");
                break;

            case FACTOR_TREE:
                viewHolder.title.setText("Factor Trees");
                break;
        }

        viewHolder.subTitle.setText("You have " + files.size() + " saved files.");
    }

    private void bindItem(final ViewHolderSavedFiles viewHolder, final int position){
        final File file = files.get(position - 1);
        final String fileName = file.getName().replace(".txt", "");

        viewHolder.fileName.setText(formatTitle(fileName));


        switch (fileType){

            case PRIMES:
                viewHolder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.purple)));
                viewHolder.icon.setText("P");
                break;

            case FACTORS:
                viewHolder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.orange)));
                viewHolder.icon.setText("F");
                break;

            case FACTOR_TREE:
                viewHolder.icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.green)));
                viewHolder.icon.setText("T");
                break;
        }

        viewHolder.dateCreated.setText(simpleDateFormat.format(new Date(file.lastModified())));

        viewHolder.fileSize.setText(humanReadableByteCount(file.length(), true));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(context, DisplayFileContentsActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                context.startActivity(intent);
            }
        });
    }

    public void sortByDate(){
        Collections.sort(files, new Comparator<File>(){
            @Override
            public int compare(File file0, File file1){
                return (int) (file1.lastModified() - file0.lastModified());
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
        return files.size() + 1;
    }

    @Override
    public int getItemViewType(int position){
        if (position == 0){
            return 0;
        }else{
            return 1;
        }
    }

    private class ViewHolderSavedFiles extends RecyclerView.ViewHolder{

        protected final TextView fileName;
        protected final TextView dateCreated;
        protected final TextView fileSize;
        protected final TextView icon;

        public ViewHolderSavedFiles(final View itemView){
            super(itemView);
            icon = (TextView) itemView.findViewById(R.id.icon);
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            dateCreated = (TextView) itemView.findViewById(R.id.textView_dateCreated);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
        }
    }

    private class ViewHolderHeader extends RecyclerView.ViewHolder{

        protected final TextView title;
        protected final TextView subTitle;

        public ViewHolderHeader(final View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            subTitle = (TextView) itemView.findViewById(R.id.subTitle);
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
