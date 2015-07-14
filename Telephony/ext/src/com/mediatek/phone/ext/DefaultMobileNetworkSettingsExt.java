package com.mediatek.phone.ext;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class DefaultMobileNetworkSettingsExt implements IMobileNetworkSettingsExt {

    @Override
    public void initPreferenceFragment(PreferenceFragment fragment) {
        // do nothing
    }

    @Override
    public void onPreferenceUpdated(Preference preference) {
        // do nothing
    }

    @Override
    public void onBroadcastReceived(Intent intent, PreferenceFragment fragment) {
        // do nothing
    }

    @Override
    public void customizeBroadcastIntentFilter(IntentFilter intentFilter) {
        // do nothing
    }

    @Override
    public void customizeAlertDialog(int token, AlertDialog.Builder builder) {
        // do nothing
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return false;
    }

    @Override
    public boolean blockMobileDataSetting(long newDataSimId) {
        return false;
    }

    @Override
    public void initPreferenceActivity(PreferenceActivity activity, ICallback callback) {
        // do nothing
    }

    @Override
    public void deinitPreferenceActivity() {
        // do nothing
    }

    @Override
    public void initPreference(PreferenceActivity activity) {
        // do nothing
    }

    @Override
    public void onBroadcastReceived(Intent intent, PreferenceActivity activity) {
        // do nothing
    }

    @Override
    public void initPreference(PreferenceActivity activity, ICallback callback) {
        // do nothing
    }

    /**
     * update mode summary
     * @param listLteNetworkMode, ListPreference
     */
    @Override
    public void updateModeSummary(ListPreference listLteNetworkMode) {
        //do nothing
    }

    /**
     * set preferred network mode entries snd values
     * @param listLteNetworkMode, ListPreference
     */
    @Override
    public void setPreferredNetworkModeEntriesAndValues(ListPreference listLteNetworkMode) {
        //do nothing
    }

    /**
     * update lte mode status
     * @param preference, ListPreference
     * @param preferenceEx, Preference
     */
    @Override
    public void updateLteModeStatus(ListPreference preference, Preference preferenceEx) {
        //do nothing
    }
}
