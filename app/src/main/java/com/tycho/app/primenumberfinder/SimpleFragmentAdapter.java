package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
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
      return Fragment.instantiate(context, items.get(position).mClass.getName(), items.get(position).args);
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

    public void add(final String title, final Class<? extends Fragment> cls){
        items.add(new AdapterItem(title, cls));
    }

    public Fragment getFragment(final int position){
        return items.get(position).fragment;
    }

    public void remove(final int index){
        items.remove(index);
    }

    private class AdapterItem{
        private final String title;
        private final Class<? extends Fragment> mClass;
        private final Bundle args;
        private Fragment fragment;

        AdapterItem(String title, Class<? extends Fragment> cls, final Bundle args) {
            this.title = title;
            mClass = cls;
            this.args = args;
        }

        AdapterItem(String title, Class<? extends Fragment> cls) {
            this(title, cls, null);
        }
    }
}
