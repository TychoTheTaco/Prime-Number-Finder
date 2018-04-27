package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.tycho.app.primenumberfinder.TreeView;
import com.tycho.app.primenumberfinder.utils.FileManager;

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

    private EditText verticalItemSpacing;
    private EditText itemTextSize;
    private EditText branchWidth;
    private EditText itemBorderWidth;

    private SeekBar verticalItemSpacingSeekBar;
    private SeekBar itemTextSizeSeekBar;
    private SeekBar branchWidthSeekBar;
    private SeekBar itemBorderWidthSeekBar;

    private TextView imageSizeTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factor_tree_export_options_activity);

        //Get the intent
        final Intent intent = getIntent();
        if (intent != null) {

            //Get extras from the intent
            final Bundle extras = intent.getExtras();
            if (extras != null) {

                //Get the file path from the extras
                final String filePath = extras.getString("filePath");
                if (filePath != null) {

                    //Set up the toolbar
                    final Toolbar toolbar = findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    final EditText fileNameInput = findViewById(R.id.file_name);

                    treeView = findViewById(R.id.factor_tree_preview);

                    exportOptions = treeView.getDefaultExportOptions();

                    imageBackgroundColorTextView = findViewById(R.id.image_background_color);
                    imageBackgroundColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.imageBackgroundColor));
                    imageBackgroundColorTextView.setText("0x" + Integer.toHexString(exportOptions.imageBackgroundColor).toUpperCase());
                    imageBackgroundColorTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ColorPickerDialog.newBuilder().setColor(exportOptions.imageBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                            selectedItemIndex = 0;
                        }
                    });

                    itemTextColorTextView = findViewById(R.id.item_text_color);
                    itemTextColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.itemTextColor));
                    itemTextColorTextView.setText("0x" + Integer.toHexString(exportOptions.itemTextColor).toUpperCase());
                    itemTextColorTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ColorPickerDialog.newBuilder().setColor(exportOptions.itemTextColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                            selectedItemIndex = 1;
                        }
                    });

                    itemBackgroundColorTextView = findViewById(R.id.item_background_color);
                    itemBackgroundColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.itemBackgroundColor));
                    itemBackgroundColorTextView.setText("0x" + Integer.toHexString(exportOptions.itemBackgroundColor).toUpperCase());
                    itemBackgroundColorTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ColorPickerDialog.newBuilder().setColor(exportOptions.itemBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                            selectedItemIndex = 2;
                        }
                    });

                    primeFactorTextColorTextView = findViewById(R.id.prime_factor_text_color);
                    primeFactorTextColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.primeFactorTextColor));
                    primeFactorTextColorTextView.setText("0x" + Integer.toHexString(exportOptions.primeFactorTextColor).toUpperCase());
                    primeFactorTextColorTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ColorPickerDialog.newBuilder().setColor(exportOptions.primeFactorTextColor).setShowAlphaSlider(true).show((FactorTreeExportOptionsActivity.this));
                            selectedItemIndex = 3;
                        }
                    });

                    branchColorTextView = findViewById(R.id.branch_color);
                    branchColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.branchColor));
                    branchColorTextView.setText("0x" + Integer.toHexString(exportOptions.branchColor).toUpperCase());
                    branchColorTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ColorPickerDialog.newBuilder().setColor(exportOptions.branchColor).setShowAlphaSlider(true).show((FactorTreeExportOptionsActivity.this));
                            selectedItemIndex = 4;
                        }
                    });

                    itemBorderColorTextView = findViewById(R.id.item_border_color);
                    itemBorderColorTextView.setBackgroundTintList(generateColorStateList(exportOptions.itemBorderColor));
                    itemBorderColorTextView.setText("0x" + Integer.toHexString(exportOptions.itemBorderColor).toUpperCase());
                    itemBorderColorTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ColorPickerDialog.newBuilder().setColor(exportOptions.itemBorderColor).setShowAlphaSlider(true).show((FactorTreeExportOptionsActivity.this));
                            selectedItemIndex = 5;
                        }
                    });

                    verticalItemSpacing = findViewById(R.id.vertical_item_spacing);
                    verticalItemSpacing.setText(String.valueOf((int) exportOptions.verticalSpacing));

                    imageSizeTextView = findViewById(R.id.image_size);
                    imageSizeTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageSizeTextView.setText("(" + NUMBER_FORMAT.format(treeView.getBoundingRect().width()) + " x " + NUMBER_FORMAT.format(Math.abs(treeView.getBoundingRect().height())) + ")");
                        }
                    });

                    verticalItemSpacingSeekBar = findViewById(R.id.vertical_item_spacing_seekbar);
                    verticalItemSpacingSeekBar.setMax(400); // (max - min) / step
                    verticalItemSpacingSeekBar.setProgress((int) exportOptions.verticalSpacing);
                    verticalItemSpacingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            exportOptions.verticalSpacing = 0 + (progress * 1);
                            verticalItemSpacing.setText(String.valueOf((int) exportOptions.verticalSpacing));
                            treeView.recalculate();
                            waitAndUpdateDimensions();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    itemTextSize = findViewById(R.id.item_text_size);
                    itemTextSize.setText(String.valueOf((int) exportOptions.itemTextSize));

                    itemTextSizeSeekBar = findViewById(R.id.item_text_size_seekbar);
                    itemTextSizeSeekBar.setMax(100 - 8); // (max - min) / step
                    itemTextSizeSeekBar.setProgress((int) exportOptions.itemTextSize - 8);
                    itemTextSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            exportOptions.itemTextSize = 8 + (progress * 1);
                            itemTextSize.setText(String.valueOf((int) exportOptions.itemTextSize));
                            treeView.recalculate();
                            waitAndUpdateDimensions();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    branchWidth = findViewById(R.id.branch_widtth);
                    branchWidth.setText(String.valueOf((int) exportOptions.branchWidth));

                    branchWidthSeekBar = findViewById(R.id.branch_width_seekbar);
                    branchWidthSeekBar.setMax(5 - 1); // (max - min) / step
                    branchWidthSeekBar.setProgress((int) exportOptions.branchWidth - 1);
                    branchWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            exportOptions.branchWidth = 1 + (progress * 1);
                            branchWidth.setText(String.valueOf((int) exportOptions.branchWidth));
                            treeView.redraw();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    itemBorderWidth = findViewById(R.id.item_border_width);
                    itemBorderWidth.setText(String.valueOf((int) exportOptions.itemBorderWidth));

                    itemBorderWidthSeekBar = findViewById(R.id.item_border_width_seekbar);
                    itemBorderWidthSeekBar.setMax(5 - 1); // (max - min) / step
                    itemBorderWidthSeekBar.setProgress((int) exportOptions.itemBorderWidth - 1);
                    itemBorderWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            exportOptions.itemBorderWidth = 1 + (progress * 1);
                            itemBorderWidth.setText(String.valueOf((int) exportOptions.itemBorderWidth));
                            treeView.redraw();
                            //waitAndUpdateDimensions();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    final CheckBox itemBackgroundsCheckbox = findViewById(R.id.item_background_checkbox);
                    itemBackgroundsCheckbox.setChecked(exportOptions.itemBackgrounds);
                    final TextView itemBackgroundColorLabel = findViewById(R.id.item_background_color_label);
                    itemBackgroundsCheckbox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Change dependent visibility
                            itemBackgroundColorTextView.setEnabled(itemBackgroundsCheckbox.isChecked());
                            itemBackgroundColorLabel.setEnabled(itemBackgroundsCheckbox.isChecked());
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

    private void waitAndUpdateDimensions(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Waiting...");
                while (!treeView.isGenerated()){
                    //TODO: This is bad
                }
                Log.d(TAG, "Done waiting.");
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

        final ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_enabled}},
                new int[]{
                        color,
                        color
                });

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
}
