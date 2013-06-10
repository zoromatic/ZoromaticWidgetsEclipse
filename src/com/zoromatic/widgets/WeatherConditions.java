package com.zoromatic.widgets;

class WeatherConditionPriorities {

	static final int PRIORITY_CLOUDS 		= 0;
	static final int PRIORITY_ATMOSPHERE 	= 1;
	static final int PRIORITY_DRIZZLE 		= 2;
	static final int PRIORITY_SNOW 			= 3;
	static final int PRIORITY_RAIN 			= 4;
	static final int PRIORITY_THUNDERSTORM 	= 5;
	static final int PRIORITY_EXTREME 		= 6;
    
    private WeatherConditionPriorities() {
      
    }
}

class WeatherIcon {
	String iconName;
	int iconId;
	boolean bDay;
	int altIconId;
	
	public WeatherIcon(String name, int id, boolean day, int altId) {
		iconId = id;
		iconName = name;
		bDay = day;
		altIconId = altId;
	}
}


public class WeatherConditions {	
	
	int THUNDERSTORM_WITH_LIGHT_RAIN 	= 200; 
	int THUNDERSTORM_WITH_RAIN 			= 201; 
	int THUNDERSTORM_WITH_HEAVY_RAIN 	= 202; 
	int LIGHT_THUNDERSTORM 				= 210; 
	int THUNDERSTORM 					= 211; 
	int HEAVY_THUNDERSTORM 				= 212; 
	int RAGGED_THUNDERSTORM 			= 221; 
	int THUNDERSTORM_WITH_LIGHT_DRIZZLE = 230;
	int THUNDERSTORM_WITH_DRIZZLE 		= 231;
	int THUNDERSTORM_WITH_HEAVY_DRIZZLE = 232; 

	int LIGHT_INTENSITY_DRIZZLE 		= 300; 
	int DRIZZLE 						= 301; 
	int HEAVY_INTENSITY_DRIZZLE 		= 302; 
	int LIGHT_INTENSITY_DRIZZLE_RAIN 	= 310; 
	int DRIZZLE_RAIN 					= 311; 
	int HEAVY_INTENSITY_DRIZZLE_RAIN 	= 312; 
	int SHOWER_DRIZZLE 					= 321; 

	int LIGHT_RAIN 						= 500; 
	int MODERATE_RAIN 					= 501; 
	int HEAVY_INTENSITY_RAIN 			= 502; 
	int VERY_HEAVY_RAIN 				= 503; 
	int EXTREME_RAIN 					= 504; 
	int FREEZING_RAIN 					= 511; 
	int LIGHT_INTENSITY_SHOWER_RAIN 	= 520; 
	int SHOWER_RAIN 					= 521; 
	int HEAVY_INTENSITY_SHOWER_RAIN 	= 522; 

	int LIGHT_SNOW 	= 600; 
	int SNOW 		= 601; 
	int HEAVY_SNOW 	= 602; 
	int SLEET 		= 611; 
	int SHOWER_SNOW = 621; 

	int MIST 				= 701; 
	int SMOKE 				= 711; 
	int HAZE 				= 721; 
	int SAND_DUST_WHIRLS 	= 731; 
	int FOG 				= 741; 

	int SKY_IS_CLEAR 		= 800; 
	int FEW_CLOUDS 			= 801; 
	int SCATTERED_CLOUDS 	= 802; 
	int BROKEN_CLOUDS 		= 803; 
	int OVERCAST_CLOUDS 	= 804; 

	int TORNADO 		= 900; 
	int TROPICAL_STORM 	= 901; 
	int HURRICANE 		= 902; 
	int COLD 			= 903; 
	int HOT 			= 904; 
	int WINDY 			= 905; 
	int HAIL 			= 906;
	
    public WeatherIcon[] m_ImageArr = 
	{
    	new WeatherIcon("01d", R.drawable.weather_01d, true, R.drawable.weather_01n),
    	new WeatherIcon("01n", R.drawable.weather_01n, false, R.drawable.weather_01d),
    	new WeatherIcon("02d", R.drawable.weather_02d, true, R.drawable.weather_02n),
    	new WeatherIcon("02n", R.drawable.weather_02n, false, R.drawable.weather_02d),
    	new WeatherIcon("03d", R.drawable.weather_03d, true, R.drawable.weather_03n),
    	new WeatherIcon("03n", R.drawable.weather_03n, false, R.drawable.weather_03d),
    	new WeatherIcon("04d", R.drawable.weather_04d, true, R.drawable.weather_04n),
    	new WeatherIcon("04n", R.drawable.weather_04n, false, R.drawable.weather_04d),
    	new WeatherIcon("09d", R.drawable.weather_09d, true, R.drawable.weather_09n),
    	new WeatherIcon("09n", R.drawable.weather_09n, false, R.drawable.weather_09d),
    	new WeatherIcon("10d", R.drawable.weather_10d, true, R.drawable.weather_10n),
    	new WeatherIcon("10n", R.drawable.weather_10n, false, R.drawable.weather_10d),
    	new WeatherIcon("11d", R.drawable.weather_11d, true, R.drawable.weather_11n),
    	new WeatherIcon("11n", R.drawable.weather_11n, false, R.drawable.weather_11d),
    	new WeatherIcon("13d", R.drawable.weather_13d, true, R.drawable.weather_13n),
    	new WeatherIcon("13n", R.drawable.weather_13n, false, R.drawable.weather_13d),
    	new WeatherIcon("50d", R.drawable.weather_50d, true, R.drawable.weather_50n),
    	new WeatherIcon("50n", R.drawable.weather_50n, false, R.drawable.weather_50d),
    	new WeatherIcon("r", R.drawable.weather_r, true, R.drawable.weather_r),
    	new WeatherIcon("r", R.drawable.weather_r, false, R.drawable.weather_r),
    	new WeatherIcon("sn50", R.drawable.weather_sn50, true, R.drawable.weather_sn50),
    	new WeatherIcon("sn50", R.drawable.weather_sn50, false, R.drawable.weather_sn50),
    	new WeatherIcon("t50", R.drawable.weather_t50, true, R.drawable.weather_t50),
    	new WeatherIcon("t50", R.drawable.weather_t50, false, R.drawable.weather_t50),
    	new WeatherIcon("w50", R.drawable.weather_w50, true, R.drawable.weather_w50),
    	new WeatherIcon("w50", R.drawable.weather_w50, false, R.drawable.weather_w50)
	};
}



