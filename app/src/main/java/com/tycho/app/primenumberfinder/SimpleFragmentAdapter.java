package com.tycho.app.primenumberfinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleFragmentAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragments = new ArrayList<>();

    public SimpleFragmentAdapter(final FragmentManager fragmentManager){
        super(fragmentManager);
    }

    public SimpleFragmentAdapter(final FragmentManager fragmentManager, Fragment... fragments){
        this(fragmentManager);
        this.fragments.addAll(Arrays.asList(fragments));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public void addFragment(final Fragment fragment){
        fragments.add(fragment);
    }

    public boolean removeFragment(final Fragment fragment){
        return fragments.remove(fragment);
    }
}
