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

package com.mediatek.settings.wfd;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

import com.android.settings.R;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

/**
 * Dialog fragment for WFD sink surface.
 */
public final class WfdSinkSurfaceFragment extends DialogFragment implements
        SurfaceHolder.Callback, View.OnLongClickListener {
    private static final String TAG = "WfdSinkSurfaceFragment";
    private WfdSettingsExt mExt;
    private SurfaceView mView;
    private Dialog mDialog;
    private DisplayManager mDisplayManager;
    private boolean mSurfaceShowing = false;
    private boolean mLatinTest = false;
    private int m_testLatinChar = 0xA0;

    public WfdSinkSurfaceFragment(WfdSettingsExt wfdSettingsExt, Context context) {
        mExt = wfdSettingsExt;
        mView = new WfdSinkSurfaceView(context);
        mView.setZOrderOnTop(true);
        mView.setFocusable(true);
        mDisplayManager = (DisplayManager) context
                          .getSystemService(Context.DISPLAY_SERVICE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Xlog.d(TAG, "onSaveInstanceState");
        if (mSurfaceShowing) {
            mExt.suspendWfdSinkConnection();
        }
        mSurfaceShowing = false;
        super.onSaveInstanceState(outState);
    }

    public Surface getSinkSurface() {
        Xlog.d(TAG, "mView is null? " + (mView == null));
        return mView != null ? mView.getHolder().getSurface() : null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Xlog.d(TAG, "mDialog is null? " + (mDialog == null));

        mLatinTest = 
            SystemProperties.get("wfd.uibc.latintest", "0") == "1" ? true : false;

        if (mDialog == null) {
            mDialog = new FullScreenDialog(getActivity());
        }
        return mDialog;
    }

    @Override
    public void onStop() {
        Xlog.d(TAG, "onStop");
        dismissAllowingStateLoss();
        super.onStop();
    }

    private class FullScreenDialog extends Dialog {

        private static final String DEFAULT_ENG_INPUTMETHOD = "com.android.inputmethod.latin/.LatinIME";
        private Activity mActivity;
        private int mOrientationBak;
        private String mInputMethodBak;

        public FullScreenDialog(Activity activity) {
            super(activity,
                  android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            mActivity = activity;
        }

        @Override
        public void onBackPressed() {
            Xlog.d(TAG, "dialog onBackPressed");
            if (mSurfaceShowing) {
                mExt.suspendWfdSinkConnection();
            }
            mSurfaceShowing = false;
            super.onBackPressed();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Xlog.d(TAG, "dialog oncreate");
            LayoutParams params = mActivity.getWindow().getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            mActivity.getWindow().setAttributes(params);
            ViewGroup.LayoutParams viewParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            setContentView(mView, viewParams);
            mView.setLongClickable(true);
        }

        @Override
        protected void onStart() {
            Xlog.d(TAG, "dialog  onStart");
            super.onStart();
            mView.getHolder().addCallback(WfdSinkSurfaceFragment.this);
            mView.setOnLongClickListener(WfdSinkSurfaceFragment.this);
            mOrientationBak = mActivity.getRequestedOrientation();
            mInputMethodBak = Settings.Secure
                    .getString(mActivity.getContentResolver(),
                            Settings.Secure.DEFAULT_INPUT_METHOD);
            Settings.Secure.putString(mActivity.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD,
                    DEFAULT_ENG_INPUTMETHOD);
            mActivity
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        protected void onStop() {
            Xlog.d(TAG, "dialog onStop");
            mView.getHolder().removeCallback(WfdSinkSurfaceFragment.this);
            Settings.Secure.putString(mActivity.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD,
                    mInputMethodBak != null ? mInputMethodBak : "");
            mActivity.setRequestedOrientation(mOrientationBak);
            super.onStop();
        }
        
        @Override
        public void dismiss() {
            Xlog.d(TAG, "dialog dismiss");
            if (mSurfaceShowing) {
                mExt.suspendWfdSinkConnection();
            }
            mSurfaceShowing = false;
            super.dismiss();
        }
    }
    
    private class WfdSinkSurfaceView extends SurfaceView {
        private static final int GENERIC_INPUT_TYPE_ID_TOUCH_DOWN = 0;
        private static final int GENERIC_INPUT_TYPE_ID_TOUCH_UP = 1;
        private static final int GENERIC_INPUT_TYPE_ID_TOUCH_MOVE = 2;
        private static final int GENERIC_INPUT_TYPE_ID_KEY_DOWN = 3;
        private static final int GENERIC_INPUT_TYPE_ID_KEY_UP = 4;
        private static final int GENERIC_INPUT_TYPE_ID_ZOOM = 5;
        private static final int LONG_PRESS_DELAY = 3000;
        private boolean mHasPerformedLongPress = false;
        private CheckForLongPress mPendingCheckForLongPress;
        private int mTouchSlop;
        private float mInitX;
        private float mInitY;

        public WfdSinkSurfaceView(Context context) {
            super(context);
            mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            final int action = ev.getAction();
            Xlog.d(TAG, "onTouchEvent action=" + action);
            switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    StringBuilder eventDesc = new StringBuilder();
                    eventDesc.append(
                            String.valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_DOWN))
                            .append(",");

                    eventDesc.append(getTouchEventDesc(ev));
                    sendUibcInputEvent(eventDesc.toString());
                }
                mHasPerformedLongPress = false;
                checkForLongClick(0);
                mInitX = ev.getX();
                mInitY = ev.getY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    StringBuilder eventDesc = new StringBuilder();
                    eventDesc.append(
                            String.valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_UP))
                            .append(",");

                    eventDesc.append(getTouchEventDesc(ev));
                    sendUibcInputEvent(eventDesc.toString());
                }
                if (!mHasPerformedLongPress) {
                    // This is a tap, so remove the longpress check
                    removeLongPressCallback();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    StringBuilder eventDesc = new StringBuilder();
                    eventDesc.append(
                            String.valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_MOVE))
                            .append(",");

                    eventDesc.append(getTouchEventDesc(ev));
                    sendUibcInputEvent(eventDesc.toString());
                }
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                if (Math.hypot(ev.getX() - mInitX, ev.getY() - mInitY) > mTouchSlop) {
                    removeLongPressCallback();
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                removeLongPressCallback();
                break;
            }


            }

            return true;
        }

        @Override
        public boolean onGenericMotionEvent(MotionEvent event) {
            Xlog.d(TAG, "onGenericMotionEvent event.getSource()="
                    + event.getSource());
            if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                if (event.getSource() == InputDevice.SOURCE_MOUSE) {
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                        StringBuilder eventDesc = new StringBuilder();
                        eventDesc
                                .append(
                                        String
                                                .valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_MOVE))
                                .append(",");
                        eventDesc.append(getTouchEventDesc(event));
                        sendUibcInputEvent(eventDesc.toString());
                        return true;
                    case MotionEvent.ACTION_SCROLL:
                        // process the scroll wheel movement...
                        return true;
                    default:
                        break;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            Xlog.d(TAG, "onKeyDown keyCode=" + keyCode);
            if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                int asciiCode = event.getUnicodeChar();
                if (asciiCode == 0 || asciiCode < 0x20) {
                    Xlog.d(TAG, "Can't find unicode for keyCode=" + keyCode);
                    asciiCode = KeyCodeConverter.keyCodeToAscii(keyCode);
                }

                if (mLatinTest && keyCode == KeyEvent.KEYCODE_F1) {
                    Xlog.d(TAG, "Latin Test Mode enabled");
                    asciiCode = m_testLatinChar;
                }
                Xlog.d(TAG, "onKeyDown asciiCode=" + asciiCode);
                if (asciiCode == 0) {
                    Xlog.d(TAG, "Can't find control for keyCode=" + keyCode);
                    return true;
                }
                StringBuilder eventDesc = new StringBuilder();
                eventDesc
                        .append(String.valueOf(GENERIC_INPUT_TYPE_ID_KEY_DOWN))
                        .append(",").append(String.format("0x%04x", asciiCode))
                        .append(", 0x0000");
                sendUibcInputEvent(eventDesc.toString());
            }
            return true;
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            Xlog.d(TAG, "onKeyUp keyCode=" + keyCode);
            if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                int asciiCode = event.getUnicodeChar();
                if (asciiCode == 0 || asciiCode < 0x20) {
                    Xlog.d(TAG, "Can't find unicode for keyCode=" + keyCode);
                    asciiCode = KeyCodeConverter.keyCodeToAscii(keyCode);
                }

                if (mLatinTest && keyCode == KeyEvent.KEYCODE_F1) {
                    Xlog.d(TAG, "Latin Test Mode enabled");
                    asciiCode = m_testLatinChar;
                    if (m_testLatinChar == 0xFF) m_testLatinChar = 0xA0;
                    else m_testLatinChar++;
                }
                Xlog.d(TAG, "onKeyUp asciiCode=" + asciiCode);
                if (asciiCode == 0) {
                    Xlog.d(TAG, "Can't find control for keyCode=" + keyCode);
                    return true;
                }
                StringBuilder eventDesc = new StringBuilder();
                eventDesc.append(String.valueOf(GENERIC_INPUT_TYPE_ID_KEY_UP))
                        .append(",").append(String.format("0x%04x", asciiCode))
                        .append(", 0x0000");
                sendUibcInputEvent(eventDesc.toString());
            }
            return true;
        }

        private String getTouchEventDesc(MotionEvent ev) {
            final int pointerCount = ev.getPointerCount();
            StringBuilder eventDesc = new StringBuilder();
            eventDesc.append(String.valueOf(pointerCount)).append(",");
            for (int p = 0; p < pointerCount; p++) {
                eventDesc.append(String.valueOf(ev.getPointerId(p)))
                .append(",").append(
                    String.valueOf((int) (ev.getXPrecision() * ev
                                          .getX(p)))).append(",").append(
                                              String.valueOf((int) (ev.getYPrecision() * ev
                                                      .getY(p)))).append(",");
            }
            return eventDesc.toString();
        }

        private void sendUibcInputEvent(String eventDesc) {
            Xlog.d(TAG, "sendUibcInputEvent: " + eventDesc);
            mDisplayManager.sendUibcInputEvent(eventDesc);
        }

        private void checkForLongClick(int delayOffset) {
            mHasPerformedLongPress = false;
            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }
            mPendingCheckForLongPress.rememberWindowAttachCount();
            postDelayed(mPendingCheckForLongPress, // ViewConfiguration.getLongPressTimeout()
                    LONG_PRESS_DELAY - delayOffset);
        }

        private void removeLongPressCallback() {
            Xlog.v(TAG, "removeLongPressCallback");
            if (mPendingCheckForLongPress != null) {
                removeCallbacks(mPendingCheckForLongPress);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            removeLongPressCallback();
            super.onDetachedFromWindow();
        }

        class CheckForLongPress implements Runnable {

            private int mOriginalWindowAttachCount;

            @Override
            public void run() {
                Xlog.v(TAG, "run: "+ (mOriginalWindowAttachCount == getWindowAttachCount()));
                if ((mParent != null)
                        && mOriginalWindowAttachCount == getWindowAttachCount()) {
                    if (performLongClick()) {
                        mHasPerformedLongPress = true;
                    }
                }
            }

            public void rememberWindowAttachCount() {
                mOriginalWindowAttachCount = getWindowAttachCount();
            }
        }
    }

    private static class KeyCodeConverter {
        private KeyCodeConverter() {}

          /* The ASCII control characters, per RFC 20. */
          /**
           * Null ('\0'): The all-zeros character which may serve to accomplish
           * time fill and media fill.  Normally used as a C string terminator.
           * <p>Although RFC 20 names this as "Null", note that it is distinct
           * from the C/C++ "NULL" pointer.
           *
           * @since 8.0
           */
          public static final byte NUL = 0;

          /**
           * Start of Heading: A communication control character used at
           * the beginning of a sequence of characters which constitute a
           * machine-sensible address or routing information.  Such a sequence is
           * referred to as the "heading."  An STX character has the effect of
           * terminating a heading.
           *
           * @since 8.0
           */
          public static final byte SOH = 1;

          /**
           * Start of Text: A communication control character which
           * precedes a sequence of characters that is to be treated as an entity
           * and entirely transmitted through to the ultimate destination.  Such a
           * sequence is referred to as "text."  STX may be used to terminate a
           * sequence of characters started by SOH.
           *
           * @since 8.0
           */
          public static final byte STX = 2;

          /**
           * End of Text: A communication control character used to
           * terminate a sequence of characters started with STX and transmitted
           * as an entity.
           *
           * @since 8.0
           */
          public static final byte ETX = 3;

          /**
           * End of Transmission: A communication control character used
           * to indicate the conclusion of a transmission, which may have
           * contained one or more texts and any associated headings.
           *
           * @since 8.0
           */
          public static final byte EOT = 4;

          /**
           * Enquiry: A communication control character used in data
           * communication systems as a request for a response from a remote
           * station.  It may be used as a "Who Are You" (WRU) to obtain
           * identification, or may be used to obtain station status, or both.
           *
           * @since 8.0
           */
          public static final byte ENQ = 5;

          /**
           * Acknowledge: A communication control character transmitted
           * by a receiver as an affirmative response to a sender.
           *
           * @since 8.0
           */
          public static final byte ACK = 6;

          /**
           * Bell ('\a'): A character for use when there is a need to call for
           * human attention.  It may control alarm or attention devices.
           *
           * @since 8.0
           */
          public static final byte BEL = 7;

          /**
           * Backspace ('\b'): A format effector which controls the movement of
           * the printing position one printing space backward on the same
           * printing line.  (Applicable also to display devices.)
           *
           * @since 8.0
           */
          public static final byte BS = 8;

          /**
           * Horizontal Tabulation ('\t'): A format effector which controls the
           * movement of the printing position to the next in a series of
           * predetermined positions along the printing line.  (Applicable also to
           * display devices and the skip function on punched cards.)
           *
           * @since 8.0
           */
          public static final byte HT = 9;

          /**
           * Line Feed ('\n'): A format effector which controls the movement of
           * the printing position to the next printing line.  (Applicable also to
           * display devices.) Where appropriate, this character may have the
           * meaning "New Line" (NL), a format effector which controls the
           * movement of the printing point to the first printing position on the
           * next printing line.  Use of this convention requires agreement
           * between sender and recipient of data.
           *
           * @since 8.0
           */
          public static final byte LF = 10;

          /**
           * Alternate name for {@link #LF}.  ({@code LF} is preferred.)
           *
           * @since 8.0
           */
          public static final byte NL = 10;

          /**
           * Vertical Tabulation ('\v'): A format effector which controls the
           * movement of the printing position to the next in a series of
           * predetermined printing lines.  (Applicable also to display devices.)
           *
           * @since 8.0
           */
          public static final byte VT = 11;

          /**
           * Form Feed ('\f'): A format effector which controls the movement of
           * the printing position to the first pre-determined printing line on
           * the next form or page.  (Applicable also to display devices.)
           *
           * @since 8.0
           */
          public static final byte FF = 12;

          /**
           * Carriage Return ('\r'): A format effector which controls the
           * movement of the printing position to the first printing position on
           * the same printing line.  (Applicable also to display devices.)
           *
           * @since 8.0
           */
          public static final byte CR = 13;

          /**
           * Shift Out: A control character indicating that the code
           * combinations which follow shall be interpreted as outside of the
           * character set of the standard code table until a Shift In character
           * is reached.
           *
           * @since 8.0
           */
          public static final byte SO = 14;

          /**
           * Shift In: A control character indicating that the code
           * combinations which follow shall be interpreted according to the
           * standard code table.
           *
           * @since 8.0
           */
          public static final byte SI = 15;

          /**
           * Data Link Escape: A communication control character which
           * will change the meaning of a limited number of contiguously following
           * characters.  It is used exclusively to provide supplementary controls
           * in data communication networks.
           *
           * @since 8.0
           */
          public static final byte DLE = 16;

          /**
           * Device Controls: Characters for the control
           * of ancillary devices associated with data processing or
           * telecommunication systems, more especially switching devices "on" or
           * "off."  (If a single "stop" control is required to interrupt or turn
           * off ancillary devices, DC4 is the preferred assignment.)
           *
           * @since 8.0
           */
          public static final byte DC1 = 17; // aka XON

          /**
           * Transmission on/off: Although originally defined as DC1, this ASCII
           * control character is now better known as the XON code used for software
           * flow control in serial communications.  The main use is restarting
           * the transmission after the communication has been stopped by the XOFF
           * control code.
           *
           * @since 8.0
           */
          public static final byte XON = 17; // aka DC1

          /**
           * @see #DC1
           *
           * @since 8.0
           */
          public static final byte DC2 = 18;

          /**
           * @see #DC1
           *
           * @since 8.0
           */
          public static final byte DC3 = 19; // aka XOFF

          /**
           * Transmission off. @see #XON
           *
           * @since 8.0
           */
          public static final byte XOFF = 19; // aka DC3

          /**
           * @see #DC1
           *
           * @since 8.0
           */
          public static final byte DC4 = 20;

          /**
           * Negative Acknowledge: A communication control character
           * transmitted by a receiver as a negative response to the sender.
           *
           * @since 8.0
           */
          public static final byte NAK = 21;

          /**
           * Synchronous Idle: A communication control character used by
           * a synchronous transmission system in the absence of any other
           * character to provide a signal from which synchronism may be achieved
           * or retained.
           *
           * @since 8.0
           */
          public static final byte SYN = 22;

          /**
           * End of Transmission Block: A communication control character
           * used to indicate the end of a block of data for communication
           * purposes.  ETB is used for blocking data where the block structure is
           * not necessarily related to the processing format.
           *
           * @since 8.0
           */
          public static final byte ETB = 23;

          /**
           * Cancel: A control character used to indicate that the data
           * with which it is sent is in error or is to be disregarded.
           *
           * @since 8.0
           */
          public static final byte CAN = 24;

          /**
           * End of Medium: A control character associated with the sent
           * data which may be used to identify the physical end of the medium, or
           * the end of the used, or wanted, portion of information recorded on a
           * medium.  (The position of this character does not necessarily
           * correspond to the physical end of the medium.)
           *
           * @since 8.0
           */
          public static final byte EM = 25;

          /**
           * Substitute: A character that may be substituted for a
           * character which is determined to be invalid or in error.
           *
           * @since 8.0
           */
          public static final byte SUB = 26;

          /**
           * Escape: A control character intended to provide code
           * extension (supplementary characters) in general information
           * interchange.  The Escape character itself is a prefix affecting the
           * interpretation of a limited number of contiguously following
           * characters.
           *
           * @since 8.0
           */
          public static final byte ESC = 27;

          /**
           * File/Group/Record/Unit Separator: These information separators may be
           * used within data in optional fashion, except that their hierarchical
           * relationship shall be: FS is the most inclusive, then GS, then RS,
           * and US is least inclusive.  (The content and length of a File, Group,
           * Record, or Unit are not specified.)
           *
           * @since 8.0
           */
          public static final byte FS = 28;

          /**
           * @see #FS
           *
           * @since 8.0
           */
          public static final byte GS = 29;

          /**
           * @see #FS
           *
           * @since 8.0
           */
          public static final byte RS = 30;

          /**
           * @see #FS
           *
           * @since 8.0
           */
          public static final byte US = 31;

          /**
           * Space: A normally non-printing graphic character used to
           * separate words.  It is also a format effector which controls the
           * movement of the printing position, one printing position forward.
           * (Applicable also to display devices.)
           *
           * @since 8.0
           */
          public static final byte SP = 32;

          /**
           * Alternate name for {@link #SP}.
           *
           * @since 8.0
           */
          public static final byte SPACE = 32;

          /**
           * Delete: This character is used primarily to "erase" or
           * "obliterate" erroneous or unwanted characters in perforated tape.
           *
           * @since 8.0
           */
          public static final byte DEL = 127;

          /**
           * The minimum value of an ASCII character.
           *
           * @since 9.0
           */
          public static final int MIN = 0;

          /**
           * The maximum value of an ASCII character.
           *
           * @since 9.0
           */
          public static final int MAX = 127;

          /**
           * Returns a copy of the input string in which all {@linkplain #isUpperCase(char) uppercase ASCII
           * characters} have been converted to lowercase. All other characters are copied without
           * modification.
           */
          public static String toLowerCase(String string) {
            int length = string.length();
            StringBuilder builder = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
              builder.append(toLowerCase(string.charAt(i)));
            }
            return builder.toString();
          }

          /**
           * If the argument is an {@linkplain #isUpperCase(char) uppercase ASCII character} returns the
           * lowercase equivalent. Otherwise returns the argument.
           */
          public static char toLowerCase(char c) {
            return isUpperCase(c) ? (char) (c ^ 0x20) : c;
          }

          /**
           * Returns a copy of the input string in which all {@linkplain #isLowerCase(char) lowercase ASCII
           * characters} have been converted to uppercase. All other characters are copied without
           * modification.
           */
          public static String toUpperCase(String string) {
            int length = string.length();
            StringBuilder builder = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
              builder.append(toUpperCase(string.charAt(i)));
            }
            return builder.toString();
          }

          /**
           * If the argument is a {@linkplain #isLowerCase(char) lowercase ASCII character} returns the
           * uppercase equivalent. Otherwise returns the argument.
           */
          public static char toUpperCase(char c) {
            return isLowerCase(c) ? (char) (c & 0x5f) : c;
          }

          /**
           * Indicates whether {@code c} is one of the twenty-six lowercase ASCII alphabetic characters
           * between {@code 'a'} and {@code 'z'} inclusive. All others (including non-ASCII characters)
           * return {@code false}.
           */
          public static boolean isLowerCase(char c) {
            return (c >= 'a') && (c <= 'z');
          }

          /**
           * Indicates whether {@code c} is one of the twenty-six uppercase ASCII alphabetic characters
           * between {@code 'A'} and {@code 'Z'} inclusive. All others (including non-ASCII characters)
           * return {@code false}.
           */
          public static boolean isUpperCase(char c) {
            return (c >= 'A') && (c <= 'Z');
          }
          private static final SparseIntArray KEYCODE_ASCII = new SparseIntArray();
          private static void populateKeycodeAscii() {
              SparseIntArray codes = KEYCODE_ASCII;
                codes.put(KeyEvent.KEYCODE_ALT_LEFT, DC2 );//Key code constant: Left Alt modifier key.
                codes.put(KeyEvent.KEYCODE_ALT_RIGHT, DC2 );//Key code constant: Right Alt modifier key.
                codes.put(KeyEvent.KEYCODE_ESCAPE, ESC);//Key code constant: Escape key.
                codes.put(KeyEvent.KEYCODE_SHIFT_LEFT, SI);//Key code constant: Left Shift modifier key.
                codes.put(KeyEvent.KEYCODE_SHIFT_RIGHT, SI);//Key code constant: Right Shift modifier key.
                codes.put(KeyEvent.KEYCODE_MOVE_END, 0x00);//Key code constant: End Movement key.
                codes.put(KeyEvent.KEYCODE_MOVE_HOME, 0x00);//Key code constant: Home Movement key.
                codes.put(KeyEvent.KEYCODE_CTRL_LEFT, 0x00);//Key code constant: Left Control modifier key.
                codes.put(KeyEvent.KEYCODE_CTRL_RIGHT, 0x00);//Key code constant: Right Control modifier key.
                codes.put(KeyEvent.KEYCODE_CAPS_LOCK, 0x00);//Key code constant: Caps Lock key.
                codes.put(KeyEvent.KEYCODE_BACK, ESC);//Key code constant: Back key.
                codes.put(KeyEvent.KEYCODE_DEL, BS);//Key code constant: Backspace key.
                codes.put(KeyEvent.KEYCODE_PAGE_DOWN, FF);//Key code constant: Page Down key.
                codes.put(KeyEvent.KEYCODE_ENTER, CR);//Key code constant: Enter key.
                codes.put(KeyEvent.KEYCODE_FORWARD_DEL, DEL);//Key code constant: Forward Delete key.
                codes.put(KeyEvent.KEYCODE_TAB, HT);//Key code constant: Tab key
            };
            
            public static int keyCodeToAscii(int keyCode) {
                int asciiCode = KEYCODE_ASCII.get(keyCode);
                return asciiCode;
            }
            
            static {
                populateKeycodeAscii();
            }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Xlog.d(TAG, "surface changed: " + width + "x" + height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Xlog.d(TAG, "surface created");
        if (!mSurfaceShowing) {
            mExt.setupWfdSinkConnection();
        }
        mSurfaceShowing = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Xlog.d(TAG, "surface destroyed");
        if (mSurfaceShowing) {
            mExt.suspendWfdSinkConnection();
        }
        mSurfaceShowing = false;
    }

    @Override
    public boolean onLongClick(View v) {
        Xlog.d(TAG, "onLongClick");
        dismiss();
        return true;
    }

}
