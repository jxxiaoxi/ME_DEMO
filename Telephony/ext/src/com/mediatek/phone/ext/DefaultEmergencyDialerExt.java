package com.mediatek.phone.ext;

import android.app.Activity;

public class DefaultEmergencyDialerExt implements IEmergencyDialerExt {
    @Override
    public void onCreate(Activity activity, IEmergencyDialerCallback emergencyDialer) {
        // do nothing
    }

    @Override
    public void updateDialAndDeleteButtonStateEnabledAttr() {
        // do nothing
    }

    @Override
    public boolean placeCall(String lastNumber) {
        return false;
    }

    @Override
    public void onDestroy() {
        // do nothing
    }
}
