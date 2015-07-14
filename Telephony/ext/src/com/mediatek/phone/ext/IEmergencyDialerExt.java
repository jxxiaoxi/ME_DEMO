package com.mediatek.phone.ext;

import android.app.Activity;

public interface IEmergencyDialerExt {

    void onCreate(Activity activity, IEmergencyDialerCallback emergencyDialer);

    void updateDialAndDeleteButtonStateEnabledAttr();

    boolean placeCall(String lastNumber);

    void onDestroy();
}
