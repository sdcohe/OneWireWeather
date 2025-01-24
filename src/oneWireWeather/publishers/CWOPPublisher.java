/* SVN FILE: $Id: CWOPPublisher.java 187 2015-12-08 23:07:01Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 187 $
* $Date: 2015-12-08 18:07:01 -0500 (Tue, 08 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-08 18:07:01 -0500 (Tue, 08 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/publishers/CWOPPublisher.java $
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

package oneWireWeather.publishers;

import java.net.*;
import java.io.*;
import java.util.*;

import oneWireWeather.ErrorLog;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherConversions;
import oneWireWeather.WeatherData;
import oneWireWeather.WeatherStation;
import oneWireWeather.WeatherStatistics;
import oneWireWeather.sensors.WindDirectionSensor;

/**
 * @author seth
 *
 */

public class CWOPPublisher extends WeatherDataPublisher
{
    private BufferedReader in;
    private PrintStream out;

    /**
     * @param m_configuration
     * @param m_station
     */
    public CWOPPublisher(StationConfiguration m_configuration, WeatherStation m_station) 
    {
	super(m_configuration, m_station);
	setConfiguration();
	this.setDebugName("CWOP");
    }

    private void setConfiguration()
    {
	this.setDebugFlag(m_configuration.isCWOPDebug());
	this.setPublishInterval(m_configuration.getCWOPUpdateIntervalInMinutes() * 60 * 1000);
	this.setEnabled(m_configuration.isCWOPEnable());
	this.setUpdateStartMinute(m_configuration.getCWOPUploadMinute());
    }

    private void send(WeatherData data, WeatherStatistics stats)
    {
	if (data == null || stats== null) return;

	String aprsData = formatAPRSData(data, stats);
	sendDataToAPRS(aprsData);
    }

    /**
     * @param aprsData
     */
    private void sendDataToAPRS(String aprsData)
    {
	try
	{
	    if (isDebugFlag()) {
		ErrorLog.logError(this.getDebugName() + ": Updating...");
	    }

	    Socket s = openAPRSConnection();

	    if (s != null) {

		// send CWOP data string
		send(aprsData);
		s.close();

		if (isDebugFlag()) {
		    ErrorLog.logError(this.getDebugName() + ": Done updating");
		}
	    }
	    else
	    {
		ErrorLog.logError(this.getDebugName() + ": Unable to update - all servers busy");
	    }
	}
	catch (IOException exception)
	{
	    ErrorLog.logError(this.getDebugName() + ": Error - " + exception);
	}
    }

    /**
     * @param data
     * @param stats
     * @return
     */
    private String formatAPRSData(WeatherData data, WeatherStatistics stats)
    {
	StringBuffer sendUrl = new StringBuffer();

	Date d = data.getSampleDate();

	// build up CWOP message based on what sensors we have and what data is valid.
	sendUrl.append(m_configuration.getCWOPUserName() + ">APRS,TCPIP*:/");
	sendUrl.append(utcDate(d)+ "z");
	
	// hard code lat/long for now.  Need to make this configurable
	sendUrl.append("3909.14N/07716.08W");

	// send wind direction.  Send N if no valid value
	int windDirection = stats.getWindAverageDirection();
	if (windDirection != WindDirectionSensor.WIND_DIRECTION_ERROR)
	{
	    sendUrl.append("_" + convertWindDir(windDirection));
	}
	else
	{
	    sendUrl.append("_" + convertWindDir(0));
	}

	// send average wind speed or 0 if no value
	if (data.getAverageWindSpeed() != Float.MIN_VALUE)
	{
	    sendUrl.append("/" + padLeadingZeroes(Long.toString(Math.round(data.getAverageWindSpeed())), 3));
	}
	else
	{
	    sendUrl.append("/000");
	}

        if (stats.getMaxWindGustInterval() != Float.MIN_VALUE) {
            sendUrl.append("g" + padLeadingZeroes(Long.toString(Math.round(stats.getMaxWindGustInterval())), 3));
        } else {
            sendUrl.append("g000");
        }
//        if (data.getWindSpeed() != Float.MIN_VALUE) {
//            sendUrl.append("g" + padLeadingZeroes(Long.toString(Math.round(data.getWindSpeed())), 3));
//        } else {
//            sendUrl.append("g000");
//        }

	//        float temperature = data.getTemperature();
	float temperature = stats.getAverageTemperatureF((int)m_configuration.getCWOPUpdateIntervalInMinutes());

	if (temperature != Float.MIN_VALUE) {
	    sendUrl.append("t" + padLeadingZeroes(Long.toString(Math.round(temperature)), 3));
	} else {
	    sendUrl.append("t...");
	}

	long rainCount;
	rainCount = Math.round(stats.getRainCounts().getHourCount()*100);

	if (rainCount >= 0)
	{
	    sendUrl.append("r" + padLeadingZeroes(Long.toString(rainCount), 3));
	}

	rainCount = Math.round(stats.getRainCounts().getHourCount(24)*100);
	if (rainCount >= 0) {
	    sendUrl.append("p" + padLeadingZeroes(Long.toString(Math.round(stats.getRainCounts().getHourCount(24)*100)), 3));
	}

        //************************************************************************************
	//
	//** Note
	//
	//     to get the accumulated rain since 00:00UTC
	//     get the current time in UTC - extract the current hour (00-23)
	//     use stats.getRainCounts().getHourCount() to get the correct amount of rainfall
	//************************************************************************************
	rainCount = Math.round(stats.getRainCounts().getDayCount()*100);
	if (rainCount >= 0) {
	    sendUrl.append("P" + padLeadingZeroes(Long.toString(Math.round(stats.getRainCounts().getDayCount()*100)), 3));
	}

	//        long humidity = Math.round(data.getHumidity());
	long humidity = Math.round(stats.getAverageHumidity((int)m_configuration.getCWOPUpdateIntervalInMinutes()));

	if (humidity >= 100)
	{
	    sendUrl.append("h00");
	}
	else if (humidity >= 0)
	{
	    sendUrl.append("h" + padLeadingZeroes(Long.toString(humidity), 2));
	}

	if (data.getPressure() != Float.MIN_VALUE)
	{
	    String pressure = Integer.toString((int)WeatherConversions.inchesToMillibars(data.getPressure() * 10));
	    while (pressure.length() < 5)
	    {
		pressure = "0" + pressure;
	    }
	    sendUrl.append("b" + pressure);
	}

	if (m_configuration.isDavisEnabled())
	{
            sendUrl.append( "jDvs");
	}
	else
	{
            sendUrl.append( "j1W");
	}
//        sendUrl.append( "KB3HHA Weather Software/1-Wire");
	
	return sendUrl.toString();
    }

    private Socket openAPRSConnection()
    {
	boolean success = false;
	Socket s = null;
	InetAddress thisAddr;
	String[] cwopServerList = m_configuration.getCWOPServer().split(":");

	try 
	{
	    for (int serverX = 0; serverX < cwopServerList.length && !success; serverX++) {

		InetAddress[] addr = InetAddress.getAllByName(cwopServerList[serverX]);

		if (isDebugFlag()) {
		    ErrorLog.logError(this.getDebugName() + ": Looked up " + m_configuration.getCWOPServer() + " found " + addr.length + " entries");
		}

		for (int i = 0; i < addr.length && !success; i++) 
		{
		    thisAddr = InetAddress.getByAddress(addr[i].getAddress());

		    if (isDebugFlag()) {
			ErrorLog.logError(this.getDebugName() + ": Opening connection to " + thisAddr.getHostName());
		    }

		    try
		    {
			// open a connection to CWOP
			s = new Socket(thisAddr.getHostName(), (int)m_configuration.getCWOPPort());

			// set up buffered readers & writers to the socket
			out = new PrintStream(s.getOutputStream());
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			// wait for banner - give it 30 seconds
			String response = readResponse(30);

			if (isDebugFlag()) {
			    ErrorLog.logError(this.getDebugName() + ": response: " + response);
			}

			if (response.length() > 0) {

			    //check for port full message and try another server if needed
			    if (response.toUpperCase().indexOf("PORT FULL") == -1) {

				// send CWOP login string and ensure we logged in OK 
				send("user " + m_configuration.getCWOPUserName() + " pass " + m_configuration.getCWOPPassword() + " vers KB3HHA_Weather_Software 1.0");
				response = readResponse(30);

				if (isDebugFlag()) {
				    ErrorLog.logError(this.getDebugName() + ": response " + response);
				}

				success = true;
			    }
			    else 
			    {
				if (isDebugFlag()) {
				    ErrorLog.logError(this.getDebugName() + ": port was full on server " + thisAddr.getHostName());
				}

				try 
				{
				    s.close();
				} 
				catch (Exception ex) {}
			    }
			}
			else
			{
			    if (isDebugFlag()) {
				ErrorLog.logError(this.getDebugName() + ": No response to connect on server " + thisAddr.getHostName());
			    }
			}
		    }
		    catch (IOException e)
		    {
			if (isDebugFlag()) {
			    ErrorLog.logError(this.getDebugName() + ": exception during login to " + thisAddr.getHostName() + ": " + e.getMessage());
			}
			try 
			{
			    if (s != null)
			    {
				s.close();
			    }

			} catch(Exception ex) {}
		    }
		}
	    }

	} 
	catch (UnknownHostException ex) 
	{
	    ErrorLog.logError(this.getDebugName() + ": UnknownHost exception during name lookup: " + ex.getMessage());
	}

	return (success ? s : null);
    }

    private void send(String s) throws IOException
    {
	if (s != null)
	{
	    if (isDebugFlag()) {
		ErrorLog.logError(this.getDebugName() + ": Sending " + s);
	    }

	    out.print(s + "\r\n" );
	}
    }

    private String readResponse(int timeout) throws IOException
    {
	if (isDebugFlag()) {
	    ErrorLog.logError(this.getDebugName() + ": Getting Response...");
	}

	// wait up to timeout seconds for reply
	int i = 0;
	String line = "";
	boolean bSuccess = false;
	int waitTime = timeout * 10;

	while (i++ < waitTime && !bSuccess)
	{
	    if (in.ready())
	    {
		while (in.ready()) {
		    line += in.readLine() + "\n";
		}

		bSuccess = true;
	    }
	    else
	    {
		try
		{
		    Thread.sleep(100);
		}
		catch (InterruptedException e)
		{}
	    }
	}

	return line;
    }

    protected String utcDate(Date d)
    {
	Calendar localTime = Calendar.getInstance();
	localTime.setTime(d);

	Calendar utcTime = WeatherConversions.localTimeToUTC(localTime);

	int day = utcTime.get(Calendar.DAY_OF_MONTH);
	int hour = utcTime.get(Calendar.HOUR_OF_DAY);
	int minute = utcTime.get(Calendar.MINUTE);

	StringBuffer dateTime = new StringBuffer();

	// ddhhmmz
	if (day < 10)
	    dateTime.append("0");
	dateTime.append(Integer.toString(day));

	if (hour < 10)
	    dateTime.append("0");
	dateTime.append(Integer.toString(hour));

	if (minute < 10)
	    dateTime.append("0");
	dateTime.append(Integer.toString(minute));

	return (dateTime.toString());
    }

    private String padLeadingZeroes(String value, int fieldWidth)
    {
	StringBuffer paddedValue = new StringBuffer();
	int pad = fieldWidth - value.length();

	while (pad > 0)
	{
	    paddedValue.append("0");
	    pad--;
	}

	paddedValue.append(value);

	return paddedValue.toString();
    }

    private String convertWindDir(int windDir)
    {
	String dir = Integer.toString(WeatherConversions.windDirToCompass(windDir));
	return padLeadingZeroes(dir, 3);
    }

    @Override
    protected void publishData() {
	this.send(m_station.getData(), m_station.getStatistics());
    }

    public void notifyConfigurationChange(StationConfiguration config) {
	this.m_configuration = config;
	setConfiguration();
    }
}
