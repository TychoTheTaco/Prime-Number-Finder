package com.tycho.app.primenumberfinder;

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
}
