package com.tycho.app.primenumberfinder.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        Preference preference = findPreference(key);
        switch (key){
            case "allowBackgroundThreads":
                PreferenceManager.set(PreferenceManager.Preference.ALLOW_BACKGROUND_TASKS, ((CheckBoxPreference) preference).isChecked());
                //PrimeNumberFinder.getPreferenceManager().setAllowBackgroundTasks(((CheckBoxPreference) preference).isChecked());
                break;

            case "allowAnalytics":
                PreferenceManager.set(PreferenceManager.Preference.ALLOW_ANALYTICS, ((CheckBoxPreference) preference).isChecked());
                Toast.makeText(getActivity(), "You must restart the app for changes to take effect.", Toast.LENGTH_LONG).show();
                break;

            case "quickCopy":
                PreferenceManager.set(PreferenceManager.Preference.QUICK_COPY, ((SwitchPreference) preference).isChecked());
                break;

            case "quickCopyKeepFormatting":
                PreferenceManager.set(PreferenceManager.Preference.QUICK_COPY_KEEP_FORMATTING, ((CheckBoxPreference) preference).isChecked());
                break;

            case "theme":
                PreferenceManager.set(PreferenceManager.Preference.THEME, ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                //TODO: Don't do this immediately
                PrimeNumberFinder.reloadTheme(getActivity());
                break;
        }
    }
}
