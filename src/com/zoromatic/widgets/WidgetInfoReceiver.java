package com.zoromatic.widgets;

import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


public class WidgetInfoReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = "WidgetInfoReceiver";
	public static final String INTENT_EXTRA = "INTENT_EXTRA";
	public static final String UPDATE_WEATHER = "UPDATE_WEATHER";
	public static final String SCHEDULED_UPDATE = "SCHEDULED_UPDATE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			
			String action = intent.getAction();
			
			Log.d(LOG_TAG, "WidgetInfoReceiver onReceive " + action);
			
			int[] appWidgetIds = null;
			ComponentName thisWidget = null;
			
			Intent startIntent = new Intent(context, WidgetUpdateService.class);
            startIntent.putExtra(INTENT_EXTRA, action);
			
            if (action.equals(WidgetUpdateService.WEATHER_UPDATE)) {
            	int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
    					AppWidgetManager.INVALID_APPWIDGET_ID);
            	
            	boolean scheduledUpdate = intent.getBooleanExtra(WidgetInfoReceiver.SCHEDULED_UPDATE, false);
    			
    			if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) 
    				return;
    			
				startIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				startIntent.putExtra(WidgetInfoReceiver.SCHEDULED_UPDATE, scheduledUpdate);
            }
            else {
            	if (action.equals(Intent.ACTION_TIME_CHANGED) 
                		|| action.equals(Intent.ACTION_TIME_TICK)) {
                	
    				thisWidget = new ComponentName(context,
                			DigitalClockAppWidgetProvider.class);            	            	
                }
            	
            	if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)
                		|| action.equals(WidgetUpdateService.CLOCK_WIDGET_UPDATE)
                		|| action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                	
    				thisWidget = new ComponentName(context,
                			DigitalClockAppWidgetProvider.class); 
    				
    				 startIntent.putExtra(WidgetInfoReceiver.UPDATE_WEATHER, true);
                }
    			
    			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
    				thisWidget = new ComponentName(context,
    						BatteryAppWidgetProvider.class);
    			}

    			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)
    					|| action.equals(WidgetUpdateService.BLUETOOTH_WIDGET_UPDATE)) {
    				thisWidget = new ComponentName(context,
    						BluetoothAppWidgetProvider.class);
    			}

    			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
    					|| action.equals(WidgetUpdateService.WIFI_WIDGET_UPDATE)) {
    				thisWidget = new ComponentName(context,
    						WifiAppWidgetProvider.class);
    			}

    			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
    					|| action.equals(WidgetUpdateService.MOBILE_DATA_WIDGET_UPDATE)) {
    				thisWidget = new ComponentName(context,
    						MobileAppWidgetProvider.class);
    			}

    			if (action.equals("android.location.GPS_ENABLED_CHANGE")
    					|| action.equals(WidgetUpdateService.GPS_WIDGET_UPDATE)) {
    				thisWidget = new ComponentName(context,
    						GpsAppWidgetProvider.class);
    			}
    			
    			if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)
    					|| action.equals(WidgetUpdateService.RINGER_WIDGET_UPDATE)) {
    				thisWidget = new ComponentName(context,
    						RingerAppWidgetProvider.class);
    			}
    			
    			if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)
    					|| action.equals(WidgetUpdateService.AIRPLANE_WIDGET_UPDATE)) {
    				thisWidget = new ComponentName(context,
    						AirplaneAppWidgetProvider.class);
    			}
    			
    			if (action.equals(Intent.ACTION_SCREEN_ON)
    					|| action.equals(WidgetUpdateService.BRIGHTNESS_WIDGET_UPDATE)) {
    				thisWidget = new ComponentName(context,
    						BrightnessAppWidgetProvider.class);
    			}
    			
    			if (thisWidget != null) {
    				appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                	
                	if (appWidgetIds.length <= 0)
                		return;
                	
                	startIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
    			}
            }
            
            context.startService(startIntent);
            
            // refresh weather data if scheduled refresh failed
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            	ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        		
        		if(activeNetworkInfo != null) {
        			thisWidget = new ComponentName(context,
                			DigitalClockAppWidgetProvider.class);
        			
        			appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                	
                	if (appWidgetIds.length <= 0)
                		return;
                	
                	for (int appWidgetId : appWidgetIds) {
                		boolean weatherSuccess = Preferences.getWeatherSuccess(context, appWidgetId);
                		
                		if (!weatherSuccess) {
	                		Intent refreshIntent = new Intent(context, WidgetUpdateService.class);
	                		refreshIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, WidgetUpdateService.WEATHER_UPDATE);
	                		refreshIntent.putExtra(WidgetInfoReceiver.SCHEDULED_UPDATE, true);
	                		refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	
	                        context.startService(refreshIntent);
                        }
                	}
        		}
            }
        }
		catch(Exception e){
			Log.e(LOG_TAG, "", e);
		}

	}

}
