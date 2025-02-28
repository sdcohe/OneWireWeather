/* SVN FILE: $Id: TemperatureSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/TemperatureSensor.java $
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

import oneWireWeather.ErrorLog;
import oneWireWeather.SensorConfiguration;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;

/**
 * This class encapsulates a hardware temperature sensor that uses
 * the Dallas semiconductor DS18S20 1-Wire temperature sensor chip.
 * 
 * @author Seth Cohen
 *
 */
public class TemperatureSensor extends HardwareSensor {

    private static final long serialVersionUID = 1L;
    private static final int maxRetryCount = 3;

    private OneWireContainer10 tempDevice = null;

    /**
     * Create an instance of the 1-Wire temperature sensor on a given adapter
     * using the specified configuration.
     * 
     * @param adapter  The adapter the temperature sensor is attached to
     * @param config   The configuration of the sensor
     */
    public TemperatureSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	tempDevice = new OneWireContainer10(adapter, config.getID());
	tempDevice.setSpeed(DSPortAdapter.SPEED_FLEX, true);

	// check to see if this device has greater than .5 degree resolution
	try
	{
	    this.getPath().open();
		
	    if (tempDevice.hasSelectableTemperatureResolution())
	    {
		// set the resolution to whatever the max is
		byte[] state = tempDevice.readDevice();
		tempDevice.setTemperatureResolution(OneWireContainer10.RESOLUTION_MAXIMUM, state);
		tempDevice.writeDevice(state);

		// System.out.println("Temp device " + deviceID + " supports high resolution");
	    }
	    
	    this.getPath().close();
	}
	catch (OneWireException e)
	{
	    ErrorLog.logError("Error getting resolution from device " + config.getID() + " " + e);
	}
    }

    /**
     * Get the current temperature reading.
     * 
     * @return The temperature in degrees F.
     */
    public float getTemperature()
    {
	float temperature = Float.MIN_VALUE;

	// make sure the temp device instance is not null
	if (tempDevice != null && isEnabled())
	{
	    if (this.isDebugFlag())
	    {
		ErrorLog.logError("Temp: Device = " + tempDevice.getName() + "  ID = " + tempDevice.getAddressAsString());
	    }

	    boolean bReadingOK = false;
	    int retryCount = 0;

	    while(!bReadingOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();
			
		    byte[] state = tempDevice.readDevice();
		    tempDevice.doTemperatureConvert(state);

		    state = tempDevice.readDevice();
		    float rawTemperature = (float)tempDevice.getTemperature(state);

		    // apply software compensation
		    float compensatedTemperature = rawTemperature * this.getGain() + this.getOffset();

		    // convert to degs F - comment out to get degrees C
		    temperature = (compensatedTemperature * 9.0f/5.0f) + 32.0f;

		    // sanity check reading.  Sometimes I see readings of 187.  Add a sanity check until
		    //        I can track down the issue
		    if (temperature < 150.0f) {
		        bReadingOK = true;
		    }

		    if (this.isDebugFlag())
		    {
			ErrorLog.logError("Temp device read raw: " + rawTemperature + " Compensated: " + compensatedTemperature + " TempF: " + temperature);
		    }
		    
		    this.getPath().close();
		}
		catch (OneWireException e)
		{
		    ErrorLog.logError("Error Reading Temperature from device " + tempDevice.getAddressAsString() + " : " + e);
		    retryCount++;
		}
	    }
	    if (retryCount >= maxRetryCount)
	    {
		ErrorLog.logError("Temperature: Retry count exceeded");
	    }
	}
	return temperature;
    }
}
