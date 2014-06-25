/*
 * Copyright (C) 2014 Slimroms
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

package com.android.settings.TheKang;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.internal.util.slim.DeviceUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class RecentPanel extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String CUSTOM_RECENT_MODE =
            "custom_recent_mode";
    private static final String CUSTOM_RECENT_SHOW_TOPMOST =
            "custom_recent_show_topmost";
    private static final String RECENT_PANEL_LEFTY_MODE =
            "recent_panel_lefty_mode";
    private static final String RECENT_PANEL_SCALE =
            "recent_panel_scale";
    private static final String RECENT_PANEL_EXPANDED_MODE =
            "recent_panel_expanded_mode";
    private static final String RECENT_MENU_CLEAR_ALL =
            "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION =
            "recent_menu_clear_all_location";
    private static final String STOCK_RECENT_PANEL_CATEGORY =
            "stock_recent_panel_category";
    private static final String RECENT_PANEL_BG_COLOR =
            "recent_panel_bg_color";
    //private static final String SLIM_RECENT_PANEL_CATEGORY =
    //        "slim_recent_panel_category";

    private CheckBoxPreference mRecentsCustom;
    private CheckBoxPreference mRecentsShowTopmost;
    private CheckBoxPreference mRecentPanelLeftyMode;
    private ListPreference mRecentPanelScale;
    private ListPreference mRecentPanelExpandedMode;
    private CheckBoxPreference mRecentClearAll;
    private ListPreference mRecentClearAllPosition;
    private PreferenceCategory mStockRecentPanel;
    //private PreferenceCategory mSlimRecentPanel;
    private ColorPickerPreference mRecentPanelBgColor;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DEFAULT_BACKGROUND_COLOR = 0x00ffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.recent_panel_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mStockRecentPanel = (PreferenceCategory) findPreference(STOCK_RECENT_PANEL_CATEGORY);
        //mSlimRecentPanel = (PreferenceCategory) findPreference(SLIM_RECENT_PANEL_CATEGORY);

        boolean enableRecentsCustom = Settings.System.getBoolean(getContentResolver(),
                                      Settings.System.CUSTOM_RECENT, false);
        mRecentsCustom = (CheckBoxPreference) findPreference(CUSTOM_RECENT_MODE);
        mRecentsCustom.setChecked(enableRecentsCustom);
        mRecentsCustom.setOnPreferenceChangeListener(this);

        mStockRecentPanel.setEnabled(!enableRecentsCustom);

        boolean enableRecentsShowTopmost = Settings.System.getBoolean(getContentResolver(),
                                      Settings.System.CUSTOM_RECENT_SHOW_TOPMOST, false);
        mRecentsShowTopmost = (CheckBoxPreference) findPreference(CUSTOM_RECENT_SHOW_TOPMOST);
        mRecentsShowTopmost.setChecked(enableRecentsShowTopmost);
        mRecentsShowTopmost.setOnPreferenceChangeListener(this);

        mRecentPanelLeftyMode =
                (CheckBoxPreference) findPreference(RECENT_PANEL_LEFTY_MODE);
        mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);

        mRecentPanelScale =
                (ListPreference) findPreference(RECENT_PANEL_SCALE);
        mRecentPanelScale.setOnPreferenceChangeListener(this);

        mRecentPanelExpandedMode =
                (ListPreference) findPreference(RECENT_PANEL_EXPANDED_MODE);
        mRecentPanelExpandedMode.setOnPreferenceChangeListener(this);

        mRecentClearAll = (CheckBoxPreference) findPreference(RECENT_MENU_CLEAR_ALL);
        mRecentClearAll.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SHOW_CLEAR_RECENTS_BUTTON, 1) == 1);
        mRecentClearAll.setOnPreferenceChangeListener(this);
        mRecentClearAllPosition = (ListPreference) findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        String recentClearAllPosition = Settings.System.getString(getContentResolver(),
                Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
             mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);

        // Recent panel background color
        mRecentPanelBgColor =
                (ColorPickerPreference) findPreference(RECENT_PANEL_BG_COLOR);
        mRecentPanelBgColor.setOnPreferenceChangeListener(this);
        final int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, 0x00ffffff);
        String hexColor = String.format("#%08x", (0x00ffffff & intColor));
        if (hexColor.equals("#00ffffff")) {
            mRecentPanelBgColor.setSummary("TRDS default");
        } else {
            mRecentPanelBgColor.setSummary(hexColor);
        }
        mRecentPanelBgColor.setNewPreviewColor(intColor);
        setHasOptionsMenu(true);

        UpdateSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateSettings();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void UpdateSettings() {
        final boolean recentLeftyMode = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_GRAVITY, Gravity.RIGHT) == Gravity.LEFT;
        mRecentPanelLeftyMode.setChecked(recentLeftyMode);

        final int recentScale = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_SCALE_FACTOR, 100);
        mRecentPanelScale.setValue(recentScale + "");

        final int recentExpandedMode = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_EXPANDED_MODE, 0);
        mRecentPanelExpandedMode.setValue(recentExpandedMode + "");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRecentPanelScale) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_SCALE_FACTOR, value);
            return true;
        } else if (preference == mRecentPanelExpandedMode) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_EXPANDED_MODE, value);
            return true;
        } else if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_GRAVITY,
                    ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
            return true;
        } else if (preference == mRecentsCustom) {
            Settings.System.putBoolean(getContentResolver(),
                    Settings.System.CUSTOM_RECENT,
                    ((Boolean) newValue) ? true : false);
            mStockRecentPanel.setEnabled(((Boolean) newValue) ? false : true);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mRecentsShowTopmost) {
            Settings.System.putBoolean(getContentResolver(),
                    Settings.System.CUSTOM_RECENT_SHOW_TOPMOST,
                    ((Boolean) newValue) ? true : false);
            return true;
        } else if (preference == mRecentClearAll) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_CLEAR_RECENTS_BUTTON,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mRecentClearAllPosition) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.CLEAR_RECENTS_BUTTON_LOCATION,
                    ((String) newValue));
            return true;
        } else if (preference == mRecentPanelBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#00ffffff")) {
                preference.setSummary("TRDS default");
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_BG_COLOR,
                    intHex);
            return true;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset_default_message)
                .setIcon(R.drawable.ic_settings_backup)
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
        alertDialog.setTitle(R.string.shortcut_action_reset);
        alertDialog.setMessage(R.string.qs_style_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
        mRecentPanelBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);
        mRecentPanelBgColor.setSummary("TRDS default");
    }
}
