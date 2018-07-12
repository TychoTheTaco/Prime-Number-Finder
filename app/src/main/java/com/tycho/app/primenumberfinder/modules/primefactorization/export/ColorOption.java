package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

public class ColorOption extends Option implements View.OnClickListener{

    private final Context context;

    private int color;

    private TextView colorTextView;

    public ColorOption(final Context context, final String text, final int color){
        super(text);
        this.context = context;
        this.color = color;
    }

    @Override
    public View inflate() {
        final View view = LayoutInflater.from(context).inflate(R.layout.color_option, null);
        ((TextView) view.findViewById(R.id.text)).setText(text);

        colorTextView = view.findViewById(R.id.color);
        colorTextView.setOnClickListener(this);

        setColor(color);

        return view;
    }

    @Override
    public void onClick(View v) {

    }

    public void setColor(final int color){
        colorTextView.setBackgroundTintList(generateColorStateList(color));
        colorTextView.setText(context.getString(R.string.hex, Integer.toHexString(color).toUpperCase()));
    }

    private ColorStateList generateColorStateList(final int color) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_enabled}},
                new int[]{
                        color,
                        color
                });
    }
}
