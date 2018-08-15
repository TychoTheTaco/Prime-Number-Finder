package com.tycho.app.primenumberfinder;

import android.os.Handler;
import android.os.Looper;
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
public class LongClickLinkMovementMethod extends LinkMovementMethod {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LongClickLinkMovementMethod.class.getSimpleName();

    private long lastClickTime;
    private final long clickDelay = 500;

    private boolean touchDown;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            touchDown = true;
            lastClickTime = System.currentTimeMillis();
            Log.e(TAG, "Touch Down!");
            handler.postDelayed(() -> {
                if (touchDown && (System.currentTimeMillis() - lastClickTime >= clickDelay)) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();

                    x += widget.getScrollX();
                    y += widget.getScrollY();

                    final Layout layout = widget.getLayout();
                    final int line = layout.getLineForVertical(y);
                    final int off = layout.getOffsetForHorizontal(line, x);

                    LongClickableSpan[] link = buffer.getSpans(off, off, LongClickableSpan.class);

                    if (link.length != 0) {
                        Log.w(TAG, "onLongClick()");
                        link[0].onLongClick(widget);
                        touchDown = false;
                    }
                }
            }, clickDelay);
        } else if (action == MotionEvent.ACTION_UP) {
            touchDown = false;
            Log.e(TAG, "Touch Up!");
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
