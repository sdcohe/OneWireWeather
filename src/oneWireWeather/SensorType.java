/* SVN FILE: $Id: SensorType.java 134 2013-07-19 19:32:22Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 134 $
* $Date: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $LastChangedBy: seth $
* $LastChangedDate: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/SensorType.java $
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

/**
 * Class to represent the various sensor type that are known to this application
 * and to hold the default values for them.
 * 
 * @author Seth Cohen
 * 
 */
public class SensorType
{

    // sensor usage types
    public static final int MAIN_OUTDOOR_TEMP = 1;
    public static final int MAIN_INDOOR_TEMP = 2;
    public static final int AUX_OUTDOOR_TEMP = 3;
    public static final int AUX_INDOOR_TEMP = 4;
    public static final int OUTDOOR_HUMIDITY = 5;
    public static final int INDOOR_HUMIDITY = 6;
    public static final int PRESSURE = 7;
    public static final int RAIN_COUNTER = 8;
    public static final int AAG_WIND_SPEED = 9;
    public static final int AAG_WIND_DIRECTION = 10;
    public static final int SOLAR = 11;
    public static final int LIGHTNING_COUNTER = 12;
    public static final int ADS_WIND_SPEED = 13;
    public static final int ADS_WIND_DIRECTION = 14;

    // sensor families
    public static final String TEMPERATURE_SENSOR_FAMILY = "10";
    public static final String RAIN_COUNTER_SENSOR_FAMILY = "1D";
    public static final String AAG_WIND_SPEED_SENSOR_FAMILY = "1D";
    public static final String ADS_WIND_SPEED_SENSOR_FAMILY = "1D";
    public static final String LIGHTNING_COUNTER_SENSOR_FAMILY = "1D";
    public static final String AAG_WIND_DIRECTION_SENSOR_FAMILY = "20";
    public static final String ADS_WIND_DIRECTION_SENSOR_FAMILY = "26";
    public static final String PRESSURE_SENSOR_FAMILY = "26";
    public static final String HUMIDITY_SENSOR_FAMILY = "26";
    public static final String SOLAR_SENSOR_FAMILY = "26";

    private int m_sensorType;
    private String m_family;
    private String m_description;
    private float defaultOffset;
    private float defaultGain;
    private int defaultPollFrequency;
    private int defaultPollOffset;

    /**
     * Create a new instance and initialize all the properties.
     * 
     * @param type
     *            The usage for this sensor ex: SensorType.MAIN_OUTDOOR_TEMP
     * @param family
     *            The family for this sensor type ex:
     *            SensorType.TEMPERATURE_SENSOR_FAMILY
     * @param description
     *            A description of this sensor type
     * @param defaultGain
     *            The default gain value for this sensor type
     * @param defaultOffset
     *            The default offset value for this sensor type
     * @param defaultPollFrequency
     *            The default poll frequency for this sensor type in
     *            milliseconds
     * @param defaultPollOffset
     *            The default offset from the start of poll period in
     *            millisecodns
     */
    public SensorType(int type, String family, String description,
	    float defaultGain, float defaultOffset, int defaultPollFrequency,
	    int defaultPollOffset)
    {
	this.m_sensorType = type;
	this.m_family = family;
	this.m_description = description;
	this.defaultGain = defaultGain;
	this.defaultOffset = defaultOffset;
	this.defaultPollFrequency = defaultPollFrequency;
	this.defaultPollOffset = defaultPollOffset;
    }

    /**
     * Get the usage type for this sensor type
     * 
     * @return the usage type for this sensor type
     */
    public int getSensorUsageType()
    {
	return this.m_sensorType;
    }

    /**
     * Get the sensor family for this sensor type
     * 
     * @return the sensor family for this sensor type
     */
    public String getFamily()
    {
	return this.m_family;
    }

    /**
     * Get the description for this sensor type
     * 
     * @return the description for this sensor type
     */
    public String getDescription()
    {
	return this.m_description;
    }

    /**
     * Get the default gain value for this sensor type
     * 
     * @return the default gain value
     */
    public float getDefaultGain()
    {
	return defaultGain;
    }

    /**
     * Get the default offset value for this sensor type
     * 
     * @return the default offset
     */
    public float getDefaultOffset()
    {
	return defaultOffset;
    }

    /**
     * Get the default poll frequency for this sensor type in millisecodns
     * 
     * @return the default poll frequency
     */
    public int getDefaultPollFrequency()
    {
	return defaultPollFrequency;
    }

    /**
     * Get the default poll offset in milliseconds for this sensor type
     * 
     * @return the default poll offset
     */
    public int getDefaultPollOffset()
    {
	return defaultPollOffset;
    }

}
