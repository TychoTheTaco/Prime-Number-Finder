package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 2/18/2017
 */

public class NavigationDrawerAdapter extends ArrayAdapter{

    private final Context context;

    private static final int layoutResourceId = R.layout.navigation_drawer_item;

    public NavigationDrawerAdapter(final Context context, final int layoutResourceId, final List<NavigationDrawerItem> navigationDrawerItems){
        super(context, layoutResourceId);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        final View rootView = layoutInflater.inflate(layoutResourceId, parent);

        return rootView;
    }

    private class NavigationDrawerItem{

        private String title;

        public NavigationDrawerItem(String title){

        }

    }

}
