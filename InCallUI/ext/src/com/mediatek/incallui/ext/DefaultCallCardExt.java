package com.mediatek.incallui.ext;

import android.content.Context;
import android.view.View;

import com.android.services.telephony.common.Call;


public class DefaultCallCardExt implements ICallCardExt {
    @Override
    public void onViewCreated(Context context, View rootView) {
        // do nothing
    }

    @Override
    public void onStateChange(Call call) {
        // do nothing
    }

    @Override
    public void updatePrimaryDisplayInfo(Call call) {
        // do nothing
    }
}
