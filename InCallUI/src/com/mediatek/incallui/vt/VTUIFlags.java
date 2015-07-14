package com.mediatek.incallui.vt;

import com.android.incallui.Log;

import android.graphics.Bitmap;

import com.google.common.base.Objects;


/**
 * Class to record VT UI flags, which will affect UI's display.
 *
 */
public class VTUIFlags {

    public static final String LOG_TAG = "VTUIFlags";
    private static VTUIFlags sInstance = new VTUIFlags();

    public enum VTLocalIconState {
        NONE, ZOOM, BRIGHTNESS, CONTRAST
    }

    public boolean mVTHideMeNow;        // indicate local video is shown or not. true - picture, false - video.

    public boolean mVTSurfaceChangedL;  // indicate whether the small surfaceView is ready.
    public boolean mVTSurfaceChangedH;  // indicate whether the bigger surfaceView is ready.

    public boolean mVTHasReceiveFirstFrame;     // indicate whether we have receive "VT_MSG_RECEIVE_FIRSTFRAME" message from VTManager.
    public VTLocalIconState mVTLocalIconState;  // indicate whether the Zoom / Bright / Contrast icon is shown or not.

    public VTConnectionStarttime mVTConnectionStarttime;    // indicate the VT call's start time(the time we receive "VT_MSG_START_COUNTER" message).
    public boolean mVTInSnapshot;               // indicate whether we are doing VTManager.savePeerPhoto().
    public boolean mVTInSwitchCamera;           // indicate whether we are doing VTManager.switchCamera().

    public boolean mVTPeerBigger;       // indicate whether the peer video is shown on bigger surfaceView

    private VTUIFlags() {
        mVTConnectionStarttime = new VTConnectionStarttime();
    }

    public static VTUIFlags getInstance() {
        return sInstance;
    }

    /**
     * when we receive VTSettingParams, we think a new VT Call is set up. so reset VT-related flags first.
     * and do some init-operation for VTUIFlags based on VTSettingParams.
     * @param vtSettingLocal
     */
    public void initVTUIFlags(VTSettingLocal vtSettingLocal) {
        Log.d(this, "initVTUIFlags()...");
        reset();
        mVTPeerBigger = vtSettingLocal.mPeerBigger;
        if (vtSettingLocal.mVTIsMT) {
            if (!"0".equals(vtSettingLocal.mShowLocalMT)) {
                mVTHideMeNow = true;
            }
        } else {
            if (!vtSettingLocal.mShowLocalMO) {
                mVTHideMeNow = true;
            }
        }
    }

    public void reset() {
        mVTHideMeNow = false;
        mVTPeerBigger = true;

        mVTSurfaceChangedL = false;
        mVTSurfaceChangedH = false;

        mVTHasReceiveFirstFrame = false;
        mVTLocalIconState = VTLocalIconState.NONE;
        mVTInSnapshot = false;
        mVTInSwitchCamera = false;

        if (mVTConnectionStarttime != null) {
            mVTConnectionStarttime.reset();
        }
    }

    public void dumpVTUIFlags() {
        Log.d(this, "*****dumpVTUIFlags Begin*****");
        Log.d(this, "*****" + this.toString());
        Log.d(this, "*****dumpVTUIFlags END*****");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mVTHideMeNow", mVTHideMeNow)
                .add("mVTPeerBigger", mVTPeerBigger)
                .add("mVTSurfaceChangedL", mVTSurfaceChangedL)
                .add("mVTSurfaceChangedH", mVTSurfaceChangedH)
                .add("mVTHasReceiveFirstFrame", mVTHasReceiveFirstFrame)
                .add("mVTInLocalAdjust", mVTLocalIconState)
                .add("mVTInSnapshot", mVTInSnapshot)
                .add("mVTInSwitchCamera", mVTInSwitchCamera)
                .toString();
    }

    public class VTConnectionStarttime {
//      public Connection mConnection;
      public long mStarttime;

      /**
       * Constructor function
       */
      public VTConnectionStarttime() {
          reset();
      }

      /**
       * reset start time
       */
      public void reset() {
          Log.d("VTConnectionStarttime", "reset...");
//          mConnection = null;
          mStarttime = -1;
      }
  }
}
