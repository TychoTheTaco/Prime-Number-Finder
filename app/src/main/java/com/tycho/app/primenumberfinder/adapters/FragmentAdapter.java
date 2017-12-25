package com.tycho.app.primenumberfinder.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tycho on 10/4/2017.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FragmentAdapter";

    /**
     * List of items in this adapter.
     */
    private final List<AdapterItem> items = new ArrayList<>();

    public FragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return items.get(position).fragment;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return items.get(position).tabTitle;
    }

    public void add(final String title, Fragment fragment){
        items.add(new AdapterItem(title, fragment));
    }

    public void remove(final int index){
        items.remove(index);
    }

    public void set(final int index, final String title, final Fragment fragment){
        items.set(index, new AdapterItem(title, fragment));
    }

    private final class AdapterItem{

        private final String tabTitle;
        private final Fragment fragment;

        private AdapterItem(final String tabTitle, final Fragment fragment){
            this.tabTitle = tabTitle;
            this.fragment = fragment;
        }
    }
}
