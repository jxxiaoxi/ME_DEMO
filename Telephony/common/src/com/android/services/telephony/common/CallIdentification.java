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
 * limitations under the License.
 */

package com.android.services.telephony.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.internal.telephony.PhoneConstants;
import com.android.services.telephony.common.Call.IMSCallMode;
import com.google.common.base.Objects;

/**
 * Class object used across CallHandlerService APIs. Describes a single call and its state.
 */
public final class CallIdentification implements Parcelable {

    // Unique identifier for the call
    private int mCallId;

    // The phone number on the other end of the connection
    private String mNumber = "";

    // Number presentation received from the carrier
    private int mNumberPresentation = Call.PRESENTATION_ALLOWED;

    // Name presentation mode received from the carrier
    private int mCnapNamePresentation = Call.PRESENTATION_ALLOWED;

    // Name associated with the other end of the connection; from the carrier.
    private String mCnapName = "";

    public CallIdentification(int callId) {
        mCallId = callId;
    }

    public CallIdentification(CallIdentification identification) {
        mCallId = identification.mCallId;
        mNumber = identification.mNumber;
        mNumberPresentation = identification.mNumberPresentation;
        mCnapNamePresentation = identification.mCnapNamePresentation;
        mCnapName = identification.mCnapName;
        /// M: @{
        mSlotId = identification.mSlotId;
        mCallType = identification.mCallType;
        mIsIncoming = identification.mIsIncoming;
        /// @}

        /// M: for VoLTE normal call switch to ECC @{
        mIsECCCall = identification.mIsECCCall;
        /// @}

        /// M: for VoLTE PAU field @{
        mPAU = identification.mPAU;
        /// @}

        /// M: for VoLTE SS runtime indication @{
        mIsWaiting = identification.mIsWaiting;
        /// @}

        /// M: for VoLTE Conference Call@{
        mIMSCallMode = identification.mIMSCallMode;
        mConferenceId = identification.mConferenceId;
        /// @}
    }

    public int getCallId() {
        return mCallId;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        mNumber = number;
    }

    public int getNumberPresentation() {
        return mNumberPresentation;
    }

    public void setNumberPresentation(int presentation) {
        mNumberPresentation = presentation;
    }

    public int getCnapNamePresentation() {
        return mCnapNamePresentation;
    }

    public void setCnapNamePresentation(int presentation) {
        mCnapNamePresentation = presentation;
    }

    public String getCnapName() {
        return mCnapName;
    }

    public void setCnapName(String cnapName) {
        mCnapName = cnapName;
    }

    /**
     * Parcelable implementation
     */

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCallId);
        dest.writeString(mNumber);
        dest.writeInt(mNumberPresentation);
        dest.writeInt(mCnapNamePresentation);
        dest.writeString(mCnapName);
        /// M: @{
        dest.writeInt(mSlotId);
        dest.writeInt(mCallType);
        boolean val[] = { mIsIncoming, mIsECCCall, mIsWaiting };
        dest.writeBooleanArray(val);
        dest.writeString(mPAU);
        dest.writeInt(mIMSCallMode);
        dest.writeInt(mConferenceId);
        /// @}
    }

    /**
     * Constructor for Parcelable implementation.
     */
    private CallIdentification(Parcel in) {
        mCallId = in.readInt();
        mNumber = in.readString();
        mNumberPresentation = in.readInt();
        mCnapNamePresentation = in.readInt();
        mCnapName = in.readString();
        /// M: @{
        mSlotId = in.readInt();
        mCallType = in.readInt();
        boolean val[] = new boolean[3];
        in.readBooleanArray(val);
        mIsIncoming = val[0];
        mIsECCCall = val[1];
        mIsWaiting = val[2];
        mPAU = in.readString();
        mIMSCallMode = in.readInt();
        mConferenceId = in.readInt();
        /// @}
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Creates Call objects for Parcelable implementation.
     */
    public static final Creator<CallIdentification> CREATOR = new Creator<CallIdentification>() {

        @Override
        public CallIdentification createFromParcel(Parcel in) {
            return new CallIdentification(in);
        }

        @Override
        public CallIdentification[] newArray(int size) {
            return new CallIdentification[size];
        }
    };

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("mCallId", mCallId)
                .add("mNumber", MoreStrings.toSafeString(mNumber))
                .add("mNumberPresentation", mNumberPresentation)
                .add("mCnapName", MoreStrings.toSafeString(mCnapName))
                .add("mCnapNamePresentation", mCnapNamePresentation)
                /// M: @{
                .add("mSlotId", mSlotId)
                .add("mVideoCallFlag", mCallType)
                .add("mIsIncoming", mIsIncoming)
                .add("mIsECCCall", mIsECCCall)
                .add("mIsWaiting", mIsWaiting)
                .add("mPAU", mPAU)
                .add("mIMSCallMode", mIMSCallMode)
                .add("mConferenceId", mConferenceId)
                /// @}
                .toString();
    }

    // ---------------- MTK ----------------
    private int mCallType = Call.CALL_TYPE_VOICE;
    private int mSlotId = -1;
    private boolean mIsIncoming = false;

    public boolean isVideoCall() {
        return this.mCallType == Call.CALL_TYPE_VIDEO;
    }

    public boolean isIncoming() {
        return this.mIsIncoming;
    }

    public void setIsIncomingFlag(boolean isIncoming) {
        this.mIsIncoming = isIncoming;
    }

    public void setCallType(int callType) {
        this.mCallType = callType;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public int getSlotId() {
        return mSlotId;
    }

    public void setSlotId(int slotId) {
        this.mSlotId = slotId;
    }

    /// for VoLTE normal call switch to ECC @{
    private boolean mIsECCCall = false;

    /**
     * whether this call is marked as ECC by Network
     * for feature:  VoLTE normal call switch to ECC
     * @return
     */
    public boolean isECCCall() {
        return mIsECCCall;
    }

    public void markAsECCCall() {
        mIsECCCall = true;
    }
    /// @}

    /// for VoLTE PAU field @{
    private String mPAU = null;

    /**
     * return the PAU field
     * for feature: VoLTE PAU field
     * @return
     */
    public String getPAUField() {
        return mPAU;
    }

    public void setPAUField(String pau) {
        mPAU = pau;
    }
    /// @}

    /// for VoLTE SS runtime indication @{
    private boolean mIsWaiting;

    /**
     * indicate the remote side is busy, this call is waiting
     * for feature: VoLTE SS runtime indication
     * @return
     */
    public boolean isWaitingCall() {
        return mIsWaiting;
    }

    /**
     * mark this call is waiting because the remote side is busy, 
     * for feature: VoLTE SS runtime indication
     * @return
     */
    public void markAsWaitingCall(boolean isWaiting) {
        mIsWaiting = isWaiting;
    }
    /// @}

    /// for VoLTE Conference Call@{
    private int mIMSCallMode = -1;
    private int mConferenceId = -1;

    public int getIMSCallMode() {
        return mIMSCallMode;
    }

    public void setIMSCallMode(int callMode) {
        mIMSCallMode = callMode;
    }

    public boolean isVoLteConferenceCall() {
        return mIMSCallMode == IMSCallMode.IMS_VOICE_CONF_HOST
                || mIMSCallMode == IMSCallMode.IMS_VOICE_CONF_PARTICIPANT;
    }

    public boolean isVoLteConferenceHost() {
        return mIMSCallMode == IMSCallMode.IMS_VOICE_CONF_HOST;
    }

    public void setConferenceId(int id) {
        mConferenceId = id;
    }

    public int getConferenceId() {
        return mConferenceId;
    }
    /// @}
}
