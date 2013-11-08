/*
    Copyright 2012 Juan Martín Runge
    
    jmrunge@gmail.com
    http://www.zirsi.com.ar
    
    This file is part of MailServer.

    MailServer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MailServer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MailServer.  If not, see <http://www.gnu.org/licenses/>.
*/
package ar.com.zir.mail.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Singleton Class that reads the mail server properties
 * 
 * @author jmrunge
 * @version 1.00
 */
public class Props {
    private static Props instance;
    private static Properties props = null;

    /**
     * Singleton constructor
     */
    private Props(){};

    /**
     * Method that reads the properties file and returns the singleton instance of this class
     * 
     * @return the singleton instance of the class
     */
    public static Props getInstance() {
        if (instance == null) {
            instance = new Props();
            props = new Properties();
            InputStream is = null;
            try {
                is = new FileInputStream("./conf/server.properties");
            } catch (FileNotFoundException e) {
                try {
                    is = new FileInputStream("../conf/server.properties");
                } catch (FileNotFoundException ex) {
                    try {
                        is = new FileInputStream("/conf/server.properties");
                    } catch (FileNotFoundException ex1) {
                        ServerLog.log(Level.SEVERE, "Error leyendo las propiedades", ex1);
                    }
                }
            }
            try {
                props.load(is);
            } catch (IOException ex) {
                ServerLog.log(Level.SEVERE, "Error leyendo las propiedades", ex);
            }
        }
        return instance;
    }

    /**
     * Method that returns the property value for this key or null if it does not exist
     * 
     * @param key the key
     * @return the value of the property for this key or null if it does not exist
     */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     * Method that returns all the properties whose keys start with the key suplied
     * and end with ".1", ".2", and so on.  Numbers should be sequential, the first hole 
     * found will make this method to stop reading properties for this key
     * 
     * @param key the key
     * @return a Map<Integer, String> of properties, where the key will be the number 
     * (1 if property ended with ".1") and the value of the property
     */
    public Map<Integer, String> getProperties(String key) {
        Map<Integer, String> properties = new HashMap<Integer, String>();
        boolean keepReading = true;
        int nr = 1;
        while (keepReading) {
            String prop = getProperty(key + "." + nr);
            if (prop != null) {
                properties.put(nr, prop);
                nr++;
            } else {
                keepReading = false;
            }
        }
        return properties;
    }
}
