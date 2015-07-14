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

import com.android.services.telephony.common.Call;
import com.mediatek.incallui.wrapper.FeatureOptionWrapper;

import java.util.ArrayList;

/**
 * Presenter for the Incoming call widget.
 */
public class AnswerPresenter extends Presenter<AnswerPresenter.AnswerUi>
        implements CallList.CallUpdateListener, CallList.Listener {

    private static final String TAG = AnswerPresenter.class.getSimpleName();

    private int mCallId = Call.INVALID_CALL_ID;
    private Call mCall = null;

    @Override
    public void onUiReady(AnswerUi ui) {
        super.onUiReady(ui);

        Log.d(TAG, "onUiReady()...");
        final CallList calls = CallList.getInstance();
        final Call call = calls.getIncomingCall();
        // TODO: change so that answer presenter never starts up if it's not incoming.
        if (call != null) {
            processIncomingCall(call);
        }

        // Listen for incoming calls.
        calls.addListener(this);
    }

    @Override
    public void onUiUnready(AnswerUi ui) {
        super.onUiUnready(ui);

        CallList.getInstance().removeListener(this);

        // This is necessary because the activity can be destroyed while an incoming call exists.
        // This happens when back button is pressed while incoming call is still being shown.
        if (mCallId != Call.INVALID_CALL_ID) {
            CallList.getInstance().removeCallUpdateListener(mCallId, this);
        }
    }

    @Override
    public void onCallListChange(CallList callList) {
        // no-op
        /// M: Add to update reject call message. @{
        Call incomingCall = callList.getIncomingCall();
        if (incomingCall != null) {
            mCall = incomingCall;
            Log.d(this, "[onCallListChange], mCall: " + mCall);
            getUi().updatePromptsMessage(true);
        }
        /// @}

        /// For "voice answer" and "answer and reject" menu update @{
        if (getUi() != null) {
            getUi().updateIncomingCallMenuButton();
        }
        /// @}
    }

    @Override
    public void onDisconnect(Call call) {
        // no-op
    }

    @Override
    public void onIncomingCall(Call call) {
        // TODO: Ui is being destroyed when the fragment detaches.  Need clean up step to stop
        // getting updates here.
        Log.d(this, "onIncomingCall: " + this);
        if (getUi() != null) {
            if (call.getCallId() != mCallId) {
                // A new call is coming in.
                processIncomingCall(call);
            }
            getUi().updateIncomingCallMenuButton();
        }
    }

    private void processIncomingCall(Call call) {
        /// M: For DualTalk @{
        // before mCallId change, remove listener for previous Call.
        // Or previous call's state change will affect us.(onCallStateChanged() will be called.)
        CallList.getInstance().removeCallUpdateListener(mCallId, this);
        /// @}

        mCallId = call.getCallId();
        mCall = call;

        // Listen for call updates for the current call.
        CallList.getInstance().addCallUpdateListener(mCallId, this);

        Log.d(TAG, "Showing incoming for call id: " + mCallId + " " + this);
        final ArrayList<String> textMsgs = CallList.getInstance().getTextResponses(
                call.getCallId());
        getUi().showAnswerUi(true);

        if (call.can(Call.Capabilities.RESPOND_VIA_TEXT) && textMsgs != null) {
            getUi().showTextButton(true);
            getUi().configureMessageDialog(textMsgs);
        } else {
            getUi().showTextButton(false);
        }
    }


    @Override
    public void onCallStateChanged(Call call) {
        Log.d(this, "onCallStateChange() " + call + " " + this);

        if (call.getState() != Call.State.INCOMING && call.getState() != Call.State.CALL_WAITING) {
            // Stop listening for updates.
            CallList.getInstance().removeCallUpdateListener(mCallId, this);

            /// M: For Dualtalk feature. @{
            // when previous incoming call becomes non-incoming, should update mCallId if has another incoming call exist.
            if (FeatureOptionWrapper.isSupportDualTalk()
                    && CallList.getInstance().getIncomingCall() != null) {
                processIncomingCall(CallList.getInstance().getIncomingCall());
                return;
            }
            /// @}

            getUi().showAnswerUi(false);
            // mCallId will hold the state of the call. We don't clear the mCall variable here as
            // it may be useful for sending text messages after phone disconnects.
            mCallId = Call.INVALID_CALL_ID;
        }
    }

    public void onAnswer() {
        if (mCallId == Call.INVALID_CALL_ID) {
            return;
        }

        Log.d(this, "onAnswer " + mCallId);

        CallCommandClient.getInstance().answerCall(mCallId);
    }

    public void onDecline() {
        Log.d(this, "onDecline " + mCallId);

        CallCommandClient.getInstance().rejectCall(mCall, false, null);
    }

    public void onText() {
        if (getUi() != null) {
            getUi().showMessageDialog();
            /** M: */
            CallCommandClient.getInstance().silenceRinger();
        }
    }

    public void rejectCallWithMessage(String message) {
        Log.d(this, "sendTextToDefaultActivity()...");

        CallCommandClient.getInstance().rejectCall(mCall, true, message);

        onDismissDialog();
    }

    public void onDismissDialog() {
        InCallPresenter.getInstance().onDismissDialog();
    }

    interface AnswerUi extends Ui {
        public void showAnswerUi(boolean show);
        public void showTextButton(boolean show);
        public void showMessageDialog();
        public void configureMessageDialog(ArrayList<String> textResponses);
        /// M: Add to update reject call message. @{
        public void updatePromptsMessage(boolean show);
        /// @}
        
        /// M: For VT and UI @{
        void updateIncomingCallMenuButton();
        /// @}
    }

    //--------------------------------------------MediaTek-----------------------------------------------------
    /// M: Add to update reject call message. @{
    private String mRejectCallNotifyMsg = null;
    /// @}

    @Override
    public void onStorageFull() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpdateRecordState(int state, int customValue) {
        // TODO Auto-generated method stub
    }
    
    public boolean isVTCall() {
        boolean isVT = false;
        if (mCall != null) {
            isVT = mCall.isVideoCall();
        }
        Log.d(this, "isVTCall()... mCall: " + mCall + " / " + isVT);
        return isVT;
    }

    /// M: restart the ringer.
    public void restartRinger() {
        Call call = CallList.getInstance().getIncomingCall();
        if (call != null && call.isIncoming()) {
            CallCommandClient.getInstance().restartRinger();
        }
    }

    /// M: stop the ringer.
    public void silenceRinger() {
        CallCommandClient.getInstance().silenceRinger();
    }

    public Call getIncomingCall() {
        return mCall;
    }

    /// M: for VoLTE Conference Call @{
    public boolean isUpdatingVoLteConfCall() {
        boolean isVoLte = false;
        if (mCall != null) {
            isVoLte = mCall.isVoLteConferenceCall();
        }
        return isVoLte;
    }
    /// @}
}
