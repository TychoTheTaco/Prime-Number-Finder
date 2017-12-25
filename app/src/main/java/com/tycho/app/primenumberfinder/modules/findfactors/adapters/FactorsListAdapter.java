package com.tycho.app.primenumberfinder.modules.findfactors.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 10/26/2016
 */

public class FactorsListAdapter extends RecyclerView.Adapter<FactorsListAdapter.ViewHolderNumberList>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FactorsListAdapter";

    private final List<Long> factors = new ArrayList<>();

    private long number;

    public FactorsListAdapter(){
        this(-1);
    }

    public FactorsListAdapter(final long number){
        this.number = number;
    }

    @Override
    public ViewHolderNumberList onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.factors_item, parent, false);
        return new ViewHolderNumberList(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderNumberList holder, int position){

        //If the ending number is not provided, we assume the last item in the list is the ending number
        if (number == -1){
            holder.factor0.setText(NumberFormat.getInstance(Locale.getDefault()).format(factors.get(position)));
            holder.factor1.setText(NumberFormat.getInstance(Locale.getDefault()).format(factors.get(factors.size() - position - 1)));
        }else{
            holder.factor0.setText(NumberFormat.getInstance(Locale.getDefault()).format(factors.get(position)));
            holder.factor1.setText(NumberFormat.getInstance(Locale.getDefault()).format(number / factors.get(position)));
        }
    }

    @Override
    public int getItemCount(){
        return factors.size();
    }

    public List<Long> getFactors(){
        return factors;
    }

    public void setNumber(final long number){
        this.number = number;
    }

    class ViewHolderNumberList extends RecyclerView.ViewHolder{

        private final TextView factor0;
        private final TextView factor1;

        ViewHolderNumberList(final View view){
            super(view);
            factor0 = view.findViewById(R.id.factor0);
            factor1 = view.findViewById(R.id.factor1);
        }
    }
}
