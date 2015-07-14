/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

/*
 * Copyright (C) 2009 The Android Open Source Project
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
 * limitations under the License.
 */

package com.mediatek.phone.volte;


import android.util.Log;

import com.android.internal.telephony.CallStateException;
//import com.android.internal.telephony.ConferenceCallXml;  // mart volte part
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.gsm.GsmConnection;
import com.android.phone.PhoneGlobals;
import com.android.phone.PhoneUtils;
import com.android.services.telephony.common.Call;
import com.android.services.telephony.common.Call.IMSCallMode;
import com.google.android.collect.Lists;
import com.mediatek.services.telephony.common.VoLteConferenceMember;
import com.mediatek.services.telephony.common.VoLteConferenceMember.IMSConferenceRole;
import com.mediatek.services.telephony.common.VoLteConferenceMember.IMSConferenceStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Helper class to handle volte conference call
 */
public class VoLteConfModeler {
    private static final String LOG_TAG = "VoLteConfModeler";

    private static VoLteConfModeler sInstance;
    private final ArrayList<Listener> mListeners = new ArrayList<Listener>();
    private static final int FAKE_CALL_ID_START_VALUE = 2000;
    private final AtomicInteger mNextFakeCallId = new AtomicInteger(FAKE_CALL_ID_START_VALUE);

    private VoLteConfModeler() {

    }

    public static VoLteConfModeler getInstance() {
        if(sInstance == null) {
            sInstance = new VoLteConfModeler();
        }
        return sInstance;
    }

    /* mark volte part
    public void processConferenceCallIndication(ConferenceCallXml xmlData) {
        int callId = xmlData.getCallId();
        log("processConferenceCallIndication, xmlData call id = " + callId);
        Connection connection = getConnByTelephonyCallId(callId);
        List<VoLteConferenceMember> members = Lists.newArrayList();

        if (connection != null) {
            log("find conference connection, connection = " + connection);
            if (((GsmConnection) connection).getRoleOfConnection() == GsmConnection.VOLTE_CONFERENCE_CREATOR_CONNECTION) {
                // conference host
                for (Connection conn : connection.getCall().getConnections()) {
                    log("loop in conference host connecitons, connection = " + conn);
                    GsmConnection gsmConn = (GsmConnection) conn;
                    if (gsmConn.getRoleOfConnection() == GsmConnection.VOLTE_CONFERENCE_USER_CONNECTION
                            && gsmConn.getState().isAlive()) {
                        Call call = PhoneGlobals.getInstance().getCallModeler().getVoLteConfedCall(
                                gsmConn);
                        int status = translateConferenceStatus(gsmConn.getConferenceUserData()
                                .getStatus());
                        members.add(new VoLteConferenceMember(call, callId,
                                IMSConferenceRole.IMS_CONFERENCE_HOST_USER, status));
                    }
                }
            } else if (((GsmConnection) connection).getRoleOfConnection() == GsmConnection.VOLTE_CONFERENCE_USER_CONNECTION) {
                // for conference participant
                for(ConferenceCallXml.User user: xmlData.getUsers()) {
                    log("loop in conference participant users, user entity = " + user.getEntity());
                    Call call = generateFakeCallForParticipant(user, callId,
                            IMSCallMode.IMS_VOICE_CONF_PARTICIPANT);
                    int status = translateConferenceStatus(user.getStatus());
                    members.add(new VoLteConferenceMember(call, callId,
                            IMSConferenceRole.IMS_CONFERENCE_PARTICIPANT_USER, status));
                }
            }
            dumpVoLTEConfMemberList(callId, members);
            notifyConferenceUpdate(callId, members);
        }
    }
    */

    public static void dumpVoLTEConfMemberList(int conferenceId, List<VoLteConferenceMember> members) {
        Log.d(LOG_TAG, "------Dump VoLTE Conference Member List Begin-------");
        if (members != null) {
            Log.d(LOG_TAG, "------List size / conferenceId: " + members.size() + " / " + conferenceId);
            for (VoLteConferenceMember member : members) {
                Log.d(LOG_TAG, "------" + member);
            }
        }
        Log.d(LOG_TAG, "------Dump VoLTE Conference Member List End-------");
    }

    private void notifyConferenceUpdate(int conferenceId, List<VoLteConferenceMember> members) {
        for (int i = 0; i < mListeners.size(); ++i) {
            mListeners.get(i).onVoLteConferenceUpdate(conferenceId, members);
        }
    }

    public void addListener(Listener listener){
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public interface Listener {
        void onVoLteConferenceUpdate(int conferenceId, List<VoLteConferenceMember> members);
    }

    /**
     * Creates a brand new connection for the call.
     */
    private Call createNewCall() {
        int callId;
        int newNextCallId;
        do {
            callId = mNextFakeCallId.get();

            // protect against overflow
            newNextCallId = (callId == Integer.MAX_VALUE ?
                    FAKE_CALL_ID_START_VALUE : callId + 1);

            // Keep looping if the change was not atomic OR the value is already taken.
            // The call to containsValue() is linear, however, most devices support a
            // maximum of 7 connections so it's not expensive.
        } while (!mNextFakeCallId.compareAndSet(callId, newNextCallId));

        return new Call(callId);
    }

    /* mark volte part
    public Call generateFakeCallForParticipant(ConferenceCallXml.User user, int conferenceId, int callMode) {
        Call call = createNewCall();
        call.setNumber(user.getEntity());
        call.setConferenceId(conferenceId);
        call.setIMSCallMode(callMode);
        call.setState(Call.State.CONFERENCED);
        return call;
    }
    */

    /* mark volte part
    public static Connection getConnByTelephonyCallId(int id) {
        log("getConnByTelephonyCallId, id = " + id);
        final List<com.android.internal.telephony.Call> telephonyCalls = Lists.newArrayList();
        telephonyCalls.addAll(PhoneUtils.getRingingCalls());
        telephonyCalls.addAll(PhoneUtils.getForegroundCalls());
        telephonyCalls.addAll(PhoneUtils.getBackgroundCalls());

        for (com.android.internal.telephony.Call telephonyCall : telephonyCalls) {
            for (Connection connection : telephonyCall.getConnections()) {
                try {
                    if(connection instanceof GsmConnection && ((GsmConnection)connection).getGSMIndex() == id) {
                        return connection;
                    }
                } catch (CallStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    */

    /* mark volte part
    public static int translateConferenceStatus(String status) {
        if (ConferenceCallXml.STATUS_CONNECTED.equals(status)) {
            return IMSConferenceStatus.STATUS_CONNECTED;
        } else if (ConferenceCallXml.STATUS_ON_HOLD.equals(status)) {
            return IMSConferenceStatus.STATUS_ON_HOLD;
        } else if (ConferenceCallXml.STATUS_DISCONNECTED.equals(status)) {
            return IMSConferenceStatus.STATUS_DISCONNECTED;
        }
        return -1;
    }
    */

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
