/* SVN FILE: $Id: WeatherServer.java 134 2013-07-19 19:32:22Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 134 $
* $Date: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $LastChangedBy: seth $
* $LastChangedDate: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/WeatherServer.java $
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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class WeatherServer implements Runnable
{

    private WeatherStation m_station;
    private boolean m_bQuit = false;
    private HashMap<WeatherServerHandler, Thread> handlerList = new HashMap<WeatherServerHandler, Thread>();

    public WeatherServer(WeatherStation station)
    {
	m_station = station;
    }

    public void run()
    {
	// wait for conections on port XXXX - read from config file
	// spawn off a new thread to handle the connection and start it up
	ErrorLog.logError("Starting TCP/IP listener on port " + m_station.getStationConfiguration().getServerPort());
	
	try
	{
	    ServerSocket sock = new ServerSocket(m_station.getStationConfiguration().getServerPort());
	    sock.setSoTimeout(5000);

	    m_bQuit = false;

	    while (!m_bQuit)
	    {
		try
		{
		    Socket incoming = sock.accept();
		    WeatherServerHandler handler = new WeatherServerHandler(incoming, this, m_station);
		    Thread thread = new Thread(handler);
		    handlerList.put(handler, thread);
		    thread.start();
		}
		catch (SocketTimeoutException ex)
		{
		    // OK to ignore this
		    continue;
		}
	    }

	    // clean up all children threads
	    Set<WeatherServerHandler> keys = handlerList.keySet();

	    for (Iterator<WeatherServerHandler> iterator = keys.iterator(); iterator.hasNext();)
	    {

		WeatherServerHandler weatherServerHandler = (WeatherServerHandler) iterator.next();
		Thread thread = handlerList.get(weatherServerHandler);
		weatherServerHandler.setQuitFlag();
		// wait for thread to exit
		try
		{
		    thread.join(5000);
		}
		catch (InterruptedException ex)
		{
		    continue;
		}

	    }
	    handlerList.clear();

	    sock.close();
	}
	catch (Exception e)
	{
	    // must be time to quit???
	    // e.printStackTrace();
	}
    }

    public synchronized void setQuitFlag()
    {
	m_bQuit = true;
    }

    public synchronized void removeHandler(WeatherServerHandler handler)
    {
	handlerList.remove(handler);
    }
}
