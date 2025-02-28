/* SVN FILE: $Id: WeatherStation.java 503 2020-10-04 18:35:55Z  $ */
/**
 * One Wire Weather : Weather station daemon for a 1-wire weather station
 *
 * $Author: $
 * $Revision: 503 $
 * $Date: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
 * $LastChangedBy: $
 * $LastChangedDate: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
 * $URL: http://192.168.123.7/svn/OneWireWeather/src/oneWireWeather/WeatherStation.java $
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
import java.io.FileNotFoundException;
import java.util.*;

import oneWireWeather.dataCollectors.WeatherDataCollector;
import oneWireWeather.publishers.BackyardWeatherPublisher;
import oneWireWeather.publishers.CWOPPublisher;
import oneWireWeather.publishers.DatabasePublisher;
import oneWireWeather.publishers.WeatherDataPublisher;
import oneWireWeather.publishers.WeatherForYouPublisher;
import oneWireWeather.publishers.WeatherUndergroundPublisher;

/**
 * WeatherStation is the main class of the one-wire weather station.
 * It provides the interface that allows a client to retrieve the
 * weather data and to administer the weather server processes and
 * configuration.
 *   
 * @author Seth Cohen
 * @version %I%, %G%
 * 
 */
public class WeatherStation implements FileChangeListener {

	private static final String m_strConfigFileName = "weatherConfig.xml";

	private StationConfiguration m_configuration;
	private WeatherData m_data;
	private WeatherDataCollector m_collector;
	private Thread m_collectorThread;
	private StationLog m_log;
	private WeatherStatistics m_stats; 
	private WeatherServer m_wxServer;
	private Thread m_wxServerThread;
	private DataLogger m_dataLogger;

	private WeatherUndergroundPublisher m_wxUnderground;
	private WeatherDataPublisher m_cwop;
	private WeatherForYouPublisher m_weatherForYou;
	private BackyardWeatherPublisher m_backyardWeather;
	private DatabasePublisher m_dbPublisher;

	private ArrayList<WeatherDataListener> m_weatherDataListeners = 
			new ArrayList<WeatherDataListener>();
	private ArrayList<ConfigurationChangeListener> m_configurationChangeListeners =
			new ArrayList<ConfigurationChangeListener>();
	private ArrayList<WeatherServerHandler> m_activeServerThreads = new ArrayList<WeatherServerHandler>();

	private FileChangeMonitor fileChangeMonitor;
	private boolean stationIsRunning = false;

	// this is the information about all of the sensors this application knows about
	// to be able to use other sensors, add them to this list 
	private static final SensorType[] m_knownSensorTypes = {
			new SensorType(SensorType.MAIN_OUTDOOR_TEMP, SensorType.TEMPERATURE_SENSOR_FAMILY, "Outdoor Temperature Sensor", 1.0f, 0.0f, 60000, 10000),
			new SensorType(SensorType.MAIN_INDOOR_TEMP, SensorType.TEMPERATURE_SENSOR_FAMILY, "Indoor Temperature Sensor", 1.0f, 0.0f, 60000, 15000),
			new SensorType(SensorType.AUX_OUTDOOR_TEMP, SensorType.TEMPERATURE_SENSOR_FAMILY, "Aux Outdoor Temperature Sensor", 1.0f, 0.0f, 60000, 20000),
			new SensorType(SensorType.AUX_INDOOR_TEMP, SensorType.TEMPERATURE_SENSOR_FAMILY, "Aux Indoor Temperature Sensor", 1.0f, 0.0f, 60000, 25000),
			new SensorType(SensorType.OUTDOOR_HUMIDITY, SensorType.HUMIDITY_SENSOR_FAMILY, "Outdoor Humidity", 1.0f, 0.0f, 60000, 30000),
			new SensorType(SensorType.INDOOR_HUMIDITY, SensorType.HUMIDITY_SENSOR_FAMILY, "Indoor Humidity", 1.0f, 0.0f, 60000, 35000),
			new SensorType(SensorType.PRESSURE, SensorType.PRESSURE_SENSOR_FAMILY, "Barometric Pressure", 0.7171f, 26.2523f, 60000, 40000),
			new SensorType(SensorType.RAIN_COUNTER, SensorType.RAIN_COUNTER_SENSOR_FAMILY, "Rain Counter", 1.0f, 0.0f, 60000, 45000),
			new SensorType(SensorType.LIGHTNING_COUNTER, SensorType.LIGHTNING_COUNTER_SENSOR_FAMILY, "Lightning Counter", 1.0f, 0.0f, 60000, 55000),
			new SensorType(SensorType.AAG_WIND_SPEED, SensorType.AAG_WIND_SPEED_SENSOR_FAMILY, "Wind Speed", 0.0f, 0.0f, 1000, 0),
			new SensorType(SensorType.AAG_WIND_DIRECTION, SensorType.AAG_WIND_DIRECTION_SENSOR_FAMILY, "Wind Direction (ADS)", 0.0f, 0.0f, 1000, 0),
			new SensorType(SensorType.ADS_WIND_SPEED, SensorType.ADS_WIND_SPEED_SENSOR_FAMILY, "Wind Speed (ADS)", 0.0f, 0.0f, 1000, 0),
			new SensorType(SensorType.ADS_WIND_DIRECTION, SensorType.ADS_WIND_DIRECTION_SENSOR_FAMILY, "Wind Direction (ADS)", 0.0f, 0.0f, 1000, 0),
			new SensorType(SensorType.SOLAR, SensorType.SOLAR_SENSOR_FAMILY, "Solar Reading", 1.0f, 0.0f, 60000, 50000)
	};

	/**
	 * Create an instance of the WeatherStation.  This constructor will also
	 * load the configuration indicating the sensors and settings for this 
	 * weather station.  It will also create/open a log file and saved statistics file
	 * and set them up to receive weather station events.
	 *
	 */
	public WeatherStation()
	{
		createWeatherStation("");
	}

	public WeatherStation(String homeDirectory)
	{
		createWeatherStation(homeDirectory);
	}

	private void createWeatherStation(String homeDirectory)
	{
		m_configuration = new StationConfiguration();
		//loadStationConfiguration();
		String fullConfigFilePath;
		if (homeDirectory != null && homeDirectory.length() > 0)
		{
			fullConfigFilePath = homeDirectory + File.separator + m_strConfigFileName;
			m_configuration.setHomeDirectory(homeDirectory);
			ErrorLog.setLogFilePath(homeDirectory);
		}
		else
		{
			fullConfigFilePath = m_strConfigFileName;
		}
		m_configuration.load(fullConfigFilePath);

		// if configured to save a log
		m_log = new StationLog(m_configuration);
		m_log.load();
		this.addWeatherDataListener(m_log);

		// if configured to save stats
		m_stats = new WeatherStatistics(m_configuration);
		this.addWeatherDataListener(m_stats);

		// if configured to save weather data - not really that useful and causes a lot of writes 
		m_dataLogger = new DataLogger(m_configuration);
		this.addWeatherDataListener(m_dataLogger);

		// add configuration change listeners
		this.addConfigurationChangeListener(m_log);
		this.addConfigurationChangeListener(m_stats);

		fileChangeMonitor = FileChangeMonitor.getInstance();
		try 
		{
			fileChangeMonitor.addFileChangeListener(this, fullConfigFilePath, 10000);
		} 
		catch (FileNotFoundException e) 
		{
			ErrorLog.logError("Configuration file not found by monitor: " + e.getMessage());
		}
	}

	/**
	 * Start the weather station process in a separate thread.
	 * This process will poll the sensor hardware based on the
	 * station configuration settings and notify this class after
	 * each reading by calling the receiveMessage() method.
	 *
	 */
	public void startWeatherStation()
	{
		ErrorLog.logError("Starting weather station at: " + new Date());

		// reload station configuration prior to starting up
		String fullConfigFilePath;
		String homeDir = m_configuration.getHomeDirectory();

		// get full path to config file
		if (homeDir != null && homeDir.length() > 0 )
		{
			fullConfigFilePath = homeDir + File.separator + m_strConfigFileName;
		}
		else
		{
			fullConfigFilePath = m_strConfigFileName;
		}
		m_configuration.load(fullConfigFilePath);

		// create data acquisition class and start it running
		m_collector = new WeatherDataCollector(m_configuration, this);
		m_collectorThread = new Thread(m_collector);
		m_collectorThread.start();

		m_wxServer = new WeatherServer(this);
		m_wxServerThread = new Thread(m_wxServer);
		m_wxServerThread.start();

		ErrorLog.logError("Starting weather data publishers");

		ErrorLog.logError("Starting Weather Underground publisher");
		m_wxUnderground = new WeatherUndergroundPublisher(m_configuration, this);
		this.addConfigurationChangeListener(m_wxUnderground);
		m_wxUnderground.startPublishing();

		ErrorLog.logError("Starting CWOP publisher");
		m_cwop = new CWOPPublisher(m_configuration, this);
		this.addConfigurationChangeListener(m_cwop);
		m_cwop.startPublishing();

		ErrorLog.logError("Starting Weather For You publisher");
		m_weatherForYou = new WeatherForYouPublisher(m_configuration, this);
		this.addConfigurationChangeListener(m_weatherForYou);
		m_weatherForYou.startPublishing();

		ErrorLog.logError("Starting Weather Bug publisher");
		m_backyardWeather = new BackyardWeatherPublisher(m_configuration, this);
		this.addConfigurationChangeListener(m_backyardWeather);
		m_backyardWeather.startPublishing();

		ErrorLog.logError("Starting JDBC database publisher");
		m_dbPublisher = new DatabasePublisher(m_configuration, this);
		this.addConfigurationChangeListener(m_dbPublisher);
		m_dbPublisher.startPublishing();

		m_stats.setLastStartDate(new Date());

		stationIsRunning = true;

		ErrorLog.logError("Weather station is running");
	}

	/**
	 * Retrieve the most recent readings from the weather station data acquisition
	 * thread.
	 * 
	 * @return The most recent sensor readings as a WeatherData class.
	 */
	public synchronized WeatherData getData()
	{
		if (m_data == null) return null;

		return m_data;
	}

	/**
	 * Retrieve the statistics that have been collected for this weather station.
	 * These statistics include the minimum and maximum values for the common
	 * sensors.  They also include averages and trends where applicable.
	 * 
	 * @return The weather station stistics as a WeatherStatistics class
	 */
	public synchronized WeatherStatistics getStatistics()
	{
		if (m_stats == null || !m_configuration.isSaveStats()) return null;

		return m_stats;
	}

	/**
	 * This method is called by the data acquisition thread whenever it has 
	 * completed polling the configured sensors.
	 * 
	 * @param data 		A WeatherData class that holds the values from the
	 * 					sensors that were just polled.  If any sensors were not
	 * 					polled this pass, the WeatherData contains the most recently
	 * 					polled values from that sensor.  
	 */
	public synchronized void receiveMessage(WeatherData data)
	{
		m_data = data;

		// notify list of listeners
		for( int i = 0; i < m_weatherDataListeners.size(); i++)
		{
			m_weatherDataListeners.get(i).notifyWeatherData(data);
		}

		if (m_configuration.isSaveStats())
		{
			data.setAverageWindSpeed(m_stats.getSustainedWindSpeed());
			data.setAverageWindDirection(m_stats.getWindAverageDirection());
		}
	}

	/**
	 * Retrieve the configuration for this instance of the WeatherStation.
	 * This includes the configured sensors, the 1-wire adapter, and the
	 * polling intervals.
	 * 
	 * @return The configuration as a StationConfiguration class instance.
	 */
	public synchronized StationConfiguration getStationConfiguration()
	{
		// return a copy of the configuration, not a reference to the original
		return (StationConfiguration)m_configuration.clone();
	}

	/**
	 * This method is used by clients to set a new configuration for this weather station.
	 * The new configuration will be saved to the disk file specified in the 
	 * variable m_strConfigFileName.  Before setting the new configuration, the weather
	 * station must be stopped and restarted.  Also, any ConfigurationChangeListeners must be
	 * notified of the configuration change.
	 *  
	 * @param config 	The new station configuration.
	 * @return 			true if the save to disk was successful 
	 */
	public synchronized boolean saveConfiguration(StationConfiguration config)
	{
		boolean saveOK = false;
		boolean stationWasRunning = stationIsRunning;

		// stop weather station
		if (stationIsRunning) {
			stopWeatherStation();
		}

		m_configuration = config;

		// save the new configuration - overlay the previous config file
		m_configuration.save(m_strConfigFileName);

		notifyConfigurationListeners();

		// restart weather station
		if (stationWasRunning) {
			startWeatherStation();
		}

		return saveOK;
	}

	private void notifyConfigurationListeners()
	{
		// notify the list of listeners
		for( int i = 0; i < m_configurationChangeListeners.size(); i++)
		{
			m_configurationChangeListeners.get(i).notifyConfigurationChange(m_configuration);
		}
	}

	/**
	 * Stop the data acquisition thread of the weather station.
	 *
	 */
	public void stopWeatherStation()
	{
		ErrorLog.logError("Stopping weather station at: " + new Date());

		if (m_collector == null && m_wxServer == null && m_wxUnderground == null && m_cwop == null) return;
		if (!stationIsRunning) return;

		m_collector.setQuitFlag();

		// don't use interrupt unless you ensure that the thread will exit in
		// a known good state.  Sometimes it gets unusual readings from the 1-wire sensors
		// when interrupted
		//m_collectorThread.interrupt();

		ErrorLog.logError("Waiting for data collector");

		try
		{
			m_collectorThread.join(60000);
		}
		catch (InterruptedException ex){}

		m_collector = null;
		m_collectorThread = null;
		ErrorLog.logError("Data collector stopped");

		m_wxServer.setQuitFlag();

		ErrorLog.logError("Waiting for server thread");
		try 
		{
			m_wxServerThread.join(60000);
		} 
		catch (InterruptedException e) {}

		m_wxServer = null;
		m_wxServerThread = null;
		ErrorLog.logError("Server thread stopped");

		m_wxUnderground.stopPublishing();
		m_wxUnderground = null;

		m_cwop.stopPublishing();
		m_cwop = null;

		m_weatherForYou.stopPublishing();
		m_weatherForYou = null;

		// notify each active server thread
		for (int i = 0; i < m_activeServerThreads.size(); i++) 
		{
			m_activeServerThreads.get(i).setQuitFlag();
		}
		m_activeServerThreads.clear();

		stationIsRunning = false;

		ErrorLog.logError("Weather station is stopped");
	}

	/**
	 * Add an observer to listen for any weather data changes.
	 * 
	 * @param listener 	a class that implements WeatherDataListener that wants 
	 * 					to be informed of any changes in the weather data.
	 */
	public synchronized void addWeatherDataListener(WeatherDataListener listener)
	{
		m_weatherDataListeners.add(listener);
	}

	public synchronized void removeWeatherDataListener(WeatherDataListener listener)
	{
		m_weatherDataListeners.remove(listener);
	}

	/**
	 * Add an observer to listen for any weather data changes.
	 * 
	 * @param listener a class that implements ConfigurationDataListener that wants 
	 * 					to be informed of any changes in the weather station configuration.
	 */
	public synchronized void addConfigurationChangeListener(ConfigurationChangeListener listener)
	{
		m_configurationChangeListeners.add(listener);
	}

	public synchronized void removeConfigurationChangeListener(ConfigurationChangeListener listener)
	{
		m_configurationChangeListeners.remove(listener);
	}

	/**
	 * Retrieve the log file from this weather station.  The log contains all of the 
	 * saved sensor readings.  The number of readings is based on the configuration
	 * setting LogFileSize.
	 *  
	 * @return The stationlog as a StationLog class.
	 */
	public synchronized StationLog getLog()
	{
		if (m_configuration.isSaveLog())
		{
			return m_log;
		}
		else
		{
			return null;
		}
	}

	/**
	 * For a given sensor usage type, return the corresponding SensorType class.
	 * 
	 * @param usage		The sensor usage type to be looked up.
	 * @return			The associated SensorType for the specified sensor usage. 
	 */
	public static SensorType getSensorTypeForUsage(int usage)
	{
		for (int i = 0; i < m_knownSensorTypes.length; i++)
		{
			if (m_knownSensorTypes[i].getSensorUsageType() == usage)
			{
				return m_knownSensorTypes[i];
			}
		}

		return null;
	}

	public synchronized void addServerThread(WeatherServerHandler thread)
	{
		m_activeServerThreads.add(thread);
	}

	public synchronized void removeServerThread(WeatherServerHandler thread)
	{
		m_activeServerThreads.remove(thread);
	}

	public void fileChanged(String fileName) {

		boolean stationWasRunning = stationIsRunning;

		ErrorLog.logError("Configuration was changed");

		// reload configuration file from disk
		m_configuration.load(m_configuration.getHomeDirectory() + File.separator + m_strConfigFileName);

		// set the new configuration
		if (stationIsRunning) {
			ErrorLog.logError("Stopping weather station for new configuration");
			stopWeatherStation();
		}

		notifyConfigurationListeners();

		if (stationWasRunning) {
			ErrorLog.logError("Starting weather station with new configuration");
			startWeatherStation();
		}

	}

	public void shutDownFileMonitor()
	{
		fileChangeMonitor.removeFileChangeListener(this, 
				m_configuration.getHomeDirectory() + File.separator + m_strConfigFileName);
	}

	public boolean isStationRunning()
	{
		return stationIsRunning;
	}

}
