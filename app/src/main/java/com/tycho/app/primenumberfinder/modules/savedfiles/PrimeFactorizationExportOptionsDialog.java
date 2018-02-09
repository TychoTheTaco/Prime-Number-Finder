package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.tycho.app.primenumberfinder.CustomRadioGroup;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.TreeView;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by tycho on 1/23/2018.
 */

public class PrimeFactorizationExportOptionsDialog extends Dialog implements ColorPickerListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeFacExptOptsDialog";

    private final Context context;

    private final File file;

    private final TreeView treeView;

    TextView imageBackgroundColorTextView;
    TextView itemTextColorTextView;
    TextView itemBackgroundColorTextView;
    TextView primeFactorTextColorTextView;

    private TreeView.ExportOptions exportOptions;

    private int selectedItemIndex = -1;

    public PrimeFactorizationExportOptionsDialog(final Context context, final File file, final TreeView treeView){
        super(context);
        this.context = context;
        this.file = file;
        this.treeView = treeView;
        try {this.exportOptions = treeView.getDefaultExportOptions().clone();

        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.prime_factorization_export_options_dialog);

        setCancelable(true);

        //Set up file name
        final EditText fileNameInput = findViewById(R.id.file_name);
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        fileNameInput.setText(name);

        imageBackgroundColorTextView = findViewById(R.id.image_background_color);
        imageBackgroundColorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder().setShowAlphaSlider(true).show((DisplayPrimeFactorizationActivity) context);
                selectedItemIndex = 0;
            }
        });

        itemTextColorTextView = findViewById(R.id.item_text_color);
        itemTextColorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder().setShowAlphaSlider(true).show((DisplayPrimeFactorizationActivity) context);
                selectedItemIndex = 1;
            }
        });

        itemBackgroundColorTextView = findViewById(R.id.item_background_color);
        itemBackgroundColorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder().setShowAlphaSlider(true).show((DisplayPrimeFactorizationActivity) context);
                selectedItemIndex = 2;
            }
        });

        primeFactorTextColorTextView = findViewById(R.id.prime_factor_text_color);
        primeFactorTextColorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog.newBuilder().setShowAlphaSlider(true).show((DisplayPrimeFactorizationActivity) context);
                selectedItemIndex = 3;
            }
        });

        final Button exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Exporting...");
                progressDialog.show();
                dismiss();

                //Convert the file to the requested format
                final File image = new File(FileManager.getInstance().getExportCacheDirectory() + File.separator + fileNameInput.getText().toString().trim() + ".png");
                try{
                    OutputStream stream = new FileOutputStream(image);
                    treeView.drawToBitmap(exportOptions).compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

                progressDialog.dismiss();

                final Uri path = FileProvider.getUriForFile(context, "com.tycho.app.primenumberfinder", image);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, path);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                context.startActivity(intent);
            }
        });
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

        switch (selectedItemIndex){

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

        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        Log.d(TAG, "Hello from dialog");
    }
}
