/* SVN FILE: $Id: AAGWindDirectionSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/AAGWindDirectionSensor.java $
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

public class AAGWindDirectionSensor extends HardwareSensor implements WindDirectionSensor
{

    private static final long serialVersionUID = 1L;
    private static final int maxRetryCount = 3;
    private OneWireContainer20 windDirectionDevice = null;

    public AAGWindDirectionSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
	super(adapter, config);

	windDirectionDevice = new OneWireContainer20(adapter, config.getID());
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
		ErrorLog.logError("Wind Dir: Device = " + windDirectionDevice.getName() + "  ID = " + windDirectionDevice.getAddressAsString());
	    }

	    while(!bOK && retryCount < maxRetryCount)
	    {
		try
		{
		    this.getPath().open();
		    
		    // set up A to D for 8 bit readings, 5.12 V full-scale
		    byte[] state = windDirectionDevice.readDevice();

		    windDirectionDevice.setADResolution(OneWireContainer20.CHANNELA, 8, state);
		    windDirectionDevice.setADResolution(OneWireContainer20.CHANNELB, 8, state);
		    windDirectionDevice.setADResolution(OneWireContainer20.CHANNELC, 8, state);
		    windDirectionDevice.setADResolution(OneWireContainer20.CHANNELD, 8, state);

		    windDirectionDevice.setADRange(OneWireContainer20.CHANNELA, 5.12, state);
		    windDirectionDevice.setADRange(OneWireContainer20.CHANNELB, 5.12, state);
		    windDirectionDevice.setADRange(OneWireContainer20.CHANNELC, 5.12, state);
		    windDirectionDevice.setADRange(OneWireContainer20.CHANNELD, 5.12, state);
		    windDirectionDevice.writeDevice(state);

		    // command each channel to read voltage
		    windDirectionDevice.doADConvert(OneWireContainer20.CHANNELA, state);
		    windDirectionDevice.doADConvert(OneWireContainer20.CHANNELB, state);
		    windDirectionDevice.doADConvert(OneWireContainer20.CHANNELC, state);
		    windDirectionDevice.doADConvert(OneWireContainer20.CHANNELD, state);

		    // read results
		    float chAVolts = (float)windDirectionDevice.getADVoltage(OneWireContainer20.CHANNELA, state);
		    float chBVolts = (float)windDirectionDevice.getADVoltage(OneWireContainer20.CHANNELB, state);
		    float chCVolts = (float)windDirectionDevice.getADVoltage(OneWireContainer20.CHANNELC, state);
		    float chDVolts = (float)windDirectionDevice.getADVoltage(OneWireContainer20.CHANNELD, state);

		    // convert the 4 A to D voltages to a wind direction
		    windDir = lookupWindDir(chAVolts, chBVolts, chCVolts, chDVolts);

		    if (windDir == WIND_DIRECTION_ERROR)
		    {
			if (isDebugFlag())
			{
			    ErrorLog.logError("Wind dir: ERROR - Ch A: " + chAVolts + " Ch B; " + chBVolts + " Ch C: " + chCVolts + " Ch D: " + chDVolts);
			}

			retryCount++;
		    }
		    else
		    {
			bOK = true;  
			if (isDebugFlag())
			{
			    ErrorLog.logError("Wind dir: " + windDir + " - Ch A: " + chAVolts + " Ch B; " + chBVolts + " Ch C: " + chCVolts + " Ch D: " + chDVolts);
			}
		    }

		    this.getPath().close();
		}
		catch (OneWireException e)
		{
		    ErrorLog.logError("Error Reading Wind Direction: " + e);
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
    private int lookupWindDir(float a, float b, float c, float d)
    {
	int i;
	int direction = WIND_DIRECTION_ERROR;


	for (i=0; i<16; i++)
	{
	    if(((a <= lookupTable[i][0] +1.0) && (a >= lookupTable[i][0] -1.0)) &&
		    ((b <= lookupTable[i][1] +1.0) && (b >= lookupTable[i][1] -1.0)) &&
		    ((c <= lookupTable[i][2] +1.0) && (c >= lookupTable[i][2] -1.0)) &&
		    ((d <= lookupTable[i][3] +1.0) && (d >= lookupTable[i][3] -1.0)) )
	    {
		direction = i;
		break;
	    }
	}
	return direction;
    }

    static final float lookupTable[][] = {
	{4.5F, 4.5F, 2.5F, 4.5F}, // N   0
	{4.5F, 2.5F, 2.5F, 4.5F}, // NNE 1
	{4.5F, 2.5F, 4.5F, 4.5F}, // NE  2
	{2.5F, 2.5F, 4.5F, 4.5F}, // ENE 3
	{2.5F, 4.5F, 4.5F, 4.5F}, // E   4
	{2.5F, 4.5F, 4.5F, 0.0F}, // ESE 5
	{4.5F, 4.5F, 4.5F, 0.0F}, // SE  6
	{4.5F, 4.5F, 0.0F, 0.0F}, // SSE 7
	{4.5F, 4.5F, 0.0F, 4.5F}, // S   8
	{4.5F, 0.0F, 0.0F, 4.5F}, // SSW 9
	{4.5F, 0.0F, 4.5F, 4.5F}, // SW  10
	{0.0F, 0.0F, 4.5F, 4.5F}, // WSW 11
	{0.0F, 4.5F, 4.5F, 4.5F}, // W   12
	{0.0F, 4.5F, 4.5F, 2.5F}, // WNW 13
	{4.5F, 4.5F, 4.5F, 2.5F}, // NW  14
	{4.5F, 4.5F, 2.5F, 2.5F}, // NNW 15
    };

    static final int channels[] = {
	OneWireContainer20.CHANNELA,
	OneWireContainer20.CHANNELB,
	OneWireContainer20.CHANNELC,
	OneWireContainer20.CHANNELD
    };
}
