package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.UUID;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 3/2/2017
 */

public class PrimeFactorizationFragment extends ModuleHostFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationFragment.class.getSimpleName();

    private ValidEditText editTextInput;

    private final PrimeFactorizationTask.SearchOptions searchOptions = new PrimeFactorizationTask.SearchOptions(0);

    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                taskListFragment.scrollToBottom();

            } else {
                Toast.makeText(getActivity(), "Invalid number", Toast.LENGTH_SHORT).show();
            }

        });

        return rootView;
    }

    @Override
    protected void loadFragments() {
        super.loadFragments();
        setResultsFragment(PrimeFactorizationResultsFragment.class);
    }

    @Override
    protected void afterLoadFragments() {
        taskListFragment.setAdapter(new AbstractTaskListAdapter(getContext()));
        taskListFragment.whitelist(PrimeFactorizationTask.class);
    }

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextInput.getText().toString());
    }

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

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
    public void onEditPressed(Task task) {
        final Intent intent = new Intent(getActivity(), PrimeFactorizationConfigurationActivity.class);
        intent.putExtra("searchOptions", ((PrimeFactorizationTask) task).getSearchOptions());
        intent.putExtra("taskId", task.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    public void onSavePressed(Task task) {
        ((PrimeFactorizationResultsFragment) resultsFragment).saveTask((PrimeFactorizationTask) task, getActivity());
    }
}
