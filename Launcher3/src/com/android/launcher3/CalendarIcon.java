package com.android.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.graphics.BitmapFactory;
import java.util.Calendar;

public class CalendarIcon {
    private Context mContext;
    private int daysId[] = {
       R.drawable.ic_calendar_0,
       R.drawable.ic_calendar_1,
       R.drawable.ic_calendar_2,
       R.drawable.ic_calendar_3,
       R.drawable.ic_calendar_4,
       R.drawable.ic_calendar_5,
       R.drawable.ic_calendar_6,
       R.drawable.ic_calendar_7,
       R.drawable.ic_calendar_8,
       R.drawable.ic_calendar_9,
    };
    public CalendarIcon(Context context) {
        mContext = context;
    }

    //得到日期，合成新图标
    private Bitmap createCalendarBitmap(Bitmap src)
    {
        String tag = "createBitmap";
        Log.d(tag, "create a new bitmap");
        if (src == null)
        {
            return null;
        }

        final Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int x = src.getWidth();
        int y = src.getHeight();
        int oneNumX = 0;
        int oneNumY = 0;
        int twoNumX = 0;

        Bitmap newb = Bitmap.createBitmap(x, y, Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(src, 0, 0, null);

        if (day <= 9) {
           Bitmap oneNumBitmap = BitmapFactory.decodeResource(mContext.getResources(),daysId[day]);
           oneNumX = oneNumBitmap.getWidth();
           oneNumY = oneNumBitmap.getHeight();
           cv.drawBitmap(oneNumBitmap, x/2- oneNumX/2 , y/2 - oneNumY/2, null);
           recycled(oneNumBitmap);
        } else {
           Bitmap oneNumBitmap = BitmapFactory.decodeResource(mContext.getResources(),daysId[getDay(day)[0]]);
           Bitmap twoNumBitmap = BitmapFactory.decodeResource(mContext.getResources(),daysId[getDay(day)[1]]);
           oneNumX = oneNumBitmap.getWidth();
           oneNumY = oneNumBitmap.getHeight();
           twoNumX = twoNumBitmap.getWidth();
           cv.drawBitmap(oneNumBitmap,x/2 - oneNumX , y/2 - oneNumY/2, null);
           cv.drawBitmap(twoNumBitmap,x/2 , y/2 - oneNumY/2, null);
           recycled(oneNumBitmap);
           recycled(twoNumBitmap);
        }
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();
        recycled(src);
        return newb;
    }

    private void recycled(Bitmap bitmap){
        if(bitmap != null){
             if(!bitmap.isRecycled()){
            //     bitmap.recycle();
             }
        }
    }

    private int[] getDay(int day){
        return stringToInts(day+"");
    }

    private int[] stringToInts(String s){
       int[] n = new int[s.length()]; 
       for(int i = 0;i<s.length();i++){
         n[i] = Integer.parseInt(s.substring(i,i+1));
       }
       return n;
    }
 

    public Bitmap getDrable() {
        BitmapDrawable background = (BitmapDrawable) mContext.getResources().getDrawable(
                R.drawable.ic_launcher_calendar);
        Bitmap icon = createCalendarBitmap(background.getBitmap());
        return icon;
    }
}
