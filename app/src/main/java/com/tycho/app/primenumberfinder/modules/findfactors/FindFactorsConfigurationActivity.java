package com.tycho.app.primenumberfinder.modules.findfactors;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.TaskConfigurationActivity;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Created by tycho on 1/24/2018.
 */

public class FindFactorsConfigurationActivity extends TaskConfigurationActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindFactorsConfigurationActivity.class.getSimpleName();

    private ValidEditText editTextNumberToFactor;

    private CheckBox notifyWhenFinishedCheckbox;
    private CheckBox autoSaveCheckbox;

    private final FindFactorsTask.SearchOptions searchOptions = new FindFactorsTask.SearchOptions(0);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_factors_configuration_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.orange_dark), ContextCompat.getColor(this, R.color.orange));

        //Set up number input
        editTextNumberToFactor = findViewById(R.id.number_input);
        editTextNumberToFactor.setClearOnTouch(false);
        editTextNumberToFactor.setHint(NUMBER_FORMAT.format(new Random().nextInt(1_000_000)));
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
                searchOptions.setNumber(getNumberToFactor().longValue());
            }
        });

        notifyWhenFinishedCheckbox = findViewById(R.id.notify_when_finished);
        autoSaveCheckbox = findViewById(R.id.auto_save);

        applyConfig(searchOptions);
    }

    @Override
    protected boolean isConfigurationValid(){
        return Validator.isValidFactorInput(getNumberToFactor());
    }

    @Override
    protected void buildReturnIntent(Intent intent){
        searchOptions.setNotifyWhenFinished(notifyWhenFinishedCheckbox.isChecked());
        searchOptions.setAutoSave(autoSaveCheckbox.isChecked());
        intent.putExtra("searchOptions", searchOptions);
    }

    private BigInteger getNumberToFactor() {
        return Utils.textToNumber(editTextNumberToFactor.getText().toString());
    }

    private void applyConfig(final FindFactorsTask.SearchOptions searchOptions) {

        if (searchOptions != null) {
            editTextNumberToFactor.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(searchOptions.getNumber()));

            //threadCountSpinner.setSelection(searchOptions.getThreadCount() - 1);

            notifyWhenFinishedCheckbox.setChecked(searchOptions.isNotifyWhenFinished());
            autoSaveCheckbox.setChecked(searchOptions.isAutoSave());
        } else {
            //editTextNumberToFactor.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(1));

            //threadCountSpinner.setSelection(0);

            notifyWhenFinishedCheckbox.setChecked(false);
            autoSaveCheckbox.setChecked(false);
        }

    }
}
