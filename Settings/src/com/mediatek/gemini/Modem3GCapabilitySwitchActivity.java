package com.mediatek.gemini;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.mediatek.telephony.SimInfoManager;

public class Modem3GCapabilitySwitchActivity extends Activity {
	private static final String TAG = "Modem3GCapabilitySwitchActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TAG + ".onCreate()");
        int simNum = SimInfoManager.getInsertedSimCount(this);
        Log.d(TAG, TAG + ".onCreate() simNum = " + simNum);
        
        if (simNum > 0) {
        	Intent intent3GSwitch = new Intent();
            intent3GSwitch.setClassName("com.android.phone", "com.mediatek.settings.Modem3GCapabilitySwitch");
            intent3GSwitch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent3GSwitch);
        } else {
        	Toast.makeText(this, getString(R.string.mobile_insert_sim_card), Toast.LENGTH_SHORT).show();
        }
        
        finish();
        
    }
}
