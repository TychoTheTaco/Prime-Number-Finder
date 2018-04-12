package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.TreeView;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class PrimeFactorizationResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeFctriztnRsltsFgmnt";

    //Views
    private ViewGroup resultsView;
    private TextView noTaskView;
    private TextView title;
    private TextView subtitle;
    private TextView primeFactorization;
    private ProgressBar progressBar;
    private TextView progress;
    private Button saveButton;

    private TextView timeElapsedTextView;
    private TextView etaTextView;

    private TreeView treeView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_results_fragment, container, false);

        title = rootView.findViewById(R.id.title);
        subtitle = rootView.findViewById(R.id.subtitle);
        primeFactorization = rootView.findViewById(R.id.prime_factorization);
        progressBar= rootView.findViewById(R.id.progress_bar);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        saveButton = rootView.findViewById(R.id.button_save);
        treeView = rootView.findViewById(R.id.factor_tree);

        timeElapsedTextView = rootView.findViewById(R.id.textView_elapsed_time);
        etaTextView = rootView.findViewById(R.id.textView_eta);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean success = FileManager.getInstance().saveTree(getTask().getFactorTree());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });

        init();

        return rootView;
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {

                    title.setText(getString(R.string.status_searching));
                    saveButton.setVisibility(View.GONE);

                    //Format subtitle
                    final String number = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getNumber());
                    final String string = getResources().getQuantityString(R.plurals.prime_factorization_results_subtitle, getTask().getPrimeFactors().size());
                    final String[] split = string.split("%\\d\\$.");
                    final String[] items = {number, split[1], NumberFormat.getInstance(Locale.getDefault()).format(getTask().getPrimeFactors().size()), split[2]};
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    for (int i = 0; i < items.length; i++){
                        if (i % 2 == 0){
                            spannableStringBuilder.append(items[i], new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.green_dark)), 0);
                            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.length() - items[i].length(), spannableStringBuilder.length(), 0);
                        }else{
                            spannableStringBuilder.append(items[i]);
                        }
                    }
                    subtitle.setText(spannableStringBuilder);
                }
            });
        }

    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    title.setText(getString(R.string.status_paused));
                    saveButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        onTaskStarted();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        updateUi();
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    title.setText(getString(R.string.status_finished));
                    saveButton.setVisibility(View.VISIBLE);

                    //Format subtitle
                    final String number = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getNumber());
                    final String string = getResources().getQuantityString(R.plurals.prime_factorization_results_subtitle, getTask().getPrimeFactors().size());
                    final String[] split = string.split("%\\d\\$.");
                    final String[] items = {number, split[1], NumberFormat.getInstance(Locale.getDefault()).format(getTask().getPrimeFactors().size()), split[2]};
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    for (int i = 0; i < items.length; i++){
                        if (i % 2 == 0){
                            spannableStringBuilder.append(items[i], new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.green_dark)), 0);
                            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.length() - items[i].length(), spannableStringBuilder.length(), 0);
                        }else{
                            spannableStringBuilder.append(items[i]);
                        }
                    }
                    subtitle.setText(spannableStringBuilder);

                    spannableStringBuilder = new SpannableStringBuilder();
                    final Map map = getTask().getPrimeFactors();
                    for (Object factor : map.keySet()){
                        spannableStringBuilder.append(NumberFormat.getInstance(Locale.getDefault()).format(factor), new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.green_dark)), 0);
                        final int startIndex = spannableStringBuilder.length();
                        spannableStringBuilder.append(NumberFormat.getInstance(Locale.getDefault()).format(map.get(factor)), new SuperscriptSpan(), 0);
                        final int endIndex = spannableStringBuilder.length();
                        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), startIndex, endIndex, 0);
                        spannableStringBuilder.append(" x ");
                    }
                    spannableStringBuilder.delete(spannableStringBuilder.length() - 3, spannableStringBuilder.length());
                    primeFactorization.setText(spannableStringBuilder);

                    treeView.setTree(getTask().getFactorTree().formatNumbers());
                }
            });
        }
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){
            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 1_000));

            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));
            etaTextView.setText("Time remaining: " + Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 2));
        }
    }

    @Override
    public PrimeFactorizationTask getTask() {
        return (PrimeFactorizationTask) super.getTask();
    }

    @Override
    public void setTask(Task task) {
        super.setTask(task);
        if (getView() != null){
            init();
        }
    }

    private void init(){
        if (getTask() != null){

            //Make sure view is visible
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            if (getView() != null && getTask().getFactorTree() != null){
                treeView.setTree(getTask().getFactorTree().formatNumbers());
            }

        }else{
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
