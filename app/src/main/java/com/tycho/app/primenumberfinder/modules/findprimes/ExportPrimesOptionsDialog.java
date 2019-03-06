package com.tycho.app.primenumberfinder.modules.findprimes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.widget.EditText;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.ExportOptionsDialog;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

public class ExportPrimesOptionsDialog extends ExportOptionsDialog {

    private FileManager.PrimesFile primesFile;

    public ExportPrimesOptionsDialog(final Context context, final File file, final int style){
        super(context, file, style);
        try {
            primesFile = new FileManager.PrimesFile(file);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up file name
        final EditText fileNameInput = findViewById(R.id.file_name);
        fileNameInput.setText("Primes from " + primesFile.getStartValue() + " to " + (primesFile.getEndValue() == 0 ? "infinity" : primesFile.getEndValue()));

    }

    @Override
    protected void export(final String fileName, final String separator, final boolean formatNumber, final NumberFormat numberFormat) {
        final File output = primesFile.export(fileName + ".txt", separator, formatNumber ? numberFormat : null);
        final Uri path = FileProvider.getUriForFile(context, "com.tycho.app.primenumberfinder", output);
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("text/plain");
        context.startActivity(intent);
    }
}
