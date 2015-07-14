package com.mediatek.phone.ext;

import android.os.Message;
import com.android.internal.app.AlertController;
import com.android.internal.telephony.MmiCode;

public class DefaultMmiCodeExt implements IMmiCodeExt {
    @Override
    public void onMmiDailogShow(Message cancelMessage) {
        // do nothing
    }

    @Override
    public boolean onUssdDialogShow(int ussdType, AlertController alertController) {
        return false;
    }

    @Override
    public void configBeforeMmiDialogShow(MmiCode mmiCode, int ussdType, int slotId) {
        // do nothing
    }

    @Override
    public boolean skipPlayingUssdTone() {
        return false;
    }
}
