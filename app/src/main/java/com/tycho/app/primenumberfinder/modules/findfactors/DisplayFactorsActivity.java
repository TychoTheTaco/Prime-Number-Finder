package com.tycho.app.primenumberfinder.modules.findfactors;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayFactorsActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "DispFactListAct";

    private File file;

    private TextView headerTextView;

    private RecyclerView recyclerView;

    private FactorsListAdapter adapter;

    private boolean allowExport;
    private boolean allowDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_factors_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.FindFactors_PopupOverlay);
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
                adapter = new FactorsListAdapter(this);
                final long number = intent.getLongExtra("number",0);
                if (number != 0) {
                    adapter.setNumber(number);
                }

                //Set up RecyclerView
                recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
                recyclerView.setItemAnimator(null);

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
                allowDelete = intent.getBooleanExtra("allowDelete", false);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_content_activity_menu, menu);
        menu.findItem(R.id.find).setVisible(false);
        menu.findItem(R.id.export).setVisible(allowExport);
        menu.findItem(R.id.delete).setVisible(allowDelete);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.export:
                final ExportOptionsDialog exportOptionsDialog = new ExportOptionsDialog(this, file, R.style.FindFactors_Dialog);
                exportOptionsDialog.show();
                break;

            case android.R.id.home:
                onBackPressed();
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

    private void loadFile(final File file) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Load file in another thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                final List<Long> numbers = FileManager.readNumbers(file);
                adapter.getFactors().addAll(numbers);

                //Set header text
                headerTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.find_factors_subtitle_results, numbers.size()), new String[]{
                        NUMBER_FORMAT.format(numbers.get(numbers.size() - 1)),
                        NUMBER_FORMAT.format(numbers.size()),
                }, ContextCompat.getColor(getBaseContext(), R.color.orange_inverse)));

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

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemRangeInserted(0, adapter.getItemCount());
                        progressDialog.dismiss();
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
}
