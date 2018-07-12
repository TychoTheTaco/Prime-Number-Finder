package com.tycho.app.primenumberfinder.modules.primefactorization.export;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ui.RangedSeekBar;
import com.tycho.app.primenumberfinder.ui.ValidEditText;

import java.awt.font.TextAttribute;

public class SliderOption extends Option implements SeekBar.OnSeekBarChangeListener{

    private final Context context;

    private ValidEditText input;
    protected RangedSeekBar rangedSeekBar;

    private float min;
    private float max;
    private int step;
    private float value;

    public SliderOption(final Context context, final String text, final float min, final float max, final float value){
        this (context, text, min, max, 1, value);
    }

    public SliderOption(final Context context, final String text, final float min, final float max, final int step, final float value){
        super(text);
        this.context = context;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
    }

    @Override
    public View inflate(final ViewGroup parent, final boolean attachToParent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.slider_option, parent, attachToParent);
        ((TextView) view.findViewById(R.id.text)).setText(text);

        input = view.findViewById(R.id.input);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | ((step == 1) ? 0 : InputType.TYPE_NUMBER_FLAG_DECIMAL));
        input.setNumber(value);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final float value = input.getFloatValue();
                input.setValid(input.length() > 0 && value >= rangedSeekBar.getMinValue() && value <= rangedSeekBar.getMaxValue());
                rangedSeekBar.setValue(value);
            }
        });

        rangedSeekBar = view.findViewById(R.id.slider);
        rangedSeekBar.setRange(min, max);
        rangedSeekBar.setSteps(step);
        rangedSeekBar.setValue(value);
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
