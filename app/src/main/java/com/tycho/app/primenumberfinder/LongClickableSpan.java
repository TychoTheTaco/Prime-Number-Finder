package com.tycho.app.primenumberfinder;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * @author Tycho Bellers
 * Date Created: 7/25/2018
 */
public abstract class LongClickableSpan extends ClickableSpan{
    public abstract void onLongClick(View view);

    @Override
    public void onClick(View view){
        //Do nothing
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        /*
        This method is intentionally left empty to prevent the super method from changing the text
        color of the span.
         */
    }
}
