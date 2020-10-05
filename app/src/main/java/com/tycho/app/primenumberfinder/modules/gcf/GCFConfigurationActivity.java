package com.tycho.app.primenumberfinder.modules.gcf;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.TaskConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.lcm.adapters.NumbersListAdapter;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;

/**
 * Created by tycho on 1/24/2018.
 */

public class GCFConfigurationActivity extends TaskConfigurationActivity{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GCFConfigurationActivity.class.getSimpleName();

    private NumbersListAdapter numbersListAdapter;

    private final GreatestCommonFactorTask.SearchOptions searchOptions = new GreatestCommonFactorTask.SearchOptions();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcf_configuration_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.blue_dark), ContextCompat.getColor(this, R.color.lt_blue));

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
        intent.putExtra("searchOptions", searchOptions);
    }
}
