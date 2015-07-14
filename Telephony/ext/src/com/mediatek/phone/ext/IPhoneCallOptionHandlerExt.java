package com.mediatek.phone.ext;

import android.content.Context;
import android.content.Intent;

import com.android.internal.telephony.ITelephony;
import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.calloption.CallOptionBaseHandler;

import java.util.LinkedList;

public interface IPhoneCallOptionHandlerExt {
    /**
     * called when start outgoing call, we should gave plugin the opportunity to modify
     * the parameter during the call process, now we need this function to mofify the 
     * intent.
     *
     * @param callOptionHandlerList the CallOptionBaseHandler list
     * @param activityContext the context of the activity
     * @param applicationContext the context of the application
     * @param intent the call intent
     * @param resultHandler the ICallOptionResultHandle
     * @param cellConnMgr the CellConnMgr
     * @param telephonyInterface the ITelephony interface
     * @param isMultipleSim to indicate if is the gemini project
     * @param is3GSwitchSupport to indicate if can 3G switch                   
     */
    public boolean doCallOptionHandle(LinkedList<CallOptionBaseHandler> callOptionHandlerList,
                                      Context activityContext, Context applicationContext, Intent intent,
                                      CallOptionBaseHandler.ICallOptionResultHandle resultHandler,
                                      CellConnMgr cellConnMgr, ITelephony telephonyInterface,
                                      boolean isMultipleSim, boolean is3GSwitchSupport);
}
