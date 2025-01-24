/* SVN FILE: $Id: FileChangeListener.java 134 2013-07-19 19:32:22Z seth $ */
/**
* One Wire Weather : Weather station daemon for a 1-wire weather station
*
* $Author: seth $
* $Revision: 134 $
* $Date: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $LastChangedBy: seth $
* $LastChangedDate: 2013-07-19 15:32:22 -0400 (Fri, 19 Jul 2013) $
* $URL: https://mustang:8443/svn/projects/OneWireWeather/src/oneWireWeather/FileChangeListener.java $
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

public interface FileChangeListener {
    
    public void fileChanged(String fileName);
    
}
