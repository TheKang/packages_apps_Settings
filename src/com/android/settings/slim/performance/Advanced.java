package com.android.settings.slim.performance;

import android.os.Bundle;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Advanced extends SettingsPreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.performance_advanced_settings);

        if (!Utils.governorControlExists()) {
            getPreferenceScreen().removePreference(findPreference("governor_control"));
        }

        if (!Utils.isGammaControlSupported()) {
            getPreferenceScreen().removePreference(findPreference("gamma_control"));
        }

        if (!Utils.voltageFileExists()) {
            getPreferenceScreen().removePreference(findPreference("voltage_control"));
        }
    }
}
