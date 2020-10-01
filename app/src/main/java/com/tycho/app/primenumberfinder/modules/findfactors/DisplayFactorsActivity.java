package com.tycho.app.primenumberfinder.modules.findfactors;

import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayFactorsActivity extends DisplayContentActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayFactorsActivity.class.getSimpleName();

    private TextView headerTextView;

    private FactorsListAdapter adapter;

    private FileManager.FactorsFile factorsFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //Set up toolbar animation
        ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            final int height = appBarLayout.getTotalScrollRange();
            headerTextView.setAlpha(1.0f - ((float) -verticalOffset) / height);
        });

        //Start loading the file
        load();
    }

    @Override
    protected void loadFile(final File file) throws Exception {
        factorsFile = new FileManager.FactorsFile(file);
        adapter.getFactors().addAll(factorsFile.readNumbers(0, -1));
    }

    @Override
    protected void onFileLoaded() {
        setTitle("Factors of " + NumberFormat.getInstance().format(factorsFile.getNumber()));

        //Set header text
        headerTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.find_factors_subtitle_results, factorsFile.getTotalNumbers()), new String[]{
                NUMBER_FORMAT.format(factorsFile.getNumber()),
                NUMBER_FORMAT.format(factorsFile.getTotalNumbers()),
        }, ContextCompat.getColor(getBaseContext(), R.color.white)));

        resizeCollapsingToolbar();

        //Update adapter
        adapter.notifyItemRangeInserted(0, adapter.getItemCount());
    }

    @Override
    protected void export(final File file) {
        FirebaseAnalytics.getInstance(this).logEvent("export_factors", null);
        final ExportOptionsDialog exportOptionsDialog = new ExportFactorsOptionsDialog(this, file, R.style.FindFactors_Dialog);
        exportOptionsDialog.show();
    }
}
