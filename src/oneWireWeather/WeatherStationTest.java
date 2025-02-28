/* SVN FILE: $Id: WeatherStationTest.java 187 2015-12-08 23:07:01Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 187 $
* $Date: 2015-12-08 18:07:01 -0500 (Tue, 08 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-08 18:07:01 -0500 (Tue, 08 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/WeatherStationTest.java $
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

import java.io.*;
import java.util.*;

import com.dalsemi.onewire.OneWireAccessProvider;
//import com.dalsemi.onewire.OneWireException;
//import com.dalsemi.onewire.adapter.DSPortAdapter;
//import com.dalsemi.onewire.container.OneWireContainer;
//import com.dalsemi.onewire.container.OneWireContainer1F;
//import com.dalsemi.onewire.container.SwitchContainer;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.utils.OWPath;

public class WeatherStationTest {

    private static final String auxTempSensorID = "4D0008001B4F8010"; // sensor in AAG 
    private static final String tempSensorID = "DB00080138E62310";
    private static final String windSpeedSensorID = "7D0000000109E51D";
    private static final String windDirectionSensorID = "6400000000FF4120";
    private static final String humiditySensorID = "52000000AEF2D226";
    private static final String barometerSensorID = "940000009E930B26";
    private static final String rainSensorID = "3A0000000A24A31D";
    private static final String lightningSensorID = "5200000009EBC11D";
    private static final String solarSensorID = "B2000000AEEF3126"; 

    /**
     * Main test harness for WeatherStation class.
     * 
     * @param args 0 - home folder
     */
    public static void main(String[] args) {

	boolean quit = false;
	String homeFolder;
	WeatherStation ws;

	if (args.length > 0)
	{
	    homeFolder = args[0];
	    ws = new WeatherStation(homeFolder);
	}
	else
	{
	    ws = new WeatherStation();
	}

	/* notes: some useful functions that should be here
	 * - test run the weather station - OK
	 * - list out adapters found on this system and their ports - OK?
	 * - list out all devices on the bus - OK
	 * - allow creating a config file from the sensors found on the bus
	 * - show the contents of the config file
	*/
	
	System.out.println("This is a test of the Weather Station project");

	// display main menu and process commands
	while (!quit)
	{
	    displayMenu();
	    char choice = getValidChoice();
	    switch(choice) {
		case 'q': // quit
		case 'Q':
		    quit = true;
		    break;
		case '1': // run the weather station and print the readings
		    if (args.length > 0)
		    {
			homeFolder = args[0];
			ws = new WeatherStation(homeFolder);
		    }
		    else
		    {
			ws = new WeatherStation();
		    }
		    runWeatherStation(ws);
		    break;
		case '2':
		    StationConfiguration newConfig = ws.getStationConfiguration();
		    initializeStationConfiguration(newConfig);
		    showStationConfiguration(newConfig);
		    System.out.println("Not saving for now...");
//		    ws.saveConfiguration(newConfig);
		    break;
		case '3':
		    System.out.println("Adapters found:");
		    listAdapters();
		    System.out.println("");
		    break;
		case '4':
		    listPortsForAdapter();
		    System.out.println("");
		    break;
		case '5':
		    System.out.println(ws.getLog().toString());
		    break;
		case '6':
		    showStationConfiguration(ws.getStationConfiguration());
		    break;
		case '7':
		    System.out.println(ws.getStatistics().getOutdoorTempStats().toString());
		    break;
		case '8':
		    StationConfiguration config = ws.getStationConfiguration();
		    String homeDir = config.getHomeDirectory();
		    String fullPath = homeDir + File.separator + "log.xml";

		    System.out.println("Saving log to " + fullPath);
		    StationLog log = ws.getLog();
		    String xml = log.toXML();
		    try 
		    {
			PrintWriter out = new PrintWriter(new FileWriter(fullPath),true);
			out.print(xml);
			out.close();
			System.out.println("Done");
		    } 
		    catch (IOException e) 
		    {
			e.printStackTrace();
		    }

		    fullPath = homeDir + File.separator + "stats.xml";
		    System.out.println("Saving stats to " + fullPath);
		    WeatherStatistics stats = ws.getStatistics();
		    xml = stats.toXML();
		    try 
		    {
			PrintWriter out = new PrintWriter(new FileWriter(fullPath),true);
			out.print(xml);
			out.close();
			System.out.println("Done");
		    } 
		    catch (IOException e) 
		    {
			e.printStackTrace();
		    }

		    fullPath = homeDir + File.separator + "data.xml";
		    System.out.println("Saving current data to " + fullPath);
		    WeatherData data = ws.getData();
		    xml = data.toXML(config);
		    try 
		    {
			PrintWriter out = new PrintWriter(new FileWriter(fullPath),true);
			out.print(xml);
			out.close();
			System.out.println("Done");
		    } 
		    catch (IOException e) 
		    {
			e.printStackTrace();
		    }

		    break;
		case '9':
		    listAdapterInfo();
		    break;
		case 'a':
		case 'A':
		    // list all one wire devices on bus
		    listDevices();
		    break;

	    }
	}

	System.out.println("Weather station test exiting.");
	
	System.exit(0);
    }

    private static void runWeatherStation(WeatherStation ws)
    {
	InputStreamReader in = new InputStreamReader(System.in);
	boolean quit = false;
	Date sampleDate = new Date();
	Date lastSampleDate = new Date();

	if (ws.isStationRunning())
	{
	    ws.stopWeatherStation();
	    ws.shutDownFileMonitor();
	}

	System.out.print("Starting weather station......");
	ws.startWeatherStation();
	System.out.println("done");

	while (!quit)
	{
	    try
	    {
		Thread.sleep(3000);
//		Thread.sleep(500);
	    }
	    catch (InterruptedException e) {}

	    // display sampled data
	    WeatherData data = ws.getData();

	    //*** add sanity check to see if sample date/time hasn't changed in a long time
	    // if so, try restarting

	    if (data != null && !lastSampleDate.equals(data.getSampleDate()))
	    {
		WeatherStatistics stats = ws.getStatistics();

		sampleDate.setTime(System.currentTimeMillis());
		System.out.println("Current Weather at: " + sampleDate + " Sample time: " + data.getSampleDate());
		System.out.println("  Outdoor Temp: Current " + WeatherConversions.formatFloat(data.getTemperature(), 2) + 
			" (" + WeatherConversions.formatFloat(stats.getOutdoorTempStats().getCumulativeValues().getMinValue(),2) + " | " + 
			WeatherConversions.formatFloat(stats.getOutdoorTempStats().getCumulativeValues().getMaxValue(),2) +  
			") Trend " + stats.getTemperatureTrend()); 
		//				System.out.println("  Min at: " + stats.getOutdoorTempStats().getCumulativeValues().getMinDate() + " Max at: " + stats.getOutdoorTempStats().getCumulativeValues().getMaxDate());
		System.out.println("  Wind Speed: Gust " + WeatherConversions.formatFloat(data.getWindSpeed(),2) + 
			" ("  + WeatherConversions.formatFloat(stats.getWindGustStats().getCumulativeValues().getMaxValue(),2) + ") "  + 
			WeatherConversions.windSpeedToLiteralMPH(data.getWindSpeed()) + 
			" from " + data.getWindDirection() + " " + WeatherConversions.getWindDirStr(data.getWindDirection()) + 
			//                        " Sustained: " + WeatherConversions.formatFloat(stats.getSustainedWindSpeed(),2) + 
			" Sustained: " + WeatherConversions.formatFloat(data.getAverageWindSpeed(),2) + 
			" (" + WeatherConversions.formatFloat(stats.getWindSustainedStats().getCumulativeValues().getMaxValue(),2) + ")");
		//				System.out.println("  Max gust: " + WeatherConversions.formatFloat(stats.getWindGustStats().getCumulativeValues().getMaxValue(),2) 
		//						+ " at " + stats.getWindGustStats().getCumulativeValues().getMaxDate() +
		//                        " Max sustained: " + WeatherConversions.formatFloat(stats.getWindSustainedStats().getCumulativeValues().getMaxValue(),2) +
		//                        " at " + stats.getWindSustainedStats().getCumulativeValues().getMaxDate());
		System.out.println("  Average wind direction: " + stats.getWindAverageDirection() + 
			WeatherConversions.getWindDirStr((int)(stats.getWindAverageDirection()/22.5)) +
			" Est cloud base: " + WeatherConversions.estimateCloudBaseHeightFahrenheit(data.getTemperature(), WeatherConversions.calcDewpointFahrenheit(data.getTemperature(), data.getHumidity())));
		System.out.println("  Wind chill: " + WeatherConversions.formatFloat(stats.getWindChillTemperature(),2) + " Heat index: " + WeatherConversions.formatFloat(WeatherConversions.calcHeatIndexFahrenheit(data.getTemperature(), data.getHumidity()),2));
		System.out.println("  Humidity: " + WeatherConversions.formatFloat(data.getHumidity(), 2) +
			" (" + WeatherConversions.formatFloat(stats.getOutdoorHumidityStats().getCumulativeValues().getMinValue(), 2) +
			" | " + WeatherConversions.formatFloat(stats.getOutdoorHumidityStats().getCumulativeValues().getMaxValue(), 2) +  
			") Dewpoint " + WeatherConversions.formatFloat(WeatherConversions.calcDewpointFahrenheit(data.getTemperature(), data.getHumidity()),2));
		System.out.println("  Pressure: " + WeatherConversions.formatFloat(data.getPressure(), 2) +
			" (" + WeatherConversions.formatFloat(stats.getPressureCumValues().getCumulativeValues().getMinValue(), 2) + " | " + 
			WeatherConversions.formatFloat(stats.getPressureCumValues().getCumulativeValues().getMaxValue(), 2) + 
			") trend: " + stats.getPressureTrend() + " " + WeatherConversions.pressureTrendInInchesToLiteral(stats.getPressureTrend()));
		System.out.println("  Indoor temperature: " +WeatherConversions.formatFloat(data.getIndoorTemperature(), 2) +
			" (" + WeatherConversions.formatFloat(stats.getIndoorTempCumValues().getCumulativeValues().getMinValue(), 2) + 
			" | " + WeatherConversions.formatFloat(stats.getIndoorTempCumValues().getCumulativeValues().getMaxValue(), 2) + 
			") humidity: " + WeatherConversions.formatFloat(data.getIndoorHumidity(), 2) + " (" +
			WeatherConversions.formatFloat(stats.getIndoorHumidityCumValues().getCumulativeValues().getMinValue(), 2) + " | " +
			WeatherConversions.formatFloat(stats.getIndoorHumidityCumValues().getCumulativeValues().getMaxValue(), 2) + ")" );
		System.out.println("  Solar: " + WeatherConversions.formatFloat(data.getSolar(), 2) + " (" +  WeatherConversions.formatFloat(stats.getSolarCumValues().getCumulativeValues().getMaxValue(),2) + ")" +
			" Lightning: " + WeatherConversions.formatInt(data.getLightning()) + " " +
					"Last Minute: " + WeatherConversions.formatFloat(stats.getLightningCounts().getMinuteCount(),2) + 
					" last Hour: " + WeatherConversions.formatFloat(stats.getLightningCounts().getHourCount(),2));
		System.out.println("  Rain: " + WeatherConversions.formatFloat(data.getRainfall(), 2) + 
			" Last minute: " + WeatherConversions.formatFloat(stats.getRainCounts().getMinuteCount(),2) + 
			" last Hour: " + WeatherConversions.formatFloat(stats.getRainCounts().getHourCount(),2));

		// look at aux sensors
		StationConfiguration config = ws.getStationConfiguration();
		String[][] values = data.extraSensors();
		for (int i = 0; i < values.length; i++)
		{
		    String value = values[i][1];
		    String key = values[i][0];
		    SensorConfiguration sensor = config.findSensorByID(key);
		    switch (sensor.getUsageType())
		    {
			//                        case SensorType.SOLAR:
			//                            System.out.println("  " + sensor.getName() + " " + value + "M/W^2");
			//                            break;
			default:
			    System.out.println("  " + sensor.getName() + " " + WeatherConversions.formatFloat(value,2));
			    break;
		    }
		}

		System.out.println("");

		lastSampleDate = data.getSampleDate();
	    }

	    // check to see if it is time to quit
	    try
	    {
		while (in.ready())
		{
		    if (in.read() == 'q')
		    {
			quit = true;
		    }
		}
	    }
	    catch (IOException e) {}
	}

	ws.stopWeatherStation();
	ws.shutDownFileMonitor();

    }

    private static void initializeStationConfiguration(StationConfiguration config)
    {
	// store the needed adapter information
	// for now, hard code these values

	// settings for Windows WS
	//      config.setAdapterName("{DS9490}");
	//      config.setAdapterPortName("USB1");
	//      config.setHardwarePollInterval(500);

	// settings for Linux WS 
	config.setAdapterName("DS9097U");
	config.setAdapterPortName("/dev/ttyS0");
	config.setHardwarePollInterval(2000);

	config.setWindDirectionAveragePeriod(5);
	config.setWindSpeedAveragePeriod(1);
	config.setLogFileName("logFile.log");
	config.setLogFileSize(1440);  // one full day's worth

	config.setWeatherUndergroundUserName("KMDGERMA9");
	config.setWeatherUndergroundPassword("mr-pest");
	config.setWeatherUndergroundUrl("rtupdate.wunderground.com");
	config.setWeatherUndergroundUpdateInterval(5000);

	config.setCWOPUserName("KB3HHA");
	config.setCWOPPassword("-1");
	config.setCWOPServer("cwop.aprs.net");
	config.setCWOPPort(14580);
	config.setCWOPUpdateIntervalInMinutes(10);

	// remove any existing sensors
	config.clearSensorList();

	// add the hard coded sensor list
	SensorConfiguration mainTempSensor = 
		new SensorConfiguration("Outdoor Temperature", 
			"Main outdoor temperature sensor",
			tempSensorID, 
			WeatherStation.getSensorTypeForUsage(SensorType.MAIN_OUTDOOR_TEMP));

	SensorConfiguration mainWindSpeedSensor = 
		new SensorConfiguration(
			"Wind Speed", 
			"Main wind speed sensor", 
			windSpeedSensorID, 
			WeatherStation.getSensorTypeForUsage(SensorType.AAG_WIND_SPEED));

	SensorConfiguration mainWindDirectionSensor = 
		new SensorConfiguration(
			"Wind Direction", 
			"Main wind direction sensor", 
			windDirectionSensorID, 
			WeatherStation.getSensorTypeForUsage(SensorType.AAG_WIND_DIRECTION));

	SensorConfiguration mainHumiditySensor = 
		new SensorConfiguration(
			"Humidity", 
			"Main humidity sensor", 
			humiditySensorID, 
			WeatherStation.getSensorTypeForUsage(SensorType.OUTDOOR_HUMIDITY));

	SensorConfiguration auxTemp = 
		new SensorConfiguration(
			"Aux temp sensor",
			"Extra temp sensor",
			auxTempSensorID,
			WeatherStation.getSensorTypeForUsage(SensorType.AUX_OUTDOOR_TEMP));

	SensorConfiguration barometer =
		new SensorConfiguration(
			"Barometer sensor",
			"Main barometer",
			barometerSensorID,
			WeatherStation.getSensorTypeForUsage(SensorType.PRESSURE));
	//		barometer.setGain(0.6698f);
	//      barometer.setOffset(26.6604f);
	barometer.setGain(0.6647f);
	barometer.setOffset(26.45f);

	SensorConfiguration solar = 
		new SensorConfiguration(
			"Solar sensor",
			"Outdoor solar sensor",
			solarSensorID,
			WeatherStation.getSensorTypeForUsage(SensorType.SOLAR));

	SensorConfiguration rain = 
		new SensorConfiguration(
			"Rain sensor",
			"Rain sensor",
			rainSensorID,
			WeatherStation.getSensorTypeForUsage(SensorType.RAIN_COUNTER));

	SensorConfiguration lightning = 
		new SensorConfiguration(
			"Lightning sensor",
			"Lightning sensor",
			lightningSensorID,
			WeatherStation.getSensorTypeForUsage(SensorType.LIGHTNING_COUNTER));

	// create main sensors
	config.addSensorToConfiguration(mainTempSensor);
	config.addSensorToConfiguration(mainWindSpeedSensor);
	config.addSensorToConfiguration(mainWindDirectionSensor);
	config.addSensorToConfiguration(mainHumiditySensor);
	config.addSensorToConfiguration(barometer);
	config.addSensorToConfiguration(solar);
	config.addSensorToConfiguration(rain);
	config.addSensorToConfiguration(lightning);

	//		
	//		// create aux sensors
	config.addSensorToConfiguration(auxTemp);	// aux temp sensor (in AAG device)
    }

    private static void displayMenu()
    {
	System.out.println("Weather Station Menu");
	System.out.println(" 1. Run Station");
	System.out.println(" 2. Create Default Configuration");
	System.out.println(" 3. List Adapters");
	System.out.println(" 4. Display Ports for an Adapter");
	System.out.println(" 5. Display Station Log");
	System.out.println(" 6. Display Station Config");
	System.out.println(" 7. Display Outdoor Temp Stats");
	System.out.println(" 8. Save all data as XML");
	System.out.println(" 9. Display Adapter Info");
	System.out.println(" A. Display Device IDs");

	System.out.println(" Q. Quit program");
    }

    private static char getValidChoice()
    {
	InputStreamReader in = new InputStreamReader(System.in);

	System.out.print("Select an option ==> ");
	char value = ' ';

	while (value != 'q' && value != 'Q' && value != '1' && value != '2' && 
		value != '3' && value != '4' && value != '5' && value != '6' && value != '7' && 
		value != '8' && value != '9'  && value != 'A' && value != 'a')
	{
	    try
	    {
		value = (char)in.read();
	    }
	    catch (IOException e) {}
	}

	return value;
    }

    private static void listAdapters()
    {
	String[] adapterList = StationConfiguration.listAdapters();

	for (int i = 0; i < adapterList.length; i++)
	{
	    System.out.println(i + ". " + adapterList[i]);
	}
	
	// show default adapter
	System.out.println("Default adapter: " + OneWireAccessProvider.getProperty("onewire.adapter.default") + 
		" Port: " +  OneWireAccessProvider.getProperty("onewire.port.default"));
    }
    
//    private static void listPorts()
//    {
//	String[] portList = StationConfiguration.listPorts();
//	for (int i = 0; i < portList.length; i++)
//	{
//	    System.out.println(i + ". " + portList[i]);
//	}
//    }

    private static void listAdapterInfo()
    {
	InputStreamReader in = new InputStreamReader(System.in);

	// get selected adapter
	String[] adapterList = StationConfiguration.listAdapters();

	listAdapters();
	System.out.print("Choose an adapter ==> ");
	int value = -1;

	//while (value < 0 || value > adapterList.size())
	while (value < 0 || value > adapterList.length)
	{
	    try
	    {
		char temp = (char) in.read();
		value = temp - '0';
	    }
	    catch (IOException e) {}
	}
	
	String[] portList = StationConfiguration.listPorts(adapterList[value]);
	
	for (int i = 0; i < portList.length; i++)
	{
	    System.out.println(i + ". " + portList[i]);
	}
	System.out.print("Choose a port ==> ");
	int portValue = -1;
	while (portValue < 0 || portValue > portList.length)
	{
	    try
	    {
		char temp = (char) in.read();
		portValue = temp - '0';
	    }
	    catch (IOException e) {}
	}
	
	String info = StationConfiguration.getAdapterInfo(adapterList[value], portList[portValue]);
	System.out.println(info);
    }
    
    private static void listPortsForAdapter()
    {
	InputStreamReader in = new InputStreamReader(System.in);

	// get selected adapter
	String[] adapterList = StationConfiguration.listAdapters();

	listAdapters();
	System.out.print("Choose an adapter ==> ");
	int value = -1;

	//while (value < 0 || value > adapterList.size())
	while (value < 0 || value > adapterList.length)
	{
	    try
	    {
		char temp = (char) in.read();
		value = temp - '0';
	    }
	    catch (IOException e) {}
	}

	// get port list for that adapter
	String[] ports = 
		StationConfiguration.listPorts(adapterList[value]);

	System.out.println("\nPorts for adapter " + adapterList[value]);

	// print the list
	for (int i = 0; i < ports.length; i++)
	{
	    System.out.println(i + ". " + ports[i]);
	}
    }

    private static void showStationConfiguration(StationConfiguration config)
    {

	System.out.println("Adapter: " + config.getAdapterName() + " on port: " + config.getAdapterPortName());
	System.out.println("Hardware poll interval: " + config.getHardwarePollInterval());
	System.out.println("Log file name: " + config.getLogFileName() + " size: " + config.getLogFileSize());
	System.out.println("Wind direction average period: " + config.getWindDirectionAveragePeriod());
	System.out.println("Wind speed average period: " + config.getWindSpeedAveragePeriod());

	System.out.println("Configured sensors");

	for (Enumeration<SensorConfiguration> e = config.getSensorList().elements(); e.hasMoreElements() ;) 
	{
	    SensorConfiguration sensor = (SensorConfiguration)e.nextElement();
	    System.out.println("ID " + sensor.getID() + " Name: " + sensor.getName() + " Desc: " + sensor.getDescription() + " Usage: " + WeatherStation.getSensorTypeForUsage(sensor.getUsageType())
		    + " Freq: " + sensor.getPollFrequency() + " poll offset: " + sensor.getPollOffset() + " gain: " + sensor.getGain() + " offset " + sensor.getOffset());
	}
    }
    
    private static void listDevices()
    {
	InputStreamReader in = new InputStreamReader(System.in);

	// get adapter and port
	String[] adapterList = StationConfiguration.listAdapters();

	listAdapters();
	System.out.print("Choose an adapter ==> ");
	int value = -1;

	//while (value < 0 || value > adapterList.size())
	while (value < 0 || value > adapterList.length)
	{
	    try
	    {
		char temp = (char) in.read();
		value = temp - '0';
	    }
	    catch (IOException e) {}
	}

	String[] portList = StationConfiguration.listPorts(adapterList[value]);
	
	// print the list
	for (int i = 0; i < portList.length; i++)
	{
	    System.out.println(i + ". " + portList[i]);
	}
	
	System.out.print("Choose a port ==> ");
	int portValue = -1;
	while (portValue < 0 || portValue > portList.length)
	{
	    try
	    {
		char temp = (char) in.read();
		portValue = temp - '0';
	    }
	    catch (IOException e) {}
	}
	
	// walk the net
	Hashtable<String, OWPath> devicePaths = StationConfiguration.walkNet(adapterList[value], portList[portValue]);

	// walk the list of devices
	for (Enumeration<String> keys = devicePaths.keys(); keys.hasMoreElements(); )
	{
	    String address = (String)keys.nextElement();
	    OWPath path = (OWPath)devicePaths.get(address);
	    OneWireContainer owc = StationConfiguration.getContainer(adapterList[value], portList[portValue], address, path);
	    System.out.println("Device " + address + " type " + owc.getName() + " path " + path);
	}
    }
    

//    private static void getPaths(String strAdapter, String port)
//    {
//	Vector<OWPath> pathsToSearch = new Vector<OWPath>();
//	boolean searchResult = false;
//	
//	/** hashtable for holding the OWPath objects for each device container. */
//	Hashtable<Long, OWPath> devicePathHash = new Hashtable<Long, OWPath>();
//	
//	/** Addresses of all current devices, mapped to their state count */
//	Hashtable<Long, Integer> deviceAddressHash = new Hashtable<Long, Integer>();
//
//	System.out.println("Searching at " + new Date());
//	      
//	try
//	{
//	    // get the adapter
//	    DSPortAdapter adapter = OneWireAccessProvider.getAdapter(strAdapter, port);
//	    
//	    // seed list with main branch
//	    pathsToSearch.addElement(new OWPath(adapter));
//
//            // aquire the adapter
//            adapter.beginExclusive(true);
//
//            // setup the search
//            adapter.setSearchAllDevices();
//            adapter.targetAllFamilies();
//            adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);
//
//	    // walk path and get all devices on branch.
//	    // if any switches are found, add them to the list of paths to search
//            // search through all of the paths
//            for(int i=0; i<pathsToSearch.size(); i++)
//            {
//                // set searches to not use reset
//                adapter.setNoResetSearch();
//
//                OWPath path = (OWPath)pathsToSearch.elementAt(i);
//                System.out.println("Searching path " + path);
//                
//                try
//                {
//                   // try to open the current path
//                   path.open();
//                }
//                catch(Exception e)
//                {
//                    // if opening the path failed, continue on to the next path
//                    System.out.println("Unable to open path " + path.toString());
//                    continue;
//                }
//
//                searchResult = adapter.findFirstDevice();
//                
//                // loop while devices found
//                while (searchResult)
//                {
//                   // get the 1-Wire address
//                   Long longAddress = new Long(adapter.getAddressAsLong());
//                   System.out.println("Found device" + Long.toHexString(longAddress));
//
//                   // check if the device allready exists in our hashtable
//                   if(!deviceAddressHash.containsKey(longAddress))
//                   {
//                      OneWireContainer owc = adapter.getDeviceContainer(longAddress.longValue());
//
//                      // check to see if it's a switch and if we are supposed
//                      // to automatically search down branches
//                      if(owc instanceof SwitchContainer) 
//                      {
//                	  System.out.println("device is a switch");
//                	  
//                         SwitchContainer sc = (SwitchContainer)owc;
//                         byte[] state = sc.readDevice();
//                         for(int j=0; j<sc.getNumberChannels(state); j++)
//                         {
//                            OWPath tmp = new OWPath(adapter, path);
//                            tmp.add(owc, j);
//                            if(!pathsToSearch.contains(tmp))
//                               pathsToSearch.addElement(tmp);
//                         }
//                      }
//
//                      synchronized(devicePathHash)
//                      {
//                         devicePathHash.put(longAddress, path);
//                      }
//                   }
//                   // check if the existing device moved
//                   else if (!path.equals((OWPath)devicePathHash.get(longAddress)))
//                   {
//                      synchronized(devicePathHash)
//                      {
//                         devicePathHash.put(longAddress, path);
//                      }
//                   }
//
//                   // update count
//                   deviceAddressHash.put(longAddress, new Integer(3));
//
//                   // find the next device on this branch
//                   path.open();
//                   searchResult = adapter.findNextDevice();
//                }
//             }
//
//            
//	    System.out.println("\nDone\n");
//	        
//	    // free up the serial port
//	    adapter.endExclusive();
//	    adapter.freePort();
//	}
//	catch (OneWireException e)
//	{
//	    System.out.println("Error Serching the 1-Wire Bus");
//	}
//	
//	// walk devicehashpath list
//	for (Enumeration<Long> keys = devicePathHash.keys(); keys.hasMoreElements(); )
//	{
//	    Long address = (Long)keys.nextElement();
//	    OWPath path = (OWPath)devicePathHash.get(address);
//	    System.out.println("Device " + Long.toHexString(address) + " path " + path);
//	}
//	
//    }

//    private static void walkNet(String strAdapter, String port)
//    {
//      System.out.println("Searching at " + new Date());
//      
//      try
//      {
//        // get the default adapter
//        DSPortAdapter adapter = OneWireAccessProvider.getAdapter(strAdapter, port);
//        Hashtable<String, WeatherSensor> sensors = new Hashtable<String, WeatherSensor>();
//        
//        adapter.reset();
//        
//        // search for all 1-wire devices & add to text area
//        System.out.println("Adapter: " + adapter.getAdapterName());
//        System.out.println("Port: " + adapter.getPortName() + "\n");
//        
//        // get exclusive use of adapter
//        adapter.beginExclusive(true);
//        
//        // search main bus with no couplers
//        searchActiveBranch(adapter, "", -1, sensors);
//        
//        // clear any previous search restrictions
//        adapter.setSearchAllDevices();
//        adapter.targetAllFamilies();
//        adapter.targetFamily(0x001f);
//        adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);
//        
//        // enumerate through all the 1-Wire devices found
//        Enumeration<?> owdEnum;
//        OneWireContainer owd;
//
//        // is there any DS2409 MicroLAN Couplers?
//        if (adapter.findFirstDevice())
//        {
//          for (owdEnum = adapter.getAllDeviceContainers(); owdEnum.hasMoreElements(); )
//          {
//            owd = (OneWireContainer)owdEnum.nextElement();
////            System.out.println("MicroLAN Coupler Found at " + owd.getAddressAsString());
//
////            System.out.println("Main Port Devices Found:");
////            activateCoupler(adapter, owd.getAddressAsString(), 0);
//            searchActiveBranch(adapter, owd.getAddressAsString(), 0, sensors);
////            deactivateCoupler(adapter, owd.getAddressAsString(), 0);
//
////            System.out.println("Aux Port Devices Found:");
////            activateCoupler(adapter, owd.getAddressAsString(), 1);
//            searchActiveBranch(adapter, owd.getAddressAsString(), 1, sensors);
////            deactivateCoupler(adapter, owd.getAddressAsString(), 1);
//          }
//        }
////        // if there aren't any couplers, scan the defualt bus
////        else
////          searchActiveBranch(adapter, sensors);
//        
//        // walk sensors list and print
//        for (Enumeration<WeatherSensor> e = sensors.elements(); e.hasMoreElements() ;) 
//        {
//            WeatherSensor sensor = e.nextElement();
//            System.out.println(sensor.toString());
//        }
//        
//        System.out.println("\nDone\n");
//        
//        // free up the serial port
//        adapter.endExclusive();
//        adapter.freePort();
//      }
//      catch (OneWireException e)
//      {
//        System.out.println("Error Serching the 1-Wire Bus");
//      }
//    }
//
//    private static void activateCoupler(DSPortAdapter adapter, String addr, int channel) throws OneWireException
//    {
//      byte[]  state;
//
//      OneWireContainer1F owc = new OneWireContainer1F(adapter, addr);
//      state = owc.readDevice();
//      owc.setLatchState(channel, true, false, state);
//      owc.writeDevice(state);
//    }
//
//    private static void deactivateCoupler(DSPortAdapter adapter, String addr, int chan) throws OneWireException
//    {
//      byte[]  state;
//      
//      OneWireContainer1F owc = new OneWireContainer1F(adapter, addr);
//      state = owc.readDevice();
//      owc.setLatchState(chan, false, false, state);
//      owc.writeDevice(state);
//    }
//    
//    private static boolean searchActiveBranch(DSPortAdapter adapter, String couplerAddress, int port, Hashtable<String, WeatherSensor> sensors) throws OneWireException
//    {
//      boolean found = false;
//      
//      if (couplerAddress != null && couplerAddress.length() > 0)
//      {
//	  activateCoupler(adapter, couplerAddress, port);
//      }
//      
//      // clear any previous search restrictions
//      adapter.setSearchAllDevices();
//      adapter.targetAllFamilies();
//      adapter.excludeFamily(0x001f);
//      adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);
//      
//      // enumerate through all the 1-Wire devices found
//      Enumeration<?> owdEnum;
//      OneWireContainer owd;
//      
//      for (owdEnum = adapter.getAllDeviceContainers(); owdEnum.hasMoreElements();)
//      {
//        owd = (OneWireContainer)owdEnum.nextElement();
////        WeatherSensor sensor = new WeatherSensor(owd.getAddressAsString(), 0, 0.0f, 0.0f, 0, 0, false, false, couplerAddress, port);
//        WeatherSensor sensor = new WeatherSensor(owd.getAddressAsString(), 0, 0.0f, 0.0f, 0, 0, false, false, null);
////        sensor.setCouplerAddress(couplerAddress);
////        sensor.setCouplerPort(port);
//        if (!sensors.containsKey(owd.getAddressAsString()))
//        {
//            sensors.put(owd.getAddressAsString(), sensor);
////          System.out.println("Addr: " + owd.getAddressAsString() + " P/N: " + owd.getName()); // + " Desc: " + owd.getDescription());
//        }
//        found = true;
//      }
//      
////      if (!found)
////        System.out.println("None");
//      
//      if (couplerAddress != null && couplerAddress.length() > 0)
//      {
//	  deactivateCoupler(adapter, couplerAddress, port);
//      }
//      
//      return found;
//    }
}

