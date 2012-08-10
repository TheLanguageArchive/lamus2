/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.management.implementation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.springframework.core.task.TaskExecutor;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceManager manager;
    @Mock private TaskExecutor mockExecutor;
    @Mock private WorkspaceFactory mockWorkspaceFactory;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceDirectoryHandler mockWorkspaceDirectoryHandler;
    @Mock private WorkspaceImportRunner mockWorkspaceImportRunner;
    
    public LamusWorkspaceManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        this.manager = new LamusWorkspaceManager(mockExecutor, mockWorkspaceFactory, mockWorkspaceDao, mockWorkspaceDirectoryHandler, mockWorkspaceImportRunner);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createWorkspace method, of class LamusWorkspaceManager.
     */
    @Test
    public void createWorkspaceSuccessfully() throws FailedToCreateWorkspaceDirectoryException {
        final int archiveNodeID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeID); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace);
            oneOf (mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf (mockWorkspaceImportRunner).setTopNodeArchiveID(archiveNodeID);
            oneOf (mockExecutor).execute(mockWorkspaceImportRunner);
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeID);
        assertNotNull("Returned workspace should not be null when object, database and directory are successfully created.", result);
    }
    
    /**
     * Test of createWorkspace method, of class LamusWorkspaceManager.
     */
    @Test
    public void creationOfWorkspaceDirectoryFails() throws FailedToCreateWorkspaceDirectoryException {
        final int archiveNodeID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        final String errorMessage = "Directory for workspace " + newWorkspace.getWorkspaceID() + " could not be created";
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeID); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace); will(throwException(new FailedToCreateWorkspaceDirectoryException(errorMessage, newWorkspace, null)));
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeID);
        assertNull("Returned workspace should be null when the directory creation fails.", result);
    }
    
    @Test
    public void openExistingWorkspaceWithRightUser() throws MalformedURLException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 0;
        final URL topNodeArchiveURL = new URL("http://some/url/node.imdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
            
            //check if the workspace directory exists
            oneOf(mockWorkspaceDirectoryHandler).workspaceDirectoryExists(workspaceToRetrieve); will(returnValue(true));
            
            oneOf(mockWorkspaceDao).updateWorkspaceSessionDates(workspaceToRetrieve);
            //update as well status?
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = manager.openWorkspace(userID, workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
        
        assertFalse("", startDate.equals(workspaceToRetrieve.getSessionStartDate()));
        assertNotNull(workspaceToRetrieve.getSessionEndDate());
    }
    
    @Test
    public void openExistingWorkspaceWithWrongUser() throws MalformedURLException {
        final int workspaceID = 1;
        final String givenUserID = "someUser";
        final String expectedUserID = "someOtherUser";
        final int topNodeID = 0;
        final URL topNodeArchiveURL = new URL("http://some/url/node.imdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, expectedUserID, topNodeID, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = manager.openWorkspace(givenUserID, workspaceID);
        
        //TODO Or throw an exception?
        
        assertNull("Returned workspace should be null", result);
    }
    
 @Test
    public void openExistingWorkspaceWithoutDirectory() throws MalformedURLException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 0;
        final URL topNodeArchiveURL = new URL("http://some/url/node.imdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
            oneOf(mockWorkspaceDirectoryHandler).workspaceDirectoryExists(workspaceToRetrieve); will(returnValue(false));
        }});
        
        Workspace result = manager.openWorkspace(userID, workspaceID);
        
        //TODO Or throw an exception?
        
        assertNull("Returned workspace should be null", result);
    }
    
    @Test
    public void openNonExistingWorkspace() throws MalformedURLException {
        final int workspaceID = 1;
        final String userID = "someUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(null));
        }});
        
        Workspace result = manager.openWorkspace(userID, workspaceID);
        assertNull("Returned workspace should be null", result);
    }
}
