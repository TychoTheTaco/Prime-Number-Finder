package com.tycho.app.primenumberfinder.modules.findprimes.adapters;

import android.animation.Animator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
 * Date Created: 5/12/2017
 */

public class PrimesAdapter extends RecyclerView.Adapter<PrimesAdapter.ViewHolder> {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = PrimesAdapter.class.getSimpleName();

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

    private int highlightedPosition = -1;
    private int lastAnimatedPosition = -1;

    public PrimesAdapter(final Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.primes_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.number.setText(NUMBER_FORMAT.format(primes.get(position)));

        if (position == highlightedPosition) {
            holder.animate();
        } else {
            //Hide the background
            holder.background.setVisibility(View.GONE);
        }
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

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    public void animate(final int position) {
        highlightedPosition = position;
        notifyItemChanged(position);
    }

    /**
     * View holder containing item views.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView number;
        private final View background;

        private static final long ANIMATION_DURATION_MILLIS = 500;

        ViewHolder(final View view) {
            super(view);
            number = view.findViewById(R.id.number);
            background = view.findViewById(R.id.background);

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

        private void animate() {
            if (getAdapterPosition() != lastAnimatedPosition) {
                itemView.post(new Runnable() {
                    @Override
                    public void run() {

                        //Make sure the background has the correct size
                        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) background.getLayoutParams();
                        layoutParams.height = itemView.getHeight();
                        background.setLayoutParams(layoutParams);

                        //Find the center of the view
                        int centerX = itemView.getWidth() / 2;
                        int centerY = itemView.getHeight() / 2;

                        //Get the two side lengths for calculating the end radius
                        double sideOne = itemView.getWidth() - centerX;
                        double sideTwo = itemView.getHeight() - centerY;

                        //Calculate start and end radius
                        float startRadius = 0;
                        float endRadius = (float) Math.hypot(sideOne, sideTwo);

                        //Make sure the view is visible before we start animating it
                        background.setVisibility(View.VISIBLE);
                        background.setAlpha(1);

                        //Create and start the animation
                        final Animator animator = ViewAnimationUtils.createCircularReveal(background, centerX, centerY, startRadius, endRadius);
                        animator.setDuration(ANIMATION_DURATION_MILLIS);
                        animator.start();

                        //Create and start the fade out animation
                        background.animate().alpha(0.35f).setDuration(ANIMATION_DURATION_MILLIS).start();

                        lastAnimatedPosition = getAdapterPosition();
                    }
                });
            } else {
                itemView.post(new Runnable() {
                    @Override
                    public void run() {

                        //Make sure the background has the correct size
                        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) background.getLayoutParams();
                        layoutParams.height = itemView.getHeight();
                        background.setLayoutParams(layoutParams);

                        //Make sure the view is visible and set the alpha value
                        background.setVisibility(View.VISIBLE);
                        background.setAlpha(0.35f);
                    }
                });
            }
        }
    }
}
