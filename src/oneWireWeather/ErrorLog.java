/* SVN FILE: $Id: ErrorLog.java 204 2016-11-23 04:34:51Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 204 $
* $Date: 2016-11-22 23:34:51 -0500 (Tue, 22 Nov 2016) $
* $LastChangedBy: seth $
* $LastChangedDate: 2016-11-22 23:34:51 -0500 (Tue, 22 Nov 2016) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/ErrorLog.java $
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
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Maintain a disk log file.
 * 
 * @author Seth Cohen
 *
 */
public class ErrorLog {

    private static String m_fileName = "";
    private static String m_filePath;

    public static void setLogFilePath(String path)
    {
	m_filePath = path;
    }

    /**
     * Log a message to the file.
     * 
     * @param message  The message to log.
     */
    public static void logError(String message)
    {
	String fullPath = getFullPath();

//	if (m_fileName.length() == 0 )
//	    m_fileName = "error.log";
//
//	if (m_filePath != null && m_filePath.length() > 0)
//	{
//	    fullPath = m_filePath + File.separator + m_fileName;
//	}
//	else
//	{
//	    fullPath = m_fileName;
//	}

	try
	{
	    FileOutputStream file = new FileOutputStream(fullPath, true);
	    file.write((new Date() + ": ").getBytes());            // time stamp
	    file.write((message + "\r\n").getBytes());            // message
	    file.close();
	}
	catch(Exception e)
	{
	    System.out.println("Exception: ErrorLogger.logError() " + e);
	}
    }

    public static void logStackTrace(Exception ex)
    {
	String fullPath = getFullPath();

//	if (m_fileName.length() == 0 )
//	    m_fileName = "error.log";
//
//	if (m_filePath != null && m_filePath.length() > 0)
//	{
//	    fullPath = m_filePath + File.separator + m_fileName;
//	}
//	else
//	{
//	    fullPath = m_fileName;
//	}

	try
	{

	    FileOutputStream file = new FileOutputStream(fullPath, true);
	    PrintStream strm = new PrintStream(file);
	    ex.printStackTrace(strm);
	    strm.close();
	    file.close();
	}
	catch(Exception e)
	{
	    System.out.println("Exception: ErrorLogger.logError() " + e);
	}
    }

    /**
     * Delete the log file. 
     */
    public static void deleteLog()
    {
        if ( m_fileName != null && m_fileName.length() != 0)
        {
            logError("Attempting to delete log");

            String fullPath = getFullPath();
//            if (m_filePath != null && m_filePath.length() > 0)
//            {
//                fullPath = m_filePath + File.separator + m_fileName;
//            }
//            else
//            {
//                fullPath = m_fileName;
//            }

            try
            {
                File file = new File(fullPath);
                boolean result = file.delete();

                if (result == false)
                    System.out.println("Could not delete " + fullPath);
            }
            catch (Exception e)
            {
                System.out.println("Could not delete Error Log: " + e);
            }
        }
    }
    /**
     * Truncate the log file. 
     */

    public static void truncateLog()
    {
        if ( m_fileName != null && m_fileName.length() != 0)
        {
            logError("Attempting to truncate log");

            String fullPath = getFullPath();
//            if (m_filePath != null && m_filePath.length() > 0)
//            {
//                fullPath = m_filePath + File.separator + m_fileName;
//            }
//            else
//            {
//                fullPath = m_fileName;
//            }

            try
            {
                File file = new File(fullPath);
                FileOutputStream stream = new FileOutputStream(file); 
                FileChannel channel = stream.getChannel();
                channel.truncate(0);
                channel.close();
                stream.close();
            }
            catch (Exception e)
            {
                System.out.println("Could not truncate Error Log: " + e);
            }
        }
    }
    
    private static String getFullPath()
    {
        String fullPath;

        if (m_fileName.length() == 0 )
            m_fileName = "error.log";

        if (m_filePath != null && m_filePath.length() > 0)
        {
            fullPath = m_filePath + File.separator + m_fileName;
        }
        else
        {
            fullPath = m_fileName;
        }

        return fullPath;
    }
}

