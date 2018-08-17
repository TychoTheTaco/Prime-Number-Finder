package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.StatisticsLayout;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.ui.TreeView;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.Map;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class PrimeFactorizationResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimeFactorizationResultsFragment.class.getSimpleName();

    //Views
    private TextView subtitle;
    private TextView bodyTextView;

    //Statistics
    private StatisticsLayout statisticsLayout;

    private TreeView treeView;

    private final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_results_fragment, container, false);

        initStandardViews(rootView);

        subtitle = rootView.findViewById(R.id.subtitle);
        subtitle.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        bodyTextView = rootView.findViewById(R.id.prime_factorization);
        treeView = rootView.findViewById(R.id.factor_tree);

        //Statistics
        statisticsLayout = new StatisticsLayout(rootView.findViewById(R.id.statistics_layout));
        statisticsLayout.add("eta", R.drawable.ic_timer_white_24dp);
        statisticsLayout.inflate();

        saveButton.setOnClickListener(v -> saveTask(getTask(), getActivity()));

        init();

        return rootView;
    }

    public void saveTask(final PrimeFactorizationTask task, final Context context){
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Saving...");
        progressDialog.show();

        new Thread(() -> {
            if (task.save()) {
                progressDialog.dismiss();
                handler.post(() -> {
                    Log.d(TAG, "Posted context: " + getContext() + " " + getActivity());
                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.successfully_saved_file), Toast.LENGTH_SHORT).show();
                });
            } else {
                handler.post(() -> Toast.makeText(context.getApplicationContext(), context.getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void postDefaults() {
        super.postDefaults();

        //Subtitle
        //TODO: Why dark is not applied??
        subtitle.setText(Utils.formatSpannable(subtitleStringBuilder,
                getString(R.string.prime_factorization_subtitle),
                new String[]{NUMBER_FORMAT.format(getTask().getNumber())},
                new boolean[]{true},
                ContextCompat.getColor(getContext(), R.color.green_dark),
                getContext()
        ));

        //Statistics
        statisticsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        //Subtitle
        subtitle.setText(Utils.formatSpannable(subtitleStringBuilder,
                getResources().getQuantityString(R.plurals.prime_factorization_subtitle_results, getTask().getPrimeFactors().size()),
                new String[]{NUMBER_FORMAT.format(getTask().getNumber()), NUMBER_FORMAT.format(getTask().getPrimeFactors().size())},
                new boolean[]{true, true},
                ContextCompat.getColor(getActivity(), R.color.green_dark),
                getContext()
        ));

        //Body
        spannableStringBuilder.clear();
        spannableStringBuilder.clearSpans();
        spannableStringBuilder.append(NUMBER_FORMAT.format(getTask().getNumber()));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.green_dark)), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        int position = spannableStringBuilder.length();
        spannableStringBuilder.append(" = ");
        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.gray)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        final Map map = getTask().getPrimeFactors();
        for (Object factor : map.keySet()){
            position = spannableStringBuilder.length();
            String content = NUMBER_FORMAT.format(factor);
            spannableStringBuilder.append(content);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.green_dark)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            position = spannableStringBuilder.length();
            content = NUMBER_FORMAT.format(map.get(factor));
            spannableStringBuilder.append(content);
            spannableStringBuilder.setSpan(new SuperscriptSpan(), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            position = spannableStringBuilder.length();
            content = " \u00D7 "; //Multiplication sign
            spannableStringBuilder.append(content);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.gray)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        spannableStringBuilder.delete(spannableStringBuilder.length() - 3, spannableStringBuilder.length());
        bodyTextView.setVisibility(View.VISIBLE);
        bodyTextView.setText(spannableStringBuilder);

        //Tree
        treeView.setVisibility(View.VISIBLE);
        treeView.setTree(getTask().getFactorTree().formatNumbers());

        centerView.setVisibility(View.GONE);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) saveButton.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        saveButton.setLayoutParams(layoutParams);

        //Statistics
        statisticsLayout.setVisibility(View.GONE);
    }

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){
            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Time remaining
            statisticsLayout.set("eta", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, ContextCompat.getColor(getActivity(), R.color.green_dark)));
        }
    }

    @Override
    public synchronized PrimeFactorizationTask getTask() {
        return (PrimeFactorizationTask) super.getTask();
    }

    @Override
    public synchronized void setTask(Task task) {
        super.setTask(task);
        if (getView() != null){
            init();
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();
        bodyTextView.setVisibility(View.GONE);
        treeView.setVisibility(View.GONE);
    }
}
