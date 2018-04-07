package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.utils.FileManager.EXTENSION;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesResultsFgmnt";

    //Views
    private ViewGroup resultsView;
    private TextView noTaskView;
    private TextView title;
    private TextView subtitleTextView;
    private ProgressBar progressBarInfinite;
    private TextView progress;
    private Button viewAllButton;
    private Button saveButton;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private final String[] subtitleItems = new String[7];
    private final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach()");

        final String[] splitSubtitle = getString(R.string.find_primes_result).split("%\\d\\$.");
        subtitleItems[0] = splitSubtitle[0];
        subtitleItems[2] = splitSubtitle[1];
        subtitleItems[4] = splitSubtitle[2];
        subtitleItems[6] = splitSubtitle[3];
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_results_fragment, container, false);

        Log.d(TAG, "onCreateView()");

        title = rootView.findViewById(R.id.title);
        subtitleTextView = rootView.findViewById(R.id.subtitle);
        progressBarInfinite = rootView.findViewById(R.id.progressBar_infinite);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        viewAllButton = rootView.findViewById(R.id.button_view_all);
        saveButton = rootView.findViewById(R.id.button_save);

        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Loading...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //Pause the task
                        final Task.State state = getTask().getState();
                        getTask().pause(true);

                        final File file;

                        //Check if cached file exists
                        final File cached = new File(FileManager.getInstance().getTaskCacheDirectory(getTask()) + File.separator + "primes");
                        if (cached.exists() && getTask().getState() == Task.State.STOPPED) {
                            file = cached;
                        } else {
                            file = getTask().saveToFile();
                        }

                        //Resume the task
                        if (state == Task.State.RUNNING) {
                            getTask().resume();
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });

                        final Intent intent = new Intent(getActivity(), DisplayPrimesActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
                        intent.putExtra("title", false);
                        getActivity().startActivity(intent);
                    }
                }).start();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Saving...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            FileManager.copy(getTask().saveToFile(), new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "Prime numbers from " + getTask().getStartValue() + " to " + (getTask().getEndValue() == FindPrimesTask.INFINITY ? getTask().getCurrentValue() : getTask().getEndValue()) + EXTENSION));
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Error saving file!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        progressDialog.dismiss();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.successfully_saved_file), Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "onTaskStarted()");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    Log.d(TAG, "onTaskStarted() handler posted");
                    title.setText(getString(R.string.status_searching));
                    progressBarInfinite.setVisibility(View.VISIBLE);
                    subtitleTextView.setText(formatSubtitle());
                    switch (getTask().getSearchOptions().getSearchMethod()) {
                        case BRUTE_FORCE:
                            viewAllButton.setVisibility(View.VISIBLE);
                            break;

                        case SIEVE_OF_ERATOSTHENES:
                            viewAllButton.setVisibility(View.GONE);
                            break;
                    }
                    saveButton.setVisibility(View.GONE);

                    //Update progress
                    if (getTask().getEndValue() != FindPrimesTask.INFINITY) {
                        progress.setText(getString(R.string.task_progress, DECIMAL_FORMAT.format(getTask().getProgress() * 100)));
                    }
                }
            }
        });
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    title.setText(getString(R.string.state_pausing));
                    subtitleTextView.setText(formatSubtitle());
                }

            }
        });
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    title.setText(getString(R.string.status_paused));
                    progressBarInfinite.setVisibility(View.GONE);
                    subtitleTextView.setText(formatSubtitle());
                    switch (getTask().getSearchOptions().getSearchMethod()) {
                        case BRUTE_FORCE:
                            saveButton.setVisibility(View.VISIBLE);
                            break;

                        case SIEVE_OF_ERATOSTHENES:
                            saveButton.setVisibility(View.GONE);
                            break;
                    }

                    //Update progress
                    if (getTask().getEndValue() != FindPrimesTask.INFINITY) {
                        progress.setText(getString(R.string.task_progress, DECIMAL_FORMAT.format(getTask().getProgress() * 100)));
                    }
                }
            }
        });
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
                    title.setText(getString(R.string.state_resuming));
                    subtitleTextView.setText(formatSubtitle());
                }
            }
        });
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        Log.d(TAG, "onTaskResumed()");
        onTaskStarted();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        if (isAdded() && !isDetached() && getTask() != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    title.setText(getString(R.string.status_finished));
                    progressBarInfinite.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    subtitleTextView.setText(formatSubtitle());
                    viewAllButton.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.VISIBLE);
                }
            });
        }

        //FileManager.saveDebugFile(new File(FileManager.getInstance().getSavedPrimesDirectory() +  File.separator + "debug"));
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            if (getTask().getEndValue() != FindPrimesTask.INFINITY) {
                progress.setText(getString(R.string.task_progress, DECIMAL_FORMAT.format(getTask().getProgress() * 100)));
            }

            final String count = NUMBER_FORMAT.format(getTask().getPrimeCount());
            subtitleStringBuilder.replace(subtitleItems[0].length(), subtitleItems[0].length() + subtitleItems[1].length(), count);
            subtitleItems[1] = count;

            subtitleTextView.setText(subtitleStringBuilder);
        }
    }

    private SpannableStringBuilder formatSubtitle() {
        final String count = NUMBER_FORMAT.format(getTask().getPrimeCount());
        final String start = NUMBER_FORMAT.format(getTask().getStartValue());
        String end = NUMBER_FORMAT.format(getTask().getEndValue());
        if (getTask().getEndValue() == FindPrimesTask.INFINITY) {
            end = getString(R.string.infinity_text);
        }

        subtitleItems[1] = count;
        subtitleItems[3] = start;
        subtitleItems[5] = end;
        subtitleStringBuilder.clear();
        for (int i = 0; i < subtitleItems.length; i++) {
            if (i % 2 != 0) {
                subtitleStringBuilder.append(subtitleItems[i], new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0);
                subtitleStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), subtitleStringBuilder.length() - subtitleItems[i].length(), subtitleStringBuilder.length(), 0);
            } else {
                subtitleStringBuilder.append(subtitleItems[i]);
            }
        }
        return subtitleStringBuilder;
    }

    @Override
    public FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    @Override
    public void setTask(final Task task) {
        super.setTask(task);
        if (getView() != null) {
            init();
        }
    }

    private void init() {
        if (getTask() != null) {

            Log.d(TAG, "init()");

            //Make sure view is visible
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

           /* final FindPrimesTask.SearchOptions searchOptions = getTask().getSearchOptions();
            if (searchOptions.getSearchMethod() == FindPrimesTask.SearchMethod.SIEVE_OF_ERATOSTHENES || searchOptions.getThreadCount() > 1) {
                recyclerView.setVisibility(View.GONE);
            } else {
                primesAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(primesAdapter.getItemCount() - 1);
            }*/

            formatSubtitle();

            progress.setVisibility(getTask().getEndValue() == FindPrimesTask.INFINITY ? View.GONE : View.VISIBLE);

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

        } else {
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
