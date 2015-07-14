package com.mediatek.incallui.volte;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.incallui.CallList;
import com.android.incallui.ContactInfoCache;
import com.android.incallui.Log;
import com.android.incallui.R;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.ContactInfoCache.ContactInfoCacheCallback;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.services.telephony.common.Call;
import com.mediatek.incallui.volte.VoLteConfAddMemberScreen;

public class VoLteConfUiController implements InCallStateListener, CallList.Listener{
    private VoLteConfAddMemberScreen mAddMemberDialog;
    private static VoLteConfUiController sController = new VoLteConfUiController();
    private Context mContext;
    private int mConfereneId = -1;

    public static VoLteConfUiController getInstance() {
        return sController;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void setAddMemberScreenInstance(VoLteConfAddMemberScreen screen) {
        mAddMemberDialog = screen;
    }

    public void showAddConferenceMemberDialog(int ConfereneId) {
        Log.d(this, "showAddConferenceMemberDialog...");
        Intent intent = new Intent(mContext, VoLteConfAddMemberScreen.class);
        intent.putExtra(VoLteConstants.EXTRA_VOLTE_CONFERENCE_CALL_ID, ConfereneId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        mConfereneId = ConfereneId;
    }

    public void dismissAddConferenceMemberDialog() {
        Log.d(this, "dismissAddConferenceMemberDialog...");
        if (mAddMemberDialog != null) {
            mAddMemberDialog.finish();
            mAddMemberDialog = null;
            mConfereneId = -1;
        }
    }

    public static class MemberChangeNotifier {
        private static final String TAG = "MemberChangeNotifier";

        public static final int NOTIFY_MEMBER_CHANGE_OTHER = -1;
        public static final int NOTIFY_MEMBER_CHANGE_JOIN = 300;
        public static final int NOTIFY_MEMBER_CHANGE_LEAVE = 301;
        public static final int NOTIFY_MEMBER_CHANGE_ADDING = 302;
        public static final int NOTIFY_MEMBER_CHANGE_ADD_FAILED = 303;

        public static void notifyMemberChange(Activity activity, int notify, String name) {
            Log.d(TAG, "notifyMemberChange, notify = " + notify + ", name = " + name);
            String msg = null;
            switch (notify) {
            case NOTIFY_MEMBER_CHANGE_LEAVE:
                msg = activity.getResources().getString(R.string.conference_member_leave, name);
                break;
            case NOTIFY_MEMBER_CHANGE_JOIN:
                msg = activity.getResources().getString(R.string.conference_member_join, name);
                break;
            case NOTIFY_MEMBER_CHANGE_ADDING:
                msg = activity.getResources().getString(R.string.conference_member_adding, name);
                break;
            case NOTIFY_MEMBER_CHANGE_ADD_FAILED:
                msg = activity.getResources().getString(R.string.conference_member_add_fail, name);
                break;
            case NOTIFY_MEMBER_CHANGE_OTHER:
                msg = name;
                break;
            default:
                break;
            }
            if (!TextUtils.isEmpty(msg)) {
                Toast toast = new Toast(activity);
                toast.setGravity(Gravity.CENTER, 0, 0);
                ViewGroup toastView = (ViewGroup) activity.getLayoutInflater().inflate(
                        R.layout.mtk_volte_conference_toast_view, null);
                TextView textView = (TextView) toastView.findViewById(R.id.toast_msg);
                if (textView != null) {
                    textView.setText(msg);
                }
                toast.setView(toastView);
                toast.show();
            }
        }
    }

    @Override
    public void onStateChange(InCallState state, CallList callList) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCallListChange(CallList callList) {
        Call call = callList.getActiveOrBackgroundCall();
        if(call == null || !call.isVoLteConferenceCall() || call.getConferenceId() != mConfereneId) {
            dismissAddConferenceMemberDialog();
        }
    }

    @Override
    public void onDisconnect(Call call) {
        if(call != null && call.isVoLteConferenceCall() && call.getConferenceId() == mConfereneId) {
            dismissAddConferenceMemberDialog();
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

}
