/* SVN FILE: $Id: WeatherDataCollector.java 192 2016-09-29 22:22:55Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 192 $
* $Date: 2016-09-29 18:22:55 -0400 (Thu, 29 Sep 2016) $
* $LastChangedBy: seth $
* $LastChangedDate: 2016-09-29 18:22:55 -0400 (Thu, 29 Sep 2016) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/dataCollectors/WeatherDataCollector.java $
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

package oneWireWeather.dataCollectors;

import java.util.*;

import oneWireWeather.ErrorLog;

//import sensors.AAGWindDirectionSensor;
//import sensors.AAGWindSpeedSensor;
//import sensors.ADSWindDirectionSensor;
//import sensors.ADSWindSpeedSensor;
//import sensors.BarometerSensor;
//import sensors.HardwareSensor;
//import sensors.HumiditySensor;
//import sensors.LightningSensor;
//import sensors.RainSensor;
//import sensors.SolarSensor;
//import sensors.TemperatureSensor;
//import sensors.WindDirectionSensor;
//import sensors.WindSpeedSensor;

//import com.dalsemi.onewire.*;
//import com.dalsemi.onewire.adapter.*;
////import com.dalsemi.onewire.application.monitor.NetworkDeviceMonitor;
//import com.dalsemi.onewire.container.OneWireContainer;
//import com.dalsemi.onewire.container.SwitchContainer;
//import com.dalsemi.onewire.utils.OWPath;

//import oneWireWeather.ErrorLog;
//import oneWireWeather.SensorConfiguration;
//import oneWireWeather.SensorType;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherData;
import oneWireWeather.WeatherStation;

public class WeatherDataCollector implements Runnable // , DeviceMonitorEventListener
{

    private StationConfiguration m_configuration;
    private WeatherStation m_station;
    private boolean m_bQuit = false;

    private OneWireDataCollector oneWireCollector = null;
    private DavisDataCollector davisCollector = null;
    
    public WeatherDataCollector(StationConfiguration config, WeatherStation station)
    {
	m_configuration = config;
	m_station = station;

	// if 1-wire is enabled in config file
	if (config.isOneWireEnabled()) 
	{
	    ErrorLog.logError("Starting up 1-wire data collector");
	    oneWireCollector = new OneWireDataCollector(config);
	    oneWireCollector.open();
	}
	
	// if Davis is enabled in config file
	if (config.isDavisEnabled())
	{
            ErrorLog.logError("Starting up Davis data collector");
	    // configure Davis weather station
	    davisCollector = new DavisDataCollector(config);
	    davisCollector.open();
	}
    }
    
    public synchronized void setQuitFlag()
    {
	m_bQuit = true;
    }

    public void run() 
    {
	int second;
	int lastSampleSecond = -99;
	Calendar cal;
	WeatherData data;

	while (!m_bQuit)
	{
	    try
	    {
		Thread.sleep(m_configuration.getHardwarePollInterval());
	    }
	    catch (InterruptedException e) 
	    {
		if (m_bQuit)
		    break;
	    }

	    // ensure we only take measurements once a second
	    cal = Calendar.getInstance();
	    second = cal.get(Calendar.SECOND);

	    if (second != lastSampleSecond && !m_bQuit)
	    {
		lastSampleSecond = second;
		if (m_configuration.isOneWireEnabled())
		{
		    data = oneWireCollector.acquireData();
		    if (data != null) m_station.receiveMessage(data);
		}

		if (m_configuration.isDavisEnabled())
		{
		    data = davisCollector.acquireData();
		    if (data != null) m_station.receiveMessage(data);
		}
	    }
	}

	if (oneWireCollector != null)
	{
	    oneWireCollector.close();
	}
	
	if (davisCollector != null)
	{
	    davisCollector.close();
	}
    }
}
