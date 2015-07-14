
package com.mediatek.incallui.wrapper;

import android.R.bool;
import android.os.SystemProperties;

import com.android.incallui.Log;
import com.android.internal.telephony.PhoneConstants;

public class FeatureOptionWrapper {

    private static final String TAG = "FeatureOptionWrapper";
    private final static String ONE = "1";
    private FeatureOptionWrapper() {
    }

    /**
     * @see FeatureOption.MTK_GEMINI_SUPPORT
     * @see FeatureOption.MTK_GEMINI_3SIM_SUPPORT
     * @see FeatureOption.MTK_GEMINI_4SIM_SUPPORT
     * @return true if the device has 2 or more slots
     */
    public static boolean isSupportGemini() {
        return PhoneConstants.GEMINI_SIM_NUM >= 2;
    }

    /**
     * Voice recording is a common feature and always supported in Phone.
     * Original FeatureOption(MTK_PHONE_VOICE_RECORDING) should be phase out.
     *
     * @return true.
     */
    public static boolean isSupportPhoneVoiceRecording() {
        return true;
    }

    public static boolean isMtkHdmiSupport() {
        boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_hdmi_support")) ? true : false;
        Log.d(TAG, "isMtkHdmiSupport(): " + isSupport);
        return isSupport;
        //return ONE.equals(SystemProperties.get("ro.ro.mtk_hdmi_support")) ? true : false;
    }

    public static boolean isMtkSmartBookSupport() {
        boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_smartbook_support")) ? true : false;
        Log.d(TAG, "isMtkSmartBookSupport(): " + isSupport);
        return isSupport;
        //return ONE.equals(SystemProperties.get("ro.ro.mtk_smartbook_support")) ? true : false;
    }

    public static boolean isMtkVoiceUiSupport() {
        boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_voice_ui_support")) ? true : false;
        Log.d(TAG, "isMtkVoiceUiSupport(): " + isSupport);
        return isSupport;
        //return ONE.equals(SystemProperties.get("ro.mtk_voice_ui_support")) ? true : false;
    }

    public static boolean isSupportVoiceUI() {
        boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_voice_ui_support")) ? true : false;
        Log.d(TAG, "isSupportVoiceUI(): " + isSupport);
        return isSupport;
    }

    public static boolean isSupportVT() {
        boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_vt3g324m_support")) ? true : false;
        Log.d(TAG, "isSupportVT(): " + isSupport);
        return isSupport;
    }

    public static boolean isSupportVTVoiceAnswer() {
        boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_phone_vt_voice_answer")) ? true : false;
        Log.d(TAG, "isSupportVTVoiceAnswer(): " + isSupport);
        return isSupport;
    }

    public static boolean isSupportDualTalk() {
        boolean isSupportDualTalk = ONE.equals(SystemProperties.get("ro.mtk_dt_support")) ? true : false;
        Log.d(TAG, "isSupportDualTalk(): " + isSupportDualTalk);
        return isSupportDualTalk;
    }

    public static boolean isSupportPrivacyProtect() {
        boolean isSupportPrivacyProtect = ONE.equals(SystemProperties.get("ro.mtk_bg_power_saving_support")) ? true : false;
        Log.d(TAG, "isSupportPrivacyProtect(): " + isSupportPrivacyProtect);
        return isSupportPrivacyProtect;
    }

    public static boolean isMtkPhoneNumberGeoDescription() {
        boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_phone_number_geo")) ? true : false;
        Log.d(TAG, "isMtkPhoneNumberGeoDescription(): " + isSupport);
        return ONE.equals(SystemProperties.get("ro.mtk_phone_number_geo")) ? true : false;
    }

    /// M: for VoLTE Conference Call @{
    public static boolean isSupportVoLte() {
        return false;
        //return com.mediatek.common.featureoption.FeatureOption.MTK_VOLTE_SUPPORT;
    }
    /// @}
}

