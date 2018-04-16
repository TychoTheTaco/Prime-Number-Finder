package com.tycho.app.primenumberfinder.modules.findprimes;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.CustomRadioGroup;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ValidEditText;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchMethod.BRUTE_FORCE;
import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchMethod.SIEVE_OF_ERATOSTHENES;
import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * Created by tycho on 1/24/2018.
 */

public class FindPrimesConfigurationActivity extends AppCompatActivity {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesConfigAct";

    private ValidEditText editTextSearchRangeStart;
    private ValidEditText editTextSearchRangeEnd;

    private CustomRadioGroup radioGroupSearchMethod;

    private FindPrimesTask.SearchMethod searchMethod = BRUTE_FORCE;

    private Spinner threadCountSpinner;

    private CheckBox notifyWhenFinishedCheckbox;
    private CheckBox autoSaveCheckbox;

    private FindPrimesTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_primes_configuration_activity);

        //Set the actionbar to a custom toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyThemeColor(ContextCompat.getColor(this, R.color.purple_dark), ContextCompat.getColor(this, R.color.purple));

        //Set up range start input
        editTextSearchRangeStart = findViewById(R.id.search_range_start);
        editTextSearchRangeStart.addTextChangedListener(new TextWatcher() {

            private boolean isDirty = true;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (isDirty) {

                    //Format text
                    if (editable.length() > 0) {
                        final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getStartValue());
                        if (!editable.toString().equals(formattedText)) {
                            isDirty = false;
                            editTextSearchRangeStart.setText(formattedText);
                        }
                        editTextSearchRangeStart.setSelection(formattedText.length());
                    }

                    //Check if the number is valid
                    if (isRangeValid()){
                        editTextSearchRangeStart.setValid(true);
                        editTextSearchRangeEnd.setValid(true);
                    }else if (editTextSearchRangeStart.hasFocus()){
                        editTextSearchRangeStart.setValid(editTextSearchRangeEnd.getText().length() == 0);
                    }

                }
                isDirty = true;
            }
        });
        editTextSearchRangeStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Clear text on touch
                editTextSearchRangeStart.getText().clear();
                return false;
            }
        });

        //Set up range end input
        editTextSearchRangeEnd = findViewById(R.id.search_range_end);
        editTextSearchRangeEnd.addTextChangedListener(new TextWatcher() {

            private boolean isDirty = true;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (isDirty) {

                    //Format text
                    if (editable.length() > 0) {
                        if (getEndValue().compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) == 0) {
                            if (!editable.toString().equals(getString(R.string.infinity_text))) {
                                editTextSearchRangeEnd.setText(getString(R.string.infinity_text));
                            }
                            editTextSearchRangeEnd.setSelection(editTextSearchRangeEnd.getText().length());
                        } else {
                            final String formattedText = NumberFormat.getNumberInstance(Locale.getDefault()).format(getEndValue());
                            if (!editable.toString().equals(formattedText)) {
                                isDirty = false;
                                editTextSearchRangeEnd.setText(formattedText);
                            }
                            editTextSearchRangeEnd.setSelection(formattedText.length());
                        }
                    }

                    notifyWhenFinishedCheckbox.setEnabled(getEndValue().compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) != 0);
                    autoSaveCheckbox.setEnabled(getEndValue().compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) != 0);

                    //Check if the number is valid
                    if (isRangeValid()){
                        editTextSearchRangeStart.setValid(true);
                        editTextSearchRangeEnd.setValid(true);
                    }else if (editTextSearchRangeEnd.hasFocus()){
                        editTextSearchRangeEnd.setValid(editTextSearchRangeStart.getText().length() == 0);
                    }

                }
                isDirty = true;
            }
        });
        editTextSearchRangeEnd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                editTextSearchRangeEnd.getText().clear();
                return false;
            }
        });

        //Set up infinity button
        final ImageButton infinityButton = findViewById(R.id.infinity_button);
        infinityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearchRangeEnd.setText(getString(R.string.infinity_text));
            }
        });

        //Set up search method
        radioGroupSearchMethod = findViewById(R.id.radio_group_search_method);
        radioGroupSearchMethod.addOnCheckChangedListener(new CustomRadioGroup.OnCheckChangedListener() {
            @Override
            public void onChecked(RadioButton radioButton, boolean isChecked) {
                switch (radioButton.getId()) {

                    case R.id.brute_force:
                        searchMethod = BRUTE_FORCE;
                        editTextSearchRangeStart.setEnabled(true);
                        infinityButton.setEnabled(true);
                        infinityButton.setAlpha(1f);
                        threadCountSpinner.setEnabled(true);
                        break;

                    case R.id.sieve_of_eratosthenes:
                        searchMethod = SIEVE_OF_ERATOSTHENES;
                        editTextSearchRangeStart.setText("0");
                        editTextSearchRangeStart.setEnabled(false);
                        if (getEndValue().compareTo(getStartValue()) <= 0) {
                            editTextSearchRangeEnd.getText().clear();
                        }
                        infinityButton.setEnabled(false);
                        infinityButton.setAlpha(0.3f);
                        threadCountSpinner.setEnabled(false);
                        break;

                }
            }
        });

        //Set up thread count
        threadCountSpinner = findViewById(R.id.thread_count_spinner);
        final String[] items = new String[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < items.length; i++) {
            items[i] = String.valueOf(i + 1);
        }
        threadCountSpinner.setAdapter(new ThreadCountAdapter(this, items));

        notifyWhenFinishedCheckbox = findViewById(R.id.notify_when_finished);
        autoSaveCheckbox = findViewById(R.id.auto_save);

        try {
            final FindPrimesTask.SearchOptions searchOptions = getIntent().getExtras().getParcelable("searchOptions");
            applyConfig(searchOptions);
        } catch (NullPointerException e) {
            e.printStackTrace();
            applyConfig(null);
        }

        try{
            task = (FindPrimesTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) getIntent().getExtras().get("taskId"));
            if (task.getState() != Task.State.NOT_STARTED){
                editTextSearchRangeStart.setEnabled(false);

                if (task.getState() != Task.State.STOPPING && task.getState() != Task.State.STOPPED){
                    editTextSearchRangeEnd.setEnabled(false);
                    infinityButton.setEnabled(false);
                    threadCountSpinner.setEnabled(false);
                }else{
                    editTextSearchRangeEnd.setEnabled(false);
                    infinityButton.setEnabled(false);
                    threadCountSpinner.setEnabled(false);
                }

                radioGroupSearchMethod.setEnabled(false);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    class ThreadCountAdapter extends ArrayAdapter<String>{

        private final LayoutInflater layoutInflater;

        private final String[] items;

        public ThreadCountAdapter(final Context context, final String[] items){
            super(context, R.layout.thread_count_list_item, R.id.text, items);
            layoutInflater = getLayoutInflater();
            this.items = items;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null){
                convertView = layoutInflater.inflate(R.layout.thread_count_list_item, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.text)).setText(items[position]);
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_configuration_menu, menu);

        if (task != null){
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
                if (isRangeValid()) {
                    final Intent intent = new Intent();
                    final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(getStartValue().longValue(), getEndValue().longValue());
                    searchOptions.setSearchMethod(searchMethod);
                    searchOptions.setThreadCount(Integer.valueOf((String) threadCountSpinner.getSelectedItem()));
                    searchOptions.notifyWhenFinished = notifyWhenFinishedCheckbox.isChecked();
                    searchOptions.autoSave = autoSaveCheckbox.isChecked();
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

    private void applyThemeColor(final int statusBarColor, final int actionBarColor) {

        //Set status bar color
        getWindow().setStatusBarColor(statusBarColor);

        //Set actionbar color
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));
    }

    private BigInteger getStartValue() {
        final String input = editTextSearchRangeStart.getText().toString().trim();
        if (input.length() > 0) {
            return new BigInteger(input.replace(",", ""));
        } else {
            return BigInteger.ZERO;
        }
    }

    private BigInteger getEndValue() {
        final String input = editTextSearchRangeEnd.getText().toString().trim();

        if (input.length() > 0) {
            if (input.equals("infinity")) {
                return BigInteger.valueOf(FindPrimesTask.INFINITY);
            }
            return new BigInteger(input.replace(",", ""));
        } else {
            return BigInteger.ZERO;
        }
    }

    private boolean isRangeValid() {

        //Validate the start value
        try {
            final BigInteger startValue = new BigInteger(editTextSearchRangeStart.getText().toString().replace(",", ""));
            BigInteger endValue;
            final String searchRangeEndText = editTextSearchRangeEnd.getText().toString();
            if (searchRangeEndText.equals(getString(R.string.infinity_text))) {
                endValue = BigInteger.valueOf(FindPrimesTask.INFINITY);
            } else {
                endValue = new BigInteger(searchRangeEndText.replace(",", ""));
            }

            //Check if numbers are greater than long range
            if (startValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1 || endValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
                return false;
            }

            //The start value must be at least 0
            if (startValue.compareTo(BigInteger.ZERO) == -1) {
                return false;
            }

            //Depends on search method
            switch (searchMethod) {
                case SIEVE_OF_ERATOSTHENES:

                    //The start value must equal to 0
                    if (startValue.compareTo(BigInteger.ZERO) != 0) {
                        return false;
                    }

                    //The end value cannot be infinity
                    if (endValue.compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) == 0) {
                        return false;
                    }

                    //The end value cannot larger than an int
                    if (endValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
                        return false;
                    }

                    break;

                case BRUTE_FORCE:
                    break;
            }

            if (endValue.compareTo(BigInteger.valueOf(FindPrimesTask.INFINITY)) != 0) {

                //End value must be greater than start value
                if (startValue.compareTo(endValue) >= 0) {
                    return false;
                }

                //End value must be greater than current number
                /*if (taskFragment != null && taskFragment.getTask() != null && taskFragment.getTask() instanceof FindPrimesTask) {
                    if (endValue.compareTo(BigInteger.valueOf(((FindPrimesTask) taskFragment.getTask()).getCurrentNumber())) <= 0) {
                        return false;
                    }
                }*/
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private void applyConfig(final FindPrimesTask.SearchOptions searchOptions) {

        if (searchOptions != null) {
            editTextSearchRangeStart.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(searchOptions.getStartValue()));
            editTextSearchRangeEnd.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(searchOptions.getEndValue()));

            switch (searchOptions.getSearchMethod()) {
                case BRUTE_FORCE:
                    radioGroupSearchMethod.check(R.id.brute_force);
                    editTextSearchRangeStart.setEnabled(true);
                    break;

                case SIEVE_OF_ERATOSTHENES:
                    radioGroupSearchMethod.check(R.id.sieve_of_eratosthenes);
                    editTextSearchRangeStart.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(0));
                    editTextSearchRangeStart.setEnabled(false);
                    break;
            }

            threadCountSpinner.setSelection(searchOptions.getThreadCount() - 1);

            notifyWhenFinishedCheckbox.setChecked(searchOptions.notifyWhenFinished);
            autoSaveCheckbox.setChecked(searchOptions.autoSave);
        } else {
            editTextSearchRangeStart.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(0));
            editTextSearchRangeStart.setEnabled(true);
            editTextSearchRangeEnd.getText().clear();
            editTextSearchRangeEnd.requestFocus();

            radioGroupSearchMethod.check(R.id.brute_force);

            threadCountSpinner.setSelection(0);

            notifyWhenFinishedCheckbox.setChecked(false);
            autoSaveCheckbox.setChecked(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        PrimeNumberFinder.getTaskManager().resumeAllTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!PrimeNumberFinder.getPreferenceManager().isAllowBackgroundTasks()) {
            PrimeNumberFinder.getTaskManager().pauseAllTasks();
        }
    }
}
