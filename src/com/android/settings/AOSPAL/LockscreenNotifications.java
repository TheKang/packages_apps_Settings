package com.android.settings.AOSPAL;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SlimSeekBarPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockscreenNotifications
        extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String KEY_LOCKSCREEN_NOTIFICATIONS = "lockscreen_notifications";
    private static final String KEY_POCKET_MODE = "pocket_mode";
    private static final String KEY_SHOW_ALWAYS = "show_always";
    private static final String KEY_HIDE_LOW_PRIORITY = "hide_low_priority";
    private static final String KEY_HIDE_NON_CLEARABLE = "hide_non_clearable";
    private static final String KEY_DISMISS_ALL = "dismiss_all";
    private static final String KEY_EXPANDED_VIEW = "expanded_view";
    private static final String KEY_FORCE_EXPANDED_VIEW = "force_expanded_view";
    private static final String KEY_WAKE_ON_NOTIFICATION = "wake_on_notification";
    private static final String KEY_NOTIFICATIONS_HEIGHT = "notifications_height";
    private static final String KEY_PRIVACY_MODE = "privacy_mode";
    private static final String KEY_DYNAMIC_WIDTH = "dynamic_width";
    private static final String KEY_OFFSET_TOP = "offset_top";
    private static final String KEY_CATEGORY_GENERAL = "category_general";
    private static final String KEY_EXCLUDED_APPS = "excluded_apps";
    private static final String KEY_NOTIFICATION_COLOR = "notification_color";

    private CheckBoxPreference mPocketMode;
    private CheckBoxPreference mShowAlways;
    private CheckBoxPreference mWakeOnNotification;
    private CheckBoxPreference mHideLowPriority;
    private CheckBoxPreference mHideNonClearable;
    private CheckBoxPreference mDismissAll;
    private CheckBoxPreference mExpandedView;
    private CheckBoxPreference mForceExpandedView;
    private NumberPickerPreference mNotificationsHeight;
    private CheckBoxPreference mPrivacyMode;
    private CheckBoxPreference mDynamicWidth;
    private SlimSeekBarPreference mOffsetTop;
    private AppMultiSelectListPreference mExcludedAppsPref;
    private ColorPickerPreference mNotificationColor;

    private Switch mActionBarSwitch;
    private LockscreenNotificationsEnabler mLockscreenNotificationsEnabler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_notifications);
        PreferenceScreen prefs = getPreferenceScreen();
        final ContentResolver cr = getActivity().getContentResolver();

        boolean hasProximitySensor = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);

        if (!hasProximitySensor) {
            mPocketMode = (CheckBoxPreference) prefs.findPreference(KEY_POCKET_MODE);
            mPocketMode.setChecked(Settings.System.getInt(cr,
                        Settings.System.LOCKSCREEN_NOTIFICATIONS_POCKET_MODE, 1) == 1);

            mShowAlways = (CheckBoxPreference) prefs.findPreference(KEY_SHOW_ALWAYS);
            mShowAlways.setChecked(Settings.System.getInt(cr,
                        Settings.System.LOCKSCREEN_NOTIFICATIONS_SHOW_ALWAYS, 1) == 1);
        }

        mWakeOnNotification = (CheckBoxPreference) prefs.findPreference(KEY_WAKE_ON_NOTIFICATION);
        mWakeOnNotification.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION, 0) == 1);

        mHideLowPriority = (CheckBoxPreference) prefs.findPreference(KEY_HIDE_LOW_PRIORITY);
        mHideLowPriority.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_LOW_PRIORITY, 0) == 1);

        mHideNonClearable = (CheckBoxPreference) prefs.findPreference(KEY_HIDE_NON_CLEARABLE);
        mHideNonClearable.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE, 0) == 1);

        mDismissAll = (CheckBoxPreference) prefs.findPreference(KEY_DISMISS_ALL);
        mDismissAll.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL, 1) == 1);

        mPrivacyMode = (CheckBoxPreference) prefs.findPreference(KEY_PRIVACY_MODE);
        mPrivacyMode.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE, 0) == 1);

        mDynamicWidth = (CheckBoxPreference) prefs.findPreference(KEY_DYNAMIC_WIDTH);
        mDynamicWidth.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_DYNAMIC_WIDTH, 1) == 1);
        mExpandedView = (CheckBoxPreference) prefs.findPreference(KEY_EXPANDED_VIEW);
        mExpandedView.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW, 1) == 1);

        mForceExpandedView = (CheckBoxPreference) prefs.findPreference(KEY_FORCE_EXPANDED_VIEW);
        mForceExpandedView.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW, 0) == 1);

        mOffsetTop = (SlimSeekBarPreference) prefs.findPreference(KEY_OFFSET_TOP);
        int val = (int)(Settings.System.getFloat(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, 0.3f) * 100);
        mOffsetTop.setInitValue(val);
        mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + val + "%");
        mOffsetTop.setOnPreferenceChangeListener(this);

        mNotificationsHeight = (NumberPickerPreference) prefs.findPreference(KEY_NOTIFICATIONS_HEIGHT);
        val = Settings.System.getInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, 4);
        mNotificationsHeight.setValue(val);
        Point displaySize = new Point();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
        int max = Math.round((float)displaySize.y * (1f - (val / 100f)) /
                (float) getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
        mNotificationsHeight.setMinValue(1);
        mNotificationsHeight.setMaxValue(max);
        mNotificationsHeight.setOnPreferenceChangeListener(this);

        mNotificationColor = (ColorPickerPreference) prefs.findPreference(KEY_NOTIFICATION_COLOR);
        mNotificationColor.setAlphaSliderEnabled(true);
        int color = Settings.System.getInt(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, 0x55555555);
        String hexColor = String.format("#%08x", (0xffffffff & color));
        mNotificationColor.setSummary(hexColor);
        mNotificationColor.setDefaultValue(color);
        mNotificationColor.setNewPreviewColor(color);
        mNotificationColor.setOnPreferenceChangeListener(this);

        mExcludedAppsPref = (AppMultiSelectListPreference) findPreference(KEY_EXCLUDED_APPS);
        Set<String> excludedApps = getExcludedApps();
        if (excludedApps != null) mExcludedAppsPref.setValues(excludedApps);
        mExcludedAppsPref.setOnPreferenceChangeListener(this);

        update();
    }

    private void update() {
        boolean enabled = Settings.System.getInt(
                getActivity().getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS, 1) == 1;

        if (mPocketMode != null) {
            mPocketMode.setEnabled(enabled);
            mShowAlways.setEnabled(mPocketMode.isChecked() && mPocketMode.isEnabled());
        }
        mWakeOnNotification.setEnabled(enabled);
        mHideLowPriority.setEnabled(enabled);
        mHideNonClearable.setEnabled(enabled);
        mDismissAll.setEnabled(!mHideNonClearable.isChecked() && enabled);
        mNotificationsHeight.setEnabled(enabled);
        mOffsetTop.setEnabled(enabled);
        mForceExpandedView.setEnabled(enabled && mExpandedView.isChecked()
                && !mPrivacyMode.isChecked());
        mExpandedView.setEnabled(enabled && !mPrivacyMode.isChecked());
        mPrivacyMode.setEnabled(enabled);
        mDynamicWidth.setEnabled(enabled);
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        Activity activity = getActivity();
        //Switch
        mActionBarSwitch = new Switch(activity);

        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                final int padding = activity.getResources().getDimensionPixelSize(
                        R.dimen.action_bar_switch_padding);
                mActionBarSwitch.setPaddingRelative(0, 0, padding, 0);
                activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView(mActionBarSwitch, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.END));
            }
        }

        mLockscreenNotificationsEnabler = new LockscreenNotificationsEnabler(activity, mActionBarSwitch);
        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(icicle);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLockscreenNotificationsEnabler != null) {
            mLockscreenNotificationsEnabler.resume();
        }
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLockscreenNotificationsEnabler != null) {
            mLockscreenNotificationsEnabler.pause();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver cr = getActivity().getContentResolver();
        if (preference == mPocketMode) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_POCKET_MODE,
                    mPocketMode.isChecked() ? 1 : 0);
            mShowAlways.setEnabled(mPocketMode.isChecked());
        } else if (preference == mShowAlways) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_SHOW_ALWAYS,
                    mShowAlways.isChecked() ? 1 : 0);
        } else if (preference == mWakeOnNotification) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION,
                    mWakeOnNotification.isChecked() ? 1 : 0);
        } else if (preference == mHideLowPriority) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_LOW_PRIORITY,
                    mHideLowPriority.isChecked() ? 1 : 0);
        } else if (preference == mHideNonClearable) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE,
                    mHideNonClearable.isChecked() ? 1 : 0);
        } else if (preference == mDismissAll) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL,
                    mDismissAll.isChecked() ? 1 : 0);
        } else if (preference == mExpandedView) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW,
                    mExpandedView.isChecked() ? 1 : 0);
        } else if (preference == mForceExpandedView) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW,
                    mForceExpandedView.isChecked() ? 1 : 0);
        } else if (preference == mPrivacyMode) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE,
                    mPrivacyMode.isChecked() ? 1 : 0);
        } else if (preference == mDynamicWidth) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_DYNAMIC_WIDTH,
                    mDynamicWidth.isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        update();
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object value) {
        if (pref == mNotificationsHeight) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, Integer.valueOf((String) value));
        } else if (pref == mOffsetTop) {
            Settings.System.putFloat(getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP,
                    Integer.valueOf((String) value) / 100f);
            mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + value + "%");
            Point displaySize = new Point();
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
            int max = Math.round((float)displaySize.y * (1f - (Integer.valueOf((String) value) / 100f)) /
                    (float) getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
            mNotificationsHeight.setMaxValue(max);
        } else if (pref == mNotificationColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(value)));
            pref.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, intHex);
            return true;
        } else if (pref == mExcludedAppsPref) {
            storeExcludedApps((Set<String>) value);
            return true;
        } else {
            return false;
        }
        return true;
    }

    private Set<String> getExcludedApps() {
        String excluded = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS);
        if (TextUtils.isEmpty(excluded))
            return null;

        return new HashSet<String>(Arrays.asList(excluded.split("\\|")));
    }

    private void storeExcludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS, builder.toString());
    }
}
