package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

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
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

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
        holder.number.setText(NUMBER_FORMAT.format(primes.get(position)));
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
                            NUMBER_FORMAT.format(primes.get(getAdapterPosition())),
                            Utils.formatNumberOrdinal(getAdapterPosition() + 1)),
                            Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    final ClipData clip = ClipData.newPlainText(NUMBER_FORMAT.format(primes.get(getAdapterPosition())), String.valueOf(primes.get(getAdapterPosition())));
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Number Copied!", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
