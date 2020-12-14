package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.ui.TreeView;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.Map;

import easytasks.ITask;

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

    private TreeView treeView;

    private final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();
    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

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
        bodyTextView.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        treeView = rootView.findViewById(R.id.factor_tree);

        initDefaultState();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskControlBubble.getRightView().setOnClickListener((v)->{if (getTask() instanceof Savable) Utils.save((Savable) getTask(), getActivity(), false);});
    }

    @Override
    protected void postDefaults() {
        super.postDefaults();

        //Subtitle
        subtitle.setText(Utils.formatSpannable(subtitleStringBuilder,
                getString(R.string.prime_factorization_subtitle),
                new String[]{numberFormat.format(getTask().getNumber())},
                new boolean[]{true},
                getTextHighlight(),
                getContext()
        ));
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        //Subtitle
        subtitle.setText(Utils.formatSpannable(subtitleStringBuilder,
                getResources().getQuantityString(R.plurals.prime_factorization_subtitle_results, getTask().getPrimeFactors().size()),
                new String[]{numberFormat.format(getTask().getNumber()), numberFormat.format(getTask().getPrimeFactors().size())},
                new boolean[]{true, true},
                getTextHighlight(),
                getContext()
        ));

        //Body
        spannableStringBuilder.clear();
        spannableStringBuilder.clearSpans();
        spannableStringBuilder.append(numberFormat.format(getTask().getNumber()));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(getTextHighlight()), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        Utils.applyCopySpan(spannableStringBuilder, 0, spannableStringBuilder.length(), getContext());
        int position = spannableStringBuilder.length();
        spannableStringBuilder.append(" = ");
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Utils.getColor(android.R.attr.textColorSecondary, getContext())), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        final Map map = getTask().getPrimeFactors();
        for (Object factor : map.keySet()){
            position = spannableStringBuilder.length();
            String content = numberFormat.format(factor);
            spannableStringBuilder.append(content);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(getTextHighlight()), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            Utils.applyCopySpan(spannableStringBuilder, position, spannableStringBuilder.length(), getContext());

            position = spannableStringBuilder.length();
            content = numberFormat.format(map.get(factor));
            spannableStringBuilder.append(content);
            spannableStringBuilder.setSpan(new SuperscriptSpan(), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            position = spannableStringBuilder.length();
            content = " \u00D7 "; //Multiplication sign
            spannableStringBuilder.append(content);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Utils.getColor(android.R.attr.textColorSecondary, getContext())), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        spannableStringBuilder.delete(spannableStringBuilder.length() - 3, spannableStringBuilder.length());
        bodyTextView.setVisibility(View.VISIBLE);
        bodyTextView.setText(spannableStringBuilder);

        //Tree
        treeView.setVisibility(View.VISIBLE);
        treeView.setTree(getTask().getFactorTree().formatNumbers());

        taskControlBubble.showRight(true);
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){
            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));
        }
    }

    @Override
    public synchronized PrimeFactorizationTask getTask() {
        return (PrimeFactorizationTask) super.getTask();
    }

    @Override
    public synchronized void setTask(ITask task) {
        super.setTask(task);
        if (getView() != null){
            initDefaultState();
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();
        bodyTextView.setVisibility(View.GONE);
        treeView.setVisibility(View.GONE);
    }
}
