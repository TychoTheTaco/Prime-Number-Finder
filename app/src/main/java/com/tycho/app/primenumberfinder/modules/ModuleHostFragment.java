package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.FabAnimator;
import com.tycho.app.primenumberfinder.FloatingActionButtonHost;
import com.tycho.app.primenumberfinder.FloatingActionButtonListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.SimpleFragmentAdapter;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;
import com.tycho.app.primenumberfinder.utils.NotificationManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_FACTORS;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_PRIMES;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_GCF;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_LCM;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_PRIME_FACTORIZATION;

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

    protected static final int REQUEST_CODE_NEW_TASK = 0;

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

    protected <T extends Fragment> T addFragment(final String title, final Class<T> cls){
        simpleFragmentAdapter.add(title, cls);

        //Instantiate fragments now to save a reference to them
        simpleFragmentAdapter.startUpdate(viewPager);
        final Fragment fragment = (Fragment) simpleFragmentAdapter.instantiateItem(viewPager, simpleFragmentAdapter.getCount() - 1);
        simpleFragmentAdapter.finishUpdate(viewPager);

        return (T) fragment;
    }

    protected void setTaskListFragment(final Class<? extends TaskListFragment> cls){
        this.taskListFragment = addFragment("Tasks", cls);
    }

    protected void setResultsFragment(final Class<? extends ResultsFragment> cls){
        this.resultsFragment = addFragment("Results", cls);
    }

    protected void loadFragments(){
        setTaskListFragment(TaskListFragment.class);
    };

    protected void afterLoadFragments(){}

    protected void startTask(final Task task){
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped() {

                final GeneralSearchOptions searchOptions;
                if (task instanceof SearchOptions){
                    searchOptions = ((SearchOptions) task).getSearchOptions();
                }else{
                    searchOptions = null;
                }

                if (searchOptions != null){
                    //Auto-save
                    if (task instanceof Savable && searchOptions.isAutoSave()){
                        new Thread(() -> {
                            final boolean success = ((Savable) task).save();
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show());
                        }).start();
                    }

                    //Notify when finished
                    if (searchOptions.isNotifyWhenFinished()) {
                        final String content;
                        final int taskType;
                        final int smallIconDrawable;
                        if (task instanceof FindPrimesTask){
                            taskType = TASK_TYPE_FIND_PRIMES;
                            smallIconDrawable = R.drawable.find_primes_icon;
                            content = "Task \"Primes from " + NUMBER_FORMAT.format(((FindPrimesTask) task).getStartValue()) + " to " + NUMBER_FORMAT.format(((FindPrimesTask) task).getEndValue()) + "\" finished.";
                        }else if (task instanceof FindFactorsTask){
                            taskType = TASK_TYPE_FIND_FACTORS;
                            smallIconDrawable = R.drawable.find_factors_icon;
                            content = "Task \"Factors of " + NUMBER_FORMAT.format(((FindFactorsTask) task).getNumber()) + "\" finished.";
                        }else if (task instanceof PrimeFactorizationTask){
                            taskType = TASK_TYPE_PRIME_FACTORIZATION;
                            smallIconDrawable = R.drawable.prime_factorization_icon;
                            content = "Task \"Prime factorization of " + NUMBER_FORMAT.format(((PrimeFactorizationTask) task).getNumber()) + "\" finished.";
                        }else if (task instanceof LeastCommonMultipleTask){
                            taskType = TASK_TYPE_LCM;
                            smallIconDrawable = R.drawable.lcm_icon;
                            content = "Task \"LCM of " + Utils.formatNumberList(((LeastCommonMultipleTask) task).getNumbers(), NUMBER_FORMAT, ",") + "\" finished.";
                        }else if (task instanceof GreatestCommonFactorTask){
                            taskType = TASK_TYPE_GCF;
                            smallIconDrawable = R.drawable.gcf_icon;
                            content = "Task \"GCF of " + Utils.formatNumberList(((GreatestCommonFactorTask) task).getNumbers(), NUMBER_FORMAT, ",") + "\" finished.";
                        } else{
                            return;
                        }
                        NotificationManager.displayNotification(getActivity(), "default", task, taskType, content, smallIconDrawable);
                    }
                }

                task.removeTaskListener(this);
            }
        });
        taskListFragment.addTask(task);
        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();
        Utils.logTaskStarted(getContext(), task);

        taskListFragment.setSelected(task);
    }

    protected int getTheme(){
        return PreferenceManager.getInt(PreferenceManager.Preference.THEME);
    }
}
