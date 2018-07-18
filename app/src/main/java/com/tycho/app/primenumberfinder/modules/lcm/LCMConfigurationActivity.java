package com.tycho.app.primenumberfinder.modules.lcm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.AbstractActivity;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.lcm.adapters.NumbersListAdapter;
import com.tycho.app.primenumberfinder.ui.CustomRadioGroup;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.INFINITY;
import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchMethod.BRUTE_FORCE;
import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchMethod.SIEVE_OF_ERATOSTHENES;

/**
 * Created by tycho on 1/24/2018.
 */

public class LCMConfigurationActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LCMConfigurationActivity.class.getSimpleName();

    private NumbersListAdapter numbersListAdapter;

    private CheckBox notifyWhenFinishedCheckbox;

    private LeastCommonMultipleTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lcm_configuration_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        numbersListAdapter = new NumbersListAdapter(this);
        numbersListAdapter.getNumbers().add(0L);
        numbersListAdapter.getNumbers().add(0L);
        numbersListAdapter.getNumbers().add(0L);
        numbersListAdapter.getNumbers().add(0L);

        //Set up number input
        final RecyclerView recyclerView = findViewById(R.id.numbers_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setAdapter(numbersListAdapter);
        recyclerView.setItemAnimator(null);

        notifyWhenFinishedCheckbox = findViewById(R.id.notify_when_finished);
        notifyWhenFinishedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //searchOptions.setNotifyWhenFinished(isChecked);
        });

        //Get search options from intent
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getParcelable("searchOptions") != null) {
                //searchOptions = getIntent().getExtras().getParcelable("searchOptions");
            }

            try {
                //task = (FindPrimesTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) getIntent().getExtras().get("taskId"));
                //searchOptions = task.getSearchOptions();
            } catch (NullPointerException e) {
                Log.w(TAG, "Task not found.");
            }
        }

        //Apply config
        //applyConfig(searchOptions);

        //Give the root view focus to prevent EditTexts from initially getting focus
        findViewById(R.id.root).requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_configuration_menu, menu);

        if (task != null) {
            menu.findItem(R.id.start).setIcon(R.drawable.ic_save_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.start:
                if (Validator.isValidLCMInput(numbersListAdapter.getValidNumbers())) {
                    final Intent intent = new Intent();
                    final LeastCommonMultipleTask.SearchOptions searchOptions = new LeastCommonMultipleTask.SearchOptions(numbersListAdapter.getValidNumbers());
                    searchOptions.setNotifyWhenFinished(notifyWhenFinishedCheckbox.isChecked());
                    intent.putExtra("searchOptions", searchOptions);
                    if (task != null) intent.putExtra("taskId", task.getId());
                    setResult(0, intent);
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.error_invalid_range), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Give the root view focus to prevent EditTexts from initially getting focus
        findViewById(R.id.root).requestFocus();
    }

    /*private void applyConfig(final FindPrimesTask.SearchOptions searchOptions) {

        //Start and end values
        editTextSearchRangeStart.setEnabled(true);
        editTextSearchRangeStart.setText(NUMBER_FORMAT.format(searchOptions.getStartValue()), true);
        editTextSearchRangeEnd.setEnabled(true);
        editTextSearchRangeEnd.setText(searchOptions.getEndValue() == INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(searchOptions.getEndValue()), true);

        //Thread count
        threadCountSpinner.setSelection(searchOptions.getThreadCount() - 1);

        //Search method
        switch (searchOptions.getSearchMethod()) {
            case BRUTE_FORCE:
                radioGroupSearchMethod.check(R.id.brute_force);
                infinityButton.setEnabled(true);
                infinityButton.setAlpha(1f);
                threadCountSpinner.setEnabled(true);
                break;

            case SIEVE_OF_ERATOSTHENES:
                radioGroupSearchMethod.check(R.id.sieve_of_eratosthenes);
                if (getEndValue().compareTo(getStartValue()) <= 0) {
                    editTextSearchRangeEnd.getText().clear();
                }
                infinityButton.setEnabled(false);
                infinityButton.setAlpha(0.3f);
                threadCountSpinner.setEnabled(false);
                threadCountSpinner.setSelection(0);
                break;
        }

        //Miscellaneous
        notifyWhenFinishedCheckbox.setChecked(searchOptions.isNotifyWhenFinished());
        autoSaveCheckbox.setChecked(searchOptions.isAutoSave());
        if (searchOptions.getEndValue() == INFINITY) {
            notifyWhenFinishedCheckbox.setEnabled(false);
            autoSaveCheckbox.setEnabled(false);
        } else {
            notifyWhenFinishedCheckbox.setEnabled(true);
            autoSaveCheckbox.setEnabled(true);
        }

        //Task state dependent
        if (task != null) {
            if (task.getState() != Task.State.NOT_STARTED) {
                editTextSearchRangeStart.setEnabled(false);

                if (task.getState() != Task.State.STOPPING && task.getState() != Task.State.STOPPED) {
                    editTextSearchRangeEnd.setEnabled(false);
                    infinityButton.setEnabled(false);
                    threadCountSpinner.setEnabled(false);
                } else {
                    editTextSearchRangeEnd.setEnabled(false);
                    infinityButton.setEnabled(false);
                    threadCountSpinner.setEnabled(false);
                }

                radioGroupSearchMethod.setEnabled(false);
            }
        }
    }*/
}
