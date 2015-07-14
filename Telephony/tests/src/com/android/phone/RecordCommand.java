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

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.Constants;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.mediatek.phone.recording.PhoneRecorder;
import com.mediatek.phone.recording.PhoneRecorderHandler;
import com.mediatek.phone.Utils;

public class RecordCommand extends DefaultCommand {
    private CallManager mCallManager;
    private boolean mIsRecording;

    @Override
    protected int beforeExecute() {
        super.beforeExecute();
        int retval = ICommand.RESULT_OK;
        mCallManager = PhoneGlobals.getInstance().mCM;
        if (mCallManager.getState() != PhoneConstants.State.OFFHOOK
                || !PhoneUtils.okToRecordVoice(mCallManager)) {
            retval = ICommand.RESULT_ABORT;
            log("!!!beforeExecute call state is not offhook or can not record!!!");
        } else {
            mIsRecording = PhoneRecorder.isRecording();
        }

        return retval;
    }

    @Override
    protected int executeInner(String parameters) {
        log("executeInner");
        final Instrumentation instrumentation = AutotestEngine.getInstance().getInstrumentation();
        if (!mIsRecording) {
            PhoneRecorderHandler.getInstance().startVoiceRecord(Constants.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE);
        } else {
            PhoneRecorderHandler.getInstance().stopRecording();
        }
        Utils.sleep(2000);
        instrumentation.waitForIdleSync();
        return ICommand.RESULT_OK;
    }

    @Override
    protected int afterExecute() {
        int retval = ICommand.RESULT_OK;
        final boolean isRecording = PhoneRecorder.isRecording();
        if (mIsRecording == isRecording) {
            log("!!!afterExecute record failed!!!");
            retval = ICommand.RESULT_FAIL;
        }
        log("PhoneRecorder.isRecording() = " + isRecording);
        return retval;
    }
}
