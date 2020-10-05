package com.tycho.app.primenumberfinder.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

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
    }

    public void showLeft(final boolean animate) {
        if (animate) {
            showLeft(false);
        } else {
            findViewById(R.id.view_all_button).setVisibility(VISIBLE);
        }
    }

    public void hideLeft(final boolean animate) {
        if (animate) {
            hideLeft(false);
        } else {
            findViewById(R.id.view_all_button).setVisibility(GONE);
        }
    }

    public void showRight(final boolean animate) {
        if (animate) {
            showRight(false);
        } else {
            findViewById(R.id.save_button).setVisibility(VISIBLE);
        }
    }

    public void hideRight(final boolean animate) {
        if (animate) {
            hideRight(false);
        } else {
            findViewById(R.id.save_button).setVisibility(GONE);
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
    }
}
