package com.mediatek.phone.ext;

import android.app.Activity;
import com.android.internal.telephony.Phone;

public interface ICommonCallback {
    Activity getHostActivity();
    Phone getHostPhone();
}
