/* SVN FILE: $Id: SolarSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/SolarSensor.java $
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

public class SolarSensor extends HardwareSensor
{

    private static final long serialVersionUID = 1L;
    private static final int maxRetryCount = 3;

    // class variables
    private OneWireContainer26 solarDevice = null;

    // class constants
    private final int VDD_SENSE_AD = 0;
    private final int CURRENT_SENSE_AD = 2;

    /**
     * 
     * Constructor that will instantiate the solar sensor of the specified ID on
     * the specified adapter.
     * 
     * @param adapter
     *            The 1-Wire adapter this barometer is connected to.
     * @param config
     *            The SensorConfiguration that specifies all the parameters for
     *            this solar sensor.
     * 
     */
    public SolarSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	// get instances of the 1-wire devices
	solarDevice = new OneWireContainer26(adapter, config.getID());
    }

    public float getSolarLevel()
    {
	double level = Double.MIN_VALUE;
	boolean bOK = false;
	int retryCount = 0;

	if (solarDevice != null && isEnabled())
	{
	     if (this.isDebugFlag())
	     {
		 ErrorLog.logError("Solar: Device = " + solarDevice.getName() + 
			 "  ID = " + solarDevice.getAddressAsString());
	     }
	     
	    while (!bOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();
		    
		    // get the current device state
		    byte[] state = solarDevice.readDevice();

		    // Read solar sensor's output voltage
		    solarDevice.doADConvert(CURRENT_SENSE_AD, state);
		    double Vad = solarDevice.getADVoltage(CURRENT_SENSE_AD, state);

		    // Read the solar sensor's power supply voltage
		    solarDevice.doADConvert(VDD_SENSE_AD, state);
		    // @SuppressWarnings("unused")
		    double Vdd = solarDevice.getADVoltage(VDD_SENSE_AD, state);

		    // // Convert to percentage of full scale (2.5v)
		    // // take absolute value in case the value rolls over
		    // level = Math.abs(Vad) / .25 * 100.0;
		    //
		    // // apply the calibration scale factor and offset
		    // level = (level + this.getOffset()) * this.getGain();
		    //
		    // // round to 1 decimal place
		    // level = ((int)(level * 10)) / 10.0;

		    // attempt to use equation from Internet posting on
		    // cocoontech.com forum

		    // double volts = Vad / 4096;
		    double volts = Vad;
		    if (volts < 0)
			volts = 0;
		    double current = volts / 390; // 390 ohm resistor
//		    double solarEnergy = current * 1157598;
		    double solarEnergy = current * 1730463.0;
		    level = (solarEnergy + this.getOffset()) * this.getGain();

		    bOK = true;

		    if (this.isDebugFlag())
		    {
			ErrorLog.logError("Supply Voltage = " + Vdd
				+ " Volts Sensor Output = " + Vad
				+ " Volts Gain = " + this.getGain()
				+ " Offset = " + this.getOffset()
				+ " Comp Solar = " + level);
		    }

		    this.getPath().close();
			
		}
		catch (OneWireException e)
		{
		    ErrorLog.logError("Error Reading Solar Sensor: " + e);
		    retryCount++;
		}
	    }
	    if (retryCount >= maxRetryCount)
	    {
		ErrorLog.logError("Solar: Retry count exceeded");
	    }

	}

	// handle casting a double to a float properly
	if (level != Double.MIN_VALUE)
	    return (float) level;
	else
	    return Float.MIN_VALUE;
    }
}
