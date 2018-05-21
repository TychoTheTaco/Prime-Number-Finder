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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
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
    private ViewGroup statisticsLayout;
    private TextView etaTextView;

    private TreeView treeView;

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
        bodyTextView = rootView.findViewById(R.id.prime_factorization);
        treeView = rootView.findViewById(R.id.factor_tree);

        //Statistics
        statisticsLayout = rootView.findViewById(R.id.statistics_layout);
        statisticsLayout.setVisibility(View.GONE);
        etaTextView = rootView.findViewById(R.id.textView_eta);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask(getTask());
            }
        });

        init();

        return rootView;
    }

    public void saveTask(final PrimeFactorizationTask task){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Saving...");
        progressDialog.show();

        task.addSavableCallbacks(new Savable.SavableCallbacks() {
            @Override
            public void onSaved() {
                progressDialog.dismiss();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.successfully_saved_file), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Error saving file!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                task.save();
            }
        }).start();
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (isAdded() && !isDetached() && getTask() != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_searching));
                    progressBar.startAnimation(rotate);

                    //Subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder,
                            getString(R.string.prime_factorization_subtitle),
                            new String[]{NUMBER_FORMAT.format(getTask().getNumber())},
                            ContextCompat.getColor(getActivity(), R.color.green_dark)
                    ));

                    //Buttons
                    centerView.getLayoutParams().width = (int) Utils.dpToPx(getActivity(), 64);
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    //viewAllButton.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);
                }
            });
        }

    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.state_pausing));

                    //Subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder,
                            getString(R.string.prime_factorization_subtitle),
                            new String[]{NUMBER_FORMAT.format(getTask().getNumber())},
                            ContextCompat.getColor(getActivity(), R.color.green_dark)
                    ));

                    //Buttons
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(false);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    saveButton.setVisibility(View.GONE);
                    //viewAllButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        if (isAdded() && !isDetached() && getTask() != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_paused));
                    progressBar.clearAnimation();

                    //Subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder,
                            getString(R.string.prime_factorization_subtitle),
                            new String[]{NUMBER_FORMAT.format(getTask().getNumber())},
                            ContextCompat.getColor(getActivity(), R.color.green_dark)
                    ));

                    //Buttons
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    saveButton.setVisibility(View.GONE);
                    //viewAllButton.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.state_resuming));

                    //Subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder,
                            getString(R.string.prime_factorization_subtitle),
                            new String[]{NUMBER_FORMAT.format(getTask().getNumber())},
                            ContextCompat.getColor(getActivity(), R.color.green_dark)
                    ));

                    //Buttons
                    pauseButton.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        onTaskStarted();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        if (isAdded() && !isDetached() && getTask() != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_finished));
                    progressBar.clearAnimation();

                    //Subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder,
                            getResources().getQuantityString(R.plurals.prime_factorization_subtitle_results, getTask().getPrimeFactors().size()),
                            new String[]{NUMBER_FORMAT.format(getTask().getNumber()), NUMBER_FORMAT.format(getTask().getPrimeFactors().size())},
                            ContextCompat.getColor(getActivity(), R.color.green_dark)
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

                    //Buttons
                    centerView.setVisibility(View.GONE);
                    pauseButton.setVisibility(View.GONE);
                    //viewAllButton.setVisibility(View.VISIBLE);
                    final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) saveButton.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    saveButton.setLayoutParams(layoutParams);
                    saveButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){
            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Elapsed time
            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));
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

    @Override
    protected void onResetViews() {
        super.onResetViews();
        bodyTextView.setVisibility(View.GONE);
        treeView.setVisibility(View.GONE);
    }
}
