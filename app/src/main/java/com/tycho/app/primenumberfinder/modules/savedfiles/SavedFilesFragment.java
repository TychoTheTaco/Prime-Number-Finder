
package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.utils.FileType;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesCardAdapter;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.VerticalItemDecoration;

import java.util.Iterator;

public class SavedFilesFragment extends Fragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "SavedFilesFragment";

    /**
     * All views
     */
    private RecyclerView recyclerViewCards;
    private TextView textViewNoFiles;

    /**
     * The adapter for {@link #recyclerViewCards}.
     */
    private SavedFilesCardAdapter cardAdapter;

    private SavedFilesCard[] cards;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cards = new SavedFilesCard[]{
                new SavedFilesCard(getActivity(), "primeNumbers", "Prime numbers", "subTitle", R.color.purple, FileType.PRIMES),
                new SavedFilesCard(getActivity(), "factors", "Factors", "subTitle", R.color.orange, FileType.FACTORS),
                new SavedFilesCard(getActivity(), "factorTree", "Factor trees", "subTitle", R.color.green, FileType.TREE)
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        final View rootView = inflater.inflate(R.layout.fragment_saved_files, viewGroup, false);

        textViewNoFiles = rootView.findViewById(R.id.textView_no_files);

        cardAdapter = new SavedFilesCardAdapter(getActivity());

        //RecyclerView
        recyclerViewCards = rootView.findViewById(R.id.recyclerView_savedFiles);
        recyclerViewCards.setHasFixedSize(true);
        recyclerViewCards.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewCards.addItemDecoration(new VerticalItemDecoration(24));
        recyclerViewCards.setAdapter(cardAdapter);
        recyclerViewCards.setItemAnimator(null);

        update();

        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            update();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void update(){

        //Remove all cards
        final Iterator<SavedFilesCard> iterator = cardAdapter.getCards().iterator();
        while (iterator.hasNext()){
            iterator.next().getSavedFilesAdapter().refresh();
            iterator.remove();
        }

        for (SavedFilesCard card : cards){
            card.getSavedFilesAdapter().refresh();
            if (!cardAdapter.getCards().contains(card) && card.getSavedFilesAdapter().getItemCount() > 0){
                cardAdapter.getCards().add(card);
            }
        }
        cardAdapter.notifyDataSetChanged();

        textViewNoFiles.setVisibility(cardAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}