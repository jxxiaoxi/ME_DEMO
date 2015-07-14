package com.mediatek.settings;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.cdma.TtyIntent;
import com.android.phone.PhoneUtils;
import com.android.phone.R;
import com.mediatek.phone.GeminiConstants;
import com.mediatek.phone.PhoneLog;
import com.mediatek.phone.ext.ExtensionManager;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.phone.wrapper.PhoneWrapper;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import android.os.SystemProperties;

public class OthersSettings extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    private static final String BUTTON_OTHERS_FDN_KEY     = "button_fdn_key";
    private static final String BUTTON_OTHERS_MINUTE_REMINDER_KEY    = "minute_reminder_key";
    private static final String BUTTON_OTHERS_DUAL_MIC_KEY = "dual_mic_key";
    private static final String BUTTON_TTY_KEY    = "button_tty_mode_key";
    private static final String BUTTON_INTER_KEY    = "international_dialing_key";
    /// Add for [ANC]
    private static final String BUTTON_ANC_KEY    = "button_anc_key";
    /// M: For ALPS01062292. @{
    // used to save or obtain the key of target preference from Bundle.
    private static final String TARGET_PREFERENCE_KEY = "target_preference_key";
    /// @}
    /// Add for [MagiConference] @
    private CheckBoxPreference mButtonMagiConference;
    private static final String BUTTON_MAGI_CONFERENCE_KEY = "button_magi_conference_key";
    /// @}

    private static final String LOG_TAG = "Settings/OthersSettings";
    private Preference mButtonFdn;
    private CheckBoxPreference mButtonMr;
    private CheckBoxPreference mButtonDualMic;
    private ListPreference mButtonTTY;
    private CheckBoxPreference mButtonInter;
    /// Add for [ANC] (Active Noise Reduction)
    private CheckBoxPreference mButtonANC;
	// add by liuwei for calllock
	private CheckBoxPreference mButtonLock;
	private static final String BUTTON_OTHERS_LOCK_KEY = "other_lock_key";

    private static final int DEFAULT_INTER_DIALING_VALUE = 0;
    private static final int INTER_DIALING_ON = 1;
    private static final int INTER_DIALING_OFF = 0;
    
    private int mSlotId = 0;
    private PreCheckForRunning mPreCfr = null;
    private Preference mTargetPreference;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            PhoneLog.d(LOG_TAG, "[action = " + action + "]");
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                setScreenEnabled();
            } else if (TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED.equals(action)) {
                setScreenEnabled();
            } else if (TelephonyIntents.ACTION_SIM_INFO_UPDATE.equals(action)) {
                setScreenEnabled();
            }
        }
    };
    private static final String TAG_OTHER_SETTINGS = "other_settings";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.mtk_others_settings);
        mButtonFdn = findPreference(BUTTON_OTHERS_FDN_KEY);
        mButtonMr = (CheckBoxPreference)findPreference(BUTTON_OTHERS_MINUTE_REMINDER_KEY);
        mButtonDualMic = (CheckBoxPreference)findPreference(BUTTON_OTHERS_DUAL_MIC_KEY);
        mButtonInter = (CheckBoxPreference)findPreference(BUTTON_INTER_KEY);
        
		// add by liuwei for calllock
		mButtonLock = (CheckBoxPreference) findPreference(BUTTON_OTHERS_LOCK_KEY);
		if (!SystemProperties.getBoolean("ro.phone.lockscreen_for_yl", false)) {
			PreferenceScreen screen = this.getPreferenceScreen();
			PreferenceScreen category = (PreferenceScreen) screen
					.findPreference("other_setting_key");
			category.removePreference((Preference) mButtonLock);
			Settings.System.putInt(getContentResolver(), "other_lock_key", 0);
		}

        if (!PhoneUtils.isSupportFeature("DUAL_MIC")) {
            this.getPreferenceScreen().removePreference(mButtonDualMic);
        }

        if (mButtonMr != null) {
            mButtonMr.setOnPreferenceChangeListener(this);
        }

        if (mButtonDualMic != null) {
            mButtonDualMic.setOnPreferenceChangeListener(this);
        }
        mButtonTTY = (ListPreference) findPreference(BUTTON_TTY_KEY);

        if (mButtonTTY != null) {
            if (PhoneUtils.isSupportFeature("TTY")) {
                mButtonTTY.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mButtonTTY);
                mButtonTTY = null;
            }
        }
        if (mButtonInter != null) {
            mButtonInter.setOnPreferenceChangeListener(this);
            int checkedStatus = Settings.System.getInt(getContentResolver(),
                    Settings.System.INTER_DIAL_SETTING, DEFAULT_INTER_DIALING_VALUE);
            mButtonInter.setChecked(checkedStatus != 0);
            PhoneLog.d(LOG_TAG, "onResume isChecked in DB:" + (checkedStatus != 0));
        }

        mPreCfr = new PreCheckForRunning(this);
		// add by liuwei for calllock
		if (mButtonLock != null) {
			mButtonLock.setOnPreferenceChangeListener(this);
		}

        /// Add for [ANC] @{
        mButtonANC = (CheckBoxPreference)findPreference(BUTTON_ANC_KEY);
        if (!PhoneUtils.isANCSupport() ) {
            this.getPreferenceScreen().removePreference(mButtonANC);
        }
        mButtonANC.setOnPreferenceChangeListener(this);
        /// @}
        /// Add for [MagiConference] @{
        mButtonMagiConference = (CheckBoxPreference)findPreference(BUTTON_MAGI_CONFERENCE_KEY);
        if (mButtonMagiConference != null) {
            if (PhoneUtils.isMagiConferenceSupport()) {
                PhoneLog.d(LOG_TAG, "[MagiConference support!]");
                mButtonMagiConference.setChecked(PhoneUtils.isMagiConferenceEnable());
                mButtonMagiConference.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mButtonMagiConference);
                mButtonMagiConference = null;
            }
        }
        /// @}
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        registerReceiver(mReceiver, intentFilter);

        ExtensionManager.getCallSettingsExt().initOtherSettings(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mButtonFdn == preference) {
            mTargetPreference = preference;
            mSlotId = GeminiUtils.getSlotId(this, preference.getTitle().toString(), android.R.style.Theme_Holo_Light_DialogWhenLarge);
            if (mSlotId != GeminiUtils.UNDEFINED_SLOT_ID) {
                GeminiUtils.startActivity(mSlotId, preference, mPreCfr);
            }
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mButtonDualMic) {
            if (mButtonDualMic.isChecked()) {
                PhoneLog.d(LOG_TAG, "onPreferenceChange mButtonDualmic turn on");
                PhoneUtils.setDualMicMode("0");
            } else {
                PhoneLog.d(LOG_TAG, "onPreferenceChange mButtonDualmic turn off");
                PhoneUtils.setDualMicMode("1");
            }
        } else if (preference == mButtonTTY) {
            handleTTYChange(preference, objValue);
        } else if (preference == mButtonInter) {
            if ((Boolean)objValue) {
                Settings.System.putInt(getContentResolver(), Settings.System.INTER_DIAL_SETTING, INTER_DIALING_ON);
            } else {
                Settings.System.putInt(getContentResolver(), Settings.System.INTER_DIAL_SETTING, INTER_DIALING_OFF);
            }
            PhoneLog.d(LOG_TAG, "onPreferenceChange mButtonInter turn :"
                    + Settings.System.getInt(getContentResolver(), Settings.System.INTER_DIAL_SETTING, -1));
            /// Add for [ANC] @{
        } else if (preference == mButtonANC) {
            if (mButtonANC.isChecked()) {
                PhoneLog.d(LOG_TAG, "onPreferenceChange mButtonANC turn on");
                PhoneUtils.setANCEnable(true);
                mButtonANC.setSummary(R.string.anc_off);
            } else {
                PhoneLog.d(LOG_TAG, "onPreferenceChange mButtonANC turn off");
                PhoneUtils.setANCEnable(false);
                mButtonANC.setSummary(R.string.anc_on);
            }
            /// @}
        /// Add for [MagiConference] @
		} else if (preference == mButtonLock) {// add by liuwei for calllock
			if ((Boolean) objValue) {
				Settings.System.putInt(getContentResolver(), "other_lock_key",
						1);
			} else {
				Settings.System.putInt(getContentResolver(), "other_lock_key",
						0);
			}
		} else if (preference == mButtonMagiConference) {
            boolean isChecked = mButtonMagiConference.isChecked();
            PhoneLog.d(LOG_TAG, "onPreferenceChange mButtonMagiConference turn on : " + isChecked);
            PhoneUtils.setMagiConferenceEnable(!isChecked);
        }
        /// @}
        return true;
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

    public void onResume() {
        super.onResume();
        setScreenEnabled();

        if (mButtonTTY != null) {
            int settingsTtyMode = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.PREFERRED_TTY_MODE,
                    Phone.TTY_MODE_OFF);
            mButtonTTY.setValue(Integer.toString(settingsTtyMode));
            updatePreferredTtyModeSummary(settingsTtyMode);
        }
    }
     
    protected void onDestroy() {
        super.onDestroy();
        if (mPreCfr != null) {
            mPreCfr.deRegister();
        }
        unregisterReceiver(mReceiver);
    }

    private void handleTTYChange(Preference preference, Object objValue) {
        int buttonTtyMode;
        buttonTtyMode = Integer.valueOf((String) objValue).intValue();
        int settingsTtyMode = android.provider.Settings.Secure.getInt(
                getContentResolver(),
                android.provider.Settings.Secure.PREFERRED_TTY_MODE, Phone.TTY_MODE_OFF);
        PhoneLog.d(LOG_TAG, "handleTTYChange: requesting set TTY mode enable (TTY) to" +
                Integer.toString(buttonTtyMode));

        if (buttonTtyMode != settingsTtyMode) {
            switch(buttonTtyMode) {
            case Phone.TTY_MODE_OFF:
            case Phone.TTY_MODE_FULL:
            case Phone.TTY_MODE_HCO:
            case Phone.TTY_MODE_VCO:
                android.provider.Settings.Secure.putInt(getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_TTY_MODE, buttonTtyMode);
                break;
            default:
                buttonTtyMode = Phone.TTY_MODE_OFF;
            }

            mButtonTTY.setValue(Integer.toString(buttonTtyMode));
            updatePreferredTtyModeSummary(buttonTtyMode);
            Intent ttyModeChanged = new Intent(TtyIntent.TTY_PREFERRED_MODE_CHANGE_ACTION);
            ttyModeChanged.putExtra(TtyIntent.TTY_PREFFERED_MODE, buttonTtyMode);
            sendBroadcast(ttyModeChanged);
        }
    }
    
    private void updatePreferredTtyModeSummary(int ttyMode) {
        String [] txts = getResources().getStringArray(R.array.tty_mode_entries);
        switch(ttyMode) {
            case Phone.TTY_MODE_OFF:
            case Phone.TTY_MODE_HCO:
            case Phone.TTY_MODE_VCO:
            case Phone.TTY_MODE_FULL:
                mButtonTTY.setSummary(txts[ttyMode]);
                break;
            default:
                mButtonTTY.setEnabled(false);
                mButtonTTY.setSummary(txts[Phone.TTY_MODE_OFF]);
        }
    }

    private void setScreenEnabled() {
        boolean airplaneModeOn = android.provider.Settings.System.getInt(getContentResolver(),
                android.provider.Settings.System.AIRPLANE_MODE_ON, -1) == 1;

        List<SimInfoRecord> insertSim = SimInfoManager.getInsertedSimInfoList(this);
        if (insertSim.size() == 0) {
            mButtonFdn.setEnabled(false);
        } else if (insertSim.size() == 1) {
            int slotId = insertSim.get(0).mSimSlotId;
            mButtonFdn.setEnabled(!PhoneWrapper.isRadioOffBySlot(slotId, this));
        } else {
            mButtonFdn.setEnabled(true);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhoneLog.d(LOG_TAG, "reqCode=" + requestCode + ",resCode=" + resultCode);
        if (GeminiUtils.REQUEST_SIM_SELECT == requestCode) {
            if (RESULT_OK == resultCode) {
                mSlotId = data.getIntExtra(GeminiConstants.SLOT_ID_KEY, GeminiUtils.UNDEFINED_SLOT_ID);
            }
        }
        PhoneLog.d(LOG_TAG, "mSlot=" + mSlotId);
        if (mSlotId != GeminiUtils.UNDEFINED_SLOT_ID) {
            GeminiUtils.startActivity(mSlotId, mTargetPreference, mPreCfr);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /// M: For ALPS01062292. @{
        // Save the preference and slotId and will use these to restore correct
        // states when Activity onCreate or onRestoreInstanceState.
        if (mTargetPreference != null) {
            outState.putString(TARGET_PREFERENCE_KEY, mTargetPreference.getKey());
        }
        PhoneLog.d(LOG_TAG, "[onSaveInstanceState], mSlotId = " + mSlotId);
        outState.putInt(GeminiConstants.SLOT_ID_KEY, mSlotId);
        /// @}
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        /// M: For ALPS01062292. @{
        String targetPreferenceKey = savedInstanceState.getString(TARGET_PREFERENCE_KEY, "");
        if (!TextUtils.isEmpty(targetPreferenceKey)) {
            PreferenceScreen prefSet = getPreferenceScreen();
            mTargetPreference = prefSet.findPreference(targetPreferenceKey);
        }
        mSlotId = savedInstanceState.getInt(GeminiConstants.SLOT_ID_KEY);
        PhoneLog.d(LOG_TAG, "[onRestoreInstanceState], mSlotId = " + mSlotId);
        /// @}
    }
}
