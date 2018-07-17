package com.tycho.app.primenumberfinder.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;

public abstract class DisplayContentActivity extends AbstractActivity {

    /**
     * The file to display. The file path should be provided in the intent that starts this activity.
     */
    protected File file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the file from the intent
        try{
            file = new File(getIntent().getStringExtra("filePath"));
        }catch (Exception e){
            showLoadingError();
        }
    }

    /*protected void load(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadFile(file);
                onFileLoaded();
            }
        }).start();
    }*/

    protected abstract void loadFile(final File file);

    protected void onFileLoaded(){

    }

    protected void showLoadingError(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error!");
        alertDialog.setMessage("An unknown error occurred while reading this file.");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                        finish();
                    }
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
