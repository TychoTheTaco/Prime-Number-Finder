package com.tycho.app.primenumberfinder.modules.findfactors;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.findprimes.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */

public class DisplayFactorsActivity extends DisplayContentActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = DisplayFactorsActivity.class.getSimpleName();

    private TextView headerTextView;

    private RecyclerView recyclerView;

    private FactorsListAdapter adapter;

    private boolean allowExport;
    private boolean allowDelete;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_factors_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.FindFactors_PopupOverlay);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

    @Override
    protected void loadFile(final File file) {
        //Load file in another thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                final List<Long> numbers = FileManager.readNumbers(file);
                adapter.getFactors().addAll(numbers);

                //Update UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //If there are no numbers, there was probably an error
                        if (numbers.size() == 0 && file.length() > 0){
                            showLoadingError();
                        }else {
                            //Set header text
                            headerTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.find_factors_subtitle_results, numbers.size()), new String[]{
                                    NUMBER_FORMAT.format(numbers.get(numbers.size() - 1)),
                                    NUMBER_FORMAT.format(numbers.size()),
                            }, ContextCompat.getColor(getBaseContext(), R.color.orange_inverse)));

                            resizeCollapsingToolbar();

                            //Update adapter
                            adapter.notifyItemRangeInserted(0, adapter.getItemCount());
                        }

                        progressDialog.dismiss();
                    }
                });
            }
        }).start();
    }
}
