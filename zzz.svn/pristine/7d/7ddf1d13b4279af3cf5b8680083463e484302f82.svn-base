#!/bin/bash

WEATHER_HOME=/var/weather
HOST=www.cloppermillweather.org
USERNAME=enter FTP username here
PASSWORD=enter FTP password here
FILE=weatherStats.xml

echo `date` Upload started
cd /tmp

# get the station data
java -cp $WEATHER_HOME/logRetriever.jar GetWeatherStationLogs.LogRetriever stats

# check for error retrieving data

#upload to web site via FTP
curl -u $USERNAME:$PASSWORD --user-agent "CurlUploader" -F"operation=upload" -F"file=@weatherStats.xml" http://$HOST/uploadweatherdata.php

echo `date` Upload finished
