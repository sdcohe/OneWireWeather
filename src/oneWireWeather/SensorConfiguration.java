/* SVN FILE: $Id: SensorConfiguration.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/SensorConfiguration.java $
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

import java.io.*;

import oneWireWeather.sensors.WeatherSensor;

/**
 * This class contains and allows maintenance of the sensor configuration
 * for the wetaher station.  It allows setting the ID's for the typical sensors, and also allows 
 * for additional sensors (such as additional temperature sensors). 
 * * 
 * @author Seth Cohen
 * 
 */
public class SensorConfiguration extends WeatherSensor implements Serializable
{

    private static final long serialVersionUID = 1L;

    protected String m_strName;
    protected String m_strDescription;
//    protected boolean m_bDebugFlag = false;

    /**
     * Create a new instance of the sensor configuration class.
     * 
     * @param name         The name of this sensor
     * @param description  The description of this sensor
     * @param ID           The 1-Wire ID of this sensor
     * @param type         The type of this sensor
     */
    public SensorConfiguration(String name, String description, String ID, SensorType type)
    {
	super(ID, type);
	this.m_strName = name;
	this.m_strDescription = description;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
	return "Name: " + this.getName() + " ID: " + this.getID() + " Description: " + this.getDescription();
    }

    /**
     * Get the decsription for this sensor.
     * 
     * @return the description for this sensor
     */
    public String getDescription() {
	return m_strDescription;
    }

    /**
     * Set the decsription for this sensor.
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {
	m_strDescription = description;
    }

    /**
     * Get the name for this sensor.
     * 
     * @return the name
     */
    public String getName() {
	return m_strName;
    }

    /**
     * Set the name for this sensor.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
	m_strName = name;
    }

}
