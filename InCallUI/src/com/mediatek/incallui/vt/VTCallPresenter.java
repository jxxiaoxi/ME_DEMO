
package com.mediatek.incallui.vt;

import android.view.SurfaceHolder;

import com.android.incallui.AudioModeProvider;
import com.android.incallui.AudioModeProvider.AudioModeListener;
import com.android.incallui.CallCommandClient;
import com.android.incallui.CallList;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.incallui.InCallPresenter.IncomingCallListener;
import com.android.incallui.Log;
import com.android.incallui.Presenter;
import com.android.incallui.Ui;
import com.android.services.telephony.common.Call;
import com.mediatek.incallui.InCallUtils;
import com.mediatek.incallui.ext.ExtensionManager;
import com.mediatek.incallui.vt.VTManagerLocal.State;
import com.mediatek.incallui.vt.VTManagerLocal.VTListener;
import com.mediatek.incallui.vt.VTUtils.VTScreenMode;

public class VTCallPresenter extends Presenter<VTCallPresenter.VTCallUi>
        implements InCallStateListener, IncomingCallListener, VTListener {

    private Call mCall;
    private boolean mIsUIReady = false;

    public interface VTCallUi extends Ui {

        void showVTCallUI(boolean show);

        void onVTStateChanged(int msg);
    }

    public void onUiReady(VTCallUi ui) {
        super.onUiReady(ui);

        // Register for call state changes last
        InCallPresenter.getInstance().addListener(this);
        InCallPresenter.getInstance().addIncomingCallListener(this);

        // For VT
        VTManagerLocal.getInstance().addVTListener(this);
        mIsUIReady = true;
        onStateChange(InCallPresenter.getInstance().getInCallState(), CallList.getInstance());
    }

    @Override
    public void onUiUnready(VTCallUi ui) {
        super.onUiUnready(ui);

        // stop getting call state changes
        InCallPresenter.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);

        // For VT
        VTManagerLocal.getInstance().removeVTListener(this);
        mIsUIReady = false;
    }

    public void init() {
        onStateChange(InCallPresenter.getInstance().getInCallState(), CallList.getInstance());
    }

    public void onStateChange(InCallState state, CallList callList) {
        Log.d(this, "onStateChange()... state: " + state);
        mCall = VTUtils.getVTCall();
        // calculate whether we should show VT UI based on current condition
        VTScreenMode vtsScreenMode = VTUtils.getVTScreenMode();
        switch (vtsScreenMode) {
            case VT_SCREEN_OPEN:
                Log.d(this, "VT_SCREEN_OPEN mode, show VT UI.");
                if (getUi() != null) {
                    getUi().showVTCallUI(true);
                }
                break;

            case VT_SCREEN_CLOSE:
                Log.d(this, "VT_SCREEN_CLOSE mode, hide VT UI.");
                if (getUi() != null) {
                    getUi().showVTCallUI(false);
                }
                break;

            case VT_SCREEN_WAITING:
                Log.d(this, "VT_SCREEN_WAITING mode, keep previous mode and do nothing.");
                break;

            default:
                break;
        }
    }

    @Override
    public void onIncomingCall(InCallState state, Call call) {
        mCall = call;
        onStateChange(state, CallList.getInstance());
    }

    void startVtRecording(int type, long maxSize) {
        CallCommandClient.getInstance().startVtRecording(type, maxSize);
    }

    void startVoiceRecording() {
        CallCommandClient.getInstance().startVoiceRecording();
    }

    void stopRecording() {
        CallCommandClient.getInstance().stopRecording();
    }

    public boolean isIncomingCall() {
        boolean isMT = false;
        if (mCall != null) {
            isMT = InCallUtils.isIncomingCall(mCall);
        } else {
            Log.i(this, "mCall is null");
        }
        return isMT;
    }

    @Override
    public void onVTStateChanged(int msgVT) {

        Log.d(this, "onVTStateChanged()... msgVT: " + msgVT);
        // Here we only handle UI related message, other logic will be done in VTManagerLocal.java.
        final VTCallUi ui = getUi();
        if (ui == null) {
            Log.d(this, "UI is not ready when onVTStateChanged(), just return.");
            return;
        }

        getUi().onVTStateChanged(msgVT);
    }
}
