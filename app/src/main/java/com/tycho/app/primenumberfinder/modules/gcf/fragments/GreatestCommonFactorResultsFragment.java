package com.tycho.app.primenumberfinder.modules.gcf.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.gcf.GCFListAdapter;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.utils.Utils;

import easytasks.ITask;

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

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    private GCFListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.gcf_results_fragment, container, false);

        initStandardViews(rootView);

        adapter = new GCFListAdapter(getContext());

        //Set up recycler view
        final RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        subtitleTextView = rootView.findViewById(R.id.subtitle);
        subtitleTextView.setMovementMethod(LongClickLinkMovementMethod.getInstance());

        initDefaultState();

        return rootView;
    }

    @Override
    protected void postDefaults() {
        super.postDefaults();

        //Subtitle
        subtitleTextView.setText(generateSubtitle());
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        //Subtitle
        subtitleTextView.setText(generateResultSubtitle());

        adapter.set(getTask().getNumbers(), getTask().getGcf());
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
    public synchronized GreatestCommonFactorTask getTask() {
        return (GreatestCommonFactorTask) super.getTask();
    }

    @Override
    public synchronized void setTask(final ITask task) {
        super.setTask(task);
        if (getView() != null) {
            initDefaultState();
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();
        adapter.clear();
    }

    private SpannableStringBuilder generateSubtitle(){
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        int position;
        spannableStringBuilder.append(getString(R.string.gcf_subtitle).split("%\\d+\\$s")[0]);
        for (int i = 0; i < getTask().getNumbers().size(); i++){
            position = spannableStringBuilder.length();
            spannableStringBuilder.append(numberFormat.format(getTask().getNumbers().get(i)), new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.blue_dark)), 0);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            Utils.applyCopySpan(spannableStringBuilder, position, spannableStringBuilder.length(), getContext());
            Utils.separateNumbers(spannableStringBuilder, getTask().getNumbers(), i, ",");
        }
        return spannableStringBuilder.append('.');
    }

    private SpannableStringBuilder generateResultSubtitle(){
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.gcf_result_long).split("%\\d+\\$s")[0]);
        for (int i = 0; i < getTask().getNumbers().size(); i++){
            final int position = spannableStringBuilder.length();
            spannableStringBuilder.append(numberFormat.format(getTask().getNumbers().get(i)), new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.blue_dark)), 0);
            Utils.applyCopySpan(spannableStringBuilder, position, spannableStringBuilder.length(), getContext());
            Utils.separateNumbers(spannableStringBuilder, getTask().getNumbers(), i, ",");
        }
        spannableStringBuilder.append(getString(R.string.gcf_result_long).split("%\\d+\\$s")[1]);
        final int position = spannableStringBuilder.length();
        spannableStringBuilder.append(numberFormat.format(getTask().getGcf()),  new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.blue_dark)), 0);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        Utils.applyCopySpan(spannableStringBuilder, position, spannableStringBuilder.length(), getContext());
        return spannableStringBuilder.append('.');
    }
}
