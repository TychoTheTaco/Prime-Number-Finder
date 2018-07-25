package com.tycho.app.primenumberfinder.modules.gcf.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.StatisticsLayout;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.utils.Utils;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class GreatestCommonFactorResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GreatestCommonFactorResultsFragment.class.getSimpleName();

    //Views
    private TextView subtitleTextView;
    private TextView bodyTextView;

    //Statistics
    private StatisticsLayout statisticsLayout;

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gcf_results_fragment, container, false);

        initStandardViews(rootView);

        subtitleTextView = rootView.findViewById(R.id.subtitle);
        bodyTextView = rootView.findViewById(R.id.text);

        //Statistics
        statisticsLayout = new StatisticsLayout(rootView.findViewById(R.id.statistics_layout));
        statisticsLayout.add("eta", R.drawable.ic_timer_white_24dp);
        statisticsLayout.inflate();

        init();

        return rootView;
    }

    @Override
    protected void postDefaults() {
        super.postDefaults();

        //Subtitle
        subtitleTextView.setText(generateSubtitle());

        //Statistics
        statisticsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        //Subtitle
        subtitleTextView.setText(generateResultSubtitle());

        //Body
        bodyTextView.setVisibility(View.GONE);

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
            statisticsLayout.set("eta", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));
        }
    }

    @Override
    public synchronized GreatestCommonFactorTask getTask() {
        return (GreatestCommonFactorTask) super.getTask();
    }

    @Override
    public synchronized void setTask(final Task task) {
        super.setTask(task);
        if (getView() != null) {
            init();
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();
        bodyTextView.setVisibility(View.VISIBLE);
        statisticsLayout.setVisibility(View.VISIBLE);
    }

    private SpannableStringBuilder generateSubtitle(){
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        int position;
        spannableStringBuilder.append(getString(R.string.gcf_subtitle).split("%\\d+\\$s")[0]);
        for (int i = 0; i < getTask().getNumbers().size(); i++){
            position = spannableStringBuilder.length();
            spannableStringBuilder.append(NUMBER_FORMAT.format(getTask().getNumbers().get(i)), new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.blue_dark)), 0);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            //Utils.applyCopySpan(spannableStringBuilder, position, spannableStringBuilder.length(), getContext());
            Utils.separateNumbers(spannableStringBuilder, getTask().getNumbers(), i, ",");
        }
        return spannableStringBuilder.append('.');
    }

    private SpannableStringBuilder generateResultSubtitle(){
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.gcf_result_long).split("%\\d+\\$s")[0]);
        for (int i = 0; i < getTask().getNumbers().size(); i++){
            spannableStringBuilder.append(NUMBER_FORMAT.format(getTask().getNumbers().get(i)), new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.blue_dark)), 0);
            Utils.separateNumbers(spannableStringBuilder, getTask().getNumbers(), i, ",");
        }
        spannableStringBuilder.append(getString(R.string.gcf_result_long).split("%\\d+\\$s")[1]);
        final int position = spannableStringBuilder.length();
        spannableStringBuilder.append(NUMBER_FORMAT.format(getTask().getGcf()),  new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.blue_dark)), 0);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        //TODO: Copy span shows text underlined and also still isnt clickable. probably because text view isnt focusable
        //Utils.applyCopySpan(spannableStringBuilder, position, spannableStringBuilder.length(), getContext());
        return spannableStringBuilder.append('.');
    }
}
