package com.mediatek.phone.ext;

import android.content.Context;
import android.content.Intent;

import com.android.internal.telephony.ITelephony;
import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.calloption.CallOptionBaseHandler;

import java.util.LinkedList;

public class DefaultPhoneCallOptionHandlerExt implements IPhoneCallOptionHandlerExt {

    @Override
    public boolean doCallOptionHandle(LinkedList<CallOptionBaseHandler> callOptionHandlerList,
                                      Context activityContext, Context applicationContext, Intent intent,
                                      CallOptionBaseHandler.ICallOptionResultHandle resultHandler,
                                      CellConnMgr cellConnMgr, ITelephony telephonyInterface,
                                      boolean isMultipleSim, boolean is3GSwitchSupport) {
        return false;
    }
}
