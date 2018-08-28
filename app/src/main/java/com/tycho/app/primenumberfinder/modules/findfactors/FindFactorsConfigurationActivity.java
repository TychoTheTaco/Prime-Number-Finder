package com.tycho.app.primenumberfinder.modules.findfactors;

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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.AbstractActivity;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import easytasks.Task;

/**
 * Created by tycho on 1/24/2018.
 */

public class FindFactorsConfigurationActivity extends AbstractActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindFactorsConfigurationActivity.class.getSimpleName();

    private ValidEditText editTextNumberToFactor;

    //private Spinner threadCountSpinner;

    private CheckBox notifyWhenFinishedCheckbox;
    private CheckBox autoSaveCheckbox;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private FindFactorsTask task;

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
            }
        });

        //Set up thread count
        /*threadCountSpinner = findViewById(R.id.thread_count_spinner);
        final String[] items = new String[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(i + 1);
        }
        threadCountSpinner.setAdapter(new ThreadCountAdapter(this, items));*/

        notifyWhenFinishedCheckbox = findViewById(R.id.notify_when_finished);
        autoSaveCheckbox = findViewById(R.id.auto_save);

        try {
            final FindFactorsTask.SearchOptions searchOptions = getIntent().getExtras().getParcelable("searchOptions");
            applyConfig(searchOptions);
        } catch (NullPointerException e) {
            Log.w(TAG, "SearchOptions not found! Using defaults.");
            applyConfig(null);
        }

        try {
            task = (FindFactorsTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) getIntent().getExtras().get("taskId"));
            if (task.getState() != Task.State.NOT_STARTED) {
                editTextNumberToFactor.setEnabled(false);
                //threadCountSpinner.setEnabled(false);
            }
        } catch (NullPointerException e) {
            Log.w(TAG, "Task not found.");
        }
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
                if (Validator.isValidFactorInput(getNumberToFactor())) {
                    final Intent intent = new Intent();
                    final FindFactorsTask.SearchOptions searchOptions = new FindFactorsTask.SearchOptions(getNumberToFactor().longValue());
                    //searchOptions.setThreadCount(Integer.valueOf((String) threadCountSpinner.getSelectedItem()));
                    searchOptions.setNotifyWhenFinished(notifyWhenFinishedCheckbox.isChecked());
                    searchOptions.setAutoSave(autoSaveCheckbox.isChecked());
                    intent.putExtra("searchOptions", searchOptions);
                    if (task != null) intent.putExtra("taskId", task.getId());
                    setResult(0, intent);
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
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
