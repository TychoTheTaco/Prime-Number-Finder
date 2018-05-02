package com.tycho.app.primenumberfinder.modules.savedfiles.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.modules.savedfiles.FindNthNumberDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayPrimesActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayPrimesActivity.class.getSimpleName();

    private File file;

    private TextView headerTextView;

    private RecyclerView recyclerView;

    private PrimesAdapter primesAdapter;

    private MenuItem findButton;

    private final Handler handler = new Handler(Looper.getMainLooper());

    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    private final CustomScrollListener scrollListener = new CustomScrollListener();

    private boolean allowExport;
    private boolean enableSearch;
    private boolean allowDelete;

    private FloatingActionButton scrollToTopFab;
    private FloatingActionButton scrollToBottomFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_primes_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.FindPrimes_PopupOverlay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null) {

            //Get the file path from the extras
            final String filePath = intent.getStringExtra("filePath");
            if (filePath != null) {

                file = new File(filePath);

                //Set a custom title if there is one
                if (intent.getBooleanExtra("title", true)) {
                    setTitle(formatTitle(file.getName().split("\\.")[0]));
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
                headerTextView = findViewById(R.id.text);

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
                        scrollListener.specialScrollToPosition(0);
                    }
                });

                //Set up scroll to bottom button
                scrollToBottomFab = findViewById(R.id.scroll_to_bottom_fab);
                scrollToBottomFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scrollListener.specialScrollToPosition(scrollListener.totalNumbers - 1);
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

        private void specialScrollToPosition(final int position) {
            Log.d(TAG, "specialScrollTo: " + position);

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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                    recyclerView.scrollToPosition(position - scrollListener.firstItemIndex);
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

    private void loadFile(final File file) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Load file in another thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                final int totalNumbers = FileManager.countTotalNumbersQuick(file);
                scrollListener.setTotalNumbers(totalNumbers);

                final List<Long> numbers = FileManager.readNumbers(file, 0, 1000);
                primesAdapter.getPrimes().addAll(numbers);

                final long[] range;
                if (getIntent().getLongArrayExtra("range") != null){
                    range = getIntent().getLongArrayExtra("range");
                }else{
                    range = FileManager.getPrimesRangeFromTitle(file);
                }

                //Set header text
                headerTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getString(R.string.find_primes_subtitle_result), new String[]{
                        NUMBER_FORMAT.format(totalNumbers),
                        NUMBER_FORMAT.format(range[0]),
                        range[1] == FindPrimesTask.INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(range[1]),
                }, ContextCompat.getColor(getBaseContext(), R.color.purple_inverse)));

                //Set correct height based on the height of the header text view
                headerTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        final int defaultHeight = getSupportActionBar().getHeight();
                        final int textHeight = headerTextView.getHeight();

                        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.homeCollapseToolbar);
                        final AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
                        layoutParams.height = (int) (defaultHeight + textHeight + Utils.dpToPx(getBaseContext(), 12.5f));
                        collapsingToolbarLayout.setLayoutParams(layoutParams);
                    }
                });

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                        progressDialog.dismiss();

                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                final int visibility = ((linearLayoutManager.findLastVisibleItemPosition() - linearLayoutManager.findFirstVisibleItemPosition()) == scrollListener.totalNumbers - 1) ? View.GONE : View.VISIBLE;
                                scrollToTopFab.setVisibility(visibility);
                                scrollToBottomFab.setVisibility(visibility);
                                findButton.setVisible(enableSearch && visibility == View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private String formatTitle(final String string) {

        try {
            //Replace all the numbers
            String replaceNumbers = string.replaceAll("[0-9]+", "<number>");

            //Replace all the text
            String onlyNumbers = string.replaceAll("[^0-9]+", "<text>");

            //Get all numbers from the string
            String numbers[] = onlyNumbers.trim().split("<text>");
            final List<Long> formattedNumbers = new ArrayList<>();
            for (String numberString : numbers) {
                if (!numberString.equals("")) {
                    formattedNumbers.add(Long.valueOf(numberString));
                }
            }

            //Replace all place holders with formatted numbers
            String title = replaceNumbers;
            for (int i = 0; i < formattedNumbers.size(); i++) {
                title = title.replaceFirst("<number>", NumberFormat.getInstance().format(formattedNumbers.get(i)));
            }

            return title;
        } catch (Exception e) {
        }

        return string;
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
                        if (number > 0 && number < scrollListener.getTotalNumbers()) {
                            scrollListener.specialScrollToPosition(number - 1);
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
}
