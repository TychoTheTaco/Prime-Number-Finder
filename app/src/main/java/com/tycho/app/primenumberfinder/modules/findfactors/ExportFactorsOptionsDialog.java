package com.tycho.app.primenumberfinder.modules.findfactors;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.widget.EditText;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

public class ExportFactorsOptionsDialog extends ExportOptionsDialog {

    private FileManager.FactorsFile factorsFile;

    public ExportFactorsOptionsDialog(final Context context, final File file, final int style){
        super(context, file, style);
        try {
            factorsFile = new FileManager.FactorsFile(file);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up file name
        final EditText fileNameInput = findViewById(R.id.file_name);
        fileNameInput.setText("Factors of " + factorsFile.getNumber());

    }

    @Override
    protected void export(final String fileName, final String separator, final boolean formatNumber, final NumberFormat numberFormat) {
        FirebaseAnalytics.getInstance(getContext()).logEvent("export_factors", null);
        final File output = factorsFile.export(fileName + ".txt", separator, formatNumber ? numberFormat : null);
        final Uri path = FileProvider.getUriForFile(context, "com.tycho.app.primenumberfinder", output);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("text/plain");
        context.startActivity(intent);
    }
}
