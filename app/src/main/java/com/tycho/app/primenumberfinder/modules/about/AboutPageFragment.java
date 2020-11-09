package com.tycho.app.primenumberfinder.modules.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;

public class AboutPageFragment extends Fragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = AboutPageFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.about_page_fragment, container, false);

        if (PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 0){
            rootView.setBackgroundResource(R.drawable.scroll_background);
        }else{
            rootView.setBackgroundResource(R.drawable.peak);
        }

        //Set version
        ((TextView) rootView.findViewById(R.id.app_version)).setText(getString(R.string.app_version_name, PrimeNumberFinder.getVersionName(getActivity())));
        ((TextView) rootView.findViewById(R.id.new_version_name)).setText("New in version " + PrimeNumberFinder.getVersionName(getActivity()));

        // Email button
        rootView.findViewById(R.id.contact_developer_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","tycho.developer@gmail.com", null));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback (Version " + PrimeNumberFinder.getVersionName(getActivity()) + ")");
            startActivity(Intent.createChooser(intent, "Send email..."));
        });

        // Github button
        rootView.findViewById(R.id.github_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TychoTheTaco"));
            startActivity(intent);

            // Analytics
            FirebaseAnalytics.getInstance(getContext()).logEvent("view_github_page", null);
        });

        //Read changelog
        final Changelog changelog = Changelog.readChangelog(getResources().openRawResource(R.raw.changelog));

        //Set changelog data
        ((TextView) rootView.findViewById(R.id.changelog)).setText(changelog.getLatestRelease().concatenate());
        rootView.findViewById(R.id.view_changelog_button).setOnClickListener(v -> {
            final Intent intent = new Intent(getActivity(), ChangelogActivity.class);
            getActivity().startActivity(intent);
        });

        //Set credits
        TextView textViewCredits = rootView.findViewById(R.id.credits);
        textViewCredits.setMovementMethod(LinkMovementMethod.getInstance());

        return rootView;
    }
}
