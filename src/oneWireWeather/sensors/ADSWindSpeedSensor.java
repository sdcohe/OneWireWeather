/* SVN FILE: $Id: ADSWindSpeedSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/ADSWindSpeedSensor.java $
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

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer1D;

/**
 * @author seth
 *
 */
public class ADSWindSpeedSensor extends HardwareSensor implements WindSpeedSensor
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
    public ADSWindSpeedSensor(DSPortAdapter adapter, SensorConfiguration config)
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
		ErrorLog.logError("ADS Wind Speed: Device = " + windSpeedDevice.getName() + 
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
			// calculate the wind speed in MPH based on the revolutions per second
//                        windSpeed = (float) ((1.25 *(currentCount-lastCount)) / ((currentTicks-lastTicks) / 1000f));
                        windSpeed = (float) ((1.25 *(currentCount-lastCount)) / ((currentTicks-lastTicks) / 1000000000.0f));
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
		    if (this.isDebugFlag())
		    {
			ErrorLog.logError("Error Reading Wind Speed: " + e);
		    }
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

