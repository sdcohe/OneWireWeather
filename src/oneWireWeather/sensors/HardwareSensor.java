/* SVN FILE: $Id: HardwareSensor.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/sensors/HardwareSensor.java $
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

import oneWireWeather.SensorConfiguration;

import com.dalsemi.onewire.adapter.*;

/**
 * Base class for all 1-Wire hardware sensors.
 * 
 * @author Seth Cohen
 *
 */
public abstract class HardwareSensor extends WeatherSensor 
{

    /**
     * 
     */
    private static final long serialVersionUID = -4752937071021679892L;
    private DSPortAdapter m_adapter;

    /**
     * Construct a HardwareSensor instance connected to a given adapter.
     * Specifics of this sensor are contained in a SensorConfiguration instance.
     * 
     * @param adapter   The 1-Wire adapter this sensor is connected to
     * @param config    A SensorConfiguration that contains the specifics for this sensor
     * 
     */
    public HardwareSensor(DSPortAdapter adapter, SensorConfiguration config)
    {
        super(config.getID(), config.getUsageType(), config.getGain(), config.getOffset(), 
        	config.getPollFrequency(), config.getPollOffset(), config.isDebugFlag(), config.isEnabled(), config.getPath());
        this.m_adapter = adapter;
    }
    
    /**
     * Get the 1-Wire adapter that this sensor is connected to 
     * 
     * @return  A DSPortAdapter instance that this sensor is connected to 
     */
    public DSPortAdapter getAdapter()
    {
        return m_adapter;
    }
    
//    protected void activateCoupler() throws OneWireException
//    {
//      byte[]  state;
//
//      OneWireContainer1F owc = new OneWireContainer1F(m_adapter, this.getCouplerAddress());
//      state = owc.readDevice();
//      owc.setLatchState(this.getCouplerPort(), true, false, state);
//      owc.writeDevice(state);
//    }
//
//    protected void deactivateCoupler() throws OneWireException
//    {
//      byte[]  state;
//      
//      OneWireContainer1F owc = new OneWireContainer1F(m_adapter, this.getCouplerAddress());
//      state = owc.readDevice();
//      owc.setLatchState(this.getCouplerPort(), false, false, state);
//      owc.writeDevice(state);
//    }

//  /**
//   * Set the 1-Wire adapter that this sensor is connected to
//   *  
//   * @param adapter   The DSPortAdapter this sensor is connected to
//   */
//  public void setAdapter(DSPortAdapter adapter)
//  {
//      m_adapter = adapter;
//  }
}
