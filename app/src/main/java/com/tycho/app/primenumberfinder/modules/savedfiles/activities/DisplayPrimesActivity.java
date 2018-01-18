package com.tycho.app.primenumberfinder.modules.savedfiles.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 11/5/2016
 */

public class DisplayPrimesActivity extends AppCompatActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "DispFactListAct";

    private File file;

    private RecyclerView recyclerView;

    private PrimesAdapter primesAdapter;

    //private TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_factors_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyThemeColor(ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null){

            //Get extras from the intent
            final Bundle extras = intent.getExtras();
            if (extras != null){

                //Get the file path from the extras
                final String filePath = extras.getString("filePath");
                if (filePath != null){

                    //Set up adapter
                    primesAdapter = new PrimesAdapter(this);

                    //Set up RecyclerView
                    recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(primesAdapter);
                    recyclerView.setItemAnimator(null);

                    file = new File(filePath);
                    loadFile(file);

                    if (extras.getBoolean("title", true)){
                        setTitle(formatTitle(file.getName().split("\\.")[0]));
                    }

                }else{
                    Log.e(TAG, "Invalid file path!");
                    Toast.makeText(this, "Error loading file!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }else{
                Log.e(TAG, "Intent had no extras!");
                Toast.makeText(this, "Error loading file!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }else{
            Log.e(TAG, "Activity was started without an intent!");
            Toast.makeText(this, "Error loading file!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadFile(final File file){
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Load file in another thread
        new Thread(new Runnable(){
            @Override
            public void run(){

                final List<Long> numbers = FileManager.getInstance().readNumbers(file);
                primesAdapter.getPrimes().addAll(numbers);

                new Handler(getMainLooper()).post(new Runnable(){
                    @Override
                    public void run(){
                        primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
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

    private void setActionBarColor(final int color) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
    }

    private void setStatusBarColor(final int color) {
        getWindow().setStatusBarColor(color);
    }

    private void applyThemeColor(final int statusBarColor, final int actionBarColor) {
        setStatusBarColor(statusBarColor);
        setActionBarColor(actionBarColor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
