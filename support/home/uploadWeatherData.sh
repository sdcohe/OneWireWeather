#!/bin/bash

WEATHER_HOME=/var/weather
HOST=www.cloppermillweather.org
USERNAME=enter FTP username here
PASSWORD=enter FTP password user name here
FILE=weatherData.xml

for i in 1 2 3 4
do
	echo `date` Upload started
	cd /tmp
	
	# get the station data
	java -cp $WEATHER_HOME/logRetriever.jar GetWeatherStationLogs.LogRetriever data
	
	# check for error retrieving data
	
	#upload to web site via FTP
	curl -silent -u $USERNAME:$PASSWORD --user-agent "CurlUploader" -F"operation=upload" -F"file=@weatherData.xml" http://$HOST/uploadweatherdata.php
	
	echo `date` Upload finished
	
	# fine tune this for the particular server
	sleep 8

done

