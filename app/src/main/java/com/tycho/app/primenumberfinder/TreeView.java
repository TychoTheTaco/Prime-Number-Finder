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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private float verticalSpacing;

    private float[] horizontalSpacing;

    private float[] offsets;

    private List<List<Rect>> rectangles = new ArrayList<>();

    private Tree<Rect> rectTree;

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
        paint.setTextSize(48);
        verticalSpacing = getStringHeight() + 20;
    }

    private final Runnable process = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Started processing");
            processing = true;

            boolean intersectionFound;
            do {
                //calculatePositions(tree, getWidth() / 2, getStringHeight(), 0);

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
            //if (!finished) new Thread(process).start();

            //drawTree(canvas, tree, getWidth() / 2, getStringHeight(), 0);
            //calculateBounds(canvas, tree, getWidth() / 2, getStringHeight(), 0, rectTree);

            //debugRectangles(rectTree, canvas, Color.argb(50, 0, 100, 0));
            Log.d(TAG, "Checking overlaps for " + tree.getValue());
            fixOverlaps(rectTree, 1080 / 2, -1);
            //debugRectangles(generateRectangleTree(1080 / 2, getStringHeight(), tree, 0), canvas, Color.argb(50, 100, 0, 0));

            Log.d(TAG, "Checking overlaps for " + tree.getChildren().get(0).getValue());
            fixOverlaps(rectTree.getChildren().get(0), rectTree.getChildren().get(0).getValue().exactCenterX(), 0);
            //debugRectangles(generateRectangleTree(1080 / 2, getStringHeight(), tree, 0), canvas, Color.argb(50, 0, 0, 100));

            Log.d(TAG, "Checking overlaps for " + tree.getValue());
            fixOverlaps(rectTree, 1080 / 2, -1);
            final Tree<Rect> modified = generateRectangleTree(1080 / 2, getStringHeight(), tree, 0);
            debugRectangles(modified, canvas, Color.argb(50, 0, 0, 100));

            drawContents(modified, tree, canvas);

            for (float space : horizontalSpacing){
                Log.d(TAG, "Space: " + space);
            }
        }
    }

    private void debugRectangles(final Tree<Rect> rectTree, final Canvas canvas, final int color){
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(color);
        canvas.drawRect(rectTree.getValue(), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRect(rectTree.getValue(), paint);

        for (Tree<Rect> child : rectTree.getChildren()){
            debugRectangles(child, canvas, color);
        }
    }

    private Tree<Rect> generateRectangleTree(final float centerX, final float centerY, final Tree<?> tree, int level){
        //Draw text
        final String text = tree.getValue().toString();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(tree.getChildren().size() == 0 ? Color.RED : Color.BLACK);
        paint.setStrokeWidth(2);
        float textX = centerX - (getStringWidth(text) / 2);
        float textY = centerY + (getStringHeight() / 2) - paint.descent();

        final Rect bounds = new Rect((int) textX, (int) (centerY + (getStringHeight() / 2)), (int) (textX + getStringWidth(text)), (int) (centerY - (getStringHeight() / 2)));

        float totalWidth = 0;
        float[] sizes = new float[tree.getChildren().size()];
        for (int i = 0; i <  tree.getChildren().size(); i++){
            sizes[i] = getStringWidth(tree.getChildren().get(i).getValue().toString());
            totalWidth += sizes[i];
        }
        final float totalSpacing = tree.getChildren().size() > 0 ? (tree.getChildren().size() - 1) * horizontalSpacing[level] : 0;

        float previousOffset = 0;
        final Tree<Rect> rectangleTree = new Tree<>(bounds);
        for (int i = 0; i < tree.getChildren().size(); i++){
            final float offset = previousOffset + (sizes[i] / 2);
            rectangleTree.addNode(generateRectangleTree(centerX - ((totalWidth + totalSpacing) / 2) + offset, centerY + verticalSpacing, tree.getChildren().get(i), level + 1));
            previousOffset = offset + (sizes[i] / 2) + horizontalSpacing[level];
        }

        return rectangleTree;
    }

    private void drawContents(final Tree<Rect> rectTree, final Tree<?> tree, final Canvas canvas){

        //Draw text
        final String text = tree.getValue().toString();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(tree.getChildren().size() == 0 ? Color.RED : Color.BLACK);
        paint.setStrokeWidth(2);
        float textX = rectTree.getValue().exactCenterX() - (getStringWidth(text) / 2);
        float textY = rectTree.getValue().exactCenterY() + (getStringHeight() / 2) - paint.descent();
        canvas.drawText(text, textX, textY, paint);

        for (int i = 0; i < tree.getChildren().size(); i++){

            //Draw lines
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            canvas.drawLine(rectTree.getValue().exactCenterX(), rectTree.getValue().top, rectTree.getChildren().get(i).getValue().exactCenterX(), rectTree.getChildren().get(i).getValue().bottom, paint);

            drawContents(rectTree.getChildren().get(i), tree.getChildren().get(i), canvas);
        }

    }

    private void fixOverlaps(final Tree<Rect> rectTree, final float border, int level){
        Log.d(TAG, "Fix overlaps with border: " + border);
        for (int i = 0; i < rectTree.getChildren().size(); i++){
            checkOverlaps(rectTree.getChildren().get(i), border, level, i);
        }
    }

    private boolean checkOverlaps(final Tree<Rect> rectTree, final float border, final int level, int side){

        final Rect rect = rectTree.getValue();

        if (side == 0){
            if (rect.right >= border){
                Log.d(TAG, "Rectangle out of bounds (right collision): " + rect);
                horizontalSpacing[level] += rect.right - border + 2;
                Log.d(TAG, "Modified horizontal spacing on level " + level + ": " + horizontalSpacing[level]);
                return true;
            }
        }else if (side == 1){
            if (rect.left <= border){
                Log.d(TAG, "Rectangle out of bounds (left collision): " + rect);
                horizontalSpacing[level] += border - rect.left + 2;
                Log.d(TAG, "Modified horizontal spacing on level " + level + ": " + horizontalSpacing[level]);
                return true;
            }
        }

        boolean overlap = false;
        for (int i = 0; i < rectTree.getChildren().size(); i++){
            overlap = checkOverlaps(rectTree.getChildren().get(i), border, level + 1, side);
            if (overlap) break;
        }

        return overlap;
    }

    private void generateGroups(final Canvas canvas){
        for (List<Rect> list : rectangles){
            Log.d(TAG, "Generating level " + rectangles.indexOf(list) + " group.");
            Rect group = null;
            for (Rect rect : list){
                Log.d(TAG, "Adding " + rect);
                if (group == null){
                    group = rect;
                }else{
                    //group.union(rect);
                    group.union(rect.left, rect.top);
                    group.union(rect.right, rect.bottom);
                    Log.d(TAG, "Group: " + group);
                }
            }

            if (group != null){
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setColor(Color.argb(100, random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                canvas.drawRect(group, paint);
            }
        }
    }

    final Random random = new Random();

    public void setTree(Tree<?> tree) {
        this.tree = tree;
        rectangles.clear();
        for (int i = 0; i < tree.getLevels(); i++) {
            rectangles.add(new ArrayList<Rect>());
        }
        offsets = new float[tree.getLevels()];
        horizontalSpacing = new float[tree.getLevels()];
        Arrays.fill(horizontalSpacing, 40);
        Arrays.fill(offsets, 40);
        finished = false;
        invalidate();
        Log.d(TAG, "Set tree: " + tree.getValue());
        Log.d(TAG, "Filled: " + tree.getLevels() + " levels.");

        //Cacalate
        rectTree = generateRectangleTree(1080 / 2, getStringHeight(), tree, 0);
        Log.d(TAG, "Rect Tree:\n" + rectTree.format());
    }

    private float getStringWidth(final String text) {
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.right + bounds.left;
    }

    private float getStringHeight() {
        return -paint.ascent() + paint.descent();
    }
}
