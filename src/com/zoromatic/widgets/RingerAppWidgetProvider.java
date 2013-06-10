package com.zoromatic.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class RingerAppWidgetProvider extends AppWidgetProvider {
	private static final String LOG_TAG = "RingerWidget";
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d(LOG_TAG, "RingerAppWidgetProvider onUpdate");
    	
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        
        Intent startIntent = new Intent(context, WidgetUpdateService.class);
        startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, AudioManager.RINGER_MODE_CHANGED_ACTION);

        context.startService(startIntent);
    }
}