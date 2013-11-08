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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

/**
 * Main Class of the Mail Server
 * 
 * @author jmrunge
 * @version 1.00
 */
public class MailServer {
            
    /**
     * Main method of the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerLog.configLog();
        String port = Props.getInstance().getProperty("server.port");
        ServerSocket server = null;
        try {
            server = new ServerSocket(Integer.parseInt(port));
	    ServerLog.log(Level.INFO, "Arranco el server");
        } catch (IOException e) {
            ServerLog.log(Level.SEVERE, "Error iniciando server en el puerto " + port, e);
            return;
        }
        while(true) {
            try {
                ServerLog.log(Level.INFO, "Esperando cliente");
                Socket client = server.accept();
                ServerLog.log(Level.INFO, "Conectando cliente: " + client.getInetAddress().getHostAddress());
                ServerConnection ct = new ServerConnection(client);
                Thread t = new Thread(ct);
                t.start();
            } catch (IOException e) {
                ServerLog.log(Level.SEVERE, "Error conectando al cliente", e);
            }
        }
    }
}
