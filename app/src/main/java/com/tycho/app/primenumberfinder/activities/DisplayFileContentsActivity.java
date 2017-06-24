package com.tycho.app.primenumberfinder.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.tycho.app.primenumberfinder.Adapters_old.NumberAdapter;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 11/5/2016
 */

public class DisplayFileContentsActivity extends AppCompatActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "DispFileContAct";

    private RecyclerView recyclerView;

    private NumberAdapter numberAdapter = new NumberAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState){

        //Call superclass
        super.onCreate(savedInstanceState);

        //Draw the layout
        setContentView(R.layout.activity_display_list);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        setSupportActionBar(toolbar);

        final Intent intent = getIntent();

        if (intent != null){

            final Bundle extras = intent.getExtras();

            if (extras != null){

                final String filePath = extras.getString("filePath");
                if (filePath != null){

                    final File file = new File(filePath);
                    loadFile(file);
                    setTitle(formatTitle(file.getName().replace(".txt", "")));

                }else{
                    Log.e(TAG, "Invalid file path!");
                }

            }else{
                Log.e(TAG, "Intent had no extras!");
            }

        }else{
            Log.e(TAG, "Activity was started without an intent!");
        }

        initRecyclerView();
    }

    private void loadFile(final File file){
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Load file in another thread
        new Thread(new Runnable(){
            @Override
            public void run(){

                final List<Long> numbers = FileManager.getInstance().readNumbers(file);
                numberAdapter.getListNumbers().addAll(numbers);

                new Handler(getMainLooper()).post(new Runnable(){
                    @Override
                    public void run(){
                        numberAdapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
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

    private void initRecyclerView(){

        //Define the RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //The recycler view has a fixed size
        recyclerView.setHasFixedSize(true);

        //Set the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Set the adapter
        recyclerView.setAdapter(numberAdapter);

        //Disable item animations
        recyclerView.setItemAnimator(null);

    }
}
