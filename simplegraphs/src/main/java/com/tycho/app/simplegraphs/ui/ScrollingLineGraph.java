package com.tycho.app.simplegraphs.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tycho.app.simplegraphs.R;
import com.tycho.app.simplegraphs.utils.Point;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 2/14/2017
 */

public class ScrollingLineGraph extends View{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "ScrollingLineGraph";

    /**
     * Paint object used to draw to canvas.
     */
    private final Paint paint = new Paint();

    /**
     * List of data points on this graph.
     */
    private final ArrayList<Point> points = new ArrayList<>();

    /**
     * The current value of the data. This can be updated at any time from any thread. When the graph
     * updates, it will create a new point with the current value.
     */
    private float value = 0;

    //Size
    private float borderWidth;
    private float scaleWidth;

    //Units
    private float unitsX;
    private float unitsY;

    //Scale
    private float scaleX;
    private float scaleY;

    //Colors
    private int borderColor;
    private int scaleColor;
    private int lineColor;
    private int highlightColor;
    private int labelColor;

    //Text
    private String title;
    private String labelXText;
    private String labelYText;

    //Text size
    private float labelTextSize;
    private float titleTextSize = 36;

    /**
     * Different modes to graph the data.
     */
    private enum Mode{
        /**
         * Create a new data point every time the graph updates. In this mode, the latest data will
         * always be displayed, but it can be a lot more resource intensive.
         */
        REAL_TIME,

        /**
         * Create a new data point at a specified interval. In this mode, data will be delayed by
         * {@link ScrollingLineGraph#dataRefreshRate} milliseconds. This ensures the graph scrolls smoothly
         * while being less resource intensive.
         */
        DELAYED
    }

    /**
     * The current graphing mode.
     */
    private Mode mode = Mode.DELAYED;

    /**
     * The number of units to scroll every second.
     */
    private float scrollSpeed = 1000;

    /**
     * If this is true, the graph will automatically change its {@link ScrollingLineGraph#unitsY} to
     * match the highest point on the graph.
     */
    private boolean autoScale = true;

    /**
     * The rate at which a new data point is created.
     */
    private long dataRefreshRate = 1000 / 2;
    private long lastUpdate;

    /**
     * The rate at which the graph refreshes itself.
     */
    private long graphRefreshRate = (1000 / 60);
    private long lastDataUpdate = 0;

    /**
     * This {@link Path} is used to draw the data points of the graph.
     */
    final Path path = new Path();

    final Path highlightPath = new Path();

    /**
     * Lock for adding and iterating over data points.
     */
    private final Object POINTS_LOCK = new Object();


    float pixelsPerUnitX;
    float pixelsPerUnitY;

    private long lastResetTime;

    long startTime = -1;
    private long elapsedTime;
    private long pauseStartTime;
    private long totalPauseTime;

    final int padding = 10;

    private boolean isRunning;

    float titlePadding = 14;
    private float graphHeight;

    private float translationX;

    private Point highestVisiblePoint;

    private boolean ellipsis = true;
    private String ellipsisText = "...";

    final Object UPDATE_LOCK = new Object();

    //Constructors

    public ScrollingLineGraph(Context context){
        super(context);
    }

    public ScrollingLineGraph(Context context, AttributeSet attrs){
        super(context, attrs);
        applyAttributes(context, attrs);
    }

    public ScrollingLineGraph(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        applyAttributes(context, attrs);
    }

    public ScrollingLineGraph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Retrieve all xml attributes and apply them to the view.
     *
     * @param context
     * @param attributeSet
     */
    private void applyAttributes(Context context, AttributeSet attributeSet){
        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.ScrollingLineGraph,
                0, 0);

        unitsX = a.getFloat(R.styleable.ScrollingLineGraph_unitsX, 100);
        unitsY = a.getFloat(R.styleable.ScrollingLineGraph_unitsY, 300);

        scaleX = a.getFloat(R.styleable.ScrollingLineGraph_unitScaleX, 25);
        scaleY = a.getFloat(R.styleable.ScrollingLineGraph_unitScaleY, 25);

        borderWidth = a.getFloat(R.styleable.ScrollingLineGraph_graphBorderWidth, 3);
        scaleWidth = a.getFloat(R.styleable.ScrollingLineGraph_unitScaleWidth, 1);

        borderColor = a.getColor(R.styleable.ScrollingLineGraph_borderColor, Color.BLACK);
        scaleColor = a.getColor(R.styleable.ScrollingLineGraph_scaleColor, Color.LTGRAY);
        lineColor = a.getColor(R.styleable.ScrollingLineGraph_lineColor, Color.BLUE);
        highlightColor = a.getColor(R.styleable.ScrollingLineGraph_highlightColor, Color.argb(100, 0, 0, 255));
        labelColor = a.getColor(R.styleable.ScrollingLineGraph_labelColor, Color.DKGRAY);

        labelTextSize = a.getFloat(R.styleable.ScrollingLineGraph_labelSize, 32);

        title = a.getString(R.styleable.ScrollingLineGraph_graphTitle);
        labelXText = a.getString(R.styleable.ScrollingLineGraph_labelXText);
        labelYText = a.getString(R.styleable.ScrollingLineGraph_labelYText);
    }

    /**
     * Start scrolling the graph.
     */
    private void start(){
        if (!isRunning){
            isRunning = true;
            startTime = System.currentTimeMillis();

            new Thread(new Runnable(){
                @Override
                public void run(){

                    try{
                        while (isRunning){
                            synchronized (UPDATE_LOCK){
                                if (!anyPointsVisible && value == 0){
                                    UPDATE_LOCK.wait();
                                }
                            }


                            switch (mode){
                                case REAL_TIME:
                                    if (System.currentTimeMillis() - lastUpdate >= dataRefreshRate){
                                        //if (autoScale) autoScale();
                                        postInvalidate();
                                        lastUpdate = System.currentTimeMillis();
                                    }
                                    break;

                                case DELAYED:
                                    if (System.currentTimeMillis() - lastUpdate >= graphRefreshRate){
                                        //if (autoScale) autoScale();
                                        postInvalidate();
                                        lastUpdate = System.currentTimeMillis();
                                    }
                                    break;
                            }


                            elapsedTime = System.currentTimeMillis() - startTime - totalPauseTime;

                            if (System.currentTimeMillis() - lastDataUpdate >= dataRefreshRate){
                                synchronized (POINTS_LOCK){
                                    if (points.size() == 0 || points.get(points.size() - 1).getY() != value){
                                        points.add(new Point(unitsX + elapsedTime, (int) value));
                                    }
                                }
                                lastDataUpdate = System.currentTimeMillis();
                            }




                            //final long remainingTime = graphRefreshRate - (System.currentTimeMillis() - lastUpdate);
                            //final long sleepTime = remainingTime > 0 ? remainingTime / 2 : 0;

                            Thread.sleep(graphRefreshRate);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    /**
     * Automatically change {@link ScrollingLineGraph#unitsX} and {@link ScrollingLineGraph#unitsY}
     * to fit the data points within the graph boundaries.
     */
    private void autoScale(){
        try{
            final Point point = points.get(points.size() - 1);
            if (point.getY() > unitsY * 0.95f){
                setUnitsY(point.getY() * 1.05f);
            }

            if (System.currentTimeMillis() - lastResetTime >= 5000){
                setUnitsY(highestVisiblePoint.getY() * 1.05f);
                highestVisiblePoint = null;
            }
        }catch (Exception e){
        }
    }

    public void pause(){
        pauseStartTime = System.currentTimeMillis();
    }

    public void resume(){
        totalPauseTime += (System.currentTimeMillis() - pauseStartTime);
    }

    public void setValue(float value){
        if (!isRunning) start();
        if (this.value != value){
            if (this.value == 0){
                synchronized (POINTS_LOCK){
                    elapsedTime = System.currentTimeMillis() - startTime - totalPauseTime;
                    points.add(new Point(unitsX + elapsedTime, 0));
                }
            }
            this.value = value;
            try{
                synchronized (UPDATE_LOCK){
                    UPDATE_LOCK.notifyAll();
                }
            }catch (IllegalMonitorStateException e){
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        recalculate();
    }

    private void recalculate(){
        graphHeight = getHeight() - titleTextSize - titlePadding - (float) Math.ceil(borderWidth / 2);

        graphBoundsPixel = new Rect(80, 50, getWidth() - 80, (int) graphHeight - 50);

        pixelsPerUnitX = (graphBoundsPixel.right - graphBoundsPixel.left) / unitsX;
        //pixelsPerUnitY = graphHeight / unitsY;
        pixelsPerUnitY = (graphBoundsPixel.bottom - graphBoundsPixel.top) / unitsY;
        Log.e(TAG, "PPX: " + pixelsPerUnitX + " PPY: " + pixelsPerUnitY);
    }

    private float getStringWidth(final String text){
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }

    Rect graphBoundsPixel;

    private boolean anyPointsVisible = false;

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        paint.setAntiAlias(true);

        drawTitle(canvas);

        //LEFT, TOP, RIGHT, BOTTOM
        final Rect graphBounds = new Rect(graphBoundsPixel);
        graphBounds.left /= pixelsPerUnitX;
        graphBounds.top /= pixelsPerUnitY;
        graphBounds.right /= pixelsPerUnitX;
        graphBounds.bottom /= pixelsPerUnitY;

        //Translate the canvas to begin drawing the graph. This allows (0, 0) to be the top left of the graph instead of the view itself.
        canvas.translate(0, titleTextSize + titlePadding);
        canvas.save();

        //canvas.clipRect(graphBoundsPixel, Region.Op.REPLACE);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(40, 0, 255, 0));
        canvas.drawRect(graphBoundsPixel, paint);

        //Draw horizontal scale lines
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(scaleWidth);
        paint.setColor(scaleColor);
        /*for (float i = unitsY - scaleY; i > 0; i -= scaleY){
            canvas.drawLine(0, i * pixelsPerUnitY, getWidth(), i * pixelsPerUnitY, paint);
        }*/
        for (float i = graphBounds.bottom - scaleY; i > graphBounds.top; i -= scaleY){
            canvas.drawLine(graphBounds.left * pixelsPerUnitX, i * pixelsPerUnitY, graphBounds.right * pixelsPerUnitX, i * pixelsPerUnitY, paint);
        }

        //Calculate horizontal translation
        if (startTime == -1){
            translationX = 0;
        }else{
            elapsedTime = System.currentTimeMillis() - startTime - totalPauseTime;
            translationX = -((float) elapsedTime / 1000) * scrollSpeed;
            if (mode == Mode.DELAYED){
                translationX += (scrollSpeed / 1000) * dataRefreshRate;
            }
        }
        //Log.e(TAG, "Translation: " + translationX);

        canvas.translate(translationX * pixelsPerUnitX, 0);

        //Draw vertical scale lines
        paint.setStrokeWidth(scaleWidth);
        paint.setColor(scaleColor);
        /*for (float i = scaleX; i < unitsX - translationX; i += scaleX){
            canvas.drawLine(i * pixelsPerUnitX, 0, i * pixelsPerUnitX, graphHeight, paint);
        }*/
        for (float i = graphBounds.left; i < graphBounds.right - translationX; i += scaleX){
            if (i > graphBounds.left - translationX)
                canvas.drawLine(i * pixelsPerUnitX, graphBounds.top * pixelsPerUnitY, i * pixelsPerUnitX, graphBounds.bottom * pixelsPerUnitY, paint);
        }

        //DRAW GRAPH

        //Draw graph
        synchronized (POINTS_LOCK){
            path.reset();
            highlightPath.reset();
            Point point;
            Point previousPoint = null;
            boolean isInBounds = true;
            boolean allPointsSame = true;

            int iterationCount = 0;

            for (int i = points.size() - 1; i >= 0; i--){

                //Get the point from the list
                point = points.get(i);
                if (previousPoint == null) previousPoint = point;

                //do some random shit
                if (highestVisiblePoint == null || point.getY() > highestVisiblePoint.getY()){
                    highestVisiblePoint = point;
                    lastResetTime = System.currentTimeMillis();
                }

                if (point.getY() > (0.2f * unitsY)){
                    lastResetTime = System.currentTimeMillis();
                }

                if (point.getY() != previousPoint.getY()){
                    allPointsSame = false;
                }

                //Check if the point is passed the left border

                if (mode == Mode.REAL_TIME){
                    isInBounds = -(3f) <= (point.getX() + translationX) /*&& (point.getX() + translationX) <= unitsX*/;
                }else if (mode == Mode.DELAYED){
                    //isInBounds = -((scrollSpeed / 1000) * dataRefreshRate) <= (point.getX() + translationX) && ((point.getX() + translationX)) <= unitsX + ((scrollSpeed / 1000) * dataRefreshRate);
                    isInBounds = -(3f) <= (point.getX() + translationX) /*&& ((point.getX() + translationX)) <= (translationX + unitsX) - (2 * scaleX)*/;
                    //Log.e(TAG, "Translation: " + translationX + " Bounds: " + (translationX + (2 * scaleX)) + " <= " + ((point.getX() + translationX)) + " <= " + ((translationX + unitsX) - (2 * scaleX)));
                }

                //If the first point is out of bounds, stop drawing
                if (i == 0 && !isInBounds){
                    break;
                }

                //Log.e(TAG, "Point " + point.toString() + " inBounds? " + isInBounds);

                //Log.e(TAG, (point.getX() + translationX) + " is " + (isInBounds ? "within" : "outside") + " bounds.");


                // Draw a line from the previous point to the current point
                paint.setColor(lineColor);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);

                if (previousPoint.getY() == 0 && point.getY() == 0){
                    path.moveTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom);
                    highlightPath.moveTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom);
                }

                /*if (previousPoint.getY() > unitsY && point.getY() > unitsY){
                    continue;
                }*/

                //Start the path at the first point
                if (path.isEmpty()){
                    path.moveTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY));
                    drawSmallCircle(canvas, graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY), Color.YELLOW);
                }else{
                    path.lineTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY));
                    drawSmallCircle(canvas, graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY), Color.BLACK);
                }

                if (highlightPath.isEmpty()){
                    highlightPath.moveTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom);
                }
                highlightPath.lineTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY));

                if (!isInBounds){
                    //Draw stuff  and iterate one more time
                    path.lineTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY));
                    highlightPath.lineTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY));
                    highlightPath.lineTo(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom);
                    drawSmallCircle(canvas, graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY), Color.BLACK);
                    break;
                }

                if (i == 0){
                    //canvas.drawLine(graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom - (point.getY() * pixelsPerUnitY), graphBoundsPixel.left + (point.getX() * pixelsPerUnitX), graphBoundsPixel.bottom, paint);
                    path.lineTo(graphBoundsPixel.left + (pixelsPerUnitX * point.getX()), graphBoundsPixel.bottom);
                    highlightPath.lineTo(graphBoundsPixel.left + (pixelsPerUnitX * point.getX()), graphBoundsPixel.bottom);
                    drawSmallCircle(canvas, graphBoundsPixel.left + (pixelsPerUnitX * point.getX()), graphBoundsPixel.bottom, Color.BLACK);
                }

                previousPoint = point;

                iterationCount++;
            }

            //Draw the path
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            canvas.drawPath(path, paint);

            //Draw the highlight
            highlightPath.close();
            paint.setColor(highlightColor);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
            canvas.drawPath(highlightPath, paint);

            //Log.e(TAG, "Iterated " + iterationCount + " times.");
            anyPointsVisible = !allPointsSame;
        }

        canvas.restore();

        //Draw the axis labels
        paint.setColor(highlightColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setTextSize(labelTextSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(labelColor);
        if (labelYText == null){
            canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(unitsY), padding, paint.getTextSize(), paint);
        }else{
            canvas.drawText(labelYText, padding, paint.getTextSize(), paint);
        }
        canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(0), padding, graphHeight - padding, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        if (labelXText == null){
            canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(unitsX), getWidth() - padding, graphHeight - padding, paint);
        }else{
            canvas.drawText(labelXText, getWidth() - padding, graphHeight - padding, paint);
        }
        paint.setTextSize(62);
        canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(value), getWidth() - padding, paint.getTextSize(), paint);

        //Draw the graph border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setColor(borderColor);
        canvas.drawRect((float) Math.ceil(borderWidth / 2), 0, getWidth() - (float) Math.ceil(borderWidth / 2), graphHeight, paint);
    }

    private void drawTitle(final Canvas canvas){
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(borderColor);
        paint.setTextSize(titleTextSize);
        paint.setTextAlign(Paint.Align.LEFT);

        if (ellipsis && getStringWidth(title) > getWidth()){
            String titleString = "";
            for (char c : title.toCharArray()){
                if (getStringWidth(titleString + c + ellipsisText) < getWidth()){
                    titleString += c;
                }else{
                    titleString = titleString.trim() + ellipsisText;
                    break;
                }
            }
            canvas.drawText(titleString, 0, paint.getTextSize(), paint);
        }else{
            canvas.drawText(title, 0, paint.getTextSize(), paint);
        }
    }

    /* @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        paint.setAntiAlias(true);

        //Draw the title
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(borderColor);
        paint.setTextSize(titleTextSize);
        paint.setTextAlign(Paint.Align.LEFT);

        if (ellipsis && getStringWidth(title) > getWidth()){
            String titleString = "";
            for (char c : title.toCharArray()){
                if (getStringWidth(titleString + c + ellipsisText) < getWidth()){
                    titleString += c;
                }else{
                    titleString = titleString.trim() + ellipsisText;
                    break;
                }
            }
            canvas.drawText(titleString, 0, paint.getTextSize(), paint);
        }else{
            canvas.drawText(title, 0, paint.getTextSize(), paint);
        }

        //Translate the canvas to begin drawing the graph. This allows (0, 0) to be the top left of the graph instead of the view itself.
        canvas.translate(0, titleTextSize + titlePadding);

        //Draw horizontal scale lines
        paint.setStrokeWidth(scaleWidth);
        paint.setColor(scaleColor);
        for (float i = unitsY - scaleY; i > 0; i -= scaleY){
            canvas.drawLine(0, i * pixelsPerUnitY, getWidth(), i * pixelsPerUnitY, paint);
        }

        canvas.save();

        //Calculate translation
        if (elapsedTime == 0){
            translationX = 0;
        }else{
            translationX = -((float) elapsedTime / 1000) * scrollSpeed;
            if (mode == Mode.DELAYED){
                translationX += (scrollSpeed / 1000) * dataRefreshRate;
            }
        }

        canvas.translate(translationX * pixelsPerUnitX, 0);

        //Log.e(TAG, "Elapsed time: " + elapsedTime + " TranslationX: " + translationX);

        //Draw vertical scale lines
        paint.setStrokeWidth(scaleWidth);
        paint.setColor(scaleColor);
        for (float i = scaleX; i < unitsX - translationX; i += scaleX){
            canvas.drawLine(i * pixelsPerUnitX, 0, i * pixelsPerUnitX, graphHeight, paint);
        }

        //Draw graph
        synchronized (POINTS_LOCK){
            path.reset();
            Point point;
            Point previousPoint = null;
            boolean isInBounds = true;
            boolean allPointsSame = true;

            int iterationCount = 0;

            for (int i = points.size() - 1; i >= 0; i--){

                //Get the next point from the list
                point = points.get(i);
                if (previousPoint == null) previousPoint = point;

                if (highestVisiblePoint == null || point.getY() > highestVisiblePoint.getY()){
                    highestVisiblePoint = point;
                    lastResetTime = System.currentTimeMillis();
                }

                if (point.getY() > (0.2f * unitsY)){
                    lastResetTime = System.currentTimeMillis();
                }

                if (point.getY() != previousPoint.getY()){
                    allPointsSame = false;
                }

                //Check if the point is within the graph bounds
                if (mode == Mode.REAL_TIME){
                    //isInBounds = 0 <= (point.getX() + translationX) && ((int) (point.getX() + translationX)) <= unitsX;
                    isInBounds = (2 * scaleX) <= (point.getX() + translationX) && ((int) (point.getX() + translationX)) <= unitsX - (2 * scaleX);
                }else if (mode == Mode.DELAYED){
                    //isInBounds = -((scrollSpeed / 1000) * dataRefreshRate) <= (point.getX() + translationX) && ((point.getX() + translationX)) <= unitsX + ((scrollSpeed / 1000) * dataRefreshRate);
                    isInBounds = translationX + (2 * scaleX) <= (point.getX() + translationX) && ((point.getX() + translationX)) <= (translationX + unitsX) - (2 * scaleX);
                    Log.e(TAG, "Translation: " + translationX + " Bounds: " + (translationX + (2 * scaleX)) + " <= " + ((point.getX() + translationX)) + " <= " + ((translationX + unitsX) - (2 * scaleX)));
                    //Why do the points stay above 10,000?
                    //We need to get relative position?
                    //Graph starts at 10,000 because first point starts on the right side
                }

                //Log.e(TAG, (point.getX() + translationX) + " is " + (isInBounds ? "within" : "outside") + " bounds.");

                //Draw a line from the previous point to the current point
                paint.setColor(lineColor);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                boolean conditionA = point.getY() > unitsY;
                boolean conditionB = previousPoint.getY() > unitsY;

                //Start the path at the bottom right point
                if (path.isEmpty()) path.moveTo(pixelsPerUnitX * previousPoint.getX(), graphHeight);
                drawSmallCircle(canvas, pixelsPerUnitX * previousPoint.getX(), graphHeight);

                if (conditionA | conditionB){

                    if (conditionA && conditionB){
                        path.lineTo((point.getX() * pixelsPerUnitX), 0);
                        drawSmallCircle(canvas, (point.getX() * pixelsPerUnitX), 0);
                    }else{
                        final float rise = point.getY() - previousPoint.getY();
                        final float run = point.getX() - previousPoint.getX();
                        final float slope = rise / run;
                        final float x = ((unitsY - point.getY()) / slope) + point.getX();

                        path.lineTo((x * pixelsPerUnitX), 0);
                        drawSmallCircle(canvas, (x * pixelsPerUnitX), 0);

                        if (conditionA){
                            canvas.drawLine((previousPoint.getX() * pixelsPerUnitX), graphHeight - (previousPoint.getY() * pixelsPerUnitY), (x * pixelsPerUnitX), 0, paint);
                        }else if (conditionB){
                            canvas.drawLine((x * pixelsPerUnitX), 0, (point.getX() * pixelsPerUnitX), graphHeight - (point.getY() * pixelsPerUnitY), paint);
                            path.lineTo((point.getX() * pixelsPerUnitX), graphHeight - (point.getY() * pixelsPerUnitY));
                            drawSmallCircle(canvas, (point.getX() * pixelsPerUnitX), graphHeight - (point.getY() * pixelsPerUnitY));
                        }
                    }

                }else{
                    canvas.drawLine((previousPoint.getX() * pixelsPerUnitX), graphHeight - (previousPoint.getY() * pixelsPerUnitY), (point.getX() * pixelsPerUnitX), graphHeight - (point.getY() * pixelsPerUnitY), paint);
                    path.lineTo((point.getX() * pixelsPerUnitX), graphHeight - (point.getY() * pixelsPerUnitY));
                    drawSmallCircle(canvas, (point.getX() * pixelsPerUnitX), graphHeight - (point.getY() * pixelsPerUnitY));
                }

                if (i == 0){
                    canvas.drawLine((point.getX() * pixelsPerUnitX), graphHeight - (point.getY() * pixelsPerUnitY), (point.getX() * pixelsPerUnitX), graphHeight, paint);
                    path.lineTo(pixelsPerUnitX * point.getX(), graphHeight);
                    drawSmallCircle(canvas, pixelsPerUnitX * point.getX(), graphHeight);
                }

                *//*if (!isInBounds){
                    path.lineTo(pixelsPerUnitX * point.getX(), 0);
                    path.lineTo(pixelsPerUnitX * point.getX(), graphHeight);
                    drawSmallCircle(canvas, pixelsPerUnitX * point.getX(), 0);
                    drawSmallCircle(canvas, pixelsPerUnitX * point.getX(), graphHeight);
                    break;
                }*//*

                previousPoint = point;

                iterationCount++;
            }

            //Draw the path (the graph outline)
            path.close();
            paint.setColor(highlightColor);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
            canvas.drawPath(path, paint);

            //Log.e(TAG, "Iterated " + iterationCount + " times.");
            anyPointsVisible = !allPointsSame;
        }

        canvas.restore();

        paint.setColor(highlightColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setTextSize(labelTextSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(labelColor);
        if (labelYText == null){
            canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(unitsY), padding, paint.getTextSize(), paint);
        }else{
            canvas.drawText(labelYText, padding, paint.getTextSize(), paint);
        }
        canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(0), padding, graphHeight - padding, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        if (labelXText == null){
            canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(unitsX), getWidth() - padding, graphHeight - padding, paint);
        }else{
            canvas.drawText(labelXText, getWidth() - padding, graphHeight - padding, paint);
        }

        paint.setTextSize(62);
        canvas.drawText(NumberFormat.getInstance(Locale.getDefault()).format(value), getWidth() - padding, paint.getTextSize(), paint);

        //Draw the graph border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setColor(borderColor);
        canvas.drawRect((float) Math.ceil(borderWidth / 2), 0, getWidth() - (float) Math.ceil(borderWidth / 2), graphHeight, paint);
    }*/

    private Paint.Style prevStyle;
    private int prevColor;
    private float prevStroke;

    private void drawSmallCircle(Canvas canvas, float x, float y, int color){
        prevStyle = paint.getStyle();
        prevColor = paint.getColor();
        prevStroke = paint.getStrokeWidth();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(4);
        canvas.drawCircle(x, y, 7, paint);
        paint.setStyle(prevStyle);
        paint.setColor(prevColor);
        paint.setStrokeWidth(prevStroke);
    }

    //Getters and setters

    public void setUnitsX(float unitsX){
        this.unitsX = unitsX;
        recalculate();
    }

    public void setUnitsY(float unitsY){
        this.unitsY = unitsY;
        recalculate();
    }
}
