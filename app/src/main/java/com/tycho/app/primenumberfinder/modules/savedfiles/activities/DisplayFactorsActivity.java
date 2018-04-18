package com.tycho.app.primenumberfinder.modules.savedfiles.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 11/5/2016
 */

public class DisplayFactorsActivity extends AppCompatActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "DispFactListAct";

    private File file;

    private RecyclerView recyclerView;

    private FactorsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_factors_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyThemeColor(ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));

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
                    adapter = new FactorsListAdapter(this);

                    //Set up RecyclerView
                    recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                    recyclerView.setItemAnimator(null);

                    final long number = extras.getLong("number");
                    if (number != 0){
                        adapter.setNumber(number);
                    }

                    file = new File(filePath);
                    loadFile(file);

                    if (extras.getBoolean("title")){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);

        //Hide the export option if this is a temp file
        menu.findItem(R.id.export).setVisible(!file.getName().equals("temp"));
        menu.findItem(R.id.find).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.export:
                final ExportOptionsDialog exportOptionsDialog = new ExportOptionsDialog(this, file);
                exportOptionsDialog.show();
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFile(final File file){
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Load file in another thread
        new Thread(new Runnable(){
            @Override
            public void run(){

                final List<Long> numbers = FileManager.readNumbers(file);
                adapter.getFactors().addAll(numbers);

                new Handler(getMainLooper()).post(new Runnable(){
                    @Override
                    public void run(){
                        adapter.notifyItemRangeInserted(0, adapter.getItemCount());
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
    protected void onResume() {
        super.onResume();
        PrimeNumberFinder.getTaskManager().resumeAllTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrimeNumberFinder.getTaskManager().pauseAllTasks();
    }
}
