package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.modules.TaskListFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FindFactorsTaskListAdapter;

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
      return Fragment.instantiate(context, items.get(position).mClass.getName());
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
        private Fragment fragment;

        AdapterItem(String title, Class<? extends Fragment> cls) {
            this.title = title;
            mClass = cls;
        }
    }
}
