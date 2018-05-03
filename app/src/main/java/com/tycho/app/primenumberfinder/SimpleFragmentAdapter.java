package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SimpleFragmentAdapter extends FragmentPagerAdapter {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SimpleFragmentAdapter.class.getSimpleName();

    private final Context context;

    private final List<AdapterItem> items = new ArrayList<>();

    public SimpleFragmentAdapter(final FragmentManager fragmentManager, final Context context){
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return Fragment.instantiate(context, items.get(position).className);
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

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Fragment fragment = (Fragment) super.instantiateItem(container, position);
        items.get(position).fragment = fragment;
        return fragment;
    }

    public void add(final String className, final String title){
        items.add(new AdapterItem(className, title));
    }

    public Fragment getFragment(final int position){
        return items.get(position).fragment;
    }

    public void remove(final int index){
        items.remove(index);
    }

    private class AdapterItem{
        private final String className;
        private final String title;
        private Fragment fragment;

        AdapterItem(String className, String title) {
            this.className = className;
            this.title = title;
        }
    }
}
