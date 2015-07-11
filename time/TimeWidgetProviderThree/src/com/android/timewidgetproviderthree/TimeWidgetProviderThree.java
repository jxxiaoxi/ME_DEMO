
package com.android.timewidgetproviderthree;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TimeWidgetProviderThree extends BroadcastReceiver {
    public Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                    R.layout.desktop);
            Intent openApp = new Intent();
            openApp.setClassName(context.getString(R.string.package_name),
                    context.getString(R.string.activity_name));
            openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.timeprivder, pendingIntent);
            ComponentName thisWidget = new ComponentName(context, TimeWidgetProviderThree.class);

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, updateViews);
        }
    }

}
