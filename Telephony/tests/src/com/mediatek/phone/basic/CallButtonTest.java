package com.mediatek.phone.basic;

import android.test.InstrumentationTestCase;

import com.android.phone.AutotestEngine;
import com.android.phone.AutotestEngineUtils;
import com.android.phone.CallCommand;
import com.android.phone.ICommand;
import com.mediatek.phone.Utils;
import com.mediatek.phone.annotation.InternalApiAnnotation;

public class CallButtonTest extends InstrumentationTestCase {

    private static final String TAG = "CallButtonTest";
    AutotestEngine mAutotestEngine;

    protected void setUp() throws Exception {
        super.setUp();
        log("setUp start");
        mAutotestEngine = AutotestEngine.makeInstance(getInstrumentation());
        getInstrumentation().waitForIdleSync();
        /// Add a call.
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute(CallCommand.FIRST_CALL_BY_SLOT_1), true);
    };

    @Override
    protected void tearDown() throws Exception {
        log("tearDown");
        // End the call.
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("End 4"));
        super.tearDown();
    }

    @InternalApiAnnotation
    public void test01_mute() throws InterruptedException {
        log("test01_mute");
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Mute"));
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Mute"));
    }

    @InternalApiAnnotation
    public void test02_speaker() throws InterruptedException {
        log("test02_speaker");
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Speaker"));
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Speaker"));
    }

    @InternalApiAnnotation
    public void test03_hold() {
        log("test03_hold");
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Hold"));
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Hold"));
    }

    @InternalApiAnnotation
    public void test04_swap() throws InterruptedException {
        log("test04_swap");
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute(CallCommand.SECOND_CALL_BY_SLOT_1), true);
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Swap"));
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Swap"));
    }

    @InternalApiAnnotation
    public void test05_merge() throws InterruptedException {
        log("test05_merge");
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute(CallCommand.SECOND_CALL_BY_SLOT_1), true);
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Merge"));
    }

    @InternalApiAnnotation
    public void test06_seperate() throws InterruptedException {
        log("test06_seperate");
        AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute(CallCommand.SECOND_CALL_BY_SLOT_1), true);
        int result = mAutotestEngine.execute("Merge");
        AutotestEngineUtils.assertAndWaitSync(result);
        if(result != ICommand.RESULT_COMMAND_NOT_SUPPORT) {
            AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Separate 0"));
            AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Merge"));
            AutotestEngineUtils.assertAndWaitSync(mAutotestEngine.execute("Separate 1"));
        }
    }

    void log(String msg) {
        Utils.log(TAG, msg);
    }

}
