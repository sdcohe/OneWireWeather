/* SVN FILE: $Id: HumiditySensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/HumiditySensor.java $
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
 * This class represents a 1-Wire humidity sensor implemented using
 * a Dallas Semiconductor DS2438 and a Honeywell HIH4000.  This sensor 
 * contains a thermometer amd a humidity sensor that returns a voltage that 
 * corresponds to the humidity reading.
 *  
 * 
 * @author Seth Cohen
 *
 */
public class HumiditySensor extends HardwareSensor {

    private static final long serialVersionUID = 1L;
    private static final int maxRetryCount = 3;

    // class variables
    private OneWireContainer26 humidityDevice = null;

    /**
     * 
     * Constructor that will instantiate the humidity sensor of
     * the specified ID on the specified adapter.
     *  
     * @param adapter	The 1-Wire adapter this barometer is connected to. 
     * @param config	The SensorConfiguration that specifies all the parameters for
     * 					this barometer.
     * 
     */	
    public HumiditySensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	// get an instance of the 1-wire device
	humidityDevice = new OneWireContainer26(adapter, config.getID());
	humidityDevice.setSpeed(DSPortAdapter.SPEED_FLEX, true);
    }

    /**
     * Retrieve the humidity reading from the sensor.  This involves reading the humidity and 
     * the temperature to compute the relative humidity.
     *   
     * @return 	The relative humidity as a float.  If there was not a valid reading
     * 			return Float.MIN_VALUE
     */
    public float getHumidity()
    {
	float humidity = Float.MIN_VALUE;
	boolean bOK = false;
	int retryCount = 0;

	if (humidityDevice != null && isEnabled())
	{
	    if (this.isDebugFlag())
	    {
		ErrorLog.logError("Humidity: Device = " + humidityDevice.getName() + 
			" ID = " + humidityDevice.getAddressAsString());
	    }

	    while (!bOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();
		    
		    // read 1-wire device's internal temperature sensor
		    byte[] state = humidityDevice.readDevice();
		    humidityDevice.doTemperatureConvert(state);
		    double temp = humidityDevice.getTemperature(state);

		    // Read humidity sensor's output voltage
		    humidityDevice.doADConvert(OneWireContainer26.CHANNEL_VAD, state);
		    double Vad = humidityDevice.getADVoltage(OneWireContainer26.CHANNEL_VAD, state);

		    // Read the humidity sensor's power supply voltage
		    humidityDevice.doADConvert(OneWireContainer26.CHANNEL_VDD, state);
		    double Vdd = humidityDevice.getADVoltage(OneWireContainer26.CHANNEL_VDD, state);

		    // calculate humidity
		    //                    double rh = (Vad/Vdd - 0.16) / 0.0062;
		    double rh = (Vad/Vdd - (0.8/Vdd)) / 0.0062;

		    // temperature compensation
		    float humidityComp = (float)(rh / (1.0546 - 0.00216 * temp));

		    // apply calibration
		    humidity = humidityComp * this.getGain() + this.getOffset();

		    if (this.isDebugFlag())
		    {
			StringBuilder message = new StringBuilder();
			message.append("Vdd " + Vdd + " ");
			message.append("Vad " + Vad + " ");
			message.append("TempC " + temp + " ");
			message.append("RH (uncomp) " + rh + " ");
			message.append("RH (comp) " + humidityComp + " ");
			message.append("RH (cal) " + humidity);

			ErrorLog.logError(message.toString());
		    }

		    // sanity checking
		    if (humidity < 0.0f) humidity = 0.0f;
		    if (humidity > 100.0f) humidity = 100.0f;

		    bOK = true;

		    this.getPath().close();
		}
		catch (OneWireException e)
		{
		    ErrorLog.logError("Error Reading Humidity: " + e);
		    retryCount++;
		}
	    }
	    if (retryCount >= maxRetryCount)
	    {
		ErrorLog.logError("Humidity: Retry count exceeded");
	    }
	}
	return humidity;
    }
}
