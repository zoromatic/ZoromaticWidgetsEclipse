package com.zoromatic.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
//import android.location.LocationManager;
import android.util.Log;

public class GpsAppWidgetProvider extends AppWidgetProvider {
	private static final String LOG_TAG = "GPSWidget";
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d(LOG_TAG, "GpsAppWidgetProvider onUpdate");
    	
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        
        Intent startIntent = new Intent(context, WidgetUpdateService.class);
        startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, "android.location.GPS_ENABLED_CHANGE");

        context.startService(startIntent);
    }
};
