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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.android.phone.CallStateMonitor;
import com.android.phone.PhoneGlobals;
import com.mediatek.phone.Utils;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.phone.wrapper.CallManagerWrapper;

import java.util.ArrayList;


public class AutotestEngine {

    static final String TAG = "AutotestEngine";

    public static final int MSG_PHONE_STATE_CHANGED = CallStateMonitor.PHONE_STATE_CHANGED;

    public static final int MSG_PHONE_DISCONNECT = CallStateMonitor.PHONE_DISCONNECT;
    public static final int MSG_SERVICE_STATE_CHANGED = 2;

    public static final int MSG_PHONE_DISCONNECT2 = 103;
    public static final int MSG_SERVICE_STATE_CHANGED2 = 1003;

    public static final int MSG_PHONE_DISCONNECT3 = 203;
    public static final int MSG_SERVICE_STATE_CHANGED3 = 2003;

    public static final int MSG_PHONE_DISCONNECT4 = 303;
    public static final int MSG_SERVICE_STATE_CHANGED4 = 3003;

    public static final int[] MSG_PHONE_DISCONNECTS = CallStateMonitor.PHONE_DISCONNECT_GEMINI;

    public static final int[] MSG_SERVICE_STATE_CHANGEDS = { MSG_SERVICE_STATE_CHANGED,
            MSG_SERVICE_STATE_CHANGED2, MSG_SERVICE_STATE_CHANGED3, MSG_SERVICE_STATE_CHANGED4 };

    private static AutotestEngine sEngine;

    protected Instrumentation mInstrumentation;

    Handler mTargetHandler;

    ArrayList<Listener> mListeners = new ArrayList<Listener>();

    boolean mInit = false;

    private AutotestEngine() {
        //
    }

    public static AutotestEngine makeInstance(Instrumentation testCase) {
        log("makeInstance start+ " + java.lang.System.currentTimeMillis());
        AutotestEngine engine = AutotestEngine.getInstance();
        engine.setInstrumentation(testCase);
        engine.init();
        testCase.waitForIdleSync();
        log("makeInstance end- " + java.lang.System.currentTimeMillis());
        return engine;
    }

    protected void init() {
        if (mInit) {
            return;
        }
        mInit = true;
        log("+init");

        try {
            waitForPhoneProcessReady();
        } catch (IllegalStateException e) {
            log("PhoneGlobal not ready!");
        }

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                mTargetHandler = new TargetHandler();
            }
        });

        registerPhoneState();
        log("-init");
    }

    public boolean waitForPhoneProcessReady() throws IllegalStateException {
        log("waitForPhoneProcessReady");
        PhoneGlobals app = null;
        int retry = 10;

        // first, wait phone process ready
        while (app == null) {
            try {
                Thread.sleep(10000);
                retry--;
                if (retry == 0) {
                    break;
                }
                app = PhoneGlobals.getInstance();
            } catch (InterruptedException e) {
                log(e.getMessage());
            }
        }
        if (retry == 0) {
            log("create phone application time out");
            return false;
        }

        // second, wait Phone ready
        retry = 10;
        while (app.phone == null) {
            try {
                Thread.sleep(10000);
                retry--;
                if (retry == 0) {
                    break;
                }
            } catch (InterruptedException e) {
                log(e.getMessage());
            }
        }
        if (retry == 0) {
            log("create Phone time out");
            return false;
        }

        return true;
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        mInstrumentation = instrumentation;
    }

    public Instrumentation getInstrumentation() {
        return mInstrumentation;
    }

    public static AutotestEngine getInstance() {
        if (sEngine == null) {
            sEngine = new AutotestEngine();
        }
        return sEngine;
    }

    /**
     * use this function to start command from testcase.
     * @param command the type like "Call 10086 1"
     * @return
     */
    public int execute(String command) {
        log("--------execute start command------------------------ " + command);
        if (TextUtils.isEmpty(command)) {
            log("execute command is error " + command);
            return ICommand.RESULT_COMMAND_NOT_SUPPORT;
        }

        int result = ICommand.RESULT_OK;
        String name;
        String parameters = null;

        // get command name and content
        final int index = command.indexOf(' ');
        if (index > 0) {
            name = command.substring(0, index);
            parameters = command.substring(index + 1);
        } else {
            name = command;
        }

        // create command
        ICommand c = CommandFactory.getInstance().getCommand(name);
        if (c == null) {
            log("create command error name: " + name);
            return ICommand.RESULT_COMMAND_NOT_SUPPORT;
        }

        // execute command
        log("execute start: " + command);
        result = c.execute(parameters);

        log("----------------execute end result----------------------- " + AutotestEngineUtils.resultToString(result));
        return result;
    }

    private void registerPhoneState() {
        CallManagerWrapper.registerForServiceStateChanged(PhoneGlobals.getInstance().phone,
                mTargetHandler, MSG_SERVICE_STATE_CHANGEDS);
        CallManagerWrapper.registerForPreciseCallStateChanged(mTargetHandler,
                CallStateMonitor.PHONE_STATE_CHANGED);
        CallManagerWrapper.registerForDisconnect(mTargetHandler, CallStateMonitor.PHONE_DISCONNECT_GEMINI);
        CallManagerWrapper.registerForSuppServiceFailed(mTargetHandler, CallStateMonitor.SUPP_SERVICE_FAILED);
    }

    private void unregisterPhoneState() {
        CallManagerWrapper.unregisterForPreciseCallStateChanged(mTargetHandler);
        CallManagerWrapper.unregisterForDisconnect(mTargetHandler);
    }

    protected void onPhoneStateChanged(AsyncResult r) {

        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onPhoneStateChanged(r);
        }
    }

    private void onDisconnect(AsyncResult r, int slot) {

        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onDisconnect(r, slot);
        }
    }

    public void onServiceStateChanged(AsyncResult result, int slot) {

        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onServiceStateChanged(result, slot);
        }
    }

    public void onSuppServiceFailed(AsyncResult result) {

        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onSuppServiceFailed(result);
        }
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    class TargetHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PHONE_STATE_CHANGED:
                    log("MSG_PHONE_STATE_CHANGED");
                    onPhoneStateChanged((AsyncResult) msg.obj);
                    break;
                case MSG_PHONE_DISCONNECT:
                case MSG_PHONE_DISCONNECT2:
                case MSG_PHONE_DISCONNECT3:
                case MSG_PHONE_DISCONNECT4:
                    int disconnectSlot = GeminiUtils.getSlotIdByRegisterEvent(msg.what, MSG_PHONE_DISCONNECTS);
                    log("MSG_PHONE_DISCONNECT  slot=" + disconnectSlot);
                    onDisconnect((AsyncResult) msg.obj, disconnectSlot);
                    break;
                case MSG_SERVICE_STATE_CHANGED:
                case MSG_SERVICE_STATE_CHANGED2:
                case MSG_SERVICE_STATE_CHANGED3:
                case MSG_SERVICE_STATE_CHANGED4:
                    int serviceStateChangeSlot = GeminiUtils.getSlotIdByRegisterEvent(msg.what,
                            MSG_SERVICE_STATE_CHANGEDS);
                    log("MSG_SERVICE_STATE_CHANGED slot=" + serviceStateChangeSlot);
                    onServiceStateChanged((AsyncResult) msg.obj, serviceStateChangeSlot);
                    break;
                case CallStateMonitor.SUPP_SERVICE_FAILED:
                    log("SUPP_SERVICE_FAILED");
                    onSuppServiceFailed((AsyncResult) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    static void log(String msg) {
        Utils.log(TAG, "[AutotestEngine] " + msg);
    }

    public interface Listener {
        void onPhoneStateChanged(AsyncResult r);

        void onDisconnect(AsyncResult r, int slot);

        void onServiceStateChanged(AsyncResult r, int slot);

        void onSuppServiceFailed(AsyncResult r);
    }

    public void release() {
        unregisterPhoneState();
        sEngine = null;
    }
}
