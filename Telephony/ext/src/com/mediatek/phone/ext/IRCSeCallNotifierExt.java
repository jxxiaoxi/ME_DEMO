package com.mediatek.phone.ext;

import android.content.Context;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Connection;

public interface IRCSeCallNotifierExt {
    void onPhoneStateChanged(CallManager cm, Context context);
    boolean onDisconnect(Connection cn);
}
