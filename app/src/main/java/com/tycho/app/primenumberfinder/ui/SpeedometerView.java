package com.tycho.app.primenumberfinder.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tycho.app.primenumberfinder.R;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 10/28/2016
 */

public class SpeedometerView extends View{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "SpeedometerView";

    /**
     * Paint object used to draw to canvas.
     */
    private final Paint paint = new Paint();

    /**
     * The current value.
     */
    private float value;

    /**
     * The minimum value of the speedometer. If the current value is smaller than the minimum
     * value, the pointer will not move past the minimum value.
     */
    private float minValue;

    /**
     * The maximum value of the speedometer. If the current value is greater than the maximum
     * value, the pointer will not move past the maximum value.
     */
    private float maxValue;


    private String unit = "";

    private float borderWidth = 3;

    private float centerX;
    private float centerY;

    /**
     * The radius of the circle. This is calculated from the view's width.
     */
    private float radius;

    private float tickmarkStartAngle = -110;
    private float tickmarkEndAngle = 110;
    private float tickmarkCount = 8;
    private float tickmarkAngleIncrement;

    private int textColor = Color.parseColor("#808080");

    private Path pointer = new Path();

    public SpeedometerView(Context context){
        super(context);
    }

    public SpeedometerView(Context context, AttributeSet attrs){
        super(context, attrs);
        obtainCustomAttributes(context, attrs);
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        obtainCustomAttributes(context, attrs);
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        obtainCustomAttributes(context, attrs);
    }

    private void obtainCustomAttributes(final Context context, final AttributeSet attributeSet){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.SpeedometerView,
                0, 0);

        unit = typedArray.getString(R.styleable.SpeedometerView_unit);
        minValue = typedArray.getFloat(R.styleable.SpeedometerView_minValue, 0f);
        maxValue = typedArray.getFloat(R.styleable.SpeedometerView_maxValue, 1f);

        typedArray.recycle();

        tickmarkAngleIncrement = Math.abs((tickmarkEndAngle - tickmarkStartAngle) / tickmarkCount);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        radius = ((getWidth() - borderWidth) / 2);
        Log.e(TAG, "Width: " + getWidth() + " Radius: " + radius);
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        paint.setAntiAlias(true);

        canvas.save();

        //Draw the outer circle
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        canvas.drawCircle(centerX, centerY, radius, paint);

        //Draw the tick marks
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(textColor);
        paint.setStrokeWidth(4);
        for (int i = 0; i <= tickmarkCount; i++){
            if (i == 0){
                canvas.rotate(tickmarkStartAngle, centerX, centerY);
            }else{
                canvas.rotate(tickmarkAngleIncrement, centerX, centerY);
            }
            canvas.drawLine(centerX, 10, centerX, 10 + 25, paint);
        }

        canvas.restore();
        canvas.save();

        float smallRad = 6;

        //Draw the pointer
        pointer.reset();
        pointer.moveTo(centerX - smallRad, centerY);
        pointer.lineTo(centerX, centerY - radius + 20);
        pointer.lineTo(centerX + smallRad, centerY);
        pointer.close();

        //start + (range) * float

        float range = Math.abs(tickmarkEndAngle - tickmarkStartAngle);

        float rotation = tickmarkStartAngle + (range * (value / (maxValue - minValue)));

        if (rotation > range / 2){
            rotation = range / 2;
        }

        if (rotation < tickmarkStartAngle){
            rotation = tickmarkStartAngle;
        }

        //Draw the pointer
        canvas.rotate(rotation, centerX, centerY);
        paint.setColor(Color.DKGRAY);
        canvas.drawPath(pointer, paint);
        canvas.restore();

        canvas.drawCircle(centerX, centerY, smallRad, paint);

        paint.setColor(textColor);
        paint.setTextSize(45);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(value), centerX, centerY + radius - 50, paint);
        paint.setTextSize(30);
        canvas.drawText(unit, centerX, centerY + radius - 15, paint);
    }

    //Getters and setters

    public float getValue(){
        return value;
    }

    public void setValue(float value){
        this.value = value;
        invalidate();
    }

    public float getMaxValue(){
        return maxValue;
    }

    public void setMaxValue(float maxValue){
        this.maxValue = maxValue;
    }
}
