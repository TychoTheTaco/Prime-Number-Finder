package com.tycho.app.primenumberfinder.modules.lcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.TaskConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.lcm.adapters.NumbersListAdapter;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;

/**
 * Created by tycho on 1/24/2018.
 */

public class LCMConfigurationActivity extends TaskConfigurationActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LCMConfigurationActivity.class.getSimpleName();

    private NumbersListAdapter numbersListAdapter;

    private CheckBox notifyWhenFinishedCheckbox;

    private final LeastCommonMultipleTask.SearchOptions searchOptions = new LeastCommonMultipleTask.SearchOptions();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lcm_configuration_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.yellow_dark), ContextCompat.getColor(this, R.color.yellow));

        numbersListAdapter = new NumbersListAdapter(){
            @Override
            protected boolean isValidNumber(BigInteger number){
                return Validator.isValidLCMInput(number);
            }
        };
        numbersListAdapter.getNumbers().add(BigInteger.ZERO);
        numbersListAdapter.getNumbers().add(BigInteger.ZERO);
        numbersListAdapter.getNumbers().add(BigInteger.ZERO);

        //Set up number input
        final RecyclerView recyclerView = findViewById(R.id.numbers_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(numbersListAdapter);
        recyclerView.setItemAnimator(null);

        notifyWhenFinishedCheckbox = findViewById(R.id.notify_when_finished);
        notifyWhenFinishedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            searchOptions.setNotifyWhenFinished(isChecked);
        });

        //Give the root view focus to prevent EditTexts from initially getting focus
        findViewById(R.id.root).requestFocus();
    }

    @Override
    protected boolean isConfigurationValid(){
        return Validator.isValidLCMInput(numbersListAdapter.getNonZeroNumbers());
    }

    @Override
    protected void buildReturnIntent(Intent intent){
        searchOptions.setNumbers(numbersListAdapter.getValidNumbers());
        searchOptions.setNotifyWhenFinished(notifyWhenFinishedCheckbox.isChecked());
        intent.putExtra("searchOptions", searchOptions);
    }
}
