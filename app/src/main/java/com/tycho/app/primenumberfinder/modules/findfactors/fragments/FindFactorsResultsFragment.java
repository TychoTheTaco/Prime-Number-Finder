package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.tycho.app.primenumberfinder.NativeTaskInterface;
import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.StatisticsLayout;
import com.tycho.app.primenumberfinder.modules.findfactors.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class FindFactorsResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindFactorsResultsFragment.class.getSimpleName();

    //Views
    private TextView subtitleTextView;
    private RecyclerView recyclerView;
    private TextView bodyTextView;

    //Statistics
    private StatisticsLayout statisticsLayout;

    private FactorsListAdapter adapter;

    private int lastAdapterSize = 0;

    private final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();
    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    /**
     * This map holds the statistics for each task. When {@linkplain FindFactorsResultsFragment#setTask(NativeTaskInterface)} is called,
     * the current task's statistics are saved to the map so that they can be used later when
     * {@linkplain FindFactorsResultsFragment#setTask(NativeTaskInterface)} is called with the same task.
     */
    private final Map<FindFactorsTask, FindFactorsResultsFragment.Statistics> statisticsMap = new HashMap<>();

    /**
     * This class keeps the statistics for a task.
     */
    private class Statistics {
        private long lastCurrentValue;
        private long lastUpdateTime = -1000;
        private long finalNumbersPerSecond;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        adapter = new FactorsListAdapter(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_results_fragment, container, false);

        initStandardViews(rootView);

        //Set up recycler view
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        subtitleTextView = rootView.findViewById(R.id.subtitle);
        subtitleTextView.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        bodyTextView = rootView.findViewById(R.id.text);

        //Statistics
        statisticsLayout = new StatisticsLayout(rootView.findViewById(R.id.statistics_layout));
        statisticsLayout.add("eta", R.drawable.ic_timer_white_24dp);
        statisticsLayout.add("nps", R.drawable.ic_trending_up_white_24dp);
        statisticsLayout.inflate();

        viewAllButton.setOnClickListener(v -> new Thread(() -> {

            handler.post(() -> Toast.makeText(getActivity(), getString(R.string.loading), Toast.LENGTH_SHORT).show());

            final Task.State state = getTask().getState();
            try {
                getTask().pauseAndWait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final File file = new File(getActivity().getFilesDir() + File.separator + "temp");
            final boolean success = FileManager.getInstance().saveFactors(getTask().getFactors(), file);
            if (!success) {
                handler.post(() -> Toast.makeText(getActivity(), getString(R.string.general_error), Toast.LENGTH_SHORT).show());
            }
            if (state == Task.State.RUNNING) {
                getTask().resume();
            }

            final Intent intent = new Intent(getActivity(), DisplayFactorsActivity.class);
            intent.putExtra("filePath", file.getAbsolutePath());
            intent.putExtra("title", false);
            intent.putExtra("number", getTask().getNumber());
            getActivity().startActivity(intent);
        }).start());

        saveButton.setOnClickListener(v -> saveTask(getTask(), getActivity()));

        init();

        return rootView;
    }

    public void saveTask(final FindFactorsTask task, final Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Saving...");
        progressDialog.show();

        new Thread(() -> {
            if (task.save()) {
                progressDialog.dismiss();
                handler.post(() -> {
                    Crashlytics.log(Log.DEBUG, TAG, "Posted context: " + getContext() + " " + getActivity());
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
        subtitleTextView.setText(Utils.formatSpannable(subtitleStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, new boolean[]{true}, ContextCompat.getColor(getActivity(), R.color.orange_dark), getContext()));

        //Statistics
        statisticsLayout.set("nps", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        //Subtitle
        Utils.formatSpannable(subtitleStringBuilder, getResources().getQuantityString(R.plurals.find_factors_subtitle_results, getTask().getFactors().size()), new String[]{NUMBER_FORMAT.format(getTask().getNumber()), NUMBER_FORMAT.format(getTask().getFactors().size())}, new boolean[]{true, true}, ContextCompat.getColor(getActivity(), R.color.orange_dark), getContext());
        if (getTask().getFactors().size() != 2) {
            subtitleTextView.setText(subtitleStringBuilder);
        } else {
            final SpannableStringBuilder ssb = new SpannableStringBuilder();
            Utils.formatSpannable(ssb, getResources().getString(R.string.find_factors_subtitle_results_extension), new String[]{"prime"}, ContextCompat.getColor(getActivity(), R.color.orange_dark));
            subtitleTextView.setText(TextUtils.concat(subtitleStringBuilder, " ", ssb));
        }

        //Body
        bodyTextView.setVisibility(View.GONE);

        //Statistics
        statisticsLayout.hide("eta");
        double elapsed = (double) getTask().getElapsedTime() / 1000;
        if (elapsed <= 0) {
            elapsed = 1;
        }
        statisticsLayout.set("nps", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.average_numbers_per_second), new String[]{NUMBER_FORMAT.format((long) (getTask().getMaxValue() / elapsed))}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Body
            bodyTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_body_text), new String[]{NUMBER_FORMAT.format(getTask().getFactors().size())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

            //Time remaining
            statisticsLayout.set("eta", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

            //Update statistics every second
            if (getTask().getElapsedTime() - statisticsMap.get(getTask()).lastUpdateTime >= 1000) {

                //Numbers per second
                final long currentValue = getTask().getCurrentValue();
                statisticsMap.get(getTask()).finalNumbersPerSecond = currentValue - statisticsMap.get(getTask()).lastCurrentValue;
                statisticsLayout.set("nps", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));
                statisticsMap.get(getTask()).lastCurrentValue = currentValue;

                statisticsMap.get(getTask()).lastUpdateTime = getTask().getElapsedTime();
            }

            //Update recyclerView
            if (lastAdapterSize != adapter.getItemCount()) {
                try {
                    adapter.notifyItemRangeInserted(lastAdapterSize, adapter.getItemCount() - lastAdapterSize);
                } catch (IllegalStateException e) {
                }
                lastAdapterSize = adapter.getItemCount();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        }
    }

    @Override
    public synchronized FindFactorsTask getTask() {
        return (FindFactorsTask) super.getTask();
    }

    @Override
    public synchronized void setTask(final NativeTaskInterface task) {
        super.setTask(task);
        if (task != null) {
            if (!statisticsMap.containsKey(getTask())) {
                statisticsMap.put(getTask(), new Statistics());
            }
        }
        if (getView() != null) {
            init();
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();

        bodyTextView.setVisibility(View.VISIBLE);
        statisticsLayout.show("eta");

        //Add factors to the adapter
        adapter.setTask(getTask());
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }
}
