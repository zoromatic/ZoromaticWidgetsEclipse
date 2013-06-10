package com.zoromatic.widgets;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ZoromaticWidgetsPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
						
		PreferenceManager localPrefs = getPreferenceManager();
        localPrefs.setSharedPreferencesName(Preferences.PREFS_NAME);
        
        addPreferencesFromResource(R.xml.zoromaticwidgets_prefs);
        
        ListPreference soundToggle = (ListPreference)findPreference(Preferences.PREF_SOUND_OPTIONS);
        
        if (soundToggle != null)
        {
        	soundToggle.setValueIndex(Preferences.getSoundOptions(this));
        	soundToggle.setSummary(soundToggle.getEntries()[Preferences.getSoundOptions(this)]);
        }
        
        Preference about = findPreference(Preferences.PREF_ABOUT_KEY);
        
        if (about != null) {
        	about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
				public boolean onPreferenceClick(Preference p) {
                	AboutDialog about = new AboutDialog(ZoromaticWidgetsPreferences.this);
                	about.setTitle("About Zoromatic Widgets");
                	about.show();
                    return true;
                }
            });        	        	
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case 0:
    		if (resultCode == RESULT_OK){
                
            }
    		break;		
		}
		super.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
    public void onBackPressed() {
        
		ListPreference soundToggle = (ListPreference)findPreference(Preferences.PREF_SOUND_OPTIONS);
        
        if (soundToggle != null)
        	Preferences.setSoundOptions(this, soundToggle.findIndexOfValue(soundToggle.getValue()));
        
		finish();
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	   
		if (key.equals(Preferences.PREF_BATTERY_NOTIF_KEY)) {
			CheckBoxPreference showBatteryNotif = (CheckBoxPreference)findPreference(Preferences.PREF_BATTERY_NOTIF_KEY);
	        
	        if (showBatteryNotif != null)
	        {
	        	Preferences.setShowBatteryNotif(this, showBatteryNotif.isChecked());
	        	
	        	Intent startIntent = new Intent(this, WidgetUpdateService.class);
	            startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, Intent.ACTION_BATTERY_CHANGED);        

	            this.startService(startIntent);
	        }
		}
		
		if (key.equals(Preferences.PREF_SOUND_OPTIONS)) {
			ListPreference soundToggle = (ListPreference)findPreference(Preferences.PREF_SOUND_OPTIONS);
	        
	        if (soundToggle != null)
	        {
	        	Preferences.setSoundOptions(this, soundToggle.findIndexOfValue(soundToggle.getValue()));
	        	soundToggle.setSummary(soundToggle.getEntries()[Preferences.getSoundOptions(this)]);
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