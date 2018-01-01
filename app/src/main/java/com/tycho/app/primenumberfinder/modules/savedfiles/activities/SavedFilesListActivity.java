package com.tycho.app.primenumberfinder.modules.savedfiles.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesListAdapter;

/**
 * @author Tycho Bellers
 *         Date Created: 11/5/2016
 */
public class SavedFilesListActivity extends AppCompatActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "SavedFilesListActivity";

    SavedFilesListAdapter adapterSavedFilesList;

    private static Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_files_list_activity);

        Intent intent = getIntent();

        int savedFileTypeId = intent.getExtras().getInt("fileType");

        final FileType fileType = FileType.findById(savedFileTypeId);

        adapterSavedFilesList = new SavedFilesListAdapter(this, fileType);
        adapterSavedFilesList.sortByDate();
        adapterSavedFilesList.notifyDataSetChanged();

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (fileType) {
            case PRIMES:
                applyThemeColor(ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
                break;

            case FACTORS:
                applyThemeColor(ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));
                break;

            case TREE:
                applyThemeColor(ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));
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
    public void onBackPressed() {
        if (adapterSavedFilesList.isSelecting()) {
            adapterSavedFilesList.setSelecting(false);
            adapterSavedFilesList.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrimeNumberFinder.getTaskManager().resumeAllTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!PrimeNumberFinder.getPreferenceManager().isAllowBackgroundTasks()) {
            PrimeNumberFinder.getTaskManager().pauseAllTasks();
        }
    }
}