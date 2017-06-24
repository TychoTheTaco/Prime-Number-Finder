package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 3/2/2017
 */

public class FactorTreeView extends View{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FactorTreeView";

    private final Paint paint = new Paint();

    private TreeNode treeNode;

    private float startingAngle = 30;
    private float dividerLength = 45;
    private float dividerPadding = 15;

    private SparseArray<Float> anglesPerLevel = new SparseArray<>();

    //Vertical distance between each level
    private float levelHeightDistance = 70;

    private float[] offsets;

    public FactorTreeView(Context context){
        super(context);
        init();
    }

    public FactorTreeView(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public FactorTreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
    }

    public FactorTreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        paint.setAntiAlias(true);

        treeNode = new TreeNode(810);
        treeNode.addNode(new TreeNode(27)
                .addNode(new TreeNode(3))
                .addNode(new TreeNode(9)
                        .addNode(new TreeNode(3))
                        .addNode(new TreeNode(3))
                )
        );
        treeNode.addNode(new TreeNode(30)
                .addNode(new TreeNode(2))
                .addNode(new TreeNode(15)
                        .addNode(new TreeNode(3))
                        .addNode(new TreeNode(5))
                )
        );

        Log.e(TAG, "Tree has " + treeNode.countLevels() + " levels.");

        offsets = new float[treeNode.countLevels()];
        Arrays.fill(offsets, -1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        Log.e(TAG, "Drawing " + treeNode.getValue());

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(48);

        iterCount = 0;
        rectangles.clear();
        rects.clear();
        //anglesPerLevel.clear();
        //drawTreeOld(canvas, treeNode, getWidth() / 2, ((getStringHeight() - paint.descent()) / 2), 0);
        drawTree(canvas, treeNode, getWidth()/2, ((getStringHeight() - paint.descent()) / 2), /*0, 0,*/ 0);

        String off = "";
        for (int i = 0; i < offsets.length; i++){
            off += offsets[i] + " ";
        }
        Log.e(TAG, "Offsets were: " + off);

        /*paint.setColor(Color.RED);
        paint.setStrokeWidth(9);

        canvas.drawLine(startX, 0, startX, getStringHeight(), paint);
        paint.setColor(Color.GREEN);
        canvas.drawLine(startX + 9, 0, startX + 9, -paint.ascent(), paint);
        paint.setColor(Color.BLUE);
        canvas.drawLine(startX + 18, 0, startX + 18, paint.descent(), paint);

        Log.e(TAG, getStringHeight() + " " + -paint.ascent() + " " + paint.descent() + " " + paint.getTextSize() + " " + startY);*/

        /*Log.e(TAG, "Angles were" + anglesPerLevel);

        boolean redraw = false;

        outer:for (List<Rect> list : rectangles){
            final int level = rectangles.indexOf(list);
            Log.e(TAG, "Level " + level + " contains: " + list);
            for (Rect rect : list){
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.parseColor("#00C900"));
                paint.setStrokeWidth(2);
                canvas.drawRect(rect, paint);
                for (Rect rect1 : list){
                    if (rect != rect1 && rect.intersect(rect1)){
                        Log.e(TAG, "INTERSECTION!");
                        //redraw = true;
                        final float existingAngle = (anglesPerLevel.get(level - 1) == null ? startingAngle : anglesPerLevel.get(level - 1));
                        anglesPerLevel.put(level - 1, existingAngle - 10);
                        Log.e(TAG, "New angles are" + anglesPerLevel);
                        //break outer;
                    }
                }
            }
        }

        if (redraw){
            //startingAngle += 10;
            try{
                Thread.sleep(500);
            }catch (InterruptedException e){}
            invalidate();
        }

        Log.e(TAG, "H: " + getStringHeight());

        primes.clear();
        getFinalLevel(treeNode);*/

        boolean redraw = false;
        int intersectLevel;

        outer:for (List<Rect> list : rects){
            final int level = rects.indexOf(list);
            Log.e(TAG, "Level " + level + " contains: " + list);
            for (Rect rect : list){
                for (Rect rect1 : list){
                    if (rect != rect1 && Rect.intersects(rect, rect1)){
                        Log.e(TAG, "INTERSECTION!");
                        redraw = true;
                        intersectLevel = level;
                        offsets[intersectLevel - 2] += 10;

                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.BLUE);
                        canvas.drawRect(rect, paint);
                        paint.setColor(Color.GREEN);
                        canvas.drawRect(rect1, paint);
                        paint.setStyle(Paint.Style.FILL);

                        break outer;
                    }
                }
            }
        }

        if (redraw){
            /*try{
                Thread.sleep(1000);
            }catch (InterruptedException e){}*/
            Log.e(TAG, "Redrawing...");
            invalidate();
        }
    }

    public void setTree(TreeNode tree){
        this.treeNode = tree;
        Log.e(TAG, "Tree has " + treeNode.countLevels() + " levels.");
        offsets = new float[treeNode.countLevels()];
        Arrays.fill(offsets, -1);
        postInvalidate();
    }

    private int iterLimit = 99;
    private int iterCount = 0;

    private final List<List<Rect>> rectangles = new ArrayList<>();

    private final List<List<Rect>> rects = new ArrayList<>();

    final int[] colors = new int[]{Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.GRAY};

    private void drawTree(final Canvas canvas, TreeNode treeNode, float centerX, float centerY/*, float offsetX, float offsetY, */,int level){
        final String text = NumberFormat.getNumberInstance().format(treeNode.getValue());

        //Draw text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(treeNode.getNodes().size() == 0 ? Color.RED : Color.BLACK);
        float textX = centerX /*+ offsetX*/ - (getStringWidth(text) / 2);
        float textY = centerY /*+ offsetY*/ + ((getStringHeight() - paint.descent()) / 2);
        canvas.drawText(text, textX, textY, paint);

        //Draw rectangles
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        bounds.offset((int) textX, (int) textY);

        List<Rect> levelRectangles;
        try{
            levelRectangles = rects.get(level);
            levelRectangles.add(bounds);
        }catch (IndexOutOfBoundsException e){
            levelRectangles = new ArrayList<>();
            levelRectangles.add(bounds);
            rects.add(levelRectangles);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(colors[level]);
        paint.setStrokeWidth(1);
        canvas.drawRect(bounds, paint);
        paint.setStyle(Paint.Style.FILL);

        /*boolean isOverlap = false;

        //Redraw if rects overlap
        Log.e(TAG, "Level " + level + " rectangles are: " + levelRectangles);
        for (Rect r : levelRectangles){
            for (Rect re : levelRectangles){
                if (r != re && Rect.intersects(r, re)){
                    Log.e(TAG, "INTERSECTION on level " + level + " between " + r + " AND " + re);

                    isOverlap = true;

                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.BLUE);
                    canvas.drawRect(r, paint);
                    paint.setColor(Color.GREEN);
                    canvas.drawRect(re, paint);
                    paint.setStyle(Paint.Style.FILL);
                }
            }
        }

        if (isOverlap){
            rects.remove(level);
            canvas.restore();
            if (offsetX < 0){
                drawTree(canvas, treeNode, centerX, centerY, offsetX - 30, offsetY, level - 1);
            }else{
                drawTree(canvas, treeNode, centerX, centerY, offsetX + 30, offsetY, level - 1);
            }
        }else{

            canvas.save();

            //Check overlaps and inrease offset for certain levels
            int xOff = level == 0 ? 50 : (50 + (level * 5));
            xOff += offsetX;
            int yOff = (int) offsetY + 70;

            for (int i = 0; i < treeNode.getNodes().size(); i++){
                if (i == 0){
                    drawTree(canvas, treeNode.getNodes().get(i), centerX, centerY, -xOff, yOff, level+1);
                }else{
                    drawTree(canvas, treeNode.getNodes().get(i), centerX, centerY, xOff, yOff, level+1);
                }
            }
        }*/

        //Check overlaps and inrease offset for certain levels
        float xOff; /*= level == 0 ? 50 : (50 + (level * 5));*/
        //xOff += offsetX;
        if (offsets[level] == -1){
            xOff = (level == 0 ? 50 : (50 + (level * 5))) /*+ offsetX*/;
            offsets[level] = xOff;
        }else{
            xOff = offsets[level];
        }
        int yOff = /*(int) offsetY + */70;

        for (int i = 0; i < treeNode.getNodes().size(); i++){
            if (i == 0){
                drawTree(canvas, treeNode.getNodes().get(i), centerX - xOff, centerY + yOff,/* -xOff, yOff,*/ level+1);
            }else{
                drawTree(canvas, treeNode.getNodes().get(i), centerX + xOff, centerY + yOff,/* xOff, yOff,*/ level+1);
            }
        }
    }

    private void drawTreeOld(Canvas canvas, TreeNode treeNode, float centerX, float centerY, int level){
        final String text = NumberFormat.getNumberInstance().format(treeNode.getValue());

        paint.setStyle(Paint.Style.FILL);
        if (treeNode.getNodes().size() == 0){
            paint.setColor(Color.RED);
        }else{
            paint.setColor(Color.BLACK);
        }
        //float stringDistance = 0;
        float textX = centerX - (getStringWidth(text) / 2);
        //float textX = centerX - stringDistance;
        float textY = centerY + ((getStringHeight() - paint.descent()) / 2);
        canvas.drawText(text, textX, textY, paint);

        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        bounds.offset((int) textX, (int) textY);
        //Log.e(TAG, "Rect for " + text + " is " + bounds.left + " " + bounds.bottom + " " + bounds.right + " " + bounds.top);

        List<Rect> levelRectangles;
        try{
            levelRectangles = rectangles.get(level);
            levelRectangles.add(bounds);
        }catch (IndexOutOfBoundsException e){
            levelRectangles = new ArrayList<>();
            levelRectangles.add(bounds);
            rectangles.add(levelRectangles);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1);
        canvas.drawRect(bounds, paint);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < treeNode.getNodes().size(); i++){

            canvas.save();
            paint.setColor(Color.DKGRAY);
            paint.setStrokeWidth(3);

            float x = 0;
            float y = 0;

            //float angle = startingAngle / (1.5f * (level + 1));
            Log.e(TAG, "Drawing angles are " + anglesPerLevel);
            float angle = (anglesPerLevel.get(level) == null ? startingAngle : anglesPerLevel.get(level));

            float offset = (getStringWidth(text) / 4);

            if (i == 0){

                x = (float) ((levelHeightDistance) * Math.tan(Math.toRadians(-angle)));
                y = levelHeightDistance;

                final float distanceBetweenCenters = (float) Math.hypot(x, y);
                final float lineLength = distanceBetweenCenters / 2;

                canvas.rotate(angle, centerX - offset, centerY);
                canvas.drawLine(centerX - offset, centerY + (y / 2) - (lineLength / 2), centerX - offset, centerY + (y / 2) - (lineLength / 2) + lineLength, paint);
                //canvas.drawLine(centerX - offset, centerY, centerX - offset, centerY + distanceBetweenCenters, paint);
                offset *= -1;
                canvas.restore();
                //x = (float) ((dividerLength * 2 + dividerPadding) * Math.sin(Math.toRadians(-angle)));
                //y = (float) ((dividerLength * 2 + dividerPadding) * Math.cos(Math.toRadians(-angle)));

                Log.e(TAG, "HYPOT: " + Math.hypot(x, y));
            }else if (i == 1){

                x = (float) ((levelHeightDistance) * Math.tan(Math.toRadians(angle)));
                y = levelHeightDistance;

                final float distanceBetweenCenters = (float) Math.hypot(x, y);
                final float lineLength = distanceBetweenCenters / 2;

                canvas.rotate(-angle, centerX + offset, centerY);
                canvas.drawLine(centerX + offset, centerY + (y / 2) - (lineLength / 2), centerX + offset, centerY + (y / 2) - (lineLength / 2) + lineLength, paint);
                //canvas.drawLine(centerX, textY - dividerPadding * 3, centerX, textY + dividerPadding + dividerLength * 3, paint);
                canvas.restore();

            }

            iterCount++;
            if (iterCount >= iterLimit) break;;

            drawTreeOld(canvas, treeNode.getNodes().get(i), centerX + x + offset, centerY + y, level + 1);
        }


    }

    private List<Long> primes = new ArrayList<>();

    private void getFinalLevel(TreeNode treeNode){
        for (TreeNode tree : treeNode.getNodes()){
            getFinalLevel(tree);
        }
        if (treeNode.getNodes().size() == 0)
        primes.add(treeNode.getValue());
    }

    private float getStringWidth(final String text){
        final Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.width();
    }

    private float getStringHeight(){
        return -paint.ascent() + paint.descent();
    }
}
