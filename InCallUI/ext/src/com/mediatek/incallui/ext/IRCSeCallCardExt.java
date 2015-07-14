package com.mediatek.incallui.ext;

import android.content.Context;
import android.view.View;
import com.android.services.telephony.common.Call;

public interface IRCSeCallCardExt {
    /**
     * called when CallCard view created, based on CallCardFragment
     * lifecycle
     * @param context host context
     * @param rootView the CallCardFragment view
     */
    void onViewCreated(Context context, View rootView);

    /**
     * called when call state changed, based on onStateChange
     * @param call the call who was changed
     */
    void onStateChange(Call call);

}
