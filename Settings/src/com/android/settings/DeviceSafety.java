package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;

public class DeviceSafety extends RestrictedSettingsFragment implements
		OnClickListener {

	private String Imei1;
	private String Imei2;
	private TextView mProductName;
	private TextView mImei;
	private ImageView mSafetyIc1;
	private ImageView mSafetyIc2;
	private ImageView mSafetyIc3;
	private TextView mDeviceSafetyCu;

	public DeviceSafety() {
		super(null /* Don't PIN protect the entire screen */);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.device_safe_info,
				container, false);
		mProductName = (TextView) view
				.findViewById(R.id.device_safety_product_name_summary);
		if (FeatureOption.CUSTOM_ALCATEL_4031D_MM)
		mProductName.setText("T400M");
		else
		mProductName.setText(Build.MODEL);

		mImei = (TextView) view.findViewById(R.id.device_safety_imei_summary);
		mImei.setText(getImei());

		mDeviceSafetyCu = (TextView) view
				.findViewById(R.id.device_safety_cu_summary);
		mDeviceSafetyCu.setText(getCU());
		mSafetyIc1 = (ImageView) view.findViewById(R.id.safety_ic_1);
		mSafetyIc1.setOnClickListener(this);
		mSafetyIc2 = (ImageView) view.findViewById(R.id.safety_ic_2);
		mSafetyIc2.setOnClickListener(this);
		mSafetyIc3 = (ImageView) view.findViewById(R.id.safety_ic_3);
		mSafetyIc3.setOnClickListener(this);

		return view;
	}

	private String getCU() {
		StringBuffer versionBuffer = new StringBuffer();

		IBinder binder = ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		byte[] buff = null;
		try {
			buff = agent.readFile(45);
		} catch (Exception e) {
			e.printStackTrace();
		}
		versionBuffer.append(new String(buff, 10, buff.length - 10));

		String versionStr = versionBuffer.toString();
		return versionStr;
	}

	private String getImei() {
		String imei = null;
		String Imei1 = TelephonyManagerEx.getDefault().getDeviceId(0);
		String Imei2 = TelephonyManagerEx.getDefault().getDeviceId(1);
		imei = Imei1 + "\n" + Imei2;
		return imei;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(), SafetyIconInfo.class);
		switch (v.getId()) {
		case R.id.safety_ic_1:
			intent.putExtra("safety_ic", "safety_ic_1");
			getActivity().startActivity(intent);
			break;
		case R.id.safety_ic_2:
			intent.putExtra("safety_ic", "safety_ic_2");
			getActivity().startActivity(intent);
			break;
		case R.id.safety_ic_3:
			intent.putExtra("safety_ic", "safety_ic_3");
			getActivity().startActivity(intent);
			break;
		default:

			break;
		}
	}
}
