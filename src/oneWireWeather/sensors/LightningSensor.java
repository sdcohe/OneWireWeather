/* SVN FILE: $Id: LightningSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/LightningSensor.java $
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

public class LightningSensor extends HardwareSensor 
{
    private static final long serialVersionUID = 1L;
    private static final int maxRetryCount = 3;

    // class variables
//    private long lastCount = 0;
//    private long lastTicks = 0;
    private OneWireContainer1D lightningDevice = null;

    // class constants
//    public static final long TICKS_PER_SECOND = 1000L;

    public LightningSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
        super(adapter, config);

        // get instances of the 1-wire devices
        lightningDevice = new OneWireContainer1D(adapter, config.getID());
        lightningDevice.setSpeed(DSPortAdapter.SPEED_FLEX, true);

    }

    public int getLightningCount()
    {
        int lightning = Integer.MIN_VALUE;
        boolean bOK = false;
        int retryCount = 0;

        if (lightningDevice != null && isEnabled())
        {
	    if (this.isDebugFlag())
	    {
		ErrorLog.logError("Lightning: Device = " + lightningDevice.getName() + 
			" ID = " + lightningDevice.getAddressAsString());
	    }

            while(!bOK && retryCount < maxRetryCount)
            {
                try
                {
                    this.getPath().open();
			
                    // read lightning count & time
                    long currentCount = lightningDevice.readCounter(15);
//                    long currentTicks = System.currentTimeMillis();

//                    if (lastTicks != 0)
//                    {
                        // calculate the lightning activity in strikes per minute since last time
//                        lightning = (int)((currentCount-lastCount)*(60L/((currentTicks-lastTicks)/ TICKS_PER_SECOND)));
                        // is this really needed?
//                    lightning = (int)(lightning * this.getGain() + this.getOffset());
                    lightning = (int)(currentCount * this.getGain() + this.getOffset());
//                    }

//                  if (debugFlag)
//                  System.out.println("Count = " + (currentCount-lastCount) + " during " +
//                  (currentTicks-lastTicks) + "ms calcs to " + lightning + " SPM\n");

                    // save the counts & time
//                    lastCount = currentCount;
//                    lastTicks = currentTicks;

                    bOK = true;

                    this.getPath().close();
			
                }
                catch (OneWireException e)
                {
                    ErrorLog.logError("Error Reading Lightning Counter: " + e);
                    retryCount++;
                }
            }    
            if (retryCount >= maxRetryCount)
            {
                ErrorLog.logError("Lightning counter: Retry count exceeded");
            }
        }

        return lightning;
    }
}