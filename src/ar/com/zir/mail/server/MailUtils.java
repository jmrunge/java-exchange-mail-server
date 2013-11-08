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

import ar.com.zir.mail.api.Attachment;
import ar.com.zir.mail.api.Mail;
import com.sun.mail.smtp.SMTPTransport;
import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Utility class for sending mails
 * 
 * @author jmrunge
 * @version 1.00
 */
public class MailUtils {
    
    /**
     * This method will try to send the mail received via the Exchange Server. 
     * If it fails, will log a message and try to send the mail via SMTP
     * 
     * @param mail the mail object to send
     * @throws NoSuchProviderException inherited from Session.getTransport()
     * @throws MessagingException inherited from Transport.connect() and Transport.close()
     * @throws FileNotFoundException inherited from getMessage()
     * @throws IOException inherited from BufferedOutputStream.write(byte[], int, int)
     * @see javax.mail.Transport
     * @see javax.mail.Session
     * @see ar.com.zir.mail.api.Mail
     */
    public static void sendMail(Mail mail) throws NoSuchProviderException, MessagingException, FileNotFoundException, IOException {
        try {
            sendExchangeMail(mail);
        } catch (Exception ex) {
            ServerLog.log(Level.WARNING, "Error enviando via Exchange, probando via SMTP", ex);
            sendSMTPMail(mail);
        }
    }

    /**
     * This method will try to send tha mail received via SMTP
     * 
     * @param mail the mail object to send
     * @throws MessagingException inherited from Transport.connect() and Transport.close()
     * @throws FileNotFoundException inherited from getMessage()
     * @throws IOException inherited from BufferedOutputStream.write(byte[], int, int)
     * @see javax.mail.Transport
     * @see ar.com.zir.mail.api.Mail
     */
    private static void sendSMTPMail(Mail mail) throws MessagingException, FileNotFoundException, IOException {

        Properties properties = new Properties();
        properties.put("mail.host", Props.getInstance().getProperty("mail.host"));
        properties.put("mail.port", Props.getInstance().getProperty("mail.port"));
        properties.put("mail.smtp.auth", "true");

        final String userName = Props.getInstance().getProperty("mail.domain") + "\\" 
                + Props.getInstance().getProperty("mail.user");
        final String password = Props.getInstance().getProperty("mail.pwd");

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };

        SMTPTransport transport = null;

        try {
            Session session = Session.getDefaultInstance(properties, authenticator);

            MimeMessage message = getMessage(session, mail);

            transport = (SMTPTransport)session.getTransport("smtp");
            transport.connect(Props.getInstance().getProperty("mail.host"), userName, password);
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            if (transport != null) {
                transport.close();
            }
        }

    }
    
    /**
     * This method will try to send the mail received via the Exchange Server. 
     * 
     * @param mail the mail object to send
     * @throws NoSuchProviderException inherited from Session.getTransport()
     * @throws MessagingException inherited from Transport.connect() and Transport.close()
     * @throws FileNotFoundException inherited from getMessage()
     * @throws IOException inherited from BufferedOutputStream.write(byte[], int, int)
     * @see javax.mail.Transport
     * @see javax.mail.Session
     * @see ar.com.zir.mail.api.Mail
     */
    private static void sendExchangeMail(Mail mail) throws NoSuchProviderException, MessagingException, FileNotFoundException, IOException {
        
        Properties mailProps = new Properties();
        mailProps.put("mail.user", Props.getInstance().getProperty("mail.user"));
        mailProps.put("mail.password", Props.getInstance().getProperty("mail.pwd"));
        mailProps.put("mail.transport.protocol", "smtp");
        mailProps.put("mail.host", Props.getInstance().getProperty("mail.host"));
        mailProps.put("mail.smtp.auth", "true");
        
        Transport transport = null;

        try {
            Authenticator auth = new SMTPAuthenticator(mailProps);
            Session mailSession = Session.getDefaultInstance(mailProps, auth);
//            mailSession.setDebug(true);
            transport = mailSession.getTransport();
            
            MimeMessage message = getMessage(mailSession, mail);
            
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
    
    /**
     * Method that builds the MimeMessage object to be sent
     * 
     * @param session the mail session
     * @param mail the mail object
     * @return the MimeMessage built from the Mail object received
     * @throws MessagingException inherited from Transport.connect() and Transport.close()
     * @throws FileNotFoundException inherited from getMessage()
     * @throws IOException inherited from BufferedOutputStream.write(byte[], int, int)
     * @see javax.mail.Session
     * @see ar.com.zir.mail.api.Mail
     * @see javax.mail.internet.MimeMessage
     */
    private static MimeMessage getMessage(Session session, Mail mail) throws MessagingException, FileNotFoundException, IOException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mail.getSender()));
        message.setSubject(mail.getSubject());

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(mail.getMessage(), "text/html");

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(textPart);

        if (mail.getAttachments() != null && !mail.getAttachments().isEmpty()) {
            for (Attachment a : mail.getAttachments()) {
                if (a.getMime() == null || a.getMime().trim().length() == 0) {
                    File f = new File(a.getName());
                    FileOutputStream fos = new FileOutputStream(f);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bos.write(a.getData(), 0, a.getData().length);
                    attachFile(mp, f);
                } else {
                    MimeBodyPart attachBodyPart = new MimeBodyPart();  
                    attachBodyPart.setFileName(a.getName());  
                    ByteArrayDataSource src = new ByteArrayDataSource(a.getData(), a.getMime());  
                    attachBodyPart.setDataHandler(new DataHandler(src));  
                    mp.addBodyPart(attachBodyPart);  
                }
            }
        }

        message.setContent(mp);
        for (String recipient : mail.getRecipients()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        }
        return message;
    }
    
    /**
     * Method that attachs the file specified to the message
     * 
     * @param mp the Multipart where the file should be attached
     * @param fileToAttach the file to attach
     * @throws MessagingException inherited from Multipart.addBodyPart(MimeBodyPart)
     * @see javax.mail.Multipart
     * @see javax.mail.internet.MimeBodyPart
     * @see javax.activation.FileDataSource
     * @see javax.activation.DataHandler
     */
    private static void attachFile(Multipart mp, File fileToAttach) throws MessagingException {
        MimeBodyPart attachFilePart = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(fileToAttach);
        attachFilePart.setDataHandler(new DataHandler(fds));
        attachFilePart.setFileName(fds.getName());
        mp.addBodyPart(attachFilePart);
    }
    
}
