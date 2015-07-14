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

import android.content.Intent;
import android.os.AsyncResult;
import android.provider.Settings;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.phone.PhoneGlobals;
import com.mediatek.phone.Utils;

public class CallCommand extends BlockingCommand {

    // Use some public call to test, these calls can be connected and disconnect auto.
    // Tester should control the time of test case.
    // Experience: 10010 and 10011 can keep about 50s between Active and Disconnect.
    public static String FIRST_CALL_BY_SLOT_0 = "Call 10010 0";
    public static String FIRST_CALL_BY_SLOT_1 = "Call 10010 1";
    public static String SECOND_CALL_BY_SLOT_0 = "Call 10011 0";
    public static String SECOND_CALL_BY_SLOT_1 = "Call 10011 1";
    // Experience: 10086 some times can just keep 20s between Active and Disconnect.
    // Should be careful about the time when use 10086 to test.
    public static String THIRD_CALL_BY_SLOT_0 = "Call 10086 0";
    public static String THIRD_CALL_BY_SLOT_1 = "Call 10086 1";

    @Override
    protected int beforeExecute() {
        super.beforeExecute();
        boolean airOn = Settings.Global.getInt(PhoneGlobals.getInstance().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        log("beforeExecute airOn: " + airOn);
        if (airOn) {
            AutotestEngine.getInstance().execute("Airplane");
            AutotestEngine.getInstance().getInstrumentation().waitForIdleSync();
        }
        airOn = Settings.Global.getInt(PhoneGlobals.getInstance().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        if (airOn) {
            log("!!!beforeExecute close ariplane mode failed!!!");
            return ICommand.RESULT_ABORT;
        }
        if (!PhoneUtils.okToAddCall(PhoneGlobals.getInstance().mCM)) {
            return ICommand.RESULT_ABORT;
        }
        return ICommand.RESULT_OK;
    }

    @Override
    protected int executeInner(String content) {
        log("executeInner parameters: " + content);
        final String[] args = content.split(" ");
        final int slot = Integer.parseInt(args[1]);
        for (int i = 0; i < 10 && !AutotestEngineUtils.checkIfOkToInitiateOutgoingCall(slot); i++) {
            Utils.sleep(5000);
        }
        boolean isSimInService = AutotestEngineUtils.checkIfOkToInitiateOutgoingCall(slot);
        log("isSimInService: " + isSimInService + " slot: " + slot);
        if (isSimInService) {
            final Intent intent = AutotestEngineUtils.generateDialIntent(
                    AutotestEngineUtils.DIAL_VOICE_CALL, args[0], slot);
            log("dialMOCall " + args[0]);
            AutotestEngineUtils.dialMOCallWithIntent(intent);
            return ICommand.RESULT_OK;
        } else {
            log("!!!sim not in service!!!");
            return ICommand.RESULT_ABORT;
        }
    }

    @Override
    public void onPhoneStateChanged(AsyncResult r) {
        CallManager cm = PhoneGlobals.getInstance().mCM;
        Call fgCall = cm.getActiveFgCall();
        if (fgCall.getState() == Call.State.DIALING) {
            log("dialing...");
        } else if (fgCall.getState() == Call.State.ALERTING) {
            log("alerting...");
        } else if (fgCall.getState() == Call.State.ACTIVE) {
            log("active...");
            notify(ICommand.RESULT_OK);
        }
    }
}
