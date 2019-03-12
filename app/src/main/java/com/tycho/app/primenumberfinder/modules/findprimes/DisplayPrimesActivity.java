package com.tycho.app.primenumberfinder.modules.findprimes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.modules.savedfiles.FindNthNumberDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import examples.FindPrimesTask;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayPrimesActivity extends DisplayContentActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayPrimesActivity.class.getSimpleName();

    private TextView headerTextView;

    private RecyclerView recyclerView;

    private PrimesAdapter primesAdapter;

    private MenuItem findButton;

    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    private final CustomScrollListener scrollListener = new CustomScrollListener();

    private FloatingActionButton scrollToTopFab;
    private FloatingActionButton scrollToBottomFab;

    private AppBarLayout appBarLayout;

    private FileManager.PrimesFile primesFile;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTheme(R.style.FindPrimes);
        setContentView(R.layout.display_primes_activity);

        //Set up the toolbar
        appBarLayout = findViewById(R.id.app_bar);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.FindPrimes_PopupOverlay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));

        //Set up adapter
        primesAdapter = new PrimesAdapter(this);

        //Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(primesAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.addOnScrollListener(scrollListener);

        //Header text
        headerTextView = findViewById(R.id.subtitle);

        //Start loading the file
        load();

        //Set up toolbar animation
        ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            final int height = appBarLayout.getTotalScrollRange();
            headerTextView.setAlpha(1.0f - ((float) -verticalOffset) / height);
        });

        setTitle("Loading...");

        //Set up scroll to top button
        scrollToTopFab = findViewById(R.id.scroll_to_top_fab);
        scrollToTopFab.setOnClickListener(v -> {
            appBarLayout.setExpanded(true);
            scrollListener.specialScrollToPosition(0, false);
        });

        //Set up scroll to bottom button
        scrollToBottomFab = findViewById(R.id.scroll_to_bottom_fab);
        scrollToBottomFab.setOnClickListener(v -> {
            appBarLayout.setExpanded(false);
            scrollListener.specialScrollToPosition(scrollListener.totalNumbers - 1, false);
        });
    }

    private class CustomScrollListener extends RecyclerView.OnScrollListener{
        private int totalNumbers = 0;

        private int totalItemCount, lastVisibleItem, visibleThreshold = 0;

        private final int INCREMENT = 250;
        private int firstItemIndex;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy){
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            if (totalItemCount - 1 <= (lastVisibleItem + visibleThreshold)){
                loadDown();
            }else if (linearLayoutManager.findFirstVisibleItemPosition() <= visibleThreshold){
                if (firstItemIndex > 0){
                    loadUp();
                }
            }
            primesAdapter.setOffset(firstItemIndex);
        }

        private void loadUp(){
            //Log.d(TAG, "Size before: " + primesAdapter.getPrimes().size());

            //Remove items from end
            final Iterator<Long> iterator = primesAdapter.getPrimes().iterator();
            final int size = primesAdapter.getItemCount();
            for (int i = 0; i < size; i++){
                iterator.next();
                if (i >= size - INCREMENT){
                    iterator.remove();
                }
            }
            recyclerView.post(() -> primesAdapter.notifyItemRangeRemoved(size - INCREMENT, INCREMENT));

            //Add items to beginning
            Log.d(TAG, "Adding from " + (firstItemIndex - INCREMENT));
            final List<Long> numbers = primesFile.readNumbers(firstItemIndex - INCREMENT, INCREMENT);
            for (int i = numbers.size() - 1; i >= 0; i--){
                primesAdapter.getPrimes().add(0, numbers.get(i));
            }
            recyclerView.post(() -> primesAdapter.notifyItemRangeInserted(0, numbers.size()));

            firstItemIndex -= numbers.size();
            // Log.d(TAG, "Size after: " + primesAdapter.getPrimes().size());
        }

        private void loadDown(){
            //Log.d(TAG, "Size before: " + primesAdapter.getPrimes().size());

            //Read new items
            final List<Long> numbers = primesFile.readNumbers(firstItemIndex + totalItemCount, INCREMENT);
            final boolean endOfFile = numbers.size() < INCREMENT;

            if (!endOfFile){
                //Remove items
                final Iterator<Long> iterator = primesAdapter.getPrimes().iterator();
                for (int i = 0; i < INCREMENT; i++){
                    iterator.next();
                    iterator.remove();
                }
                recyclerView.post(() -> primesAdapter.notifyItemRangeRemoved(0, INCREMENT));
            }

            //Add new items
            primesAdapter.getPrimes().addAll(numbers);
            recyclerView.post(() -> primesAdapter.notifyItemRangeInserted(primesAdapter.getItemCount(), numbers.size()));

            if (!endOfFile){
                firstItemIndex += numbers.size();
            }

            //Log.d(TAG, "Size after: " + primesAdapter.getPrimes().size());
        }

        private void specialScrollToPosition(final int position, final boolean animate){
            //Scroll to correct position
            int startIndex = (position / scrollListener.INCREMENT) * (scrollListener.INCREMENT);

            //Try to read from start
            startIndex -= INCREMENT;
            if (startIndex < 0){
                startIndex = 0;
            }
            List<Long> numbers = primesFile.readNumbers(startIndex, primesAdapter.getItemCount());
            final boolean endOfFile = numbers.size() < INCREMENT;

            if (endOfFile){
                int previousSize = 0;
                while ((numbers = primesFile.readNumbers(startIndex, primesAdapter.getItemCount())).size() > INCREMENT && previousSize != numbers.size()){
                    //Log.d(TAG, "Read " + numbers.size());
                    startIndex -= (primesAdapter.getItemCount() - numbers.size());
                    previousSize = numbers.size();
                    numbers.clear();
                }
            }

            //Log.d(TAG, "startIndex: " + startIndex);
            //Log.d(TAG, "Numbers: (" + numbers.get(0) + ", " + numbers.get(numbers.size() - 1) + ")");
            //Log.d(TAG, "size: " + numbers.size());
            primesAdapter.getPrimes().clear();
            primesAdapter.getPrimes().addAll(numbers);
            scrollListener.setFirstItemIndex(startIndex);
            runOnUiThread(() -> {
                primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                final int pos = position - scrollListener.firstItemIndex;
                recyclerView.scrollToPosition(pos);
                if (animate){
                    primesAdapter.animate(pos);
                }
            });
        }

        public void setTotalNumbers(final int count){
            this.totalNumbers = count;
        }

        public int getTotalNumbers(){
            return this.totalNumbers;
        }

        public void setFirstItemIndex(final int index){
            firstItemIndex = index;
            primesAdapter.setOffset(index);
        }
    }

    @Override
    protected void loadFile(final File file){
        try{
            primesFile = new FileManager.PrimesFile(file);
            setTitle("Prime numbers from " + primesFile.getStartValue() + " to " + primesFile.getEndValue());
            scrollListener.setTotalNumbers(primesFile.getTotalNumbers());

            //Read numbers
            final List<Long> numbers = primesFile.readNumbers(0, 1000);
            primesAdapter.getPrimes().addAll(numbers);

            //Update UI
            runOnUiThread(() -> {

                //If there are no numbers, there was probably an error
                if (numbers.size() == 0 && primesFile.getTotalNumbers() > 0){
                    showLoadingError();
                }else{
                    //Set header text
                    headerTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.find_primes_subtitle_result, primesFile.getTotalNumbers()), new String[]{
                            NUMBER_FORMAT.format(primesFile.getTotalNumbers()),
                            NUMBER_FORMAT.format(primesFile.getStartValue()),
                            primesFile.getEndValue() == 0 ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(primesFile.getEndValue()),
                    }, ContextCompat.getColor(getBaseContext(), R.color.white)));

                    resizeCollapsingToolbar();

                    //Update adapter
                    primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                    recyclerView.post(new Runnable(){

                        /**
                         * Minimum number of extra adapter items before displaying the
                         * scroll to top and scroll to bottom buttons.
                         */
                        private final int SCROLL_BUTTON_MIN_OVERFLOW = 20;

                        @Override
                        public void run(){
                            final int visibility = ((linearLayoutManager.findLastVisibleItemPosition() - linearLayoutManager.findFirstVisibleItemPosition()) >= scrollListener.totalNumbers - SCROLL_BUTTON_MIN_OVERFLOW) ? View.GONE : View.VISIBLE;
                            scrollToTopFab.setVisibility(visibility);
                            scrollToBottomFab.setVisibility(visibility);
                            findButton.setVisible(hasFlag(Flag.ALLOW_SEARCH) && visibility == View.VISIBLE);
                        }
                    });
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        findButton = menu.findItem(R.id.find);
        return true;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);
        findButton = menu.findItem(R.id.find);
        findButton.setVisible(enableSearch);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.find:
                final FindNthNumberDialog findNthNumberDialog = new FindNthNumberDialog(this, scrollListener.totalNumbers);
                findNthNumberDialog.addListener(number -> {
                    if (number > 0 && number <= scrollListener.getTotalNumbers()){
                        appBarLayout.setExpanded(false);
                        scrollListener.specialScrollToPosition(number - 1, true);
                    }else{
                        Toast.makeText(DisplayPrimesActivity.this, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                });
                findNthNumberDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
