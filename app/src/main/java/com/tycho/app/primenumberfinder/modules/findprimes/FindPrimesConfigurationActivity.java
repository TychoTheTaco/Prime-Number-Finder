package com.tycho.app.primenumberfinder.modules.findprimes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.AbstractActivity;
import com.tycho.app.primenumberfinder.ui.CustomRadioGroup;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.FPT.SearchOptions.SearchMethod.BRUTE_FORCE;
import static com.tycho.app.primenumberfinder.FPT.SearchOptions.SearchMethod.SIEVE_OF_ERATOSTHENES;
import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.INFINITY;

/**
 * Created by tycho on 1/24/2018.
 */

public class FindPrimesConfigurationActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesConfigurationActivity.class.getSimpleName();

    private ValidEditText editTextSearchRangeStart;
    private ValidEditText editTextSearchRangeEnd;

    private ImageButton infinityButton;

    private CustomRadioGroup radioGroupSearchMethod;

    /**
     * {@linkplain NumberFormat} instance used to format numbers with commas.
     */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private Spinner threadCountSpinner;

    private CheckBox notifyWhenFinishedCheckbox;
    private CheckBox autoSaveCheckbox;

    private FindPrimesTask task;

    /**
     * The search options currently representing the user's selection
     */
    private FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, 0, BRUTE_FORCE, 1, false, false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_primes_configuration_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Utils.applyTheme(this, ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));

        //Set up range start input
        editTextSearchRangeStart = findViewById(R.id.search_range_start);
        editTextSearchRangeStart.setHint(NUMBER_FORMAT.format(0));
        editTextSearchRangeStart.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchOptions.setStartValue(getStartValue().longValue());

                //Check if the number is valid
                editTextSearchRangeStart.setValid((Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod()) && editTextSearchRangeStart.length() > 0) || (editTextSearchRangeStart.length() != 0 && editTextSearchRangeEnd.length() == 0));
                editTextSearchRangeEnd.setValid(editTextSearchRangeEnd.length() > 0);
            }
        });
        editTextSearchRangeStart.setClearOnTouch(false);
        editTextSearchRangeStart.setAllowZeroInput(true);

        //Set up range end input
        editTextSearchRangeEnd = findViewById(R.id.search_range_end);
        editTextSearchRangeEnd.setHint(NUMBER_FORMAT.format(new Random().nextInt(1_000_000)));
        editTextSearchRangeEnd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Check if infinity
                if (!editable.toString().equals(getString(R.string.infinity_text))) {

                    //Format text
                    final String formatted = NUMBER_FORMAT.format(getEndValue());
                    if (editable.length() > 0 && !editable.toString().equals(formatted)) {
                        editTextSearchRangeEnd.setText(formatted, formatted.length() > 1);
                        applyConfig(searchOptions);
                    } else if (editable.toString().equals(NUMBER_FORMAT.format(0))) {
                        editTextSearchRangeEnd.getText().clear();
                    }
                }

                searchOptions.setEndValue(getEndValue().longValue());

                //Check if the number is valid
                editTextSearchRangeStart.setValid(editTextSearchRangeStart.length() > 0);
                editTextSearchRangeEnd.setValid(Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod()) && editTextSearchRangeEnd.length() != 0);
            }
        });
        editTextSearchRangeEnd.setClearOnTouch(false);
        editTextSearchRangeEnd.overrideDefaultTextWatcher();
        editTextSearchRangeEnd.requestFocus();

        //Set up infinity button
        infinityButton = findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(v -> {
            editTextSearchRangeEnd.setText(getString(R.string.infinity_text), false);
            applyConfig(searchOptions);
        });

        //Set up search method
        radioGroupSearchMethod = findViewById(R.id.radio_group_search_method);
        radioGroupSearchMethod.addOnCheckChangedListener((radioButton, isChecked) -> {
            switch (radioButton.getId()) {

                case R.id.brute_force:
                    searchOptions.setSearchMethod(BRUTE_FORCE);
                    break;

                case R.id.sieve_of_eratosthenes:
                    searchOptions.setSearchMethod(SIEVE_OF_ERATOSTHENES);
                    break;

            }
            applyConfig(searchOptions);
        });

        //Set up thread count
        threadCountSpinner = findViewById(R.id.thread_count_spinner);
        final String[] items = new String[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(i + 1);
        }
        threadCountSpinner.setAdapter(new ThreadCountAdapter(this, items));
        threadCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchOptions.setThreadCount(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        notifyWhenFinishedCheckbox = findViewById(R.id.notify_when_finished);
        notifyWhenFinishedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> searchOptions.setNotifyWhenFinished(isChecked));
        autoSaveCheckbox = findViewById(R.id.auto_save);
        autoSaveCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> searchOptions.setAutoSave(isChecked));

        //Get search options from intent
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getParcelable("searchOptions") != null) {
                searchOptions = getIntent().getExtras().getParcelable("searchOptions");
            }

            try {
                task = (FindPrimesTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) getIntent().getExtras().get("taskId"));
                searchOptions = task.getSearchOptions();
            } catch (NullPointerException e) {
                Log.w(TAG, "Task not found.");
            }
        }

        //Apply config
        applyConfig(searchOptions);
    }

    class ThreadCountAdapter extends ArrayAdapter<String> {

        private final LayoutInflater layoutInflater;

        private final String[] items;

        public ThreadCountAdapter(final Context context, final String[] items) {
            super(context, R.layout.thread_count_list_item, R.id.text, items);
            layoutInflater = getLayoutInflater();
            this.items = items;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.thread_count_list_item, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.text)).setText(items[position]);
            return convertView;
        }
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
                if (Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod())) {
                    final Intent intent = new Intent();
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

    private BigInteger getStartValue() {
        return Utils.textToNumber(editTextSearchRangeStart.getText().toString());
    }

    private BigInteger getEndValue() {
        final String input = editTextSearchRangeEnd.getText().toString().trim();

        //Check for infinity
        if (input.equals(getString(R.string.infinity_text))) {
            return BigInteger.valueOf(INFINITY);
        }

        return Utils.textToNumber(input);
    }

    private void applyConfig(final FindPrimesTask.SearchOptions searchOptions) {

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

        //Task subtitle dependent
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
    }
}
