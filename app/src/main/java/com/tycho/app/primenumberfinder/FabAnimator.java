package com.tycho.app.primenumberfinder;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesFragment;

public class FabAnimator implements ViewPager.OnPageChangeListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FabAnimator.class.getSimpleName();

    private final FloatingActionButton fab;

    public FabAnimator(final FloatingActionButton floatingActionButton){
        this.fab = floatingActionButton;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        switch (position) {
            case 0:
                fab.setVisibility(View.VISIBLE);
                fab.setTranslationX(-positionOffsetPixels);
                final float circumference = (float) (2f * Math.PI * ((float) fab.getWidth() / 2));
                if (circumference > 0) {
                    final float maxWidth = positionOffset == 0 ? 0 : (1 / positionOffset) * positionOffsetPixels;
                    fab.setRotation(-360 * positionOffset * (maxWidth / circumference));
                }
                break;

            default:
                fab.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
