package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class FindNthNumberDialog extends Dialog {

    private final CopyOnWriteArrayList<OnFindClickedListener> listeners = new CopyOnWriteArrayList<>();

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private EditText numberInput;

    private final int max;

    public FindNthNumberDialog(final Context context, final int max) {
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

        numberInput = findViewById(R.id.number_input);
        numberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String formattedText = NUMBER_FORMAT.format(getNumber());
                if (!editable.toString().equals(formattedText)) {
                    numberInput.setText(formattedText);
                }
                numberInput.setSelection(formattedText.length());

            }
        });

        findViewById(R.id.find_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberInput.getText().length() > 0) {
                    final int number = getNumber().intValue();
                    if (number > 0 && number <= max) {
                        sendOnFindClicked(number);
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private BigInteger getNumber() {
        if (numberInput.getText().length() > 0){
            return new BigInteger(numberInput.getText().toString().replace(",", ""));
        }
        return BigInteger.valueOf(-1);
    }

    public interface OnFindClickedListener {
        void onFindClicked(final int number);
    }

    public boolean addListener(final OnFindClickedListener listener) {
        if (!listeners.contains(listener)) {
            return listeners.add(listener);
        }
        return false;
    }

    public boolean removeListener(final OnFindClickedListener listener) {
        return listeners.remove(listener);
    }

    private void sendOnFindClicked(final int number) {
        for (OnFindClickedListener listener : listeners) {
            listener.onFindClicked(number);
        }
    }
}
