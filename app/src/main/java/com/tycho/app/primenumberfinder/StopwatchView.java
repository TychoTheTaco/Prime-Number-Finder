package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

/**
 * Created by Tycho Belers on 9/27/2016.
 */

public class StopwatchView extends View {

    private long time;

    private final Paint paint = new Paint();

    public StopwatchView(Context context) {
        super(context);
    }

    public StopwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StopwatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StopwatchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);

        canvas.save();

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawCircle(centerX, centerY, getHeight() / 14 / 2 * 12, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#808080"));
        canvas.drawRoundRect(getWidth() / 2.7f, 0, getWidth() - (getWidth() / 2.7f), getHeight() / 14, 12, 12, paint);

        canvas.rotate(50, centerX, centerY);

        canvas.drawRoundRect(getWidth() / 2.4f, 0, getWidth() - (getWidth() / 2.4f), getHeight() / 14, 12, 12, paint);

        canvas.restore();

        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);

        String text = formatTime();

        Rect r = new Rect();
        paint.getTextBounds(text, 0, text.length(), r);
        float yPos = centerY + (Math.abs(r.height()))/2;
        canvas.drawText(text, centerX, yPos, paint);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        postInvalidate();
    }

    private String formatTime(){
        long millis = getTime();

        int second = (int) (millis / 1000) % 60;
        int minute = (int) (millis / (1000 * 60)) % 60;
        int hour = (int) (millis / (1000 * 60 * 60)) % 24;
        int days = (int) (millis / (1000 * 60 * 60 * 24)) % 7;

        String time = String.format(Locale.getDefault(),"%02d:%02d:%02d:%02d", days, hour, minute, second);

        return time;
    }
}
