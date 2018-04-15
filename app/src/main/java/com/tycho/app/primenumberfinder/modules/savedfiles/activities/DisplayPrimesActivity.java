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
                            specialScrollToPosition(0);
                        }
                    });

                    scrollToBottomFab = findViewById(R.id.scroll_to_bottom_fab);
                    scrollToBottomFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            specialScrollToPosition(scrollListener.totalNumbers - 1);
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
        private boolean loading = false;

        private final int INCREMENT = 250;
        private final int[] range = new int[]{0, INCREMENT - 1};

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            if (!loading) {
                if (totalItemCount - 1 <= (lastVisibleItem + visibleThreshold)) {
                    loading = true;
                    loadMore(true);
                    loading = false;
                } else if (linearLayoutManager.findFirstVisibleItemPosition() <= visibleThreshold) {
                    loading = true;
                    loadMore(false);
                    loading = false;
                }
            }
        }

        public void setTotalNumbers(final int count) {
            this.totalNumbers = count;
        }

        public int getTotalNumbers() {
            return this.totalNumbers;
        }

        public void setRange(final int start, final int end) {
            range[0] = start;
            range[1] = end;
            primesAdapter.setOffset(start);
        }

        private void loadMore(final boolean end) {
            if (end && range[1] >= totalNumbers - 1) {
                return;
            }
            if (!end && range[0] == 0) {
                return;
            }
            //Log.d(TAG, "Loading more...");

            Log.d(TAG, "Before: " + primesAdapter.getPrimes().size());
            if (end) {

                //Remove first half
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

                //Log.d(TAG, "After remove: " + primesAdapter.getPrimes());

                //Add
                //Log.d(TAG, "Before add range[1] = " + range[1]);
                final List<Long> numbers = FileManager.readNumbers(file, range[1] + 1, INCREMENT);
                //Log.d(TAG, "First added: " + numbers.get(0));
                primesAdapter.getPrimes().addAll(numbers);
                //Log.d(TAG, "Added " + numbers.size());
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        primesAdapter.notifyItemRangeInserted(range[1] + 1, numbers.size());
                    }
                });

                range[0] += INCREMENT;
                range[1] += numbers.size();
            } else {
                final List<Long> numbers = FileManager.readNumbers(file, range[0] - INCREMENT, INCREMENT);

                //Remove last half
                final Iterator<Long> iterator = primesAdapter.getPrimes().iterator();
                final int size = primesAdapter.getPrimes().size();
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

                //Log.d(TAG, "After remove: " + primesAdapter.getPrimes());

                //Add
                for (int i = numbers.size() - 1; i >= 0; i--) {
                    primesAdapter.getPrimes().add(0, numbers.get(i));
                }

                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        primesAdapter.notifyItemRangeInserted(0, INCREMENT);
                    }
                });

                range[0] -= INCREMENT;
                range[1] -= INCREMENT;
            }
            primesAdapter.setOffset(range[0]);
            Log.d(TAG, "After: " + primesAdapter.getItemCount());
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
                Log.d(TAG, "Read " + numbers.size());
                scrollListener.setRange(0, numbers.size() - 1);
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
                            specialScrollToPosition(number - 1);
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

    private void specialScrollToPosition(final int position) {

        //Scroll to correct position
        int startIndex = (position / scrollListener.INCREMENT) * (scrollListener.INCREMENT);
        final List<Long> numbers = new ArrayList<>();
        boolean endOfFile = FileManager.readNumbers(file, numbers, startIndex, 1000);
        int extra = 0;
        while (!endOfFile && numbers.size() < (scrollListener.INCREMENT * 3)){
            startIndex -= scrollListener.INCREMENT;
            extra += scrollListener.INCREMENT;
            numbers.clear();
            endOfFile = FileManager.readNumbers(file, numbers, startIndex, 1000);
        }
        final int extraAdded = extra;
        scrollListener.setRange(startIndex, startIndex + numbers.size());
        primesAdapter.getPrimes().clear();
        primesAdapter.getPrimes().addAll(numbers);
        handler.post(new Runnable() {
            @Override
            public void run() {
                primesAdapter.notifyItemRangeInserted(0, primesAdapter.getItemCount());
                recyclerView.scrollToPosition(position % scrollListener.INCREMENT + extraAdded);
            }
        });
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
