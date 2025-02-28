/* SVN FILE: $Id: ADSWindDirectionSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/ADSWindDirectionSensor.java $
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
 * @author seth
 *
 */
public class ADSWindDirectionSensor extends HardwareSensor  implements WindDirectionSensor
{
    private static final long serialVersionUID = 1L;

    private static final int maxRetryCount = 3;
//    private static final double WIND_DIR_ADJUST = 1.009;
//    private static final double WIND_DIR_ADJUST = 1.000;
    private OneWireContainer26 windDirectionDevice = null;

    public ADSWindDirectionSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	windDirectionDevice = new OneWireContainer26(adapter, config.getID());
	windDirectionDevice.setSpeed(DSPortAdapter.SPEED_FLEX, true);
    }
    
    public int getWindDirection()
    {
	int windDir = WIND_DIRECTION_ERROR;
	boolean bOK = false;
	int retryCount = 0;

	if (windDirectionDevice != null && isEnabled())
	{
	    if (this.isDebugFlag())
	    {
		ErrorLog.logError("ADS Wind Dir: Device = " + 
			windDirectionDevice.getName() + "  ID = " + 
			windDirectionDevice.getAddressAsString());
	    }

	    while(!bOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();
		    
		    // read 1-wire device's state
		    byte[] state = windDirectionDevice.readDevice();
		          
		    // Read sensor's output voltage
		    windDirectionDevice.doADConvert(OneWireContainer26.CHANNEL_VAD, state);
		    double Vad = windDirectionDevice.getADVoltage(OneWireContainer26.CHANNEL_VAD, state);
		          
		    // Read the sensor's power supply voltage - mostly for debugging
		    windDirectionDevice.doADConvert(OneWireContainer26.CHANNEL_VDD, state);
		    double Vdd = windDirectionDevice.getADVoltage(OneWireContainer26.CHANNEL_VDD, state);
		          
		    // compensate for supply variations
		    Vad = Vad * this.getGain() + this.getOffset();
		        
		    // convert the A to D voltage to a wind direction
		    windDir = lookupWindDir(Vad);
		        
		    if (windDir == WIND_DIRECTION_ERROR)
		    {
			if (isDebugFlag())
			{
			    ErrorLog.logError("Wind dir: ERROR - Vad: " + Vad + " Vdd: " + Vdd);
			}

			retryCount++;
		    }
		    else
		    {
			bOK = true;  
			if (isDebugFlag())
			{
			    ErrorLog.logError("Wind dir: Vad: " + Vad + " Vdd: " + Vdd + " Dir: " + windDir);
			}
		    }

		    this.getPath().close();
		}
		catch (OneWireException e)
		{
		    if (this.isDebugFlag())
		    {
			ErrorLog.logError("Error Reading Wind Direction: " + e);
		    }
		    retryCount++;
		}
	    }
	    if (retryCount >= maxRetryCount)
	    {
		ErrorLog.logError("Wind Direction: Retry count exceeded");
	    }
	}

	return windDir;
    }


    // convert wind direction A to D results to direction value
    private int lookupWindDir(double volts) // ADS
    {
      int direction = WIND_DIRECTION_ERROR;
      
      for (int i = 0; i < 16; i++)
      {
	  if((volts <= lookupTable[i] + 0.04) && (volts >= lookupTable[i] - 0.04)) 
	  {
	      direction = i;
	      break;
	  }
      }
      
      return direction;
      
    }

    static final double lookupTable[] = 
    {
	2.69, // N
	6.52, // NNE 1
	5.99, // NE  2
	9.38, // ENE 3
	9.30, // E   4
	9.53, // ESE 5
	8.51, // SE  6
	9.01, // SSE 7
	7.60, // S   8
	7.98, // SSW 9
	4.31, // SW  10
	4.62, // WSW 11
	0.92, // W   12
	2.23, // WNW 13
	1.57, // NW  14
	3.57, // NNW 15
    };

}
