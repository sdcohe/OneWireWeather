/* SVN FILE: $Id: DatabasePublisher.java 183 2015-12-06 20:58:06Z seth $ */
/**
 * One Wire Weather : Weather station daemon for a 1-wire weather station
 *
 * $Author: seth $
 * $Revision: 183 $
 * $Date: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
 * $LastChangedBy: seth $
 * $LastChangedDate: 2015-12-06 15:58:06 -0500 (Sun, 06 Dec 2015) $
 * $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/publishers/DatabasePublisher.java $
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

package oneWireWeather.publishers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import oneWireWeather.ErrorLog;
import oneWireWeather.StationConfiguration;
import oneWireWeather.WeatherConversions;
import oneWireWeather.WeatherData;
import oneWireWeather.WeatherStation;
import oneWireWeather.sensors.WindDirectionSensor;

/**
 * @author seth
 *
 */
public class DatabasePublisher extends WeatherDataPublisher
{
	// jdbc:postgresql://192.168.123.143/weather?user=weather&password=&ssl=true
	private String JDBCConnectString; // = "jdbc:postgresql://192.168.123.122/weather?user=weather&password=";
	private Connection conn;
	private boolean persistConnectionFlag;

	public DatabasePublisher(StationConfiguration m_configuration, WeatherStation m_station)
	{
		super(m_configuration, m_station);
		setConfiguration();
		this.setDebugName("JDBC");

		if (isDebugFlag())
		{
			ErrorLog.logError("JDBC: logging to " + this.getJDBCConnectString() + " interval " + this.getPublishInterval() + "secs. persistent connection " + this.isPersistConnectionFlag());
		}
	}

	/* (non-Javadoc)
	 * @see oneWireWeather.ConfigurationChangeListener#notifyConfigurationChange(oneWireWeather.StationConfiguration)
	 */
	@Override
	public void notifyConfigurationChange(StationConfiguration config)
	{
		this.m_configuration = config;
		setConfiguration();
	}

	private void setConfiguration()
	{
		this.setDebugFlag(m_configuration.isDatabaseDebugFlag());
		this.setEnabled(m_configuration.isDatabaseEnableFlag());
		this.setPublishInterval(m_configuration.getDatabaseUpdateInterval() * 1000);
		this.setJDBCConnectString(m_configuration.getJDBCConnectString());
		this.setPersistConnectionFlag(m_configuration.isPersistDBConnection());
	}

	private void setJDBCConnectString(String connectString)
	{
		this.JDBCConnectString = connectString;
	}

	private String getJDBCConnectString()
	{
		return this.JDBCConnectString;
	}

	/* (non-Javadoc)
	 * @see oneWireWeather.WeatherDataPublisher#publishData()
	 */
	@Override
	protected void publishData()
	{
		if (conn == null)
		{
			conn = getConnection();
		}

		// save data
		WeatherData data = m_station.getData();
		String sql = "insert into weatherdata values(?,?,?,?,?,?,?,?,?,?,?) ";
		//	String sql = "insert into weatherdata" 
		//		+ " (sample_time, outdoor_temperature, outdoor_humidity, wind_speed, wind_direction, rain_fall, pressure, solar, lightning, indoor_temperature, indoor_humidity)"
		//		+ " values(?,?,?,?,?,?,?,?,?,?,?) ";
		if (isDebugFlag())
		{
			ErrorLog.logError("JDBC: Updating database");
		}

		// create prepared statement
		try
		{
			PreparedStatement pst = conn.prepareStatement(sql);

			// set parameters and execute
			try
			{
				// timestamp is the table PK.  This will prevent storing duplicate records
				// all other values must have surrogate nulls converted to database nulls 
				pst.setTimestamp(1, new Timestamp(data.getDateTimeMillis()));
				setField(pst, 2, data.getTemperature());
				setField(pst, 3, data.getHumidity());
				setField(pst, 4, data.getWindSpeed());
				setWindDirectionField(pst, 5, data.getWindDirection());
				setField(pst, 6, data.getRainfall());
				setField(pst, 7, data.getPressure());
				setField(pst, 8, data.getSolar());
				setField(pst, 9, data.getLightning());
				setField(pst, 10, data.getIndoorTemperature());
				setField(pst, 11, data.getIndoorHumidity());

				pst.executeUpdate();

				if (isDebugFlag())
				{
					ErrorLog.logError("JDBC: Update complete");
				}
			}
			catch (Exception e)
			{
				ErrorLog.logError("JDBC error updating DB " + e.getMessage());
			}
			finally
			{
				try
				{
					pst.close();
				}
				catch (Exception ex)
				{
					ErrorLog.logError("JDBC error closing statement " + ex.getMessage());
				}
			}
		}
		catch (Exception e)
		{
			// fatal error creating statement.  Connection to database is probably no longer valid
			// Close connection and try again next go round
			ErrorLog.logError("JDBC error creating statement " + e.getMessage());
			closeConnection();
		}

		// check if connection should remain open (persistent connection)
		if (!isPersistConnectionFlag())
		{
			closeConnection();
		}
	}

	private void setWindDirectionField(PreparedStatement pst, int field, int value)
	{
		try
		{
			if (value == WindDirectionSensor.WIND_DIRECTION_ERROR)
			{
				pst.setNull(field, java.sql.Types.INTEGER);
			}
			else
			{
				pst.setInt(field, WeatherConversions.windDirToCompass(value));
			}
		}
		catch (SQLException e)
		{
			ErrorLog.logError("JDBC error setting field " + e.getMessage());
		}
	}

	private void setField(PreparedStatement pst, int field, float value)
	{
		try
		{
			if (value == Float.MIN_VALUE || value == Float.MAX_VALUE)
			{
				pst.setNull(field, java.sql.Types.FLOAT);
			}
			else
			{
				pst.setFloat(field, value);
			}
		}
		catch (SQLException e)
		{
			ErrorLog.logError("JDBC error setting field " + e.getMessage());
		}
	}

	private Connection getConnection()  
	{
		if (isDebugFlag())
		{
			ErrorLog.logError("JDBC: Opening connection for " + this.getJDBCConnectString());
		}

		Connection conn = null;
		Properties connectionProps = new Properties();
		//	    connectionProps.put("user", "");
		//	    connectionProps.put("password", "");
		try
		{
			conn = DriverManager.getConnection(this.getJDBCConnectString(), connectionProps);
		}
		catch (SQLException e)
		{
			ErrorLog.logError("JDBC error opening DB connection " + e.getMessage());
		}

		return conn;
	}

	/* (non-Javadoc)
	 * @see oneWireWeather.WeatherDataPublisher#startPublishing()
	 */
	@Override
	public void startPublishing()
	{
		conn = getConnection();
		super.startPublishing();
	}

	/* (non-Javadoc)
	 * @see oneWireWeather.WeatherDataPublisher#stopPublishing()
	 */
	@Override
	public void stopPublishing()
	{
		closeConnection();
		super.stopPublishing();
	}

	private void closeConnection()
	{
		if (conn != null)
		{
			if (isDebugFlag())
			{
				ErrorLog.logError("JDBC: Closing connection for " + this.JDBCConnectString);
			}
			try
			{
				conn.close();
			}
			catch (SQLException e)
			{
				ErrorLog.logError("JDBC error closing DB connection " + e.getMessage());
			}
		}

		conn = null;
	}

	/**
	 * @return the persistConnectionFlag
	 */
	public boolean isPersistConnectionFlag()
	{
		return persistConnectionFlag;
	}

	/**
	 * @param persistConnectionFlag the persistConnectionFlag to set
	 */
	public void setPersistConnectionFlag(boolean persistConnectionFlag)
	{
		this.persistConnectionFlag = persistConnectionFlag;
	}
}
