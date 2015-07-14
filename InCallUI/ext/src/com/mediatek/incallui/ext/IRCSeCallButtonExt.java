package com.mediatek.incallui.ext;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;

import com.android.services.telephony.common.Call;

public interface IRCSeCallButtonExt {
    /**
     * called when CallButtonFragment view created.
     * customize this view
     * @param context host Context
     * @param rootView the CallButtonFragment view
     */
    void onViewCreated(Context context, View rootView);

    /**
     * called when call state changed
     * notify the foreground call to plug-in
     * @param call current foreground call
     * @param callMap a mapping of callId -> call for all current calls
     */
    void onStateChange(Call call, HashMap<Integer, Call> callMap);

    /**
     * called when preparing options menu, based on InCallActivity.onPrepareOptionsMenu
     * @param menu the menu of InCallActivity
     * @param call the CallButton's mCall, the call whose state was latest changed
     */
    public void setupMenuItems(Menu menu, Call call);

    /**
     * called when popup menu item in CallButtonFragment clicked.
     * involved popup menus such as audio mode, vt
     * @param menuItem the clicked menu item
     * @return true if this menu event has already handled by plugin
     */
    boolean handleMenuItemClick(MenuItem menuItem);
}
