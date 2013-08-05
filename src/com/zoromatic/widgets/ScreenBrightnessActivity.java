package com.zoromatic.widgets;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.zoromatic.widgets.R;

public class ScreenBrightnessActivity extends Activity {

	private int brightnessValue;
	private boolean isAuto;
	private SeekBar seekBarBrightness;
	private CheckBox checkAuto;
	private Button buttonSave;
	private Button buttonCancel;

	public int mOldBrightness = MAXIMUM_BACKLIGHT;
	public int mOldAutomatic = 0;

	private static int mScreenBrightnessDim = 20;
	private static final int MAXIMUM_BACKLIGHT = 255;
	private static final int SEEK_BAR_RANGE = MAXIMUM_BACKLIGHT - mScreenBrightnessDim;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.brightnessdialog);
		setTitle("Set Brightness");

		seekBarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);

		seekBarBrightness
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						saveBrightness(getContentResolver(), brightnessValue);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						brightnessValue = progress + mScreenBrightnessDim;
						setBrightness(brightnessValue);
					}
				});

		seekBarBrightness.setMax(SEEK_BAR_RANGE);

		try {
			mOldBrightness = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException snfe) {
			mOldBrightness = MAXIMUM_BACKLIGHT;
		}

		seekBarBrightness.setProgress(mOldBrightness - mScreenBrightnessDim);

		checkAuto = (CheckBox) findViewById(R.id.checkBoxBrightness);

		checkAuto.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (isChecked) {
					startAutoBrightness();

					seekBarBrightness.setVisibility(View.GONE);
				} else {
					stopAutoBrightness();

					seekBarBrightness.setVisibility(View.VISIBLE);
					setBrightness(seekBarBrightness.getProgress()
							+ mScreenBrightnessDim);
				}
			}
		});

		try {
			mOldAutomatic = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
		} catch (SettingNotFoundException snfe) {
			mOldAutomatic = 0;
		}

		checkAuto.setChecked(mOldAutomatic != 0);

		isAuto = isAutoBrightness(getContentResolver());

		if (isAuto) {
			// stopAutoBrightness();

			seekBarBrightness.setVisibility(View.GONE);
		}

		buttonSave = (Button) findViewById(R.id.buttonSave);

		buttonSave.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}

		});

		buttonCancel = (Button) findViewById(R.id.buttonCancel);

		buttonCancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				if (mOldAutomatic == 0) {
					stopAutoBrightness();
				} else {
					startAutoBrightness();
				}

				saveBrightness(getContentResolver(), mOldBrightness);

				setResult(RESULT_CANCELED);
				finish();
			}

		});
	}

	@Override
	public void onBackPressed() {
		
		if (mOldAutomatic == 0) {
			stopAutoBrightness();
		} else {
			startAutoBrightness();
		}

		saveBrightness(getContentResolver(), mOldBrightness);

		setResult(RESULT_CANCELED);
		
		finish();		
	}

	public boolean isAutoBrightness(ContentResolver aContentResolver) {
		boolean automicBrightness = false;
		try {
			automicBrightness = Settings.System.getInt(aContentResolver,
					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}

	public void setBrightness(int bright) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = Float.valueOf(bright) * (1f / 255f);
		getWindow().setAttributes(lp);
	}

	public void startAutoBrightness() {
		Settings.System.putInt(getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	public void stopAutoBrightness() {
		Settings.System.putInt(getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	public void saveBrightness(ContentResolver resolver, int bright) {
		Uri uri = android.provider.Settings.System
				.getUriFor("screen_brightness");
		android.provider.Settings.System.putInt(resolver, "screen_brightness",
				bright);
		resolver.notifyChange(uri, null);
	}

}