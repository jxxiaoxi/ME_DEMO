package com.mediatek.phone.ext;

import android.content.Context;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;

public class DefaultRCSeCallNotifierExt implements IRCSeCallNotifierExt {
    @Override
    public void onPhoneStateChanged(CallManager cm, Context context) {
        //do nothing
    }

    @Override
    public boolean onDisconnect(Connection cn) {
        return false;
    }
}
