package com.tycho.app.primenumberfinder.modules.lcm.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.ui.NumberInput;
import com.tycho.app.primenumberfinder.utils.Utils;
import com.tycho.app.primenumberfinder.utils.Validator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
    private final List<BigInteger> numbers = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lcm_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == getItemCount() - 1){
            holder.input.setBackgroundTintList(Utils.generateColorStateList(
                    new int[]{},
                    new int[]{ContextCompat.getColor(holder.itemView.getContext(), R.color.list_view_add_new_item_background)}
            ));
            holder.input.setShowRandomHint(false);
            holder.input.setHint("+");
            holder.input.setText("");
        }else{
            holder.input.setShowRandomHint(true);
            holder.input.setNumber(numbers.get(position));
            holder.input.setBackgroundTintList(null);
        }
    }

    @Override
    public int getItemCount(){
        return numbers.size();
    }

    private RecyclerView recyclerView;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public List<BigInteger> getNumbers(){
        return numbers;
    }

    public List<BigInteger> getNonZeroNumbers(){
        final List<BigInteger> numbers = new ArrayList<>();
        for (BigInteger bigInteger : this.numbers){
            if (bigInteger.compareTo(BigInteger.ZERO) != 0){
                numbers.add(bigInteger);
            }
        }
        return numbers;
    }

    public List<Long> getValidNumbers(){
        final List<Long> numbers = new ArrayList<>();
        for (BigInteger i : this.numbers){
            if (Validator.isValidLCMInput(i)) numbers.add(i.longValue());
        }
        return numbers;
    }

    protected boolean isValidNumber(final BigInteger number){
        return true;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final NumberInput input;

        ViewHolder(final View view){
            super(view);
            input = view.findViewById(R.id.number_input);
            input.setClearOnTouch(false);
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
                    input.setValid(isValidNumber(number));
                    numbers.set(getAdapterPosition(), number);
                }
            });

            input.setOnClickListener(v -> {
                if (getAdapterPosition() == getItemCount() - 1){
                    numbers.add(BigInteger.ZERO);
                    notifyItemInserted(getItemCount());
                    notifyItemChanged(getItemCount() - 2);
                    v.getParent().requestLayout();
                    input.requestFocus();
                    recyclerView.scrollToPosition(getItemCount() - 1);
                }
            });
        }

        private BigInteger getInput(){
            return Utils.textToNumber(input.getText().toString());
        }
    }
}
