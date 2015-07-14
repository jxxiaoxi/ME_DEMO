package com.mediatek.services.telephony.common;

import com.google.common.base.Objects;
import com.android.services.telephony.common.Call;

import android.os.Parcel;
import android.os.Parcelable;

public class VoLteConferenceMember implements Parcelable {

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
       dest.writeInt(mCallId);
       dest.writeInt(mRole);
       dest.writeInt(mStatus);
       dest.writeParcelable(mAssociactedCall, 0);

    }

    /**
     * Constructor for Parcelable implementation.
     */
    public VoLteConferenceMember(Call call,int ConferenceId, int role, int status) {
        mCallId = ConferenceId;
        mRole = role;
        mStatus = status;
        mAssociactedCall = call;
    }

    /**
     * Constructor for Parcelable implementation.
     */
    private VoLteConferenceMember(Parcel in) {
        mCallId = in.readInt();
        mRole = in.readInt();
        mStatus = in.readInt();
        mAssociactedCall = in.readParcelable(Call.class.getClassLoader());
    }

    /**
     * Creates Call objects for Parcelable implementation.
     */
    public static final Parcelable.Creator<VoLteConferenceMember> CREATOR
            = new Parcelable.Creator<VoLteConferenceMember>() {

        @Override
        public VoLteConferenceMember createFromParcel(Parcel in) {
            return new VoLteConferenceMember(in);
        }

        @Override
        public VoLteConferenceMember[] newArray(int size) {
            return new VoLteConferenceMember[size];
        }
    };

    /**
     * define the roles of a single call in the whole IMS conference call
     */
    public static class IMSConferenceRole {
        public static final int IMS_CONFERENCE_HOST_CREATOR = 100; // the creator connection, for host device
        public static final int IMS_CONFERENCE_HOST_USER = 101; // the user connection, for host device
        public static final int IMS_CONFERENCE_PARTICIPANT_USER = 102; // the user connection, for participant device
    }

    /**
     * define the status of a single call in the whole IMS conference call
     */
    public static class IMSConferenceStatus {
        public static final int STATUS_CONNECTED = 200;
        public static final int STATUS_DISCONNECTED = 201;
        public static final int STATUS_ON_HOLD = 202;
    }

    private int mCallId = -1;

    private int mRole = -1;

    private int mStatus = -1;

    private Call mAssociactedCall = null;

    /**
     * set the association call object for the conference call
     * @return
     */
    public void setAssociactedCall(Call call) {
        mAssociactedCall = call;
    }

    /**
     * get the association call object for the conference call
     * @return
     */
    public Call getAssociactedCall() {
        return mAssociactedCall;
    }

    /**
     * set the call id for the conference call
     * @return
     */
    public void setConfernceId(int id) {
        mCallId = id;
    }

    /**
     * set the association call id for the conference call
     * @return
     */
    public int getConfernceId() {
        return mCallId;
    }

    /**
     * set the role of this call in the whole conference call
     * refer to {@link IMSConferenceRole} {@link #getIMSConferenceCallRole}
     * @return
     */
    public void setConferenceCallRole(int role) {
        mRole = role;
    }

    /**
     * get the role of this call in the whole conference call
     * refer to {@link IMSConferenceRole} {@link #setIMSConferenceCallRole}
     * @return
     */
    public int getConferenceCallRole() {
        return mRole;
    }

    /**
     * set the status of this call in the whole conference call
     * refer to {@link IMSConferenceStatus} {@link #getStatus}
     * @return
     */
    public void setStatus(int status) {
        mStatus = status;
    }

    /**
     * get the status of this call in the whole conference call
     * refer to {@link IMSConferenceStatus} {@link #setSatus}
     * @return
     */
    public int getStatus() {
        return mStatus;
    }

    public String getEntity() {
        if (mAssociactedCall == null) {
            return null;
        }
        return mAssociactedCall.getNumber();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
        .add("mCallId", mCallId)
        .add("mRole", mRole)
        .add("mStatus", mStatus)
        .add("mAssociactedCall", mAssociactedCall)
        .toString();
    }
}
