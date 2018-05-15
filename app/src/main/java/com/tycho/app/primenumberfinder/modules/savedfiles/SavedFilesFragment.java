
package com.tycho.app.primenumberfinder.modules.savedfiles;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.VerticalItemDecoration;
import com.tycho.app.primenumberfinder.modules.savedfiles.adapters.SavedFilesCardAdapter;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.util.Iterator;

public class SavedFilesFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = SavedFilesFragment.class.getSimpleName();

    private TextView textViewNoFiles;

    private SavedFilesCardAdapter cardAdapter;

    private SavedFilesCard[] cards;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cards = new SavedFilesCard[]{
                new SavedFilesCard(context, "primeNumbers", "Prime Numbers", R.color.purple, FileManager.getInstance().getSavedPrimesDirectory()),
                new SavedFilesCard(context, "factors", "Factors", R.color.orange, FileManager.getInstance().getSavedFactorsDirectory()),
                new SavedFilesCard(context, "factorTree", "Factor Trees", R.color.green, FileManager.getInstance().getSavedTreesDirectory())
        };
        cardAdapter = new SavedFilesCardAdapter(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_saved_files, container, false);

        textViewNoFiles = rootView.findViewById(R.id.textView_no_files);

        //RecyclerView
        final RecyclerView recyclerViewCards = rootView.findViewById(R.id.recyclerView_savedFiles);
        recyclerViewCards.setHasFixedSize(true);
        recyclerViewCards.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewCards.addItemDecoration(new VerticalItemDecoration((int) Utils.dpToPx(getActivity(), 8)));
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
            iterator.next().getFilesListAdapter().refresh();
            iterator.remove();
        }

        for (SavedFilesCard card : cards){
            card.getFilesListAdapter().refresh();
            if (!cardAdapter.getCards().contains(card) && card.getFilesListAdapter().getItemCount() > 0){
                cardAdapter.getCards().add(card);
            }
        }
        cardAdapter.notifyDataSetChanged();

        textViewNoFiles.setVisibility(cardAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}