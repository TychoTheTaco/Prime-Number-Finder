package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

public class ColorOption extends Option implements View.OnClickListener {

    private final Context context;

    protected int color;

    private TextView colorTextView;

    public ColorOption(final Context context, final String text, final int color) {
        super(text);
        this.context = context;
        this.color = color;
    }

    @Override
    public View inflate(final ViewGroup parent, final boolean attachToParent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.color_option, parent, attachToParent);
        titleTextView = view.findViewById(R.id.text);
        titleTextView.setText(text);

        colorTextView = view.findViewById(R.id.color);
        colorTextView.setOnClickListener(this);

        setColor(color);

        return view;
    }

    @Override
    public void onClick(View v) {

    }

    public void setColor(final int color) {
        this.color = color;
        colorTextView.setBackgroundTintList(generateColorStateList(color));
        colorTextView.setText(context.getString(R.string.hex, Integer.toHexString(color).toUpperCase()));
        colorTextView.setTextColor(getTextColor(color));
    }

    private int getTextColor(final int background) {
        final int a = (background >> 24) & 0xFF;
        final int r = (background >> 16) & 0xFF;
        final int g = (background >> 8) & 0xFF;
        final int b = background & 0xFF;
        if (a < 128) {
            return Color.BLACK;
        }
        if (g >= 128 && b >= 128) {
            return Color.BLACK;
        }
        return Color.WHITE;
    }

    private ColorStateList generateColorStateList(final int color) {
        return Utils.generateColorStateList(
                new int[]{
                        android.R.attr.state_enabled,
                        -android.R.attr.state_enabled},
                new int[]{
                        color,
                        ContextCompat.getColor(context, R.color.item_disabled)});
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        colorTextView.setEnabled(enabled);
        colorTextView.setBackgroundTintList(generateColorStateList(this.color));
        colorTextView.setTextColor(enabled ? getTextColor(this.color) : ContextCompat.getColor(context, R.color.gray));
    }
}
