package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ui.CustomRadioGroup;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by tycho on 1/23/2018.
 */

public class ExportOptionsDialog extends Dialog {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ExportOptionsDialog.class.getSimpleName();

    private final Context context;

    private final File file;

    public ExportOptionsDialog(final Context context, final File file, final int style){
        super(context, style);
        this.context = context;
        this.file = file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.export_options_dialog);
        setCancelable(true);

        //Set up file name
        final EditText fileNameInput = findViewById(R.id.file_name);
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        fileNameInput.setText(name);

        final EditText itemSeparatorInput = findViewById(R.id.item_separator);

        final RadioButton newLineButton = findViewById(R.id.new_line);
        final RadioButton otherButton = findViewById(R.id.other);

        //Set up item separator selection
        final CustomRadioGroup customRadioGroup = findViewById(R.id.item_separator_radio_group);
        customRadioGroup.addOnCheckChangedListener((radioButton, isChecked) -> {

            //Toggle 'Other' input box
            itemSeparatorInput.setEnabled(otherButton.isChecked());
        });
        newLineButton.setChecked(true);

        final Button exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(v -> {

            //Get item separator
            final String separator;
            if (newLineButton.isChecked()){
                separator = System.lineSeparator();
            }else{
                separator = itemSeparatorInput.getText().toString().replace("\\n", System.lineSeparator());
            }

            //Get format options
            final boolean formatNumber = ((CheckBox) findViewById(R.id.format_number)).isChecked();
            final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Exporting...");
            progressDialog.show();
            dismiss();

            //Convert the file to the requested format
            new Thread(() -> {
                final File output = FileManager.getInstance().export(file, fileNameInput.getText().toString().trim() + ".txt", separator, formatNumber ? numberFormat : null);
                progressDialog.dismiss();

                final Uri path = FileProvider.getUriForFile(context, "com.tycho.app.primenumberfinder", output);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, path);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("text/plain");
                context.startActivity(intent);
            }).start();
        });
    }
}
