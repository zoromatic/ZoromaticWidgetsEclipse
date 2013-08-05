package com.zoromatic.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("deprecation")
public class DigitalClockAppWidgetPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
		
		PreferenceManager localPrefs = getPreferenceManager();
        localPrefs.setSharedPreferencesName(Preferences.PREFS_NAME);
        
        addPreferencesFromResource(R.xml.digitalclockwidget_prefs);
        
        CheckBoxPreference show24hrs = (CheckBoxPreference)findPreference(Preferences.PREF_24HRS_KEY);
        
        if (show24hrs != null)
        	show24hrs.setChecked(Preferences.getShow24Hrs(this, mAppWidgetId));
        
        ListPreference color = (ListPreference)findPreference(Preferences.PREF_COLOR_KEY);
        
        if (color != null)
        {
        	color.setValueIndex(Preferences.getColorItem(this, mAppWidgetId));
        	
        	int iColorItem = color.findIndexOfValue(color.getValue());
        	Preferences.setColorItem(this, mAppWidgetId, iColorItem);
        	
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
        	
        	SpannableString summary = new SpannableString ( color.getEntries()[Preferences.getColorItem(this, mAppWidgetId)] );
        	summary.setSpan( new ForegroundColorSpan( systemColor ), 0, summary.length(), 0 );
        	//summary.setSpan( new BackgroundColorSpan( systemColor ), 0, summary.length(), 0 );
        	
        	color.setSummary(summary);                
        }
        
        ListPreference clockSkin = (ListPreference)findPreference(Preferences.PREF_CLOCK_SKIN);
        
        if (clockSkin != null)
        {
        	clockSkin.setValueIndex(Preferences.getClockSkin(this, mAppWidgetId));
        	clockSkin.setSummary(clockSkin.getEntries()[Preferences.getClockSkin(this, mAppWidgetId)]);
        }
        
        CheckBoxPreference showDate = (CheckBoxPreference)findPreference(Preferences.PREF_DATE_KEY);
        
        if (showDate != null)
        	showDate.setChecked(Preferences.getShowDate(this, mAppWidgetId));
        
        ListPreference dateFormat = (ListPreference)findPreference(Preferences.PREF_DATEFORMAT_KEY);
        
        if (dateFormat != null)
        {
        	dateFormat.setValueIndex(Preferences.getDateFormatItem(this, mAppWidgetId));
        	dateFormat.setSummary(dateFormat.getEntries()[Preferences.getDateFormatItem(this, mAppWidgetId)]);
        }
        
        CheckBoxPreference showBattery = (CheckBoxPreference)findPreference(Preferences.PREF_BATTERY_KEY);
        
        if (showBattery != null)
        	showBattery.setChecked(Preferences.getShowBattery(this, mAppWidgetId));	
        
        SeekBarPreference transparency = (SeekBarPreference)findPreference(Preferences.PREF_TRANSPARENCY_KEY);
        
        if (transparency != null)
        {
        	transparency.setProgress(Preferences.getTransparency(this, mAppWidgetId));
        	transparency.setSummary(String.valueOf(Preferences.getTransparency(this, mAppWidgetId))+" %");
        }
        
        ListPreference refreshInterval = (ListPreference)findPreference(Preferences.PREF_REFRESH_INTERVAL_KEY);
        
        if (refreshInterval != null)
        {
        	refreshInterval.setValueIndex(Preferences.getRefreshInterval(this, mAppWidgetId));
        	refreshInterval.setSummary(refreshInterval.getEntries()[Preferences.getRefreshInterval(this, mAppWidgetId)]);
        }
        
        ListPreference tempScale = (ListPreference)findPreference(Preferences.PREF_TEMP_SCALE_KEY);
        
        if (tempScale != null)
        {
        	tempScale.setValueIndex(Preferences.getTempScale(this, mAppWidgetId));
        	tempScale.setSummary(tempScale.getEntries()[Preferences.getTempScale(this, mAppWidgetId)]);
        }
        
        Preference refreshNow = findPreference(Preferences.PREF_REFRESH_NOW_KEY);
        
        if (refreshNow != null)
        {
        	refreshNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
				public boolean onPreferenceClick(Preference p) {
                	float lat = Preferences.getLocationLat(getApplicationContext(), mAppWidgetId);
                	float lon = Preferences.getLocationLon(getApplicationContext(), mAppWidgetId);
                	long id = Preferences.getLocationId(getApplicationContext(), mAppWidgetId);
                	
                	if ((id == -1) && (lat == -222 || lon == -222 || Float.isNaN(lat) || Float.isNaN(lon))) {
                		Toast.makeText(getApplicationContext(), "No location defined.", Toast.LENGTH_LONG).show();
                	}
                	else {
                		Intent startIntent = new Intent(getApplicationContext(), WidgetUpdateService.class);
                        startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, WidgetUpdateService.WEATHER_UPDATE);
                        startIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

                        getApplicationContext().startService(startIntent);
                        
	        	        Toast.makeText(getApplicationContext(), "Updating weather.", Toast.LENGTH_LONG).show();
                	}
                	
                	return true;
                }
            }); 
        	
        	long lastrefresh = Preferences.getLastRefresh(this, mAppWidgetId);
        	
        	if (lastrefresh > 0) {
	        	boolean bShow24Hrs = Preferences.getShow24Hrs(this, mAppWidgetId);
	        	int iDateFormatItem = Preferences.getDateFormatItem(this, mAppWidgetId);
	        	Date resultdate = new Date(lastrefresh);
	        	
	        	String currentTime;
	
	    		if (bShow24Hrs) {
	    			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	    			currentTime = String.format(sdf.format(resultdate));
	    		} else {
	    			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
	    			currentTime = String.format(sdf.format(resultdate));
	    		}
	    		
	    		String currentDate = "";
	    		String[] mTestArray = getResources().getStringArray(R.array.dateFormat);
	    		
				SimpleDateFormat sdf = new SimpleDateFormat(mTestArray[iDateFormatItem]);
				currentDate = String.format(sdf.format(resultdate));				    	
	    		
	        	refreshNow.setSummary(getResources().getString(R.string.lastrefresh) + " " + currentDate + ", " + currentTime);
        	}
        	else {
        		refreshNow.setSummary(getResources().getString(R.string.lastrefreshnever));
        	}
        }
        
        PreferenceScreen locationScreen = (PreferenceScreen) findPreference(Preferences.PREF_LOCATION_SETTINGS_KEY);
        
        if (locationScreen != null)
        {
        	locationScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
				public boolean onPreferenceClick(Preference p) {
                	Intent locationIntent = new Intent(DigitalClockAppWidgetPreferences.this, ConfigureLocationActivity.class);
                	locationIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                	DigitalClockAppWidgetPreferences.this.startActivityForResult(locationIntent, 0);
                	
                    return true;
                }                              
            });
        	
        	locationScreen.setSummary(Preferences.getLocation(this, mAppWidgetId));    	    
        }
        
        Preference openWeather = findPreference(Preferences.PREF_OPENWEATHER_KEY);
        
        if (openWeather != null)
        {
        	openWeather.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
				public boolean onPreferenceClick(Preference p) {
                	String url = getResources().getString(R.string.openweathermaplink);
                	if (!url.startsWith("http://") && !url.startsWith("https://"))
                		   url = "http://" + url;
                	
                	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                	startActivity(browserIntent);
                    return true;
                }
            });        	        	
        }
        
        CheckBoxPreference refreshWiFiOnly = (CheckBoxPreference)findPreference(Preferences.PREF_REFRESH_WIFI_ONLY);
        
        if (refreshWiFiOnly != null)
        {
        	boolean bWiFiOnly = Preferences.getRefreshWiFiOnly(this, mAppWidgetId);
        	refreshWiFiOnly.setChecked(bWiFiOnly);
        	
        	String connection = "";
        	
        	if (bWiFiOnly)
        		connection = getResources().getString(R.string.refreshwifionlyconnection);
        	else
        		connection = getResources().getString(R.string.refreshanyconnection);
        	
        	refreshWiFiOnly.setSummary(connection);
        }              
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == 0) {
            if (resultCode == RESULT_OK){            	
            	PreferenceScreen locationScreen = (PreferenceScreen) findPreference(Preferences.PREF_LOCATION_SETTINGS_KEY);
                
                if (locationScreen != null)
                	locationScreen.setSummary(Preferences.getLocation(this, mAppWidgetId));                
            }
        }
    }
		
	
	@Override
    public void onBackPressed() {

		CheckBoxPreference show24hrs = (CheckBoxPreference)findPreference(Preferences.PREF_24HRS_KEY);
        
        if (show24hrs != null)
        	Preferences.setShow24Hrs(this, mAppWidgetId, show24hrs.isChecked());        	
        
        ListPreference color = (ListPreference)findPreference(Preferences.PREF_COLOR_KEY);
        
        if (color != null)
        	Preferences.setColorItem(this, mAppWidgetId, color.findIndexOfValue(color.getValue()));  
        
        ListPreference clockSkin = (ListPreference)findPreference(Preferences.PREF_CLOCK_SKIN);
        
        if (clockSkin != null)
        	Preferences.setClockSkin(this, mAppWidgetId, clockSkin.findIndexOfValue(clockSkin.getValue()));
        
        CheckBoxPreference showDate = (CheckBoxPreference)findPreference(Preferences.PREF_DATE_KEY);
        
        if (showDate != null)
        	Preferences.setShowDate(this, mAppWidgetId, showDate.isChecked());
        
        ListPreference dateFormat = (ListPreference)findPreference(Preferences.PREF_DATEFORMAT_KEY);
        
        if (dateFormat != null)
        	Preferences.setDateFormatItem(this, mAppWidgetId, dateFormat.findIndexOfValue(dateFormat.getValue()));
        
        CheckBoxPreference showBattery = (CheckBoxPreference)findPreference(Preferences.PREF_BATTERY_KEY);
        
        if (showBattery != null)
        	Preferences.setShowBattery(this, mAppWidgetId, showBattery.isChecked());
        
        SeekBarPreference transparency = (SeekBarPreference)findPreference(Preferences.PREF_TRANSPARENCY_KEY);
        
        if (transparency != null)
        {
        	Preferences.setTransparency(this, mAppWidgetId, transparency.getProgress());
        }
        
        ListPreference refreshInterval = (ListPreference)findPreference(Preferences.PREF_REFRESH_INTERVAL_KEY);
        
        if (refreshInterval != null)
        	Preferences.setRefreshInterval(this, mAppWidgetId, refreshInterval.findIndexOfValue(refreshInterval.getValue()));
        
        ListPreference tempScale = (ListPreference)findPreference(Preferences.PREF_TEMP_SCALE_KEY);
        
        if (tempScale != null)
        	Preferences.setTempScale(this, mAppWidgetId, tempScale.findIndexOfValue(tempScale.getValue()));
        
        CheckBoxPreference refreshWiFiOnly = (CheckBoxPreference)findPreference(Preferences.PREF_REFRESH_WIFI_ONLY);
        
        if (refreshWiFiOnly != null)
        	Preferences.setRefreshWiFiOnly(this, mAppWidgetId, refreshWiFiOnly.isChecked());
		
		Intent startIntent = new Intent(this, WidgetUpdateService.class);
        startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, Intent.ACTION_TIME_CHANGED);
        startIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        this.startService(startIntent);             
        
        setResult(RESULT_OK, startIntent);
        
		finish();
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	   
		if (key.equals(Preferences.PREF_24HRS_KEY)) {
			CheckBoxPreference show24hrs = (CheckBoxPreference)findPreference(Preferences.PREF_24HRS_KEY);
	        
	        if (show24hrs != null)
	        	Preferences.setShow24Hrs(this, mAppWidgetId, show24hrs.isChecked());
		}
		
		if (key.equals(Preferences.PREF_COLOR_KEY)) {
			ListPreference color = (ListPreference)findPreference(Preferences.PREF_COLOR_KEY);
	        
	        if (color != null)
	        {
	        	int iColorItem = color.findIndexOfValue(color.getValue());
	        	Preferences.setColorItem(this, mAppWidgetId, iColorItem);
	        	
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
	        	
	        	SpannableString summary = new SpannableString ( color.getEntries()[Preferences.getColorItem(this, mAppWidgetId)] );
	        	summary.setSpan( new ForegroundColorSpan( systemColor ), 0, summary.length(), 0 );
	        	//summary.setSpan( new BackgroundColorSpan( systemColor ), 0, summary.length(), 0 );
	        	
	        	color.setSummary(summary);
	        }
		}
		
		if (key.equals(Preferences.PREF_CLOCK_SKIN)) {
			ListPreference clockSkin = (ListPreference)findPreference(Preferences.PREF_CLOCK_SKIN);
	        
	        if (clockSkin != null)
	        {
	        	Preferences.setClockSkin(this, mAppWidgetId, clockSkin.findIndexOfValue(clockSkin.getValue()));
	        	clockSkin.setSummary(clockSkin.getEntries()[Preferences.getClockSkin(this, mAppWidgetId)]);
	        }
		}
        
		if (key.equals(Preferences.PREF_DATE_KEY)) {
			CheckBoxPreference showDate = (CheckBoxPreference)findPreference(Preferences.PREF_DATE_KEY);
	        
	        if (showDate != null)
	        	Preferences.setShowDate(this, mAppWidgetId, showDate.isChecked());
		}
        
		if (key.equals(Preferences.PREF_DATEFORMAT_KEY)) {
			ListPreference dateFormat = (ListPreference)findPreference(Preferences.PREF_DATEFORMAT_KEY);
	        
	        if (dateFormat != null)
	        {
	        	Preferences.setDateFormatItem(this, mAppWidgetId, dateFormat.findIndexOfValue(dateFormat.getValue()));
	        	dateFormat.setSummary(dateFormat.getEntries()[Preferences.getDateFormatItem(this, mAppWidgetId)]);
	        }
		}
		
		if (key.equals(Preferences.PREF_BATTERY_KEY)) {
			CheckBoxPreference showBattery = (CheckBoxPreference)findPreference(Preferences.PREF_BATTERY_KEY);
	        
	        if (showBattery != null)
	        	Preferences.setShowBattery(this, mAppWidgetId, showBattery.isChecked());
		}
		
		if (key.equals(Preferences.PREF_TRANSPARENCY_KEY)) {
			SeekBarPreference transparency = (SeekBarPreference)findPreference(Preferences.PREF_TRANSPARENCY_KEY);
	        
	        if (transparency != null)
	        {
	        	Preferences.setTransparency(this, mAppWidgetId, transparency.getProgress());
	        	transparency.setSummary(String.valueOf(transparency.getProgress())+" %");
	        }
		}
		
		if (key.equals(Preferences.PREF_REFRESH_INTERVAL_KEY)) {
			ListPreference refreshInterval = (ListPreference)findPreference(Preferences.PREF_REFRESH_INTERVAL_KEY);
	        
	        if (refreshInterval != null)
	        {
	        	Preferences.setRefreshInterval(this, mAppWidgetId, refreshInterval.findIndexOfValue(refreshInterval.getValue()));
	        	refreshInterval.setSummary(refreshInterval.getEntries()[Preferences.getRefreshInterval(this, mAppWidgetId)]);
	        	
	        	DigitalClockAppWidgetProvider.setAlarm(this, mAppWidgetId);
	        }
		}
		
		if (key.equals(Preferences.PREF_TEMP_SCALE_KEY)) {
			ListPreference tempScale = (ListPreference)findPreference(Preferences.PREF_TEMP_SCALE_KEY);
	        
	        if (tempScale != null)
	        {
	        	Preferences.setTempScale(this, mAppWidgetId, tempScale.findIndexOfValue(tempScale.getValue()));
	        	tempScale.setSummary(tempScale.getEntries()[Preferences.getTempScale(this, mAppWidgetId)]);
	        }
		}
		
		if (key.equals(Preferences.PREF_LOCATION_SETTINGS_KEY)) {
			ListPreference location = (ListPreference)findPreference(Preferences.PREF_LOCATION_SETTINGS_KEY);
	        
	        if (location != null)
	        {
	        	location.setSummary(Preferences.getLocation(this, mAppWidgetId));
	        }
		}
		
		if (key.equals(Preferences.PREF_REFRESH_WIFI_ONLY)) {
			CheckBoxPreference refreshWiFiOnly = (CheckBoxPreference)findPreference(Preferences.PREF_REFRESH_WIFI_ONLY);
	        
	        if (refreshWiFiOnly != null) {
	        	boolean bWiFiOnly = refreshWiFiOnly.isChecked();
	        	
	        	Preferences.setRefreshWiFiOnly(this, mAppWidgetId, bWiFiOnly);
	        	
	        	String connection = "";
	        	
	        	if (bWiFiOnly)
	        		connection = getResources().getString(R.string.refreshwifionlyconnection);
	        	else
	        		connection = getResources().getString(R.string.refreshanyconnection);
	        	
	        	refreshWiFiOnly.setSummary(connection);	        	
	        }
		}
	}
		    
    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);	       	        
    }	     
}