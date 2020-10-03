package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tycho.app.primenumberfinder.ActionViewListener;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
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

import easytasks.ITask;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_FACTORS;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_PRIMES;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_GCF;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_LCM;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_PRIME_FACTORIZATION;

public abstract class ModuleHostFragment extends Fragment implements AbstractTaskListAdapter.EventListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ModuleHostFragment.class.getSimpleName();

    protected ActionViewListener actionViewListener;

    protected static final int REQUEST_CODE_NEW_TASK = 0;

    private ResultsFragment resultsFragment;

    private Class cls;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ActionViewListener){
            actionViewListener = (ActionViewListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.module_host_fragment, container, false);

        //Give the root view focus to prevent EditTexts from initially getting focus
        rootView.requestFocus();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View advanced = view.findViewById(R.id.advanced_search);
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getActivity(), cls);
                startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
            }
        });
    }

    protected void setConfigurationClass(final Class cls){
        this.cls = cls;
    }

    protected void setConfigurationContainer(final Fragment fragment){
        getChildFragmentManager().beginTransaction().replace(R.id.configuration_container, fragment).commit();
    }

    protected void inflate(final int id){
        getLayoutInflater().inflate(id, requireView().findViewById(R.id.configuration_container));
    }

    protected void setResultsFragment(final ResultsFragment fragment){
        this.resultsFragment = fragment;
        getChildFragmentManager().beginTransaction().replace(R.id.results_container, fragment).commit();
    }

    @Override
    public void onTaskSelected(ITask task) {

    }

    @Override
    public void onPausePressed(ITask task) {

    }

    @Override
    public void onTaskRemoved(ITask task) {

    }

    @Override
    public void onSavePressed(ITask task){

    }

    protected void startTask(final ITask task){
        this.resultsFragment.setTask(task);

        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onTaskStopped(final ITask task) {

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

                    final NumberFormat numberFormat = NumberFormat.getNumberInstance();

                    //Notify when finished
                    if (searchOptions.isNotifyWhenFinished()) {
                        final String content;
                        final int taskType;
                        final int smallIconDrawable;
                        if (task instanceof FindPrimesTask){
                            taskType = TASK_TYPE_FIND_PRIMES;
                            smallIconDrawable = R.drawable.find_primes_icon;
                            content = "Task \"Primes from " + numberFormat.format(((FindPrimesTask) task).getStartValue()) + " to " + numberFormat.format(((FindPrimesTask) task).getEndValue()) + "\" finished.";
                        }else if (task instanceof FindFactorsTask){
                            taskType = TASK_TYPE_FIND_FACTORS;
                            smallIconDrawable = R.drawable.find_factors_icon;
                            content = "Task \"Factors of " + numberFormat.format(((FindFactorsTask) task).getNumber()) + "\" finished.";
                        }else if (task instanceof PrimeFactorizationTask){
                            taskType = TASK_TYPE_PRIME_FACTORIZATION;
                            smallIconDrawable = R.drawable.prime_factorization_icon;
                            content = "Task \"Prime factorization of " + numberFormat.format(((PrimeFactorizationTask) task).getNumber()) + "\" finished.";
                        }else if (task instanceof LeastCommonMultipleTask){
                            taskType = TASK_TYPE_LCM;
                            smallIconDrawable = R.drawable.lcm_icon;
                            content = "Task \"LCM of " + Utils.formatNumberList(((LeastCommonMultipleTask) task).getNumbers(), numberFormat, ",") + "\" finished.";
                        }else if (task instanceof GreatestCommonFactorTask){
                            taskType = TASK_TYPE_GCF;
                            smallIconDrawable = R.drawable.gcf_icon;
                            content = "Task \"GCF of " + Utils.formatNumberList(((GreatestCommonFactorTask) task).getNumbers(), numberFormat, ",") + "\" finished.";
                        } else{
                            return;
                        }
                        NotificationManager.displayNotification(getActivity(), "default", task, taskType, content, smallIconDrawable);
                    }
                }

                task.removeTaskListener(this);
            }
        });
        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();
        Utils.logTaskStarted(getContext(), task);
    }

    protected int getTheme(){
        return PreferenceManager.getInt(PreferenceManager.Preference.THEME);
    }
}
