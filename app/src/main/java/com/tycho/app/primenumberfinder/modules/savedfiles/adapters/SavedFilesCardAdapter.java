package com.tycho.app.primenumberfinder.modules.savedfiles.adapters;

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

import com.tycho.app.primenumberfinder.modules.savedfiles.SavedFilesCard;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.savedfiles.SavedFilesListActivity;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 11/3/2016
 */

public class SavedFilesCardAdapter extends RecyclerView.Adapter<SavedFilesCardAdapter.ViewHolderSavedFilesCard>{

    /**
     * List of cards in the recycler view
     */
    private final List<SavedFilesCard> cards = new ArrayList<>();

    private final Context context;

    public SavedFilesCardAdapter(final Context context){
        this.context = context;
    }

    @Override
    public ViewHolderSavedFilesCard onCreateViewHolder(ViewGroup parent, int viewType){
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_saved_files, parent, false);
        return new SavedFilesCardAdapter.ViewHolderSavedFilesCard(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolderSavedFilesCard holder, int position){

        //Get the card at this position
        final SavedFilesCard card = cards.get(position);

        holder.recyclerView.setAdapter(card.getFilesListAdapter());
        card.getFilesListAdapter().refresh();
       // holder.listView.setAdapter(card.getAdapterSavedFilesTest());

        final ViewGroup.LayoutParams layoutParams = holder.recyclerView.getLayoutParams();
        //final ViewGroup.LayoutParams layoutParams = holder.listView.getLayoutParams();
        float height = card.getFilesListAdapter().getItemCount() * Utils.dpToPx(context, 32) + Utils.dpToPx(context, 8);
        float maxHeight = Utils.dpToPx(context, 32 * 3) + Utils.dpToPx(context, 8);
        layoutParams.height = (int) Math.min(height, maxHeight);

        if (height == Utils.dpToPx(context, 8)){
            holder.bottomBar.setVisibility(View.GONE);
        }

        holder.title.setText(card.getTitle());
        holder.subTitle.setText(card.getSubTitle());
        holder.headerLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), card.getBackgroundColor()));
    }

    @Override
    public int getItemCount(){
        return cards.size();
    }

    public List<SavedFilesCard> getCards(){
        return cards;
    }

    class ViewHolderSavedFilesCard extends RecyclerView.ViewHolder{

        private final View headerLayout;
        private final TextView title;
        private final TextView subTitle;
        private final RecyclerView recyclerView;
        private final View bottomBar;
        private final Button viewAllButton;

        public ViewHolderSavedFilesCard(final View itemView){
            super(itemView);

            headerLayout = itemView.findViewById(R.id.layout_title);
            headerLayout.setElevation(((CardView) itemView).getCardElevation() * 1.3f);

            title = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subtitle);

            recyclerView = itemView.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setItemAnimator(null);

            bottomBar = itemView.findViewById(R.id.layout_bottomBar);

            viewAllButton = itemView.findViewById(R.id.button);
            viewAllButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    final Intent intent = new Intent(context, SavedFilesListActivity.class);
                    intent.putExtra("directory", cards.get(getAdapterPosition()).getDirectory());
                    context.startActivity(intent);
                }
            });

        }
    }
}
