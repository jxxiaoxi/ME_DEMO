/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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

package com.mediatek.settings;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.internal.telephony.TelephonyIntents;
import com.android.phone.R;
import com.mediatek.phone.gemini.GeminiUtils;
import com.mediatek.phone.vt.VTCallUtils;
import com.mediatek.phone.wrapper.PhoneWrapper;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.telephony.SimInfoManager.SimInfoRecord;
import com.mediatek.xlog.Xlog;

public class VTAdvancedSetting extends PreferenceActivity implements OnPreferenceChangeListener {

    //In current stage, we consider that only the card slot 0 support wcdma
    private static final String BUTTON_VT_REPLACE_KEY     = "button_vt_replace_expand_key";
    private static final String SELECT_DEFAULT_PICTURE    = "0";
    private static final String SELECT_MY_PICTURE         = "2";
    
    public static final String SELECT_DEFAULT_PICTURE2    = "0";
    public static final String SELECT_MY_PICTURE2         = "1";
    
    public static final String NAME_PIC_TO_REPLACE_LOCAL_VIDEO_USERSELECT = "pic_to_replace_local_video_userselect";
    public static final String NAME_PIC_TO_REPLACE_LOCAL_VIDEO_DEFAULT = "pic_to_replace_local_video_default";
    public static final String NAME_PIC_TO_REPLACE_PEER_VIDEO_USERSELECT = "pic_to_replace_peer_video_userselect";
    public static final String NAME_PIC_TO_REPLACE_PEER_VIDEO_DEFAULT = "pic_to_replace_peer_video_default";

    /** The launch code when picking a photo and the raw data is returned */
    public static final int REQUESTCODE_PICTRUE_PICKED_WITH_DATA = 3021;
    private static final int REQUESTCODE_PICTRUE_CROP = 3022;
    private static final String ACTION_CROP = "com.android.camera.action.CROP";
    private ListPreference mButtonVTReplace;
    private ListPreference mButtonVTPeerReplace;
    private int mWhichToSave = 0;

    // debug data
    private static final String LOG_TAG = "Settings/VTAdvancedSetting";
    private static final boolean DBG = true; // (PhoneApp.DBG_LEVEL >= 2);

    //Operator customization: SS used data
    private int mSlotId = -1;
    private static final String BUTTON_VT_CF_KEY = "button_cf_expand_key";
    private static final String BUTTON_VT_CB_KEY = "button_cb_expand_key";
    private static final String BUTTON_VT_MORE_KEY = "button_more_expand_key";

    private static final String BUTTON_VT_PEER_REPLACE_KEY = "button_vt_replace_peer_expand_key";
    private static final String BUTTON_VT_ENABLE_PEER_REPLACE_KEY = "button_vt_enable_peer_replace_key";
    private static final String BUTTON_VT_ENABLE_BACK_CAMERA_KEY     = "button_vt_enable_back_camera_key";
    private static final String BUTTON_VT_PEER_BIGGER_KEY     = "button_vt_peer_bigger_key";
    private static final String BUTTON_VT_MO_LOCAL_VIDEO_DISPLAY_KEY     = "button_vt_mo_local_video_display_key";
    private static final String BUTTON_VT_MT_LOCAL_VIDEO_DISPLAY_KEY     = "button_vt_mt_local_video_display_key";
    private static final String BUTTON_VT_AUTO_DROPBACK_KEY = "button_vt_auto_dropback_key";
    private Preference mButtonCf = null;
    private Preference mButtonCb = null;
    private Preference mButtonMore = null;
    private CheckBoxPreference mButtonVTEnablePeerReplace;
    private CheckBoxPreference mButtonVTMoVideo;
    private CheckBoxPreference mButtonVTEnablebackCamer;
    private CheckBoxPreference mButtonVTPeerBigger;
    private CheckBoxPreference mButtonVTAutoDropBack;
    private ListPreference mButtonVTMtVideo;

    private PreCheckForRunning mPreCfr = null;

    private static final String PHOTO_DATE_FORMAT = "'IMG'_yyyyMMdd_HHmmss";
    private static final String FILE_PROVIDER_AUTHORITY = "com.android.phone.files";
    private Uri mCroppedPhotoUri;

    private static void log(String msg) {
        Xlog.d(LOG_TAG, msg);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Xlog.d(LOG_TAG, "[action = " + intent.getAction() + "]");
            String action = intent.getAction();
            Xlog.d(LOG_TAG, "[action = " + action + "]");
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                setScreenEnabled();
            } else if (TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED.equals(action)) {
                setScreenEnabled();
            } else if (TelephonyIntents.ACTION_SIM_INFO_UPDATE.equals(action)) {
                setScreenEnabled();
            }
        }
    };

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.mtk_vt_advanced_setting);
        mPreCfr = new PreCheckForRunning(this);
        mButtonVTReplace = (ListPreference)findPreference(BUTTON_VT_REPLACE_KEY);
        mButtonVTReplace.setOnPreferenceChangeListener(this);
        mButtonVTPeerReplace = (ListPreference)findPreference(BUTTON_VT_PEER_REPLACE_KEY);
        mButtonVTPeerReplace.setOnPreferenceChangeListener(this);
        mButtonCf = (Preference)findPreference(BUTTON_VT_CF_KEY);
        mButtonCb = (Preference)findPreference(BUTTON_VT_CB_KEY);
        mButtonMore = (Preference)findPreference(BUTTON_VT_MORE_KEY);
        mButtonVTEnablePeerReplace = (CheckBoxPreference)findPreference(BUTTON_VT_ENABLE_PEER_REPLACE_KEY);
        mButtonVTEnablePeerReplace.setOnPreferenceChangeListener(this);
        mButtonVTMoVideo = (CheckBoxPreference)findPreference(BUTTON_VT_MO_LOCAL_VIDEO_DISPLAY_KEY);
        mButtonVTMoVideo.setOnPreferenceChangeListener(this);
        mButtonVTMtVideo = (ListPreference)findPreference(BUTTON_VT_MT_LOCAL_VIDEO_DISPLAY_KEY);
        mButtonVTMtVideo.setOnPreferenceChangeListener(this);
        mButtonVTEnablebackCamer = (CheckBoxPreference)findPreference(BUTTON_VT_ENABLE_BACK_CAMERA_KEY);
        mButtonVTEnablebackCamer.setOnPreferenceChangeListener(this);
        mButtonVTPeerBigger = (CheckBoxPreference)findPreference(BUTTON_VT_PEER_BIGGER_KEY);
        mButtonVTPeerBigger.setOnPreferenceChangeListener(this);
        mButtonVTAutoDropBack = (CheckBoxPreference)findPreference(BUTTON_VT_AUTO_DROPBACK_KEY);
        mButtonVTAutoDropBack.setOnPreferenceChangeListener(this);

        findSimId();
        initVTSettings();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        registerReceiver(mReceiver, intentFilter);
        //generate a Uri for Gallery or other activity to fill data.
        mCroppedPhotoUri = generateTempCroppedImageUri(this);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mButtonCf == preference || mButtonCb == preference || mButtonMore == preference) {
            mSlotId = GeminiUtils.getSlotId(this, preference.getTitle().toString(), android.R.style.Theme_Holo_Light_DialogWhenLarge);
            if (GeminiUtils.isValidSlot(mSlotId)) {
                Intent intent = preference.getIntent();
                intent.putExtra("ISVT", true);
                GeminiUtils.startActivity(mSlotId, preference, mPreCfr);
            }
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        Xlog.d(LOG_TAG,"[mSlotId = " + mSlotId + "]");
        Xlog.d(LOG_TAG,"[objValue = " + objValue + "]");
        Xlog.d(LOG_TAG,"[key = " + preference.getKey() + "]");
        if (preference == mButtonVTReplace) {
            mWhichToSave = 0;

            if (objValue.toString().equals(SELECT_DEFAULT_PICTURE)) {
                if (DBG) {
                    log(" Picture for replacing local video -- selected DEFAULT PICTURE");
                }
                showDialogDefaultPic(VTAdvancedSetting.getPicPathDefault());
            }  else if (objValue.toString().equals(SELECT_MY_PICTURE)) {
                if (DBG) {
                    log(" Picture for replacing local video -- selected MY PICTURE");
                }
                showDialogMyPic(VTAdvancedSetting.getPicPathUserselect(mSlotId));
            }
        } else if (preference == mButtonVTPeerReplace) {
            mWhichToSave = 1;
            if (objValue.toString().equals(SELECT_DEFAULT_PICTURE2)) {
                if (DBG) {
                    log(" Picture for replacing peer video -- selected DEFAULT PICTURE");
                }
                showDialogDefaultPic(VTAdvancedSetting.getPicPathDefault2());
            } else if (objValue.toString().equals(SELECT_MY_PICTURE2)) {
                if (DBG) {
                    log(" Picture for replacing peer video -- selected MY PICTURE");
                }
                showDialogMyPic(VTAdvancedSetting.getPicPathUserselect2(mSlotId));
            }
        }
        return true;
    }

    public void onResume() {
        super.onResume();
        setScreenEnabled();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri generateTempCroppedImageUri(Context context) {

        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY,
                new File(pathForTempPhoto(context, generateTempCroppedPhotoFileName())));
    }

    private String pathForTempPhoto(Context context, String fileName) {
        final File dir = context.getCacheDir();
        dir.mkdirs();
        final File f = new File(dir, fileName);
        return f.getAbsolutePath();
    }

    private static String generateTempCroppedPhotoFileName() {
        final Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(PHOTO_DATE_FORMAT, Locale.US);
        return "ContactPhoto-" + dateFormat.format(date) + "-cropped.jpg";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) {
            log("onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        Uri uri;
        if (data != null && data.getData() != null) {
            uri = data.getData();
        }else{
            uri = null;
        }
        switch (requestCode) {
        case REQUESTCODE_PICTRUE_PICKED_WITH_DATA:
            doCropPhoto(uri, mCroppedPhotoUri);
            break;

        case REQUESTCODE_PICTRUE_CROP:
            if (uri == null) {
                uri = mCroppedPhotoUri;
            }
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                if (bitmap != null) {
                    saveBitMap(bitmap);
                    showDialogMyPic();
                } else {
                    Log.e(LOG_TAG, "get crop data, bitmap is null!!!~~");
                }
            }catch (Exception e){
                Log.e(LOG_TAG, "getBitMapException !");
            }
            break;

        default:
            break;
        }
    }

    private void saveBitMap(Bitmap bitmap) {
        try {
            if (bitmap != null) {
                if (mWhichToSave == 0) {
                    VTCallUtils.saveMyBitmap(VTAdvancedSetting.getPicPathUserselect(mSlotId), bitmap);
                } else {
                    VTCallUtils.saveMyBitmap(VTAdvancedSetting.getPicPathUserselect2(mSlotId), bitmap);
                }
                bitmap.recycle();
                if (DBG) {
                    log(" - Bitmap.isRecycled() : " + bitmap.isRecycled());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDialogMyPic() {
        if (mWhichToSave == 0) {
            showDialogMyPic(VTAdvancedSetting.getPicPathUserselect(mSlotId));
        } else {
            showDialogMyPic(VTAdvancedSetting.getPicPathUserselect2(mSlotId));
        }
    }

    /**
     * Sends a newly acquired photo to Gallery for cropping
     */
    private void doCropPhoto(Uri inputUri, Uri outputUri) {
        try {
            // Launch gallery to crop the photo
            Intent intent = getCropImageIntent(inputUri, outputUri);
            try {
                startActivityForResult(intent, REQUESTCODE_PICTRUE_CROP);
            } catch (ActivityNotFoundException e) {
              Log.e(LOG_TAG, "Crop, ActivityNotFoundException !");
            }
        }finally{
        }
    }

    /**
     * Constructs an intent for picking a photo from Gallery, and returning the bitmap.
     */
    private Intent getPhotoPickIntent(Uri outputUri) {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        addPhotoPickerExtras(intent, outputUri);
        return intent;
    }

    /**
     * Constructs an intent for image cropping.
     */
    private Intent getCropImageIntent(Uri inputUri, Uri outputUri) {
        Intent intent = new Intent(ACTION_CROP);
        intent.setDataAndType(inputUri, "image/*");
        addCropExtras(intent);
        addPhotoPickerExtras(intent, outputUri);
        return intent;
    }

    /**
     * Adds common extras to gallery intents.
     *
     * @param intent The intent to add extras to.
     * @param photoUri The uri of the file to save the image to.
     */
    public void addPhotoPickerExtras(Intent intent, Uri photoUri) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri(MediaStore.EXTRA_OUTPUT, photoUri));
    }

    public void addCropExtras(Intent intent) {
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", getResources().getDimensionPixelSize(R.dimen.qcif_x));
        intent.putExtra("outputY", getResources().getDimensionPixelSize(R.dimen.qcif_y));
        intent.putExtra("scaleUpIfNeeded", true);
    }

    private void showDialogDefaultPic(String filename) {
        final ImageView mImg = new ImageView(this);
        final Bitmap mBitmap = BitmapFactory.decodeFile(filename);
        mImg.setImageBitmap(mBitmap);
        LinearLayout linear = new LinearLayout(this);
        linear.addView(mImg, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        linear.setGravity(Gravity.CENTER);

        AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        myBuilder.setView(linear);
        myBuilder.setTitle(R.string.vt_pic_replace_local_default);
        myBuilder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,
                int which) {
            }
        });

        AlertDialog myAlertDialog = myBuilder.create();
        myAlertDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mImg.setImageBitmap(null);
                if (mBitmap != null && !mBitmap.isRecycled()) {
                    mBitmap.recycle();
                }
            }
        });
        myAlertDialog.show();
    }

    private void showDialogMyPic(String filename) {
        final ImageView mImg2 = new ImageView(this);
        final Bitmap mBitmap2 = BitmapFactory.decodeFile(filename);
        mImg2.setImageBitmap(mBitmap2);
        LinearLayout linear = new LinearLayout(this);
        linear.addView(mImg2, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        linear.setGravity(Gravity.CENTER);

        AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        myBuilder.setView(linear);
        myBuilder.setTitle(R.string.vt_pic_replace_local_mypic);
        myBuilder.setPositiveButton(R.string.vt_change_my_pic,
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,
                    int which) {
                // TODO Auto-generated method stub

                try {
                    Intent intent = getPhotoPickIntent(mCroppedPhotoUri);
                    startActivityForResult(intent, REQUESTCODE_PICTRUE_PICKED_WITH_DATA);

                } catch (ActivityNotFoundException e) {
                    if (DBG) {
                        log("get Content, ActivityNotFoundException !");
                    }
                }
                return;
            }
        });
        myBuilder.setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,
                    int which) {
            }
        });

        AlertDialog myAlertDialog = myBuilder.create();
        myAlertDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mImg2.setImageBitmap(null);
                if (!mBitmap2.isRecycled()) {
                    mBitmap2.recycle();
                }
            }
        });
        myAlertDialog.show();
    }

    public static String getPicPathDefault() {
        return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_LOCAL_VIDEO_DEFAULT + ".vt";
    }

    public static String getPicPathUserselect(int slodId) {
        return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_LOCAL_VIDEO_USERSELECT + "_" + slodId + ".vt";
    }

    public static String getPicPathDefault2() {
        return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_PEER_VIDEO_DEFAULT + ".vt";
    }

    public static String getPicPathUserselect2(int slodId) {
        return "/data/data/com.android.phone/" + NAME_PIC_TO_REPLACE_PEER_VIDEO_USERSELECT + "_" + slodId + ".vt";
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mPreCfr != null) {
            mPreCfr.deRegister();
        }
        unregisterReceiver(mReceiver);
    }

    private void setScreenEnabled() {
        if (isMultiSim()) {
            if (DBG) {
                log("setScreenEnabled -- has multi-sim finished.");
            }
            finish();
        }
        List<SimInfoRecord> simList = GeminiUtils.get3GSimCards(this.getApplicationContext());
        if (simList.size() == 1 && simList.get(0).mSimSlotId == mSlotId) {
            boolean isRadioOn = !PhoneWrapper.isRadioOffBySlot(mSlotId, this);
            boolean is3GEnable = mSlotId >= 0;
            mButtonVTReplace.setEnabled(is3GEnable);
            mButtonVTPeerReplace.setEnabled(is3GEnable);
            mButtonVTEnablePeerReplace.setEnabled(is3GEnable);
            mButtonVTMoVideo.setEnabled(is3GEnable);
            mButtonVTMtVideo.setEnabled(is3GEnable);
            mButtonVTEnablebackCamer.setEnabled(is3GEnable);
            mButtonVTPeerBigger.setEnabled(is3GEnable);
            mButtonVTAutoDropBack.setEnabled(is3GEnable);

            mButtonCf.setEnabled(isRadioOn && is3GEnable);
            mButtonCb.setEnabled(isRadioOn && is3GEnable);
            mButtonMore.setEnabled(isRadioOn && is3GEnable);
        } else {
            GeminiUtils.goUpToTopLevelSetting(this, CallSettings.class);
        }
    }

    private void initVTSettings() {
        SharedPreferences sp = 
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (mButtonVTEnablePeerReplace != null) {
            String key = mButtonVTEnablePeerReplace.getKey() + "_" + mSlotId;
            mButtonVTEnablePeerReplace.setKey(key);
            mButtonVTEnablePeerReplace.setChecked(sp.getBoolean(key, true));
        }
        if (mButtonVTMoVideo != null) {
            String key = mButtonVTMoVideo.getKey() + "_" + mSlotId;
            mButtonVTMoVideo.setKey(key);
            mButtonVTMoVideo.setChecked(sp.getBoolean(key, true));
        }
        if (mButtonVTMtVideo != null) {
            String key = mButtonVTMtVideo.getKey() + "_" + mSlotId;
            mButtonVTMtVideo.setKey(key);
            mButtonVTMtVideo.setValue(sp.getString(key, "0"));
        }
        if (mButtonVTEnablebackCamer != null) {
            String key = mButtonVTEnablebackCamer.getKey() + "_" + mSlotId;
            mButtonVTEnablebackCamer.setKey(key);
            mButtonVTEnablebackCamer.setChecked(sp.getBoolean(key, true));
        }
        if (mButtonVTPeerBigger != null) {
            String key = mButtonVTPeerBigger.getKey() + "_" + mSlotId;
            mButtonVTPeerBigger.setKey(key);
            mButtonVTPeerBigger.setChecked(sp.getBoolean(key, true));
        }
        if (mButtonVTAutoDropBack != null) {
            String key = mButtonVTAutoDropBack.getKey() + "_" + mSlotId;
            mButtonVTAutoDropBack.setKey(key);
            mButtonVTAutoDropBack.setChecked(sp.getBoolean(key, false));
        }
        if (mButtonVTReplace != null) {
            String key = mButtonVTReplace.getKey() + "_" + mSlotId;
            mButtonVTReplace.setKey(key);
            mButtonVTReplace.setValue(sp.getString(key, "0"));
        }
        if (mButtonVTPeerReplace != null) {
            String key = mButtonVTPeerReplace.getKey() + "_" + mSlotId;
            mButtonVTPeerReplace.setKey(key);
            mButtonVTPeerReplace.setValue(sp.getString(key, "0"));
        }
    }

    private void findSimId() {
        List<SimInfoRecord> simList = GeminiUtils.get3GSimCards(this.getApplicationContext());
        if (simList.size() == 1) {
            mSlotId = simList.get(0).mSimSlotId;
        } else {
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /// M: ALPS01011728 handle that the checkbox display incorrectly after rotate the screen.
        getListView().clearScrapViewsIfNeeded();
        getListView().requestLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isMultiSim()) {
            finish();
        }
    }

    private boolean isMultiSim() {
        List<SimInfoRecord> simInserted = SimInfoManager.getInsertedSimInfoList(
                getApplicationContext());
        if (simInserted != null && simInserted.size() > 1) {
            return true;
        }
        return false;
    }
}
