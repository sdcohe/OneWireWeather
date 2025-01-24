/* SVN FILE: $Id: CountValues.java 134 2013-07-19 19:32:22Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 134 $
* $Date: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $LastChangedBy: seth $
* $LastChangedDate: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/CountValues.java $
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
import java.util.Date;

public class CountValues implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
//    private static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String VALUE_START_ELEMENT = "<VALUE>";
    private static final String VALUE_END_ELEMENT = "</VALUE>";
    private static final String DATE_START_ELEMENT = "<DATE>";
    private static final String DATE_END_ELEMENT = "</DATE>";
    
    private float countValue;
    private Date countTime;
    
    public CountValues()
    {
        countValue = Float.MIN_VALUE;
        countTime = new Date();
    }
    
    public void resetCount(float value, Date time)
    {
        if (value != Float.MIN_VALUE) {
            this.countValue = value;
        }
        this.countTime = time;
    }

    public void resetCount(Date time)
    {
        this.countValue = 0.0f;
        this.countTime = time;
    }
    
    public void resetCount()
    {
        this.countValue = 0.0f;
        this.countTime = new Date();
    }
    
    public void incrementCount(float value, Date time)
    {
        this.countValue += value;
        this.countTime = time;
    }

    public void incrementCount(float value)
    {
        this.countValue += value;
    }

    /**
     * @return the countTime
     */
    public Date getCountTime() {
        return countTime;
    }

    /**
     * @return the countValue
     */
    public float getCountValue() {
        return countValue;
    }
    
    public String toXML()
    {
        StringBuilder str = new StringBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
      
        str.append(VALUE_START_ELEMENT + countValue + VALUE_END_ELEMENT);
        str.append(DATE_START_ELEMENT + fmt.format(this.countTime) + DATE_END_ELEMENT);
       
        return str.toString();
    }
    
    public void fromXML(String xmlString)
    {
        this.countValue = Float.MIN_VALUE;
        this.countTime = new Date();
        
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
        String data;
        
        int startPos = xmlString.indexOf(VALUE_START_ELEMENT);
        int endPos= xmlString.indexOf(VALUE_END_ELEMENT);
        if (startPos != -1 && endPos > startPos)
        {
            data = xmlString.substring(startPos + VALUE_START_ELEMENT.length(), endPos);
            this.countValue = Float.parseFloat(data);
        }

        startPos = xmlString.indexOf(DATE_START_ELEMENT);
        endPos = xmlString.indexOf(DATE_END_ELEMENT);
        if (startPos != -1 && endPos > startPos)
        {
            data = xmlString.substring(startPos + DATE_START_ELEMENT.length(), endPos);
            try {
                this.countTime = fmt.parse(data);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }        
    }
    
}
