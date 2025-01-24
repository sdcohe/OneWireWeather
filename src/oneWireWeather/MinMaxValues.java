/* SVN FILE: $Id: MinMaxValues.java 183 2015-12-06 20:58:06Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 183 $
* $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $LastChangedBy: seth $
* $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/MinMaxValues.java $
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
import java.io.*;

/**
 * Hold the minimum and maximum values, along with the associated dates, for a
 * given weather variable.
 * 
 * @author Seth Cohen
 * 
 */
public class MinMaxValues implements Serializable
{

    private static final long serialVersionUID = 1L;

//    private static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private float m_minValue;
    private float m_maxValue;
    private Date m_minDate;
    private Date m_maxDate;

    /**
     * Constructor. Initialize min and max values to min and maximum values for
     * a float. Initialize dates to today's date and time.
     */
    public MinMaxValues()
    {
	m_minValue = Float.MAX_VALUE;
	m_maxValue = Float.MIN_VALUE;

	// initialize dates?
	m_minDate = new Date();
	m_maxDate = new Date();
    }

    public String toXML()
    {
	StringBuilder str = new StringBuilder();
	SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);

	str.append("  <MIN value=\"" + m_minValue + "\" date=\""
		+ fmt.format(m_minDate) + "\"/>");
	str.append("  <MAX value=\"" + m_maxValue + "\" date=\""
		+ fmt.format(m_maxDate) + "\"/>");

	return str.toString();
    }

    public void fromXML(String xmlString)
    {
	m_minValue = Float.MAX_VALUE;
	m_maxValue = Float.MIN_VALUE;

	// initialize dates?
	m_minDate = new Date();
	m_maxDate = new Date();

	int minStartPos = xmlString.indexOf("<MIN");
	int maxStartPos = xmlString.indexOf("<MAX");
	int endPos;
	String data;
	SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);

	// get min value and date
	if (minStartPos >= 0)
	{
	    endPos = xmlString.indexOf("/>", minStartPos);
	    data = xmlString.substring(minStartPos, endPos + 2);

	    String value = getAttribute("value", data);
	    m_minValue = Float.parseFloat(value);

	    value = getAttribute("date", data);
	    try
	    {
		m_minDate = fmt.parse(value);
	    }
	    catch (ParseException e)
	    {
		e.printStackTrace();
	    }
	}

	// get max value and date
	if (maxStartPos >= 0)
	{
	    endPos = xmlString.indexOf("/>", maxStartPos);
	    data = xmlString.substring(maxStartPos, endPos + 2);

	    String value = getAttribute("value", data);
	    m_maxValue = Float.parseFloat(value);

	    value = getAttribute("date", data);
	    try
	    {
		m_maxDate = fmt.parse(value);
	    }
	    catch (ParseException e)
	    {
		e.printStackTrace();
	    }
	}
    }

    private String getAttribute(String attribute, String data)
    {
	int startPos;
	int endPos;

	startPos = data.indexOf(attribute);
	startPos += attribute.length() + 2;
	endPos = data.indexOf("\"", startPos);
	String value = data.substring(startPos, endPos);
	return value;
    }

    /**
     * Retrieve the date of the maximum value.
     * 
     * @return the date associated with the maximum value
     */
    public Date getMaxDate()
    {
	return m_maxDate;
    }

    /**
     * Retrieve the maximum value.
     * 
     * @return the maximum value
     */
    public float getMaxValue()
    {
	return m_maxValue;
    }

    /**
     * Retrieve the date of the minimum value.
     * 
     * @return the date associated with the minimum value
     */
    public Date getMinDate()
    {
	return m_minDate;
    }

    /**
     * Retrieve the minimum value.
     * 
     * @return the minimum value
     */
    public float getMinValue()
    {
	return m_minValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
	return "Min " + m_minValue + " at " + m_minDate + " Max " + m_maxValue
		+ " at " + m_maxDate;
    }

    /**
     * Update the saved minimum and maximum values and dates, using the passed
     * in values.
     * 
     * @param value
     *            The new value
     * @param date
     *            The date of the new value
     */
    public void updateValues(float value, Date date)
    {
        //***TODO: update if date has changed
	if (value > m_maxValue || m_maxValue == Float.MIN_VALUE)
	{
	    m_maxValue = value;
	    m_maxDate = date;
	}

	if (((value != Float.MIN_VALUE) && (value < m_minValue))
		|| m_minValue == Float.MAX_VALUE)
	{
	    m_minValue = value;
	    m_minDate = date;
	}
    }
}
