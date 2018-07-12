package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class Option {

    protected String text;

    protected TextView titleTextView;

    protected Option(final String text){
        this.text = text;
    }

    public abstract View inflate(final ViewGroup parent, final boolean attachToParent);

    public void setEnabled(final boolean enabled){
        if (titleTextView != null) titleTextView.setEnabled(enabled);
    }
}
