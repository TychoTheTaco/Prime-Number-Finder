package com.tycho.app.primenumberfinder.modules.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class AboutPageFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "AboutPageFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.about_page_fragment, container, false);

		//Set version
        ((TextView) rootView.findViewById(R.id.app_version)).setText(getString(R.string.app_version_name, PrimeNumberFinder.getVersionName(getActivity())));
        ((TextView) rootView.findViewById(R.id.new_version_name)).setText("New in version " + PrimeNumberFinder.getVersionName(getActivity()));
        rootView.findViewById(R.id.contact_developer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","tycho.developer@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Prime Number Finder Feedback (Version " + PrimeNumberFinder.getVersionName(getActivity()) + ")");
                startActivity(Intent.createChooser(intent, "Send email..."));
            }
        });

        //Set changelog data
        ((TextView) rootView.findViewById(R.id.changelog)).setText(getCurrentUpdate());
        rootView.findViewById(R.id.view_changelog_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), ChangelogActivity.class);
                getActivity().startActivity(intent);
            }
        });

        //Set credits
        TextView textViewCredits = rootView.findViewById(R.id.credits);
        textViewCredits.setMovementMethod(LinkMovementMethod.getInstance());

		return rootView;
	}

	private String getCurrentUpdate(){
        final Pattern versionPattern = Pattern.compile("(\\d+\\/\\d+\\/\\d+).+?(\\d+\\..+)");
        final Pattern devVersionPattern = Pattern.compile("(\\?+\\/\\?+\\/\\d+).+?(\\d+\\..+)");
        final Pattern itemPattern = Pattern.compile(".+");
        Matcher matcher;
        Matcher itemMatcher;

        final List<String> notes = new ArrayList<>();

        String line;
        try{
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.changelog)));
            while ((line = bufferedReader.readLine()) != null){
                matcher = versionPattern.matcher(line);
                itemMatcher = itemPattern.matcher(line);
                if (matcher.find() || devVersionPattern.matcher(line).find()){
                    notes.clear();
                }else if (itemMatcher.find()){
                    notes.add(itemMatcher.group());
                }else{
                    Log.d(TAG, "String not recognized: " + line);
                }
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String string : notes){
            stringBuilder.append(string);
            stringBuilder.append(System.lineSeparator());
        }

        if (stringBuilder.length() > 0){
            return stringBuilder.substring(0, stringBuilder.length() - 1);
        }else{
            return "";
        }
    }
}
