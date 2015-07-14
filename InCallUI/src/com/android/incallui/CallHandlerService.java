/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.incallui;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.android.services.telephony.common.AudioMode;
import com.android.services.telephony.common.Call;
import com.android.services.telephony.common.DualtalkCallInfo;
import com.android.services.telephony.common.ICallCommandService;
import com.android.services.telephony.common.ICallHandlerService;
import com.mediatek.services.telephony.common.VTManagerParams;
import com.mediatek.services.telephony.common.VTSettingParams;
import com.mediatek.incallui.ext.ExtensionManager;
import com.mediatek.incallui.volte.VoLteConfCallList;
import com.mediatek.incallui.vt.VTManagerLocal;
import com.mediatek.incallui.vt.VTSettingLocal;
import com.mediatek.incallui.vt.VTUIFlags;
import com.mediatek.services.telephony.common.VoLteConferenceMember;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * Service used to listen for call state changes.
 */
public class CallHandlerService extends Service {

    private final static String TAG = CallHandlerService.class.getSimpleName();

    private static final int ON_UPDATE_CALL = 1;
    private static final int ON_UPDATE_MULTI_CALL = 2;
    private static final int ON_UPDATE_CALL_WITH_TEXT_RESPONSES = 3;
    private static final int ON_AUDIO_MODE = 4;
    private static final int ON_SUPPORTED_AUDIO_MODE = 5;
    private static final int ON_DISCONNECT_CALL = 6;
    private static final int ON_BRING_TO_FOREGROUND = 7;
    private static final int ON_POST_CHAR_WAIT = 8;
    private static final int ON_START = 9;
    private static final int ON_DESTROY = 10;

    /// M: unuse Google code:@{
    /*
    private static final int LARGEST_MSG_ID = ON_DESTROY;
    */
    ///@}


    private CallList mCallList;
    private Handler mMainHandler;
    private Object mHandlerInitLock = new Object();
    private InCallPresenter mInCallPresenter;
    private AudioModeProvider mAudioModeProvider;
    private boolean mServiceStarted = false;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        synchronized(mHandlerInitLock) {
            if (mMainHandler == null) {
                mMainHandler = new MainHandler();
            }
        }

        /// M: Plug-in @{
        ExtensionManager.registerApplicationContext(getApplicationContext());
        /// @}

        /// M: ALPS01383147 @{
        startServiceForeground();
        /// @}
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        // onDestroy will get called when:
        // 1) there are no more calls
        // 2) the client (TeleService) crashes.
        //
        // Because onDestroy is not sequenced with calls to CallHandlerService binder,
        // we cannot know which is happening.
        // Thats okay since in both cases we want to end all calls and let the UI know it can tear
        // itself down when it's ready. Start the destruction sequence.
        mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_DESTROY));

        /// M: ALPS01383147 @{
        Log.d(TAG, "stopServiceForeground");
        stopForeground(true);
        /// @}
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");

        // Returning true here means we get called on rebind, which is a feature we do not need.
        // Return false so that all reconnections happen with a call to onBind().
        return false;
    }

    private final ICallHandlerService.Stub mBinder = new ICallHandlerService.Stub() {

        @Override
        public void startCallService(ICallCommandService service) {
            try {
                Log.d(TAG, "startCallService: " + service.toString());

                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_START, service));
            } catch (Exception e) {
                Log.e(TAG, "Error processing setCallCommandservice() call", e);
            }
        }

        @Override
        public void onDisconnect(Call call) {
            try {
                Log.i(TAG, "onDisconnected: " + call);
                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_DISCONNECT_CALL, call));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onDisconnect() call.", e);
            }
        }

        @Override
        public void onIncoming(Call call, List<String> textResponses) {
            try {
                Log.i(TAG, "onIncomingCall: " + call);
                Map.Entry<Call, List<String>> incomingCall
                        = new AbstractMap.SimpleEntry<Call, List<String>>(call, textResponses);
                mMainHandler.sendMessage(mMainHandler.obtainMessage(
                        ON_UPDATE_CALL_WITH_TEXT_RESPONSES, incomingCall));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onIncoming() call.", e);
            }
        }

        @Override
        public void onUpdate(List<Call> calls) {
            try {
                Log.i(TAG, "onUpdate: " + calls);
                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_UPDATE_MULTI_CALL, calls));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onUpdate() call.", e);
            }
        }

        @Override
        public void onAudioModeChange(int mode, boolean muted) {
            try {
                Log.i(TAG, "onAudioModeChange : " +
                        AudioMode.toString(mode));
                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_AUDIO_MODE, mode,
                            muted ? 1 : 0, null));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onAudioModeChange() call.", e);
            }
        }

        @Override
        public void onSupportedAudioModeChange(int modeMask) {
            try {
                Log.i(TAG, "onSupportedAudioModeChange : " +
                        AudioMode.toString(modeMask));
                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_SUPPORTED_AUDIO_MODE,
                        modeMask, 0, null));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onSupportedAudioModeChange() call.", e);
            }
        }

        @Override
        public void bringToForeground(boolean showDialpad) {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_BRING_TO_FOREGROUND,
                    showDialpad ? 1 : 0, 0));
        }

        @Override
        public void onPostDialWait(int callId, String chars) {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_POST_CHAR_WAIT, callId, 0,
                    chars));
        }

        // ---------------- MTK --------------------

        public void onUpdateRecordState(final int state, final int customValue) {
            try {
                Log.i(TAG, "state = " + state + "customValue = " + customValue);
                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_UPDATE_RECORD_STATE, state,
                        customValue));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onUpdateRecordState().", e);
            }
        }

        public void onStorageFull() {
            try {
                Log.i(TAG, "onStorageFull");
                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_STORAGE_FULL));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onStorageFull().", e);
            }
        }

        public void onSuppServiceFailed(String message) {
            try {
                Log.i(TAG, "onSuppServiceFailed");
                mMainHandler.sendMessage(mMainHandler
                        .obtainMessage(ON_SUPP_SERVICE_FAILED, message));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onSuppServiceFailed().", e);
            }
        }

        public void onVTStateChanged(int msgVT) {
            try {
                Log.i(TAG, "onVTStateChanged()... state = " + msgVT);
                mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_VT_STATE_CHANGE, msgVT));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onVTStateChanged().", e);
            }
        }

        public void pushVTSettingParams(VTSettingParams params, Bitmap bitmap) {
            try {
                Log.i(TAG, "pushVTSettingParams()...");
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_VT_SETTING_PARAMS, params);
                bundle.putParcelable(KEY_VT_SETTING_PARAMS_BITMAP, bitmap);
                Message msg = mMainHandler.obtainMessage(PUSH_VT_SETTING_PARAMS);
                msg.setData(bundle);
                mMainHandler.sendMessage(msg);
            } catch (Exception e) {
                Log.e(TAG, "Error processing pushVTSettingParams().", e);
            }
        }

        public void pushVTManagerParams(VTManagerParams params) {
            // because Binder will catch except from binder, so need do things in our thread.
            try {
                Log.i(TAG, "pushVTManagerParams()...");
                mMainHandler.sendMessage(mMainHandler.obtainMessage(PUSH_VT_MANAGER_PARAMS, params));
            } catch (Exception e) {
                Log.e(TAG, "Error processing pushVTManagerParams().", e);
            }
        }

        @Override
        public void updateDualtalkCallStatus(DualtalkCallInfo info) {
            Log.e(TAG, "[updateDualtalkCallStatus], info:" + info);
            mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_DUALTALK_INFO_UPDATE, info));
        }

        @Override
        public void onVoLteConferenceUpdate(int conferenceId, List<VoLteConferenceMember> members) {
            Log.i(TAG, "onVoLteConferenceUpdate~~");
            mMainHandler.sendMessage(mMainHandler.obtainMessage(ON_VOLTE_CONFERENCE_INFO_UPDATE, conferenceId, -1, members));
        }

        @Override
        public void onCrssSuppServiceNumberUpdate(int callId, String number) {
            try {
                Log.i(TAG, "onCrssSuppServiceNumberUpdate()... callId = " + callId +", number = " + number);
                mMainHandler.sendMessage(
                        mMainHandler.obtainMessage(ON_CRSS_SUPP_SERVICE_NUMBER_UPDATE, callId, -1, number));
            } catch (Exception e) {
                Log.e(TAG, "Error processing onCrssSuppServiceNumberUpdate()...", e);
            }
        }
    };

    private void doStart(ICallCommandService service) {
        Log.i(TAG, "doStart");

        // always setup the new callcommandservice
        CallCommandClient.getInstance().setService(service);

        // If we have a new service when one is already started, we can continue
        // using the service that we already have.
        if (mServiceStarted) {
            Log.i(TAG, "Starting a service before another one is completed");
            doStop();
        }

        mCallList = CallList.getInstance();
        mAudioModeProvider = AudioModeProvider.getInstance();
        mInCallPresenter = InCallPresenter.getInstance();

        mInCallPresenter.setUp(getApplicationContext(), mCallList, mAudioModeProvider);

        mServiceStarted = true;
    }

    public void doStop() {
        Log.i(TAG, "doStop");

        if (!mServiceStarted) {
            Log.i(TAG, "[doStop]service not started, skip doStop");
            return;
        }

        mServiceStarted = false;

        // We are disconnected, clear the call list so that UI can start
        // tearing itself down.
        ///M: if we still have incoming or outgoing call just one call is disconnected(ing)
        /// we do not clear call list and reset incallpresenter since they are single instance.
        if (mCallList.clearOnDisconnect()) {
            return;
        }
        mCallList = null;

        mInCallPresenter.tearDown();
        mInCallPresenter = null;
        mAudioModeProvider = null;
    }

    /**
     * Handles messages from the service so that they get executed on the main thread, where they
     * can interact with UI.
     */
    private class MainHandler extends Handler {
        MainHandler() {
            super(getApplicationContext().getMainLooper(), null, true);
        }

        @Override
        public void handleMessage(Message msg) {
            executeMessage(msg);
        }
    }

    private void executeMessage(Message msg) {
        if (msg.what > LARGEST_MSG_ID) {
            // If you got here, you may have added a new message and forgotten to
            // update LARGEST_MSG_ID
            Log.wtf(TAG, "Cannot handle message larger than LARGEST_MSG_ID.");
        }

        // If we are not initialized, ignore all messages except start up
        if (!mServiceStarted && msg.what != ON_START) {
            Log.i(TAG, "System not initialized.  Ignoring message: " + msg.what);
            return;
        }

        Log.d(TAG, "executeMessage " + msg.what);
        /// M:@{
        handleMTKMessage(msg);
        /// @}

        switch (msg.what) {
            case ON_UPDATE_CALL:
                Log.i(TAG, "ON_UPDATE_CALL: " + msg.obj);
                mCallList.onUpdate((Call) msg.obj);
                break;
            case ON_UPDATE_MULTI_CALL:
                Log.i(TAG, "ON_UPDATE_MULTI_CALL: " + msg.obj);
                mCallList.onUpdate((List<Call>) msg.obj);
                break;
            case ON_UPDATE_CALL_WITH_TEXT_RESPONSES:
                AbstractMap.SimpleEntry<Call, List<String>> entry
                        = (AbstractMap.SimpleEntry<Call, List<String>>) msg.obj;
                Log.i(TAG, "ON_INCOMING_CALL: " + entry.getKey());
                mCallList.onIncoming(entry.getKey(), entry.getValue());
                break;
            case ON_DISCONNECT_CALL:
                Log.i(TAG, "ON_DISCONNECT_CALL: " + msg.obj);
                /// M: for VT auto Dropback@{
                // need to use the context
                /* Original code:
                mCallList.onDisconnect((Call) msg.obj);
                */
                mCallList.onDisconnect(getApplicationContext(), (Call) msg.obj);
                /// @}
                break;
            case ON_POST_CHAR_WAIT:
                mInCallPresenter.onPostDialCharWait(msg.arg1, (String) msg.obj);
                break;
            case ON_AUDIO_MODE:
                Log.i(TAG, "ON_AUDIO_MODE: " +
                        AudioMode.toString(msg.arg1) + ", muted (" + (msg.arg2 == 1) + ")");
                mAudioModeProvider.onAudioModeChange(msg.arg1, msg.arg2 == 1);
                break;
            case ON_SUPPORTED_AUDIO_MODE:
                Log.i(TAG, "ON_SUPPORTED_AUDIO_MODE: " + AudioMode.toString(
                        msg.arg1));

                mAudioModeProvider.onSupportedAudioModeChange(msg.arg1);
                break;
            case ON_BRING_TO_FOREGROUND:
                Log.i(TAG, "ON_BRING_TO_FOREGROUND" + msg.arg1);
                if (mInCallPresenter != null) {
                    mInCallPresenter.bringToForeground(msg.arg1 != 0);
                }
                break;
            case ON_START:
                doStart((ICallCommandService) msg.obj);
                break;
            case ON_DESTROY:
                doStop();
                break;
            default:
                break;
		}
		if ((msg.what == ON_DISCONNECT_CALL)
				|| (msg.what == ON_VT_STATE_CHANGE && (Integer) msg.obj == VTManagerLocal.VT_MSG_CLOSE)) {
			CallCommandClient.getInstance().unbindAfterUiUpdate();
		}
    }

    //-------------------------------------MTK-----------------------------
    private static final int ON_UPDATE_RECORD_STATE = 11;
    private static final int ON_STORAGE_FULL = 12;
    private static final int ON_SUPP_SERVICE_FAILED = 13;
    private static final int ON_DUALTALK_INFO_UPDATE = 14;
    private static final int ON_VT_STATE_CHANGE = 15;
    private static final int VT_ANSWER_CALL_PRE = 16;
    private static final int VT_DIAL_CALL_SUCCESS = 17;
    private static final int PUSH_VT_MANAGER_PARAMS = 18;
    private static final int PUSH_VT_SETTING_PARAMS = 19;
    private static final int ON_CRSS_SUPP_SERVICE_NUMBER_UPDATE = 20;
    /// M: for VoLTE Conference Call @{
    private static final int ON_VOLTE_CONFERENCE_INFO_UPDATE = 21;
    /// @}
    private static final int LARGEST_MSG_ID = ON_VOLTE_CONFERENCE_INFO_UPDATE;

    // below are Key value for VT, not message
    private static final String KEY_VT_SETTING_PARAMS = "KEY_VT_SETTING_PARAMS";
    private static final String KEY_VT_SETTING_PARAMS_BITMAP = "KEY_VT_SETTING_PARAMS_BITMAP";

    private boolean handleMTKMessage(Message msg) {
        switch (msg.what) {
            case ON_STORAGE_FULL:
                mCallList.onStorageFull();
                return true;
            case ON_UPDATE_RECORD_STATE:
                Log.i(TAG, "msg.arg1 = " + msg.arg1 + "msg.arg2 = " + msg.arg2);
                mCallList.onUpdateRecordState(msg.arg1, msg.arg2);
                return true;
            case ON_SUPP_SERVICE_FAILED:
                Log.i(TAG, "msg.obj = " + (String)msg.obj);
                mCallList.onSuppServiceFailed((String)msg.obj);
                return true;
            case ON_VT_STATE_CHANGE:
                Log.i(TAG, "ON_VT_STATE_CHANGE, msg.obj = " + msg.obj);
                VTManagerLocal.getInstance().onVTStateChanged((Integer)msg.obj);
                return true;
            case PUSH_VT_MANAGER_PARAMS:
                Log.i(TAG, "PUSH_VT_MANAGER_PARAMS");
                VTManagerLocal.getInstance().getVTManagerParams((VTManagerParams)msg.obj);
                return true;
            case PUSH_VT_SETTING_PARAMS:
                Log.i(TAG, "PUSH_VT_SETTING_PARAMS");
                Bundle bundle = msg.getData();
                VTSettingParams params = (VTSettingParams)bundle.getParcelable(KEY_VT_SETTING_PARAMS);
                Bitmap bitmap = bundle.getParcelable(KEY_VT_SETTING_PARAMS_BITMAP);
                VTSettingLocal.getInstance().getVTSettingParams(params, bitmap);
                VTUIFlags.getInstance().initVTUIFlags(VTSettingLocal.getInstance());
            case ON_DUALTALK_INFO_UPDATE:
                Log.i(TAG, "msg.obj = " + msg.obj);
                InCallPresenter.getInstance().updateDualtalkCallInfo((DualtalkCallInfo)msg.obj);
                return true;
            case ON_CRSS_SUPP_SERVICE_NUMBER_UPDATE:
                Log.i(TAG, "ON_CRSS_SUPP_SERVICE_NUMBER_UPDATE");
                mCallList.onCrssSuppServiceNumberUpdate(getApplicationContext(),msg.arg1, (String)msg.obj);
                return true;
            /// M: for VoLTE Conference Call @{
            case ON_VOLTE_CONFERENCE_INFO_UPDATE:
                Log.i(TAG, "ON_VOLTE_CONFERENCE_INFO_UPDATE");
                VoLteConfCallList.getInstance().onVoLteConferenceUpdate(msg.arg1, (List<VoLteConferenceMember>) msg.obj);
                return true;
            /// @}
            default:
                break;
        }
        return false;
    }

    /**
     * improve CallHandlerService Prority.
     */
    private static final int ON_GOING = 800;
    public void startServiceForeground() {
        Log.d(TAG, "startServiceForeground");
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setOngoing(true);
        // Make the notification prioritized over the other normal notifications.
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setContentText(null);
        builder.setSmallIcon(0);
        builder.setWhen(System.currentTimeMillis());

        Notification noti = builder.build();
        noti.flags |= Notification.FLAG_NO_CLEAR;
        noti.flags |= Notification.FLAG_HIDE_NOTIFICATION;
        startForeground(ON_GOING, noti);
    }
}
