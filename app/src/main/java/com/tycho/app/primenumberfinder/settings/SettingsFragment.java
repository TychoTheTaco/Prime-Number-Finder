package com.tycho.app.primenumberfinder.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;

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
            default:
                break;

            case "allowBackgroundThreads":
                PrimeNumberFinder.getPreferenceManager().setAllowBackgroundTasks(((CheckBoxPreference) preference).isChecked());
                break;

            case "allowAnalytics":
                PrimeNumberFinder.getPreferenceManager().setAllowAnalytics(((CheckBoxPreference) preference).isChecked());
                Toast.makeText(getActivity(), "You must restart the app for changes to take effect.", Toast.LENGTH_LONG).show();
                break;
        }
        PrimeNumberFinder.getPreferenceManager().savePreferences();
    }
}
