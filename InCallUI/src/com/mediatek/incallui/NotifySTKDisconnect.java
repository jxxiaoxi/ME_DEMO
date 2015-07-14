package com.mediatek.incallui;

import com.android.incallui.CallList;
import com.android.services.telephony.common.Call;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class NotifySTKDisconnect {

    private static final String LOG_TAG = "NotifySTKDisconnect";
    private static NotifySTKDisconnect sInstance = new NotifySTKDisconnect();

    private static final String STK_BROADCAST_ACTION = "android.intent.action.stk.CALL_DISCONNECTED";
    private static final String STK_SLOT_EXTRA = "sim_id";
    private boolean mShouldNotifySTK = false;
    private int mNotifyStkSlotId = -1;

    public static NotifySTKDisconnect getInstance() {
        if (sInstance == null) {
            sInstance = new NotifySTKDisconnect();
        }
        return sInstance;
    }

    public void clearNotifyStkFlag() {
        mShouldNotifySTK = false;
        mNotifyStkSlotId = -1;
    }

    public void setNotifyStkFlag(int slot) {
        mShouldNotifySTK = true;
        mNotifyStkSlotId = slot;
        log("setShouldNotifySTK(): mNotifyStkSlotId = " + mNotifyStkSlotId);
    }

    public void notifyStkCallDisconnected(Context context) {
        log("notifyStkCallDisconnected(), isNoActiveCall() / mShouldNotifySTK / mNotifyStkSlotId: "
                + isNoActiveCall() + " / " + mShouldNotifySTK + " / " + mNotifyStkSlotId);
        if (context == null) {
            log("context is null when notifyStkCallDisconnected() is called ! ");
        }
        if (isNoActiveCall() && mShouldNotifySTK && mNotifyStkSlotId != -1 && context != null) {
            log("notifyStkCallDisconnected(), done !");
            Intent intent = new Intent(STK_BROADCAST_ACTION);
            intent.putExtra(STK_SLOT_EXTRA, mNotifyStkSlotId);
            context.sendBroadcast(intent);
        }
        clearNotifyStkFlag();
    }

    private boolean isNoActiveCall() {
        boolean isNoActiveCall = false;
        if (CallList.getInstance().getActiveOrBackgroundCall() == null ) {
            isNoActiveCall = true;
        }
        return isNoActiveCall;
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
