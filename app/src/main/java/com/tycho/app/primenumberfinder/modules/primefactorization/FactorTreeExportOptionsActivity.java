package com.tycho.app.primenumberfinder.modules.primefactorization;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

    private TextView imageBackgroundColorTextView;
    private TextView itemTextColorTextView;
    private TextView itemBackgroundColorTextView;
    private TextView primeFactorTextColorTextView;
    private TextView branchColorTextView;
    private TextView itemBorderColorTextView;

    private TreeView.ExportOptions exportOptions;

    private int selectedItemIndex = -1;

    private ValidEditText verticalItemSpacing;
    private ValidEditText itemTextSize;
    private ValidEditText branchWidth;
    private ValidEditText itemBorderWidth;

    private RangedSeekBar verticalItemSpacingSeekBar;
    private RangedSeekBar itemTextSizeSeekBar;
    private RangedSeekBar branchWidthSeekBar;
    private RangedSeekBar itemBorderWidthSeekBar;

    private TextView imageSizeTextView;

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

                //Vertical item spacing
                verticalItemSpacing = findViewById(R.id.vertical_item_spacing);
                verticalItemSpacing.setHint(exportOptions.verticalSpacing);
                verticalItemSpacing.setNumber(exportOptions.verticalSpacing);
                verticalItemSpacing.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        final int value = verticalItemSpacing.getIntValue();
                        verticalItemSpacing.setValid(verticalItemSpacing.length() > 0 && value >= 0 && value <= 400);

                        verticalItemSpacingSeekBar.setValue(value);
                    }
                });
                verticalItemSpacingSeekBar = findViewById(R.id.vertical_item_spacing_seekbar);
                verticalItemSpacingSeekBar.setRange(0, 400);
                verticalItemSpacingSeekBar.setValue(exportOptions.verticalSpacing);
                verticalItemSpacingSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangedAdapter() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        exportOptions.verticalSpacing = verticalItemSpacingSeekBar.getFloatValue();
                        if (fromUser) {
                            verticalItemSpacing.setNumber(exportOptions.verticalSpacing);
                        }
                        treeView.recalculate();
                        waitAndUpdateDimensions();
                    }
                });

                //Image background color
                imageBackgroundColorTextView = findViewById(R.id.image_background_color);
                imageBackgroundColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.imageBackgroundColor));
                imageBackgroundColorTextView.setText(getString(R.string.hex, Integer.toHexString(exportOptions.imageBackgroundColor).toUpperCase()));
                imageBackgroundColorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.imageBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 0;
                    }
                });

                //Item text color
                itemTextColorTextView = findViewById(R.id.item_text_color);
                itemTextColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.itemTextColor));
                itemTextColorTextView.setText(getString(R.string.hex, Integer.toHexString(exportOptions.itemTextColor).toUpperCase()));
                itemTextColorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.itemTextColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 1;
                    }
                });

                //Item background color
                itemBackgroundColorTextView = findViewById(R.id.item_background_color);
                itemBackgroundColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.itemBackgroundColor));
                itemBackgroundColorTextView.setText(getString(R.string.hex, Integer.toHexString(exportOptions.itemBackgroundColor).toUpperCase()));
                itemBackgroundColorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.itemBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                        selectedItemIndex = 2;
                    }
                });

                //Prime factor text color
                primeFactorTextColorTextView = findViewById(R.id.prime_factor_text_color);
                primeFactorTextColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.primeFactorTextColor));
                primeFactorTextColorTextView.setText(getString(R.string.hex, Integer.toHexString(exportOptions.primeFactorTextColor).toUpperCase()));
                primeFactorTextColorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.primeFactorTextColor).setShowAlphaSlider(true).show((FactorTreeExportOptionsActivity.this));
                        selectedItemIndex = 3;
                    }
                });

                //Branch color
                branchColorTextView = findViewById(R.id.branch_color);
                branchColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.branchColor));
                branchColorTextView.setText(getString(R.string.hex, Integer.toHexString(exportOptions.branchColor).toUpperCase()));
                branchColorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.branchColor).setShowAlphaSlider(true).show((FactorTreeExportOptionsActivity.this));
                        selectedItemIndex = 4;
                    }
                });

                //Item border color
                itemBorderColorTextView = findViewById(R.id.item_border_color);
                itemBorderColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.itemBorderColor));
                itemBorderColorTextView.setText(getString(R.string.hex, Integer.toHexString(exportOptions.itemBorderColor).toUpperCase()));
                itemBorderColorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                        ColorPickerDialog.newBuilder().setColor(exportOptions.itemBorderColor).setShowAlphaSlider(true).show((FactorTreeExportOptionsActivity.this));
                        selectedItemIndex = 5;
                    }
                });

                imageSizeTextView = findViewById(R.id.image_size);
                imageSizeTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (treeView.isGenerated()) {
                            imageSizeTextView.setText("(" + NUMBER_FORMAT.format(treeView.getBoundingRect().width()) + " x " + NUMBER_FORMAT.format(Math.abs(treeView.getBoundingRect().height())) + ")");
                        }
                    }
                });

                //Item text size
                itemTextSize = findViewById(R.id.item_text_size);
                itemTextSize.setHintNumber(exportOptions.itemTextSize);
                itemTextSize.setNumber(exportOptions.itemTextSize);
                itemTextSize.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        final int value = itemTextSize.getIntValue();
                        itemTextSize.setValid(itemTextSize.length() > 0 && value >= 8 && value <= 100);

                        itemTextSizeSeekBar.setValue(value);
                    }
                });
                itemTextSizeSeekBar = findViewById(R.id.item_text_size_seekbar);
                itemTextSizeSeekBar.setRange(8, 100);
                itemTextSizeSeekBar.setSteps(10);
                itemTextSizeSeekBar.setValue(exportOptions.itemTextSize);
                itemTextSizeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangedAdapter() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        exportOptions.itemTextSize = itemTextSizeSeekBar.getIntValue();
                        if (fromUser) {
                            itemTextSize.setNumber(exportOptions.itemTextSize);
                        }

                        treeView.recalculate();
                        waitAndUpdateDimensions();
                    }
                });

                branchWidth = findViewById(R.id.branch_width);
                branchWidth.setHintNumber(exportOptions.branchWidth);
                branchWidth.setNumber(exportOptions.branchWidth);
                branchWidth.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        final float value = branchWidth.getFloatValue();
                        branchWidth.setValid(branchWidth.length() > 0 && value >= 1 && value <= 5);

                        branchWidthSeekBar.setValue(value);
                    }
                });
                branchWidthSeekBar = findViewById(R.id.branch_width_seekbar);
                branchWidthSeekBar.setRange(1, 5);
                branchWidthSeekBar.setSteps(10);
                branchWidthSeekBar.setValue(exportOptions.branchWidth);
                branchWidthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangedAdapter() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        exportOptions.branchWidth = branchWidthSeekBar.getFloatValue();
                        if (fromUser) {
                            branchWidth.setNumber(exportOptions.branchWidth);
                        }
                        treeView.redraw();
                    }
                });

                //Item border width
                itemBorderWidth = findViewById(R.id.item_border_width);
                itemBorderWidth.setHintNumber(exportOptions.itemBorderWidth);
                itemBorderWidth.setNumber(exportOptions.itemBorderWidth);
                itemBorderWidth.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        final float value = itemBorderWidth.getFloatValue();
                        itemBorderWidth.setValid(itemBorderWidth.length() > 0 && value >= 1 && value <= 5);

                        itemBorderWidthSeekBar.setValue(value);
                    }
                });
                itemBorderWidthSeekBar = findViewById(R.id.item_border_width_seekbar);
                itemBorderWidthSeekBar.setRange(1, 5);
                itemBorderWidthSeekBar.setSteps(1);
                itemBorderWidthSeekBar.setValue(exportOptions.branchWidth);
                itemBorderWidthSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangedAdapter() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        exportOptions.itemBorderWidth = itemBorderWidthSeekBar.getFloatValue();
                        if (fromUser) {
                            itemBorderWidth.setNumber(exportOptions.itemBorderWidth);
                        }
                        treeView.redraw();
                        //waitAndUpdateDimensions();
                    }
                });

                final CheckBox itemBackgroundsCheckbox = findViewById(R.id.item_background_checkbox);
                itemBackgroundsCheckbox.setChecked(exportOptions.itemBackgrounds);
                itemBackgroundsCheckbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Change dependent visibility
                        itemBackgroundColorTextView.setEnabled(itemBackgroundsCheckbox.isChecked());
                        itemBorderColorTextView.setEnabled(itemBackgroundsCheckbox.isChecked());
                        itemBorderWidth.setEnabled(itemBackgroundsCheckbox.isChecked());
                        itemBorderWidthSeekBar.setEnabled(itemBackgroundsCheckbox.isChecked());

                        exportOptions.itemBackgrounds = itemBackgroundsCheckbox.isChecked();
                        treeView.redraw();
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

    private ColorStateList generateColorStateList(final int color) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_enabled}},
                new int[]{
                        color,
                        color
                });
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

        final ColorStateList colorStateList = generateColorStateList(color);

        final String hex = "0x" + Integer.toHexString(color).toUpperCase();

        switch (selectedItemIndex) {

            case 0:
                imageBackgroundColorTextView.setBackgroundTintList(colorStateList);
                imageBackgroundColorTextView.setText(hex);
                exportOptions.imageBackgroundColor = color;
                break;

            case 1:
                itemTextColorTextView.setBackgroundTintList(colorStateList);
                itemTextColorTextView.setText(hex);
                exportOptions.itemTextColor = color;
                break;

            case 2:
                itemBackgroundColorTextView.setBackgroundTintList(colorStateList);
                itemBackgroundColorTextView.setText(hex);
                exportOptions.itemBackgroundColor = color;
                break;

            case 3:
                primeFactorTextColorTextView.setBackgroundTintList(colorStateList);
                primeFactorTextColorTextView.setText(hex);
                exportOptions.primeFactorTextColor = color;
                break;

            case 4:
                branchColorTextView.setBackgroundTintList(colorStateList);
                branchColorTextView.setText(hex);
                exportOptions.branchColor = color;
                break;

            case 5:
                itemBorderColorTextView.setBackgroundTintList(colorStateList);
                itemBorderColorTextView.setText(hex);
                exportOptions.itemBorderColor = color;
                break;
        }

        treeView.redraw();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private class OnSeekBarChangedAdapter implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            findViewById(R.id.root).requestFocus();
            Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private boolean checkInputs() {
        final ValidEditText[] editTexts = new ValidEditText[]{verticalItemSpacing, branchWidth, itemTextSize, itemBorderWidth};
        for (ValidEditText editText : editTexts) {
            if (!editText.isValid()) {
                return false;
            }
        }
        return true;
    }
}
