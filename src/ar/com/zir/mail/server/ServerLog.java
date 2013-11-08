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

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** 
 * This class is used to log the mail server events to a file
 *
 * @author jmrunge
 * @version 1.00
 */
public class ServerLog {
    private static final Logger logger = Logger.getLogger("MailServer");
    private static final String LOG_FILE_FORMATED = "./log/MailServer.%g.log";

    /**
     * Method that performs the log configuration, should be invoked prior to
     * invoke any other method.
     */
    public static void configLog() {
        try {
            FileHandler fh = new FileHandler(LOG_FILE_FORMATED, 1048576, 15, true);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method for logging messages with the specified level
     * @param level level to use for logging this message
     * @param msg message to log
     */
    public static void log(Level level, String msg) {
        logger.log(level, msg);
    }

    /**
     * Method for logging exceptions with the sepecified level
     * and the specified additional message
     * @param level level to use for logging this message
     * @param msg additional message to log
     * @param e exception to log
     */
    public static void log(Level level, String msg, Exception e) {
        logger.log(level, msg, e);
    }

}
