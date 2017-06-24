package com.tycho.app.primenumberfinder;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

/**
 * @author Tycho Bellers
 *         Date Created: 5/19/2017
 */
public class StatisticsAdapter extends FragmentStatePagerAdapter{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "StatisticsAdapter";

    private final Fragment[] fragments = new Fragment[]{new StatisticsFragment0(), new StatisticsFragment1(), new StatisticsFragment2()};

    public StatisticsAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position){
        return fragments[position];
    }

    @Override
    public int getCount(){
        return fragments.length;
    }
}
