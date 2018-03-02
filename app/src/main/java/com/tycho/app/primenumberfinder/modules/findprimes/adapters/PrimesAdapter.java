package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.security.PublicKey;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Tycho Bellers
 *         Date Created: 5/12/2017
 */

public class PrimesAdapter extends RecyclerView.Adapter<PrimesAdapter.ViewHolder> {

    /**
     * List of prime numbers in this adapter.
     */
    private final List<Long> primes = new ArrayList<>();

    /**
     * {@linkplain NumberFormat} used for formatting numbers.
     */
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

    /**
     * The context used to display toast messages.
     */
    private final Context context;

    public PrimesAdapter(final Context context){
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.primes_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            holder.number.setText(numberFormat.format(primes.get(position)));
        }catch (IndexOutOfBoundsException e){}
    }

    @Override
    public int getItemCount() {
        return primes.size();
    }

    public void add(final long number){
        this.primes.add(number);
    }

    public List<Long> getPrimes() {
        return primes;
    }

    /**
     * View holder containing item views.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView number;

        ViewHolder(final View view) {
            super(view);
            number = view.findViewById(R.id.number);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, context.getString(R.string.primes_list_toast_message,
                            numberFormat.format(primes.get(getAdapterPosition())),
                            Utils.formatNumberOrdinal(getAdapterPosition() + 1)),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
