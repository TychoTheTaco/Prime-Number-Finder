package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * @author Tycho Bellers
 *         Date Created: 5/12/2017
 */

public class PrimesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    private int offset = 0;

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_HEADER = 1;

    private long totalPrimes = 0;

    private int[] range = new int[]{0, 69};

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    public PrimesAdapter(final Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER){
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.primes_list_header, parent, false));
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.primes_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //if (holder instanceof ViewHolder){
            final ViewHolder viewHolder = (ViewHolder) holder;

            if (getItemViewType(0) == VIEW_TYPE_HEADER){
                position--;
            }

            viewHolder.number.setText(NUMBER_FORMAT.format(primes.get(position)));
        /*}else{
            final HeaderViewHolder viewHolder = (HeaderViewHolder) holder;

            //Header text
            viewHolder.text.setText(Utils.formatSpannable(spannableStringBuilder, context.getString(R.string.find_primes_subtitle_result), new String[]{
                    NUMBER_FORMAT.format(totalPrimes),
                    NUMBER_FORMAT.format(range[0]),
                    range[1] == FindPrimesTask.INFINITY ? context.getString(R.string.infinity_text) : NUMBER_FORMAT.format(range[1]),
            }, ContextCompat.getColor(context, R.color.purple_dark)));
        }*/
    }

    @Override
    public int getItemCount() {
        return primes.size();
    }

    /*@Override
    public int getItemCount() {
        return primes.size() + (offset >= 1 ? 0 : 1);
    }*/

    /*@Override
    public int getItemViewType(int position) {
        if (position == 0 && offset == 0){
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_DEFAULT;
    }*/

    public void setRange(final int start, final int end) {
        this.range[0] = start;
        this.range[1] = end;
    }

    public void setTotalPrimes(long totalPrimes) {
        this.totalPrimes = totalPrimes;
    }

    public void add(final long number){
        this.primes.add(number);
    }

    public List<Long> getPrimes() {
        return primes;
    }

    public void setOffset(final int offset){
        this.offset = offset;
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
                            Utils.formatNumberOrdinal(getAdapterPosition() + 1 + offset)),
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

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView text;

        HeaderViewHolder(final View view) {
            super(view);
            text = view.findViewById(R.id.text);
        }
    }
}
