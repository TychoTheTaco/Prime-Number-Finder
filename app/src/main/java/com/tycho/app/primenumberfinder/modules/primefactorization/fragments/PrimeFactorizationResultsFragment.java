package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
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
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimeFactorizationActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import easytasks.Task;
import simpletrees.Tree;

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
    private ProgressBar progressBarInfinite;
    private TextView progress;
    private Button saveButton;
    //private Button viewAllButton;

    private TreeView treeView;
    private TreeView treeViewTest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_results_fragment, container, false);

        title = rootView.findViewById(R.id.title);
        subtitle = rootView.findViewById(R.id.subtitle);
        primeFactorization = rootView.findViewById(R.id.prime_factorization);
        //viewAllButton = rootView.findViewById(R.id.button_view_all);
        progressBarInfinite = rootView.findViewById(R.id.progressBar_infinite);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        saveButton = rootView.findViewById(R.id.button_save);
        treeView = rootView.findViewById(R.id.factor_tree);
        treeViewTest = rootView.findViewById(R.id.factor_tree_test);

        final Tree<Long> t = new Tree<>(81L);
        t.addNodeWithChildren(27L)
                .addNode(2L)
                .addNodeWithChildren(9L)
                .addNode(3L)
                .addNode(3L);
        t.addNodeWithChildren(30L)
                .addNode(2L)
                .addNodeWithChildren(15L)
                .addNode(3L)
                .addNode(5L);
        treeView.setTree(t);

        /*final Tree<String> tree = new Tree<>("This");
        tree.addNodeWithChildren("is")
                .addNodeWithChildren("to")
                .addNode("view")
                .addNode("works")
                .addNodeWithChildren("as")
                .addNode("does")
                .addNode(":)");
        tree.addNodeWithChildren("a")
                .addNode("see")
                .addNodeWithChildren("if")
                .addNode("intended.")
                .addNode("I");
        tree.addNodeWithChildren("test")
                .addNode("the")
                .addNodeWithChildren("tree")
                .addNode("really")
                .addNode("hope")
                .addNode("it");*/
        treeViewTest.setTree(t.formatNumbers());

        /*((TreeView) rootView.findViewById(R.id.factor_tree_test)).setTree(tree);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){

                }

                Log.w(TAG, "Setting delayed tree");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((TreeView) rootView.findViewById(R.id.factor_tree_test)).setTree(t);
                    }
                });

            }
        }).start();*/

        /*viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final File file = new File(getActivity().getFilesDir() + File.separator + "temp");
                final boolean success = FileManager.getInstance().saveTree(getTask().getFactorTree(), file);
                if (!success){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.general_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                final Intent intent = new Intent(getActivity(), DisplayPrimeFactorizationActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                intent.putExtra("title", false);
                getActivity().startActivity(intent);
            }
        });*/

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

        return rootView;
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();

        handler.post(new Runnable() {
            @Override
            public void run() {
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

                //viewAllButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();

        handler.post(new Runnable() {
            @Override
            public void run() {

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

                treeView.setTree(getTask().getFactorTree());

                //viewAllButton.setVisibility(View.VISIBLE);
            }
        });
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
                   // viewAllButton.setVisibility(View.GONE);
                    break;

                case PAUSED:
                    title.setText(getString(R.string.status_paused));
                    progressBarInfinite.setVisibility(View.GONE);
                    saveButton.setVisibility(View.VISIBLE);
                   // viewAllButton.setVisibility(View.GONE);
                    break;

                case STOPPED:
                    progressBarInfinite.setVisibility(View.GONE);
                    title.setText(getString(R.string.status_finished));
                    saveButton.setVisibility(View.VISIBLE);
                   // viewAllButton.setVisibility(View.VISIBLE);

                    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
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
                    break;
            }

            //Update progress
            /*if (getTask().getState() != Task.State.STOPPED){
                progress.setVisibility(View.VISIBLE);
                progress.setText(getString(R.string.task_progress, decimalFormat.format(getTask().getProgress() * 100)));
            }else{
                progress.setVisibility(View.GONE);
            }*/
            progress.setVisibility(View.GONE);

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
    }

    @Override
    public PrimeFactorizationTask getTask() {
        return (PrimeFactorizationTask) super.getTask();
    }

    @Override
    public void setTask(Task task) {
        super.setTask(task);

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

        }else{
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
