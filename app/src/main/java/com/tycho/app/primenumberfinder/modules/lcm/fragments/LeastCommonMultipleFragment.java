package com.tycho.app.primenumberfinder.modules.lcm.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.VerticalItemDecoration;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.FindPrimesTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.lcm.LCMConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.modules.lcm.adapters.LCMTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.lcm.adapters.NumbersListAdapter;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import easytasks.TaskAdapter;

import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

/**
 * This {@linkplain Fragment} allows the user to input a number they want to factor. This fragment
 * will display the progress and statistics of the factorization, along with a list of factors that
 * are found.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public class LeastCommonMultipleFragment extends ModuleHostFragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LeastCommonMultipleFragment.class.getSimpleName();

    final List<ValidEditText> inputs = new ArrayList<>();

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.lcm_fragment, container, false);

        //Set up input
        inputs.add(rootView.findViewById(R.id.input0).findViewById(R.id.input));
        inputs.add(rootView.findViewById(R.id.input1).findViewById(R.id.input));
        inputs.add(rootView.findViewById(R.id.input2).findViewById(R.id.input));
        for (ValidEditText editText : inputs){
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    editText.setValid(Validator.isValidLCMInput(editText.getLongValue()));
                }
            });
        }

        //Set up start button
        final Button buttonFindFactors = rootView.findViewById(R.id.button_find_factors);
        buttonFindFactors.setOnClickListener(v -> {

            //Check if the number is valid
            if (Validator.isValidLCMInput(getNumbers())) {

                LeastCommonMultipleTask task = new LeastCommonMultipleTask(new LeastCommonMultipleTask.SearchOptions(getNumbers()));
                startTask(task);

               /* //Create a new task
                searchOptions.setNumber(getNumberToFactor().longValue());
                try {
                    startTask((FindFactorsTask.SearchOptions) searchOptions.clone());
                }catch (CloneNotSupportedException e){
                    e.printStackTrace();
                }*/

                hideKeyboard(getActivity());
                taskListFragment.scrollToBottom();

            } else {
                Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private List<Long> getNumbers(){
        final List<Long> numbers = new ArrayList<>();
        for (ValidEditText editText : inputs){
            if (editText.isValid()) numbers.add(editText.getLongValue());
        }
        return numbers;
    }

    @Override
    protected void loadFragments() {
        super.loadFragments();
        setResultsFragment(LeastCommonMultipleResultsFragment.class);
    }

    @Override
    protected void afterLoadFragments() {
        super.afterLoadFragments();
        taskListFragment.setAdapter(new LCMTaskListAdapter(getContext()));
    }

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), LCMConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
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
