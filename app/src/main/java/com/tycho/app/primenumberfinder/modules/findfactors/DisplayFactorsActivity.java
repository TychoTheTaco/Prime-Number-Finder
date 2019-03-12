package com.tycho.app.primenumberfinder.modules.findfactors;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayFactorsActivity extends DisplayContentActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayFactorsActivity.class.getSimpleName();

    private TextView headerTextView;

    private FactorsListAdapter adapter;

    private FileManager.FactorsFile factorsFile;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTheme(R.style.FindFactors);
        setContentView(R.layout.display_factors_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.FindFactors_PopupOverlay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));

        //Set up adapter
        adapter = new FactorsListAdapter(this);

        //Set up RecyclerView
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        //Header text
        headerTextView = findViewById(R.id.subtitle);

        //Start loading the file
        setTitle("Loading...");
        load();

        //Set up toolbar animation
        ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            final int height = appBarLayout.getTotalScrollRange();
            headerTextView.setAlpha(1.0f - ((float) -verticalOffset) / height);
        });
    }

    @Override
    protected void loadFile(final File file){
        try{
            factorsFile = new FileManager.FactorsFile(file);
            setTitle("Factors of " + NumberFormat.getInstance().format(factorsFile.getNumber()));
            final List<Long> numbers = factorsFile.readNumbers(0, -1);
            adapter.getFactors().addAll(numbers);

            //Update UI
            runOnUiThread(() -> {

                //If there are no numbers, there was probably an error
                if (numbers.size() == 0 && file.length() > 0){
                    showLoadingError();
                }else{
                    //Set header text
                    headerTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.find_factors_subtitle_results, numbers.size()), new String[]{
                            NUMBER_FORMAT.format(numbers.get(numbers.size() - 1)),
                            NUMBER_FORMAT.format(numbers.size()),
                    }, ContextCompat.getColor(getBaseContext(), R.color.white)));

                    resizeCollapsingToolbar();

                    //Update adapter
                    adapter.notifyItemRangeInserted(0, adapter.getItemCount());
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void export(final File file){
        final ExportOptionsDialog exportOptionsDialog = new ExportFactorsOptionsDialog(this, file, R.style.FindFactors_Dialog);
        exportOptionsDialog.show();
    }
}
