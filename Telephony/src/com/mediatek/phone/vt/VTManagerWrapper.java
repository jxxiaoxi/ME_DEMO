
package com.mediatek.phone.vt;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.android.internal.telephony.PhoneConstants;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.android.phone.R;
import com.mediatek.services.telephony.common.VTManagerParams;
import com.mediatek.phone.recording.PhoneRecorderHandler;
import com.mediatek.settings.VTSettingUtils;
import com.mediatek.vt.VTManager;

public class VTManagerWrapper {
    private final static String TAG = "VTManagerWrapper";
    private final static VTManagerWrapper sInstance = new VTManagerWrapper();

    public static final int VT_RESULT_SWITCHCAMERA_OK = 128;
    public static final int VT_RESULT_SWITCHCAMERA_FAIL = 129;
    public static final int VT_RESULT_PEER_SNAPSHOT_OK = 126;
    public static final int VT_RESULT_PEER_SNAPSHOT_FAIL = 127;
    public static final int VT_RESULT_PEER_SNAPSHOT_FAIL_SDCARD_NULL = 130;
    public static final int VT_RESULT_PEER_SNAPSHOT_FAIL_SDACRD_NOT_ENOUGH = 131;
    private static final int VT_TAKE_PEER_PHOTO_DISK_MIN_SIZE = 1000000;
    private static final int VT_MEDIA_RECORDER_ERROR_UNKNOWN = 1;
    private static final int VT_MEDIA_RECORDER_NO_I_FRAME = 0x7FFF;
    private static final int VT_MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED = 801;
    private static final int VT_MEDIA_OCCUPIED = 1;
    private static final int VT_MEDIA_ERROR_VIDEO_FAIL = 1;

    private VTManager mVTManager = null;

    private boolean mVTSurfaceReady = false;    // record whether the surface in InCallUI is ready.
    private boolean mVTHasMediaRecordError;
    private boolean mVTStatusActive = false;    // record whether we have receive "PHONE_VT_STATUS_INFO" msg
    // used to record template State after we call VTManager.setVTClose()
    // and before VTManager'state becomes to be COLSE. (The "CLOSING" state).
    // In the template state, we should not call any VTService related functions
    // for VTService may be closed at any time. Or NE may raise, like ALPS01527707.
    private boolean mVTIsClosing = false;

    private VTManagerWrapper() {
        mVTManager = VTManager.getInstance();
    }

    public static VTManagerWrapper getInstance() {
        return sInstance;
    }

    /**
     * register listener to VTManager.
     */
    public void registerDefaultVTListener() {
        Log.i(TAG, "- call VTManager.registerDefaultVTListener() begin !");
        mVTManager.registerVTListener(mHandler);
        Log.i(TAG, "- call VTManager.registerDefaultVTListener() end !");
    }

    /**
     * set local surface and peer surface to VTManager, and set value to mVTSurfaceReady.
     * @param local
     * @param peer
     */
    public void setDisplay(Surface local, Surface peer) {
        mVTSurfaceReady = (local != null && peer != null);

        Log.i(TAG, "- call VTManager.setDisplay() begin with surfaces : " + local + ", " + peer);
        mVTManager.setDisplay(local, peer);
        Log.i(TAG, "- call VTManager.setDisplay() end !");
    }

    /**
     * This function call VTManager.setVTVisible() to set surfaces (comes from setDisplay()) to native,
     * which will case VTMAL to connect to the surfaces.
     * Note: setVTVisible(false) and setVTVisible(true) should be as a pair.
     *       when surfaces changed, want VTMAL connect to the changed(new) surfaces,
     *       should call VTManager.setVTVisible(false) first to clear old surface in VTMAL,
     *       then VTManager.setDisplay() to set surfaces to VTManager,
     *       then VTManager.setVTVisible(true) to set surfaces from VTManager to VTMAL.
     *       ==> VTManager.setVTVisible(false) -> VTManager.setDisplay() -> VTManager.setVTVisible(true).
     * @param isVisible
     */
    public void setVTVisible(boolean isVisible) {
        if (isVTClosingOrClosed() || VTCallUtils.isVTIdle()) {
            log("setVTVisible() error --> called when VTManager is CLOSE or VT Call is IDLE.");
            return;
        }
        Log.i(TAG, "setVTVisible : " + isVisible + ", mVTSurfaceReady=" + mVTSurfaceReady);
        if (isVisible) {
            if (mVTSurfaceReady) {
                log("- call VTManager.setVTVisible(true) begin ! ");
                mVTManager.setVTVisible(isVisible);
                log("- call VTManager.setVTVisible(true) end ! ");
            }
        } else {
            log("- call VTManager.setVTVisible(false) begin ! ");
            mVTManager.setVTVisible(isVisible);
            log("- call VTManager.setVTVisible(false) end ! ");
        }
    }

    /**
     * setVTOpen(), should be called under CLOSE state.
     * Because VTManager handle setVTOPen(), setVTReady() and setVTCLose() in one handler,
     * so can make ensure they will be executed in right order.
     * @param context
     * @param slotId
     */
    public void setVTOpen(Context context, int slotId) {
        Log.i(TAG, "setVTOpen()... VTManager State: " + mVTManager.getState() + ", slotId=" + slotId);
        if (mVTManager.getState() == VTManager.State.CLOSE) {
            log("- call VTManager.setVTOpen() begin ! ");
            mVTManager.setVTOpen(context, slotId);
            log("- call VTManager.setVTOpen() end ! ");
        }
    }

    /**
     * setVTReady(), should be called with OPEN state and surface ready.
     */
    public void setVTReady() {
        Log.i(TAG, "setVTReady()... VTManager State: " + mVTManager.getState() + ", mVTSurfaceReady=" + mVTSurfaceReady);
        if (mVTManager.getState() == VTManager.State.OPEN && mVTSurfaceReady) {
            // For ALPS01234020 open speaker for VT;
            PhoneUtils.setSpeakerForVT(true);
            log("- call VTManager.setVTReady() begin ! ");
            mVTManager.setVTReady();
            log("- call VTManager.setVTReady() end ! ");
        }
    }

    /**
     * handle "PHONE_VT_STATUS_INFO" msg, we only care status with 0, which means the connection is set up and ready to transfer video data
     * Check VT Call status and call setVTConnected()
     * @see PHONE_VT_STATUS_INFO
     * @param asyncResult VT call status: 0 is active, 1 is disconnected
     */
    public void handleVTStatusInfo(AsyncResult asyncResult, PhoneConstants.State state) {
        log("handleVTStatusInfo()... ");
        boolean isDisconent = false;
        if (null != asyncResult) {
            final int result = ((int[]) asyncResult.result)[0];
            log("handleVTStatusInfo()... result=" + result);
            isDisconent = (result != 0);
        }

        mVTStatusActive = !isDisconent;

        if (state == PhoneConstants.State.IDLE) {
            log("handleVTStatusInfo()... Phone state is IDLE, just return! ");
            return;
        }

        VTManagerWrapper.getInstance().setVTConnected();
    }

    /**
     * setVTConnected(), should be called under READY state and receive "PHONE_VT_STATUS_INFO" message with status = 0.
     * Check CallManager PHONE_VT_STATUS_INFO Message (mVTStatusActive) and VT Status
     * (VTManager.State.READY), if all ready, call VTManager.setVTConnected()
     */
    public void setVTConnected() {
        Log.i(TAG, "setVTConnected()... VTManager State: " + mVTManager.getState() + ", mVTStatusActive: " + mVTStatusActive);
        if (mVTManager.getState() == VTManager.State.READY && mVTStatusActive) {
            log("- call VTManager.setVTConnected() begin ! ");
            mVTManager.setVTConnected();
            log("- call VTManager.setVTConnected() end ! ");
        }
    }

    /**
     * call this function just before setVTClose().
     * when receive disconnect message of correct VT call will trigger this,
     * and we use this to reset mVTStatusActive flag, instead of "PHONE_VT_STATUS_INFO" message with status = 1.
     */
    public void onDisconnected() {
        Log.i(TAG, "setVTConnected()... VTManager State: " + mVTManager.getState());
        log("- call VTManager.onDisconnected() begin ! ");
        mVTManager.onDisconnected();
        log("- call VTManager.onDisconnected() end ! ");
        mVTStatusActive  = false;
    }

    /**
     * setVTClose(), can be called under any non-CLOSE state to end correct VT flow,
     * and also set mVTIsClosing flag, which will be reset after we receive MSG_VT_CLOSE.
     */
    public void setVTClose() {
        Log.i(TAG, "setVTClose()... VTManager State: " + mVTManager.getState());
        if (mVTManager.getState() != VTManager.State.CLOSE) {
            log("- call VTManager.setVTClose() begin ! ");
            mVTIsClosing = true;
            mVTManager.setVTClose();
            log("- call VTManager.setVTClose() end ! ");
        }
    }

    /**
     * the handler runs in Main-thread, functions called in this handler all runs in Main-thread;
     * But other functions called from CallCommandService runs in another thread (Binder thread);
     * So there exist unsync, maybe we should run those VTManager-related functions just in one thread.
     * see ALPS01447218.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            log("VTManagerWrapper handleMessage message:" + msg);

            switch (msg.what) {

            case VTManager.VT_MSG_OPEN:
                log("- handler : VT_MSG_OPEN ! ");
                VTSettingUtils.getInstance().pushVTSettingParams();
                VTCallUtils.setLocalViewToVTManager();
                VTCallUtils.setPeerViewToVTManager();
                setVTReady();
                onVTStateChanged(msg.what);
                pushVTManagerParams();
                break;

            case VTManager.VT_MSG_READY:
                log("- handler : VT_MSG_READY ! ");
                setVTConnected();
                onVTStateChanged(msg.what);
                pushVTManagerParams();
                break;

            case VTManager.VT_MSG_CONNECTED:
                log("- handler : VT_MSG_CONNECTED ! ");
                PhoneUtils.setAudioMode();
                onVTStateChanged(msg.what);
                pushVTManagerParams();
                break;

            case VTManager.VT_MSG_DISCONNECTED:
                log("- handler : VT_MSG_DISCONNECTED ! ");
                onVTStateChanged(msg.what);
                break;

            case VTManager.VT_MSG_CLOSE:
                log("- handler : VT_MSG_CLOSE ! ");
                mVTIsClosing = false;
                onVTStateChanged(msg.what);
                break;

            case VTManager.VT_MSG_RECEIVE_FIRSTFRAME:
                log("- handler : VT_MSG_RECEIVE_FIRSTFRAME ! ");
                onVTStateChanged(msg.what);
                break;

            case VTManager.VT_MSG_START_COUNTER:
                log("- handler : VT_MSG_START_COUNTER ! ");
                PhoneGlobals.getInstance().notifier.onReceiveVTManagerStartCounter();
                /// For VT time count. see ALPS01425992 @{
                // To call onVTStateChanged to notify InCallUI start count.
                onVTStateChanged(msg.what);
                /// @}
                break;

            case VTManager.VT_MSG_EM_INDICATION:
                log("- handler : VT_MSG_EM_INDICATION ! ");
                showToast((String) msg.obj);
                break;

            case VTManager.VT_ERROR_CALL_DISCONNECT:
                log("- handler : VT_ERROR_CALL_DISCONNECT ! ");
                PhoneUtils.hangupAllCalls();
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_error_network));
                break;

            case VTManager.VT_NORMAL_END_SESSION_COMMAND:
                log("- handler : VT_NORMAL_END_SESSION_COMMAND ! ");
                PhoneUtils.hangupAllCalls();
                break;

            case VTManager.VT_ERROR_START_VTS_FAIL:
                log("- handler : VT_ERROR_START_VTS_FAIL ! ");
                PhoneUtils.hangupAllCalls();
                if (VT_MEDIA_ERROR_VIDEO_FAIL == msg.arg2) {
                    showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_media_video_fail));
                } else {
                    showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_error_media));
                }
                break;

            case VTManager.VT_ERROR_CAMERA:
                log("- handler : VT_ERROR_CAMERA ! ");
                PhoneUtils.hangupAllCalls(true, null);
                if (VT_MEDIA_OCCUPIED == msg.arg2) {
                    showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_media_occupied));
                } else {
                    showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_error_media));
                }
                break;

            case VTManager.VT_ERROR_MEDIA_SERVER_DIED:
                log("- handler : VT_ERROR_MEDIA_SERVER_DIED ! ");
                PhoneUtils.hangupAllCalls();
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_error_media));
                break;

            case VTManager.VT_ERROR_MEDIA_RECORDER_EVENT_INFO:
                log("- handler : VT_ERROR_MEDIA_RECORDER_EVENT_INFO ! ");
                if (VT_MEDIA_RECORDER_NO_I_FRAME == msg.arg1) {
                    showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_recorder_only_voice));
                } else if (VT_MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == msg.arg1) {
                    PhoneRecorderHandler.getInstance().stopRecording();
                    if (PhoneRecorderHandler.getInstance().getListener() != null) {
                        PhoneRecorderHandler.getInstance().getListener().onStorageFull(); // false for recording case
                    }
                }
                break;

            case VTManager.VT_ERROR_MEDIA_RECORDER_EVENT_ERROR:
                log("- handler : VT_ERROR_MEDIA_RECORDER_EVENT_ERROR ! ");
                if (VT_MEDIA_RECORDER_ERROR_UNKNOWN == msg.arg1 && !mVTHasMediaRecordError) {
                    showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_recording_error));
                    /// M: For ALPS00568488 @{
                    // We need a flag to indicate if any error happen when recording.
                    // If recording error happen mark it.
                    mVTHasMediaRecordError = true;
                    /// @}
                    PhoneRecorderHandler.getInstance().stopRecording();
                }
                break;

            case VTManager.VT_ERROR_MEDIA_RECORDER_COMPLETE:
                log("- handler : VT_ERROR_MEDIA_RECORDER_COMPLETE ! ");
                int ok = 0;
                if (ok == msg.arg1 && !mVTHasMediaRecordError) {
                    log("- handler : VT_ERROR_MEDIA_RECORDER_COMPLETE, arg is OK ");
                    showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_recording_saved));
                }

                /// M: For ALPS00568488 @{
                // We need a flag to indicate if any error happen when recording.
                // When recording complete, reset it.
                mVTHasMediaRecordError = false;
                /// @}
                break;

            case VTManager.VT_MSG_PEER_CAMERA_OPEN:
                log("- handler : VT_MSG_PEER_CAMERA_OPEN ! ");
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_peer_camera_open));
                break;

            case VTManager.VT_MSG_PEER_CAMERA_CLOSE:
                log("- handler : VT_MSG_PEER_CAMERA_CLOSE ! ");
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_peer_camera_close));
                break;

            case VT_RESULT_PEER_SNAPSHOT_OK:
                log("- handler : VT_RESULT_PEER_SNAPSHOT_OK ! ");
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_pic_saved_to_sd));
                break;

            case VT_RESULT_PEER_SNAPSHOT_FAIL:
                log("- handler : VT_RESULT_PEER_SNAPSHOT_FAIL ! ");
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_pic_saved_to_sd_fail));
                break;

            case VT_RESULT_PEER_SNAPSHOT_FAIL_SDCARD_NULL:
                log("- handler : VT_RESULT_PEER_SNAPSHOT_FAIL_SDCARD_NULL ! ");
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_sd_null));
                break;

            case VT_RESULT_PEER_SNAPSHOT_FAIL_SDACRD_NOT_ENOUGH:
                log("- handler : VT_RESULT_PEER_SNAPSHOT_FAIL_SDACRD_NOT_ENOUGH ! ");
                showToast(PhoneGlobals.getInstance().getResources().getString(R.string.vt_sd_not_enough));
                break;

            default:
                Log.wtf(TAG, "mHandler: unexpected message: " + msg);
                break;
            }
        }
    };

    /**
     * Listener to notify to InCallUI
     *
     */
    public interface Listener {
        void onVTStateChanged(int msgVT);
        void pushVTManagerParams(VTManagerParams params);
    }

    private Listener mListener;
    private VTManagerParams mVTManagerParams = new VTManagerParams();

    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * push Camera's parameters to InCallUI, which will be used to show icon and menu.
     * Because we don't want to enqueue them when CallHandlerService is not connected in CallHandlerServiceProxy,
     * so we call it when MSG_VT_OPEN / MSG_VT_READY / MSG_VT_CONNECT.
     */
    private void pushVTManagerParams() {
        updateVTManagerParams();
        if (mListener == null) {
            log("pushVTManagerParams()... mListener is null, need check !");
        } else {
            mListener.pushVTManagerParams(mVTManagerParams);
        }
    }

    private void updateVTManagerParams() {
        // For ALPS01383116 & ALPS01435927 & ALPS01439919,
        // we can't call VTManager's methods when VTService is down;
        // But here we can't not use !VTCallUtils.isVTCallActive(),
        // for VT-Dialing, should show menu "switch camera", which is triggered by this function
        if (isVTClosingOrClosed() || VTCallUtils.isVTIdle()) {
            log("updateVTManagerParams() error --> called when VTManager is CLOSE or VT Call is IDLE.");
            return;
        }
        log("updateVTManagerParams()...");
        mVTManagerParams.mCameraSensorCount = mVTManager.getCameraSensorCount();
        mVTManagerParams.mVideoQuality = mVTManager.getVideoQuality();
        mVTManagerParams.mCanDecBrightness = mVTManager.canDecBrightness();
        mVTManagerParams.mCanIncBrightness = mVTManager.canIncBrightness();
        mVTManagerParams.mCanDecZoom = mVTManager.canDecZoom();
        mVTManagerParams.mCanIncZoom = mVTManager.canIncZoom();
        mVTManagerParams.mCanDecContrast = mVTManager.canDecContrast();
        mVTManagerParams.mCanIncContrast = mVTManager.canIncContrast();
        mVTManagerParams.mIsSupportNightMode = mVTManager.isSupportNightMode();
        mVTManagerParams.mIsNightModeOn = mVTManager.getNightMode();
        mVTManagerParams.mColorEffect = mVTManager.getColorEffect();
        mVTManagerParams.mSupportedColorEffects = mVTManager.getSupportedColorEffects();
    }

    private void onVTStateChanged(int msgVT) {
        if (mListener == null) {
            log("onVTStateChanged()... mListener is null, need check !");
        } else {
            mListener.onVTStateChanged(msgVT);
        }
    }

    private void showToast(String string) {
        Toast.makeText(PhoneGlobals.getInstance(), string, Toast.LENGTH_LONG).show();
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    public void switchCamera() {
        if (isVTClosingOrClosed()) {
            log("switchCamera() error --> called when VTManager is CLOSE .");
            onVTStateChanged(VT_RESULT_SWITCHCAMERA_FAIL);
            return;
        }
        // because switch camera may spend 2-4 second
        // new a thread to finish it so that it cannot block UI update
        (new Thread() {
            public void run() {
                log("- call VTManager.switchCamera() begin !");
                boolean result = mVTManager.switchCamera();
                log("- call VTManager.switchCamera() end ! result = " + result);
                if (result) {
                    // VTManager will show toast to notify user if switch fail,
                    // so here we just notify UI the operation is done.
                     onVTStateChanged(VT_RESULT_SWITCHCAMERA_OK);
                } else {
                     onVTStateChanged(VT_RESULT_SWITCHCAMERA_FAIL);
                }
                pushVTManagerParams();
            }
        }).start();
    }

    public void savePeerPhoto() {
        if (!PhoneUtils.isExternalStorageMounted()) {
            log("savePeerPhoto() failed, SD card is full.");
            mHandler.sendMessage(Message.obtain(mHandler, VT_RESULT_PEER_SNAPSHOT_FAIL_SDCARD_NULL));
            // here we need notify UI the operation is done, can do this operation again.
            onVTStateChanged(VT_RESULT_PEER_SNAPSHOT_FAIL);
            return;
        }

        if (!PhoneUtils.diskSpaceAvailable(VT_TAKE_PEER_PHOTO_DISK_MIN_SIZE)) {
            log("savePeerPhoto() failed, SD card space is not enough.");
            mHandler.sendMessage(Message.obtain(mHandler, VT_RESULT_PEER_SNAPSHOT_FAIL_SDACRD_NOT_ENOUGH));
            // here we need notify UI the operation is done, can do this operation again.
            onVTStateChanged(VT_RESULT_PEER_SNAPSHOT_FAIL);
            return;
        }

        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("savePeerPhoto() error --> called when VTManager is CLOSE or no active VT call exist.");
            onVTStateChanged(VT_RESULT_PEER_SNAPSHOT_FAIL);
            return;
        }

        (new Thread() {
            public void run() {
                log("- call VTManager.savePeerPhoto() begin !");
                boolean result = mVTManager.savePeerPhoto();
                log("- call VTManager.savePeerPhoto() end ! result = " + result);
                if (result) {
                    mHandler.sendMessage(Message.obtain(mHandler, VT_RESULT_PEER_SNAPSHOT_OK));
                    // here we need notify UI the operation is done, can do this operation again.
                    onVTStateChanged(VT_RESULT_PEER_SNAPSHOT_OK);
                } else {
                    mHandler.sendMessage(Message.obtain(mHandler, VT_RESULT_PEER_SNAPSHOT_FAIL));
                    onVTStateChanged(VT_RESULT_PEER_SNAPSHOT_FAIL);
                }
            }
        }).start();
    }

    public void hideLocal(boolean on) {
        if (isVTClosingOrClosed()) {
            log("hideLocal() error --> called when VTManager is CLOSE.");
            return;
        }
        log("hideLocal()...on: " + on);
        if (on) {
            VTCallUtils.updatePicToReplaceLocalVideo();
        } else {
            setLocalView(0, "");
        }
    }

    public void setNightMode(boolean isOnNight) {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("setNightMode() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.setNightMode() begin with isOnNight: " + isOnNight);
        mVTManager.setNightMode(isOnNight);
        log("- call VTManager.setNightMode() end !");
        pushVTManagerParams();
    }

    public void setVideoQuality(int quality) {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("setVideoQuality() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.setVideoQuality() begin with quality: " + quality);
        mVTManager.setVideoQuality(quality);
        log("- call VTManager.setVideoQuality() end !");
        pushVTManagerParams();
    }

    public void setColorEffect(String colorEffect) {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("setColorEffect() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.setColorEffect() begin with colorEffect: " + colorEffect);
        mVTManager.setColorEffect(colorEffect);
        log("- call VTManager.setColorEffect() end !");
        pushVTManagerParams();
    }

    public void decZoom() {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("decZoom() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.decZoom() begin !");
        mVTManager.decZoom();
        log("- call VTManager.decZoom() end !");
        pushVTManagerParams();
    }

    public void incZoom() {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("incZoom() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.incZoom() begin !");
        mVTManager.incZoom();
        log("- call VTManager.incZoom() end !");
        pushVTManagerParams();
    }

    public void incBrightness() {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("incBrightness() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.incBrightness() begin !");
        mVTManager.incBrightness();
        log("- call VTManager.incBrightness() end !");
        pushVTManagerParams();
    }

    public void decBrightness() {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("decBrightness() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.decBrightness() begin !");
        mVTManager.decBrightness();
        log("- call VTManager.decBrightness() end !");
        pushVTManagerParams();
    }

    public void incContrast() {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("incContrast() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.incContrast() begin !");
        mVTManager.incContrast();
        log("- call VTManager.incContrast() end !");
        pushVTManagerParams();
    }

    public void decContrast() {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("decContrast() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.decContrast() begin !");
        mVTManager.decContrast();
        log("- call VTManager.decContrast() end !");
        pushVTManagerParams();
    }

    public void onUserInput(String input) {
        if (isVTClosingOrClosed()) {
            log("onUserInput() error --> called when VTManager is CLOSE.");
            return;
        }
        log("- call VTManager.onUserInput() begin with input: " + input);
        mVTManager.onUserInput(input);
        log("- call VTManager.onUserInput() end !");
    }

    public void incomingVTCall(int flag) {
        log("- call VTManager.incomingVTCall() begin with flag: " + flag);
        mVTManager.incomingVTCall(flag);
        log("- call VTManager.incomingVTCall() end !");
    }

    public void startRecording(int type, long maxSize) {
        if (isVTClosingOrClosed() || !VTCallUtils.isVTCallActive()) {
            log("startRecording() error --> called when VTManager is CLOSE or no active VT call exist.");
            return;
        }
        log("- call VTManager.startRecording() begin with type / maxSize: " + type + " / " + maxSize);
        mVTManager.startRecording(type, maxSize);
        log("- call VTManager.startRecording() end !");
    }

    public void stopRecording() {
        if (isVTClosingOrClosed()) {
            log("stopRecording() error --> called when VTManager is CLOSE.");
            return;
        }
        log("- call VTManager.stopRecording() begin !");
        mVTManager.stopRecording();
        log("- call VTManager.stopRecording() end !");
    }

    public VTManager.State getState() {
        return mVTManager.getState();
    }

    /**
     * maybe it actually means slot id.
     * @return
     */
    public int getSimId() {
        return mVTManager.getSimId();
    }

    public void setPeerView(int bEnableReplacePeerVideo,
            String sReplacePeerVideoPicturePath) {
        log("- call VTManager.setPeerView() begin with bEnableReplacePeerVideo / sReplacePeerVideoPicturePath:"
                + bEnableReplacePeerVideo + " / " + sReplacePeerVideoPicturePath);
        mVTManager.setPeerView(bEnableReplacePeerVideo, sReplacePeerVideoPicturePath);
        log("- call VTManager.setPeerView() end !");
    }

    public void setLocalView(int videoType, String path) {
        log("- call VTManager.setLocalView() begin with videoType / path : " + videoType + " / " + path);
        mVTManager.setLocalView(videoType, path);
        log("- call VTManager.setLocalView() end !");
    }

    public void lockPeerVideo() {
        if (isVTClosingOrClosed()) {
            log("lockPeerVideo() error --> called when VTManager is CLOSE.");
            return;
        }
        log("- call VTManager.lockPeerVideo() begin !");
        mVTManager.lockPeerVideo();
        log("- call VTManager.lockPeerVideo() end !");
    }

    public void unlockPeerVideo() {
        if (isVTClosingOrClosed()) {
            log("unlockPeerVideo() error --> called when VTManager is CLOSE.");
            return;
        }
        log("- call VTManager.unlockPeerVideo() begin !");
        mVTManager.unlockPeerVideo();
        log("- call VTManager.unlockPeerVideo() end !");
    }

    public boolean isVTClosingOrClosed() {
        boolean result = false;
        if (getState() == VTManager.State.CLOSE || mVTIsClosing) {
            result = true;
        }
        return result;
    }
}
