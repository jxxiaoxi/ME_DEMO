package com.mediatek.phone.ext;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.android.internal.telephony.Phone;
import com.mediatek.calloption.CallOptionHandlerFactory;

public interface IPhoneMiscExt {

    /**
     * plugin should check the hostReceiver class, to check whether this
     * API call belongs to itself
     * if (hostReceiver.getClass().getSimpleName() == "PhoneGlobalsBroadcastReceiver") {}
     *
     * @param context the received context
     * @param intent the received intent
     * @return true if plug-in decide to skip host execution
     */
    boolean onPhoneGlobalsBroadcastReceive(Context context, Intent intent);

    /**
     * called when PhoneGlobals.onCreate()
     *
     * @param context the application context
     * @param phone the Phone instance
     */
    void onPhoneGlobalsCreate(Context context, Phone phone);

    /**
     * called when PhoneCallOptionHandlerFactory.createHandlerPrototype()
     * if plug-in need add new CallOptionHandler, it can do in this API
     *
     * @param callOptionHandlerFactory
     */
    void createHandlerPrototype(CallOptionHandlerFactory callOptionHandlerFactory);

    /**
     * called before a text containing sub-string "SIM" shows to end user.
     * usually be shown via Toast, Dialog, etc
     * plug-in can change the text if necessary. e.g. replace "SIM" with "UIM"
     *
     * @param originalText the original text string
     * @param slotId the slotId according to the text
     * @return the text which would be shown to end user.
     */
    String changeTextContainingSim(String originalText, int slotId);

    /**
     * called in NetworkQueryService.onBind()
     * google default behavior defined a LocalBinder to prevent  NetworkQueryService
     * being accessed by outside components.
     * but, there is a situation that plug-in need the mBinder. LocalBinder can't be
     * accessed by plug-in.
     * it would be risky if plug-in really returns true directly without any security check.
     * if this happen, other 3rd party component can access this binder, too.
     *
     * @return true if Plug-in need to get the binder
     */
    boolean publishBinderDirectly();

    /**
     * called when the NetworkSelect notification is about to show.
     * plugin can customize the notification text or PendingIntent
     *
     * @param notification the notification to be shown
     * @param titleText the title
     * @param expandedText the expanded text
     * @param pi the PendingIntent when click the notification
     */
    void customizeNetworkSelectionNotification(Notification notification, String titleText, String expandedText, PendingIntent pi);
}
