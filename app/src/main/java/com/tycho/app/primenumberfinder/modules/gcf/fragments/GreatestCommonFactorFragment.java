package com.tycho.app.primenumberfinder.modules.gcf.fragments;

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

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.gcf.GCFConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

public class GreatestCommonFactorFragment extends ModuleHostFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GreatestCommonFactorFragment.class.getSimpleName();

    final List<ValidEditText> inputs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setResultsFragment(new GreatestCommonFactorResultsFragment());
        setConfigurationClass(GCFConfigurationActivity.class);

        final View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView != null){

            // Inflate configuration layout
            inflater.inflate(R.layout.gcf_configuration_fragment, rootView.findViewById(R.id.configuration_container));

            //Set up input
            inputs.add(rootView.findViewById(R.id.input0).findViewById(R.id.input));
            inputs.add(rootView.findViewById(R.id.input1).findViewById(R.id.input));
            inputs.add(rootView.findViewById(R.id.input2).findViewById(R.id.input));
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
                    }
                });
            }

            //Set up start button
            final Button startButton = rootView.findViewById(R.id.button_find_factors);
            startButton.setOnClickListener(v -> {

                //Check if the number is valid
                if (Validator.isValidLCMInput(getBigNumbers())) {

                    GreatestCommonFactorTask task = new GreatestCommonFactorTask(new GreatestCommonFactorTask.SearchOptions(getNumbers()));
                    startTask(task);

                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return rootView;
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
            if (editText.isValid()) numbers.add(editText.getLongValue());
        }
        return numbers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final GreatestCommonFactorTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    final GreatestCommonFactorTask task = (GreatestCommonFactorTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    if (task == null) {
                        startTask(new GreatestCommonFactorTask(searchOptions));
                    } else {
                        task.setSearchOptions(searchOptions);
                    }
                }
                break;
        }
    }
}
