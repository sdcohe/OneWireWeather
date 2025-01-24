/* SVN FILE: $Id: RainSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/RainSensor.java $
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
 * This class encapsulates a hardware rain gauge. The rain gauge is composed of
 * a DSXXXX 1-wire counter chip. The class provides a method to read the sensor.
 * The offset field in the sensor configuration is subtracted from the count,
 * whcih is always cumulative.
 * 
 * @author Seth Cohen
 * 
 */
public class RainSensor extends HardwareSensor
{

    private static final long serialVersionUID = 1L;
    private static final int maxRetryCount = 3;

    // class variables
    private OneWireContainer1D rainDevice = null;

    /**
     * Create a new instance of the rain sensor.
     * 
     * @param adapter
     *            The adapter this rain gauge is physically connected to
     * @param config
     *            The configuration parameters for this sensor
     */
    public RainSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	// get instances of the 1-wire devices
	rainDevice = new OneWireContainer1D(adapter, config.getID());
	rainDevice.setSpeed(DSPortAdapter.SPEED_FLEX, true);

    }

    /**
     * Retrieve the current value of the rain counter.
     * 
     * @return The rain counter value.
     */
    public float getRainCount()
    {
	float rain = Float.MIN_VALUE;
	boolean bOK = false;
	int retryCount = 0;

	if (rainDevice != null && isEnabled())
	{
	    if (this.isDebugFlag())
	    {
		ErrorLog.logError("Rain: Device = " + rainDevice.getName() + "  ID = " + rainDevice.getAddressAsString());
	    }

	    while (!bOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();
			
		    // read rain count from counter 15 and apply gain and offset
		    // to zero the counter, set the offset to be the current
		    // counter value * -1
		    long counterValue = rainDevice.readCounter(15);
		    rain = counterValue * this.getGain() + this.getOffset();

		    // convert to inches
		    rain /= 100F;

		    // convert to centimeters if required
		    // rain *= 2.54f;

		    if (this.isDebugFlag())
		    {
			ErrorLog.logError("Rain counter: " + counterValue + 
				" gain: " + this.getGain() + 
				" offset: " + this.getOffset());
		    }
		    
		    bOK = true;
		    
		    this.getPath().close();
		}
		catch (OneWireException e)
		{
		    if (this.isDebugFlag())
		    {
			ErrorLog.logError("Error Reading Rain Counter: " + e);
		    }
		    retryCount++;
		}
	    }
	    if (retryCount >= maxRetryCount)
	    {
		ErrorLog.logError("Rain counter: Retry count exceeded");
	    }
	}
	return rain;
    }
}
