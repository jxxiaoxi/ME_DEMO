/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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

package com.mediatek.incallui.vt;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.incallui.BaseFragment;
import com.android.incallui.CallCommandClient;
import com.android.incallui.CallList;
import com.android.incallui.InCallActivity;
import com.android.incallui.InCallPresenter;
import com.android.incallui.Log;
import com.android.incallui.R;
import com.android.services.telephony.common.Call;
import com.mediatek.incallui.InCallUtils;
import com.mediatek.incallui.ext.ExtensionManager;
import com.mediatek.incallui.recorder.PhoneRecorderUtils;
import com.mediatek.incallui.recorder.PhoneRecorderUtils.RecorderState;
import com.mediatek.incallui.vt.VTCallPresenter.VTCallUi;
import com.mediatek.incallui.vt.VTManagerLocal.State;
import com.mediatek.incallui.vt.VTUIFlags.VTLocalIconState;

public class VTCallFragment extends BaseFragment<VTCallPresenter, VTCallPresenter.VTCallUi>
        implements VTCallPresenter.VTCallUi, SurfaceHolder.Callback, View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {

    private static final int SECOND_TO_MILLISECOND = 1000;
    // messages and time to dismiss icons of mVTHighUp / mVTHighDown / mVTLowUp / mVTLowDown.
    private static final int DELAY_HIDE_VT_ICON_MESSAGE = 101;
    private static final int DELAY_HIDE_VT_ICON_TIME = 10 * SECOND_TO_MILLISECOND;
    // messages and time to dismiss mVTMTAskDialog.
    private static final int DELAY_DISMISS_VT_ASK_DIALOG_MESSAGE = 102;
    private static final int DELAY_DISMISS_VT_ASK_DIALOG_TIME = 5 * SECOND_TO_MILLISECOND;

    public VTCallPresenter createPresenter() {
        return new VTCallPresenter();
    }

    public VTCallPresenter.VTCallUi getUi() {
        return this;
    }

    private static final boolean DBG = true;
    private static final boolean VDBG = true;

    private VTSurfaceView mVTHighVideo;
    private VTSurfaceView mVTLowVideo;
    private SurfaceHolder mLowVideoHolder;
    private SurfaceHolder mHighVideoHolder;

    private AlertDialog mInCallVideoSettingDialog;
    private AlertDialog mInCallVideoSettingLocalEffectDialog;
    private AlertDialog mInCallVideoSettingLocalNightmodeDialog;
    private AlertDialog mInCallVideoSettingPeerQualityDialog;
    private AlertDialog mVTMTAskDialog;
    private AlertDialog mVTRecorderSelector;

    // icons to adjust local video (Zoom / brightness / contrast)
    private ImageButton mVTHighUp;
    private ImageButton mVTHighDown;
    private ImageButton mVTLowUp;
    private ImageButton mVTLowDown;

    ArrayList<String> mVTRecorderEntries;

    private Context mContext;

    // when VT UI is shown, acquire a wake lock to keep screen on.
    private PowerManager.WakeLock mVTWakeLock;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        PowerManager pw = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mVTWakeLock = pw.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                                  PowerManager.ON_AFTER_RELEASE,
                                                  "VTWakeLock");
        getPresenter().init();

        /// M: get the status bar and navigation bar height @{
        mStatusBarHeight = getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);
        mNavigationBarHeight = getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.navigation_bar_height);
        final Point point = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);
        mScreenHeight = point.y;
        mScreenWidth = point.x;
        /// @}
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.mtk_vt_incall_screen, container, false);
    }

    @Override
    public void onDestroyView() {
        Log.d(this, "onDestroyView");
        // remove un-executed messages when UI destroy. see ALPS01526300
        if (mHandler != null) {
            if (mHandler.hasMessages(DELAY_HIDE_VT_ICON_MESSAGE)) {
                mHandler.removeMessages(DELAY_HIDE_VT_ICON_MESSAGE);
            }
            if (mHandler.hasMessages(DELAY_DISMISS_VT_ASK_DIALOG_MESSAGE)) {
                mHandler.removeMessages(DELAY_DISMISS_VT_ASK_DIALOG_MESSAGE);
            }
        }
        super.onDestroyView();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log("onViewCreated()...");

        mVTHighVideo = (VTSurfaceView) view.findViewById(R.id.VTHighVideo);
        mVTHighVideo.setFocusable(false);
        mVTHighVideo.setFocusableInTouchMode(false);

        mVTLowVideo = (VTSurfaceView) view.findViewById(R.id.VTLowVideo);
        mVTLowVideo.setFocusable(false);
        mVTLowVideo.setFocusableInTouchMode(false);

        mVTHighVideo.setOnClickListener(this);
        mVTLowVideo.setOnClickListener(this);

        mHighVideoHolder = mVTHighVideo.getHolder();
        mLowVideoHolder = mVTLowVideo.getHolder();

        mHighVideoHolder.addCallback(this);
        mLowVideoHolder.addCallback(this);


        mVTLowVideo.setZOrderMediaOverlay(true);

        mVTHighUp = (ImageButton) view.findViewById(R.id.VTHighUp);
        mVTHighUp.setBackgroundColor(0);
        mVTHighUp.setOnClickListener(this);

        mVTHighDown = (ImageButton) view.findViewById(R.id.VTHighDown);
        mVTHighDown.setBackgroundColor(0);
        mVTHighDown.setOnClickListener(this);

        mVTLowUp = (ImageButton) view.findViewById(R.id.VTLowUp);
        mVTLowUp.setBackgroundColor(0);
        mVTLowUp.setOnClickListener(this);

        mVTLowDown = (ImageButton) view.findViewById(R.id.VTLowDown);
        mVTLowDown.setBackgroundColor(0);
        mVTLowDown.setOnClickListener(this);

        hideLocalZoomOrBrightness(true);

        // mVTLowVideo.setZOrderOnTop(true);
//        initVTSurface();

        // For ALPS01298431 @{
        // For MO, dialVTCallSuccess() may be called before onViewCreate(),
        //         so update operation in dialVTCallSuccess() may be not executed. So we need do it here.
        // For MT, answerVTCallPre() is called actually after onViewCreate(),
        //         so we do it in answerVTCallPre() to make sure it will called after pushVTSettingParams.
        if (VTUtils.isVTOutgoing()) {
            updatePeerVideoBkgDrawable();
        }
        /// @}

        // If there has no VT call, make the VT UI as GONE as default.
        if (VTUtils.isVTIdle()) {
            getView().setVisibility(View.GONE);
        }

        /// For ALPS01494898 @{
        ///InCallUI may be killed before surfaceDestoryed be called. So, need to reset here.
        CallCommandClient.getInstance().setDisplay(null, null);
        /// @}

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mtk_vt_incall_menu, menu);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder == mHighVideoHolder) {
            log("surfaceChanged : HighVideo");
            VTUIFlags.getInstance().mVTSurfaceChangedH = true;
        }

        if (holder == mLowVideoHolder) {
            log("surfaceChanged : LowVideo");
            VTUIFlags.getInstance().mVTSurfaceChangedL = true;
        }
        log("surfaceChanged : " + holder.toString() + ", w=" + width + ", height=" + height);

        if (okToSetDisplay()) {
            onSurfaceReady(true);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (DBG) {
            log("surfaceCreated : " + holder.toString());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (DBG) {
            log("surfaceDestroyed : " + holder.toString());
        }

        if (holder == mHighVideoHolder) {
            if (DBG) {
                log("surfaceDestroyed : HighVideo," +
                        " set mVTSurfaceChangedH = false");
            }
            VTUIFlags.getInstance().mVTSurfaceChangedH = false;
        }

        if (holder == mLowVideoHolder) {
            if (DBG) {
                log("surfaceDestroyed : LowVideo," +
                        " set mVTSurfaceChangedL = false");
            }
            VTUIFlags.getInstance().mVTSurfaceChangedL = false;
        }

        if ((!VTUIFlags.getInstance().mVTSurfaceChangedH)
                && (!VTUIFlags.getInstance().mVTSurfaceChangedL)) {
            onSurfaceReady(false);
        }
    }

    /**
     * check whether can we call VTManager.setDisplay() and VTMangaer.setVTVisible(),
     * this function must be called after we receive VT setting params and surface ready.
     * @return
     */
    private boolean okToSetDisplay() {
        boolean result = false;
        if (VTUIFlags.getInstance().mVTSurfaceChangedH
                && VTUIFlags.getInstance().mVTSurfaceChangedL
                && VTManagerLocal.getInstance().getState() != State.CLOSE) {
            result = true;
        }
        return result;
    }

    /**
     * wrapper function for VTManager.setDisplay() and VTMangaer.setVTVisible().
     * @param ready
     */
    private void onSurfaceReady(boolean ready) {
        log("onSurfaceReady()... ready: " + ready);
        if (ready) {
            updateVTLocalPeerDisplay();
            CallCommandClient.getInstance().setVTVisible(true);
            CallCommandClient.getInstance().setVTReady();
        } else {
            CallCommandClient.getInstance().setDisplay(null, null);
            CallCommandClient.getInstance().setVTVisible(false);
        }
    }

    private void onVTInCallVideoSettingLocalEffect() {
        if (DBG) {
            log("onVTInCallVideoSettingLocalEffect() ! ");
        }
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(getActivity());
        myBuilder.setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingLocalEffectDialog != null) {
                    mInCallVideoSettingLocalEffectDialog.dismiss();
                    mInCallVideoSettingLocalEffectDialog = null;
                }
            }
        });

        List<String> supportEntryValues = VTManagerLocal.getInstance().getSupportedColorEffects();

        if (supportEntryValues == null || supportEntryValues.size() <= 0) {
            return;
        }

        CharSequence[] entryValues = getResources().getStringArray(
                R.array.vt_incall_setting_local_video_effect_values);
        CharSequence[] entries = getResources().getStringArray(
                R.array.vt_incall_setting_local_video_effect_entries);
        ArrayList<CharSequence> entryValues2 = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entries2 = new ArrayList<CharSequence>();

        for (int i = 0, len = entryValues.length; i < len; i++) {
            if (supportEntryValues.indexOf(entryValues[i].toString()) >= 0) {
                entryValues2.add(entryValues[i]);
                entries2.add(entries[i]);
            }
        }

        if (DBG) {
            log("onVTInCallVideoSettingLocalEffect() : entryValues2.size() - "
                    + entryValues2.size());
        }
        int currentValue = entryValues2.indexOf(VTManagerLocal.getInstance().getColorEffect());

        InCallVideoSettingLocalEffectListener myClickListener
                = new InCallVideoSettingLocalEffectListener();
        myClickListener.setValues(entryValues2);
        myBuilder.setSingleChoiceItems(entries2.toArray(
                            new CharSequence[entryValues2.size()]),
                            currentValue, myClickListener);
        myBuilder.setTitle(R.string.vt_local_video_effect);
        mInCallVideoSettingLocalEffectDialog = myBuilder.create();
        mInCallVideoSettingLocalEffectDialog.show();
    }

    class InCallVideoSettingLocalEffectListener implements DialogInterface.OnClickListener {
        private ArrayList<CharSequence> mValues = new ArrayList<CharSequence>();

        /**
         * set values
         * @param values    values
         */
        public void setValues(ArrayList<CharSequence> values) {
            for (int i = 0; i < values.size(); i++) {
                mValues.add(values.get(i));
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            if (mInCallVideoSettingLocalEffectDialog != null) {
                mInCallVideoSettingLocalEffectDialog.dismiss();
                mInCallVideoSettingLocalEffectDialog = null;
            }
            CallCommandClient.getInstance().setColorEffect(mValues.get(which).toString());
            updateLocalZoomOrBrightness();
        }
    }

    private void onVTInCallVideoSettingLocalNightMode() {
        if (DBG) {
            log("onVTInCallVideoSettingLocalNightMode() ! ");
        }

        AlertDialog.Builder myBuilder = new AlertDialog.Builder(getActivity());
        myBuilder.setNegativeButton(getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingLocalNightmodeDialog != null) {
                    mInCallVideoSettingLocalNightmodeDialog.dismiss();
                    mInCallVideoSettingLocalNightmodeDialog = null;
                }
            }
        });
        myBuilder.setTitle(R.string.vt_local_video_nightmode);

        DialogInterface.OnClickListener myClickListener
                = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingLocalNightmodeDialog != null) {
                    mInCallVideoSettingLocalNightmodeDialog.dismiss();
                    mInCallVideoSettingLocalNightmodeDialog = null;
                }
                if (0 == which) {
                    if (DBG) {
                        log("onVTInCallVideoSettingLocalNightMode() :" +
                                " CallCommandClient.getInstance().setNightMode(true);");
                    }
                    CallCommandClient.getInstance().setNightMode(true);
//                    updateLocalZoomOrBrightness();
                } else if (1 == which) {
                    if (DBG) {
                        log("onVTInCallVideoSettingLocalNightMode() :"
                                + " CallCommandClient.getInstance().setNightMode(false);");
                    }
                    CallCommandClient.getInstance().setNightMode(false);
//                    updateLocalZoomOrBrightness();
                }
            }
        };

        if (VTManagerLocal.getInstance().isSupportNightMode()) {
            if (VTManagerLocal.getInstance().getNightMode()) {
                myBuilder.setSingleChoiceItems(
                                R.array.vt_incall_video_setting_local_nightmode_entries, 0,
                                myClickListener);
            } else {
                myBuilder.setSingleChoiceItems(
                                R.array.vt_incall_video_setting_local_nightmode_entries, 1,
                                myClickListener);
            }
        } else {
            myBuilder.setSingleChoiceItems(
                    R.array.vt_incall_video_setting_local_nightmode_entries2, 0,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (mInCallVideoSettingLocalNightmodeDialog != null) {
                                mInCallVideoSettingLocalNightmodeDialog.dismiss();
                                mInCallVideoSettingLocalNightmodeDialog = null;
                            }
                        }
                    });
        }

        mInCallVideoSettingLocalNightmodeDialog = myBuilder.create();
        mInCallVideoSettingLocalNightmodeDialog.show();
    }

    private void onVTInCallVideoSettingPeerQuality() {
        if (DBG) {
            log("onVTInCallVideoSettingPeerQuality() ! ");
        }
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(getActivity());
        myBuilder.setNegativeButton(getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingPeerQualityDialog != null) {
                    mInCallVideoSettingPeerQualityDialog.dismiss();
                    mInCallVideoSettingPeerQualityDialog = null;
                }
            }
        });
        myBuilder.setTitle(R.string.vt_peer_video_quality);

        DialogInterface.OnClickListener myClickListener
                = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingPeerQualityDialog != null) {
                    mInCallVideoSettingPeerQualityDialog.dismiss();
                    mInCallVideoSettingPeerQualityDialog = null;
                }
                if (0 == which) {
                    if (DBG) {
                        log("onVTInCallVideoSettingPeerQuality() :" +
                                " CallCommandClient.getInstance()" +
                                ".setVideoQuality( CallCommandClient.VT_VQ_NORMAL );");
                    }
                    CallCommandClient.getInstance().setVideoQuality(VTManagerLocal.VT_VQ_NORMAL);
                } else if (1 == which) {
                    if (DBG) {
                        log("onVTInCallVideoSettingPeerQuality() :" +
                                " CallCommandClient.getInstance().setVideoQuality" +
                                "( CallCommandClient.VT_VQ_SHARP );");
                    }
                    CallCommandClient.getInstance().setVideoQuality(VTManagerLocal.VT_VQ_SHARP);
                }
            }
        };

        if (VTManagerLocal.VT_VQ_NORMAL == VTManagerLocal.getInstance().getVideoQuality()) {
            myBuilder.setSingleChoiceItems(
                    R.array.vt_incall_video_setting_peer_quality_entries, 0,
                    myClickListener);
        } else if ( VTManagerLocal.VT_VQ_SHARP == VTManagerLocal.getInstance().getVideoQuality()) {
            myBuilder.setSingleChoiceItems(
                    R.array.vt_incall_video_setting_peer_quality_entries, 1,
                    myClickListener);
        } else {
            if (DBG) {
                log("CallCommandClient.getInstance().getVideoQuality()" +
                        " is not VT_VQ_SHARP" +
                        " or VT_VQ_NORMAL , error ! ");
            }
        }

        mInCallVideoSettingPeerQualityDialog = myBuilder.create();
        mInCallVideoSettingPeerQualityDialog.show();
    }


    public void dismissVTDialogs() {
        if (DBG) {
            log("dismissVTDialogs() ! ");
        }
        if (mInCallVideoSettingDialog != null) {
            mInCallVideoSettingDialog.dismiss();
            mInCallVideoSettingDialog = null;
        }
        if (mInCallVideoSettingLocalEffectDialog != null) {
            mInCallVideoSettingLocalEffectDialog.dismiss();
            mInCallVideoSettingLocalEffectDialog = null;
        }
        if (mInCallVideoSettingLocalNightmodeDialog != null) {
            mInCallVideoSettingLocalNightmodeDialog.dismiss();
            mInCallVideoSettingLocalNightmodeDialog = null;
        }
        if (mInCallVideoSettingPeerQualityDialog != null) {
            mInCallVideoSettingPeerQualityDialog.dismiss();
            mInCallVideoSettingPeerQualityDialog = null;
        }
        if (mVTMTAskDialog != null) {
            mVTMTAskDialog.dismiss();
            mVTMTAskDialog = null;
        }
        if (mVTRecorderSelector != null) {
            mVTRecorderSelector.dismiss();
            mVTRecorderSelector = null;
        }
    }

    public void onVTReceiveFirstFrame() {
        if (DBG) {
            log("onVTReceiveFirstFrame()...  mVTPeerBigger: " + VTUIFlags.getInstance().mVTPeerBigger);
        }
        if (VTUIFlags.getInstance().mVTPeerBigger) {
            if (mVTHighVideo != null) {
                if (mVTHighVideo.getBackground() != null) {
                    mVTHighVideo.setBackgroundDrawable(null);
                }
            }
        } else {
            if (mVTLowVideo != null) {
                if (mVTLowVideo.getBackground() != null) {
                    mVTLowVideo.setBackgroundDrawable(null);
                }
            }
        }
    }

    public void onVTReady() {
        /// DM lock Feature @{
        if (InCallUtils.isDMLocked()) {
            if (DBG) {
                log("Now DM locked, just return");
            }
            return;
        }

        if (VTManagerLocal.getInstance().getState() != State.READY && VTManagerLocal.getInstance().getState() != State.CONNECTED) {
            log("VT is in CLOSE or OPEN state, just return.");
            return;
        }
        
        if (mVTMTAskDialog != null && mVTMTAskDialog.isShowing()) {
            log("mVTMTAskDialog is already shown, just return.");
            return;
        }
        
        CallCommandClient.getInstance().unlockPeerVideo();
        if (DBG) {
            log("Now DM not locked," + " VTManager.getInstance().unlockPeerVideo();");
        }
        /// @}
        if (getPresenter() != null && getPresenter().isIncomingCall()
                && "1".equals(VTSettingLocal.getInstance().mShowLocalMT)) {
            if (DBG) {
                log("- VTSettingUtils.getInstance().mShowLocalMT : 1 !");
            }
            mVTMTAskDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getResources().getString(R.string.vt_ask_show_local))
                    .setPositiveButton(getResources().getString(R.string.vt_ask_show_local_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DBG) {
                                        log(" user select yes !! ");
                                    }

                                    if (mVTMTAskDialog != null) {
                                        mVTMTAskDialog.dismiss();
                                        mVTMTAskDialog = null;
                                    }
                                    onVTHideMeClick();
                                    return;
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.vt_ask_show_local_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DBG) {
                                        log(" user select no !! ");
                                    }

                                    if (mVTMTAskDialog != null) {
                                        mVTMTAskDialog.dismiss();
                                        mVTMTAskDialog = null;
                                    }
                                    CallCommandClient.getInstance().updatePicToReplaceLocalVideo();
                                    return;
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface arg0) {
                            if (DBG) {
                                log(" user no selection , default show !! ");
                            }

                            if (mVTMTAskDialog != null) {
                                mVTMTAskDialog.dismiss();
                                mVTMTAskDialog = null;
                            }
                            onVTHideMeClick();
                            return;
                        }
                    }).create();
            mVTMTAskDialog.show();

            mHandler.sendMessageDelayed(
                    Message.obtain(mHandler, DELAY_DISMISS_VT_ASK_DIALOG_MESSAGE),
                    DELAY_DISMISS_VT_ASK_DIALOG_TIME);
        }
    }

    public void onVTClose() {
        if (DBG) {
            log("onVTClose()...");
        }
        dismissVTDialogs();
        hideLocalZoomOrBrightness(true);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (VDBG) {
            log("onClick(View " + view + ", id " + id + ")...");
        }
        switch (id) {
        case R.id.VTHighUp:
        case R.id.VTLowUp:
            adjustLocalVT(true);
            break;
        case R.id.VTHighDown:
        case R.id.VTLowDown:
            adjustLocalVT(false);
            break;
        default:
            break;
        }
        
    }

    private void showGenericErrorDialog(int resid, boolean isStartupError) {
        log("showGenericErrorDialog ");
//        mInCallScreen.showGenericErrorDialog(resid, isStartupError);
    }

    public void onStop() {
        Log.d(this, "onStop");
        super.onStop();
        /// M: for ALPS01288272 @{
        // If VT screen is not visible, release the wake lock.
        releaseVtWakeLock();
        /// @}
    }

    @Override
    public void onStart() {
        Log.d(this, "onStart");
        super.onStart();
        /// M: for ALPS01288272 @{
        // If VT screen is visible, acquire the wake lock.
        acquireVtWakeLock();
        /// @}
    }

    private void updateVTLocalPeerDisplay() {
        log("updateVTLocalPeerDisplay()...mVTPeerBigger: " + VTUIFlags.getInstance().mVTPeerBigger);
        if (VTUIFlags.getInstance().mVTPeerBigger) {
            CallCommandClient.getInstance().setDisplay(mLowVideoHolder.getSurface(), mHighVideoHolder.getSurface());
        } else {
            CallCommandClient.getInstance().setDisplay(mHighVideoHolder.getSurface(), mLowVideoHolder.getSurface());
        }
    }

    private void dismissVideoSettingDialogs() {
        if (mInCallVideoSettingDialog != null) {
            mInCallVideoSettingDialog.dismiss();
            mInCallVideoSettingDialog = null;
        }
        if (mInCallVideoSettingLocalEffectDialog != null) {
            mInCallVideoSettingLocalEffectDialog.dismiss();
            mInCallVideoSettingLocalEffectDialog = null;
        }
        if (mInCallVideoSettingLocalNightmodeDialog != null) {
            mInCallVideoSettingLocalNightmodeDialog.dismiss();
            mInCallVideoSettingLocalNightmodeDialog = null;
        }
        if (mInCallVideoSettingPeerQualityDialog != null) {
            mInCallVideoSettingPeerQualityDialog.dismiss();
            mInCallVideoSettingPeerQualityDialog = null;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    public void handleVTMenuClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.menu_switch_camera:
             onVTSwitchCameraClick();
             break;
        case R.id.menu_take_peer_photo:
             onVTTakePeerPhotoClick();
             break;
        case R.id.menu_hide_local_video:
             onVTHideMeClick();
             break;
        case R.id.menu_swap_videos:
             onVTSwapVideoClick();
             break;
        case R.id.menu_vt_record:
             onVoiceVideoRecordClick(menuItem);
             break;
        case R.id.menu_video_setting:
             onVTShowSettingClick();
             break;
        default:
            Log.d(this, "This is not VT menu item.");
        }
    }

    public void onVTSwitchCameraClick() {
        if (VTManagerLocal.getInstance().getState() != VTManagerLocal.State.READY
                && VTManagerLocal.getInstance().getState() != VTManagerLocal.State.CONNECTED) {
            Log.d(this, "onVTSwitchCameraClick: failed, state should be READY or CONNECTED.");
            return;
        }

        Log.i(this, "onVTSwitchCameraClick() / mVTInSwitchCamera: " + VTUIFlags.getInstance().mVTInSwitchCamera);
        if (VTUIFlags.getInstance().mVTInSwitchCamera) {
            Log.i(this, "VTManager is handling switchcamera now, so returns this time.");
            return;
        }

        // switch camera, when telephony complete this action, will push a message to us, then we will set this flag to false.
        VTUIFlags.getInstance().mVTInSwitchCamera = true;
        CallCommandClient.getInstance().switchCamera();
//        updateVTScreen(getVTScreenMode());

        hideLocalZoomOrBrightness(true);
    }

    public void onVTTakePeerPhotoClick() {
        Log.d(this, "onVTTakePeerPhotoClick()...");

        if (!VTUIFlags.getInstance().mVTHasReceiveFirstFrame
                || VTManagerLocal.getInstance().getState() != VTManagerLocal.State.CONNECTED) {
            Log.d(this, "onVTTakePeerPhotoClick: failed, peer video is unvisiable now.");
            return;
        }

        if (VTUIFlags.getInstance().mVTInSnapshot) {
            Log.d(this, "onVTTakePeerPhotoClick: failed, VTManager is handling snapshot now.");
            return;
        }

        VTUIFlags.getInstance().mVTInSnapshot = true;
        CallCommandClient.getInstance().savePeerPhoto();
    }

    public void onVTHideMeClick() {
        Log.d(this, "onVTHideMeClick()...");

        if (VTManagerLocal.getInstance().getState() != VTManagerLocal.State.READY
                && VTManagerLocal.getInstance().getState() != VTManagerLocal.State.CONNECTED) {
            Log.d(this, "onVTHideMeClick: failed, state should be READY or CONNECTED.");
            return;
        }

        VTUIFlags.getInstance().mVTHideMeNow = !VTUIFlags.getInstance().mVTHideMeNow;
        CallCommandClient.getInstance().hideLocal(VTUIFlags.getInstance().mVTHideMeNow);
        hideLocalZoomOrBrightness(true);
    }

    public void onVTSwapVideoClick() {
        if (!VTUIFlags.getInstance().mVTHasReceiveFirstFrame
                || VTManagerLocal.getInstance().getState() != VTManagerLocal.State.CONNECTED) {
            Log.d(this, "onVTSwapVideoClick: failed, peer video is unvisiable now.");
            return;
        }

        // this variable will only be set when the button is clicked and sync from VTSetting.
        if (VTUIFlags.getInstance().mVTLocalIconState != VTLocalIconState.NONE) {
            hideLocalZoomOrBrightness(false);
        }

        VTUIFlags.getInstance().mVTPeerBigger = !VTUIFlags.getInstance().mVTPeerBigger;

        CallCommandClient.getInstance().setVTVisible(false);
        updateVTLocalPeerDisplay();
        CallCommandClient.getInstance().setVTVisible(true);

        if (VTUIFlags.getInstance().mVTLocalIconState != VTLocalIconState.NONE) {
            showVTLocalIcons(VTUIFlags.getInstance().mVTLocalIconState);
        }
    }

    public void onVTShowSettingClick() {
        log("onVTInCallVideoSetting() ! ");

        if (VTManagerLocal.getInstance().getState() != VTManagerLocal.State.CONNECTED) {
            Log.d(this, "onVTShowSettingClick: failed, state should be CONNECTED.");
            return;
        }

        DialogInterface.OnClickListener myClickListener
                = new DialogInterface.OnClickListener() {

            private static final int DIALOG_ITEM_THREE = 3;
            private static final int DIALOG_ITEM_FOUR = 4;

            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingDialog != null) {
                    mInCallVideoSettingDialog.dismiss();
                    mInCallVideoSettingDialog = null;
                }
                if (0 == which) {
                    if (DBG) {
                        log("onVTInCallVideoSetting() : select - 0 ");
                    }
                    if (!VTManagerLocal.getInstance().canDecZoom()
                            && !VTManagerLocal.getInstance().canIncZoom()) {
                        showToast(getResources().getString(R.string.vt_cannot_support_setting));
                    } else {
                        showVTLocalIcons(VTLocalIconState.ZOOM);
                    }
                } else if (1 == which) {
                    if (DBG) {
                        log("onVTInCallVideoSetting() : select - 1 ");
                    }
                    if (!VTManagerLocal.getInstance().canDecBrightness()
                            && !VTManagerLocal.getInstance().canIncBrightness()) {
                        showToast(getResources().getString(R.string.vt_cannot_support_setting));
                    } else {
                        showVTLocalIcons(VTLocalIconState.BRIGHTNESS);
                    }
                } else if (2 == which) {
                    if (DBG) {
                        log("onVTInCallVideoSetting() : select - 2 ");
                    }
                    if (!VTManagerLocal.getInstance().canDecContrast()
                            && !VTManagerLocal.getInstance().canIncContrast()) {
                        showToast(getResources().getString(R.string.vt_cannot_support_setting));
                    } else {
                        showVTLocalIcons(VTLocalIconState.CONTRAST);
                    }
                } else if (DIALOG_ITEM_THREE == which) {
                    if (DBG) {
                        log("onVTInCallVideoSetting() : select - 3 ");
                    }
                    onVTInCallVideoSettingLocalEffect();
                } else if (DIALOG_ITEM_FOUR == which) {
                    if (DBG) {
                        log("onVTInCallVideoSetting() : select - 4 ");
                    }
                    onVTInCallVideoSettingLocalNightMode();
                } else {
                    if (DBG) {
                        log("onVTInCallVideoSetting() : select - 5 ");
                    }
                    onVTInCallVideoSettingPeerQuality();
                }
            }
        };

        AlertDialog.Builder myBuilder = new AlertDialog.Builder(getActivity());
        myBuilder.setNegativeButton(R.string.custom_message_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mInCallVideoSettingDialog != null) {
                    mInCallVideoSettingDialog.dismiss();
                    mInCallVideoSettingDialog = null;
                }
            }
        });

        if (!VTUIFlags.getInstance().mVTHideMeNow) {
            myBuilder.setItems(R.array.vt_incall_video_setting_entries, myClickListener)
                     .setTitle(R.string.vt_settings);
        } else {
            myBuilder.setItems(R.array.vt_incall_video_setting_entries2,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (mInCallVideoSettingDialog != null) {
                                mInCallVideoSettingDialog.dismiss();
                                mInCallVideoSettingDialog = null;
                            }
                            onVTInCallVideoSettingPeerQuality();
                        }
                    }).setTitle(R.string.vt_settings);
        }
        mInCallVideoSettingDialog = myBuilder.create();
        mInCallVideoSettingDialog.show();
    
    }

    private void adjustLocalVT(boolean inc) {
        VTLocalIconState state = VTUIFlags.getInstance().mVTLocalIconState;
        if (VDBG) {
            log("onClick: adjustLocalVT()...state / inc: " + state + " / " + inc);
        }
        switch (state) {
            case NONE:
                log("adjustLocalVT() triggered with NONE state, need check!");
                break;
            case ZOOM:
                if (inc) {
                    CallCommandClient.getInstance().incZoom();
                } else {
                    CallCommandClient.getInstance().decZoom();
                }
                break;
            case BRIGHTNESS:
                if (inc) {
                    CallCommandClient.getInstance().incBrightness();
                } else {
                    CallCommandClient.getInstance().decBrightness();
                }
                break;
            case CONTRAST:
                if (inc) {
                    CallCommandClient.getInstance().incContrast();
                } else {
                    CallCommandClient.getInstance().decContrast();
                }
                break;
            default:
                log("no such state: " + state + " , Need Check !!!");
                break;
        }
        delayHideVTIcon();
    }

    private void showVTLocalIcons(VTLocalIconState state) {
        if (DBG) {
            log("showVTLocalIcons()...state: " + state);
        }

        // only when VTManager is under REDAY / CONNECT, can we do "Zoom" / "Brightness" / "Contrast" operations.
        if (VTManagerLocal.getInstance().getState() != VTManagerLocal.State.READY
                && VTManagerLocal.getInstance().getState() != VTManagerLocal.State.CONNECTED) {
            log("VTManager is not in READY or CONNECTED state, just return.");
            return;
        }

        int imageResourceUp = 0;
        int imageResourceDown = 0;
        boolean enableUp = false;
        boolean enableDown = false;
        switch (state) {
            case NONE:
                log("adjustLocalVT() triggered with NONE state, need check!");
                break;
            case ZOOM:
                imageResourceUp = R.drawable.mtk_vt_incall_button_zoomup;
                imageResourceDown = R.drawable.mtk_vt_incall_button_zoomdown;
                enableUp = VTManagerLocal.getInstance().canIncZoom();
                enableDown = VTManagerLocal.getInstance().canDecZoom();
                break;
            case BRIGHTNESS:
                imageResourceUp = R.drawable.mtk_vt_incall_button_brightnessup;
                imageResourceDown = R.drawable.mtk_vt_incall_button_brightnessdown;
                enableUp = VTManagerLocal.getInstance().canIncBrightness();
                enableDown = VTManagerLocal.getInstance().canDecBrightness();
                break;
            case CONTRAST:
                imageResourceUp = R.drawable.mtk_vt_incall_button_contrastup;
                imageResourceDown = R.drawable.mtk_vt_incall_button_contrastdown;
                enableUp = VTManagerLocal.getInstance().canIncContrast();
                enableDown = VTManagerLocal.getInstance().canDecContrast();
                break;
            default:
                log("no such state: " + state + " , Need Check !!!");
                break;
        }
        if (VTUIFlags.getInstance().mVTPeerBigger) {
            mVTLowUp.setImageResource(imageResourceUp);
            mVTLowDown.setImageResource(imageResourceDown);
            mVTLowUp.setVisibility(View.VISIBLE);
            mVTLowDown.setVisibility(View.VISIBLE);
            mVTLowUp.setEnabled(enableUp);
            mVTLowDown.setEnabled(enableDown);
        } else {
            mVTHighUp.setImageResource(imageResourceUp);
            mVTHighDown.setImageResource(imageResourceDown);
            mVTHighUp.setVisibility(View.VISIBLE);
            mVTHighDown.setVisibility(View.VISIBLE);
            mVTHighUp.setEnabled(enableUp);
            mVTHighDown.setEnabled(enableDown);
        }
        VTUIFlags.getInstance().mVTLocalIconState = state;
        delayHideVTIcon();
    }

    // when we call this method, we will hide local zoom,brightness and contrast
    private void hideLocalZoomOrBrightness(boolean resetSetting) {
        if (DBG) {
            log("hideLocalZoomOrBrightness()...");
        }

        mVTHighUp.setVisibility(View.GONE);
        mVTHighDown.setVisibility(View.GONE);
        mVTLowUp.setVisibility(View.GONE);
        mVTLowDown.setVisibility(View.GONE);

        if (resetSetting) {
            VTUIFlags.getInstance().mVTLocalIconState = VTLocalIconState.NONE;
        }
    }

    private void updateLocalZoomOrBrightness() {
        if (DBG) {
            log("updateLocalZoomOrBrightness()...");
        }

        boolean enableUp = false;
        boolean enableDown = false;
        switch (VTUIFlags.getInstance().mVTLocalIconState) {
            case NONE:
                log("updateLocalZoomOrBrightness()... not in any operation, set related view gone.");
                hideLocalZoomOrBrightness(true);
                return;
            case ZOOM:
                enableUp = VTManagerLocal.getInstance().canIncZoom();
                enableDown = VTManagerLocal.getInstance().canDecZoom();
                break;
            case BRIGHTNESS:
                enableUp = VTManagerLocal.getInstance().canIncBrightness();
                enableDown = VTManagerLocal.getInstance().canDecBrightness();
                break;
            case CONTRAST:
                enableUp = VTManagerLocal.getInstance().canIncContrast();
                enableDown = VTManagerLocal.getInstance().canDecContrast();
                break;
            default:
                log("no such state: " + VTUIFlags.getInstance().mVTLocalIconState + " , Need Check !!!");
                break;
        }

        if (VTUIFlags.getInstance().mVTPeerBigger) {
            mVTLowUp.setEnabled(enableUp);
            mVTLowDown.setEnabled(enableDown);
        }else {
            mVTHighUp.setEnabled(enableUp);
            mVTHighDown.setEnabled(enableDown);
        }
    }

    private void log(String msg) {
        Log.d(this, msg);
    }

    private void onVoiceVideoRecordClick(MenuItem menuItem) {
        Log.d(this, "onVoiceVideoRecordClick");

        final String title = menuItem.getTitle().toString();
        if (TextUtils.isEmpty(title)) {
            return;
        }

        if (!PhoneRecorderUtils.isExternalStorageMounted(mContext)) {
            Toast.makeText(mContext, getResources().getString(R.string.error_sdcard_access),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!PhoneRecorderUtils.diskSpaceAvailable(PhoneRecorderUtils.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
            handleStorageFull(true); // true for checking case
            return;
        }

        if (title.equals(mContext.getString(R.string.start_record_vt))) {
            if (!InCallPresenter.getInstance().isRecording()) {
                Log.d(this, "startRecord");
                showStartVTRecorderDialog();
            }
        } else if (title.equals(mContext.getString(R.string.stop_record_vt))) {
            Log.d(this, "stopRecord");
            getPresenter().stopRecording();
        }
    }

    private void showStartVTRecorderDialog() {
        Log.d(this, "showStartVTRecorderDialog() ...");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(R.string.custom_message_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mVTRecorderSelector != null) {
                            mVTRecorderSelector.dismiss();
                            mVTRecorderSelector = null;
                        }
                    }
                });
        builder.setTitle(R.string.vt_recorder_start);

        if (mVTRecorderEntries == null) {
            mVTRecorderEntries = new ArrayList<String>();
        } else {
            mVTRecorderEntries.clear();
        }

        mVTRecorderEntries.add(getResources().getString(
                               R.string.vt_recorder_voice_and_peer_video));
        mVTRecorderEntries.add(getResources().getString(
                               R.string.vt_recorder_only_voice));
        mVTRecorderEntries.add(getResources().getString(
                               R.string.vt_recorder_only_peer_video));

        DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mVTRecorderSelector != null) {
                    mVTRecorderSelector.dismiss();
                    mVTRecorderSelector = null;
                }

                String currentString = mVTRecorderEntries.get(which);
                int type = 0;

                if (currentString.equals(getResources().getString(
                        R.string.vt_recorder_voice_and_peer_video))) {
                    if (DBG) {
                        log("The choice of start VT recording : voice and peer video");
                    }
                    type = PhoneRecorderUtils.PHONE_RECORDING_TYPE_VOICE_AND_PEER_VIDEO;
                } else if (currentString.equals(getResources().getString(
                        R.string.vt_recorder_only_voice))) {
                    if (DBG) {
                        log("The choice of start VT recording : only voice");
                    }
                    type = PhoneRecorderUtils.PHONE_RECORDING_TYPE_ONLY_VOICE;
                } else if (currentString.equals(getResources().getString(
                        R.string.vt_recorder_only_peer_video))) {
                    if (DBG) {
                        log("The choice of start VT recording : only peer video");
                    }
                    type = PhoneRecorderUtils.PHONE_RECORDING_TYPE_ONLY_PEER_VIDEO;
                } else {
                    if (DBG) {
                        log("The choice of start VT recording : wrong string");
                    }
                    return;
                }
                startRecord(type);
            }
        };

        builder.setSingleChoiceItems(mVTRecorderEntries
                .toArray(new CharSequence[mVTRecorderEntries.size()]), -1,
                myClickListener);

        mVTRecorderSelector = builder.create();
        mVTRecorderSelector.show();
    }

    private void startRecord(int type) {
        if (DBG) {
            log("startVTRecorder() ...");
        }

        if (VTManagerLocal.getInstance().getState() != VTManagerLocal.State.CONNECTED) {
            Log.d(this, "startRecord: failed, state should be CONNECTED.");
            return;
        }

        long sdMaxSize = PhoneRecorderUtils.getDiskAvailableSize() - PhoneRecorderUtils.PHONE_RECORD_LOW_STORAGE_THRESHOLD;
        if (sdMaxSize > 0) {
            if (PhoneRecorderUtils.PHONE_RECORDING_TYPE_ONLY_VOICE == type) {
                getPresenter().startVoiceRecording();
            } else if (type > 0) {
                getPresenter().startVtRecording(type, sdMaxSize);
            }
        } else if (-1 == sdMaxSize) {
            showToast(getResources().getString(R.string.vt_sd_null));
        } else {
            showToast(getResources().getString(R.string.vt_sd_not_enough));
        }
    }

    private void showToast(String string) {
        Toast.makeText(mContext, string, Toast.LENGTH_LONG).show();
    }

    private void handleStorageFull(final boolean isForCheckingOrRecording) {
        if (PhoneRecorderUtils.getMountedStorageCount(mContext) > 1) {
            // SD card case
            log("handleStorageFull(), mounted storage count > 1");
            if (PhoneRecorderUtils.STORAGE_TYPE_SD_CARD == PhoneRecorderUtils.getDefaultStorageType(
                    mContext)) {
                log("handleStorageFull(), SD card is using");
                showStorageFullDialog(com.mediatek.internal.R.string.storage_sd, true);
            } else if (PhoneRecorderUtils.STORAGE_TYPE_PHONE_STORAGE == PhoneRecorderUtils.getDefaultStorageType(
                    mContext)) {
                log("handleStorageFull(), phone storage is using");
                showStorageFullDialog(com.mediatek.internal.R.string.storage_withsd, true);
            } else {
                // never happen here
                log("handleStorageFull(), never happen here");
            }
        } else if (1 == PhoneRecorderUtils.getMountedStorageCount(mContext)) {
            log("handleStorageFull(), mounted storage count == 1");
            if (PhoneRecorderUtils.STORAGE_TYPE_SD_CARD == PhoneRecorderUtils.getDefaultStorageType(
                    mContext)) {
                log("handleStorageFull(), SD card is using, " + (isForCheckingOrRecording ? "checking case" : "recording case"));
                String toast = isForCheckingOrRecording ? getResources().getString(R.string.vt_sd_not_enough) :
                                                          getResources().getString(R.string.vt_recording_saved_sd_full);
                showToast(toast);
            } else if (PhoneRecorderUtils.STORAGE_TYPE_PHONE_STORAGE == PhoneRecorderUtils.getDefaultStorageType(
                    mContext)) {
                // only Phone storage case
                log("handleStorageFull(), phone storage is using");
                showStorageFullDialog(com.mediatek.internal.R.string.storage_withoutsd, false);
            } else {
                // never happen here
                log("handleStorageFull(), never happen here");
            }
        }
    }

    private AlertDialog mStorageSpaceDialog;

    public void showStorageFullDialog(final int resid, final boolean isSDCardExist) {
        if (DBG) {
            log("showStorageDialog... ");
        }

        if (null != mStorageSpaceDialog) {
            if (mStorageSpaceDialog.isShowing()) {
                return;
            }
        }
        CharSequence msg = getResources().getText(resid);

        // create the clickListener and cancel listener as needed.
        DialogInterface.OnClickListener oKClickListener = null;
        DialogInterface.OnClickListener cancelClickListener = null;
        OnCancelListener cancelListener = new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        };

        if (isSDCardExist) {
            oKClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (DBG) {
                        log("showStorageDialog... , on click, which=" + which);
                    }
                    if (null != mStorageSpaceDialog) {
                        mStorageSpaceDialog.dismiss();
                    }
                    //To Setting Storage
                    Intent intent = new Intent(PhoneRecorderUtils.STORAGE_SETTING_INTENT_NAME);
                    startActivity(intent);
                }
            };
        }

        cancelClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DBG) {
                    log("showStorageDialog... , on click, which=" + which);
                }
                if (null != mStorageSpaceDialog) {
                    mStorageSpaceDialog.dismiss();
                }
            }
        };

        CharSequence cancelButtonText = isSDCardExist ? getResources().getText(R.string.alert_dialog_dismiss) :
                                                        getResources().getText(R.string.ok);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity()).setMessage(msg)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getResources().getText(R.string.reminder))
            .setNegativeButton(cancelButtonText, cancelClickListener)
            .setOnCancelListener(cancelListener);
        if (isSDCardExist) {
            dialogBuilder.setPositiveButton(getResources().getText(R.string.vt_change_my_pic),
                                            oKClickListener);
        }

        mStorageSpaceDialog = dialogBuilder.create();
        mStorageSpaceDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mStorageSpaceDialog.show();
    }

    /**
     * If user choose picture to replace Peer Video in VTCallSetting, VTManager can't handle it before receive first frame,
     * So before receiving first frame, we do this function in app, and remove it when receiving first frame.
     * Note: We only do this just before VTCallFragment actually shown to make sure we have got latest values of VTSetting from TelService.
     * See ALPS01284999, VTCallSettingUtils.pushVTSettingParams() is called between
     * updatePeerVideoBkgDrawable() and onVTReceiveFirstFrame().
     */
    private void updatePeerVideoBkgDrawable() {
        log("updatePeerVideoBkgDrawable()... mVTToReplacePeer / mVTPeerBigger: "
                + VTSettingLocal.getInstance().mToReplacePeer + " / "
                + VTUIFlags.getInstance().mVTPeerBigger);

        if (VTManagerLocal.getInstance().getState() == State.CLOSE) {
            log("VT State is CLOSE, do nothing.");
            return;
        }

        if (VTUIFlags.getInstance().mVTHasReceiveFirstFrame) {
            log("updatePeerVideoBkgDrawable()...Already receive first frame of VT Call, so do nothing here. ");
            return;
        }

        if (VTSettingLocal.getInstance().mToReplacePeer) {
            if (null != VTSettingLocal.getInstance().mVTReplacePeerBitmap) {
                log("updatePeerVideoBkgDrawable(): replace the peer video with drawable.");
                if (VTUIFlags.getInstance().mVTPeerBigger) {
                    mVTHighVideo.setBackgroundDrawable(new BitmapDrawable(VTSettingLocal.getInstance().mVTReplacePeerBitmap));
                } else {
                    mVTLowVideo.setBackgroundDrawable(new BitmapDrawable(VTSettingLocal.getInstance().mVTReplacePeerBitmap));
                }
            } else {
                if (DBG) {
                    log("VTSettingLocal.getInstance().mVTReplacePeerBitmap is null");
                }
            }
        } else {
            log("updatePeerVideoBkgDrawable(): replace the peer video with BLACK color.");
            if (VTUIFlags.getInstance().mVTPeerBigger) {
                mVTHighVideo.setBackgroundColor(Color.BLACK);
            } else {
                mVTLowVideo.setBackgroundColor(Color.BLACK);
            }
        }
    }

    private void acquireVtWakeLock() {
        if (VTUtils.isVTActive() || VTUtils.isVTOutgoing()) {
            if (mVTWakeLock != null && !mVTWakeLock.isHeld()) {
                mVTWakeLock.acquire();
                Log.d(this, "acquire VT wake lock");
            }
        }
    }

    private void releaseVtWakeLock() {
        if (mVTWakeLock != null && mVTWakeLock.isHeld()) {
            mVTWakeLock.release();
            Log.d(this, "release VT wake lock");
        }
    }

    private int mScreenHeight;
    private int mScreenWidth;
    private int mStatusBarHeight;
    private int mNavigationBarHeight;

    /// M: Compute the vt dynamic layout.
    public void amendVtLayout(int callCardBottom) {
        // set vt call screen begain with call card bottom.
        final View view = getView();
        if (view != null) {
            ((MarginLayoutParams) view.getLayoutParams()).topMargin = callCardBottom;
        }

        // interval between mVTHighVideo and mVTLowVideo
        int vtButtonInterval = (int) getResources().getDimension(R.dimen.vt_incall_screen_button_interval);

        int highVideoWidth;     // width of mVTHighVideo
        int highVideoHeight;    // height of mVTHighVideo
        int lowVideoWidth;      // width of mVTLowVideo
        int lowVideoHeight;     // height of mVTLowVideo

        // below calculate size of mVTHighVideo.
        highVideoWidth = mScreenWidth;
        highVideoHeight = (highVideoWidth * 144) / 176;   // the scale of video is fixed as (Width : Height = 176 : 144)

        // below calculate size of mVTLowVideo
        if (InCallUtils.isLandscape(mContext)) {
            int marginTop = 0;
            marginTop = (int) getResources().getDimension(R.dimen.vt_call_low_video_margin_top);
            lowVideoHeight = mScreenHeight -mStatusBarHeight -callCardBottom -marginTop;
        } else {
            lowVideoHeight = mScreenHeight - mStatusBarHeight - callCardBottom - highVideoHeight - vtButtonInterval;
        }
        if (InCallUtils.hasNavigationBar()) {
            lowVideoHeight -= mNavigationBarHeight;
        }
        lowVideoWidth = (lowVideoHeight * 176) / 144;

        // if lowVideoWidth is too big or too small, need re-calculate size of mVTHighVideo and mVTLowVideo
        int lowVideoMaxWidth = (mScreenWidth * 7) / 10;
        int lowVideoMinWidth = (mScreenWidth * 3) / 10;
        int vtCallButtonMarginBottom = 0;

        if (lowVideoWidth > lowVideoMaxWidth) {
            // low video is too big, eg, FHD with smart book plugged.
            lowVideoWidth = lowVideoMaxWidth;
            vtCallButtonMarginBottom = lowVideoHeight;
            lowVideoHeight = (lowVideoWidth * 144) / 176;
            vtCallButtonMarginBottom = vtCallButtonMarginBottom - lowVideoHeight;
            if (vtCallButtonMarginBottom < 0) {
                vtCallButtonMarginBottom = 0;
            }
        } else if (lowVideoWidth < lowVideoMinWidth) {
            // low video is too small, eg, HVGA with NavigationBar(no hasPermanentMenuKey).
            lowVideoWidth = lowVideoMinWidth;
            lowVideoHeight = (lowVideoWidth * 144) / 176;
            highVideoHeight = mScreenHeight - mStatusBarHeight - callCardBottom - lowVideoHeight - vtButtonInterval;
            if (InCallUtils.hasNavigationBar()) {
                highVideoHeight -= mNavigationBarHeight;
            }
            highVideoWidth = (highVideoHeight * 176) / 144;
        }

        // set property of mVTHighVideo and mVTLowVideo
        mVTHighVideo.getLayoutParams().height = highVideoHeight;
        mVTHighVideo.getLayoutParams().width = highVideoWidth;

        mVTLowVideo.getLayoutParams().height = lowVideoHeight;
        mVTLowVideo.getLayoutParams().width = lowVideoWidth;

        // set sizes and positions of mVTHighUp / mVTHighDown / mVTLowUp / mVTLowDown
        adjustLocalVTIcons(highVideoHeight, lowVideoHeight);

        // notify for call button re-layout
        int vtCallButtonMarginLeft = lowVideoWidth;
        int vtCallButtonHeight = lowVideoHeight;
        if (InCallUtils.isLandscape(mContext)) {
            ((InCallActivity) getActivity()).onFinishVtVideoLayout(0, 0, callCardBottom);
        } else {
            ((InCallActivity) getActivity()).onFinishVtVideoLayout(vtCallButtonMarginLeft, vtCallButtonMarginBottom, vtCallButtonHeight);
        }
    }

    /**
     * This function to adjust mVTHighUp / mVTHighDown / mVTLowUp / mVTLowDown based on heights of mVTHighVideo and mVTLowVideo.
     * For mVTHighUp and mVTHighDown, we set size of them as 20% of mVTHighVideo's height, and set their margin as 10%;
     * For mVTLowUp and mVTLowDown, we set size of them as 30% of mVTHighVideo's height, and set their margin as 10%;
     * @param highVideoHeight
     * @param lowVideoHeight
     */
    public void adjustLocalVTIcons(int highVideoHeight, int lowVideoHeight) {
        int highImageSize = (highVideoHeight * 2) / 10;
        int highImageEdge = (highVideoHeight * 1) / 10;
        int lowImageSize = (lowVideoHeight * 35) / 100;
        int lowImageEdge = (lowVideoHeight * 1) / 10;

        mVTHighUp.getLayoutParams().height = highImageSize;
        mVTHighUp.getLayoutParams().width = highImageSize;
        mVTHighDown.getLayoutParams().height = highImageSize;
        mVTHighDown.getLayoutParams().width = highImageSize;

        mVTLowUp.getLayoutParams().height = lowImageSize;
        mVTLowUp.getLayoutParams().width = lowImageSize;
        mVTLowDown.getLayoutParams().height = lowImageSize;
        mVTLowDown.getLayoutParams().width = lowImageSize;

        MarginLayoutParams highUpParams = (MarginLayoutParams) mVTHighUp.getLayoutParams();
        MarginLayoutParams highDownParams = (MarginLayoutParams) mVTHighDown.getLayoutParams();
        MarginLayoutParams lowUpParams = (MarginLayoutParams) mVTLowUp.getLayoutParams();
        MarginLayoutParams lowDownParams = (MarginLayoutParams) mVTLowDown.getLayoutParams();

        highUpParams.leftMargin = highImageEdge;
        highUpParams.topMargin = highImageEdge;
        highDownParams.leftMargin = highImageEdge;
        highDownParams.bottomMargin = highImageEdge;

        lowUpParams.leftMargin = lowImageEdge;
        lowUpParams.topMargin = lowImageEdge;
        lowDownParams.leftMargin = lowImageEdge;
        lowDownParams.bottomMargin = lowImageEdge;
    }

    public void onFinishLayoutAmend() {
        mVTLowVideo.setVisibility(View.VISIBLE);
    }

    public void showVTCallUI(boolean show) {
        log("showVTCallUI()... show : " + show);
        getView().setVisibility(show ? View.VISIBLE : View.GONE);

        if (show) {
            acquireVtWakeLock();
        } else {
            releaseVtWakeLock();
            dismissVTDialogs();
            hideLocalZoomOrBrightness(true);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DELAY_HIDE_VT_ICON_MESSAGE:
                    log("DELAY_HIDE_VT_ICON_MESSAGE");
                    hideLocalZoomOrBrightness(true);
                    break;
                case DELAY_DISMISS_VT_ASK_DIALOG_MESSAGE:
                    log("DELAY_DISMISS_VT_ASK_DIALOG_MESSAGE");
                    if (mVTMTAskDialog != null) {
                        if (mVTMTAskDialog.isShowing()) {
                            mVTMTAskDialog.cancel();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void delayHideVTIcon() {
        if (mHandler.hasMessages(DELAY_HIDE_VT_ICON_MESSAGE)) {
            mHandler.removeMessages(DELAY_HIDE_VT_ICON_MESSAGE);
        }
        mHandler.sendMessageDelayed(Message.obtain(mHandler, DELAY_HIDE_VT_ICON_MESSAGE), DELAY_HIDE_VT_ICON_TIME);
    }

    public void onVTStateChanged(int msgVT) {
        Log.d(this, "onVTStateChanged()... msgVT: " + msgVT);

        switch (msgVT) {

        case VTManagerLocal.VT_MSG_OPEN:
            dismissVTDialogs();
            hideLocalZoomOrBrightness(true);
            updatePeerVideoBkgDrawable();       // MSG_VT_OPEN(get right value of VT Setting) / onResume() UI show
            if (okToSetDisplay()) {
                onSurfaceReady(true);           // MSG_VT_OPEN(get right value of VT Setting) / SurfaceChange
            }
            break;
        case VTManagerLocal.VT_MSG_READY:
            onVTReady();
            break;

        case VTManagerLocal.VT_MSG_CLOSE:
            dismissVTDialogs();
            hideLocalZoomOrBrightness(true);
            break;

        case VTManagerLocal.VT_MSG_RECEIVE_FIRSTFRAME:
            onVTReceiveFirstFrame();
            break;

        case VTManagerLocal.VT_MANAGER_PARAMS_UPDATE:
            updateLocalZoomOrBrightness();
            break;

        case VTManagerLocal.VT_SETTING_PARAMS_UPDATE:
            
            break;

        default:
            break;
        }
    }

}
