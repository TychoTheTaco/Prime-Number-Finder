package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.graphics.Bitmap;
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

    public Bitmap drawToBitmap(){
        final Rect bounds = calculateBounds(rectTree);
        Log.d(TAG,"Export width: " + bounds.width());
        Log.d(TAG,"Export height: " + bounds.height());
        final Bitmap bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        this.draw(new Canvas(bitmap));
        return bitmap;
    }

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

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(1);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

            if (!generated){
                while(true){
                    rectTree = generateRectangleTree(0, getStringHeight() / 2 + 10, tree, 0);
                    if (!checkChildren(rectTree, 0)) break;
                }
                Log.d(TAG, "Canvas width: " + canvas.getWidth());
                Log.d(TAG, "Generated:\n" + rectTree.format());
                generated = true;
            }

            canvas.translate(getWidth() / 2, 0);
            debugRectangles(rectTree, canvas, Color.argb(50, 0, 0, 100));
            drawContents(rectTree, tree, canvas);
        }
    }

    private boolean checkChildren(Tree<Rect> rectTree, int level){

        if (fixOverlaps(rectTree, level)) return true;

        for (Tree<Rect> child : rectTree.getChildren()){
            if (fixOverlaps(child, level + 1)) return true;
        }

        for (Tree<Rect> child : rectTree.getChildren()){
            if (checkChildren(child, level + 1)) return true;
        }

        return false;
    }

    private void debugRectangles(final Tree<Rect> rectTree, final Canvas canvas, final int color){
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(color);
        canvas.drawRect(rectTree.getValue(), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1);
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

    private boolean fixOverlaps(final Tree<Rect> rectTree, int level){
        final float border = rectTree.getValue().exactCenterX();
        boolean overlap = false;
        for (int i = 0; i < rectTree.getChildren().size(); i++){
            overlap = checkOverlaps(rectTree.getChildren().get(i), border, level, i);
            if (overlap) break;
        }
        return overlap;
    }

    private Rect calculateBounds(final Tree<Rect> rectTree){
        final float left = findLeft(rectTree, getWidth() / 2);
        final float right = findRight(rectTree, getWidth() / 2);
        final float bottom = findBottom(rectTree, 0);
        Log.d(TAG, "Left bound is " + left);
        Log.d(TAG, "Right bound is " + right);
        Log.d(TAG, "Bottom bound is " + bottom);
        //return new Rect(0, (int) bottom, (int) (right - left), 0);
        return new Rect((int) left, 0, (int) right, (int) bottom);
    }

    private float findLeft(final Tree<Rect> rectTree, float value){

        float smallest = value;

        if (rectTree.getValue().left < smallest){
            smallest = rectTree.getValue().left;
        }

        float smallerChild;
        for (Tree<Rect> child : rectTree.getChildren()){
            smallerChild = findLeft(child, smallest);
            if (smallerChild < smallest){
                smallest = smallerChild;
            }
        }

        return smallest;
    }

    private float findRight(final Tree<Rect> rectTree, float value){

        float largest = value;

        if (rectTree.getValue().right > largest){
            largest = rectTree.getValue().right;
        }

        float largestChild;
        for (Tree<Rect> child : rectTree.getChildren()){
            largestChild = findRight(child, largest);
            if (largestChild > largest){
                largest = largestChild;
            }
        }

        return largest;
    }

    private float findBottom(final Tree<Rect> rectTree, float value){

        float largest = value;

        if (rectTree.getValue().bottom > largest){
            largest = rectTree.getValue().bottom;
        }

        float largestChild;
        for (Tree<Rect> child : rectTree.getChildren()){
            largestChild = findBottom(child, largest);
            if (largestChild > largest){
                largest = largestChild;
            }
        }

        return largest;
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
