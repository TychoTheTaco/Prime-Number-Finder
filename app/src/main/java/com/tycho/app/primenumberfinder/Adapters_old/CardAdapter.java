package com.tycho.app.primenumberfinder.Adapters_old;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.CardViewSavedFiles;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.activities.ActivityListFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 11/3/2016
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolderSavedFilesCard>{

    /**
     * List of cards in the recycler view
     */
    private final List<CardViewSavedFiles> cards = new ArrayList<>();

    private final Context context;

    public CardAdapter(final Context context){
        this.context = context;
    }

    @Override
    public ViewHolderSavedFilesCard onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_saved_files, parent, false);
        //final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_saved_files_test, parent, false);
        return new CardAdapter.ViewHolderSavedFilesCard(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderSavedFilesCard holder, int position){

        //Get the card at this position
        final CardViewSavedFiles card = cards.get(position);

        /*holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerView.setItemAnimator(null);
        holder.recyclerView.setAdapter(card.getAdapterSavedFiles());*/

        holder.recyclerView.setAdapter(card.getAdapterSavedFiles());
        card.getAdapterSavedFiles().sortByDate();
       // holder.listView.setAdapter(card.getAdapterSavedFilesTest());

        holder.button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(context, ActivityListFiles.class);
                intent.putExtra("savedFileType", card.getAdapterSavedFiles().getFileType().getId());
                context.startActivity(intent);
            }
        });

        final ViewGroup.LayoutParams layoutParams = holder.recyclerView.getLayoutParams();
        //final ViewGroup.LayoutParams layoutParams = holder.listView.getLayoutParams();
        float height = card.getAdapterSavedFiles().getItemCount() * PrimeNumberFinder.dpToPx(context, 32) + PrimeNumberFinder.dpToPx(context, 8);
        float maxHeight = PrimeNumberFinder.dpToPx(context, 32 * 3) + PrimeNumberFinder.dpToPx(context, 8);
        layoutParams.height = (int) Math.min(height, maxHeight);

        if (height == PrimeNumberFinder.dpToPx(context, 8)){
            holder.bottomBar.setVisibility(View.GONE);
        }

        holder.title.setText(card.getTitle());
        holder.subTitle.setText(card.getSubTitle());
        //((CardView)holder.itemView).setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), card.getBackgroundColor()));
        holder.layout_title.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), card.getBackgroundColor()));

        float elevation = ((CardView) holder.itemView).getCardElevation();
        holder.layout_title.setElevation(elevation * 1.3f);
    }

    @Override
    public int getItemCount(){
        return cards.size();
    }

    public List<CardViewSavedFiles> getCards(){
        return cards;
    }

    protected class ViewHolderSavedFilesCard extends RecyclerView.ViewHolder{

        private final View layout_title;
        private final TextView title;
        private final TextView subTitle;
        private final RecyclerView recyclerView;
        //private final ListView listView;
        private final View bottomBar;
        private final Button button;

        public ViewHolderSavedFilesCard(final View itemView){
            super(itemView);
            layout_title = itemView.findViewById(R.id.layout_title);
            title = (TextView) itemView.findViewById(R.id.title);
            subTitle = (TextView) itemView.findViewById(R.id.subTitle);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerView);
            //listView = (ListView) itemView.findViewById(R.id.recyclerView);
            bottomBar = itemView.findViewById(R.id.layout_bottomBar);
            button = (Button) itemView.findViewById(R.id.button);

            initRecyclerView();
        }

        private void initRecyclerView(){
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setItemAnimator(null);
        }
    }
}
