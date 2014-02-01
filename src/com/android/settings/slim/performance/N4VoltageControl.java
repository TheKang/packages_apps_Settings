package com.android.settings.slim.performance;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SeekBarPreference;

public class N4VoltageControl extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Paths {

    private static final String TAG = "Performance-N4VoltageControl";

    private static final String PREF_UV_ENABLED = "uv_enabled";
    private static final String PREF_UV_BOOST = "uv_boost";
    private static final String PREF_UV_LOWER_UV = "uv_lower_uv";
    private static final String PREF_UV_HIGHER_UV = "uv_higher_uv";
    private static final String PREF_UV_HIGHER_KHZ_THRES = "uv_higher_khz_thres";
    private static final String PREF_UV_APPLY = "uv_apply";
    private static final String PREF_UV_CPU_TABLE = "uv_cpu_table";
    private static final String PREF_UV_RESET = "uv_reset";
    private static final String PREF_UV_APPLY_BOOT = "uv_apply_boot";

    private CheckBoxPreference mUVEnabled;
    private CheckBoxPreference mUVBoost;
    private SeekBarPreference mUVLowerUV;
    private SeekBarPreference mUVHigherUV;
    private EditTextPreference mUVHigherKhzThres;
    private Preference mUVApply;
    private Preference mUVCpuTable;
    private Preference mUVReset;
    private CheckBoxPreference mUVApplyBoot;

    private Context mContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.voltage_control_n4);

        mContext = getActivity();

        mUVEnabled = (CheckBoxPreference) findPreference(PREF_UV_ENABLED);
        mUVEnabled.setChecked(PreferencesProvider.getBoolean(mContext, PREF_UV_ENABLED, false));
        mUVEnabled.setOnPreferenceChangeListener(this);

        mUVBoost = (CheckBoxPreference) findPreference(PREF_UV_BOOST);
        mUVBoost.setChecked(PreferencesProvider.getBoolean(mContext, PREF_UV_BOOST, false));
        mUVBoost.setOnPreferenceChangeListener(this);

        int lowerUv = Integer.parseInt(Utils.readOneLine(LOWER_UV_FILE));
        int higherUv = Integer.parseInt(Utils.readOneLine(HIGHER_UV_FILE));

        mUVLowerUV = (SeekBarPreference) findPreference(PREF_UV_LOWER_UV);
        mUVLowerUV.setInterval(12500);
        mUVLowerUV.setDefault(0);
        mUVLowerUV.setMax(150000);
        mUVLowerUV.setInitValue(lowerUv);
        mUVLowerUV.setOnPreferenceChangeListener(this);

        mUVHigherUV = (SeekBarPreference) findPreference(PREF_UV_HIGHER_UV);
        mUVHigherUV.setInterval(12500);
        mUVHigherUV.setDefault(0);
        mUVHigherUV.setMax(150000);
        mUVHigherUV.setInitValue(higherUv);
        mUVHigherUV.setOnPreferenceChangeListener(this);

        mUVHigherKhzThres = (EditTextPreference) findPreference(PREF_UV_HIGHER_KHZ_THRES);
        mUVHigherKhzThres.setOnPreferenceChangeListener(this);

        mUVApply = (Preference) findPreference(PREF_UV_APPLY);

        mUVCpuTable = (Preference) findPreference(PREF_UV_CPU_TABLE);

        mUVReset = (Preference) findPreference(PREF_UV_RESET);

        mUVApplyBoot = (CheckBoxPreference) findPreference(PREF_UV_APPLY_BOOT);
        mUVApplyBoot.setChecked(PreferencesProvider.getBoolean(mContext, PREF_UV_APPLY_BOOT, false));
        mUVApplyBoot.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUVEnabled) {
            PreferencesProvider.putBoolean(mContext, PREF_UV_ENABLED, mUVEnabled.isChecked());
            return true;
        } else if (preference == mUVBoost) {
            PreferencesProvider.putBoolean(mContext, PREF_UV_BOOST, mUVBoost.isChecked());
            Utils.writeValue(UV_BOOST_FILE, mUVBoost.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mUVLowerUV) {
            PreferencesProvider.putString(mContext, PREF_UV_LOWER_UV, (String) newValue);
            Log.d(TAG, "Lower Value: " + (String) newValue);
            return true;
        } else if (preference == mUVHigherUV) {
            PreferencesProvider.putString(mContext, PREF_UV_HIGHER_UV, (String) newValue);
            Log.d(TAG, "Higher Value: " + (String) newValue);
            return true;
        } else if (preference == mUVHigherKhzThres) {
            PreferencesProvider.putString(mContext, PREF_UV_HIGHER_KHZ_THRES, (String) newValue);
            Log.d(TAG, "KHZ Thres Value: " + (String) newValue);
            return true;
        } else if (preference == mUVApplyBoot) {
            PreferencesProvider.putBoolean(mContext, PREF_UV_APPLY_BOOT, mUVApplyBoot.isChecked());
            return true;
        }
        return false;
    }
}
