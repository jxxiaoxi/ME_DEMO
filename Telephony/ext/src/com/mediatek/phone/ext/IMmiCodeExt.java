package com.mediatek.phone.ext;

import android.os.Message;
import com.android.internal.app.AlertController;
import com.android.internal.telephony.MmiCode;

public interface IMmiCodeExt {
    /**
     * use the cancel message will dismiss the MMI/USSD dialog
     * @param cancelMessage
     */
    void onMmiDailogShow(Message cancelMessage);

    /**
     * called when UssdAlertActivity.onResume()
     * @param ussdType
     * @param alertController
     * @return true to skip all host flow in onResume
     */
    boolean onUssdDialogShow(int ussdType, AlertController alertController);

    void configBeforeMmiDialogShow(MmiCode mmiCode, int ussdType, int slotId);

    boolean skipPlayingUssdTone();
}
