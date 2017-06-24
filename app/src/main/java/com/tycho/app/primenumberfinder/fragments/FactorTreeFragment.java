package com.tycho.app.primenumberfinder.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tycho.app.primenumberfinder.FactorTreeView;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.TaskFragment;
import com.tycho.app.primenumberfinder.TaskListener;
import com.tycho.app.primenumberfinder.tasks.FactorTreeTask;

/**
 * @author Tycho Bellers
 *         Date Created: 3/2/2017
 */

public class FactorTreeFragment extends TaskFragment{

    private EditText editTextInput;

    private FactorTreeView factorTreeView;

    FactorTreeTask factorTreeTask;

    private Button generateFactorTreeButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        //Inflate the view
        final View rootView = inflater.inflate(R.layout.fragment_factor_tree, container, false);

        editTextInput = (EditText) rootView.findViewById(R.id.editText_input_number);
        factorTreeView = (FactorTreeView) rootView.findViewById(R.id.testTree);

        generateFactorTreeButton = (Button) rootView.findViewById(R.id.button_generate_factor_tree);
        generateFactorTreeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                long number = 10;

                try{
                    number = Long.valueOf(editTextInput.getText().toString().trim());
                }catch (Exception e){}

                final FactorTreeTask factorTreeTask = new FactorTreeTask(number);
                factorTreeTask.addTaskListener(new TaskListener(){
                    @Override
                    public void onTaskStarted(){

                    }

                    @Override
                    public void onTaskPaused(){

                    }

                    @Override
                    public void onTaskResumed(){

                    }

                    @Override
                    public void onTaskStopped(){

                    }

                    @Override
                    public void onTaskFinished(){
                        factorTreeView.setTree(factorTreeTask.getFactorTree());
                    }

                    @Override
                    public void onProgressChanged(float percent){

                    }
                });
                new Thread(factorTreeTask).start();
            }
        });

        return rootView;
    }

    @Override
    protected void reset(){

    }

    //@Override
    public void onTaskFinished(){
        factorTreeView.setTree(((FactorTreeTask)getTask()).getFactorTree());
    }
}
