package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.mediatek.common.featureoption.FeatureOption;

public class SafetyIconInfo extends Activity {
	private TextView mInfoTextView;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String safetyIc = (String) intent.getExtra("safety_ic", "safety_ic_1");
		setContentView(R.layout.device_safety_ic_info);
		mInfoTextView = (TextView) findViewById(R.id.safety_ic);
		mWebView = (WebView) findViewById(R.id.safety_ic_web);
		initActionBar(safetyIc);
	}

	private void initActionBar(String safetyIc) {
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		if ("safety_ic_1".equals(safetyIc)) {
			mInfoTextView.setText(getString(R.string.device_safety_sound_info));
			mInfoTextView.setVisibility(View.VISIBLE);
			actionBar.setTitle(getString(R.string.device_safety_sound));

		} else if ("safety_ic_2".equals(safetyIc)) {
			actionBar.setTitle(getString(R.string.device_safety_weee));
			mWebView.setVisibility(View.VISIBLE);
			setWebViewContent1();
		} else {
			actionBar.setTitle(getString(R.string.device_safety_sar));
			mWebView.setVisibility(View.VISIBLE);
			setWebViewContent2();
		}
	}

	private void setWebViewContent1() {
		WebSettings wSet = mWebView.getSettings();
		wSet.setJavaScriptEnabled(true);
		Log.e("liuwei", "getLocalLanguage :     " + getLocalLanguage());
		if (getLocalLanguage().equals("US")) {
			mWebView.loadUrl("file:///android_asset/wee_en_us.html");
		} else if (getLocalLanguage().equals("GB")) {
			mWebView.loadUrl("file:///android_asset/wee_en_uk.html");
		} else if (getLocalLanguage().equals("CN")) {
			mWebView.loadUrl("file:///android_asset/wee_zh_rcn.html");
		} else if (getLocalLanguage().equals("TW")) {
			mWebView.loadUrl("file:///android_asset/wee_zh_rtw.html");
		} else if (getLocalLanguage().equals("EG")
				|| getLocalLanguage().equals("IL")) {// 阿拉伯语
			mWebView.loadUrl("file:///android_asset/wee_ar.html");
		} else if (getLocalLanguage().equals("HR")) {// 克罗地亚语
			mWebView.loadUrl("file:///android_asset/wee_hr.html");
		} else if (getLocalLanguage().equals("CZ")) {// 捷克语
			mWebView.loadUrl("file:///android_asset/wee_cs.html");
		} else if (getLocalLanguage().equals("DK")) {// 丹麦语
			mWebView.loadUrl("file:///android_asset/wee_da.html");
		} else if (getLocalLanguage().equals("BE")
				|| getLocalLanguage().equals("NL")) {// 荷兰
			mWebView.loadUrl("file:///android_asset/wee_nl.html");
		} else if (getLocalLanguage().equals("AF")
				|| getLocalLanguage().equals("IR")) {// 波斯语
			mWebView.loadUrl("file:///android_asset/wee_fa.html");
		} else if (getLocalLanguage().equals("BE")
				|| getLocalLanguage().equals("CA")
				|| getLocalLanguage().equals("CH")
				|| getLocalLanguage().equals("FR")) {// 法语
			mWebView.loadUrl("file:///android_asset/wee_fr.html");
		} else if (getLocalLanguage().equals("FI")) {// 芬兰
			mWebView.loadUrl("file:///android_asset/wee_fi.html");
		} else if (getLocalLanguage().equals("AT")
				|| getLocalLanguage().equals("CH")
				|| getLocalLanguage().equals("DE")
				|| getLocalLanguage().equals("LI")) {// 德语
			mWebView.loadUrl("file:///android_asset/wee_de.html");
		} else if (getLocalLanguage().equals("GR")) {// 希腊
			mWebView.loadUrl("file:///android_asset/wee_el.html");
		} else if (getLocalLanguage().equals("IL")) {// 希伯来文
			mWebView.loadUrl("file:///android_asset/wee_he.html");
		} else if (getLocalLanguage().equals("HU")) {// 匈牙利
			mWebView.loadUrl("file:///android_asset/wee_hu.html");
		} else if (getLocalLanguage().equals("ID")) {// 印尼
			mWebView.loadUrl("file:///android_asset/wee_in.html");
		} else if (getLocalLanguage().equals("CH")
				|| getLocalLanguage().equals("IT")) {// 意大利
			mWebView.loadUrl("file:///android_asset/wee_it.html");
		} else if (getLocalLanguage().equals("JP")) {// 日本
			mWebView.loadUrl("file:///android_asset/wee_ja.html");
		} else if (getLocalLanguage().equals("AZ")) {// 拉丁
			mWebView.loadUrl("file:///android_asset/wee_az.html");
		} else if (getLocalLanguage().equals("MY")) {// 马来语
			mWebView.loadUrl("file:///android_asset/wee_ms.html");
		} else if (getLocalLanguage().equals("PL")) {// 波兰
			mWebView.loadUrl("file:///android_asset/wee_pl.html");
		} else if (getLocalLanguage().equals("BR")
				|| getLocalLanguage().equals("PT")) {// 葡萄牙语
			mWebView.loadUrl("file:///android_asset/wee_pt.html");
		} else if (getLocalLanguage().equals("RO")) {// 罗马尼亚语
			mWebView.loadUrl("file:///android_asset/wee_ro.html");
		} else if (getLocalLanguage().equals("RU")) {// 俄罗斯
			mWebView.loadUrl("file:///android_asset/wee_ru.html");
		} else if (getLocalLanguage().equals("BA")
				|| getLocalLanguage().equals("SP")) {// 塞尔维亚
			mWebView.loadUrl("file:///android_asset/wee_sr.html");
		} else if (getLocalLanguage().equals("SK")) {// 斯洛伐克
			mWebView.loadUrl("file:///android_asset/wee_sk.html");
		} else if (getLocalLanguage().equals("SI")) {// 斯洛文尼亚语
			mWebView.loadUrl("file:///android_asset/wee_sl.html");
		} else if (getLocalLanguage().equals("ES")
				|| getLocalLanguage().equals("US")) {// 西班牙语
			mWebView.loadUrl("file:///android_asset/wee_es.html");
		} else if (getLocalLanguage().equals("SE")) {// 瑞典语
			mWebView.loadUrl("file:///android_asset/wee_sv.html");
		} else if (getLocalLanguage().equals("TH")) {// 泰国语
			mWebView.loadUrl("file:///android_asset/wee_th.html");
		} else if (getLocalLanguage().equals("TR")) {// 土耳其语
			mWebView.loadUrl("file:///android_asset/wee_tr.html");
		} else if (getLocalLanguage().equals("VN")) {// 越南语
			mWebView.loadUrl("file:///android_asset/wee_vi.html");
		} else if (getLocalLanguage().equals("UA")) {// 乌克兰语
			mWebView.loadUrl("file:///android_asset/wee_uk.html");
		} else if (getLocalLanguage().equals("KH")) {// 柬埔寨语
			mWebView.loadUrl("file:///android_asset/wee_km.html");
		} else if (getLocalLanguage().equals("MM")) {// 缅甸语语
			mWebView.loadUrl("file:///android_asset/wee_my.html");
		} else if (getLocalLanguage().equals("BD")) {// 孟加拉语
			mWebView.loadUrl("file:///android_asset/wee_bn.html");
		} else if (getLocalLanguage().equals("BG")) {// 保加利亚语
			mWebView.loadUrl("file:///android_asset/wee_bg.html");
		} else if (getLocalLanguage().equals("NO")) {// 挪威语
			mWebView.loadUrl("file:///android_asset/wee_nb.html");
		} else {
			Log.e("liuwei", "default    ");
			mWebView.loadUrl("file:///android_asset/wee_en_us.html");
		}
	}

	private void setWebViewContent2() {
		Log.e("liuwei", "getLocalLanguage :     " + getLocalLanguage());
		WebSettings wSet = mWebView.getSettings();
		wSet.setJavaScriptEnabled(true);
		//add fengw
		if(FeatureOption.CUSTOM_ALCATEL_4031D_IN)
		{
		    mWebView.loadUrl("file:///android_asset/sar_en_us.html");
		    return;
		}
		if (getLocalLanguage().equals("US")) {
			mWebView.loadUrl("file:///android_asset/sar_en_us.html");
		} else if (getLocalLanguage().equals("GB")) {
			mWebView.loadUrl("file:///android_asset/sar_en_uk.html");
		} else if (getLocalLanguage().equals("CN")) {
			mWebView.loadUrl("file:///android_asset/sar_zh_rcn.html");
		} else if (getLocalLanguage().equals("TW")) {
			mWebView.loadUrl("file:///android_asset/sar_zh_rtw.html");
		} else if (getLocalLanguage().equals("EG")
				|| getLocalLanguage().equals("IL")) {// 阿拉伯语
			mWebView.loadUrl("file:///android_asset/sar_ar.html");
		} else if (getLocalLanguage().equals("HR")) {// 克罗地亚语
			mWebView.loadUrl("file:///android_asset/sar_hr.html");
		} else if (getLocalLanguage().equals("CZ")) {// 捷克语
			mWebView.loadUrl("file:///android_asset/sar_cs.html");
		} else if (getLocalLanguage().equals("DK")) {// 丹麦语
			mWebView.loadUrl("file:///android_asset/sar_da.html");
		} else if (getLocalLanguage().equals("BE")
				|| getLocalLanguage().equals("NL")) {// 荷兰
			mWebView.loadUrl("file:///android_asset/sar_nl.html");
		} else if (getLocalLanguage().equals("AF")
				|| getLocalLanguage().equals("IR")) {// 波斯语
			mWebView.loadUrl("file:///android_asset/sar_fa.html");
		} else if (getLocalLanguage().equals("BE")
				|| getLocalLanguage().equals("CA")
				|| getLocalLanguage().equals("CH")
				|| getLocalLanguage().equals("FR")) {// 法语
			mWebView.loadUrl("file:///android_asset/sar_fr.html");
		} else if (getLocalLanguage().equals("FI")) {// 芬兰
			mWebView.loadUrl("file:///android_asset/sar_fi.html");
		} else if (getLocalLanguage().equals("AT")
				|| getLocalLanguage().equals("CH")
				|| getLocalLanguage().equals("DE")
				|| getLocalLanguage().equals("LI")) {// 德语
			mWebView.loadUrl("file:///android_asset/sar_de.html");
		} else if (getLocalLanguage().equals("GR")) {// 希腊
			mWebView.loadUrl("file:///android_asset/sar_el.html");
		} else if (getLocalLanguage().equals("IL")) {// 希伯来文
			mWebView.loadUrl("file:///android_asset/sar_he.html");
		} else if (getLocalLanguage().equals("HU")) {// 匈牙利
			mWebView.loadUrl("file:///android_asset/sar_hu.html");
		} else if (getLocalLanguage().equals("ID")) {// 印尼
			mWebView.loadUrl("file:///android_asset/sar_in.html");
		} else if (getLocalLanguage().equals("CH")
				|| getLocalLanguage().equals("IT")) {// 意大利
			mWebView.loadUrl("file:///android_asset/sar_it.html");
		} else if (getLocalLanguage().equals("JP")) {// 日本
			mWebView.loadUrl("file:///android_asset/sar_ja.html");
		} else if (getLocalLanguage().equals("AZ")) {// 拉丁
			mWebView.loadUrl("file:///android_asset/sar_az.html");
		} else if (getLocalLanguage().equals("MY")) {// 马来语
			mWebView.loadUrl("file:///android_asset/sar_ms.html");
		} else if (getLocalLanguage().equals("PL")) {// 波兰
			mWebView.loadUrl("file:///android_asset/sar_pl.html");
		} else if (getLocalLanguage().equals("BR")
				|| getLocalLanguage().equals("PT")) {// 葡萄牙语
			mWebView.loadUrl("file:///android_asset/sar_pt.html");
		} else if (getLocalLanguage().equals("RO")) {// 罗马尼亚语
			mWebView.loadUrl("file:///android_asset/sar_ro.html");
		} else if (getLocalLanguage().equals("RU")) {// 俄罗斯
			mWebView.loadUrl("file:///android_asset/sar_ru.html");
		} else if (getLocalLanguage().equals("BA")
				|| getLocalLanguage().equals("SP")) {// 塞尔维亚
			mWebView.loadUrl("file:///android_asset/sar_sr.html");
		} else if (getLocalLanguage().equals("SK")) {// 斯洛伐克
			mWebView.loadUrl("file:///android_asset/sar_sk.html");
		} else if (getLocalLanguage().equals("SI")) {// 斯洛文尼亚语
			mWebView.loadUrl("file:///android_asset/sar_sl.html");
		} else if (getLocalLanguage().equals("ES")
				|| getLocalLanguage().equals("US")) {// 西班牙语
			mWebView.loadUrl("file:///android_asset/sar_es.html");
		} else if (getLocalLanguage().equals("SE")) {// 瑞典语
			mWebView.loadUrl("file:///android_asset/sar_sv.html");
		} else if (getLocalLanguage().equals("TH")) {// 泰国语
			mWebView.loadUrl("file:///android_asset/sar_th.html");
		} else if (getLocalLanguage().equals("TR")) {// 土耳其语
			mWebView.loadUrl("file:///android_asset/sar_tr.html");
		} else if (getLocalLanguage().equals("VN")) {// 越南语
			mWebView.loadUrl("file:///android_asset/sar_vi.html");
		} else if (getLocalLanguage().equals("UA")) {// 乌克兰语
			mWebView.loadUrl("file:///android_asset/sar_uk.html");
		} else if (getLocalLanguage().equals("KH")) {// 柬埔寨语
			mWebView.loadUrl("file:///android_asset/sar_km.html");
		} else if (getLocalLanguage().equals("MM")) {// 缅甸语语
			mWebView.loadUrl("file:///android_asset/sar_my.html");
		} else if (getLocalLanguage().equals("BD")) {// 孟加拉语
			mWebView.loadUrl("file:///android_asset/sar_bn.html");
		} else if (getLocalLanguage().equals("BG")) {// 保加利亚语
			mWebView.loadUrl("file:///android_asset/sar_bg.html");
		} else if (getLocalLanguage().equals("NO")) {// 挪威语
			mWebView.loadUrl("file:///android_asset/sar_nb.html");
		} else {
			Log.e("liuwei", "default    ");
			mWebView.loadUrl("file:///android_asset/sar_en_us.html");
		}
	}

	public String getLocalLanguage() {
		return getResources().getConfiguration().locale.getCountry();
	}
}
