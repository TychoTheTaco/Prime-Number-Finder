package com.tycho.app.primenumberfinder.Adapters_old;

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

public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.ViewHolderNumberList>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "NumberAdapter";

    private final List<Long> listNumbers = new ArrayList<>();

    @Override
    public ViewHolderNumberList onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_number, parent, false);
        return new ViewHolderNumberList(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderNumberList holder, final int position){
        holder.textViewNumber.setText(NumberFormat.getInstance(Locale.getDefault()).format(listNumbers.get(position)));
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.e(TAG, "Height is " + holder.textViewNumber.getHeight());
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

    protected class ViewHolderNumberList extends RecyclerView.ViewHolder{

        private final TextView textViewNumber;

        public ViewHolderNumberList(final View view){
            super(view);
            textViewNumber = (TextView) view.findViewById(R.id.number);
        }
    }
}
