/* SVN FILE: $Id: WeatherSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/WeatherSensor.java $
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

package oneWireWeather.sensors;

import java.io.*;

import oneWireWeather.SensorType;

import com.dalsemi.onewire.utils.OWPath;

/**
 * The base class for all weather station sensor types. It serve as the base for
 * the concrete hardware sensors, as well as the base class for sensor
 * configuration settings as they share many of the same attributes.
 * 
 * @author Seth Cohen
 * 
 */
public class WeatherSensor implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String m_deviceID;
    private int m_usageType;
    private float m_gain;
    private float m_offset;
    private int m_pollFrequency;
    private int m_pollOffset;
    private boolean m_bDebugFlag = false;
    private boolean m_bEnabled = true;
    private OWPath path = null;

    /**
     * Construct a WeatherSensor, given a 1-Wire ID and a SensorType containing
     * the default values for this type of hardware sensor.
     * 
     * @param id
     *            The 1-Wire ID of this physical sensor
     * @param sensorType
     *            A SensorType containing the default values for this sensor
     * 
     */
    public WeatherSensor(String id, SensorType sensorType)
    {
	this.m_deviceID = id;
	this.m_usageType = sensorType.getSensorUsageType();
	this.m_gain = sensorType.getDefaultGain();
	this.m_offset = sensorType.getDefaultOffset();
	this.m_pollFrequency = sensorType.getDefaultPollFrequency();
	this.m_pollOffset = sensorType.getDefaultPollOffset();
	this.m_bDebugFlag = false;
	this.m_bEnabled = true;
    }

    /**
     * Construct a weather sensor given a 1-Wire ID and the specific parameters
     * for this sensor type
     * 
     * @param id
     *            The 1-Wire ID of this physical sensor
     * @param usage
     *            The usage type of this sensor (ex:
     *            SensorType.MAIN_OUTDOOR_TEMP)
     * @param gain
     *            The gain to apply to measurements made by this sensor
     * @param offset
     *            The offset to apply to measurements made by this sensor
     * @param pollFrequency
     *            How often to poll this sensor in milliseconds
     * @param pollOffset
     *            How many milliseconds from the minute start to poll this
     *            sensor
     * 
     */
    public WeatherSensor(String id, int usage, float gain, float offset,
	    int pollFrequency, int pollOffset, boolean debugFlag,
	    boolean enabled, OWPath path)
    {
	this.m_deviceID = id;
	this.m_usageType = usage;
	this.m_gain = gain;
	this.m_offset = offset;
	this.m_pollFrequency = pollFrequency;
	this.m_pollOffset = pollOffset;
	this.m_bDebugFlag = debugFlag;
	this.m_bEnabled = enabled;
	this.path = path;
    }

    /**
     * Get the 1-Wire ID for this sensor
     * 
     * @return The 1-Wire ID for this sensor as a string
     */
    public String getID()
    {
	return m_deviceID;
    }

    /**
     * ' Get the offset to apply to measurements made by this sensor
     * 
     * @return The offset to apply
     * 
     */
    public float getOffset()
    {
	return m_offset;
    }

    /**
     * Get the gain to apply to measurements made by this sensor
     * 
     * @return The gain to apply
     */
    public float getGain()
    {
	return m_gain;
    }

    /**
     * Set the 1-Wire address for this sensor
     * 
     * @param id
     *            The 1-Wire address for this sensor
     * 
     */
    public void setID(String id)
    {
	m_deviceID = id;
    }

    /**
     * Set the gain to apply to measurements made by this sensor
     * 
     * @param gain
     *            The gain for this sensor
     * 
     */
    public void setGain(float gain)
    {
	this.m_gain = gain;
    }

    /**
     * Set the offset to apply to measurements made by this sensor
     * 
     * @param offset
     *            The offset for this sensor
     * 
     */
    public void setOffset(float offset)
    {
	this.m_offset = offset;
    }

    /**
     * Get how frequently to poll this sensor
     * 
     * @return the poll frequency in milliseconds
     */
    public int getPollFrequency()
    {
	return m_pollFrequency;
    }

    /**
     * Set how frequently to poll this sensor
     * 
     * @param frequency
     *            the poll frequency for this sensor in milliseconds
     */
    public void setPollFrequency(int frequency)
    {
	m_pollFrequency = frequency;
    }

    /**
     * Get the offset from the start of a minute to poll this sensor. This is
     * used so that we don't try to poll all the sensors at the same time
     * 
     * @return the poll offset in milliseconds
     */
    public int getPollOffset()
    {
	return m_pollOffset;
    }

    /**
     * Set the offset from the start of a minute to poll this sensor. This is
     * used so that we don't try to poll all the sensors at the same time
     * 
     * @param offset
     *            the poll offset in milliseconds to set
     */
    public void setPollOffset(int offset)
    {
	m_pollOffset = offset;
    }

    /**
     * Get the usage type for this sensor.
     * <p>
     * Some example usage types are:
     * <ul>
     * <li>SensorType.MAIN_OUTDOOR_TEMP
     * <li>SensorType.OUTDOOR_HUMIDITY
     * <li>SensorType.PRESSURE
     * </ul>
     * 
     * @return the usage type for this sensor
     */
    public int getUsageType()
    {
	return m_usageType;
    }

    /**
     * Set the usage type for this sensor.
     * <p>
     * Some example usage types are:
     * <ul>
     * <li>SensorType.MAIN_OUTDOOR_TEMP
     * <li>SensorType.OUTDOOR_HUMIDITY
     * <li>SensorType.PRESSURE
     * </ul>
     * 
     * @param type
     *            the usage yype to set
     */
    public void setUsageType(int type)
    {
	m_usageType = type;
    }

    /**
     * @return
     */
    public boolean isDebugFlag()
    {
	return m_bDebugFlag;
    }

    /**
     * @param debugFlag
     */
    public void setDebugFlag(boolean debugFlag)
    {
	this.m_bDebugFlag = debugFlag;
    }

    public boolean isEnabled()
    {
	return m_bEnabled;
    }

    public void setEnabled(boolean m_bEnabled)
    {
	this.m_bEnabled = m_bEnabled;
    }

    public String toString()
    {
	return (m_deviceID + " " + ((path != null) ? path.toString() : ""));
    }

    public OWPath getPath()
    {
	return path;
    }

    public void setPath(OWPath path)
    {
	this.path = path;
    }
}
