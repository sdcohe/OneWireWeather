/* SVN FILE: $Id: BackyardWeatherPublisher.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/publishers/BackyardWeatherPublisher.java $
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

// Publish data to Weather Bug 
public class BackyardWeatherPublisher extends WeatherDataPublisher
{
    public BackyardWeatherPublisher(StationConfiguration m_configuration,
	    WeatherStation m_station)
    {
	super(m_configuration, m_station);
	setConfiguration();
	setDebugName("WeatherBug");
    }

    private void setConfiguration()
    {
	this.setDebugFlag(m_configuration.isBackyardWeatherDebugFlag());
	this.setPublishInterval(m_configuration
		.getBackyardWeatherUpdateInterval() * 60 * 1000);
	this.setEnabled(m_configuration.isBackyardWeatherEnableFlag());
    }

    private void send(WeatherData data, WeatherStatistics stats)
    {
	// don't try to send data if we have none
	if (data == null || stats == null)
	    return;

	String sendUrl = formatData(data, stats);

	if (isDebugFlag())
	{
	    ErrorLog.logError(this.getDebugName() + ": Updating...");
	}

	String response = "";

	response = publishViaHTTPGet("http://"
		+ m_configuration.getBackyardWeatherUrl() + sendUrl);

	if (isDebugFlag())
	{
	    ErrorLog.logError(this.getDebugName() + ": response " + response);
	    if (response.toLowerCase().indexOf("success", 0) > -1)
	    {
		ErrorLog.logError(this.getDebugName() + ": Success");
	    }
	    else
	    {
		ErrorLog.logError(this.getDebugName() + ": Failed");
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
	// place to concatenate the data
	StringBuffer sendUrl = new StringBuffer();

	Date d = data.getSampleDate();

	// format the data to be sent
	sendUrl.append("/data/livedata.aspx?");

	// http://data.backyard2.weatherbug.com/data/livedata.aspx?ID=P000001&Key=XXXXXX&num=xxxx&dateutc=2000-01-01+10%3A32%3A35&winddir=230&windspeedmph=12&w
	// indgustmph=12&tempf=70&tempfhi=81&tempflo=50&rainin=0&baromin=29.1&dewptf=68.2&humidity=90
	// &weather=&clouds=&softwaretype=vws%20versionxx

	sendUrl.append("Action=live");
	sendUrl.append("&ID="
		+ URLEncodeUTF8(m_configuration.getBackyardWeatherPubId()));
	sendUrl.append("&Key="
		+ URLEncodeUTF8(m_configuration.getBackyardWeatherPassword()));
	sendUrl.append("&Num="
		+ URLEncodeUTF8(m_configuration
			.getBackyardWeatherStationNumber()));
	sendUrl.append("&dateutc=" + utcDate(d));

	// wind gust dir
	sendUrl.append("&winddir=");
	if (data.getWindDirection() != WindDirectionSensor.WIND_DIRECTION_ERROR)
	{
	    sendUrl.append(WeatherConversions.windDirToCompass(data.getWindDirection()));
	}

	// Wind Speed
	sendUrl.append("&windspeedmph=");
	if (data.getAverageWindSpeed() != Float.MIN_VALUE)
	{
	    sendUrl.append(data.getAverageWindSpeed());
	}

	// wind gust
	sendUrl.append("&windgustmph=");
        if (stats.getMaxWindGustInterval() != Float.MIN_VALUE)
        {
            sendUrl.append(stats.getMaxWindGustInterval());
        }
//        if (data.getWindSpeed() != Float.MIN_VALUE)
//        {
//            sendUrl.append(data.getWindSpeed());
//        }

	// Humidity
	sendUrl.append("&humidity=");
	float humidity = stats.getFiveMinuteAverageHumidity();
	if (humidity >= 0)
	{
	    sendUrl.append(humidity);
	}

	// Temperature
	sendUrl.append("&tempf=");
	float temperature = stats.getFiveMinuteAverageTemperatureF();
	if (temperature != Float.MIN_VALUE)
	{
	    sendUrl.append(temperature);
	}

	// rain last hour
	sendUrl.append("&rainin=");
	float rainCount = stats.getRainCounts().getHourCount();
	if (rainCount >= 0.0f)
	{
	    sendUrl.append(stats.getRainCounts().getHourCount());
	}

	// daily rain
	sendUrl.append("&dailyrainin=");
	rainCount = stats.getRainCounts().getDayCount();
	if (rainCount >= 0.0f)
	{
	    sendUrl.append(rainCount);
	}

	// Baro Pressure
	sendUrl.append("&baromin=");
	if (data.getPressure() != Float.MIN_VALUE)
	{
	    sendUrl.append(data.getPressure());
	}

	// Temp HI today
	sendUrl.append("&tempfhi=");
	float tempHi = stats.getOutdoorTempStats().getDailyValues().getMaxValue();
	if (tempHi != Float.MIN_VALUE)
	{
	    sendUrl.append(tempHi);
	}

	// Temp Lo today
	sendUrl.append("&tempflo=");
	float tempLo = stats.getOutdoorTempStats().getDailyValues().getMinValue();
	if (tempLo != Float.MAX_VALUE)
	{
	    sendUrl.append(tempLo);
	}

	// rain this month
	sendUrl.append("&monthlyrainin=");
	float monthlyRain = stats.getRainCounts().getMonthCount();
	if (monthlyRain >= 0.00f)
	{
	    sendUrl.append(monthlyRain);
	}

	// rain this year
	sendUrl.append("&Yearlyrainin=");
	float yearlyRain = stats.getRainCounts().getYearCount();
	if (yearlyRain >= 0.00f)
	{
	    sendUrl.append(yearlyRain);
	}

	if ((temperature != Float.MIN_VALUE) && humidity >= 0)
	{
	    sendUrl.append("&dewptf="
		    + oneWireWeather.WeatherConversions.calcDewpointFahrenheit(
			    temperature, humidity));
	}
	
	return sendUrl.toString();
    }

    @Override
    protected void publishData()
    {
	this.send(m_station.getData(), m_station.getStatistics());
    }

    @Override
    public void notifyConfigurationChange(StationConfiguration config)
    {
	this.m_configuration = config;
	setConfiguration();
    }
}
