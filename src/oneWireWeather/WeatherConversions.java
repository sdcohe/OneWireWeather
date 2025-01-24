/* SVN FILE: $Id: WeatherConversions.java 134 2013-07-19 19:32:22Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 134 $
* $Date: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $LastChangedBy: seth $
* $LastChangedDate: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/WeatherConversions.java $
* $Copyright: (c)2007 Seth Cohen. All Rights Reserved $
* $License: This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.$
*/

package oneWireWeather;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Conversions and useful functions for the weather application.
 * 
 * @author Seth Cohen
 * 
 */
public final class WeatherConversions
{

    // may need to make this configurable
    private static final int NORTH_OFFSET = 0;

    // temperature conversions
    /**
     * Convert Fahrenheit to Centigrade
     * 
     * @param tempFahrenheit
     *            The temperature in Fahrenheit
     * @return The equivalent temperature in Centigrade
     */
    public static float fahrenheitToCentigrade(float tempFahrenheit)
    {
	return ((tempFahrenheit - 32.0f) / 9.0f * 5.0f);
    }

    /**
     * Convert Centigrade to Kelvin
     * 
     * @param tempCentigrade
     *            The temperature in centigrade
     * @return The equivalent temperature in kelvin
     */
    public static float centigradeToKelvin(float tempCentigrade)
    {
	return tempCentigrade + 273.15F;
    }

    /**
     * Convert kelvin to centigrade
     * 
     * @param tempKelvin
     *            The temperature in degrees kelvin
     * @return The equivalent temperature in degrees centigrade
     */
    public static float kelvinToCentigrade(float tempKelvin)
    {
	return tempKelvin - 273.15F;
    }

    /**
     * Convert kelvin to fahrenheit
     * 
     * @param tempKelvin
     *            The temperature in kelvin
     * @return The equivalent temperature in fahrenheit
     */
    public static float kelvinToFahrenheit(float tempKelvin)
    {
	return centigradeToFahrenheit(kelvinToCentigrade(tempKelvin));
    }

    /**
     * @param tempCentigrade
     * @return
     */
    public static float centigradeToFahrenheit(float tempCentigrade)
    {
	return (tempCentigrade * 9 / 5) + 32;
    }

    /**
     * @param tempFahrenheit
     * @return
     */
    public static float fahrenheitToKelvin(float tempFahrenheit)
    {
	return centigradeToKelvin(fahrenheitToCentigrade(tempFahrenheit));
    }

    // speed conversions
    /**
     * @param mph
     * @return
     */
    public static float mphToMetersSec(float mph)
    {
	return (float) (mph * 0.44704);
    }

    /**
     * @param mph
     * @return
     */
    public static float mphTokph(float mph)
    {
	return (float) (mph * 1.60934);
    }

    /**
     * @param mph
     * @return
     */
    public static float mphToKnots(float mph)
    {
	return (float) (mph * 0.8680);
    }

    /**
     * @param mps
     * @return
     */
    public static float metersPerSecToMPH(float mps)
    {
	return (float) (mps * 2.23693);
    }

    /**
     * @param mps
     * @return
     */
    public static float metersPerSecToKPH(float mps)
    {
	return (float) (mps * 3.6);
    }

    /**
     * @param mps
     * @return
     */
    public static float metersPerSecToKnots(float mps)
    {
	return (float) (mps * 1.96936);
    }

    /**
     * @param kph
     * @return
     */
    public static float kphToMph(float kph)
    {
	return (float) (kph * 0.62137);
    }

    /**
     * @param kph
     * @return
     */
    public static float kphToMetersPerSecond(float kph)
    {
	return (float) (kph * 0.27778);
    }

    /**
     * @param kph
     * @return
     */
    public static float kphToKnots(float kph)
    {
	return (float) (kph * 0.54704);
    }

    /**
     * @param knots
     * @return
     */
    public static float knotsToMph(float knots)
    {
	return (float) (knots * 1.15193);
    }

    /**
     * @param knots
     * @return
     */
    public static float knotsToMetersPerSecond(float knots)
    {
	return (float) (knots * 0.50778);
    }

    /**
     * @param knots
     * @return
     */
    public static float knotsToKph(float knots)
    {
	return (float) (knots * 1.82800);
    }

    // distance conversions

    // miles to km
    public static float milesToKilometers(float miles)
    {
	return miles * 1.609344f;
    }
    
    // km to miles
    public static float kilometersToMiles(float kilometers)
    {
	return kilometers / 1.609344f;
    }

    // pressure conversions
    /**
     * @param inches
     * @return
     */
    public static float inchesToMillibars(float inches)
    {
	return (float) (inches * 33.8653);
    }

    /**
     * @param inches
     * @return
     */
    public static float inchesToKiloPascals(float inches)
    {
	return (float) (inches * 3.38653);
    }

    /**
     * @param millibars
     * @return
     */
    public static float millibarsToInches(float millibars)
    {
	return (float) (millibars / 33.8653);
    }

    /**
     * @param millibars
     * @return
     */
    public static float millibarsToKiloPascals(float millibars)
    {
	return (float) (millibars / 10);
    }

    /**
     * @param kiloPascals
     * @return
     */
    public static float kiloPascalsToInches(float kiloPascals)
    {
	return (float) (kiloPascals / 3.38653);
    }

    /**
     * @param kiloPascals
     * @return
     */
    public static float kiloPascalsToMillibars(float kiloPascals)
    {
	return (float) (kiloPascals * 10);
    }

    // literal lookups
    /**
     * @param trend
     * @return
     */
    public static String pressureTrendInInchesToLiteral(float trend)
    {
	String value = "";

	if (trend >= 0.06)
	{
	    value = "rising rapidly";
	}
	else if (trend >= 0.02 && trend < 0.06)
	{
	    value = "rising slowly";
	}
	else if (trend < 0.02 && trend > -0.02)
	{
	    value = "steady";
	}
	else if (trend <= -0.02 && trend > -0.06)
	{
	    value = "falling slowly";
	}
	else if (trend <= -0.06)
	{
	    value = "falling rapidly";
	}

	return value;
    }

    /**
     * @param speedMPH
     * @return
     */
    public static String windSpeedToLiteralMPH(float speedMPH)
    {
	String value = "";

	if (speedMPH < 1.0)
	{
	    value = "calm";
	}
	else if (speedMPH >= 1.0 && speedMPH < 4.0)
	{
	    value = "light air";
	}
	else if (speedMPH >= 4.0 && speedMPH < 8.0)
	{
	    value = "slight breeze";
	}
	else if (speedMPH >= 8.0 && speedMPH < 13.0)
	{
	    value = "gentle breeze";
	}
	else if (speedMPH >= 13.0 && speedMPH < 19.0)
	{
	    value = "moderate breeze";
	}
	else if (speedMPH >= 19.0 && speedMPH < 25.0)
	{
	    value = "fresh breeze";
	}
	else if (speedMPH >= 25.0 && speedMPH < 32.0)
	{
	    value = "strong breeze";
	}
	else if (speedMPH >= 32.0 && speedMPH < 39.0)
	{
	    value = "moderate gale";
	}
	else if (speedMPH >= 39.0 && speedMPH < 47.0)
	{
	    value = "fresh gale";
	}
	else if (speedMPH >= 47.0 && speedMPH < 55.0)
	{
	    value = "strong gale";
	}
	else if (speedMPH >= 55 && speedMPH < 64.0)
	{
	    value = "whole gale";
	}
	else if (speedMPH >= 64.0 && speedMPH < 75.0)
	{
	    value = "storm";
	}
	else if (speedMPH >= 75.0)
	{
	    value = "hurricane";
	}

	return value;
    }

    // convert direction value into compass direction string
    /**
     * @param input
     * @return
     */
    public static String getWindDirStr(int input)
    {
	String[] direction =
	{ " N ", "NNE", "NE ", "ENE", " E ", "ESE", "SE ", "SSE", " S ", "SSW",
		"SW ", "WSW", " W ", "WNW", "NW ", "NNW", " ---" };

	/*
	 * if (debugFlag) System.out.println("GetWindDirectionString input = " +
	 * input + " and cal = " + NORTH_OFFSET);
	 */

	// valid inputs 0 thru 16
	if (input < 0 || input >= 16)
	    input = 16;
	else
	    input = (input + NORTH_OFFSET) % 16;

	return direction[input];
    }

    // statistical calculations
    /**
     * @param tempFahrenheit
     * @param hum
     * @return
     */
    public static float calcDewpointFahrenheit(float tempFahrenheit, float hum)
    {
	if (tempFahrenheit == Float.MIN_VALUE || hum == Float.MIN_VALUE)
	    return Float.MIN_VALUE;

	// compute the dew point from relative humidity & temperature

	// convert to degrees K
	double tempK = (double) fahrenheitToKelvin(tempFahrenheit);

	// calc dewpoint
	double dp = tempK
		/ ((-0.0001846 * Math.log(hum / 100.0) * tempK) + 1.0);

	// convert back to degrees F
	return kelvinToFahrenheit((float) dp);
    }

    /**
     * @param tempCentigrade
     * @param hum
     * @return
     */
    public static float calcDewpointCentigrade(float tempCentigrade, float hum)
    {
	if (tempCentigrade == Float.MIN_VALUE || hum == Float.MIN_VALUE)
	    return Float.MIN_VALUE;

	// compute the dewpoint from relative humidity & temperature

	// now convert to degrees K
	double tempK = (double) centigradeToKelvin(tempCentigrade);

	// calc dewpoint
	double dp = tempK
		/ ((-0.0001846 * Math.log(hum / 100.0) * tempK) + 1.0);

	// convert back to degrees C
	return kelvinToCentigrade((float) dp);
    }

    /**
     * @param tempFahrenheit
     * @param humidity
     * @return
     */
    public static float calcHeatIndexFahrenheit(float tempFahrenheit,
	    float humidity)
    {
	if (tempFahrenheit < 80 || humidity < 40)
	{
	    return tempFahrenheit;
	}

	double tempSquared = tempFahrenheit * tempFahrenheit;
	double tempCubed = tempSquared * tempFahrenheit;
	double humiditySquared = humidity * humidity;
	double humidityCubed = humiditySquared * humidity;

	return (float) (16.923 + (0.185212 * tempFahrenheit)
		+ (5.37941 * humidity) - (0.100254 * tempFahrenheit * humidity)
		+ ((0.941695e-2) * tempSquared)
		+ ((0.728898e-2) * humiditySquared)
		+ ((0.345372e-3) * tempSquared * humidity)
		- ((0.814971e-3) * tempFahrenheit * humiditySquared)
		+ ((0.102102e-4) * tempSquared * humiditySquared)
		- ((0.38646e-4) * tempCubed) + ((0.291583e-4) * humidityCubed)
		+ ((0.142721e-5) * tempCubed * humidity)
		+ ((0.197483e-6) * tempFahrenheit * humidityCubed)
		- ((0.218429e-7) * tempCubed * humiditySquared)
		+ ((0.843296e-9) * tempSquared * humidityCubed) - ((0.481975e-10) * tempCubed * humidityCubed));
    }

    public static float calcWindChillFahrenheit(float temperature,
	    float windSpeed)
    {
	float windChillTemperature = temperature;

	if (temperature <= 50.0f && windSpeed >= 3.0f)
	{
	    // Windchill (ºF) = 35.74 + 0.6215T - 35.75(V^0.16) +
	    // 0.4275T(V^0.16)
	    windChillTemperature = (float) (35.74 + (0.6215 * temperature)
		    - (35.75 * Math.pow((double) windSpeed, 0.16)) + (0.4275 * temperature * Math
		    .pow(windSpeed, 0.16)));
	    if (windChillTemperature > temperature)
	    {
		windChillTemperature = temperature;
	    }
	}
	else
	{
	    windChillTemperature = temperature;
	}

	return windChillTemperature;
    }

    public static float estimateCloudBaseHeightFahrenheit(float tempF,
	    float dewPointF)
    {
	float spread = tempF - dewPointF;
	spread /= 4.4;
	return spread * 1000.0f;
    }

    // *** TODO: get a more accurate algorithm. This will do for the interim??
    public static float calcWetBulbF(float tempF, float rh)
    {
	double interim = ((tempF - 32) / 1.8);
	double wetBulb = ((-5.806 + 0.672 * interim - 0.006 * interim * interim
		+ (0.061 + 0.004 * interim + 0.000099 * interim * interim) * rh + (-0.000033
		- 0.000005 * interim - 0.0000001 * interim * interim)
		* rh * rh) * 1.8) + 32;
	return (float) wetBulb;
    }

    // planetary
    // sunrise
    // sunset
    // phase of moon
    // new moon
    // crescent moom
    // first quarter
    // waxing gibbous moon
    // full moon
    // waning gibbous moon
    // last quarter
    // old moon

    /**
     * @param value
     * @param numberDigits
     * @return
     */
    public static String formatFloat(float value, int numberDigits)
    {
	if (value == Float.MIN_VALUE || value == Float.MAX_VALUE)
	{
	    return "---";
	}

	if (numberDigits < 0)
	{
	    numberDigits = 0;
	}

	// try using DecimalFormat
	String formatString = "0.";
	for (int i = 0; i < numberDigits; i++)
	{
	    formatString += "0";
	}
	DecimalFormat df = new DecimalFormat(formatString);
	return df.format(value);

	// float roundVal = (float)(.5 * Math.pow(10, -1 * numberDigits));
	//
	// StringTokenizer tkn = new StringTokenizer(Double.toString(value +
	// roundVal), ".");
	// String integerPart = tkn.nextToken();
	// String decimalPart = tkn.nextToken();
	//
	// if (numberDigits == 0)
	// {
	// return integerPart;
	// }
	// else
	// {
	// return integerPart + "." + decimalPart.substring(0,numberDigits);
	// }
    }

    /**
     * @param value
     * @param numberDigits
     * @return
     */
    public static String formatFloat(String value, int numberDigits)
    {
	float floatValue;
	try
	{
	    floatValue = Float.parseFloat(value);
	    return formatFloat(floatValue, numberDigits);
	}
	catch (NumberFormatException e)
	{
	    return "---";
	}
    }

    public static String formatInt(int value)
    {
	if (value == Integer.MIN_VALUE || value == Integer.MAX_VALUE)
	    return "---";
	else
	    return Integer.toString(value);
    }

    public static int windDirToCompass(int windDir)
    {
	return windDir * 360 / 16;
    }

    public static Calendar localTimeToUTC(Calendar localTime)
    {
	Calendar utcTime = Calendar.getInstance();
	utcTime.setTime(localTime.getTime());
	TimeZone localTimeZone = localTime.getTimeZone();
	int offset = localTimeZone.getOffset(localTime.getTimeInMillis()) / 1000;
	utcTime.add(Calendar.SECOND, offset * -1);

	return utcTime;
    }

}
