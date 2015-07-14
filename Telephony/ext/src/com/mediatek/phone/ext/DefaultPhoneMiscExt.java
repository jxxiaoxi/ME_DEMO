package com.mediatek.phone.ext;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.android.internal.telephony.Phone;
import com.mediatek.calloption.CallOptionHandlerFactory;

public class DefaultPhoneMiscExt implements IPhoneMiscExt {

    @Override
    public boolean onPhoneGlobalsBroadcastReceive(Context context, Intent intent) {
        return false;
    }

    @Override
    public void onPhoneGlobalsCreate(Context context, Phone phone) {
        // do nothing
    }

    @Override
    public void createHandlerPrototype(CallOptionHandlerFactory callOptionHandlerFactory) {
        // do nothing
    }

    @Override
    public String changeTextContainingSim(String originalText, int slotId) {
        return originalText;
    }

    @Override
    public boolean publishBinderDirectly() {
        return false;
    }

    @Override
    public void customizeNetworkSelectionNotification(Notification notification, String titleText, String expandedText, PendingIntent pi) {
        // do nothing
    }
}
