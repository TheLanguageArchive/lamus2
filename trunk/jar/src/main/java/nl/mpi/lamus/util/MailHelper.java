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
package nl.mpi.lamus.util;

import javax.mail.Message;

/**
 * Helper class for the handling of email messages.
 * 
 * @author guisil
 */
public interface MailHelper {
    
    /**
     * Creates an email message with the given parameters.
     * 
     * @param toAddress Email address to which the message should be sent
     * @param subject Subject of the email message
     * @param text Content of the email message
     * @param addBcc true if bcc to the corpus manager should be added - in case some problem happened
     * @return Created email message
     */
    public Message getMailMessage(String toAddress, String subject, String text, boolean addBcc);
    
    /**
     * Sends the given email message.
     * 
     * @param message Message to be sent
     */
    public void sendMailMessage(Message message);
}
