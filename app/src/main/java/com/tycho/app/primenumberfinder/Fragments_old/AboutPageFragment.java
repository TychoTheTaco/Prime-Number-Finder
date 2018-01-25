package com.tycho.app.primenumberfinder.Fragments_old;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AboutPageFragment extends Fragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "AboutPageFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.about_page_fragment, container, false);

        ((TextView) rootView.findViewById(R.id.app_version)).setText(getString(R.string.app_version_name, PrimeNumberFinder.getVersionName(getActivity())));

        TextView textViewCredits = rootView.findViewById(R.id.credits);
        textViewCredits.setMovementMethod(LinkMovementMethod.getInstance());

        /*((TextView) rootView.findViewById(R.id.temporary)).setText(generateList());*/

        //((CardView) rootView.findViewById(R.id.card)).setCardBackgroundColor(Color.GRAY);

       /* final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.card).findViewById(R.id.recyclerView);

        //The recycler view has a fixed size
        recyclerView.setHasFixedSize(true);

        //Set the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Set the adapter
        recyclerView.setAdapter(new SavedFilesSmallListAdapter(getActivity(), FileType.PRIMES));*/

        //Disable item animations
        //recyclerViewSavedFilesCards.setItemAnimator(null);
		
		return rootView;
	}

	private String generateList(){
	    final StringBuilder stringBuilder = new StringBuilder();

        List<List<String>> list = new ArrayList<>();

        Pattern sectionPattern = Pattern.compile("\\[(.+)\\]");
        Pattern itemPattern = Pattern.compile("\"(.+)\"");
        Matcher matcher;
        Matcher itemMatcher;

        int section = -1;

        String line;
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.icons)));
            while ((line = bufferedReader.readLine()) != null){
                matcher = sectionPattern.matcher(line);
                itemMatcher = itemPattern.matcher(line);
                if (matcher.find()){
                    section++;
                    list.add(new ArrayList<String>());
                }else if (itemMatcher.find()){
                    list.get(section).add(itemMatcher.group(1));
                }else{
                    Log.d(TAG, "String not recognized: " + line);
                }
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        for (List<String> strings : list){
            for (String string : strings){
                stringBuilder.append(string);
                stringBuilder.append("\n");
            }
            stringBuilder.append("\n");
        }

	    return stringBuilder.toString();
    }
}
