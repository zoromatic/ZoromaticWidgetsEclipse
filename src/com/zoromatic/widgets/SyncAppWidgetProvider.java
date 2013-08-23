package com.zoromatic.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SyncAppWidgetProvider extends AppWidgetProvider {
	private static final String LOG_TAG = "SyncWidget";
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d(LOG_TAG, "SyncAppWidgetProvider onUpdate");
    	
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        
        Intent startIntent = new Intent(context, WidgetUpdateService.class);
        startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, "com.android.sync.SYNC_CONN_STATUS_CHANGED");

        context.startService(startIntent);
    }
};
