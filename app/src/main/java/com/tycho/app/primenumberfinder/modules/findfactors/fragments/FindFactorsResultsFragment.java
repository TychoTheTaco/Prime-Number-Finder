package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
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
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.io.IOException;

import easytasks.ITask;
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

    private FactorsListAdapter adapter;

    private int lastAdapterSize = 0;

    private final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();
    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Override
    public void onAttach(@NonNull Context context) {
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

        initDefaultState();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskControlBubble.getLeftView().setOnClickListener(v -> new Thread(() -> {

            //Pause the task
            final Task.State state = getTask().getState();
            try {
                getTask().pauseAndWait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Save to file
            final File file;
            if (getTask().getState() == Task.State.STOPPED && getTask().isSaved()){
                // Task is stopped and saved already, load saved file
                file = FileManager.buildFile(getTask());
            }else{
                // Task has not finished or is not saved, saved to temp file
                file = new File(getContext().getCacheDir() + File.separator + "factors");
                try {
                    getTask().saveToFile(file);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            //Restore task state
            if (state == Task.State.RUNNING) {
                getTask().resume();
            }

            final Intent intent = new Intent(getActivity(), DisplayFactorsActivity.class);
            intent.putExtra("filePath", file.getAbsolutePath());
            requireActivity().startActivity(intent);
        }).start());

        // Set up save button
        taskControlBubble.getRightView().setOnClickListener((v)->{if (getTask() instanceof Savable) Utils.save((Savable) getTask(), getActivity(), false);});
    }

    @Override
    protected void postDefaults() {
        super.postDefaults();

        //Subtitle
        subtitleTextView.setText(Utils.formatSpannable(subtitleStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, new boolean[]{true}, ContextCompat.getColor(getActivity(), R.color.orange_dark), getContext()));
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

        taskControlBubble.showRight(true);
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Body
            bodyTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_body_text), new String[]{NUMBER_FORMAT.format(getTask().getFactors().size())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

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
    public synchronized void setTask(final ITask task) {
        super.setTask(task);
        if (getView() != null) {
            initDefaultState();
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();

        bodyTextView.setVisibility(View.VISIBLE);

        //Add factors to the adapter
        adapter.setTask(getTask());
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }
}
