package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tycho.app.primenumberfinder.modules.findprimes.adapters.BufferedPrimesAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import easytasks.ITask;
import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

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
    private BufferedPrimesAdapter adapter = new BufferedPrimesAdapter(100);

    private LinearLayoutManager linearLayoutManager;
    private final CustomScrollListener scrollListener = new CustomScrollListener();

    private FileManager.PrimesFile primesFile;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        linearLayoutManager = new LinearLayoutManager(context);
    }

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
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(scrollListener);

        initDefaultState();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskControlBubble.getLeftView().setOnClickListener(v -> {

            // Make sure task has stopped
            if (getTask().getState() != Task.State.STOPPED){
                Toast.makeText(getContext(), "Task must be finished!", Toast.LENGTH_SHORT).show();
                return;
            }

            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Loading...");
            progressDialog.show();

            new Thread(() -> {

                final File file = new File(getTask().getCacheDirectory() + File.separator + "primes");
                getTask().saveToFile(file);

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
            if (getTask().getSearchOptions().getSearchMethod() == BRUTE_FORCE) {
                bodyTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.find_primes_body_text), new String[]{NUMBER_FORMAT.format(getTask().getPrimeCount())}, getTextHighlight()));
            }
        }
    }

    @Override
    public synchronized FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    private final Object LOCK = new Object();
    private boolean cancel = false;

    private final TaskListener taskListener = new TaskAdapter(){
        @Override
        public void onTaskStopped(ITask task) {
            // Save to temporary file
            final File file = new File(getTask().getCacheDirectory() + File.separator + "primes");
            getTask().saveToFile(file);

            // Load items into adapter
            try {
                primesFile = new FileManager.PrimesFile(file);
                synchronized (LOCK){
                    final List<Long> numbers = primesFile.readNumbers(0, 1000);
                    if (recyclerView != null){
                        recyclerView.post(() -> {
                            adapter.getPrimes().addAll(numbers);
                            adapter.notifyItemRangeInserted(0, adapter.getItemCount());
                        });
                    }else{
                        adapter.getPrimes().addAll(numbers);
                    }
                    cancel = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public synchronized void setTask(final ITask task) {
        if (getTask() != null){
            getTask().removeTaskListener(taskListener);
        }
        super.setTask(task);
        if (recyclerView != null){
            recyclerView.post(() -> {
                adapter.getPrimes().clear();
                adapter.notifyDataSetChanged();
            });
        }else{
            adapter.getPrimes().clear();
        }
        if (getTask() != null) {
            task.addTaskListener(taskListener);
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

    private class CustomScrollListener extends RecyclerView.OnScrollListener {

        private int totalItemCount, lastVisibleItem, visibleThreshold = 0;

        private final int INCREMENT = 250;
        private int firstItemIndex;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            if (totalItemCount == 0){
                return;
            }

            if (totalItemCount - 1 <= (lastVisibleItem + visibleThreshold)) {
                loadBelow();
            } else if (linearLayoutManager.findFirstVisibleItemPosition() <= visibleThreshold) {
                if (firstItemIndex > 0) {
                    loadAbove();
                }
            }
        }

        private void loadAbove() {
            synchronized (LOCK){
                if (cancel){
                    cancel = false;
                    return;
                }

                //Try to read new numbers
                final List<Long> numbers = new ArrayList<>();
                try{
                    numbers.addAll(primesFile.readNumbers(firstItemIndex - INCREMENT, INCREMENT));
                }catch (IOException e){
                    e.printStackTrace();
                }

                if (numbers.size() > 0){
                    //Remove items from end
                    adapter.getPrimes().subList(adapter.getItemCount() - INCREMENT, adapter.getItemCount() - 1).clear();
                    recyclerView.post(() -> adapter.notifyItemRangeRemoved(adapter.getPrimes().size(), INCREMENT));

                    //Add items to beginning
                    for (int i = numbers.size() - 1; i >= 0; i--) {
                        adapter.getPrimes().add(0, numbers.get(i));
                    }
                    recyclerView.post(() -> adapter.notifyItemRangeInserted(0, numbers.size()));
                    firstItemIndex -= numbers.size();
                }
            }
        }

        private void loadBelow() {
            synchronized (LOCK){
                if (cancel){
                    cancel = false;
                    return;
                }

                //Try to read new items
                final List<Long> numbers = new ArrayList<>();
                try{
                    numbers.addAll(primesFile.readNumbers(firstItemIndex + totalItemCount, INCREMENT));
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (numbers.size() > 0){
                    final boolean endOfFile = numbers.size() < INCREMENT;

                    if (!endOfFile) {
                        //Remove items from beginning
                        adapter.getPrimes().subList(0, INCREMENT).clear();
                        recyclerView.post(() -> adapter.notifyItemRangeRemoved(0, INCREMENT));
                    }

                    //Add items to end
                    adapter.getPrimes().addAll(numbers);
                    recyclerView.post(() -> adapter.notifyItemRangeInserted(adapter.getItemCount(), numbers.size()));

                    if (!endOfFile) {
                        firstItemIndex += numbers.size();
                    }
                }
            }

        }
    }
}
