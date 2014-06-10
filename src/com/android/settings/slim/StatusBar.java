/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.slim.DeviceUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarSettings";

    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String STATUS_BAR_AUTO_UNHIDE = "status_bar_auto_unhide";
    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL = "status_bar_network_stats_update_interval";
    private static final String STATUS_BAR_NETWORK_STATS_HIDE = "status_bar_network_stats_hide";
    private static final String STATUS_BAR_NETWORK_STATS_COLOR = "status_bar_network_stats_color";

    private PreferenceScreen mClockStyle;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mStatusBarAutoUnhide;
    private CheckBoxPreference mStatusBarNetStats;
    private ListPreference mStatusBarNetStatsUpdate;
    private CheckBoxPreference mStatusBarNetStatsHide;
    private ColorPickerPreference mStatusBarNetStatsColor;

    private static final int MENU_RESET = Menu.FIRST;

    static final int DEFAULT_NETWORK_STATS_COLOR = 0xffffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        // Start observing for changes on auto brightness
        StatusBarBrightnessChangedObserver statusBarBrightnessChangedObserver =
            new StatusBarBrightnessChangedObserver(new Handler());
        statusBarBrightnessChangedObserver.startObserving();

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);
        if (mClockStyle != null) {
            updateClockStyleDescription();
        }

        mStatusBarBrightnessControl =
            (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        mStatusBarAutoUnhide = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_AUTO_UNHIDE);
        mStatusBarAutoUnhide.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_AUTO_UNHIDE, 0) == 1));
        mStatusBarAutoUnhide.setOnPreferenceChangeListener(this);

        mStatusBarNetStats = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS);
        mStatusBarNetStats.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1));

        long statsUpdate = Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);

        mStatusBarNetStatsUpdate = (ListPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL);
        mStatusBarNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntry());
        mStatusBarNetStatsUpdate.setOnPreferenceChangeListener(this);

        mStatusBarNetStatsHide = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS_HIDE);
        mStatusBarNetStatsHide.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS_HIDE, 1) == 1));

        // custom colors
        mStatusBarNetStatsColor = (ColorPickerPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS_COLOR);
        mStatusBarNetStatsColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
                   Settings.System.STATUS_BAR_NETWORK_STATS_COLOR, 0xff000000);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarNetStatsColor.setSummary(hexColor);
        mStatusBarNetStatsColor.setNewPreviewColor(intColor);

        setHasOptionsMenu(true);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarBrightnessControl) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mStatusBarAutoUnhide) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_AUTO_UNHIDE,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mStatusBarNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) newValue);
            int index = mStatusBarNetStatsUpdate.findIndexOfValue((String) newValue);
            Settings.System.putLong(getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarNetStatsColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_COLOR, intHex);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mStatusBarNetStats) {
            value = mStatusBarNetStats.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarNetStatsHide) {
            value = mStatusBarNetStatsHide.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_HIDE, value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
        updateStatusBarBrightnessControl();
    }

    private void updateStatusBarBrightnessControl() {
        try {
            if (mStatusBarBrightnessControl != null) {
                int mode = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    mStatusBarBrightnessControl.setEnabled(false);
                    mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
                } else {
                    mStatusBarBrightnessControl.setEnabled(true);
                    mStatusBarBrightnessControl.setSummary(
                        R.string.status_bar_toggle_brightness_summary);
                }
            }
        } catch (SettingNotFoundException e) {
        }
    }

    private void updateClockStyleDescription() {
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
//            mClockStyle.setSummary(getString(R.string.enabled));
        } else {
//            mClockStyle.setSummary(getString(R.string.disabled));
         }
    }

    private class StatusBarBrightnessChangedObserver extends ContentObserver {
        public StatusBarBrightnessChangedObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateStatusBarBrightnessControl();
        }

        public void startObserving() {
            getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                    false, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.status_bar_network_usage_color_reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.status_bar_network_usage_color_reset);
        alertDialog.setMessage(R.string.status_bar_network_usage_color_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                NetworkStatsColorReset();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void NetworkStatsColorReset() {
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS_COLOR, DEFAULT_NETWORK_STATS_COLOR);

        mStatusBarNetStatsColor.setNewPreviewColor(DEFAULT_NETWORK_STATS_COLOR);
        String hexColor = String.format("#%08x", (0xffffffff & DEFAULT_NETWORK_STATS_COLOR));
        mStatusBarNetStatsColor.setSummary(hexColor);
    }
}
