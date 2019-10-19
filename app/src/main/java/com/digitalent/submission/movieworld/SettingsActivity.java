package com.digitalent.submission.movieworld;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.digitalent.submission.movieworld.util.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.action_settings));
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class SettingsFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings);

            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

            Preference preference = findPreference(getString(R.string.key_pref_language));

            if (preference != null) {
                String language = sharedPreferences.getString(preference.getKey(), LocaleHelper.getLanguage(getContext()));
                setPreferenceSummary(preference, language);
                preference.setOnPreferenceChangeListener(this);
            }
        }

        private void setPreferenceSummary(Preference preference, String value) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(value);
                if (index >= 0) {
                    listPreference.setSummary(listPreference.getEntries()[index]);
                }
            } else {
                preference.setSummary(value);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference.getKey().equals(getString(R.string.key_pref_language))) {
                setPreferenceSummary(preference, (String) newValue);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().recreate();
                        }
                    }
                }, 1);
            }

            return true;
        }
    }
}
