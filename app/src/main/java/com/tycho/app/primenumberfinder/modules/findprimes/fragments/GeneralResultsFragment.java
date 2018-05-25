package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;

/**
 * Created by tycho on 12/12/2017.
 */

public class GeneralResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GeneralResultsFragment.class.getSimpleName();

    private ResultsFragment content;

    private TextView emptyMessage;

    private ViewGroup container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.general_results_fragment, container, false);
        this.container = (ViewGroup) rootView;
        emptyMessage = rootView.findViewById(R.id.empty_message);
        //updateContent();
        return rootView;
    }

    @Override
    protected void onUiUpdate() {}

    public void setContent(final ResultsFragment fragment){
        this.content = fragment;
        updateContent();
    }

    public ResultsFragment getContent(){
        if (container == null) return null;
        Log.d(TAG, "Container ID: " + container.getId());
        return (ResultsFragment) getChildFragmentManager().findFragmentById(container.getId());
        //return this.content;
    }

    private void updateContent(){
        if (container != null && content == null){
            emptyMessage.setVisibility(View.VISIBLE);
            final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            for (Fragment fragment : getChildFragmentManager().getFragments()){
                fragmentTransaction.remove(fragment);
            }
            fragmentTransaction.commit();
        }
        if (content != null && container != null){
            emptyMessage.setVisibility(View.GONE);
            getChildFragmentManager().beginTransaction().replace(container.getId(), content).commit();
        }
    }
}
