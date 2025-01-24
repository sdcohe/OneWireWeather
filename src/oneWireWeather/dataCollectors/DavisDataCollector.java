/* SVN FILE: $Id: DavisDataCollector.java 314 2019-06-18 18:29:02Z  $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: $
* $Revision: 314 $
* $Date: 2019-06-18 14:29:02 -0400 (Tue, 18 Jun 2019) $
* $LastChangedBy: $
* $LastChangedDate: 2019-06-18 14:29:02 -0400 (Tue, 18 Jun 2019) $
* $URL: http://192.168.123.7/svn/OneWireWeather/src/oneWireWeather/dataCollectors/DavisDataCollector.java $
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

package oneWireWeather.dataCollectors;

import java.io.ByteArrayOutputStream;
//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import oneWireWeather.ErrorLog;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherData;
import oneWireWeather.sensors.WindDirectionSensor;

public class DavisDataCollector
{
    public static void main(String[] args)
    {
        System.out.println("Testing Davis data collector");

        StationConfiguration config = new StationConfiguration();
        config.load("D:/Users/seth.CHERRYWOOD/Documents/workspace/OneWireWeather/weatherConfig.xml");
        DavisDataCollector collector = new DavisDataCollector(config);

        collector.open();

        WeatherData data = collector.acquireData();
        System.out.println("data:");
        System.out.println(data.toString(config));

        collector.close();

        System.out.println("End of test");
    }

    private static final long crc_table[] =
    { 
            0x0,    0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 
            0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 
            0x1231, 0x210,  0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6, 
            0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 
            0x2462, 0x3443, 0x420,  0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485, 
            0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 
            0x3653, 0x2672, 0x1611, 0x630,  0x76d7, 0x66f6, 0x5695, 0x46b4, 
            0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc, 
            0x48c4, 0x58e5, 0x6886, 0x78a7, 0x840,  0x1861, 0x2802, 0x3823, 
            0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b, 
            0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0xa50,  0x3a33, 0x2a12, 
            0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a, 
            0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0xc60,  0x1c41, 
            0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49, 
            0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0xe70, 
            0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78, 
            0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f, 
            0x1080, 0xa1,   0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067, 
            0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e, 
            0x2b1,  0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256, 
            0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d, 
            0x34e2, 0x24c3, 0x14a0, 0x481,  0x7466, 0x6447, 0x5424, 0x4405, 
            0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c, 
            0x26d3, 0x36f2, 0x691,  0x16b0, 0x6657, 0x7676, 0x4615, 0x5634, 
            0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab, 
            0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x8e1,  0x3882, 0x28a3, 
            0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a, 
            0x4a75, 0x5a54, 0x6a37, 0x7a16, 0xaf1,  0x1ad0, 0x2ab3, 0x3a92, 
            0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9, 
            0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0xcc1, 
            0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8, 
            0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0xed1,  0x1ef0 
    };

    private static final int ACK = 0x06;
//    private static final int NAK = 0x21;
//    private static final int CANCEL = 0x18;

    private StationConfiguration m_configuration;
    private SerialPort serialPort = null;

    private PrintStream out = null;
    private InputStream in = null;
    
    private byte consoleType;

    public DavisDataCollector(StationConfiguration configuration)
    {
        this.m_configuration = configuration;
    }

    public void open()
    {
        ErrorLog.logError("Opening Davis serial port " + m_configuration.getDavisComPort());
        
        // open com port
        CommPortIdentifier portIdentifier;
        try
        {
            portIdentifier = CommPortIdentifier.getPortIdentifier(m_configuration.getDavisComPort());
            if (portIdentifier.isCurrentlyOwned())
            {
                ErrorLog.logError("Davis data collector port " + m_configuration.getDavisComPort() + " is in use");
            }
            else
            {
                ErrorLog.logError("Davis port found");
                
                CommPort commPort;
                try
                {
                    commPort = portIdentifier.open(this.getClass().getName(), 2000);
                    if (commPort instanceof SerialPort)
                    {
                        serialPort = (SerialPort) commPort;
                        try
                        {
                            ErrorLog.logError("Davis serial port is open");
                            ErrorLog.logError("Davis Setting serial port parameters");
                            
                            serialPort.setSerialPortParams(
                                    m_configuration.getDavisBaudRate(),
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);
                            serialPort.enableReceiveTimeout(1000);
                            serialPort.enableReceiveThreshold(0);

                            ErrorLog.logError("Setting up serial port reader and writer");
                            
                            // // set up buffered readers & writers to the port
                            out = new PrintStream(serialPort.getOutputStream());
                            in = serialPort.getInputStream();

                            // get version info from console
                            consoleType = getConsoleType();

                            String firmwareVersion = getConsoleFirmwareVersion();
                            String firmwareDateCode = getConsoleFirmwareDateCode();
                            
                            ErrorLog.logError("Davis console type " + Byte.toString(consoleType) + " " + getConsoleNameFromType(consoleType) +
                                    " firmware revision " + firmwareVersion + " " + firmwareDateCode);
                        }
                        catch (UnsupportedCommOperationException e)
                        {
                            ErrorLog.logError("Davis unsupported serial port operation " + e);
                            serialPort = null;
                        }
                        catch (IOException e)
                        {
                            ErrorLog.logError("Davis I/O exception " + e);
                            serialPort = null;
                        }
                    }
                    else
                    {
                        ErrorLog.logError("Davis port is not a serial port");
                        commPort.close();
                        serialPort = null;
                    }
                }
                catch (PortInUseException e1)
                {
                    ErrorLog.logError("Davis port is in use: " + e1.getMessage());
                    serialPort = null;
                }
            }
        }
        catch (NoSuchPortException e2)
        {
            ErrorLog.logError("Davis no such port exists");
            serialPort = null;
        }
    }

    public WeatherData acquireData()
    {
        int retryCount = m_configuration.getDavisRetryCount();
        boolean isConsoleAwake = false;
        
        WeatherData data = new WeatherData(System.currentTimeMillis(), Float.MIN_VALUE,
                Float.MIN_VALUE, Float.MIN_VALUE,
                WindDirectionSensor.WIND_DIRECTION_ERROR, Float.MIN_VALUE,
                Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE,
                Float.MIN_VALUE, 0);

        if (serialPort == null)
        {
            return null;
        }
        
        while (!isConsoleAwake && retryCount > 0)
        {
            isConsoleAwake = wakeupConsole();
            retryCount--;
        }

        if (isConsoleAwake)
        {
            retryCount = m_configuration.getDavisRetryCount();
            boolean isPacketValid = false;

            while (!isPacketValid && retryCount-- > 0)
            {
                sendConsoleCommand("LOOP 1".getBytes());
                byte[] buffer = readConsole();

                // if the buffer is valid then
                if (bufferIsValid(buffer))
                {
                    // extract the packet
                    byte[] packet = Arrays.copyOfRange(buffer, 1, buffer.length);

                    // if the packet CRC is valid
                    if (checkCRC(packet))
                    {
                        isPacketValid = true;
                        data = parseLoopPacket(packet);
                    }
                    else
                    {
//                        ErrorLog.logError("Davis packet CRC failure");
                    }
                }
                else
                {
                    // invalid buffer
//                    ErrorLog.logError("Davis buffer invalid");
                }

                // if we didn't get a valid packet then pause before retrying
                if (!isPacketValid)
                {
                    delay(500);
                }
            }
            
            if (!isPacketValid)
            {
                // log inability to get a valid packet after all retries
                ErrorLog.logError("Davis retry count exceeded in acquireData()");
                return null;
            }
        }
        else
        {
            // log communications error - console not responding
            ErrorLog.logError("Unable to wake up Davis console");
            return null;
        }

        return data;
    }

    public void close()
    {
        ErrorLog.logError("Closing Davis port");
        
        // close any open streams
        if (out != null)
        {
            out.close();
        }

        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                ErrorLog.logError("Davis error closing serial port " + e);
            }
        }

        // close com port
        if (serialPort != null)
        {
            serialPort.close();
        }
    }

    private boolean wakeupConsole()
    {
        int retryCount = 3;
        boolean isConsoleAwake = false;

        while (retryCount > 0 && !isConsoleAwake)
        {
            sendConsoleCommand(new String("").getBytes());
            delay(250);

            try
            {
                Byte[] buffer = new Byte[100];
                int idx = 0;
                while (in.available() > 0 && idx < 100)
                {
                    buffer[idx++] = (byte) in.read();
                }
                if (idx >= 2)
                {
                    isConsoleAwake = (buffer[idx - 2] == 0x0a
                            && buffer[idx - 1] == 0x0d);
                }
            }
            catch (IOException e)
            {
                ErrorLog.logError("Davis I/O exception waking up console");
                ErrorLog.logStackTrace(e);
            }

            retryCount--;

            if (!isConsoleAwake)
            {
                delay(500);
            }
        }

        return isConsoleAwake;
    }

    private void sendConsoleCommand(byte[] commandToSend)
    {
        try
        {
            out.write(commandToSend);
            out.write(0x0a);
            delay(100);
        }
        catch (IOException e)
        {
            ErrorLog.logError("Davis I/O exception sending command to console");
            ErrorLog.logStackTrace(e);
        }
    }

    // return the console type as defined by Davis
    // return -1 to signify an error
    //
    // The return values are as follows:
    // Value    Station
    // -----    -------
    // 0        Wizard III
    // 1        Wizard II
    // 2        Monitor
    // 3        Perception
    // 4        GroWeather
    // 5        Energy Enviromontor
    // 6        Health Enviromonitor
    // 16       Vantage Pro, Vantage Pro 2
    // 17       Vantage Vue
    private byte getConsoleType()
    {
        if (wakeupConsole())
        {
            byte[] command = new byte[5];
            command[0] = 'W';
            command[1] = 'R';
            command[2] = 'D';
            command[3] = 0x12;
            command[4] = 0x4d;
            sendConsoleCommand(command);

            byte[] buffer = readConsole();
            if (buffer[0] == ACK)
            {
                return buffer[1];
            }
            else
            {
                return -1;
            }
        }
        else
        {
            ErrorLog.logError("Davis unable to wake up console to get console type");
        }

        return -1;
    }
    
    private String getConsoleNameFromType(byte consoleType)
    {
        switch (consoleType)
        {
            case 0:
                return "Wizard III";
            case 1:
                return "Wizard II";
            case 2:
                return "Monitor";
            case 3: 
                return "Perception";
            case 4: 
                return "GroWeather";
            case 5: 
                return "Energy Enviromontor";
            case 6: 
                return "Health Enviromonitor";
            case 16: 
                return "Vantage Pro, Vantage Pro 2";
            case 17: 
                return "Vantage Vue";
        }
        
        return "Unknown console type";
    }

    private byte[] readConsole()
    {
        int retries = 0;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try
        {
            while (in.available() <= 0 && retries++ < 5)
            {
                delay(100);
            }

            while (in.available() > 0)
            {
                buffer.write(in.read());
            }

            return buffer.toByteArray();
        }
        catch (IOException e)
        {
            ErrorLog.logError("Davis I/O exception reading console");
            ErrorLog.logStackTrace(e);

            return null;
        }
    }

    private String getConsoleFirmwareVersion()
    {
        if (wakeupConsole())
        {
            sendConsoleCommand(new String("NVER").getBytes());
            byte[] buffer = readConsole();

            // for(int i = 0; i < buffer.length; i++)
            // {
            // System.out.println(i + " " + (int)buffer[i]);
            // }

            // skip 1st 5 bytes
            return new String(buffer, 6, buffer.length - 8);
        }
        else
        {
            ErrorLog.logError("Davis unable to wake up console to get firmware version");
        }

        return null;
    }

    private String getConsoleFirmwareDateCode()
    {
        if (wakeupConsole())
        {
            sendConsoleCommand(new String("VER").getBytes());
            byte[] buffer = readConsole();

            // for(int i = 0; i < buffer.length; i++)
            // {
            // System.out.println(i + " " + (int)buffer[i]);
            // }

            // skip 1st 5 bytes
            return new String(buffer, 6, buffer.length - 8);
        }
        else
        {
            ErrorLog.logError("Davis unable to wake up console to get firmware daet code");
        }

        return null;
    }

    private int calculateCRC(byte[] buffer)
    {
        long crc = 0L;

        for (int i = 0; i < buffer.length; i++)
        {
            try
            {
                crc = (crc_table[(int)((crc >> 8) ^ buffer[i] & 0xff)] ^ (crc << 8))
                        & 0xffffL;
            }
            catch (Exception ex)
            {
                ErrorLog.logError("Exception in calculateCRC i=" + i + " exception=" + ex.getMessage());
                ErrorLog.logError("CRC table index = " + (int) ((crc >> 8) ^ buffer[i] & 0xff));
                return -1;
            }
        }

        return (int) crc;
    }

    private boolean checkCRC(byte[] bufferWithCRC)
    {
        long crc = calculateCRC(bufferWithCRC);

        return (crc == 0);
    }

    private void delay(long count)
    {
        try
        {
            Thread.sleep(count);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }
    }
    
    private boolean bufferIsValid(byte[] buffer)
    {
        return (buffer != null && buffer.length >= 99 && buffer[0] == ACK);
        
    }
    
    private WeatherData parseLoopPacket(byte[] packet)
    {
        float temp = Float.MIN_VALUE;
        float windSpeed = Float.MIN_VALUE;
        float humidity = Float.MIN_VALUE;
        int windDirection = WindDirectionSensor.WIND_DIRECTION_ERROR;
        float pressure = Float.MIN_VALUE;
        float rainfall = Float.MIN_VALUE;
        float solar = Float.MIN_VALUE;
        int lightning = Integer.MIN_VALUE;
        float indoorTemperature = Float.MIN_VALUE;
        float indoorHumidity = Float.MIN_VALUE;

        if (packet[3] != 'P')
        {
            // 5 is barometer trend
        }

        // get pressure
        if (m_configuration.isDavisPressureEnabled())
        {
        	int value = convertTwoBytesToSignedInt((byte)(packet[8] & 0xff), (byte) (packet[7] & 0xff));
        	if (value != Short.MAX_VALUE) {
        		pressure = value / 1000.0f;
        	}
        }

        // get indoor temp
        if (m_configuration.isDavisIndoorTemperatureEnabled())
        {
        	int value = convertTwoBytesToSignedInt((byte)(packet[10] & 0xff), (byte) (packet[9] & 0xff));
        	if (value != Short.MAX_VALUE) {
        		indoorTemperature = value / 10.0f;
        	}
        }

        // get indoor humidity
        if (m_configuration.isDavisIndoorHumidityEnabled())
        {
            indoorHumidity = packet[11] & 0xff;
        }

        // get outdoor temp
        if (m_configuration.isDavisOutdoorTemperatureEnabled())
        {
        	int value =  convertTwoBytesToSignedInt((byte)(packet[13] & 0xff), (byte) (packet[12] & 0xff));
        	if (value != Short.MAX_VALUE) {
        		temp = value / 10.0f;
        	}
        }
        
        // get outdoor humidity
        if (m_configuration.isDavisOutdoorHumidityEnabled())
        {
            humidity = packet[33] & 0xff;
        }

        // get wind speed
        if (m_configuration.isDavisWindSpeedEnabled())
        {
            windSpeed = packet[14] & 0xff;
        }

        // get wind direction
        if (m_configuration.isDavisWindDirectionEnabled())
        {
            windDirection = ((int) ((((packet[17] & 0xff) << 8) + (packet[16] & 0xff))));
            
            if (windDirection == 0)
            {
                windDirection = WindDirectionSensor.WIND_DIRECTION_ERROR;
            }
            else
            {
                windDirection = Math.round(windDirection / 22.5f);
                if (windDirection == 16)
                {
                    windDirection = 0;
                }
            }
        }
        
        // rainfall
        if (m_configuration.isDavisRainfallEnabled())
        {
            rainfall = ((float) ((((packet[55] & 0xff) << 8)
                    + (packet[54] & 0xff)) / 100.0f));
        }
        
        // check ISS type and get solar information if the ISS supports it 
        if (consoleType == 16 && m_configuration.isDavisSolarEnabled())
        {
        	int value = convertTwoBytesToSignedInt((byte)(packet[45] & 0xff), (byte) (packet[44] & 0xff));
        	if (value != Short.MAX_VALUE) {
        		solar = value / 10.0f;
        	}
        }
        else
        {
            solar = 0.0f;
        }
        
        if (m_configuration.isDavisLightningEnabled())
        {
            lightning = 0;
        }
        else
        {
            lightning = 0;
        }
        
        // create weatherData class
        WeatherData data = new WeatherData(System.currentTimeMillis(), temp,
                humidity, windSpeed, windDirection, rainfall,
                pressure, indoorTemperature, indoorHumidity, solar,
                lightning);

        return data;
    }
    
    private int convertTwoBytesToSignedInt(byte highOrderByte, byte lowOrderByte)
    {
        /* Note: byte order of a ByteBuffer defaults to big endian unless specified
         *       so the system byte order does not need to be taken into account
         *
         * The input parameters are marked high order byte (the part of the number to be 
         *      multiplied by 256) and low order byte
        */
        
        // follow rules for big endian, regardless of native byte ordering for the processor
        byte[] byteArray = new byte[2];
        byteArray[0] = highOrderByte;
        byteArray[1] = lowOrderByte;
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        
        return byteBuffer.getShort();
    }
}
