package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.DisplayContentActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.ColorPickerListener;
import com.tycho.app.primenumberfinder.ui.TreeView;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
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

public class FactorTreeExportOptionsActivity extends DisplayContentActivity implements ColorPickerListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FactorTreeExportOptionsActivity.class.getSimpleName();

    private final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private Tree<Long> factorTree;

    private TreeView treeView;

    private TreeView.ExportOptions exportOptions;

    private int selectedItemIndex = -1;

    private TextView imageSizeTextView;

    private final List<Section> sections = new ArrayList<>();

    private EditText fileNameInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.PrimeFactorization);
        setContentView(R.layout.factor_tree_export_options_activity);

        //Set up the toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.green_dark), ContextCompat.getColor(this, R.color.green));

        fileNameInput = findViewById(R.id.file_name);
        treeView = findViewById(R.id.factor_tree_preview);
        exportOptions = treeView.getDefaultExportOptions();
        if (PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 1) {
            exportOptions = TreeView.DarkThemeExportOptions.create(this);
        }
        treeView.setExportOptions(exportOptions);

        //Image style
        final Section imageStyleSection = new Section(this, "Image Style");
        imageStyleSection.addOption(new ColorOption(this, "Background color", exportOptions.imageBackgroundColor) {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                ColorPickerDialog.newBuilder().setColor(exportOptions.imageBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                selectedItemIndex = 0;
            }
        });
        imageStyleSection.addOption(new SliderOption(this, "Vertical item spacing", 0, 400, exportOptions.verticalSpacing) {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                super.onProgressChanged(seekBar, progress, fromUser);
                exportOptions.verticalSpacing = rangedSeekBar.getFloatValue();
                ((SliderOption) sections.get(1).getOptions().get(2)).setMax(exportOptions.verticalSpacing / 2);
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
        branchStyleSection.addOption(new ColorOption(this, "Color", exportOptions.branchColor) {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                ColorPickerDialog.newBuilder().setColor(exportOptions.branchColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                selectedItemIndex = 4;
            }
        });
        branchStyleSection.addOption(new SliderOption(this, "Width", 1, 10, 10, exportOptions.branchWidth) {
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
        branchStyleSection.addOption(new SliderOption(this, "Padding", 0, exportOptions.verticalSpacing / 2, 10, exportOptions.branchPadding) {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                super.onProgressChanged(seekBar, progress, fromUser);
                exportOptions.branchPadding = rangedSeekBar.getFloatValue();
                treeView.redraw();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                findViewById(R.id.root).requestFocus();
            }
        });
        branchStyleSection.addOption(new SliderOption(this, "Length", 0, 100, 10, exportOptions.branchLength * 100, "%") {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                super.onProgressChanged(seekBar, progress, fromUser);
                exportOptions.branchLength = rangedSeekBar.getFloatValue() / 100;
                treeView.redraw();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                findViewById(R.id.root).requestFocus();
            }
        });

        //Text style
        final Section textStyleSection = new Section(this, "Text Style");
        textStyleSection.addOption(new CheckboxOption(this, "Format numbers", true) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                treeView.setTree(checkBox.isChecked() ? factorTree.formatNumbers() : factorTree);
            }
        });
        textStyleSection.addOption(new ColorOption(this, "Text color", exportOptions.itemTextColor) {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                ColorPickerDialog.newBuilder().setColor(exportOptions.itemTextColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                selectedItemIndex = 1;
            }
        });
        textStyleSection.addOption(new ColorOption(this, "Prime factor text color", exportOptions.primeFactorTextColor) {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                ColorPickerDialog.newBuilder().setColor(color).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                selectedItemIndex = 3;
            }
        });
        textStyleSection.addOption(new SliderOption(this, "Text size", 8, 100, exportOptions.itemTextSize) {
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
        itemBackgroundSection.addOption(new CheckboxOption(this, "Item backgrounds", exportOptions.itemBackgrounds) {
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
        itemBackgroundSection.addOption(new ColorOption(this, "Background color", exportOptions.itemBackgroundColor) {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                ColorPickerDialog.newBuilder().setColor(exportOptions.itemBackgroundColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                selectedItemIndex = 2;
            }
        });
        itemBackgroundSection.addOption(new ColorOption(this, "Border color", exportOptions.itemBorderColor) {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(FactorTreeExportOptionsActivity.this);
                ColorPickerDialog.newBuilder().setColor(exportOptions.itemBorderColor).setShowAlphaSlider(true).show(FactorTreeExportOptionsActivity.this);
                selectedItemIndex = 5;
            }
        });
        itemBackgroundSection.addOption(new SliderOption(this, "Border width", 1, 10, 10, exportOptions.itemBorderWidth) {
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
        layoutParams.topMargin = Utils.dpToPx(this, 24);
        for (Section section : sections) {
            final View view = section.inflate(null, false);
            view.setLayoutParams(layoutParams);
            optionsLayout.addView(view);
        }

        //Change dependent visibility
        sections.get(3).getOptions().get(1).setEnabled(exportOptions.itemBackgrounds);
        sections.get(3).getOptions().get(2).setEnabled(exportOptions.itemBackgrounds);
        sections.get(3).getOptions().get(3).setEnabled(exportOptions.itemBackgrounds);

        imageSizeTextView = findViewById(R.id.image_size);
        imageSizeTextView.post(() -> {
            if (treeView.isGenerated()) {
                imageSizeTextView.setText("(" + NUMBER_FORMAT.format(treeView.getBoundingRect().width()) + " x " + NUMBER_FORMAT.format(Math.abs(treeView.getBoundingRect().height())) + ")");
            }
        });

        //Set up export button
        final Button exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(v -> {
            if (checkInputs()) {
                final com.tycho.app.primenumberfinder.ProgressDialog progressDialog = new com.tycho.app.primenumberfinder.ProgressDialog(FactorTreeExportOptionsActivity.this);
                progressDialog.setTitle("Exporting...");
                progressDialog.show();

                new Thread(() -> {

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

                    final Bundle bundle = new Bundle();
                    bundle.putLong("number", factorTree.getValue());
                    FirebaseAnalytics.getInstance(getBaseContext()).logEvent("tree_exported", bundle);

                    final Uri path = FileProvider.getUriForFile(FactorTreeExportOptionsActivity.this, "com.tycho.app.primenumberfinder", image);
                    final Intent intent1 = new Intent(Intent.ACTION_SEND);
                    intent1.putExtra(Intent.EXTRA_STREAM, path);
                    intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent1.setType("image/*");
                    startActivity(intent1);
                    FirebaseAnalytics.getInstance(this).logEvent("export_tree", null);
                }).start();
            } else {
                Toast.makeText(getBaseContext(), "Invalid inputs!", Toast.LENGTH_LONG).show();
            }
        });

        //Read the tree from the file
        load();
    }

    @Override
    protected void loadFile(File file) {
        factorTree = FileManager.getInstance().readTree(file);
    }

    @Override
    protected void onFileLoaded() {
        setTitle(getString(R.string.export_options_title));
        fileNameInput.setText("Factor tree of " + factorTree.getValue());
        treeView.setTree(factorTree.formatNumbers());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Give the root view focus to prevent EditTexts from initially getting focus
        findViewById(R.id.root).requestFocus();
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
                ((ColorOption) sections.get(2).getOptions().get(1)).setColor(color);
                break;

            case 2:
                exportOptions.itemBackgroundColor = color;
                ((ColorOption) sections.get(3).getOptions().get(1)).setColor(color);
                break;

            case 3:
                exportOptions.primeFactorTextColor = color;
                ((ColorOption) sections.get(2).getOptions().get(2)).setColor(color);
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
