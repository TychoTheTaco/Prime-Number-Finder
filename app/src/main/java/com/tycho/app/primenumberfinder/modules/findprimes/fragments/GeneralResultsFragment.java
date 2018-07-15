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

        //Empty message
        emptyMessage = rootView.findViewById(R.id.empty_message);
        emptyMessage.setVisibility(this.content == null ? View.VISIBLE : View.GONE);

        if (content != null){
            getChildFragmentManager().beginTransaction().replace(this.container.getId(), content).commit();
        }

        return rootView;
    }

    @Override
    protected void onUiUpdate() {}

    public void setContent(final ResultsFragment fragment){
        this.content = fragment;

        if (getView() != null){
            emptyMessage.setVisibility(this.content == null ? View.VISIBLE : View.GONE);
            final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            if (content == null){
                for (Fragment f : getChildFragmentManager().getFragments()){
                    fragmentTransaction.remove(f);
                }
            }else{
                fragmentTransaction.replace(container.getId(), content);
            }
            fragmentTransaction.commit();
        }
    }

    public ResultsFragment getContent(){
        if (container == null) return null;
        return (ResultsFragment) getChildFragmentManager().findFragmentById(container.getId());
    }
}
