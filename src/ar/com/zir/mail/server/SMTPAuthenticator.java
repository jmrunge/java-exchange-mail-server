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

import java.util.Properties;
import javax.mail.PasswordAuthentication;

/**
 * Class that handles the authentication with the mail server itself
 * 
 * @author jmrunge
 * @version 1.00
 */
public class SMTPAuthenticator extends javax.mail.Authenticator {
    private Properties props;

    /**
     * Constructor that receives a properties object to extract the relevant ones:
     * mail.user and mail.password
     * 
     * @param mailProps Properties object with mail.user and mail.password properties
     */
    public SMTPAuthenticator(Properties mailProps) {
        props = mailProps;
    }

    /**
     * Method that returns the PasswordAuthentication object built from the user a password
     * obtained from the properties object
     * 
     * @return the PasswordAuthentication object built from the user and password received
     * in the properties object
     */
    @Override
    public PasswordAuthentication getPasswordAuthentication()
    {
        String username = props.getProperty("mail.user");
        String password = props.getProperty("mail.password");
        return new PasswordAuthentication(username, password);
    }
}
