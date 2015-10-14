/**
 * Copyright CMW Mobile.com, 2010. 
 */
package com.zoromatic.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.internal.widget.TintCheckedTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

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
	private int[] batteryIds = null;
	private int[] colorSchemeIds = null;

	/**
	 * ImageArrayAdapter constructor.
	 * @param context the context.
	 * @param textViewResourceId resource id of the text view.
	 * @param objects to be displayed.
	 * @param ids resource id of the images to be displayed.
	 * @param i index of the previous selected item.
	 */
	public ImageArrayAdapter(Context context, int textViewResourceId,
			CharSequence[] objects, int[] images, int[] colors, String[] fonts, int[] batteries, int[] colorSchemes, int i) {
		super(context, textViewResourceId, objects);

		index = i;
		imageIds = images;
		colorIds = colors;
		fontPaths  = fonts;
		batteryIds = batteries;
		colorSchemeIds = colorSchemes;
	}
	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.listitem, parent, false);

		if (imageIds != null && imageIds.length > 0) {
			ImageView imageView = (ImageView)row.findViewById(R.id.image);
			imageView.setImageResource(imageIds[position]);
		}
		
		if (batteryIds != null && batteryIds.length > 0) {
			ImageView imageView = (ImageView)row.findViewById(R.id.image);
			imageView.setImageResource(batteryIds[position]);
			
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				String theme = Preferences.getMainTheme(getContext());
	        	if (theme.compareToIgnoreCase("dark") == 0) {
					imageView.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_ATOP);
				} else {
					imageView.setColorFilter(Color.BLACK, android.graphics.PorterDuff.Mode.SRC_ATOP);
				}
			}
		}
		
		TintCheckedTextView checkedTextView = (TintCheckedTextView)row.findViewById(R.id.check);
		checkedTextView.setText(getItem(position));
		
		int[] attrs = new int[] { android.R.attr.textColorSecondary };
		TypedArray a = getContext().getTheme().obtainStyledAttributes(android.R.style.TextAppearance_Widget_TextView, attrs);
		int defaultTextColor = a.getColor(0, Color.BLACK);
		a.recycle();
		
		checkedTextView.setTextColor(defaultTextColor);
		
		if (colorIds != null && colorIds.length > 0) {
			int txtColor = getContext().getResources().getColor(colorIds[position]);			
			
			checkedTextView.setTextColor(txtColor);	
		}				
		
		if (fontPaths != null && fontPaths.length > 0) {			
			
			Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), 
					fontPaths[position]);
			
			checkedTextView.setTypeface(typeface);							
		}
		
		if (colorSchemeIds != null && colorSchemeIds.length > 0) {
			checkedTextView.setVisibility(View.GONE);
			
			ImageView imageView = (ImageView)row.findViewById(R.id.image);
			imageView.requestLayout();
			imageView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
			imageView.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			
			ColorDrawable cd = new ColorDrawable(getContext().getResources().getColor(colorSchemeIds[position]));
			imageView.setImageDrawable(cd);
			
			ImageView imageViewCheck = (ImageView)row.findViewById(R.id.imageCheck);
			imageViewCheck.requestLayout();
			imageViewCheck.getLayoutParams().width = LayoutParams.MATCH_PARENT;
			imageViewCheck.getLayoutParams().height = LayoutParams.MATCH_PARENT;
			
			if (position == index) {
				imageViewCheck.setVisibility(View.VISIBLE);
			} else {
				imageViewCheck.setVisibility(View.GONE);
			}				
		}
		
		if (position == index) {
			checkedTextView.setChecked(true);
		}

		return row;
	}
}
