package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;

import java.util.concurrent.CopyOnWriteArrayList;

public class FindNthNumberDialog extends Dialog {

    private final CopyOnWriteArrayList<OnFindClickedListener> listeners = new CopyOnWriteArrayList<>();

    private final int max;

    public FindNthNumberDialog(final Context context, final int max){
        super(context);
        this.max = max;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Allow the dialog to be canceled by tapping out of its bounds
        setCancelable(true);

        //Inflate the custom layout
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        final View rootView = layoutInflater.inflate(R.layout.find_nth_number_dialog, null);

        //Set up the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(rootView);

        final EditText numberInput = findViewById(R.id.number_input);

        findViewById(R.id.find_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberInput.getText().length() > 0){
                    final int number = Integer.valueOf(numberInput.getText().toString());
                    if (number > 0 && number <= max){
                        sendOnFindClicked(number);
                        dismiss();
                    }else{
                        Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public interface OnFindClickedListener {
        void onFindClicked(final int number);
    }

    public boolean addListener(final OnFindClickedListener listener){
        if (!listeners.contains(listener)){
            return listeners.add(listener);
        }
        return false;
    }

    public boolean removeListener(final OnFindClickedListener listener){
        return listeners.remove(listener);
    }

    private void sendOnFindClicked(final int number){
        for (OnFindClickedListener listener : listeners){
            listener.onFindClicked(number);
        }
    }
}
