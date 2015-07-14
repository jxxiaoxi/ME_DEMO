package com.mediatek.phone.ext;

import android.app.Activity;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class DefaultCallSettingsExt implements ICallSettingsExt {
    @Override
    public void initOtherSettingsFragment(PreferenceFragment fragment) {
        // do nothing
    }

    @Override
    public void initCdmaCallForwardOptionsActivity(PreferenceActivity activity) {
        // do nothing
    }

    @Override
    public void replaceCallSettingsActivity(Activity activity) {
        // do nothing
    }

    @Override
    public void initOtherSettings(PreferenceActivity activity) {
        // do nothing
    }
}
