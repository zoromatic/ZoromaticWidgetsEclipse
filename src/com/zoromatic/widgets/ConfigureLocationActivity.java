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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
//import java.util.List;


import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.alertdialogpro.AlertDialogPro;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.SunriseSunsetLocation;
import com.zoromatic.widgets.LocationProvider.LocationResult;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.internal.widget.TintCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton.OnCheckedChangeListener;

@SuppressWarnings({ "unused", "deprecation" })
public class ConfigureLocationActivity extends ThemeActionBarActivity {    
	
	public static final String LOG_TAG = "ConfigureLocationActivity";
    
    public static int LOCATION_TYPE_CUSTOM = 0;
    public static int LOCATION_TYPE_CURRENT = 1;

    private ImageButton mButtonSearch;
    private EditText mEditLocation;
    private RadioButton mRadioCurrent;
    private RadioButton mRadioManual;
    private TintCheckBox mCheckCurrent;
    private Button mButtonMap;

    private double mLat = Double.NaN;
    private double mLon = Double.NaN;
    private String mLocation = "";
    private long mLocationID = -1;
    private int mLocationType = 0;
    
    private double mLatPref = Double.NaN;
    private double mLonPref = Double.NaN;
    private String mLocationPref = "";
    private long mLocationIDPref = -1;
    private int mLocationTypePref = LOCATION_TYPE_CUSTOM;
    
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String LOC = "loc";
    public static final String ID = "id";
    public static final String LOCTYPE = "loctype";    
    
    public static final String LAST_UPDATED = "lastUpdated";
    public static final String CONFIGURED = "configured";
    public static final int CONFIGURED_TRUE = 1;
    public static int mSelectedGeocode = 0;
    public static boolean mStateSaved = false;
    
    Bundle mSavedState = null;
    public boolean mActivityDelete = false;
    
    public static final String AUTHORITY = "com.zoromatic.widgets";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/appwidgets");
    public static String FIND_CITIES_URL = "http://api.openweathermap.org/data/2.5/find?q=%s&type=like&lang=%s&APPID=364a27c67e53df61c49db6e5bdf26aa5";
    public static String GET_CITY_URL = "http://api.openweathermap.org/data/2.5/find?lat=%f&lon=%f&cnt=1&lang=%s&APPID=364a27c67e53df61c49db6e5bdf26aa5";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    static final String APPWIDGETID = "AppWidgetId";
    private static final int ZOOM_LEVEL = 15;
    
    private Toolbar toolbar;
    
    private ProgressDialogFragment mProgressFragment = null;
    
    static HttpTask mHttpTask;
    static DataProviderTask mDataProviderTask;
    static ConfigureLocationActivity mConfigurationActivity;
    
    static CharSequence[] items = null;
    static JSONArray list = null;
	String parseString = null;	
	
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_LOCATION=2;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int UPDATE_ID = Menu.FIRST + 2;
    private static final int DEFAULT_ID = Menu.FIRST + 3;
    
    private SQLiteDbAdapter mDbHelper;
    ListView mListView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   
        
        mSavedState = savedInstanceState;
        
        // Read the appWidgetId to configure from the incoming intent
        setAppWidgetId(getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, getAppWidgetId()));
        setConfigureResult(Activity.RESULT_CANCELED);
        if (getAppWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        
        mConfigurationActivity = this;
        mDbHelper = new SQLiteDbAdapter(this);
        
        displayActivity();
        
        if (mHttpTask != null)
        	mHttpTask.setActivity(mConfigurationActivity);
        
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary,
                outValue,
                true);
        int primaryColor = outValue.resourceId;
        
        setStatusBarColor(findViewById(R.id.statusBarBackground), 
	    		getResources().getColor(primaryColor));                    
    }
    
    private void displayActivity() {
    	setContentView(R.layout.configurelocation);
    	
    	toolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
	    
	    ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }
        
        mListView = (ListView)findViewById(R.id.listLocations);
        
        //create location in database from old preferences
        if (Preferences.getLocationType(getDialogContext(), mAppWidgetId) == LOCATION_TYPE_CUSTOM) {
	        mDbHelper.open();
	    	Cursor locationsCursor = mDbHelper.fetchAllLocations();
	    	
	    	float latPref = Preferences.getLocationLat(getDialogContext(), mAppWidgetId);
			float lonPref = Preferences.getLocationLon(getDialogContext(), mAppWidgetId);
			String locationPref = Preferences.getLocation(getDialogContext(), mAppWidgetId);
			long locationIDPref = Preferences.getLocationId(getDialogContext(), mAppWidgetId);        
			boolean bFound = false;
	    	
	    	if (locationsCursor != null && locationsCursor.getCount() > 0) {
	    		locationsCursor.moveToFirst();
	        	
	        	do {
	        		long locId = locationsCursor.getLong(locationsCursor.getColumnIndex(SQLiteDbAdapter.KEY_LOCATION_ID));              
	
	        		if (locId != -1 && locationIDPref == locId) {
	        			bFound = true;
	        		}
	
	        		locationsCursor.moveToNext();
	
	        	} while (!locationsCursor.isAfterLast());
	    	}
	    	
	    	if (bFound == false && locationIDPref >= 0) {
	    		mDbHelper.createLocation(locationIDPref, latPref, lonPref, locationPref);
	    	}
	    	
	    	mDbHelper.close();
        }
        
        //fillData();
        
        // Show the ProgressDialogFragment on this thread
        mProgressFragment = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", (String) getResources().getText(R.string.working));
		args.putString("message", (String) getResources().getText(R.string.retrieving));
		mProgressFragment.setArguments(args);
		mProgressFragment.show(getSupportFragmentManager(), "tagProgress");

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/*Intent i = new Intent(view.getContext(), LocationsEdit.class);
		        i.putExtra(SQLiteDbAdapter.KEY_ROWID, id);
		        startActivityForResult(i, ACTIVITY_EDIT);*/	
				
				// make default
				makeDefault(id);				
			}
		});
        
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				//return false;
				mActivityDelete = true;
		        setListViewItems();
		        
		        View tempView = getViewByPosition(position, mListView);
        		
        		if (tempView != null) {
	        		TintCheckBox checkBox = (TintCheckBox) tempView.findViewById(R.id.checkBoxSelect);
	        		
	        		if (checkBox != null) {
	        			checkBox.setChecked(true);
	        		}	        			 
				}
		        
		        return true;
			}
		});
        
        registerForContextMenu(mListView);
        
        mCheckCurrent = (TintCheckBox) findViewById(R.id.checkCurrent);

        mCheckCurrent.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked && getLocationType() != LOCATION_TYPE_CURRENT) {
					showCurrentLocationInfo();                
	                //setLocationType(LOCATION_TYPE_CURRENT);
				} else if (!isChecked && getLocationType() != LOCATION_TYPE_CUSTOM) {
					setLocationType(LOCATION_TYPE_CUSTOM);
					getCheckCurrent().setText(getResources().getText(R.string.locationcurrent));
					
					if (mListView != null && mListView.getCount() > 0) {
						CustomSimpleCursorAdapter cursorAdapter = (CustomSimpleCursorAdapter)mListView.getAdapter();
						makeDefault(cursorAdapter.getItemId(0)); // make first default
					}
				}
			}
		});
        
        mLatPref = Preferences.getLocationLat(this, getAppWidgetId());
        mLonPref = Preferences.getLocationLon(this, getAppWidgetId());
        mLocationPref = Preferences.getLocation(this, getAppWidgetId());
        mLocationIDPref = Preferences.getLocationId(this, getAppWidgetId());        
        mLocationTypePref = Preferences.getLocationType(this, getAppWidgetId());

        // If restoring, read location and units from bundle
        if (mSavedState != null) {
            setLat(mSavedState.getDouble(LAT));
            setLon(mSavedState.getDouble(LON));
            setLoc(mSavedState.getString(LOC));
            setLocationID(mSavedState.getLong(ID));
            setLocationType(mSavedState.getInt(LOCTYPE));
        } else {
        	setLat(mLatPref);
            setLon(mLonPref);
            setLoc(mLocationPref);
            setLocationID(mLocationIDPref);
            setLocationType(mLocationTypePref);
            
            items = null;
        	list = null;
        	parseString = null;
        }
        
        if (getLocationType() == LOCATION_TYPE_CURRENT) {
        	mCheckCurrent.setChecked(true);			
        	mCheckCurrent.setText(getResources().getText(R.string.locationcurrent) + " [" + mLocationPref + "]");
		} else {
			mCheckCurrent.setChecked(false);
			mCheckCurrent.setText(getResources().getText(R.string.locationcurrent));
		} 
        
        // Start a new thread that will download all the data
        mDataProviderTask = new DataProviderTask();
        mDataProviderTask.setActivity(mConfigurationActivity);
        mDataProviderTask.execute();                                                          
	}
    
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
    
    private void setListViewItems() {
    	
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
		    	supportInvalidateOptionsMenu();
		    	
		    	if (mListView != null && mListView.getCount() > 0) {
		        	for (int i=0; i<mListView.getCount(); i++) {
		        		View view = getViewByPosition(i, mListView);
		        		
		        		if (view != null) {
		        			setListViewItem(view);
		        		}
		        	}
		        }
			}
    	});
    }
    
    private void setListViewItem(View view) {
    	if (view == null)
    		return;
    	
    	/*TypedValue outValue = new TypedValue();
        getDialogContext().getTheme().resolveAttribute(R.attr.colorAccent, outValue, true);
        int color = outValue.resourceId;
        int colorAccent = getDialogContext().getResources().getColor(color);*/  
        
        TextView text = (TextView) view.findViewById(R.id.label);
		String strLabel = "L"; 
		
		if (text != null) {
			strLabel = text.getText().toString();
			
			if (strLabel.length() > 0)
				strLabel = strLabel.subSequence(0, 1).toString();
			else
				strLabel = "L";
		}
		
		ImageView image = (ImageView) view.findViewById(R.id.iconWeather);
		if (image != null) {
			//image.setColorFilter(colorAccent);	
			image.setVisibility(mActivityDelete?View.GONE:View.VISIBLE);	
			
			final Resources res = getDialogContext().getResources();
		    final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

		    final LetterTileProvider tileProvider = new LetterTileProvider(getDialogContext());
		    final Bitmap letterTile = tileProvider.getLetterTile(strLabel, strLabel, tileSize, tileSize);
		    
		    image.setImageBitmap(letterTile);
		    
		    //String font = "fonts/Roboto.ttf";
			//image.setImageBitmap(WidgetUpdateService.getFontBitmap(getDialogContext(), strLabel, colorAccent, font, true, 96)); 		    
		}
		
		TintCheckBox checkBox = (TintCheckBox) view.findViewById(R.id.checkBoxSelect);
		if (checkBox != null) {
			checkBox.setVisibility(mActivityDelete?View.VISIBLE:View.GONE);
			
			if (!mActivityDelete)
				checkBox.setChecked(false);
		}
    }
    
    private class CustomSimpleCursorAdapter extends SimpleCursorAdapter {
    	public CustomSimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
    	public View getView(int position, View convertView, ViewGroup parent) {  
    		View view = super.getView(position, convertView, parent);   		
    		
    		if (view != null) {
    			setListViewItem(view);
    		}
    		
       		return view;  
    	}
    }
    
    private class DataProviderTask extends AsyncTask<Void, Void, Void> {
    	
    	ConfigureLocationActivity configureLocationActivity = null;
    	
    	void setActivity(ConfigureLocationActivity activity) {
    		configureLocationActivity = activity;
		}
    	
    	ConfigureLocationActivity getActivity() {
    		return configureLocationActivity;
		}
    	
    	@Override
		protected void onPreExecute() {
    		 	
        }

		@Override
		protected Void doInBackground(Void... params) {
			Log.i(LOG_TAG, "ConfigureLocationActivity - Background thread starting");
        	
			configureLocationActivity.fillData();
        	
			return null;
		}
		
		@Override
	    protected void onPostExecute(Void result) {
        	
        	if (configureLocationActivity.mProgressFragment != null) {
        		configureLocationActivity.mProgressFragment.dismiss(); 
        	}     
        	
        	configureLocationActivity.mActivityDelete = false;
        	configureLocationActivity.setListViewItems();
        }        	
   }
    
    private void fillData() {
    	
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
		    	if (mDbHelper == null)
		    		mDbHelper = new SQLiteDbAdapter(getDialogContext());
		    	
		    	mDbHelper.open();
		    	
		    	// Get all of the rows from the database and create the item list
		    	final Cursor locationsCursor = mDbHelper.fetchAllLocations();
		        startManagingCursor(locationsCursor);
		        
		        if (Preferences.getLocationType(getDialogContext(), mConfigurationActivity.getAppWidgetId()) == LOCATION_TYPE_CUSTOM && locationsCursor != null && locationsCursor.getCount() == 1) {
		        	// if only one, make default
		        	locationsCursor.moveToFirst();
		        	
		        	double lat = locationsCursor.getDouble(locationsCursor.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LATITUDE));
			        double lon = locationsCursor.getDouble(locationsCursor.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LONGITUDE));
			        String loc = locationsCursor.getString(locationsCursor.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_NAME));
			        long locId = locationsCursor.getLong(locationsCursor.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LOCATION_ID));
			        
			        mConfigurationActivity.setLat(lat);
			        mConfigurationActivity.setLon(lon);
			        mConfigurationActivity.setLoc(loc);
			        mConfigurationActivity.setLocationID(locId);
			        mConfigurationActivity.setLocationType(LOCATION_TYPE_CUSTOM);	        
			        	        
			        Preferences.setLocationLat(getDialogContext(), mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLat());
			        Preferences.setLocationLon(getDialogContext(), mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLon());
			        Preferences.setLocation(getDialogContext(), mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLoc());
			        
			        Preferences.setLocationId(getDialogContext(), mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationID());
			        Preferences.setLocationType(getDialogContext(), mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationType());
		        } else if (Preferences.getLocationType(getDialogContext(), mConfigurationActivity.getAppWidgetId()) == LOCATION_TYPE_CUSTOM && locationsCursor != null && locationsCursor.getCount() == 0) {
		        	mConfigurationActivity.setLat(Double.NaN);
			        mConfigurationActivity.setLon(Double.NaN);
			        mConfigurationActivity.setLoc("");
			        mConfigurationActivity.setLocationID(-1);
			        mConfigurationActivity.setLocationType(LOCATION_TYPE_CUSTOM);	        
			        	        
			        Preferences.setLocationLat(getDialogContext(), mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLat());
			        Preferences.setLocationLon(getDialogContext(), mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLon());
			        Preferences.setLocation(getDialogContext(), mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLoc());
			        
			        Preferences.setLocationId(getDialogContext(), mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationID());
			        Preferences.setLocationType(getDialogContext(), mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationType());
		        }
		        
		        // Create an array to specify the fields we want to display in the list (only TITLE)
		        final String[] from = new String[]{SQLiteDbAdapter.KEY_NAME};
		        
		        // and an array of the fields we want to bind those fields to (in this case just text1)
		        final int[] to = new int[]{R.id.label};
                		
		        // Now create a simple cursor adapter and set it to display
		        CustomSimpleCursorAdapter notes = 
		        	    new CustomSimpleCursorAdapter(getDialogContext(), R.layout.locations_row, locationsCursor, from, to);
		        
		        notes.setViewBinder(new CustomSimpleCursorAdapter.ViewBinder() {
		            @Override
		            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		                if (view.getId() == R.id.label) {
		                	int getIndex = cursor.getColumnIndex(SQLiteDbAdapter.KEY_NAME);
		                    String name = cursor.getString(getIndex);
		                    
		                	TextView textView = (TextView) view;
		                	textView.setText(name);
		                	
		                    getIndex = cursor.getColumnIndex(SQLiteDbAdapter.KEY_LOCATION_ID);
		                    long locId = cursor.getLong(getIndex);
		                    
		                    int locationType = Preferences.getLocationType(getDialogContext(), mAppWidgetId);
		                    long locIdTemp = -1;
		                    
		                    if (locationType == ConfigureLocationActivity.LOCATION_TYPE_CUSTOM) {
		                    	locIdTemp = Preferences.getLocationId(getDialogContext(), mAppWidgetId);
		                    }
		                    
		                    if (locIdTemp >= 0 && locIdTemp == locId) {
		                    	name += " [" + (String) getResources().getText(R.string.defaultsummary) + "]";
		                    }
		                    
		                    int lnLoc = name.length();
		            		SpannableString spStrLoc = new SpannableString(name);
		            		
		            		if (locIdTemp >= 0 && locIdTemp == locId) {
		            			spStrLoc.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, lnLoc, 0);                    
		                    } else {
		                    	spStrLoc.setSpan(new StyleSpan(Typeface.NORMAL), 0, lnLoc, 0);
		                    }            		
		            		
		            		textView.setText(spStrLoc);
		                    
		                    return true;
		                }
		                return false;
		            }
		        });
		       
		       	mListView.setAdapter(notes);  
		       	stopManagingCursor(locationsCursor);
		       	mDbHelper.close();		       		       	
			}
        });              	       	       	           
    }
    
    @Override
    public void onBackPressed() {
    	if (mActivityDelete) {
    		mActivityDelete = false;
    		setListViewItems();
    	}
    	else {
	    	if (Double.isNaN(mConfigurationActivity.getLat()) || Double.isNaN(mConfigurationActivity.getLon()) || mConfigurationActivity.getLoc() == "") {
	        	setResult(RESULT_CANCELED);
			} else {
	        	Preferences.setLocationLat(this, mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLat());
	            Preferences.setLocationLon(this, mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLon());
	            Preferences.setLocation(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLoc());
	            
	            Preferences.setLocationId(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationID());
	            Preferences.setLocationType(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationType());
	            
	            setResult(RESULT_OK);
	        }  
	    	
	        showProgressDialog(false);
	        super.onBackPressed();
    	}              
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.insert).setVisible(!mActivityDelete);
        menu.findItem(R.id.delete).setVisible(mActivityDelete);
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.locationmenu, menu);
	    return true;        
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
	        onBackPressed();
			//finish();
	        return true;
		case R.id.insert:
            addLocation();
            return true;
		case R.id.delete:
            deleteLocation();
            return true;
	    default:
	    	return super.onOptionsItemSelected(item);	         
		}
	}

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
    	//super.onCreateContextMenu(menu, v, menuInfo);
        //menu.add(0, DEFAULT_ID, 0, R.string.menu_make_default);
        //menu.add(0, DELETE_ID, 1, R.string.menu_delete_location);                
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
        case DELETE_ID:
            AdapterContextMenuInfo infoDelete = (AdapterContextMenuInfo) item.getMenuInfo();
            
            boolean moveDefault = false;
            long locId = -1;
            
            mDbHelper.open();
    		Cursor location = mDbHelper.fetchLocation(infoDelete.id);            
            
    		// check if this one was default and move default
    		if (location != null && location.getCount() > 0) {
            	location.moveToFirst();
            
    	        locId = location.getLong(location.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LOCATION_ID));	    	        
    	        long locIdTemp = Preferences.getLocationId(getDialogContext(), mAppWidgetId);
    	        
    	        if (locId != -1 && locId == locIdTemp && Preferences.getLocationType(getDialogContext(), mAppWidgetId) == LOCATION_TYPE_CUSTOM) {
    	        	moveDefault = true;
    	        }
            }
    		
    		mDbHelper.close();
                        
            mDbHelper.open();
            mDbHelper.deleteLocation(infoDelete.id);
            mDbHelper.close();
            
            // delete cache file
        	File parentDirectory = new File(this.getFilesDir().getAbsolutePath());
            
            if (parentDirectory.exists()) {
            	File cacheFile = null;
                
                if (locId >= 0) {        
                	cacheFile = new File(parentDirectory, "weather_cache_loc_" + locId);
                	
                	if (cacheFile.exists()) {
                		cacheFile.delete();	
                	}
                	
                	cacheFile = new File(parentDirectory, "forecast_cache_loc_" + locId);
                	
                	if (cacheFile.exists()) {
                		cacheFile.delete();	
                	}
                }
            }
                
    		if (moveDefault) {
    			CustomSimpleCursorAdapter cursorAdapter = (CustomSimpleCursorAdapter)mListView.getAdapter();
            	makeDefault(cursorAdapter.getItemId(0)); // make first default
            }
            
            //fillData();
    		
            // Show the ProgressDialogFragment on this thread
            mProgressFragment = new ProgressDialogFragment();
    		Bundle args = new Bundle();
    		args.putString("title", (String) getResources().getText(R.string.working));
    		args.putString("message", (String) getResources().getText(R.string.retrieving));
    		mProgressFragment.setArguments(args);
    		mProgressFragment.show(getSupportFragmentManager(), "tagProgress");

            // Start a new thread that will download all the data
            mDataProviderTask = new DataProviderTask();
            mDataProviderTask.setActivity(mConfigurationActivity);
            mDataProviderTask.execute();
            
            return true;        
        case DEFAULT_ID:
        	AdapterContextMenuInfo infoUpdate = (AdapterContextMenuInfo) item.getMenuInfo();
            makeDefault(infoUpdate.id);
            return true;
        }
        return super.onContextItemSelected(item);
	}
    
    private void addLocation() {
    	AlertDialogFragment	addLocationFragment = new AlertDialogFragment();
		Bundle args = new Bundle();
        args.putString("title", getResources().getString(R.string.new_location));
        args.putString("message", getResources().getString(R.string.enter_location));
        args.putBoolean("editbox", true);
        addLocationFragment.setArguments(args);
        addLocationFragment.show(getSupportFragmentManager(), "tagAlert"); 
    }
    
    private void deleteLocation() {
    	if (mListView != null && mListView.getCount() > 0) {
    		CustomSimpleCursorAdapter cursorAdapter = (CustomSimpleCursorAdapter)mListView.getAdapter();
    		
        	for (int i=mListView.getCount()-1; i>=0; i--) {
        		View tempView = getViewByPosition(i, mListView);
        		
        		if (tempView != null) {
	        		TintCheckBox checkBox = (TintCheckBox) tempView.findViewById(R.id.checkBoxSelect);
	        		
	        		if (checkBox != null && checkBox.isChecked()) {
	        			int id = (int) cursorAdapter.getItemId(i);			
						boolean moveDefault = false;
			            long locId = -1;
			            
			            mDbHelper.open();
			    		Cursor location = mDbHelper.fetchLocation(id);            
			            
			    		// check if this one was default and move default
			    		if (location != null && location.getCount() > 0) {
			            	location.moveToFirst();
			            
			    	        locId = location.getLong(location.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LOCATION_ID));	    	        
			    	        long locIdTemp = Preferences.getLocationId(getDialogContext(), mAppWidgetId);
			    	        
			    	        if (locId != -1 && locId == locIdTemp && Preferences.getLocationType(getDialogContext(), mAppWidgetId) == LOCATION_TYPE_CUSTOM) {
			    	        	moveDefault = true;
			    	        }
			            }
			    		
			    		mDbHelper.close();
			                        
			            mDbHelper.open();
			            mDbHelper.deleteLocation(id);
			            mDbHelper.close();
			            
			            // delete cache file
			        	File parentDirectory = new File(this.getFilesDir().getAbsolutePath());
			            
			            if (parentDirectory.exists()) {
			            	File cacheFile = null;
			                
			                if (locId >= 0) {        
			                	cacheFile = new File(parentDirectory, "weather_cache_loc_" + locId);
			                	
			                	if (cacheFile.exists()) {
			                		cacheFile.delete();	
			                	}
			                	
			                	cacheFile = new File(parentDirectory, "forecast_cache_loc_" + locId);
			                	
			                	if (cacheFile.exists()) {
			                		cacheFile.delete();	
			                	}
			                }
			            }
			                
			    		if (moveDefault) {
			            	makeDefault(cursorAdapter.getItemId(0)); // make first default
			            }
	        		}	        			
        		}
        	}
        	
        	mDataProviderTask = new DataProviderTask();
            mDataProviderTask.setActivity(mConfigurationActivity);
            mDataProviderTask.execute();                        
        }
    }
    
    void makeDefault(long id) {    
    	mDbHelper.open();
		Cursor location = mDbHelper.fetchLocation(id);
        startManagingCursor(location);
        
        if (location != null && location.getCount() > 0) {
        	
        	location.moveToFirst();
        
	        double lat = location.getDouble(location.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LATITUDE));
	        double lon = location.getDouble(location.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LONGITUDE));
	        String loc = location.getString(location.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_NAME));
	        long locId = location.getLong(location.getColumnIndexOrThrow(SQLiteDbAdapter.KEY_LOCATION_ID));
	        
	        mConfigurationActivity.setLat(lat);
	        mConfigurationActivity.setLon(lon);
	        mConfigurationActivity.setLoc(loc);
	        mConfigurationActivity.setLocationID(locId);
	        mConfigurationActivity.setLocationType(LOCATION_TYPE_CUSTOM);
	        mConfigurationActivity.getCheckCurrent().setChecked(false);
	        mConfigurationActivity.getCheckCurrent().setText(getResources().getText(R.string.locationcurrent));
	        	        
	        Preferences.setLocationLat(this, mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLat());
	        Preferences.setLocationLon(this, mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLon());
	        Preferences.setLocation(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLoc());
	        
	        Preferences.setLocationId(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationID());
	        Preferences.setLocationType(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationType());
        }
        
        stopManagingCursor(location);
        mDbHelper.close();
        
        //fillData();
        
        // Show the ProgressDialogFragment on this thread
        mProgressFragment = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", (String) getResources().getText(R.string.working));
		args.putString("message", (String) getResources().getText(R.string.retrieving));
		mProgressFragment.setArguments(args);
		mProgressFragment.show(getSupportFragmentManager(), "tagProgress");

        // Start a new thread that will download all the data
        mDataProviderTask = new DataProviderTask();
        mDataProviderTask.setActivity(mConfigurationActivity);
        mDataProviderTask.execute();
    }
    
    @SuppressLint("InlinedApi")
	public void setStatusBarColor(View statusBar,int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
               Window w = getWindow();
               w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
               //status bar height
               //int actionBarHeight = getActionBarHeight();
               int statusBarHeight = getStatusBarHeight();
               //action bar height
               statusBar.getLayoutParams().height = /*actionBarHeight + */statusBarHeight;
               statusBar.setBackgroundColor(color);
         } else {
        	 statusBar.setVisibility(View.GONE);
         }
    }
    
    public int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
           actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }          
	
	private Context getDialogContext() {
		final Context context;
		String theme = Preferences.getMainTheme(this);
		int colorScheme = Preferences.getMainColorScheme(this);
		
		if (theme.compareToIgnoreCase("light") == 0) {
			switch (colorScheme) {
			case 0:
				context = new ContextThemeWrapper(this, R.style.ThemeLightBlack);
				break;
			case 1:
				context = new ContextThemeWrapper(this, R.style.ThemeLightWhite);
				break;
			case 2:
				context = new ContextThemeWrapper(this, R.style.ThemeLightRed);
				break;
			case 3:
				context = new ContextThemeWrapper(this, R.style.ThemeLightPink);
				break;
			case 4:
				context = new ContextThemeWrapper(this, R.style.ThemeLightPurple);
				break;
			case 5:
				context = new ContextThemeWrapper(this, R.style.ThemeLightDeepPurple);
				break;
			case 6:
				context = new ContextThemeWrapper(this, R.style.ThemeLightIndigo);
				break;
			case 7:
				context = new ContextThemeWrapper(this, R.style.ThemeLightBlue);
				break;
			case 8:
				context = new ContextThemeWrapper(this, R.style.ThemeLightLightBlue);
				break;
			case 9:
				context = new ContextThemeWrapper(this, R.style.ThemeLightCyan);
				break;
			case 10:
				context = new ContextThemeWrapper(this, R.style.ThemeLightTeal);
				break;
			case 11:
				context = new ContextThemeWrapper(this, R.style.ThemeLightGreen);
				break;
			case 12:
				context = new ContextThemeWrapper(this, R.style.ThemeLightLightGreen);
				break;
			case 13:
				context = new ContextThemeWrapper(this, R.style.ThemeLightLime);
				break;
			case 14:
				context = new ContextThemeWrapper(this, R.style.ThemeLightYellow);
				break;
			case 15:
				context = new ContextThemeWrapper(this, R.style.ThemeLightAmber);
				break;
			case 16:
				context = new ContextThemeWrapper(this, R.style.ThemeLightOrange);
				break;
			case 17:
				context = new ContextThemeWrapper(this, R.style.ThemeLightDeepOrange);
				break;
			case 18:
				context = new ContextThemeWrapper(this, R.style.ThemeLightBrown);
				break;
			case 19:
				context = new ContextThemeWrapper(this, R.style.ThemeLightGrey);
				break;
			case 20:
				context = new ContextThemeWrapper(this, R.style.ThemeLightBlueGrey);
				break;
			default:
				context = new ContextThemeWrapper(this, R.style.ThemeLightBlack);
				break;
			}
			
		} else { 
			switch (colorScheme) {
			case 0:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkBlack);
				break;
			case 1:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkWhite);
				break;
			case 2:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkRed);
				break;
			case 3:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkPink);
				break;
			case 4:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkPurple);
				break;
			case 5:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkDeepPurple);
				break;
			case 6:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkIndigo);
				break;
			case 7:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkBlue);
				break;
			case 8:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkLightBlue);
				break;
			case 9:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkCyan);
				break;
			case 10:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkTeal);
				break;
			case 11:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkGreen);
				break;
			case 12:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkLightGreen);
				break;
			case 13:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkLime);
				break;
			case 14:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkYellow);
				break;
			case 15:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkAmber);
				break;
			case 16:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkOrange);
				break;
			case 17:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkDeepOrange);
				break;
			case 18:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkBrown);
				break;
			case 19:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkGrey);
				break;
			case 20:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkBlueGrey);
				break;
			default:
				context = new ContextThemeWrapper(this, R.style.ThemeDarkBlack);
				break;
			}
		}	

		return context;
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

        outState.putDouble(LAT, getLat());
        outState.putDouble(LON, getLon());  
        outState.putString(LOC, getLoc());
        outState.putLong(ID, getLocationID());
        outState.putInt(LOCTYPE, getLocationType());
        outState.putInt(APPWIDGETID, mAppWidgetId);
        
        mSavedState = outState;
        
        mStateSaved = true;        
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        
        setLat(outState.getDouble(LAT));
        setLon(outState.getDouble(LON));  
        setLoc(outState.getString(LOC));
        setLocationID(outState.getLong(ID));
        setLocationType(outState.getInt(LOCTYPE));
        mAppWidgetId = outState.getInt(APPWIDGETID);
        
        mSavedState = outState;
    }
    
    @Override
    protected void onPostResume() {
    	super.onPostResume();
    	
    	mStateSaved = false;        
    }
    
    DialogInterface.OnClickListener dialogLocationDisabledClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                startLocationsActivity();
                break;

            default:
                showProgressDialog(false);
            	break;
            }
        }
    };
    
    DialogInterface.OnClickListener dialogAddLocationClickListener = new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,
				int whichButton) {
			
    		EditText input = (EditText) ((AlertDialogPro) dialog).findViewById(AlertDialogFragment.TEXT_ID);
    		
			if (input != null) {
				Editable value = input.getText();
	
				if (value != null && value.length() > 0) {
					HttpTaskInfo info = new HttpTaskInfo();
					info.cityName = value.toString();
					info.latitude = Double.NaN;
					info.longitude = Double.NaN;
					info.appWidgetId = getAppWidgetId();
	
					parseString = null;
					list = null;
					items = null;
	
					mHttpTask = new HttpTask();
					mHttpTask.setActivity(mConfigurationActivity);
					showProgressDialog(true);
	
					try {
						mHttpTask.execute(info);
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
			}
		}
	};
    
    DialogInterface.OnCancelListener dialogCancelListener = new DialogInterface.OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			showProgressDialog(false);			
		}
	};
	
	 DialogInterface.OnKeyListener dialogKeyListener = new DialogInterface.OnKeyListener() {
		
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				dialog.dismiss();
				showProgressDialog(false);
            }
			
			return true;
		}
	};
	
    DialogInterface.OnClickListener dialogSelectLocationClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int item) {
	    	if (!parseCustomLocation(item)) {
	    		Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();	            
	    	}
	    	dialog.dismiss();	    	    	
	    }
    };
    
    private void startLocationsActivity() {
    	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    	startActivityForResult(intent, ACTIVITY_LOCATION);
    }
    
    private void getLocation () {
    	LocationResult locationResult = new LocationResult(){
            @Override
            public void gotLocation(Location location){
                
            	showProgressDialog(false);
            	
            	if (location != null) {
            		setLat(location.getLatitude());
                    setLon(location.getLongitude());
                    
                    // determine city name
                    startGeolocation(location);
            	} else {
                    setLat(Double.NaN);
                    setLon(Double.NaN);
                    
                    runOnUiThread(new Runnable() {
                  	  public void run() {
                  		  Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
                  	  }
                  	});
                }                       
            }
        };
        
        LocationProvider loc = new LocationProvider();
        loc.getLocation(this, locationResult);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_CREATE || requestCode == ACTIVITY_EDIT) {
        	//fillData();
        	
            // Show the ProgressDialogFragment on this thread
            mProgressFragment = new ProgressDialogFragment();
    		Bundle args = new Bundle();
    		args.putString("title", (String) getResources().getText(R.string.working));
    		args.putString("message", (String) getResources().getText(R.string.retrieving));
    		mProgressFragment.setArguments(args);
    		mProgressFragment.show(getSupportFragmentManager(), "tagProgress");

            // Start a new thread that will download all the data
            mDataProviderTask = new DataProviderTask();
            mDataProviderTask.setActivity(mConfigurationActivity);
            mDataProviderTask.execute();
            
        } else {    	
	    	if (requestCode == ACTIVITY_LOCATION) {
	        	LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	        	List<String> providers = locationManager.getProviders(new Criteria(), true);
	
	            if (!providers.isEmpty()) {
	            	getLocation();
	            } else {
	            	showProgressDialog(false);
	            }            
	        }
        } 
    }
    
    public void showCurrentLocationInfo() {
    	mCheckCurrent.setChecked(false);
    	mCheckCurrent.setText(getResources().getText(R.string.locationcurrent));
    	
    	showProgressDialog(true); 
        
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(new Criteria(), true);

        if (!providers.isEmpty()) {
        	getLocation();                    	
        } else {
        	showLocationDisabledAlertDialog();                                      
        }
    }
    
    public void showProgressDialog(boolean show) {
    	if (show) {
	    	try {
	    		if (!mStateSaved) {
	    			ProgressDialogFragment	progressFragment = new ProgressDialogFragment();
					progressFragment.setTask(mHttpTask);
					Bundle args = new Bundle();
					args.putString("title", getResources().getString(R.string.searchinglocations));
					args.putString("message", getResources().getString(R.string.searching));
					progressFragment.setArguments(args);
					progressFragment.show(getSupportFragmentManager(), "tagProgress");					    			
	    		}
	    	} catch (IllegalStateException e) {
	    		e.printStackTrace();
	    	} 
    	} else {
    		try {
	    		ProgressDialogFragment progressFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag("tagProgress");
	        	if (progressFragment != null) {
	        		progressFragment.dismiss();
	        	}
    		} catch (IllegalStateException e) {
	    		e.printStackTrace();
	    	}
    	}
    }
    
    public void showLocationDisabledAlertDialog() {
    	try {
    		if (!mStateSaved) {    	
    			showProgressDialog(false);
    			
    			AlertDialogFragment	disabledLocationsFragment = new AlertDialogFragment();
	    		Bundle args = new Bundle();
		        args.putString("title", getResources().getString(R.string.locationsettings));
		        args.putString("message", getResources().getString(R.string.locationssettingsdisabled));
		        args.putBoolean("locationdisabled", true);
		        disabledLocationsFragment.setArguments(args);
		        disabledLocationsFragment.show(getSupportFragmentManager(), "tagAlert");    			
    		}
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    	}
    }
    
    public void showSelectLocationAlertDialog(CharSequence[] items) {
    	try {
    		if (!mStateSaved) { 
    			showProgressDialog(false);
    			
    			AlertDialogFragment	selectLocationFragment = new AlertDialogFragment();
    			Bundle args = new Bundle();
		        args.putString("title", getResources().getString(R.string.selectlocation));
		        args.putCharSequenceArray("items", items);
		        selectLocationFragment.setArguments(args);
		        selectLocationFragment.show(getSupportFragmentManager(), "tagChoice");    			
    		}
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    	}
    }

    
    private void startGeolocation (Location location) {
    	HttpTaskInfo info = new HttpTaskInfo();
    	
		info.cityName = "";
		info.latitude = getLat();
		info.longitude = getLon();		
		info.appWidgetId = getAppWidgetId();
		
		parseString = null;
		list = null;
		items = null;
		
		mHttpTask = new HttpTask();
		mHttpTask.setActivity(mConfigurationActivity);
		showProgressDialog(true);
		
		try {
			mHttpTask.execute(info);			
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
    }
    
    private boolean parseCustomLocations(int appWidgetId, String parseString) {
		Log.d(LOG_TAG, "ConfigureLocationActivity parseCityData appWidgetId: " + appWidgetId);
		
		showProgressDialog(false);
		
		if (parseString.equals(null) || parseString.equals("") || parseString.contains("<html>") || parseString.contains("failed to connect")) {
			Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
            return false; 	    	
		}			
		
		JSONTokener parser = new JSONTokener(parseString);
		try {
			JSONObject query = (JSONObject)parser.nextValue();
			
			list = query.getJSONArray("list");
			
		    if (list == null || list.length() == 0) {
		    	Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
		        return false;
		    }
		    
		    ArrayList<String> locations = new ArrayList<String>();
		    
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
		    
		    items = new CharSequence[locations.size()];
		    
		    for (int i=0; i<locations.size(); i++) {
		    	items[i] = locations.get(i);
		    }
		    
		    return true;
			
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
			return false;
		}
    		
	}
    
    private boolean parseCurrentLocation(int appWidgetId, String parseString) {
		Log.d(LOG_TAG, "ConfigureLocationActivity parseCityData appWidgetId: " + appWidgetId);
		
		showProgressDialog(false);
		
		if (parseString.equals(null) || parseString.equals("") || parseString.contains("<html>") || parseString.contains("failed to connect")) {
			Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
            return false; 	    	
		}		
		
		parseString.trim();
		
		String start = parseString.substring(0, 1);
		String end = parseString.substring(parseString.length()-2, parseString.length()-1);
		
		if (!(start.equalsIgnoreCase("{") && end.equalsIgnoreCase("}")) 
				&& !(start.equalsIgnoreCase("[") && end.equalsIgnoreCase("]")))
			return false;
		
		JSONTokener parser = new JSONTokener(parseString);
		try {
			JSONObject query = (JSONObject)parser.nextValue();
			JSONObject weatherJSON = null;
			
			if (query.has("list")) {
			
				JSONArray list = query.getJSONArray("list");
			
	            if (list.length() == 0) {
	                return false;
	            }
	            
	            weatherJSON = list.getJSONObject(0);
			}
			else {
				weatherJSON = query;
			}
            
            int cityId = weatherJSON.getInt("id");
            String location = weatherJSON.getString("name");
            
            double lat = -222;
            double lon = -222; 
            
            JSONObject coord = null;
            try {
                coord = weatherJSON.getJSONObject("coord");
            } catch (JSONException e) {	                
            }
            try {
                lat = coord.getDouble("lat");
                lon = coord.getDouble("lon");                                
            } catch (JSONException e) {	                
            }                                                      
		    
            mConfigurationActivity.setLat(lat);
            mConfigurationActivity.setLon(lon);
            mConfigurationActivity.setLocationID(-1);
            mConfigurationActivity.setLoc(location);
            mConfigurationActivity.setLocationType(LOCATION_TYPE_CURRENT);
            
            Preferences.setLocationLat(this, mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLat());
            Preferences.setLocationLon(this, mConfigurationActivity.getAppWidgetId(), (float)mConfigurationActivity.getLon());
            Preferences.setLocation(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLoc());
            
            mConfigurationActivity.getCheckCurrent().setChecked(true);
            mConfigurationActivity.getCheckCurrent().setText(getResources().getText(R.string.locationcurrent) + 
            		" [" + Preferences.getLocation(this, mConfigurationActivity.getAppWidgetId()) + "]");
            
            Preferences.setLocationId(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationID());
            Preferences.setLocationType(this, mConfigurationActivity.getAppWidgetId(), mConfigurationActivity.getLocationType());
	        
	        //fillData();
            
            // Show the ProgressDialogFragment on this thread
            mProgressFragment = new ProgressDialogFragment();
    		Bundle args = new Bundle();
    		args.putString("title", (String) getResources().getText(R.string.working));
    		args.putString("message", (String) getResources().getText(R.string.retrieving));
    		mProgressFragment.setArguments(args);
    		mProgressFragment.show(getSupportFragmentManager(), "tagProgress");

            // Start a new thread that will download all the data
            mDataProviderTask = new DataProviderTask();
            mDataProviderTask.setActivity(mConfigurationActivity);
            mDataProviderTask.execute();
            		    
		    return true;
			
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
			return false;
		}
    		
	}
	
	private boolean parseCustomLocation(int item) {
		int cityId = -1;
        String name = "";
        double lat = Double.NaN;
        double lon = Double.NaN;
        
        if (list == null || list.length() < item)
        	return false;
        
    	try {
	    	JSONObject cityJSON = list.getJSONObject(item);
            
        	cityId = cityJSON.getInt("id");
            name = cityJSON.getString("name");
            String country = "";
            
            JSONObject sys = null;
            try {
                sys = cityJSON.getJSONObject("sys");
            } catch (JSONException e) {
            	return false;
            }
            try {
                country = sys.getString("country");                              
            } catch (JSONException e) {
            	return false;
            }
            
            JSONObject coord = null;
            lat = Double.NaN;
            lon = Double.NaN;
            try {
            	coord = cityJSON.getJSONObject("coord");
            } catch (JSONException e) {
            	return false;
            }
            try {
                lat = coord.getDouble("lat");
                lon = coord.getDouble("lon");
            } catch (JSONException e) {
            	return false;
            }
    	} catch (JSONException e) {
    		return false;
        }
    		    	
    	mDbHelper.open();
    	Cursor locationsCursor = mDbHelper.fetchAllLocations();
    	
    	boolean bFound = false;
    	
    	if (locationsCursor != null && locationsCursor.getCount() > 0) {
    		locationsCursor.moveToFirst();
        	
        	do {
        		long locId = locationsCursor.getLong(locationsCursor.getColumnIndex(SQLiteDbAdapter.KEY_LOCATION_ID));              

        		if (locId != -1 && cityId == locId) {
        			bFound = true;
        		}

        		locationsCursor.moveToNext();

        	} while (!locationsCursor.isAfterLast());
    	}
    	
    	if (bFound == false && cityId >= 0) {
    		long id = mDbHelper.createLocation(cityId, lat, lon, name);
    	}
    	
    	mDbHelper.close();
    	
    	//fillData();
    	
        // Show the ProgressDialogFragment on this thread
        mProgressFragment = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", (String) getResources().getText(R.string.working));
		args.putString("message", (String) getResources().getText(R.string.retrieving));
		mProgressFragment.setArguments(args);
		mProgressFragment.show(getSupportFragmentManager(), "tagProgress");

        // Start a new thread that will download all the data
        mDataProviderTask = new DataProviderTask();
        mDataProviderTask.setActivity(mConfigurationActivity);
        mDataProviderTask.execute();
        
        return true;
	}
	
	void selectLocation (boolean current) {
		if (current) {
			if ((!parseCurrentLocation(getAppWidgetId(), parseString)) || (items != null && items.length <= 0)) {
				Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
				return;
			}
		} else
		{
			if ((!parseCustomLocations(getAppWidgetId(), parseString)) || (items != null && items.length <= 0)) {
				Toast.makeText(getDialogContext(), getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
				return;
			}
			
			showSelectLocationAlertDialog(items);
		}    	
	}
    
    /**
     * Custom location
     */
    
    public static class HttpTask extends AsyncTask<HttpTaskInfo, Void, OpenQuery> {

    	ConfigureLocationActivity locationActivity = null;
    	ProgressDialogFragment mProgressFragment;
        int mProgress = 0;

        void setProgressFragment(ProgressDialogFragment fragment) {
        	mProgressFragment = fragment;
        }
    	
    	void setActivity(ConfigureLocationActivity activity) {
    		locationActivity = activity;
		}
    	
    	ConfigureLocationActivity getActivity() {
    		return locationActivity;
		}
    	
    	@Override
		protected void onPreExecute() {
    		 	
        }
    	
    	public OpenQuery doInBackground(HttpTaskInfo... info){
	    	
    		String cityName = info[0].cityName;
	    	int appWidgetId = info[0].appWidgetId;
	    	double latitude = info[0].latitude;
		    double longitude = info[0].longitude; 
		    
		    String lang = Preferences.getLanguageOptions(locationActivity.getDialogContext());
	    	
		    if (lang.equals("")) {
	    		String langDef = Locale.getDefault().getLanguage();
	    		
	    		if (!langDef.equals(""))
	    			lang = langDef;
	    		else
	    			lang = "en";    	    
	    	}
	    	
	    	OpenQuery openResult = null;

	    	try {
	        	Reader responseReader = null;
		        HttpClient client = new DefaultHttpClient();
		        HttpGet request = null;
		        
		        if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
		        	//request = new HttpGet(String.format(GET_CITY_URL, latitude, longitude, lang));
		        	request = new HttpGet(String.format(WidgetUpdateService.WEATHER_SERVICE_COORD_URL, latitude, longitude, lang));
		        	
		        } else if (cityName != "") {
		        	cityName = cityName.replaceAll(" ", "%20");
			        request = new HttpGet(String.format(FIND_CITIES_URL, cityName, lang));
		        } else {
		        	return openResult;
		        }
		        
		        if (request == null) {
		        	return openResult;
		        }
		        
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
	            
	            openResult = new OpenQuery(result.toString(), TextUtils.isEmpty(cityName));
	            
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
    	
    	@Override
		protected void onPostExecute(OpenQuery found) {
    		
    		if (locationActivity == null || locationActivity.isFinishing()) {
    			if (mProgressFragment != null)
        			mProgressFragment.taskFinished();
    			
    			return;
    		}
    		
    		locationActivity.showProgressDialog(false);
    		
    		if (found != null) { 
    			locationActivity.parseString = found.httpResult;
            	locationActivity.selectLocation(found.current);            	
            } else { 
            	locationActivity.setLat(Double.NaN);
            	locationActivity.setLon(Double.NaN);
            	locationActivity.setLocationID(-1);
            	//mLocation = "";
                
            	locationActivity.runOnUiThread(new Runnable() {
              	  public void run() {
              		  Toast.makeText(locationActivity.getDialogContext(), locationActivity.getResources().getText(R.string.locationnotfound), Toast.LENGTH_LONG).show();
              	  }
              	});
            }
    		
    		if (mProgressFragment != null)
    			mProgressFragment.taskFinished();
        }
	}
	
	public class HttpTaskInfo {
		String cityName = "";
		double latitude = Double.NaN;
	    double longitude = Double.NaN; 
		int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	}
	
	private static class OpenQuery {
        String httpResult = null;
        boolean current = true;
        
        public OpenQuery(String query, boolean emptyCity) {
        	httpResult = query;            
        	current = emptyCity;
        }        
	}
	    
    public void setConfigureResult(int resultCode) {
        final Intent data = new Intent();
        data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, getAppWidgetId());
        setResult(resultCode, data);
    }

	@Override
    protected void onPause() {
        super.onPause();                              
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        
        this.setTitle(getResources().getString(R.string.locationsettings));                               
    }

	public double getLat() {
		return mLat;
	}

	public void setLat(double mLat) {
		this.mLat = mLat;
	}

	public double getLon() {
		return mLon;
	}

	public void setLon(double mLon) {
		this.mLon = mLon;
	}

	public void setLoc(String mLocation) {
		this.mLocation = mLocation;
	}
	
	public String getLoc() {
		 return mLocation;
	}

	public long getLocationID() {
		return mLocationID;
	}

	public void setLocationID(long mLocationID) {
		this.mLocationID = mLocationID;
	}

	public int getLocationType() {
		return mLocationType;
	}

	public void setLocationType(int mLocationType) {
		this.mLocationType = mLocationType;
	}

	public EditText getEditLocation() {
		return mEditLocation;
	}

	public void setEditLocation(EditText mEditLocation) {
		this.mEditLocation = mEditLocation;
	}

	public int getAppWidgetId() {
		return mAppWidgetId;
	}

	public void setAppWidgetId(int mAppWidgetId) {
		this.mAppWidgetId = mAppWidgetId;
	}

	/**
	 * @return the mCheckCurrent
	 */
	private TintCheckBox getCheckCurrent() {
		return mCheckCurrent;
	}

	/**
	 * @param mCheckCurrent the mCheckCurrent to set
	 */
	private void setCheckCurrent(TintCheckBox mCheckCurrent) {
		this.mCheckCurrent = mCheckCurrent;
	}
}
	