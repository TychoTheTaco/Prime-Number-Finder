package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

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

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesNativeTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.ui.NumberInput;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Random;
import java.util.UUID;

import easytasks.ITask;

import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.BRUTE_FORCE;
import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.SIEVE_OF_ERATOSTHENES;
import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * @author Tycho Bellers
 * Date Created: 11/12/2016
 */
public class FindPrimesFragment extends ModuleHostFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesFragment.class.getSimpleName();

    //Views
    private NumberInput editTextPrimalityInput;
    private NumberInput editTextSearchRangeStart;
    private NumberInput editTextSearchRangeEnd;

    //Results fragments
    private final FindPrimesResultsFragment findPrimesResultsFragment = new FindPrimesResultsFragment();
    private final CheckPrimalityResultsFragment checkPrimalityResultsFragment = new CheckPrimalityResultsFragment();

    /**
     * Search options used for starting tasks.
     */
    private final FindPrimesTask.SearchOptions searchOptions = new FindPrimesTask.SearchOptions(0, FindPrimesTask.INFINITY, BRUTE_FORCE, 1);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setConfigurationClass(FindPrimesConfigurationActivity.class);

        final View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView != null) {

            // Inflate configuration layout
            inflater.inflate(R.layout.find_primes_configuration_fragment, rootView.findViewById(R.id.configuration_container));

            final NumberFormat numberFormat = NumberFormat.getNumberInstance();

            //Set up check primality button
            final Button buttonCheckPrimality = rootView.findViewById(R.id.check_primality_button);
            buttonCheckPrimality.setOnClickListener(v -> {

                //Check if the number is valid
                if (Validator.isPrimalityInputValid(getPrimalityInput())) {

                    //Create a new task
                    final ITask task = new CheckPrimalityTask(getPrimalityInput().longValue());
                    startTask(task);
                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
                }
            });

            //Set up find primes button
            final Button buttonFindPrimes = rootView.findViewById(R.id.button_find_primes);
            buttonFindPrimes.setOnClickListener(v -> {

                //Determine best search method
                searchOptions.setSearchMethod(determineBestSearchMethod());

                //Check if the range is valid
                if (Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod())) {

                    //Create a new task
                    searchOptions.setThreadCount(1);

                    searchOptions.setCacheDirectory(getActivity().getCacheDir());

                    try {
                        startTask(new FindPrimesNativeTask((FindPrimesTask.SearchOptions) searchOptions.clone()));
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }

                    //Reset search options
                    searchOptions.setSearchMethod(BRUTE_FORCE);

                    hideKeyboard(getActivity());

                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_range), Toast.LENGTH_SHORT).show();
                }
            });

            //Set up number input
            editTextPrimalityInput = rootView.findViewById(R.id.primality_input);
            editTextPrimalityInput.setHint(numberFormat.format(new Random().nextInt(1_000_000)));
            editTextPrimalityInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //Check if the number is valid
                    editTextPrimalityInput.setValid(Validator.isPrimalityInputValid(getPrimalityInput()));
                }
            });
            editTextPrimalityInput.setOnEditorActionListener((v, actionId, event) -> buttonCheckPrimality.performClick());

            //Set up range start input
            editTextSearchRangeStart = rootView.findViewById(R.id.search_range_start);
            editTextSearchRangeStart.setHint(numberFormat.format(0));
            editTextSearchRangeStart.setAllowZeroInput(true);
            editTextSearchRangeStart.setText(numberFormat.format(searchOptions.getStartValue()));
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

            //Set up range end input
            editTextSearchRangeEnd = rootView.findViewById(R.id.search_range_end);
            editTextSearchRangeEnd.setShowRandomHint(false);
            editTextSearchRangeEnd.setHint("∞");
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
                        final String formatted = numberFormat.format(getEndValue());
                        if (editable.length() > 0 && !editable.toString().equals(formatted)) {
                            editTextSearchRangeEnd.setText(formatted, formatted.length() > 1);
                        } else if (editable.toString().equals(numberFormat.format(0))) {
                            editTextSearchRangeEnd.getText().clear();
                        }
                    }

                    searchOptions.setEndValue(getEndValue().longValue());

                    //Check if the number is valid
                    editTextSearchRangeStart.setValid(editTextSearchRangeStart.length() > 0);
                    editTextSearchRangeEnd.setValid(Validator.isFindPrimesRangeValid(getStartValue(), getEndValue(), searchOptions.getSearchMethod()) && editTextSearchRangeEnd.length() != 0);
                    if (editTextSearchRangeEnd.length() == 0){
                        editTextSearchRangeEnd.setValid(true);
                    }
                }
            });
            editTextSearchRangeEnd.overrideDefaultTextWatcher();
            editTextSearchRangeEnd.setOnEditorActionListener((v, actionId, event) -> buttonFindPrimes.performClick());
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_NEW_TASK:
                if (data != null && data.getExtras() != null) {
                    final FindPrimesTask task = (FindPrimesTask) PrimeNumberFinder.getTaskManager().findTaskById((UUID) data.getExtras().get("taskId"));
                    final FindPrimesTask.SearchOptions searchOptions = data.getExtras().getParcelable("searchOptions");
                    if (searchOptions != null) {
                        if (task == null) {
                            startTask(new FindPrimesNativeTask(searchOptions));
                        } else {
                            task.setSearchOptions(searchOptions);
                        }
                    }
                }
                break;
        }
    }

    private FindPrimesTask.SearchOptions.SearchMethod determineBestSearchMethod() {

        //Check if end value is infinity or is greater than int range
        if (getEndValue().longValue() == FindPrimesTask.INFINITY || getEndValue().compareTo(BigInteger.valueOf(Integer.MAX_VALUE - 1)) > 0) {
            return BRUTE_FORCE;
        }

        //Make sure we have enough memory to use the sieve
        if (getStartValue().compareTo(BigInteger.ZERO) >= 0 && hasEnoughMemoryForSieve()) {
            return SIEVE_OF_ERATOSTHENES;
        }

        return BRUTE_FORCE;
    }

    private boolean hasEnoughMemoryForSieve() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;

        final long bits = searchOptions.getEndValue() + 1;
        final int longCount = (int) (bits / 8) + 1;
        final int arraySize = longCount * 8;
        final long estimatedPrimeCount = (long) ((searchOptions.getEndValue() / Math.log(searchOptions.getEndValue())) - (searchOptions.getStartValue() / Math.log(searchOptions.getStartValue())));
        final long primesSize = estimatedPrimeCount * 8;
        final long totalBytes = arraySize + primesSize;

        //Add an extra 13% due to inaccurate estimate of prime count
        final int requiredMB = (int) ((totalBytes * 1.13f) / (1024 * 1024));

        //Log.d(TAG, "RAM: " + usedMemInMB + " / " + maxHeapSizeInMB);
        //Log.d(TAG, "Avail: " + availHeapSizeInMB);
        //Log.d(TAG, "Req: " + requiredMB);
        FirebaseCrashlytics.getInstance().setCustomKey("requiredMB", requiredMB);
        FirebaseCrashlytics.getInstance().setCustomKey("availableHeapMB", availHeapSizeInMB);

        return requiredMB <= availHeapSizeInMB;
    }

    private BigInteger getPrimalityInput() {
        return Utils.textToNumber(editTextPrimalityInput.getText().toString());
    }

    private BigInteger getStartValue() {
        return Utils.textToNumber(editTextSearchRangeStart.getText().toString());
    }

    private BigInteger getEndValue() {
        final String input = editTextSearchRangeEnd.getText().toString().trim();

        //Check for infinity
        if (input.equals(getString(R.string.infinity_text))) {
            return BigInteger.valueOf(FindPrimesTask.INFINITY);
        }

        return Utils.textToNumber(input);
    }

    @Override
    protected void startTask(ITask task) {
        if (task instanceof FindPrimesTask){
            setResultsFragment(findPrimesResultsFragment);
        }else{
            setResultsFragment(checkPrimalityResultsFragment);
        }
        super.startTask(task);
    }
}
