package com.tycho.app.primenumberfinder.modules.findprimes;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tycho.app.primenumberfinder.R;

/**
 * Created by tycho on 1/24/2018.
 */

public class FindPrimesConfigurationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_primes_configuration_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        applyThemeColor(ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));
    }

    private void applyThemeColor(final int statusBarColor, final int actionBarColor) {

        //Set status bar color
        getWindow().setStatusBarColor(statusBarColor);

        //Set actionbar color
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));
    }
}
