package com.tycho.app.primenumberfinder.Fragments_old;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.Adapters_old.AdapterSavedFiles;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.SavedFileType;

public class AboutPageFragment extends Fragment{
    /*public static ActionBar actionBar;
    public static Window window;
    public static View background;*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        TextView textViewCredits = (TextView) rootView.findViewById(R.id.credits);
        textViewCredits.setMovementMethod(LinkMovementMethod.getInstance());

        ((CardView) rootView.findViewById(R.id.card)).setCardBackgroundColor(Color.GRAY);

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.card).findViewById(R.id.recyclerView);

        //The recycler view has a fixed size
        recyclerView.setHasFixedSize(true);

        //Set the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Set the adapter
        recyclerView.setAdapter(new AdapterSavedFiles(getActivity(), SavedFileType.PRIMES));

        initFloatingActionButtons();

        //Disable item animations
        //recyclerViewSavedFilesCards.setItemAnimator(null);
		
		return rootView;
	}

    private void initFloatingActionButtons(){
        //((MainActivity) getActivity()).setFloatingActionButtonVisibility(0, View.GONE);
        //((MainActivity) getActivity()).setFloatingActionButtonVisibility(1, View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        //Save actionbar information
        //outState.putCharSequence("actionBarTitle", MainActivity.actionBar.getTitle());
        //outState.putCharSequence("actionBarSubTitle", MainActivity.actionBar.getSubtitle());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null){
            //Set the actionbar title and subtitle
            //MainActivity.actionBar.setTitle(savedInstanceState.getCharSequence("actionBarTitle"));
            //MainActivity.actionBar.setSubtitle(savedInstanceState.getCharSequence("actionBarSubTitle"));
        }
    }
}
