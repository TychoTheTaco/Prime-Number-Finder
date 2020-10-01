package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.activities.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SelectableAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.sort.SortPopupWindow;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.ArrayList;
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

    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;

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
                setTheme(R.style.FindPrimes);
                break;

            case FACTORS:
                setTheme(R.style.FindFactors);
                break;

            case TREE:
                setTheme(R.style.PrimeFactorization);
                break;
        }

        //Set content
        setContentView(R.layout.saved_files_list_activity);

        //Set up adapter
        adapterSavedFilesList = new SavedFilesListAdapter(this, directory);
        adapterSavedFilesList.addOnSelectionStateChangedListener(new SelectableAdapter.OnSelectionStateChangedListener() {

            private int defaultScrollFlags;

            private boolean locked = false;

            @Override
            public void onStartSelection() {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.round_clear_24);
                menu.findItem(R.id.delete).setVisible(true);
                menu.findItem(R.id.sort).setVisible(false);
                appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener);
                appBarLayout.setExpanded(true);
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

                //Restore scroll flags
                appBarLayout.removeOnOffsetChangedListener(onOffsetChangedListener);
                final AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
                layoutParams.setScrollFlags(defaultScrollFlags);
                collapsingToolbarLayout.setLayoutParams(layoutParams);
                locked = false;
            }

            private final AppBarLayout.OnOffsetChangedListener onOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (verticalOffset == 0 && !locked){
                        //Lock action bar
                        final AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
                        defaultScrollFlags = layoutParams.getScrollFlags();
                        layoutParams.setScrollFlags(0);
                        collapsingToolbarLayout.setLayoutParams(layoutParams);
                        locked = true;
                    }
                }
            };
        });

        //Set the actionbar to a custom toolbar
        appBarLayout = findViewById(R.id.app_bar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (fileType) {
            case PRIMES:
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
                break;

            case FACTORS:
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));
                break;

            case TREE:
                Utils.applyTheme(this, ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));
                break;
        }

        //Set up subtitle
        subTitleTextView = findViewById(R.id.subtitle);
        totalSizeTextView = findViewById(R.id.right_message);
        toolbar.post(this::updateSubtitle);

        //Set up toolbar animation
        final ViewGroup expandedLayout = findViewById(R.id.expanded_layout);
        ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            final int height = appBarLayout.getTotalScrollRange();
            expandedLayout.setAlpha(1.0f - ((float) -verticalOffset) / height);
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
                sortPopupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                sortPopupWindow.showAsDropDown(findViewById(R.id.sort), -(sortPopupWindow.getContentView().getMeasuredWidth() - (int) Utils.dpToPx(this, 45)), (int) Utils.dpToPx(this, -48));
                sortPopupWindow.setSearchMethod(lastSortMethod != null ? lastSortMethod : SortPopupWindow.SortMethod.DATE, lasSortAscending);
                break;

            case R.id.delete:
                //Show warning dialog
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Warning");
                alertDialog.setMessage(getResources().getQuantityString(R.plurals.delete_warning, adapterSavedFilesList.getSelectedItemCount(), adapterSavedFilesList.getSelectedItemCount()));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                        (dialog, which) -> {

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

                            if (adapterSavedFilesList.getItemCount() == 0) {
                                finish();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        (dialog, which) -> alertDialog.dismiss());
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