package com.tycho.app.primenumberfinder.modules.lcm.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.lcm.LCMConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * This {@linkplain Fragment} allows the user to input a number they want to factor. This fragment
 * will display the progress and statistics of the factorization, along with a list of factors that
 * are found.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class LeastCommonMultipleFragment extends ModuleHostFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LeastCommonMultipleFragment.class.getSimpleName();

    final List<ValidEditText> inputs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setResultsFragment(new LeastCommonMultipleResultsFragment());
        setConfigurationClass(LCMConfigurationActivity.class);

        final View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView != null){

            // Inflate configuration layout
            inflater.inflate(R.layout.lcm_configuration_fragment, rootView.findViewById(R.id.configuration_container));

            //Set up input
            inputs.add(rootView.findViewById(R.id.input0).findViewById(R.id.number_input));
            inputs.add(rootView.findViewById(R.id.input1).findViewById(R.id.number_input));
            inputs.add(rootView.findViewById(R.id.input2).findViewById(R.id.number_input));
            for (ValidEditText editText : inputs){
                editText.setValid(Validator.isValidLCMInput((BigInteger) editText.getNumberValue()));
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        editText.setValid(Validator.isValidLCMInput((BigInteger) editText.getNumberValue()));
                        refreshValidity();
                    }
                });
            }

            //Set up start button
            final Button buttonFindFactors = rootView.findViewById(R.id.button_find_factors);
            buttonFindFactors.setOnClickListener(v -> {

                //Check if the number is valid
                if (Validator.isValidLCMInput(getBigNumbers())) {

                    LeastCommonMultipleTask task = new LeastCommonMultipleTask(new LeastCommonMultipleTask.SearchOptions(getNumbers()));
                    startTask(task);

                    hideKeyboard(requireActivity());

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return rootView;
    }

    private void refreshValidity(){
        for (ValidEditText input : inputs){
            if (Validator.isValidLCMInput((BigInteger) input.getNumberValue())){
                input.setValid(true);
            }else{
                input.setValid(getBigNumbers().size() >= 2);
            }
        }
    }

    private List<BigInteger> getBigNumbers(){
        final List<BigInteger> numbers = new ArrayList<>();
        for (ValidEditText editText : inputs){
            if (Validator.isValidLCMInput((BigInteger) editText.getNumberValue())) numbers.add((BigInteger) editText.getNumberValue());
        }
        return numbers;
    }

    private List<Long> getNumbers(){
        final List<Long> numbers = new ArrayList<>();
        for (ValidEditText editText : inputs){
            if (Validator.isValidLCMInput((BigInteger) editText.getNumberValue())) numbers.add(editText.getLongValue());
        }
        return numbers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final LeastCommonMultipleTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final LeastCommonMultipleTask task = (LeastCommonMultipleTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(new LeastCommonMultipleTask(searchOptions));
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
    }
}
