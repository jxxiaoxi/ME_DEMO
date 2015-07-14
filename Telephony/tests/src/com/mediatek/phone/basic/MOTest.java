package com.mediatek.phone.basic;

import android.test.InstrumentationTestCase;

import com.android.phone.AutotestEngine;
import com.android.phone.AutotestEngineUtils;
import com.android.phone.CallCommand;
import com.mediatek.phone.Utils;
import com.mediatek.phone.annotation.InternalApiAnnotation;

public class MOTest extends InstrumentationTestCase {

    private static final String TAG = "MOTest";
    AutotestEngine mAutotestEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log("setUp");
        mAutotestEngine = AutotestEngine.makeInstance(getInstrumentation());
        getInstrumentation().waitForIdleSync();
    };

    @Override
    protected void tearDown() throws Exception {
        log("tearDown");
        super.tearDown();
        // End the call.
        mAutotestEngine.execute("End 4");
    }

    @InternalApiAnnotation
    public void test01_placeCall() throws Exception {
        log("test01_callBySlot0");
        int result = mAutotestEngine.execute(CallCommand.FIRST_CALL_BY_SLOT_0);
        AutotestEngineUtils.assertAndWaitSync(result, true);
    }

    @InternalApiAnnotation
    public void test02_EmergencyCall() throws Exception {
        log("test02_callEmergency");
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Airplane"));
        int result = mAutotestEngine.execute("EmergencyCall " + "112" + " 0");
        AutotestEngineUtils.assertAndWaitSync(result);
    }

    void log(String msg) {
        Utils.log(TAG, msg);
    }
}
