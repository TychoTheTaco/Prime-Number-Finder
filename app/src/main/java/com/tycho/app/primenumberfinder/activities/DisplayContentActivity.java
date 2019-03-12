package com.tycho.app.primenumberfinder.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class DisplayContentActivity extends AbstractActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayContentActivity.class.getSimpleName();

    /**
     * The file to display. The file path should be provided in the intent that starts this activity.
     */
    private File file;

    public enum Flag{
        ALLOW_DELETE,
        ALLOW_EXPORT,
        ALLOW_SEARCH
    }

    private final Set<Flag> flags = new HashSet<>(3);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        file = new File(getIntent().getStringExtra("filePath"));
        setFlag(Flag.ALLOW_DELETE, intent.getBooleanExtra("allowDelete", false));
        setFlag(Flag.ALLOW_EXPORT, intent.getBooleanExtra("allowExport", false));
        setFlag(Flag.ALLOW_SEARCH, intent.getBooleanExtra("enableSearch", false));
        load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);
        menu.findItem(R.id.delete).setVisible(hasFlag(Flag.ALLOW_DELETE));
        menu.findItem(R.id.export).setVisible(hasFlag(Flag.ALLOW_EXPORT));
        menu.findItem(R.id.find).setVisible(hasFlag(Flag.ALLOW_SEARCH));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.export:
                export(file);
                break;

            case R.id.delete:
                delete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setFlag(final Flag flag, final boolean enabled){
        if (enabled){
            flags.add(flag);
        }else{
            flags.remove(flag);
        }
    }

    protected boolean hasFlag(final Flag flag){
        return flags.contains(flag);
    }

    protected void load(){
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");
        new Thread(() -> {
            loadFile(file);
            onFileLoaded();
            progressDialog.dismiss();
        }).start();
    }

    /**
     * Export the current file. This method is left empty, so it is up to the subclasses to determine what happens.
     *
     * @param file The file to export.
     */
    protected void export(final File file){

    }

    /**
     * Prompt the user to confirm before deleting the file, and end the activity upon successful deletion.
     */
    protected void delete(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Warning");
        alertDialog.setMessage("Are you sure you want to delete this saved file?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DELETE",
                (dialog, which) -> {
                    if (file.delete()){
                        alertDialog.dismiss();
                        finish();
                    }else{
                        Log.e(TAG, "Failed to delete file: " + file);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialog, which) -> alertDialog.dismiss());
        alertDialog.show();
    }

    protected abstract void loadFile(final File file);

    protected void onFileLoaded(){

    }

    protected void showLoadingError(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error!");
        alertDialog.setMessage("An unknown error occurred while reading this file.");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                (dialog, which) -> {
                    alertDialog.dismiss();
                    finish();
                });
        alertDialog.show();
    }

    /**
     * Set the correct height for the toolbar depending on the required height of its header.
     */
    protected void resizeCollapsingToolbar(){
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        Utils.reLayoutChildren(collapsingToolbarLayout);
        final int defaultHeight = getSupportActionBar().getHeight();
        final int headerHeight = collapsingToolbarLayout.findViewById(R.id.expanded_layout).getHeight();
        final AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        layoutParams.height = defaultHeight + headerHeight;
        collapsingToolbarLayout.setLayoutParams(layoutParams);
    }
}
