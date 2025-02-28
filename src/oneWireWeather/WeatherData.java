/* SVN FILE: $Id: WeatherData.java 314 2019-06-18 18:29:02Z  $ */
/**
 * One Wire Weather : Weather station daemon for a 1-wire weather station
 *
 * $Author: $
 * $Revision: 314 $
 * $Date: 2019-06-18 14:29:02 -0400 (Tue, 18 Jun 2019) $
 * $LastChangedBy: $
 * $LastChangedDate: 2019-06-18 14:29:02 -0400 (Tue, 18 Jun 2019) $
 * $URL: http://192.168.123.7/svn/OneWireWeather/src/oneWireWeather/WeatherData.java $
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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Seth Cohen
 * 
 */
public class WeatherData implements Serializable // , DocumentHandler
{

	private static final long serialVersionUID = 1L;

	private long dateTimeMillis = 0L;
	private float temperature = 0.0f;
	private float humidity = 0.0f;
	private float windSpeed = 0.0f;
	private int windDirection = 0;
	private float rainFall = 0.0f;
	private float pressure = 0.0f;
	private float solar = 0.0f;
	private int lightning = 0;
	private float indoorTemperature = 0.0f;
	private float indoorHumidity = 0.0f;
	private float averageWindSpeed = 0.0f;
	private int averageWindDirection = 0;

	private HashMap<String, Object> extraSensors = new HashMap<String, Object>();

	// private String currentElement;
	// private String currentAuxId;

	private static final String WEATHER_DATA_ELEMENT = "WEATHERDATAENTRY";
	private static final String DATE_TIME_ELEMENT = "DATETIME";
	private static final String OUTDOOR_TEMPERATURE_ELEMENT = "OUTDOORTEMPERATURE";
	private static final String OUTDOOR_HUMIDITY_ELEMENT = "OUTDOORHUMIDITY";
	private static final String PRESSURE_ELEMENT = "PRESSURE";
	private static final String WIND_SPEED_ELEMENT = "WINDSPEED";
	private static final String WIND_DIRECTION_ELEMENT = "WINDDIRECTION";
	private static final String RAINFALL_ELEMENT = "RAINFALL";
	private static final String AUX_ELEMENT = "AUX";
	private static final String SOLAR_ELEMENT = "SOLAR";
	private static final String LIGHTNING_ELEMENT = "LIGHTNING";
	private static final String INDOOR_TEMPERATURE_ELEMENT = "INDOORTEMPERATURE";
	private static final String INDOOR_HUMIDITY_ELEMENT = "INDOORHUMIDITY";
	private static final String AVERAGE_WIND_SPEED_ELEMENT = "AVERAGEWINDSPEED";
	private static final String AVERAGE_WIND_DIRECTION_ELEMENT = "AVERAGEWINDDIRECTION";

	// private static final String ID_ELEMENT = "ID";
	private static final String NAME_ELEMENT = "NAME";

	//    private static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss";
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	/**
	 * Convert this WeatherData instance to an XML string
	 * 
	 * @param config
	 *            - the station configuration
	 * @return - an XML string
	 */
	public String toXML(StationConfiguration config)
	{
		StringBuilder strBuf = new StringBuilder();
		SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);

		addStartTag(strBuf, WEATHER_DATA_ELEMENT);

		addElement(strBuf, DATE_TIME_ELEMENT,
				fmt.format(new Date(this.getDateTimeMillis())));
		addElement(strBuf, OUTDOOR_TEMPERATURE_ELEMENT,
				Float.toString(this.getTemperature()));
		addElement(strBuf, OUTDOOR_HUMIDITY_ELEMENT,
				Float.toString(this.getHumidity()));
		addElement(strBuf, PRESSURE_ELEMENT, Float.toString(this.getPressure()));
		addElement(strBuf, WIND_SPEED_ELEMENT,
				Float.toString(this.getWindSpeed()));
		addElement(strBuf, WIND_DIRECTION_ELEMENT,
				Integer.toString(this.getWindDirection()));
		addElement(strBuf, RAINFALL_ELEMENT, Float.toString(this.getRainfall()));
		addElement(strBuf, INDOOR_TEMPERATURE_ELEMENT,
				Float.toString(this.getIndoorTemperature()));
		addElement(strBuf, INDOOR_HUMIDITY_ELEMENT,
				Float.toString(this.getIndoorHumidity()));
		addElement(strBuf, SOLAR_ELEMENT, Float.toString(this.getSolar()));
		addElement(strBuf, LIGHTNING_ELEMENT,
				Integer.toString(this.getLightning()));
		addElement(strBuf, AVERAGE_WIND_SPEED_ELEMENT,
				Float.toString(this.getAverageWindSpeed()));
		addElement(strBuf, AVERAGE_WIND_DIRECTION_ELEMENT,
				Integer.toString(this.getAverageWindDirection()));

		// look at aux sensors
		String[][] values = this.extraSensors();
		for (int i = 0; i < values.length; i++)
		{
			String value = values[i][1];
			String key = values[i][0];
			SensorConfiguration sensor = config.findSensorByID(key);
			if (sensor != null)
			{
				strBuf.append("<" + AUX_ELEMENT + " ID=\"" + key + "\" "
						+ NAME_ELEMENT + "=\"" + sensor.getName() + "\">"
						+ value + "</" + AUX_ELEMENT + ">");
			}
			else
			{
				strBuf.append("<" + AUX_ELEMENT + " ID=\"" + key + "\" >"
						+ value + "</" + AUX_ELEMENT + ">");
			}
		}

		addEndTag(strBuf, WEATHER_DATA_ELEMENT);

		return strBuf.toString();
	}

	/**
	 * @param xmlString
	 */
	public void fromXML(String xmlString)
	{
		extraSensors.clear();

		String data;
		data = getStringData(xmlString, WEATHER_DATA_ELEMENT);

		Date date = getDateData(data, DATE_TIME_ELEMENT);
		this.dateTimeMillis = date.getTime();

		this.temperature = getFloatData(data, OUTDOOR_TEMPERATURE_ELEMENT);
		this.humidity = getFloatData(data, OUTDOOR_HUMIDITY_ELEMENT);
		this.pressure = getFloatData(data, PRESSURE_ELEMENT);
		this.windSpeed = getFloatData(data, WIND_SPEED_ELEMENT);
		this.windDirection = (int) getLongData(data, WIND_DIRECTION_ELEMENT);
		this.rainFall = getFloatData(data, RAINFALL_ELEMENT);
		this.solar = getFloatData(data, SOLAR_ELEMENT);
		this.lightning = (int) getLongData(data, LIGHTNING_ELEMENT);
		this.indoorTemperature = getFloatData(data, INDOOR_TEMPERATURE_ELEMENT);
		this.indoorHumidity = getFloatData(data, INDOOR_HUMIDITY_ELEMENT);
		this.averageWindSpeed = getFloatData(data, AVERAGE_WIND_SPEED_ELEMENT);
		this.averageWindDirection = (int) getLongData(data,
				AVERAGE_WIND_DIRECTION_ELEMENT);

		// int auxPos = data.indexOf(AUX_ELEMENT);
		// HashMap<String, Object> auxValues = new HashMap<String, Object>();
		//
		// while (auxPos != -1) {
		//
		// String element = getStringData(data, AUX_ELEMENT);
		//
		// // get ID and value from element
		// String id = getAttribute(element, "ID");
		// String name = getAttribute(element, NAME_ELEMENT);
		// String Value = getFloatData(xmlData, tag)
		//
		// // need to correct auxPos for finding next element
		// auxPos += element.length() + AUX_ELEMENT.length();
		// auxPos = data.indexOf(AUX_ELEMENT, auxPos + 1);
		// }
		// // AUX_ELEMENT = "AUX";
		// // find each aux sensor - may be multiple
		// // find 1st sensor
		// // while sensor found
		// // add sensor
		// // find next sensor

		// SAXParser parser = new SAXParser();
		// parser.setDocumentHandler(this);
		// InputSource source = new InputSource(new StringReader(xmlString));
		// try {
		// parser.parse(source);
		// } catch (SAXException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	// private String getAttribute(String xmlData, String tag)
	// {
	// String value = "";
	// int startPos = xmlData.indexOf(tag);
	// if (startPos >= 0)
	// {
	// startPos = xmlData.indexOf("\"", startPos + 1);
	// int endPos = xmlData.indexOf("\"", startPos);
	//
	// if (startPos >= 0 && endPos > startPos)
	// {
	// value = xmlData.substring(startPos, endPos);
	// }
	// }
	//
	// return value;
	// }

	private String getStringData(String xmlData, String tag)
	{
		String startTag = "<" + tag + ">";
		String endTag = "</" + tag + ">";

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

	private Date getDateData(String xmlData, String tag)
	{
		SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
		Date value;

		String dateData = getStringData(xmlData, tag);
		try
		{
			value = fmt.parse(dateData);
		}
		catch (ParseException ex)
		{
			value = new Date(0);
			ErrorLog.logError("WeatherData:Error parsing date " + dateData);
		}

		return value;
	}

	private float getFloatData(String xmlData, String tag)
	{
		String value = getStringData(xmlData, tag);
		try
		{
			return Float.parseFloat(value);
		}
		catch (NumberFormatException ex)
		{
			ErrorLog.logError("WeatherData:Error parsing float" + value);
			return 0.0f;
		}
	}

	private long getLongData(String xmlData, String tag)
	{
		String value = getStringData(xmlData, tag);
		try
		{
			return Long.parseLong(value);
		}
		catch (NumberFormatException ex)
		{
			ErrorLog.logError("WeatherData:Error parsing long" + value);
			return 0L;
		}
	}

	/**
	 * Convert this WeatherData instance to a string.
	 * 
	 * @param config
	 *            - the station configuration
	 * @return - a string containing the weather data
	 */
	public String toString(StationConfiguration config)
	{
		StringBuilder strBuf = new StringBuilder();

		strBuf.append("Weather at: " + this.getSampleDate() + "\n");
		strBuf.append("  Temp: Current "
				+ WeatherConversions.formatFloat(this.getTemperature(), 2)
				+ "\n");
		strBuf.append("  Wind Speed: "
				+ WeatherConversions.formatFloat(this.getWindSpeed(), 2) + " "
				+ WeatherConversions.windSpeedToLiteralMPH(this.getWindSpeed())
				+ " Direction: " + this.getWindDirection() + " "
				+ WeatherConversions.getWindDirStr(this.getWindDirection())
				+ "\n");
		strBuf.append("  Heat index: "
				+ WeatherConversions.formatFloat(
						WeatherConversions.calcHeatIndexFahrenheit(
								this.getTemperature(), this.getHumidity()), 2)
				+ "\n");
		strBuf.append("  Humidity: "
				+ WeatherConversions.formatFloat(this.getHumidity(), 2)
				+ " Dewpoint "
				+ WeatherConversions.formatFloat(
						WeatherConversions.calcDewpointFahrenheit(
								this.getTemperature(), this.getHumidity()), 2)
				+ "\n");
		strBuf.append("  Pressure: "
				+ WeatherConversions.formatFloat(this.getPressure(), 2)
				+ "\n");
		strBuf.append("   Indoor temp: "
				+ WeatherConversions.formatFloat(this.getIndoorTemperature(), 2)
				+ " humidity: "
				+ WeatherConversions.formatFloat(this.getIndoorHumidity(), 2) + "\n");
		strBuf.append("   Solar: "
				+ WeatherConversions.formatFloat(this.getSolar(), 2));
		strBuf.append("   Lightning: " + this.getLightning() + "\n");

		// look at aux sensors
		String[][] values = this.extraSensors();
		for (int i = 0; i < values.length; i++)
		{
			String value = values[i][1];
			String key = values[i][0];
			SensorConfiguration sensor = config.findSensorByID(key);
			String name;
			if (sensor != null)
			{
				name = sensor.getName();
				switch (sensor.getUsageType())
				{
				default:
					strBuf.append("  " + name + " "
							+ WeatherConversions.formatFloat(value, 2)
							+ "\n");
					break;
				}
			}
			else
			{
				strBuf.append("  " + key + " " + value + "%\n");
			}
		}

		return strBuf.toString();
	}

	/**
	 * @param millis
	 * @param temp
	 * @param humidity
	 * @param windSpeed
	 * @param direction
	 * @param rain
	 * @param pressure
	 */
	public WeatherData(long millis, float temp, float humidity,
			float windSpeed, int direction, float rain, float pressure,
			float indoorTemperature, float indoorHumidity, float solar,
			int lightning)
	{
		this.dateTimeMillis = millis;
		this.temperature = temp;
		this.humidity = humidity;
		this.windSpeed = windSpeed;
		this.windDirection = direction;
		this.rainFall = rain;
		this.pressure = pressure;
		this.indoorTemperature = indoorTemperature;
		this.indoorHumidity = indoorHumidity;
		this.solar = solar;
		this.lightning = lightning;
	}

	/**
	 * @param xml
	 */
	public WeatherData(String xml)
	{
		this.fromXML(xml);
	}

	/**
	 * Get the data and time in millis of this reading
	 * 
	 * @return - the date and time in millis
	 */
	public long getDateTimeMillis()
	{
		return dateTimeMillis;
	}

	/**
	 * Return the date and time of this reading as a Date
	 * 
	 * @return - the date and time this sample was taken
	 */
	public Date getSampleDate()
	{
		return new Date(this.dateTimeMillis);
	}

	/**
	 * Get the outside temperature in degrees F
	 * 
	 * @return - the outside temperature in degrees F
	 */
	public float getTemperature()
	{
		return temperature;
	}

	/**
	 * Get the wind speed in mph
	 * 
	 * @return - the wind speed in mph
	 */
	public float getWindSpeed()
	{
		return windSpeed;
	}

	/**
	 * Get the wind direction as a position of 0 - 15. If the reading had an
	 * error, return 16.
	 * 
	 * @return - the wind direction
	 */
	public int getWindDirection()
	{
		return windDirection;
	}

	/**
	 * Get the relative humidity reading
	 * 
	 * @return - the relative humidity readng as a percent
	 */
	public float getHumidity()
	{
		return humidity;
	}

	/**
	 * @return - the accumulated rainfall in inches
	 */
	public float getRainfall()
	{
		return rainFall;
	}

	/**
	 * @return - the barometric pressure reading
	 */
	public float getPressure()
	{
		return pressure;
	}

	/**
	 * Get the sensor data for any extra sensors
	 * 
	 * @return - an array of Strings containing the sensors values and IDs
	 */
	public String[][] extraSensors()
	{
		String[][] values = new String[extraSensors.size()][2];
		int i = 0;

		Iterator<Map.Entry<String, Object>> iter = extraSensors.entrySet()
				.iterator();
		while (iter.hasNext())
		{
			Map.Entry<String, Object> entry = iter.next();
			String value = entry.getValue().toString();
			String key = entry.getKey().toString();
			values[i][0] = key;
			values[i][1] = value;

			i++;
		}

		return values;
	}

	// **** do not make available as a web service method

	/**
	 * @param auxValues
	 */
	public void putExtraSensorValues(HashMap<String, Object> auxValues)
	{
		extraSensors.putAll(auxValues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#characters(char[], int, int)
	 */
	// public void characters(char[] arg0, int arg1, int arg2) throws
	// SAXException
	// {
	// String value = "";
	// String name = this.currentElement;
	//
	// if (!name.equalsIgnoreCase(WEATHER_DATA_ELEMENT) &&
	// !name.equalsIgnoreCase("DOCUMENT"))
	// {
	// value = new String(arg0, arg1, arg2).trim();
	// }
	// // System.out.println("Name: " + name + " Value: " + value);
	//
	// if (name.equalsIgnoreCase(DATE_TIME_ELEMENT))
	// {
	// SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
	// Date dt;
	// try {
	// dt = fmt.parse(value);
	// dateTimeMillis = dt.getTime();
	// } catch (ParseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// if (name.equalsIgnoreCase(OUTDOOR_TEMPERATURE_ELEMENT))
	// {
	// temperature = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(OUTDOOR_HUMIDITY_ELEMENT))
	// {
	// humidity = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(PRESSURE_ELEMENT))
	// {
	// pressure = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(WIND_SPEED_ELEMENT))
	// {
	// windSpeed = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(WIND_DIRECTION_ELEMENT))
	// {
	// windDirection = Integer.parseInt(value);
	// }
	//
	// if (name.equalsIgnoreCase(RAINFALL_ELEMENT))
	// {
	// rainFall = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(INDOOR_TEMPERATURE_ELEMENT))
	// {
	// this.indoorTemperature = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(INDOOR_HUMIDITY_ELEMENT))
	// {
	// this.indoorHumidity = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(SOLAR_ELEMENT))
	// {
	// this.solar = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(LIGHTNING_ELEMENT))
	// {
	// this.lightning = Integer.parseInt(value);
	// }
	//
	// if (name.equalsIgnoreCase(AVERAGE_WIND_SPEED_ELEMENT))
	// {
	// this.averageWindSpeed = Float.parseFloat(value);
	// }
	//
	// if (name.equalsIgnoreCase(AVERAGE_WIND_DIRECTION_ELEMENT))
	// {
	// this.averageWindDirection = Integer.parseInt(value);
	// }
	//
	// if (name.equalsIgnoreCase(AUX_ELEMENT))
	// {
	// String id = this.currentAuxId;
	// // System.out.println("Adding sensor data: " + id + " " + value);
	// extraSensors.put(id, value);
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#endDocument()
	 */
	// public void endDocument() throws SAXException {
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#endElement(java.lang.String)
	 */
	// public void endElement(String arg0) throws SAXException {
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#ignorableWhitespace(char[], int, int)
	 */
	// public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws
	// SAXException {
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#processingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	// public void processingInstruction(String arg0, String arg1) throws
	// SAXException {
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	// public void setDocumentLocator(Locator arg0) {
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#startDocument()
	 */
	// public void startDocument() throws SAXException {
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.DocumentHandler#startElement(java.lang.String,
	 * org.xml.sax.AttributeList)
	 */
	// public void startElement(String name, AttributeList atts) throws
	// SAXException {
	//
	// this.currentElement = name;
	//
	// // if (atts.getLength() > 0)
	// // {
	// // System.out.println("Atts for: " + name);
	// // for (int i = 0; i < atts.getLength(); i++)
	// // {
	// // System.out.println(atts.getName(i) + " " + atts.getValue(i));
	// // }
	// // }
	//
	// if (name.equalsIgnoreCase(AUX_ELEMENT))
	// {
	// String id = atts.getValue(ID_ELEMENT);
	// this.currentAuxId = id;
	// // System.out.println("Aux id = " + this.currentAuxId);
	// }
	// else
	// {
	// this.currentAuxId = "";
	// }
	//
	// }

	private void addElement(StringBuilder buf, String element, String value)
	{
		addStartTag(buf, element);
		buf.append(value);
		addEndTag(buf, element);
	}

	private void addStartTag(StringBuilder buf, String element)
	{
		buf.append("<" + element + ">");
	}

	private void addEndTag(StringBuilder buf, String element)
	{
		buf.append("</" + element + ">");
	}

	/**
	 * @return the indoorHumidity
	 */
	public float getIndoorHumidity()
	{
		return indoorHumidity;
	}

	/**
	 * @param indoorHumidity
	 *            the indoorHumidity to set
	 */
	public void setIndoorHumidity(float indoorHumidity)
	{
		this.indoorHumidity = indoorHumidity;
	}

	/**
	 * @return the indoorTemperature
	 */
	public float getIndoorTemperature()
	{
		return indoorTemperature;
	}

	/**
	 * @param indoorTemperature
	 *            the indoorTemperature to set
	 */
	public void setIndoorTemperature(float indoorTemperature)
	{
		this.indoorTemperature = indoorTemperature;
	}

	/**
	 * @return the lightning
	 */
	public int getLightning()
	{
		return lightning;
	}

	/**
	 * @param lightning
	 *            the lightning to set
	 */
	public void setLightning(int lightning)
	{
		this.lightning = lightning;
	}

	/**
	 * @return the solar
	 */
	public float getSolar()
	{
		return solar;
	}

	/**
	 * @param solar
	 *            the solar to set
	 */
	public void setSolar(float solar)
	{
		this.solar = solar;
	}

	/**
	 * @return the averageWindSpeed
	 */
	public float getAverageWindSpeed()
	{
		return averageWindSpeed;
	}

	/**
	 * @param averageWindSpeed
	 *            the averageWindSpeed to set
	 */
	public void setAverageWindSpeed(float averageWindSpeed)
	{
		this.averageWindSpeed = averageWindSpeed;
	}

	public void setAverageWindDirection(int averageWindDirection)
	{
		this.averageWindDirection = averageWindDirection;
	}

	public int getAverageWindDirection()
	{
		return this.averageWindDirection;
	}
}
