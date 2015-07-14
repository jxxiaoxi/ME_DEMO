package com.mediatek.incallui.volte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.mediatek.incallui.volte.VoLteConfUiController.MemberChangeNotifier;
import com.android.incallui.CallCommandClient;
import com.android.incallui.R;

public class VoLteConfAddMemberScreen extends AlertActivity implements DialogInterface.OnClickListener {

    private static AddMemberEditView mEditView;
    private static final int ADD_CONFERENCE_MEMBER_RESULT = 10000;
    private static ImageButton mChooseContactsView;
    public static final String ADD_CONFERENCE_MEMBER_DIALOG = "add conference_member";
    private static final String LOG_TAG = "AddConferenceMemberAcitivity";
    private Map<String, String> mContactsMap = new HashMap<String, String>();
    private int mConferenceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate...");
        super.onCreate(savedInstanceState);
        mConferenceId = (Integer) getIntent().getExtra(VoLteConstants.EXTRA_VOLTE_CONFERENCE_CALL_ID,
                -1);
        final AlertController.AlertParams p = mAlertParams;
        p.mView = createView();
        p.mTitle = getResources().getString(R.string.volte_add_conference_member_title);
        p.mPositiveButtonText = getString(com.android.internal.R.string.ok);
        p.mNegativeButtonText = getString(com.android.internal.R.string.cancel);
        p.mPositiveButtonListener =  this;
        p.mNegativeButtonListener = this;
        VoLteConfUiController.getInstance().setAddMemberScreenInstance(this);
        setupAlert();
    }

    @Override
    protected void onDestroy() {
        log("onDestroy...");
        mContactsMap.clear();
        VoLteConfUiController.getInstance().setAddMemberScreenInstance(null);
        super.onDestroy();
    }


    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.mtk_volte_add_conference_member, null);

        mChooseContactsView = (ImageButton) view.findViewById(R.id.choose_contacts);
        mChooseContactsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                chooseFromContacts();
            }
        });

        mEditView = (AddMemberEditView) view.findViewById(R.id.memeber_editor);
        mEditView.setAdapter(new ConferenceMemberAdatper(this));
        mEditView.setTokenizer(new Rfc822Tokenizer());
        mEditView.requestFocus();
        return view;
    }

    private void processAddConferenceMemberAction() {
        List<String> list = getNumbers();
        for(String s: list) {
            log("processAddConferenceMemberAction, number in list: "+ s);
        }
        if (list.size() != 1) {
            MemberChangeNotifier.notifyMemberChange(this,
                    MemberChangeNotifier.NOTIFY_MEMBER_CHANGE_OTHER, getResources().getString(
                            R.string.add_conference_member_limit));
            return;
        }

        // VoLTE TODO: to prevent adding the same number twice

        String  number = list.get(0);
        log("processAddConferenceMemberAction, Number = " + number);
        MemberChangeNotifier.notifyMemberChange(this,
                MemberChangeNotifier.NOTIFY_MEMBER_CHANGE_ADDING, getContactsName(number));
        // add member
        CallCommandClient.getInstance().addVoLteConfMember(mConferenceId, number);

        finish();
    }

    private void chooseFromContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI);
        startActivityForResult(intent, ADD_CONFERENCE_MEMBER_RESULT);
    }

    public void handleChooseContactsResult(Context context, Intent data) {
        Uri uri = data.getData();
        log("handleChooseContactsResult, return data is " + data);
        // query from contacts
        String name = null;
        String number = null;
        Cursor c = context.getContentResolver().query(uri,
                new String[] { Phone.DISPLAY_NAME, Phone.NUMBER }, null, null, null);
        try {
            if (c.moveToNext()) {
                name = c.getString(0);
                number = c.getString(1);
                mContactsMap.put(number, name);
            }
        } finally {
            c.close();
        }
        log("name = " + name + ", number = " + number);
        mEditView.append(number + ",");

    }

    private void log(String msg){
        Log.d(LOG_TAG, msg);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        if(DialogInterface.BUTTON_POSITIVE == which) {
            processAddConferenceMemberAction();
            finish();
        } else if(DialogInterface.BUTTON_NEGATIVE == which){
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult, request code = " + requestCode);
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
            case ADD_CONFERENCE_MEMBER_RESULT:
                handleChooseContactsResult(getApplicationContext(), data);
                break;
            default:
                break;
            }
        } else {
            Log.w(LOG_TAG, "onActivityResult fail!!");
        }
    }

    public List<String> getNumbers() {
        Spanned sp = mEditView.getText();
        log("getNumbers, numbers = " + sp.toString());
        List<String> list = new ArrayList<String>();
        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(sp);
        if (tokens.length == 0 && (sp != null && sp.length() > 0)) {
            list.add(sp.toString());
        }
        for (Rfc822Token token : tokens) {
            log("number:" + token.getAddress());
            String number = PhoneNumberUtils.replaceUnicodeDigits(token.getAddress());
            list.add(number);
        }
        return list;
    }

    private String getContactsName(String number) {
        String ret = number;
        if(mContactsMap.containsKey(number)) {
            log("getContactsName, find in map ~~");
            ret = mContactsMap.get(number);
        } else {
            String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
            if (!TextUtils.isEmpty(normalizedNumber)) {
                Cursor c = getContentResolver().query(
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, normalizedNumber),
                        new String[] { Phone.DISPLAY_NAME }, null, null, null);
                try {
                    if (c != null && c.moveToFirst()) {
                        ret = c.getString(0);
                        mContactsMap.put(number, ret);
                    }
                } finally {
                    c.close();
                }
            }
        }
        log("getContactsName for " + number + ", name =" + ret);
        return ret;
    }

}
