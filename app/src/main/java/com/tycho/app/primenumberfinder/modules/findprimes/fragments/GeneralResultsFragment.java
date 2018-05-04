package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;

/**
 * Created by tycho on 12/12/2017.
 */

public class GeneralResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "GeneralResultsFragment";

    private ResultsFragment content;

    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.general_results_fragment, container, false);
        this.container = (LinearLayout) rootView;
        updateContent();
        return rootView;
    }

    @Override
    protected void onUiUpdate() {

    }

    public void setContent(final ResultsFragment fragment){
        this.content = fragment;
        updateContent();
    }

    public ResultsFragment getContent(){
        return this.content;
    }

    private void updateContent(){
        if (content != null && container != null){
            Log.w(TAG, "Set content: " + content + (content.getTask() != null ? " (" + ((FindPrimesResultsFragment) content).getTask().getEndValue() + ")" : "") + "\nFragments: " + getChildFragmentManager().getFragments());
            getChildFragmentManager().beginTransaction().replace(container.getId(), content).commit();
        }
    }
}
