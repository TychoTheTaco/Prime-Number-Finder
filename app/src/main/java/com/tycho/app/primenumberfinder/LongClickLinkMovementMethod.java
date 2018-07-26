package com.tycho.app.primenumberfinder;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author Tycho Bellers
 * Date Created: 7/25/2018
 */
public class LongClickLinkMovementMethod extends LinkMovementMethod{

    private long lastClickTime;
    private final long clickDelay = 250;
    private int lastX = 0;
    private int lastY = 0;

    private boolean touchDown;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN){
            touchDown = true;
        }else if (action == MotionEvent.ACTION_UP){
            touchDown = false;
        }

        if (action == MotionEvent.ACTION_DOWN){
            lastClickTime = System.currentTimeMillis();
        }

        if (touchDown && System.currentTimeMillis() - lastClickTime >= clickDelay){
            buffer.getSpans(0, buffer.length(), LongClickableSpan.class)[0].onLongClick(widget);
            touchDown = false;
            return true;
        }

        /*if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            lastX = x;
            lastY = y;
            int deltaX = Math.abs(x-lastX);
            int deltaY = Math.abs(y-lastY);

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            LongClickableSpan[] link = buffer.getSpans(off, off, LongClickableSpan.class);

            if (link.length != 0) {
                if (touchDown){
                    if (System.currentTimeMillis() - lastClickTime < clickDelay){
                        Log.w("LCLMM", "onClick()");
                        link[0].onClick(widget);
                    }else if (deltaX < 10 && deltaY < 10){
                        Log.w("LCLMM", "onLongClick()");
                        link[0].onLongClick(widget);
                    }
                }else{
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                    lastClickTime = System.currentTimeMillis();
                }
                //return true?
            }

            touchDown = (action == MotionEvent.ACTION_DOWN);
            Log.d("TAG", "touchDown: " + touchDown);
            Log.d("TAG", "event: " + action);
        }*/

        return super.onTouchEvent(widget, buffer, event);
    }


    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new LongClickLinkMovementMethod();

        return sInstance;
    }

    private static LongClickLinkMovementMethod sInstance;
}
