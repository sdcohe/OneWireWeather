/* SVN FILE: $Id: StationLog.java 163 2014-03-15 06:03:44Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 163 $
* $Date: 2014-03-15 02:03:44 -0400 (Sat, 15 Mar 2014) $
* $LastChangedBy: seth $
* $LastChangedDate: 2014-03-15 02:03:44 -0400 (Sat, 15 Mar 2014) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/StationLog.java $
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
//import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.*;

// the station log data is maintained in an array.  When the array is full, the next entry
// position wraps around back to the beginning of the array.  To read the data, you need
// to first locate the earliest entry, based on the millis of that entry.  Then process each
// non-null entry, wrapping around back through the beginning of the array to the entry
// that is considered the first.
public class StationLog implements WeatherDataListener, ConfigurationChangeListener{

    private File m_logFile;
    private WeatherData[] m_data;
    private int m_currentIndex = 0;
    private StationConfiguration m_config;
    private int lastSampleMinute = -99;
    
    public StationLog(StationConfiguration config)
    {
        m_config = config;
        m_data = new WeatherData[(int)m_config.getLogFileSize()];
    }
    
    public StationLog()
    {
        m_config = new StationConfiguration();
        m_data = new WeatherData[(int)m_config.getLogFileSize()];
    }

    public synchronized String toXML()
    {
        StringBuilder str = new StringBuilder();
        WeatherData data;
        
        // find min value and start from that entry
        // first find a non-blank entry to start with
        int startPos = getMinLogEntry();

        if (m_data[startPos] == null)
            return "";
        
        if (m_data[startPos] != null)
        {
            data = m_data[startPos];
            str.append(data.toXML(m_config));
        }
        
        int i = (startPos + 1) % m_data.length;
        while(i != startPos)
        {
            if (m_data[i] != null)
            {
                data = m_data[i];
                str.append(data.toXML(m_config));
            }
            i = (i + 1) % m_data.length;
        }

        return str.toString();
    }
    
    public synchronized void fromXML(String xmlString)
    {
        // clear the log
        m_data = new WeatherData[(int)m_config.getLogFileSize()];

        // for each Weather data entry
        int startPos = 0;
        int endPos = 0;
        m_currentIndex = 0;
        String startLiteral = "<WEATHERDATAENTRY>";
        String endLiteral = "</WEATHERDATAENTRY>";
        
        startPos = xmlString.indexOf(startLiteral, endPos);
        while (startPos >= 0)
        {
            endPos = xmlString.indexOf(endLiteral, startPos);
            String xmlData = xmlString.substring(startPos, endPos + endLiteral.length());
            
            //  create a new weather data from the entry
            WeatherData data = new WeatherData(xmlData);
            
            //  add it to the log
            this.addEntryNoSave(data);

            startPos = xmlString.indexOf(startLiteral, endPos);
        }
        
        this.locateCurrentIndex();
    }
    
    public synchronized String toString()
    {
        StringBuilder str = new StringBuilder();
        WeatherData data;
        
        // find min value and start from that entry
        int startPos = 0;
        startPos = getMinLogEntry();
        
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
        
        if (m_data[startPos] != null)
        {
            data = m_data[startPos];
            str.append(fmt.format(new Date(data.getDateTimeMillis())) + "," + data.getTemperature() + "," +
                    data.getHumidity() + "," + data.getPressure() + "," + data.getRainfall() +
                    "," + data.getWindSpeed() + "," + data.getWindDirection() + "\n");
        }
        
        int i = (startPos + 1) % m_data.length;
        while(i != startPos)
        {
            if (m_data[i] != null)
            {
                data = m_data[i];
                str.append(fmt.format(new Date(data.getDateTimeMillis())) + "," + data.getTemperature() + "," +
                        data.getHumidity() + "," + data.getPressure() + "," + data.getRainfall() +
                        "," + data.getWindSpeed() + "," + data.getWindDirection() + "\n");
            }
            i = (i + 1) % m_data.length;
        }
        return str.toString();
    }
    
    public synchronized WeatherData[] getLogData()
    {
        // find first and last entry
        // build string
        // return it
        // or should it just return the raw data???
        return m_data;
    }
    
    public synchronized void load()
    {
        loadLogFile();
    }
    
    private synchronized void loadLogFile()
    {
        String fullLogFilePath;
        
        if (m_config.getHomeDirectory() != null && m_config.getHomeDirectory().length() > 0)
        {
            fullLogFilePath = m_config.getHomeDirectory() + File.separator + m_config.getLogFileName();
        }
        else
        {
            fullLogFilePath = m_config.getLogFileName();
        }
        
        m_logFile = new File(fullLogFilePath);
        
        // if the file doesn't exist then create it
        if (!m_logFile.exists())
        {
            try
            {
                m_logFile.createNewFile();
            }
            catch (IOException e)
            {
                // handle exception
                ErrorLog.logError("Error loading log in loadLogFile(): " + e);
            }
        }
        else
        {
            // load the contents of the log file
            try
            {
                // read in array of objects
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(m_logFile));
                
                m_data = (WeatherData[])in.readObject();
                in.close();

                // size of saved log file may not match the size allocated.  Need to 
                // resize the data as needed.
                if (m_data.length != m_config.getLogFileSize())
                {
                    resizeData((int)m_config.getLogFileSize());
                    
                }
            }
            catch(ClassNotFoundException ex)
            {
                ErrorLog.logError("loadLogFile(): Class not found: " + ex);
            }
            catch(FileNotFoundException ex)
            {
        	ErrorLog.logError("loadLogFile(): File not found: " + ex);
            }
            catch(IOException ex)
            {
        	ErrorLog.logError("loadLogFile(): I/O exception: " + ex);
            }
        }
        locateCurrentIndex();
    }
    
    private synchronized void addEntryNoSave(WeatherData data)
    {
        m_data[m_currentIndex] = data;
        m_currentIndex = (m_currentIndex + 1) % (int)m_config.getLogFileSize();
    }

    private synchronized void addLogEntry(WeatherData data)
    {
        //*** check if logging interval has expired before logging
        
        m_data[m_currentIndex] = data;
        m_currentIndex = (m_currentIndex + 1) % (int)m_config.getLogFileSize();
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(m_logFile));
            out.writeObject(m_data);
            out.close();
        }
        catch(IOException ex)
        {
            ErrorLog.logError("addLogEntry(): Error writing log: " + ex);
        }
    }

    public synchronized void notifyWeatherData(WeatherData data) {

        if (m_config.isSaveLog())
        {
            // log entries once per minute
            Calendar cal = Calendar.getInstance();
            int minute = cal.get(Calendar.MINUTE);

            if (minute != lastSampleMinute)
            {
                this.addLogEntry(data);
                lastSampleMinute = minute;
            }
        }
    }

    public void notifyConfigurationChange(StationConfiguration config) {

        // react to config changes
        
        // resize log array as needed
        if (config.getLogFileSize() != m_config.getLogFileSize())
        {
            resizeData((int)config.getLogFileSize());
        }
        
        //*** implement new logging interval as needed
        
        // rename log file as needed
        if (! config.getLogFileName().equalsIgnoreCase(m_config.getLogFileName()))
        {
            // rename the log file
            File newFile = new File(config.getLogFileName());
            if (!m_logFile.renameTo(newFile))
            {
                ErrorLog.logError("Error renaming log file from " + m_config.getLogFileName() + " to " + config.getLogFileName());
            }
            else
            {
                m_logFile = newFile;
            }
        }
        
        m_config = config;
        
    }
   
    private synchronized void locateCurrentIndex()
    {
        // determine the current index
        m_currentIndex = 0;
        
        // find the first non-null enrty
        while (m_currentIndex < m_data.length && m_data[m_currentIndex] == null)
        {
            m_currentIndex++;
        }
        
        if (m_currentIndex == m_data.length)
        {
            m_currentIndex = 0;
            return;
        }
        
        // we have a non-null entry to compare to.  Now find the most recent entry
        for(int i = 1; i < m_data.length; i++)
        {
            if (m_data[i] != null)
            {
                if (m_data[i].getDateTimeMillis() > m_data[m_currentIndex].getDateTimeMillis())
                {
                    m_currentIndex = i;
                }
            }
        }
        
//        System.out.println("Max log entry at: " + m_currentIndex);
        
        // set the index to one past the most recent entry
        m_currentIndex = (m_currentIndex + 1) % m_data.length;
    }
    
    private synchronized int getMinLogEntry()
    {
        // find the first non-null entry
        // determine the current index
        int startIndex = 0;
        int minIndex = 0;
        
        // find the first non-null enrty
        while (startIndex < m_data.length && m_data[startIndex] == null)
        {
            startIndex++;
        }
        
        if (startIndex != m_data.length)
        {
            minIndex = startIndex;
            int searchIndex = (startIndex + 1) % m_data.length;
            while (searchIndex != startIndex)
            {
                if (m_data[searchIndex] != null)
                {
                    if (m_data[searchIndex].getDateTimeMillis() < m_data[minIndex].getDateTimeMillis())
                    {
                        minIndex = searchIndex;
                    }
                }
                searchIndex = (searchIndex + 1) % m_data.length;
            }
        }

//        System.out.println("Min log entry at: " + minIndex);
        return minIndex;
    }
    
    private synchronized void resizeData(int newSize)
    {
        // create a temp array to hold the old data
        WeatherData[] oldData = new WeatherData[m_data.length];
        System.arraycopy(m_data, 0, oldData, 0, m_data.length);
        
        // create new log
        m_data = new WeatherData[newSize];
        
        if (newSize > oldData.length)
        {
            System.arraycopy(oldData, 0, m_data, 0, oldData.length);
        }
        else if (newSize < oldData.length)
        {
            // copy only the most recent config.getLogFileSize() entries back
            // need to determine how much to restore
            // find the m_data.length most recent entries
            int startPos = 0;
            
            // find the most recent entry in the log
            for(int i = 1; i < oldData.length; i++)
            {
                if (oldData[i] != null && oldData[startPos] != null)
                {
                    if (oldData[i].getDateTimeMillis() > oldData[startPos].getDateTimeMillis())
                    {
                        startPos = i;
                    }
                }
            }
            
            // startpos is the most recent entry
            // copy items one by one moving backward in the array until the
            // new array is full
            for(int i = m_data.length - 1; i >= 0; i--)
            {
                m_data[i] = oldData[startPos--];

                if (startPos < 0) 
                {
                    startPos = oldData.length - 1;
                }
            }
        }
        
        locateCurrentIndex();
    }

    public float getMaxGustTimeInterval(int minutes)
    {
        float maxGust = 0.0f;
        int currentIdx;
        
        currentIdx = m_currentIndex - 1;
        if (currentIdx < 0) currentIdx = m_data.length - 1;
        
        for (int i = 0; i < minutes; i++)
        {
            if (m_data[currentIdx].getWindSpeed() > maxGust)
            {
                maxGust = m_data[currentIdx].getWindSpeed();
            }
            currentIdx--;
            if (currentIdx < 0) currentIdx = m_data.length - 1;
        }
        
        return maxGust;
    }
    
    public double getCurrentRainRate()
    {
        int currentIdx;
        int priorIdx;
        
        currentIdx = m_currentIndex - 1;
        if (currentIdx < 0) currentIdx = m_data.length - 1;
        
        priorIdx = currentIdx - 1;
        if (priorIdx < 0) priorIdx = m_data.length - 1;
        
        if (m_data[currentIdx] != null && m_data[priorIdx] != null)
        {
            return m_data[currentIdx].getRainfall() - m_data[priorIdx].getRainfall();
        }
        else
        {
            return 0.0;
        }
    }
    
    public double getMaxRainRateTimeInterval(int minutes)
    {
        //TODO: add validity checking of interval
        
        int currentIdx;
        int priorIdx;
        double maxRate = 0.0;
        
        currentIdx = m_currentIndex - 1;
        if (currentIdx < 0) currentIdx = m_data.length - 1;
        
        priorIdx = currentIdx - 1;
        if (priorIdx < 0) priorIdx = m_data.length - 1;
        
        for (int i = 0; i < minutes; i++)
        {
            if (m_data[currentIdx] != null && m_data[priorIdx] != null)
            {
                double rate =  m_data[currentIdx].getRainfall() - m_data[priorIdx].getRainfall();
                if (rate > maxRate)
                {
                    maxRate = rate;
                }
            }
            currentIdx = priorIdx;
            priorIdx--;
            if (priorIdx < 0) priorIdx = m_data.length - 1;
        }
        
        return maxRate;
    }
    
}
