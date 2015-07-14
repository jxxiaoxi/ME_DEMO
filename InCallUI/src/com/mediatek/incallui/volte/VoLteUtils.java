package com.mediatek.incallui.volte;

import android.text.TextUtils;
import android.util.Log;

public class VoLteUtils {

    private static final String LOG_TAG = "VoLteUtils";
    private static final String PAU_FIELD_NUMBER = "<tel:";
    private static final String PAU_FIELD_NAME = "<name:";
    private static final String PAU_FIELD_SIP_NUMBER = "<sip:";
    private static final String PAU_FIELD_END_FLAG = ">";

    public static String getNumberFromPAU(String pau) {
        String number = "";
        if (!TextUtils.isEmpty(pau)) {
            number = getFieldValue(pau, PAU_FIELD_NUMBER);
        }
        Log.d(LOG_TAG, "getNumberFromPAU()... number / pau : " + number + " / " + pau);
        return number;
    }

    public static String getNameFromPAU(String pau) {
        String name = "";
        if (!TextUtils.isEmpty(pau)) {
            name = getFieldValue(pau, PAU_FIELD_NAME);
        }
        Log.d(LOG_TAG, "getNumberFromPAU()... name / pau : " + name + " / " + pau);
        return name;
    }

    public static String getSipNumberFromPAU(String pau) {
        String sipNumber = "";
        if (!TextUtils.isEmpty(pau)) {
            sipNumber = getFieldValue(pau, PAU_FIELD_SIP_NUMBER);
        }
        Log.d(LOG_TAG, "getNumberFromPAU()... sipNumber / pau : " + sipNumber + " / " + pau);

        // If The sip number is comprised with digit only, then return number without domain name. Eg, "+14253269830@10.174.2.2" => "+14253269830".
        // and if is not only comprised with digit, then return number + domain name. Eg, "Baicolin@iptel.org", then return "Baicolin@iptel.org".
        // the first digit maybe contains "+" or "-", like "+10010",  then we think it as the first case.
//        if (!TextUtils.isEmpty(sipNumber) && sipNumber.contains("@")) {
//            int index = sipNumber.indexOf("@");
//            String realNumber = sipNumber.substring(0, index);
//            realNumber = realNumber.trim();
//            if (realNumber.matches("^[+-]*[0-9]*$")) {
//                sipNumber = realNumber;
//            }
//        }
        return sipNumber;
    }

    private static String getFieldValue(String pau, String field) {
        String value = "";
        if (TextUtils.isEmpty(pau) || TextUtils.isEmpty(field)) {
            Log.e(LOG_TAG, "getFieldValue()... pau or field is null !");
            return value;
        }

        if (!pau.contains(field)) {
            Log.i(LOG_TAG, "getFieldValue()... There is no such field in pau !" + " field / pau :" + field + " / " + pau);
            return value;
        }

        int startIndex = pau.indexOf(field);
        startIndex += field.length();
        int endIndex = pau.indexOf(PAU_FIELD_END_FLAG, startIndex);
        value = pau.substring(startIndex, endIndex);
        return value;
    }

}
