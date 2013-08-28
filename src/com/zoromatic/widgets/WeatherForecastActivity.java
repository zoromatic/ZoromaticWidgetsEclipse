package com.zoromatic.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.zoromatic.sunrisesunset.SunriseSunsetCalculator;
import com.zoromatic.sunrisesunset.dto.SunriseSunsetLocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class WeatherForecastActivity extends Activity {

	private static String LOG_TAG = "WeatherForecastActivity";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read the appWidgetId to configure from the incoming intent
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        
        setContentView(R.layout.weatherforecast);          
        readCachedData(mAppWidgetId);
    }
	
	void readCachedData(int appWidgetId) {
		readCachedWeatherData(appWidgetId);
		readCachedForecastData(appWidgetId);
	}
	
	void readCachedWeatherData(int appWidgetId) {
		Log.d(LOG_TAG, "WeatherForecastActivity readCachedWeatherData appWidgetId: " + appWidgetId);

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
            
            parseWeatherData(appWidgetId, result.toString(), true, false);
            
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	void parseWeatherData(int appWidgetId, String parseString, boolean updateFromCache, boolean scheduledUpdate) {
		Log.d(LOG_TAG, "WeatherForecastActivity parseWeatherData appWidgetId: " + appWidgetId + " cache: " + updateFromCache + " scheduled: " + scheduledUpdate);

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
            
    		TextView text = (TextView) findViewById(R.id.textViewLocToday);
    		text.setText(spStrLoc);
    		
    		long timestamp = weatherJSON.getLong("dt");
            Date time = new Date(timestamp * 1000);
            
            JSONObject main = null;
            try {
                main = weatherJSON.getJSONObject("main");
            } catch (JSONException e) {	                
            }
            try {
                double currentTemp = main.getDouble("temp") - 273.15;
                text = (TextView) findViewById(R.id.textViewTempToday);
        		
                if (tempScale == 1)
                	text.setText(String.valueOf((int)(currentTemp*1.8+32)) + "�");
                else
                	text.setText(String.valueOf((int)currentTemp) + "�");
                
                
            } catch (JSONException e) {	                
            }
            
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
                    
                    text = (TextView) findViewById(R.id.textViewDescToday);        		
            		text.setText(weatherMain);
            		
            		ImageView image = (ImageView)findViewById(R.id.imageViewWeatherToday);
            		
            		WeatherConditions conditions = new WeatherConditions();
                     
                    int icons = Preferences.getWeatherIcons(this, appWidgetId);
                    int resource = R.drawable.tick_weather_04d;
                    WeatherIcon[] imageArr;
                     
                    switch (icons) {
 					case 0:
 						resource = R.drawable.tick_weather_04d;
 						imageArr = conditions.m_ImageArrTick;
 						break;
 					case 1:
 						resource = R.drawable.touch_weather_04d;
 						imageArr = conditions.m_ImageArrTouch;
 						break;
 					case 2:
 						resource = R.drawable.icon_set_weather_04d;
 						imageArr = conditions.m_ImageArrIconSet;
 						break;
 					case 3:
 						resource = R.drawable.weezle_weather_04d;
 						imageArr = conditions.m_ImageArrWeezle;
 						break;
 					default:
 						resource = R.drawable.tick_weather_04d;
 						imageArr = conditions.m_ImageArrTick;
 						break;
 					}
            		
            		image.setImageResource(resource);
            		
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
                    
                    for (int j=0; j<imageArr.length; j++) {
                    	if (iconName.equals(imageArr[j].iconName) || iconNameAlt.equals(imageArr[j].iconName)) {
                    		if (imageArr[j].bDay != bDay)
                    			image.setImageResource(imageArr[j].altIconId);
                    		else
                    			image.setImageResource(imageArr[j].iconId);
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
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	void readCachedForecastData(int appWidgetId) {
		Log.d(LOG_TAG, "WidgetUpdateService readCachedForecastData appWidgetId: " + appWidgetId);

		try {
			File parentDirectory = new File(this.getFilesDir().getAbsolutePath());
            
            if (!parentDirectory.exists()) {
                Log.e(LOG_TAG, "Cache file parent directory does not exist.");
                
                if(!parentDirectory.mkdirs()) {
                	Log.e(LOG_TAG, "Cannot create cache file parent directory."); 
                }
            }

            File cacheFile = new File(parentDirectory, "forecast_cache_"+appWidgetId);
            
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
            
            parseForecastData(appWidgetId, result.toString(), true, false);
            
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	void parseForecastData(int appWidgetId, String parseString, boolean updateFromCache, boolean scheduledUpdate) {
		Log.d(LOG_TAG, "WidgetUpdateService parseForecastData appWidgetId: " + appWidgetId + " cache: " + updateFromCache + " scheduled: " + scheduledUpdate);

		if (parseString.equals(null) || parseString.equals(""))
			return;
		
		JSONTokener parser = new JSONTokener(parseString);
		int tempScale = Preferences.getTempScale(this, appWidgetId);
        try {
			JSONObject query = (JSONObject)parser.nextValue();
			
			JSONArray list = query.getJSONArray("list");
			
            if (list.length() == 0) {
                return;
            }
            
            for (int i=1; i<list.length(); i++) {
            	if (i == 6)
            		break;
            	
            	JSONObject weatherJSON = list.getJSONObject(i);
            	long timestamp = weatherJSON.getLong("dt");
            	
            	Calendar calendar = Calendar.getInstance();
            	calendar.setTimeInMillis(timestamp*1000);
            	
            	TextView textDate = null;
            	TextView textTempHigh = null;
            	TextView textTempLow = null;
            	TextView textMain = null;
        		ImageView image = null;
        		
        		WeatherConditions conditions = new WeatherConditions();
                
                int icons = Preferences.getWeatherIcons(this, appWidgetId);
                int iconID = R.drawable.tick_weather_04d;
                WeatherIcon[] imageArr;
                 
                switch (icons) {
				case 0:
					iconID = R.drawable.tick_weather_04d;
					imageArr = conditions.m_ImageArrTick;
					break;
				case 1:
					iconID = R.drawable.touch_weather_04d;
					imageArr = conditions.m_ImageArrTouch;
					break;
				case 2:
					iconID = R.drawable.icon_set_weather_04d;
					imageArr = conditions.m_ImageArrIconSet;
					break;
				case 3:
					iconID = R.drawable.weezle_weather_04d;
					imageArr = conditions.m_ImageArrWeezle;
					break;
				default:
					iconID = R.drawable.tick_weather_04d;
					imageArr = conditions.m_ImageArrTick;
					break;
				}
                
        		String sTempHigh = "";
        		String sTempLow = "";
        		String weatherMain = "";
            	String date = "";
            	
        		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
        		date = String.format(sdf.format(calendar.getTime()));   
        		
        		JSONObject tempJSON = null;
                try {
                	tempJSON = weatherJSON.getJSONObject("temp");
                } catch (JSONException e) {	                
                }
                try {
                	double temp = tempJSON.getDouble("max") - 273.15;           	
                	
                	if (tempScale == 1)
                		sTempHigh = "H: " + String.valueOf((int)(temp*1.8+32)) + "�";
                    else
                    	sTempHigh = "H: " + String.valueOf((int)temp) + "�";	
                	
                	temp = tempJSON.getDouble("min") - 273.15;           	
                	
                	if (tempScale == 1)
                		sTempLow = "L: " + String.valueOf((int)(temp*1.8+32)) + "�";
                    else
                    	sTempLow = "L: " + String.valueOf((int)temp) + "�";
                } catch (JSONException e) {
                }                        	        	
            	
            	try {
                    JSONArray weathers = weatherJSON.getJSONArray("weather");
                    
                    JSONObject weather = weathers.getJSONObject(0);
                    int weatherId = weather.getInt("id");
                    weatherMain = weather.getString("main");
                    String iconName = weather.getString("icon");
                    String iconNameAlt = iconName + "d";
                    
                    for (int k=0; k<imageArr.length; k++) {
                    	if (iconName.equals(imageArr[k].iconName) || iconNameAlt.equals(imageArr[k].iconName)) {
                    		iconID = imageArr[k].iconId;
                    		break;
                    	}
                    }                    
                } catch (JSONException e) {
                    //no weather type
                }
            	
            	switch (i) {
				case 1:
					textDate = (TextView) findViewById(R.id.textViewDate1);        		
					textTempHigh = (TextView) findViewById(R.id.textViewTempHigh1);        		
					textTempLow = (TextView) findViewById(R.id.textViewTempLow1);        		
	        		textMain = (TextView) findViewById(R.id.textViewDesc1);        		
            		image = (ImageView)findViewById(R.id.imageViewWeather1);            	
            		break;
				case 2:
					textDate = (TextView) findViewById(R.id.textViewDate2);        		
					textTempHigh = (TextView) findViewById(R.id.textViewTempHigh2);        		
					textTempLow = (TextView) findViewById(R.id.textViewTempLow2);        		
					textMain = (TextView) findViewById(R.id.textViewDesc2);        		
            		image = (ImageView)findViewById(R.id.imageViewWeather2);
					break;
				case 3:
					textDate = (TextView) findViewById(R.id.textViewDate3);        		
					textTempHigh = (TextView) findViewById(R.id.textViewTempHigh3);        		
					textTempLow = (TextView) findViewById(R.id.textViewTempLow3);        		
					textMain = (TextView) findViewById(R.id.textViewDesc3);        		
            		image = (ImageView)findViewById(R.id.imageViewWeather3);
					break;
				case 4:
					textDate = (TextView) findViewById(R.id.textViewDate4);        		
					textTempHigh = (TextView) findViewById(R.id.textViewTempHigh4);        		
					textTempLow = (TextView) findViewById(R.id.textViewTempLow4);        		
					textMain = (TextView) findViewById(R.id.textViewDesc4);        		
            		image = (ImageView)findViewById(R.id.imageViewWeather4);
					break;
				case 5:
					textDate = (TextView) findViewById(R.id.textViewDate5);        		
					textTempHigh = (TextView) findViewById(R.id.textViewTempHigh5);        		
					textTempLow = (TextView) findViewById(R.id.textViewTempLow5);        		
					textMain = (TextView) findViewById(R.id.textViewDesc5);        		
            		image = (ImageView)findViewById(R.id.imageViewWeather5);
					break;
				default:
					break;
				}
            	
            	textDate.setText(date);
            	textTempHigh.setText(sTempHigh);
            	textTempLow.setText(sTempLow);
            	textMain.setText(weatherMain);
            	image.setImageResource(iconID);
            }
            		            
            if (!updateFromCache)
            {
            	Preferences.setLastRefresh(this, appWidgetId, System.currentTimeMillis()); 
            	
            	if (scheduledUpdate)
            		Preferences.setForecastSuccess(this, appWidgetId, true);
            }            
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static long daysBetween(final Calendar startDate, final Calendar endDate) {  
		 int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;  
		 long endInstant = endDate.getTimeInMillis();  
		 int presumedDays = (int) ((endInstant - startDate.getTimeInMillis()) / MILLIS_IN_DAY);  
		 Calendar cursor = (Calendar) startDate.clone();  
		 cursor.add(Calendar.DAY_OF_YEAR, presumedDays);  
		 long instant = cursor.getTimeInMillis();  
		 if (instant == endInstant)  
		  return presumedDays;  
		 final int step = instant < endInstant ? 1 : -1;  
		 do {  
		  cursor.add(Calendar.DAY_OF_MONTH, step);  
		  presumedDays += step;  
		 } while (cursor.getTimeInMillis() != endInstant);  
		 return presumedDays;  
	}  
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.weatherforecastmenu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.refresh:
	    	float lat = Preferences.getLocationLat(getApplicationContext(), mAppWidgetId);
        	float lon = Preferences.getLocationLon(getApplicationContext(), mAppWidgetId);
        	long id = Preferences.getLocationId(getApplicationContext(), mAppWidgetId);
        	
        	if ((id == -1) && (lat == -222 || lon == -222 || Float.isNaN(lat) || Float.isNaN(lon))) {
        		Toast.makeText(getApplicationContext(), "No location defined.", Toast.LENGTH_LONG).show();
        	}
        	else {
		    	Intent refreshIntent = new Intent(getApplicationContext(), WidgetUpdateService.class);
	            refreshIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, WidgetUpdateService.WEATHER_UPDATE);
	            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	            getApplicationContext().startService(refreshIntent);            
	            Toast.makeText(getApplicationContext(), "Updating weather.", Toast.LENGTH_LONG).show();
	            
	            finish();
        	}
            
	    	return true;
	    case R.id.settings:
	    	Intent settingsIntent = new Intent(this, DigitalClockAppWidgetPreferences.class);
			settingsIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			startActivity(settingsIntent);
			
			finish();
			
	    	return true;
	    default:
	    	return super.onOptionsItemSelected(item);
		}
	}
}
