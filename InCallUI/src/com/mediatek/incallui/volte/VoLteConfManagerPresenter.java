/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.mediatek.incallui.volte;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.incallui.CallCommandClient;
import com.android.incallui.CallList;
import com.android.incallui.ContactInfoCache;
import com.android.incallui.InCallPresenter;
import com.android.incallui.Log;
import com.android.incallui.Presenter;
import com.android.incallui.Ui;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.ContactInfoCache.ContactInfoCacheCallback;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.services.telephony.common.Call;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import com.mediatek.incallui.volte.VoLteConfCallList.VoLteConfDataUpdateListener;
import com.mediatek.incallui.volte.VoLteConfUiController.MemberChangeNotifier;
import com.mediatek.services.telephony.common.VoLteConferenceMember;
import com.mediatek.services.telephony.common.VoLteConferenceMember.IMSConferenceRole;
import com.mediatek.services.telephony.common.VoLteConferenceMember.IMSConferenceStatus;

/**
 * Logic for call buttons.
 */
public class VoLteConfManagerPresenter
        extends Presenter<VoLteConfManagerPresenter.ConferenceManagerUi>
        implements InCallStateListener, VoLteConfDataUpdateListener {

    private static final int MAX_CALLERS_IN_CONFERENCE = 5;

    private int mNumCallersInConference;
    private List<VoLteConferenceMember> mConferenceMembers;
    private Context mContext;

    @Override
    public void onUiReady(ConferenceManagerUi ui) {
        super.onUiReady(ui);

        // register for call state changes last
        InCallPresenter.getInstance().addListener(this);

        /// M: for VoLTE Conference Call @{
        VoLteConfCallList.getInstance().addListener(this);
        /// @}
    }

    @Override
    public void onUiUnready(ConferenceManagerUi ui) {
        super.onUiUnready(ui);

        InCallPresenter.getInstance().removeListener(this);

        /// M: for VoLTE Conference Call @{
        VoLteConfCallList.getInstance().removeListener(this);
        /// @}
    }

    @Override
    public void onStateChange(InCallState state, CallList callList) {
        if (getUi().isFragmentVisible()) {
            Log.v(this, "onStateChange" + state);
            if (state == InCallState.INCALL) {
                final Call call = callList.getActiveOrBackgroundCall();
                if (call != null && call.isVoLteConferenceCall()) {
                    update(callList);
                } else {
                    getUi().setVisible(false);
                }
            } else {
                getUi().setVisible(false);
            }
        }
    }

    public void init(Context context, CallList callList) {
        mContext = Preconditions.checkNotNull(context);
        mContext = context;
        update(callList);
    }

    private void update(CallList callList) {
        CallList.getInstance().dumpCallList();
        Call call = callList.getActiveOrBackgroundCall();
        if(call != null && call.isVoLteConferenceCall()) {
            int id = call.getConferenceId();
            updateData(VoLteConfCallList.getInstance().getConferenceMembers(id));
        }
    }

    private void updateData(List<VoLteConferenceMember> members) {
        if(members == null) {
            Log.i(this, "VoLTE conference member list is null in updateData()...");
            return;
        }
        mConferenceMembers = members;
        mNumCallersInConference = members.size();
        Log.v(this, "Number of calls is " + String.valueOf(mNumCallersInConference));

        // Users can split out a call from the conference call if there either the active call
        // or the holding call is empty. If both are filled at the moment, users can not split out
        // another call.
        for (int i = 0; i < MAX_CALLERS_IN_CONFERENCE; i++) {
            if (i < mNumCallersInConference) {
                // Fill in the row in the UI for this caller.
                VoLteConferenceMember member = mConferenceMembers.get(i);
                final ContactCacheEntry contactCache = ContactInfoCache.getInstance(mContext).
                        getInfo(member.getAssociactedCall().getCallId());
                final boolean isOnLine = member.getStatus()==IMSConferenceStatus.STATUS_CONNECTED;
                final boolean canManage = member.getConferenceCallRole() == IMSConferenceRole.IMS_CONFERENCE_HOST_USER;
                updateManageConferenceRow(i, contactCache, isOnLine, canManage);
            } else {
                // Blank out this row in the UI
                updateManageConferenceRow(i, null, false, false);
            }
        }
    }

    /**
      * Updates a single row of the "Manage conference" UI.  (One row in this
      * UI represents a single caller in the conference.)
      *
      * @param i the row to update
      * @param contactCacheEntry the contact details corresponding to this caller.
      *        If null, that means this is an "empty slot" in the conference,
      *        so hide this row in the UI.
      * @param canSeparate if true, show a "Separate" (i.e. "Private") button
      *        on this row in the UI.
      */
    public void updateManageConferenceRow(final int i,
                                          final ContactCacheEntry contactCacheEntry,
                                          boolean isOnLine, boolean canManage) {

        if (contactCacheEntry != null) {
            // Activate this row of the Manage conference panel:
            getUi().setRowVisible(i, true);

            final String name = getEntryDisplayName(contactCacheEntry);
            final String number = getEntryDisplayNumber(contactCacheEntry);
            final String type = getEntryDisplayType(contactCacheEntry);

            getUi().setStatusForRow(i, isOnLine);

            // display the CallerInfo.
            getUi().setupRemoveButtonForRow(i, canManage);

            getUi().displayCallerInfoForConferenceRow(i, name, number, type);

        } else {
            // Disable this row of the Manage conference panel:
            getUi().setRowVisible(i, false);
        }
    }

    public void manageConferenceDoneClicked() {
        getUi().setVisible(false);
    }

    public int getMaxCallersInConference() {
        return MAX_CALLERS_IN_CONFERENCE;
    }

    public void endConferenceConnection(int rowId) {
        CallCommandClient.getInstance().disconnectCall(
                mConferenceMembers.get(rowId).getAssociactedCall().getCallId());
    }

    public interface ConferenceManagerUi extends Ui {
        void setVisible(boolean on);
        boolean isFragmentVisible();
        void setRowVisible(int rowId, boolean on);
        void displayCallerInfoForConferenceRow(int rowId, String callerName, String callerNumber,
                String callerNumberType);
        void setStatusForRow(final int rowId, boolean isOnLine);
        void setupRemoveButtonForRow(final int rowId, boolean canRemove);
        void startConferenceTime(long base);
        void stopConferenceTime();
        void notifyMemberChange(int notifyType, String name);
    }

    // ---------------- MTK ---------------------------------

    private String getEntryDisplayName(ContactCacheEntry entry) {
        if (entry == null) {
            Log.e(this, "[getEntryDisplayName]entry is null");
            return "";
        }
        if (TextUtils.isEmpty(entry.name)) {
            Log.d(this, "[getEntryDisplayName]name is empty, use number as name: " + entry.number);
            return entry.number;
        }
        return entry.name;
    }

    private String getEntryDisplayNumber(ContactCacheEntry entry) {
        if (entry == null) {
            Log.e(this, "[getEntryDisplayNumber]entry is null");
            return "";
        }
        return (TextUtils.isEmpty(entry.name)) ? "" : entry.number;
    }

    private String getEntryDisplayType(ContactCacheEntry entry) {
        if (entry == null) {
            Log.e(this, "[getEntryDisplayType]entry is null");
            return "";
        }
        return (TextUtils.isEmpty(entry.name)) ? "" : entry.label;
    }

    @Override
    public void onMemberAddIn(List<VoLteConferenceMember> newMembers) {
        for (VoLteConferenceMember member : newMembers) {
            ContactInfoCache.getInstance(mContext).findInfo(
                    member.getAssociactedCall().getIdentification(), false,
                    new UpdateMemberInfoCallBack(member, false, true, false));
        }
    }

    @Override
    public void onMemberLeft(List<VoLteConferenceMember> leftMembers) {
        for (VoLteConferenceMember member : leftMembers) {
            ContactInfoCache.getInstance(mContext).findInfo(
                    member.getAssociactedCall().getIdentification(), false,
                    new UpdateMemberInfoCallBack(member, false, false, true));
        }
    }

    @Override
    public void onVoLteConferenceUpdate(List<VoLteConferenceMember> members, int conferenceId) {
        Call currentCall = CallList.getInstance().getActiveOrBackgroundCall();
        if (currentCall != null && currentCall.isVoLteConferenceCall()
                && currentCall.getConferenceId() == conferenceId) {
            updateData(members);
            for (VoLteConferenceMember member : members) {
                ContactInfoCache.getInstance(mContext).findInfo(
                        member.getAssociactedCall().getIdentification(), false,
                        new UpdateMemberInfoCallBack(member, true, false, false));
            }
        }
    }

    public class UpdateMemberInfoCallBack implements ContactInfoCacheCallback {
        private VoLteConferenceMember mMember = null;
        private boolean mNeedUpdate = false;
        private boolean mNeedNotifyNew = false;
        private boolean mNeedNotifyLeft = false;

        public UpdateMemberInfoCallBack(VoLteConferenceMember member, boolean needUpdate,
                boolean needNotifyNew, boolean needNotifyLeft) {
            mMember = member;
            mNeedUpdate = needUpdate;
            mNeedNotifyNew = needNotifyNew;
            mNeedNotifyLeft = needNotifyLeft;
        }

        @Override
        public void onContactInfoComplete(int callId, ContactCacheEntry entry) {
            if (mNeedUpdate) {
                final int index = mConferenceMembers.indexOf(mMember);
                if (index >= 0 && index < MAX_CALLERS_IN_CONFERENCE) {
                    final ContactCacheEntry contactCache = ContactInfoCache.getInstance(mContext)
                            .getInfo(mMember.getAssociactedCall().getCallId());
                    final boolean isOnLine = mMember.getStatus() == IMSConferenceStatus.STATUS_CONNECTED;
                    final boolean canManage = mMember.getConferenceCallRole() == IMSConferenceRole.IMS_CONFERENCE_HOST_USER;
                    updateManageConferenceRow(index, contactCache, isOnLine, canManage);
                }
            }
            if(mNeedNotifyNew) {
                final ContactCacheEntry contactCache = ContactInfoCache.getInstance(mContext)
                .getInfo(mMember.getAssociactedCall().getCallId());
                String name = null;
                if(!TextUtils.isEmpty(contactCache.name)) {
                    name = contactCache.name;
                } else {
                    name = contactCache.number;
                }
                getUi().notifyMemberChange(MemberChangeNotifier.NOTIFY_MEMBER_CHANGE_JOIN, name);
            }

            if(mNeedNotifyLeft) {
                final ContactCacheEntry contactCache = ContactInfoCache.getInstance(mContext)
                .getInfo(mMember.getAssociactedCall().getCallId());
                String name = null;
                if(!TextUtils.isEmpty(contactCache.name)) {
                    name = contactCache.name;
                } else {
                    name = contactCache.number;
                }
                getUi().notifyMemberChange(MemberChangeNotifier.NOTIFY_MEMBER_CHANGE_LEAVE, name);
            }
        }

        @Override
        public void onImageLoadComplete(int callId, ContactCacheEntry entry) {
        }
    }
}
