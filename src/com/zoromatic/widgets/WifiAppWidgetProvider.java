package com.zoromatic.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiAppWidgetProvider extends AppWidgetProvider {
	private static final String LOG_TAG = "WiFiWidget";
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d(LOG_TAG, "WifiAppWidgetProvider onUpdate");
    	
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        
        Intent startIntent = new Intent(context, WidgetUpdateService.class);
        startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, WifiManager.WIFI_STATE_CHANGED_ACTION);

        context.startService(startIntent);
    }
};
