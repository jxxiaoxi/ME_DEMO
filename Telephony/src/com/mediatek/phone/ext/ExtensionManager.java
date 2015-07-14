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

package com.mediatek.phone.ext;

import com.android.phone.PhoneGlobals;

import com.mediatek.phone.PhoneLog;
import com.mediatek.pluginmanager.Plugin;
import com.mediatek.pluginmanager.Plugin.ObjectCreationException;
import com.mediatek.pluginmanager.PluginManager;

public final class ExtensionManager {

    private static final String LOG_TAG = "ExtensionManager";

    private ExtensionManager() {
    }

    private static void log(String msg) {
        PhoneLog.d(LOG_TAG, msg);
    }

    private static IMobileNetworkSettingsExt sMobileNetworkSettingsExt;
    private static ICallSettingsExt sCallSettingsExt;
    private static IRCSeCallNotifierExt sRCSeCallNotifierExt;
    private static IEmergencyDialerExt sEmergencyDialerExt;
    private static IPhoneMiscExt sPhoneMiscExt;
    private static IMmiCodeExt sMmiCodeExt;
    private static IModem3GCapabilitySwitchExt sModem3GCapabilitySwitchExt;
    private static INetworkSettingExt sNetworkSettingExt;
    private static IPhoneCallOptionHandlerExt sIPhoneCallOptionHandlerExt;

    public static IMobileNetworkSettingsExt getMobileNetworkSettingsExt() {
        if (sMobileNetworkSettingsExt == null) {
            synchronized (IMobileNetworkSettingsExt.class) {
                if (sMobileNetworkSettingsExt == null) {
                    try {
                        sMobileNetworkSettingsExt = (IMobileNetworkSettingsExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), IMobileNetworkSettingsExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sMobileNetworkSettingsExt = new DefaultMobileNetworkSettingsExt();
                    }
                    log("[getMobileNetworkSettingsExt]create ext instance: " + sMobileNetworkSettingsExt);
                }
            }
        }
        return sMobileNetworkSettingsExt;
    }

    public static IRCSeCallNotifierExt getRCSeCallNotifierExt() {
        if (sRCSeCallNotifierExt == null) {
            synchronized (IRCSeCallNotifierExt.class) {
                if (sRCSeCallNotifierExt == null) {
                    try {
                        sRCSeCallNotifierExt = (IRCSeCallNotifierExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), IRCSeCallNotifierExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sRCSeCallNotifierExt = new DefaultRCSeCallNotifierExt();
                    }
                    log("[getRCSeCallNotifierExt]create ext instance: " + sRCSeCallNotifierExt);
                }
            }
        }
        return sRCSeCallNotifierExt;
    }

    public static ICallSettingsExt getCallSettingsExt() {
        if (sCallSettingsExt == null) {
            synchronized (ICallSettingsExt.class) {
                if (sCallSettingsExt == null) {
                    try {
                        sCallSettingsExt = (ICallSettingsExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), ICallSettingsExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sCallSettingsExt = new DefaultCallSettingsExt();
                    }
                    log("[getCallSettingsExt]create ext instance: " + sCallSettingsExt);
                }
            }
        }
        return sCallSettingsExt;
    }

    public static IEmergencyDialerExt getEmergencyDialerExt() {
        if (sEmergencyDialerExt == null) {
            synchronized (IEmergencyDialerExt.class) {
                if (sEmergencyDialerExt == null) {
                    try {
                        sEmergencyDialerExt = (IEmergencyDialerExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), IEmergencyDialerExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sEmergencyDialerExt = new DefaultEmergencyDialerExt();
                    }
                    log("[getEmergencyDialerExt]create ext instance: " + sEmergencyDialerExt);
                }
            }
        }
        return sEmergencyDialerExt;
    }

    public static IPhoneMiscExt getPhoneMiscExt() {
        if (sPhoneMiscExt == null) {
            synchronized (IPhoneMiscExt.class) {
                if (sPhoneMiscExt == null) {
                    try {
                        sPhoneMiscExt = (IPhoneMiscExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), IPhoneMiscExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sPhoneMiscExt = new DefaultPhoneMiscExt();
                    }
                    log("[getPhoneMiscExt]create ext instance: " + sPhoneMiscExt);
                }
            }
        }
        return sPhoneMiscExt;
    }

    public static IMmiCodeExt getMmiCodeExt() {
        if (sMmiCodeExt == null) {
            synchronized (IMmiCodeExt.class) {
                if (sMmiCodeExt == null) {
                    try {
                        sMmiCodeExt = (IMmiCodeExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), IMmiCodeExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sMmiCodeExt = new DefaultMmiCodeExt();
                    }
                    log("[getMmiCodeExt]create ext instance: " + sMmiCodeExt);
                }
            }
        }
        return sMmiCodeExt;
    }

    public static IModem3GCapabilitySwitchExt getModem3GCapabilitySwitchExt() {
        if (sModem3GCapabilitySwitchExt == null) {
            synchronized (IModem3GCapabilitySwitchExt.class) {
                if (sModem3GCapabilitySwitchExt == null) {
                    try {
                        sModem3GCapabilitySwitchExt = (IModem3GCapabilitySwitchExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), IModem3GCapabilitySwitchExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sModem3GCapabilitySwitchExt = new DefaultModem3GCapabilitySwitchExt();
                    }
                    log("[getModem3GCapabilitySwitchExt]create ext instance: " + sModem3GCapabilitySwitchExt);
                }
            }
        }
        return sModem3GCapabilitySwitchExt;
    }

    public static INetworkSettingExt getNetworkSettingExt() {
        if (sNetworkSettingExt == null) {
            synchronized (INetworkSettingExt.class) {
                if (sNetworkSettingExt == null) {
                    try {
                        sNetworkSettingExt = (INetworkSettingExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), INetworkSettingExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sNetworkSettingExt = new DefaultNetworkSettingExt();
                    }
                    log("[getNetworkSettingExt]create ext instance: " + sNetworkSettingExt);
                }
            }
        }
        return sNetworkSettingExt;
    }

    public static IPhoneCallOptionHandlerExt getPhoneCallOptionHandlerExt() {
        if (sIPhoneCallOptionHandlerExt == null) {
            synchronized (IPhoneCallOptionHandlerExt.class) {
                if (sIPhoneCallOptionHandlerExt == null) {
                    try {
                        sIPhoneCallOptionHandlerExt = (IPhoneCallOptionHandlerExt) PluginManager.createPluginObject(
                                PhoneGlobals.getInstance(), IPhoneCallOptionHandlerExt.class.getName());
                    } catch (Plugin.ObjectCreationException e) {
                        sIPhoneCallOptionHandlerExt = new DefaultPhoneCallOptionHandlerExt();
                    }
                    log("[getPhoneCallOptionHandlerExt]create ext instance: " + sIPhoneCallOptionHandlerExt);
                }
            }
        }
        return sIPhoneCallOptionHandlerExt;
    }
}
