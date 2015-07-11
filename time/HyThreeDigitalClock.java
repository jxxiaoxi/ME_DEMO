
package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews.RemoteView;


import com.android.internal.R;

import java.util.Date;

@RemoteView
public class HyThreeDigitalClock extends RelativeLayout {
    private ImageView mTimeMinOne;
    private ImageView mTimeMinTen;
    private ImageView mTimeHourOne;
    private ImageView mTimeHourTen;
    private ImageView mTimeFormat;
    private static boolean mIs24Hour;
    private TextView mDayofweek;
    private TextView mTvDate;
    private RelativeLayout mTimeprivder;
    
    private boolean mAttached;
    private UpBroadcastReceiver mBroadcastReceiver;
    private ContentObserver mFormatChangeObserver;
    private TypedArray mTypedArray;
    private static final int drawableId[] = {
            com.android.internal.R.styleable.HyOneDigitalClock_time_0,
            com.android.internal.R.styleable.HyOneDigitalClock_time_1,
            com.android.internal.R.styleable.HyOneDigitalClock_time_2,
            com.android.internal.R.styleable.HyOneDigitalClock_time_3,
            com.android.internal.R.styleable.HyOneDigitalClock_time_4,
            com.android.internal.R.styleable.HyOneDigitalClock_time_5,
            com.android.internal.R.styleable.HyOneDigitalClock_time_6,
            com.android.internal.R.styleable.HyOneDigitalClock_time_7,
            com.android.internal.R.styleable.HyOneDigitalClock_time_8,
            com.android.internal.R.styleable.HyOneDigitalClock_time_9
    };

    public HyThreeDigitalClock(Context context) {
        // TODO Auto-generated constructor stub
        super(context);
        Log.e("liuwei", "HyOneDigitalClock1");
        initClock(context);
    }

    public HyThreeDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e("liuwei", "HyOneDigitalClock2");
        mTypedArray= context.obtainStyledAttributes(attrs,  
                com.android.internal.R.styleable.HyOneDigitalClock); 

        initClock(context);
    }

    private void initClock(Context context) {

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.hy_threedigitalclock, this, true);
        mTimeprivder = (RelativeLayout) this.findViewById(R.id.timeprivder);
        mTimeprivder.setBackgroundDrawable(mTypedArray.getDrawable(com.android.internal.R.styleable.HyOneDigitalClock_time_background));
        mTimeMinOne = (ImageView) this.findViewById(R.id.time_min_one);
        mTimeMinTen = (ImageView) this.findViewById(R.id.time_min_ten);
        mTimeHourOne = (ImageView) this.findViewById(R.id.time_hour_one);
        mTimeHourTen = (ImageView) this.findViewById(R.id.time_hour_ten);
        mTimeFormat = (ImageView) this.findViewById(R.id.timeformat);
        mDayofweek = (TextView) this.findViewById(R.id.dayofweek);
        mTvDate = (TextView) this.findViewById(R.id.tv_date);
        Log.e("liuwei", "initClock");
        updateDateTime();
    }

    public void updateDateTime() {
        mIs24Hour = android.text.format.DateFormat.is24HourFormat(mContext);
        Time t = new Time();
        t.setToNow();
        int hour = t.hour;
        int min = t.minute;
        mTimeMinOne.setBackgroundDrawable(mTypedArray.getDrawable(drawableId[min % 10]));
        mTimeMinTen.setBackgroundDrawable(mTypedArray.getDrawable(drawableId[min / 10]));
        Log.e("liuwei", "mIs24Hour : " + mIs24Hour);
        if (mIs24Hour) {
            mTimeFormat.setVisibility(View.GONE);
            mTimeHourOne.setBackgroundDrawable(mTypedArray.getDrawable(drawableId[hour % 10]));
            mTimeHourTen.setBackgroundDrawable(mTypedArray.getDrawable(drawableId[hour / 10]));
        } else {
            mTimeFormat.setVisibility(View.VISIBLE);
            if (hour >= 12) {
                hour = (hour == 12) ? hour : hour % 12;
                mTimeFormat.setBackgroundDrawable(mTypedArray.getDrawable(com.android.internal.R.styleable.HyOneDigitalClock_time_pm));
            } else {
                if (hour == 0)
                    hour = 12;
                mTimeFormat.setBackgroundDrawable(mTypedArray.getDrawable(com.android.internal.R.styleable.HyOneDigitalClock_time_am));
            }
            if (hour >= 10) {
                mTimeHourTen.setBackgroundDrawable(mTypedArray.getDrawable(drawableId[1]));
            } else {
                mTimeHourTen.setBackgroundDrawable(mTypedArray.getDrawable(drawableId[0]));
            }
            mTimeHourOne.setBackgroundDrawable(mTypedArray.getDrawable(drawableId[hour  % 10]));
        }

        Date dt = new Date();
        String curDate = (String) DateFormat.getDateFormat(mContext).format(dt);

        mTvDate.setText(curDate);
        mDayofweek.setText(DateFormat.format(mTypedArray
                .getString(com.android.internal.R.styleable.HyOneDigitalClock_day_of_week),
                new Date()));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            mBroadcastReceiver = new UpBroadcastReceiver();
            getContext().registerReceiver(mBroadcastReceiver, filter);
            mFormatChangeObserver = new FormatChangeObserver();
            getContext().getContentResolver().registerContentObserver(
                    Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        }
    }

    public class UpBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            updateDateTime();
        }

    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.e("liuwei", "the system date format change");
            setDateFormat();
        }
    }

    public void setDateFormat() {
        updateDateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mBroadcastReceiver);
            mAttached = false;
        }
    }
}
