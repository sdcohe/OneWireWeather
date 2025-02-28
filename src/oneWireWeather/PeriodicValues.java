/* SVN FILE: $Id: PeriodicValues.java 134 2013-07-19 19:32:22Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 134 $
* $Date: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $LastChangedBy: seth $
* $LastChangedDate: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/PeriodicValues.java $
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

public class PeriodicValues implements Serializable {

    private static int HOUR = 0;
    private static int DAY = 1;
    private static int YESTERDAY = 2;
    private static int WEEK = 3;
    private static int MONTH = 4;
    private static int YEAR = 5;
    private static int CUMULATIVE = 6;

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

    private int[] values;

    public PeriodicValues()
    {
        initialize();
    }

    private void initialize()
    {
        values = new int[7];
        
        for (int i = 0; i < values.length; i++)
        {
            values[i] = -1;
        }
    }

    public String toXML()
    {
        StringBuilder out = new StringBuilder();

        out.append(HOURLY_ELEMENT);
        out.append(Integer.toString(values[HOUR]));
        out.append(HOURLY_END_ELEMENT);
        
        out.append(DAILY_ELEMENT);
        out.append(Integer.toString(values[DAY]));
        out.append(DAILY_END_ELEMENT);

        out.append(YESTERDAY_ELEMENT);
        out.append(Integer.toString(values[YESTERDAY]));
        out.append(YESTERDAY_END_ELEMENT);

        out.append(WEEKLY_ELEMENT);
        out.append(Integer.toString(values[WEEK]));
        out.append(WEEKLY_END_ELEMENT);

        out.append(MONTHLY_ELEMENT);
        out.append(Integer.toString(values[MONTH]));
        out.append(MONTHLY_END_ELEMENT);

        out.append(ANNUAL_ELEMENT);
        out.append(Integer.toString(values[YEAR]));
        out.append(ANNUAL_END_ELEMENT);

        out.append(CUM_ELEMENT);
        out.append(Integer.toString(values[CUMULATIVE]));
        out.append(CUM_END_ELEMENT);

        return out.toString();
    }

    public void fromXML(String xmlString)
    {
        values[HOUR] = getIntegerData(xmlString, HOURLY_ELEMENT, HOURLY_END_ELEMENT);
        values[DAY] = getIntegerData(xmlString, DAILY_ELEMENT, DAILY_END_ELEMENT);
        values[YESTERDAY] = getIntegerData(xmlString, YESTERDAY_ELEMENT, YESTERDAY_END_ELEMENT);
        values[WEEK] = getIntegerData(xmlString, WEEKLY_ELEMENT, WEEKLY_END_ELEMENT);
        values[MONTH] = getIntegerData(xmlString, MONTHLY_ELEMENT, MONTHLY_END_ELEMENT);
        values[YEAR] = getIntegerData(xmlString, ANNUAL_ELEMENT, ANNUAL_END_ELEMENT);
        values[CUMULATIVE] = getIntegerData(xmlString, CUM_ELEMENT, CUM_END_ELEMENT);
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
    
    private int getIntegerData(String xmlString, String startString, String endString)
    {
        String data = getData(xmlString, startString, endString);
        int value;
        try
        {
            value = Integer.parseInt(data);
        }
        catch (NumberFormatException ex)
        {
            value = -1;
        }
        
        return value;
    }

    public int getHourValue()
    {
        return values[HOUR];
    }

    public void setHourValue(int value)
    {
        values[HOUR] = value;
    }

    public int getDayValue()
    {
        return values[DAY];
    }

    public void setDayValue(int value)
    {
        values[DAY] = value;
    }

    public int getYesterdayValue()
    {
        return values[YESTERDAY];
    }

    public void setYesterdayValue(int value)
    {
        values[YESTERDAY] = value;
    }

    public int getWeekValue()
    {
        return values[WEEK];
    }

    public void setWeekValue(int value)
    {
        values[WEEK] = value;
    }

    public int getMonthValue()
    {
        return values[MONTH];
    }

    public void setMonthValue(int value)
    {
        values[MONTH] = value;
    }

    public int getYearValue()
    {
        return values[YEAR];
    }

    public void setYearValue(int value)
    {
        values[YEAR] = value;
    }

    public int getCumulativeValue()
    {
        return values[CUMULATIVE];
    }

    public void setCumulativeValue(int value)
    {
        values[CUMULATIVE] = value;
    }

}
