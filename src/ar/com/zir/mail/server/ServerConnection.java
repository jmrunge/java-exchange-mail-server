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

import ar.com.zir.mail.api.Mail;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;

/**
 * This class is used to handle the communication with the mail client
 * 
 * @author jmrunge
 * @version 1.00
 */
public class ServerConnection implements Runnable {
    private Socket client = null;
    
    /**
     * Constructor that receives the socket where the mail client is writing to
     * @param client the socket where the mail client is writing to
     */
    public ServerConnection(Socket client) {
        this.client = client;
    }

    /**
     * Method that receives the mail object and send it to the utility class for sending.
     * If no error was thrown, it sends a Boolean.TRUE response to the mail client.
     * Else, it sends a Boolean.FALSE to the mail client.
     * @see ar.com.zir.mail.api.Mail
     * @see ar.com.zir.mail.server.MailUtils
     */
    @Override
    public void run() {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(client.getInputStream());
            Mail mail = (Mail) ois.readObject();
            ServerLog.log(Level.INFO, "Recibido mail:\n" + mail.toString());
            MailUtils.sendMail(mail);
            ServerLog.log(Level.INFO, "Mail enviado exitosamente" + mail.toString());
            oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(Boolean.TRUE);
        } catch (Exception ex) {
            ServerLog.log(Level.SEVERE, "Error leyendo datos del cliente", ex);
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                oos.writeObject(Boolean.FALSE);
            } catch (IOException ex1) {
                ServerLog.log(Level.SEVERE, "Error enviando respuesta a cliente", ex1);
            }
        } finally {
            try {
                ois.close();
                oos.close();
                client.close();
            } catch (Exception ex) {
                ServerLog.log(Level.SEVERE, "Error cerrando recursos", ex);
            }
        }
    }
    
}
