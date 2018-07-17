package com.tycho.app.primenumberfinder.modules.lcm.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.ui.ValidEditText;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 10/26/2016
 */

public class NumbersListAdapter extends RecyclerView.Adapter<NumbersListAdapter.ViewHolder>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = NumbersListAdapter.class.getSimpleName();

    /**
     * List of numbers in this adapter.
     */
    private List<Long> numbers = new ArrayList<>();

    private final Context context;

    public NumbersListAdapter(final Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lcm_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == getItemCount() - 1){
            holder.input.setBackgroundTintList(Utils.generateColorStateList(
                    new int[]{},
                    new int[]{Color.YELLOW}
            ));
            holder.input.setHint("Add");
        }else{
            holder.input.setBackgroundTintList(null);
            holder.input.setHint("");
        }
    }

    @Override
    public int getItemCount(){
        return numbers.size();
    }

    public List<Long> getNumbers(){
        return numbers;
    }

    public List<Long> getValidNumbers(){
        final List<Long> numbers = new ArrayList<>();
        for (Long l : this.numbers){
            if (Validator.isValidLCMInput(l)) numbers.add(l);
        }
        return numbers;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final ValidEditText input;

        ViewHolder(final View view){
            super(view);
            input = view.findViewById(R.id.input);
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    final BigInteger number = getInput();
                    input.setValid(Validator.isValidFactorInput(number));
                    if (input.isValid()){
                        numbers.set(getAdapterPosition(), number.longValue());
                    }
                }
            });

            input.addOnTouchListener((v, event) -> {
                if (getAdapterPosition() == getItemCount() - 1){
                    numbers.add(0L);
                    notifyDataSetChanged();
                }
                return false;
            });
        }

        private BigInteger getInput(){
            return Utils.textToNumber(input.getText().toString());
        }
    }
}
