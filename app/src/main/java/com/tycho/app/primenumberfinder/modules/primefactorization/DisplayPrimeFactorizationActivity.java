package com.tycho.app.primenumberfinder.modules.primefactorization;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LocaleSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.ui.TreeView;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import simpletrees.Tree;

/**
 * Created by tycho on 11/12/2017.
 */

public class DisplayPrimeFactorizationActivity extends DisplayContentActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayPrimeFactorizationActivity.class.getSimpleName();

    private File file;

    private Tree<Long> factorTree;

    private TextView subtitleTextView;
    private TextView bodyTextView;
    private TreeView treeView;

    private boolean allowExport;
    private boolean allowDelete;

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_prime_factorization_activity);

        subtitleTextView = findViewById(R.id.subtitle);
        bodyTextView = findViewById(R.id.body);
        treeView = findViewById(R.id.factor_tree);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.PrimeFactorization_PopupOverlay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null) {

            //Get the file path from the extras
            final String filePath = intent.getStringExtra("filePath");
            if (filePath != null) {

                file = new File(filePath);

                //Set a custom title if there is one
                if (intent.getBooleanExtra("title", true)) {
                    setTitle(Utils.formatTitle(file));
                }

                //Start loading the file
                loadFile(file);

                allowExport = intent.getBooleanExtra("allowExport", false);
                allowDelete = intent.getBooleanExtra("allowDelete", false);

            } else {
                Log.e(TAG, "Invalid file path!");
                Toast.makeText(this, "Error loading file!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e(TAG, "Activity was started without an intent!");
            Toast.makeText(this, "Error loading file!", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    protected void load(File file) {

    }

    private void loadFile(final File file) {
        //Load file in another thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                factorTree = FileManager.getInstance().readTree(file);
                progressDialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (factorTree == null){
                            showLoadingError();
                            return;
                        }
                        
                        treeView.setTree(factorTree.formatNumbers());

                        final Map<Long, Integer> map = new TreeMap<>();
                        getPrimeFactors(map, factorTree);

                        //Subtitle
                        subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder,
                                getResources().getQuantityString(R.plurals.prime_factorization_subtitle_results, map.keySet().size()),
                                new String[]{NUMBER_FORMAT.format(factorTree.getValue()), NUMBER_FORMAT.format(map.keySet().size())},
                                ContextCompat.getColor(getBaseContext(), R.color.green_dark)
                        ));

                        //Body
                        spannableStringBuilder.clear();
                        spannableStringBuilder.clearSpans();
                        spannableStringBuilder.append(NUMBER_FORMAT.format(factorTree.getValue()));
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.green_dark)), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        int position = spannableStringBuilder.length();
                        spannableStringBuilder.append(" = ");
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.gray)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        for (Long factor : map.keySet()) {
                            position = spannableStringBuilder.length();
                            String content = NUMBER_FORMAT.format(factor);
                            spannableStringBuilder.append(content);
                            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.green_dark)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                            position = spannableStringBuilder.length();
                            content = NUMBER_FORMAT.format(map.get(factor));
                            spannableStringBuilder.append(content);
                            spannableStringBuilder.setSpan(new SuperscriptSpan(), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                            position = spannableStringBuilder.length();
                            content = " \u00D7 "; //Multiplication sign
                            spannableStringBuilder.append(content);
                            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.gray)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        }
                        spannableStringBuilder.delete(spannableStringBuilder.length() - 3, spannableStringBuilder.length());
                        bodyTextView.setVisibility(View.VISIBLE);
                        bodyTextView.setText(spannableStringBuilder);
                    }
                });

            }
        }).start();
    }

    private void getPrimeFactors(final Map<Long, Integer> map, final Tree<Long> tree) {
        if (tree.getChildren().size() > 0) {
            for (Tree<Long> child : tree.getChildren()) {
                getPrimeFactors(map, child);
            }
        } else {
            if (map.get(tree.getValue()) == null) {
                map.put(tree.getValue(), 1);
            } else {
                map.put(tree.getValue(), map.get(tree.getValue()) + 1);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);
        menu.findItem(R.id.find).setVisible(false);
        menu.findItem(R.id.export).setVisible(allowExport);
        menu.findItem(R.id.delete).setVisible(allowDelete);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.export:
                final Intent intent = new Intent(this, FactorTreeExportOptionsActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                startActivity(intent);
                break;

            case R.id.delete:
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("Are you sure you want to delete this saved file?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                alertDialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }
}
