package com.mediatek.settings;

import android.app.*;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.PhoneGlobals;
import com.android.phone.R;

import com.mediatek.gemini.simui.SimSelectDialogPreference;
import com.mediatek.phone.ext.IModem3GCapabilitySwitchExt;
import com.mediatek.phone.PhoneLog;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.phone.PhoneInterfaceManagerEx;
import com.mediatek.phone.ext.ExtensionManager;
import com.mediatek.phone.ext.ICommonCallback;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.phone.wrapper.PhoneWrapper;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Modem3GCapabilitySwitch extends PreferenceActivity 
        implements Preference.OnPreferenceChangeListener {

    public static final String SERVICE_LIST_KEY = "preferred_3g_service_key";
    public static final String NETWORK_MODE_KEY = "preferred_network_mode_key";

    private SimSelectDialogPreference mServiceList = null;
    private ListPreference mNetworkMode = null;
    private static final boolean DBG = true;
    private static final String TAG = "Settings/Modem3GCapabilitySwitch";
    
    PhoneInterfaceManagerEx mPhoneMgrEx = null;
    private Phone mPhone;
    private NetWorkHandler mNetworkHandler;
    private StatusBarManager mStatusBarManager = null;
    private ModemSwitchReceiver mSwitchReceiver;
    //As this activity may be destroyed and re-instance, give them a public "progress dialog"
    
    private static final long SIMID_SERVICE_OFF = -1;
    private static final int SIMID_SERVICE_NOT_SET = -2;

    private static final int PROGRESS_DIALOG = 300;
    private static final int SWITCH_TIME_OUT_MSG = 1000;
    ///For 3G switch fail time out set 1 min 
    private static final int SWITCH_TIME_OUT_VALUE = 60000;

    private boolean mIsAirplaneModeOn;
    private static int sInstanceFlag = 0;
    private int mInstanceIndex = 0;
    // smart 3g switch
    private boolean mIsSimSwitchManualChangeAllowed = false;
    private int mManualAllowedSlot = -1;
    private List<SimInfoRecord> mSimInfoList;

    private static final int DIALOG_CAPABILITY_SWITCH = 500;
    private static final int DIALOG_HINT_CHANNGE_DATA = 501;

    private long mCurrentServiceSim;
    private long mSelectedServiceSim;

    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (SWITCH_TIME_OUT_MSG == msg.what) {
                PhoneLog.d("TEST","3G switch time out remove the progress dialog");
                removeDialog(PROGRESS_DIALOG);
                setStatusBarEnableStatus(true);
            }
        }
    }; 

    public Modem3GCapabilitySwitch() {
        mInstanceIndex = ++sInstanceFlag;
        PhoneLog.i(TAG, "Modem3GCapabilitySwitch(), instanceIndex=" + mInstanceIndex);
    }
    
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PhoneLog.d(TAG, "onCreate()");
        addPreferencesFromResource(R.xml.mtk_service_3g_setting);
        // init preference
        initPreference();
        // init variable
        initPhoneAndSimSwitch();
        // init IntentFilter
        IntentFilter intentFilter = initIntentFilter();
        registerReceiver(mSwitchReceiver, intentFilter);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ExtensionManager.getModem3GCapabilitySwitchExt().initPreferenceActivity(this, mCallbackForPlugin);
    }

    protected void onResume() {
        super.onResume();
        PhoneLog.d(TAG, "onResume....");
        long simId = SIMID_SERVICE_NOT_SET;
        /// for 4G switch@{
        int slot = GeminiUtils.get34GCapabilitySIM();
        /// @}
        if (slot == SIMID_SERVICE_OFF) {
            simId = slot;
        } else {
            SimInfoRecord info = SimInfoManager.getSimInfoBySlot(this, slot);
            simId = info != null ? info.mSimInfoId : SIMID_SERVICE_NOT_SET;
        }
        initServiceSwitchPref();
        updateSummarys(simId);
        updateNetworkAndServiceStatus();
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

    private void updateNetworkAndServiceStatus() {
        /// for 4G switch @{
        int slot = GeminiUtils.get34GCapabilitySIM();
        /// @}
        PhoneLog.d(TAG, "updateNetworkMode(), 3G/4G capability slot=" + slot + " mIsAirplaneModeOn=" + mIsAirplaneModeOn);
        /// for 4G switch @{
        boolean locked =  mPhoneMgrEx.isSimSwitchLocked();
        /// @}
        int settingsNetworkMode = android.provider.Settings.Global.getInt(
                        mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE,
                        Phone.NT_MODE_LTE_GSM_WCDMA);
        if (mNetworkMode != null) {
            if (!locked
                    && slot != -1
                    && !PhoneWrapper.isRadioOffBySlot(slot, this)) {
                if (GeminiUtils.isLteSupport()) {
                    boolean isLteDcModeEnable = GeminiUtils.isLteDcModeEnable(slot);
                    if (GeminiUtils.isTddSupportForLte()) {
                        GeminiUtils.updateSglteMode(mNetworkMode, settingsNetworkMode);
                    }
                    PhoneLog.d(TAG, "LTE Try to get preferred network mode for isLteDcModeEnable " + isLteDcModeEnable);
                    mNetworkMode.setEnabled(!mIsAirplaneModeOn && isLteDcModeEnable);
                    if (!PhoneGlobals.getInstance().phoneMgrEx.isAirplanemodeAvailableNow()) {
                        PhoneLog.d(TAG, "updateNetworkMode, now is Switching so direct return");
                        return;
                    }
                    PhoneWrapper.getPreferredNetworkType(mPhone,
                    mNetworkHandler.obtainMessage(NetWorkHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE), slot);
                } else {
                    mNetworkMode.setEnabled(true);
                    if (!PhoneGlobals.getInstance().phoneMgrEx.isAirplanemodeAvailableNow()) {
                        PhoneLog.d(TAG, "updateNetworkMode, now is Switching so direct return");
                        return;
                    }
                    PhoneLog.d(TAG, "Try to get preferred network mode for slot " + slot);
                    PhoneWrapper.getPreferredNetworkType(mPhone,
                            mNetworkHandler.obtainMessage(NetWorkHandler.MESSAGE_GET_PREFERRED_NETWORK_TYPE), slot);
                }
            } else {
                mNetworkMode.setEnabled(false);
                mNetworkMode.setSummary("");
            }

            ExtensionManager.getModem3GCapabilitySwitchExt().updateLteModeStatus(mNetworkMode, null);
        }

        mServiceList.setEnabled(!mIsAirplaneModeOn && !locked);

        if (mIsAirplaneModeOn) {
            dismissDialogs();
        }
    }

    private void initPhoneAndSimSwitch() {
        mPhone = PhoneFactory.getDefaultPhone();
        mPhoneMgrEx = PhoneGlobals.getInstance().phoneMgrEx;
        /// for 4G switch @{
        mIsSimSwitchManualChangeAllowed = mPhoneMgrEx.isSimSwitchManualChangeSlotAllowed();
        /// @}
        PhoneLog.d(TAG, "mIsSimSwitchManualChangeAllowed: " + mIsSimSwitchManualChangeAllowed);
        if (!mIsSimSwitchManualChangeAllowed) {
            /// for 4G switch @{
            mManualAllowedSlot = mPhoneMgrEx.getSimSwitchAllowedSlots();
            /// @}
            PhoneLog.d(TAG, "mManualAllowedSlot: " + mManualAllowedSlot);
        }
        mNetworkHandler = new NetWorkHandler(Modem3GCapabilitySwitch.this,mNetworkMode);

        mSwitchReceiver = new ModemSwitchReceiver();
        mIsAirplaneModeOn = Settings.System.getInt(
                getContentResolver(), Settings.System.AIRPLANE_MODE_ON, -1) == 1;
        initServiceSwitchPref();
    }
    
    private IntentFilter initIntentFilter() {
        IntentFilter intentFilter = new IntentFilter(PhoneWrapper.EVENT_SIM_SWITCH_LOCK_CHANGED);
        intentFilter.addAction(PhoneWrapper.EVENT_PRE_CAPABILITY_SWITCH);
        intentFilter.addAction(PhoneWrapper.EVENT_CAPABILITY_SWITCH_DONE);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        return intentFilter;
    }
    
    private void initPreference() {
        mServiceList = (SimSelectDialogPreference)findPreference(SERVICE_LIST_KEY);
        mServiceList.setOnPreferenceChangeListener(this);
        mNetworkMode = (ListPreference)findPreference(NETWORK_MODE_KEY);
        mNetworkMode.setOnPreferenceChangeListener(this);
        if (FeatureOption.isMtkSimSwitch()) {
            setTitle(R.string.setting_for_4g_3g_service);
            mServiceList.setDialogTitle(R.string.setting_for_4g_3g_service);
            mServiceList.setTitle(R.string.enable_4g_3g_service);
            setPreferredNetworkModeEntriesAndValuesFor4G(mNetworkMode);
        }
        ExtensionManager.getModem3GCapabilitySwitchExt().NetworkModeFor3GSwitch(getPreferenceScreen(),
                mNetworkMode, this);
    }

    private void updateSummarys(long simId) {
        PhoneLog.d(TAG, "updateSummarys(), simId=" + simId);
        // restore current 3G sim ID
        mCurrentServiceSim = simId;
        mServiceList.setValue(simId);
        if (simId == SIMID_SERVICE_OFF) {
            if (mNetworkMode != null) {
                mNetworkMode.setSummary("");
            }
        } else if (simId == SIMID_SERVICE_NOT_SET) {
            //Clear the summary
            mServiceList.setSummary("");
            mNetworkMode.setSummary("");
        } else {
            SimInfoRecord info = SimInfoManager.getSimInfoById(this, simId);
            if (info != null) {
                //if the 3G service slot is radio off, disable the network mode
                boolean isPowerOn = !PhoneWrapper.isRadioOffBySlot(info.mSimSlotId, this);
                PhoneLog.d(TAG, "updateSummarys(), SIM " + simId + " power status is " + isPowerOn);
                if (!isPowerOn) {
                    mNetworkMode.setSummary("");
                }
            }
        }
    }
    
    public void changeForNetworkMode(Object objValue) {
        mNetworkMode.setValue((String) objValue);
        int buttonNetworkMode = Integer.valueOf((String) objValue).intValue();
        int settingsNetworkMode = Settings.Global.getInt(
                mPhone.getContext().getContentResolver(),
                Settings.Global.PREFERRED_NETWORK_MODE, Phone.PREFERRED_NT_MODE);
        if (buttonNetworkMode != settingsNetworkMode) {
            showDialog(GeminiUtils.PROGRESS_DIALOG);            
            mNetworkMode.setSummary(mNetworkMode.getEntry());
            
            Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                    Settings.Global.PREFERRED_NETWORK_MODE,
                    buttonNetworkMode);
            Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                    Settings.Global.USER_PREFERRED_NETWORK_MODE,
                    buttonNetworkMode);
            //Set the modem network mode
            /// for 4G switch @{
            int slot = GeminiUtils.get34GCapabilitySIM();
            /// @}
            PhoneLog.d(TAG, "changeForNetworkMode buttonNetworkMode = " + buttonNetworkMode);
            PhoneWrapper.setPreferredNetworkType(mPhone, buttonNetworkMode, mNetworkHandler.obtainMessage(NetWorkHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE), slot);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mServiceList) {
            mSelectedServiceSim = Long.valueOf(objValue.toString());
            PhoneLog.d(TAG, "onPreferenceChange mSelectedServiceSim = " + mSelectedServiceSim + " mCurrentServiceSim = " + mCurrentServiceSim);
            if (mSelectedServiceSim != mCurrentServiceSim) {
                if (FeatureOption.isMtkVT3G324MSupport()) {
                    showDialog(DIALOG_CAPABILITY_SWITCH);
                } else {
                    if (ExtensionManager.getModem3GCapabilitySwitchExt().
                            isShowHintDialogForSimSwitch(mSelectedServiceSim, this)) {
                        showDialog(DIALOG_HINT_CHANNGE_DATA);
                    } else {
                        handleServiceSwitch(mSelectedServiceSim);
                    }
                }
            }
        } else if (preference == mNetworkMode) {
            changeForNetworkMode(objValue);
        }
        return true;
    }

    public Dialog onCreateDialog(int id) {
        PhoneLog.d(TAG, "Create and show the dialog[id = " + id + "]");
        Dialog dialog = null;
        switch (id) {
        case GeminiUtils.PROGRESS_DIALOG:
            ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage(getResources().getString(R.string.updating_settings));
            progress.setCancelable(false);
            dialog = progress;
            break;
        case PROGRESS_DIALOG:
            ProgressDialog progress3g = new ProgressDialog(this);
            progress3g.setMessage(getResources().getString(R.string.modem_switching));
            progress3g.setCancelable(false);
            Window win = progress3g.getWindow();
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED;
            win.setAttributes(lp);
            dialog = progress3g;
            break;
        case DIALOG_CAPABILITY_SWITCH:
            PhoneLog.d(TAG, "onCreateDialog mSelectedServiceSim = "
                    + mSelectedServiceSim + " mCurrentServiceSim = " + mCurrentServiceSim);
            int confirmId = FeatureOption.isMtkSimSwitch() ? R.string.confirm_4g_3g_switch : R.string.confirm_3g_switch;
            int confirmOffId = FeatureOption.isMtkSimSwitch() ? R.string.confirm_4g_3g_switch_to_off : R.string.confirm_3g_switch_to_off;
            int msgId = mSelectedServiceSim == SIMID_SERVICE_OFF ? confirmOffId : confirmId;
            AlertDialog alert = new AlertDialog.Builder(this)
                   .setTitle(android.R.string.dialog_alert_title)
                   .setPositiveButton(R.string.buttonTxtContinue, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PhoneLog.d(TAG, "onClick mSelectedServiceSim = "
                                     + mSelectedServiceSim + " mCurrentServiceSim = " + mCurrentServiceSim);
                            handleServiceSwitch(mSelectedServiceSim);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mServiceList.setValue(mCurrentServiceSim);
                        }
                    })
                    .setCancelable(false)
                    .setMessage(msgId)
                    .create();
            dialog = alert;
            break;
        case DIALOG_HINT_CHANNGE_DATA:
            PhoneLog.d(TAG, "onCreateDialog mSelectedServiceSim = " + mSelectedServiceSim +
                    " mCurrentServiceSim = " + mCurrentServiceSim);
            String msg = ExtensionManager.getModem3GCapabilitySwitchExt().getHintString();
            AlertDialog dataChangeHintDialog = new AlertDialog.Builder(this)
                   .setTitle(android.R.string.dialog_alert_title)
                   .setPositiveButton(R.string.buttonTxtContinue, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PhoneLog.d(TAG, "switch 4G\3G service" );
                            handleServiceSwitch(mSelectedServiceSim);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PhoneLog.d(TAG, "don't change data" );
                            mServiceList.setValue(mCurrentServiceSim);
                        }
                    })
                    .setCancelable(false)
                    .setMessage(msg)
                    .create();
            dialog = dataChangeHintDialog;
            break;
        default:
            break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case DIALOG_CAPABILITY_SWITCH:
            int confirmId = FeatureOption.isMtkSimSwitch() ? R.string.confirm_4g_3g_switch : R.string.confirm_3g_switch;
            int confirmOffId = FeatureOption.isMtkSimSwitch() ? R.string.confirm_4g_3g_switch_to_off : R.string.confirm_3g_switch_to_off;
            int msgId = mSelectedServiceSim == SIMID_SERVICE_OFF ? confirmOffId : confirmId;
            ((AlertDialog)dialog).setMessage(getResources().getString(msgId));
            break;
        default:
            break;
        }
        super.onPrepareDialog(id, dialog);
    }

    private void clearAfterSwitch(Intent it) {
        long updateSimId = SIMID_SERVICE_NOT_SET;
        PhoneLog.d(TAG, "clearAfterSwitch(), remove switching dialog");
        removeDialog(PROGRESS_DIALOG);
        setStatusBarEnableStatus(true);
        //the slot which supports 3g service after switch
        //then get the simid which inserted to the 3g slot
        int currentCapilitySlot = it.getIntExtra(PhoneWrapper.EXTRA_CAPABILITY_SIM, SIMID_SERVICE_NOT_SET);
        if (currentCapilitySlot == SIMID_SERVICE_OFF) {
            updateSimId = SIMID_SERVICE_OFF;
        } else {
            SimInfoRecord info = SimInfoManager.getSimInfoBySlot(this, currentCapilitySlot);
            if (info != null) {
                updateSimId = info.mSimInfoId;
            }
        }
        updateSummarys(updateSimId);
        updateNetworkAndServiceStatus();
    }
    
    private void handleServiceSwitch(long simId) {
        /// For 4G switch @{
        if (mPhoneMgrEx.isSimSwitchLocked()) {
        /// @}
            PhoneLog.d(TAG, "Switch has been locked, return");
            mServiceList.setValue(mCurrentServiceSim);
            return ;
        }
        PhoneLog.d(TAG, "handleServiceSwitch(" + simId + "), show switching dialog first");
        showDialog(PROGRESS_DIALOG);
        setStatusBarEnableStatus(false);
        int slotId = -1;
        if (simId != -1) {
            SimInfoRecord info = SimInfoManager.getSimInfoById(this, simId);
            slotId = info == null ? -1 : info.mSimSlotId;
        }
        /// for 4G Switch @{
        boolean isSwitchSuccessful = false;
        if (FeatureOption.isMtkSimSwitch()) {
            if (PhoneFactory.isLteSupport()) {
                saveUserPreferredNeteorkMode();
                isSwitchSuccessful = mPhoneMgrEx.setCapabilitySIM(
                        TelephonyCapabilities.CAPABILITY_34G, slotId);
            } else {
                isSwitchSuccessful = mPhoneMgrEx.setCapabilitySIM(
                        TelephonyCapabilities.CAPABILITY_3G, slotId);
            }
        } else if (FeatureOption.isMtkGemini3GSwitch()) {
            isSwitchSuccessful = mPhoneMgrEx.setCapabilitySIM(
                    TelephonyCapabilities.CAPABILITY_3G, slotId);
        }
        PhoneLog.d(TAG, "isSwitchSuccessful: " + isSwitchSuccessful);
        if (isSwitchSuccessful) {
        /// @}
            PhoneLog.d(TAG, "Receive ok for the switch, and starting the waiting...");
        } else {
            PhoneLog.d(TAG, "Receive error for the switch & Dismiss switching didalog");
            removeDialog(PROGRESS_DIALOG);
            setStatusBarEnableStatus(true);
            mServiceList.setValue(mCurrentServiceSim);
        }
    }
    
    protected void onDestroy() {
        super.onDestroy();
        PhoneLog.d(TAG, "Instance[" + mInstanceIndex + "]." + "onDestroy()");
        ExtensionManager.getModem3GCapabilitySwitchExt().deinitPreferenceActivity();
        if (mSwitchReceiver != null) {
            unregisterReceiver(mSwitchReceiver);
        }
        //restore status bar status after finish, to avoid unexpected event
        setStatusBarEnableStatus(true);
        mTimerHandler.removeMessages(SWITCH_TIME_OUT_MSG);
    }
    
    /**
     * When switching modem, the status bar should be disabled
     * @param enabled
     */
    private void setStatusBarEnableStatus(boolean enabled) {
        PhoneLog.i(TAG, "setStatusBarEnableStatus(" + enabled + ")");
        if (mStatusBarManager == null) {
            mStatusBarManager = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
        }
        if (mStatusBarManager != null) {
            if (enabled) {
                mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
            } else {
                mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND |
                                          StatusBarManager.DISABLE_RECENT |
                                          StatusBarManager.DISABLE_HOME);
            }
        } else {
            PhoneLog.e(TAG, "Fail to get status bar instance");
        }
    }
    
    class ModemSwitchReceiver extends BroadcastReceiver {
        
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PhoneWrapper.EVENT_SIM_SWITCH_LOCK_CHANGED.equals(action)) {
                PhoneLog.d(TAG, "receives EVENT_SIM_SWITCH_LOCK_CHANGED...");
                boolean bLocked = intent.getBooleanExtra(PhoneWrapper.EXTRA_SIM_SWITCH_LOCKED, false);
                /// M: ALPS00653849 @{
                // When phone state change, we update all Items.
                updateNetworkAndServiceStatus();
                /// @}
            } else if (PhoneWrapper.EVENT_PRE_CAPABILITY_SWITCH.equals(action)) {
                PhoneLog.d(TAG, "Starting the switch......@" + this);
                showDialog(PROGRESS_DIALOG);
                showInstanceIndex("Receive starting switch broadcast");
                setStatusBarEnableStatus(false);
                mTimerHandler.sendEmptyMessageDelayed(SWITCH_TIME_OUT_MSG, SWITCH_TIME_OUT_VALUE);
                if (mNetworkMode.getDialog() != null) {
                    mNetworkMode.getDialog().dismiss();
                }
            } else if (PhoneWrapper.EVENT_CAPABILITY_SWITCH_DONE.equals(action)) {
                PhoneLog.d(TAG, "Done the switch......@" + this);
                showInstanceIndex("Receive switch done broadcast");
                clearAfterSwitch(intent);
                mTimerHandler.removeMessages(SWITCH_TIME_OUT_MSG);
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
                PhoneLog.d(TAG, "mIsAirplaneModeOn new  state is [" + mIsAirplaneModeOn + "]");
                removeDialog(DIALOG_CAPABILITY_SWITCH);
                updateNetworkAndServiceStatus();
            } else if (action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
                PhoneLog.d(TAG, "ACTION_SIM_INFO_UPDATE received");
                removeDialog(DIALOG_CAPABILITY_SWITCH);
                List<SimInfoRecord> temp =
                        SimInfoManager.getInsertedSimInfoList(Modem3GCapabilitySwitch.this);
                boolean isSimSwitchManualEnabled =
                        PhoneGlobals.getInstance().phoneMgrEx.isSimSwitchManualModeEnabled();
                PhoneLog.d(TAG, "isSimSwitchManualEnabled......" + isSimSwitchManualEnabled);
                if (temp.size() > 0 && isSimSwitchManualEnabled) {
                    mIsSimSwitchManualChangeAllowed = PhoneGlobals.getInstance()
                            .phoneMgrEx.isSimSwitchManualChangeSlotAllowed();
                    PhoneLog.d(TAG, "mIsSimSwitchManualChangeAllowed......"
                            + mIsSimSwitchManualChangeAllowed);
                    if (!mIsSimSwitchManualChangeAllowed) {
                        mManualAllowedSlot = PhoneGlobals.getInstance()
                                .phoneMgrEx.getSimSwitchAllowedSlots();
                        PhoneLog.d(TAG, "mManualAllowedSlots......" + mManualAllowedSlot);
                    }
                    initServiceSwitchPref();
                    long simId = SIMID_SERVICE_NOT_SET;
                    /// for 4G switch @{
                    int slot = GeminiUtils.get34GCapabilitySIM();
                    /// @}
                    //update summary
                    if (slot == SIMID_SERVICE_OFF) {
                        simId = slot;
                    } else {
                        SimInfoRecord info = SimInfoManager.getSimInfoBySlot(Modem3GCapabilitySwitch.this, slot);
                        simId = info != null ? info.mSimInfoId : SIMID_SERVICE_NOT_SET;
                    }
                    updateSummarys(simId);
                    updateNetworkAndServiceStatus();
                } else {
                    finish();
                } 
            } else if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
                PhoneLog.d(TAG, "receives ACTION_SIM_INDICATOR_STATE_CHANGED...");
                int state = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_STATE, -1);
                int slotId = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, -1);
                PhoneLog.d(TAG,"state = " + state + " slotId = " + slotId);
                updateNetworkAndServiceStatus();
                mServiceList.updateSimIndicator(slotId, state);
            }
        }
    }
    
    private void showInstanceIndex(String msg) {
        if (DBG) {
            PhoneLog.i(TAG, "Instance[" + mInstanceIndex + "]: " + msg);
        }
    }

    private void initServiceSwitchPref() {
        mSimInfoList = SimInfoManager.getInsertedSimInfoList(this);
        Collections.sort(mSimInfoList, new GeminiUtils.SIMInfoComparable());
        List<Integer> simIndicatorList = new ArrayList<Integer>();
        List<Long> entryValues = new ArrayList<Long>();
        List<Boolean> itemStatus = new ArrayList<Boolean>();
        for (SimInfoRecord siminfo : mSimInfoList) {
            simIndicatorList.add(GeminiUtils.getSimIndicator(this, siminfo.mSimSlotId));
            entryValues.add(siminfo.mSimInfoId);
            // for smart 3g switch
            itemStatus.add(GeminiUtils.isServiceSwitchManualEnableSlot(
                    siminfo.mSimSlotId, mIsSimSwitchManualChangeAllowed, mManualAllowedSlot));
        }
        List<String> normalListSimSwitch = new ArrayList<String>();
        List<Long> normalEntryValues = new ArrayList<Long>();
        // feature: For ALPS00791254 remove 3g switch off radio and NMOpFor3GSwitch @{
        if (mSimInfoList.size() > 0 && !ExtensionManager.getModem3GCapabilitySwitchExt().
                isRemoveRadioOffFor3GSwitchFlag()) {
            normalListSimSwitch.add(getString(R.string.service_3g_off));
            normalEntryValues.add(SIMID_SERVICE_OFF);
        }
        ExtensionManager.getModem3GCapabilitySwitchExt().customizeSimSelectList(normalListSimSwitch, normalEntryValues);
        entryValues.addAll(normalEntryValues);
        mServiceList.setEntriesData(mSimInfoList, simIndicatorList, normalListSimSwitch, itemStatus);
        mServiceList.setEntryValues(entryValues);
    }

    /**
     * Add for ALPS01266612
     * If the activity is on the background, we hide the dialogs
     */
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        PhoneLog.d(TAG, "onPause....");
        dismissDialogs();
    }

    /**
     * Dismiss the dialogs belongs to the activity
     */
    private void dismissDialogs() {
        PhoneLog.d(TAG, "dismissDialogs...");
        Dialog switchDialog = mServiceList.getDialog();
        if (switchDialog != null && switchDialog.isShowing()) {
            switchDialog.dismiss();
        }
        Dialog netWorkDialog = mNetworkMode.getDialog();
        if (netWorkDialog != null && netWorkDialog.isShowing()) {
            netWorkDialog.dismiss();
        }
    }

    /**
     * set arrary and value for 4g.
     * @param mButtonPreferredNetworkMode
     */
    private void setPreferredNetworkModeEntriesAndValuesFor4G(ListPreference mButtonPreferredNetworkMode) {
        int slot = GeminiUtils.get34GCapabilitySIM();
        PhoneLog.d(TAG,"[setPreferredNetworkModeEntriesAndValues]... slot: " + slot);

        if (PhoneFactory.isLteSupport()) {
            mButtonPreferredNetworkMode.setTitle(R.string.preferred_network_mode_title);
            mButtonPreferredNetworkMode.setDialogTitle(R.string.preferred_network_mode_dialogtitle);
            if (GeminiUtils.isTddSupportForLte()) {
                mButtonPreferredNetworkMode.setEntries(R.array.sglte_network_mode_choices);
                mButtonPreferredNetworkMode.setEntryValues(R.array.sglte_network_mode_values);
            } else {
                mButtonPreferredNetworkMode.setEntries(R.array.enabled_networks_except_gsm_4g_choices);
                mButtonPreferredNetworkMode.setEntryValues(R.array.enabled_networks_except_gsm_values);
            }
        }
    }

    private CallFromPlugin mCallbackForPlugin = new CallFromPlugin();
    private class CallFromPlugin implements ICommonCallback {

        @Override
        public Activity getHostActivity() {
            return Modem3GCapabilitySwitch.this;
        }

        @Override
        public Phone getHostPhone() {
            return mPhone;
        }
    }

    private void saveUserPreferredNeteorkMode() {
        int newUserMode = -1;//-1 framework auto select mode.
        int oldUserMode = android.provider.Settings.Global.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.USER_PREFERRED_NETWORK_MODE,
                    9);
        PhoneLog.d(TAG,"saveUserPreferredNeteorkMode:newUserMode = " + 
                 newUserMode + " oldUserMode = " + oldUserMode);
        android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.USER_PREFERRED_NETWORK_MODE,
                    newUserMode);
    }
}
