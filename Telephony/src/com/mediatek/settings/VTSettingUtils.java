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

package com.mediatek.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.phone.PhoneGlobals;
import com.mediatek.services.telephony.common.VTSettingParams;
import com.mediatek.phone.vt.VTCallFlags;
import com.mediatek.phone.vt.VTManagerWrapper;
import com.mediatek.vt.VTManager;

public class VTSettingUtils {

    private static final String LOG_TAG = "VTSettingUtils";
    private static final boolean DBG = true;// (PhoneApp.DBG_LEVEL >= 2);
    private static final boolean DBGEM = true;
    public static final int INVALID_SLOT_ID = -1;

    private static final VTSettingUtils sInstance = new VTSettingUtils();
    private VTEngineerModeValues mVTEngineerModeValues;

    private int mSlotId = 0;             // indicate which slot the VT Call is from, so we can get VT settings on this slot.

    private boolean mToReplacePeer;      // "Display peer video": display picture for replacing peer video when it is unavailable
    private String mPicToReplacePeer;    // "Peer video replacement": 0-default picture, 1-my picture

    private boolean mShowLocalMO;        // "Outgoing video call": display local video when make a video call
    private String mShowLocalMT;         // "Video incoming call": 0-video, 1-ask, 2-picture
    private String mPicToReplaceLocal;   // "Local video replacement": 0-default, 1-freeze me, 2-my picture

    private boolean mEnableBackCamera;   // "Enable back camera";
    private boolean mPeerBigger;         // "Bigger Peer video";
    private boolean mAutoDropBack;       // "Auto drop back";

    private Bitmap mReplacePeerBitmap;   // the bitmap will be pushed to InCallUI to replace peer view.

    private VTSettingUtils() {
        mVTEngineerModeValues = new VTEngineerModeValues();
        resetVTSettingToDefaultValue();
    }

    public static VTSettingUtils getInstance() {
        return sInstance;
    }

    public void setSlotId(int slotId) {
        mSlotId = slotId;
    }

    public int getSlotId() {
        return mSlotId;
    }

    public boolean getToReplacePeer() {
        return mToReplacePeer;
    }

    public String getPicToReplacePeer() {
        return mPicToReplacePeer;
    }

    public boolean getShowLocalMO() {
        return mShowLocalMO;
    }

    public String getShowLocalMT() {
        return mShowLocalMT;
    }

    public String getPicToReplaceLocal() {
        return mPicToReplaceLocal;
    }

    /**
     * set VT Settings to default values.
     */
    public void resetVTSettingToDefaultValue() {
        if (DBG) {
            log("resetVTSettingToDefaultValue()...");
        }
        mPicToReplaceLocal = "0";
        mEnableBackCamera = true;
        mPeerBigger = true;
        mShowLocalMO = true;
        mShowLocalMT = "0";
        mAutoDropBack = false;
        mToReplacePeer = true;
        mPicToReplacePeer = "0";
    }

    /**
     * get VT Settings on certain slot. and update the bitmap, which will be pushed to InCallUI later.
     * @param slotId
     */
    public void updateVTSettings(int slotId) {
        if (DBG) {
            log("updateVTSettings()...slotId: " + slotId);
        }

        SharedPreferences sp = PhoneGlobals.getInstance().getApplicationContext()
            .getSharedPreferences("com.android.phone_preferences" , Context.MODE_PRIVATE);
        if (null == sp) {
            if (DBG) { 
                log("updateVTSettings() : can not find 'com.android.phone_preferences'...");
            }
            return;
        }

        mPicToReplaceLocal = sp.getString("button_vt_replace_expand_key_" + slotId, "0");
        mEnableBackCamera = sp.getBoolean("button_vt_enable_back_camera_key_" + slotId, true);
        mPeerBigger = sp.getBoolean("button_vt_peer_bigger_key_" + slotId, true);
        mShowLocalMO = sp.getBoolean("button_vt_mo_local_video_display_key_" + slotId, true);
        mShowLocalMT = sp.getString("button_vt_mt_local_video_display_key_" + slotId, "0");
        mAutoDropBack = sp.getBoolean("button_vt_auto_dropback_key_" + slotId, false);
        mToReplacePeer = sp.getBoolean("button_vt_enable_peer_replace_key_" + slotId, true);
        mPicToReplacePeer = sp.getString("button_vt_replace_peer_expand_key_" + slotId, "0");

        // update mReplacePeerBitmap which will be pushed to InCallUI.
        updateReplacePeerBitmap(slotId);

        if (DBG) {
            log(" ------- dumpVTSettings() begin ------- ");
            log(" - mSlotId = " + mSlotId);
            log(" - mPicToReplaceLocal = " + mPicToReplaceLocal);
            log(" - mEnableBackCamera = " + mEnableBackCamera);
            log(" - mPeerBigger = " + mPeerBigger);
            log(" - mShowLocalMO = " + mShowLocalMO);
            log(" - mShowLocalMT = " + mShowLocalMT);
            log(" - mAutoDropBack = " + mAutoDropBack);
            log(" - mToReplacePeer = " + mToReplacePeer);
            log(" - mPicToReplacePeer = " + mPicToReplacePeer);
            log(" - mReplacePeerBitmap = " + mReplacePeerBitmap);
            log(" ------- dumpVTSettings() end ------- ");
        }
    }

    /**
     * generate the bitmap which will be pushed to InCallUI to replace peer video, based on correct VT Settings.
     * @param slotId
     */
    private void updateReplacePeerBitmap(int slotId) {
        if (null != mReplacePeerBitmap) {
            mReplacePeerBitmap.recycle();
            mReplacePeerBitmap = null;
        }
        if (VTSettingUtils.getInstance().mPicToReplacePeer.equals(VTAdvancedSetting.SELECT_DEFAULT_PICTURE2)) {
            mReplacePeerBitmap = BitmapFactory.decodeFile(VTAdvancedSetting.getPicPathDefault2());
        } else if (VTSettingUtils.getInstance().mPicToReplacePeer.equals(VTAdvancedSetting.SELECT_MY_PICTURE2)) {
            mReplacePeerBitmap = BitmapFactory.decodeFile(VTAdvancedSetting.getPicPathUserselect2(slotId));
        }
    }

    public class VTEngineerModeValues {
        public String working_mode;
        public String working_mode_detail;
        public String config_audio_channel_adapt;
        public String config_video_channel_adapt;
        public String config_video_channel_reverse;
        public String config_multiplex_level;
        public String config_video_codec_preference;
        public String config_use_wnsrp;
        public String config_terminal_type;
        public boolean auto_answer;
        public String auto_answer_time;
        public boolean debug_message;
        public boolean h223_raw_data;
        public boolean log_to_file;
        public boolean h263_only;
        public boolean get_raw_data;

        public int log_filter_tag_0_value;
        public int log_filter_tag_1_value;
        public int log_filter_tag_2_value;
        public int log_filter_tag_3_value;

        public VTEngineerModeValues() {
            resetValuesToDefault();
        }

        public void resetValuesToDefault() {
            working_mode = "0";
            working_mode_detail = "0";
            config_audio_channel_adapt = "0";
            config_video_channel_adapt = "0";
            config_video_channel_reverse = "0";
            config_multiplex_level = "0";
            config_video_codec_preference = "0";
            config_use_wnsrp = "0";
            config_terminal_type = "0";
            auto_answer = false;
            auto_answer_time = "0";
            debug_message = false;
            h223_raw_data = false;
            log_to_file = false;
            h263_only = false;
            get_raw_data = false;

            log_filter_tag_0_value = 24;
            log_filter_tag_1_value = 24;
            log_filter_tag_2_value = 24;
            log_filter_tag_3_value = 24;
        }
    } 

    //update the VT Engineer Mode values and set them to VTManager
    public void updateVTEngineerModeValues() {
        if (DBGEM) {
            log("updateVTEngineerModeValues()...");
        }

        Context emContext = null;
        try {
            emContext = PhoneGlobals.getInstance().createPackageContext("com.mediatek.engineermode",
                                                                    Context.CONTEXT_INCLUDE_CODE);
        } catch (NameNotFoundException e) {
            if (DBGEM) {
                log("updateVTEngineerModeValues() : can not find 'com.mediatek.engineermode'...");
            }
            return;
        }

        SharedPreferences sp = emContext.getSharedPreferences("engineermode_vt_preferences",
                                                              Context.MODE_WORLD_READABLE);
        if (null == sp) {
            if (DBGEM) {
                log("updateVTEngineerModeValues() : can not find 'engineermode_vt_preferences'...");
            }
            return;
        }

        mVTEngineerModeValues.working_mode = sp.getString("working_mode", "0");
        mVTEngineerModeValues.working_mode_detail = sp.getString("working_mode_detail", "0");
        mVTEngineerModeValues.config_audio_channel_adapt = sp.getString("config_audio_channel_adapt", "0");
        mVTEngineerModeValues.config_video_channel_adapt = sp.getString("config_video_channel_adapt", "0");
        mVTEngineerModeValues.config_video_channel_reverse = sp.getString("config_video_channel_reverse", "0");
        mVTEngineerModeValues.config_multiplex_level = sp.getString("config_multiplex_level", "0");
        mVTEngineerModeValues.config_video_codec_preference = sp.getString("config_video_codec_preference", "0");
        mVTEngineerModeValues.config_use_wnsrp = sp.getString("config_use_wnsrp", "0");
        mVTEngineerModeValues.config_terminal_type = sp.getString("config_terminal_type", "0");
        mVTEngineerModeValues.auto_answer = sp.getBoolean("auto_answer", false);
        mVTEngineerModeValues.auto_answer_time = sp.getString("auto_answer_time", "0");        
        mVTEngineerModeValues.debug_message = sp.getBoolean("debug_message", false);
        mVTEngineerModeValues.h223_raw_data = sp.getBoolean("h223_raw_data", false);    
        mVTEngineerModeValues.log_to_file = sp.getBoolean("log_to_file", false); 
        mVTEngineerModeValues.h263_only = sp.getBoolean("h263_only", false);
        mVTEngineerModeValues.get_raw_data = sp.getBoolean("get_raw_data", false);

        mVTEngineerModeValues.log_filter_tag_0_value = sp.getInt("log_filter_tag_0_value", 24);
        mVTEngineerModeValues.log_filter_tag_1_value = sp.getInt("log_filter_tag_1_value", 24);
        mVTEngineerModeValues.log_filter_tag_2_value = sp.getInt("log_filter_tag_2_value", 24);
        mVTEngineerModeValues.log_filter_tag_3_value = sp.getInt("log_filter_tag_3_value", 24);

        if (DBGEM) {
            log(" - mVTEngineerModeValues.working_mode = " + mVTEngineerModeValues.working_mode);
            log(" - mVTEngineerModeValues.working_mode_detail = " + mVTEngineerModeValues.working_mode_detail);
            log(" - mVTEngineerModeValues.config_audio_channel_adapt = " + mVTEngineerModeValues.config_audio_channel_adapt);
            log(" - mVTEngineerModeValues.config_video_channel_adapt = " + mVTEngineerModeValues.config_video_channel_adapt);
            log(" - mVTEngineerModeValues.config_video_channel_reverse = " 
                    + mVTEngineerModeValues.config_video_channel_reverse);
            log(" - mVTEngineerModeValues.config_multiplex_level = " + mVTEngineerModeValues.config_multiplex_level);
            log(" - mVTEngineerModeValues.config_video_codec_preference = " 
                    + mVTEngineerModeValues.config_video_codec_preference);
            log(" - mVTEngineerModeValues.config_use_wnsrp = " + mVTEngineerModeValues.config_use_wnsrp);
            log(" - mVTEngineerModeValues.config_terminal_type = " + mVTEngineerModeValues.config_terminal_type);
            log(" - mVTEngineerModeValues.auto_answer = " + mVTEngineerModeValues.auto_answer);
            log(" - mVTEngineerModeValues.auto_answer_time = " + mVTEngineerModeValues.auto_answer_time);
            log(" - mVTEngineerModeValues.debug_message = " + mVTEngineerModeValues.debug_message);
            log(" - mVTEngineerModeValues.h223_raw_data = " + mVTEngineerModeValues.h223_raw_data);
            log(" - mVTEngineerModeValues.log_to_file = " + mVTEngineerModeValues.log_to_file);
            log(" - mVTEngineerModeValues.h263_only = " + mVTEngineerModeValues.h263_only);
            log(" - mVTEngineerModeValues.get_raw_data = " + mVTEngineerModeValues.get_raw_data);
            log(" - mVTEngineerModeValues.log_filter_tag_0_value = " + mVTEngineerModeValues.log_filter_tag_0_value);
            log(" - mVTEngineerModeValues.log_filter_tag_1_value = " + mVTEngineerModeValues.log_filter_tag_1_value);
            log(" - mVTEngineerModeValues.log_filter_tag_2_value = " + mVTEngineerModeValues.log_filter_tag_2_value);
            log(" - mVTEngineerModeValues.log_filter_tag_3_value = " + mVTEngineerModeValues.log_filter_tag_3_value);
        }

        VTManager.setEM(0, new Integer(mVTEngineerModeValues.working_mode).intValue(), 
                        new Integer(mVTEngineerModeValues.working_mode_detail).intValue());
        VTManager.setEM(1, 0, new Integer(mVTEngineerModeValues.config_audio_channel_adapt).intValue());
        VTManager.setEM(1, 1, new Integer(mVTEngineerModeValues.config_video_channel_adapt).intValue());
        VTManager.setEM(1, 2, new Integer(mVTEngineerModeValues.config_video_channel_reverse).intValue());
        VTManager.setEM(1, 3, new Integer(mVTEngineerModeValues.config_multiplex_level).intValue());
        VTManager.setEM(1, 4, new Integer(mVTEngineerModeValues.config_video_codec_preference).intValue());
        VTManager.setEM(1, 5, new Integer(mVTEngineerModeValues.config_use_wnsrp).intValue());
        VTManager.setEM(1, 6, new Integer(mVTEngineerModeValues.config_terminal_type).intValue());

        if (mVTEngineerModeValues.get_raw_data) {
            VTManager.setEM(3, 0, 1);
            VTManager.setEM(4, 0, 1);
            VTManager.setEM(6, 1, 0);
        } else {
            VTManager.setEM(3, 0, 0);
            VTManager.setEM(4, 0, 0);
            VTManager.setEM(6, 0, 0);
        }

        VTManager.setEM(3, 1, 0);
        VTManager.setEM(4, 1, 0);

        if (mVTEngineerModeValues.debug_message) {
            VTManager.setEM(5, 1, 0);
        } else {
            VTManager.setEM(5, 0, 0);
        }

        if (mVTEngineerModeValues.log_to_file) {
            VTManager.setEM(7, 1, 0);
        } else {
            VTManager.setEM(7, 0, 0);
        }

        VTManager.setEM(8, 0, mVTEngineerModeValues.log_filter_tag_0_value);
        VTManager.setEM(8, 1, mVTEngineerModeValues.log_filter_tag_1_value);
        VTManager.setEM(8, 2, mVTEngineerModeValues.log_filter_tag_2_value);
        VTManager.setEM(8, 3, mVTEngineerModeValues.log_filter_tag_3_value);

        if (mVTEngineerModeValues.h263_only) {
            VTManager.setEM(9, 1, 0);
        } else {
            VTManager.setEM(9, 0, 0);
        }
    }


    /**
     * Listener to push VT Setting parameters to InCallUI, implemented by CallHandlerServiceProxy
     */
    public interface Listener {
        void pushVTSettingParams(VTSettingParams params, Bitmap bitmap);
    }

    private Listener mListener;
    private VTSettingParams mVTSettingParams = new VTSettingParams();

    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * push VT Settings on recorded slot to InCallUI.
     */
    public void pushVTSettingParams() {
        if (mSlotId != INVALID_SLOT_ID) {
            pushVTSettingParams(mSlotId);
        } else {
            log("pushVTSettingParams()...mSlotId is invalid(-1), need check !!!");
        }
    }

    /**
     * push VT Settings on certain slot to InCallUI.
     * @param slotId
     */
    public void pushVTSettingParams(int slotId) {
        log("pushVTSettingParams()...slotId : " + slotId);

        // update VTSettingUtils' values.
        updateVTSettings(slotId);
        // build up VTSettingParams which will be pushed to InCallUI from VTSettingUtils.
        updateVTSettingParams();
        if (mListener != null) {
            mListener.pushVTSettingParams(mVTSettingParams, mReplacePeerBitmap);
        } else {
            log("pushVTSettingParams()...mListener has not been set, need check !!!");
        }
    }

    private void updateVTSettingParams() {
        mVTSettingParams.mPicToReplaceLocal = mPicToReplaceLocal;
        mVTSettingParams.mEnableBackCamera = mEnableBackCamera;
        mVTSettingParams.mPeerBigger = mPeerBigger;
        mVTSettingParams.mShowLocalMO = mShowLocalMO;
        mVTSettingParams.mShowLocalMT = mShowLocalMT;
        mVTSettingParams.mAutoDropBack = mAutoDropBack;
        mVTSettingParams.mToReplacePeer = mToReplacePeer;
        mVTSettingParams.mPicToReplacePeer = mPicToReplacePeer;
        mVTSettingParams.mVTIsMT = VTCallFlags.getInstance().mVTIsMT;
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

}
