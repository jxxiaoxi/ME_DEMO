package com.mediatek.incallui.ext;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;

import com.android.services.telephony.common.Call;

public class DefaultRCSeCallButtonExt implements IRCSeCallButtonExt {
    @Override
    public void onViewCreated(Context context, View rootView) {
        // do nothing
    }

    @Override
    public void onStateChange(Call call, HashMap<Integer, Call> callMap) {
        // do nothing
    }

    @Override
    public void setupMenuItems(Menu menu, Call call) {
        // do nothing
    }

    @Override
    public boolean handleMenuItemClick(MenuItem menuItem) {
        return false;
    }
}
