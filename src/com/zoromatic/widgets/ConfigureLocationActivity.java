/*
 * Copyright (C) 2009 Jeff Sharkey, http://jsharkey.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zoromatic.widgets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
//import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Activity to configure forecast widgets. Usually launched automatically by an
 * {@link AppWidgetHost} after the {@link AppWidgetManager#EXTRA_APPWIDGET_ID}
 * has been bound to a widget.
 */
public class ConfigureLocationActivity extends Activity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    public static final String LOG_TAG = "ConfigureLocationActivity";

    //private Button mButtonMap;
    //private Button mButtonSave;
    private ImageButton mButtonSearch;
    private EditText mEditLocation;
    private RadioButton mRadioCurrent;
    private RadioButton mRadioManual;    

    private double mLat = Double.NaN;
    private double mLon = Double.NaN;
    private String mLocation = "";
    private long mLocationID = -1;
    
    private double mLatPref = Double.NaN;
    private double mLonPref = Double.NaN;
    private String mLocationPref = "";
    private long mLocationIDPref = -1;
    
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String LOC = "loc";
    public static final String ID = "id";
    
    public static final String LAST_UPDATED = "lastUpdated";
    public static final String CONFIGURED = "configured";
    public static final int CONFIGURED_TRUE = 1;
    public static int mSelectedGeocode = 0;
    public static int mSelectedOpen = 0;
    AlertDialog mAlert;
    
    public static final String AUTHORITY = "com.zoromatic.widgets";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/appwidgets");
    public static String GET_CITY_URL = "http://api.openweathermap.org/data/2.5/find?q=%s&type=like&APPID=364a27c67e53df61c49db6e5bdf26aa5";

    /**
     * Default zoom level when showing map to verify location.
     */
    //private static final int ZOOM_LEVEL = 10;

    /**
     * Last found location fix, used when user selects "My current location."
     */
    private Location mLastFix;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    /**
     * Spawn a reverse geocoding operation to find names for the given
     * {@link Location}. Will update GUI when finished.
     */
    private void startGeocode(Location location) {
        new GeocoderTask().execute(new GeocodeQuery(location));
    }

    /**
     * Spawn a forward geocoding operation to find the location of a given name.
     * Will update GUI when finished.
     */
    private void startGeocode(String query) {
        new GeocoderTask().execute(new GeocodeQuery(query));
    }

    /**
     * Background task to perform a geocoding operation. Will disable GUI
     * actions while running in the background, and then update GUI with results
     * when found.
     * <p>
     * If no reverse geocoding results found, will still return original
     * coordinates but will leave suggested title empty.
     */
    private class GeocoderTask extends AsyncTask<GeocodeQuery, Void, GeocodeQuery> {
        private Geocoder mGeocoder;        
        ProgressDialog progressDialog;

        private GeocoderTask() {
            mGeocoder = new Geocoder(ConfigureLocationActivity.this);
        }

        @Override
		protected void onPreExecute() {
            // Show progress spinner and disable buttons
            setProgressBarIndeterminateVisibility(true);            
            setActionEnabled(false);
            
            progressDialog = ProgressDialog.show(ConfigureLocationActivity.this, "",
                    "Searching...");
        }

        @Override
		protected GeocodeQuery doInBackground(GeocodeQuery... args) {
            GeocodeQuery query = args[0];
            GeocodeQuery result = null;
            mSelectedGeocode = 0;

            try {
                if (!TextUtils.isEmpty(query.name)) {
                    // Forward geocode using query
                    final List<Address> results = mGeocoder.getFromLocationName(query.name, 5);
                    
                    int size = results.size();
                    for(int j=size-1; j>0; j--) 
                    {
                    	Address addr = results.get(j);
                    	
                    	if (addr == null) {
                    		results.remove(j);
                    		continue;
                    	}
                    	
                    	if (addr.getLocality() == null || addr.getCountryCode() == null) {
                    		results.remove(j);
                    		continue;
                    	}
                    }
                    
                    if (results.size() > 0) {
                    	selectLocation(results);
                    	                    	                                        	
                        result = new GeocodeQuery(results.get(mSelectedGeocode));
                    }
                } else if (!Double.isNaN(query.lat) && !Double.isNaN(query.lon)) {
                    // Reverse geocode using location
                    final List<Address> results = mGeocoder.getFromLocation(query.lat, query.lon, 5);
                    
                    int size = results.size();
                    for(int j=size-1; j>0; j--) 
                    {
                    	Address addr = results.get(j);
                    	
                    	if (addr == null) {
                    		results.remove(j);
                    		continue;
                    	}
                    	
                    	if (addr.getLocality() == null || addr.getCountryCode() == null) {
                    		results.remove(j);
                    		continue;
                    	}
                    }
                    
                    if (results.size() > 0) {
                    	selectLocation(results);
                    	
                        result = new GeocodeQuery(results.get(mSelectedGeocode));
                        result.lat = query.lat;
                        result.lon = query.lon;
                    } else {
                        result = query;
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem using geocoder", e);
            }

            return result;
        }
        
        private void selectLocation(final List<Address> results) {
        	ConfigureLocationActivity.this.runOnUiThread(new Runnable() {
        	    public void run() {
        	    	final CharSequence[] items = new CharSequence[results.size()];
                	
                	for (int i=0; i<results.size(); i++) {
                		Address addr = results.get(i);
                		
                		items[i] = addr.getLocality() + ", " + addr.getCountryCode();
                	}
                	
                	AlertDialog.Builder builder = new AlertDialog.Builder(ConfigureLocationActivity.this);
                	builder.setTitle(R.string.selectlocation);
                	
                	builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int item) {
                	    	mSelectedGeocode = item;
                	    	dialog.dismiss();
                	    }
                	});
                	
                	mAlert = builder.create();  
                	mAlert.show();
        	    }
        	});
        }

        @Override
		protected void onPostExecute(GeocodeQuery found) {
            setProgressBarIndeterminateVisibility(false);
            progressDialog.dismiss();

            // Update GUI with resolved string
            if (found == null) {
                mLat = Double.NaN;
                mLon = Double.NaN;
                mLocation = "";
                setActionEnabled(false);
                Toast.makeText(getApplicationContext(), "Location not found!", Toast.LENGTH_LONG).show();
            } else {
                mLat = found.lat;
                mLon = found.lon;
                mLocation = found.name;
                setActionEnabled(true);
            }
            
            mEditLocation.setText(mLocation);
        }
    }

    /**
     * Temporary object to hold geocoding query and/or results.
     */
    private static class GeocodeQuery {
        String name = null;

        double lat = Double.NaN;
        double lon = Double.NaN;        

        public GeocodeQuery(String query) {
            name = query;
        }

        public GeocodeQuery(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();            
        }

        /**
         * Summarize details of the given {@link Address}, walking down a
         * prioritized list of names until valid text is found to describe it.
         */
        public GeocodeQuery(Address address) {
            name = address.getLocality();
            if (name == null) {
                name = address.getFeatureName();
            }
            if (name == null) {
                name = address.getAdminArea();
            }
            if (name == null) {
                name = address.getPostalCode();
            }
            if (name == null) {
                name = address.getCountryName();
            }

            // Fill in coordinates, if given
            if (address.hasLatitude() && address.hasLongitude()) {
                lat = address.getLatitude();
                lon = address.getLongitude();
            }
        }
    }

    /**
     * Enable or disable any GUI actions, including text fields and buttons.
     */
    protected void setActionEnabled(boolean enabled) {
        //mButtonMap.setEnabled(enabled);
        //mButtonSave.setEnabled(enabled);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read the appWidgetId to configure from the incoming intent
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setConfigureResult(Activity.RESULT_CANCELED);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.configurelocation);
        
        mRadioCurrent = (RadioButton)findViewById(R.id.radioCurrent);
        mRadioManual = (RadioButton)findViewById(R.id.radioManual);
               
        mRadioCurrent.setOnClickListener(this);
        mRadioManual.setOnClickListener(this);

        //mButtonMap = (Button)findViewById(R.id.buttonMap);
        //mButtonSave = (Button)findViewById(R.id.buttonSave);
        mButtonSearch = (ImageButton)findViewById(R.id.buttonSearch);
        mEditLocation = (EditText)findViewById(R.id.editTextLocation);
        
        mEditLocation.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
              
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            	
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	String location = mEditLocation.getText().toString();
            	
            	if (!location.equals(mLocation))
            		setActionEnabled(false);
            }
         });

        //mButtonMap.setOnClickListener(this);
        //mButtonSave.setOnClickListener(this);
        mButtonSearch.setOnClickListener(this);

        // TODO: handle editing an existing widget by reading values
        mLatPref = Preferences.getLocationLat(this, mAppWidgetId);
        mLonPref = Preferences.getLocationLon(this, mAppWidgetId);
        mLocationPref = Preferences.getLocation(this, mAppWidgetId);
        mLocationIDPref = Preferences.getLocationId(this, mAppWidgetId);

        // If restoring, read location and units from bundle
        if (savedInstanceState != null) {
            mLat = savedInstanceState.getDouble(LAT);
            mLon = savedInstanceState.getDouble(LON);
            mLocation = savedInstanceState.getString(LOC);
            mLocationID = savedInstanceState.getLong(ID);
        }
        else {
        	mLat = mLatPref;
            mLon = mLonPref;
            mLocation = mLocationPref;
            mLocationID = mLocationIDPref;
        }
        
        mEditLocation.setText(mLocation);

        // Start listener to find current location
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);

        if (provider != null) {
            // Fire off geocoding request for last fix, but only if not restoring
            mLastFix = locationManager.getLastKnownLocation(provider);
            if (mLastFix != null && savedInstanceState == null) {
                startGeocode(mLastFix);
            }
        }

        if (mLastFix == null) {
            // No enabled providers found, so disable option
            mRadioCurrent.setEnabled(false);
            
            // Enable other options
            mRadioManual.setChecked(true);            
            mEditLocation.setEnabled(true);
            mButtonSearch.setEnabled(true);
        }
        else {
        	// Do the opposite
            mRadioCurrent.setEnabled(true);
            mRadioCurrent.setChecked(true);
            
            mRadioManual.setChecked(false);
            
            mEditLocation.setEnabled(false);
            mButtonSearch.setEnabled(false);
        }
        
        if (Double.isNaN(mLat) || Double.isNaN(mLon) || mLocation == "")
        	setActionEnabled(false);
    }

    /**
     * Handle any new intents wrapping around from {@link SearchManager}.
     */
    @Override
    public void onNewIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            // Fire off geocoding request for given query
            String query = intent.getStringExtra(SearchManager.QUERY);
            startGeocode(query);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble(LAT, mLat);
        outState.putDouble(LON, mLon);  
        outState.putString(LOC, mLocation);
        outState.putLong(ID, mLocationID);
    }

    @Override
	public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radioCurrent: {
                // Picked current location, start geocode to find location name
            	mEditLocation.setEnabled(false);
                mButtonSearch.setEnabled(false);
            	startGeocode(mLastFix);
                break;
            }
            case R.id.radioManual: {
                // Picked manual search, so trigger search dialog
                //onSearchRequested();
            	mEditLocation.setEnabled(true);
                mButtonSearch.setEnabled(true);
                break;
            }
            case R.id.buttonSearch: {
            	mLocation = mEditLocation.getText().toString();
            	//startGeocode(mLocation);
            	
            	HttpTaskInfo info = new HttpTaskInfo();
    			info.cityName = mLocation;
    			info.appWidgetId = mAppWidgetId;
    			new HttpTask().execute(info);
    			
                break;
            }
//            case R.id.buttonMap: {
//                // Picked verify on map, so launch mapping intent
//                Uri mapUri = Uri.parse(String.format("geo:%f,%f?z=%d", mLat, mLon, ZOOM_LEVEL));
//
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
//                mapIntent.setData(mapUri);
//
//                startActivity(mapIntent);
//                break;
//            }
//            case R.id.buttonSave: {
//                final Intent data = new Intent();
//                mLocation = mEditLocation.getText().toString();
//                
//                if (Double.isNaN(mLat) || Double.isNaN(mLon) || mLocation == "")
//        		{
//                	setResult(RESULT_CANCELED, data);
//        		}
//                else
//                {
//                	Preferences.setLocationLat(this, mAppWidgetId, (float)mLat);
//	                Preferences.setLocationLon(this, mAppWidgetId, (float)mLon);
//	                Preferences.setLocation(this, mAppWidgetId, mLocation);
//	                setResult(RESULT_OK, data);
//                }                
//                
//                finish();
//
//                break;
//            }
        }
    } 
    
    public class HttpTask extends AsyncTask<HttpTaskInfo, Void, OpenQuery>{

    	ProgressDialog progressDialog;
    	
    	@Override
		protected void onPreExecute() {
            // Show progress spinner and disable buttons
            setProgressBarIndeterminateVisibility(true);            
            setActionEnabled(false);
            
            progressDialog = ProgressDialog.show(ConfigureLocationActivity.this, "",
                    "Searching...");
        }
    	
    	@SuppressWarnings("unused")
		public OpenQuery doInBackground(HttpTaskInfo... info){
	    	String cityName = info[0].cityName;
	    	int appWidgetId = info[0].appWidgetId;
	    	OpenQuery openResult = null;

	    	try {
	        	Reader responseReader = null;
		        HttpClient client = new DefaultHttpClient();
		        cityName = cityName.replaceAll(" ", "%20");
		        HttpGet request = new HttpGet(String.format(GET_CITY_URL, cityName));
		        
	            HttpResponse response = client.execute(request);

	            StatusLine status = response.getStatusLine();
	            Log.d(LOG_TAG, "Request returned status " + status);

	            HttpEntity entity = response.getEntity();
	            responseReader = new InputStreamReader(entity.getContent()); 
	            
	            if (responseReader == null) {	            	            
	            	return openResult;
	            }
	            
	            char[] buf = new char[1024];
	            StringBuilder result = new StringBuilder();
	            int read = responseReader.read(buf);
	            
	            while (read >= 0) {
	                result.append(buf, 0, read);
	                read = responseReader.read(buf);
	            }	 
	            
	            openResult = parseCityData(appWidgetId, result.toString());
	            
	        } catch (UnknownHostException e) {
	        	e.printStackTrace();	        	
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();	            
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();	            
	        } catch (IOException e) {
	            e.printStackTrace();	            
	        }

	        return openResult;
	    }
    	
    	@SuppressWarnings("unused")
    	OpenQuery parseCityData(int appWidgetId, String parseString) {
    		Log.d(LOG_TAG, "ConfigureLocationActivity parseCityData appWidgetId: " + appWidgetId);
    		OpenQuery openResult = null;

    		JSONTokener parser = new JSONTokener(parseString);
    		try {
    			JSONObject query = (JSONObject)parser.nextValue();
    			
    			JSONArray list = query.getJSONArray("list");
    			
                if (list.length() == 0) {
                    return openResult;
                }
                
                ArrayList<String> locations = new ArrayList<String>();
                mSelectedOpen = 0;
                
                for (int i=0; i<list.length(); i++) {
                	JSONObject cityJSON = list.getJSONObject(i);
                
                	int cityId = cityJSON.getInt("id");
    	            String name = cityJSON.getString("name");
    	            String country = "";
    	            
    	            JSONObject sys = null;
    	            try {
    	                sys = cityJSON.getJSONObject("sys");
    	            } catch (JSONException e) {	                
    	            }
    	            try {
    	                country = sys.getString("country");                              
    	            } catch (JSONException e) {	                
    	            }
    	            
    	            if (name == null || country == null)
    	            	continue;
    	            
    	            locations.add(name + ", " + country);
                }
                
                final CharSequence[] items = new CharSequence[locations.size()];
                
                for (int i=0; i<locations.size(); i++) {
                	items[i] = locations.get(i);
                }
                
                selectLocation(items);
            	
                // use selected result 
                JSONObject cityJSON = list.getJSONObject(mSelectedOpen);
                
            	int cityId = cityJSON.getInt("id");
	            String name = cityJSON.getString("name");
	            String country = "";
	            
	            JSONObject sys = null;
	            try {
	                sys = cityJSON.getJSONObject("sys");
	            } catch (JSONException e) {	                
	            }
	            try {
	                country = sys.getString("country");                              
	            } catch (JSONException e) {	                
	            }
	            
	            JSONObject coord = null;
	            double lat = Double.NaN;
	            double lon = Double.NaN;
	            try {
	            	coord = cityJSON.getJSONObject("coord");
	            } catch (JSONException e) {	                
	            }
	            try {
	                lat = coord.getDouble("lat");
	                lon = coord.getDouble("lon");
	            } catch (JSONException e) {	                
	            }
	            
	            openResult = new OpenQuery(name, cityId, lat, lon);
	                            
                return openResult;
    			
    		} catch (JSONException e) {
    			e.printStackTrace();
    			return openResult;
    		}    	
    	}
    	
    	private void selectLocation(final CharSequence[] items) {
        	ConfigureLocationActivity.this.runOnUiThread(new Runnable() {
        	    public void run() {
        	    	
        	    	AlertDialog.Builder builder = new AlertDialog.Builder(ConfigureLocationActivity.this);
                	builder.setTitle(R.string.selectlocation);
                	
                	builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int item) {
                	    	mSelectedOpen = item;
                	    	dialog.dismiss();
                	    }
                	});
                	
                	mAlert = builder.create();  
                	mAlert.show();
        	    }
        	});
        }

    	@Override
		protected void onPostExecute(OpenQuery found) {
            setProgressBarIndeterminateVisibility(false);
            progressDialog.dismiss();

            // Update GUI with resolved string
            if (found == null) {
            	mLat = Double.NaN;
            	mLon = Double.NaN;
            	mLocationID = -1;
            	mLocation = "";
                setActionEnabled(false);
                Toast.makeText(getApplicationContext(), "Location not found!", Toast.LENGTH_LONG).show();
            } else {
            	mLat = found.lat;
            	mLon = found.lon;
            	mLocationID = found.ID;
                mLocation = found.name;
                
                Preferences.setLocationLat(getApplicationContext(), mAppWidgetId, (float)mLat);
                Preferences.setLocationLon(getApplicationContext(), mAppWidgetId, (float)mLon);
                Preferences.setLocation(getApplicationContext(), mAppWidgetId, mLocation);
                Preferences.setLocationId(getApplicationContext(), mAppWidgetId, mLocationID);
                
                setActionEnabled(true);
            }
            
            mEditLocation.setText(mLocation);
        }
	}
	
	public class HttpTaskInfo {
		String cityName;
		int appWidgetId;
	}
	
	private static class OpenQuery {
        String name = null;
        long ID = -1;
        double lat = Double.NaN;
        double lon = Double.NaN;
        
        public OpenQuery(String query, long locationID, double locationLat, double locationLon) {
            name = query;
            ID = locationID; 
            lat = locationLat;
            lon = locationLon;
        }        
	}
	    
    @Override
    public void onBackPressed() {
    	final Intent data = new Intent();
        mLocation = mEditLocation.getText().toString();
        
        if (Double.isNaN(mLat) || Double.isNaN(mLon) || mLocation == "")
		{
        	setResult(RESULT_CANCELED, data);
		}
        else
        {
        	Preferences.setLocationLat(this, mAppWidgetId, (float)mLat);
            Preferences.setLocationLon(this, mAppWidgetId, (float)mLon);
            Preferences.setLocation(this, mAppWidgetId, mLocation);
            
            if (mRadioCurrent.isChecked())
            	mLocationID = -1;
            
            Preferences.setLocationId(this, mAppWidgetId, mLocationID);
            setResult(RESULT_OK, data);
        }                
        
        finish();		
    }

    public void setConfigureResult(int resultCode) {
        final Intent data = new Intent();
        data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(resultCode, data);
    }

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		
	}
}
