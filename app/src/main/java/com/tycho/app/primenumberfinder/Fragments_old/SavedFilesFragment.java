
package com.tycho.app.primenumberfinder.Fragments_old;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.Adapters_old.CardAdapter;
import com.tycho.app.primenumberfinder.CardViewSavedFiles;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SavedFileType;
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
    private CardAdapter cardAdapter;

    //Override methods

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){

        //Inflate the view
        final View rootView = inflater.inflate(R.layout.fragment_saved_files, viewGroup, false);

        textViewNoFiles = (TextView) rootView.findViewById(R.id.textView_no_files);

        cardAdapter = new CardAdapter(getActivity());

        //RecyclerView
        recyclerViewCards = (RecyclerView) rootView.findViewById(R.id.recyclerView_savedFiles);
        recyclerViewCards.setHasFixedSize(true);
        recyclerViewCards.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewCards.addItemDecoration(new VerticalItemDecoration(24));
        recyclerViewCards.setAdapter(cardAdapter);
        recyclerViewCards.setItemAnimator(null);

        cardAdapter.getCards().add(new CardViewSavedFiles(getActivity(), "primeNumbers", "Prime numbers", "subTitle", R.color.purple, SavedFileType.PRIMES));
        cardAdapter.getCards().add(new CardViewSavedFiles(getActivity(), "factors", "Factors", "subTitle", R.color.orange, SavedFileType.FACTORS));
        cardAdapter.getCards().add(new CardViewSavedFiles(getActivity(), "factorTree", "Factor trees", "subTitle", R.color.green, SavedFileType.FACTOR_TREE));

        final Iterator<CardViewSavedFiles> iterator = cardAdapter.getCards().iterator();
        while (iterator.hasNext()){
            final CardViewSavedFiles card = iterator.next();
            if (card.getAdapterSavedFiles().getFiles().size() == 0){
                iterator.remove();
            }
        }
        if (cardAdapter.getCards().size() == 0){
            textViewNoFiles.setVisibility(View.VISIBLE);
        }else{
            textViewNoFiles.setVisibility(View.GONE);
        }

        return rootView;
    }
}