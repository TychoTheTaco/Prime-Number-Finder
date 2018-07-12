package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;

public class CheckboxOption extends Option implements View.OnClickListener{

    private final Context context;

    protected CheckBox checkBox;

    private boolean checked;

    public CheckboxOption(final Context context, final String text, final boolean checked){
        super(text);
        this.context = context;
        this.checked = checked;
    }

    @Override
    public View inflate(final ViewGroup parent, final boolean attachToParent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.checkbox_option, parent, attachToParent);

        checkBox = view.findViewById(R.id.checkbox);
        checkBox.setText(text);
        checkBox.setChecked(checked);
        checkBox.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkBox.setEnabled(enabled);
    }
}
