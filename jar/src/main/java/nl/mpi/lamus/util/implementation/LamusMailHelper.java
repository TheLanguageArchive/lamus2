/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.util.implementation;

import java.util.Calendar;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import nl.mpi.lamus.util.MailHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @see MailHelper
 * @author guisil
 */
@Component
public class LamusMailHelper implements MailHelper {

    private String mailServer;
    private String mailFromAddress;
    private String mailBccAddress;
    
    @Autowired
    public LamusMailHelper(
            @Qualifier("mailServer") String mailServer,
            @Qualifier("mailFromAddress") String mailFromAddress,
            @Qualifier("mailBccAddress") String mailBccAddress) {
        this.mailServer = mailServer;
        this.mailFromAddress = mailFromAddress;
        this.mailBccAddress = mailBccAddress;
    }
    
    /**
     * @see MailHelper#getMailMessage(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public Message getMailMessage(String toAddress, String subject, String text, boolean addBcc) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", mailServer);
        properties.put("mail.from", mailFromAddress);
        
        Session session = Session.getDefaultInstance(properties);
        
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(mailFromAddress));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(mailBccAddress));
            message.setSubject(subject);
            message.setSentDate(Calendar.getInstance().getTime());
            message.setText(text);
        } catch (MessagingException ex) {
            throw new UnsupportedOperationException("exception not handled yet");
        }
        
        return message;
    }

    /**
     * @see MailHelper#sendMailMessage(javax.mail.Message)
     */
    @Override
    public void sendMailMessage(Message message) {
        
        try {
            Transport.send(message);
        } catch (MessagingException ex) {
            throw new UnsupportedOperationException("exception not handled yet");
        }
    }
}
