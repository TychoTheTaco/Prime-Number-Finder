package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Custom implementation of <code>EditText</code> used as a workaround for a bug.
 *
 * The bug causes <code>android:windowSoftInputMode="adjustPan"</code> to only work the first time
 * the view is focused. Any subsequent times the keyboard will just cover the view. Removing center
 * gravity also works to prevent the bug. Supposedly fixed in Android N.
 *
 * @author Tycho Bellers
 *         Date Created: 3/20/2017
 */

public class CustomEditText extends AppCompatEditText{

    public CustomEditText(Context context){
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
