/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

package com.android.phone;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.telephony.ServiceState;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.Constants;
import com.android.phone.PhoneGlobals;
import com.android.phone.Constants.CallStatusCode;
import com.mediatek.phone.DualTalkUtils;
import com.mediatek.phone.Utils;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.phone.wrapper.PhoneWrapper;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;

import java.lang.reflect.Field;
import java.util.List;

import junit.framework.Assert;

public class AutotestEngineUtils {

    private static final String TAG = "AutotestEngineUtils";

    private static final String RESULT_UNKNOWN = "UNKNOWN";
    private static final String RESULT_OK = "OK";
    private static final String RESULT_FAIL = "FAIL";
    private static final String RESULT_COMMAND_NOT_SUPPORT = "COMMAND_NOT_SUPPORT";
    private static final String RESULT_TIME_OUT = "TIME_OUT";
    private static final String RESULT_ABORT = "ABORT";

    public static final int DIAL_VOICE_CALL = 0;
    public static final int DIAL_VIDEO_CALL = 1;
    public static final int DIAL_SIP_CALL = 2;

    protected static int sSimState = -1;

    /**
     * make a common call intent
     * @param type
     * @param number
     * @param slot
     * @return
     */
    static Intent generateDialIntent(int type, String number, int slot) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.fromParts("tel", number, null));
        intent.setAction(Intent.ACTION_CALL_PRIVILEGED);
        if (type == DIAL_VIDEO_CALL) {
            intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
        }
        intent.putExtra(Constants.EXTRA_SLOT_ID, slot);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * make a emergency call intent
     * @param number
     * @param slot
     * @return
     */
    static Intent generateEmergencyDialIntent(String number, int slot) {
        Intent intent = new Intent();
        intent.setData(Uri.fromParts("tel", number, null));
        intent.setAction(Intent.ACTION_CALL_EMERGENCY);
        if (slot >= 0) {
            intent.putExtra(Constants.EXTRA_SLOT_ID, slot);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * dial a call
     * @param intent
     */
    static void dialMOCallWithIntent(final Intent intent) {
        AutotestEngine.getInstance().getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                PhoneGlobals.getInstance().callController.placeCall(intent);
            }
        });
    }

    static String resultToString(int result) {
     switch (result) {
            case ICommand.RESULT_UNKNOWN:
                return RESULT_UNKNOWN;
            case ICommand.RESULT_OK:
                return RESULT_OK;
            case ICommand.RESULT_FAIL:
                return RESULT_FAIL;
            case ICommand.RESULT_COMMAND_NOT_SUPPORT:
                return RESULT_COMMAND_NOT_SUPPORT;
            case ICommand.RESULT_TIME_OUT:
                return RESULT_TIME_OUT;
            case ICommand.RESULT_ABORT:
                return RESULT_ABORT;
            default:
                break;
        }
        return RESULT_UNKNOWN;
    }

    static int getSimState() {
        if (sSimState == -1) {
            if (FeatureOption.isMtkGeminiSupport()) {
                List<SimInfoRecord> simInfos = SimInfoManager.getInsertedSimInfoList(PhoneGlobals.getInstance());
                if (simInfos.size() > 1) {
                    sSimState = 3;
                } else {
                    sSimState = simInfos.get(0).mSimSlotId + 1;
                }
            } else {
                sSimState = 0;
            }
        }
        return sSimState;
    }

    static void toast(final String msg) {
        Instrumentation instrumentation = AutotestEngine.getInstance().getInstrumentation();
        instrumentation.runOnMainSync(new Runnable() {
            public void run() {
                Toast.makeText(PhoneGlobals.getInstance(), msg, Toast.LENGTH_SHORT).show();
            }
        });
        instrumentation.waitForIdleSync();
    }

    static boolean checkIfOkToInitiateOutgoingCall(int slot) {
        log("checkIfOkToInitiateOutgoingCall, slot = " + slot);
        PhoneGlobals app = PhoneGlobals.getInstance();
        int realState;
        if (GeminiUtils.isGeminiSupport() && GeminiUtils.isValidSlot(slot)) {
            log("checkIfOkToInitiateOutgoingCall, Gemini Support and is Valid Slot = ");
            Phone phone = app.phone;
            if (!DualTalkUtils.isSupportDualTalk()) {
                /// M:Gemini+ @{
                final int[] geminiSlots = GeminiUtils.getSlots();
                for (int gs : geminiSlots) {
                    if (gs != slot && PhoneWrapper.getState(phone, gs) != PhoneConstants.State.IDLE) {
                        log("checkIfOkToInitiateOutgoingCall return false: other slot is not idle");
                        return false;
                    }
                }
                /// @}
            }

            if (DualTalkUtils.isSupportDualTalk()) {
                if (app.notifier.mDualTalk == null) {
                    app.notifier.mDualTalk = DualTalkUtils.getInstance();
                }
                if (!app.notifier.mDualTalk.isPhoneCallAllowed(slot)) {
                    log("checkIfOkToInitiateOutgoingCall  return false:  dualtalk phone call not allowed");
                    return false;
                }
            }

            realState = PhoneWrapper.getServiceState(phone, slot).getState();
            log("realState = " + realState);
        } else {
            realState = PhoneWrapper.getServiceState(PhoneGlobals.getPhone(), slot).getState();
        }
        log("checkIfOkToInitiateOutgoingCall, realState = " + realState);
        return realState == android.telephony.ServiceState.STATE_IN_SERVICE;
    }

    public static void assertAndWaitSync(int result) {
        assertAndWaitSync(result, false);
    }

    public static void assertAndWaitSync(int result, boolean ignoreAbort) {
        if (result != ICommand.RESULT_COMMAND_NOT_SUPPORT) {
            if (ignoreAbort) {
                // Not care about ABORT fail.
                Assert.assertTrue(result == ICommand.RESULT_OK || result == ICommand.RESULT_ABORT);
            } else {
                // The case is related with the last case, so can't ignore ABORT fail.
                Assert.assertTrue(result == ICommand.RESULT_OK);
            }
        }
        AutotestEngine.getInstance().getInstrumentation().waitForIdleSync();
        Utils.sleep(2000);
    }

    static void log(String msg) {
        Log.d(AutotestEngine.TAG, TAG +" " + msg);
    }
}
