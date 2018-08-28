package com.tycho.app.primenumberfinder.modules.gcf.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter;
import com.tycho.app.primenumberfinder.modules.ModuleHostFragment;
import com.tycho.app.primenumberfinder.modules.gcf.GCFConfigurationActivity;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter.Button.DELETE;
import static com.tycho.app.primenumberfinder.modules.AbstractTaskListAdapter.Button.PAUSE;
import static com.tycho.app.primenumberfinder.utils.NotificationManager.TASK_TYPE_GCF;
import static com.tycho.app.primenumberfinder.utils.Utils.hideKeyboard;

public class GreatestCommonFactorFragment extends ModuleHostFragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GreatestCommonFactorFragment.class.getSimpleName();

    final List<ValidEditText> inputs = new ArrayList<>();

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gcf_fragment, container, false);

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
                taskListFragment.scrollToBottom();

            } else {
                Toast.makeText(getActivity(), getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
            }
        });

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
            if (editText.isValid() && editText.getLongValue() > 1) numbers.add(editText.getLongValue());
        }
        return numbers;
    }

    @Override
    protected void loadFragments() {
        super.loadFragments();
        setResultsFragment(GreatestCommonFactorResultsFragment.class);
    }

    @Override
    protected void afterLoadFragments() {
        super.afterLoadFragments();
        taskListFragment.setAdapter(new AbstractTaskListAdapter<GreatestCommonFactorTask>(getContext(), PAUSE, DELETE){

            @Override
            protected CharSequence getTitle(GreatestCommonFactorTask task) {
                return context.getString(R.string.gcf_task_list_item_title, Utils.formatNumberList(task.getNumbers(), NUMBER_FORMAT, ";"));
            }

            @Override
            protected CharSequence getSubtitle(GreatestCommonFactorTask task) {
                if (task.getState() == Task.State.STOPPED){
                    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    spannableStringBuilder.append(context.getString(R.string.status_finished));
                    spannableStringBuilder.append(": ");
                    spannableStringBuilder.append(context.getString(R.string.gcf_result, NUMBER_FORMAT.format((task.getGcf()))));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, getTheme() == 0 ? R.color.accent_dark : R.color.accent_light_but_not_that_light)), context.getString(R.string.status_finished).length() + 2, spannableStringBuilder.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    return spannableStringBuilder;
                }
                return super.getSubtitle(task);
            }

            @Override
            protected int getTaskType() {
                return TASK_TYPE_GCF;
            }
        });
        taskListFragment.whitelist(GreatestCommonFactorTask.class);
    }

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(getActivity(), GCFConfigurationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_TASK);
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
