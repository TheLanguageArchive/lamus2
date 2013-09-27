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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.util.DateTimeHelper;
import nl.mpi.lamus.workspace.exception.WorkspaceFilesystemException;
import nl.mpi.lamus.workspace.exporting.WorkspaceExportRunner;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceManager manager;
    @Mock private ExecutorService mockExecutorService;
    @Mock private WorkspaceFactory mockWorkspaceFactory;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceDirectoryHandler mockWorkspaceDirectoryHandler;
    @Mock private WorkspaceImportRunner mockWorkspaceImportRunner;
    @Mock private WorkspaceExportRunner mockWorkspaceExportRunner;
    @Mock private Future<Boolean> mockFuture;
    @Mock private DateTimeHelper mockDateTimeHelper;
    
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
        this.manager = new LamusWorkspaceManager(
                mockExecutorService, mockWorkspaceFactory, mockWorkspaceDao,
                mockWorkspaceDirectoryHandler, mockWorkspaceImportRunner, mockWorkspaceExportRunner,
                mockDateTimeHelper);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createWorkspace method, of class LamusWorkspaceManager.
     */
    @Test
    public void createWorkspaceSuccessfully() throws WorkspaceFilesystemException, InterruptedException, ExecutionException, URISyntaxException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        final WorkspaceStatus expectedStatus = WorkspaceStatus.INITIALISING;
        final String expectedMessage = "Workspace initialising";
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace.getWorkspaceID());
            oneOf (mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf (mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf (mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf (mockFuture).get(); will(returnValue(Boolean.TRUE));
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeURI);
        assertNotNull("Returned workspace should not be null when object, database and directory are successfully created.", result);
        assertEquals("Workspace status different from expected", expectedStatus, result.getStatus());
        assertEquals("Workspace message different from expected", expectedMessage, result.getMessage());
    }
    
    /**
     * Test of createWorkspace method, of class LamusWorkspaceManager.
     */
    @Test
    public void creationOfWorkspaceDirectoryFails() throws WorkspaceFilesystemException, URISyntaxException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        final String errorMessage = "Directory for workspace " + newWorkspace.getWorkspaceID() + " could not be created";
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace.getWorkspaceID());
                will(throwException(new WorkspaceFilesystemException(errorMessage, newWorkspace.getWorkspaceID(), null)));
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeURI);
        assertNull("Returned workspace should be null when the directory creation fails.", result);
    }
    
    @Test
    public void deleteWorkspaceSuccessfully() throws IOException {
        
        final int workspaceID = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).deleteWorkspace(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).deleteWorkspaceDirectory(workspaceID);
        }});
        
        boolean result = manager.deleteWorkspace(workspaceID);
        
        assertTrue("Result should be true.", result);
    }
    
    @Test
    public void deleteWorkspaceThrowsException() throws IOException {
        
        final int workspaceID = 1;
        final String exceptionMessage = "some message";
        final Exception expectedException = new IOException(exceptionMessage);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).deleteWorkspace(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).deleteWorkspaceDirectory(workspaceID); will(throwException(expectedException));
        }});
        
        boolean result = manager.deleteWorkspace(workspaceID);
        
        assertFalse("Result should be false.", result);
    }
    
    @Test
    public void openExistingWorkspaceWithRightUser() throws URISyntaxException, MalformedURLException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
            
            //check if the workspace directory exists
            oneOf(mockWorkspaceDirectoryHandler).workspaceDirectoryExists(workspaceToRetrieve); will(returnValue(Boolean.TRUE));
            
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
    public void openExistingWorkspaceWithWrongUser() throws URISyntaxException, MalformedURLException {
        final int workspaceID = 1;
        final String givenUserID = "someUser";
        final String expectedUserID = "someOtherUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, expectedUserID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = manager.openWorkspace(givenUserID, workspaceID);
        
        //TODO Or throw an exception?
        
        assertNull("Returned workspace should be null", result);
    }
    
    @Test
    public void openExistingWorkspaceWithoutDirectory() throws URISyntaxException, MalformedURLException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
            oneOf(mockWorkspaceDirectoryHandler).workspaceDirectoryExists(workspaceToRetrieve); will(returnValue(Boolean.FALSE));
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
    
    @Test
    public void submitWorkspaceSuccessful() throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        final Date startDate = startCalendar.getTime();
        final Date endDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus initialStatus = WorkspaceStatus.INITIALISED;
        final WorkspaceStatus successfullySubmittedStatus = WorkspaceStatus.SUBMITTED;
        final String initialMessage = "workspace is in good shape";
        final String successfullySubmittedMessage = "workspace was successfully submitted";
        final String archiveInfo = "still not sure what this would be";
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final Workspace initialWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                initialStatus, initialMessage, archiveInfo);
        
        final Workspace updatedWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, endDate, startDate, endDate, usedStorageSpace, maxStorageSpace,
                successfullySubmittedStatus, successfullySubmittedMessage, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(initialWorkspace));
            
            //TODO something else?
            
            oneOf(mockWorkspaceExportRunner).setWorkspace(initialWorkspace);
//            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(Boolean.TRUE);
            
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockDateTimeHelper).getCurrentDateTime(); will(returnValue(endDate));
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(updatedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(updatedWorkspace);
        }});
        
        boolean result = manager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void submitWorkspaceUnsuccessful() throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException {
        
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        final Date startDate = startCalendar.getTime();
        final Date endDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus initialStatus = WorkspaceStatus.INITIALISED;
        final WorkspaceStatus unsuccessfullySubmittedStatus = WorkspaceStatus.DATA_MOVED_ERROR;
        final String initialMessage = "workspace is in good shape";
        final String unsuccessfullySubmittedMessage = "there were errors when submitting the workspace";
        final String archiveInfo = "still not sure what this would be";
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final Workspace initialWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                initialStatus, initialMessage, archiveInfo);
        
        final Workspace updatedWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, endDate, startDate, endDate, usedStorageSpace, maxStorageSpace,
                unsuccessfullySubmittedStatus, unsuccessfullySubmittedMessage, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(initialWorkspace));
            
            //TODO something else?
            
            oneOf(mockWorkspaceExportRunner).setWorkspace(initialWorkspace);
//            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(Boolean.TRUE);
            
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockDateTimeHelper).getCurrentDateTime(); will(returnValue(endDate));
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(updatedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(updatedWorkspace);
        }});
        
        boolean result = manager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
        assertFalse("Result should be false", result);

    }
}
