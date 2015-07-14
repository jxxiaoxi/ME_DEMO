package com.mediatek.incallui.ext;

import java.util.List;

import com.android.services.telephony.common.Call;

public class DefaultNotificationExt implements INotificationExt {
    @Override
    public int getSmallIconResId(int defaultResId, Call call, int[][] resIdsInHost) {
        return defaultResId;
    }
}
