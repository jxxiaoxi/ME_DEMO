package com.mediatek.incallui.ext;

import android.app.Activity;
import android.os.Bundle;

public class DefaultRCSeInCallExt implements IRCSeInCallExt {
    @Override
    public void onCreate(Bundle icicle, Activity inCallActivity, IInCallScreen iInCallScreen) {
        // do nothing
    }

    @Override
    public void onDestroy(Activity inCallActivity) {
        // do nothing
    }
}
