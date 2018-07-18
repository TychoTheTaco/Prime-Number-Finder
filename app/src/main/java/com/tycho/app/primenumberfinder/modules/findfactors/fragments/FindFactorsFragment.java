package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.UUID;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * This {@linkplain Fragment} allows the user to input a number they want to factor. This fragment
 * will display the progress and statistics of the factorization, along with a list of factors that
 * are found.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class FindFactorsFragment extends ModuleHostFragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindFactorsFragment.class.getSimpleName();

    private ValidEditText editTextNumberToFactor;

    private final FindFactorsTask.SearchOptions searchOptions = new FindFactorsTask.SearchOptions(0);

    @Override
    public View createView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_fragment, viewGroup, false);

        //Set up factor input
        editTextNumberToFactor = rootView.findViewById(R.id.editText_input_number);
        editTextNumberToFactor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Check if the number is valid
                editTextNumberToFactor.setValid(Validator.isValidFactorInput(getNumberToFactor()));
            }
        });

        //Set up start button
        final Button buttonFindFactors = rootView.findViewById(R.id.button_find_factors);
        buttonFindFactors.setOnClickListener(v -> {

            //Check if the number is valid
            if (Validator.isValidFactorInput(getNumberToFactor())) {

                //Create a new task
                searchOptions.setNumber(getNumberToFactor().longValue());
                try {
                    startTask(new FindFactorsTask((FindFactorsTask.SearchOptions) searchOptions.clone()));
                }catch (CloneNotSupportedException e){
                    e.printStackTrace();
                }

                hideKeyboard(getActivity());
                taskListFragment.scrollToBottom();

            } else {
                Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    protected void loadFragments() {
        super.loadFragments();
        setResultsFragment(FindFactorsResultsFragment.class);
    }

    @Override
    protected void afterLoadFragments() {
        taskListFragment.setAdapter(new AbstractTaskListAdapter(getContext()));
        taskListFragment.whitelist(FindFactorsTask.class);
    }

    @Override
    public void onEditPressed(Task task) {
        final Intent intent = new Intent(getActivity(), FindFactorsConfigurationActivity.class);
        intent.putExtra("searchOptions", ((FindFactorsTask) task).getSearchOptions());
        intent.putExtra("taskId", task.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    public void onSavePressed(Task task) {
        ((FindFactorsResultsFragment) resultsFragment).saveTask((FindFactorsTask) task, getActivity());
    }

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), FindFactorsConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final FindFactorsTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final FindFactorsTask task = (FindFactorsTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(new FindFactorsTask(searchOptions));
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
    }

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextNumberToFactor.getText().toString());
    }
}
