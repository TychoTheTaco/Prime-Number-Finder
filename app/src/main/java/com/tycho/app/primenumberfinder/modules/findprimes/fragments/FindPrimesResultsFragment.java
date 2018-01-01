package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

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
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesResultsFragment extends ResultsFragment implements FindPrimesTask.EventListener{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesResultsFgmnt";

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

    private final PrimesAdapter primesAdapter = new PrimesAdapter();

    private int lastAdapterSize = 0;

    private boolean errorOccurred = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_results_fragment, container, false);

        //Set up recycler view
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(primesAdapter);
        recyclerView.setItemAnimator(null);

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
                        final boolean success = FileManager.getInstance().savePrimes(getTask().getPrimes(), file);
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

                        final Intent intent = new Intent(getActivity(), DisplayPrimesActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
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
                        final boolean success = FileManager.getInstance().savePrimes(getTask().getStartValue(), getTask().getCurrentNumber(), getTask().getPrimes());
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

        updateUi();

        return rootView;
    }

    @Override
    public void onProgressChanged(float progress) {
        requestUiUpdate();
    }

    @Override
    public void onPrimeFound(long prime) {
        primesAdapter.getListNumbers().add(prime);
        requestUiUpdate();
    }

    @Override
    public void onErrorOccurred(Object exception) {
        if (exception instanceof OutOfMemoryError){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    errorOccurred = true;
                    Toast.makeText(getActivity(), "Error finding primes!", Toast.LENGTH_LONG).show();
                    subtitle.setText("Could not allocate enough memory to search for primes up to " + getTask().getEndValue() + ". Please try a smaller number.");
                    title.setText("Error");
                    progressBarInfinite.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){

            if (!errorOccurred && isAdded() && !isDetached()){

                //Update task state
                switch (getTask().getState()){
                    case RUNNING:
                        title.setText(getString(R.string.status_searching));
                        progressBarInfinite.setVisibility(View.VISIBLE);
                        saveButton.setVisibility(View.GONE);
                        break;

                    case PAUSED:
                        title.setText(getString(R.string.status_paused));
                        progressBarInfinite.setVisibility(View.GONE);
                        saveButton.setVisibility(View.VISIBLE);
                        break;

                    case STOPPED:
                        progressBarInfinite.setVisibility(View.GONE);
                        title.setText(getString(R.string.status_finished));
                        saveButton.setVisibility(View.VISIBLE);
                        break;
                }

                //Update progress
                if (getTask().getEndValue() != FindPrimesTask.END_VALUE_INFINITY && getTask().getState() != Task.State.STOPPED){
                    progress.setVisibility(View.VISIBLE);
                    progress.setText(getString(R.string.task_progress, decimalFormat.format(getTask().getProgress() * 100)));
                }else{
                    progress.setVisibility(View.GONE);
                }

                //Update recyclerView
                if (lastAdapterSize != primesAdapter.getItemCount()){
                    try {
                        primesAdapter.notifyItemRangeInserted(lastAdapterSize, primesAdapter.getItemCount() - lastAdapterSize);
                    }catch (IllegalStateException e) {}
                    lastAdapterSize = primesAdapter.getItemCount();
                    recyclerView.scrollToPosition(primesAdapter.getItemCount() - 1);
                }

                //Format subtitle
                final String count = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getPrimes().size());
                final String start = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getStartValue());
                String end = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getEndValue());
                if (getTask().getEndValue() == FindPrimesTask.END_VALUE_INFINITY){
                    end = getString(R.string.infinity_text);
                }
                final String string = getString(R.string.find_primes_result);
                final String[] split = string.split("%\\d\\$.");
                final String[] items = {split[0], count, split[1], start, split[2], end, split[3]};
                final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                for (int i = 0; i < items.length; i++){
                    if (i % 2 != 0){
                        spannableStringBuilder.append(items[i], new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0);
                        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.length() - items[i].length(), spannableStringBuilder.length(), 0);
                    }else{
                        spannableStringBuilder.append(items[i]);
                    }
                }
                subtitle.setText(spannableStringBuilder);
            }else{
                subtitle.setText("Could not allocate enough memory to search for primes up to " + getTask().getEndValue() + ". Please try a smaller number.");
            }
        }
    }

    @Override
    public FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    @Override
    public void setTask(final Task task) {

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

        try {
            init();
        }catch (NullPointerException e){}

        updateUi();
    }

    private void init(){
        if (getTask() != null){

            //Make sure view is visible
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            //Add primes to the adapter
            if (!errorOccurred && getTask().getState() !=  Task.State.RUNNING){
                primesAdapter.getListNumbers().clear();
                primesAdapter.getListNumbers().addAll(getTask().getPrimes());
                primesAdapter.notifyItemRangeInserted(0, getTask().getPrimes().size());
                recyclerView.scrollToPosition(primesAdapter.getItemCount() - 1);
            }

        }else{
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
