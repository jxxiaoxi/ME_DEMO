package com.mediatek.incallui.vt;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.android.incallui.CallCommandClient;
import com.android.incallui.CallList;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallPresenter;
import com.android.incallui.Log;
import com.android.incallui.R;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.services.telephony.common.Call;
import com.android.services.telephony.common.Call.DisconnectCause;
import com.mediatek.incallui.InCallUtils;
import com.mediatek.incallui.wrapper.FeatureOptionWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public final class VTUtils {

    private static final String TAG = VTUtils.class.getSimpleName();
    private static final int INVALID_RES_ID = -1;

    /**
     * The function to judge whether the call is video call
     * @param call Call object
     * @return true for yes, false for no
     */
    public static boolean isVTCall(Call call) {
        boolean isVT = false;
        if (call != null) {
            isVT = call.isVideoCall();
        }
        return isVT; 
    }

    /**
     * The function to get current VT Call: incoming > outgoing > active > onHold > disconnecting > disconnect.
     * @return the current VT call, may be null
     */
    public static Call getVTCall() {
        Call vtCall = null;
        if (FeatureOptionWrapper.isSupportVT() && CallList.getInstance().existsLiveCall()) {
            Call ringingCall = CallList.getInstance().getIncomingCall();
            Call outgoingCall = CallList.getInstance().getOutgoingCall();
            Call activeCall = CallList.getInstance().getActiveCall();
            Call holdCall = CallList.getInstance().getBackgroundCall();
            Call disconnectingCall = CallList.getInstance().getDisconnectingCall();
            Call disconnectedCall = CallList.getInstance().getDisconnectedCall();

            if (ringingCall != null && ringingCall.isVideoCall()) {
                vtCall = ringingCall;
            } else if (outgoingCall != null && outgoingCall.isVideoCall()) {
                vtCall = outgoingCall;
            } else if (activeCall != null && activeCall.isVideoCall()) {
                vtCall = activeCall;
            } else if (holdCall != null && holdCall.isVideoCall()) {
                vtCall = holdCall;
            } else if (disconnectingCall != null && disconnectingCall.isVideoCall()) {
                vtCall = disconnectingCall;
            } else if (disconnectedCall != null && disconnectedCall.isVideoCall()) {
                vtCall = disconnectedCall;
            }
        }
        Log.d(TAG, "getVTCall()... vtCall: " + vtCall);
        return vtCall;
    }

    /**
     * The function to judge whether has VT outgoing call based on current CallList.
     * @return true for yes, false for no
     */
    public static boolean isVTOutgoing() {
        boolean isVTOutgoing = false;
        if (FeatureOptionWrapper.isSupportVT()) {
            Call outgoingCall = CallList.getInstance().getOutgoingCall();
            if (outgoingCall != null) {
                isVTOutgoing = outgoingCall.isVideoCall();
            }
        }
        Log.d(TAG, "isVTOutgoing()... isVTOutgoing: " + isVTOutgoing);
        return isVTOutgoing;
    }

    /**
     * The function to judge whether has ringing VT call based on current CallList.
     * @return true for yes, false for no
     */
    public static boolean isVTRinging() {
        boolean isVTRinging = false;
        if (FeatureOptionWrapper.isSupportVT()) {
            Call ringingCall = CallList.getInstance().getIncomingCall();
            if (ringingCall != null) {
                isVTRinging = ringingCall.isVideoCall();
            }
        }
        Log.d(TAG, "isVTRinging()... isVTRinging: " + isVTRinging);
        return isVTRinging;
    }

    /**
     * The function to judge whether VT is totally idle based on current CallList.
     * @return true for yes, false for no
     */
    public static boolean isVTIdle() {
        boolean isVTIdle = true;
        if (FeatureOptionWrapper.isSupportVT() && CallList.getInstance().existsLiveCall() ) {
            Call ringingCall = CallList.getInstance().getIncomingCall();
            Call activeCall = CallList.getInstance().getActiveCall();
            Call holdCall = CallList.getInstance().getBackgroundCall();
            Call outgoingCall = CallList.getInstance().getOutgoingCall();
            Call disconnectingCall = CallList.getInstance().getDisconnectingCall();
            Call disconnectedCall = CallList.getInstance().getDisconnectedCall();

            if (ringingCall != null && ringingCall.isVideoCall()) {
                isVTIdle = false;
            }
            if (isVTIdle && activeCall != null && activeCall.isVideoCall()) {
                isVTIdle = false;
            }
            if (isVTIdle && holdCall != null && holdCall.isVideoCall()) {
                isVTIdle = false;
            }
            if (isVTIdle && outgoingCall != null && outgoingCall.isVideoCall()) {
                isVTIdle = false;
            }
            if (isVTIdle && disconnectingCall != null && disconnectingCall.isVideoCall()) {
                isVTIdle = false;
            }
            if (isVTIdle && disconnectedCall != null && disconnectedCall.isVideoCall()) {
                isVTIdle = false;
            }
        }
        Log.d(TAG, "isVTIdle()... isVTIdle: " + isVTIdle);
        return isVTIdle;
    }

    /**
     * The function to judge whether has active VT call based on current CallList.
     * Note: here we only judge the active call.
     * @return true for yes, false for no
     */
    public static boolean isVTActive() {
        boolean isVTActive = false;
        if (FeatureOptionWrapper.isSupportVT()) {
            Call activeCall = CallList.getInstance().getActiveCall();
            if (activeCall != null) {
                isVTActive = activeCall.isVideoCall();
            }
        }
        Log.d(TAG, "isVTActive()... isVTActive: " + isVTActive);
        return isVTActive;
    }

    /**
     * The function to judge whether exist non-Video call for now.
     * @return
     */
    public static boolean existNonVTCall() {
        boolean existNonVTCall = false;
        Collection<Call> callList = CallList.getInstance().getCallMap().values();
        if (callList != null) {
            for (Call call : callList) {
                if (!call.isVideoCall()) {
                    existNonVTCall = true;
                    break;
                }
            }
        }
        Log.d(TAG, "existNonVTCall()... existNonVTCall: " + existNonVTCall);
        return false;
    }

    /**
     * VT screen mode for updating VT UI
     */
    public static enum VTScreenMode {
        VT_SCREEN_CLOSE,
        VT_SCREEN_OPEN,
        VT_SCREEN_WAITING
        // VT_SCREEN_WAITING is a transient mode, for the purpose that sometimes we need
        // change mode but keep the VT canvas visibility for a while
        // Eg, when VT drop back dialog shown, all calls will got to IDLE, but now we should still show VT UI.
    }

    /**
     * get VT screen mode based on correct CallList.
     * only when we can surely know it should be OPEN or CLOSE, we set it as OPEN or CLOSE mode, otherwise we keep previous mode.
     * @return
     */
    public static VTScreenMode getVTScreenMode() {
        VTScreenMode vtScreenMode = VTScreenMode.VT_SCREEN_WAITING;
        if (InCallPresenter.getInstance().getInCallState() == InCallState.INCOMING) {
            vtScreenMode = VTScreenMode.VT_SCREEN_CLOSE;
        } else if (isVTOutgoing() || isVTActive()) {
            vtScreenMode = VTScreenMode.VT_SCREEN_OPEN;
        } else if (existNonVTCall()) {
            vtScreenMode = VTScreenMode.VT_SCREEN_CLOSE;
        } else {
            vtScreenMode = VTScreenMode.VT_SCREEN_WAITING;
        }
        return vtScreenMode;
    }

    /**
     * handle VT auto drop back.
     * @param context
     * @param call
     */
    public static void handleAutoDropBack(Context context, Call call) {
        Log.d(TAG, "handleAutoDropBack, check whether drop back~~");
        if (call == null) {
            return;
        }
        int resId = VTUtils.getResIdForVTReCallDialog(call.getDisconnectCause());
        if (resId != INVALID_RES_ID && context != null) {
            Log.d(TAG, "make auto drop back voice recall, resId = " + resId);
            Toast.makeText(context, context.getResources().getString(R.string.vt_voice_connecting),
                    Toast.LENGTH_LONG).show();
            makeVoiceReCall(context, call.getNumber(), call.getSlotId());
        }
    }

    public static final String EXTRA_SLOT_ID = "com.android.phone.extra.slot";
    public static final String EXTRA_VT_MAKE_VOICE_RECALL = "com.android.phone.extra.vt_make_voice_recall";
    public static final String EXTRA_INTERNATIONAL_DIAL_OPTION = "com.android.phone.extra.international";
    public static final int INTERNATIONAL_DIAL_OPTION_IGNORE = 2;

    /**
     * actually make voice re-call.
     * @param context
     * @param number
     * @param slot
     */
    public static void makeVoiceReCall(Context context, final String number, final int slot) {
        Log.d(TAG, "makeVoiceReCall(), number is " + number + " slot is " + slot);

        /// For ALPS01315489 @{
        // here we sure will display voice call UI, so clear VT disconnect call to make sure VT call UI won't show again.
        CallList.getInstance().clearDisconnectStateForVT();
        /// @}

        final Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null));
        intent.putExtra(EXTRA_SLOT_ID, slot);
        intent.putExtra(EXTRA_INTERNATIONAL_DIAL_OPTION, INTERNATIONAL_DIAL_OPTION_IGNORE);
        intent.putExtra(EXTRA_VT_MAKE_VOICE_RECALL, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * get corresponding msg to show when we should show Error dialog to user, based on the disconnect cause.
     * @param cause
     * @return
     */
    public static int getResIdForVTErrorDialog(Call.DisconnectCause cause) {
        int resId = INVALID_RES_ID;

        if (cause == Call.DisconnectCause.UNOBTAINABLE_NUMBER
                || cause == Call.DisconnectCause.INVALID_NUMBER_FORMAT
                || cause == Call.DisconnectCause.INVALID_NUMBER) {
            resId = R.string.callFailed_unobtainable_number;
        } else if (cause == Call.DisconnectCause.CM_MM_RR_CONNECTION_RELEASE) {
            resId = R.string.vt_network_unreachable;
        } else if (cause == Call.DisconnectCause.NO_ROUTE_TO_DESTINATION
                || cause == Call.DisconnectCause.CALL_REJECTED
                || cause == Call.DisconnectCause.FACILITY_REJECTED
                || cause == Call.DisconnectCause.CONGESTION
                || cause == Call.DisconnectCause.SERVICE_NOT_AVAILABLE
                || cause == Call.DisconnectCause.BEARER_NOT_IMPLEMENT
                || cause == Call.DisconnectCause.FACILITY_NOT_IMPLEMENT
                || cause == Call.DisconnectCause.RESTRICTED_BEARER_AVAILABLE
                || cause == Call.DisconnectCause.OPTION_NOT_AVAILABLE
                || cause == Call.DisconnectCause.ERROR_UNSPECIFIED) {
            resId = R.string.vt_iot_error_01;
        } else if (cause == Call.DisconnectCause.BUSY) {
            resId = R.string.vt_iot_error_02;
        } else if (cause == Call.DisconnectCause.NO_USER_RESPONDING
                || cause == Call.DisconnectCause.USER_ALERTING_NO_ANSWER) {
            resId = R.string.vt_iot_error_03;
        } else if (cause == Call.DisconnectCause.SWITCHING_CONGESTION) {
            resId = R.string.vt_iot_error_04;
        }
        return resId;
    }

    /**
     * get corresponding message to show when we should show voice-re-dial dialog, based on the disconnect cause.
     * @param cause
     * @return
     */
    public static int getResIdForVTReCallDialog(Call.DisconnectCause cause) {
        int resId = INVALID_RES_ID;

        if (cause == Call.DisconnectCause.INCOMPATIBLE_DESTINATION) {
            resId = R.string.callFailed_dsac_vt_incompatible_destination;
        } else if (cause == Call.DisconnectCause.RESOURCE_UNAVAILABLE) {
            resId = R.string.callFailed_dsac_vt_resource_unavailable;
        } else if (cause == Call.DisconnectCause.BEARER_NOT_AUTHORIZED) {
            resId = R.string.callFailed_dsac_vt_bear_not_authorized;
        } else if (cause == Call.DisconnectCause.BEARER_NOT_AVAIL) {
            resId = R.string.callFailed_dsac_vt_bearer_not_avail;
        } else if (cause == Call.DisconnectCause.NO_CIRCUIT_AVAIL) {
            resId = R.string.callFailed_dsac_vt_bearer_not_avail;
        }
        return resId;
    }

    public static void setVTVisible(final boolean bIsVisible) {
        Log.d(TAG, "setVTVisible()... bIsVisible: " + bIsVisible);
        if (bIsVisible) {
            if (VTUIFlags.getInstance().mVTSurfaceChangedH && VTUIFlags.getInstance().mVTSurfaceChangedL) {
                Log.d(TAG, "setVTVisible(true)...");
                CallCommandClient.getInstance().setVTVisible(true);
            }
        } else {
            CallCommandClient.getInstance().setVTVisible(false);
        }
    }

    /// For VT time count. @{
    private static String[] sNumbersNone = { "12531", "+8612531" };
    private static String[] sNumbersDefault = { "12535", "13800100011", "+8612535", "+8613800100011" };

    public static enum VTTimingMode {
        VT_TIMING_NONE, /* VT_TIMING_SPECIAL, */VT_TIMING_DEFAULT
    }

    /**
     * Check video call time mode according to phone number
     * @param number phone number
     * @return video call time mode
     */
    public static VTTimingMode checkVTTimingMode(String number) {
        Log.d(TAG,"checkVTTimingMode - number:" + number);

        ArrayList<String> arrayListNone = new ArrayList<String>(Arrays.asList(sNumbersNone));
        ArrayList<String> arrayListDefault = new ArrayList<String>(Arrays.asList(sNumbersDefault));

        if (arrayListNone.indexOf(number) >= 0) {
            Log.d(TAG,"checkVTTimingMode - return:" + VTTimingMode.VT_TIMING_NONE);
            return VTTimingMode.VT_TIMING_NONE;
        }

        if (arrayListDefault.indexOf(number) >= 0) {
            Log.d(TAG,"checkVTTimingMode - return:" + VTTimingMode.VT_TIMING_DEFAULT);
            return VTTimingMode.VT_TIMING_DEFAULT;
        }

        return VTTimingMode.VT_TIMING_DEFAULT;
    }
    /// @}

}
