package com.tycho.app.primenumberfinder.modules.gcf;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

public class GCFListAdapter extends RecyclerView.Adapter<GCFListAdapter.ViewHolderNumberList>{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = GCFListAdapter.class.getSimpleName();

    /**
     * List of factors in this adapter.
     */
    private List<Long> numbers = new ArrayList<>();

    private long gcf;

    private final Context context;

    private final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    public GCFListAdapter(final Context context){
        this.context = context;
    }

    @Override
    public ViewHolderNumberList onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gcf_list_item, parent, false);
        return new ViewHolderNumberList(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderNumberList holder, int position){
        holder.number.setText(NUMBER_FORMAT.format(numbers.get(position)));
        holder.gcf.setText(NUMBER_FORMAT.format(gcf));
        holder.factor.setText(NUMBER_FORMAT.format(numbers.get(position) / gcf));
    }

    @Override
    public int getItemCount(){
        return numbers.size();
    }

    public void set(final List<Long> numbers, final long gcf){
        this.gcf = gcf;
        this.numbers = numbers;
        notifyDataSetChanged();
    }

    class ViewHolderNumberList extends RecyclerView.ViewHolder{

        private final TextView number;
        private final TextView gcf;
        private final TextView factor;

        ViewHolderNumberList(final View view){
            super(view);
            number = view.findViewById(R.id.number);
            gcf = view.findViewById(R.id.gcf);
            factor = view.findViewById(R.id.factor);
        }
    }
}
