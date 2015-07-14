package com.mediatek.phone.ext;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import java.util.List;

public class DefaultModem3GCapabilitySwitchExt implements IModem3GCapabilitySwitchExt {
    private static final String LOG_TAG = "DefaultModem3GCapabilitySwitchExt";

    @Override
    public void customizeSimSelectList(List<String> customLabels, List<Long> customValues) {
        // do nothing
    }

    @Override
    public void initPreferenceActivity(PreferenceActivity activity, ICommonCallback callback) {
        // do nothing
    }

    @Override
    public void deinitPreferenceActivity() {
        // do nothing
    }

    /**
     * 3/4G switch Flag for removing radio off
     * @return true if plugin need remove 3G Switch
     */
    @Override
    public boolean isRemoveRadioOffFor3GSwitchFlag() {
       return false;
    }

    /**
     * show hint dialog for sim switch
     * @param selectedServiceSim
     * @param context
     * @return true when should hint dialog
     */
    @Override
    public boolean isShowHintDialogForSimSwitch(long selectedServiceSim, Context context) {
        return false;
    }

    /**
     * get hint string
     * @return plugin string
     */
    @Override
    public String getHintString() {
        return " ";
    }

    /**
     * init network mode for 3G switch
     * @param prefsc, PreferenceScreen
     * @param networkMode, Preference
     * @param context, Context
     */
    @Override
    public void NetworkModeFor3GSwitch(PreferenceScreen prefsc, Preference networkMode,
            Context context) {
        //do nothing
    }

    /**
     * update Lte Mode Status
     * @param preference, ListPreference
     * @param preferenceEx, Preference
     */
    @Override
    public void updateLteModeStatus(ListPreference preference, Preference preferenceEx) {
        //do nothing
    }
}
