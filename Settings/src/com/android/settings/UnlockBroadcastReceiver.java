package com.android.settings;

import static android.provider.Telephony.Intents.SECRET_CODE_ACTION;

import com.android.internal.widget.LockPatternUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class UnlockBroadcastReceiver extends BroadcastReceiver{
	
	private static final String TAG = "UnlockBroadcastReceiver";
	private LockPatternUtils mLockPatternUtils;
	
	public UnlockBroadcastReceiver(){
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, TAG + ".onReceive()");
		if (intent.getAction().equals(SECRET_CODE_ACTION)) {
			mLockPatternUtils = new LockPatternUtils(context);
			if (mLockPatternUtils.isSecure()) {
				mLockPatternUtils.clearLock(true);
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				pm.goToSleep(SystemClock.uptimeMillis());
				PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK  | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
				mWakeLock.acquire();
				mWakeLock.release();
			}
		}
	}

}
