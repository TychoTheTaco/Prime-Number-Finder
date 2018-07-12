package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.ColorPickerListener;
import com.tycho.app.primenumberfinder.ui.RangedSeekBar;
import com.tycho.app.primenumberfinder.ui.TreeView;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import simpletrees.Tree;

/**
 * Created by tycho on 2/9/2018.
 */

public class FactorTreeExportOptionsActivity extends AbstractActivity implements ColorPickerListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FactorTreeExportOptionsActivity.class.getSimpleName();

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private File file;

    private Tree<Long> factorTree;

    private TreeView treeView;

    private TreeView.ExportOptions exportOptions;

    private int selectedItemIndex = -1;

    private TextView imageSizeTextView;

    private final List<Section> sections = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factor_tree_export_options_activity);

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null) {

            //Get the file path from the extras
            final String filePath = intent.getStringExtra("filePath");
            if (filePath != null) {

                //Set up the toolbar
                final Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                final EditText fileNameInput = findViewById(R.id.file_name);
                treeView = findViewById(R.id.factor_tree_preview);
                exportOptions = treeView.getDefaultExportOptions();

                //Image style
                final Section imageStyleSection = new Section(this, "Image Style");
                imageStyleSection.addOption(new ColorOption(this, "Background color", exportOptions.imageBackgroundColor){
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.imageBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 0;
                    }
                });
                imageStyleSection.addOption(new SliderOption(this, "Vertical item spacing", 0, 400, exportOptions.verticalSpacing){
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        super.onProgressChanged(seekBar, progress, fromUser);
                        exportOptions.verticalSpacing = rangedSeekBar.getFloatValue();
                        treeView.recalculate();
                        waitAndUpdateDimensions();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        findViewById(R.id.root).requestFocus();
                    }
                });

                //Branch style
                final Section branchStyleSection = new Section(this, "Branch Style");
                branchStyleSection.addOption(new ColorOption(this, "Color", exportOptions.branchColor){
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.branchColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 4;
                    }
                });
                branchStyleSection.addOption(new SliderOption(this, "Width", 1, 10, 10, exportOptions.branchWidth){
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        super.onProgressChanged(seekBar, progress, fromUser);
                        exportOptions.branchWidth = rangedSeekBar.getFloatValue();
                        treeView.redraw();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        findViewById(R.id.root).requestFocus();
                    }
                });

                //Text style
                final Section textStyleSection = new Section(this, "Text Style");
                textStyleSection.addOption(new ColorOption(this, "Text color", exportOptions.itemTextColor){
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.branchColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 1;
                    }
                });
                textStyleSection.addOption(new ColorOption(this, "Prime factor text color", exportOptions.primeFactorTextColor){
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(color).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 3;
                    }
                });
                textStyleSection.addOption(new SliderOption(this, "Text size", 8, 100, exportOptions.itemTextSize){
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        super.onProgressChanged(seekBar, progress, fromUser);
                        exportOptions.itemTextSize = rangedSeekBar.getIntValue();
                        treeView.recalculate();
                        waitAndUpdateDimensions();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        findViewById(R.id.root).requestFocus();
                    }
                });

                //Item background style
                final Section itemBackgroundSection = new Section(this, "Item Background Style");
                itemBackgroundSection.addOption(new CheckboxOption(this, "Item backgrounds", exportOptions.itemBackgrounds){
                    @Override
                    public void onClick(View v) {
                        super.onClick(v);

                        //Change dependent visibility
                        sections.get(3).getOptions().get(1).setEnabled(checkBox.isChecked());
                        sections.get(3).getOptions().get(2).setEnabled(checkBox.isChecked());
                        sections.get(3).getOptions().get(3).setEnabled(checkBox.isChecked());

                        exportOptions.itemBackgrounds = checkBox.isChecked();
                        treeView.redraw();
                    }
                });
                itemBackgroundSection.addOption(new ColorOption(this, "Background color", exportOptions.itemBackgroundColor){
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.itemBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 2;
                    }
                });
                itemBackgroundSection.addOption(new ColorOption(this, "Border color", exportOptions.itemBorderColor){
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.itemBorderColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 5;
                    }
                });
                itemBackgroundSection.addOption(new SliderOption(this, "Border width", 1, 10, 10, exportOptions.itemBorderWidth){
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        super.onProgressChanged(seekBar, progress, fromUser);
                        exportOptions.itemBorderWidth = rangedSeekBar.getFloatValue();
                        treeView.redraw();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        findViewById(R.id.root).requestFocus();
                    }
                });

                sections.add(imageStyleSection);
                sections.add(branchStyleSection);
                sections.add(textStyleSection);
                sections.add(itemBackgroundSection);

                //Add sections
                final ViewGroup optionsLayout = findViewById(R.id.options_layout);
                final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int) Utils.dpToPx(this, 24);
                for (Section section : sections){
                    final View view = section.inflate(null, false);
                    view.setLayoutParams(layoutParams);
                    optionsLayout.addView(view);
                }

                imageSizeTextView = findViewById(R.id.image_size);
                imageSizeTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (treeView.isGenerated()) {
                            imageSizeTextView.setText("(" + NUMBER_FORMAT.format(treeView.getBoundingRect().width()) + " x " + NUMBER_FORMAT.format(Math.abs(treeView.getBoundingRect().height())) + ")");
                        }
                    }
                });

                //Set up export button
                final Button exportButton = findViewById(R.id.export_button);
                exportButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkInputs()) {
                            final com.tycho.app.primenumberfinder.ProgressDialog progressDialog = new com.tycho.app.primenumberfinder.ProgressDialog(FactorTreeExportOptionsActivity.this);
                            progressDialog.setTitle("Exporting...");
                            progressDialog.show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    //Convert the file to the requested format
                                    final File image = new File(FileManager.getInstance().getExportCacheDirectory() + File.separator + fileNameInput.getText().toString().trim() + ".png");
                                    try {
                                        final OutputStream stream = new FileOutputStream(image);
                                        treeView.drawToBitmap(exportOptions).compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        stream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    progressDialog.dismiss();

                                    final Uri path = FileProvider.getUriForFile(FactorTreeExportOptionsActivity.this, "com.tycho.app.primenumberfinder", image);
                                    final Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_STREAM, path);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.setType("image/*");
                                    startActivity(intent);
                                }
                            }).start();
                        } else {
                            Toast.makeText(getBaseContext(), "Invalid inputs!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                //Read the tree from the file
                file = new File(filePath);
                loadFile(file);

                //Set up file name
                String name = file.getName();
                int pos = name.lastIndexOf(".");
                if (pos > 0) {
                    name = name.substring(0, pos);
                }
                fileNameInput.setText(name);

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
    public void onBackPressed() {
        super.onBackPressed();

        //Give the root view focus to prevent EditTexts from initially getting focus
        findViewById(R.id.root).requestFocus();
    }

    private void waitAndUpdateDimensions() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.d(TAG, "Waiting...");
                while (!treeView.isGenerated()) {
                    //TODO: This is bad
                }
                //Log.d(TAG, "Done waiting.");
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        imageSizeTextView.setText("(" + NUMBER_FORMAT.format(treeView.getBoundingRect().width()) + " x " + NUMBER_FORMAT.format(Math.abs(treeView.getBoundingRect().height())) + ")");
                    }
                });
            }
        }).start();
    }

    private void loadFile(final File file) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading file.");

        //Load file in another thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                factorTree = FileManager.getInstance().readTree(file);
                progressDialog.dismiss();

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        treeView.setTree(factorTree.formatNumbers());
                    }
                });

            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (selectedItemIndex) {
            case 0:
                exportOptions.imageBackgroundColor = color;
                ((ColorOption) sections.get(0).getOptions().get(0)).setColor(color);
                break;

            case 1:
                exportOptions.itemTextColor = color;
                ((ColorOption) sections.get(2).getOptions().get(0)).setColor(color);
                break;

            case 2:
                exportOptions.itemBackgroundColor = color;
                ((ColorOption) sections.get(3).getOptions().get(1)).setColor(color);
                break;

            case 3:
                exportOptions.primeFactorTextColor = color;
                ((ColorOption) sections.get(2).getOptions().get(1)).setColor(color);
                break;

            case 4:
                exportOptions.branchColor = color;
                ((ColorOption) sections.get(1).getOptions().get(0)).setColor(color);
                break;

            case 5:
                exportOptions.itemBorderColor = color;
                ((ColorOption) sections.get(3).getOptions().get(2)).setColor(color);
                break;
        }

        treeView.redraw();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private boolean checkInputs() {
        /*final ValidEditText[] editTexts = new ValidEditText[]{verticalItemSpacing, branchWidth, itemTextSize, itemBorderWidth};
        for (ValidEditText editText : editTexts) {
            if (!editText.isValid()) {
                return false;
            }
        }*/
        return true;
    }
}
