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

import android.os.AsyncResult;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;

public class EndCommand extends BlockingCommand {

    public static final int END_RINGING = 1;
    public static final int END_ACTIVE = 2;
    public static final int END_HOLDING = 3;
    public static final int END_ALL = 4;

    protected int mEndType = END_ACTIVE;
    protected boolean mDisconnected;

    @Override
    protected int beforeExecute() {
        super.beforeExecute();
        log("beforeExecute");
        int retval = ICommand.RESULT_OK;
        final CallManager cm = PhoneGlobals.getInstance().mCM;
        if (cm.getState() == PhoneConstants.State.IDLE) {
            log("!!!beforeExecute call state is idle!!!");
            retval = ICommand.RESULT_ABORT;
        }
        mDisconnected = false;
        return retval;
    }

    @Override
    protected int executeInner(String content) {
        log("executeInner");
        String[] args = content.split(" ");
        mEndType = Integer.parseInt(args[0]);
        log("mEndType = " + mEndType);
        switch(mEndType) {
            case END_RINGING:
                endRinging();
                break;
            case END_ACTIVE:
                endActive();
                break;
            case END_HOLDING:
                endHolding();
                break;
            case END_ALL:
                endAll();
            default:
                break;
        }
        return ICommand.RESULT_OK;
    }

    @Override
    public void onDisconnect(AsyncResult r, int slot) {
        final CallManager cm = PhoneGlobals.getInstance().mCM;
        final Call fgCall = cm.getActiveFgCall();
        final Call bgCall = cm.getFirstActiveBgCall();
        log("onDisconnect fgCallState = " + fgCall.getState() + " bgCall State = " + bgCall.getState());
        mDisconnected = true;
    }

    @Override
    public void onPhoneStateChanged(AsyncResult r) {
        final CallManager cm = PhoneGlobals.getInstance().mCM;
        final Call fgCall = cm.getActiveFgCall();
        final Call bgCall = cm.getFirstActiveBgCall();
        final Call ringingCall = cm.getFirstActiveRingingCall();
        log("fgCall State = " + fgCall.getState() + " bgCall State = " + bgCall.getState());
        if (!mDisconnected) {
            return;
        }
        switch(mEndType) {
            case END_RINGING:
                if (ringingCall.getState() == Call.State.IDLE) {
                    notify(ICommand.RESULT_OK);
                }
                break;
            case END_ACTIVE:
                if (bgCall.getState() == Call.State.IDLE
                        && (fgCall.getState() == Call.State.ACTIVE
                                || fgCall.getState() == Call.State.IDLE)) {
                    notify(ICommand.RESULT_OK);
                }
                break;
            case END_HOLDING:
                if (bgCall.getState() == Call.State.IDLE) {
                    notify(ICommand.RESULT_OK);
                }
                break;
            case END_ALL:
                if (bgCall.getState() == Call.State.IDLE && fgCall.getState() == Call.State.IDLE) {
                    notify(ICommand.RESULT_OK);
                }
                break;
            default:
                break;
        }
    }

    protected void endRinging() {
        log("endRinging");
        PhoneUtils.hangupRingingCall(PhoneGlobals.getInstance().mCM.getFirstActiveRingingCall());
    }

    protected void endActive() {
        log("endActive");
        PhoneUtils.hangupActiveCall(PhoneGlobals.getInstance().mCM.getActiveFgCall());
    }

    protected void endHolding() {
        log("endHolding");
        PhoneUtils.hangupHoldingCall(PhoneGlobals.getInstance().mCM.getFirstActiveBgCall());
    }

    private void endAll() {
        log("endAll");
        PhoneUtils.hangupAllCalls();
    }

}
