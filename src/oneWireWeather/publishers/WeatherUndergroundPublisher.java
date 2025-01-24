/* SVN FILE: $Id: WeatherUndergroundPublisher.java 503 2020-10-04 18:35:55Z  $ */
/**
 * One Wire Weather : Weather station daemon for a 1-wire weather station
 *
 * $Author: $
 * $Revision: 503 $
 * $Date: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
 * $LastChangedBy: $
 * $LastChangedDate: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
 * $URL: http://192.168.123.7/svn/OneWireWeather/src/oneWireWeather/publishers/WeatherUndergroundPublisher.java $
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

/******************************************************************************
 Project Name: oneWireWeather
 File name:    WeatherUnderground.java
 Version:      1.0 12/06/2007

 This class provides the interface to the Weather Underground.
 *****************************************************************************/

import java.util.*;

import oneWireWeather.ErrorLog;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherConversions;
import oneWireWeather.WeatherData;
import oneWireWeather.WeatherStation;
import oneWireWeather.WeatherStatistics;
import oneWireWeather.sensors.WindDirectionSensor;

public class WeatherUndergroundPublisher extends WeatherDataPublisher
{
	// user constants

	// class variables

	public WeatherUndergroundPublisher(StationConfiguration m_configuration,
			WeatherStation m_station)
	{
		super(m_configuration, m_station);
		setConfiguration();
		this.setDebugName("WeatherUnderground");
	}

	private void setConfiguration()
	{
		this.setDebugFlag(m_configuration.isWeatherUndergroundDebug());
		this.setPublishInterval(m_configuration.getWeatherUndergroundUpdateInterval());
		this.setEnabled(m_configuration.isWeatherUndergroundEnable());
	}

	private void send(WeatherData data, WeatherStatistics stats)
	{
		if (data == null || stats == null)
			return;

		String sendUrl = formatData(data, stats);

		if (isDebugFlag())
		{
			ErrorLog.logError("WeatherUnderground: Updating..." + m_configuration.getWeatherUndergroundUrl());
		}

		String response = "";

//		response = publishViaHTTPGet("http://"
//				+ m_configuration.getWeatherUndergroundUrl()
//				+ sendUrl);
		response = publishViaHTTPGet(m_configuration.getWeatherUndergroundUrl()
				+ sendUrl);

		if (isDebugFlag())
		{
			ErrorLog.logError("WeatherUnderground: response is " + response);
		}

		if (isDebugFlag())
		{
			if (response.toLowerCase().indexOf("success", 0) > -1)
			{
				ErrorLog.logError("WeatherUnderground: Success");
			}
			else
			{
				ErrorLog.logError("WeatherUnderground: Failed");
			}
		}
	}

	/**
	 * @param data
	 * @param stats
	 * @return
	 */
	private String formatData(WeatherData data, WeatherStatistics stats)
	{
		StringBuffer sendUrl = new StringBuffer();

		Date d = data.getSampleDate();

		// build up wunderground message based on what sensors we have.
		// comment out the lines if you don't have that sensor
		sendUrl.append("/weatherstation/updateweatherstation.php?");
		sendUrl.append("ID="
				+ URLEncodeUTF8(m_configuration.getWeatherUndergroundUserName()));
		sendUrl.append("&PASSWORD="
				+ URLEncodeUTF8(m_configuration.getWeatherUndergroundPassword()));
		sendUrl.append("&dateutc=" + utcDate(d));

		// Temperature
		float temperature = stats.getFiveMinuteAverageTemperatureF();
		if (temperature != Float.MIN_VALUE) sendUrl.append("&tempf=" + temperature);

		// Humidity
		float humidity = stats.getFiveMinuteAverageHumidity();
		if (humidity >= 0) sendUrl.append("&humidity=" + humidity);

		// Dewpoint
		if ((temperature != Float.MIN_VALUE) && humidity >= 0)
		{
			sendUrl.append("&dewptf="
					+ oneWireWeather.WeatherConversions.calcDewpointFahrenheit(
							temperature, humidity));
		}

		// Wind Speed and Direction
		if (data.getAverageWindSpeed() != Float.MIN_VALUE) 
			sendUrl.append("&windspeedmph=" + data.getAverageWindSpeed());

		if (stats.getMaxWindGustInterval() != Float.MIN_VALUE)
			sendUrl.append("&windgustmph=" + stats.getMaxWindGustInterval());
		//        if (data.getWindSpeed() != Float.MIN_VALUE)
		//            sendUrl.append("&windgustmph=" + data.getWindSpeed());

		// need wind gust dir
		if (data.getWindDirection() != WindDirectionSensor.WIND_DIRECTION_ERROR)
		{
			sendUrl.append("&windgustdir="
					+ WeatherConversions.windDirToCompass(data
							.getWindDirection()));
		}

		int windDirection = stats.getWindAverageDirection();
		if (windDirection != WindDirectionSensor.WIND_DIRECTION_ERROR)
		{
			sendUrl.append("&winddir="
					+ WeatherConversions.windDirToCompass(windDirection));
		}

		// Baro Pressure
		if (data.getPressure() != Float.MIN_VALUE) sendUrl.append("&baromin=" + data.getPressure());

		// Rain
		float rainCount = stats.getRainCounts().getHourCount();
		if (rainCount >= 0.0f) sendUrl.append("&rainin=" + stats.getRainCounts().getHourCount());

		rainCount = stats.getRainCounts().getDayCount();
		if (rainCount >= 0.0f) sendUrl.append("&dailyrainin=" + stats.getRainCounts().getDayCount());

		// Software Type & action
		// sendUrl.append("&softwaretype=OneWireWeather&action=updateraw&realtime=1&rtfreq="
		// + this.getPublishInterval()
		// / 1000
		// + " HTTP/1.1\r\nConnection: keep-alive\r\n\r\n");

		sendUrl.append("&softwaretype=OneWireWeather&action=updateraw&realtime=1&rtfreq="
				+ this.getPublishInterval() / 1000);

		return sendUrl.toString();
	}

	@Override
	protected void publishData()
	{
		WeatherData data = m_station.getData();
		WeatherStatistics stats = m_station.getStatistics();

		this.send(data, stats);
	}

	public void notifyConfigurationChange(StationConfiguration config)
	{
		this.m_configuration = config;
		setConfiguration();
	}
}