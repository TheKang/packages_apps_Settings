package com.android.settings.slim.performance;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SeekBarPreference;

public class Extras extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Paths {

    private static final String USE_DITHERING_PREF = "pref_use_dithering";
    private static final String USE_DITHERING_PROP = "persist.sys.use_dithering";
    private static final String USE_16BPP_ALPHA_PREF = "pref_use_16bpp_alpha";
    private static final String USE_16BPP_ALPHA_PROP = "persist.sys.use_16bpp_alpha";

    private Context mContext;

    private static final String VIBRATION_PREF = "pref_vibration";

    private SeekBarPreference mVibrationPref;

    private ListPreference mUseDitheringPref;

    private CheckBoxPreference mUse16bppAlphaPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.performance_extras);

        mContext = getActivity();

        PreferenceScreen pref = getPreferenceScreen();

        String useDithering = SystemProperties.get(USE_DITHERING_PROP, "1");

        mUseDitheringPref = (ListPreference) pref.findPreference(USE_DITHERING_PREF);
        mUseDitheringPref.setOnPreferenceChangeListener(this);
        mUseDitheringPref.setValue(useDithering);
        mUseDitheringPref.setSummary(mUseDitheringPref.getEntry());

        mUse16bppAlphaPref = (CheckBoxPreference) pref.findPreference(USE_16BPP_ALPHA_PREF);
        String use16bppAlpha = SystemProperties.get(USE_16BPP_ALPHA_PROP, "0");
        mUse16bppAlphaPref.setChecked("1".equals(use16bppAlpha));
        mUse16bppAlphaPref.setOnPreferenceChangeListener(this);

        mVibrationPref = (SeekBarPreference) pref.findPreference(VIBRATION_PREF);
        if (Utils.fileExists(VIBRATION_PATH)) {
            String vibe_init = Utils.readOneLine(VIBRATION_PATH);
            mVibrationPref.setInitValue(Integer.parseInt(vibe_init));
            mVibrationPref.setInterval(1);
            mVibrationPref.setOnPreferenceChangeListener(this);
        } else {
            pref.removePreference(mVibrationPref);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUse16bppAlphaPref) {
            SystemProperties.set(USE_16BPP_ALPHA_PROP,
                    mUse16bppAlphaPref.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mUseDitheringPref) {
            String newVal = (String) newValue;
            int index = mUseDitheringPref.findIndexOfValue(newVal);
            SystemProperties.set(USE_DITHERING_PROP, newVal);
            mUseDitheringPref.setSummary(mUseDitheringPref.getEntries()[index]);
            return true;
        } else if (preference == mVibrationPref) {
            String value = (String) newValue;
            PreferencesProvider.putString(mContext, PREF_VIBRATION_VAL, value);
            Utils.writeValue(VIBRATION_PATH, value);
            Vibrator vib = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(1000);
            return true;
        }
        return false;
    }
}
