package com.mediatek.incallui.vt;

import android.graphics.Bitmap;

import com.android.incallui.Log;
import com.mediatek.services.telephony.common.VTSettingParams;
import com.google.common.base.Objects;

/**
 * Class to record VT setting parameters from TeleService.
 * All of those values are just record of VT Setting in TeleService, we do not change them at all.
 *
 */
public class VTSettingLocal {

    public static final String LOG_TAG = "VTSettingLocal";
    private static VTSettingLocal sInstance = new VTSettingLocal();

    // Flag comes from VTSettingUtils @{
    public String mPicToReplaceLocal;       // seems no need
    public String mPicToReplacePeer;        // seems no need
    public String mShowLocalMT;             // setting of show local video when MT.
    public boolean mEnableBackCamera;       // flag to indicate whether back camera is valid
    public boolean mPeerBigger;             // flag to decide whether peer video is bigger than local video.
    public boolean mShowLocalMO;            // flag to decide whether show local video when VT MO.
    public boolean mAutoDropBack;           // flag to decide whether auto drop back when VT MO failed.
    public boolean mToReplacePeer;          // flag to decide whether replace peer video or not when peer video is invalid.
    public boolean mVTIsMT;                 // flag to indicate whether the video call is MT or not
    public Bitmap mVTReplacePeerBitmap;     // bitmap to replace peer video if needs.

    public static VTSettingLocal getInstance() {
        return sInstance;
    }

    /**
     * get VT Setting parameters from TeleService, and record them here.
     * @param params
     * @param bitmap
     */
    public void getVTSettingParams(VTSettingParams params, Bitmap bitmap) {
        Log.d(LOG_TAG, "getVTSettingParams()... params: " + params.toString());
        mPicToReplaceLocal = params.mPicToReplaceLocal;
        mPicToReplacePeer = params.mPicToReplacePeer;
        mShowLocalMT = params.mShowLocalMT;
        mEnableBackCamera = params.mEnableBackCamera;
        mPeerBigger = params.mPeerBigger;
        mShowLocalMO = params.mShowLocalMO;
        mAutoDropBack = params.mAutoDropBack;
        mToReplacePeer = params.mToReplacePeer;
        mVTIsMT = params.mVTIsMT;
        mVTReplacePeerBitmap = bitmap;
    }

    public void reset() {
        mPicToReplaceLocal = "0";
        mPicToReplacePeer = "0";
        mShowLocalMT = "0";
        mEnableBackCamera = true;
        mPeerBigger = true;
        mShowLocalMO = true;
        mAutoDropBack = false;
        mToReplacePeer = true;
        if (null != mVTReplacePeerBitmap) {
            mVTReplacePeerBitmap.recycle();
            mVTReplacePeerBitmap = null;
        }
    }

    public void dumpVTSettingParams() {
        Log.d(this, "*****dumpVTSettingParams Begin*****");
        Log.d(this, "*****" + this.toString());
        Log.d(this, "***** mVTReplacePeerBitmap : " + mVTReplacePeerBitmap);
        Log.d(this, "*****dumpVTSettingParams END*****");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mPicToReplaceLocal", mPicToReplaceLocal)
                .add("mShowLocalMT", mShowLocalMT)
                .add("mPicToReplacePeer", mPicToReplacePeer)
                .add("mEnableBackCamera", mEnableBackCamera)
                .add("mPeerBigger", mPeerBigger)
                .add("mShowLocalMO", mShowLocalMO)
                .add("mAutoDropBack", mAutoDropBack)
                .add("mToReplacePeer", mToReplacePeer)
                .add("mVTIsMT", mVTIsMT)
                .toString();
    }
}
