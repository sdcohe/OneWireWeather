/* SVN FILE: $Id: CumulativeValues.java 277 2018-01-07 15:28:17Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 277 $
* $Date: 2018-01-07 10:28:17 -0500 (Sun, 07 Jan 2018) $
* $LastChangedBy: seth $
* $LastChangedDate: 2018-01-07 10:28:17 -0500 (Sun, 07 Jan 2018) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/CumulativeValues.java $
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

import java.util.*;
import java.io.*;

/**
 * Track the cumulative values for the weather station measurements.
 * This includes the min and max values for the current hour, day, week,
 * month, year, and cumulative.
 * 
 * @author Seth Cohen
 *
 */
public class CumulativeValues implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String HOURLY_ELEMENT = "<HOURLY>";
    private static final String HOURLY_END_ELEMENT = "</HOURLY>";
    private static final String DAILY_ELEMENT = "<DAILY>";
    private static final String DAILY_END_ELEMENT = "</DAILY>";
    private static final String WEEKLY_ELEMENT = "<WEEKLY>";
    private static final String WEEKLY_END_ELEMENT = "</WEEKLY>";
    private static final String MONTHLY_ELEMENT = "<MONTHLY>";
    private static final String MONTHLY_END_ELEMENT = "</MONTHLY>";
    private static final String ANNUAL_ELEMENT = "<ANNUAL>";
    private static final String ANNUAL_END_ELEMENT = "</ANNUAL>";
    private static final String CUM_ELEMENT = "<CUM>";
    private static final String CUM_END_ELEMENT = "</CUM>";
    private static final String YESTERDAY_ELEMENT = "<YESTERDAY>";
    private static final String YESTERDAY_END_ELEMENT = "</YESTERDAY>";

    // cum values
    private MinMaxValues m_cumValues = new MinMaxValues();

    // annual values
    private MinMaxValues m_annualValues = new MinMaxValues();
    private int m_currentYear;

    // monthly values
    private MinMaxValues m_monthlyValues = new MinMaxValues();
    private int m_currentMonth;

    // weekly values
    private MinMaxValues m_weeklyValues = new MinMaxValues();
    private int m_currentWeek;

    // daily values
    private MinMaxValues m_dailyValues = new MinMaxValues();
    private int m_currentDay;

    // yesterday's values
    private MinMaxValues m_yesterdayValues = new MinMaxValues();

    // hourly values
    private MinMaxValues m_hourlyValues = new MinMaxValues();
    private int m_currentHour;

    /**
     * Constructor to initialize all values to the current date
     */
    public CumulativeValues()
    {
        Calendar now = Calendar.getInstance();
        m_currentHour = now.get(Calendar.HOUR);
        m_currentDay = now.get(Calendar.DATE);
        m_currentWeek = now.get(Calendar.WEEK_OF_YEAR);
        m_currentMonth = now.get(Calendar.MONTH);
        m_currentYear = now.get(Calendar.YEAR);
    }

    public CumulativeValues(Calendar initDate)
    {
        m_currentHour = initDate.get(Calendar.HOUR);
        m_currentDay = initDate.get(Calendar.DATE);
        m_currentWeek = initDate.get(Calendar.WEEK_OF_YEAR);
        m_currentMonth = initDate.get(Calendar.MONTH);
        m_currentYear = initDate.get(Calendar.YEAR);
    }

    /**
     * Get the hourly values
     * 
     * @return the hourly values as a MinMaxValues class
     */
    public MinMaxValues getHourlyValues()
    {
	return m_hourlyValues;
    }

    /**
     * Get the daily values
     * 
     * @return the daily values as a MinMaxValues class
     */
    public MinMaxValues getDailyValues()
    {
	return m_dailyValues;
    }

    public MinMaxValues getYesterdayValues()
    {
	return m_yesterdayValues;
    }

    /**
     * Get the weekly values
     * 
     * @return the weekly values as a MinMaxValues class
     */
    public MinMaxValues getWeeklyValues()
    {
	return m_weeklyValues;
    }

    /**
     * Get the monthly values
     * 
     * @return the monthly values as a MinMaxValues class
     */
    public MinMaxValues getMonthlyValues()
    {
	return m_monthlyValues;
    }

    /**
     * Get the yearly values
     * 
     * @return the yearly values as a MinMaxValues class
     */
    public MinMaxValues getYearlyValues()
    {
	return m_annualValues;
    }

    /**
     * Get the cumulative values
     * 
     * @return the cumulative values as a MinMaxValues class
     */
    public MinMaxValues getCumulativeValues()
    {
	return m_cumValues;
    }

    /**
     * Update all the stored sample values as needed based on the given
     * sample value and value date.
     * 
     * @param value The new sample value to store
     * @param date	The date of the new sample.
     */
    public void updateValues(float value, Date date)
    {
	// check for reset of totals
	Calendar valueDate = Calendar.getInstance();
	valueDate.setTime(date);
	int hour = valueDate.get(Calendar.HOUR_OF_DAY);
	int day = valueDate.get(Calendar.DAY_OF_MONTH);
	int week = valueDate.get(Calendar.WEEK_OF_YEAR);
	int month = valueDate.get(Calendar.MONTH);
	int year = valueDate.get(Calendar.YEAR);

	if (m_currentHour != hour)
	{
	    m_currentHour = hour;
	    m_hourlyValues = new MinMaxValues();
	}

	if (m_currentDay != day)
	{
	    m_currentDay = day;
	    m_yesterdayValues = m_dailyValues;
	    m_dailyValues = new MinMaxValues();
	}

	if (m_currentWeek != week)
	{
	    m_currentWeek = week;
	    m_weeklyValues = new MinMaxValues();
	}

	if (m_currentMonth != month)
	{
	    m_currentMonth = month;
	    m_monthlyValues = new MinMaxValues();
	}

	if (m_currentYear != year)
	{
	    m_currentYear = year;
	    m_annualValues = new MinMaxValues();
	}

	// update values
	m_hourlyValues.updateValues(value, date);
	m_dailyValues.updateValues(value, date);
	m_weeklyValues.updateValues(value, date);
	m_monthlyValues.updateValues(value, date);
	m_annualValues.updateValues(value, date);
	m_cumValues.updateValues(value, date);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
	StringBuilder out = new StringBuilder();

	out.append("Hourly: " + m_hourlyValues.toString() + "\n");
	out.append("Daily: " + m_dailyValues.toString() + "\n");
	out.append("Yesterday: " + m_yesterdayValues.toString() + "\n");
	out.append("Weekly: " + m_weeklyValues.toString() + "\n");
	out.append("Monthly: " + m_monthlyValues.toString() + "\n");
	out.append("Annual: " + m_annualValues.toString() + "\n");
	out.append("Cum: " + m_cumValues.toString());

	return out.toString();
    }

    public String toXML()
    {
	StringBuilder out = new StringBuilder();

	out.append(HOURLY_ELEMENT);
	out.append(m_hourlyValues.toXML());
	out.append(HOURLY_END_ELEMENT);

	out.append(DAILY_ELEMENT);
	out.append(m_dailyValues.toXML());
	out.append(DAILY_END_ELEMENT);

	out.append(YESTERDAY_ELEMENT);
	out.append(m_yesterdayValues.toXML());
	out.append(YESTERDAY_END_ELEMENT);

	out.append(WEEKLY_ELEMENT);
	out.append(m_weeklyValues.toXML());
	out.append(WEEKLY_END_ELEMENT);

	out.append(MONTHLY_ELEMENT);
	out.append(m_monthlyValues.toXML());
	out.append(MONTHLY_END_ELEMENT);

	out.append(ANNUAL_ELEMENT);
	out.append(m_annualValues.toXML());
	out.append(ANNUAL_END_ELEMENT);

	out.append(CUM_ELEMENT);
	out.append(m_cumValues.toXML());
	out.append(CUM_END_ELEMENT);

	return out.toString();
    }

    public void fromXML(String xmlString)
    {
	m_hourlyValues.fromXML(getData(xmlString, HOURLY_ELEMENT, HOURLY_END_ELEMENT));
	m_dailyValues.fromXML(getData(xmlString, DAILY_ELEMENT, DAILY_END_ELEMENT));
	m_yesterdayValues.fromXML(getData(xmlString, YESTERDAY_ELEMENT, YESTERDAY_END_ELEMENT));
	m_weeklyValues.fromXML(getData(xmlString, WEEKLY_ELEMENT, WEEKLY_END_ELEMENT));
	m_monthlyValues.fromXML(getData(xmlString, MONTHLY_ELEMENT, MONTHLY_END_ELEMENT));
	m_annualValues.fromXML(getData(xmlString, ANNUAL_ELEMENT, ANNUAL_END_ELEMENT));
	m_cumValues.fromXML(getData(xmlString, CUM_ELEMENT, CUM_END_ELEMENT));
	
//        Calendar now = Calendar.getInstance();
//        m_currentHour = now.get(Calendar.HOUR);
//        m_currentDay = now.get(Calendar.DATE);
//        m_currentWeek = now.get(Calendar.WEEK_OF_YEAR);
//        m_currentMonth = now.get(Calendar.MONTH);
//        m_currentYear = now.get(Calendar.YEAR);
	
    }

    private String getData(String xmlString, String startString, String endString)
    {
	int startPos;
	int endPos;
	String data = "";

	startPos = xmlString.indexOf(startString);
	endPos = xmlString.indexOf(endString);
	if (startPos != -1 && endPos >= startPos)
	{
	    data = xmlString.substring(startPos + startString.length(), endPos);
	}

	return data;
    }

}
