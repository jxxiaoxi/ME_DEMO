package com.mediatek.phone.ext;

import java.util.List;

import android.app.AlertDialog;
import android.preference.PreferenceActivity;

import com.android.internal.telephony.OperatorInfo;

public interface INetworkSettingExt {
    /**
     * called when init the preference Activity(onCreate)
     * plugin can customize the Activity, like add/remove preference screen
     * plugin should check the Activity class name before use it, like:
     * if (TextUtils.equals(activity.getClass().getSimpleName(), "Modem3GCapabilitySwitch") {}
     *
     * @param activity the PreferenceActivity which call this API
     *
     */
    void initPreferenceActivity(PreferenceActivity activity);

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
     * called in onDestroy()
     * plug-in should clear up the resources it contains, to avoid leakage
     */
    void deinitPreferenceActivity();

    /**
     * Let plug-in customize the OperatorInfo list before display.
     *
     * @param operatorInfoList The OperatorInfo list get from framework
     * @param soltId The solt id user selected
     * @return new list OperatorInfo
     */
    public List<OperatorInfo> customizeNetworkList(List<OperatorInfo> operatorInfoList, int soltId);


    /**
     * CU feature, customize forbidden Preference click, pop up a toast.
     * @param operatorInfo Preference's operatorInfo
     * @param soltId solt id
     * @return true It means the preference click will be done
     */
    public boolean onPreferenceTreeClick(OperatorInfo operatorInfo, int soltId);
}
