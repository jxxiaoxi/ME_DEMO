package com.mediatek.phone;

import com.android.internal.telephony.PhoneConstants;
import com.android.phone.PhoneGlobals;

import android.R.bool;
import android.content.Context;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;

public class PhoneFeatureConstants {

    public static final class FeatureOption {

        private final static String ONE = "1";
        private static final String TAG = "FeatureOption";
        public static boolean isMtkCtaSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("persist.mtk_cta_support")) ? true : false;
            PhoneLog.d(TAG, "isMtkCtaSupport(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtkGeminiSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_gemini_support")) ? true : false;
            PhoneLog.d(TAG , "isMtkGeminiSupport(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtkVT3G324MSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_vt3g324m_support")) ? true : false;;
            PhoneLog.d(TAG, "isMtkVT3G324MSupport: " + isSupport);
            return ONE.equals(SystemProperties.get("ro.mtk_vt3g324m_support")) ? true : false;
        }

        private static final String MTK_TTY_SUPPORT = "MTK_TTY_SUPPORT";
        private static final String MTK_TTY_SUPPORT_on = "MTK_TTY_SUPPORT=true";
        public static boolean isMtkTtySupport() {
            String state = null;
            AudioManager audioManager = (AudioManager) PhoneGlobals.getInstance().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                state = audioManager.getParameters(MTK_TTY_SUPPORT);
                PhoneLog.d(state, "isMtkTtySupport(): state: " + state);
                if (state.equalsIgnoreCase(MTK_TTY_SUPPORT_on)) {
                    return true;
                }
            }
            return false;
        }

        private static final String MTK_DUAL_MIC_SUPPORT = "MTK_DUAL_MIC_SUPPORT";
        private static final String MTK_DUAL_MIC_SUPPORT_on = "MTK_DUAL_MIC_SUPPORT=true";
        public static boolean isMtkDualMicSupport() {
            String state = null;
            AudioManager audioManager = (AudioManager) PhoneGlobals.getInstance().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                state = audioManager.getParameters(MTK_DUAL_MIC_SUPPORT);
                PhoneLog.d(state, "isMtkDualMicSupport(): state: " + state);
                if (state.equalsIgnoreCase(MTK_DUAL_MIC_SUPPORT_on)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isMtkPhoneVtVoiceAnswerSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_phone_vt_voice_answer")) ? true : false;;
            PhoneLog.d(TAG, "isMtkPhoneVtVoiceAnswerSupport(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_phone_vt_voice_answer")) ? true : false;
        }

        public static boolean isMtkPhoneVoiceRecordingSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_phone_voice_recording")) ? true : false;
            PhoneLog.d(TAG, "isMtkPhoneVoiceRecordingSupport(): " + isSupport);
            return true;
            //return ONE.equals(SystemProperties.get("ro.mtk_phone_voice_recording")) ? true : false;
        }

        public static boolean isMtkBrazilCustomizationVivo() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.brazil_cust_vivo")) ? true : false;
            PhoneLog.d(TAG, "isMtkBrazilCustomizationVivo(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.brazil_cust_vivo")) ? true : false;
        }

        public static boolean isMtkBrazilCustomizationClaro() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.brazil_cust_claro")) ? true : false;
            PhoneLog.d(TAG, "isMtkBrazilCustomizationClaro(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.brazil_cust_claro")) ? true : false;
        }

        public static boolean isMtkGemini3GSwitch() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_gemini_3g_switch")) ? true : false;
            PhoneLog.d(TAG, "isMtkGemini3GSwitch(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_gemini_3g_switch")) ? true : false;
        }

        public static boolean isMtkTbAppCallForceSpeakerOn() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_tb_call_speaker_on")) ? true : false;
            PhoneLog.d(TAG, "isMtkTbAppCallForceSpeakerOn(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_tb_call_speaker_on")) ? true : false;
        }

        public static boolean isMtkPhoneNumberGeoDescription() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_phone_number_geo")) ? true : false;
            PhoneLog.d(TAG, "isMtkPhoneNumberGeoDescription(): " + isSupport);
            return ONE.equals(SystemProperties.get("ro.mtk_phone_number_geo")) ? true : false;
        }

        public static boolean isEvdoDtSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.evdo_dt_support")) ? true : false;
            PhoneLog.d(TAG, "isEvdoDtSupport(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.evdo_dt_support")) ? true : false;
        }

        public static boolean isMtkDtSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_dt_support")) ? true : false;
            PhoneLog.d(TAG, "isMtkDtSupport(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_dt_support")) ? true : false;
        }

        public static boolean isMtkAudioProfiles() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_audio_profiles")) ? true : false;
            PhoneLog.d(TAG, "isMtkAudioProfiles(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_audio_profiles")) ? true : false;
        }

        public static boolean isMtkFlightModePowerOffMd() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_flight_mode_power_off_md")) ? true : false;
            PhoneLog.d(TAG, "isMtkFlightModePowerOffMd(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_flight_mode_power_off_md")) ? true : false;
        }

        public static boolean isMtkVoiceUiSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_voice_ui_support")) ? true : false;
            PhoneLog.d(TAG, "isMtkVoiceUiSupport(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_voice_ui_support")) ? true : false;
        }

        public static boolean isMtkMultisimRingtoneSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_multisim_ringtone")) ? true : false;
            PhoneLog.d(TAG, "isMtkMultisimRingtoneSupport(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_multisim_ringtone")) ? true : false;
        }

        public static boolean isMtkFemtoCellSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_femto_cell_support")) ? true : false;
            PhoneLog.d(TAG, "isMtkFemtoCellSupport(): " + isSupport);
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_femto_cell_support")) ? true : false;
        }

        public static boolean isMtk3gDongleSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_3gdongle_support")) ? true : false;
            PhoneLog.d(TAG, "isMtk3gDongleSupport()");
            return isSupport;
            //return ONE.equals(SystemProperties.get("ro.mtk_3gdongle_support")) ? true : false;
        }

        public static boolean isMtkGemini4SimSupport() {
            int num = PhoneConstants.GEMINI_SIM_NUM;
            PhoneLog.d(TAG, "isMtkGemini4SimSupport()... num: " + num);
            return (num == 4);
        }

        public static boolean isMtkGemini3SimSupport() {
            int num = PhoneConstants.GEMINI_SIM_NUM;
            PhoneLog.d(TAG, "isMtkGemini4SimSupport()... num: " + num);
            return (num == 3);
        }

        public static boolean isMtkVolteSupport() {
            return false;
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

        public static boolean isMtkUmtsTdd128Mode() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_umts_tdd128_mode")) ? true : false;
            Log.d(TAG, "isMtkUmtsTdd128Mode(): " + isSupport);
            return isSupport;
        }

        // do not support
        public static boolean isMtkSimSwitch() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_sim_switch")) ? true : false;
            Log.d(TAG, "isMtkSimSwitch(): " + isSupport);
            return isSupport;
        }

        public static boolean isMtkLteDcSupport() {
            boolean isSupport = ONE.equals(SystemProperties.get("ro.mtk_lte_dc_support")) ? true : false;
            Log.d(TAG, "isMtkLteDcSupport(): " + isSupport);
            return isSupport;
        }
    }
}
