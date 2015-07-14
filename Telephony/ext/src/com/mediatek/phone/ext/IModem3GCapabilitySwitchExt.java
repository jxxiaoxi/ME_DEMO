package com.mediatek.phone.ext;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import java.util.List;

public interface IModem3GCapabilitySwitchExt {

    /**
     * customize the items in SIM Selection Dialog.
     * the part which be customized can only be the items apart of SIM info.
     *
     * @param customLabels
     * @param customValues
     */
    void customizeSimSelectList(List<String> customLabels, List<Long> customValues);

    /**
     * called when init the preference Activity(onCreate)
     * plugin can customize the Activity, like add/remove preference screen
     * plugin should check the Activity class name before use it, like:
     * if (TextUtils.equals(activity.getClass().getSimpleName(), "Modem3GCapabilitySwitch") {}
     *
     * @param activity the PreferenceActivity which call this API
     * @param callback provide Activity and Phone
     */
    void initPreferenceActivity(PreferenceActivity activity, ICommonCallback callback);

    /**
     * called in onDestroy()
     * plug-in should clear up the resources it contains, to avoid leakage
     */
    void deinitPreferenceActivity();

   /**
     * 3/4G switch Flag for removing radio off
     */
    boolean isRemoveRadioOffFor3GSwitchFlag();

    /**
     * show hint dialog for sim switch
     * @param selectedServiceSim
     * @param context
     * @return true when should hint dialog
     */
    boolean isShowHintDialogForSimSwitch(long selectedServiceSim, Context context);

    /**
     * get hint string
     * @return plugin string
     */
    String getHintString();

    /**
     * init network mode for 3G switch
     * @param prefsc, PreferenceScreen
     * @param networkMode, Preference
     * @param context, Context
     */
    void NetworkModeFor3GSwitch(PreferenceScreen prefsc, Preference networkMode, Context context);

    /**
     * update Lte Mode Status
     * @param preference, ListPreference
     * @param preferenceEx, Preference
     */
    void updateLteModeStatus(ListPreference preference, Preference preferenceEx);
}
