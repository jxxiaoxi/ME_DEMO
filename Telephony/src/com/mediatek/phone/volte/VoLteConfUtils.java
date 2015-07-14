package com.mediatek.phone.volte;

import java.util.List;

import android.R.bool;
import android.util.Log;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gsm.GsmCall;
import com.android.internal.telephony.gsm.GsmConnection;
//import com.android.services.telephony.common.Call.IMSCallMode;    // mark volte part
import com.mediatek.phone.PhoneFeatureConstants.FeatureOption;
import com.mediatek.phone.wrapper.CallManagerWrapper;

public class VoLteConfUtils {
    private static final String LOG_TAG = "VoLteConfUtils";

    public static int getIMSCallMode(Connection connection) {
        int callMode = -1;
        /* // mark volte part
        if (FeatureOption.MTK_VOLTE_SUPPORT && connection instanceof GsmConnection) {
            GsmConnection gsmConnection = (GsmConnection) connection;
            int telePhonyCallMode = gsmConnection.getCallMode();
            if (isVoLteConfCall(gsmConnection.getCall())) {
                if (isConferenceHost(gsmConnection.getCall())) {
                    callMode = IMSCallMode.IMS_VOICE_CONF_HOST;
                } else {
                    callMode = IMSCallMode.IMS_VOICE_CONF_PARTICIPANT;
                }
            } else if(telePhonyCallMode == GsmConnection.IMS_VOICE_CALL) {
                callMode = IMSCallMode.IMS_VOICE_CALL;
            } else if(telePhonyCallMode == GsmConnection.IMS_VIDEO_CALL) {
                callMode = IMSCallMode.IMS_VIDEO_CALL;
            }
        }
        */
        return callMode;
    }

    public static int getIMSConferenceId(Connection connection) {
        int conferenceId = -1;
        /* // mark volte part
        if (FeatureOption.MTK_VOLTE_SUPPORT && connection instanceof GsmConnection
                && isVoLteConfCall(connection.getCall())) {
            GsmConnection gc = getConferenceConnection(connection.getCall());
            if (gc != null) {
                try {
                    conferenceId = gc.getGSMIndex();
                } catch (CallStateException e) {
                    e.printStackTrace();
                }
            }
        }
        */
        return conferenceId;
    }

    public static GsmConnection getConferenceConnection(com.android.internal.telephony.Call call) {
        List<Connection> connections = call.getConnections();
        GsmConnection result = null;
        /* // mark volte part
        for (Connection connection : connections) {
            if (connection instanceof GsmConnection) {
                GsmConnection conn = (GsmConnection) connection;
                int role = conn.getRoleOfConnection();
                if (isConferenceHost(call)) {
                    if (GsmConnection.VOLTE_CONFERENCE_CREATOR_CONNECTION == role) {
                        result = conn;
                        break;
                    }
                } else if (GsmConnection.VOLTE_CONFERENCE_USER_CONNECTION == role) {
                    result = conn;
                    break;
                }
            }
        }
        */
        log("getConferenceConnection, result = " + result);
        return result;
    }

    public static boolean isConferenceHost(com.android.internal.telephony.Call call) {
        log("isConferenceHost, call = " + call);
        boolean isHost = false;
        /* // mark volte part
        if (FeatureOption.MTK_VOLTE_SUPPORT && call instanceof GsmCall) {
            List<Connection> connections = call.getConnections();
            for (Connection conn : connections) {
                if (((GsmConnection) conn).getRoleOfConnection() == GsmConnection.VOLTE_CONFERENCE_CREATOR_CONNECTION) {
                    isHost = true;
                }
            }
        }
        */
        log("is Host = " + isHost);
        return isHost;
    }

    public static boolean isVoLteConfMainConnection(Connection connection) {
        Connection conn = getConferenceConnection(connection.getCall());
        if(connection != null && conn == connection) {
            return true;
        }
        return false;
    }

    public static boolean isVoLteConfCall(Connection connection) {
        return isVoLteConfCall(connection.getCall());
    }
 
    public static boolean isVoLteConfCall(com.android.internal.telephony.Call call) {
        log("isVoLteConfCall, call = " + call);
        boolean isConference = false;
        /* // mark volte part
        if(FeatureOption.MTK_VOLTE_SUPPORT &&  call instanceof GsmCall) {
            List<Connection> connections = call.getConnections();
            
            // use call mode to judge
            for(Connection conn: connections) {
                if(((GsmConnection) conn).getCallMode() == GsmConnection.IMS_VOICE_CONF) {
                    isConference = true;
                }
            }
        */

            // plan B
            /*
            if(connections.size() > 1 && isConferenceHost(call)) {
                // creator case
                isConference = true;
            } else if(connections.size() == 1) {
                // participant case
                if (call.getState().isRinging()) {
                    // if ringing call the connection role is not ready for use, so use call mode
                    if(((GsmConnection)call.getLatestConnection()).getCallMode() == GsmConnection.IMS_VOICE_CONF) {
                        isConference = true;
                    }
                } else {
                    GsmConnection gc = (GsmConnection) connections.get(0);
                    int role = gc.getRoleOfConnection();
                    if (GsmConnection.VOLTE_CONFERENCE_USER_CONNECTION == role
                            || GsmConnection.VOLTE_CONFERENCE_CREATOR_CONNECTION == role) {
                        isConference = true;
                    }
                }
            }
            */
//        }     // mark volte part
        log("is conference = " + isConference);
        return isConference;
    }

    public static void addConferenceMember(int conferenceId, String number) {
        log("addConferenceMember");
        /* // mark volte part
        List<Connection> connections = CallManager.getInstance().getFgCallConnections();
        connections.addAll(CallManager.getInstance().getBgCallConnections());
        Phone phone = null;
        for (Connection connection : connections) {
            if (connection instanceof GsmConnection) {
                int index = -1;
                try {
                    index = ((GsmConnection) connection).getGSMIndex();
                } catch (CallStateException e) {
                    log("exception when getGSMIndex!!!!!!");
                }
                if (index == conferenceId) {
                    log("find the right connection~~");
                    phone = connection.getCall().getPhone();
                    break;
                }
            }
        }
        if(phone != null) {
            CallManagerWrapper.addConferenceMember(phone, number);
        }
        */
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
