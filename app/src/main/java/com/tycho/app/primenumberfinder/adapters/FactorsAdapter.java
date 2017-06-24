package com.tycho.app.primenumberfinder.adapters;

import android.content.Context;
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
 *         Date Created: 5/12/2017
 */

public class FactorsAdapter extends RecyclerView.Adapter<FactorsAdapter.ViewHolderNumberList>{

    private final Context context;

    private long number = 1;

    private final List<Long> listNumbers = new ArrayList<>();

    public FactorsAdapter(final Context context/*, final long number*/){
        this.context = context;
        //this.number = number;
    }

    @Override
    public ViewHolderNumberList onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.factors_item, parent, false);
        return new ViewHolderNumberList(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderNumberList holder, final int position){

        holder.factor0.setText(NumberFormat.getInstance(Locale.getDefault()).format(listNumbers.get(position)));
        holder.factor1.setText(NumberFormat.getInstance(Locale.getDefault()).format(number / listNumbers.get(position)));

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.e("TAG", NumberFormat.getInstance(Locale.getDefault()).format(listNumbers.get(position)) + " is the " + NumberFormat.getInstance(Locale.getDefault()).format(position) + " prime number.");
            }
        });
    }

    @Override
    public int getItemCount(){
        return listNumbers.size();
    }

    public List<Long> getListNumbers(){
        return listNumbers;
    }

    public void setNumber(final long number){
        this.number = number;
    }

    protected class ViewHolderNumberList extends RecyclerView.ViewHolder{

        private final TextView factor0;
        private final TextView factor1;

        public ViewHolderNumberList(final View view){
            super(view);
            factor0 = (TextView) view.findViewById(R.id.factor0);
            factor1 = (TextView) view.findViewById(R.id.factor1);
        }
    }
}
