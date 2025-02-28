/* SVN FILE: $Id: AAGWindSpeedSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/AAGWindSpeedSensor.java $
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
 * Get and return the wind speed from an AAG V3 anemometer
 * 
 * @author Seth Cohen
 * 
 */
public class AAGWindSpeedSensor extends HardwareSensor implements WindSpeedSensor
{

    private static final long serialVersionUID = 1L;

    private OneWireContainer1D windSpeedDevice = null;
    private static final int maxRetryCount = 3;
    private long lastCount = 0;
    private long lastTicks = 0;

    /**
     * 
     * Constructor that will instantiate the wind speed sensor of the specified
     * ID on the specified adapter.
     * 
     * @param adapter
     *            The 1-Wire adapter this sensor is connected to.
     * @param config
     *            The SensorConfiguration that specifies all the parameters for
     *            this sensor.
     * 
     */
    public AAGWindSpeedSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	windSpeedDevice = new OneWireContainer1D(adapter, config.getID());
	windSpeedDevice.setSpeed(DSPortAdapter.SPEED_FLEX, true);
    }

    /**
     * Method to read the wind speed sensor and return the speed in miles per
     * hour.
     * 
     * @return The wind speed in miles per hour. If there was not a valid
     *         reading, return Float.MIN_VALUE.
     * 
     */
    public float getWindSpeed()
    {
	float windSpeed = Float.MIN_VALUE;
	boolean bOK = false;
	int retryCount = 0;

	if (windSpeedDevice != null && isEnabled())
	{
	    if (this.isDebugFlag())
	    {
		ErrorLog.logError("AAG Wind Speed: Device = " + windSpeedDevice.getName() + 
					" ID = " + windSpeedDevice.getAddressAsString());
	    }

	    while (!bOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();

		    // read wind counter & system time
		    long currentCount = windSpeedDevice.readCounter(15);
//                    long currentTicks = System.currentTimeMillis();
                    long currentTicks = System.nanoTime();

		    if (lastTicks != 0)
		    {
			// calculate the wind speed based on the revolutions per  second
//                        windSpeed = ((currentCount - lastCount) / ((currentTicks - lastTicks) / 1000f)) / 2.0f * 2.453f; // MPH
                        windSpeed = ((currentCount - lastCount) / ((currentTicks - lastTicks) / 1000000000.0f)) / 2.0f * 2.453f; // MPH
		    }

		    if (this.isDebugFlag())
		    {
//                        ErrorLog.logError("Count = " + (currentCount-lastCount) + " during " +
//                                (currentTicks-lastTicks) + "ms calcs to " + windSpeed);
                        ErrorLog.logError("Count = " + (currentCount-lastCount) + " during " +
                                (currentTicks-lastTicks) + "ns calcs to " + windSpeed);
		    }

		    // remember count & time
		    lastCount = currentCount;
		    lastTicks = currentTicks;
		    bOK = true;
		    
		    this.getPath().close();
		    
		}
		catch (OneWireException e)
		{
		    ErrorLog.logError("Error Reading Wind Speed: " + e);
		    retryCount++;
		}
	    }
	    if (retryCount >= maxRetryCount)
	    {
		ErrorLog.logError("Wind Speed: Retry count exceeded");
	    }
	}
	return windSpeed;
    }
}
