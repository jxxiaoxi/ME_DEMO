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
import android.os.AsyncResult;
import android.test.TouchUtils;
import android.widget.Button;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.mediatek.phone.DualTalkUtils;

public class HoldCommand extends BlockingCommand {

    protected boolean mHolded;

    @Override
    protected int beforeExecute() {
        super.beforeExecute();
        int retval = ICommand.RESULT_OK;
        final CallManager cm = PhoneGlobals.getInstance().mCM;
        if(!PhoneUtils.okToSupportHold(cm)) {
            log("!!!beforeExecute not ok to support hold!!!");
            retval = ICommand.RESULT_ABORT;
        }
        if (cm.getState() == PhoneConstants.State.IDLE) {
            log("!!!beforeExecute call state is idle!!!");
            retval = ICommand.RESULT_ABORT;
        } else {
            final Call fgCall = cm.getActiveFgCall();
            final Call bgCall = cm.getFirstActiveBgCall();
            if (fgCall.getState() != Call.State.IDLE && bgCall.getState() != Call.State.IDLE) {
                log("!!!beforeExecute has two calls!!!");
                retval = ICommand.RESULT_ABORT;
            } else {
                Call current = fgCall;
                if (fgCall.getState() == Call.State.IDLE) {
                    current = bgCall;
                }
                if (current.getState() == Call.State.HOLDING) {
                    mHolded = true;
                } else if (current.getState() == Call.State.ACTIVE) {
                    mHolded = false;
                } else {
                    log("!!!beforeExecute call state is error!!!");
                    retval = ICommand.RESULT_ABORT;
                }
            }
        }
        return retval;
    }


    @Override
    protected int executeInner(String parameters) {
        log("executeInner");
        final Instrumentation instrumentation = AutotestEngine.getInstance().getInstrumentation();
        PhoneUtils.onHoldClick();
        instrumentation.waitForIdleSync();
        return ICommand.RESULT_OK;
    }

    @Override
    public void onPhoneStateChanged(AsyncResult r) {
        CallManager cm = PhoneGlobals.getInstance().mCM;
        Call call = null;
        if (mHolded) {
            call = cm.getActiveFgCall();
            if (call.getState() == Call.State.ACTIVE) {
                log("active...");
                if (mHolded) {
                    notify(ICommand.RESULT_OK);
                }
            }
        } else {
            call = cm.getFirstActiveBgCall();
            if (call.getState() == Call.State.HOLDING) {
                log("holding...");
                if (!mHolded) {
                    notify(ICommand.RESULT_OK);
                }
            }
        }
    }

}
