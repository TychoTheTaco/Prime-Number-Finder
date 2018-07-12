package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ui.RangedSeekBar;
import com.tycho.app.primenumberfinder.ui.ValidEditText;

public class SliderOption extends Option implements SeekBar.OnSeekBarChangeListener{

    private final Context context;

    private ValidEditText input;
    private RangedSeekBar rangedSeekBar;

    public SliderOption(final Context context, final String text, final float min, final float max, final float value){
        super(text);
        this.context = context;
    }

    @Override
    public View inflate() {
        final View view = LayoutInflater.from(context).inflate(R.layout.color_option, null);
        ((TextView) view.findViewById(R.id.text)).setText(text);

        input = view.findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final int value = input.getIntValue();
                input.setValid(input.length() > 0 && value >= rangedSeekBar.getMinValue() && value <= rangedSeekBar.getMaxValue());
                rangedSeekBar.setValue(value);
            }
        });

        rangedSeekBar = view.findViewById(R.id.slider);
        rangedSeekBar.setOnSeekBarChangeListener(this);

        return view;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            input.setNumber(rangedSeekBar.getFloatValue());
        }
    }
}
