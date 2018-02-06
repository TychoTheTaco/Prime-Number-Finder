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
import android.view.MotionEvent;
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
     * The vertical spacing (in pixels) between layers of the tree.
     */
    private float verticalSpacing;

    /**
     * The horizontal spacing between items in each level
     */
    private float[] horizontalSpacing;

    private float paddingLeft = 5;
    private float paddingRight = 5;
    private float paddingTop = 3;
    private float paddingBottom = 3;

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
        verticalSpacing = getStringHeight() + 40;
    }

    int count = 0;

    boolean generated = false;

    public Bitmap drawToBitmap(){
        final float borderPadding = 10;
        final Rect bounds = getBoundingRect(rectTree);
        final Bitmap bitmap = Bitmap.createBitmap((int) (Math.abs(bounds.width()) + (borderPadding * 2)), (int) (Math.abs(bounds.height()) + (borderPadding * 2)), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        //Draw tree
        canvas.drawColor(Color.WHITE);
        canvas.translate(-bounds.left + borderPadding, -rectTree.getValue().bottom + borderPadding);
        debugRectangles(rectTree, canvas, Color.argb(50, 0, 100, 0));
        drawContents(rectTree, tree, canvas);

        return bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setClipBounds(new Rect(0, 0, getWidth(), getHeight()));
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

            //Draw view border
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            canvas.drawRect(0, 0, getWidth() - 1, getHeight() - 1, paint);

            if (!generated && tree != null){
                while(true){
                    rectTree = generateRectangleTree(0, getStringHeight() / 2, tree, 0);
                    if (!checkChildren(rectTree, 0)) break;
                }
                generated = true;
            }

            if (tree != null){
                //Draw tree contents
                canvas.translate(getWidth() / 2, 0);
                canvas.translate(translationX, translationY);
                debugRectangles(rectTree, canvas, Color.argb(50, 0, 100, 0));
                drawContents(rectTree, tree, canvas);
            }
        }
    }

    private float translationX;
    private float translationY;
    private float lastTouchX;
    private float lastTouchY;

    private float scrollPaddingLeft = 20;
    private float scrollPaddingRight = 20;
    private float scrollPaddingTop = 20;
    private float scrollPaddingBottom = 20;

    private static final int INVALID_POINTER_ID = -1;

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                final float y = event.getY();

                lastTouchX = x;
                lastTouchY = y;

                // Save the ID of this pointer
                mActivePointerId = event.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                final float dx = x - lastTouchX;
                final float dy = y - lastTouchY;

                final Rect bounds = getBoundingRect(rectTree);

                final float maxTranslationX = -(bounds.right - (getWidth() / 2)) - scrollPaddingRight;
                final float minTranslationX = -(bounds.left + (getWidth() / 2)) + scrollPaddingLeft;
                final float minTranslationY = 0 + scrollPaddingTop;
                final float maxTranslationY = getHeight() - Math.abs(bounds.height()) - scrollPaddingBottom;

                translationX += dx;
                translationY += dy;

                if (bounds.width() < getWidth()){
                    if (translationX > maxTranslationX){
                        translationX = maxTranslationX;
                    }else if (translationX < minTranslationX){
                        translationX = minTranslationX;
                    }
                }else{
                    if (translationX < maxTranslationX){
                        translationX = maxTranslationX;
                    }else if (translationX > minTranslationX){
                        translationX = minTranslationX;
                    }
                }

                if (Math.abs(bounds.height()) < getHeight()){
                    if (translationY > maxTranslationY){
                        translationY = maxTranslationY;
                    }else if (translationY < minTranslationY){
                        translationY = minTranslationY;
                    }
                }else{
                    if (translationY < maxTranslationY){
                        translationY = maxTranslationY;
                    }else if (translationY > minTranslationY){
                        translationY = minTranslationY;
                    }
                }

                lastTouchX = x;
                lastTouchY = y;

                invalidate();
                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
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

        final Rect bounds = new Rect((int) (textX - paddingLeft), (int) (centerY + (getStringHeight() / 2) + paddingTop), (int) (textX + getStringWidth(text) + paddingRight), (int) (centerY - (getStringHeight() / 2) - paddingBottom));

        final Tree<Rect> rectangleTree = new Tree<>(bounds);

        if (tree.getChildren().size() > 0){
            float totalWidth = 0;
            float[] sizes = new float[tree.getChildren().size()];
            for (int i = 0; i <  tree.getChildren().size(); i++){
                sizes[i] = getStringWidth(tree.getChildren().get(i).getValue().toString());
                totalWidth += sizes[i];
            }
            final float totalSpacing = tree.getChildren().size() > 0 ? (tree.getChildren().size() - 1) * horizontalSpacing[level + 1] : 0;

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

    private Rect getBoundingRect(final Tree<Rect> rectTree){
        final float left = findLeft(rectTree, 0);
        final float right = findRight(rectTree, 0);
        final float bottom = findBottom(rectTree, rectTree.getValue().bottom);
        final float top = findTop(rectTree, 0);
        return new Rect((int) left, (int) top, (int) right, (int) bottom);
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

        float smallest = value;

        if (rectTree.getValue().bottom < smallest){
            smallest = rectTree.getValue().bottom;
        }

        float smallestChild;
        for (Tree<Rect> child : rectTree.getChildren()){
            smallestChild = findBottom(child, smallest);
            if (smallestChild < smallest){
                smallest = smallestChild;
            }
        }

        return smallest;
    }

    private float findTop(final Tree<Rect> rectTree, float value){

        float largest = value;

        if (rectTree.getValue().top > largest){
            largest = rectTree.getValue().top;
        }

        float largestChild;
        for (Tree<Rect> child : rectTree.getChildren()){
            largestChild = findTop(child, largest);
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
            }
        }else if (side == 1){
            if (rect.left <= border){
                off = border - rect.left;
            }
        }

        if (off > 0){
            horizontalSpacing[level + 1] += (off * 2) + 20;
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
