package com.mediatek.incallui.volte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.util.Log;

import com.android.incallui.CallList;
import com.android.services.telephony.common.Call;
import com.google.android.collect.Maps;
import com.google.android.collect.Sets;
import com.mediatek.services.telephony.common.VoLteConferenceMember;

public class VoLteConfCallList implements CallList.Listener{

    private static final String LOG_TAG = "VoLteConfCallList";

    private static VoLteConfCallList sInstance = new VoLteConfCallList();

    private final HashMap<Integer, List<VoLteConferenceMember>> mCallMap = Maps.newHashMap();
    private final Set<VoLteConfDataUpdateListener> mListeners = Sets.newArraySet();

    private VoLteConfCallList() {
    }

    public static VoLteConfCallList getInstance() {
        return sInstance;
    }

    public void onVoLteConferenceUpdate(int conferenceId, List<VoLteConferenceMember> members) {
        dumpVoLTEConfMemberList(conferenceId, members, LOG_TAG);
        List<VoLteConferenceMember> oldList = mCallMap.get(conferenceId);
        List<VoLteConferenceMember> newMembers = new ArrayList<VoLteConferenceMember>();
        List<VoLteConferenceMember> leftMembers = new ArrayList<VoLteConferenceMember>();
        if(oldList != null) {
            boolean isNew = false;
            boolean isLeft = false;

            // find new members
            for(VoLteConferenceMember member : members) {
                isNew = true;
                for (VoLteConferenceMember oldMember : oldList) {
                    if(member.getEntity().equals(oldMember.getEntity())) {
                        isNew = false;
                        break;
                    }
                }
                if(isNew) {
                    newMembers.add(member);
                }
            }

            // find left members
            for(VoLteConferenceMember oldMember : oldList) {
                isLeft = true;
                for (VoLteConferenceMember member : members) {
                    if(member.getEntity().equals(oldMember.getEntity())) {
                        isLeft = false;
                        break;
                    }
                }
                if (isLeft) {
                    leftMembers.add(oldMember);
                }
            }
        } 

        for(VoLteConfDataUpdateListener listener : mListeners) {
            listener.onVoLteConferenceUpdate(members, conferenceId);
        }

        if(newMembers.size() > 0) {
            for(VoLteConfDataUpdateListener listener : mListeners) {
                listener.onMemberAddIn(newMembers);
            }
        }

        if(leftMembers.size() > 0) {
            for(VoLteConfDataUpdateListener listener : mListeners) {
                listener.onMemberLeft(leftMembers);
            }
        }

        mCallMap.put(conferenceId, members);
    }

    public List<VoLteConferenceMember> getConferenceMembers(int conferenceId) {
        return mCallMap.get(conferenceId);
    }

    public void addListener(VoLteConfDataUpdateListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(VoLteConfDataUpdateListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public interface VoLteConfDataUpdateListener {
        void onVoLteConferenceUpdate(List<VoLteConferenceMember> members, int conferenceId);
        void onMemberAddIn(List<VoLteConferenceMember> newMembers);
        void onMemberLeft(List<VoLteConferenceMember> leftMembers);
    }

    @Override
    public void onCallListChange(CallList callList) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDisconnect(Call call) {
        if(call.isVoLteConferenceCall()) {
            mCallMap.remove(call.getConferenceId());
        }
    }

    @Override
    public void onIncomingCall(Call call) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStorageFull() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onUpdateRecordState(int state, int customValue) {
        // TODO Auto-generated method stub
        
    }

    public static void dumpVoLTEConfMemberList(int conferenceId, List<VoLteConferenceMember> members, String logTag) {
        Log.d(logTag, "------Dump VoLTE Conference Member List Begin-------");
        if (members != null) {
            Log.d(logTag, "------List size / conferenceId: " + members.size() + " / " + conferenceId);
            for (VoLteConferenceMember member : members) {
                Log.d(logTag, "------" + member);
            }
        }
        Log.d(logTag, "------Dump VoLTE Conference Member List End-------");
    }
}
