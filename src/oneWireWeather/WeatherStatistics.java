/* SVN FILE: $Id: WeatherStatistics.java 277 2018-01-07 15:28:17Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 277 $
* $Date: 2018-01-07 10:28:17 -0500 (Sun, 07 Jan 2018) $
* $LastChangedBy: seth $
* $LastChangedDate: 2018-01-07 10:28:17 -0500 (Sun, 07 Jan 2018) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/WeatherStatistics.java $
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import oneWireWeather.sensors.WindDirectionSensor;

import java.util.Calendar;
import java.io.*;
import java.util.*;

/**
 * Compute and store weather statistics based on the collected weather data. In
 * this case, statistics means mostly min/max information. Complex calculations
 * need to be done by the client application, as there is limited math
 * functionality on the TINI, which is the target architecture.
 * 
 * Note: Temporarily running on a low-end computer running Linux.
 * 
 * @author Seth Cohen
 * 
 */
public class WeatherStatistics implements Serializable, WeatherDataListener,
	ConfigurationChangeListener
{

    private enum TrendTypes
    {
	OUTDOOR_TEMPERATURE, OUTDOOR_HUMIDITY, PRESSURE, INDOOR_TEMPERATURE, INDOOR_HUMIDITY, SOLAR
    }

    // don't need to persist
    private static final long serialVersionUID = 1L;
    private static final String statisticsFileName = "stats.xml";
    private static final int MINUTE_SAMPLE_MAX_SIZE = 180;
//    private static final String DATE_TIME_FORMAT = "MM/dd/yy HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static final String STATISTICS_ELEMENT = "<WEATHERSTATISTICS>";
    private static final String STATISTICS_END_ELEMENT = "</WEATHERSTATISTICS>";
    private static final String OUTDOOR_TEMP_ELEMENT = "<OUTDOORTEMPSTATS>";
    private static final String OUTDOOR_TEMP_END_ELEMENT = "</OUTDOORTEMPSTATS>";
    private static final String OUTDOOR_HUMIDITY_ELEMENT = "<OUTDOORHUMIDITYSTATS>";
    private static final String OUTDOOR_HUMIDITY_END_ELEMENT = "</OUTDOORHUMIDITYSTATS>";
    private static final String WIND_GUST_ELEMENT = "<WINDGUSTSTATS>";
    private static final String WIND_GUST_END_ELEMENT = "</WINDGUSTSTATS>";
    private static final String WIND_SUSTAINED_STAT_START_ELEMENT = "<WINDSUSTAINEDSTATS>";
    private static final String WIND_SUSTAINED_STAT_END_ELEMENT = "</WINDSUSTAINEDSTATS>";
    private static final String AVERAGE_WIND_DIRECTION_START_ELEMENT = "<AVERAGEWINDDIRECTION>";
    private static final String AVERAGE_WIND_DIRECTION_END_ELEMENT = "</AVERAGEWINDDIRECTION>";
    private static final String WIND_CHILL_START_ELEMENT = "<WINDCHILL>";
    private static final String WIND_CHILL_END_ELEMENT = "</WINDCHILL>";
    private static final String WIND_SUSTAINED_START_ELEMENT = "<WINDSUSTAINED>";
    private static final String WIND_SUSTAINED_END_ELEMENT = "</WINDSUSTAINED>";
    private static final String OUTDOOR_TEMP_TREND_START_ELEMENT = "<OUTDOORTEMPTREND>";
    private static final String OUTDOOR_TEMP_TREND_END_ELEMENT = "</OUTDOORTEMPTREND>";
    private static final String OUTDOOR_HUMIDITY_TREND_START_ELEMENT = "<OUTDOORHUMIDITYTREND>";
    private static final String OUTDOOR_HUMIDITY_TREND_END_ELEMENT = "</OUTDOORHUMIDITYTREND>";
    private static final String PRESSURE_TREND_START_ELEMENT = "<PRESSURETREND>";
    private static final String PRESSURE_TREND_END_ELEMENT = "</PRESSURETREND>";
    private static final String HEAT_INDEX_STATS_ELEMENT = "<HEATINDEXSTATS>";
    private static final String HEAT_INDEX_STATS_END_ELEMENT = "</HEATINDEXSTATS>";
    private static final String WIND_CHILL_STATS_ELEMENT = "<WINDCHILLSTATS>";
    private static final String WIND_CHILL_STATS_END_ELEMENT = "</WINDCHILLSTATS>";
    private static final String DEW_POINT_STATS_ELEMENT = "<DEWPOINTSTATS>";
    private static final String DEW_POINT_STATS_END_ELEMENT = "</DEWPOINTSTATS>";
    private static final String INDOOR_TEMP_ELEMENT = "<INDOORTEMPSTATS>";
    private static final String INDOOR_TEMP_END_ELEMENT = "</INDOORTEMPSTATS>";
    private static final String INDOOR_HUMIDITY_ELEMENT = "<INDOORHUMIDITYSTATS>";
    private static final String INDOOR_HUMIDITY_END_ELEMENT = "</INDOORHUMIDITYSTATS>";
    private static final String PRESSURE_ELEMENT = "<PRESSURESTATS>";
    private static final String PRESSURE_END_ELEMENT = "</PRESSURESTATS>";
    private static final String SOLAR_ELEMENT = "<SOLARSTATS>";
    private static final String SOLAR_END_ELEMENT = "</SOLARSTATS>";
    private static final String RAIN_STATS_START_ELEMENT = "<RAINCOUNTS>";
    private static final String RAIN_STATS_END_ELEMENT = "</RAINCOUNTS>";
    private static final String LIGHTNING_STATS_START_ELEMENT = "<LIGHTNINGCOUNTS>";
    private static final String LIGHTNING_STATS_END_ELEMENT = "</LIGHTNINGCOUNTS>";
    private static final String TEMP_GT_86_ELEMENT = "<TEMPGT86>";
    private static final String TEMP_GT_86_END_ELEMENT = "</TEMPGT86>";
    private static final String TEMP_GT_77_ELEMENT = "<TEMPGT77>";
    private static final String TEMP_GT_77_END_ELEMENT = "</TEMPGT77>";
    private static final String TEMP_LT_32_ELEMENT = "<TEMPLT32>";
    private static final String TEMP_LT_32_END_ELEMENT = "</TEMPLT32>";
    private static final String TEMP_LT_5_ELEMENT = "<TEMPLT5>";
    private static final String TEMP_LT_5_END_ELEMENT = "</TEMPLT5>";
    private static final String WIND_RUN_COUNTS_START_ELEMENT = "<WINDRUNCOUNTS>";
    private static final String WIND_RUN_COUNTS_END_ELEMENT = "</WINDRUNCOUNTS>";
    private static final String WIND_MAX_AVERAGE_DIRECTION_START_ELEMENT = "<MAXAVERAGEWINDDIRECTION>";
    private static final String WIND_MAX_AVERAGE_DIRECTION_END_ELEMENT = "</MAXAVERAGEWINDDIRECTION>";
    private static final String WIND_MAX_GUST_DIRECTION_START_ELEMENT = "<MAXGUSTWINDDIRECTION>";
    private static final String WIND_MAX_GUST_DIRECTION_END_ELEMENT = "</MAXGUSTWINDDIRECTION>";
    private static final String MIN_MAX_DAY_START_ELEMENT = "<MINMAXDAY>";
    private static final String MIN_MAX_DAY_END_ELEMENT = "</MINMAXDAY>";
    private static final String MIN_MAX_NIGHT_START_ELEMENT = "<MINMAXNIGHT>";
    private static final String MIN_MAX_NIGHT_END_ELEMENT = "</MINMAXNIGHT>";
    private static final String WIND_RUN_START_ELEMENT = "<WINDRUN>";
    private static final String WIND_RUN_END_ELEMENT = "</WINDRUN>";
    private static final String SOLAR_TREND_START_ELEMENT = "<SOLARTREND>";
    private static final String SOLAR_TREND_END_ELEMENT = "</SOLARTREND>";
    private static final String INDOOR_TEMP_TREND_START_ELEMENT = "<INDOORTEMPTREND>";
    private static final String INDOOR_TEMP_TREND_END_ELEMENT = "</INDOORTEMPTREND>";
    private static final String INDOOR_HUMIDITY_TREND_START_ELEMENT = "<INDOORHUMIDITYTREND>";
    private static final String INDOOR_HUMIDITY_TREND_END_ELEMENT = "</INDOORHUMIDITYTREND>";
    private static final String STATS_START_DATE_START_ELEMENT = "<STATSSTARTDATE>";
    private static final String STATS_START_DATE_END_ELEMENT = "</STATSSTARTDATE>";
    private static final String LAST_START_DATE_START_ELEMENT = "<LASTSTARTDATE>";
    private static final String LAST_START_DATE_END_ELEMENT = "</LASTSTARTDATE>";
    private static final String TEMP_GT_86_DATE_ELEMENT = "<TEMPGT86DATE>";
    private static final String TEMP_GT_86_DATE_END_ELEMENT = "</TEMPGT86DATE>";
    private static final String TEMP_GT_77_DATE_ELEMENT = "<TEMPGT77DATE>";
    private static final String TEMP_GT_77_DATE_END_ELEMENT = "</TEMPGT77DATE>";
    private static final String TEMP_LT_32_DATE_ELEMENT = "<TEMPLT32DATE>";
    private static final String TEMP_LT_32_DATE_END_ELEMENT = "</TEMPLT32DATE>";
    private static final String TEMP_LT_5_DATE_ELEMENT = "<TEMPLT5DATE>";
    private static final String TEMP_LT_5_DATE_END_ELEMENT = "</TEMPLT5DATE>";
    private static final String LAST_SAMPLE_DATE_START_ELEMENT = "<LASTSAMPLEDATE>";
    private static final String LAST_SAMPLE_DATE_END_ELEMENT = "</LASTSAMPLEDATE>";
    private static final String MAX_WIND_GUST_INTERVAL_START_ELEMENT = "<MAXGUSTINTERVAL>";
    private static final String MAX_WIND_GUST_INTERVAL_END_ELEMENT = "</MAXGUSTINTERVAL>";

    private File m_statsFile;
    private Calendar m_nextWindDirectionSample;
    private Calendar m_windSpeedSampleExpiration;
    private LinkedList<WeatherData> m_windSpeedSamples = new LinkedList<WeatherData>();
    private long[] m_windDirectionCounts = new long[20];
    private int m_windAverageDirection = 0;
    private float m_windChillTemperature = 0.0f;
    private float m_sustainedWindSpeed = 0.0f;

    private ArrayList<WeatherData> m_MinuteSamples = new ArrayList<WeatherData>();
    private int lastMinute = -1;
    private float m_temperatureTrend = 0.0f; //
    private float m_pressureTrend = 0.0f; //
    private float m_humidityTrend = 0.0f; //
    private float m_solarTrend = 0.0f; //
    private float m_indoorTemperatureTrend = 0.0f;
    private float m_indoorHumidityTrend = 0.0f;

    private StationConfiguration m_config;
//    private StationLog m_log;

    // stats to persist
    private CumulativeValues m_outdoorTempCumValues = new CumulativeValues();
    private CumulativeValues m_outdoorHumidityCumValues = new CumulativeValues();
    private CumulativeValues m_windGustCumValues = new CumulativeValues();
    private CumulativeValues m_windSustainedCumValues = new CumulativeValues();
    private CumulativeValues m_indoorTempCumValues = new CumulativeValues();
    private CumulativeValues m_indoorHumidityCumValues = new CumulativeValues();
    private CumulativeValues m_pressureCumValues = new CumulativeValues();
    private CumulativeValues m_solarCumValues = new CumulativeValues();
    private CumulativeValues m_heatIndexCumValues = new CumulativeValues();
    private CumulativeValues m_windChillCumValues = new CumulativeValues();
    private CumulativeValues m_dewPointCumValues = new CumulativeValues();
    private CumulativeValues m_minMaxDayTemp = new CumulativeValues();
    private CumulativeValues m_minMaxNightTemp = new CumulativeValues();

    private CumulativeCounts m_rainCounts = new CumulativeCounts();
    private CumulativeCounts m_lightningCounts = new CumulativeCounts();
    private CumulativeCounts m_windRunCounts = new CumulativeCounts();

    private long m_tempMaxGT86F = 0;
    private long m_tempMaxGT77F = 0;
    private long m_tempMinLT32F = 0;
    private long m_tempMinLT5F = 0;

    private Date m_tempMaxGT86FDate = new Date(0);
    private Date m_tempMaxGT77FDate = new Date(0);
    private Date m_tempMinLT32FDate = new Date(0);
    private Date m_tempMinLT5FDate = new Date(0);

    private PeriodicValues m_maxAverageWindDirection = new PeriodicValues();
    private PeriodicValues m_maxGustWindDirection = new PeriodicValues();
    private Date lastSampleDate = new Date(0);

    private float m_windGustValues[];
//    private int m_windGustMinutes[];
    private int m_windGustIndex = 0;
    
    // ** this should be a double
    private float m_windRun = 0.0f;

    private Date m_lastStartDate = new Date(0);
    private Date m_statsBeginDate = new Date(0);

    /**
     * Construct and initialize the statistics class. Load the historical
     * information from a disk file specified in the configuration.
     * 
     * @param config
     *            Station configuration specifying the disk file to load
     */
    public WeatherStatistics(StationConfiguration config)
    {
	m_config = config;
//	m_log = log;
	
	m_windGustValues = new float[m_config.getWindGustInterval()];
//	m_windGustMinutes = new int[m_config.getWindGustInterval()];

	// load instance data from persistent storage
	loadStatsFile();
	initializeWindDirectionCounts();
	initializeWindSpeedCounts();

	// initialize cum values
	Date dtInit = new Date();
	m_windGustCumValues.updateValues(0.0f, dtInit);
	m_windSustainedCumValues.updateValues(0.0f, dtInit);
    }

    public WeatherStatistics(String xmlString)
    {
	this.fromXML(xmlString);
    }

    public String toXML()
    {
	StringBuilder str = new StringBuilder();
	SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);

	str.append(STATISTICS_ELEMENT);

	// last sample date, last start date and data start date
	str.append(LAST_SAMPLE_DATE_START_ELEMENT);
	str.append(fmt.format(lastSampleDate));
	str.append(LAST_SAMPLE_DATE_END_ELEMENT);

	str.append(LAST_START_DATE_START_ELEMENT);
	str.append(fmt.format(m_lastStartDate));
	str.append(LAST_START_DATE_END_ELEMENT);

	str.append(STATS_START_DATE_START_ELEMENT);
	str.append(fmt.format(m_statsBeginDate));
	str.append(STATS_START_DATE_END_ELEMENT);

	// outdoor temp cum values
	str.append(OUTDOOR_TEMP_ELEMENT);
	str.append(m_outdoorTempCumValues.toXML());
	str.append(OUTDOOR_TEMP_END_ELEMENT);

	// outdoor humidity cum values
	str.append(OUTDOOR_HUMIDITY_ELEMENT);
	str.append(m_outdoorHumidityCumValues.toXML());
	str.append(OUTDOOR_HUMIDITY_END_ELEMENT);

	// wind gust cum values
	str.append(WIND_GUST_ELEMENT);
	str.append(m_windGustCumValues.toXML());
	str.append(WIND_GUST_END_ELEMENT);

	// wind sustained cum values
	str.append(WIND_SUSTAINED_STAT_START_ELEMENT);
	str.append(m_windSustainedCumValues.toXML());
	str.append(WIND_SUSTAINED_STAT_END_ELEMENT);

	// indoor temp
	str.append(INDOOR_TEMP_ELEMENT);
	str.append(m_indoorTempCumValues.toXML());
	str.append(INDOOR_TEMP_END_ELEMENT);

	// indoor humidity
	str.append(INDOOR_HUMIDITY_ELEMENT);
	str.append(m_indoorHumidityCumValues.toXML());
	str.append(INDOOR_HUMIDITY_END_ELEMENT);

	// pressure
	str.append(PRESSURE_ELEMENT);
	str.append(m_pressureCumValues.toXML());
	str.append(PRESSURE_END_ELEMENT);

	// solar
	str.append(SOLAR_ELEMENT);
	str.append(m_solarCumValues.toXML());
	str.append(SOLAR_END_ELEMENT);

	// heat index
	str.append(HEAT_INDEX_STATS_ELEMENT);
	str.append(m_heatIndexCumValues.toXML());
	str.append(HEAT_INDEX_STATS_END_ELEMENT);

	// wind chill
	str.append(WIND_CHILL_STATS_ELEMENT);
	str.append(m_windChillCumValues.toXML());
	str.append(WIND_CHILL_STATS_END_ELEMENT);

	// dew point
	str.append(DEW_POINT_STATS_ELEMENT);
	str.append(m_dewPointCumValues.toXML());
	str.append(DEW_POINT_STATS_END_ELEMENT);

	// rain
	str.append(RAIN_STATS_START_ELEMENT);
	str.append(m_rainCounts.toXML());
	str.append(RAIN_STATS_END_ELEMENT);

	// lightning
	str.append(LIGHTNING_STATS_START_ELEMENT);
	str.append(m_lightningCounts.toXML());
	str.append(LIGHTNING_STATS_END_ELEMENT);

	// average wind direction
	str.append(AVERAGE_WIND_DIRECTION_START_ELEMENT);
	str.append(this.getWindAverageDirection());
	str.append(AVERAGE_WIND_DIRECTION_END_ELEMENT);

	// wind chill temperature
	str.append(WIND_CHILL_START_ELEMENT);
	str.append(this.getWindChillTemperature());
	str.append(WIND_CHILL_END_ELEMENT);

	// sustained wind speed
	str.append(WIND_SUSTAINED_START_ELEMENT);
	str.append(this.getSustainedWindSpeed());
	str.append(WIND_SUSTAINED_END_ELEMENT);

	// temperature trend
	str.append(OUTDOOR_TEMP_TREND_START_ELEMENT);
	str.append(Float.toString(m_temperatureTrend));
	str.append(OUTDOOR_TEMP_TREND_END_ELEMENT);

	str.append(OUTDOOR_HUMIDITY_TREND_START_ELEMENT);
	str.append(Float.toString(m_humidityTrend));
	str.append(OUTDOOR_HUMIDITY_TREND_END_ELEMENT);

	// pressure trend
	str.append(PRESSURE_TREND_START_ELEMENT);
	str.append(Float.toString(m_pressureTrend));
	str.append(PRESSURE_TREND_END_ELEMENT);

	// temp counts
	// str.append(TEMP_GT_77_ELEMENT);
	// str.append(this.getTempMaxGT77F());
	// str.append(TEMP_GT_77_END_ELEMENT);
	//
	// str.append(TEMP_GT_86_ELEMENT);
	// str.append(this.getTempMaxGT86F());
	// str.append(TEMP_GT_86_END_ELEMENT);
	//
	// str.append(TEMP_LT_32_ELEMENT);
	// str.append(this.getTempMinLT32F());
	// str.append(TEMP_LT_32_END_ELEMENT);
	//
	// str.append(TEMP_LT_5_ELEMENT);
	// str.append(this.getTempMinLT5F());
	// str.append(TEMP_LT_5_END_ELEMENT);

	str.append(WIND_RUN_COUNTS_START_ELEMENT);
	str.append(m_windRunCounts.toXML());
	str.append(WIND_RUN_COUNTS_END_ELEMENT);

	str.append(WIND_MAX_AVERAGE_DIRECTION_START_ELEMENT);
	str.append(m_maxAverageWindDirection.toXML());
	str.append(WIND_MAX_AVERAGE_DIRECTION_END_ELEMENT);

	str.append(WIND_MAX_GUST_DIRECTION_START_ELEMENT);
	str.append(m_maxGustWindDirection.toXML());
	str.append(WIND_MAX_GUST_DIRECTION_END_ELEMENT);

	str.append(MIN_MAX_DAY_START_ELEMENT);
	str.append(this.m_minMaxDayTemp.toXML());
	str.append(MIN_MAX_DAY_END_ELEMENT);

	str.append(MIN_MAX_NIGHT_START_ELEMENT);
	str.append(this.m_minMaxNightTemp.toXML());
	str.append(MIN_MAX_NIGHT_END_ELEMENT);

	str.append(WIND_RUN_START_ELEMENT);
	str.append(Double.toString(m_windRun));
	str.append(WIND_RUN_END_ELEMENT);

	str.append(SOLAR_TREND_START_ELEMENT);
	str.append(Float.toString(m_solarTrend));
	str.append(SOLAR_TREND_END_ELEMENT);

	str.append(INDOOR_TEMP_TREND_START_ELEMENT);
	str.append(Float.toString(m_indoorTemperatureTrend));
	str.append(INDOOR_TEMP_TREND_END_ELEMENT);

	str.append(INDOOR_HUMIDITY_TREND_START_ELEMENT);
	str.append(Float.toString(m_indoorHumidityTrend));
	str.append(INDOOR_HUMIDITY_TREND_END_ELEMENT);

	str.append(TEMP_GT_86_DATE_ELEMENT);
	str.append(fmt.format(m_tempMaxGT86FDate));
	str.append(TEMP_GT_86_DATE_END_ELEMENT);

	str.append(TEMP_GT_86_ELEMENT);
	str.append(Long.toString(m_tempMaxGT86F));
	str.append(TEMP_GT_86_END_ELEMENT);

	str.append(TEMP_GT_77_DATE_ELEMENT);
	str.append(fmt.format(m_tempMaxGT77FDate));
	str.append(TEMP_GT_77_DATE_END_ELEMENT);

	str.append(TEMP_GT_77_ELEMENT);
	str.append(Long.toString(m_tempMaxGT77F));
	str.append(TEMP_GT_77_END_ELEMENT);

	str.append(TEMP_LT_32_DATE_ELEMENT);
	str.append(fmt.format(m_tempMinLT32FDate));
	str.append(TEMP_LT_32_DATE_END_ELEMENT);

	str.append(TEMP_LT_32_ELEMENT);
	str.append(Long.toString(m_tempMinLT32F));
	str.append(TEMP_LT_32_END_ELEMENT);

	str.append(TEMP_LT_5_DATE_ELEMENT);
	str.append(fmt.format(m_tempMinLT5FDate));
	str.append(TEMP_LT_5_DATE_END_ELEMENT);

	str.append(TEMP_LT_5_ELEMENT);
	str.append(Long.toString(m_tempMinLT5F));
	str.append(TEMP_LT_5_END_ELEMENT);
	
	str.append(MAX_WIND_GUST_INTERVAL_START_ELEMENT);
	str.append(Float.toString(getMaxWindGustInterval()));
	str.append(MAX_WIND_GUST_INTERVAL_END_ELEMENT);

	str.append(STATISTICS_END_ELEMENT);

	return str.toString();
    }

    public void fromXML(String xmlString)
    {
	String data;

	// *** use getStringData()
	// grab everything between the WEATHERSTATISTICS tags
	int startPos = xmlString.indexOf(STATISTICS_ELEMENT)
		+ STATISTICS_ELEMENT.length();
	int endPos = xmlString.indexOf(STATISTICS_END_ELEMENT);
	if (startPos >= 0 && endPos > startPos)
	    data = xmlString.substring(startPos, endPos);
	else
	    data = "";

	lastSampleDate = getDateData(data, LAST_SAMPLE_DATE_START_ELEMENT,
		LAST_SAMPLE_DATE_END_ELEMENT);
	m_lastStartDate = getDateData(data, LAST_START_DATE_START_ELEMENT,
		LAST_START_DATE_END_ELEMENT);
	m_statsBeginDate = getDateData(data, STATS_START_DATE_START_ELEMENT,
		STATS_START_DATE_END_ELEMENT);

	Calendar lastSample = Calendar.getInstance();
	lastSample.setTime(lastSampleDate);
	
	m_outdoorTempCumValues = new CumulativeValues(lastSample);
	m_outdoorHumidityCumValues = new CumulativeValues(lastSample);
	m_windGustCumValues = new CumulativeValues(lastSample);
	m_windSustainedCumValues = new CumulativeValues(lastSample);
	m_indoorTempCumValues = new CumulativeValues(lastSample);
	m_indoorHumidityCumValues = new CumulativeValues(lastSample);
	m_pressureCumValues = new CumulativeValues(lastSample);
	m_solarCumValues = new CumulativeValues(lastSample);
	m_heatIndexCumValues = new CumulativeValues(lastSample);
	m_windChillCumValues = new CumulativeValues(lastSample);
	m_dewPointCumValues = new CumulativeValues(lastSample);
	m_minMaxDayTemp = new CumulativeValues(lastSample);
	m_minMaxNightTemp = new CumulativeValues(lastSample);

	m_outdoorTempCumValues.fromXML(getElement(data, OUTDOOR_TEMP_ELEMENT,
		OUTDOOR_TEMP_END_ELEMENT));
	m_outdoorHumidityCumValues.fromXML(getElement(data,
		OUTDOOR_HUMIDITY_ELEMENT, OUTDOOR_HUMIDITY_END_ELEMENT));
	m_windGustCumValues.fromXML(getElement(data, WIND_GUST_ELEMENT,
		WIND_GUST_END_ELEMENT));
	m_windSustainedCumValues.fromXML(getElement(data,
		WIND_SUSTAINED_STAT_START_ELEMENT,
		WIND_SUSTAINED_STAT_END_ELEMENT));
	m_indoorTempCumValues.fromXML(getElement(data, INDOOR_TEMP_ELEMENT,
		INDOOR_TEMP_END_ELEMENT));
	m_indoorHumidityCumValues.fromXML(getElement(data,
		INDOOR_HUMIDITY_ELEMENT, INDOOR_HUMIDITY_END_ELEMENT));
	m_pressureCumValues.fromXML(getElement(data, PRESSURE_ELEMENT,
		PRESSURE_END_ELEMENT));
	m_solarCumValues.fromXML(getElement(data, SOLAR_ELEMENT,
		SOLAR_END_ELEMENT));
	m_heatIndexCumValues.fromXML(getElement(data, HEAT_INDEX_STATS_ELEMENT,
		HEAT_INDEX_STATS_END_ELEMENT));
	m_windChillCumValues.fromXML(getElement(data, WIND_CHILL_STATS_ELEMENT,
		WIND_CHILL_STATS_END_ELEMENT));
	m_dewPointCumValues.fromXML(getElement(data, DEW_POINT_STATS_ELEMENT,
		DEW_POINT_STATS_END_ELEMENT));
	m_rainCounts.fromXML(getElement(data, RAIN_STATS_START_ELEMENT,
		RAIN_STATS_END_ELEMENT));
	m_lightningCounts.fromXML(getElement(data,
		LIGHTNING_STATS_START_ELEMENT, LIGHTNING_STATS_END_ELEMENT));
	m_windRunCounts.fromXML(getElement(data, WIND_RUN_COUNTS_START_ELEMENT,
		WIND_RUN_COUNTS_END_ELEMENT));
	m_windAverageDirection = (int) getLongData(data,
		AVERAGE_WIND_DIRECTION_START_ELEMENT,
		AVERAGE_WIND_DIRECTION_END_ELEMENT);
	m_windChillTemperature = getFloatData(data, WIND_CHILL_START_ELEMENT,
		WIND_CHILL_END_ELEMENT);
	m_sustainedWindSpeed = getFloatData(data, WIND_SUSTAINED_START_ELEMENT,
		WIND_SUSTAINED_END_ELEMENT);
	m_temperatureTrend = getFloatData(data,
		OUTDOOR_TEMP_TREND_START_ELEMENT,
		OUTDOOR_TEMP_TREND_END_ELEMENT);
	m_pressureTrend = getFloatData(data, PRESSURE_TREND_START_ELEMENT,
		PRESSURE_TREND_END_ELEMENT);
	m_humidityTrend = getFloatData(data,
		OUTDOOR_HUMIDITY_TREND_START_ELEMENT,
		OUTDOOR_HUMIDITY_TREND_END_ELEMENT);
	m_solarTrend = getFloatData(data, SOLAR_TREND_START_ELEMENT,
		SOLAR_TREND_END_ELEMENT);
	m_tempMaxGT77F = getLongData(data, TEMP_GT_77_ELEMENT,
		TEMP_GT_77_END_ELEMENT);
	m_tempMaxGT86F = getLongData(data, TEMP_GT_86_ELEMENT,
		TEMP_GT_86_END_ELEMENT);
	m_tempMinLT32F = getLongData(data, TEMP_LT_32_ELEMENT,
		TEMP_LT_32_END_ELEMENT);
	m_tempMinLT5F = getLongData(data, TEMP_LT_5_ELEMENT,
		TEMP_LT_5_END_ELEMENT);
	m_maxAverageWindDirection.fromXML(getElement(data,
		WIND_MAX_AVERAGE_DIRECTION_START_ELEMENT,
		WIND_MAX_AVERAGE_DIRECTION_END_ELEMENT));
	m_maxGustWindDirection.fromXML(getElement(data,
		WIND_MAX_GUST_DIRECTION_START_ELEMENT,
		WIND_MAX_GUST_DIRECTION_END_ELEMENT));
	m_minMaxDayTemp.fromXML(getElement(data, MIN_MAX_DAY_START_ELEMENT,
		MIN_MAX_DAY_END_ELEMENT));
	m_minMaxNightTemp.fromXML(getElement(data, MIN_MAX_NIGHT_START_ELEMENT,
		MIN_MAX_NIGHT_END_ELEMENT));
	m_windRun = getFloatData(data, WIND_RUN_START_ELEMENT,
		WIND_RUN_END_ELEMENT);
	m_indoorTemperatureTrend = getFloatData(data,
		INDOOR_TEMP_TREND_START_ELEMENT, INDOOR_TEMP_TREND_END_ELEMENT);
	m_indoorHumidityTrend = getFloatData(data,
		INDOOR_HUMIDITY_TREND_START_ELEMENT,
		INDOOR_HUMIDITY_TREND_END_ELEMENT);

	m_tempMaxGT77FDate = getDateData(data, TEMP_GT_77_DATE_ELEMENT,
		TEMP_GT_77_DATE_END_ELEMENT);
	m_tempMaxGT86FDate = getDateData(data, TEMP_GT_86_DATE_ELEMENT,
		TEMP_GT_86_DATE_END_ELEMENT);
	m_tempMinLT32FDate = getDateData(data, TEMP_LT_32_DATE_ELEMENT,
		TEMP_LT_32_DATE_END_ELEMENT);
	m_tempMinLT5FDate = getDateData(data, TEMP_LT_5_DATE_ELEMENT,
		TEMP_LT_5_DATE_END_ELEMENT);
	
	ErrorLog.logError("Finished loading stats from " + m_statsBeginDate.toString());
    }

    private Date getDateData(String xmlData, String startTag, String endTag)
    {
	SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
	Date value;
	String dateData = getStringData(xmlData, startTag, endTag);
	try
	{
	    value = fmt.parse(dateData);
	}
	catch (ParseException ex)
	{
	    value = new Date(0);
	    ErrorLog.logError("WeatherStatistics:Error parsing date "
		    + dateData);
	}

	return value;
    }

    private float getFloatData(String xmlData, String startTag, String endTag)
    {
	String value = getStringData(xmlData, startTag, endTag);
	try
	{
	    return Float.parseFloat(value);
	}
	catch (NumberFormatException ex)
	{
	    ErrorLog.logError("WeatherStatistics:Error parsing float" + value);
	    return 0.0f;
	}
    }

    private long getLongData(String xmlData, String startTag, String endTag)
    {
	String value = getStringData(xmlData, startTag, endTag);
	try
	{
	    return Long.parseLong(value);
	}
	catch (NumberFormatException ex)
	{
	    ErrorLog.logError("WeatherStatistics:Error parsing long" + value);
	    return 0L;
	}
    }

    private String getStringData(String xmlData, String startTag, String endTag)
    {
	int startPos = xmlData.indexOf(startTag) + startTag.length();
	int endPos = xmlData.indexOf(endTag);

	if (startPos >= 0 && endPos > startPos)
	{
	    return xmlData.substring(startPos, endPos);
	}
	else
	{
	    return "";
	}
    }

    // private String getAttribute(String attribute, String data)
    // {
    // int startPos;
    // int endPos;
    //
    // startPos = data.indexOf(attribute);
    // startPos += attribute.length()+2;
    // endPos = data.indexOf("\"", startPos);
    // String value = data.substring(startPos, endPos);
    // return value;
    // }

    private String getElement(String xmlString, String elementStartTag,
	    String elementEndTag)
    {
	int startPos = xmlString.indexOf(elementStartTag);
	if (startPos >= 0)
	{
	    int endPos = xmlString.indexOf(elementEndTag, startPos)
		    + elementEndTag.length();
	    return xmlString.substring(startPos, endPos);
	}
	else
	    return "";
    }

    private void initializeWindSpeedCounts()
    {
	m_windSpeedSampleExpiration = Calendar.getInstance();
	m_windSpeedSampleExpiration.add(Calendar.MINUTE,
		-1 * m_config.getWindSpeedAveragePeriod());
	// System.out.println("Next wind speed samples expire at: " +
	// m_windSpeedSampleExpiration.getTime());
    }

    private void initializeWindDirectionCounts()
    {
	m_nextWindDirectionSample = Calendar.getInstance();
	m_nextWindDirectionSample.add(Calendar.MINUTE,
		m_config.getWindDirectionAveragePeriod());

	// initialize consensus average bins
	for (int i = 0; i < m_windDirectionCounts.length; i++)
	{
	    m_windDirectionCounts[i] = 0;
	}
    }

    private void updateStatistics(WeatherData data)
    {
	// grab all the data values up front
	float temperature = data.getTemperature();
	float humidity = data.getHumidity();
	float windSpeed = data.getWindSpeed();
	int windDirection = data.getWindDirection();
	float solar = data.getSolar();
	float pressure = data.getPressure();
	float indoorTemp = data.getIndoorTemperature();
	float indoorHumidity = data.getIndoorHumidity();
	float rainCount = data.getRainfall();
	int lightningCount = data.getLightning();
	Date dtSampleTime = data.getSampleDate();

	Calendar calSampleTime = Calendar.getInstance();
	calSampleTime.setTime(dtSampleTime);
	int sampleHour = calSampleTime.get(Calendar.HOUR_OF_DAY);
	int sampleMinute = calSampleTime.get(Calendar.MINUTE);
	int sampleDay = calSampleTime.get(Calendar.DAY_OF_YEAR);
	// int sampleWeek = calSampleTime.get(Calendar.WEEK_OF_YEAR);
	int sampleMonth = calSampleTime.get(Calendar.MONTH);
	// int sampleYear = calSampleTime.get(Calendar.YEAR);

	Calendar calLastSampleDate = Calendar.getInstance();
	calLastSampleDate.setTime(lastSampleDate);
	int lastSampleDay = calLastSampleDate.get(Calendar.DAY_OF_YEAR);
	int lastSampleMonth = calLastSampleDate.get(Calendar.MONTH);
	int lastSampleMinute = calLastSampleDate.get(Calendar.MINUTE);

	// on month change reset min and max counts
	if (sampleMonth != lastSampleMonth)
	{
	    m_tempMaxGT86F = 0;
	    m_tempMaxGT77F = 0;
	    m_tempMinLT32F = 0;
	    m_tempMinLT5F = 0;
	    m_tempMaxGT77FDate = new Date(0);
	    m_tempMaxGT77FDate = new Date(0);
	    m_tempMinLT32FDate = new Date(0);
	    m_tempMinLT5FDate = new Date(0);
	}

	// if (temperature != Float.MIN_VALUE)
	{
	    m_outdoorTempCumValues.updateValues(temperature, dtSampleTime);
	    // need to make sure we haven't already counted today's temperature
	    int testDay;
//	    int testYear;
	    Calendar calTestTime = Calendar.getInstance();

	    calTestTime.setTime(m_tempMaxGT86FDate);
	    testDay = calTestTime.get(Calendar.DAY_OF_YEAR);
	    // testYear = calTestTime.get(Calendar.YEAR);
	    if (temperature > 86.0 && sampleDay != testDay)
	    {
		m_tempMaxGT86F++;
		m_tempMaxGT86FDate = dtSampleTime;
	    }

	    calTestTime.setTime(m_tempMaxGT77FDate);
	    testDay = calTestTime.get(Calendar.DAY_OF_YEAR);
	    if (temperature > 77.0f && sampleDay != testDay)
	    {
		m_tempMaxGT77F++;
		m_tempMaxGT77FDate = dtSampleTime;
	    }

	    calTestTime.setTime(m_tempMinLT32FDate);
	    testDay = calTestTime.get(Calendar.DAY_OF_YEAR);
	    if (temperature < 32.0f && sampleDay != testDay)
	    {
		m_tempMinLT32F++;
		m_tempMinLT32FDate = dtSampleTime;
	    }

	    calTestTime.setTime(m_tempMinLT5FDate);
	    testDay = calTestTime.get(Calendar.DAY_OF_YEAR);
	    if (temperature < 5.0f && sampleDay != testDay)
	    {
		m_tempMinLT5F++;
		m_tempMinLT5FDate = dtSampleTime;
	    }

	    if (sampleHour >= 6 && sampleHour < 18)
	    {
		m_minMaxDayTemp.updateValues(temperature, dtSampleTime);
	    }
	    else
	    {
		m_minMaxNightTemp.updateValues(temperature, dtSampleTime);
	    }
	}

	// if (humidity != Float.MIN_VALUE) {
	m_outdoorHumidityCumValues.updateValues(humidity, dtSampleTime);
	// }

	if (windSpeed < 0.0f)
	{
	    windSpeed = 0.0f;
	}
	// if (windSpeed >= 0.0f)
	{
	    // compute average wind speed over 1 minute interval
	    computeSustainedWindSpeed(data);

	    // accumulate wind speed cum numbers
	    m_windGustCumValues.updateValues(windSpeed, dtSampleTime);
	    m_windSustainedCumValues.updateValues(m_sustainedWindSpeed,
		    dtSampleTime);
	    
	    if (sampleMinute != lastSampleMinute)
	    {
		m_windRun += m_sustainedWindSpeed / 60.0f;
		m_windGustIndex = (m_windGustIndex + 1) % m_config.getWindGustInterval();
		m_windGustValues[m_windGustIndex] = 0.0f;
	    }
	    
	    if (windSpeed > m_windGustValues[m_windGustIndex])
	    {
	        m_windGustValues[m_windGustIndex] = windSpeed;
	    }

	    // update wind run counts
	    m_windRunCounts.updateValues(m_windRun, dtSampleTime);

	}

	// compute wind average direction using consensus averaging
	if (windDirection == WindDirectionSensor.WIND_DIRECTION_ERROR)
	{
	    windDirection = 0;

	}
	if (windDirection < 16 && windDirection >= 0)
	{
	    m_windDirectionCounts[windDirection]++;
	    if (windDirection < 4)
	    {
		m_windDirectionCounts[windDirection + 16]++;
	    }

	    Calendar currentTime = Calendar.getInstance();
	    if (m_nextWindDirectionSample.before(currentTime))
	    {
		computeAverageWindDirection();
	    }

	    if (m_windGustCumValues.getHourlyValues().getMaxDate().equals(dtSampleTime))
	    {
		// save gust direction
		m_maxGustWindDirection.setHourValue(windDirection);
	    }

	    if (lastSampleDay != sampleDay)
	    {
		m_maxGustWindDirection.setYesterdayValue(m_maxGustWindDirection
			.getDayValue());
	    }

	    if (m_windGustCumValues.getDailyValues().getMaxDate()
		    .equals(dtSampleTime))
	    {
		// save gust direction
		m_maxGustWindDirection.setDayValue(windDirection);
	    }

	    if (m_windGustCumValues.getWeeklyValues().getMaxDate()
		    .equals(dtSampleTime))
	    {
		// save gust direction
		m_maxGustWindDirection.setWeekValue(windDirection);
	    }

	    if (m_windGustCumValues.getMonthlyValues().getMaxDate()
		    .equals(dtSampleTime))
	    {
		// save gust direction
		m_maxGustWindDirection.setMonthValue(windDirection);
	    }

	    if (m_windGustCumValues.getYearlyValues().getMaxDate()
		    .equals(dtSampleTime))
	    {
		// save gust direction
		m_maxGustWindDirection.setYearValue(windDirection);
	    }

	    if (m_windGustCumValues.getCumulativeValues().getMaxDate()
		    .equals(dtSampleTime))
	    {
		// save gust direction
		m_maxGustWindDirection.setCumulativeValue(windDirection);
	    }
	}

	if (m_windSustainedCumValues.getHourlyValues().getMaxDate()
		.equals(dtSampleTime))
	{
	    // save average direction
	    m_maxAverageWindDirection.setHourValue(m_windAverageDirection);
	}

	if (lastSampleDay != sampleDay)
	{
	    m_maxAverageWindDirection
		    .setYesterdayValue(m_maxAverageWindDirection.getDayValue());
	}

	if (m_windSustainedCumValues.getDailyValues().getMaxDate()
		.equals(dtSampleTime))
	{
	    // save average direction
	    m_maxAverageWindDirection.setDayValue(m_windAverageDirection);
	}

	if (m_windSustainedCumValues.getWeeklyValues().getMaxDate()
		.equals(dtSampleTime))
	{
	    // save average direction
	    m_maxAverageWindDirection.setWeekValue(m_windAverageDirection);
	}

	if (m_windSustainedCumValues.getMonthlyValues().getMaxDate()
		.equals(dtSampleTime))
	{
	    // save average direction
	    m_maxAverageWindDirection.setMonthValue(m_windAverageDirection);
	}

	if (m_windSustainedCumValues.getYearlyValues().getMaxDate()
		.equals(dtSampleTime))
	{
	    // save average direction
	    m_maxAverageWindDirection.setYearValue(m_windAverageDirection);
	}

	if (m_windSustainedCumValues.getCumulativeValues().getMaxDate()
		.equals(dtSampleTime))
	{
	    // save average direction
	    m_maxAverageWindDirection
		    .setCumulativeValue(m_windAverageDirection);
	}

	// compute wind chill
	if (temperature > Float.MIN_VALUE
		&& this.getSustainedWindSpeed() >= 0.0f)
	{
	    this.m_windChillTemperature = WeatherConversions
		    .calcWindChillFahrenheit(temperature,
			    this.getSustainedWindSpeed());
	    // m_windChillCumValues.updateValues(this.m_windChillTemperature,
	    // dtSampleTime);
	}
	else
	{
	    this.m_windChillTemperature = temperature;
	}
	m_windChillCumValues.updateValues(this.m_windChillTemperature,
		dtSampleTime);

	// prepare samples for trend analysis
	if (sampleMinute != lastMinute)
	{
	    m_MinuteSamples.add(data);

	    // sample for 3 hours
	    if (m_MinuteSamples.size() >= MINUTE_SAMPLE_MAX_SIZE)
	    {
		m_MinuteSamples.remove(0);
	    }

	    // compute trends - don't compute until we have at least 3 samples
	    if (m_MinuteSamples.size() >= 3)
	    {
		m_temperatureTrend = leastSquaresSlope(TrendTypes.OUTDOOR_TEMPERATURE);
		m_pressureTrend = leastSquaresSlope(TrendTypes.PRESSURE);
		m_humidityTrend = leastSquaresSlope(TrendTypes.OUTDOOR_HUMIDITY);
		m_solarTrend = leastSquaresSlope(TrendTypes.SOLAR);
		m_indoorHumidityTrend = leastSquaresSlope(TrendTypes.INDOOR_HUMIDITY);
		m_indoorTemperatureTrend = leastSquaresSlope(TrendTypes.INDOOR_TEMPERATURE);
	    }

	    lastMinute = sampleMinute;

	    // ErrorLog.logError("Temp: " + data.getTemperature() +
	    // " 5 min avg: " + this.getFiveMinuteAverageTemperatureF());
	    // ErrorLog.logError("Humidity: " + data.getHumidity() +
	    // " 5 min avg: " + this.getFiveMinuteAverageHumidity());
	}

	// if (indoorTemp != Float.MIN_VALUE)
	{
	    m_indoorTempCumValues.updateValues(indoorTemp, dtSampleTime);
	}

	// if (indoorHumidity != Float.MIN_VALUE)
	{
	    m_indoorHumidityCumValues
		    .updateValues(indoorHumidity, dtSampleTime);
	}

	// if (solar != Float.MIN_VALUE)
	{
	    m_solarCumValues.updateValues(solar, dtSampleTime);

	}

	// if (pressure != Float.MIN_VALUE)
	{
	    m_pressureCumValues.updateValues(pressure, dtSampleTime);
	}

	// if (temperature != Float.MIN_VALUE && humidity != Float.MIN_VALUE)
	{
	    float heatIndex = WeatherConversions.calcHeatIndexFahrenheit(
		    temperature, humidity);
	    m_heatIndexCumValues.updateValues(heatIndex, dtSampleTime);
	}

	// compute dew point
	// if (temperature != Float.MIN_VALUE && humidity != Float.MIN_VALUE)
	{
	    float dewPoint = WeatherConversions.calcDewpointFahrenheit(
		    temperature, humidity);
	    m_dewPointCumValues.updateValues(dewPoint, dtSampleTime);
	}

	// save off rainfall stats
	if (rainCount != Float.MIN_VALUE)
	{
	    m_rainCounts.updateValues(rainCount, dtSampleTime);
	}
	else
	{
	    m_rainCounts.updateValues(m_rainCounts.getLastSampleValue(), dtSampleTime);
	}

	// save off lightning stats
	if (lightningCount != Integer.MIN_VALUE)
	{
	    m_lightningCounts.updateValues(lightningCount, dtSampleTime);
	}

	// persist state
	saveStatsFile();

	lastSampleDate = dtSampleTime;
    }

    private void computeSustainedWindSpeed(WeatherData data)
    {
	// reset 1 minute clock
	initializeWindSpeedCounts();

	// add new data sample to end of list
	m_windSpeedSamples.add(data);

	// traverse list, removing older samples and summing relevamt samples
	double windSpeedSum = 0.0f;
	int windSpeedCount = 0;
	m_sustainedWindSpeed = 0;
	Iterator<WeatherData> iter = m_windSpeedSamples.iterator();
	while (iter.hasNext())
	{
	    WeatherData sample = iter.next();
	    if (sample.getDateTimeMillis() < m_windSpeedSampleExpiration
		    .getTimeInMillis())
	    {
		// System.out.println("Expiring sample from: " + new
		// Date(sample.getDateTimeMillis()));
		iter.remove();
	    }
	    else
	    {
		// System.out.println("Counting sample from: " + new
		// Date(sample.getDateTimeMillis()));
		float windSpeed = sample.getWindSpeed();
		if (windSpeed < 0.0f)
		{
		    windSpeed = 0.0f;
		}
		windSpeedSum += windSpeed;
		windSpeedCount++;
	    }
	}

	// compute average
	if (windSpeedCount != 0)
	{
	    m_sustainedWindSpeed = (float) (windSpeedSum / windSpeedCount);
	}
    }

    private void computeAverageWindDirection()
    {
	// do consensus averaging
	int maxSumBin = 0;
	long maxSum = 0;
	for (int i = 0; i < 16; i++)
	{
	    long sum = m_windDirectionCounts[i] + m_windDirectionCounts[i + 1]
		    + m_windDirectionCounts[i + 2]
		    + m_windDirectionCounts[i + 3]
		    + m_windDirectionCounts[i + 4];

	    if (sum > maxSum)
	    {
		maxSum = sum;
		maxSumBin = i;
	    }
	}
	long W = ((m_windDirectionCounts[maxSumBin + 1]
		+ (2 * m_windDirectionCounts[maxSumBin + 2])
		+ (3 * m_windDirectionCounts[maxSumBin + 3]) + (4 * m_windDirectionCounts[maxSumBin + 4])) * 45)
		/ maxSum;
	long D = maxSumBin * 45 + W;
	while (D > 720)
	{
	    D -= 720;
	}
	if (D < 0)
	{
	    D = 0;
	}

	m_windAverageDirection = (int) ((D / 2) / 22.5);

	initializeWindDirectionCounts();
    }

    /**
     * Get the statistics for the outdoor temperature
     * 
     * @return The outdoor temperature statistics
     */
    public synchronized CumulativeValues getOutdoorTempStats()
    {
	return m_outdoorTempCumValues;
    }

    /**
     * Get the statistics for the outdoor humidity
     * 
     * @return The outdoor humidity statistics
     */
    public synchronized CumulativeValues getOutdoorHumidityStats()
    {
	return m_outdoorHumidityCumValues;
    }

    /**
     * Get the statistics for the widn gusts
     * 
     * @return The wind gust statistics
     */
    public synchronized CumulativeValues getWindGustStats()
    {
	return m_windGustCumValues;
    }

    /**
     * Get the statistics for the sustained wind speed
     * 
     * @return The sustained wind speed statistics
     */
    public synchronized CumulativeValues getWindSustainedStats()
    {
	return m_windSustainedCumValues;
    }

    /**
     * Get the average wind direction. Use a consensus averaging technique.
     * 
     * @return The average wind direction as an integer
     */
    public synchronized int getWindAverageDirection()
    {
	return m_windAverageDirection;
    }

    /**
     * Get the wind chill temperature
     * 
     * @return The wind chill temperature in degrees F
     */
    public synchronized float getWindChillTemperature()
    {
	return m_windChillTemperature;
    }

    /**
     * Get the sustained wind speed
     * 
     * @return The sustained wind speed in mph
     */
    public synchronized float getSustainedWindSpeed()
    {
	return m_sustainedWindSpeed;
    }

    private synchronized void loadStatsFile()
    {
	if (m_config.getHomeDirectory() != null
		&& m_config.getHomeDirectory().length() > 0)
	{
	    m_statsFile = new File(m_config.getHomeDirectory() + File.separator
		    + statisticsFileName);

	}
	else
	{
	    m_statsFile = new File(statisticsFileName);
	}

	ErrorLog.logError("Stats file is " + m_statsFile);
	
	// if the file doesn't exist then create it
	if (!m_statsFile.exists() || m_statsFile.length() == 0)
	{
	    try
	    {
		ErrorLog.logError("Ceating new stats file");
		m_statsFile.createNewFile();
		m_statsBeginDate = new Date();
	    }
	    catch (IOException e)
	    {
		// handle exception
		// System.out.println(e);
		ErrorLog.logError("Error loading stats file: " + e);
	    }
	}
	else
	{
	    // load the contents of the log file
	    ErrorLog.logError("Reading existing stats file");
	    
	    try
	    {
		BufferedReader reader = new BufferedReader(new FileReader(
			m_statsFile));
		StringBuffer data = new StringBuffer();
		String s;

		while ((s = reader.readLine()) != null)
		{
		    data.append(s);
		}
		reader.close();

		this.fromXML(data.toString());

		// // read in array of objects
		// ObjectInputStream in = new ObjectInputStream(new
		// FileInputStream(m_statsFile));
		//
		// try
		// {
		// m_outdoorTempCumValues = (CumulativeValues) in.readObject();
		// m_outdoorHumidityCumValues = (CumulativeValues)
		// in.readObject();
		// m_windGustCumValues = (CumulativeValues) in.readObject();
		// m_windSustainedCumValues = (CumulativeValues)
		// in.readObject();
		// m_indoorTempCumValues = (CumulativeValues) in.readObject();
		// m_indoorHumidityCumValues = (CumulativeValues)
		// in.readObject();
		// m_pressureCumValues = (CumulativeValues) in.readObject();
		// m_solarCumValues = (CumulativeValues) in.readObject();
		//
		// m_heatIndexCumValues = (CumulativeValues) in.readObject();
		// m_windChillCumValues = (CumulativeValues) in.readObject();
		// m_dewPointCumValues = (CumulativeValues) in.readObject();
		// m_rainCounts = (CumulativeCounts) in.readObject();
		// m_lightningCounts = (CumulativeCounts) in.readObject();
		//
		// m_tempMaxGT77F = in.readLong();
		// m_tempMaxGT86F = in.readLong();
		// m_tempMinLT32F = in.readLong();
		// m_tempMinLT5F = in.readLong();
		//
		// m_windRunCounts = (CumulativeCounts)in.readObject();
		//
		// m_maxAverageWindDirection = (PeriodicValues)in.readObject();
		// m_maxGustWindDirection = (PeriodicValues)in.readObject();
		//
		// m_minMaxDayTemp = (CumulativeValues) in.readObject();
		// m_minMaxNightTemp = (CumulativeValues) in.readObject();
		//
		// m_windRun = (float)(in.readDouble());
		//
		// long data = in.readLong();
		// m_statsBeginDate = new Date(data);
		// data = in.readLong();
		// m_lastStartDate = new Date(data);
		// }
		// catch (ClassNotFoundException ex)
		// {
		// //System.out.println("Class not found reading stats: " + ex);
		// ErrorLog.logError("Class not found reading stats: " + ex);
		// }
		//
		// in.close();
	    }
	    catch (FileNotFoundException ex)
	    {
		// System.out.println("File not found: " + ex);
		ErrorLog.logError("File not found in loadStatsFile(): " + ex);
	    }
	    catch (IOException ex)
	    {
		// System.out.println("I/O exception: " + ex);
		ErrorLog.logError("I/O exception in loadStatsFile(): " + ex);
		ErrorLog.logStackTrace(ex);
	    }
	}
    }

    private synchronized void saveStatsFile()
    {
	try
	{
	    // ObjectOutputStream out = new ObjectOutputStream(new
	    // FileOutputStream(m_statsFile));
	    //
	    // out.writeObject(m_outdoorTempCumValues);
	    // out.writeObject(m_outdoorHumidityCumValues);
	    // out.writeObject(m_windGustCumValues);
	    // out.writeObject(m_windSustainedCumValues);
	    // out.writeObject(m_indoorTempCumValues);
	    // out.writeObject(m_indoorHumidityCumValues);
	    // out.writeObject(m_pressureCumValues);
	    // out.writeObject(m_solarCumValues);
	    //
	    // out.writeObject(m_heatIndexCumValues);
	    // out.writeObject(m_windChillCumValues);
	    // out.writeObject(m_dewPointCumValues);
	    // out.writeObject(m_rainCounts);
	    // out.writeObject(m_lightningCounts);
	    //
	    // out.writeLong(m_tempMaxGT77F);
	    // out.writeLong(m_tempMaxGT86F);
	    // out.writeLong(m_tempMinLT32F);
	    // out.writeLong(m_tempMinLT5F);
	    //
	    // out.writeObject(m_windRunCounts);
	    //
	    // out.writeObject(m_maxAverageWindDirection);
	    // out.writeObject(m_maxGustWindDirection);
	    //
	    // out.writeObject(m_minMaxDayTemp);
	    // out.writeObject(m_minMaxNightTemp);
	    //
	    // out.writeDouble(m_windRun);
	    //
	    // out.writeLong(m_statsBeginDate.getTime());
	    // out.writeLong(m_lastStartDate.getTime());
	    //
	    // out.close();

	    // convert to XML before beginning I/O operations.
	    String stats = this.toXML();
	    FileOutputStream strm = new FileOutputStream(m_statsFile);
	    strm.write(stats.getBytes());
	    strm.close();
	}
	catch (IOException ex)
	{
	    // System.out.println("Error writing log: " + ex);
	    ErrorLog.logError("Error writing log in saveStatsFile(): " + ex);
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see oneWireWeather.WeatherDataListener#notifyWeatherData(oneWireWeather.
     * WeatherData)
     */
    public synchronized void notifyWeatherData(WeatherData data)
    {
        if (m_config.isSaveStats())
        {
            this.updateStatistics(data);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * oneWireWeather.ConfigurationChangeListener#notifyConfigurationChange(
     * oneWireWeather.StationConfiguration)
     */
    public void notifyConfigurationChange(StationConfiguration config)
    {
	m_config = config;

	// ** react to config change as needed
    }

    /**
     * Get the outdoor temperature trend
     * 
     * @return The outdoor temperature trend in degrees/hour
     */
    public synchronized float getTemperatureTrend()
    {
	return m_temperatureTrend;
    }

    /**
     * Get the barometric pressure trend
     * 
     * @return The barometric pressure trend in inches/3 hours
     */
    public synchronized float getPressureTrend()
    {
	// return m_pressureTrend * MINUTE_SAMPLE_MAX_SIZE;
	// ErrorLog.logError("Returning pressure trend " + m_pressureTrend);
	return m_pressureTrend;
    }

    // computation results in minutes since samples are per minute
    // to convert to hours multiply by 60 (3 hours by 180)
    private float leastSquaresSlope(TrendTypes trendType)
    {
	double sumX = 0.0;
	double sumY = 0.0;
	double sumXX = 0.0;
	double sumXY = 0.0;

	int listSize = m_MinuteSamples.size();
	int readingCount = 0;

	switch (trendType)
	{
	    case OUTDOOR_TEMPERATURE:
		// do trend on temperature
		for (int i = 0; i < listSize; i++)
		{
		    float temp = m_MinuteSamples.get(i).getTemperature();

		    if (temp != Float.MIN_VALUE)
		    {
			sumX += i;
			sumY += temp;
			sumXY += (i * temp);
			sumXX += (i * i);

			readingCount++;
		    }
		}
		break;

	    case OUTDOOR_HUMIDITY:
		for (int i = 0; i < listSize; i++)
		{
		    float humidity = m_MinuteSamples.get(i).getHumidity();

		    if (humidity != Float.MIN_VALUE)
		    {
			sumX += i;
			sumY += humidity;
			sumXY += (i * humidity);
			sumXX += (i * i);

			readingCount++;
		    }
		}
		break;

	    case PRESSURE:
		for (int i = 0; i < listSize; i++)
		{
		    float pressure = m_MinuteSamples.get(i).getPressure();

		    if (pressure != Float.MIN_VALUE)
		    {
			sumX += i;
			sumY += pressure;
			sumXY += (i * pressure);
			sumXX += (i * i);

			// ErrorLog.logError("Pressure value " + readingCount +
			// " " + pressure);
			readingCount++;
		    }
		}
		// ErrorLog.logError("sumX = " + sumX + " sumY = " + sumY);
		// ErrorLog.logError("sumXY = " + sumXY + " sumXX = " + sumXX);
		break;
	    case INDOOR_TEMPERATURE:
		for (int i = 0; i < listSize; i++)
		{
		    float temp = m_MinuteSamples.get(i).getIndoorTemperature();

		    if (temp != Float.MIN_VALUE)
		    {
			sumX += i;
			sumY += temp;
			sumXY += (i * temp);
			sumXX += (i * i);

			readingCount++;
		    }
		}
		break;

	    case INDOOR_HUMIDITY:
		for (int i = 0; i < listSize; i++)
		{
		    float humidity = m_MinuteSamples.get(i).getIndoorHumidity();

		    if (humidity != Float.MIN_VALUE)
		    {
			sumX += i;
			sumY += humidity;
			sumXY += (i * humidity);
			sumXX += (i * i);

			readingCount++;
		    }
		}
		break;

	    case SOLAR:
		for (int i = 0; i < listSize; i++)
		{
		    float solar = m_MinuteSamples.get(i).getSolar();

		    if (solar != Float.MIN_VALUE)
		    {
			sumX += i;
			sumY += solar;
			sumXY += (i * solar);
			sumXX += (i * i);

			readingCount++;
		    }
		}
		break;
	}

	// double a = (sumX * sumY) - (sumXY * listSize);
	// double d = (sumX * sumX) - (sumXX * listSize);
	double a = (sumX * sumY) - (sumXY * readingCount);
	double d = (sumX * sumX) - (sumXX * readingCount);

	// if (trendType == TrendTypes.PRESSURE)
	// {
	// ErrorLog.logError("a = " + a + " d = " + d + " a/d " + a/d);
	// }

	if (d != 0)
	    return (float) (a / d);
	else
	    return 0.0f;
    }

    /**
     * @return the m_indoorHumidityCumValues
     */
    public synchronized CumulativeValues getIndoorHumidityCumValues()
    {
	return m_indoorHumidityCumValues;
    }

    /**
     * @return the m_indoorTempCumValues
     */
    public synchronized CumulativeValues getIndoorTempCumValues()
    {
	return m_indoorTempCumValues;
    }

    /**
     * @return the m_pressureCumValues
     */
    public synchronized CumulativeValues getPressureCumValues()
    {
	return m_pressureCumValues;
    }

    /**
     * @return the m_solarCumValues
     */
    public synchronized CumulativeValues getSolarCumValues()
    {
	return m_solarCumValues;
    }

    /**
     * @return the m_dewPointCumValues
     */
    public synchronized CumulativeValues getDewPointCumValues()
    {
	return m_dewPointCumValues;
    }

    /**
     * @return the m_heatIndexCumValues
     */
    public synchronized CumulativeValues getHeatIndexCumValues()
    {
	return m_heatIndexCumValues;
    }

    /**
     * @return the m_windChillCumValues
     */
    public synchronized CumulativeValues getWindChillCumValues()
    {
	return m_windChillCumValues;
    }

    public synchronized CumulativeCounts getRainCounts()
    {
	return m_rainCounts;
    }

    public CumulativeCounts getLightningCounts()
    {
	return m_lightningCounts;
    }

    /**
     * @return the tempMaxGT77F
     */
    public synchronized long getTempMaxGT77F()
    {
	return m_tempMaxGT77F;
    }

    /**
     * @return the tempMaxGT86F
     */
    public synchronized long getTempMaxGT86F()
    {
	return m_tempMaxGT86F;
    }

    /**
     * @return the tempMinLT32F
     */
    public synchronized long getTempMinLT32F()
    {
	return m_tempMinLT32F;
    }

    /**
     * @return the tempMinLT5f
     */
    public synchronized long getTempMinLT5F()
    {
	return m_tempMinLT5F;
    }

    /**
     * @return the m_windRunCounts
     */
    public synchronized CumulativeCounts getWindRunCounts()
    {
	return m_windRunCounts;
    }

    /**
     * @return the m_maxAverageWindDirection
     */
    public synchronized PeriodicValues getMaxAverageWindDirection()
    {
	return m_maxAverageWindDirection;
    }

    /**
     * @return the m_maxGustWindDirection
     */
    public synchronized PeriodicValues getMaxGustWindDirection()
    {
	return m_maxGustWindDirection;
    }

    public synchronized CumulativeValues getMinMaxTemperatureDay()
    {
	return m_minMaxDayTemp;
    }

    public synchronized CumulativeValues getMinMaxTemperatureNight()
    {
	return m_minMaxNightTemp;
    }

    /**
     * @return the m_humidityTrend
     */
    public synchronized float getHumidityTrend()
    {
	return m_humidityTrend;
    }

    /**
     * @return the m_indoorHumidityTrend
     */
    public synchronized float getIndoorHumidityTrend()
    {
	return m_indoorHumidityTrend;
    }

    /**
     * @return the m_indoorTemperatureTrend
     */
    public synchronized float getIndoorTemperatureTrend()
    {
	return m_indoorTemperatureTrend;
    }

    /**
     * @return the m_solarTrend
     */
    public synchronized float getSolarTrend()
    {
	return m_solarTrend;
    }

    /**
     * @return the m_lastStartDate
     */
    public Date getLastStartDate()
    {
	return m_lastStartDate;
    }

    /**
     * @param startDate
     *            the m_lastStartDate to set
     */
    public void setLastStartDate(Date startDate)
    {
	m_lastStartDate = startDate;
    }

    /**
     * @return the m_statsBeginDate
     */
    public Date getStatsBeginDate()
    {
	return m_statsBeginDate;
    }

    public float getFiveMinuteAverageTemperatureF()
    {
	return getAverageTemperatureF(5);
    }

    public float getAverageTemperatureF(int minuteCount)
    {
	float total = 0.0f;
	float averageValue = Float.MIN_VALUE;
	int numValues = 0;
	int sampleSize = m_MinuteSamples.size();

	if (minuteCount > sampleSize)
	{
	    minuteCount = sampleSize;
	}

	if (minuteCount <= 0)
	{
	    minuteCount = 1;
	}

	for (int i = sampleSize - minuteCount; i < sampleSize; i++)
	{
	    float temp = m_MinuteSamples.get(i).getTemperature();

	    if (temp != Float.MIN_VALUE)
	    {
		total += temp;
		numValues++;
	    }
	}

	if (numValues > 0)
	{
	    averageValue = total / numValues;
	}

	return averageValue;
    }

    public float getFiveMinuteAverageHumidity()
    {
	return getAverageHumidity(5);
    }

    public float getAverageHumidity(int minuteCount)
    {
	float total = 0.0f;
	float averageValue = Float.MIN_VALUE;
	int numValues = 0;
	int sampleSize = m_MinuteSamples.size();

	if (minuteCount > sampleSize)
	{
	    minuteCount = sampleSize;
	}

	if (minuteCount <= 0)
	{
	    minuteCount = 1;
	}

	for (int i = sampleSize - minuteCount; i < sampleSize; i++)
	{
	    float humidity = m_MinuteSamples.get(i).getHumidity();

	    if (humidity != Float.MIN_VALUE)
	    {
		total += humidity;
		numValues++;
	    }
	}

	if (numValues > 0)
	{
	    averageValue = total / numValues;
	}

	return averageValue;
    }
    
    public float getMaxWindGustInterval()
    {
        float maxGust = 0.0f;
        
        for (int i = 0; i < m_config.getWindGustInterval(); i++)
        {
            if (m_windGustValues[i] > maxGust)
            {
                maxGust = m_windGustValues[i];
            }
        }
        
        return maxGust;
        
//        int minutes = m_config.getWindGustInterval();
//        return m_log.getMaxGustTimeInterval(minutes);
    }
}
