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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import javax.mail.Message;
import nl.mpi.lamus.ams.AmsServiceBridge;
import nl.mpi.lamus.util.MailHelper;
import nl.mpi.lamus.workspace.exporting.WorkspaceMailer;
import nl.mpi.lamus.workspace.model.Workspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceMailerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock AmsServiceBridge mockAmsBridge;
    @Mock MailHelper mockMailHelper;
    
    @Mock Workspace mockWorkspace;
    @Mock Message mockMessage;
    
    private WorkspaceMailer workspaceMailer;
    
    
    public LamusWorkspaceMailerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        workspaceMailer = new LamusWorkspaceMailer(mockAmsBridge, mockMailHelper);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void sendWorkspaceFinalMessageSuccess() throws MalformedURLException {
        
        final int workspaceID = 10;
        final String userID = "someUser";
        final String emailAddress = "someUser@test.nl";
        final Date startDate = Calendar.getInstance().getTime();
        final URL topNodeArchiveURL = new URL("http://some/url/and/stuff.html");
        
        final String subject = "Workspace - Success";
        final String text = "Your workspace (ID: " + workspaceID + "; creation date: " + startDate.toString() + ") was successfully submitted.\n\n"
                + "The data was moved into the archive at '" + topNodeArchiveURL + "' and the database was updated.";
        final boolean addBcc = Boolean.FALSE;

        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getUserID(); will(returnValue(userID));
            oneOf(mockAmsBridge).getMailAddress(userID); will(returnValue(emailAddress));
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspace).getStartDate(); will(returnValue(startDate));
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(topNodeArchiveURL));
            oneOf(mockMailHelper).getMailMessage(emailAddress, subject, text, addBcc); will(returnValue(mockMessage));
            oneOf(mockMailHelper).sendMailMessage(mockMessage);
        }});
        
        workspaceMailer.sendWorkspaceFinalMessage(mockWorkspace, Boolean.TRUE, Boolean.TRUE);
    }
    
    @Test
    public void sendWorkspaceFinalMessageCrawlerFailure() {
        
        final int workspaceID = 10;
        final String userID = "someUser";
        final String emailAddress = "someUser@test.nl";
        final Date startDate = Calendar.getInstance().getTime();
        
        final String subject = "Workspace - Failure";
        final String text = "Your workspace (ID: " + workspaceID + "; creation date: " + startDate.toString() + ") was submitted.\n\n"
                + "The data was moved into the archive but there were problems updating the database.\n"
                + "Please contact the corpus management team.";
        final boolean addBcc = Boolean.TRUE;

        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getUserID(); will(returnValue(userID));
            oneOf(mockAmsBridge).getMailAddress(userID); will(returnValue(emailAddress));
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspace).getStartDate(); will(returnValue(startDate));
            oneOf(mockMailHelper).getMailMessage(emailAddress, subject, text, addBcc); will(returnValue(mockMessage));
            oneOf(mockMailHelper).sendMailMessage(mockMessage);
        }});
        
        workspaceMailer.sendWorkspaceFinalMessage(mockWorkspace, Boolean.FALSE, Boolean.TRUE);
    }
    
    @Test
    public void sendWorkspaceFinalMessageVersioningFailure() {
        
        final int workspaceID = 10;
        final String userID = "someUser";
        final String emailAddress = "someUser@test.nl";
        final Date startDate = Calendar.getInstance().getTime();
        
        final String subject = "Workspace - Failure";
        final String text = "Your workspace (ID: " + workspaceID + "; creation date: " + startDate.toString() + ") was successfully submitted.\n\n"
                    + "The data was moved into the archive and the database was updated, but there were problems with versioning in the database.\n"
                    + "Please contact the corpus management team.";
        final boolean addBcc = Boolean.TRUE;

        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getUserID(); will(returnValue(userID));
            oneOf(mockAmsBridge).getMailAddress(userID); will(returnValue(emailAddress));
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockWorkspace).getStartDate(); will(returnValue(startDate));
            oneOf(mockMailHelper).getMailMessage(emailAddress, subject, text, addBcc); will(returnValue(mockMessage));
            oneOf(mockMailHelper).sendMailMessage(mockMessage);
        }});
        
        workspaceMailer.sendWorkspaceFinalMessage(mockWorkspace, Boolean.TRUE, Boolean.FALSE);
    }
}