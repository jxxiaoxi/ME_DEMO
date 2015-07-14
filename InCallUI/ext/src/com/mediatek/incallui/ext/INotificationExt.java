package com.mediatek.incallui.ext;

import com.android.services.telephony.common.Call;

import java.util.List;

public interface INotificationExt {

    /**
     * called when preparing the notification icon.
     * plugin can change the icon via this interface
     *
     * @param defaultResId the default res id, if plug-in decide no to change the icon, should return the default icon
     * @param call         current call who will be shown in status bar
     * @param resIdsInHost plugin will select a resId in this set to show.
     * @return the selected resId
     */
    int getSmallIconResId(int defaultResId, Call call, int[][] resIdsInHost);
}
