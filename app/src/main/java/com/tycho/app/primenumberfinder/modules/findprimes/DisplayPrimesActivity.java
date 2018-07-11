package com.tycho.app.primenumberfinder.modules.findprimes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.modules.savedfiles.FindNthNumberDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayPrimesActivity extends DisplayContentActivity {

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

    private boolean allowExport;
    private boolean enableSearch;
    private boolean allowDelete;

    private FloatingActionButton scrollToTopFab;
    private FloatingActionButton scrollToBottomFab;

    private AppBarLayout appBarLayout;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_primes_activity);

        //Set up the toolbar
        appBarLayout = findViewById(R.id.app_bar);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.FindPrimes_PopupOverlay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null) {
            if (file != null) {

                //Set a custom title if there is one
                if (intent.getBooleanExtra("title", true)) {
                    setTitle(Utils.formatTitle(file));
                }

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
                loadFile(file);

                //Set up toolbar animation
                ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        final int height = appBarLayout.getTotalScrollRange();
                        headerTextView.setAlpha(1.0f - ((float) -verticalOffset) / height);
                    }
                });

                allowExport = intent.getBooleanExtra("allowExport", false);
                enableSearch = intent.getBooleanExtra("enableSearch", false);
                allowDelete = intent.getBooleanExtra("allowDelete", false);

                //Set up scroll to top button
                scrollToTopFab = findViewById(R.id.scroll_to_top_fab);
                scrollToTopFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appBarLayout.setExpanded(true);
                        scrollListener.specialScrollToPosition(0, false);
                    }
                });

                //Set up scroll to bottom button
                scrollToBottomFab = findViewById(R.id.scroll_to_bottom_fab);
                scrollToBottomFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appBarLayout.setExpanded(false);
                        scrollListener.specialScrollToPosition(scrollListener.totalNumbers - 1, false);
                    }
                });

            } else {
                Log.e(TAG, "Invalid file path!");
                Toast.makeText(this, "Error loading file!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e(TAG, "Activity was started without an intent!");
            Toast.makeText(this, "Error loading file!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class CustomScrollListener extends RecyclerView.OnScrollListener {
        private int totalNumbers = 0;

        private int totalItemCount, lastVisibleItem, visibleThreshold = 0;

        private final int INCREMENT = 250;
        private int firstItemIndex;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            if (totalItemCount - 1 <= (lastVisibleItem + visibleThreshold)) {
                loadDown();
            } else if (linearLayoutManager.findFirstVisibleItemPosition() <= visibleThreshold) {
                if (firstItemIndex > 0) {
                    loadUp();
                }
            }
            primesAdapter.setOffset(firstItemIndex);
        }

        private void loadUp() {
            //Log.d(TAG, "Size before: " + primesAdapter.getPrimes().size());

            //Remove items from end
            final Iterator<Long> iterator = primesAdapter.getPrimes().iterator();
            final int size = primesAdapter.getItemCount();
            for (int i = 0; i < size; i++) {
                iterator.next();
                if (i >= size - INCREMENT) {
                    iterator.remove();
                }
            }
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    primesAdapter.notifyItemRangeRemoved(size - INCREMENT, INCREMENT);
                }
            });

            //Add items to beginning
            Log.d(TAG, "Adding from " + (firstItemIndex - INCREMENT));
            final List<Long> numbers = FileManager.readNumbers(file, firstItemIndex - INCREMENT, INCREMENT);
            for (int i = numbers.size() - 1; i >= 0; i--) {
                primesAdapter.getPrimes().add(0, numbers.get(i));
            }
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    primesAdapter.notifyItemRangeInserted(0, numbers.size());
                }
            });

            firstItemIndex -= numbers.size();
            // Log.d(TAG, "Size after: " + primesAdapter.getPrimes().size());
        }

        private void loadDown() {
            //Log.d(TAG, "Size before: " + primesAdapter.getPrimes().size());

            //Read new items
            final List<Long> numbers = new ArrayList<>();
            final boolean endOfFile = FileManager.readNumbers(file, numbers, firstItemIndex + totalItemCount, INCREMENT);

            if (!endOfFile) {
                //Remove items
                final Iterator<Long> iterator = primesAdapter.getPrimes().iterator();
                for (int i = 0; i < INCREMENT; i++) {
                    iterator.next();
                    iterator.remove();
                }
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        primesAdapter.notifyItemRangeRemoved(0, INCREMENT);
                    }
                });
            }

            //Add new items
            primesAdapter.getPrimes().addAll(numbers);
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    primesAdapter.notifyItemRangeInserted(primesAdapter.getItemCount(), numbers.size());
                }
            });

            if (!endOfFile) {
                firstItemIndex += numbers.size();
            }

            //Log.d(TAG, "Size after: " + primesAdapter.getPrimes().size());
        }

        private void specialScrollToPosition(final int position, final boolean animate) {
            //Scroll to correct position
            int startIndex = (position / scrollListener.INCREMENT) * (scrollListener.INCREMENT);
            final List<Long> numbers = new ArrayList<>();

            //Try to read from start
            startIndex -= INCREMENT;
            if (startIndex < 0) {
                startIndex = 0;
            }
            final boolean endOfFile = FileManager.readNumbers(file, numbers, startIndex, primesAdapter.getItemCount());

            if (endOfFile) {
                int previousSize = 0;
                while (FileManager.readNumbers(file, numbers, startIndex, primesAdapter.getItemCount()) && previousSize != numbers.size()) {
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                    final int pos = position - scrollListener.firstItemIndex;
                    recyclerView.scrollToPosition(pos);
                    if (animate) {
                        primesAdapter.animate(pos);
                    }
                }
            });
        }

        public void setTotalNumbers(final int count) {
            this.totalNumbers = count;
        }

        public int getTotalNumbers() {
            return this.totalNumbers;
        }

        public void setFirstItemIndex(final int index) {
            firstItemIndex = index;
            primesAdapter.setOffset(index);
        }
    }

    @Override
    protected void loadFile(final File file) {
        //Load file in another thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Count numbers
                final int totalNumbers = FileManager.countTotalNumbersQuick(file);
                scrollListener.setTotalNumbers(totalNumbers);

                //Read file
                final List<Long> numbers = FileManager.readNumbers(file, 0, 1000);
                primesAdapter.getPrimes().addAll(numbers);

                //Get total range
                final long[] range;
                if (getIntent().getLongArrayExtra("range") != null) {
                    range = getIntent().getLongArrayExtra("range");
                } else {
                    range = FileManager.getPrimesRangeFromTitle(file);
                }

                //Update UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //If there are no numbers, there was probably an error
                        if (numbers.size() == 0 && totalNumbers > 0){
                            showLoadingError();
                        }else {
                            //Set header text
                            headerTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.find_primes_subtitle_result, totalNumbers), new String[]{
                                    NUMBER_FORMAT.format(totalNumbers),
                                    NUMBER_FORMAT.format(range[0]),
                                    range[1] == FindPrimesTask.INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(range[1]),
                            }, ContextCompat.getColor(getBaseContext(), R.color.purple_inverse)));

                            resizeCollapsingToolbar();

                            //Update adapter
                            primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                            recyclerView.post(new Runnable() {

                                /**
                                 * Minimum number of extra adapter items before displaying the
                                 * scroll to top and scroll to bottom buttons.
                                 */
                                private final int SCROLL_BUTTON_MIN_OVERFLOW = 20;

                                @Override
                                public void run() {
                                    final int visibility = ((linearLayoutManager.findLastVisibleItemPosition() - linearLayoutManager.findFirstVisibleItemPosition()) >= scrollListener.totalNumbers - SCROLL_BUTTON_MIN_OVERFLOW) ? View.GONE : View.VISIBLE;
                                    scrollToTopFab.setVisibility(visibility);
                                    scrollToBottomFab.setVisibility(visibility);
                                    findButton.setVisible(enableSearch && visibility == View.VISIBLE);
                                }
                            });
                        }

                        //Dismiss loading dialog
                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);
        findButton = menu.findItem(R.id.find);
        findButton.setVisible(enableSearch);
        menu.findItem(R.id.export).setVisible(allowExport);
        menu.findItem(R.id.delete).setVisible(allowDelete);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.find:
                final FindNthNumberDialog findNthNumberDialog = new FindNthNumberDialog(this, scrollListener.totalNumbers);
                findNthNumberDialog.addListener(new FindNthNumberDialog.OnFindClickedListener() {
                    @Override
                    public void onFindClicked(final int number) {
                        if (number > 0 && number <= scrollListener.getTotalNumbers()) {
                            appBarLayout.setExpanded(false);
                            scrollListener.specialScrollToPosition(number - 1, true);
                        } else {
                            Toast.makeText(DisplayPrimesActivity.this, "Invalid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                findNthNumberDialog.show();
                break;

            case R.id.export:
                final ExportOptionsDialog exportOptionsDialog = new ExportOptionsDialog(this, file, R.style.FindPrimes_Dialog);
                exportOptionsDialog.show();
                break;

            case R.id.delete:
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage("Are you sure you want to delete this saved file?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                alertDialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }
}
