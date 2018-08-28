package com.tycho.app.primenumberfinder;

import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author Tycho Bellers
 * Date Created: 7/25/2018
 */
public class LongClickLinkMovementMethod extends LinkMovementMethod {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LongClickLinkMovementMethod.class.getSimpleName();

    private long lastClickTime;

    /**
     * The delay in milliseconds that determines what should be considered a long click.
     */
    private final long clickDelay = 500;

    private boolean touchDown;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            touchDown = true;
            lastClickTime = System.currentTimeMillis();
            handler.postDelayed(() -> {
                //Make sure that the touch was down for the entire delay duration
                if (touchDown && (System.currentTimeMillis() - lastClickTime >= clickDelay)) {

                    //Get absolute position of the TextView
                    final int[] location = new int[2];
                    widget.getLocationOnScreen(location);

                    //Compute touch position relative to the TextView
                    int x = (int) event.getX() - location[0];
                    int y = (int) event.getY() - location[1];

                    //Find the character offset that was touched
                    final Layout layout = widget.getLayout();
                    final int line = layout.getLineForVertical(y);
                    final int horizontalOffset = layout.getOffsetForHorizontal(line, x);

                    final LongClickableSpan[] link = buffer.getSpans(horizontalOffset, horizontalOffset, LongClickableSpan.class);
                    if (link.length != 0) {
                        link[0].onLongClick(widget, x, y);
                        touchDown = false;
                    }
                }
            }, clickDelay);
        } else if (action == MotionEvent.ACTION_UP) {
            touchDown = false;
        }

        return super.onTouchEvent(widget, buffer, event);
    }


    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new LongClickLinkMovementMethod();

        return sInstance;
    }

    private static LongClickLinkMovementMethod sInstance;
}
