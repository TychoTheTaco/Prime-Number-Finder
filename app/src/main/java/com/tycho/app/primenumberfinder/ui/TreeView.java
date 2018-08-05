package com.tycho.app.primenumberfinder.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.tycho.app.primenumberfinder.R;

import simpletrees.Tree;

/**
 * @author Tycho Bellers
 *         Date Created: 3/2/2017
 */

public class TreeView extends View {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = TreeView.class.getSimpleName();

    /**
     * The {@linkplain Tree} object displayed by this view.
     */
    private Tree<?> tree;

    /**
     * The {@linkplain Paint} object used for drawing to the canvas.
     */
    private final Paint paint = new Paint();

    /**
     * The horizontal spacing between items in each level.
     */
    private float[] horizontalSpacing;

    private float paddingLeft = 5;
    private float paddingRight = 5;
    private float paddingTop = -2;
    private float paddingBottom = -2;

    private ExportOptions exportOptions;

    private boolean touchEnabled;

    public TreeView(Context context) {
        super(context);
        init(context, null);
    }

    public TreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public TreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attributeSet) {
        paint.setAntiAlias(true);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        exportOptions = new ExportOptions();
        exportOptions.imageBackgroundColor = Color.WHITE;
        exportOptions.itemTextSize = 14;
        exportOptions.itemTextColor = Color.BLACK;
        exportOptions.itemBackgrounds = true;
        exportOptions.itemBackgroundColor = Color.argb(50, 0, 100, 0);
        exportOptions.verticalSpacing = 40;
        exportOptions.primeFactorTextColor = Color.RED;
        exportOptions.itemBorderColor = Color.argb(255, 0, 130, 0);
        exportOptions.itemBorderWidth = 1;
        exportOptions.branchColor = Color.BLACK;
        exportOptions.branchWidth = 2;

        //Set custom attributes
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TreeView, 0, 0);
        try {
            touchEnabled = typedArray.getBoolean(R.styleable.TreeView_touchEnabled, true);
        }finally {
            typedArray.recycle();
        }
    }

    private boolean generated = false;

    public boolean isGenerated(){
        return this.generated;
    }

    //TODO: maybe make border padding = text size?
    private final float borderPadding = 10;

    public Bitmap drawToBitmap(final ExportOptions options) {
        final Rect bounds = getBoundingRect(itemTree);
        final Paint creditsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        float size = options.itemTextSize >= 30 ? (options.itemTextSize / 2.5f) : 12;
        final String text = "Created with Prime Number Finder on Android";
        while(getStringWidth(text, paint) > (Math.abs(bounds.width()) + (borderPadding * 2))){
            size -= 0.25f;
            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, getResources().getDisplayMetrics()));
        }

        final Bitmap bitmap = Bitmap.createBitmap((int) (Math.abs(bounds.width()) + (borderPadding * 2)), (int) (Math.abs(bounds.height()) + (borderPadding * 2) + getStringHeight(creditsPaint) + borderPadding), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        //Draw tree
        canvas.drawColor(options.imageBackgroundColor);
        canvas.save();
        canvas.translate(-bounds.left + borderPadding, borderPadding);
        drawItemBackgrounds(itemTree, canvas, options);
        drawContents(itemTree, canvas, options);
        canvas.restore();
        drawCredits(canvas, creditsPaint, options);

        return bitmap;
    }

    private void drawCredits(final Canvas canvas, final Paint paint, final ExportOptions options){
        final String text = "Created with Prime Number Finder on Android";

        //Draw text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        float textX = canvas.getWidth() - getStringWidth(text, paint) - borderPadding;
        float textY = canvas.getHeight() - borderPadding;
        canvas.drawText(text, textX, textY, paint);
    }

    private Rect clipBounds = new Rect();

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        clipBounds.set(0, 0, getWidth(), getHeight());
        setClipBounds(clipBounds);
    }

    public ExportOptions getDefaultExportOptions() {
        return this.exportOptions;
    }

    private boolean threadStarted = false;

    private Tree<Item> itemTree;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        //Draw background
        canvas.drawColor(exportOptions.imageBackgroundColor);

        canvas.save();

        //Generate the tree if it hasn't been created yet
        if (!generated && tree != null) {

            //Draw text
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            final String text = "Generating...";
            canvas.drawText(text, ((canvas.getWidth() - getStringWidth(text, paint)) / 2), ((canvas.getHeight() - getStringHeight(paint)) / 2), paint);

            if (!threadStarted){
                new Thread(() -> {
                    do{
                        dirty = false;
                        while (true) {
                            itemTree = generateRectangleTree(tree, exportOptions);
                            if (!checkChildren(itemTree, 0)) break;
                        }
                        generated = true;
                    }while(dirty);
                    postInvalidate();
                    threadStarted = false;
                }).start();
                threadStarted = true;
            }
        }

        if (!dirty && tree != null && itemTree != null) {
            //Draw tree contents
            canvas.translate(getWidth() / 2, 0);
            canvas.translate(translationX, translationY);
            drawItemBackgrounds(itemTree, canvas, exportOptions);
            drawContents(itemTree, canvas, exportOptions);
        }

        canvas.restore();

        //Draw view border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(0);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    /*public void recalculateBounds(){
        translationX = 0;
        translationY = scrollPaddingTop;
        horizontalSpacing = new float[tree.getLevels()];
        generated = false;
        dirty = true;
        do{
            dirty = false;
            while (true) {
                itemTree = generateRectangleTree(tree, exportOptions);
                if (!checkChildren(itemTree, 0)) break;
            }
            generated = true;
        }while(dirty);
    }*/

    private float scrollPaddingLeft = 20;
    private float scrollPaddingRight = 20;
    private float scrollPaddingTop = 20;
    private float scrollPaddingBottom = 20;

    private float translationX;
    private float translationY = scrollPaddingTop;
    private float lastTouchX;
    private float lastTouchY;

    private static final int INVALID_POINTER_ID = -1;

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (touchEnabled && tree != null) {
            final int action = event.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = event.getX();
                    final float y = event.getY();

                    lastTouchX = x;
                    lastTouchY = y;

                    // SAVE the ID of this pointer
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

                    final Rect bounds = getBoundingRect(itemTree);

                    final float maxTranslationX = -(bounds.right - (getWidth() / 2)) - scrollPaddingRight;
                    final float minTranslationX = -(bounds.left + (getWidth() / 2)) + scrollPaddingLeft;
                    final float minTranslationY = 0 + scrollPaddingTop;
                    final float maxTranslationY = getHeight() - Math.abs(bounds.height()) - scrollPaddingBottom;

                    translationX += dx;
                    translationY += dy;

                    if (bounds.width() < getWidth()) {
                        if (translationX > maxTranslationX) {
                            translationX = maxTranslationX;
                        } else if (translationX < minTranslationX) {
                            translationX = minTranslationX;
                        }
                    } else {
                        if (translationX < maxTranslationX) {
                            translationX = maxTranslationX;
                        } else if (translationX > minTranslationX) {
                            translationX = minTranslationX;
                        }
                    }

                    if (Math.abs(bounds.height()) < getHeight()) {
                        if (translationY > maxTranslationY) {
                            translationY = maxTranslationY;
                        } else if (translationY < minTranslationY) {
                            translationY = minTranslationY;
                        }
                    } else {
                        if (translationY < maxTranslationY) {
                            translationY = maxTranslationY;
                        } else if (translationY > minTranslationY) {
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
        }

        return true;
    }

    private boolean checkChildren(Tree<Item> itemTree, int level) {

        if (fixOverlaps(itemTree, level)) return true;

        for (Tree<Item> child : itemTree.getChildren()) {
            if (fixOverlaps(child, level + 1)) return true;
        }

        for (Tree<Item> child : itemTree.getChildren()) {
            if (checkChildren(child, level + 1)) return true;
        }

        return false;
    }

    private void drawItemBackgrounds(final Tree<Item> itemTree, final Canvas canvas, final ExportOptions options) {

        if (options.itemBackgrounds){

            //Draw item background
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(options.itemBackgroundColor);
            canvas.drawRect(itemTree.getValue().bounds, paint);

            //Draw item borders
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(options.itemStyle);
            paint.setColor(options.itemBorderColor);
            paint.setStrokeWidth(options.itemBorderWidth);
            canvas.drawRect(itemTree.getValue().bounds, paint);
            paint.setStrokeJoin(Paint.Join.BEVEL);

            for (Tree<Item> child : itemTree.getChildren()) {
                drawItemBackgrounds(child, canvas, options);
            }

        }
    }

    private Tree<Item> generateRectangleTree(final Tree<?> tree, final ExportOptions options){
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, options.itemTextSize, getResources().getDisplayMetrics()));
        return generateRectangleTree(0, 0, tree, 0, paint, options);
    }

    //TODO: item heights can be slightly different, use getStringHeight instead to calculate height and just center the text vertically

    private Tree<Item> generateRectangleTree(final float centerX, final float topY, final Tree<?> tree, int level, final Paint paint, final ExportOptions options) {

        //Calculate text bounds
        final String text = tree.getValue().toString();
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, options.itemTextSize, getResources().getDisplayMetrics()));
        final Rect bounds = getStringBounds(text, paint);

        final float left = bounds.left;
        final float bottom = bounds.bottom;

        bounds.offset(((int) centerX - (bounds.width() / 2)), ((int) (topY - bounds.top + 1 + paddingTop)));

        final int height = bounds.height();
        final float stringHeight = getStringHeight(paint);

        final float textX = bounds.left - left;
        final float textY = bounds.bottom - bottom + 1 + ((stringHeight - height) / 2);

        final Tree<Item> itemTree = new Tree<>(new Item(text, bounds, textX, textY));

        //Apply padding
        bounds.left -= paddingLeft;
        bounds.top -= paddingTop;
        bounds.right += paddingRight;
        bounds.bottom += paddingBottom + (stringHeight - height);

        if (tree.getChildren().size() > 0) {
            float totalWidth = 0;
            float[] sizes = new float[tree.getChildren().size()];
            for (int i = 0; i < tree.getChildren().size(); i++) {
                sizes[i] = getStringWidth(tree.getChildren().get(i).getValue().toString(), paint);
                totalWidth += sizes[i];
            }
            final float totalSpacing = tree.getChildren().size() > 0 ? (tree.getChildren().size() - 1) * horizontalSpacing[level + 1] : 0;

            float previousOffset = 0;
            for (int i = 0; i < tree.getChildren().size(); i++) {
                final float offset = previousOffset + (sizes[i] / 2);
                itemTree.addNode(generateRectangleTree(centerX - ((totalWidth + totalSpacing) / 2) + offset, topY + bounds.height()/*+ getStringHeight(paint)*/ /*+ paddingTop + paddingBottom*/ + options.verticalSpacing, tree.getChildren().get(i), level + 1, paint, options));
                previousOffset = offset + (sizes[i] / 2) + horizontalSpacing[level + 1];
            }
        }

        return itemTree;
    }

    private void drawContents(final Tree<Item> itemTree, final Canvas canvas, final ExportOptions options) {

        //Draw text
        final String text = itemTree.getValue().text;
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, options.itemTextSize, getResources().getDisplayMetrics()));
        paint.setColor(itemTree.getChildren().size() == 0 ? options.primeFactorTextColor : options.itemTextColor);
        canvas.drawText(text, itemTree.getValue().textX, itemTree.getValue().textY, paint);

        for (int i = 0; i < itemTree.getChildren().size(); i++) {

            //Draw branches connecting nodes to parent
            paint.setColor(options.branchColor);
            paint.setStrokeWidth(options.branchWidth);
            paint.setStrokeCap(options.branchStyle);
            canvas.drawLine(itemTree.getValue().bounds.exactCenterX(), itemTree.getValue().bounds.bottom + options.branchPadding, itemTree.getChildren().get(i).getValue().bounds.exactCenterX(), itemTree.getChildren().get(i).getValue().bounds.top - options.branchPadding, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);

            drawContents(itemTree.getChildren().get(i), canvas, options);
        }

    }

    private boolean fixOverlaps(final Tree<Item> itemTree, int level) {
        final float border = itemTree.getValue().bounds.exactCenterX();
        boolean overlap = false;
        for (int i = 0; i < itemTree.getChildren().size(); i++) {
            overlap = checkOverlaps(itemTree.getChildren().get(i), border, level, i);
            if (overlap) break;
        }
        return overlap;
    }

    public Rect getBoundingRect(){
        return getBoundingRect(itemTree);
    }

    private Rect getBoundingRect(final Tree<Item> itemTree) {
        final float left = findLeft(itemTree, 0);
        final float right = findRight(itemTree, 0);
        final float bottom = findBottom(itemTree, 0);
        final float top = findTop(itemTree, itemTree.getValue().bounds.top);
        return new Rect((int) left, (int) top, (int) right, (int) bottom);
    }

    private float findLeft(final Tree<Item> itemTree, float value) {

        float smallest = value;

        if (itemTree.getValue().bounds.left < smallest) {
            smallest = itemTree.getValue().bounds.left;
        }

        float smallerChild;
        for (Tree<Item> child : itemTree.getChildren()) {
            smallerChild = findLeft(child, smallest);
            if (smallerChild < smallest) {
                smallest = smallerChild;
            }
        }

        return smallest;
    }

    private float findRight(final Tree<Item> itemTree, float value) {

        float largest = value;

        if (itemTree.getValue().bounds.right > largest) {
            largest = itemTree.getValue().bounds.right;
        }

        float largestChild;
        for (Tree<Item> child : itemTree.getChildren()) {
            largestChild = findRight(child, largest);
            if (largestChild > largest) {
                largest = largestChild;
            }
        }

        return largest;
    }

    private float findBottom(final Tree<Item> itemTree, float value) {

        float smallest = value;

        if (itemTree.getValue().bounds.bottom > smallest) {
            smallest = itemTree.getValue().bounds.bottom;
        }

        float smallestChild;
        for (Tree<Item> child : itemTree.getChildren()) {
            smallestChild = findBottom(child, smallest);
            if (smallestChild > smallest) {
                smallest = smallestChild;
            }
        }

        return smallest;
    }

    private float findTop(final Tree<Item> itemTree, float value) {

        float largest = value;

        if (itemTree.getValue().bounds.top < largest) {
            largest = itemTree.getValue().bounds.top;
        }

        float largestChild;
        for (Tree<Item> child : itemTree.getChildren()) {
            largestChild = findTop(child, largest);
            if (largestChild < largest) {
                largest = largestChild;
            }
        }

        return largest;
    }

    private boolean checkOverlaps(final Tree<Item> itemTree, final float border, final int level, int side) {

        final Rect rect = itemTree.getValue().bounds;

        float off = 0;
        if (side == 0) {
            if (rect.right >= border - 10) {
                off = rect.right - border + 10;
            }
        } else if (side == 1) {
            if (rect.left <= border + 10) {
                off = border - rect.left + 10;
            }
        }

        if (off > 0) {
            horizontalSpacing[level + 1] += (off * 2);
            return true;
        }

        boolean overlap = false;
        for (int i = 0; i < itemTree.getChildren().size(); i++) {
            overlap = checkOverlaps(itemTree.getChildren().get(i), border, level, side);
            if (overlap) break;
        }

        return overlap;
    }

    private volatile boolean dirty = true;

    public void setTree(Tree<?> tree) {
        this.tree = tree;
        horizontalSpacing = new float[tree.getLevels()];
        generated = false;
        translationX = 0;
        translationY = scrollPaddingTop;
        dirty = true;
        invalidate();
    }

    public void recalculate(){
        translationX = 0;
        translationY = scrollPaddingTop;
        horizontalSpacing = new float[tree.getLevels()];
        generated = false;
        dirty = true;
        postInvalidate();
    }

    public void redraw(){
        invalidate();
    }

    private float getStringWidth(final String text, final Paint paint) {
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return Math.abs(bounds.width());
    }

    private Rect getStringBounds(final String text, final Paint paint){
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    private float getStringHeight(final Paint paint) {
        return -paint.ascent() + paint.descent();
    }

    public static class ExportOptions implements Cloneable {

        public int imageBackgroundColor;

        public int itemTextSize;
        public int itemTextColor;
        public boolean itemBackgrounds;
        public int itemBackgroundColor;
        public int itemBorderColor;
        public float itemBorderWidth;
        public Paint.Join itemStyle = Paint.Join.ROUND;

        public int primeFactorTextColor;

        public int branchColor;
        public float branchWidth;
        public Paint.Cap branchStyle = Paint.Cap.ROUND;
        public float branchPadding = 2;

        public float verticalSpacing;

        @Override
        public ExportOptions clone() throws CloneNotSupportedException {
            return (ExportOptions) super.clone();
        }
    }

    private static class Item{

        private String text;

        private Rect bounds;

        private float textX;
        private float textY;

        public Item(final String text, final Rect bounds, final float textX, final float textY){
            this.text = text;
            this.bounds = bounds;
            this.textX = textX;
            this.textY =  textY;
        }
    }
}
