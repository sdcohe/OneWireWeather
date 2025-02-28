/* SVN FILE: $Id: CumulativeCounts.java 300 2019-01-10 18:01:16Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 300 $
* $Date: 2019-01-10 13:01:16 -0500 (Thu, 10 Jan 2019) $
* $LastChangedBy: seth $
* $LastChangedDate: 2019-01-10 13:01:16 -0500 (Thu, 10 Jan 2019) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/CumulativeCounts.java $
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
import java.util.Calendar;
import java.util.Date;

/**
 * @author Seth Cohen 1/29/2007
 *
 */
public class CumulativeCounts implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
//    private static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    
    private static final String LAST_CHANGE_DATE_START_ELEMENT = "<LASTCHANGEDATE>";
    private static final String LAST_CHANGE_DATE_END_ELEMENT = "</LASTCHANGEDATE>";
    private static final String LAST_SAMPLE_DATE_START_ELEMENT = "<LASTSAMPLEDATE>";
    private static final String LAST_SAMPLE_DATE_END_ELEMENT = "</LASTSAMPLEDATE>";
    private static final String LAST_SAMPLE_VALUE_START_ELEMENT = "<LASTSAMPLEVALUE>";
    private static final String LAST_SAMPLE_VALUE_END_ELEMENT = "</LASTSAMPLEVALUE>";
    // minute counts
    // hour counts
    private static final String DAY_COUNT_START_ELEMENT = "<DAYCOUNT>";
    private static final String DAY_COUNT_END_ELEMENT = "</DAYCOUNT>";
    private static final String YESTERDAY_COUNT_START_ELEMENT = "<YESTERDAYCOUNT>";
    private static final String YESTERDAY_COUNT_END_ELEMENT = "</YESTERDAYCOUNT>";
    private static final String WEEK_COUNT_START_ELEMENT = "<WEEKCOUNT>";
    private static final String WEEK_COUNT_END_ELEMENT = "</WEEKCOUNT>";
    private static final String MONTH_COUNT_START_ELEMENT = "<MONTHCOUNT>";
    private static final String MONTH_COUNT_END_ELEMENT = "</MONTHCOUNT>";
    private static final String YEAR_COUNT_START_ELEMENT = "<YEARCOUNT>";
    private static final String YEAR_COUNT_END_ELEMENT = "</YEARCOUNT>";
//    private static final String CUM_COUNT_START_ELEMENT = "<CUMCOUNT>";
//    private static final String CUM_COUNT_END_ELEMENT = "</CUMCOUNT>";
    private static final String WEEK_MAX_DAY_COUNT_START_ELEMENT = "<WEEKMAXCOUNT>";
    private static final String WEEK_MAX_DAY_COUNT_END_ELEMENT = "</WEEKMAXCOUNT>";
    private static final String MONTH_MAX_DAY_COUNT_START_ELEMENT = "<MONTHMAXCOUNT>";
    private static final String MONTH_MAX_DAY_COUNT_END_ELEMENT = "</MONTHMAXCOUNT>";
    private static final String YEAR_MAX_DAY_COUNT_START_ELEMENT = "<YEARMAXCOUNT>";
    private static final String YEAR_MAX_DAY_COUNT_END_ELEMENT = "</YEARMAXCOUNT>";
    private static final String CUM_MAX_DAY_COUNT_START_ELEMENT = "<CUMMAXCOUNT>";
    private static final String CUM_MAX_DAY_COUNT_END_ELEMENT = "</CUMMAXCOUNT>";
    // max rate hours
    private static final String MAX_RATE_TODAY_START_ELEMENT = "<MAXRATETODAY>";
    private static final String MAX_RATE_TODAY_END_ELEMENT = "</MAXRATETODAY>";
    private static final String MAX_RATE_YESTERDAY_START_ELEMENT = "<MAXRATEYESTERDAY>";
    private static final String MAX_RATE_YESTERDAY_END_ELEMENT = "</MAXRATEYESTERDAY>";
    private static final String MAX_RATE_THIS_WEEK_START_ELEMENT = "<MAXRATETHISWEEK>";
    private static final String MAX_RATE_THIS_WEEK_END_ELEMENT = "</MAXRATETHISWEEK>";
    private static final String MAX_RATE_THIS_MONTH_START_ELEMENT = "<MAXRATETHISMONTH>";
    private static final String MAX_RATE_THIS_MONTH_END_ELEMENT = "</MAXRATETHISMONTH>";
    private static final String MAX_RATE_THIS_YEAR_START_ELEMENT = "<MAXRATETHISYEAR>";
    private static final String MAX_RATE_THIS_YEAR_END_ELEMENT = "</MAXRATETHISYEAR>";
    private static final String MAX_RATE_CUM_START_ELEMENT = "<MAXRATECUM>";
    private static final String MAX_RATE_CUM_END_ELEMENT = "</MAXRATECUM>";
    private static final String COUNT_CHANGE_WEEK_START_ELEMENT = "<COUNTCHANGEWEEK>";
    private static final String COUNT_CHANGE_WEEK_END_ELEMENT = "</COUNTCHANGEWEEK>";
    private static final String COUNT_CHANGE_MONTH_START_ELEMENT = "<COUNTCHANGEMONTH>";
    private static final String COUNT_CHANGE_MONTH_END_ELEMENT = "</COUNTCHANGEMONTH>";
    private static final String COUNT_CHANGE_YEAR_START_ELEMENT = "<COUNTCHANGEYEAR>";
    private static final String COUNT_CHANGE_YEAR_END_ELEMENT = "</COUNTCHANGEYEAR>";
    private static final String COUNT_CHANGE_CUM_START_ELEMENT = "<COUNTCHANGECUM>";
    private static final String COUNT_CHANGE_CUM_END_ELEMENT = "</COUNTCHANGECUM>";
    
    private Date lastChangeDate;
    private Date lastSampleDate;
    private float lastSampleValue;

    // count totals - accumulate by minute, hour, day, week, month, year, cum
    private CountValues[] minuteStartCounts;
    private CountValues[] hourStartCounts;
    private CountValues dayStartCount;
    private CountValues yesterdayStartCount;
    private CountValues weekStartCount;
    private CountValues monthStartCount;
    private CountValues yearStartCount;
//    private CountValues cumulativeStartCount;

    // max count totals - highest count day
    private MinMaxValues highestDailyValueThisWeek;
    private MinMaxValues highestDailyValueThisMonth;
    private MinMaxValues highestDailyValueThisYear;
    private MinMaxValues highestDailyValueThisCumulative;

    // max rates
    private MinMaxValues[] maxRateHours;
    private MinMaxValues maxRateToday;
    private MinMaxValues maxRateYesterday;
    private MinMaxValues maxRateThisWeek;
    private MinMaxValues maxRateThisMonth;
    private MinMaxValues maxRateThisYear;
    private MinMaxValues maxRateCumulative;

    // cumulative/stats
    private int countChangeDaysThisWeek;
    private int countChangeDaysThisMonth;
    private int countChangeDaysThisYear;
    private int countChangeDaysCumulative;

    public CumulativeCounts()
    {
        initialize();
    }

    private void initialize()
    {
        // initialize last change date to yesterday.  Otherwise, we don't count any changes 
        // that occurred the first day of system startup 
        lastChangeDate = new Date();
        lastChangeDate.setTime(lastChangeDate.getTime() - 1000 * 60 * 60 * 24);
        
        lastSampleDate = new Date(0);
        lastSampleValue = Float.MIN_VALUE;

        minuteStartCounts = new CountValues[60];
        hourStartCounts = new CountValues[24];
        dayStartCount = new CountValues();
        yesterdayStartCount = new CountValues();
        weekStartCount = new CountValues();
        monthStartCount = new CountValues();
        yearStartCount = new CountValues();
//        cumulativeStartCount = new CountValues();

        highestDailyValueThisWeek = new MinMaxValues();
        highestDailyValueThisMonth = new MinMaxValues();
        highestDailyValueThisYear = new MinMaxValues();
        highestDailyValueThisCumulative = new MinMaxValues();

        maxRateHours = new MinMaxValues[24];
        maxRateToday = new MinMaxValues();
        maxRateYesterday = new MinMaxValues();
        maxRateThisWeek = new MinMaxValues();
        maxRateThisMonth = new MinMaxValues();
        maxRateThisYear = new MinMaxValues();
        maxRateCumulative = new MinMaxValues();

        countChangeDaysThisWeek = 0;
        countChangeDaysThisMonth = 0;
        countChangeDaysThisYear = 0;
        countChangeDaysCumulative = 0;

        for (int i = 0; i < minuteStartCounts.length; i++)
        {
            minuteStartCounts[i] = new CountValues();
        }

        for (int i = 0; i < hourStartCounts.length; i++)
        {
            hourStartCounts[i] = new CountValues();
        }
        
        for (int i = 0; i < maxRateHours.length; i++)
        {
            maxRateHours[i] = new MinMaxValues();
        }
    }
    
    public void updateValues(float value, Date date)
    {
//        if (value != Float.MIN_VALUE)
        {
            // get date/time values for this sample
            Calendar sampleDate = Calendar.getInstance();
            sampleDate.setTime(date);
            int sampleHour = sampleDate.get(Calendar.HOUR_OF_DAY);
            int sampleMinute = sampleDate.get(Calendar.MINUTE);
            int sampleDay = sampleDate.get(Calendar.DAY_OF_MONTH);
            int sampleWeek = sampleDate.get(Calendar.WEEK_OF_YEAR);
            int sampleMonth = sampleDate.get(Calendar.MONTH);
            int sampleYear = sampleDate.get(Calendar.YEAR);

            // get date/time values for prior sample
            Calendar priorSampleDate = Calendar.getInstance();
            priorSampleDate.setTime(lastSampleDate);
            int lastSampleHour = priorSampleDate.get(Calendar.HOUR_OF_DAY);
            int lastSampleMinute = priorSampleDate.get(Calendar.MINUTE);
            int lastSampleDay = priorSampleDate.get(Calendar.DAY_OF_MONTH);
            int lastSampleWeek = priorSampleDate.get(Calendar.WEEK_OF_YEAR);
            int lastSampleMonth = priorSampleDate.get(Calendar.MONTH);
            int lastSampleYear = priorSampleDate.get(Calendar.YEAR);

            // if the year changed reset year values
            if (sampleYear != lastSampleYear || yearStartCount.getCountValue() == Float.MIN_VALUE)
            {
            	value = 0.0f;
                yearStartCount.resetCount(value, date);
                highestDailyValueThisYear = new MinMaxValues();
                maxRateThisYear = new MinMaxValues();
                countChangeDaysThisYear = 0;
            }

            // if the month changed reset month values
            if (sampleMonth != lastSampleMonth || monthStartCount.getCountValue() == Float.MIN_VALUE)
            {
                monthStartCount.resetCount(value, date);
                highestDailyValueThisMonth = new MinMaxValues();
                maxRateThisMonth = new MinMaxValues();
                countChangeDaysThisMonth = 0;
            }

            // if the week changed reset weekly values
            if (sampleWeek != lastSampleWeek || weekStartCount.getCountValue() == Float.MIN_VALUE)
            {
                weekStartCount.resetCount(value, date);
                highestDailyValueThisWeek = new MinMaxValues();
                maxRateThisWeek = new MinMaxValues();
                countChangeDaysThisWeek = 0;
            }

            // if the day changed copy today's values to yesterday and reset daily values
            if (sampleDay != lastSampleDay || dayStartCount.getCountValue() == Float.MIN_VALUE)
            {
                yesterdayStartCount.resetCount(dayStartCount.getCountValue(), dayStartCount.getCountTime());
                dayStartCount.resetCount(value, date);

                maxRateYesterday = maxRateToday;
                maxRateToday = new MinMaxValues();
            }

            // if hour changed then update hour values
            if (sampleHour != lastSampleHour || hourStartCounts[sampleHour].getCountValue() == Float.MIN_VALUE)
            {
                hourStartCounts[sampleHour].resetCount(value, date);
                maxRateHours[sampleHour] = new MinMaxValues();
            }

            // if minute changed then update minute values
            if (sampleMinute != lastSampleMinute || minuteStartCounts[sampleMinute].getCountValue() == Float.MIN_VALUE)
            {
                minuteStartCounts[sampleMinute].resetCount(value, date);
            }

            // update max rates
            float currentRate = this.getCurrentRatePerMinute();
            maxRateHours[sampleHour].updateValues(currentRate, date);
            maxRateToday.updateValues(currentRate, date);
            maxRateYesterday.updateValues(currentRate, date);
            maxRateThisWeek.updateValues(currentRate, date);
            maxRateThisMonth.updateValues(currentRate, date);
            maxRateThisYear.updateValues(currentRate, date);
            maxRateCumulative.updateValues(currentRate, date);

            // should these highs be updated every pass or once a day?
            float dayValue = this.getDayCount();
            highestDailyValueThisWeek.updateValues(dayValue, date);
            highestDailyValueThisMonth.updateValues(dayValue, date);
            highestDailyValueThisYear.updateValues(dayValue, date);
            highestDailyValueThisCumulative.updateValues(dayValue, date);

            // if the value is greater than zero then update counts of days where count has
            //  a positive value.  Only count once per day.
            if (value > lastSampleValue && lastSampleValue != Float.MIN_VALUE)
            {
                Calendar lastChange = Calendar.getInstance();
                lastChange.setTime(lastChangeDate);

                // check to see if this is a different day than when we last updated.  This probably
                // needs to check the year as well
                if (lastChange.get(Calendar.DAY_OF_YEAR) != sampleDate.get(Calendar.DAY_OF_YEAR) ||
                        lastChange.get(Calendar.YEAR) != sampleYear)
                {
                    countChangeDaysCumulative++;
                    countChangeDaysThisMonth++;
                    countChangeDaysThisWeek++;
                    countChangeDaysThisYear++;
                }

                lastChangeDate = date;
            }
        }

        lastSampleDate = date;
        lastSampleValue = value;
    }

    public float getCurrentRatePerMinute()
    {
        return getAverageRatePerMinute(1);
    }

    public float getAverageRatePerMinute(int numberOfMinutes)
    {
        float countValue = getMinuteCount(numberOfMinutes);
        float rate = 0.0f;

        if (countValue != Float.MIN_VALUE)
        {
            rate = countValue / (float)numberOfMinutes;
        }

        return rate;
    }

    public float getHourlyAverageRatePerMinute()
    {
        return this.getHourlyAverageRatePerMinute(1);
    }

    public float getHourlyAverageRatePerMinute(int numberOfHours)
    {
        float countValue = getHourCount(numberOfHours);
        float rate = 0.0f;

        if (countValue != Float.MIN_VALUE)
        {
            rate = countValue / (numberOfHours * 60.0f);
        }

        return rate;
    }

    public MinMaxValues getHighestDailyValueDayThisMonth()
    {
        return highestDailyValueThisMonth;
    }

    public MinMaxValues getHighestDailyValueThisWeek()
    {
        return highestDailyValueThisWeek;
    }
    
    public MinMaxValues getHighestDailyValueDayThisYear()
    {
        return highestDailyValueThisYear;
    }

    public MinMaxValues getHighestDailyValueCumulative()
    {
        return highestDailyValueThisCumulative;
    }

    private float getCountValue(float startValue)
    {
        float value = 0.0f;
        if (lastSampleValue != Float.MIN_VALUE && startValue != Float.MIN_VALUE)
        {
            value = lastSampleValue - startValue;
        }
        return value;
    }

    public float getCumulativeCount()
    {
        return getCountValue(yearStartCount.getCountValue());
//        return getCountValue(cumulativeStartCount.getCountValue());
    }

    public float getYearCount()
    {
        return getCountValue(yearStartCount.getCountValue());
    }

    public float getMonthCount()
    {
        return getCountValue(monthStartCount.getCountValue());
    }

    public float getWeekCount()
    {
        return getCountValue(weekStartCount.getCountValue());
    }

    public float getDayCount()
    {
        return getCountValue(dayStartCount.getCountValue());
    }

    public float getYesterdayCount()
    {
        float value = Float.MIN_VALUE;
        if (dayStartCount.getCountValue() != Float.MIN_VALUE && yesterdayStartCount.getCountValue() != Float.MIN_VALUE)
        {
            value = dayStartCount.getCountValue() - yesterdayStartCount.getCountValue();
        }
        return value;
    }

    public float getMinuteCount()
    {
        return getMinuteCount(1);
    }

    public float getMinuteCount(int numberOfMinutes)
    {
        int currentIndex;
        int priorIndex;

        float value = 0.0f;

        if (numberOfMinutes <= 60 && numberOfMinutes > 0)
        {
            Calendar sampleDate = Calendar.getInstance();
            sampleDate.setTime(lastSampleDate);
            currentIndex = sampleDate.get(Calendar.MINUTE);
            priorIndex = currentIndex - numberOfMinutes + 1;
            if (priorIndex < 0)
            {
                priorIndex += minuteStartCounts.length;
            }

            float currentValue = this.lastSampleValue;
            float priorValue = minuteStartCounts[priorIndex].getCountValue();

            if (currentValue != Float.MIN_VALUE && priorValue != Float.MIN_VALUE)
            {
                value = currentValue - priorValue;
            }
        }

        return Math.max(value, 0.0f);
    }

    public float getHourCount()
    {
        return getHourCount(1);
    }

    public float getHourCount(int numberOfHours)
    {
        int currentIndex;
        int priorIndex;

        float value = 0.0f;

        if (numberOfHours <= 24 && numberOfHours > 0)
        {
            Calendar sampleDate = Calendar.getInstance();
            sampleDate.setTime(lastSampleDate);
            currentIndex = sampleDate.get(Calendar.HOUR_OF_DAY);
            priorIndex = currentIndex - (numberOfHours - 1);
            if (priorIndex < 0)
            {
                priorIndex += hourStartCounts.length;
            }

            float currentValue = this.lastSampleValue;
            float priorValue = hourStartCounts[priorIndex].getCountValue();
            
            while (priorValue == Float.MIN_VALUE && priorIndex != currentIndex)
            {
                priorIndex++;
                priorIndex %= hourStartCounts.length;
                priorValue = hourStartCounts[priorIndex].getCountValue();
            }

//            if (currentValue != Float.MIN_VALUE && priorValue != Float.MIN_VALUE)
            if (currentValue != Float.MIN_VALUE)
            {
                value = currentValue - priorValue;
            }
        }

        return Math.max(value, 0.0f);
    }

    public Date getLastChangeDate()
    {
        return lastChangeDate;
    }

    public Date getLastSampleDate()
    {
        return lastSampleDate;
    }

    public String toXML()
    {
        StringBuilder out = new StringBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);

        out.append(LAST_CHANGE_DATE_START_ELEMENT);
        out.append(fmt.format(this.lastChangeDate));
        out.append(LAST_CHANGE_DATE_END_ELEMENT);

        out.append(LAST_SAMPLE_DATE_START_ELEMENT);
        out.append(fmt.format(this.lastSampleDate));
        out.append(LAST_SAMPLE_DATE_END_ELEMENT);
        
        out.append(LAST_SAMPLE_VALUE_START_ELEMENT);
        out.append(Float.toString(lastSampleValue));
        out.append(LAST_SAMPLE_VALUE_END_ELEMENT);

        for (int i = 0; i < minuteStartCounts.length; i++)
        {
            out.append("<MINUTESTART" + i + ">");
            out.append(minuteStartCounts[i].toXML());
            out.append("</MINUTESTART" + i + ">");
        }

        for (int i = 0; i < hourStartCounts.length; i++)
        {
            out.append("<HOURSTART" + i + ">");
            out.append(hourStartCounts[i].toXML());
            out.append("</HOURSTART" + i + ">");
        }

        out.append(DAY_COUNT_START_ELEMENT);
        out.append(dayStartCount.toXML());
        out.append(DAY_COUNT_END_ELEMENT);
        
        out.append(YESTERDAY_COUNT_START_ELEMENT);
        out.append(yesterdayStartCount.toXML());
        out.append(YESTERDAY_COUNT_END_ELEMENT);
        
        out.append(WEEK_COUNT_START_ELEMENT);
        out.append(weekStartCount.toXML());
        out.append(WEEK_COUNT_END_ELEMENT);
        
        out.append(MONTH_COUNT_START_ELEMENT);
        out.append(monthStartCount.toXML());
        out.append(MONTH_COUNT_END_ELEMENT);
        
        out.append(YEAR_COUNT_START_ELEMENT);
        out.append(yearStartCount.toXML());
        out.append(YEAR_COUNT_END_ELEMENT);
        
//        out.append(CUM_COUNT_START_ELEMENT);
//        out.append(cumulativeStartCount.toXML());
//        out.append(CUM_COUNT_END_ELEMENT);
        
        out.append(WEEK_MAX_DAY_COUNT_START_ELEMENT);
        out.append(highestDailyValueThisWeek.toXML());
        out.append(WEEK_MAX_DAY_COUNT_END_ELEMENT);
        
        out.append(MONTH_MAX_DAY_COUNT_START_ELEMENT);
        out.append(highestDailyValueThisMonth.toXML());
        out.append(MONTH_MAX_DAY_COUNT_END_ELEMENT);
        
        out.append(YEAR_MAX_DAY_COUNT_START_ELEMENT);
        out.append(highestDailyValueThisYear.toXML());
        out.append(YEAR_MAX_DAY_COUNT_END_ELEMENT);
        
        out.append(CUM_MAX_DAY_COUNT_START_ELEMENT);
        out.append(highestDailyValueThisCumulative.toXML());
        out.append(CUM_MAX_DAY_COUNT_END_ELEMENT);

        for (int i = 0; i < maxRateHours.length; i++)
        {
            out.append("<MAXRATEHOUR" + i + ">");
            out.append(maxRateHours[i].toXML());
            out.append("</MAXRATEHOUR" + i + ">");
        }
        
        out.append(MAX_RATE_TODAY_START_ELEMENT);
        out.append(maxRateToday.toXML());
        out.append(MAX_RATE_TODAY_END_ELEMENT);
        
        out.append(MAX_RATE_YESTERDAY_START_ELEMENT);
        out.append(maxRateYesterday.toXML());
        out.append(MAX_RATE_YESTERDAY_END_ELEMENT);
        
        out.append(MAX_RATE_THIS_WEEK_START_ELEMENT);
        out.append(maxRateThisWeek.toXML());
        out.append(MAX_RATE_THIS_WEEK_END_ELEMENT);
        
        out.append(MAX_RATE_THIS_MONTH_START_ELEMENT);
        out.append(maxRateThisMonth.toXML());
        out.append(MAX_RATE_THIS_MONTH_END_ELEMENT);
        
        out.append(MAX_RATE_THIS_YEAR_START_ELEMENT);
        out.append(maxRateThisYear.toXML());
        out.append(MAX_RATE_THIS_YEAR_END_ELEMENT);
        
        out.append(MAX_RATE_CUM_START_ELEMENT);
        out.append(maxRateCumulative.toXML());
        out.append(MAX_RATE_CUM_END_ELEMENT);
        
        out.append(COUNT_CHANGE_WEEK_START_ELEMENT);
        out.append(Integer.toString(countChangeDaysThisWeek));
        out.append(COUNT_CHANGE_WEEK_END_ELEMENT);
        
        out.append(COUNT_CHANGE_MONTH_START_ELEMENT);
        out.append(Integer.toString(countChangeDaysThisMonth));
        out.append(COUNT_CHANGE_MONTH_END_ELEMENT);
        
        out.append(COUNT_CHANGE_YEAR_START_ELEMENT);
        out.append(Integer.toString(countChangeDaysThisYear));
        out.append(COUNT_CHANGE_YEAR_END_ELEMENT);
        
        out.append(COUNT_CHANGE_CUM_START_ELEMENT);
        out.append(Integer.toString(countChangeDaysCumulative));
        out.append(COUNT_CHANGE_CUM_END_ELEMENT);
        
        return out.toString();
    }

    public void fromXML(String xmlString)
    {
        initialize();
        
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
        try 
        {
            this.lastChangeDate = fmt.parse(getData(xmlString, LAST_CHANGE_DATE_START_ELEMENT, LAST_CHANGE_DATE_END_ELEMENT));
        } 
        catch (ParseException e) 
        {
            e.printStackTrace();
            this.lastChangeDate = new Date(0);
            ErrorLog.logError("Error reading last change date");
            ErrorLog.logError("Data: " + getData(xmlString, LAST_CHANGE_DATE_START_ELEMENT, LAST_CHANGE_DATE_END_ELEMENT));
            ErrorLog.logStackTrace(e);
        }
      
        try 
        {
            this.lastSampleDate = fmt.parse(getData(xmlString, LAST_SAMPLE_DATE_START_ELEMENT, LAST_SAMPLE_DATE_END_ELEMENT));
        } 
        catch (ParseException e) 
        {
            e.printStackTrace();
            this.lastSampleDate = new Date(0);
            ErrorLog.logError("Error reading last sample date");
            ErrorLog.logError("Data: " + getData(xmlString, LAST_SAMPLE_DATE_START_ELEMENT, LAST_SAMPLE_DATE_END_ELEMENT));
            ErrorLog.logStackTrace(e);
        }

        lastSampleValue = Float.parseFloat(getData(xmlString, LAST_SAMPLE_VALUE_START_ELEMENT, LAST_SAMPLE_VALUE_END_ELEMENT));
        for (int i = 0; i < minuteStartCounts.length; i++)
        {
            minuteStartCounts[i].fromXML(getData(xmlString, "<MINUTESTART" + i + ">", "</MINUTESTART" + i + ">"));
        }

        for (int i = 0; i < hourStartCounts.length; i++)
        {
            hourStartCounts[i].fromXML(getData(xmlString, "<HOURSTART" + i + ">", "</HOURSTART" + i + ">"));
        }
        
        dayStartCount.fromXML(getData(xmlString, DAY_COUNT_START_ELEMENT, DAY_COUNT_END_ELEMENT));
        yesterdayStartCount.fromXML(getData(xmlString, YESTERDAY_COUNT_START_ELEMENT, YESTERDAY_COUNT_END_ELEMENT));
        weekStartCount.fromXML(getData(xmlString, WEEK_COUNT_START_ELEMENT, WEEK_COUNT_END_ELEMENT));
        monthStartCount.fromXML(getData(xmlString, MONTH_COUNT_START_ELEMENT, MONTH_COUNT_END_ELEMENT));
        yearStartCount.fromXML(getData(xmlString, YEAR_COUNT_START_ELEMENT, YEAR_COUNT_END_ELEMENT));
//        cumulativeStartCount.fromXML(getData(xmlString, CUM_COUNT_START_ELEMENT, CUM_COUNT_END_ELEMENT));
        highestDailyValueThisWeek.fromXML(getData(xmlString, WEEK_MAX_DAY_COUNT_START_ELEMENT, WEEK_MAX_DAY_COUNT_END_ELEMENT));
        highestDailyValueThisMonth.fromXML(getData(xmlString, MONTH_MAX_DAY_COUNT_START_ELEMENT, MONTH_MAX_DAY_COUNT_END_ELEMENT));
        highestDailyValueThisYear.fromXML(getData(xmlString, YEAR_MAX_DAY_COUNT_START_ELEMENT, YEAR_MAX_DAY_COUNT_END_ELEMENT));
        highestDailyValueThisCumulative.fromXML(getData(xmlString, CUM_MAX_DAY_COUNT_START_ELEMENT, CUM_MAX_DAY_COUNT_END_ELEMENT));

        for (int i = 0; i < maxRateHours.length; i++)
        {
            maxRateHours[i].fromXML(getData(xmlString, "<MAXRATEHOUR" + i + ">", "</MAXRATEHOUR" + i + ">"));
        }
        
        maxRateToday.fromXML(getData(xmlString, MAX_RATE_TODAY_START_ELEMENT, MAX_RATE_TODAY_END_ELEMENT));
        maxRateYesterday.fromXML(getData(xmlString, MAX_RATE_YESTERDAY_START_ELEMENT, MAX_RATE_YESTERDAY_END_ELEMENT));
        maxRateThisWeek.fromXML(getData(xmlString, MAX_RATE_THIS_WEEK_START_ELEMENT, MAX_RATE_THIS_WEEK_END_ELEMENT));
        maxRateThisMonth.fromXML(getData(xmlString, MAX_RATE_THIS_MONTH_START_ELEMENT, MAX_RATE_THIS_MONTH_END_ELEMENT));
        maxRateThisYear.fromXML(getData(xmlString, MAX_RATE_THIS_YEAR_START_ELEMENT, MAX_RATE_THIS_YEAR_END_ELEMENT));
        maxRateCumulative.fromXML(getData(xmlString, MAX_RATE_CUM_START_ELEMENT, MAX_RATE_CUM_END_ELEMENT));
        countChangeDaysThisWeek = Integer.parseInt(getData(xmlString, COUNT_CHANGE_WEEK_START_ELEMENT, COUNT_CHANGE_WEEK_END_ELEMENT));
        countChangeDaysThisMonth = Integer.parseInt(getData(xmlString, COUNT_CHANGE_MONTH_START_ELEMENT, COUNT_CHANGE_MONTH_END_ELEMENT));
        countChangeDaysThisYear = Integer.parseInt(getData(xmlString, COUNT_CHANGE_YEAR_START_ELEMENT, COUNT_CHANGE_YEAR_END_ELEMENT));
        countChangeDaysCumulative = Integer.parseInt(getData(xmlString, COUNT_CHANGE_CUM_START_ELEMENT, COUNT_CHANGE_CUM_END_ELEMENT));
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

    /**
     * @return the maxRateCumulative
     */
    public MinMaxValues getMaxRateCumulative() {
        return maxRateCumulative;
    }

    /**
     * @return the maxRateLastMonth
     */
    public MinMaxValues getMaxRateThisMonth() {
        return maxRateThisMonth;
    }

    /**
     * @return the maxRateLastWeek
     */
    public MinMaxValues getMaxRateThisWeek() {
        return maxRateThisWeek;
    }

    /**
     * @return the maxRateLastYear
     */
    public MinMaxValues getMaxRateThisYear() {
        return maxRateThisYear;
    }

    /**
     * @return the maxRateToday
     */
    public MinMaxValues getMaxRateToday() {
        return maxRateToday;
    }

    /**
     * @return the maxRateYesterday
     */
    public MinMaxValues getMaxRateYesterday() {
        return maxRateYesterday;
    }

    /**
     * @return the countChangeDaysThisMonth
     */
    public int getCountChangeDaysThisMonth() {
        return countChangeDaysThisMonth;
    }

    /**
     * @return the countChangeDaysThisWeek
     */
    public int getCountChangeDaysThisWeek() {
        return countChangeDaysThisWeek;
    }

    /**
     * @return the countChangeDaysThisYear
     */
    public int getCountChangeDaysThisYear() {
        return countChangeDaysThisYear;
    }

    /**
     * @return the countChangeDaysCumulative
     */
    public int getCountChangeDaysCumulative() {
        return countChangeDaysCumulative;
    }

    public int daysWithNoChange()
    {
        Calendar lastChange = Calendar.getInstance();
        lastChange.setTime(lastChangeDate);
        long lastChangeDay = lastChange.getTimeInMillis(); 

        Calendar lastSample = Calendar.getInstance();
        lastSample.setTime(lastSampleDate);
        long lastSampleDay = lastSample.getTimeInMillis();

        // differenceInMillis
        long difference = lastSampleDay - lastChangeDay;

        return (int)(difference / 1000 / 60 / 60 / 24);
    }
    
    public MinMaxValues getMaxRateHour()
    {
        Calendar lastSample = Calendar.getInstance();
        lastSample.setTime(lastSampleDate);
        int hour = lastSample.get(Calendar.HOUR_OF_DAY);
        return maxRateHours[hour];
    }
    
    public MinMaxValues getMaxRateHour(int numberOfHours)
    {
        MinMaxValues value = new MinMaxValues();
        
        if (numberOfHours > 0 && numberOfHours <= maxRateHours.length)
        {
            Calendar lastSample = Calendar.getInstance();
            lastSample.setTime(lastSampleDate);
            int hour = lastSample.get(Calendar.HOUR_OF_DAY);
            int index = hour;
            int i = 0;

            do 
            {
                value.updateValues(maxRateHours[index].getMaxValue(), maxRateHours[index].getMaxDate());
                index--;
                if (index < 0) index += maxRateHours.length;
                i++;
                
            } 
            while (i < numberOfHours - 1);

        }
        
        return value;
    }

    public float getLastSampleValue()
    {
	return lastSampleValue;
    }

    public CountValues getDayStartCount()
    {
        return dayStartCount;
    }
    
    public CountValues getYesterdayStartCount()
    {
        return yesterdayStartCount;
    }
}
