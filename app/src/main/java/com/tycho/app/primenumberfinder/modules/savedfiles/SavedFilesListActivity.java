package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SelectableAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.sort.SortPopupWindow;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tycho Bellers
 * Date Created: 11/5/2016
 */
public class SavedFilesListActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesListActivity.class.getSimpleName();

    private TextView subTitleTextView;
    private TextView totalSizeTextView;

    SavedFilesListAdapter adapterSavedFilesList;

    private Menu menu;

    private FileType fileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply custom theme depending on directory
        final File directory = (File) getIntent().getSerializableExtra("directory");
        fileType = FileManager.getFileType(directory);
        switch (fileType) {
            case PRIMES:
                setTheme(R.style.FindPrimes_Activity);
                break;

            case FACTORS:
                setTheme(R.style.FindFactors_Activity);
                break;

            case TREE:
                setTheme(R.style.PrimeFactorization_Activity);
                break;
        }

        //Set content
        setContentView(R.layout.saved_files_list_activity);

        //Set up adapter
        adapterSavedFilesList = new SavedFilesListAdapter(this, directory);
        adapterSavedFilesList.addOnSelectionStateChangedListener(new SelectableAdapter.OnSelectionStateChangedListener() {

            @Override
            public void onStartSelection() {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.round_clear_24);
                menu.findItem(R.id.delete).setVisible(true);
                menu.findItem(R.id.sort).setVisible(false);
            }

            @Override
            public void onItemSelected() {
                updateSubtitle();
            }

            @Override
            public void onItemDeselected() {
                updateSubtitle();
            }

            @Override
            public void onStopSelection() {
                getSupportActionBar().setHomeAsUpIndicator(0);
                menu.findItem(R.id.delete).setVisible(false);
                menu.findItem(R.id.sort).setVisible(true);
                updateSubtitle();
            }
        });

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up subtitle
        subTitleTextView = findViewById(R.id.subtitle);
        totalSizeTextView = findViewById(R.id.right_message);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                updateSubtitle();
            }
        });

        //Set up toolbar animation
        final ViewGroup expandedLayout = findViewById(R.id.expanded_layout);
        ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                final int height = appBarLayout.getTotalScrollRange();
                expandedLayout.setAlpha(1.0f - ((float) -verticalOffset) / height);
            }
        });

        switch (fileType) {
            case PRIMES:
                setTitle("Prime Numbers");
                toolbar.setPopupTheme(R.style.FindPrimes_PopupOverlay);
                break;

            case FACTORS:
                setTitle("Factors");
                toolbar.setPopupTheme(R.style.FindFactors_PopupOverlay);
                break;

            case TREE:
                setTitle("Factor Trees");
                toolbar.setPopupTheme(R.style.PrimeFactorization_PopupOverlay);
                break;
        }

        //Set up the RecyclerView
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterSavedFilesList);
        recyclerView.setItemAnimator(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.saved_files_activity_menu, menu);
        this.menu = menu;
        return true;
    }

    private SortPopupWindow.SortMethod lastSortMethod;
    private boolean lasSortAscending;

    private SortPopupWindow sortPopupWindow;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sort:
                final int color;

                final List<SortPopupWindow.SortMethod> sortMethods = new ArrayList<>();
                sortMethods.add(SortPopupWindow.SortMethod.DATE);
                sortMethods.add(SortPopupWindow.SortMethod.FILE_SIZE);

                switch (fileType) {
                    case PRIMES:
                        color = ContextCompat.getColor(this, R.color.purple_light);
                        sortMethods.add(SortPopupWindow.SortMethod.SEARCH_RANGE_START);
                        break;

                    case FACTORS:
                        color = ContextCompat.getColor(this, R.color.orange_light);
                        sortMethods.add(SortPopupWindow.SortMethod.NUMBER);
                        break;

                    case TREE:
                        color = ContextCompat.getColor(this, R.color.green_light);
                        sortMethods.add(SortPopupWindow.SortMethod.NUMBER);
                        break;

                    default:
                        color = ContextCompat.getColor(this, R.color.primary_light);
                        break;
                }

                sortPopupWindow = new SortPopupWindow(this, color, sortMethods) {
                    @Override
                    protected void onSortMethodSelected(SortMethod sortMethod, boolean ascending) {
                        switch (sortMethod) {
                            case DATE:
                                adapterSavedFilesList.sortDate(ascending);
                                break;

                            case FILE_SIZE:
                                adapterSavedFilesList.sortSize(ascending);
                                break;

                            case SEARCH_RANGE_START:
                                adapterSavedFilesList.sortSearchRange(ascending);
                                break;

                            case NUMBER:
                                adapterSavedFilesList.sortNumber(ascending);
                                break;
                        }
                        lastSortMethod = sortMethod;
                        lasSortAscending = ascending;
                    }
                };
                sortPopupWindow.setFocusable(true);
                sortPopupWindow.setOutsideTouchable(true);
                sortPopupWindow.showAsDropDown(findViewById(R.id.sort), (int) Utils.dpToPx(this, -106), (int) Utils.dpToPx(this, -48));
                sortPopupWindow.setSearchMethod(lastSortMethod != null ? lastSortMethod : SortPopupWindow.SortMethod.DATE, lasSortAscending);
                break;

            case R.id.delete:
                //Show warning dialog
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage(getResources().getQuantityString(R.plurals.delete_warning, adapterSavedFilesList.getSelectedItemCount(), adapterSavedFilesList.getSelectedItemCount()));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                //Get the files to be deleted
                                final int[] selectedItemIndexes = adapterSavedFilesList.getSelectedItemIndexes();
                                final List<File> files = new ArrayList<>();
                                for (int i : selectedItemIndexes) {
                                    files.add(adapterSavedFilesList.getFiles().get(i));
                                }

                                final Iterator<File> iterator = files.iterator();
                                int position = 0;
                                while (iterator.hasNext()) {
                                    final File file = iterator.next();

                                    //Delete the file
                                    file.delete();
                                    adapterSavedFilesList.getFiles().remove(file);
                                    adapterSavedFilesList.notifyItemRemoved(selectedItemIndexes[position] - position);
                                    position++;
                                }
                                adapterSavedFilesList.setSelectionMode(false);
                                alertDialog.dismiss();
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

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Cancel selection mode
        if (adapterSavedFilesList.isSelectionMode()) {
            adapterSavedFilesList.setSelectionMode(false);
            return;
        }

        super.onBackPressed();
    }

    private void updateSubtitle() {
        if (adapterSavedFilesList.isSelectionMode()) {
            subTitleTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.selected_item_count, adapterSavedFilesList.getItemCount()), new String[]{
                    NUMBER_FORMAT.format(adapterSavedFilesList.getSelectedItemCount()),
                    NUMBER_FORMAT.format(adapterSavedFilesList.getItemCount())
            }, Color.WHITE));
            totalSizeTextView.setText(Utils.humanReadableByteCount(adapterSavedFilesList.getSelectedSize(), true) + " / " + Utils.humanReadableByteCount(adapterSavedFilesList.getTotalStorage(), true));
        } else {
            subTitleTextView.setText(Utils.formatSpannable(new SpannableStringBuilder(), getResources().getQuantityString(R.plurals.saved_files_count, adapterSavedFilesList.getItemCount()), new String[]{
                    NUMBER_FORMAT.format(adapterSavedFilesList.getItemCount())
            }, Color.WHITE));
            totalSizeTextView.setText(Utils.humanReadableByteCount(adapterSavedFilesList.getTotalStorage(), true));
        }

        //Set correct height based on the height of the header text view
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        Utils.reLayoutChildren(collapsingToolbarLayout);
        final int defaultHeight = getSupportActionBar().getHeight();
        final int textHeight = subTitleTextView.getHeight();
        final ViewGroup.LayoutParams layoutParams = collapsingToolbarLayout.getLayoutParams();
        layoutParams.height = defaultHeight + textHeight;
        collapsingToolbarLayout.setLayoutParams(layoutParams);
    }
}