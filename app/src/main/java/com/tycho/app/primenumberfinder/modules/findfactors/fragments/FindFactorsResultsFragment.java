package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class FindFactorsResultsFragment extends ResultsFragment implements FindFactorsTask.EventListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsResultsFgmnt";

    //Views
    private ViewGroup resultsView;
    private TextView noTaskView;
    private TextView title;
    private TextView subtitle;
    private ProgressBar progressBarInfinite;
    private TextView progress;
    private RecyclerView recyclerView;
    private Button viewAllButton;
    private Button saveButton;

    private final FactorsListAdapter adapter = new FactorsListAdapter();

    private int lastAdapterSize = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_results_fragment, container, false);

        //Set up recycler view
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        final FindFactorsTask task = getTask();
        if (task != null){
            adapter.setNumber(task.getNumber());
        }

        title = rootView.findViewById(R.id.title);
        subtitle = rootView.findViewById(R.id.subtitle);
        progressBarInfinite = rootView.findViewById(R.id.progressBar_infinite);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        viewAllButton = rootView.findViewById(R.id.button_view_all);
        saveButton = rootView.findViewById(R.id.button_save);

        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.loading), Toast.LENGTH_SHORT).show();
                            }
                        });

                        final Task.State state = getTask().getState();
                        getTask().pause();
                        final File file = new File(getActivity().getFilesDir() + File.separator + "temp");
                        final boolean success = FileManager.getInstance().saveFactors(getTask().getFactors(), file);
                        if (!success){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), getString(R.string.general_error), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        if (state == Task.State.RUNNING){
                            getTask().resume();
                        }

                        final Intent intent = new Intent(getActivity(), DisplayFactorsActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
                        intent.putExtra("number", getTask().getNumber());
                        getActivity().startActivity(intent);
                    }
                }).start();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.saving_file), Toast.LENGTH_SHORT).show();
                            }
                        });
                        final boolean success = FileManager.getInstance().saveFactors(getTask().getFactors(), getTask().getNumber());
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

        return rootView;
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();

        final FindFactorsTask task = getTask();
        if (task != null){
            adapter.setNumber(task.getNumber());
        }
    }

    @Override
    public void onFactorFound(long prime) {
        adapter.getFactors().add(prime);
        requestUiUpdate();
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){

            //Update task state
            switch (getTask().getState()){
                case RUNNING:
                    title.setText(getString(R.string.status_searching));
                    progressBarInfinite.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.GONE);

                    //Set progress
                    progress.setVisibility(View.VISIBLE);
                    progress.setText(getString(R.string.task_progress, decimalFormat.format(getTask().getProgress() * 100)));
                    break;

                case PAUSED:
                    title.setText(getString(R.string.status_paused));
                    progressBarInfinite.setVisibility(View.GONE);
                    saveButton.setVisibility(View.VISIBLE);

                    //Set progress
                    progress.setVisibility(View.VISIBLE);
                    progress.setText(getString(R.string.task_progress, decimalFormat.format(getTask().getProgress() * 100)));
                    break;

                case STOPPED:
                    progressBarInfinite.setVisibility(View.GONE);
                    title.setText(getString(R.string.status_finished));
                    saveButton.setVisibility(View.VISIBLE);

                    //Set progress
                    progress.setVisibility(View.GONE);
                    break;
            }

            //Update recyclerView
            if (lastAdapterSize != adapter.getItemCount()){
                try {
                    adapter.notifyItemRangeInserted(lastAdapterSize, adapter.getItemCount() - lastAdapterSize);
                }catch (IllegalStateException e) {}
                lastAdapterSize = adapter.getItemCount();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            //Format subtitle
            final String start = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getNumber());
            final String count = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getFactors().size());
            final String string = getResources().getQuantityString(R.plurals.find_factors_results_description, getTask().getFactors().size());
            final String[] split = string.split("%\\d\\$.");
            final String[] items = {start, split[1], count, split[2]};
            final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            for (int i = 0; i < items.length; i++){
                if (i % 2 == 0){
                    spannableStringBuilder.append(items[i], new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.orange_dark)), 0);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.length() - items[i].length(), spannableStringBuilder.length(), 0);
                }else{
                    spannableStringBuilder.append(items[i]);
                }
            }
            subtitle.setText(spannableStringBuilder);
        }
    }

    @Override
    public FindFactorsTask getTask() {
        return (FindFactorsTask) super.getTask();
    }

    @Override
    public void setTask(Task task) {

        //Remove task listener from previous task
        if (getTask() != null){
            if (!getTask().removeEventListener(this)){
                Log.d(TAG, "Failed to remove event listener!");
            }
        }

        super.setTask(task);

        //Add task listener to new task
        if (getTask() != null){
            getTask().addEventListener(this);
        }

        if (task != null){

            //Make sure view is visible
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            //Add factors to the adapter
            final int count = adapter.getItemCount();
            adapter.getFactors().clear();
            adapter.notifyItemRangeRemoved(0, count);
            adapter.getFactors().addAll(((FindFactorsTask) task).getFactors());
            adapter.setNumber(((FindFactorsTask) task).getNumber());
            final int newSize = ((FindFactorsTask) task).getFactors().size();
            adapter.notifyItemRangeInserted(0, newSize);
            lastAdapterSize = newSize;
            recyclerView.scrollToPosition(newSize - 1);

        }else{
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }

        updateUi();
    }
}