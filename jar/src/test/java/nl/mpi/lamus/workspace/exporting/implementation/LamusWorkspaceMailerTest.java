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
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.util.MailHelper;
import nl.mpi.lamus.workspace.exporting.WorkspaceMailer;
import nl.mpi.lamus.workspace.model.Workspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceMailerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock AmsBridge mockAmsBridge;
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
    public void sendWorkspaceFinalMessageSuccess() {
        
        final int workspaceID = 10;
        final String userID = "someUser";
        final String emailAddress = "someUser@test.nl";
        
        final String subject = "Workspace - Success";
        final String text = "Workspace " + workspaceID + " was successfully submitted.\n"
                + "Data was moved into the archive and the database was updated.";

        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getUserID(); will(returnValue(userID));
            oneOf(mockAmsBridge).getMailAddress(userID); will(returnValue(emailAddress));
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockMailHelper).getMailMessage(emailAddress, subject, text); will(returnValue(mockMessage));
            oneOf(mockMailHelper).sendMailMessage(mockMessage);
        }});
        
        workspaceMailer.sendWorkspaceFinalMessage(mockWorkspace, Boolean.TRUE);
    }
    
    @Test
    public void sendWorkspaceFinalMessageFailure() {
        
        final int workspaceID = 10;
        final String userID = "someUser";
        final String emailAddress = "someUser@test.nl";
        
        final String subject = "Workspace - Failure";
        final String text = "Workspace " + workspaceID + " was submitted.\n"
                + "Data was moved into the archive but there were problems updating the database.\n"
                + "Please contact the corpus management team.";

        context.checking(new Expectations() {{
            
            oneOf(mockWorkspace).getUserID(); will(returnValue(userID));
            oneOf(mockAmsBridge).getMailAddress(userID); will(returnValue(emailAddress));
            
            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockMailHelper).getMailMessage(emailAddress, subject, text); will(returnValue(mockMessage));
            oneOf(mockMailHelper).sendMailMessage(mockMessage);
        }});
        
        workspaceMailer.sendWorkspaceFinalMessage(mockWorkspace, Boolean.FALSE);
    }
}