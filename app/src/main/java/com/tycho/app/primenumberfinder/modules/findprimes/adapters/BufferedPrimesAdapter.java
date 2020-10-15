package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tycho.app.primenumberfinder.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BufferedPrimesAdapter extends RecyclerView.Adapter<BufferedPrimesAdapter.ViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = BufferedPrimesAdapter.class.getSimpleName();

    /**
     * List of prime numbers in this adapter.
     */
    private final List<Long> primes = new ArrayList<>();

    /**
     * {@linkplain NumberFormat} used for formatting numbers.
     */
    private final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    public BufferedPrimesAdapter(final int bufferSize) {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.primes_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.number.setText(NUMBER_FORMAT.format(primes.get(position)));
    }

    @Override
    public int getItemCount() {
        return primes.size();
    }

    public void add(final long number) {
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
        }

    }
}
