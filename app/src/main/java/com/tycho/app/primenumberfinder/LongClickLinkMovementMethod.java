package com.tycho.app.primenumberfinder;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author Tycho Bellers
 * Date Created: 7/25/2018
 */
public class LongClickLinkMovementMethod extends LinkMovementMethod{

    private long lastClickTime;
    private final long clickDelay = 1000;
    private int lastX = 0;
    private int lastY = 0;

    private boolean touchDown;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event){
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN){
            touchDown = true;
            lastClickTime = System.currentTimeMillis();
        }else if (action == MotionEvent.ACTION_UP){
            touchDown = false;
        }

        if (touchDown){
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (action == MotionEvent.ACTION_DOWN){
                lastX = x;
                lastY = y;
            }
            int deltaX = Math.abs(x - lastX);
            int deltaY = Math.abs(y - lastY);

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            final Layout layout = widget.getLayout();
            final int line = layout.getLineForVertical(y);
            final int off = layout.getOffsetForHorizontal(line, x);

            LongClickableSpan[] link = buffer.getSpans(off, off, LongClickableSpan.class);

            if (link.length != 0){
                if (touchDown){
                    if (System.currentTimeMillis() - lastClickTime >= clickDelay){
                        Log.w("LCLMM", "onLongClick()");
                        link[0].onLongClick(widget);
                        touchDown = false;
                        return true;
                    }
                }else if (action == MotionEvent.ACTION_UP){
                    Log.d("LCLMM", "Early release!");
                }
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }


    public static MovementMethod getInstance(){
        if (sInstance == null)
            sInstance = new LongClickLinkMovementMethod();

        return sInstance;
    }

    private static LongClickLinkMovementMethod sInstance;
}
