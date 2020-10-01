package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ui.CustomRadioGroup;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by tycho on 1/23/2018.
 */

public abstract class ExportOptionsDialog extends Dialog {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ExportOptionsDialog.class.getSimpleName();

    protected final Context context;

    protected final File file;

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

        final EditText fileNameInput = findViewById(R.id.file_name);
        /*//Set up file name
        try{
            final FileManager.PrimesFile primesFile = new FileManager.PrimesFile(file);
            fileNameInput.setText("Primes from " + primesFile.getStartValue() + " to " + (primesFile.getEndValue() == 0 ? "infinity" : primesFile.getEndValue()));
        }catch (IOException e){
            e.printStackTrace();
        }*/

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
                export(fileNameInput.getText().toString().trim(), separator, formatNumber, numberFormat);
                progressDialog.dismiss();
            }).start();
        });
    }

    protected abstract void export(final String fileName, final String separator, final boolean formatNumber, final NumberFormat numberFormat);
}
