/* SVN FILE: $Id: WeatherServerHandler.java 192 2016-09-29 22:22:55Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 192 $
* $Date: 2016-09-29 18:22:55 -0400 (Thu, 29 Sep 2016) $
* $LastChangedBy: seth $
* $LastChangedDate: 2016-09-29 18:22:55 -0400 (Thu, 29 Sep 2016) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/WeatherServerHandler.java $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
//import java.net.SocketTimeoutException;

class WeatherServerHandler implements Runnable{

    private Socket m_socket;
    private WeatherStation m_station;
    private WeatherServer m_wxServer;
    private boolean m_bQuit = false;

    public WeatherServerHandler(Socket sock, WeatherServer server, WeatherStation station)
    {
	m_socket = sock;
	m_wxServer = server;
	m_station = station;
    }

    // implement the following commands:
    // q - close this session
    // w - retrieve current weather data as an XML document
    // s - retrieve statistics as XML
    // l - retrieve log as XML data
    // c - retrieve the station configuration

    public void run() 
    {
	// m_station.addServerThread(this);
	// add this thread to the list of active threads
	m_bQuit = false;
	
	try 
	{
	    m_socket.setSoTimeout(1000);
	} 
	catch (SocketException e1) 
	{
	    //            e1.printStackTrace();
	}

	try
	{
	    BufferedReader in = 
		new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
	    PrintWriter out = new PrintWriter(m_socket.getOutputStream(), true);

	    // print banner
	    out.println("KB3HHA Weather Server v1.0\r\n");
	    out.flush();

	    while (!m_bQuit)
	    {
		try
		{
//		    while (!in.ready() && !m_bQuit) {
//			try 
//			{
//			    Thread.sleep(100);
//			} 
//			catch (Exception e) 
//			{
//			    // ignore this exception
//			}
//		    }

//                        if (!m_bQuit)
//		    {
			String str = in.readLine().trim();
			str = str.toLowerCase();

			// weather data
			if (str.startsWith("w"))
			{
			    sendCurrentData(out);
			}

			// stats
			if (str.startsWith("s"))
			{
			    sendStatisticalData(out);
			}

			if (str.startsWith("l"))
			{
			    sendLogData(out);
			}

			if (str.startsWith("c"))
			{
			    sendConfiguration();
			}

			if (str.startsWith("q"))
			{
			    m_bQuit = true;
			}
//		    }
		}
//		catch(SocketTimeoutException ex)
//		{
//		    // if timed out then go ahead and quit 
//		    m_bQuit = true;
//		}
//		catch(IOException ex)
//		{
//                    m_bQuit = true;
//		}
		catch(Exception ex)
		{
		    // close up if timed out, I/O error, or any other exception
		    m_bQuit = true;
		}
	    }
	    
	    in.close();
	    out.close();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    try 
	    {
		m_socket.close();
	    } 
	    catch (IOException e) 
	    {
		ErrorLog.logStackTrace(e);
	    }
	}

        // remove this thread from the list of active threads
	m_wxServer.removeHandler(this);
    }

    public synchronized void setQuitFlag()
    {
	m_bQuit = true;
	// TODO: if quitting hangs in readLine() then close the open socket.  That will
	//     cause readLine() to throw an exception and the handler will exit
	//     another option is to use socket.shutdownInput()
    }

    private void sendCurrentData(PrintWriter out)
    {
	WeatherData data = m_station.getData();

	StringBuilder strBuf = new StringBuilder();
	strBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
	strBuf.append("<DOCUMENT>\r\n");
	if (data != null)
	{
	    strBuf.append(data.toXML(m_station.getStationConfiguration()));
	}
	strBuf.append("</DOCUMENT>\r\n");

	out.println(strBuf.toString());
	out.flush();

	// test the fromXML method by restoring a new WeatherData from the generated XML
	//        WeatherData newData = new WeatherData(System.currentTimeMillis(), 0.0f, 0.0f, 
	//                0.0f, 0, 0.0f, 0.0f);
	//        newData.fromXML(strBuf.toString());
	//        System.out.println(newData.toString(m_station.getStationConfiguration()));

	// attempt at using the XML parser classes to build XML and to parse it back
	//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	//        Document document;
	//        try {
	//          DocumentBuilder builder = factory.newDocumentBuilder();
	//          document = builder.newDocument();  // Create from whole cloth
	//          Element rootElement = document.createElement("DOCUMENT");
	//          document.appendChild(rootElement);
	//          
	//          data.toXML(m_station.getStationConfiguration(), document);
	//          
	//          // convert to String representation and send it out the port
	//          String xmlString = WeatherConversions.XMLDocumentToString(document);
	//          out.println(xmlString);
	//          
	//*** test - save this!!!
	// example of how to convert String to an XML document and use it 
	//        WeatherData newData = new WeatherData(System.currentTimeMillis(), 0.0f, 0.0f, 
	//                0.0f, 0, 0.0f, 0.0f);
	//          Document newDoc = WeatherConversions.stringToXMLDocument(xmlString);
	//          Node dataNode = newDoc.getFirstChild().getFirstChild();
	//            newData.fromXML(dataNode);
	//          //newData.fromXML(rootElement.getFirstChild());
	//        System.out.println(newData.toString(m_station.getStationConfiguration()));
	//          
	//        } catch (ParserConfigurationException pce) {
	//            // Parser with specified options can't be built
	//            pce.printStackTrace();
	//        } catch (TransformerFactoryConfigurationError e) {
	//            e.printStackTrace();
	//        }
    }

    private void sendLogData(PrintWriter out)
    {
	StationLog log = m_station.getLog();

	StringBuilder strBuf = new StringBuilder();
	strBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
	strBuf.append("<DOCUMENT>\r\n");
	if (log != null)
	{
	    String logData = log.toXML();
	    strBuf.append(logData);
	}
	else
	{
	    strBuf.append("Logging disabled");
	}
	strBuf.append("</DOCUMENT>\r\n");

	out.println(strBuf.toString());
	out.flush();

	//        // test the load from XML method
	//        StationLog newLog = new StationLog();
	//        newLog.fromXML(strBuf.toString());
	//        if (!logData.equalsIgnoreCase(newLog.toXML()))
	//        {
	//            System.out.println("Log mismatch");
	//            System.out.println("Before:");
	//            System.out.println(logData);
	//            System.out.println("\nAfter:");
	//            System.out.println(newLog.toXML());
	//        }

	// test using XML package
	//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	//        Document document;
	//        try {
	//            DocumentBuilder builder = factory.newDocumentBuilder();
	//            document = builder.newDocument();  // Create from whole cloth
	//            Element rootElement = document.createElement("DOCUMENT");
	//            document.appendChild(rootElement);
	//            
	//            log.toXML(document);
	//            // convert to String representation
	//            String xmlString = WeatherConversions.XMLDocumentToString(document);
	//            out.println(xmlString);
	//            System.out.println(xmlString);
	//            
	//          } catch (ParserConfigurationException pce) {
	//              // Parser with specified options can't be built
	//              pce.printStackTrace();
	//          } catch (TransformerFactoryConfigurationError e) {
	//              e.printStackTrace();
	//          }

    }

    private void sendStatisticalData(PrintWriter out)
    {
	WeatherStatistics stats = m_station.getStatistics();

	StringBuilder strBuf = new StringBuilder();
	strBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
	strBuf.append("<DOCUMENT>\r\n");
	if (stats != null)
	{
	    strBuf.append(stats.toXML());
	}
	else
	{
	    strBuf.append("Statistics are disabled");
	}
	strBuf.append("</DOCUMENT>\r\n");

	out.println(strBuf.toString());
	out.flush();

	//        WeatherStatistics newStats = new WeatherStatistics(m_station.getStationConfiguration());
	//        newStats.fromXML(strBuf.toString());
	//        if (!stats.toXML().equalsIgnoreCase(newStats.toXML()))
	//        {
	//            System.out.println("XML mismatch on statistics");
	//            System.out.println("Before:");
	//            System.out.println(stats.toXML());
	//            System.out.println("\nAfter:");
	//            System.out.println(newStats.toXML());
	//        }

    }

    private void sendConfiguration()
    {
	StationConfiguration config = m_station.getStationConfiguration();
	try 
	{
	    config.save(m_socket.getOutputStream());
	} 
	catch (IOException e) 
	{
	    ErrorLog.logStackTrace(e);
	}

    }
}
