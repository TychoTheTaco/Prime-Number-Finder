package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tycho on 1/23/2018.
 */

public class CustomRadioGroup extends LinearLayout {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "CustomRadioGroup";

    private int selectedItemId = -1;

    private final List<RadioButton> radioButtons = new ArrayList<>();

    private final List<OnCheckChangedListener> checkChangedListeners = new CopyOnWriteArrayList<>();

    public CustomRadioGroup(Context context) {
        super(context);
        init();
    }

    public CustomRadioGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomRadioGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomRadioGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findButtons(this);
    }

    private void findButtons(final ViewGroup viewGroup){
        for (int i = 0; i < viewGroup.getChildCount(); i++){
            final View child = viewGroup.getChildAt(i);
            if (child instanceof RadioButton){
                ((RadioButton) child).setOnCheckedChangeListener(onCheckedChangeListener);
                radioButtons.add((RadioButton) child);
            }else if (child instanceof ViewGroup){
                findButtons((ViewGroup) child);
            }
        }
    }

    private void init(){
        setOrientation(VERTICAL);
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            for (RadioButton radioButton : radioButtons){
                if (radioButton != buttonView){
                    radioButton.setOnCheckedChangeListener(null);
                    radioButton.setChecked(false);
                    radioButton.setOnCheckedChangeListener(this);
                }else if (buttonView.isChecked()){
                    selectedItemId = radioButtons.indexOf(radioButton);
                }
            }
            sendOnCheckChanged((RadioButton) buttonView, isChecked);
        }
    };

    public int getSelectedItemId(){
        return selectedItemId;
    }

    public int getItemCount(){
        return radioButtons.size();
    }

    public interface OnCheckChangedListener{
        void onChecked(final RadioButton radioButton, boolean isChecked);
    }

    public void addOnCheckChangedListener(final OnCheckChangedListener onCheckChangedListener){
        checkChangedListeners.add(onCheckChangedListener);
    }

    private void sendOnCheckChanged(final RadioButton radioButton, final boolean isChecked){
        for (OnCheckChangedListener onCheckChangedListener : checkChangedListeners){
            onCheckChangedListener.onChecked(radioButton, isChecked);
        }
    }
}
