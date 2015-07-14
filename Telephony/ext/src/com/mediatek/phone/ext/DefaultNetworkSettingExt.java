package com.mediatek.phone.ext;

import java.util.List;

import android.app.AlertDialog;
import android.preference.PreferenceActivity;

import com.android.internal.telephony.OperatorInfo;

public class DefaultNetworkSettingExt implements INetworkSettingExt {
    @Override
    public void initPreferenceActivity(PreferenceActivity activity) {
        // do nothing
    }

    @Override
    public void customizeAlertDialog(int token, AlertDialog.Builder builder) {
        // do nothing
    }

    @Override
    public void deinitPreferenceActivity() {
        // do nothing
    }

    @Override
    public List<OperatorInfo> customizeNetworkList(List<OperatorInfo> operatorInfoList,
            int soltId) {
        return operatorInfoList;
    }

    @Override
    public boolean onPreferenceTreeClick(OperatorInfo operatorInfo, int slotId) {
        return false;
    }

}
