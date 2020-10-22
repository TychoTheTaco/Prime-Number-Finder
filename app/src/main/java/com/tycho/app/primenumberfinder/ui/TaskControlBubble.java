package com.tycho.app.primenumberfinder.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

public class TaskControlBubble extends RelativeLayout {

    public TaskControlBubble(Context context) {
        super(context);
        init(context);
    }

    public TaskControlBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TaskControlBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TaskControlBubble(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(final Context context) {
        inflate(context, R.layout.task_control_bar, this);
        setClipToPadding(false);
        setClipChildren(false);
        setPadding(0, Utils.dpToPx(context, 8), 0, Utils.dpToPx(context, 8));
        hideLeft(false);
        hideRight(false);

        final View center = findViewById(R.id.pause_button);
        final TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, out, true);
        final int color = ContextCompat.getColor(context, out.resourceId);
        center.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.blendARGB(color, Color.BLACK, 0.3f)));

        final View left = findViewById(R.id.view_all_button);
        final View right = findViewById(R.id.save_button);
        left.setBackgroundTintList(ColorStateList.valueOf(color));
        right.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public void showLeft(final boolean animate) {
        final View view = findViewById(R.id.view_all_button);
        if (view.getVisibility() == VISIBLE){
            return;
        }
        if (animate) {
            showLeft(false);
            //view.clearAnimation();
            //view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            //view.startAnimation(new Reveal(view, view.getMeasuredWidth()));
        } else {
            view.setVisibility(VISIBLE);
        }
    }

    public void hideLeft(final boolean animate) {
        final View view = findViewById(R.id.view_all_button);
        if (view.getVisibility() != VISIBLE){
            return;
        }
        if (animate) {
            hideLeft(false);
            //view.clearAnimation();
            //view.startAnimation(new Reveal(view, 0));
        } else {
            view.setVisibility(INVISIBLE);
        }
    }

    public void showRight(final boolean animate) {
        final View view = findViewById(R.id.save_button);
        if (view.getVisibility() == VISIBLE){
            return;
        }
        if (animate) {
            showRight(false);
            //view.clearAnimation();
            //view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            //view.startAnimation(new Reveal(view, view.getMeasuredWidth()));
        } else {
            view.setVisibility(VISIBLE);
        }
    }

    private static class Reveal extends Animation{

        private final View view;
        private final int start;
        private final int end;

        public Reveal(final View view, final int end){
            this.view = view;
            this.start = view.getWidth();
            this.end = end;
            setDuration(200);
            setFillAfter(true);
            setFillBefore(true);
            if (end == 0){
                setInterpolator(new AccelerateDecelerateInterpolator());
            }else{
                setInterpolator(new OvershootInterpolator());
            }

            setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (end == 0){
                        view.setVisibility(INVISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            view.getLayoutParams().width = (int) (start + ((end - start) * interpolatedTime));
            view.requestLayout();
        }
    }

    public void hideRight(final boolean animate) {
        final View view = findViewById(R.id.save_button);
        if (view.getVisibility() != VISIBLE){
            return;
        }
        if (animate) {
            hideRight(false);
            //view.clearAnimation();
            //view.startAnimation(new Reveal(view, 0));
        } else {
            view.setVisibility(INVISIBLE);
        }
    }

    public View getLeftView() {
        return findViewById(R.id.view_all_button);
    }

    public View getRightView() {
        return findViewById(R.id.save_button);
    }

    public void setFinished(final boolean finished){
        final ImageButton button = findViewById(R.id.pause_button);
        if (finished){
            button.setImageResource(R.drawable.round_check_24);
            button.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        }else{
            button.setImageResource(R.drawable.ic_pause_white_24dp);
        }

        if (getLeftView().getVisibility() != VISIBLE && getRightView().getVisibility() != VISIBLE){
            setVisibility(GONE);
        }
    }
}
