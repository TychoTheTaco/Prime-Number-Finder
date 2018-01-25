package com.tycho.app.primenumberfinder.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PrimeNumberFinder.getPreferenceManager().getName());
        addPreferencesFromResource(R.xml.preferences);
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
                //PrimeNumberFinder.getPreferenceManager().putBoolean(KEY_ALLOW_BACKGROUND_TASKS, ((CheckBoxPreference) preference).isChecked());
                //PreferenceManagerOld.setAllowBackgroundThreads(((CheckBoxPreference) preference).isChecked());
                break;
        }

        //PreferenceManagerOld.getInstance().saveAll();
        PrimeNumberFinder.getPreferenceManager().savePreferences();
    }
}
