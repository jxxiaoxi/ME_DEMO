package com.mediatek.hdmi;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.common.hdmi.IHDMINative;
import com.mediatek.common.hdmi.IMtkHdmiManager;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.List;

public class HDMISettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "HDMISettings";
    private static final String KEY_TOGGLE_HDMI = "hdmi_toggler";
    private static final String KEY_VIDEO_RESOLUTION = "video_resolution";
    private static final String KEY_VIDEO_SCALE = "video_scale";
    private static final String ACTION_EDID_UPDATE = "mediatek.action.HDMI_EDID_UPDATED";
    private static final int HDMI_RESOLUTION_AUTO = 100;
    private CheckBoxPreference mToggleHdmiPref;
    private ListPreference mVideoResolutionPref;
    private ListPreference mVideoScalePref;
    private IMtkHdmiManager mHdmiManager;
    private Activity mActivity;

    private ContentObserver mHdmiSettingsObserver = new ContentObserver(
            new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Xlog.d(TAG, "mHdmiSettingsObserver onChanged: " + selfChange);
            updatePref();
        }
    };

    // { @ M: Smart book hdmi settings
    private BroadcastReceiver mSmartBookPlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context1, Intent intent) {
            Boolean isSmartBookPluggedIn = intent.getBooleanExtra(Intent.EXTRA_SMARTBOOK_PLUG_STATE, false);
            Xlog.d(TAG, "smartbook plug:" + isSmartBookPluggedIn);
            // if smart book plug in, finish HDMI settings UI
            if (isSmartBookPluggedIn) {
                HDMISettings.this.getActivity().finish();
            }
        }
    }; 
    // @ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Xlog.i(TAG, "HDMISettings.onCreate()");
        addPreferencesFromResource(R.xml.hdmi_settings);
        mActivity = getActivity();
        mToggleHdmiPref = (CheckBoxPreference) findPreference(KEY_TOGGLE_HDMI);
        mToggleHdmiPref.setOnPreferenceChangeListener(this);
        mVideoResolutionPref = (ListPreference) findPreference(KEY_VIDEO_RESOLUTION);
        mVideoResolutionPref.setOnPreferenceChangeListener(this);
        mVideoScalePref = (ListPreference) findPreference(KEY_VIDEO_SCALE);
        mVideoScalePref.setOnPreferenceChangeListener(this);
        CharSequence[] entries = mVideoScalePref.getEntries();
        CharSequence[] values = mVideoScalePref.getEntryValues();
        List<CharSequence> scaleEntries = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            if (Integer.parseInt(values[i].toString()) != 0) {
                scaleEntries.add(mActivity.getResources().getString(
                        R.string.hdmi_scale_scale_down, values[i]));
            } else {
                scaleEntries.add(mActivity.getResources().getString(
                        R.string.hdmi_scale_no_scale));
            }
        }
        mVideoScalePref.setEntries(scaleEntries.toArray(new CharSequence[scaleEntries.size()]));
        mHdmiManager = IMtkHdmiManager.Stub.asInterface(ServiceManager
                .getService(Context.MTK_HDMI_SERVICE));
        if (null == mHdmiManager) {
            finish();
            return;
        }
        try {
            if (mHdmiManager.getDisplayType() == IHDMINative.DISPLAY_TYPE_MHL) {
                String hdmi = getString(R.string.hdmi_replace_hdmi);
                String mhl = getString(R.string.hdmi_replace_mhl);
                mActivity.setTitle(mActivity.getTitle().toString().replaceAll(hdmi,
                        mhl));
                mToggleHdmiPref.setTitle(mToggleHdmiPref.getTitle().toString()
                        .replaceAll(hdmi, mhl));
            }
            if (!mHdmiManager.isFeatureSupported(IHDMINative.FEATURE_SCALE_ADJUST)) {
                Xlog.d(TAG, "remove mVideoScalePref");
                getPreferenceScreen().removePreference(mVideoScalePref);
            }
        } catch (RemoteException e) {
            Xlog.d(TAG, "getDisplayType RemoteException");
        }
        // { @ M: Smart book hdmi settings
        if (FeatureOption.MTK_SMARTBOOK_SUPPORT) {
            mActivity.registerReceiver(mSmartBookPlugReceiver, new IntentFilter(Intent.ACTION_SMARTBOOK_PLUG));
        }
        // @ }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePref();
        mActivity.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.HDMI_ENABLE_STATUS),
                false, mHdmiSettingsObserver);
        mActivity.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.HDMI_CABLE_PLUGGED),
                false, mHdmiSettingsObserver);
    }

    @Override
    public void onPause() {
        mActivity.getContentResolver().unregisterContentObserver(
                mHdmiSettingsObserver);
        super.onPause();
    };

    @Override
    public void onDestroy() {
        // { @ M: Smart book hdmi settings
        if (FeatureOption.MTK_SMARTBOOK_SUPPORT) {
            mActivity.unregisterReceiver(mSmartBookPlugReceiver);
        }
        // @ }
        super.onDestroy();
    };

    private void updatePref() {
        Xlog.i(TAG, "updatePref");
        updatePrefStatus();
        updateSelectedResolution();
        updateSelectedScale();
    }

    private void updatePrefStatus() {
        Xlog.i(TAG, "updatePrefStatus");
        Dialog dlg = mVideoResolutionPref.getDialog();
        if (dlg != null && dlg.isShowing()) {
            dlg.cancel();
        }
        dlg = mVideoScalePref.getDialog();
        if (dlg != null && dlg.isShowing()) {
            dlg.cancel();
        }
        boolean shouldEnable = false;
        try {
            shouldEnable = mHdmiManager.isSignalOutputting();
        } catch (RemoteException e) {
            Xlog.w(TAG, "hdmi manager RemoteException: " + e.getMessage());
        }
        mVideoResolutionPref.setEnabled(shouldEnable);
        mVideoScalePref.setEnabled(shouldEnable);
        boolean hdmiEnabled = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.HDMI_ENABLE_STATUS, 1) == 1;
        mToggleHdmiPref.setChecked(hdmiEnabled);
    }

    private void updateSelectedResolution() {
        Xlog.i(TAG, "updateSelectedResolution");
        Dialog dlg = mVideoResolutionPref.getDialog();
        if (dlg != null && dlg.isShowing()) {
            dlg.cancel();
        }
        int videoResolution = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.HDMI_VIDEO_RESOLUTION, HDMI_RESOLUTION_AUTO);
        if (videoResolution > HDMI_RESOLUTION_AUTO) {
            videoResolution = HDMI_RESOLUTION_AUTO;
        }
        int[] supportedResolutions = { HDMI_RESOLUTION_AUTO };
        try {
            supportedResolutions = mHdmiManager.getSupportedResolutions();
        } catch (RemoteException e) {
            Xlog.w(TAG, "hdmi manager RemoteException: " + e.getMessage());
        }
        CharSequence[] resolutionEntries = mActivity.getResources()
                .getStringArray(R.array.hdmi_video_resolution_entries);
        List<CharSequence> realResolutionEntries = new ArrayList<CharSequence>();
        List<CharSequence> realResolutionValues = new ArrayList<CharSequence>();
        realResolutionEntries.add(mActivity.getResources().getString(
                R.string.hdmi_auto));
        realResolutionValues.add(Integer.toString(HDMI_RESOLUTION_AUTO));
        for (int resolution : supportedResolutions) {
            try {
                realResolutionEntries.add(resolutionEntries[resolution]);
                realResolutionValues.add(Integer.toString(resolution));
            } catch (ArrayIndexOutOfBoundsException e) {
                Xlog.d(TAG, e.getMessage());
            }
        }
        mVideoResolutionPref.setEntries((CharSequence[]) realResolutionEntries
                .toArray(new CharSequence[realResolutionEntries.size()]));
        mVideoResolutionPref
                .setEntryValues((CharSequence[]) realResolutionValues
                        .toArray(new CharSequence[realResolutionValues.size()]));
        mVideoResolutionPref.setValue(Integer.toString(videoResolution));
    }

    private void updateSelectedScale() {
        Xlog.i(TAG, "updateSelectedScale");
        Dialog dlg = mVideoScalePref.getDialog();
        if (dlg != null && dlg.isShowing()) {
            dlg.cancel();
        }
        int videoScale = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.HDMI_VIDEO_SCALE, 0);
        mVideoScalePref.setValue(Integer.toString(videoScale));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        Xlog.d(TAG, key + " preference changed");
        try {
            if (KEY_TOGGLE_HDMI.equals(key)) {
                boolean checked = ((Boolean) newValue).booleanValue();
                mHdmiManager.enableHdmi(checked);
            } else if (KEY_VIDEO_RESOLUTION.equals(key)) {
                mHdmiManager.setVideoResolution(Integer
                        .parseInt((String) newValue));
            } else if (KEY_VIDEO_SCALE.equals(key)) {
                int scaleValue = Integer.parseInt((String) newValue);
                if (scaleValue >= 0 && scaleValue <= 10) {
                    mHdmiManager.setVideoScale(scaleValue);
                } else {
                    Xlog.d(TAG, "scaleValue error: " + scaleValue);
                }
            }
        } catch (RemoteException e) {
            Xlog.w(TAG, "hdmi manager RemoteException: " + e.getMessage());
        }
        return true;
    }
}
