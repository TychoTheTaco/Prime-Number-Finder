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

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesListAdapter;
import com.tycho.app.primenumberfinder.utils.Utils;

/**
 * @author Tycho Bellers
 *         Date Created: 11/5/2016
 */
public class SavedFilesListActivity extends AbstractActivity {

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

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (fileType) {
            case PRIMES:
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
                break;

            case FACTORS:
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));
                break;

            case TREE:
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));
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