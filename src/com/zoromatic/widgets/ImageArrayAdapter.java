/**
 * Copyright CMW Mobile.com, 2010. 
 */
package com.zoromatic.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

/**
 * The ImageArrayAdapter is the array adapter used for displaying an additional
 * image to a list preference item.
 * @author Casper Wakkers
 */
public class ImageArrayAdapter extends ArrayAdapter<CharSequence> {
	private int index = 0;
	private int[] imageIds = null;
	private int[] colorIds = null;
	private String[] fontPaths = null;

	/**
	 * ImageArrayAdapter constructor.
	 * @param context the context.
	 * @param textViewResourceId resource id of the text view.
	 * @param objects to be displayed.
	 * @param ids resource id of the images to be displayed.
	 * @param i index of the previous selected item.
	 */
	public ImageArrayAdapter(Context context, int textViewResourceId,
			CharSequence[] objects, int[] images, int[] colors, String[] fonts, int i) {
		super(context, textViewResourceId, objects);

		index = i;
		imageIds = images;
		colorIds = colors;
		fontPaths  = fonts;
	}
	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
		View row = inflater.inflate(R.layout.listitem, parent, false);

		if (imageIds != null && imageIds.length > 0) {
			ImageView imageView = (ImageView)row.findViewById(R.id.image);
			imageView.setImageResource(imageIds[position]);
		}
		
		CheckedTextView checkedTextView = (CheckedTextView)row.findViewById(R.id.check);		
		checkedTextView.setText(getItem(position));
		
		checkedTextView.setTextColor(Color.BLACK);
		checkedTextView.setBackgroundColor(Color.WHITE);
		
		if (colorIds != null && colorIds.length > 0) {
			int txtColor = ((Activity)getContext()).getResources().getColor(colorIds[position]);			
			
			if (txtColor == Color.WHITE)
				checkedTextView.setTextColor(Color.LTGRAY);
			else
				checkedTextView.setTextColor(txtColor);	
		}
		
		if (fontPaths != null && fontPaths.length > 0) {			
			
			Typeface typeface = Typeface.createFromAsset(((Activity)getContext()).getAssets(), 
					fontPaths[position]);
			
			checkedTextView.setTypeface(typeface);			
		}
        
        if (position == index) {
			checkedTextView.setChecked(true);
		}

		return row;
	}
}
