package com.tycho.app.primenumberfinder;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Tycho Bellers
 *         Date Created: 3/8/2017
 */

public class FragmentA extends Fragment{

    private TextView textView;
    private EditText editText;
    private Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        final View rootView = inflater.inflate(R.layout.test_fragment, container, false);

        String text = "";

        if (textView != null)
            text = textView.getText().toString();
            Log.e("FragmentA", "Text was: " + text);

        textView = (TextView) rootView.findViewById(R.id.textView);
        editText = (EditText) rootView.findViewById(R.id.editText);
        button = (Button) rootView.findViewById(R.id.button);
        ((TextView)rootView.findViewById(R.id.fragment)).setText("Fragment A");

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.e("FragmentA", "Set text: " + editText.getText());
                textView.setText(editText.getText());
            }
        });

        textView.setText(text);

        Log.e("FragmentA", "Text now: " + textView.getText());

        return rootView;
    }
}
