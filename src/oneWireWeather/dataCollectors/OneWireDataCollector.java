package oneWireWeather.dataCollectors;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.utils.OWPath;

import oneWireWeather.ErrorLog;
import oneWireWeather.SensorConfiguration;
import oneWireWeather.SensorType;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherData;
import oneWireWeather.WeatherStation;
import oneWireWeather.sensors.AAGWindDirectionSensor;
import oneWireWeather.sensors.AAGWindSpeedSensor;
import oneWireWeather.sensors.ADSWindDirectionSensor;
import oneWireWeather.sensors.ADSWindSpeedSensor;
import oneWireWeather.sensors.BarometerSensor;
import oneWireWeather.sensors.HardwareSensor;
import oneWireWeather.sensors.HumiditySensor;
import oneWireWeather.sensors.LightningSensor;
import oneWireWeather.sensors.RainSensor;
import oneWireWeather.sensors.SolarSensor;
import oneWireWeather.sensors.TemperatureSensor;
import oneWireWeather.sensors.WindDirectionSensor;
import oneWireWeather.sensors.WindSpeedSensor;

public class OneWireDataCollector
{
    private StationConfiguration m_configuration;
    private DSPortAdapter m_adapter;

    private HashMap<String,Object> auxValues = new HashMap<String,Object>();
    private float temp = Float.MIN_VALUE;
    private float windSpeed = Float.MIN_VALUE;
    private float humidity = Float.MIN_VALUE;
    private int windDirection = WindDirectionSensor.WIND_DIRECTION_ERROR;
    private float pressure = Float.MIN_VALUE;
    private float rainfall = Float.MIN_VALUE;
    private float solar = Float.MIN_VALUE;
    private int lightning = Integer.MIN_VALUE;
    private float indoorTemperature = Float.MIN_VALUE;
    private float indoorHumidity = Float.MIN_VALUE;

    private Hashtable<String, HardwareSensor> m_sensorList = new Hashtable<String, HardwareSensor>();
    private Hashtable<String, Long> m_intervalList = new Hashtable<String, Long>();
    private Hashtable<String, SensorConfiguration> configurationEntries;

    public OneWireDataCollector(StationConfiguration configuration)
    {
        this.m_configuration = configuration;
    }
    
    public void open()
    {
        // get the configures sensor list from the configuration file
        configurationEntries = m_configuration.getSensorList();

        // set hardware configuration
        m_adapter = createAdapter();

        if (m_adapter != null)
        {
            // get list of devices and paths on net
            Hashtable<String, OWPath> devicePaths = StationConfiguration.walkNet(m_adapter);

            // look for devices on bus but not configured
            for (Enumeration<String> keys = devicePaths.keys(); keys.hasMoreElements(); )
            {
                String address = (String)keys.nextElement();
                OWPath path = (OWPath)devicePaths.get(address);

                // check if is in config file
                if (!configurationEntries.containsKey(address))
                {
                    // open path and get container so we can try to log what type of device this is
                    //  This info will be handy for debugging and modifying the config file by hand  
                    try
                    {
                        path.open();
                        OneWireContainer owc = m_adapter.getDeviceContainer(address);
                        ErrorLog.logError("Address on bus not in config file " + owc.getName() + " " + address + " path " + path);
                    }
                    catch (Exception e)
                    {
                        ErrorLog.logError("Address on bus not in config file " + address + " path " + path);
                    }

                }
                else
                {
                    SensorConfiguration sensor = configurationEntries.get(address);
                    SensorType type = WeatherStation.getSensorTypeForUsage(sensor.getUsageType());
                    ErrorLog.logError("Found address on bus: " + sensor.getName() + " " + type.getDescription() + " " + address + " at path " + path);
                }
            }

            // walk through config and match up with bus, report devices not found on bus
            for (Enumeration<SensorConfiguration> e = configurationEntries.elements(); e.hasMoreElements() ;) 
            {
                SensorConfiguration sensor = (SensorConfiguration)e.nextElement();
                OWPath path = devicePaths.get(sensor.getID());

                if (path != null)
                {
                    sensor.setPath(path);
                    addSensorToList(sensor);
                }
                else
                {
                    ErrorLog.logError("Address in config file but not on bus " + sensor.getName() + " " + sensor.getID());
                }
            }
        }
    }

    public WeatherData acquireData()
    {
        WeatherData data = null;

        try
        {
            if (m_adapter != null && m_adapter.adapterDetected())
            {
                m_adapter.beginExclusive(true);

                resetBus();

                // m_adapter.setSpeed(DSPortAdapter.SPEED_FLEX);

                data = localAcquireData();
            }
            else
            {
                // log an adapter error
                ErrorLog.logError("No adapter detected in run()");
                
                // we couldn't even find an adapter
                // clear data to show we got no reading at all
                data = new WeatherData(System.currentTimeMillis(), Float.MIN_VALUE, Float.MIN_VALUE, 
                        Float.MIN_VALUE, WindDirectionSensor.WIND_DIRECTION_ERROR, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, 0);

                // try and recover?
                // m_adapter.freePort();
                // m_adapter = createAdapter();
            }
        }
        catch (OneWireException ex)
        {
            // log an exception detecting the adapter
            ErrorLog.logError("Data acquisition error: " + ex);
        }
        finally
        {
            if (m_adapter != null)
            {
                m_adapter.endExclusive();
            }
        }
        
        return data;
    }
    
    public void close()
    {
        // clean up adapter port - this causes the serial port to be closed properly
        try 
        {
            m_adapter.freePort();
            m_adapter = null;
        } 
        catch (Exception e) 
        {
            // just log exception, no action to take
            ErrorLog.logError("Error exiting run");
            ErrorLog.logStackTrace(e);
        }
    }
    
    private WeatherData localAcquireData()
    {
        WeatherData data;
        long currentMillis = Calendar.getInstance().getTimeInMillis();

        synchronized (m_sensorList)
        {
            for (Enumeration<HardwareSensor> e = m_sensorList.elements(); e.hasMoreElements() ;) 
            {
                HardwareSensor sensor = (HardwareSensor)e.nextElement();

                if (currentMillis >= m_intervalList.get(sensor.getID()).longValue())
                {
                    switch(sensor.getUsageType())
                    {
                        case SensorType.MAIN_OUTDOOR_TEMP:
                            temp = ((TemperatureSensor)sensor).getTemperature();
                            break;

                        case SensorType.OUTDOOR_HUMIDITY:
                            humidity = ((HumiditySensor)sensor).getHumidity();
                            break;

                        case SensorType.AAG_WIND_DIRECTION:
                        case SensorType.ADS_WIND_DIRECTION: 
                            windDirection = ((WindDirectionSensor)sensor).getWindDirection();
                            break;

                        case SensorType.AAG_WIND_SPEED:
                        case SensorType.ADS_WIND_SPEED:
                            windSpeed = ((WindSpeedSensor)sensor).getWindSpeed();
                            break;

                        case SensorType.AUX_OUTDOOR_TEMP:
                        case SensorType.AUX_INDOOR_TEMP:
                            auxValues.put(sensor.getID(), 
                                    ((TemperatureSensor)sensor).getTemperature());
                            break;

                        case SensorType.PRESSURE:
                            pressure = ((BarometerSensor)sensor).getPressure();
                            break;

                        case SensorType.RAIN_COUNTER:
                            rainfall = ((RainSensor)sensor).getRainCount();
                            break;

                        case SensorType.MAIN_INDOOR_TEMP:
                            indoorTemperature = ((TemperatureSensor)sensor).getTemperature();
                            break;

                        case SensorType.INDOOR_HUMIDITY:
                            indoorHumidity = ((HumiditySensor)sensor).getHumidity();
                            break;

                        case SensorType.SOLAR:
                            solar = ((SolarSensor)sensor).getSolarLevel();
                            break;

                        case SensorType.LIGHTNING_COUNTER:
                            lightning = ((LightningSensor)sensor).getLightningCount();
                            break;

                    }

                    m_intervalList.put(sensor.getID(), new Long(currentMillis + sensor.getPollFrequency()));
                }
            }
        }

        data = new WeatherData(System.currentTimeMillis(), temp, humidity, 
                windSpeed, windDirection, rainfall, pressure, indoorTemperature, indoorHumidity, solar, lightning);

        // add aux values *** do we need to expire old values?
        data.putExtraSensorValues(auxValues);

        return data;
    }

    /**
     * @param sensorsOnBus
     * @param sensor
     */
    private void addSensorToList(SensorConfiguration sensor)
    {
        int usageType = sensor.getUsageType();
        long currentTime = System.currentTimeMillis();
        HardwareSensor newSensor = null;

        // instantiate the correct hardware sensor and do an initial read
        switch (usageType)
        {
            case SensorType.MAIN_INDOOR_TEMP:
            case SensorType.MAIN_OUTDOOR_TEMP:
            case SensorType.AUX_INDOOR_TEMP:
            case SensorType.AUX_OUTDOOR_TEMP:
                newSensor = new TemperatureSensor(m_adapter, sensor);

                if (newSensor.isEnabled())
                {
                    if (usageType == SensorType.MAIN_OUTDOOR_TEMP)
                    {
                        temp = ((TemperatureSensor)newSensor).getTemperature();
                    }
                    if (usageType == SensorType.MAIN_INDOOR_TEMP)
                    {
                        indoorTemperature = ((TemperatureSensor)newSensor).getTemperature();
                    }
                }
                break;

            case SensorType.AAG_WIND_DIRECTION:
                newSensor = new AAGWindDirectionSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {
                    windDirection = ((WindDirectionSensor)newSensor).getWindDirection();
                }
                break;

            case SensorType.AAG_WIND_SPEED:
                newSensor = new AAGWindSpeedSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {
                    windSpeed = ((WindSpeedSensor)newSensor).getWindSpeed();
                }
                break;

            case SensorType.OUTDOOR_HUMIDITY:
            case SensorType.INDOOR_HUMIDITY:
                newSensor = new HumiditySensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {
                    if (usageType == SensorType.OUTDOOR_HUMIDITY)
                        humidity = ((HumiditySensor)newSensor).getHumidity();
                    else
                        indoorHumidity = ((HumiditySensor)newSensor).getHumidity();
                }
                break;

            case SensorType.PRESSURE:
                newSensor = new BarometerSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {                       
                    pressure = ((BarometerSensor)newSensor).getPressure();
                }
                break;

            case SensorType.RAIN_COUNTER:
                newSensor = new RainSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {                       
                    rainfall = ((RainSensor)newSensor).getRainCount();
                }
                break;

            case SensorType.SOLAR:
                newSensor = new SolarSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {
                    solar = ((SolarSensor)newSensor).getSolarLevel();
                }
                break;

            case SensorType.LIGHTNING_COUNTER:
                newSensor = new LightningSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {
                    lightning = ((LightningSensor)newSensor).getLightningCount();
                }
                break;

            case SensorType.ADS_WIND_DIRECTION:
                newSensor = new ADSWindDirectionSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {
                    windDirection = ((WindDirectionSensor)newSensor).getWindDirection();
                }
                break;

            case SensorType.ADS_WIND_SPEED:
                newSensor = new ADSWindSpeedSensor(m_adapter, sensor);
                if (newSensor.isEnabled())
                {
                    windSpeed = ((WindSpeedSensor)newSensor).getWindSpeed();
                }
                break;

        }

        if (newSensor != null)
        {
            synchronized (m_sensorList)
            {
                m_sensorList.put(sensor.getID(), newSensor);
                m_intervalList.put(sensor.getID(), new Long(currentTime + sensor.getPollOffset()));
            }
        }
    }

    private DSPortAdapter createAdapter()
    {
        // create hardware configuration
        try
        {
            ErrorLog.logError("Getting adapter " + m_configuration.getAdapterName() + " on port " + m_configuration.getAdapterPortName());

            m_adapter = OneWireAccessProvider.getAdapter(m_configuration.getAdapterName(), 
                    m_configuration.getAdapterPortName());

            StringBuilder builder = new StringBuilder();
            builder.append("  Class name: " + m_adapter.getClass().getName());
            builder.append(" Port type: " + m_adapter.getPortTypeDescription() + "\n");
            builder.append("  Class version: " + m_adapter.getClassVersion() + "\n");
            builder.append("  Adapter name: " + m_adapter.getAdapterName());
            builder.append(" version: " + m_adapter.getAdapterVersion());
            builder.append(" address: " + m_adapter.getAdapterAddress() + "\n");
            builder.append("  Can break: " + m_adapter.canBreak());
            builder.append(" deliver power: " + m_adapter.canDeliverPower());
            builder.append(" deliver smart power: " + m_adapter.canDeliverSmartPower());
            builder.append(" flex: " + m_adapter.canFlex());
            builder.append(" hyperdrive: " + m_adapter.canHyperdrive());
            builder.append(" overdrive: " + m_adapter.canOverdrive());
            builder.append(" program: " + m_adapter.canProgram());
            builder.append(" Speed: " + m_adapter.getSpeed());

            ErrorLog.logError("Adapter found");
            ErrorLog.logError(builder.toString());

        }
        catch (OneWireException e)
        {
            ErrorLog.logError("Error creating the adapter: " + e);
            return null;
        }

        resetBus();

        return m_adapter;
    }

    private void resetBus()
    {
        if (m_adapter == null)
            return;

        try
        {
            if (!m_adapter.adapterDetected())
            {
                ErrorLog.logError("No adapter detected in resetBus()");
                // *** test will this even work?
                //              m_adapter = createAdapter();
                return;
            }
        }
        catch (OneWireException ex)
        {
            ErrorLog.logError("Exception in resetBus() " + ex);
            return;
        }

        // reset the 1-wire bus
        try
        {
            int result = m_adapter.reset();
            m_adapter.setSearchAllDevices();
            m_adapter.targetAllFamilies();

            if (result == DSPortAdapter.RESET_NOPRESENCE)
            {
                ErrorLog.logError("Warning: Reset indicates no Device Present");
            }
            else if (result == DSPortAdapter.RESET_SHORT)
            {
                ErrorLog.logError("Warning: Reset indicates 1-Wire bus is shorted.");
            }
        }
        catch (OneWireException ex)
        {
            ErrorLog.logError("Exception resetting the bus: " + ex);
        }

        // TODO: reset hub?
    }

}
