package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import simpletrees.Tree;

/**
 * @author Tycho Bellers
 *         Date Created: 3/2/2017
 */

public class TreeView extends View {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FactorTreeView";

    /**
     * The {@linkplain Tree} object displayed by this view.
     */
    private Tree<?> tree;

    /**
     * The {@linkplain Paint} object used for drawing to the canvas.
     */
    private final Paint paint = new Paint();

    private volatile boolean processing = false;
    private volatile boolean finished = false;

    /**
     * The minimum vertical spacing (in pixels) between layers of the tree.
     */
    private float verticalSpacing = 50;

    private float startingAngle = 30;
    private float dividerLength = 45;
    private float dividerPadding = 15;

    private float[] offsets;

    private List<List<Rect>> rectangles = new ArrayList<>();

    public TreeView(Context context) {
        super(context);
        init();
    }

    public TreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
    }

    private final Runnable process = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Started processing");
            processing = true;

            boolean intersectionFound;
            do {
                calculatePositions(tree, getWidth() / 2, getStringHeight(), 0);

                intersectionFound = false;
                for (int i = 0; i < rectangles.size(); i++) {

                    //Get rectangles in this row
                    final List<Rect> rowRectangles = rectangles.get(i);

                    //Check if any rectangles overlap
                    for (Rect a : rowRectangles) {
                        for (Rect b : rowRectangles) {

                            //Find the intersection between 2 rectangles
                            Rect intersection = new Rect();
                            if (a != b && (intersection.setIntersect(a, b))) {
                                //Intersection
                                Log.d(TAG, a + " intersects " + b);
                                Log.w(TAG, "Intersection on level " + (i + 1));
                                Log.d(TAG, "Intersect width: " + intersection.width());
                                offsets[i - 1] += intersection.width() + 40; //impossible to intersect on level 0
                                intersectionFound = true;
                                break;
                            }
                        }
                        if (intersectionFound) break;
                    }
                    if (intersectionFound) break;
                }

                //Reset rectangles list
                if (intersectionFound) {
                    rectangles.clear();
                    for (int i = 0; i < tree.getLevels(); i++) {
                        rectangles.add(new ArrayList<Rect>());
                    }
                }

            } while (intersectionFound);

            processing = false;
            finished = true;
            Log.d(TAG, "Finished processing");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                    Log.d(TAG, "Invalidate called");
                }
            });
        }
    };

    int count = 0;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (processing) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            paint.setTextSize(48);
            String text = "Generating tree";
            for (int i = 0; i < count; i++) {
                text += '.';
            }
            canvas.drawText(text, getWidth() / 2 - getStringWidth(text) / 2, getHeight() / 2, paint);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {}

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (++count > 5) count = 0;
                            invalidate();
                        }
                    });
                }
            }).start();

        } else {
            if (!finished) new Thread(process).start();

            drawTree(canvas, tree, getWidth() / 2, getStringHeight(), 0);
        }
    }

    final int[] DEBUG_COLORS = {
            Color.argb(100, 255, 0, 0),
            Color.argb(100, 0, 255, 0),
            Color.argb(100, 0, 0, 255)
    };

    private void calculatePositions(final Tree<?> tree, final float centerX, final float centerY, int level) {
        final float SPACING = offsets[level];
        Log.d(TAG, "Level " + level + " calculate spacing: " + SPACING);

        //Calculate total row width
        float totalWidth = 0;
        final float totalSpacing = tree.getChildren().size() > 0 ? (tree.getChildren().size() - 1) * SPACING : 0;
        float[] sizes = new float[tree.getChildren().size()];
        for (int i = 0; i < tree.getChildren().size(); i++) {
            if (tree.getChildren().get(i).getValue() instanceof String) {
                sizes[i] = getStringWidth((String) tree.getChildren().get(i).getValue());
            } else {
                sizes[i] = getStringWidth(NumberFormat.getNumberInstance().format(tree.getChildren().get(i).getValue()));
            }
            totalWidth += sizes[i];
        }

        //Debug
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.argb(100, 0, 200, 0));
        paint.setStrokeWidth(1);
        final Rect rect = new Rect(
                (int) (centerX - (totalWidth / 2) - (totalSpacing / 2)),
                (int) (centerY + 50 - (getStringHeight() / 2) + paint.descent()),
                (int) (centerX - (totalWidth / 2) - (totalSpacing / 2) + totalWidth + totalSpacing),
                (int) (centerY + 50 + 50 - (getStringHeight() / 2))
        );
        if (rect.width() > 0) rectangles.get(level).add(rect);


        float previousOffset = 0;
        for (int i = 0; i < tree.getChildren().size(); i++) {
            float offset = previousOffset + (sizes[i] / 2);
            calculatePositions(tree.getChildren().get(i), centerX - (totalWidth / 2) - (totalSpacing / 2) + offset, centerY + 50, level + 1);
            previousOffset = offset + (sizes[i] / 2) + SPACING; //Add the remaining half of the item width to prepare for next iteration
        }
    }

    private void drawTree(final Canvas canvas, final Tree<?> tree, final float centerX, final float centerY, int level) {

        final float SPACING = offsets[level];
        //Log.d(TAG, "Level " + level + " draw spacing: " + SPACING);

        //Draw text
        final String text;
        if (tree.getValue() instanceof String) {
            text = (String) tree.getValue();
        } else {
            text = NumberFormat.getNumberInstance().format(tree.getValue());
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(tree.getChildren().size() == 0 ? Color.RED : Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setTextSize(48);
        float textX = centerX - (getStringWidth(text) / 2);
        float textY = centerY + ((getStringHeight() - paint.descent()) / 2);
        canvas.drawText(text, textX, textY, paint);

        //Debug
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        canvas.drawLine(centerX, centerY, centerX, centerY + 50, paint);

        //Calculate total row width
        float totalWidth = 0;
        final float totalSpacing = tree.getChildren().size() > 0 ? (tree.getChildren().size() - 1) * SPACING : 0;
        float[] sizes = new float[tree.getChildren().size()];
        for (int i = 0; i < tree.getChildren().size(); i++) {
            if (tree.getChildren().get(i).getValue() instanceof String) {
                sizes[i] = getStringWidth((String) tree.getChildren().get(i).getValue());
            } else {
                sizes[i] = getStringWidth(NumberFormat.getNumberInstance().format(tree.getChildren().get(i).getValue()));
            }
            totalWidth += sizes[i];
        }

        //Debug
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.argb(100, 0, 200, 0));
        paint.setStrokeWidth(1);
        final Rect rect = new Rect(
                (int) (centerX - (totalWidth / 2) - (totalSpacing / 2)),
                (int) (centerY + 50 - (getStringHeight() / 2) + paint.descent()),
                (int) (centerX - (totalWidth / 2) - (totalSpacing / 2) + totalWidth + totalSpacing),
                (int) (centerY + 50 + 50 - (getStringHeight() / 2))
        );
        canvas.drawRect(rect, paint);

        float previousOffset = 0;
        for (int i = 0; i < tree.getChildren().size(); i++) {
            float offset = previousOffset + (sizes[i] / 2);
            //Log.d(TAG, tree.getValue() + ": offset[" + i + "] = " + offset);
            drawTree(canvas, tree.getChildren().get(i), centerX - (totalWidth / 2) - (totalSpacing / 2) + offset, centerY + 50, level + 1);
            previousOffset = offset + (sizes[i] / 2) + SPACING; //Add the remaining half of the item width to prepare for next iteration
        }

    }

    public void setTree(Tree<?> tree) {
        this.tree = tree;
        rectangles.clear();
        for (int i = 0; i < tree.getLevels(); i++) {
            rectangles.add(new ArrayList<Rect>());
        }
        offsets = new float[tree.getLevels()];
        Arrays.fill(offsets, 40);
        finished = false;
        invalidate();
        Log.d(TAG, "Set tree: " + tree.getValue());
    }

    private float getStringWidth(final String text) {
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }

    private float getStringHeight() {
        return -paint.ascent() + paint.descent();
    }
}
