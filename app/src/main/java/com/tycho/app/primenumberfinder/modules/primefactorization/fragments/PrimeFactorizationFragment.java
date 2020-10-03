package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.SearchOptions;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.GeneralSearchOptions;
import com.tycho.app.primenumberfinder.utils.NotificationManager;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import easytasks.ITask;
import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_FACTORS;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_FIND_PRIMES;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_GCF;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_LCM;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_PRIME_FACTORIZATION;
import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 3/2/2017
 */

public class PrimeFactorizationFragment extends Fragment implements AbstractTaskListAdapter.EventListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationFragment.class.getSimpleName();

    private ValidEditText editTextInput;

    private final PrimeFactorizationTask.SearchOptions searchOptions = new PrimeFactorizationTask.SearchOptions(0);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_fragment, container, false);

        //Set up factor input
        editTextInput = rootView.findViewById(R.id.editText_input_number);
        editTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Check if the number is valid
                editTextInput.setValid(Validator.isValidFactorInput(getNumberToFactor()));
            }
        });

        //Set up start button
        rootView.findViewById(R.id.button_generate_factor_tree).setOnClickListener(v -> {

            //Check if the number is valid
            if (Validator.isValidFactorInput(getNumberToFactor())) {

                //Create a new task
                searchOptions.setNumber(getNumberToFactor().longValue());
                try {
                    startTask(new PrimeFactorizationTask((PrimeFactorizationTask.SearchOptions) searchOptions.clone()));
                } catch (CloneNotSupportedException e) {
                }

                hideKeyboard(getActivity());

            } else {
                Toast.makeText(getActivity(), "Invalid number", Toast.LENGTH_SHORT).show();
            }

        });

        fragment = new PrimeFactorizationResultsFragment();
        getChildFragmentManager().beginTransaction().add(R.id.container, fragment).commit();

        advanced = rootView.findViewById(R.id.advanced_search);
        advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
            }
        });

        return rootView;
    }

    private View advanced;

    private ResultsFragment fragment;

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextInput.getText().toString());
    }

    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    protected static final int REQUEST_CODE_NEW_TASK = 0;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final PrimeFactorizationTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final PrimeFactorizationTask task = (PrimeFactorizationTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(new PrimeFactorizationTask(searchOptions));
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
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
    public void onSavePressed(ITask task) {

    }

    protected void startTask(final ITask task){

        if (task instanceof PrimeFactorizationTask){
            fragment.setTask(task);
        }

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
        PrimeNumberFinder.getTaskManager().registerTask(task);

        //Start the task
        task.startOnNewThread();
        Utils.logTaskStarted(getContext(), task);
    }
}
