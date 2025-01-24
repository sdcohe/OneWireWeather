/* SVN FILE: $Id: BarometerSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/BarometerSensor.java $
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
import com.dalsemi.onewire.container.OneWireContainer26;

/**
 * This class encapsulates a 1-Wire barometer that uses the DS2438Z 
 * smart battery monitor chip from Dallas Semiconductors along with
 * a MPXA4115A pressure sensor chip.
 * 
 * Note: This class will not work with the AAG TAI-8570 barometer.
 * 
 * @author Seth Cohen
 *
 */
public class BarometerSensor extends HardwareSensor 
{

    private static final long serialVersionUID = 1L;
    private static final int maxRetryCount = 3;

    // class variables
    private OneWireContainer26 baroDevice = null;

    /**
     * 
     * Constructor that will instantiate the barometer sensor of
     * the specified ID on the specified adapter.
     *  
     * @param adapter	The 1-Wire adapter this barometer is connected to. 
     * @param config	The SensorConfiguration that specifies all the parameters for
     * 					this barometer.
     * 
     */
    public BarometerSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	// get instances of the 1-wire devices
	baroDevice = new OneWireContainer26(adapter, config.getID());
	baroDevice.setSpeed(DSPortAdapter.SPEED_FLEX, true);
    }

    /**
     * Method to read the barometer sensor and return the pressure in
     * inches of mercury.
     * 
     * @return	The barometric pressure in inches of mercury.  If there was not
     * 			a valid reading, return Float.MIN_VALUE.
     * 
     */
    public float getPressure()
    {
	double pressure = Double.MIN_VALUE;
	boolean bOK = false;
	int retryCount = 0;

	if (baroDevice != null && isEnabled())
	{
	    if (this.isDebugFlag())
	    {
		ErrorLog.logError("Baro Pressure: Device = " + baroDevice.getName() + 
			" ID = " + baroDevice.getAddressAsString());
	    }

	    while( !bOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();
			
		    byte[] state = baroDevice.readDevice();

		    // Read pressure A to D output
		    baroDevice.doADConvert(OneWireContainer26.CHANNEL_VAD, state);
		    double Vad = baroDevice.getADVoltage(OneWireContainer26.CHANNEL_VAD, state);

		    // Read Supply Voltage (for reference only)
		    baroDevice.doADConvert(OneWireContainer26.CHANNEL_VDD, state);
		    //					@SuppressWarnings("unused")
		    double Vdd = baroDevice.getADVoltage(OneWireContainer26.CHANNEL_VDD, state);

		    // apply calibration
		    pressure = Vad * this.getGain() + this.getOffset();

		    // scale to mb if required
		    //pressure *= 33.8640;

		    if (this.isDebugFlag())
		    {
			ErrorLog.logError("Sensor Output = " + Vad + " Volts Supply Voltage = " + 
				Vdd + " Volts Scale Factor = " + this.getGain() + "Offset = " + this.getOffset() + 
				"Baro Pressure = " + pressure);
		    }

		    bOK = true;

		    this.getPath().close();
		    
		}
		catch (OneWireException e)
		{
		    ErrorLog.logError("Error Reading Baro Sensor: " + e);
		    retryCount++;
		}
	    }
	    if (retryCount >= maxRetryCount)
	    {
		ErrorLog.logError("Barometer: Retry count exceeded");
	    }
	}

	// handle casting a double to a float properly
	if (pressure != Double.MIN_VALUE)
	    return (float)pressure;
	else
	    return Float.MIN_VALUE;
    }
}
