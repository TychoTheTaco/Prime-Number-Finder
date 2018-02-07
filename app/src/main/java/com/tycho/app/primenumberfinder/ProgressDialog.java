package com.tycho.app.primenumberfinder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by tycho on 2/1/2018.
 */

public class ProgressDialog extends Dialog{

    private final Context context;

    private String title;

    private TextView titleTextView;

    public ProgressDialog(final Context context){
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progress_dialog);

        setCancelable(false);

        titleTextView = findViewById(R.id.title);
        titleTextView.setText(title);
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        this.title = String.valueOf(title);
        if (titleTextView != null){
            titleTextView.setText(title);
        }
    }
}
