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

public class FragmentB extends Fragment{

    private TextView textView;
    private EditText editText;
    private Button button;

    boolean visible = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        final View rootView = inflater.inflate(R.layout.test_fragment, container, false);

        if (textView != null)
            Log.e("FragmentB", "Text was: " + textView.getText().toString());

        textView = (TextView) rootView.findViewById(R.id.textView);
        editText = (EditText) rootView.findViewById(R.id.editText);
        button = (Button) rootView.findViewById(R.id.button);
        ((TextView)rootView.findViewById(R.id.fragment)).setText("Fragment B");

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.e("FragmentB", "Set text: " + editText.getText());
                textView.setText(editText.getText());
                visible = !visible;
                textView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        });

        Log.e("FragmentB", "Text now: " + textView.getText());

        return rootView;
    }

    public void restoreInstanceState(final Bundle bundle){
        Log.e("FragmentB", "Restoring: " + bundle.getString("text"));
        textView.setText(bundle.getString("text"));
    }

    public Bundle getSavedInstanceState(){
        final Bundle bundle = new Bundle();

        bundle.putString("text", textView.getText().toString());

        Log.e("FragmentB", "Saving: " + textView.getText());

        return bundle;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        Log.e("FragmentB", "Bundle: " + savedInstanceState);

        super.onViewStateRestored(savedInstanceState);
        try{
            textView.setText(savedInstanceState.getString("text"));
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        Log.e("FragmentB", "Bundle: " + savedInstanceState);
        Log.e("FragmentB", "onViewStateRestored");
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("text", textView.getText().toString());
        Log.e("FragmentB", "onSaveInstanceState");
    }
}
