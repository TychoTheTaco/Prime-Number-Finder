package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.PrimesAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;

import easytasks.ITask;
import easytasks.Task;

import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.BRUTE_FORCE;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesResultsFragment.class.getSimpleName();

    //Views
    private TextView subtitleTextView;
    private TextView bodyTextView;

    /**
     * This {@link SpannableStringBuilder} is used to format any text displayed in
     * {@link FindPrimesResultsFragment#subtitleTextView}.
     */
    final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();

    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    private RecyclerView recyclerView;
    private PrimesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_results_fragment, container, false);

        initStandardViews(rootView);

        subtitleTextView = rootView.findViewById(R.id.subtitle);
        subtitleTextView.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        bodyTextView = rootView.findViewById(R.id.text);

        //Set up recycler view
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new PrimesAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        initDefaultState();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskControlBubble.getLeftView().setOnClickListener(v -> {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Loading...");
            progressDialog.show();

            new Thread(() -> {

                //Save to file
                final File file;
                if (getTask().getState() == Task.State.STOPPED && getTask().isSaved()){
                    // Task is stopped and saved already, load saved file
                    file = FileManager.buildFile(getTask());
                }else{
                    // Task has not finished or is not saved, saved to temp file
                    file = new File(getTask().getCacheDirectory() + File.separator + "primes");
                    getTask().saveToFile(file);
                }

                handler.post(progressDialog::dismiss);

                final Intent intent = new Intent(getActivity(), DisplayPrimesActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                intent.putExtra("enableSearch", true);
                getActivity().startActivity(intent);
            }).start();
        });
    }

    @Override
    protected void postDefaults() {
        super.postDefaults();

        //Subtitle
        subtitleTextView.setText(Utils.formatSpannable(subtitleStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                NUMBER_FORMAT.format(getTask().getStartValue()),
                getTask().isEndless() ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
                getTask().getSearchOptions().getSearchMethod() == BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
        }, new boolean[]{
                true,
                !getTask().isEndless(),
                false
        }, getTextHighlight(), getContext()));

        switch (getTask().getSearchOptions().getSearchMethod()) {
            case BRUTE_FORCE:
                taskControlBubble.showLeft(true);
                break;

            case SIEVE_OF_ERATOSTHENES:
                taskControlBubble.hideLeft(true);
                break;
        }
    }

    @Override
    protected void onPostPaused() {
        super.onPostPaused();

        if (getTask().getEndValue() == FindPrimesTask.INFINITY){
            taskControlBubble.showRight(true);
        }
    }

    @Override
    protected void onPostResumed() {
        super.onPostResumed();
        onPostStarted();
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        taskControlBubble.showLeft(true);
        taskControlBubble.showRight(true);

        //Subtitle
        subtitleTextView.setText(Utils.formatSpannable(subtitleStringBuilder, getResources().getQuantityString(R.plurals.find_primes_subtitle_result, getTask().getPrimeCount()), new String[]{
                NUMBER_FORMAT.format(getTask().getPrimeCount()),
                NUMBER_FORMAT.format(getTask().getStartValue()),
                getTask().isEndless() ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
        }, new boolean[]{
                true,
                true,
                !getTask().isEndless()
        }, getTextHighlight(), getContext()));

        //Body
        bodyTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            if (!getTask().isEndless()) {
                progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
                progressBar.setProgress((int) (getTask().getProgress() * 100));
            } else {
                progressBar.setProgress(50);
            }

            //Body
            switch (getTask().getSearchOptions().getSearchMethod()) {
                case BRUTE_FORCE:
                    bodyTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.find_primes_body_text), new String[]{NUMBER_FORMAT.format(getTask().getPrimeCount())}, getTextHighlight()));
                    break;

                case SIEVE_OF_ERATOSTHENES:
                    switch (getTask().getStatus()) {
                        default:
                            bodyTextView.setText("Preparing...");
                            break;

                        case "counting":
                            bodyTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.find_primes_body_text_sieve_counting), new String[]{NUMBER_FORMAT.format(getTask().getPrimeCount())}, getTextHighlight()));
                            break;

                        case "searching":
                            bodyTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.find_primes_body_text_sieve_marking), new String[]{NUMBER_FORMAT.format(getTask().getCurrentFactor())}, getTextHighlight()));
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    public synchronized FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    @Override
    public synchronized void setTask(final ITask task) {
        super.setTask(task);
        if (getTask() != null) {
            if (getView() != null) {
                initDefaultState();
            }
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();
        progress.setVisibility(getTask().isEndless() ? View.GONE : View.VISIBLE);
        bodyTextView.setVisibility(View.VISIBLE);
    }
}
