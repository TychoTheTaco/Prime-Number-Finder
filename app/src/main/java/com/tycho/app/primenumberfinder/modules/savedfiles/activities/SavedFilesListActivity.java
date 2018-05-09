package com.tycho.app.primenumberfinder.modules.savedfiles.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesListAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;

/**
 * @author Tycho Bellers
 *         Date Created: 11/5/2016
 */
public class SavedFilesListActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesListActivity.class.getSimpleName();

    SavedFilesListAdapter adapterSavedFilesList;

    private static Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply custom theme depending on directory
        final File directory = (File) getIntent().getSerializableExtra("directory");
        switch (FileManager.getFileType(directory)){
            case PRIMES:
                setTheme(R.style.FindPrimes_Activity);
                break;

            case FACTORS:
                setTheme(R.style.FindFactors_Activity);
                break;

            case TREE:
                setTheme(R.style.PrimeFactorization_Activity);
                break;
        }

        //Set content
        setContentView(R.layout.saved_files_list_activity);

        //Set up adapter
        adapterSavedFilesList = new SavedFilesListAdapter(this, directory);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up subtitle
        final TextView subTitleTextView = findViewById(R.id.text);
        subTitleTextView.setText("You have " + adapterSavedFilesList.getItemCount() + " saved files.");

        //Set up toolbar animation
        ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                final int height = appBarLayout.getTotalScrollRange();
                subTitleTextView.setAlpha(1.0f - ((float) -verticalOffset) / height);
            }
        });

        switch (FileManager.getFileType(directory)) {
            case PRIMES:
                setTitle("Prime Numbers");
                break;

            case FACTORS:
                setTitle("Factors");
                break;

            case TREE:
                setTitle("Factor Trees");
                break;
        }

        //Set up the RecyclerView
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterSavedFilesList);
        recyclerView.setItemAnimator(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.saved_files_activity_menu, menu);
        SavedFilesListActivity.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.delete:
                adapterSavedFilesList.deleteSelected();
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void setDeleteVisibility(final boolean visible){
        menu.findItem(R.id.delete).setVisible(visible);
    }

    @Override
    public void onBackPressed() {
        if (adapterSavedFilesList.isSelecting()) {
            adapterSavedFilesList.setSelecting(false);
            adapterSavedFilesList.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }
}