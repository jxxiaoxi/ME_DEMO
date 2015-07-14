package com.mediatek.phone.ext;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public interface IMobileNetworkSettingsExt {

    /**
     * called when init the preference fragment(onCreate)
     * plugin can customize the fragment, like add/remove preference screen
     * plugin should check the fragment class name, to distinct the caller, avoid do wrong work.
     * if (TextUtils.equals(fragment.getClass().getSimpleName(), "MobileNetworkSettingFragment") {}
     *
     * @param fragment the MobileNetworkSettingsFragment instance
     */
    void initPreferenceFragment(PreferenceFragment fragment);

    /**
     * called when init the preference (onCreate)
     * plugin can customize the activity, like add/remove preference screen
     * plugin should check the activity class name, to distinct the caller, avoid do wrong work.
     *
     * @param fragment the MobileNetworkSettingsFragment instance
     */
    void initPreference(PreferenceActivity activity);
    /**
     * called when some preference state changed(enabled/disabled)
     * plugin should check which preference updated
     *
     * @param preference the updated preference, like DataConnectionDialog
     *
     */

    void onPreferenceUpdated(Preference preference);

    /**
     * called when the MobileNetworkSettingsActivity receive any broadcast
     * the intent filter is defined in customizeBroadcastIntentFilter
     * @see com.mediatek.phone.ext.IMobileNetworkSettingsExt#customizeBroadcastIntentFilter(android.content.IntentFilter)
     *
     * @param intent the broadcast intent
     * @param fragment the MobileNetworkSettingsFragment instance
     */
    void onBroadcastReceived(Intent intent, PreferenceFragment fragment);

    /**
     * called when the MobileNetworkSettingsActivity receive any broadcast
     * the intent filter is defined in customizeBroadcastIntentFilter
     * @see com.mediatek.phone.ext.IMobileNetworkSettingsExt#customizeBroadcastIntentFilter(android.content.IntentFilter)
     *
     * @param intent the broadcast intent
     * @param fragment the MobileNetworkSettingsFragment instance
     */
    void onBroadcastReceived(Intent intent, PreferenceActivity activity);

    /**
     * plugin can customize the broadcast IntentFilter, to add/remove actions.
     * in this way, additional broadcast can be received, and finally notify the
     * plugin via
     * {@link com.mediatek.phone.ext.IMobileNetworkSettingsExt#onBroadcastReceived(android.content.Intent, android.preference.PreferenceFragment)}
     * @param intentFilter the intent filter for broadcast receiver
     */
    void customizeBroadcastIntentFilter(IntentFilter intentFilter);

    /**
     * plugin can customize the AlertDialog according to the builder or the dialog itself.
     * this API is called right before builder.create().
     * plugin should check the activity and token to determine which Dialog is this API stand for
     *
     * @param token the token of current dialog
     * @param builder the builder to create dialog.
     */
    void customizeAlertDialog(int token, AlertDialog.Builder builder);

    /**
     * Attention, returning false means nothing but telling host to go on its own flow.
     * host would never return plug-in's "false" to the caller of onPreferenceTreeClick()
     *
     * @param preferenceScreen the clicked preference screen
     * @param preference the clicked preference
     * @return true if plug-in want to skip host flow. whether return true or false, host will
     * return true to its real caller.
     */
    boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);

    /**
     * called before host attempt to change MobileData state.
     * in gemini case: called when user select a SIM as default data connection
     * in single SIM: called when user enable/disable data connection
     *
     *
     * @param newDataSimId the sim id of current setting to be apply. 0 means data connection off
     * @see com.mediatek.phone.ext.IMobileNetworkSettingsExt.ICallback
     * @return true if plug-in want to block host behavior after user operation
     *          false means plug-in want host go on original flow
     */
    boolean blockMobileDataSetting(long newDataSimId);

    /**
     * called in onCreate() of the Activity
     * plug-in can init itself, preparing for it's function
     * @param activity the MobileNetworkSettings activity
     * @param callback the callback by which plug-in can get host help
     */
    void initPreferenceActivity(PreferenceActivity activity, ICallback callback);

    /**
     * called in onCreate() of the Activity
     * plug-in can init itself, preparing for it's function
     * @param activity the MobileNetworkSettings activity
     * @param callback the callback by which plug-in can get host help
     */
    void initPreference(PreferenceActivity activity, ICallback callback);
    /**
     * called in onDestroy()
     * plug-in should clear up the resources it contains, to avoid leakage
     */
    void deinitPreferenceActivity();

    /**
     * update mode summary
     * @param listLteNetworkMode
     */
    void updateModeSummary(ListPreference listLteNetworkMode);

    /**
     * set preferred network mode entries and values
     * @param listLteNetworkMode
     */
    void setPreferredNetworkModeEntriesAndValues(ListPreference listLteNetworkMode);

    /**
     * update lte mode status
     * @param preference
     * @param preferenceEx
     */
    void updateLteModeStatus(ListPreference preference, Preference preferenceEx);

    public static interface ICallback extends ICommonCallback {
        Handler getHostDataStateUpdateHandler();
        void setHostDataStateChangeState(boolean isChanged);
    }
}
