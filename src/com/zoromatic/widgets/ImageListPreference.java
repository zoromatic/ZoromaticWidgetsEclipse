/**
 * Copyright CMW Mobile.com, 2010. 
 */
package com.zoromatic.widgets;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.ListAdapter;

/**
 * The ImageListPreference class responsible for displaying an image for each
 * item within the list.
 * @author Casper Wakkers
 */
public class ImageListPreference extends ListPreference {
	private int[] imageIds = null;
	private int[] colorIds = null;

	/**
	 * Constructor of the ImageListPreference. Initializes the custom images.
	 * @param context application context.
	 * @param attrs custom xml attributes.
	 */
	public ImageListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray typedArray = context.obtainStyledAttributes(attrs,
			R.styleable.ImageListPreference);
		
		if (typedArray.getIndexCount() > 0) {

			int id = typedArray.getResourceId(R.styleable.ImageListPreference_entryImages, -1);
			
			if (id >= 0) {
				String[] imageNames = context.getResources().getStringArray(id);
				
				if (imageNames[0] == null) {
					imageIds = null;
				} else {
		
					imageIds = new int[imageNames.length];
			
					for (int i=0;i<imageNames.length;i++) {
						String imageName = imageNames[i].substring(
							imageNames[i].indexOf('/') + 1,
							imageNames[i].lastIndexOf('.'));
			
						imageIds[i] = context.getResources().getIdentifier(imageName,
							null, context.getPackageName());
					}							
				}
			} else
				imageIds = null;
			
			id = typedArray.getResourceId(R.styleable.ImageListPreference_entryColors, -1);
			
			if (id >= 0) {			
				String[] colorNames = context.getResources().getStringArray(id);
		
				if (colorNames[0] == null) {
					colorIds = null;
				} else {
					colorIds = new int[colorNames.length];
			
					for (int i=0;i<colorNames.length;i++) {
						String colorName = colorNames[i].substring(
							colorNames[i].indexOf('/') + 1,
							colorNames[i].lastIndexOf('.'));
			
						colorIds[i] = context.getResources().getIdentifier(colorName,
							null, context.getPackageName());
					}							
				}
			} else
				colorIds = null;
		}	
		
		typedArray.recycle();
	}
	/**
	 * {@inheritDoc}
	 */
	protected void onPrepareDialogBuilder(Builder builder) {
		int index = findIndexOfValue(getSharedPreferences().getString(
			getKey(), "1"));

		ListAdapter listAdapter = new ImageArrayAdapter(getContext(),
			R.layout.listitem, getEntries(), imageIds, colorIds, index);

		// Order matters.
		builder.setAdapter(listAdapter, this);
		super.onPrepareDialogBuilder(builder);
	}
}
