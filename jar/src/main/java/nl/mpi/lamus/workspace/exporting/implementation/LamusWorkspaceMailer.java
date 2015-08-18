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
package nl.mpi.lamus.workspace.exporting.implementation;

import javax.mail.Message;
import nl.mpi.lamus.ams.AmsServiceBridge;
import nl.mpi.lamus.util.MailHelper;
import nl.mpi.lamus.workspace.exporting.WorkspaceMailer;
import nl.mpi.lamus.workspace.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceMailer
 * @author guisil
 */
@Component
public class LamusWorkspaceMailer implements WorkspaceMailer {
    
    private final AmsServiceBridge amsBridge;
    private final MailHelper mailHelper;
    
    @Autowired
    public LamusWorkspaceMailer(AmsServiceBridge amsBridge, MailHelper mailHelper) {
        this.amsBridge = amsBridge;
        this.mailHelper = mailHelper;
    }

    /**
     * @see WorkspaceMailer#sendWorkspaceFinalMessage(nl.mpi.lamus.workspace.model.Workspace, boolean, boolean)
     */
    @Override
    public void sendWorkspaceFinalMessage(Workspace workspace, boolean crawlerWasSuccessful, boolean versioningWasSuccessful) {
        
        String toAddress = amsBridge.getMailAddress(workspace.getUserID());
        
        String subject;
        String text;
        boolean addBcc = false;
        
        if(!crawlerWasSuccessful) {
            subject = "Workspace - Failure";
            text = "Workspace " + workspace.getWorkspaceID() + " was submitted.\n"
                    + "Data was moved into the archive but there were problems updating the database.\n"
                    + "Please contact the corpus management team.";
            addBcc = true;
        } else if(!versioningWasSuccessful) {
            subject = "Workspace - Failure";
            text = "Workspace " + workspace.getWorkspaceID() + " was successfully submitted.\n"
                    + "Data was moved into the archive and the database was updated, but there were problems with versioning in the database.\n"
                    + "Please contact the corpus management team.";
            addBcc = true;
        } else {
            subject = "Workspace - Success";
            text = "Workspace " + workspace.getWorkspaceID() + " was successfully submitted.\n"
                    + "Data was moved into the archive and the database was updated.";
        }
        
        Message mailMessage = mailHelper.getMailMessage(toAddress, subject, text, addBcc);
        
        mailHelper.sendMailMessage(mailMessage);
    }
    
}
