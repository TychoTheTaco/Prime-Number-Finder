package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.UUID;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setResultsFragment(new PrimeFactorizationResultsFragment());
        setConfigurationClass(PrimeFactorizationConfigurationActivity.class);

        final View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView != null){

            // Inflate configuration layout
            inflater.inflate(R.layout.prime_factorization_configuration_fragment, rootView.findViewById(R.id.configuration_container));

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
        }
        return rootView;
    }

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextInput.getText().toString());
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

}
