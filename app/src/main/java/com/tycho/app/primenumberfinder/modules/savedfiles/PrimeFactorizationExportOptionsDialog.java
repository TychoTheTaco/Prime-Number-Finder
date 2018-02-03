package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.tycho.app.primenumberfinder.CustomRadioGroup;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.TreeView;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by tycho on 1/23/2018.
 */

public class PrimeFactorizationExportOptionsDialog extends Dialog {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeFacExportOptionsDialog";

    private final Context context;

    private final File file;

    private final TreeView treeView;

    public PrimeFactorizationExportOptionsDialog(final Context context, final File file, final TreeView treeView){
        super(context);
        this.context = context;
        this.file = file;
        this.treeView = treeView;
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
                    treeView.drawToBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
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
}
