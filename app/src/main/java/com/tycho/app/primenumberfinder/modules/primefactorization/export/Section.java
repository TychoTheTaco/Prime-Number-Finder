package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

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

        //final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //layoutParams.topMargin = (int) Utils.dpToPx(context, 4);
        for (Option option : options){
            final View view = option.inflate(viewGroup, attachToParent);
            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            layoutParams.topMargin = (int) Utils.dpToPx(context, 4);
            //view.setLayoutParams(layoutParams);
            viewGroup.addView(view);
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
