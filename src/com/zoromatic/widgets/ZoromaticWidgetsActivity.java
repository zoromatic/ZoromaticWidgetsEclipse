package com.zoromatic.widgets;

import com.zoromatic.widgets.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class ZoromaticWidgetsActivity extends Activity {

	static final String LOG_TAG = "ZoromaticWidgetsActivity";
    CheckBox mShowBatteryNotification;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Context context = ZoromaticWidgetsActivity.this; 
        
        boolean bShowBatteryNotif = Preferences.getShowBatteryNotif(context);
        
        if (bShowBatteryNotif)
        {
        	Intent startIntent = new Intent(context, WidgetUpdateService.class);
            startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, Intent.ACTION_BATTERY_CHANGED);
            
            context.startService(startIntent);
        }
        
        mShowBatteryNotification = (CheckBox)findViewById(R.id.checkBoxShowBatteryNotif);        
        mShowBatteryNotification.setOnClickListener(mOnClickCShowBatteryNotif); 
                
        setControls(context);        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent); 
    	
    }
    
    @Override
	public void onBackPressed() {
        super.onBackPressed();
        savePreferences(ZoromaticWidgetsActivity.this);
    }
        
    public void savePreferences(Context context) {
    	mShowBatteryNotification = (CheckBox)findViewById(R.id.checkBoxShowBatteryNotif);
    	Preferences.setShowBatteryNotif(context, mShowBatteryNotification.isChecked());    	
    }
        
    public void setControls(Context context) {
    	mShowBatteryNotification = (CheckBox)findViewById(R.id.checkBoxShowBatteryNotif);
    	mShowBatteryNotification.setChecked(Preferences.getShowBatteryNotif(context));    				     
    }
    
    View.OnClickListener mOnClickCShowBatteryNotif = new View.OnClickListener() {
        @Override
		public void onClick(View v) {
        	final Context context = ZoromaticWidgetsActivity.this;
        	
        	savePreferences(ZoromaticWidgetsActivity.this);        	
        	
        	Intent startIntent = new Intent(context, WidgetUpdateService.class);
            startIntent.putExtra(WidgetInfoReceiver.INTENT_EXTRA, Intent.ACTION_BATTERY_CHANGED);
            
            context.startService(startIntent);        	        
        }
    };
}