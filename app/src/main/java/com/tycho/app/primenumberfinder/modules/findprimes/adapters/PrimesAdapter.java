package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

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

public class PrimesAdapter extends RecyclerView.Adapter<PrimesAdapter.ViewHolder>{

    private final List<Long> listNumbers = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_number, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){

        holder.number.setText(NumberFormat.getInstance(Locale.getDefault()).format(listNumbers.get(position)));

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.e("TAG", NumberFormat.getInstance(Locale.getDefault()).format(listNumbers.get(holder.getAdapterPosition())) + " is the " + NumberFormat.getInstance(Locale.getDefault()).format(holder.getAdapterPosition()) + " prime number.");
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

    protected class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView number;

        public ViewHolder(final View view){
            super(view);
            number = view.findViewById(R.id.number);
        }
    }
}