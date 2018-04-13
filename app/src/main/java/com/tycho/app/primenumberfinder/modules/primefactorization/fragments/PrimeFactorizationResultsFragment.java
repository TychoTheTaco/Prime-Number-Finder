package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
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
    private TextView bodyTextView;
    private ProgressBar progressBar;
    private TextView progress;

    //Buttons
    private ImageButton pauseButton;
    private ImageButton viewAllButton;
    private ImageButton saveButton;
    private View centerView;

    //Statistics
    private ViewGroup statisticsLayout;
    private TextView timeElapsedTextView;
    private TextView etaTextView;

    private TreeView treeView;

    private final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Set up progress animation
        rotate.setDuration(3000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_results_fragment, container, false);

        title = rootView.findViewById(R.id.title);
        subtitle = rootView.findViewById(R.id.subtitle);
        bodyTextView = rootView.findViewById(R.id.prime_factorization);
        progressBar = rootView.findViewById(R.id.progress_bar);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        treeView = rootView.findViewById(R.id.factor_tree);

        //Statistics
        statisticsLayout = rootView.findViewById(R.id.statistics_layout);
        timeElapsedTextView = rootView.findViewById(R.id.textView_elapsed_time);
        etaTextView = rootView.findViewById(R.id.textView_eta);

        //Buttons
        pauseButton = rootView.findViewById(R.id.pause_button);
        viewAllButton = rootView.findViewById(R.id.view_all_button);
        saveButton = rootView.findViewById(R.id.save_button);
        centerView = rootView.findViewById(R.id.center);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTask().getState() == Task.State.RUNNING) {
                    getTask().pause(false);
                } else if (getTask().getState() == Task.State.PAUSED) {
                    getTask().resume();
                }
            }
        });

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
                    onUiUpdate();

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
                    viewAllButton.setVisibility(View.GONE);
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
                    //Title
                    title.setText(getString(R.string.state_pausing));

                    //Subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder,
                            getString(R.string.prime_factorization_subtitle),
                            new String[]{NUMBER_FORMAT.format(getTask().getNumber())},
                            ContextCompat.getColor(getActivity(), R.color.green_dark)
                    ));

                    //Buttons
                    pauseButton.setEnabled(false);
                    saveButton.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUiUpdate();

                    //Title
                    title.setText(getString(R.string.status_paused));
                    progressBar.clearAnimation();

                    //Buttons
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    saveButton.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        Log.d(TAG, "onTaskResuming()");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    Log.d(TAG, "onTaskResuming() handler posted");
                    onUiUpdate();

                    //Title
                    title.setText(getString(R.string.state_resuming));

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
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Call onUiUpdate() one last time.");
                    onUiUpdate();
                    Log.d(TAG, "Begin final UI update.");

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
                        content = " x ";
                        spannableStringBuilder.append(content);
                        spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.gray)), position, spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                    spannableStringBuilder.delete(spannableStringBuilder.length() - 3, spannableStringBuilder.length());
                    bodyTextView.setVisibility(View.VISIBLE);
                    bodyTextView.setText(spannableStringBuilder);

                    //Tree
                    treeView.setVisibility(View.VISIBLE);
                    treeView.setTree(getTask().getFactorTree().formatNumbers());

                    //Statistics
                    //etaTextView.setVisibility(View.GONE);

                    //Buttons
                    centerView.getLayoutParams().width = 0;
                    pauseButton.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.VISIBLE);
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

            //Time remaining
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            final String time = Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1);
            spannableStringBuilder.append(time);
            spannableStringBuilder.append(" remaining");
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0, time.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            etaTextView.setText(spannableStringBuilder);
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
            Log.d(TAG, "init()");

            //Reset view states
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            //progress.setVisibility(View.VISIBLE);
            //bodyTextView.setVisibility(View.VISIBLE);
            //pauseButton.setVisibility(View.VISIBLE);
            //saveButton.setVisibility(View.VISIBLE);
            //etaTextView.setVisibility(View.VISIBLE);
            //bodyTextView.setVisibility(View.GONE);
            //treeView.setVisibility(View.GONE);
            //statisticsLayout.setVisibility(View.VISIBLE);

            /*if (getView() != null && getTask().getFactorTree() != null){
                treeView.setTree(getTask().getFactorTree().formatNumbers());
            }*/

            switch (getTask().getState()) {
                case RUNNING:
                    onTaskStarted();
                    break;

                case PAUSING:
                    onTaskPausing();
                    break;

                case PAUSED:
                    onTaskPaused();
                    break;

                case RESUMING:
                    onTaskResuming();
                    break;

                case STOPPING:
                    onTaskStopping();
                    break;

                case STOPPED:
                    onTaskStopped();
                    break;
            }

        }else{
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
