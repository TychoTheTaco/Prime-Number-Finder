package com.tycho.app.primenumberfinder.modules.savedfiles.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.modules.savedfiles.FindNthNumberDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayPrimesActivity extends AppCompatActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "DispFactListAct";

    private File file;

    private RecyclerView recyclerView;

    private PrimesAdapter primesAdapter;

    private MenuItem findButton;

    private final Handler handler = new Handler(Looper.getMainLooper());

    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    private final CustomScrollListener scrollListener = new CustomScrollListener();

    private boolean allowExport;
    private boolean enableSearch;

    private FloatingActionButton scrollToTopFab;
    private FloatingActionButton scrollToBottomFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_primes_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyThemeColor(ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null) {

            //Get extras from the intent
            final Bundle extras = intent.getExtras();
            if (extras != null) {

                //Get the file path from the extras
                final String filePath = extras.getString("filePath");
                if (filePath != null) {

                    file = new File(filePath);

                    //Set up adapter
                    primesAdapter = new PrimesAdapter(this);

                    //Set up RecyclerView
                    recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(primesAdapter);
                    recyclerView.setItemAnimator(null);
                    recyclerView.addOnScrollListener(scrollListener);

                    loadFile(file);

                    if (extras.getBoolean("title", true)) {
                        setTitle(formatTitle(file.getName().split("\\.")[0]));
                    }

                    allowExport = extras.getBoolean("allowExport", false);
                    enableSearch = extras.getBoolean("enableSearch", false);

                    //Set up floating action buttons
                    scrollToTopFab = findViewById(R.id.scroll_to_top_fab);
                    scrollToTopFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            scrollListener.specialScrollToPosition(0);
                        }
                    });

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
                Log.e(TAG, "Intent had no extras!");
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

        private volatile boolean loading = false;

        private final int INCREMENT = 250;
        private int firstItemIndex;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            if (totalItemCount - 1 <= (lastVisibleItem + visibleThreshold)) {
                //Log.d(TAG, "TRIGGERED DOWN");
                loadDown();
            } else if (linearLayoutManager.findFirstVisibleItemPosition() <= visibleThreshold) {
                if (firstItemIndex > 0) {
                    //Log.d(TAG, "TRIGGERED DOWN");
                    loadUp();
                }
            }
            primesAdapter.setOffset(firstItemIndex);
        }

        private void loadUp() {
            loading = true;
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
                    loading = false;
                }
            });

            firstItemIndex -= numbers.size();
           // Log.d(TAG, "Size after: " + primesAdapter.getPrimes().size());
        }

        private void loadDown() {
            loading = true;
            //Log.d(TAG, "Size before: " + primesAdapter.getPrimes().size());

            //Read new items
            final List<Long> numbers = new ArrayList<>();
            final boolean endOfFile = FileManager.readNumbers(file, numbers, firstItemIndex + totalItemCount, INCREMENT);

            if (!endOfFile){
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
                    loading = false;
                }
            });

            if (!endOfFile){
                firstItemIndex += numbers.size();
            }

            //Log.d(TAG, "Size after: " + primesAdapter.getPrimes().size());
        }

        private void specialScrollToPosition(final int position) {
            loading = true;
            Log.d(TAG, "specialScrollTo: " + position);

            //Scroll to correct position
            int startIndex = (position / scrollListener.INCREMENT) * (scrollListener.INCREMENT);
            final List<Long> numbers = new ArrayList<>();

            //Try to read from start
            startIndex -= INCREMENT;
            if (startIndex < 0){
                startIndex = 0;
            }
            final boolean endOfFile = FileManager.readNumbers(file, numbers, startIndex, primesAdapter.getItemCount());

            if (endOfFile){
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
                    loading = false;
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

                scrollListener.setTotalNumbers(FileManager.countTotalNumbersQuick(file));
                final List<Long> numbers = FileManager.readNumbers(file, 0, 1000);
                primesAdapter.getPrimes().addAll(numbers);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);
        findButton = menu.findItem(R.id.find);
        findButton.setVisible(enableSearch);
        menu.findItem(R.id.export).setVisible(allowExport);
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
                final ExportOptionsDialog exportOptionsDialog = new ExportOptionsDialog(this, file);
                exportOptionsDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrimeNumberFinder.getTaskManager().resumeAllTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrimeNumberFinder.getTaskManager().pauseAllTasks();
    }
}
