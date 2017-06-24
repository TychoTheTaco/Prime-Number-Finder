package com.tycho.app.primenumberfinder.Adapters_old;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SavedFileType;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 11/7/2016
 */

public class TestAdapter extends ArrayAdapter<File>{

    protected final ArrayList<File> files = new ArrayList<>();

    private final SavedFileType fileType;

    private Context context;

    public TestAdapter(final Context context, final SavedFileType fileType){
        super(context, 0, FileManager.getInstance(context).getDirectorySavedPrimes().listFiles());

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

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        final String fileName = files.get(position).getName().replace(".txt", "");

        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_saved_file, parent, false);
        }

        TextView textViewFileName = (TextView) convertView.findViewById(R.id.file_name);
        textViewFileName.setText(formatTitle(fileName));

        TextView icon = (TextView) convertView.findViewById(R.id.icon);

        switch (fileType){

            case PRIMES:
                icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple)));
                icon.setText("P");
                break;

            case FACTORS:
                icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange)));
                icon.setText("F");
                break;

            case FACTOR_TREE:
                icon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green)));
                icon.setText("T");
                break;
        }

        return convertView;
    }
}
