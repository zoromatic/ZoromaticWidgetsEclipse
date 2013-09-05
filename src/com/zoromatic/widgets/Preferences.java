package com.zoromatic.widgets;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
	public static final String PREFS_NAME = "com.zoromatic.widgets.Preferences";
	public static final String PREFS_NAME_SEC = "com.zoromatic.widgets.Preferences.Second";
	public static final String PREF_DATE_KEY = "showdate_";
	public static final String PREF_24HRS_KEY = "show24hrs_";
	public static final String PREF_COLOR_KEY = "color_";
	public static final String PREF_DATEFORMAT_KEY = "dateformat_";
	public static final String PREF_BATTERY_KEY = "showbattery_";
	public static final String PREF_TRANSPARENCY_KEY = "transparency_";
	public static final String PREF_BATTERY_NOTIF = "showbatterynotif_";
	public static final String PREF_BATTERY_NOTIF_KEY = "showbatterynotifkey_";
	public static final String PREF_REFRESH_INTERVAL_KEY = "refreshinterval_";
	public static final String PREF_REFRESH_NOW_KEY = "refreshnow_";
	public static final String PREF_LOCATION_SETTINGS_KEY = "locationsettings_";
	public static final String PREF_TEMP_SCALE_KEY = "tempscale_";
	public static final String PREF_LOCATION_LAT_KEY = "locationlat_";
	public static final String PREF_LOCATION_LON_KEY = "locationlon_";
	public static final String PREF_LOCATION_KEY = "location_";
	public static final String PREF_LOCATION_ID_KEY = "locationid_";
	public static final String PREF_LAST_ALARM_KEY = "lastalarm_";
	public static final String PREF_LAST_REFRESH_KEY = "lastrefresh_";
	public static final String PREF_ABOUT_KEY = "zoromaticabout_";
	public static final String PREF_OPENWEATHER_KEY = "openweatherlink_";
	public static final String PREF_WEATHER_SUCCESS = "weathersuccess_";
	public static final String PREF_FORECAST_SUCCESS = "forecastsuccess_";
	public static final String PREF_REFRESH_WIFI_ONLY = "refreshwifionly_";
	public static final String PREF_LOCK_SCREEN_ADMIN = "lockscreenadmin_";
	public static final String PREF_CLOCK_SKIN = "clockskin_";
	public static final String PREF_SOUND_OPTIONS = "soundoptions_";
	public static final String PREF_SOUND_OPTIONS_KEY = "soundoptionskey_";
	public static final String PREF_RESTART_SERVICE = "restartservice_";
	public static final String PREF_WEATHER_ICONS_KEY = "weathericons_";
	public static final String PREF_FONT_KEY = "font_";

	private static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, 0);
	}

	public static boolean getShowDate(Context context, int appWidgetId) {
		return getPreferences(context).getBoolean(PREF_DATE_KEY + appWidgetId,
				true);
	}

	public static boolean getShow24Hrs(Context context, int appWidgetId) {
		return getPreferences(context).getBoolean(PREF_24HRS_KEY + appWidgetId,
				true);
	}

	public static int getColorItem(Context context, int appWidgetId) {
		return getPreferences(context).getInt(PREF_COLOR_KEY + appWidgetId, 3);
	}

	public static int getDateFormatItem(Context context, int appWidgetId) {
		return getPreferences(context).getInt(
				PREF_DATEFORMAT_KEY + appWidgetId, 0);
	}

	public static boolean getShowBattery(Context context, int appWidgetId) {
		return getPreferences(context).getBoolean(
				PREF_BATTERY_KEY + appWidgetId, false);
	}
	
	public static int getTransparency(Context context, int appWidgetId) {
		return getPreferences(context).getInt(
				PREF_TRANSPARENCY_KEY + appWidgetId, 100);
	}

	public static boolean getShowBatteryNotif(Context context) {
		return getPreferences(context)
				.getBoolean(PREF_BATTERY_NOTIF_KEY, false);
	}
	
	public static int getRefreshInterval(Context context, int appWidgetId) {
		return getPreferences(context).getInt(
				PREF_REFRESH_INTERVAL_KEY + appWidgetId, 3);
	}
	
	public static int getTempScale(Context context, int appWidgetId) {
		return getPreferences(context).getInt(
				PREF_TEMP_SCALE_KEY + appWidgetId, 0);
	}
	
	public static float getLocationLat(Context context, int appWidgetId) {
		return getPreferences(context).getFloat(
				PREF_LOCATION_LAT_KEY + appWidgetId, -222);
	}
	
	public static float getLocationLon(Context context, int appWidgetId) {
		return getPreferences(context).getFloat(
				PREF_LOCATION_LON_KEY + appWidgetId, -222);
	}
	
	public static String getLocation(Context context, int appWidgetId) {
		return getPreferences(context).getString(
				PREF_LOCATION_KEY + appWidgetId, "");
	}
	
	public static long getLocationId(Context context, int appWidgetId) {
		return getPreferences(context).getLong(
				PREF_LOCATION_ID_KEY + appWidgetId, -1);
	}
	
	public static long getLastAlarm(Context context, int appWidgetId) {
		return getPreferences(context).getLong(
				PREF_LAST_ALARM_KEY + appWidgetId, 0);
	}
	
	public static long getLastRefresh(Context context, int appWidgetId) {
		return getPreferences(context).getLong(
				PREF_LAST_REFRESH_KEY + appWidgetId, 0);
	}
	
	public static boolean getWeatherSuccess(Context context, int appWidgetId) {
		return getPreferences(context).getBoolean(
				PREF_WEATHER_SUCCESS + appWidgetId, true);
	}
	
	public static boolean getForecastSuccess(Context context, int appWidgetId) {
		return getPreferences(context).getBoolean(
				PREF_FORECAST_SUCCESS + appWidgetId, true);
	}
	
	public static boolean getRefreshWiFiOnly(Context context, int appWidgetId) {
		return getPreferences(context).getBoolean(
				PREF_REFRESH_WIFI_ONLY + appWidgetId, true);
	}
	
	public static int getClockSkin(Context context, int appWidgetId) {
		return getPreferences(context).getInt(
				PREF_CLOCK_SKIN + appWidgetId, 0);
	}
	
	public static int getSoundOptions(Context context) {
		return getPreferences(context)
				.getInt(PREF_SOUND_OPTIONS_KEY, 0);
	}
	
	public static int getWeatherIcons(Context context, int appWidgetId) {
		return getPreferences(context).getInt(
				PREF_WEATHER_ICONS_KEY + appWidgetId, 0);
	}
	
	public static int getFontItem(Context context, int appWidgetId) {
		return getPreferences(context).getInt(PREF_FONT_KEY + appWidgetId, 0);
	}

	public static void setShowDate(Context context, int appWidgetId,
			boolean value) {
		getPreferences(context).edit()
				.putBoolean(PREF_DATE_KEY + appWidgetId, value).commit();
	}

	public static void setShow24Hrs(Context context, int appWidgetId,
			boolean value) {
		getPreferences(context).edit()
				.putBoolean(PREF_24HRS_KEY + appWidgetId, value).commit();
	}

	public static void setColorItem(Context context, int appWidgetId, int value) {
		getPreferences(context).edit()
				.putInt(PREF_COLOR_KEY + appWidgetId, value).commit();
	}

	public static void setDateFormatItem(Context context, int appWidgetId,
			int value) {
		getPreferences(context).edit()
				.putInt(PREF_DATEFORMAT_KEY + appWidgetId, value).commit();
	}

	public static void setShowBattery(Context context, int appWidgetId,
			boolean value) {
		getPreferences(context).edit()
				.putBoolean(PREF_BATTERY_KEY + appWidgetId, value).commit();
	}
	
	public static void setTransparency(Context context, int appWidgetId,
			int value) {
		getPreferences(context).edit()
				.putInt(PREF_TRANSPARENCY_KEY + appWidgetId, value).commit();
	}

	public static void setShowBatteryNotif(Context context, boolean value) {
		getPreferences(context).edit()
				.putBoolean(PREF_BATTERY_NOTIF_KEY, value).commit();
	}
	
	public static void setRefreshInterval(Context context, int appWidgetId,
			int value) {
		getPreferences(context).edit()
				.putInt(PREF_REFRESH_INTERVAL_KEY + appWidgetId, value).commit();
	}
	
	public static void setTempScale(Context context, int appWidgetId,
			int value) {
		getPreferences(context).edit()
				.putInt(PREF_TEMP_SCALE_KEY + appWidgetId, value).commit();
	}
	
	public static void setLocationLat(Context context, int appWidgetId,
			float value) {
		getPreferences(context).edit()
				.putFloat(PREF_LOCATION_LAT_KEY + appWidgetId, value).commit();
	}
	
	public static void setLocationLon(Context context, int appWidgetId,
			float value) {
		getPreferences(context).edit()
				.putFloat(PREF_LOCATION_LON_KEY + appWidgetId, value).commit();
	}
	
	public static void setLocation(Context context, int appWidgetId,
			String value) {
		getPreferences(context).edit()
				.putString(PREF_LOCATION_KEY + appWidgetId, value).commit();
	}
	
	public static void setLocationId(Context context, int appWidgetId,
			long value) {
		getPreferences(context).edit()
				.putLong(PREF_LOCATION_ID_KEY + appWidgetId, value).commit();
	}
	
	public static void setLastAlarm(Context context, int appWidgetId,
			long value) {
		getPreferences(context).edit()
				.putLong(PREF_LAST_ALARM_KEY + appWidgetId, value).commit();
	}
	
	public static void setLastRefresh(Context context, int appWidgetId,
			long value) {
		getPreferences(context).edit()
				.putLong(PREF_LAST_REFRESH_KEY + appWidgetId, value).commit();
	}
	
	public static void setWeatherSuccess(Context context, int appWidgetId,
			boolean value) {
		getPreferences(context).edit()
				.putBoolean(PREF_WEATHER_SUCCESS + appWidgetId, value).commit();
	}
	
	public static void setForecastSuccess(Context context, int appWidgetId,
			boolean value) {
		getPreferences(context).edit()
				.putBoolean(PREF_FORECAST_SUCCESS + appWidgetId, value).commit();
	}
	
	public static void setRefreshWiFiOnly(Context context, int appWidgetId,
			boolean value) {
		getPreferences(context).edit()
				.putBoolean(PREF_REFRESH_WIFI_ONLY + appWidgetId, value).commit();
	}
	
	public static void setClockSkin(Context context, int appWidgetId,
			int value) {
		getPreferences(context).edit()
				.putInt(PREF_CLOCK_SKIN + appWidgetId, value).commit();
	}
	
	public static void setSoundOptions(Context context, int value) {
		getPreferences(context).edit()
				.putInt(PREF_SOUND_OPTIONS_KEY, value).commit();
	}
	
	public static void setWeatherIcons(Context context, int appWidgetId,
			int value) {
		getPreferences(context).edit()
				.putInt(PREF_WEATHER_ICONS_KEY + appWidgetId, value).commit();
	}
	
	public static void setFontItem(Context context, int appWidgetId, int value) {
		getPreferences(context).edit()
				.putInt(PREF_FONT_KEY + appWidgetId, value).commit();
	}
}
