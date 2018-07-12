package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class Option {

    protected String text;

    protected Option(final String text){
        this.text = text;
    }

    public abstract View inflate(final ViewGroup parent, final boolean attachToParent);
}
