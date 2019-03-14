package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.StatisticsLayout;
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

    //Statistics
    private StatisticsLayout statisticsLayout;

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.check_primality_results_fragment, container, false);

        initStandardViews(rootView);

        subtitle = rootView.findViewById(R.id.subtitle);
        subtitle.setMovementMethod(LongClickLinkMovementMethod.getInstance());

        //Statistics
        statisticsLayout = new StatisticsLayout(rootView.findViewById(R.id.statistics_layout));
        statisticsLayout.add("eta", R.drawable.ic_timer_white_24dp);
        statisticsLayout.inflate();

        initDefaultState();

        return rootView;
    }

    @Override
    protected void postDefaults() {
        //Format subtitle
        subtitle.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.check_primality_subtitle_searching), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, new boolean[]{true}, ContextCompat.getColor(getContext(), R.color.purple_dark), getActivity()));

        //Statistics
        statisticsLayout.setVisibility(View.VISIBLE);
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

        //Statistics
        statisticsLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {
            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Time remaining
            statisticsLayout.set("eta", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, getTextHighlight()));
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
