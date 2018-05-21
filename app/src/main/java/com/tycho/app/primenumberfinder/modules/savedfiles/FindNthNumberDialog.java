package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ui.FormattedEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.awt.font.TextAttribute;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class FindNthNumberDialog extends Dialog {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindNthNumberDialog.class.getSimpleName();

    private final CopyOnWriteArrayList<OnFindClickedListener> listeners = new CopyOnWriteArrayList<>();

    private FormattedEditText numberInput;

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

        //Set up the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.find_nth_number_dialog);

        numberInput = findViewById(R.id.number_input);

        findViewById(R.id.find_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int number = getNumber().intValue();
                if (number > 0 && number <= max) {
                    sendOnFindClicked(number);
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private BigInteger getNumber() {
        return Utils.textToNumber(numberInput.getText().toString());
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
