package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;
import com.tycho.app.primenumberfinder.utils.Utils;

import easytasks.ITask;

/**
 * Created by tycho on 11/16/2017.
 */
public class CheckPrimalityResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = CheckPrimalityResultsFragment.class.getSimpleName();

    //Views
    private TextView subtitle;

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.check_primality_results_fragment, container, false);

        initStandardViews(rootView);

        subtitle = rootView.findViewById(R.id.subtitle);
        subtitle.setMovementMethod(LongClickLinkMovementMethod.getInstance());

        initDefaultState();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskControlBubble.hideLeft(false);
    }

    @Override
    protected void postDefaults() {
        //Format subtitle
        subtitle.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.check_primality_subtitle_searching), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, new boolean[]{true}, ContextCompat.getColor(getContext(), R.color.purple_dark), getActivity()));
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        //Format subtitle
        if (getTask().isPrime()){
            subtitle.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.check_primality_result), new String[]{
                    NUMBER_FORMAT.format(getTask().getNumber()),
                    "prime"
            }, new boolean[]{
                    true,
                    false
            }, getTextHighlight(), getActivity()));
        }else{
            subtitle.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.check_primality_result_negative), new String[]{
                    NUMBER_FORMAT.format(getTask().getNumber()),
                    "not prime",
                    NUMBER_FORMAT.format(getTask().getFactor())
            }, new boolean[]{
                    true,
                    false,
                    true
            }, getTextHighlight(), getActivity()));
        }
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {
            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));
        }
    }

    @Override
    public CheckPrimalityTask getTask() {
        return (CheckPrimalityTask) super.getTask();
    }

    @Override
    public void setTask(final ITask task) {
        super.setTask(task);
        if (getView() != null) {
            initDefaultState();
        }
    }
}
