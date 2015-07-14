package com.android.phone;

import java.util.SortedSet;

import android.R.integer;
import android.os.AsyncResult;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.phone.CallModeler.CallResult;
import com.android.services.telephony.common.Call;
import com.mediatek.phone.Utils;

/**
 * Separate a user defined call from conference call.
 * The conference call must in active state.
 * Insert the call id to command like this: "Separate 1", means separate the first
 * connected call from the conference call. "Separate 2", means separate the second
 * connected call from the conference call.
 */

public class SeparateCommand extends BlockingCommand {

    CallModeler mModeler;
    Integer[] mChildCallIds;
    Connection mSeperateConnection;

    @Override
    protected int beforeExecute() {
        super.beforeExecute();
        mModeler = PhoneGlobals.getInstance().getCallModeler();
        CallManager cm = PhoneGlobals.getInstance().mCM;
        // Can't separate when have both active and hold call.
        final boolean hasActiveCall = cm.hasActiveFgCall();
        final boolean hasHoldingCall = cm.hasActiveBgCall();
        boolean canSeperate = !(hasActiveCall && hasHoldingCall);
        log("beforeExecute canSeperate " + canSeperate);
        if (canSeperate) {
            for (Call call : mModeler.getFullList()) {
                // The conference call must in active state.
                if (call.isConferenceCall() && call.getState() == Call.State.ACTIVE) {
                    mChildCallIds = call.getChildCallIds().toArray(new Integer[0]);
                    log("beforeExecute, child size: " + mChildCallIds.length);
                    return ICommand.RESULT_OK;
                }
            }
        }
        return ICommand.RESULT_ABORT;
    };

    @Override
    protected int executeInner(String content) {
        // get the call id from the content.
        final String[] args = content.split(" ");
        int callId = mChildCallIds[Integer.parseInt(args[0])];
        // get the call by call id.
        CallResult result = mModeler.getCallWithId(callId);
        // separate it.
        mSeperateConnection = result.getConnection();
        log("executeInner callId: " + callId + " Seperate Connection: " + mSeperateConnection);
        if (result != null) {
            if (Call.State.CONFERENCED == result.getCall().getState()) {
                try {
                    mSeperateConnection.separate();
                } catch (CallStateException e) {
                    e.printStackTrace();
                    return ICommand.RESULT_FAIL;
                }
            }
        }
        return super.executeInner(content);
    }

    @Override
    public void onPhoneStateChanged(AsyncResult r) {
        CallManager cm = PhoneGlobals.getInstance().mCM;
        log("onPhoneStateChanged hasActiveFg: " + cm.hasActiveFgCall() + " hasActiveBg: "
                + cm.hasActiveBgCall() + " Active Connection size: "
                + cm.getActiveFgCall().getConnections().size() + " Active  Connection "
                + cm.getActiveFgCall().getConnections().get(0));

        // If separate successful, the active connection is mSeperateConnection.
        if (cm.hasActiveFgCall() && cm.hasActiveBgCall()
                && cm.getActiveFgCall().getConnections().size() == 1
                && cm.getActiveFgCall().getConnections().get(0) == mSeperateConnection) {
            notify(ICommand.RESULT_OK);
        }
    }
}
