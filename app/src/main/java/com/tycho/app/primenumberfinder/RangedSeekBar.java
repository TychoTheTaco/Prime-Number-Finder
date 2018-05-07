package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

public class RangedSeekBar extends AppCompatSeekBar {

    private float minValue = 0;
    private float maxValue = 100;

    /**
     * The number of steps per whole number. For example, with a min value of 1.0 and a max of 2.0,
     * 10 steps would allow the user to select 1.1, 1.2, etc., whereas 100 steps would allow them
     * to select numbers with hundred's place accuracy.
     */
    private int steps = 1;

    public RangedSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public RangedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RangedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attributeSet){
        recalculateBounds();
    }

    private void recalculateBounds(){
        setMax((int) ((maxValue - minValue) * steps));
    }

    public float getFloatValue(){
        return minValue + ((float) getProgress() / steps);
    }

    public int getIntValue(){
        return (int) (minValue + (getProgress() / steps));
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
        recalculateBounds();
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        recalculateBounds();
    }

    public void setSteps(int steps) {
        this.steps = steps;
        recalculateBounds();
    }

    public void setRange(final float minValue, final float maxValue){
        setMinValue(minValue);
        setMaxValue(maxValue);
    }

    public void setValue(final float value){
        setProgress((int) ((value - minValue) * steps));
    }
}
