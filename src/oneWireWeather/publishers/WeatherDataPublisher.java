/* SVN FILE: $Id: WeatherDataPublisher.java 503 2020-10-04 18:35:55Z  $ */
/**
 * One Wire Weather : Weather station daemon for a 1-wire weather station
 *
 * $Author: $
 * $Revision: 503 $
 * $Date: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
 * $LastChangedBy: $
 * $LastChangedDate: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
 * $URL: http://192.168.123.7/svn/OneWireWeather/src/oneWireWeather/publishers/WeatherDataPublisher.java $
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import oneWireWeather.ConfigurationChangeListener;
import oneWireWeather.ErrorLog;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherConversions;
import oneWireWeather.WeatherStation;

public abstract class WeatherDataPublisher implements
ConfigurationChangeListener
{

	protected StationConfiguration m_configuration;
	protected WeatherStation m_station;
	private long publishInterval = 60000; // default to 1 minute
	private boolean m_bEnable = true;

	private Timer timer;
	private long updateStartMinute = -1;
	private boolean debugFlag = true;
	private String debugName;

	/**
	 * @param m_configuration
	 * @param m_station
	 */
	public WeatherDataPublisher(StationConfiguration m_configuration,
			WeatherStation m_station)
	{
		this.m_configuration = m_configuration;
		this.m_station = m_station;

		timer = new Timer();
	}

	protected abstract void publishData();

	public void startPublishing()
	{
		WeatherPublisherTask task = new WeatherPublisherTask(this);

		// set the start time based on a particular minute
		if (this.getUpdateStartMinute() >= 0
				&& this.getUpdateStartMinute() <= 59)
		{
			Calendar then = Calendar.getInstance();
			int currentMinute = then.get(Calendar.MINUTE);
			int offset = (currentMinute / 10) * 10;
			offset += this.getUpdateStartMinute();
			if (offset <= currentMinute)
			{
				offset += 10;
			}

			if (isDebugFlag())
			{
				ErrorLog.logError(this.getDebugName() + ": publish current minute = " + currentMinute
						+ " offset = " + offset);
			}

			// set the minute. If it is > 60 then the Calendar should adjust
			then.set(Calendar.MINUTE, offset);
			then.set(Calendar.SECOND, 0);
			then.set(Calendar.MILLISECOND, 0);
			Date startTime = new Date(then.getTimeInMillis());
			timer.scheduleAtFixedRate(task, startTime, this.getPublishInterval());
			if (isDebugFlag())
			{
				ErrorLog.logError(this.getDebugName() + ": starting publish at " + startTime.toString());
			}
		}
		else
		{
			timer.scheduleAtFixedRate(task, this.getPublishInterval(), this.getPublishInterval());
			if (isDebugFlag())
			{
				ErrorLog.logError(this.getDebugName() + ": starting publish in " + this.getPublishInterval() / 1000 + " seconds");
			}
		}
	}

	public void stopPublishing()
	{
		timer.cancel();
	}

	/**
	 * @return the publishInterval
	 */
	public long getPublishInterval()
	{
		return publishInterval;
	}

	/**
	 * @param publishInterval
	 *            the publishInterval to set
	 */
	public void setPublishInterval(long publishInterval)
	{
		this.publishInterval = publishInterval;
	}

	public boolean isEnabled()
	{
		return m_bEnable;
	}

	public void setEnabled(boolean enabled)
	{
		this.m_bEnable = enabled;
		ErrorLog.logError(this.debugName + ": is " + (isEnabled() ? "enabled" : "disabled"));
	}

	/**
	 * @return the updateStartMinute
	 */
	public long getUpdateStartMinute()
	{
		return updateStartMinute;
	}

	/**
	 * @param updateStartMinute
	 *            the updateStartMinute to set
	 */
	public void setUpdateStartMinute(long updateStartMinute)
	{
		this.updateStartMinute = updateStartMinute;
	}

	class WeatherPublisherTask extends TimerTask
	{
		private WeatherDataPublisher publisher;

		public WeatherPublisherTask(WeatherDataPublisher publisher)
		{
			this.publisher = publisher;
		}

		@Override
		public void run()
		{
			if (isEnabled())
			{
				publisher.publishData();
			}
		}
	}

	/**
	 * @return the debugFlag
	 */
	public boolean isDebugFlag()
	{
		return debugFlag;
	}

	/**
	 * @param debugFlag
	 *            the debugFlag to set
	 */
	public void setDebugFlag(boolean debugFlag)
	{
		this.debugFlag = debugFlag;
	}

	protected String utcDate(Date d)
	{
		Calendar localTime = Calendar.getInstance();
		localTime.setTime(d);

		Calendar utcTime = WeatherConversions.localTimeToUTC(localTime);

		int month = utcTime.get(Calendar.MONTH) + 1;
		int day = utcTime.get(Calendar.DAY_OF_MONTH);
		int hour = utcTime.get(Calendar.HOUR_OF_DAY);
		int minute = utcTime.get(Calendar.MINUTE);
		int year = utcTime.get(Calendar.YEAR);
		int second = utcTime.get(Calendar.SECOND);

		StringBuffer dateTime = new StringBuffer(year + "-");

		if (month < 10)
			dateTime.append("0");

		dateTime.append(Integer.toString(month) + "-");

		if (day < 10)
			dateTime.append("0");

		dateTime.append(Integer.toString(day) + "+");

		if (hour < 10)
			dateTime.append("0");

		dateTime.append(Integer.toString(hour) + "%3A");
		//	dateTime.append(Integer.toString(hour) + ":");

		if (minute < 10)
			dateTime.append("0");

		dateTime.append(Integer.toString(minute) + "%3A");
		if (second < 10)
		{
			dateTime.append("0");
		}
		dateTime.append(Integer.toString(second));

		return (dateTime.toString());
	}

	/**
	 * @return the debugName
	 */
	public String getDebugName()
	{
		return debugName;
	}

	/**
	 * @param debugName the debugName to set
	 */
	public void setDebugName(String debugName)
	{
		this.debugName = debugName;
	}

	protected String publishViaHTTPGet(String urlToRead) 
	{
		URL url;
		BufferedReader rd;

		String line;
		String result = "";

		if (isDebugFlag())
		{
			ErrorLog.logError(this.getDebugName() + ": sending " + urlToRead); 
		}

		try 
		{
			url = new URL(urlToRead);
			if (isDebugFlag())
			{
				ErrorLog.logError(this.getDebugName() + ": opening connection");
			}

			if (url.getProtocol().equalsIgnoreCase("https"))
			{
				if (isDebugFlag())
				{
					ErrorLog.logError(this.getDebugName() + ": using SSL");
				}
				// use SSL
				HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
				con.setRequestMethod("GET");
				con.connect();

				if (isDebugFlag())
				{
					ErrorLog.logError(this.getDebugName() + ": waiting for response");
				}

				rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

				while ((line = rd.readLine()) != null) 
				{
					result += line;
				}

				rd.close();
				if (isDebugFlag())
				{
					ErrorLog.logError(this.getDebugName() + ": Response code: " + con.getResponseCode() + " Message: " + con.getResponseMessage());
				}
				con.disconnect();
			}
			else
			{
				// don't use ssl
				if (isDebugFlag())
				{
					ErrorLog.logError(this.getDebugName() + ": not using SSL");
				}
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.connect();

				if (isDebugFlag())
				{
					ErrorLog.logError(this.getDebugName() + ": waiting for response");
				}

				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				while ((line = rd.readLine()) != null) 
				{
					result += line;
				}

				rd.close();
				if (isDebugFlag())
				{
					ErrorLog.logError(this.getDebugName() + ": Response code: " + conn.getResponseCode() + " Message: " + conn.getResponseMessage());
				}
				conn.disconnect();
			}

		} 
		catch (Exception e) 
		{
			ErrorLog.logError(this.getDebugName() + ": Exception " + e.getMessage());
		}

		return result;
	}

	protected String URLEncodeUTF8(String dataToEncode)
	{
		try
		{
			return URLEncoder.encode(dataToEncode, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			ErrorLog.logError("URL encoding exception: " + e.getMessage());
			return dataToEncode;
		}
	}
}
