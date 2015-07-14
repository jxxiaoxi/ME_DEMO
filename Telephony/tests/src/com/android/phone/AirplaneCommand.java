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
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.ServiceState;

import com.android.phone.PhoneGlobals;
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;

public class AirplaneCommand extends BlockingCommand {

    protected int mMode = 0;
    protected int mSimState = -1;
    protected int[] mServiceState = new int[2];

    @Override
    protected int beforeExecute() {
        mSimState = AutotestEngineUtils.getSimState();
        setTimeOut(60000);
        log("beforeExecute" + mSimState);
        return super.beforeExecute();
    }

    @Override
    protected int executeInner(String content) {
        final PhoneGlobals app = PhoneGlobals.getInstance();
        mMode = Settings.Global.getInt(app.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                0);
        switchAirMode();
        return ICommand.RESULT_OK;
    }

    static void switchAirMode() {
        final PhoneGlobals app = PhoneGlobals.getInstance();
        // get the now mode
        int mode = Settings.Global.getInt(app.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        int newAirMode = 0;
        int newSimMode = 3;
        if (mode == 0) {
            newAirMode = 1;
            newSimMode = 0;
        }
        // Change the airPlane
        Settings.Global.putInt(app.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                newAirMode);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", newAirMode == 1);
        app.sendBroadcast(intent);
        // Change the radio
        Settings.System.putInt(app.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING,
                newSimMode);
        final Intent intent1 = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
        intent1.putExtra(Intent.EXTRA_DUAL_SIM_MODE, newSimMode);
        app.sendBroadcast(intent1);
    }

    @Override
    public void onServiceStateChanged(AsyncResult r, int slot) {
        final ServiceState state = (ServiceState)r.result;
        int newMode = Settings.Global.getInt(PhoneGlobals.getInstance().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, -1);
        log("onServiceStateChanged, mode: " + newMode);
        mServiceState[slot] = state.getState();
        if (FeatureOption.isMtkGeminiSupport()) {
            switch (mSimState) {
                case 3:
                    if (mMode == 0 && newMode == 1) {
                        if (mServiceState[0] == ServiceState.STATE_POWER_OFF
                                && mServiceState[1] == ServiceState.STATE_POWER_OFF) {
                            notify(ICommand.RESULT_OK);
                        }
                    } else if (mMode == 1 && newMode == 0){
                        if (mServiceState[0] == ServiceState.STATE_IN_SERVICE
                                && mServiceState[1] == ServiceState.STATE_IN_SERVICE) {
                            notify(ICommand.RESULT_OK);
                        }
                    }
                    break;
                case 2:
                    if (mMode == 0 && newMode == 1) {
                        if (mServiceState[1] == ServiceState.STATE_POWER_OFF) {
                            notify(ICommand.RESULT_OK);
                        }
                    } else if (mMode == 1 && newMode == 0) {
                        if (mServiceState[1] == ServiceState.STATE_IN_SERVICE) {
                            notify(ICommand.RESULT_OK);
                        }
                    }
                    break;
                case 1:
                    if (mMode == 0 && newMode == 1) {
                        if (mServiceState[0] == ServiceState.STATE_POWER_OFF) {
                            notify(ICommand.RESULT_OK);
                        }
                    } else if (mMode == 1 && newMode == 0) {
                        if (mServiceState[0] == ServiceState.STATE_IN_SERVICE) {
                            notify(ICommand.RESULT_OK);
                        }
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (mMode == 0 && newMode == 1) {
                if (mServiceState[0] == ServiceState.STATE_POWER_OFF) {
                    notify(ICommand.RESULT_OK);
                }
            } else if (mMode == 1 && newMode == 0){
                if (mServiceState[0] == ServiceState.STATE_IN_SERVICE) {
                    notify(ICommand.RESULT_OK);
                }
            }
        }
    }

}
