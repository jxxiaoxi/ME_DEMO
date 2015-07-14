package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import com.mediatek.common.featureoption.FeatureOption;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Google Setup Wizard APP will set screen off timeout to 121000 milliseconds
        // We need to reset it as the default value
        try {
            int timeout = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
            if (121000 == timeout) {
if (FeatureOption.CUSTOM_HAIER_ES_US) 
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, 30000);
else
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, 60000);
            }
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
    }
}
