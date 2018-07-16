package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FabAnimator;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SimpleFragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.fragments.FindFactorsResultsFragment;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

public abstract class ModuleHostFragment extends Fragment implements FloatingActionButtonListener, AbstractTaskListAdapter.EventListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ModuleHostFragment.class.getSimpleName();

    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    protected FloatingActionButtonHost floatingActionButtonHost;

    protected ActionViewListener actionViewListener;

    protected ViewPager viewPager;

    private FabAnimator fabAnimator;

    private SimpleFragmentAdapter simpleFragmentAdapter;

    protected TaskListFragment taskListFragment;
    protected ResultsFragment resultsFragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FloatingActionButtonHost) {
            floatingActionButtonHost = (FloatingActionButtonHost) context;
        }
        if (context instanceof ActionViewListener){
            actionViewListener = (ActionViewListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = createView(inflater, container, savedInstanceState);

        //Apply action button color
        if (floatingActionButtonHost != null) {
            floatingActionButtonHost.getFab(0).setBackgroundTintList(new ColorStateList(
                    new int[][]{
                            new int[]{}
                    },
                    new int[]{
                            Utils.getAccentColor(rootView.getContext())
                    }));
        }

        //Set up fragment adapter and load fragments
        simpleFragmentAdapter = new SimpleFragmentAdapter(getChildFragmentManager(), getContext());
        viewPager = rootView.findViewById(R.id.view_pager);
        loadFragments();
        afterLoadFragments();
        taskListFragment.addEventListener(this);
        taskListFragment.addActionViewListener(actionViewListener);

        //Set up view pager
        viewPager.setAdapter(simpleFragmentAdapter);
        fabAnimator = new FabAnimator(floatingActionButtonHost.getFab(0));
        viewPager.addOnPageChangeListener(fabAnimator);
        final TabLayout tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        //Give the root view focus to prevent EditTexts from initially getting focus
        rootView.requestFocus();

        //Scroll to Results fragment if started from a notification
        if (getActivity().getIntent().getSerializableExtra("taskId") != null){
            viewPager.setCurrentItem(1);
        }

        return rootView;
    }

    protected abstract View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void onClick(View view) {

    }

    @Override
    public void initFab(View view) {
        if (fabAnimator != null){
            fabAnimator.onPageScrolled(viewPager.getCurrentItem(), 0, 0);

            if (getView() != null){
                floatingActionButtonHost.getFab(0).setBackgroundTintList(new ColorStateList(
                        new int[][]{
                                new int[]{}
                        },
                        new int[]{
                                Utils.getAccentColor(getView().getContext())
                        })
                );
            }
        }
    }

    @Override
    public void onTaskSelected(Task task) {
        resultsFragment.setTask(task);
    }

    @Override
    public void onPausePressed(Task task) {

    }

    @Override
    public void onTaskRemoved(Task task) {
        if (resultsFragment.getTask() == task) {
            resultsFragment.setTask(null);
        }

        taskListFragment.update();
    }

    @Override
    public void onEditPressed(Task task) {

    }

    @Override
    public void onSavePressed(Task task) {

    }

    protected Fragment addFragment(final String title, final Class<? extends Fragment> cls){
        simpleFragmentAdapter.add(title, cls);

        //Instantiate fragments now to save a reference to them
        simpleFragmentAdapter.startUpdate(viewPager);
        final Fragment fragment = (Fragment) simpleFragmentAdapter.instantiateItem(viewPager, simpleFragmentAdapter.getCount() - 1);
        simpleFragmentAdapter.finishUpdate(viewPager);

        return fragment;
    }

    protected abstract void loadFragments();

    protected void afterLoadFragments(){}
}
