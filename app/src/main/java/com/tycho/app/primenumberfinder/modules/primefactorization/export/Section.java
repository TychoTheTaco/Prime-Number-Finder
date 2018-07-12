package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;

import java.util.ArrayList;
import java.util.List;

public class Section {

    private final Context context;

    protected String title;

    private final List<Option> options = new ArrayList<>();

    protected Section(final Context context, final String title){
        this.context = context;
        this.title = title;
    }

    public View inflate(final ViewGroup parent, final boolean attachToParent){
        final ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.option_section, parent);
        ((TextView) viewGroup.findViewById(R.id.title)).setText(title);

        for (Option option : options){
            viewGroup.addView(option.inflate(viewGroup, attachToParent));
        }

        return viewGroup;
    }

    public void addOption(final Option option){
        this.options.add(option);
    }

    public List<Option> getOptions() {
        return options;
    }
}
