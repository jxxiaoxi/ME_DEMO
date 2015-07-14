package com.mediatek.phone.ext;

import android.app.Activity;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public interface ICallSettingsExt {
    /**
     * called when init the preference fragment(onCreate)
     * plugin can customize the fragment, like add/remove preference screen
     * plugin should check the fragment class name, to distinct the caller, avoid do wrong work.
     * if (TextUtils.equals(fragment.getClass().getSimpleName(), "OthersSettingsFragment") {}
     *
     * @param fragment the PreferenceFragment instance
     */
    void initOtherSettingsFragment(PreferenceFragment fragment);

    /**
     * called when init the preference (onCreate)
     * plugin can customize the activity, like add/remove preference screen
     * plugin should check the activiyt class name, to distinct the caller, avoid do wrong work.
     * if (TextUtils.equals(getClass().getSimpleName(), "OthersSettings") {}
     *
     * @param fragment the PreferenceFragment instance
     */
    void initOtherSettings(PreferenceActivity activity);
    /**
     * called when init the preference Activity(onCreate)
     * plugin can customize the Activity, like add/remove preference screen
     * plugin should check the Activity class name before use it, like:
     *
     * @param activity the PreferenceActivity which call this API
     */
    void initCdmaCallForwardOptionsActivity(PreferenceActivity activity);

    /**
     * Finish current Activity and start CT customized Activity instead
     *
     * @param activity Default CallSettings activity
     *
     * Used in CallSettings.java
     */
    void replaceCallSettingsActivity(Activity activity);
}
