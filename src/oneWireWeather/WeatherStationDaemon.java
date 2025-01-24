/* SVN FILE: $Id: WeatherStationDaemon.java 132 2013-07-19 19:16:08Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 132 $
* $Date: 2013-07-19 15:16:08 -0400 (Fri, 19 Jul 2013) $
* $LastChangedBy: seth $
* $LastChangedDate: 2013-07-19 15:16:08 -0400 (Fri, 19 Jul 2013) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/WeatherStationDaemon.java $
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

package oneWireWeather;

import java.io.File;


public class WeatherStationDaemon
{

    static protected boolean shutdownRequested = false;
    static private Thread mainThread;

    public static void main(String[] args)
    {
	try
	{
	    daemonize();
	    addDaemonShutdownHook();

	    WeatherStation m_weatherStation;

	    String configFilePath = "";
	    if (args.length > 0)
	    {
		configFilePath = args[0];
	    }
	    m_weatherStation = new WeatherStation(configFilePath);
	    ErrorLog.logError("Weather station starting up.");
	    m_weatherStation.startWeatherStation();

	    while (!isShutdownRequested())
	    {
		try
		{
		    Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
		    // e.printStackTrace();
		}
	    }

	    ErrorLog.logError("Weather station shutting down.");
	    m_weatherStation.stopWeatherStation();
	    m_weatherStation.shutDownFileMonitor();
	}
	catch (Throwable e)
	{
	    ErrorLog.logError("Startup failed: " + e);
	}
    }

    static public void shutdown()
    {
	shutdownRequested = true;

	try
	{
	    getMainDaemonThread().join();
	}
	catch (InterruptedException e)
	{
	    ErrorLog.logError("Interrupted while waiting on main daemon thread to complete.");
	}
    }

    static public boolean isShutdownRequested()
    {
	return shutdownRequested;
    }

    static private Thread getMainDaemonThread()
    {
	return mainThread;
    }

    static public void daemonize()
    {
	mainThread = Thread.currentThread();
	getPIDFile().deleteOnExit();
	System.out.close();
	System.err.close();
    }

    static File getPIDFile()
    {
	String filePath = System.getProperty("daemon.pidfile");
	return new File(filePath);
    }

    static protected void addDaemonShutdownHook()
    {
	Runtime.getRuntime().addShutdownHook(new Thread()
	{
	    public void run()
	    {
		WeatherStationDaemon.shutdown();
	    }
	});
    }

}
