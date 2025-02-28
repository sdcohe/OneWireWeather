/* SVN FILE: $Id: WeatherForYouPublisher.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/publishers/WeatherForYouPublisher.java $
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

import java.util.Date;

import oneWireWeather.ErrorLog;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherConversions;
import oneWireWeather.WeatherData;
import oneWireWeather.WeatherStation;
import oneWireWeather.WeatherStatistics;
import oneWireWeather.sensors.WindDirectionSensor;

public class WeatherForYouPublisher extends WeatherDataPublisher
{
    // class variables

    public WeatherForYouPublisher(StationConfiguration m_configuration,
	    WeatherStation m_station)
    {
	super(m_configuration, m_station);
	setConfiguration();
	this.setDebugName("WeatherForYou");
    }

    private void setConfiguration()
    {
	this.setDebugFlag(m_configuration.isWeatherForYouDebugFlag());
	this.setPublishInterval(m_configuration.getWeatherForYouUpdateInterval() * 60 * 1000);
	this.setEnabled(m_configuration.isWeatherForYouEnableFlag());
    }

//  http://www.hamweather.net/weatherstations/pwsupdate.php?
//*	ID=STATIONID&
//*	PASSWORD=password&
//*	dateutc=2000-12-01+15%3A20%3A01&
//*	winddir=225&
//*	windspeedmph=0.0&
//*	windgustmph=0.0&
//*	tempf=34.88&
//*	rainin=0.06&
//*	baromin=29.49&
//*	dewptf=30.16&
//*	humidity=83&
//*	weather=OVC&
//*	softwaretype=Example%20ver1.1&
//*	action=updateraw

    private void send(WeatherData data, WeatherStatistics stats)
    {
	if (data == null || stats == null)
	    return;

	StringBuffer sendUrl = new StringBuffer();

	Date d = data.getSampleDate();

	// build up weather for you message based on what sensors we have.
	// send a blank field if you don't have that sensor or value is invalid

	sendUrl.append("/weatherstations/pwsupdate.php?");
	sendUrl.append("ID=" + URLEncodeUTF8(m_configuration.getWeatherForYouID()));
	sendUrl.append("&PASSWORD="
		+ URLEncodeUTF8(m_configuration.getWeatherForYouPassword()));
	sendUrl.append("&dateutc=" + utcDate(d));

	// Wind Speed and Direction
	int windDirection = stats.getWindAverageDirection();
	sendUrl.append("&winddir="
		+ ((windDirection != WindDirectionSensor.WIND_DIRECTION_ERROR) ? WeatherConversions.windDirToCompass(windDirection) : ""));
	sendUrl.append("&windspeedmph=" + ((data.getAverageWindSpeed() != Float.MIN_VALUE) ? data.getAverageWindSpeed() : ""));
        sendUrl.append("&windgustmph=" + ((stats.getMaxWindGustInterval() != Float.MIN_VALUE) ? stats.getMaxWindGustInterval() : ""));
//        sendUrl.append("&windgustmph=" + ((data.getWindSpeed() != Float.MIN_VALUE) ? data.getWindSpeed() : ""));

	// Temperature
	float temperature = stats.getFiveMinuteAverageTemperatureF();
	sendUrl.append("&tempf=" + ((temperature != Float.MIN_VALUE) ? temperature : ""));

	// Rain
	float rainCount = stats.getRainCounts().getHourCount();
	sendUrl.append("&rainin=" + ((rainCount >= 0.0f) ? stats.getRainCounts().getHourCount() : ""));

	// Baro Pressure
	sendUrl.append("&baromin=" + ((data.getPressure() != Float.MIN_VALUE) ? data.getPressure() : ""));

	// Dewpoint
	float humidity = stats.getFiveMinuteAverageHumidity();
	sendUrl.append("&dewptf="
		    + (((temperature != Float.MIN_VALUE) && humidity >= 0) ? 
			    oneWireWeather.WeatherConversions.calcDewpointFahrenheit(temperature, humidity) : ""));
	
	// Humidity
	sendUrl.append("&humidity=" + ((humidity >= 0) ? humidity : ""));

	// weather condition - leave blank for now
	sendUrl.append("&weather=&clouds=");
	
	// Software Type & action
	sendUrl.append("&softwaretype=KB3HHAOneWireWeather&action=updateraw");

	if (isDebugFlag())
	{
	    ErrorLog.logError(this.getDebugName() + ": Updating Weather For You...");
	}

	String response = this.publishViaHTTPGet("http://" + m_configuration.getWeatherForYouUrl() + sendUrl.toString());

	if (isDebugFlag())
	{
	    ErrorLog.logError(this.getDebugName() + ": response - " + response);
	}
    }

    @Override
    public void notifyConfigurationChange(StationConfiguration config)
    {
	this.m_configuration = config;
	setConfiguration();
    }

    @Override
    protected void publishData()
    {
	WeatherData data = m_station.getData();
	WeatherStatistics stats = m_station.getStatistics();

	this.send(data, stats);
    }
}
