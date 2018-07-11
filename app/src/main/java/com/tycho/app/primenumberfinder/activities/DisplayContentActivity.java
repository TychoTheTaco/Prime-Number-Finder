package com.tycho.app.primenumberfinder.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.DisplayFactorsActivity;

import java.io.File;

public abstract class DisplayContentActivity extends AbstractActivity {

    protected abstract void load(final File file);

    protected void showLoadingError(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error!");
        alertDialog.setMessage("There was an error reading this file.");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
    }
}
