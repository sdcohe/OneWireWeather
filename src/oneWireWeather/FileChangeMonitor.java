/* SVN FILE: $Id: FileChangeMonitor.java 503 2020-10-04 18:35:55Z  $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: $
* $Revision: 503 $
* $Date: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
* $LastChangedBy: $
* $LastChangedDate: 2020-10-04 14:35:55 -0400 (Sun, 04 Oct 2020) $
* $URL: http://192.168.123.7/svn/OneWireWeather/src/oneWireWeather/FileChangeMonitor.java $
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

import java.util.*;
import java.io.*;
import java.net.*;

public class FileChangeMonitor {
    
    private static final FileChangeMonitor instance = new FileChangeMonitor();

    private Timer timer;
    private Hashtable<String, FileMonitorTask> timerEntries;

    public static FileChangeMonitor getInstance() {
        return instance;
    }

    protected FileChangeMonitor() { 
        
        // Create timer, run timer thread as daemon.
        timer = new Timer(true);
        timerEntries = new Hashtable<String, FileMonitorTask>();
    }

    /** Add a monitored file with a FileChangeListener.
     * @param listener listener to notify when the file changed.
     * @param fileName name of the file to monitor.
     * @param period polling period in milliseconds.
     */
    public void addFileChangeListener(FileChangeListener listener, String fileName, long period) throws FileNotFoundException {
        
        removeFileChangeListener(listener, fileName);
        FileMonitorTask task = new FileMonitorTask(listener, fileName);
        timerEntries.put(fileName + listener.hashCode(), task);
        timer.schedule(task, period, period);
    }

    /** Remove the listener from the notification list.
     * @param listener the listener to be removed.
     * @param fileName name of the file that was being monitored.
     */
    public void removeFileChangeListener(FileChangeListener listener, String fileName) {
        FileMonitorTask task = (FileMonitorTask) timerEntries.remove(fileName + listener.hashCode());
        if (task != null) {
            task.cancel();
        }
    }

    protected void fireFileChangeEvent(FileChangeListener listener, String fileName) {
        listener.fileChanged(fileName);
    }

    class FileMonitorTask extends TimerTask {
        
        FileChangeListener listener; 
        String fileName;
        File monitoredFile;
        long lastModified;

        public FileMonitorTask(FileChangeListener listener, String fileName) throws FileNotFoundException {
            this.listener = listener;
            this.fileName = fileName;
            this.lastModified = 0;

            monitoredFile = new File(fileName);
            if (!monitoredFile.exists()) {  // but is it on CLASSPATH?
                URL fileURL = listener.getClass().getClassLoader().getResource(fileName);
                if (fileURL != null) {
                    monitoredFile = new File(fileURL.getFile());
                }
                else {
                    throw new FileNotFoundException("File Not Found: " 
                            + fileName);
                }
            }
            this.lastModified = monitoredFile.lastModified();
        }

        public void run() {
            long lastModified = monitoredFile.lastModified();
            if (lastModified != this.lastModified) {
                this.lastModified = lastModified;
                fireFileChangeEvent(this.listener, this.fileName);
            }
        }
    }

}
