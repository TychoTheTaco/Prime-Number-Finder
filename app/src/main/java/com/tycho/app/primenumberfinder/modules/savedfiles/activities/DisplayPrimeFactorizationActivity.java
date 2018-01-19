package com.tycho.app.primenumberfinder.modules.savedfiles.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.TreeView;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.CustomFileProvider;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import simpletrees.Tree;

/**
 * Created by tycho on 11/12/2017.
 */

public class DisplayPrimeFactorizationActivity extends AppCompatActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "DispPrimeFacAct";

    private File file;

    private Tree<Long> factorTree;

    private TextView primeFactorization;
    private TextView title;
    //private TreeView treeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_prime_factorization);

        primeFactorization = findViewById(R.id.prime_factorization);
        title = findViewById(R.id.title);
        //treeView = findViewById(R.id.factor_tree);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyThemeColor(ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null){

            //Get extras from the intent
            final Bundle extras = intent.getExtras();
            if (extras != null){

                //Get the file path from the extras
                final String filePath = extras.getString("filePath");
                if (filePath != null){

                    file = new File(filePath);
                    loadFile(file);

                    if (extras.getBoolean("title")){
                        //setTitle(formatTitle(file.getName().split("\\.")[0]));
                        setTitle("Prime factorization");
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

                factorTree = FileManager.getInstance().readTree(file);
                progressDialog.dismiss();

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //treeView.setTree(factorTree);
                        title.setText("Prime factorization of " + NumberFormat.getInstance(Locale.getDefault()).format(factorTree.getValue()));

                        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                        final Map<Long, Integer> map = new HashMap<>();
                        getPrimeFactors(map, factorTree);
                        for (Object factor : map.keySet()){
                            spannableStringBuilder.append(NumberFormat.getInstance(Locale.getDefault()).format(factor));
                            final int startIndex = spannableStringBuilder.length();
                            spannableStringBuilder.append(NumberFormat.getInstance(Locale.getDefault()).format(map.get(factor)), new SuperscriptSpan(), 0);
                            final int endIndex = spannableStringBuilder.length();
                            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), startIndex, endIndex, 0);
                            spannableStringBuilder.append(" x ");
                        }
                        spannableStringBuilder.delete(spannableStringBuilder.length() - 3, spannableStringBuilder.length());
                        primeFactorization.setText(spannableStringBuilder);
                    }
                });

            }
        }).start();
    }

    private void getPrimeFactors(final Map<Long, Integer> map, final Tree<Long> tree){
        if (tree.getChildren().size() > 0){
            for (Tree child : tree.getChildren()){
                getPrimeFactors(map, child);
            }
        }else{
            if (map.get(tree.getValue()) == null){
                map.put(tree.getValue(),  1);
            }else{
                map.put(tree.getValue(), map.get(tree.getValue()) + 1);
            }

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

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;

            /*case R.id.export:
                final Uri path = CustomFileProvider.getUriForFile(this,"com.tycho.app.primenumberfinder", file);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, path);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("text/plain");
                startActivity(intent);
                break;*/
        }

        return super.onOptionsItemSelected(item);
    }
}
