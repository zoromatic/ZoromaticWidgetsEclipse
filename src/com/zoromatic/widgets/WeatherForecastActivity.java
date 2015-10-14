package com.zoromatic.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint({ "SimpleDateFormat", "RtlHardcoded" })
public class WeatherForecastActivity extends ThemeActionBarActivity {

	private static String LOG_TAG = "WeatherForecastActivity";
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private static final int ACTIVITY_SETTINGS = 0;
	private BroadcastReceiver mReceiver;
	private Toolbar toolbar;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView leftDrawerList;
	private List<RowItem> rowItems;

	public static final String DRAWEROPEN = "draweropen";
	private boolean mDrawerOpen = false;

	private SlidingTabLayout mSlidingTabLayout;
	private ViewPager mViewPager;
	private ForecastFragmentPagerAdapter mFragmentPagerAdapter; 

	private List<ForecastPagerItem> mTabs = new ArrayList<ForecastPagerItem>();
	private int mCurrentItem = 0;
	private static final String KEY_CURRENT_ITEM = "key_current_item";

	private ProgressDialogFragment mProgressFragment = null;

	static DataProviderTask mDataProviderTask;
	static WeatherForecastActivity mWeatherForecastActivity; 
	private MenuItem refreshItem = null;
	LayoutInflater inflater = null;
	ImageView imageView = null;
	Animation rotation = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Read the appWidgetId to configure from the incoming intent
		mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}

		super.onCreate(savedInstanceState);

		mWeatherForecastActivity = this;

		String lang = Preferences.getLanguageOptions(this);

		if (lang.equals("")) {
			String langDef = Locale.getDefault().getLanguage();

			if (!langDef.equals(""))
				lang = langDef;
			else
				lang = "en";

			Preferences.setLanguageOptions(this, lang);                
		}

		// Change locale settings in the application
		Resources res = getApplicationContext().getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(lang.toLowerCase());
		res.updateConfiguration(conf, dm);

		setContentView(R.layout.weatherforecast);

		initView();
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		initDrawer();

		TypedValue outValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.colorPrimary,
				outValue,
				true);
		int primaryColor = outValue.resourceId;

		setStatusBarColor(findViewById(R.id.statusBarBackground), 
				getResources().getColor(primaryColor));	 
		
		/*inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (inflater != null) {
			imageView = (ImageView)inflater.inflate(R.layout.refresh_menuitem, null);
								
			if (imageView != null) {
				outValue = new TypedValue();
				getTheme().resolveAttribute(R.attr.iconRefresh,
						outValue,
						true);
				int refreshIcon = outValue.resourceId;
				imageView.setImageResource(refreshIcon);
				
				rotation = AnimationUtils.loadAnimation(this, R.anim.animate_menu);
			}
		}*/
		
		rotation = AnimationUtils.loadAnimation(this, R.anim.animate_menu);

		// Show the ProgressDialogFragment on this thread
		mProgressFragment = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", (String) getResources().getText(R.string.working));
		args.putString("message", (String) getResources().getText(R.string.retrieving));
		mProgressFragment.setArguments(args);
		mProgressFragment.show(getSupportFragmentManager(), "tagProgress");
		
		// Start a new thread that will download all the data
		mDataProviderTask = new DataProviderTask();
		mDataProviderTask.setActivity(mWeatherForecastActivity);
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

	@SuppressLint("NewApi")
	public void refreshData() {
		drawerLayout.closeDrawers();
		mDrawerOpen = false;

		float lat = Preferences.getLocationLat(getApplicationContext(), mAppWidgetId);
		float lon = Preferences.getLocationLon(getApplicationContext(), mAppWidgetId);
		long id = Preferences.getLocationId(getApplicationContext(), mAppWidgetId);

		if ((id == -1) && (lat == -222 || lon == -222 || Float.isNaN(lat) || Float.isNaN(lon))) {
			Toast.makeText(getApplicationContext(), getResources().getText(R.string.nolocationdefined), Toast.LENGTH_LONG).show();
		}
		else {
			Intent refreshIntent = new Intent(getApplicationContext(), WidgetUpdateService.class);
			refreshIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, WidgetUpdateService.WEATHER_UPDATE);
			refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			getApplicationContext().startService(refreshIntent);
			
			if (refreshItem != null && /*imageView != null && */rotation != null) {
				
				if (MenuItemCompat.getActionView(refreshItem) != null) {
					MenuItemCompat.getActionView(refreshItem).startAnimation(rotation);
				}
				
				//imageView.startAnimation(rotation);				
				//MenuItemCompat.setActionView(refreshItem, imageView);			
			}
			
			//Toast.makeText(getApplicationContext(), "Updating weather.", Toast.LENGTH_LONG).show();
		}
	}

	public void openSettings() {
		drawerLayout.closeDrawers();
		mDrawerOpen = false;

		Intent settingsIntent = new Intent(getApplicationContext(), DigitalClockAppWidgetPreferenceActivity.class);				

		if (settingsIntent != null) {
			settingsIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

			startActivityForResult(settingsIntent, ACTIVITY_SETTINGS);
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

	private void initView() {
		String theme = Preferences.getMainTheme(this);

		leftDrawerList = (ListView) findViewById(R.id.left_drawer);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

		rowItems = new ArrayList<RowItem>();

		RowItem item = new RowItem(theme.compareToIgnoreCase("light") == 0?R.drawable.ic_refresh_black_48dp:R.drawable.ic_refresh_white_48dp, 
				(String) getResources().getText(R.string.refresh), false);
		rowItems.add(item);
		item = new RowItem(theme.compareToIgnoreCase("light") == 0?R.drawable.ic_settings_black_48dp:R.drawable.ic_settings_white_48dp, 
				(String) getResources().getText(R.string.settings), false);
		rowItems.add(item);

		ItemAdapter adapter = new ItemAdapter(this, rowItems);
		leftDrawerList.setAdapter(adapter);        

		leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		if (theme.compareToIgnoreCase("light") == 0)
			leftDrawerList.setBackgroundColor(getResources().getColor(android.R.color.white));
		else 
			leftDrawerList.setBackgroundColor(getResources().getColor(android.R.color.black));
	}

	private void initDrawer() {

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				mDrawerOpen = false;
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				mDrawerOpen = true;
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	// The click listener for ListView in the navigation drawer 
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		leftDrawerList.setItemChecked(position, true);
		drawerLayout.closeDrawers(); 
		mDrawerOpen = false;

		switch (position) {
		case 0: // Refresh
			refreshData();
			break;
		case 1: // Settings
			openSettings();

			break;
		default:
			break;
		}            	
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);		
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean(DRAWEROPEN, mDrawerOpen);

		if (mViewPager != null)
			savedInstanceState.putInt(KEY_CURRENT_ITEM, mViewPager.getCurrentItem());

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		mDrawerOpen = savedInstanceState.getBoolean(DRAWEROPEN);
		mCurrentItem = savedInstanceState.getInt(KEY_CURRENT_ITEM);

		if (mDrawerOpen) {
			if (drawerLayout != null) {
				drawerLayout.openDrawer(Gravity.LEFT);
				mDrawerOpen = true;
				drawerToggle.syncState();
			}		
		}						
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.weatherforecastmenu, menu);
		
		refreshItem = menu.findItem(R.id.refresh);
		
		if (refreshItem != null) {
			final Menu menuFinal = menu;

			if (MenuItemCompat.getActionView(refreshItem) != null) {
				TypedValue outValue = new TypedValue();
				getTheme().resolveAttribute(R.attr.iconRefresh,
						outValue,
						true);
				int refreshIcon = outValue.resourceId;
				((ImageView)MenuItemCompat.getActionView(refreshItem)).setImageResource(refreshIcon);
				
				MenuItemCompat.getActionView(refreshItem).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {   
						menuFinal.performIdentifierAction(refreshItem.getItemId(), 0);
					}
				});
			}
		}
		
		return true;				
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.refresh:
			refreshData();

			return true;
		case R.id.settings:	    	
			openSettings();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == ACTIVITY_SETTINGS) {
			// Show the ProgressDialogFragment on this thread
			if (mProgressFragment != null) {
				mProgressFragment.dismiss();
			}
			mProgressFragment = new ProgressDialogFragment();
			Bundle args = new Bundle();
			args.putString("title", (String) getResources().getText(R.string.working));
			args.putString("message", (String) getResources().getText(R.string.retrieving));
			mProgressFragment.setArguments(args);
			mProgressFragment.show(getSupportFragmentManager(), "tagProgress");

			// Start a new thread that will download all the data
			mDataProviderTask = new DataProviderTask();
			mDataProviderTask.setActivity(mWeatherForecastActivity);
			mDataProviderTask.execute();
		}    	    	     
	}

	public boolean loadData() {
		try {
			if (mTabs != null) {
				//Thread.sleep(3000);

				for (ForecastPagerItem tab : mTabs) {
					tab.setFragment(null);
				}

				mTabs.clear();			        

				TypedValue outValue = new TypedValue();
				getTheme().resolveAttribute(R.attr.tabTextColor, outValue, true);
				int textColor = outValue.resourceId;
				int colorIndicator = getResources().getColor(textColor);	 

				int locationType = Preferences.getLocationType(getApplicationContext(), mAppWidgetId);

				if (locationType == ConfigureLocationActivity.LOCATION_TYPE_CURRENT) {
					mTabs.add(new ForecastPagerItem("Current [" + Preferences.getLocation(getApplicationContext(), mAppWidgetId) + "]", colorIndicator, mAppWidgetId, -1));
				}

				long locIdTemp = -1;

				if (locationType == ConfigureLocationActivity.LOCATION_TYPE_CUSTOM) {
					locIdTemp = Preferences.getLocationId(getApplicationContext(), mAppWidgetId);
				}

				SQLiteDbAdapter dbHelper = new SQLiteDbAdapter(getApplicationContext());
				dbHelper.open();
				Cursor locationsCursor = dbHelper.fetchAllLocations();

				if (locationsCursor != null && locationsCursor.getCount() > 0) {

					// insert default location
					locationsCursor.moveToFirst();

					do {
						String title = locationsCursor.getString(locationsCursor.getColumnIndex(SQLiteDbAdapter.KEY_NAME));
						long locId = locationsCursor.getLong(locationsCursor.getColumnIndex(SQLiteDbAdapter.KEY_LOCATION_ID));

						if (locIdTemp >= 0 && locId == locIdTemp)
							mTabs.add(new ForecastPagerItem(title, colorIndicator, mAppWidgetId, locId));	        		

						locationsCursor.moveToNext();
					} while (!locationsCursor.isAfterLast());	        

					// insert other locations
					locationsCursor.moveToFirst();

					if (locationsCursor != null) {
						do {
							String title = locationsCursor.getString(locationsCursor.getColumnIndex(SQLiteDbAdapter.KEY_NAME));
							long locId = locationsCursor.getLong(locationsCursor.getColumnIndex(SQLiteDbAdapter.KEY_LOCATION_ID));

							if (locIdTemp < 0 || locId != locIdTemp)
								mTabs.add(new ForecastPagerItem(title, colorIndicator, mAppWidgetId, locId));	        				

							locationsCursor.moveToNext();
						} while (!locationsCursor.isAfterLast());
					}
				}

				dbHelper.close(); 
			}

			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public boolean setSliders() {
		if (mTabs != null) {
			TypedValue outValue = new TypedValue();
			getTheme().resolveAttribute(R.attr.colorPrimary,
					outValue,
					true);
			int primaryColor = outValue.resourceId;            
			int colorTabs = getResources().getColor(primaryColor);

			mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
			mViewPager = (ViewPager) findViewById(R.id.viewpager);
			mFragmentPagerAdapter = new ForecastFragmentPagerAdapter(getSupportFragmentManager());

			mViewPager.setAdapter(mFragmentPagerAdapter);

			final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
					.getDisplayMetrics());
			mViewPager.setPageMargin(pageMargin);

			mSlidingTabLayout.setTabsColor(colorTabs);
			mSlidingTabLayout.setViewPager(mViewPager);

			mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

				@Override
				public int getIndicatorColor(int position) {
					if (mTabs.size() > position)
						return mTabs.get(position).getIndicatorColor();
					else {
						TypedValue outValue = new TypedValue();
						getTheme().resolveAttribute(R.attr.tabTextColor, outValue, true);
						int textColor = outValue.resourceId;
						int colorIndicator = getResources().getColor(textColor);

						return colorIndicator;
					}						
				}
			});

			if (mViewPager != null && mViewPager.getChildCount() > 0) 
				mViewPager.setCurrentItem(Math.min(mCurrentItem, mTabs.size()-1));
		}

		return true;
	}

	private class DataProviderTask extends AsyncTask<Void, Void, Void> {

		WeatherForecastActivity weatherForecastActivity = null;

		void setActivity(WeatherForecastActivity activity) {
			weatherForecastActivity = activity;
		}

		@SuppressWarnings("unused")
		WeatherForecastActivity getActivity() {
			return weatherForecastActivity;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.i(LOG_TAG, "WeatherForecastActivity - Background thread starting");

			weatherForecastActivity.loadData();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			weatherForecastActivity.setSliders();

			if (weatherForecastActivity.mProgressFragment != null) {
				weatherForecastActivity.mProgressFragment.dismiss(); 
			}                                	
		}        	
	}
	
	@SuppressLint("NewApi")
	void readCachedData (Context context) {
		if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
			
			if (refreshItem != null) {
				
				if (MenuItemCompat.getActionView(refreshItem) != null) {
					MenuItemCompat.getActionView(refreshItem).clearAnimation();
					//MenuItemCompat.setActionView(refreshItem, null);
				}
			}
			
			if (mTabs != null) {
				for (ForecastPagerItem tab : mTabs) {
					WeatherContentFragment fragment = tab.getFragment();

					if (fragment != null && fragment.getView() != null) {
						fragment.readCachedData(context, mAppWidgetId);
						
						SwipeRefreshLayout swipeLayoutFragment = fragment.getSwipeLayout();
						if (swipeLayoutFragment != null) {
							swipeLayoutFragment.setRefreshing(false);
						}
					}	            				            			                	
				}  	                		                
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter intentFilter = new IntentFilter(WidgetUpdateService.UPDATE_FORECAST);
		
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {    
				int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				
				if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && appWidgetId == mAppWidgetId) {
					readCachedData(context);
				}
			}			
		};

		registerReceiver(mReceiver, intentFilter);        
		setTitle(getResources().getString(R.string.weatherforecast));              
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(this.mReceiver);

		/*if (mTabs != null) {

        	for (ForecastPagerItem tab : mTabs) {
        		tab.setFragment(null);
        	}

        	mTabs.clear();			        
        }*/

		if (mViewPager != null) { 
			mViewPager.getAdapter().notifyDataSetChanged();    		
			mCurrentItem = mViewPager.getCurrentItem();
		}
	}

	@Override
	public void onBackPressed() {
		if (mDataProviderTask != null)
			mDataProviderTask.cancel(true);

		if (!mDrawerOpen) {
			super.onBackPressed();
			finish();
		} else {
			if (drawerLayout != null)
				drawerLayout.closeDrawers();
			mDrawerOpen = false;
		}		
	}

	static class ForecastPagerItem {
		private CharSequence mTitle;
		private final int mIndicatorColor;
		private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		private long mLocationId;

		private WeatherContentFragment mFragment;

		ForecastPagerItem(CharSequence title, int indicatorColor, int appWidgetId, long locId) {
			mTitle = title;
			mIndicatorColor = indicatorColor;
			mAppWidgetId = appWidgetId;
			mLocationId = locId;
		}

		Fragment createFragment() {
			Fragment fragment = WeatherContentFragment.newInstance(mTitle, mIndicatorColor, mAppWidgetId, mLocationId);
			((WeatherContentFragment) fragment).setTitle((String) mTitle);
			mFragment = (WeatherContentFragment) fragment;            

			return fragment;
		}

		CharSequence getTitle() {
			return mTitle;
		}

		void setTitle (CharSequence title) {
			mTitle = title;
		}

		int getIndicatorColor() {
			return mIndicatorColor;
		}

		public WeatherContentFragment getFragment() {
			return mFragment;
		}

		public void setFragment(WeatherContentFragment mFragment) {
			this.mFragment = mFragment;
		}        
	}

	class ForecastFragmentPagerAdapter extends FragmentStatePagerAdapter {

		ForecastFragmentPagerAdapter(FragmentManager fm) {
			super(fm);                        	       	        
		}

		@Override
		public Fragment getItem(int i) {
			return mTabs.get(i).createFragment();        	
		}

		@Override
		public int getCount() {
			return mTabs.size();        	
		}

		@Override
		public CharSequence getPageTitle(int position) {        	
			return mTabs.get(position).getTitle();        	
		}                             
	}
}
