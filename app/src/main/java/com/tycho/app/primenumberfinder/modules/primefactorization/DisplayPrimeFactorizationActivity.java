package com.tycho.app.primenumberfinder.modules.primefactorization;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.modules.primefactorization.export.FactorTreeExportOptionsActivity;
import com.tycho.app.primenumberfinder.ui.TreeView;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import simpletrees.Tree;

/**
 * Created by tycho on 11/12/2017.
 */

public class DisplayPrimeFactorizationActivity extends DisplayContentActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayPrimeFactorizationActivity.class.getSimpleName();

    private Tree<Long> factorTree;

    private TextView subtitleTextView;
    private TextView bodyTextView;
    private TreeView treeView;

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState){
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
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));

        //Start loading the file
        load();
    }

    @Override
    protected void loadFile(final File file){
        factorTree = FileManager.getInstance().readTree(file);

        setTitle("Prime Factorization of " + NUMBER_FORMAT.format(factorTree.getValue()));

        runOnUiThread(() -> {

            if (factorTree == null){
                showLoadingError();
                return;
            }

            treeView.setTree(factorTree.formatNumbers());
            if (PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 1){
                final TreeView.ExportOptions exportOptions = treeView.getDefaultExportOptions();
                exportOptions.itemTextColor = Color.WHITE;
                exportOptions.itemBorderColor = ContextCompat.getColor(this, R.color.accent_light_but_not_that_light);
                exportOptions.branchColor = Color.WHITE;
                exportOptions.itemBackgroundColor = Color.BLACK;
                exportOptions.imageBorderColor = Color.WHITE;
                exportOptions.imageBackgroundColor = Color.BLACK;
                exportOptions.primeFactorTextColor = ContextCompat.getColor(this, R.color.red);
                treeView.setExportOptions(exportOptions);
            }

            final Map<Long, Integer> map = new TreeMap<>();
            getPrimeFactors(map, factorTree);

            //Subtitle
            subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder,
                    getResources().getQuantityString(R.plurals.prime_factorization_subtitle_results, map.keySet().size()),
                    new String[]{NUMBER_FORMAT.format(factorTree.getValue()), NUMBER_FORMAT.format(map.keySet().size())},
                    ContextCompat.getColor(getBaseContext(), PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.green_dark : R.color.green_light)
            ));

            //Body
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            spannableStringBuilder.append(NUMBER_FORMAT.format(factorTree.getValue()));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.green_dark : R.color.green_light)), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            int position = spannableStringBuilder.length();
            spannableStringBuilder.append(" = ");
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.gray)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            for (Long factor : map.keySet()){
                position = spannableStringBuilder.length();
                String content = NUMBER_FORMAT.format(factor);
                spannableStringBuilder.append(content);
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0 ? R.color.green_dark : R.color.green_light)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
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
        });
    }

    @Override
    protected void export(File file){
        final Intent intent = new Intent(this, FactorTreeExportOptionsActivity.class);
        intent.putExtra("filePath", file.getAbsolutePath());
        startActivity(intent);
    }

    private void getPrimeFactors(final Map<Long, Integer> map, final Tree<Long> tree){
        if (tree.getChildren().size() > 0){
            for (Tree<Long> child : tree.getChildren()){
                getPrimeFactors(map, child);
            }
        }else{
            if (map.get(tree.getValue()) == null){
                map.put(tree.getValue(), 1);
            }else{
                map.put(tree.getValue(), map.get(tree.getValue()) + 1);
            }
        }
    }
}
