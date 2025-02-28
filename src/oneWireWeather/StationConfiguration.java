/* SVN FILE: $Id: StationConfiguration.java 204 2016-11-23 04:34:51Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 204 $
* $Date: 2016-11-22 23:34:51 -0500 (Tue, 22 Nov 2016) $
* $LastChangedBy: seth $
* $LastChangedDate: 2016-11-22 23:34:51 -0500 (Tue, 22 Nov 2016) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/StationConfiguration.java $
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

import gnu.io.CommPortIdentifier;
//import oneWireWeather.sensors.WindDirectionSensor;

import java.util.*;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.SwitchContainer;
import com.dalsemi.onewire.utils.OWPath;
//import com.dalsemi.onewire.container.*;
import java.io.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Supply the configuration information for this weather station.
 *
 * Read from a configuration file
 * 
 * @author Seth Cohen
 *
 */
public class StationConfiguration implements Serializable, Cloneable 
{
    
    public static void main(String[] args)
    {
        StationConfiguration config = new StationConfiguration();
        config.load("D:/Users/seth.CHERRYWOOD/Documents/workspace/OneWireWeather/support/weatherConfig.xml");
//        config.save("D:/Users/seth.CHERRYWOOD/Documents/workspace/OneWireWeather/support/weatherConfig.xml");
    }

    private static final long serialVersionUID = 1L;

    // weather data providers
    // 1-Wire
    // Davis (Vantage/Vue)
    
    // 1-Wire weather parameters
    private static final String ONE_WIRE_ENABLED = "onewire.enabled";
    private static final String ADAPTER_NAME = "onewire.adapter.name";
    private static final String ADAPTER_PORT = "onewire.adapter.port";
    private static final String HARDWARE_POLL_INTERVAL = "onewire.hardwarePollInterval";
    
    // Davis specific parameters
    private static final String DAVIS_ENABLED = "davis.enabled";
    private static final String DAVIS_PORT = "davis.port";
    private static final String DAVIS_BAUDRATE = "davis.baudRate";
    private static final String DAVIS_BITS = "davis.bits";
    private static final String DAVIS_STOP = "davis.stop";
    private static final String DAVIS_PARITY = "davis.parity";
    private static final String DAVIS_RETRY_COUNT = "davis.retryCount";
    private static final String DAVIS_OUTDOOR_TEMP_ENABLED = "davis.outdoorTemp.enabled";
    private static final String DAVIS_WIND_SPEED_ENABLED = "davis.windSpeed.enabled";
    private static final String DAVIS_OUTDOOR_HUMIDITY_ENABLED = "davis.outdoorHumidity.enabled";
    private static final String DAVIS_WIND_DIRECTION_ENABLED = "davis.windDirection.enabled";
    private static final String DAVIS_PRESSURE_ENABLED = "davis.pressure.enabled";
    private static final String DAVIS_RAINFALL_ENABLED = "davis.rainfall.enabled";
    private static final String DAVIS_SOLAR_ENABLED = "davis.solar.enabled";
    private static final String DAVIS_LIGHTNING_ENABLED = "davis.lightning.enabled";
    private static final String DAVIS_INDOOR_TEMPERATURE_ENABLED = "davis.indoorTemp.enabled";
    private static final String DAVIS_INDOOR_HUMIDITY_ENABLED = "davis.indoorHumidity.enabled";
    
    // application wide parameters
    private static final String LOG_FILE_NAME = "logFileName";
    private static final String LOG_FILE_SIZE = "logFileSize";
    private static final String WIND_DIRECTION_AVERAGE_PERIOD = "averagePeriod.windDirection";
    private static final String WIND_SPEED_AVERAGE_PERIOD = "averagePeriod.windSpeed";
    private static final String WIND_GUST_INTERVAL = "windGustInterval";
    private static final String SAVE_DATA = "saveData";
    private static final String SAVE_STATS = "saveStats";
    private static final String SAVE_LOG = "saveLog";
    private static final String SERVER_PORT = "serverPort";
    private static final String DATA_FILE_NAME = "datafilename";
    
    // 1-Wire sensor parameters
    private static final String NAME = "name";
    private static final String ID = "ID";
    private static final String DESCRIPTION = "description";
    private static final String GAIN = "gain";
    private static final String OFFSET = "offset";
    private static final String POLL_OFFSET = "poll.offset";
    private static final String POLL_FREQUENCY = "poll.frequency";
    private static final String USAGE_TYPE = "usageType"; 
    
    private static final String SENSOR = "onewire.sensors.sensor";

    // general useage parameters
    private static final String DEBUG_FLAG = "debugFlag"; 
    private static final String ENABLED_FLAG = "enabledFlag"; 
    
    //    private static final String HOME_DIR = "homeDirectory";

    private static final String WU_USER_NAME = "publishers.weatherUnderground.userName";
    private static final String WU_PASSWORD = "publishers.weatherUnderground.password";
    private static final String WU_URL = "publishers.weatherUnderground.URL";
    private static final String WU_INTERVAL = "publishers.weatherUnderground.updateInterval";
    private static final String WU_DEBUG_FLAG = "publishers.weatherUnderground.debugFlag";
    private static final String WU_ENABLE_FLAG = "publishers.weatherUnderground.enableFlag";

    private static final String CWOP_USER_NAME = "publishers.CWOP.userName";
    private static final String CWOP_PASSWORD = "publishers.CWOP.password";
    private static final String CWOP_SERVER = "publishers.CWOP.server";
    private static final String CWOP_INTERVAL = "publishers.CWOP.updateIntervalInMinutes";
    private static final String CWOP_PORT = "publishers.CWOP.port";
    private static final String CWOP_DEBUG_FLAG = "publishers.CWOP.debugFlag";
    private static final String CWOP_ENABLE_FLAG = "publishers.CWOP.enableFlag";
    private static final String CWOP_UPLOAD_MINUTE = "publishers.CWOP.uploadMinute";

    // info for Weather For You publisher
    private static final String WFY_USER_ID = "publishers.weatherForYou.userID";
    private static final String WFY_PASSWORD = "publishers.weatherForYou.password";
    private static final String WFY_URL = "publishers.weatherForYou.URL";
    private static final String WFY_INTERVAL = "publishers.weatherForYou.updateInterval";
    private static final String WFY_DEBUG_FLAG = "publishers.weatherForYou.debugFlag";
    private static final String WFY_ENABLE_FLAG = "publishers.weatherForYou.enableFlag";

    // info for Backyard Weather
    private static final String BYW_PUB_ID = "publishers.backyardWeather.publisherID";
    private static final String BYW_PASSWORD = "publishers.backyardWeather.password";
    private static final String BYW_URL = "publishers.backyardWeather.Url";
    private static final String BYW_INTERVAL = "publishers.backyardWeather.updateInterval";
    private static final String BYW_DEBUG_FLAG = "publishers.backyardWeather.debugFlag";
    private static final String BYW_ENABLE_FLAG = "publishers.backyardWeather.enableFlag";
    private static final String BYW_STATION_NUMBER = "publishers.backyardWeather.stationNumber";
    
    // info for database update
    private static final String DB_INTERVAL = "publishers.database.updateInterval";
    private static final String DB_DEBUG_FLAG = "publishers.database.debugFlag";
    private static final String DB_ENABLE_FLAG = "publishers.database.enableFlag";
    private static final String DB_CONNECT_STRING = "publishers.database.connectString";
    private static final String DB_PERSIST_FLAG = "publishers.database.persistFlag";
    
    // info for site builder program
    private static final String BUILDER_TEMPLATE_FOLDER = "builderTemplateFolder";
    private static final String BUILDER_OUTPUT_FOLDER = "builderOutputFolder";
    private static final String BUILDER_FTP_SERVER = "builderFTPServer";
    private static final String BUILDER_FTP_FOLDER = "builderFTPFolder";
    private static final String BUILDER_FTP_LOGIN = "builderFTPLogin";
    private static final String BUILDER_FTP_PASSWORD = "builderFTPPassword";

    private boolean m_oneWireEnabled = false;
    private String m_adapterName = DEFAULT_ADAPTER_NAME;
    private String m_adapterPortName  = DEFAULT_ADAPTER_PORT_NAME;
    private long m_hardwarePollInterval = DEFAULT_POLL_INTERVAL; // in millis
    private int m_serverPort = DEFAULT_SERVER_PORT;
    private Hashtable<String, SensorConfiguration> sensorList = 
	    new Hashtable<String, SensorConfiguration>();

    // Davis parameters
    private boolean m_davisEnabled = false;
    private boolean m_davisOutdoorTemperatureEnabled = true;
    private boolean m_davisWindSpeedEnabled = true;
    private boolean m_davisOutdoorHumidityEnabled = true;
    private boolean m_davisWindDirectionEnabled = true;
    private boolean m_davisPressureEnabled = true;
    private boolean m_davisRainfallEnabled = true;
    private boolean m_davisSolarEnabled = true;
    private boolean m_davisLightningEnabled = true;
    private boolean m_davisIndoorTemperatureEnabled = true;
    private boolean m_davisIndoorHumidityEnabled = true;
    private String m_davisPort = DEFAULT_DAVIS_PORT;
    private int m_davisBaudRate = DEFAULT_DAVIS_BAUD_RATE;
    private String m_davisParity = DEFAULT_DAVIS_PARITY;
    private int m_davisStopBits = DEFAULT_DAVIS_STOP_BITS;
    private int m_davisBits = DEFAULT_DAVIS_BITS;
    private int m_davisRetryCount = DEFAULT_DAVIS_RETRY_COUNT;
    
    private String m_strLogFileName = DEFAULT_LOG_FILE_NAME;
    private long m_logFileSize = DEFAULT_LOG_FILE_SIZE;
    private String m_weatherHomeDirectory = "";
    private String m_dataFileName = "";
    private boolean m_saveData = true;
    private boolean m_saveStats = true;
    private boolean m_saveLog = true;

    // in use - if adding something new, set default in constructor
    private int m_windSpeedAveragePeriod = DEFAULT_WIND_SPEED_AVERAGE_PERIOD; // in minutes
    private int m_windDirectionAveragePeriod = DEFAULT_WIND_DIRECTION_AVERAGE_PERIOD; // in minutes
    private int m_windGustInterval = DEFAULT_WIND_GUST_INTERVAL; // in minutes

    // weather underground settings
    private String weatherUndergroundUserName = "";
    private String weatherUndergroundPassword = "";
    private String weatherUndergroundUrl = DEFAULT_WEATHER_UNDERGROUND_URL;
    private long weatherUndergroundUpdateInterval = -1;
    private boolean weatherUndergroundDebugFlag = false;
    private boolean weatherUndergroundEnableFlag = false;

    // backyard weather settings
    private String backyardWeatherUserName = "";
    private String backyardWeatherPassword = "";
    private String backyardWeatherUrl = DEFAULT_BACKYARD_WEATHER_URL;
    private long backyardWeatherUpdateInterval = -1;
    private boolean backyardWeatherDebugFlag = false;
    private boolean backyardWeatherEnableFlag = false;
    private String backyardWeatherStationNumber = "";
    
    // Weather for you settings
    private String weatherForYouID = "";
    private String weatherForYouPassword = "";
    private String weatherForYouUrl = DEFAULT_WEATHER_FOR_YOU_URL;
    private long weatherForYouUpdateInterval = -1;
    private boolean weatherForYouDebugFlag = false;
    private boolean weatherForYouEnableFlag = false;
    
    // CWOP settings
    private String CWOPUserName = "";
    private String CWOPPassword = "";
    private String CWOPServer = DEFAULT_CWOP_SERVER;
    private long CWOPUpdateIntervalInMinutes = DEFAULT_CWOP_UPDATE_INTERVAL;
    private long CWOPPort = DEFAULT_CWOP_PORT;
    private boolean CWOPDebugFlag = false;
    private boolean CWOPEnableFlag = false;
    private long CWOPUploadMinute = 7;

    // builder settings
    private String builderTemplateFolder;
    private String builderOutputFolder;
    private String builderFTPServer;
    private String builderFTPFolder;
    private String builderFTPLogin;
    private String builderFTPPassword;

    // database settings
    private boolean DatabaseDebugFlag = false;
    private boolean DatabaseEnableFlag = false;
    private long DatabaseUpdateInterval = 60000;
    private String JDBCConnectString = "";
    private boolean persistDBConnection = false;
    
    // default settings
    private static final String DEFAULT_LOG_FILE_NAME = "logfile.log";
    private static final int DEFAULT_LOG_FILE_SIZE = 1440; // number of records
    private static final int DEFAULT_POLL_INTERVAL = 2000; // millis
    private static final int DEFAULT_WIND_SPEED_AVERAGE_PERIOD = 1; // minutes
    private static final int DEFAULT_WIND_DIRECTION_AVERAGE_PERIOD = 5; // minutes
    private static final int DEFAULT_WIND_GUST_INTERVAL = 10; // minutes 
    private static final int DEFAULT_SERVER_PORT = 8189;
    private static final String DEFAULT_ADAPTER_NAME = "DS9097U";
    private static final String DEFAULT_ADAPTER_PORT_NAME = "/dev/ttyUSB0";
    private static final String DEFAULT_WEATHER_UNDERGROUND_URL = "rtupdate.wunderground.com";
    private static final String DEFAULT_BACKYARD_WEATHER_URL = "data.backyard2.weatherbug.com";
    private static final String DEFAULT_WEATHER_FOR_YOU_URL = "www.hamweather.net";
    private static final String DEFAULT_CWOP_SERVER = "rotate.aprs.net";
    private static final int DEFAULT_CWOP_PORT = 14580;
    private static final int DEFAULT_CWOP_UPDATE_INTERVAL = 10;
    private static final String DEFAULT_DAVIS_PORT = "COM1";
    private static final int DEFAULT_DAVIS_BAUD_RATE = 19200;
    private static final String DEFAULT_DAVIS_PARITY = "N";
    private static final int DEFAULT_DAVIS_STOP_BITS = 1;
    private static final int DEFAULT_DAVIS_BITS = 8;
    private static final int DEFAULT_DAVIS_RETRY_COUNT = 3;
    
    /**
     * Constructor to create a new StationConfiguration and initialize
     * the members with default values. 
     */
    public StationConfiguration()
    {
	// set defaults

	//        try 
	//        {
	//            m_adapterName = OneWireAccessProvider.getDefaultAdapter().getAdapterName();
	//            m_adapterPortName = OneWireAccessProvider.getDefaultAdapter().getPortName();
	//        } catch (OneWireIOException e) {
	//            System.err.println("I/O Error getting default adapter: " + e);
	//        } catch (OneWireException e) {
	//            System.err.println("1-Wire Error getting default adapter: " + e);
	//        }
	m_strLogFileName = DEFAULT_LOG_FILE_NAME;
	m_logFileSize = DEFAULT_LOG_FILE_SIZE; // # records
	m_hardwarePollInterval = DEFAULT_POLL_INTERVAL; // millis
	m_windSpeedAveragePeriod = DEFAULT_WIND_SPEED_AVERAGE_PERIOD; // minutes
	m_windDirectionAveragePeriod = DEFAULT_WIND_DIRECTION_AVERAGE_PERIOD; // minutes
	m_windGustInterval = DEFAULT_WIND_GUST_INTERVAL; // minutes
	m_serverPort = DEFAULT_SERVER_PORT;
    }

//    @SuppressWarnings("unchecked")
    private void loadConfig(XMLConfiguration config)
    {
	// file loaded - retrieve the properties
        this.setOneWireEnabled(config.getBoolean(ONE_WIRE_ENABLED, false));
	this.setAdapterName(config.getString(ADAPTER_NAME));
	this.setAdapterPortName(config.getString(ADAPTER_PORT));
	this.setHardwarePollInterval(config.getLong(HARDWARE_POLL_INTERVAL));
	this.setLogFileName(config.getString(LOG_FILE_NAME));
	this.setLogFileSize(config.getInt(LOG_FILE_SIZE));
	this.setWindDirectionAveragePeriod(config.getInt(WIND_DIRECTION_AVERAGE_PERIOD));
	this.setWindSpeedAveragePeriod(config.getInt(WIND_SPEED_AVERAGE_PERIOD));
	this.setWindGustInterval(config.getInt(WIND_GUST_INTERVAL));
	this.setServerPort(config.getInt(SERVER_PORT));
	//        this.m_weatherHomeDirectory = config.getString(HOME_DIR);
	this.setDataFileName(config.getString(DATA_FILE_NAME));
        this.setSaveData(config.getBoolean(SAVE_DATA, true));
        this.setSaveStats(config.getBoolean(SAVE_STATS, true));
        this.setSaveLog(config.getBoolean(SAVE_LOG, true));

        // Davis parameters
        this.setDavisEnabled(config.getBoolean(DAVIS_ENABLED, false));
        this.setDavisIndoorHumidityEnabled(config.getBoolean(DAVIS_INDOOR_HUMIDITY_ENABLED, true));
        this.setDavisIndoorTemperatureEnabled(config.getBoolean(DAVIS_INDOOR_TEMPERATURE_ENABLED, true));
        this.setDavisLightningEnabled(config.getBoolean(DAVIS_LIGHTNING_ENABLED, true));
        this.setDavisOutdoorHumidityEnabled(config.getBoolean(DAVIS_OUTDOOR_HUMIDITY_ENABLED, true));
        this.setDavisOutdoorTemperatureEnabled(config.getBoolean(DAVIS_OUTDOOR_TEMP_ENABLED, true));
        this.setDavisPressureEnabled(config.getBoolean(DAVIS_PRESSURE_ENABLED, true));
        this.setDavisRainfallEnabled(config.getBoolean(DAVIS_RAINFALL_ENABLED, true));
        this.setDavisSolarEnabled(config.getBoolean(DAVIS_SOLAR_ENABLED, true));
        this.setDavisWindDirectionEnabled(config.getBoolean(DAVIS_WIND_DIRECTION_ENABLED, true));
        this.setDavisWindSpeedEnabled(config.getBoolean(DAVIS_WIND_SPEED_ENABLED, true));
        this.setDavisComPort(config.getString(DAVIS_PORT, DEFAULT_DAVIS_PORT));
        this.setDavisBaudRate(config.getInt(DAVIS_BAUDRATE, DEFAULT_DAVIS_BAUD_RATE));
        this.setDavisBits(config.getInt(DAVIS_BITS, DEFAULT_DAVIS_BITS));
        this.setDavisParity(config.getString(DAVIS_PARITY, DEFAULT_DAVIS_PARITY));
        this.setDavisStopBits(config.getInt(DAVIS_STOP, DEFAULT_DAVIS_STOP_BITS));
        this.setDavisRetryCount(config.getInt(DAVIS_RETRY_COUNT, DEFAULT_DAVIS_RETRY_COUNT));
        
	this.setWeatherUndergroundUserName(config.getString(WU_USER_NAME));
	this.setWeatherUndergroundPassword(config.getString(WU_PASSWORD));
	this.setWeatherUndergroundUrl(config.getString(WU_URL));
	this.setWeatherUndergroundUpdateInterval(config.getLong(WU_INTERVAL));
	this.setWeatherUndergroundDebug(config.getBoolean(WU_DEBUG_FLAG));
	this.setWeatherUndergroundEnable(config.getBoolean(WU_ENABLE_FLAG));

	this.setWeatherForYouID(config.getString(WFY_USER_ID));
	this.setWeatherForYouPassword(config.getString(WFY_PASSWORD));
	this.setWeatherForYouUrl(config.getString(WFY_URL));
	this.setWeatherForYouUpdateInterval(config.getLong(WFY_INTERVAL));
	this.setWeatherForYouDebugFlag(config.getBoolean(WFY_DEBUG_FLAG));
	this.setWeatherForYouEnableFlag(config.getBoolean(WFY_ENABLE_FLAG));
	
	this.setBackyardWeatherPubID(config.getString(BYW_PUB_ID));
	this.setBackyardWeatherPassword(config.getString(BYW_PASSWORD));
	this.setBackyardWeatherUrl(config.getString(BYW_URL));
	this.setBackyardWeatherUpdateInterval(config.getLong(BYW_INTERVAL));
	this.setBackyardWeatherDebugFlag(config.getBoolean(BYW_DEBUG_FLAG));
	this.setBackyardWeatherEnableFlag(config.getBoolean(BYW_ENABLE_FLAG));
	this.setBackyardWeatherStationNumber(config.getString(BYW_STATION_NUMBER));
	
	this.setCWOPUserName(config.getString(CWOP_USER_NAME));
	this.setCWOPPassword(config.getString(CWOP_PASSWORD));
	this.setCWOPServer(config.getString(CWOP_SERVER));
	this.setCWOPUpdateIntervalInMinutes(config.getLong(CWOP_INTERVAL));
	this.setCWOPPort(config.getLong(CWOP_PORT));
	this.setCWOPDebug(config.getBoolean(CWOP_DEBUG_FLAG));
	this.setCWOPEnable(config.getBoolean(CWOP_ENABLE_FLAG));
	try
	{
	    this.setCWOPUploadMinute(config.getLong(CWOP_UPLOAD_MINUTE));
	}
	catch (Exception ex)
	{
	    this.setCWOPUploadMinute(-1);
	}

	this.setDatabaseDebugFlag(config.getBoolean(DB_DEBUG_FLAG, false));
	this.setDatabaseEnableFlag(config.getBoolean(DB_ENABLE_FLAG, false));
	this.setDatabaseUpdateInterval(config.getLong(DB_INTERVAL, -1));
	this.setJDBCConnectString(config.getString(DB_CONNECT_STRING, ""));
        this.setDatabasePersistFlag(config.getBoolean(DB_PERSIST_FLAG, false));
	
	this.setBuilderFTPFolder(config.getString(BUILDER_FTP_FOLDER));
	this.setBuilderFTPLogin(config.getString(BUILDER_FTP_LOGIN));
	this.setBuilderFTPPassword(config.getString(BUILDER_FTP_PASSWORD));
	this.setBuilderFTPServer(config.getString(BUILDER_FTP_SERVER));
	this.setBuilderOutputFolder(config.getString(BUILDER_OUTPUT_FOLDER));
	this.setBuilderTemplateFolder(config.getString(BUILDER_TEMPLATE_FOLDER));

	// load the sensor list
	List<HierarchicalConfiguration> sensors = (List<HierarchicalConfiguration>) config.configurationsAt(SENSOR);
	for (Iterator<HierarchicalConfiguration> it = sensors.iterator(); it.hasNext();)
	{
	    HierarchicalConfiguration sensor = it.next();
	    SensorConfiguration sensorConfig = 
		    new SensorConfiguration(sensor.getString(NAME), 
			    sensor.getString(DESCRIPTION), sensor.getString(ID), 
			    WeatherStation.getSensorTypeForUsage(sensor.getInt(USAGE_TYPE)));
	    sensorConfig.setGain(sensor.getFloat(GAIN));
	    sensorConfig.setOffset(sensor.getFloat(OFFSET));
	    sensorConfig.setPollFrequency(sensor.getInt(POLL_FREQUENCY));
	    sensorConfig.setPollOffset(sensor.getInt(POLL_OFFSET));
	    sensorConfig.setDebugFlag(sensor.getBoolean(DEBUG_FLAG));
	    sensorConfig.setEnabled(sensor.getBoolean(ENABLED_FLAG, true));

	    this.addSensorToConfiguration(sensorConfig);

	    ErrorLog.logError("Read sensor from config file: " + sensorConfig.getName() + " ID " + sensorConfig.getID() + " " +
	    		"debug " + sensorConfig.isDebugFlag() + " enabled " + sensorConfig.isEnabled());
	}
    }

    public void load(Reader in)
    {
	ErrorLog.logError("Loading configuration");
	
	// try and load the config file
	try 
	{
	    XMLConfiguration config = new XMLConfiguration();
	    config.load(in);
	    loadConfig(config);
	} 
	catch (ConfigurationException e) 
	{
	    // file not found - go with default configuration
	    ErrorLog.logError("No configuration file found: loading defaults");
	}
    }

    public void load(String configFileName)
    {
	ErrorLog.logError("Loading configuration from: " + configFileName);
	
	// try and load the config file
	try 
	{
	    XMLConfiguration config = new XMLConfiguration(configFileName);
	    loadConfig(config);
	} 
	catch (ConfigurationException e) 
	{
	    // file not found - go with default configuration
	    ErrorLog.logError("No configuration file found: loading defaults");
	    System.out.println("file " + configFileName + " not found");
	}
    }

    private void saveConfigProperties(XMLConfiguration config)
    {
        config.addProperty(ONE_WIRE_ENABLED, this.isOneWireEnabled());
	config.addProperty(ADAPTER_NAME, this.getAdapterName());
	config.addProperty(ADAPTER_PORT, this.getAdapterPortName());
	config.addProperty(HARDWARE_POLL_INTERVAL, this.getHardwarePollInterval());
	config.addProperty(LOG_FILE_NAME, this.getLogFileName());
	config.addProperty(LOG_FILE_SIZE, this.getLogFileSize());
	config.addProperty(WIND_DIRECTION_AVERAGE_PERIOD, this.getWindDirectionAveragePeriod());
	config.addProperty(WIND_SPEED_AVERAGE_PERIOD, this.getWindSpeedAveragePeriod());
	config.addProperty(WIND_GUST_INTERVAL, this.getWindGustInterval());
	config.addProperty(SERVER_PORT, this.getServerPort());
	//        config.addProperty(HOME_DIR, this.m_weatherHomeDirectory);
	config.addProperty(DATA_FILE_NAME, this.getDataFileName());
        config.addProperty(SAVE_DATA, this.isSaveData());
        config.addProperty(SAVE_STATS, this.isSaveStats());
        config.addProperty(SAVE_LOG, this.isSaveLog());

        // Davis parameters
        config.addProperty(DAVIS_ENABLED, this.isDavisEnabled());

        config.addProperty(DAVIS_INDOOR_HUMIDITY_ENABLED, this.isDavisIndoorHumidityEnabled());
        config.addProperty(DAVIS_INDOOR_TEMPERATURE_ENABLED, this.isDavisIndoorTemperatureEnabled());
        config.addProperty(DAVIS_LIGHTNING_ENABLED, this.isDavisLightningEnabled());
        config.addProperty(DAVIS_OUTDOOR_HUMIDITY_ENABLED, this.isDavisOutdoorHumidityEnabled());
        config.addProperty(DAVIS_OUTDOOR_TEMP_ENABLED, this.isDavisOutdoorTemperatureEnabled());
        config.addProperty(DAVIS_PRESSURE_ENABLED, this.isDavisPressureEnabled());
        config.addProperty(DAVIS_RAINFALL_ENABLED, this.isDavisRainfallEnabled());
        config.addProperty(DAVIS_SOLAR_ENABLED, this.isDavisSolarEnabled());
        config.addProperty(DAVIS_WIND_DIRECTION_ENABLED, this.isDavisWindDirectionEnabled());
        config.addProperty(DAVIS_WIND_SPEED_ENABLED, this.isDavisWindSpeedEnabled());

        config.addProperty(DAVIS_PORT, this.getDavisComPort());
        config.addProperty(DAVIS_BAUDRATE, this.getDavisBaudRate());
        config.addProperty(DAVIS_BITS, this.getDavisBits());
        config.addProperty(DAVIS_PARITY, this.getDavisParity());
        config.addProperty(DAVIS_STOP, this.getDavisStopBits());
        config.addProperty(DAVIS_RETRY_COUNT, this.getDavisRetryCount());
        
	config.addProperty(WU_USER_NAME, this.getWeatherUndergroundUserName());
	config.addProperty(WU_PASSWORD, this.getWeatherUndergroundPassword());
	config.addProperty(WU_URL, this.getWeatherUndergroundUrl());
	config.addProperty(WU_INTERVAL, this.getWeatherUndergroundUpdateInterval());
	config.addProperty(WU_DEBUG_FLAG, this.isWeatherUndergroundDebug());
	config.addProperty(WU_ENABLE_FLAG, this.isWeatherUndergroundEnable());

	config.addProperty(WFY_USER_ID, this.getWeatherForYouID());
	config.addProperty(WFY_PASSWORD, this.getWeatherForYouPassword());
	config.addProperty(WFY_URL, this.getWeatherForYouUrl());
	config.addProperty(WFY_INTERVAL, this.getWeatherForYouUpdateInterval());
	config.addProperty(WFY_DEBUG_FLAG, this.isWeatherForYouDebugFlag());
	config.addProperty(WFY_ENABLE_FLAG, this.isWeatherForYouEnableFlag());

	config.addProperty(CWOP_USER_NAME, this.getCWOPUserName());
	config.addProperty(CWOP_PASSWORD, this.getCWOPPassword());
	config.addProperty(CWOP_SERVER, this.getCWOPServer());
	config.addProperty(CWOP_INTERVAL, this.getCWOPUpdateIntervalInMinutes());
	config.addProperty(CWOP_PORT, this.getCWOPPort());
	config.addProperty(CWOP_DEBUG_FLAG, this.isCWOPDebug());
	config.addProperty(CWOP_ENABLE_FLAG, this.isCWOPEnable());
	config.addProperty(CWOP_UPLOAD_MINUTE, this.getCWOPUploadMinute());

	config.addProperty(BYW_PUB_ID, this.getBackyardWeatherPubId());
	config.addProperty(BYW_PASSWORD, this.getBackyardWeatherPassword());
	config.addProperty(BYW_URL, this.getBackyardWeatherUrl());
	config.addProperty(BYW_INTERVAL, this.getBackyardWeatherUpdateInterval());
	config.addProperty(BYW_DEBUG_FLAG, this.isBackyardWeatherDebugFlag());
	config.addProperty(BYW_ENABLE_FLAG, this.isBackyardWeatherEnableFlag());
	config.addProperty(BYW_STATION_NUMBER, this.getBackyardWeatherStationNumber());

	// database properties
	config.addProperty(DB_DEBUG_FLAG, this.isDatabaseDebugFlag());
	config.addProperty(DB_ENABLE_FLAG, this.isDatabaseEnableFlag());
	config.addProperty(DB_INTERVAL, this.getDatabaseUpdateInterval());
	config.addProperty(DB_CONNECT_STRING, this.getJDBCConnectString());
        config.addProperty(DB_PERSIST_FLAG, this.isPersistDBConnection());
	
	// builder properties
	config.addProperty(BUILDER_FTP_FOLDER, this.getBuilderFTPFolder());
	config.addProperty(BUILDER_FTP_LOGIN, this.getBuilderFTPLogin());
	config.addProperty(BUILDER_FTP_PASSWORD, this.getBuilderFTPPassword());
	config.addProperty(BUILDER_FTP_SERVER, this.getBuilderFTPServer());
	config.addProperty(BUILDER_OUTPUT_FOLDER, this.getBuilderOutputFolder());
	config.addProperty(BUILDER_TEMPLATE_FOLDER, this.getBuilderTemplateFolder());

	// add sensors
	for (Enumeration<SensorConfiguration> e = getSensorList().elements(); e.hasMoreElements() ;) 
	{
	    SensorConfiguration sensor = e.nextElement();
	    
	    config.addProperty(SENSOR + "(-1)." + ID, sensor.getID());
	    config.addProperty(SENSOR + "." + USAGE_TYPE, sensor.getUsageType());
	    config.addProperty(SENSOR + "." + GAIN, sensor.getGain());
	    config.addProperty(SENSOR + "." + OFFSET, sensor.getOffset());
	    config.addProperty(SENSOR + "." + POLL_FREQUENCY, sensor.getPollFrequency());
	    config.addProperty(SENSOR + "." + POLL_OFFSET, sensor.getPollOffset());
	    config.addProperty(SENSOR + "." + NAME, sensor.getName());
	    config.addProperty(SENSOR + "." + DESCRIPTION, sensor.getDescription());
	    config.addProperty(SENSOR + "." + DEBUG_FLAG, sensor.isDebugFlag());
	    config.addProperty(SENSOR + "." + ENABLED_FLAG, sensor.isEnabled());
	}

//	SensorConfiguration[] entries = this.getSensorList();
//	for (int i = 0; i < entries.length; i++)
//	{
//	    config.addProperty(SENSOR + "(-1)." + ID, entries[i].getID());
//	    config.addProperty(SENSOR + "." + USAGE_TYPE, entries[i].getUsageType());
//	    config.addProperty(SENSOR + "." + GAIN, entries[i].getGain());
//	    config.addProperty(SENSOR + "." + OFFSET, entries[i].getOffset());
//	    config.addProperty(SENSOR + "." + POLL_FREQUENCY, entries[i].getPollFrequency());
//	    config.addProperty(SENSOR + "." + POLL_OFFSET, entries[i].getPollOffset());
//	    config.addProperty(SENSOR + "." + NAME, entries[i].getName());
//	    config.addProperty(SENSOR + "." + DESCRIPTION, entries[i].getDescription());
//	    config.addProperty(SENSOR + "." + DEBUG_FLAG, entries[i].isDebugFlag());
//	    config.addProperty(SENSOR + "." + ENABLED_FLAG, entries[i].isEnabled());
//	}
    }

    public void save(String configFileName)
    {
	XMLConfiguration config;
	try 
	{
	    config = new XMLConfiguration();
	    saveConfigProperties(config);
	    config.save(configFileName);
	} 
	catch (ConfigurationException e) 
	{
	    e.printStackTrace();
	}
    }

    public void save(OutputStream out)
    {
	XMLConfiguration config = new XMLConfiguration();
	saveConfigProperties(config);
	try 
	{
	    config.save(out);
	} 
	catch (ConfigurationException e) 
	{
	    e.printStackTrace();
	}
    }

    /**
     * One wire adapter discovery.  Find all the potential 1-wire
     * adapters and return them as in a list.
     *
     * @return return a list of potential adapters as an array of strings.
     */
    @SuppressWarnings("unchecked")
    public static String[] listAdapters()
    {
	String[] a = new String[1];

	Enumeration<DSPortAdapter> adapters = (Enumeration<DSPortAdapter>) OneWireAccessProvider.enumerateAllAdapters();
	ArrayList<String> adapterNames = new ArrayList<String>();
	while(adapters.hasMoreElements())
	{
	    DSPortAdapter adapter = adapters.nextElement();
	    String adapterName = adapter.getAdapterName();
	    adapterNames.add(adapterName);
	}

	return adapterNames.toArray(a);
	
    }
    
    public static String[] listPorts()
    {
	CommPortIdentifier commPort;

	String[] a = new String[1];

	Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
	ArrayList<String> portNames = new ArrayList<String>();

	while (ports.hasMoreElements())
	{
		commPort = (CommPortIdentifier)(ports.nextElement());
		
		if (commPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
		{
			String portName = commPort.getName();
			
			if (commPort.getCurrentOwner() != null)
				portName += " **BUSY**";
			
			portNames.add(portName);
		}
	}
	
	return portNames.toArray(a);
    }
    
    public static String getAdapterInfo(String adapterName, String port)
    {
	DSPortAdapter adapter = findAdapterByName(adapterName);
	try
	{
	    adapter.selectPort(port);
	}
	catch (OneWireIOException e1)
	{
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	catch (OneWireException e1)
	{
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	StringBuilder builder = new StringBuilder();
	
	if (adapter != null)
	{
	    builder.append("Port type: " + adapter.getPortTypeDescription() + "\n");
	    builder.append("Class version: " + adapter.getClassVersion() + "\n");
	    try
	    {
		builder.append("Adapter name: " + adapter.getAdapterName() + "\n");
		builder.append("Adapter version: " + adapter.getAdapterVersion() + "\n");
		builder.append("Adapter address: " + adapter.getAdapterAddress() + "\n");
		builder.append("Can break: " + adapter.canBreak() + "\n");
		builder.append("Can deliver power: " + adapter.canDeliverPower() + "\n");
		builder.append("Can deliver smart power: " + adapter.canDeliverSmartPower() + "\n");
		builder.append("Can flex: " + adapter.canFlex() + "\n");
		builder.append("Can hyperdrive: " + adapter.canHyperdrive() + "\n");
		builder.append("Can overdrive: " + adapter.canOverdrive() + "\n");
		builder.append("Can program: " + adapter.canProgram() + "\n");
		builder.append("Speed: " + adapter.getSpeed());

		adapter.freePort();
	    }
	    catch (OneWireIOException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
		System.err.println("Caught one wire I/O exception " + e.getMessage());
	    }
	    catch (OneWireException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
		System.err.println("Caught one wire exception " + e.getMessage());
	    }
	}
	
	return builder.toString();
    }

    /**
     * return a list of port names for a given adapter
     * 
     * @param adapterName  The port list will be returned for this adapter 
     * @return             The list of ports as an array of strings
     */
    @SuppressWarnings("unchecked")
    public static String[] listPorts(String adapterName)
    {
	ArrayList<String> portNames;
	String[] a;
	
	try
	{
	    DSPortAdapter adapter = findAdapterByName(adapterName);
	    
	    portNames = new ArrayList<String>();
	    a = new String[1];

	    if (adapter != null )
	    {
	        Enumeration<String> ports = (Enumeration<String>) adapter.getPortNames();
	        while (ports.hasMoreElements())
	        {
	            String portName = ports.nextElement();
        //	    System.out.println("Debug: " + portName);
        	    portNames.add(portName);
	        }
	    }
	    else
	    {
	        System.out.println("Debug: Adapter not found in listPorts() " + adapterName);
	    }
	}
	catch (Exception e)
	{
	    portNames = new ArrayList<String>();
	    a = new String[1];
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return portNames.toArray(a);
    }

    public static Hashtable<String, OWPath> walkNet(String adapterName, String port)
    {
	// get the adapter
	DSPortAdapter adapter;
	Hashtable<String, OWPath> result = null;
	
	try
	{
	    adapter = OneWireAccessProvider.getAdapter(adapterName, port);
	    result = walkNet(adapter);
//	    adapter.freePort();
		
	}
	catch (OneWireIOException e)
	{
	    return null;
	}
	catch (OneWireException e)
	{
	    return null;
	}
	
	return result;
	
    }
    
    public static Hashtable<String, OWPath> walkNet(DSPortAdapter adapter)
    {
	// vector for holding a list of paths to be searched
	Vector<OWPath> pathsToSearch = new Vector<OWPath>();
	boolean searchResult = false;

	// hash table for holding the OWPath objects for each device container.
	Hashtable<String, OWPath> devicePathHash = new Hashtable<String, OWPath>();

	try
	{
	    // seed list with main branch
	    pathsToSearch.addElement(new OWPath(adapter));

	    // acquire the adapter
	    adapter.beginExclusive(true);

	    // setup the search
	    adapter.setSearchAllDevices();
	    adapter.targetAllFamilies();
	    adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);

	    // walk path and get all devices on branch.
	    // if any switches are found, add them to the list of paths to search
	    // search through all of the paths
	    for (int i = 0; i < pathsToSearch.size(); i++)
	    {
		// set searches to not use reset
		adapter.setNoResetSearch();

		// get the next path to search and open it
		OWPath path = (OWPath)pathsToSearch.elementAt(i);

		try
		{
		    // try to open the current path
		    path.open();
		}
		catch(Exception e)
		{
		    // if opening the path failed, log an error and continue on to the next path
		    ErrorLog.logError("walkNet(): Error opening path " + path.toString());
		    continue;
		}

		// get the devices on the currently open path
		searchResult = adapter.findFirstDevice();

		// loop while devices found
		while (searchResult)
		{
		    // get the 1-Wire address
		    String address = adapter.getAddressAsString();

		    // check if the device already exists in the hash table of addresses
		    if (!devicePathHash.containsKey(address))
		    {
			OneWireContainer owc = adapter.getDeviceContainer(address);

			// check to see if it's a switch.  If so, add it to the paths to be searched
			//	if we haven't already searched it
			if (owc instanceof SwitchContainer) 
			{
			    SwitchContainer sc = (SwitchContainer)owc;
			    byte[] state = sc.readDevice();
			    for (int j = 0; j < sc.getNumberChannels(state); j++)
			    {
				OWPath tmp = new OWPath(adapter, path);
				tmp.add(owc, j);
				if (!pathsToSearch.contains(tmp))
				{
				    pathsToSearch.addElement(tmp);
				}
			    }
			}

			// save off the address and path
			synchronized(devicePathHash)
			{
			    devicePathHash.put(address, path);
			}
		    }
		    // check if the existing device moved
		    else if (!path.equals((OWPath)devicePathHash.get(address)))
		    {
			// if it has moved, add the new address/path pair
			synchronized(devicePathHash)
			{
			    devicePathHash.put(address, path);
			}
		    }

		    // find the next device on this branch
		    path.open();
		    searchResult = adapter.findNextDevice();
		}

		path.close();
	    }

	    // clean up after ourselves
	    adapter.setSearchAllDevices();
	    adapter.endExclusive();
	}
	catch (OneWireException e)
	{
	    ErrorLog.logError("Error Serching the 1-Wire Bus");
	}

	return devicePathHash;

    }
    
    public static OneWireContainer getContainer(String adapterName, String port, String address, OWPath path)
    {
	// get the adapter
	DSPortAdapter adapter;
	OneWireContainer owc = null;
	
	try
	{
	    adapter = OneWireAccessProvider.getAdapter(adapterName, port);
	    path.open();
	    owc = adapter.getDeviceContainer(address);
	    path.close();
//	    adapter.freePort();
	}
	catch (OneWireIOException e)
	{
	    System.out.println("Exception: " + e.getMessage());
	    return null;
	}
	catch (OneWireException e)
	{
	    System.out.println("Exception: " + e.getMessage());
	    return null;
	}
	
	return owc;
    }


//    /**
//     * List all the 1-wire devices found on a given adapter and port.
//     * 
//     * @param adapterName  The adapter to search
//     * @param portName     The adapter port to search
//     * @return             The list of 1-wire IDs found as an array of strings
//     */
//    @SuppressWarnings("unchecked")
//    public synchronized static String[] listDevices(String adapterName, String portName)
//    {
//	DSPortAdapter adapter = null;
//	ArrayList<String> deviceIDs = new ArrayList<String>();
//	String[] a = new String[1];
//
//	try
//	{
//	    adapter = OneWireAccessProvider.getAdapter(adapterName, portName);
//	}
//	catch( OneWireException ex)
//	{
//	    System.out.println("1-Wire exception getting adapter: " + ex);
//	}
//
//	try
//	{
//	    if (adapter != null && adapter.adapterDetected())
//	    {
//		adapter.beginExclusive(true);
//		Enumeration<OneWireContainer> devices = (Enumeration<OneWireContainer>) adapter.getAllDeviceContainers();
//
//		while(devices.hasMoreElements())
//		{
//		    OneWireContainer container = devices.nextElement();
//		    deviceIDs.add(container.getAddressAsString());
//		}
//		adapter.reset();
//	    }
//	}
//	catch(OneWireIOException ex)
//	{
//	    System.out.println("1-Wire IO Exception in listDevices(): " + ex);
//	}
//	catch(OneWireException ex)
//	{
//	    System.out.println("1-Wire Exception in listDeices(): " + ex);
//	}
//	finally
//	{
//	    if (adapter != null )
//		adapter.endExclusive();
//	}
//
//	return deviceIDs.toArray(a);
//    }

    /**
     * Find an adapter with the given name.
     * 
     * @param adapterName  The name of the adapter to search for
     * @return             The adapter or nul if not found
     */
    @SuppressWarnings("unchecked")
    private static DSPortAdapter findAdapterByName(String adapterName)
    {
	Enumeration<DSPortAdapter> adapters = (Enumeration<DSPortAdapter>) OneWireAccessProvider.enumerateAllAdapters();
	boolean found = false;
	DSPortAdapter adapter = null;

	while( adapters.hasMoreElements() && !found)
	{
	    adapter = adapters.nextElement();
	    if (adapter.getAdapterName().equalsIgnoreCase(adapterName))
	    {
		return adapter;
	    }
	}

	return null;
    }

    /*
     * One wire sensor discovery
     */
    // return a list of discovered ID's for a given adapter
    // return a list of discovered ID's for a given adapter of a given family
    // return a list of discovered ID's for a given adapter that aren't configured 
    // return a list of discovered ID's for a given adapter that aren't configured for a given family 

    /**
     * Return the configured adapter name
     * 
     * @return The adapter name as a string
     */
    public String getAdapterName()
    {
	return m_adapterName;
    }

    /**
     * Set the configured adapter name.
     * 
     * @param name The adapter name
     */
    public void setAdapterName(String name)
    {
	m_adapterName = name;
    }

    /**
     * Return the configured adapter port name
     * 
     * @return The adapter port name
     */
    public String getAdapterPortName()
    {
	return m_adapterPortName;
    }

    /**
     * Set this configuration's adapter port.
     * 
     * @param name The port name to set
     */
    public void setAdapterPortName(String name)
    {
	m_adapterPortName = name;
    }

    /**
     * Return this configuration's hardware poll interval.  This is how frequently
     * the hardware acquisition thread will poll the configured hardware sensors.
     * 
     * @return The hardware poll interval in milliseconds
     */
    public long getHardwarePollInterval()
    {
	return m_hardwarePollInterval;
    }

    /**
     * Set the hardware poll interval.  This is how frequently
     * the hardware acquisition thread will poll the configured hardware sensors.
     * 
     * @param interval The new hardware poll interval in milliseconds
     */
    public void setHardwarePollInterval(long interval)
    {
	m_hardwarePollInterval = interval;
    }

    /**
     * Empty the list of sensors for this configuration.
     */
    public void clearSensorList()
    {
	sensorList.clear();
    }

    /**
     * Get the list of configured sensors.
     * 
     * @return The list of configured sensors.
     */
    public Hashtable<String, SensorConfiguration> getSensorList()
    {
	return sensorList;
    }

    /**
     * For a given 1-wire ID, find and return the corresponding sensor
     * from the list of configured sensors.
     * 
     * @param ID   The 1-wire ID to search for
     * @return     The corresponding sensor configuration or null if not found
     */
    public SensorConfiguration findSensorByID(String ID)
    {
	return sensorList.get(ID);
    }

    /**
     * Add a sensor to the list of configured sensors.
     * 
     * @param config   The sensor configuration of the sensor to add
     */
    public void addSensorToConfiguration(SensorConfiguration config)
    {
	sensorList.put(config.getID(), config);
    }

    /**
     * Remove the sensor with the specified 1-wire ID from the list of
     * configured sensors.
     * 
     * @param ID   The 1-wire ID of the sensor to remove
     */
    public void removeSensorFromConfiguration(String ID)
    {
	if (sensorList.containsKey(ID))
	{
	    sensorList.remove(ID);
	}
    }

    /**
     * Remove the specified sensor from the list of configured sensors.
     * 
     * @param config   The SensorConfiguration of the sensor to remove
     */
    public void removeSensorFromConfiguration(SensorConfiguration config)
    {
	removeSensorFromConfiguration(config.getID());
    }

    /**
     * Return the name fo the log file
     * 
     * @return The log file name as a string
     */
    public String getLogFileName()
    {
	return m_strLogFileName;
    }

    /**
     * Set the name of the log file
     * 
     * @param name The new name for the log file
     */
    public void setLogFileName(String name)
    {
	m_strLogFileName = name;
    }

    /**
     * Return the log file max size in number of records
     * 
     * @return The log file max size as number of records
     */
    public long getLogFileSize()
    {
	return m_logFileSize;
    }

    /**
     * Set the max log file size.
     * 
     * @param size The max log filer size as a number of records
     */
    public void setLogFileSize(long size)
    {
	m_logFileSize = size;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public synchronized Object clone()
    {
	StationConfiguration config = new StationConfiguration();

	// copy the fields
	config.setAdapterName(this.getAdapterName());
	config.setAdapterPortName(this.getAdapterPortName());
	config.setHardwarePollInterval(this.getHardwarePollInterval());
	config.setLogFileName(this.getLogFileName());
	config.setLogFileSize(this.getLogFileSize());
	config.setWindDirectionAveragePeriod(this.getWindDirectionAveragePeriod());
	config.setWindSpeedAveragePeriod(this.getWindSpeedAveragePeriod());

	// copy sensorlist
//	SensorConfiguration[] sensors = this.getSensorList();
//	Hashtable<String, SensorConfiguration> sensors = this.getSensorList();
	for (Enumeration<SensorConfiguration> e = getSensorList().elements(); e.hasMoreElements() ;) 
	{
	    SensorConfiguration sensor = e.nextElement();
	    config.addSensorToConfiguration(sensor);
	}
	
//	for (int i = 0; i < sensorList.size(); i++)
//	{
//	    config.addSensorToConfiguration(sensors[i]);
//	}

	return config;
    }

    public int getServerPort()
    {
	return m_serverPort;
    }

    public void setServerPort(int port)
    {
	m_serverPort = port;
    }

    /**
     * Return the sample size used when computing the average wind direction in minutes
     * 
     * @return the sample size in minutes
     */
    public int getWindDirectionAveragePeriod() {
	return m_windDirectionAveragePeriod;
    }

    // used
    /**
     * Set the samples size (in minutes) used when computing the average wind direction
     * 
     * @param directionAveragePeriod the sample size to set
     */
    public void setWindDirectionAveragePeriod(int directionAveragePeriod) {
	m_windDirectionAveragePeriod = directionAveragePeriod;
    }

    // used
    /**
     * Return the sample size used when computing the sustained wind speed
     * The value is specified in minutes
     * 
     * @return the sample size in minutes
     */
    public int getWindSpeedAveragePeriod() {
	return m_windSpeedAveragePeriod;
    }

    // used
    /**
     * Set the sample size used when computing the sustained wind speed.
     * 
     * @param speedAveragePeriod the sample size specified in minutes
     */
    public void setWindSpeedAveragePeriod(int speedAveragePeriod) {
	m_windSpeedAveragePeriod = speedAveragePeriod;
    }

    public void setWindGustInterval(int interval)
    {
        m_windGustInterval = interval;
    }
    
    public int getWindGustInterval()
    {
        return m_windGustInterval;
    }
    
    public String getHomeDirectory()
    {
	return this.m_weatherHomeDirectory;
    }

    public void setHomeDirectory(String dir)
    {
	this.m_weatherHomeDirectory = dir;
    }

    /**
     * @return the m_dataFileName
     */
    public String getDataFileName() {
	return m_dataFileName;
    }

    /**
     * @param fileName the m_dataFileName to set
     */
    public void setDataFileName(String fileName) {
	m_dataFileName = fileName;
    }

    /**
     * @return the weatherUndergroundPassword
     */
    public String getWeatherUndergroundPassword() {
	return weatherUndergroundPassword;
    }

    /**
     * @param weatherUndergroundPassword the weatherUndergroundPassword to set
     */
    public void setWeatherUndergroundPassword(String weatherUndergroundPassword) {
	this.weatherUndergroundPassword = weatherUndergroundPassword;
    }

    /**
     * @return the weatherUndergroundUpdateInterval
     */
    public long getWeatherUndergroundUpdateInterval() {
	return weatherUndergroundUpdateInterval;
    }

    /**
     * @param weatherUndergroundUpdateInterval the weatherUndergroundUpdateInterval to set
     */
    public void setWeatherUndergroundUpdateInterval(
	    long weatherUndergroundUpdateInterval) {
	this.weatherUndergroundUpdateInterval = weatherUndergroundUpdateInterval;
    }

    /**
     * @return the weatherUndergroundUrl
     */
    public String getWeatherUndergroundUrl() {
	return weatherUndergroundUrl;
    }

    /**
     * @param weatherUndergroundUrl the weatherUndergroundUrl to set
     */
    public void setWeatherUndergroundUrl(String weatherUndergroundUrl) {
	this.weatherUndergroundUrl = weatherUndergroundUrl;
    }

    /**
     * @return the weatherUndergroundUserName
     */
    public String getWeatherUndergroundUserName() {
	return weatherUndergroundUserName;
    }

    /**
     * @param weatherUndergroundUserName the weatherUndergroundUserName to set
     */
    public void setWeatherUndergroundUserName(String weatherUndergroundUserName) {
	this.weatherUndergroundUserName = weatherUndergroundUserName;
    }

    /**
     * @return the cWOPPassword
     */
    public String getCWOPPassword() {
	return CWOPPassword;
    }

    /**
     * @param password the cWOPPassword to set
     */
    public void setCWOPPassword(String password) {
	CWOPPassword = password;
    }

    /**
     * @return the cWOPPort
     */
    public long getCWOPPort() {
	return CWOPPort;
    }

    /**
     * @param port the cWOPPort to set
     */
    public void setCWOPPort(long port) {
	CWOPPort = port;
    }

    /**
     * @return the cWOPServer
     */
    public String getCWOPServer() {
	return CWOPServer;
    }

    /**
     * @param server the cWOPServer to set
     */
    public void setCWOPServer(String server) {
	CWOPServer = server;
    }

    /**
     * @return the cWOPUpdateIntervalInMinutes
     */
    public long getCWOPUpdateIntervalInMinutes() {
	return CWOPUpdateIntervalInMinutes;
    }

    /**
     * @param updateIntervalInMinutes the cWOPUpdateIntervalInMinutes to set
     */
    public void setCWOPUpdateIntervalInMinutes(long updateIntervalInMinutes) {
	CWOPUpdateIntervalInMinutes = updateIntervalInMinutes;
    }

    /**
     * @return the cWOPUserName
     */
    public String getCWOPUserName() {
	return CWOPUserName;
    }

    /**
     * @param userName the cWOPUserName to set
     */
    public void setCWOPUserName(String userName) {
	CWOPUserName = userName;
    }

    /**
     * @return the weatherUndergroundDebugFlag
     */
    public boolean isWeatherUndergroundDebug() {
	return weatherUndergroundDebugFlag;
    }

    /**
     * @param weatherUndergroundDebugFlag the weatherUndergroundDebugFlag to set
     */
    public void setWeatherUndergroundDebug(boolean weatherUndergroundDebugFlag) {
	this.weatherUndergroundDebugFlag = weatherUndergroundDebugFlag;
    }

    /**
     * @return the cWOPDebugFlag
     */
    public boolean isCWOPDebug() {
	return CWOPDebugFlag;
    }

    /**
     * @param debugFlag the cWOPDebugFlag to set
     */
    public void setCWOPDebug(boolean debugFlag) {
	CWOPDebugFlag = debugFlag;
    }

    /**
     * @return the weatherUndergroundEnableFlag
     */
    public boolean isWeatherUndergroundEnable() {
	return weatherUndergroundEnableFlag;
    }

    /**
     * @param weatherUndergroundEnableFlag the weatherUndergroundEnableFlag to set
     */
    public void setWeatherUndergroundEnable(boolean weatherUndergroundEnableFlag) {
	this.weatherUndergroundEnableFlag = weatherUndergroundEnableFlag;
    }

    /**
     * @return the cWOPEnableFlag
     */
    public boolean isCWOPEnable() {
	return CWOPEnableFlag;
    }

    /**
     * @param enableFlag the cWOPEnableFlag to set
     */
    public void setCWOPEnable(boolean enableFlag) {
	CWOPEnableFlag = enableFlag;
    }

    /**
     * @return the cWOPUploadMinute
     */
    public long getCWOPUploadMinute() {
	return CWOPUploadMinute;
    }

    /**
     * @param uploadMinute the cWOPUploadMinute to set
     */
    public void setCWOPUploadMinute(long uploadMinute) {
	CWOPUploadMinute = uploadMinute;
    }

    /**
     * @return the builderTemplateFolder
     */
    public String getBuilderTemplateFolder() {
	return builderTemplateFolder;
    }

    /**
     * @param builderTemplateFolder the builderTemplateFolder to set
     */
    public void setBuilderTemplateFolder(String builderTemplateFolder) {
	this.builderTemplateFolder = builderTemplateFolder;
    }

    /**
     * @return the builderOutputFolder
     */
    public String getBuilderOutputFolder() {
	return builderOutputFolder;
    }

    /**
     * @param builderOutputFolder the builderOutputFolder to set
     */
    public void setBuilderOutputFolder(String builderOutputFolder) {
	this.builderOutputFolder = builderOutputFolder;
    }

    /**
     * @return the builderFTPServer
     */
    public String getBuilderFTPServer() {
	return builderFTPServer;
    }

    /**
     * @param builderFTPServer the builderFTPServer to set
     */
    public void setBuilderFTPServer(String builderFTPServer) {
	this.builderFTPServer = builderFTPServer;
    }

    /**
     * @return the builderFTPFolder
     */
    public String getBuilderFTPFolder() {
	return builderFTPFolder;
    }

    /**
     * @param builderFTPFolder the builderFTPFolder to set
     */
    public void setBuilderFTPFolder(String builderFTPFolder) {
	this.builderFTPFolder = builderFTPFolder;
    }

    /**
     * @return the builderFTPLogin
     */
    public String getBuilderFTPLogin() {
	return builderFTPLogin;
    }

    /**
     * @param builderFTPLogin the builderFTPLogin to set
     */
    public void setBuilderFTPLogin(String builderFTPLogin) {
	this.builderFTPLogin = builderFTPLogin;
    }

    /**
     * @return the builderFTPPassword
     */
    public String getBuilderFTPPassword() {
	return builderFTPPassword;
    }

    /**
     * @param builderFTPPassword the builderFTPPassword to set
     */
    public void setBuilderFTPPassword(String builderFTPPassword) {
	this.builderFTPPassword = builderFTPPassword;
    }

    public String getWeatherForYouUrl()
    {
        return weatherForYouUrl;
    }

    public void setWeatherForYouUrl(String weatherForYouUrl)
    {
        this.weatherForYouUrl = weatherForYouUrl;
    }

    public long getWeatherForYouUpdateInterval()
    {
        return weatherForYouUpdateInterval;
    }

    public void setWeatherForYouUpdateInterval(long weatherForYouUpdateInterval)
    {
        this.weatherForYouUpdateInterval = weatherForYouUpdateInterval;
    }

    public boolean isWeatherForYouDebugFlag()
    {
        return weatherForYouDebugFlag;
    }

    public void setWeatherForYouDebugFlag(boolean weatherForYouDebugFlag)
    {
        this.weatherForYouDebugFlag = weatherForYouDebugFlag;
    }

    public boolean isWeatherForYouEnableFlag()
    {
        return weatherForYouEnableFlag;
    }

    public void setWeatherForYouEnableFlag(boolean weatherForYouEnableFlag)
    {
        this.weatherForYouEnableFlag = weatherForYouEnableFlag;
    }

    /**
     * @return the weatherForYouID
     */
    public String getWeatherForYouID()
    {
        return weatherForYouID;
    }

    /**
     * @param weatherForYouID the weatherForYouID to set
     */
    public void setWeatherForYouID(String weatherForYouID)
    {
        this.weatherForYouID = weatherForYouID;
    }

    /**
     * @return the weatherForYouPassword
     */
    public String getWeatherForYouPassword()
    {
        return weatherForYouPassword;
    }

    /**
     * @param weatherForYouPassword the weatherForYouPassword to set
     */
    public void setWeatherForYouPassword(String weatherForYouPassword)
    {
        this.weatherForYouPassword = weatherForYouPassword;
    }

    /**
     * @return the databaseDebugFlag
     */
    public boolean isDatabaseDebugFlag()
    {
        return DatabaseDebugFlag;
    }

    /**
     * @param databaseDebugFlag the databaseDebugFlag to set
     */
    public void setDatabaseDebugFlag(boolean databaseDebugFlag)
    {
        DatabaseDebugFlag = databaseDebugFlag;
    }

    /**
     * @return the databaseEnableFlag
     */
    public boolean isDatabaseEnableFlag()
    {
        return DatabaseEnableFlag;
    }

    /**
     * @param databaseEnableFlag the databaseEnableFlag to set
     */
    public void setDatabaseEnableFlag(boolean databaseEnableFlag)
    {
        DatabaseEnableFlag = databaseEnableFlag;
    }

    /**
     * @return the databaseUpdateInterval
     */
    public long getDatabaseUpdateInterval()
    {
        return DatabaseUpdateInterval;
    }

    /**
     * @param databaseUpdateInterval the databaseUpdateInterval to set
     */
    public void setDatabaseUpdateInterval(long databaseUpdateInterval)
    {
        DatabaseUpdateInterval = databaseUpdateInterval;
    }

    /**
     * @return the jDBCConnectString
     */
    public String getJDBCConnectString()
    {
        return this.JDBCConnectString;
    }

    /**
     * @param jDBCConnectString the jDBCConnectString to set
     */
    public void setJDBCConnectString(String jDBCConnectString)
    {
        this.JDBCConnectString = jDBCConnectString;
    }

    /**
     * @return the backyardWeatherUserName
     */
    public String getBackyardWeatherPubId()
    {
        return backyardWeatherUserName;
    }

    /**
     * @param backyardWeatherUserName the backyardWeatherUserName to set
     */
    public void setBackyardWeatherPubID(String backyardWeatherUserName)
    {
        this.backyardWeatherUserName = backyardWeatherUserName;
    }

    /**
     * @return the backyardWeatherPassword
     */
    public String getBackyardWeatherPassword()
    {
        return backyardWeatherPassword;
    }

    /**
     * @param backyardWeatherPassword the backyardWeatherPassword to set
     */
    public void setBackyardWeatherPassword(String backyardWeatherPassword)
    {
        this.backyardWeatherPassword = backyardWeatherPassword;
    }

    /**
     * @return the backyardWeatherUrl
     */
    public String getBackyardWeatherUrl()
    {
        return backyardWeatherUrl;
    }

    /**
     * @param backyardWeatherUrl the backyardWeatherUrl to set
     */
    public void setBackyardWeatherUrl(String backyardWeatherUrl)
    {
        this.backyardWeatherUrl = backyardWeatherUrl;
    }

    /**
     * @return the backyardWeatherUpdateInterval
     */
    public long getBackyardWeatherUpdateInterval()
    {
        return backyardWeatherUpdateInterval;
    }

    /**
     * @param backyardWeatherUpdateInterval the backyardWeatherUpdateInterval to set
     */
    public void setBackyardWeatherUpdateInterval(long backyardWeatherUpdateInterval)
    {
        this.backyardWeatherUpdateInterval = backyardWeatherUpdateInterval;
    }

    /**
     * @return the backyardWeatherDebugFlag
     */
    public boolean isBackyardWeatherDebugFlag()
    {
        return backyardWeatherDebugFlag;
    }

    /**
     * @param backyardWeatherDebugFlag the backyardWeatherDebugFlag to set
     */
    public void setBackyardWeatherDebugFlag(boolean backyardWeatherDebugFlag)
    {
        this.backyardWeatherDebugFlag = backyardWeatherDebugFlag;
    }

    /**
     * @return the backyardWeatherEnableFlag
     */
    public boolean isBackyardWeatherEnableFlag()
    {
        return backyardWeatherEnableFlag;
    }

    /**
     * @param backyardWeatherEnableFlag the backyardWeatherEnableFlag to set
     */
    public void setBackyardWeatherEnableFlag(boolean backyardWeatherEnableFlag)
    {
        this.backyardWeatherEnableFlag = backyardWeatherEnableFlag;
    }

    /**
     * @return the backyardWeatherStationNumber
     */
    public String getBackyardWeatherStationNumber()
    {
        return backyardWeatherStationNumber;
    }

    /**
     * @param backyardWeatherStationNumber the backyardWeatherStationNumber to set
     */
    public void setBackyardWeatherStationNumber(String backyardWeatherStationNumber)
    {
        this.backyardWeatherStationNumber = backyardWeatherStationNumber;
    }

    /**
     * @return the m_saveData
     */
    public boolean isSaveData()
    {
        return m_saveData;
    }

    /**
     * @param m_saveData the m_saveData to set
     */
    public void setSaveData(boolean m_saveData)
    {
        this.m_saveData = m_saveData;
    }

    /**
     * @return the m_saveStats
     */
    public boolean isSaveStats()
    {
        return m_saveStats;
    }

    /**
     * @param m_saveStats the m_saveStats to set
     */
    public void setSaveStats(boolean m_saveStats)
    {
        this.m_saveStats = m_saveStats;
    }

    /**
     * @return the m_saveLog
     */
    public boolean isSaveLog()
    {
        return m_saveLog;
    }

    /**
     * @param m_saveLog the m_saveLog to set
     */
    public void setSaveLog(boolean m_saveLog)
    {
        this.m_saveLog = m_saveLog;
    }

    /**
     * @return the persistDBConnection
     */
    public boolean isPersistDBConnection()
    {
        return persistDBConnection;
    }

    /**
     * @param persistDBConnection the persistDBConnection to set
     */
    public void setDatabasePersistFlag(boolean persistDBConnection)
    {
        this.persistDBConnection = persistDBConnection;
    }

    public void setOneWireEnabled(boolean enabledFlag)
    {
        this.m_oneWireEnabled = enabledFlag;
    }
    
    public boolean isOneWireEnabled()
    {
        return m_oneWireEnabled;
    }
    public void setDavisEnabled(boolean enabledFlag)
    {
        this.m_davisEnabled = enabledFlag;
    }
    
    public boolean isDavisEnabled()
    {
        return m_davisEnabled;
    }
    
    public String getDavisComPort()
    {
        return this.m_davisPort;
    }
    
    public void setDavisComPort(String port)
    {
        this.m_davisPort = port;
    }
    
    public int getDavisBaudRate()
    {
        return this.m_davisBaudRate;
    }
    
    public void setDavisBaudRate(int baudRate)
    {
        this.m_davisBaudRate = baudRate;
    }

    /**
     * @return the m_davisParity
     */
    public String getDavisParity()
    {
        return m_davisParity;
    }

    /**
     * @param m_davisParity the m_davisParity to set
     */
    public void setDavisParity(String davisParity)
    {
        this.m_davisParity = davisParity;
    }

    /**
     * @return the m_davisStopBits
     */
    public int getDavisStopBits()
    {
        return m_davisStopBits;
    }

    /**
     * @param m_davisStopBits the m_davisStopBits to set
     */
    public void setDavisStopBits(int davisStopBits)
    {
        this.m_davisStopBits = davisStopBits;
    }

    /**
     * @return the m_davisBits
     */
    public int getDavisBits()
    {
        return m_davisBits;
    }

    /**
     * @param m_davisBits the m_davisBits to set
     */
    public void setDavisBits(int davisBits)
    {
        this.m_davisBits = davisBits;
    }

    /**
     * @return the m_davisRetryCount
     */
    public int getDavisRetryCount()
    {
        return m_davisRetryCount;
    }

    /**
     * @param m_davisRetryCount the m_davisRetryCount to set
     */
    public void setDavisRetryCount(int m_davisRetryCount)
    {
        this.m_davisRetryCount = m_davisRetryCount;
    }

    /**
     * @return the m_davisOutdoorTemperatureEnabled
     */
    public boolean isDavisOutdoorTemperatureEnabled()
    {
        return m_davisOutdoorTemperatureEnabled;
    }

    /**
     * @param m_davisOutdoorTemperatureEnabled the m_davisOutdoorTemperatureEnabled to set
     */
    public void setDavisOutdoorTemperatureEnabled(
            boolean m_davisOutdoorTemperatureEnabled)
    {
        this.m_davisOutdoorTemperatureEnabled = m_davisOutdoorTemperatureEnabled;
    }

    /**
     * @return the m_davisWindSpeedEnabled
     */
    public boolean isDavisWindSpeedEnabled()
    {
        return m_davisWindSpeedEnabled;
    }

    /**
     * @param m_davisWindSpeedEnabled the m_davisWindSpeedEnabled to set
     */
    public void setDavisWindSpeedEnabled(boolean m_davisWindSpeedEnabled)
    {
        this.m_davisWindSpeedEnabled = m_davisWindSpeedEnabled;
    }

    /**
     * @return the m_davisOutdoorHumidityEnabled
     */
    public boolean isDavisOutdoorHumidityEnabled()
    {
        return m_davisOutdoorHumidityEnabled;
    }

    /**
     * @param m_davisOutdoorHumidityEnabled the m_davisOutdoorHumidityEnabled to set
     */
    public void setDavisOutdoorHumidityEnabled(
            boolean m_davisOutdoorHumidityEnabled)
    {
        this.m_davisOutdoorHumidityEnabled = m_davisOutdoorHumidityEnabled;
    }

    /**
     * @return the m_davisWindDirectionEnabled
     */
    public boolean isDavisWindDirectionEnabled()
    {
        return m_davisWindDirectionEnabled;
    }

    /**
     * @param m_davisWindDirectionEnabled the m_davisWindDirectionEnabled to set
     */
    public void setDavisWindDirectionEnabled(boolean m_davisWindDirectionEnabled)
    {
        this.m_davisWindDirectionEnabled = m_davisWindDirectionEnabled;
    }

    /**
     * @return the m_davisPressureEnabled
     */
    public boolean isDavisPressureEnabled()
    {
        return m_davisPressureEnabled;
    }

    /**
     * @param m_davisPressureEnabled the m_davisPressureEnabled to set
     */
    public void setDavisPressureEnabled(boolean m_davisPressureEnabled)
    {
        this.m_davisPressureEnabled = m_davisPressureEnabled;
    }

    /**
     * @return the m_davisRainfallEnabled
     */
    public boolean isDavisRainfallEnabled()
    {
        return m_davisRainfallEnabled;
    }

    /**
     * @param m_davisRainfallEnabled the m_davisRainfallEnabled to set
     */
    public void setDavisRainfallEnabled(boolean m_davisRainfallEnabled)
    {
        this.m_davisRainfallEnabled = m_davisRainfallEnabled;
    }

    /**
     * @return the m_davisSolarEnabled
     */
    public boolean isDavisSolarEnabled()
    {
        return m_davisSolarEnabled;
    }

    /**
     * @param m_davisSolarEnabled the m_davisSolarEnabled to set
     */
    public void setDavisSolarEnabled(boolean m_davisSolarEnabled)
    {
        this.m_davisSolarEnabled = m_davisSolarEnabled;
    }

    /**
     * @return the m_davisLightningEnabled
     */
    public boolean isDavisLightningEnabled()
    {
        return m_davisLightningEnabled;
    }

    /**
     * @param m_davisLightningEnabled the m_davisLightningEnabled to set
     */
    public void setDavisLightningEnabled(boolean m_davisLightningEnabled)
    {
        this.m_davisLightningEnabled = m_davisLightningEnabled;
    }

    /**
     * @return the m_davisIndoorTemperatureEnabled
     */
    public boolean isDavisIndoorTemperatureEnabled()
    {
        return m_davisIndoorTemperatureEnabled;
    }

    /**
     * @param m_davisIndoorTemperatureEnabled the m_davisIndoorTemperatureEnabled to set
     */
    public void setDavisIndoorTemperatureEnabled(
            boolean m_davisIndoorTemperatureEnabled)
    {
        this.m_davisIndoorTemperatureEnabled = m_davisIndoorTemperatureEnabled;
    }

    /**
     * @return the m_davisIndoorHumidityEnabled
     */
    public boolean isDavisIndoorHumidityEnabled()
    {
        return m_davisIndoorHumidityEnabled;
    }

    /**
     * @param m_davisIndoorHumidityEnabled the m_davisIndoorHumidityEnabled to set
     */
    public void setDavisIndoorHumidityEnabled(
            boolean m_davisIndoorHumidityEnabled)
    {
        this.m_davisIndoorHumidityEnabled = m_davisIndoorHumidityEnabled;
    }
}
