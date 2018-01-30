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

    int count = 0;

    boolean generated = false;

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

            //Draw lines
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), paint);

            //debugRectangles(rectTree, canvas, Color.argb(50, 0, 100, 0));

            if (!generated){
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                Log.d(TAG, "Generated:\n" + rectTree.format());

                /*fixed = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                Log.d(TAG, "Overlap 1: " + checkAllOverlaps(fixed, 0));
                Log.d(TAG, "Overlap 2: " + checkAllOverlaps(fixed, 0));
                Log.d(TAG, "Overlap 3: " + checkAllOverlaps(fixed, 0));*/

                //rectTree = callUntilFalse(rectTree, 0);
                //rectTree = callUntilFalse(rectTree.getChildren().get(0), 1);
                //rectTree = callUntilFalse(rectTree.getChildren().get(1), 1);

                fixOverlaps(rectTree, 0); //Overlap - start over
                /*rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree, 0); //Overlap - start over
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree, 0); //Overlap - start over
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree, 0); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(0), 1); //Overlap - start over
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree, 0); //Overlap - start over
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree, 0); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(0), 1); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(0).getChildren().get(0), 2); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(0).getChildren().get(1), 2); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(0).getChildren().get(1).getChildren().get(0), 3); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(0).getChildren().get(1).getChildren().get(1), 3); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(1), 1); //No overlap
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(1).getChildren().get(0), 2); //No overlap*/


                //doFixes(rectTree, rectTree, 0);

               /* boolean didOverlap = false;
                while (true){
                    final boolean value = fixOverlaps(rectTree, 0);
                    rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                    didOverlap = (value | didOverlap);
                    if (!value) break;
                }
                Log.d(TAG, "didOverlap: " + didOverlap);

                didOverlap = false;
                while (true){
                    final boolean value = fixOverlaps(rectTree.getChildren().get(0), 0);
                    rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                    didOverlap = (value | didOverlap);
                    if (!value) break;
                }
                Log.d(TAG, "didOverlap: " + didOverlap);*/


                /*rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree.getChildren().get(0), 0);
                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                fixOverlaps(rectTree, -1);*/

                rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
                Log.d(TAG, "Generated final tree:\n" + rectTree.format());
                generated = true;
            }

            //debugRectangles(rectTree, canvas, Color.argb(50, 0, 100, 0));
            //debugRectangles(generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0), canvas, Color.argb(50, 100, 0, 0));

            //fixOverlaps(rectTree.getChildren().get(0), 0);
            //fixOverlaps(rectTree, -1);

            debugRectangles(rectTree, canvas, Color.argb(50, 0, 0, 100));
            drawContents(rectTree, tree, canvas);



            /*fixOverlaps(rectTree, rectTree, -1);
            final Tree<Rect> modified = generateRectangleTree(1080 / 2, getStringHeight(), tree, 0);
            debugRectangles(modified, canvas, Color.argb(50, 0, 0, 100));*/
        }
    }

    private Tree<Rect> fixed;

    private Tree<Rect> callUntilFalse(Tree<Rect> rectTree, int level){
        boolean didOverlap = false;
        while (true){
            final boolean value = fixOverlaps(rectTree, level);
            rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
            didOverlap = (value | didOverlap);
            if (!value) break;
        }
        Log.d(TAG, "didOverlap: " + didOverlap);
        return rectTree;
    }

    private boolean checkAllOverlaps(Tree<Rect> rectTree, int level){

        boolean didOverlap = false;
        while (true){
            final boolean value = fixOverlaps(rectTree, level);
            fixed = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
            rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
            didOverlap = (value | didOverlap);
            if (!value) break;
        }

        if (didOverlap){
            Log.d(TAG, "Returning early!");
            return true;
        }

        for (Tree<Rect> child : rectTree.getChildren()){
            didOverlap = checkAllOverlaps(child, level + 1);
            if (didOverlap) break;
        }

        return didOverlap;
    }

    private boolean doFixes(final Tree<Rect> root, Tree<Rect> rectTree, int level){

        boolean didOverlap = false;
        while (true){
            final boolean value = fixOverlaps(rectTree, 0);
            rectTree = generateRectangleTree(getWidth() / 2, getStringHeight(), tree, 0);
            didOverlap = (value | didOverlap);
            if (!value) break;
        }

        if (didOverlap){
            doFixes(root, root, 0);
            return true;
        }else{
            boolean childrenOverlapped = false;
            for (Tree<Rect> child : rectTree.getChildren()){
                childrenOverlapped = doFixes(root, child, level + 1);
                if (childrenOverlapped) break;
            }
            return childrenOverlapped;
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
        //float textY = centerY + (getStringHeight() / 2) - paint.descent();

        final Rect bounds = new Rect((int) textX, (int) (centerY + (getStringHeight() / 2)), (int) (textX + getStringWidth(text)), (int) (centerY - (getStringHeight() / 2)));

        final Tree<Rect> rectangleTree = new Tree<>(bounds);

        if (tree.getChildren().size() > 0){
            float totalWidth = 0;
            float[] sizes = new float[tree.getChildren().size()];
            for (int i = 0; i <  tree.getChildren().size(); i++){
                sizes[i] = getStringWidth(tree.getChildren().get(i).getValue().toString());
                totalWidth += sizes[i];
            }
            final float totalSpacing = tree.getChildren().size() > 0 ? (tree.getChildren().size() - 1) * horizontalSpacing[level + 1] : 0;
            //Log.d(TAG, "Generating level " + level + " with " + horizontalSpacing[level + 1] + " child spacing");

            float previousOffset = 0;
            for (int i = 0; i < tree.getChildren().size(); i++){
                final float offset = previousOffset + (sizes[i] / 2);
                rectangleTree.addNode(generateRectangleTree(centerX - ((totalWidth + totalSpacing) / 2) + offset, centerY + verticalSpacing, tree.getChildren().get(i), level + 1));
                previousOffset = offset + (sizes[i] / 2) + horizontalSpacing[level + 1];
            }
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

    private void fixOverlaps(final Tree<Rect> root, final Tree<Rect> rectangles, int level){
        final float border = rectangles.getValue().exactCenterX();
        Log.d(TAG, "Fix overlaps with border: " + border);
        boolean overlap = false;
        for (int i = 0; i < rectangles.getChildren().size(); i++){
            overlap = checkOverlaps(rectangles.getChildren().get(i), border, level, i);
            if (overlap) break;
        }

        if (overlap){
            //Restart from beginning
            fixOverlaps(root, root, -1);
        }else{
            for (int i = 0; i < rectangles.getChildren().size(); i++){
                fixOverlaps(root, rectangles.getChildren().get(i), level + 1);
            }
        }
    }

    /*private boolean fix(final Tree<Rect> rectangles){
        boolean overlap = false;
        for (int i = 0; i < rectTree.getChildren().size(); i++){
            overlap = checkOverlaps(rectTree.getChildren().get(i), border, level, i);
            if (overlap) break;;
        }
        return overlap;
    }*/

    private boolean fixOverlaps(final Tree<Rect> rectTree, int level){
        final float border = rectTree.getValue().exactCenterX();
        Log.d(TAG, "Fix overlaps with border: " + border);
        boolean overlap = false;
        for (int i = 0; i < rectTree.getChildren().size(); i++){
            overlap = checkOverlaps(rectTree.getChildren().get(i), border, level, i);
            if (overlap) break;
        }
        Log.d(TAG, "Overlapped: " + overlap);
        return overlap;
    }

    private boolean checkOverlaps(final Tree<Rect> rectTree, final float border, final int level, int side){

        final Rect rect = rectTree.getValue();

        float off = 0;
        if (side == 0){
            if (rect.right >= border){
                off = rect.right - border;
                Log.d(TAG, "Rectangle out of bounds (right collision): " + rect + "\noff by " + off);
            }
        }else if (side == 1){
            if (rect.left <= border){
                off = border - rect.left;
                Log.d(TAG, "Rectangle out of bounds (left collision): " + rect + "\noff by " + off);
            }
        }

        if (off > 0){
            horizontalSpacing[level + 1] += (off * 2) + 20;
            Log.d(TAG, "Modified horizontal spacing on level " + (level + 1) + ": " + horizontalSpacing[level + 1]);
            return true;
        }

        boolean overlap = false;
        for (int i = 0; i < rectTree.getChildren().size(); i++){
            overlap = checkOverlaps(rectTree.getChildren().get(i), border, level, side);
            if (overlap) break;
        }

        return overlap;
    }

    public void setTree(Tree<?> tree) {
        this.tree = tree;
        horizontalSpacing = new float[tree.getLevels()];
        Arrays.fill(horizontalSpacing, 40);
        finished = false;
        generated = false;
        invalidate();
        Log.d(TAG, "Set tree:\n" + tree.format());
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
