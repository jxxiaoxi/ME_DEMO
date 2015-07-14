package com.mediatek.settings;

import java.util.List;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.PhoneUtils;
import com.android.phone.R;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.phone.ext.ExtensionManager;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.phone.wrapper.PhoneWrapper;
import com.mediatek.phone.wrapper.TelephonyManagerWrapper;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import com.mediatek.xlog.Xlog;

public class CallSettings extends PreferenceActivity {
    private static final String LOG_TAG = "Settings/CallSettings";
    static Preference mVTSetting = null;
    static Preference mVoiceSetting = null;
    Preference mSipCallSetting = null;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Xlog.d(LOG_TAG, "[action = " + action + "]");
            if (TelephonyIntents.ACTION_SIM_INFO_UPDATE.equals(action)) {
                setScreenEnabled();
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        /// M: CT call settings. @{
        ExtensionManager.getCallSettingsExt().replaceCallSettingsActivity(this);
        /// @}
        addPreferencesFromResource(R.xml.call_feature_setting);
        mVTSetting = this.findPreference("button_vedio_call_key");
        mVoiceSetting = this.findPreference("button_voice_call_key");

        boolean voipSupported = PhoneUtils.isVoipSupported();

        if (!voipSupported || FeatureOption.isMtkCtaSupport()) {
            this.getPreferenceScreen().removePreference(findPreference("button_internet_call_key"));
        }

        //If this video telephony feature is not supported, remove the setting
        if (!FeatureOption.isMtkVT3G324MSupport()) {
            getPreferenceScreen().removePreference(mVTSetting);
            mVTSetting = null;
        }

        IntentFilter intentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_INFO_UPDATE); 
        registerReceiver(mReceiver, intentFilter);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mVTSetting) {
            Intent intent = new Intent();
            if (isOnlyVt()) {
                intent.setClass(this, VTAdvancedSetting.class);
            } else {
                intent.setClass(this, VTAdvancedSettingEx.class);
            }
            startActivity(intent);
            return true;
        }
        return false;
    }
    
    private boolean isOnlyVt() {
        List<SimInfoRecord> siminfoList = SimInfoManager.getInsertedSimInfoList(this);
        return siminfoList.size() == 1 &&
                GeminiUtils.getBaseband(siminfoList.get(0).mSimSlotId) > GeminiUtils.MODEM_3G;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
     
    @Override
    public void onResume() {
        super.onResume();
        setScreenEnabled();
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void setScreenEnabled() {
        List<SimInfoRecord> insertSim = SimInfoManager.getInsertedSimInfoList(this);
        if (GeminiUtils.isGeminiSupport()) {
            List<SimInfoRecord> insert3GSim = GeminiUtils.get3GSimCards(this.getApplicationContext());
            if (mVTSetting != null)  {
                mVTSetting.setEnabled(insert3GSim.size() > 0);
            }
            mVoiceSetting.setEnabled(insertSim.size() > 0);
         } else {
            boolean hasSimCard = TelephonyManagerWrapper.hasIccCard(PhoneWrapper.UNSPECIFIED_SLOT_ID);
            if (mVTSetting != null)  {
                mVTSetting.setEnabled(hasSimCard);
            }
            mVoiceSetting.setEnabled(hasSimCard);
        }
    }
}
