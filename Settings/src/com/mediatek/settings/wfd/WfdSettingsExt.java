/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.settings.wfd;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import com.android.settings.R;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.Arrays;

public class WfdSettingsExt {
    private static final String TAG = "WfdSettingsExt";

    private Activity mContext;
    private DisplayManager mDisplayManager;
    private static final String PREF_KEY_SOURCE = "wfd_source";
    private static final String PREF_KEY_SINK = "wfd_sink";
    private static final String SINK_SURFACE_TAG = "WfdSinkSurface";
    //change resolution menu id
    private static final int MENU_ID_CHANGE_RESOLUTION = Menu.FIRST + 1;

    //720p device resolution 0: 720p 30fps off; 1:720p 60fps off; 3:720p 60fps on; 4:720p 30fps on
    public static final int DEVICE_720P_60FPS_RESOLUTION = 3;
    public static final int DEVICE_720P_30FPS_RESOLUTION = 4;
    public static final ArrayList<Integer> DEVICE_720P_RESOLUTION_LIST = new ArrayList(
            Arrays.asList(DEVICE_720P_60FPS_RESOLUTION, DEVICE_720P_30FPS_RESOLUTION));
    
    //1080p device resolution 2: 1080p off; 5:1080p on; 6:720p 60 fps on; 7:720p 30 fps on
    public static final int DEVICE_1080P_ON_RESOLUTION = 5;
    public static final int DEVICE_1080P_60FPS_RESOLUTION = 6;
    public static final int DEVICE_1080P_30FPS_RESOLUTION = 7;
    public static final ArrayList<Integer> DEVICE_1080P_RESOLUTION_LIST = new ArrayList(
            Arrays.asList(DEVICE_1080P_ON_RESOLUTION, DEVICE_1080P_60FPS_RESOLUTION, 
            DEVICE_1080P_30FPS_RESOLUTION));

    // WFD sink added
    private Boolean mSelectSource = null;
    private Runnable mCallback = null;
    private WfdSinkSurfaceFragment mSinkFragment;
    private SinkSourcePrefClickListener mPreflistener;

    public WfdSettingsExt(Activity context) {
        mContext = context;
        mDisplayManager = (DisplayManager) mContext
                .getSystemService(Context.DISPLAY_SERVICE);
    }

    /**
     * add change resolution option menu
     * @param menu the menu that change resolution menuitem will be added to
     * @param status current wfd status
     */
    public void onCreateOptionMenu(Menu menu, WifiDisplayStatus status) {
        int currentResolution = Settings.Global.getInt(mContext.getContentResolver(), 
                Settings.Global.WIFI_DISPLAY_RESOLUTION, 0);
        Xlog.d(TAG, "current resolution is " + currentResolution);
        if(DEVICE_720P_RESOLUTION_LIST.contains(currentResolution) || 
                DEVICE_1080P_RESOLUTION_LIST.contains(currentResolution)) {
            menu.add(Menu.NONE, MENU_ID_CHANGE_RESOLUTION, 0 ,R.string.wfd_change_resolution_menu_title)
            .setEnabled(status.getFeatureState() == WifiDisplayStatus.FEATURE_STATE_ON)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }
    
    /**
     * called when the option menu is selected
     * @param item the selected menu item
     * @return true, change resolution item is selected, otherwise false
     */
    public boolean onOptionMenuSelected(MenuItem item, FragmentManager fragmentManager) {
        if(item.getItemId() == MENU_ID_CHANGE_RESOLUTION) {
            new WfdChangeResolutionFragment().show(
                    fragmentManager, "change resolution");
            return true;
        }
        return false;
    }

    /**
     * Add source and sink preferences to WFDSettings, user could select WFD
     * working mode
     * 
     * @param preferenceScreen
     *            Preference screen which the preferences added
     * @return true, add preferences success, no need to add other preference in
     *         WFDSettings, otherwize false
     */
    public boolean addSourceSinkPreference(PreferenceScreen preferenceScreen) {
        boolean handled = false;
        WifiDisplayStatus mWifiDisplayStatus = mDisplayManager
                .getWifiDisplayStatus();
        boolean bStateOn = (mWifiDisplayStatus != null && mWifiDisplayStatus
                .getFeatureState() == WifiDisplayStatus.FEATURE_STATE_ON);
        Xlog.d(TAG, "addSourceSinkPreference bStateOn: " + bStateOn);
        if (bStateOn) {
            Xlog.d(TAG, "addSourceSinkPreference active: "
                    + mWifiDisplayStatus.getActiveDisplayState()
                    + " mSelectSource: " + mSelectSource);
            switch (mWifiDisplayStatus.getActiveDisplayState()) {
            case WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED:
                if (mSelectSource != Boolean.TRUE) {
                    addPreferences(preferenceScreen);
                    handled = true;
                }
                if (mSinkFragment != null) {
                    mSinkFragment.dismiss();
                }
                break;
            case WifiDisplayStatus.DISPLAY_STATE_CONNECTING:
            case WifiDisplayStatus.DISPLAY_STATE_CONNECTED:
                if (isSink()) {
                    addPreferences(preferenceScreen);
                    handled = true;
                }
                break;
            default:
                break;
            }
        }
        Xlog.d(TAG, "addSourceSinkPreference " + handled);
        return handled;
    }

    /**
     * Called when WFDSettings started
     */
    public void onStart() {
        Xlog.d(TAG, "onstart");
    }

    /**
     * Called when WFDSettings stopped
     */
    public void onStop() {
        Xlog.d(TAG, "onstop");
        if (isSink()) {
            suspendWfdSinkConnection();
        }
        mCallback = null;
        mSelectSource = null;
    }

    private void addPreferences(PreferenceScreen preferenceScreen) {
        PreferenceCategory cat = new PreferenceCategory(mContext);
        cat.setLayoutResource(R.layout.wfd_source_sink_tips);
        preferenceScreen.addPreference(cat);
        PreferenceCategory actionsCategory = new PreferenceCategory(mContext);
        actionsCategory.setTitle(R.string.wfd_category_actions);
        preferenceScreen.addPreference(actionsCategory);
        Preference p = new Preference(mContext);
        p.setTitle(R.string.wfd_source_title);
        p.setKey(PREF_KEY_SOURCE);
        if (mPreflistener == null) {
            mPreflistener = new SinkSourcePrefClickListener(preferenceScreen);
        }
        p.setOnPreferenceClickListener(mPreflistener);
        actionsCategory.addPreference(p);
        p = new Preference(mContext);
        p.setTitle(R.string.wfd_sink_title);
        p.setKey(PREF_KEY_SINK);
        p.setOnPreferenceClickListener(mPreflistener);
        actionsCategory.addPreference(p);
    }

    private class SinkSourcePrefClickListener implements
            Preference.OnPreferenceClickListener {
        private PreferenceScreen mPrefScreen;

        public SinkSourcePrefClickListener(PreferenceScreen preferenceScreen) {
            mPrefScreen = preferenceScreen;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            boolean handled = false;
            if (PREF_KEY_SOURCE.equals(preference.getKey())) {
                Xlog.v(TAG, "wfd source item click");
                mSelectSource = Boolean.TRUE;
                enableSink(false);
                if (mCallback != null) {
                    mCallback.run();
                }
                handled = true;
            } else if (PREF_KEY_SINK.equals(preference.getKey())) {
                mSelectSource = Boolean.FALSE;
                Xlog.v(TAG, "wfd sink item click");
                prepareWfdSinkConnection();
                handled = true;
            }
            return handled;
        }
    }

    private boolean isSink() {
        return mDisplayManager.getIfSinkEnabled();
    }

    private void enableSink(boolean sink) {
        Xlog.d(TAG, "enableSink " + sink);
        mDisplayManager.requestEnableSink(sink);
    }

    private void restoreWfdSinkDisplay() {
        mDisplayManager.requestSuspendDisplay(false, mSinkFragment
                .getSinkSurface());
    }

    private void waitWfdSinkDisplay() {
        mDisplayManager.requestWaitConnection(mSinkFragment.getSinkSurface());
    }

    private void suspendWfdSinkDisplay() {
        mDisplayManager.requestSuspendDisplay(true, null);
    }

    private void disconnectWfdSinkConnection() {
        Xlog.d(TAG, "disconnectWfdSinkConnection");
        mDisplayManager.disconnectWifiDisplay();
        Xlog.d(TAG, "after disconnectWfdSinkConnection");
    }

    /**
     * Add WFD source mode callback
     * @param runnable
     */
    public void addCallback(Runnable runnable) {
        Xlog.d(TAG, "add callback");
        mCallback = runnable;
    }

    /**
     * Setup WFD sink connection, called when WFD sink surface is available
     */
    public void setupWfdSinkConnection() {
        Xlog.d(TAG, "setupWfdSinkConnection");
        if (isWfdSinkConnected()) {
            restoreWfdSinkDisplay();
        } else {
            waitWfdSinkDisplay();
        }
    }

    /**
     * Suspend WFD sink connection, called when WFD sink surface will exit
     */
    public void suspendWfdSinkConnection() {
        Xlog.d(TAG, "suspendWfdSinkConnection");
        disconnectWfdSinkConnection();
    }
    
    private void prepareWfdSinkConnection() {
        Xlog.d(TAG, "prepareWfdSinkConnection");
        enableSink(true);
        if (mSinkFragment == null) {
            mSinkFragment = new WfdSinkSurfaceFragment(WfdSettingsExt.this,
                    mContext);
        }
        if (mSinkFragment.isAdded()) {
            Xlog.d(TAG, "fragment is added");
        } else {
            mSinkFragment.show(mContext.getFragmentManager(), SINK_SURFACE_TAG);
        }
    }
    
    private boolean isWfdSinkConnected() {
        WifiDisplayStatus wfdStatus = mDisplayManager
                .getWifiDisplayStatus();
        return wfdStatus == null ? false
                : isWfdSinkAvalible(wfdStatus)
                        && (wfdStatus.getActiveDisplayState() == WifiDisplayStatus.DISPLAY_STATE_CONNECTED);
    }
    
    private boolean isWfdSinkAvalible(WifiDisplayStatus wfdStatus) {
        return wfdStatus != null
                && wfdStatus.getFeatureState() == WifiDisplayStatus.FEATURE_STATE_ON
                && isSink();
    }

}