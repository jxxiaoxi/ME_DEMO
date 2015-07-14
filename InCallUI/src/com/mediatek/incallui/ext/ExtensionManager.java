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

package com.mediatek.incallui.ext;

import android.content.Context;

import com.android.incallui.Log;

import com.mediatek.pluginmanager.Plugin;
import com.mediatek.pluginmanager.Plugin.ObjectCreationException;
import com.mediatek.pluginmanager.PluginManager;

public final class ExtensionManager {

    private static final String TAG = ExtensionManager.class.getSimpleName();
    private static Context sApplicationContext;

    private ExtensionManager() {
    }

    public static void registerApplicationContext(Context context) {
        if (sApplicationContext == null) {
            sApplicationContext = context.getApplicationContext();
        }
    }

    private static IRCSeCallButtonExt sRCSeCallButtonExt;
    private static IRCSeCallCardExt sRCSeCallCardExt;
    private static IRCSeInCallExt sRCSeInCallExt;
    private static ICallCardExt sCallCardExt;
    private static INotificationExt sNotificationExt;

    public static IRCSeCallButtonExt getRCSeCallButtonExt() {
        if (sRCSeCallButtonExt == null) {
            synchronized (IRCSeCallButtonExt.class) {
                if (sRCSeCallButtonExt == null) {
                    try {
                        sRCSeCallButtonExt = (IRCSeCallButtonExt) PluginManager.createPluginObject(
                                sApplicationContext, IRCSeCallButtonExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sRCSeCallButtonExt = new DefaultRCSeCallButtonExt();
                    }
                    Log.i(TAG, "[getRCSeCallButtonExt]create ext instance: " + sRCSeCallButtonExt);
                }
            }
        }
        return sRCSeCallButtonExt;
    }

    public static IRCSeCallCardExt getRCSeCallCardExt() {
        if (sRCSeCallCardExt == null) {
            synchronized (IRCSeCallCardExt.class) {
                if (sRCSeCallCardExt == null) {
                    try {
                        sRCSeCallCardExt = (IRCSeCallCardExt) PluginManager.createPluginObject(
                                sApplicationContext, IRCSeCallCardExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sRCSeCallCardExt = new DefaultRCSeCallCardExt();
                    }
                    Log.i(TAG, "[getRCSeCallCardExt]create ext instance: " + sRCSeCallCardExt);
                }
            }
        }
        return sRCSeCallCardExt;
    }

    public static IRCSeInCallExt getRCSeInCallExt() {
        if (sRCSeInCallExt == null) {
            synchronized (IRCSeInCallExt.class) {
                if (sRCSeInCallExt == null) {
                    try {
                        sRCSeInCallExt = (IRCSeInCallExt) PluginManager.createPluginObject(
                                sApplicationContext, IRCSeInCallExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sRCSeInCallExt = new DefaultRCSeInCallExt();
                    }
                    Log.i(TAG, "[getRCSeInCallExt]create ext instance: " + sRCSeInCallExt);
                }
            }
        }
        return sRCSeInCallExt;
    }

    public static ICallCardExt getCallCardExt() {
        if (sCallCardExt == null) {
            synchronized (ICallCardExt.class) {
                if (sCallCardExt == null) {
                    try {
                        sCallCardExt = (ICallCardExt) PluginManager.createPluginObject(
                                sApplicationContext, ICallCardExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sCallCardExt = new DefaultCallCardExt();
                    }
                    Log.i(TAG, "[getCallCardExt]create ext instance: " + sCallCardExt);
                }
            }
        }
        return sCallCardExt;
    }

    public static INotificationExt getNotificationExt() {
        if (sNotificationExt == null) {
            synchronized (INotificationExt.class) {
                if (sNotificationExt == null) {
                    try {
                        sNotificationExt = (INotificationExt) PluginManager.createPluginObject(
                                sApplicationContext, INotificationExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sNotificationExt = new DefaultNotificationExt();
                    }
                    Log.i(TAG, "[getNotificationExt]create ext instance: " + sNotificationExt);
                }
            }
        }
        return sNotificationExt;
    }
}
