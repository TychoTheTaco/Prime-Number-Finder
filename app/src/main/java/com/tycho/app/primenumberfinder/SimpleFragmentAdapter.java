package com.tycho.app.primenumberfinder;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SimpleFragmentAdapter extends FragmentPagerAdapter {

    private final List<AdapterItem> items = new ArrayList<>();

    public SimpleFragmentAdapter(final FragmentManager fragmentManager){
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return items.get(position).fragment;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return items.get(position).title;
    }

    public void add(final Fragment fragment, final String title){
        items.add(new AdapterItem(fragment, title));
    }

    public void remove(final Fragment fragment){
        for (AdapterItem adapterItem : items){
            if (adapterItem.fragment == fragment){
                items.remove(adapterItem);
            }
        }
    }

    public void remove(final int index){
        items.remove(index);
    }

    private class AdapterItem{
        private final Fragment fragment;
        private final String title;

        AdapterItem(Fragment fragment, String title) {
            this.fragment = fragment;
            this.title = title;
        }
    }
}
