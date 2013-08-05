package com.zoromatic.widgets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.zoromatic.sunrisesunset.SunriseSunsetCalculator;
import com.zoromatic.sunrisesunset.dto.SunriseSunsetLocation;
import com.zoromatic.widgets.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
//import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@SuppressLint("SimpleDateFormat")
@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class WidgetUpdateService extends Service {
	private static String LOG_TAG = "WidgetUpdateService";
	public static String BLUETOOTH_WIDGET_UPDATE = "com.zoromatic.widgets.BLUETOOTH_WIDGET_UPDATE";
	public static String WIFI_WIDGET_UPDATE = "com.zoromatic.widgets.WIFI_WIDGET_UPDATE";
	public static String MOBILE_DATA_WIDGET_UPDATE = "com.zoromatic.widgets.MOBILE_DATA_WIDGET_UPDATE";
	public static String GPS_WIDGET_UPDATE = "com.zoromatic.widgets.GPS_WIDGET_UPDATE";
	public static String RINGER_WIDGET_UPDATE = "com.zoromatic.widgets.RINGER_WIDGET_UPDATE";
	public static String CLOCK_WIDGET_UPDATE = "com.zoromatic.widgets.CLOCK_WIDGET_UPDATE";
	public static String WEATHER_UPDATE = "com.zoromatic.widgets.WEATHER_UPDATE";
	public static String AIRPLANE_WIDGET_UPDATE = "com.zoromatic.widgets.AIRPLANE_WIDGET_UPDATE";
	public static String BRIGHTNESS_WIDGET_UPDATE = "com.zoromatic.widgets.BRIGHTNESS_WIDGET_UPDATE";

	protected static long GPS_UPDATE_TIME_INTERVAL = 3000; // milliseconds
	protected static float GPS_UPDATE_DISTANCE_INTERVAL = 0; // meters
	private LocationManager mlocManager = null;
	private WidgetGPSListener mGpsListener = null;
	private WidgetLocationListener mlocListener = null;
	
	private static Notification notification;
	private static NotificationManager notificationManager;

	private static IntentFilter mIntentFilter;
	private WidgetInfoReceiver mWidgetInfo = null;
	
	//public static String WEATHER_SERVICE_COORD_URL = "http://openweathermap.org/data/2.1/find/city?lat=%f&lon=%f&APPID=364a27c67e53df61c49db6e5bdf26aa5";
	public static String WEATHER_SERVICE_COORD_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&APPID=364a27c67e53df61c49db6e5bdf26aa5";
	//public static String WEATHER_SERVICE_ID_URL = "http://api.openweathermap.org/data/2.1/weather/city/%d?type=json&APPID=364a27c67e53df61c49db6e5bdf26aa5";
	public static String WEATHER_SERVICE_ID_URL = "http://api.openweathermap.org/data/2.5/weather?id=%d&APPID=364a27c67e53df61c49db6e5bdf26aa5";
	//public static String WEATHER_FORECAST_ID_URL = "http://api.openweathermap.org/data/2.1/forecast/city/%d?mode=daily_compact&APPID=364a27c67e53df61c49db6e5bdf26aa5";
	public static String WEATHER_FORECAST_COORD_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=%f&lon=%f&cnt=7&APPID=364a27c67e53df61c49db6e5bdf26aa5";
	public static String WEATHER_FORECAST_ID_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?id=%d&cnt=7&APPID=364a27c67e53df61c49db6e5bdf26aa5";

	static {

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mIntentFilter.addAction("android.location.GPS_ENABLED_CHANGE");
		mIntentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
		mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
		mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
		mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);

		mIntentFilter.addAction(BLUETOOTH_WIDGET_UPDATE);
		mIntentFilter.addAction(WIFI_WIDGET_UPDATE);
		mIntentFilter.addAction(MOBILE_DATA_WIDGET_UPDATE);
		mIntentFilter.addAction(GPS_WIDGET_UPDATE);
		mIntentFilter.addAction(RINGER_WIDGET_UPDATE);
		mIntentFilter.addAction(CLOCK_WIDGET_UPDATE);
		mIntentFilter.addAction(WEATHER_UPDATE);
		mIntentFilter.addAction(AIRPLANE_WIDGET_UPDATE);
		mIntentFilter.addAction(BRIGHTNESS_WIDGET_UPDATE);
	}

	private class WidgetGPSListener implements GpsStatus.Listener {
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:

				break;
			case GpsStatus.GPS_EVENT_STARTED:

				break;
			case GpsStatus.GPS_EVENT_STOPPED:

				break;
			}
		}
	}

	public class WidgetLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {

		}

		@Override
		public void onProviderDisabled(String provider) {
			
			sendBroadcast(new Intent("android.location.GPS_ENABLED_CHANGE"));
		}

		@Override
		public void onProviderEnabled(String provider) {
			
			sendBroadcast(new Intent("android.location.GPS_ENABLED_CHANGE"));
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (mWidgetInfo != null)
				unregisterReceiver(mWidgetInfo);
		} catch (Exception e) {
			Log.e(LOG_TAG, "", e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "WidgetUpdateService onStartCommand");
		
		if (intent == null)
			return START_STICKY;

		mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		if (mWidgetInfo == null && mGpsListener == null && mlocListener == null) {
			
			mWidgetInfo = new WidgetInfoReceiver();
			registerReceiver(mWidgetInfo, mIntentFilter);
			
			if (mlocManager != null) {
				mGpsListener = new WidgetGPSListener();
				mlocListener = new WidgetLocationListener();
				
				mlocManager.addGpsStatusListener(mGpsListener);
				mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL,
						mlocListener);
			}
			
			// Skip double update processing.
			//return START_STICKY;
		}	
		
		Bundle extras = intent.getExtras();

		if (extras == null)
			return START_STICKY;

		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);
		boolean updateWeather = extras.getBoolean(WidgetInfoReceiver.UPDATE_WEATHER, false);
		boolean scheduledUpdate = intent.getBooleanExtra(WidgetInfoReceiver.SCHEDULED_UPDATE, false);

//		String action = intent.getAction();
//		if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
//			AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
//			int[] allWidgetIds = null;
//			ComponentName widget = new ComponentName(this,
//        			DigitalClockAppWidgetProvider.class);
//			
//			if (widget != null) {
//				allWidgetIds = widgetManager.getAppWidgetIds(widget);
//            	
//            	if (allWidgetIds.length > 0) {
//            		for (int widgetId : allWidgetIds) {
//    					
//    					RemoteViews remoteViews = buildClockUpdate(widgetId);    					
//    					updateWeatherStatus(remoteViews, widgetId, false);    					
//    					widgetManager.updateAppWidget(widgetId, remoteViews);
//    				}            		
//            	}			
//			}
//		}
		
		if (intentExtra.equals(Intent.ACTION_BATTERY_CHANGED))
			updateNotificationBatteryStatus(intent);
		
		if (intentExtra.equals(Intent.ACTION_TIME_CHANGED) 
				|| intentExtra.equals(Intent.ACTION_TIME_TICK)
				|| intentExtra.equals(Intent.ACTION_TIMEZONE_CHANGED)
				|| intentExtra.equals(CLOCK_WIDGET_UPDATE)
				|| intentExtra.equals(Intent.ACTION_CONFIGURATION_CHANGED)
				|| intentExtra.equals(WEATHER_UPDATE)) {
			
			int appWidgetIds[] = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			int appWidgetIdSingle = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
			
			//AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
			
			if (appWidgetIds != null) {
				for (int appWidgetId : appWidgetIds) {
					
					RemoteViews updateViews = buildClockUpdate(appWidgetId);
					
					if (intentExtra.equals(WEATHER_UPDATE))
						updateWeatherStatus(updateViews, appWidgetId, scheduledUpdate);					
					else
						updateClockStatus(updateViews, appWidgetId, updateWeather);					
					
					//appWidgetManager.updateAppWidget(appWidgetId, updateViews);	
					
				}
			}
			else {
				if (appWidgetIdSingle != AppWidgetManager.INVALID_APPWIDGET_ID) {
					RemoteViews updateViews = buildClockUpdate(appWidgetIdSingle);
					
					if (intentExtra.equals(WEATHER_UPDATE))
						updateWeatherStatus(updateViews, appWidgetIdSingle, scheduledUpdate);
					else
						updateClockStatus(updateViews, appWidgetIdSingle, updateWeather);					
					
					//appWidgetManager.updateAppWidget(appWidgetIdSingle, updateViews);
				}				
			}
		}
		else {
			RemoteViews updateViews = buildUpdate(intent);
	
			if (updateViews != null) {
				
				try {
					// Push update for this widget to the home screen								
					ComponentName thisWidget = null;
	
					if (intentExtra.equals(Intent.ACTION_BATTERY_CHANGED)) {
						thisWidget = new ComponentName(this,
								BatteryAppWidgetProvider.class);
					}
	
					if (intentExtra.equals(BluetoothAdapter.ACTION_STATE_CHANGED)
							|| intentExtra.equals(BLUETOOTH_WIDGET_UPDATE)) {
						thisWidget = new ComponentName(this,
								BluetoothAppWidgetProvider.class);
					}
	
					if (intentExtra.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
							|| intentExtra.equals(WIFI_WIDGET_UPDATE)) {
						thisWidget = new ComponentName(this,
								WifiAppWidgetProvider.class);
					}
	
					if (intentExtra.equals(ConnectivityManager.CONNECTIVITY_ACTION)
							|| intentExtra.equals(MOBILE_DATA_WIDGET_UPDATE)) {
						thisWidget = new ComponentName(this,
								MobileAppWidgetProvider.class);
					}
	
					if (intentExtra.equals("android.location.GPS_ENABLED_CHANGE")
							|| intentExtra.equals(GPS_WIDGET_UPDATE)) {
						thisWidget = new ComponentName(this,
								GpsAppWidgetProvider.class);
					}
					
					if (intentExtra.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)
							|| intentExtra.equals(RINGER_WIDGET_UPDATE)) {
						thisWidget = new ComponentName(this,
								RingerAppWidgetProvider.class);
					}
					
					if (intentExtra.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)
							|| intentExtra.equals(AIRPLANE_WIDGET_UPDATE)) {
						thisWidget = new ComponentName(this,
								AirplaneAppWidgetProvider.class);
					}
					
					if (intentExtra.equals(Intent.ACTION_SCREEN_ON)
							|| intentExtra.equals(BRIGHTNESS_WIDGET_UPDATE)) {
						thisWidget = new ComponentName(this,
								BrightnessAppWidgetProvider.class);
					}
					
					if (thisWidget != null) {
	
						AppWidgetManager appWidgetManager = AppWidgetManager
								.getInstance(this);
	
						if (appWidgetManager != null && updateViews != null) {
	
							appWidgetManager.updateAppWidget(thisWidget,
									updateViews);
						}
					}
					
					// stop the service, clear up memory - can't do this, need the
					// Broadcast Receiver running
					// stopSelf();
				} catch (Exception e) {
					Log.e(LOG_TAG, "", e);
				}
			}			
		}
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public RemoteViews buildUpdate(Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService buildUpdate");

		// Build an update that holds the updated widget contents

		Bundle extras = intent.getExtras();

		if (extras == null)
			return null;

		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);
		
		RemoteViews updateViews = null;

		if (intentExtra.equals(Intent.ACTION_BATTERY_CHANGED)) {
			updateViews = new RemoteViews(getPackageName(),
					R.layout.batterywidget);

			try {
				updateBatteryStatus(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}

			try {
				Intent powerUsageIntent = new Intent(
						Intent.ACTION_POWER_USAGE_SUMMARY);
				ResolveInfo resolveInfo = getPackageManager().resolveActivity(
						powerUsageIntent, PackageManager.MATCH_DEFAULT_ONLY);

				if (resolveInfo != null) {
					PendingIntent pendingIntent = PendingIntent.getActivity(
							this, 0, powerUsageIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);
					updateViews.setOnClickPendingIntent(R.id.batteryWidget,
							pendingIntent);
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}

		if (intentExtra.equals(BluetoothAdapter.ACTION_STATE_CHANGED)
				|| intentExtra.equals(BLUETOOTH_WIDGET_UPDATE)) {
			updateViews = new RemoteViews(getPackageName(),
					R.layout.bluetoothwidget);

			try {
				updateBluetoothStatus(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}

			try {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
						0, new Intent(BLUETOOTH_WIDGET_UPDATE),
						PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.bluetoothWidget,
						pendingIntent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}

		if (intentExtra.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
				|| intentExtra.equals(WIFI_WIDGET_UPDATE)) {
			updateViews = new RemoteViews(getPackageName(), R.layout.wifiwidget);

			try {
				updateWifiStatus(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}

			try {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
						0, new Intent(WIFI_WIDGET_UPDATE),
						PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.wifiWidget,
						pendingIntent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}

		if (intentExtra.equals(ConnectivityManager.CONNECTIVITY_ACTION)
				|| intentExtra.equals(MOBILE_DATA_WIDGET_UPDATE)) {

			updateViews = new RemoteViews(getPackageName(),
					R.layout.mobilewidget);

			try {
				updateDataStatus(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}

			try {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
						0, new Intent(MOBILE_DATA_WIDGET_UPDATE),
						PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.mobileWidget,
						pendingIntent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}

		if (intentExtra.equals("android.location.GPS_ENABLED_CHANGE")
				|| intentExtra.equals(GPS_WIDGET_UPDATE)) {
			updateViews = new RemoteViews(getPackageName(), R.layout.gpswidget);

			try {
				updateGpsStatus(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}

			if (canToggleGPS()) {
				try {
					PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
							0, new Intent(GPS_WIDGET_UPDATE),
							PendingIntent.FLAG_UPDATE_CURRENT);
					updateViews.setOnClickPendingIntent(R.id.gpsWidget,
							pendingIntent);
				} catch (Exception e) {
					Log.e(LOG_TAG, "", e);
				}
			} else {
				Intent locationIntent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				ResolveInfo resolveInfo = getPackageManager().resolveActivity(
						locationIntent, PackageManager.MATCH_DEFAULT_ONLY);
				PendingIntent pendingIntent;

				if (resolveInfo != null) {
					pendingIntent = PendingIntent.getActivity(this, 0,
							locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					updateViews.setOnClickPendingIntent(R.id.gpsWidget,
							pendingIntent);					
				} else {
					pendingIntent = null;
				}
			}
		}
		
		if (intentExtra.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)
				|| intentExtra.equals(RINGER_WIDGET_UPDATE)) {
			updateViews = new RemoteViews(getPackageName(),
					R.layout.ringerwidget);

			try {
				updateRingerStatus(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}

			try {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
						0, new Intent(RINGER_WIDGET_UPDATE),
						PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.ringerWidget,
						pendingIntent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}	
		
		if (intentExtra.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)
				|| intentExtra.equals(AIRPLANE_WIDGET_UPDATE)) {
			updateViews = new RemoteViews(getPackageName(), R.layout.airplanewidget);

			try {
				updateAirplaneMode(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}

			try {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
						0, new Intent(AIRPLANE_WIDGET_UPDATE),
						PendingIntent.FLAG_UPDATE_CURRENT);
				updateViews.setOnClickPendingIntent(R.id.airplaneWidget,
						pendingIntent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}
		
		if (intentExtra.equals(Intent.ACTION_SCREEN_ON)
				|| intentExtra.equals(BRIGHTNESS_WIDGET_UPDATE)) {
			updateViews = new RemoteViews(getPackageName(), R.layout.brightnesswidget);
			
			try {
				updateBrightness(updateViews, intent);
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}
			
			try {
				Intent brightnessIntent = new Intent(this, ScreenBrightnessActivity.class);
				
			    PendingIntent pendingBrightnessIntent = PendingIntent.getActivity(this,
						0, brightnessIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				
			    updateViews.setOnClickPendingIntent(R.id.brightnessWidget, pendingBrightnessIntent);			
			} catch (Exception e) {
				Log.e(LOG_TAG, "", e);
			}    
		}
		
		return updateViews;
	}
	
	public RemoteViews buildClockUpdate(int appWidgetId) {
		Log.d(LOG_TAG, "WidgetUpdateService buildClockUpdate");		
	
		RemoteViews updateViews = new RemoteViews(getPackageName(),
				R.layout.digitalclockwidget);

		Intent intent = new Intent(this, DigitalClockAppWidgetPreferences.class);

		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.clockWidget, pendingIntent);
		
		{
			Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

		    // Verify clock implementation
		    String clockImpls[][] = {
		    		{"Standard Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
		    		{"Standard Desk Clock", "com.android.deskclock", "com.android.deskclock.DeskClock"},	
		    		{"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
		    		{"Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
	                {"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"}
		    };
		    
		    boolean foundClockImpl = false;

		    for(int i=0; i<clockImpls.length; i++) {
		        String packageName = clockImpls[i][1];
		        String className = clockImpls[i][2];
		        try {
		            ComponentName cn = new ComponentName(packageName, className);
		            getPackageManager().getActivityInfo(cn, PackageManager.GET_META_DATA);
		            alarmClockIntent.setComponent(cn);
		            foundClockImpl = true;
		            break;
		        } catch (NameNotFoundException nf) {
		        	continue;
		        }
		    }
		    
		    if (foundClockImpl) {
		    	PendingIntent pendingIntentClock = PendingIntent.getActivity(
						this, 0, alarmClockIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
		    	
				updateViews.setOnClickPendingIntent(R.id.textViewClockMinute,
						pendingIntentClock);				
				updateViews.setOnClickPendingIntent(R.id.textViewClockHour,
						pendingIntentClock);
		    }
		    
		    // start weather forecast activity
		    Intent weatherForecastIntent = new Intent(this, WeatherForecastActivity.class);
		    weatherForecastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		    
		    PendingIntent pendingForecastIntent = PendingIntent.getActivity(this,
					appWidgetId, weatherForecastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
		    updateViews.setOnClickPendingIntent(R.id.imageViewWeather, pendingForecastIntent);		    
		}		

		return (updateViews);		
	}

	public void updateBatteryStatus(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateBatteryStatus");

		int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
				BatteryManager.BATTERY_STATUS_UNKNOWN);
		int level = -1;
		int icon = R.drawable.stat_sys_battery_plain000;

		if (rawlevel >= 0 && scale > 0) {
			level = (rawlevel * 100) / scale;
		}

		if (level == -1) {
			Intent batteryIntent = getApplicationContext().registerReceiver(
					null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			int rawlevel1 = batteryIntent.getIntExtra(
					BatteryManager.EXTRA_LEVEL, -1);
			int scale1 = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE,
					-1);
			int status1 = batteryIntent.getIntExtra(
					BatteryManager.EXTRA_STATUS,
					BatteryManager.BATTERY_STATUS_UNKNOWN);
			if (rawlevel1 >= 0 && scale1 > 0) {
				level = (rawlevel1 * 100) / scale1;
			}
			status = status1;
		}
		
		String strLevel = String.valueOf(level); 
		
		int lnColon = strLevel.length();
		SpannableString spStrLevel = new SpannableString(strLevel);
		spStrLevel.setSpan(new StyleSpan(Typeface.BOLD), 0, lnColon, 0);

		updateViews.setTextViewText(R.id.textViewBatteryStatus, spStrLevel);

//		if (level > 90)
//			icon = R.drawable.battery_010;
//		else if (level <= 90 && level > 75)
//			icon = R.drawable.battery_009;
//		else if (level <= 75 && level > 60)
//			icon = R.drawable.battery_008;
//		else if (level <= 60 && level > 50)
//			icon = R.drawable.battery_007;
//		else if (level <= 50 && level > 40)
//			icon = R.drawable.battery_006;
//		else if (level <= 40 && level > 25)
//			icon = R.drawable.battery_005;
//		else if (level <= 25 && level > 15)
//			icon = R.drawable.battery_004;
//		else if (level <= 15 && level > 5)
//			icon = R.drawable.battery_003;
//		else if (level <= 5)
//			icon = R.drawable.battery_002;
//		else
//			icon = R.drawable.battery_001;
//		
//		if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
//			icon = R.drawable.battery_000;
//		}
		
		switch (status) {
		
		case (BatteryManager.BATTERY_STATUS_UNKNOWN):
			icon = R.drawable.stat_sys_battery_charge_plain000;
			break;
		case (BatteryManager.BATTERY_STATUS_FULL):
			icon = R.drawable.stat_sys_battery_plainfull;
			break;
		case (BatteryManager.BATTERY_STATUS_CHARGING):			
			icon = R.drawable.stat_sys_battery_charge_plain000 + level;
			break;
		case (BatteryManager.BATTERY_STATUS_DISCHARGING):
		case (BatteryManager.BATTERY_STATUS_NOT_CHARGING):
			icon = R.drawable.stat_sys_battery_plain000 + level;
			break;
		}

		updateViews.setImageViewResource(R.id.imageViewBattery, icon);
	}
	
	public void updateNotificationBatteryStatus(Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateNotificationBatteryStatus");
		Context context = this;
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		if (Preferences.getShowBatteryNotif(this) == false)
		{
			notificationManager.cancel(R.string.batterynotification);
			return;
		}

		int rawlevel = intent
				.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
				BatteryManager.BATTERY_STATUS_UNKNOWN);
		int level = -1;
		int icon = R.drawable.stat_sys_battery_charge_anim000;
		
		if (rawlevel >= 0 && scale > 0) {
			level = (rawlevel * 100) / scale;
		}

		if (level == -1) {
			Intent batteryIntent = getApplicationContext().registerReceiver(
					null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			int rawlevel1 = batteryIntent.getIntExtra(
					BatteryManager.EXTRA_LEVEL, -1);
			int scale1 = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE,
					-1);
			int status1 = batteryIntent.getIntExtra(
					BatteryManager.EXTRA_STATUS,
					BatteryManager.BATTERY_STATUS_UNKNOWN);
			if (rawlevel1 >= 0 && scale1 > 0) {
				level = (rawlevel1 * 100) / scale1;
			}
			status = status1;
		}
		
		switch (status) {
		
		case (BatteryManager.BATTERY_STATUS_UNKNOWN):
			icon = R.drawable.stat_sys_battery_charge_anim000;
			break;
		case (BatteryManager.BATTERY_STATUS_FULL):
			icon = R.drawable.stat_sys_battery_full;
			break;
		case (BatteryManager.BATTERY_STATUS_CHARGING):			
			icon = R.drawable.stat_sys_battery_charge_anim000 + level;
			break;
		case (BatteryManager.BATTERY_STATUS_DISCHARGING):
		case (BatteryManager.BATTERY_STATUS_NOT_CHARGING):
			icon = R.drawable.stat_sys_battery_000 + level;
			break;
		}
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		notification = new Notification(icon, "Battery Status",
				System.currentTimeMillis());
		
		//if (status == BatteryManager.BATTERY_STATUS_CHARGING)
		//	notification.number = level;
		
		notification.flags |= Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_NO_CLEAR;
		notification.when = 0;

		Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
		ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(
				powerUsageIntent, PackageManager.MATCH_DEFAULT_ONLY);
		PendingIntent pendingIntent;

		if (resolveInfo != null) {
			pendingIntent = PendingIntent.getActivity(context, 0,
					powerUsageIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			pendingIntent = null;
		}

		CharSequence batteryStatus = "Battery status: ";

		if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
			batteryStatus = batteryStatus + "Unknown";
		} else if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
			batteryStatus = batteryStatus + "Charging";
		} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
			batteryStatus = batteryStatus + "Discharging";
		} else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
			batteryStatus = batteryStatus + "Not charging";
		} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
			batteryStatus = batteryStatus + "Full";
		}

		notification.setLatestEventInfo(context, "Battery Level: " + level
				+ "%", batteryStatus, pendingIntent);
		notificationManager.notify(R.string.batterynotification, notification);
	}
	
	protected int getBrightness() {

		int brightness = 255;
		
		try {
			brightness = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException snfe) {
			brightness = 255;
		}

		return brightness;
	}
	
	public boolean isAutoBrightness() {
		boolean automicBrightness = false;
		try {
			automicBrightness = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}

	public void setBrightness(int brightness) {

		Log.v(LOG_TAG, "setBrightness - " + brightness);		
	}

	public void updateBrightness(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateBrightness");

		Bundle extras = intent.getExtras();

		if (extras == null)
			return;
		
		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);

		if (intentExtra.equals(Intent.ACTION_SCREEN_ON)) {
			int resource = R.drawable.brightness_mid;			
			
			if (isAutoBrightness()) {
				//resource = R.drawable.brightness_auto;
			} else {
				int brightness = getBrightness();
				
				if (brightness < 50) {
					//resource = R.drawable.brightness_off;
					resource = R.drawable.brightness_mid;
				} else {
					
					if (brightness >= 50 && brightness < 150) {
						resource = R.drawable.brightness_mid;
					} else {
						//resource = R.drawable.brightness_on;
						resource = R.drawable.brightness_mid;
					}					
				}
			}
			
			updateViews.setImageViewResource(R.id.imageViewBrightness, resource);
		}		
	}
	
	public boolean getAirplaneMode() {
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
	        return Settings.System.getInt(getContentResolver(), 
	                Settings.System.AIRPLANE_MODE_ON, 0) != 0;          
	    } else {
	        return Settings.Global.getInt(getContentResolver(), 
	                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	    }       
	}
	
	public void setAirplaneMode(boolean airplaneMode) {
	
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Settings.System.putInt(
			      getContentResolver(),
			      Settings.System.AIRPLANE_MODE_ON, airplaneMode ? 1 : 0);
		} else {
			Settings.Global.putInt(
			      getContentResolver(),
			      Settings.Global.AIRPLANE_MODE_ON, airplaneMode ? 1 : 0);
		}

		// Post an intent to reload
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", airplaneMode);
		sendBroadcast(intent);
	}
	
	public void updateAirplaneMode(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateAirplaneMode");

		Bundle extras = intent.getExtras();

		if (extras == null)
			return;
		
		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);

		if (intentExtra.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
			Boolean airplaneMode = getAirplaneMode();
			int resource = (airplaneMode == null ? R.drawable.airplane_off
					: airplaneMode ? R.drawable.airplane_on
							: R.drawable.airplane_off);

			updateViews.setImageViewResource(R.id.imageViewAirplane, resource);
		}

		if (intentExtra.equals(AIRPLANE_WIDGET_UPDATE)) {
			Boolean airplaneMode = getAirplaneMode();
			// ignore toggle requests if the Airplane mode is currently changing
			// state
			if (airplaneMode != null) {
				setAirplaneMode(!airplaneMode);
			}
		}
	}

	protected Boolean getBluetoothState() {

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		Boolean bluetoothState;

		switch (bluetoothAdapter.getState()) {
		case BluetoothAdapter.STATE_OFF:
			bluetoothState = false;
			break;
		case BluetoothAdapter.STATE_TURNING_OFF:
			bluetoothState = null;
			break;
		case BluetoothAdapter.STATE_ON:
			bluetoothState = true;
			break;
		case BluetoothAdapter.STATE_TURNING_ON:
			bluetoothState = null;
			break;
		default:
			bluetoothState = false;
		}

		Log.v(LOG_TAG, "getBluetoothState - " + bluetoothState);

		return bluetoothState;

	}

	public void setBluetoothState(boolean bluetoothState) {

		Log.v(LOG_TAG, "setBluetoothState - " + bluetoothState);

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (bluetoothState) {
			bluetoothAdapter.enable();
		} else {
			bluetoothAdapter.disable();
		}
	}

	public void updateBluetoothStatus(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateBluetoothStatus");

		if (getAirplaneMode()) {
			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				String radios = Settings.System.getString(getContentResolver(),
			            Settings.System.AIRPLANE_MODE_RADIOS);
				
				if (radios.contains(Settings.System.RADIO_BLUETOOTH)) {
					return;
				}          
		    } else {
		    	String radios = Settings.Global.getString(getContentResolver(),
			            Settings.Global.AIRPLANE_MODE_RADIOS);
				
				if (radios.contains(Settings.Global.RADIO_BLUETOOTH)) {
					return;
				}
		    }					
		}
		
		Bundle extras = intent.getExtras();

		if (extras == null)
			return;
		
		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);

		if (intentExtra.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			Boolean bluetoothState = getBluetoothState();
			int resource = (bluetoothState == null ? R.drawable.bluetooth_off
					: bluetoothState ? R.drawable.bluetooth_on
							: R.drawable.bluetooth_off);

			updateViews.setImageViewResource(R.id.imageViewBluetooth, resource);
		}

		if (intentExtra.equals(BLUETOOTH_WIDGET_UPDATE)) {
			Boolean bluetoothState = getBluetoothState();
			// ignore toggle requests if the BlueTooth is currently changing
			// state
			if (bluetoothState != null) {
				setBluetoothState(!bluetoothState);
			}
		}
	}

	protected Boolean getWifiState() {

		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		Boolean wifiState;

		switch (wifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			wifiState = false;
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			wifiState = null;
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			wifiState = true;
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			wifiState = null;
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			wifiState = false;
			break;
		default:
			wifiState = false;
		}

		Log.v(LOG_TAG, "getWifiState - " + wifiState);

		return wifiState;

	}

	public void setWifiState(boolean wifiState) {

		Log.v(LOG_TAG, "setWifiState - " + wifiState);

		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(wifiState);

	}

	public void updateWifiStatus(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateWifiStatus");

		if (getAirplaneMode()) {
			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				String radios = Settings.System.getString(getContentResolver(),
			            Settings.System.AIRPLANE_MODE_RADIOS);
				
				if (radios.contains(Settings.System.RADIO_WIFI)) {
					return;
				}          
		    } else {
		    	String radios = Settings.Global.getString(getContentResolver(),
			            Settings.Global.AIRPLANE_MODE_RADIOS);
				
				if (radios.contains(Settings.Global.RADIO_WIFI)) {
					return;
				}
		    }					
		}
		
		Bundle extras = intent.getExtras();

		if (extras == null)
			return;
		
		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);

		if (intentExtra.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			Boolean wifiState = getWifiState();
			int resource = (wifiState == null ? R.drawable.wifi_off
					: wifiState ? R.drawable.wifi_on : R.drawable.wifi_off);

			updateViews.setImageViewResource(R.id.imageViewWiFi, resource);
		}

		if (intentExtra.equals(WIFI_WIDGET_UPDATE)) {
			Boolean wifiState = getWifiState();
			// ignore toggle requests if the WiFi is currently changing state
			if (wifiState != null) {
				setWifiState(!wifiState);				
			}
		}
	}

	protected Boolean getMobileState() {

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Boolean mobileState;

		int extraDataState = telephonyManager.getDataState();

		switch (extraDataState) {
		case TelephonyManager.DATA_CONNECTED:
			mobileState = true;
			break;

		case TelephonyManager.DATA_DISCONNECTED:
			mobileState = false;
			break;

		case TelephonyManager.DATA_CONNECTING:
			mobileState = null;
			break;

		case TelephonyManager.DATA_SUSPENDED:
			mobileState = null;
			break;
		default:
			mobileState = false;		
		}

		Log.v(LOG_TAG, "getMobileState - " + mobileState);

		return mobileState;

	}

	public void setMobileState(boolean mobileState) {

		Log.v(LOG_TAG, "setMobileState - " + mobileState);

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;

		if (currentapiVersion <= android.os.Build.VERSION_CODES.FROYO) {
			setMobileStateFroyo(mobileState);
		} else {
			setMobileStateGingerbread(mobileState);
		}
	}

	private void setMobileStateFroyo(boolean mobileState) {

		Method dataConnSwitchmethod = null;
		Class telephonyManagerClass = null;
		Object ITelephonyStub = null;
		Class ITelephonyClass = null;

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		try {
			telephonyManagerClass = Class.forName(telephonyManager.getClass()
					.getName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Method getITelephonyMethod = null;
		try {
			getITelephonyMethod = telephonyManagerClass
					.getDeclaredMethod("getITelephony");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getITelephonyMethod.setAccessible(true);
		try {
			ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ITelephonyClass = Class
					.forName(ITelephonyStub.getClass().getName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!mobileState) {
			try {
				dataConnSwitchmethod = ITelephonyClass
						.getDeclaredMethod("disableDataConnectivity");
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				dataConnSwitchmethod = ITelephonyClass
						.getDeclaredMethod("enableDataConnectivity");
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		dataConnSwitchmethod.setAccessible(true);
		try {
			dataConnSwitchmethod.invoke(ITelephonyStub);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setMobileStateGingerbread(boolean mobileState) {

		ConnectivityManager conman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		Class conmanClass = null;
		try {
			conmanClass = Class.forName(conman.getClass().getName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Field iConnectivityManagerField = null;
		try {
			iConnectivityManagerField = conmanClass
					.getDeclaredField("mService");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		iConnectivityManagerField.setAccessible(true);
		Object iConnectivityManager = null;
		try {
			iConnectivityManager = iConnectivityManagerField.get(conman);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Class iConnectivityManagerClass = null;
		try {
			iConnectivityManagerClass = Class.forName(iConnectivityManager
					.getClass().getName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Method setMobileDataEnabledMethod = null;
		try {
			setMobileDataEnabledMethod = iConnectivityManagerClass
					.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		setMobileDataEnabledMethod.setAccessible(true);

		try {
			setMobileDataEnabledMethod
					.invoke(iConnectivityManager, mobileState);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void updateDataStatus(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateMobileStatus");

		if (getAirplaneMode()) {
			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				String radios = Settings.System.getString(getContentResolver(),
			            Settings.System.AIRPLANE_MODE_RADIOS);
				
				if (radios.contains(Settings.System.RADIO_CELL)) {
					return;
				}          
		    } else {
		    	String radios = Settings.Global.getString(getContentResolver(),
			            Settings.Global.AIRPLANE_MODE_RADIOS);
				
				if (radios.contains(Settings.Global.RADIO_CELL)) {
					return;
				}
		    }					
		}

		Bundle extras = intent.getExtras();

		if (extras == null)
			return;

		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);

		if (intentExtra.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			Boolean mobileState = getMobileState();
			int resource = (mobileState == null ? R.drawable.data_off
					: mobileState ? R.drawable.data_on
							: R.drawable.data_off);

			updateViews.setImageViewResource(R.id.imageViewMobile, resource);
		}

		if (intentExtra.equals(MOBILE_DATA_WIDGET_UPDATE)) {
			Boolean mobileState = getMobileState();
			// ignore toggle requests if the Mobile Data is currently changing
			// state
			if (mobileState != null) {
				setMobileState(!mobileState);				
			}
		}
	}

	private boolean canToggleGPS() {
		PackageManager pacman = getPackageManager();
		PackageInfo pacInfo = null;

		try {
			pacInfo = pacman.getPackageInfo("com.android.settings",
					PackageManager.GET_RECEIVERS);
		} catch (NameNotFoundException e) {
			return false; // package not found
		}

		if (pacInfo != null) {
			for (ActivityInfo actInfo : pacInfo.receivers) {
				// test if receiver is exported. if so, we can toggle GPS.
				if (actInfo.name
						.equals("com.android.settings.widget.SettingsAppWidgetProvider")
						&& actInfo.exported) {
					return true;
				}
			}
		}

		return false; // default
	}

	protected Boolean getGpsState() {

		Boolean gpsState = false;

		LocationManager gpsstatus = (LocationManager) (getSystemService(Context.LOCATION_SERVICE));

		if (gpsstatus == null) {
			return null;
		}

		if (gpsstatus.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			gpsState = true;
		} else {
			gpsState = false;
		}

		Log.v(LOG_TAG, "getGpsState - " + gpsState);

		return gpsState;

	}

	public void setGpsState(boolean gpsState) {

		if (canToggleGPS()) {
			if (gpsState) {
				final Intent poke = new Intent();
				poke.setClassName("com.android.settings",
						"com.android.settings.widget.SettingsAppWidgetProvider");
				poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				poke.setData(Uri.parse("3"));
				sendBroadcast(poke);
			} else {
				final Intent poke = new Intent();
				poke.setClassName("com.android.settings",
						"com.android.settings.widget.SettingsAppWidgetProvider");
				poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				poke.setData(Uri.parse("3"));
				sendBroadcast(poke);
			}
		}

	}

	public void updateGpsStatus(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateGpsStatus");

		Bundle extras = intent.getExtras();

		if (extras == null)
			return;

		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);

		if (intentExtra.equals("android.location.GPS_ENABLED_CHANGE")) {
			Boolean gpsState = getGpsState();
			int resource = (gpsState == null ? R.drawable.gps_off
					: gpsState ? R.drawable.gps_on : R.drawable.gps_off);

			updateViews.setImageViewResource(R.id.imageViewGps, resource);
		}

		if (intentExtra.equals(GPS_WIDGET_UPDATE)) {
			Boolean gpsState = getGpsState();
			// ignore toggle requests if the BlueTooth is currently changing
			// state
			if (gpsState != null) {
				setGpsState(!gpsState);				
			}
		}
	}
	
	protected int getRingerState() {

		AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);		 
		int ringerState = audioManager.getRingerMode();	

		Log.v(LOG_TAG, "getRingerState - " + ringerState);

		return ringerState;

	}

	public void setRingerState(int ringerState) {

		Log.v(LOG_TAG, "setRingerState - " + ringerState);
		
		AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);		
		audioManager.setRingerMode(ringerState);
		
	}

	public void updateRingerStatus(RemoteViews updateViews, Intent intent) {
		Log.d(LOG_TAG, "WidgetUpdateService updateRingerStatus");

		Bundle extras = intent.getExtras();

		if (extras == null)
			return;

		String intentExtra = extras.getString(WidgetInfoReceiver.INTENT_EXTRA);

		if (intentExtra.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
			int ringerState = getRingerState();
			
			int resource = R.drawable.ringer_normal;
			
			switch (ringerState) {
			case AudioManager.RINGER_MODE_SILENT:
				resource = R.drawable.ringer_silent;
				break;

			case AudioManager.RINGER_MODE_VIBRATE:
				resource = R.drawable.ringer_vibrate;
				break;

			case AudioManager.RINGER_MODE_NORMAL:
				resource = R.drawable.ringer_normal;
				break;

			default:
				resource = R.drawable.ringer_normal;
				break;
			}				

			updateViews.setImageViewResource(R.id.imageViewRinger, resource);
		}

		if (intentExtra.equals(RINGER_WIDGET_UPDATE)) {
			int ringerState = getRingerState();
			
			int nToggle = Preferences.getSoundOptions(this);
			
			switch (nToggle) {
			case 0:
				setRingerState((ringerState+2)%3); // Sound/Vibration/Silent
				break;
			case 1:
				setRingerState((ringerState+1)%3); // Sound/Silent/Vibration
				break;
			case 2:
				setRingerState(ringerState == AudioManager.RINGER_MODE_NORMAL ? 
						AudioManager.RINGER_MODE_VIBRATE : AudioManager.RINGER_MODE_NORMAL); // Sound/Vibration
				break;
			case 3:
				setRingerState(ringerState == AudioManager.RINGER_MODE_NORMAL ? 
						AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_NORMAL); // Sound/Silent
				break;
			default:
				setRingerState((ringerState+2)%3); // same as 0
				break;
			}
			
			//setRingerState((ringerState+1)%3);			
		}
	}
	
	public void updateClockStatus(RemoteViews updateViews, int appWidgetId, boolean updateWeather) {
		Log.d(LOG_TAG, "WidgetUpdateService updateClockStatus");

		if (updateViews == null) {
			updateViews = new RemoteViews(getPackageName(),
					R.layout.digitalclockwidget);
		}
		
		boolean bShowDate = Preferences.getShowDate(this, appWidgetId);
		boolean bShow24Hrs = Preferences.getShow24Hrs(this, appWidgetId);
		boolean bShowBattery = Preferences.getShowBattery(this, appWidgetId);
		int iColorItem = Preferences.getColorItem(this, appWidgetId);
		int iClockSkinItem = Preferences.getClockSkin(this, appWidgetId);
		int iDateFormatItem = Preferences.getDateFormatItem(this, appWidgetId);
		int iTransparency = Preferences.getTransparency(this, appWidgetId);
		String colon = " ";
		
		if (iClockSkinItem == 2) {
			colon = ":";
		}
		
		String currentHour, currentMinute;

		SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
		currentMinute = String.format(sdfMinute.format(new Date()));
		
		if (bShow24Hrs) {
			SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
			currentHour = String.format(sdfHour.format(new Date()));
		} else {
			SimpleDateFormat sdfHour = new SimpleDateFormat("hh");
			currentHour = String.format(sdfHour.format(new Date()));
		}		

		if (bShowBattery || bShowDate) {
			updateViews.setViewVisibility(R.id.textViewDate,
					View.VISIBLE);
		} else {
			updateViews.setViewVisibility(R.id.textViewDate,
					View.INVISIBLE);
		}

		int lnHour = currentHour.length();
		SpannableString spStrHour = new SpannableString(currentHour);
		spStrHour.setSpan(new StyleSpan(Typeface.BOLD), 0, lnHour, 0);
		
		int lnMinute = currentMinute.length();
		SpannableString spStrMinute = new SpannableString(currentMinute);
		spStrMinute.setSpan(new StyleSpan(Typeface.BOLD), 0, lnMinute, 0);
		
		int lnColon = colon.length();
		SpannableString spStrColon = new SpannableString(colon);
		spStrColon.setSpan(new StyleSpan(Typeface.BOLD), 0, lnColon, 0);
		
		int systemColor = Color.WHITE;

		switch (iColorItem) {
		case 0:
			systemColor = Color.BLACK;	
			break;
		case 1:
			systemColor = Color.DKGRAY;
			break;
		case 2:
			systemColor = Color.GRAY;
			break;
		case 3:
			systemColor = Color.LTGRAY;
			break;
		case 4:
			systemColor = Color.WHITE;	
			break;
		case 5:
			systemColor = Color.RED;
			break;
		case 6:
			systemColor = Color.GREEN;
			break;
		case 7:
			systemColor = Color.BLUE;
			break;
		case 8:
			systemColor = Color.YELLOW;
			break;
		case 9:
			systemColor = Color.CYAN;
			break;
		case 10:
			systemColor = Color.MAGENTA;
			break;
		default:
			systemColor = Color.WHITE;
			break;
		}

		spStrHour.setSpan(new ForegroundColorSpan(systemColor), 0, lnHour, 0);
		spStrMinute.setSpan(new ForegroundColorSpan(systemColor), 0, lnMinute, 0);
		spStrColon.setSpan(new ForegroundColorSpan(systemColor), 0, lnColon, 0);
		
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);

		switch(metrics.densityDpi)
		{
			case DisplayMetrics.DENSITY_LOW:  //LDPI	
			case DisplayMetrics.DENSITY_HIGH: //HDPI			
			case DisplayMetrics.DENSITY_MEDIUM: //MDPI
			
		}
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			
			if (metrics.densityDpi > DisplayMetrics.DENSITY_LOW) {
				spStrHour.setSpan(new AbsoluteSizeSpan(96), 0, lnHour, 0);
				spStrMinute.setSpan(new AbsoluteSizeSpan(96), 0, lnMinute, 0);
				spStrColon.setSpan(new AbsoluteSizeSpan(96), 0, lnColon, 0);
			}
		}

//		boolean bShowAdditional = (bShowBattery || bShowDate);

//		float fDecreaseSpan = 0;

//		if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
//			fDecreaseSpan = 2.0f;
//		}

//		if (bShowAdditional) {		
//			spStrHour.setSpan(new RelativeSizeSpan(3.5f - fDecreaseSpan), 0, lnHour, 0);
//			spStrMinute.setSpan(new RelativeSizeSpan(3.5f - fDecreaseSpan), 0, lnMinute, 0);
//		}
//		else {
//			spStrHour.setSpan(new RelativeSizeSpan(2.0f - fDecreaseSpan), 0, lnHour, 0);
//			spStrMinute.setSpan(new RelativeSizeSpan(2.0f - fDecreaseSpan), 0, lnMinute, 0);
//		}
		
		updateViews.setTextViewText(R.id.textViewClockHour, spStrHour);
		updateViews.setTextViewText(R.id.textViewClockMinute, spStrMinute);
		updateViews.setTextViewText(R.id.textViewClockSpace, spStrColon);
		
		String currentDate = "";
		String[] mTestArray = getResources().getStringArray(R.array.dateFormat);

		if (bShowDate) {
			SimpleDateFormat sdf = new SimpleDateFormat(mTestArray[iDateFormatItem]);
			currentDate = String.format(sdf.format(new Date()));			
		}
		
		if (bShowBattery) {
		
			Intent intentBattery = new Intent(Intent.ACTION_BATTERY_CHANGED);
			
			int rawlevel = intentBattery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intentBattery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int level = -1;
	
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
	
			if (level == -1) {
				Intent batteryIntent = getApplicationContext()
						.registerReceiver(null,
								new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				int rawlevel1 = batteryIntent.getIntExtra(
						BatteryManager.EXTRA_LEVEL, -1);
				int scale1 = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE,
						-1);
				if (rawlevel1 >= 0 && scale1 > 0) {
					level = (rawlevel1 * 100) / scale1;
				}
			}
			
			currentDate = currentDate + (bShowDate?"\t[":"") + level + "%" + (bShowDate?"]":"");
		}
		
		int lnDate = currentDate.length();
		SpannableString spStrDate = new SpannableString(currentDate);
		spStrDate.setSpan(new StyleSpan(Typeface.BOLD), 0, lnDate, 0);
        
        updateViews.setTextViewText(R.id.textViewDate, spStrDate);		
		
		updateViews.setInt(R.id.backgroundImage, "setAlpha", iTransparency*255/100);		
		
		switch (iClockSkinItem) {
		case 0:
			updateViews.setInt(R.id.textViewClockHour, "setBackgroundResource", R.drawable.bck_left);
			updateViews.setInt(R.id.textViewClockMinute, "setBackgroundResource", R.drawable.bck_right);
			break;
		case 1:
			updateViews.setInt(R.id.textViewClockHour, "setBackgroundResource", R.drawable.bck_left_light);
			updateViews.setInt(R.id.textViewClockMinute, "setBackgroundResource", R.drawable.bck_right_light);		
			break;
		case 2:
			updateViews.setInt(R.id.textViewClockHour, "setBackgroundResource", 0);
			updateViews.setInt(R.id.textViewClockMinute, "setBackgroundResource", 0);
			break;
		default:
			updateViews.setInt(R.id.textViewClockHour, "setBackgroundResource", R.drawable.bck_left);
			updateViews.setInt(R.id.textViewClockMinute, "setBackgroundResource", R.drawable.bck_right);
			break;
		}
		
		if (updateWeather || currentMinute.equals("00")) {
			// update weather info from cache
			readCachedWeatherData(updateViews, appWidgetId);
		}
		else {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup localView = (ViewGroup) inflater.inflate(updateViews.getLayoutId(), null);
			updateViews.reapply(getApplicationContext(), localView);
			
			 TextView tv = (TextView) localView.findViewById(R.id.textViewLoc);
			 Log.d("blah", (String) tv.getText());
			 
			 if ((String)tv.getText() == getResources().getString(R.string.locationempty)) {
				 // update weather info from cache
				 readCachedWeatherData(updateViews, appWidgetId);
			 }
		}
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
	}
	
	public void updateWeatherStatus(RemoteViews updateViews, int appWidgetId, boolean scheduledUpdate) {
		Log.d(LOG_TAG, "WidgetUpdateService updateWeatherStatus appWidgetId: " + appWidgetId + " scheduled: " + scheduledUpdate);

		if (updateViews == null) {
			updateViews = new RemoteViews(getPackageName(),
					R.layout.digitalclockwidget);
		}		
		
		try {
			boolean bWiFiOnly = Preferences.getRefreshWiFiOnly(this, appWidgetId);
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (scheduledUpdate && bWiFiOnly && !mWifi.isConnected()) {
				Preferences.setWeatherSuccess(this, appWidgetId, false);			
				readCachedWeatherData(updateViews, appWidgetId);
				
				return;
			}
			
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			
			if(activeNetworkInfo == null) {
				//Toast.makeText(this, "No Internet connection.", Toast.LENGTH_SHORT).show();
				
				if (scheduledUpdate)
					Preferences.setWeatherSuccess(this, appWidgetId, false);
				
				readCachedWeatherData(updateViews, appWidgetId);
				return;
			}					
	        
			HttpTaskInfo info = new HttpTaskInfo();
			info.updateViews = updateViews;
			info.appWidgetId = appWidgetId;
			info.scheduledUpdate = scheduledUpdate;
			new HttpTask().execute(info);
			
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
        	
        	readCachedWeatherData(updateViews, appWidgetId);
		} catch (Exception e) {
			e.printStackTrace();
        	
        	readCachedWeatherData(updateViews, appWidgetId);		
		}
				
	}
	
	public class HttpTask extends AsyncTask<HttpTaskInfo, Void, Boolean>{

	    public Boolean doInBackground(HttpTaskInfo... info){
	    	RemoteViews updateViews = info[0].updateViews;
	    	int appWidgetId = info[0].appWidgetId;
	    	boolean scheduledUpdate = info[0].scheduledUpdate;
	    	
	    	boolean current = getCurrentWeatherData(updateViews, appWidgetId, scheduledUpdate);
	    	boolean forecast = getForecastData(updateViews, appWidgetId, scheduledUpdate);

	    	return (current && forecast);
	    }

	    public void onPostExecute(Boolean result){
	    	super.onPostExecute(result);
	    }
	}
	
	public class HttpTaskInfo {
		RemoteViews updateViews;
		int appWidgetId;
		boolean scheduledUpdate;
	}
	
	@SuppressWarnings("unused")
	boolean getCurrentWeatherData(RemoteViews updateViews, int appWidgetId, boolean scheduledUpdate) {
		boolean ret = true;
		try {
        	Reader responseReader = null;
	        HttpClient client = new DefaultHttpClient();
	        HttpGet request = null;
	        
	        if (Preferences.getLocationId(WidgetUpdateService.this, appWidgetId) >= 0) {
	        	request = new HttpGet(String.format(WEATHER_SERVICE_ID_URL, Preferences.getLocationId(WidgetUpdateService.this, appWidgetId)));		        	
	        }
	        else {
	        	request = new HttpGet(String.format(WEATHER_SERVICE_COORD_URL, Preferences.getLocationLat(WidgetUpdateService.this, appWidgetId), 
		        		Preferences.getLocationLon(WidgetUpdateService.this, appWidgetId)));
	        }
	        
            HttpResponse response = client.execute(request);

            StatusLine status = response.getStatusLine();
            Log.d(LOG_TAG, "Request returned status " + status);

            HttpEntity entity = response.getEntity();
            responseReader = new InputStreamReader(entity.getContent()); 
            
            if (responseReader == null) {
            	if (scheduledUpdate)
    				Preferences.setWeatherSuccess(WidgetUpdateService.this, appWidgetId, false);
            	
            	readCachedWeatherData(updateViews, appWidgetId);            
            	return false;
            }
            
            char[] buf = new char[1024];
            StringBuilder result = new StringBuilder();
            int read = responseReader.read(buf);
            
            while (read >= 0) {
                result.append(buf, 0, read);
                read = responseReader.read(buf);
            }
            
            parseWeatherData(updateViews, appWidgetId, result.toString(), false, scheduledUpdate);
            
            // save cache
            File parentDirectory = new File(WidgetUpdateService.this.getFilesDir().getAbsolutePath());
            
            if (!parentDirectory.exists()) {
                Log.e(LOG_TAG, "Cache file parent directory does not exist.");
                
                if(!parentDirectory.mkdirs()) {
                	Log.e(LOG_TAG, "Cannot create cache file parent directory."); 
                }
            }

            File cacheFile = new File(parentDirectory, "weather_cache_"+appWidgetId);
            cacheFile.createNewFile();
            
            final BufferedWriter cacheWriter = new BufferedWriter(new FileWriter(cacheFile), result.length());
            cacheWriter.write(result.toString());
            cacheWriter.close();
            
        } catch (UnknownHostException e) {
        	e.printStackTrace();
        	
        	if (scheduledUpdate)
				Preferences.setWeatherSuccess(WidgetUpdateService.this, appWidgetId, false);
        	
        	readCachedWeatherData(updateViews, appWidgetId);
        	
        	ret = false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            
            if (scheduledUpdate)
				Preferences.setWeatherSuccess(WidgetUpdateService.this, appWidgetId, false);
            
            readCachedWeatherData(updateViews, appWidgetId);
            
            ret = false;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            
            if (scheduledUpdate)
				Preferences.setWeatherSuccess(WidgetUpdateService.this, appWidgetId, false);
            
            readCachedWeatherData(updateViews, appWidgetId);
            
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();
            
            if (scheduledUpdate)
				Preferences.setWeatherSuccess(WidgetUpdateService.this, appWidgetId, false);
            
            readCachedWeatherData(updateViews, appWidgetId);
            
            ret = false;
        }

        return ret;
	}
	
	@SuppressWarnings("unused")
	boolean getForecastData(RemoteViews updateViews, int appWidgetId, boolean scheduledUpdate) {
		boolean ret = true;
		try {
        	Reader responseReader = null;
	        HttpClient client = new DefaultHttpClient();
	        HttpGet request = null;
	        
	        if (Preferences.getLocationId(WidgetUpdateService.this, appWidgetId) >= 0) {
	        	request = new HttpGet(String.format(WEATHER_FORECAST_ID_URL, Preferences.getLocationId(WidgetUpdateService.this, appWidgetId)));		        	
	        }
	        else {
	        	request = new HttpGet(String.format(WEATHER_FORECAST_COORD_URL, Preferences.getLocationLat(WidgetUpdateService.this, appWidgetId), 
		        		Preferences.getLocationLon(WidgetUpdateService.this, appWidgetId)));
	        }
	        
            HttpResponse response = client.execute(request);

            StatusLine status = response.getStatusLine();
            Log.d(LOG_TAG, "Request returned status " + status);

            HttpEntity entity = response.getEntity();
            responseReader = new InputStreamReader(entity.getContent()); 
            
            if (responseReader == null) {
            	if (scheduledUpdate)
    				Preferences.setForecastSuccess(WidgetUpdateService.this, appWidgetId, false);
            	
            	return false;
            }
            
            char[] buf = new char[1024];
            StringBuilder result = new StringBuilder();
            int read = responseReader.read(buf);
            
            while (read >= 0) {
                result.append(buf, 0, read);
                read = responseReader.read(buf);
            }
            
            // save cache
            File parentDirectory = new File(WidgetUpdateService.this.getFilesDir().getAbsolutePath());
            
            if (!parentDirectory.exists()) {
                Log.e(LOG_TAG, "Cache file parent directory does not exist.");
                
                if(!parentDirectory.mkdirs()) {
                	Log.e(LOG_TAG, "Cannot create cache file parent directory."); 
                }
            }

            File cacheFile = new File(parentDirectory, "forecast_cache_"+appWidgetId);
            cacheFile.createNewFile();
            
            final BufferedWriter cacheWriter = new BufferedWriter(new FileWriter(cacheFile), result.length());
            cacheWriter.write(result.toString());
            cacheWriter.close();
            
        } catch (UnknownHostException e) {
        	e.printStackTrace();    
        	
        	if (scheduledUpdate)
				Preferences.setForecastSuccess(WidgetUpdateService.this, appWidgetId, false);
        	
        	ret = false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();            
            
            if (scheduledUpdate)
				Preferences.setForecastSuccess(WidgetUpdateService.this, appWidgetId, false);
            
            ret = false;
        } catch (ClientProtocolException e) {
            e.printStackTrace();            
            
            if (scheduledUpdate)
				Preferences.setForecastSuccess(WidgetUpdateService.this, appWidgetId, false);
            
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();            
            
            if (scheduledUpdate)
				Preferences.setForecastSuccess(WidgetUpdateService.this, appWidgetId, false);
            
            ret = false;
        }

        return ret;
	}
	
	void readCachedWeatherData(RemoteViews updateViews, int appWidgetId) {
		Log.d(LOG_TAG, "WidgetUpdateService readCachedWeatherData appWidgetId: " + appWidgetId);

		if (updateViews == null) {
			updateViews = new RemoteViews(getPackageName(),
					R.layout.digitalclockwidget);
		}		
		
		try {
			File parentDirectory = new File(this.getFilesDir().getAbsolutePath());
            
            if (!parentDirectory.exists()) {
                Log.e(LOG_TAG, "Cache file parent directory does not exist.");
                
                if(!parentDirectory.mkdirs()) {
                	Log.e(LOG_TAG, "Cannot create cache file parent directory."); 
                }
            }

            File cacheFile = new File(parentDirectory, "weather_cache_"+appWidgetId);
            
            if (!cacheFile.exists())
            	return;
            
    		BufferedReader cacheReader = new BufferedReader(new FileReader(cacheFile));				
    		char[] buf = new char[1024];
			StringBuilder result = new StringBuilder();
            int read = cacheReader.read(buf);
            
            while (read >= 0) {
                result.append(buf, 0, read);
                read = cacheReader.read(buf);
            }
            
			cacheReader.close();
            
            parseWeatherData(updateViews, appWidgetId, result.toString(), true, false);
            
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	void parseWeatherData(RemoteViews updateViews, int appWidgetId, String parseString, boolean updateFromCache, boolean scheduledUpdate) {
		Log.d(LOG_TAG, "WidgetUpdateService parseWeatherData appWidgetId: " + appWidgetId + " cache: " + updateFromCache + " scheduled: " + scheduledUpdate);

		if (updateViews == null) {
			updateViews = new RemoteViews(getPackageName(),
					R.layout.digitalclockwidget);
		}				
		
		if (parseString.equals(null) || parseString.equals(""))
			return;
		
		JSONTokener parser = new JSONTokener(parseString);
		int tempScale = Preferences.getTempScale(this, appWidgetId);
        try {
			JSONObject query = (JSONObject)parser.nextValue();
			JSONObject weatherJSON = null;
			
			if (query.has("list")) {
			
				JSONArray list = query.getJSONArray("list");
			
	            if (list.length() == 0) {
	                return;
	            }
	            
	            weatherJSON = list.getJSONObject(0);
			}
			else {
				weatherJSON = query;
			}
            
            int cityId = weatherJSON.getInt("id");
            String location = weatherJSON.getString("name");
            
            int lnLoc = location.length();
    		SpannableString spStrLoc = new SpannableString(location);
    		spStrLoc.setSpan(new StyleSpan(Typeface.BOLD), 0, lnLoc, 0);
            
            updateViews.setTextViewText(R.id.textViewLoc, spStrLoc);
            
            long timestamp = weatherJSON.getLong("dt");
            Date time = new Date(timestamp * 1000);
            
            JSONObject main = null;
            try {
                main = weatherJSON.getJSONObject("main");
            } catch (JSONException e) {	                
            }
            try {
                double currentTemp = main.getDouble("temp") - 273.15;
                
                if (tempScale == 1)
                	updateViews.setTextViewText(R.id.textViewTemp, String.valueOf((int)(currentTemp*1.8+32)) + "°");
                else
                	updateViews.setTextViewText(R.id.textViewTemp, String.valueOf((int)currentTemp) + "°");
                
                
            } catch (JSONException e) {	                
            }
//            try {
//                double minTemp = main.getDouble("temp_min") - 273.15;
//                String min = "";
//                
//                if (tempScale == 1)
//                	min = "L: " + String.valueOf((int)(minTemp*1.8+32)) + "°";
//                else
//                	min = "L: " + String.valueOf((int)minTemp) + "°";
//                
//                updateViews.setTextViewText(R.id.textViewTempLow, min);                
//            } catch (JSONException e) {
//            }
//            try {
//                double maxTemp = main.getDouble("temp_max") - 273.15;	
//                String max = "";
//                
//                if (tempScale == 1)
//                	max = "H: " + String.valueOf((int)(maxTemp*1.8+32)) + "°";
//                else
//                	max = "H: " + String.valueOf((int)maxTemp) + "°";
//                
//                int lnMax = max.length();
//        		SpannableString spStrMax = new SpannableString(max);
//        		spStrMax.setSpan(new StyleSpan(Typeface.BOLD), 0, lnMax, 0);
//                
//                updateViews.setTextViewText(R.id.textViewTempHigh, spStrMax);                
//            } catch (JSONException e) {
//            }
            
            JSONObject windJSON = null;
            try {
                windJSON = weatherJSON.getJSONObject("wind");
            } catch (JSONException e) {	                
            }
            try {
                double speed = windJSON.getDouble("speed");	                
            } catch (JSONException e) {
            }
            try {
                double deg = windJSON.getDouble("deg");	                
            } catch (JSONException e) {
            }

            try {
            	double humidityValue = weatherJSON.getJSONObject("main").getDouble("humidity");
            } catch (JSONException e) {
            }
            
            try {
                JSONArray weathers = weatherJSON.getJSONArray("weather");
                for (int i = 0; i < weathers.length(); i++) {
                    JSONObject weather = weathers.getJSONObject(i);
                    int weatherId = weather.getInt("id");
                    String weatherMain = weather.getString("main");
                    String iconName = weather.getString("icon");
                    String iconNameAlt = iconName + "d";
                    
                    updateViews.setTextViewText(R.id.textViewDesc, weatherMain);
                    updateViews.setImageViewResource(R.id.imageViewWeather, R.drawable.weather_04d);
                    
                    float lat = Preferences.getLocationLat(this, appWidgetId);
                    float lon = Preferences.getLocationLon(this, appWidgetId);
                    boolean bDay = true;
                    
                    if (!Float.isNaN(lat) && !Float.isNaN(lon)) {
                    	SunriseSunsetCalculator calc;
                    	SunriseSunsetLocation loc = new SunriseSunsetLocation(String.valueOf(lat), String.valueOf(lon));
                    	calc = new SunriseSunsetCalculator(loc, TimeZone.getDefault());
                    	Calendar calendarForDate = Calendar.getInstance();                    	
                    	Calendar civilSunriseCalendarForDate = calc.getCivilSunriseCalendarForDate(calendarForDate);
                    	Calendar civilSunsetCalendarForDate = calc.getCivilSunsetCalendarForDate(calendarForDate);
                    	
                    	if (calendarForDate.before(civilSunriseCalendarForDate) || calendarForDate.after(civilSunsetCalendarForDate))                    
                    		bDay = false;
                    	else
                    		bDay = true;                    	
                    }
                    
                    WeatherConditions conditions = new WeatherConditions();
                    
                    for (int j=0; j<conditions.m_ImageArr.length; j++) {
                    	if (iconName.equals(conditions.m_ImageArr[j].iconName) || iconNameAlt.equals(conditions.m_ImageArr[j].iconName)) {
                    		
                    		if (conditions.m_ImageArr[j].bDay != bDay)
                    			updateViews.setImageViewResource(R.id.imageViewWeather, conditions.m_ImageArr[j].altIconId);
                    		else
                    			updateViews.setImageViewResource(R.id.imageViewWeather, conditions.m_ImageArr[j].iconId);
                    	}
                    }                    
                }
            } catch (JSONException e) {
                //no weather type
            }
            
            if (!updateFromCache)
            {
            	Preferences.setLastRefresh(this, appWidgetId, System.currentTimeMillis()); 
            	
            	if (scheduledUpdate)
            		Preferences.setWeatherSuccess(this, appWidgetId, true);
            }
            
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            appWidgetManager.updateAppWidget(appWidgetId, updateViews);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
