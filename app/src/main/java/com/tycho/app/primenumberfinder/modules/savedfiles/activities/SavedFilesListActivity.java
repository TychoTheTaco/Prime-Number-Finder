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
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SelectableAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */
public class SavedFilesListActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesListActivity.class.getSimpleName();

    private TextView subTitleTextView;

    SavedFilesListAdapter adapterSavedFilesList;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply custom theme depending on directory
        final File directory = (File) getIntent().getSerializableExtra("directory");
        switch (FileManager.getFileType(directory)) {
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
        adapterSavedFilesList.addOnSelectionStateChangedListener(new SelectableAdapter.OnSelectionStateChangedListener() {
            @Override
            public void onStartSelection() {
                menu.findItem(R.id.delete).setVisible(true);
            }

            @Override
            public void onItemSelected() {
                updateSubtitle();
            }

            @Override
            public void onItemDeselected() {
                updateSubtitle();
            }

            @Override
            public void onStopSelection() {
                menu.findItem(R.id.delete).setVisible(false);
                updateSubtitle();
            }
        });

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up subtitle
        subTitleTextView = findViewById(R.id.text);
        updateSubtitle();

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
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.delete:
                //Get the files to be deleted
                final int[] selectedItemIndexes = adapterSavedFilesList.getSelectedItemIndexes();
                final List<File> files = new ArrayList<>();
                for (int i : selectedItemIndexes){
                    files.add(adapterSavedFilesList.getFiles().get(i));
                }

                final Iterator<File> iterator = files.iterator();
                int position = 0;
                while (iterator.hasNext()){
                    final File file = iterator.next();

                    //Delete the file
                    file.delete();
                    adapterSavedFilesList.getFiles().remove(file);
                    adapterSavedFilesList.notifyItemRemoved(selectedItemIndexes[position] - position);
                    position++;
                }
                adapterSavedFilesList.setSelectionMode(false);
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (adapterSavedFilesList.isSelectionMode()) {
            adapterSavedFilesList.setSelectionMode(false);
        } else {
            super.onBackPressed();
        }
    }

    private void updateSubtitle() {
        String subtitle = getResources().getQuantityString(R.plurals.saved_files_count, adapterSavedFilesList.getItemCount(), NUMBER_FORMAT.format(adapterSavedFilesList.getItemCount()));
        if (adapterSavedFilesList.isSelectionMode()) {
            subtitle += ' ' + getResources().getQuantityString(R.plurals.selected_item_count, adapterSavedFilesList.getSelectedItemCount(), NUMBER_FORMAT.format(adapterSavedFilesList.getSelectedItemCount()));
        }
        subTitleTextView.setText(subtitle);
    }
}