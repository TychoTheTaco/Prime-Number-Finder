package com.tycho.app.primenumberfinder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.tycho.app.primenumberfinder.Adapters_old.SavedFilesAdapter;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SavedFileType;

/**
 * @author Tycho Bellers
 *         Date Created: 11/5/2016
 */

public class ActivityListFiles extends AppCompatActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "ActivityListFiles";

    private RecyclerView recyclerView;

    View colorView;

    View layoutToolbar;

    private Toolbar toolbar;

    private Context context;

    SavedFilesAdapter adapterSavedFilesList;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        //Call superclass
        super.onCreate(savedInstanceState);

        //Draw the layout
        setContentView(R.layout.activity_list_files);

        Intent intent = getIntent();

        int savedFileTypeId = intent.getExtras().getInt("savedFileType");

        final SavedFileType savedFileType = SavedFileType.findById(savedFileTypeId);

        adapterSavedFilesList = new SavedFilesAdapter(this, savedFileType);
        adapterSavedFilesList.sortByDate();
        adapterSavedFilesList.notifyDataSetChanged();

        context = this;

        //Set the actionbar to a custom toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        setSupportActionBar(toolbar);

        colorView = findViewById(R.id.color_view);

        switch (savedFileType){
            case PRIMES:
                colorView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
                break;

            case FACTORS:
                colorView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                break;

            case FACTOR_TREE:
                colorView.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                break;
        }

        layoutToolbar = findViewById(R.id.layout_toolbar);

        initRecyclerView();
    }

    private void initRecyclerView(){

        //Define the RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //The recycler view has a fixed size
        recyclerView.setHasFixedSize(true);

        //Set the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Set the adapter
        recyclerView.setAdapter(adapterSavedFilesList);

        //Disable item animations
        recyclerView.setItemAnimator(null);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                super.onScrolled(recyclerView, dx, dy);

                scrollColoredViewParallax(dy);
                if (dy > 0){
                    hideToolbarBy(dy);
                }else{
                    showToolbarBy(dy);
                }

            }
        });

    }

    private void scrollColoredViewParallax(int dy){
        colorView.setTranslationY(Math.min(0, colorView.getTranslationY() - dy / 3));
    }

    private void hideToolbarBy(int dy){
        if (cannotHideMore(dy)){
            layoutToolbar.setTranslationY(-toolbar.getBottom());
        }else{
            layoutToolbar.setTranslationY(layoutToolbar.getTranslationY() - dy);
        }
    }

    private boolean cannotHideMore(int dy){
        return Math.abs(layoutToolbar.getTranslationY() - dy) > toolbar.getBottom();
    }

    private void showToolbarBy(int dy){
        if (cannotShowMore(dy)){
            layoutToolbar.setTranslationY(0);
        }else{
            layoutToolbar.setTranslationY(layoutToolbar.getTranslationY() - dy);
        }
    }

    private boolean cannotShowMore(int dy){
        return layoutToolbar.getTranslationY() - dy > 0;
    }

    @Override
    protected void onPause(){
        super.onPause();

        Log.e(TAG, "PAUSED");

        if (!PrimeNumberFinder.getPreferenceManager().isAllowBackgroundTasks()){
            //PrimeNumberFinder.pauseAllThreads();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(TAG, "RESUMED");
        //PrimeNumberFinder.resumeAllThreads();
    }
}
