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
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.util.CalendarHelper;
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
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock private CalendarHelper mockCalendarHelper;
    
    @Mock private Future<Boolean> mockFuture;
    @Mock private Workspace mockWorkspace;
    @Mock private Calendar mockCalendar;

    
    private final int numberOfDaysOfInactivityAllowedSinceLastSession = 60;
    
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
                mockWorkspaceDirectoryHandler, mockWorkspaceImportRunner, mockWorkspaceExportRunner, mockCalendarHelper);
        
        ReflectionTestUtils.setField(manager, "numberOfDaysOfInactivityAllowedSinceLastSession", numberOfDaysOfInactivityAllowedSinceLastSession);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void createWorkspaceSuccessfully()
            throws URISyntaxException, IOException, InterruptedException, ExecutionException, WorkspaceImportException, WorkspaceNotFoundException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        newWorkspace.setWorkspaceID(workspaceID);
        
        final WorkspaceStatus expectedStatus = WorkspaceStatus.INITIALISING;
        final String expectedMessage = "Workspace initialising";
        
        final int topNodeID = 100;
        final Workspace expectedWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        expectedWorkspace.setWorkspaceID(workspaceID);
        expectedWorkspace.setTopNodeID(topNodeID);
        expectedWorkspace.setStatus(expectedStatus);
        expectedWorkspace.setMessage(expectedMessage);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf(mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf(mockWorkspaceDirectoryHandler).createWorkspaceDirectory(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).createUploadDirectoryForWorkspace(workspaceID);
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(expectedWorkspace));
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeURI);
        assertNotNull("Returned workspace should not be null when object, database and directory are successfully created.", result);
        
//        assertEquals("Workspace status different from expected", expectedStatus, result.getStatus());
//        assertEquals("Workspace message different from expected", expectedMessage, result.getMessage());
    }
    
    /**
     * Test of createWorkspace method, of class LamusWorkspaceManager.
     */
    @Test
    public void creationOfWorkspaceDirectoryFails()
            throws URISyntaxException, IOException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        final String errorMessage = "Directory for workspace " + newWorkspace.getWorkspaceID() + " could not be created";
        final IOException expectedException = new IOException(errorMessage);
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace.getWorkspaceID());
                will(throwException(expectedException));
        }});
        
        try {
            manager.createWorkspace(userID, archiveNodeURI);
        } catch(WorkspaceImportException ex) {
            String expectedMainErrorMessage = "Error creating workspace in node " + archiveNodeURI;
            assertEquals("Message different from expected", expectedMainErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", newWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
        
    }
    
    @Test
    public void createWorkspaceThreadInterrupted()
            throws URISyntaxException, IOException, InterruptedException, ExecutionException, WorkspaceImportException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        newWorkspace.setWorkspaceID(workspaceID);
        
        final WorkspaceStatus expectedStatus = WorkspaceStatus.INITIALISING;
        final String expectedMessage = "Workspace initialising";
        
        final int topNodeID = 100;
        final Workspace expectedWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        expectedWorkspace.setWorkspaceID(workspaceID);
        expectedWorkspace.setTopNodeID(topNodeID);
        expectedWorkspace.setStatus(expectedStatus);
        expectedWorkspace.setMessage(expectedMessage);
        
        final InterruptedException expectedException = new InterruptedException("some exception message");
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf(mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf(mockWorkspaceDirectoryHandler).createWorkspaceDirectory(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).createUploadDirectoryForWorkspace(workspaceID);
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            
        }});
        
        try {
            manager.createWorkspace(userID, archiveNodeURI);
        } catch(WorkspaceImportException ex) {
            String expectedMainErrorMessage = "Interruption in thread while creating workspace in node " + archiveNodeURI;
            assertEquals("Message different from expected", expectedMainErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", newWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void createWorkspaceExecutionException()
            throws URISyntaxException, IOException, InterruptedException, ExecutionException, WorkspaceImportException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        newWorkspace.setWorkspaceID(workspaceID);
        
        final WorkspaceStatus expectedStatus = WorkspaceStatus.INITIALISING;
        final String expectedMessage = "Workspace initialising";
        
        final int topNodeID = 100;
        final Workspace expectedWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        expectedWorkspace.setWorkspaceID(workspaceID);
        expectedWorkspace.setTopNodeID(topNodeID);
        expectedWorkspace.setStatus(expectedStatus);
        expectedWorkspace.setMessage(expectedMessage);
        
        final ExecutionException expectedException = new ExecutionException("some exception message", null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf(mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf(mockWorkspaceDirectoryHandler).createWorkspaceDirectory(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).createUploadDirectoryForWorkspace(workspaceID);
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            
        }});
        
        try {
            manager.createWorkspace(userID, archiveNodeURI);
        } catch(WorkspaceImportException ex) {
            String expectedMainErrorMessage = "Problem with thread execution while creating workspace in node " + archiveNodeURI;
            assertEquals("Message different from expected", expectedMainErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", newWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void createWorkspaceFails()
            throws URISyntaxException, IOException, InterruptedException, ExecutionException, WorkspaceImportException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        newWorkspace.setWorkspaceID(workspaceID);
        
        final WorkspaceStatus expectedStatus = WorkspaceStatus.INITIALISING;
        final String expectedMessage = "Workspace initialising";
        
        final int topNodeID = 100;
        final Workspace expectedWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        expectedWorkspace.setWorkspaceID(workspaceID);
        expectedWorkspace.setTopNodeID(topNodeID);
        expectedWorkspace.setStatus(expectedStatus);
        expectedWorkspace.setMessage(expectedMessage);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf(mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf(mockWorkspaceDirectoryHandler).createWorkspaceDirectory(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).createUploadDirectoryForWorkspace(workspaceID);
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.FALSE));
            
        }});
        
        try {
            manager.createWorkspace(userID, archiveNodeURI);
        } catch(WorkspaceImportException ex) {
            String expectedMainErrorMessage = "Workspace creation failed in node " + archiveNodeURI;
            assertEquals("Message different from expected", expectedMainErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", newWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void createWorkspaceThrowsWorkspaceNotFoundException()
            throws URISyntaxException, IOException, InterruptedException, ExecutionException, WorkspaceImportException, WorkspaceNotFoundException {
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final int workspaceID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        newWorkspace.setWorkspaceID(workspaceID);
        
        final WorkspaceStatus expectedStatus = WorkspaceStatus.INITIALISING;
        final String expectedMessage = "Workspace initialising";
        
        final int topNodeID = 100;
        final Workspace expectedWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        expectedWorkspace.setWorkspaceID(workspaceID);
        expectedWorkspace.setTopNodeID(topNodeID);
        expectedWorkspace.setStatus(expectedStatus);
        expectedWorkspace.setMessage(expectedMessage);
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf(mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf(mockWorkspaceDirectoryHandler).createWorkspaceDirectory(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).createUploadDirectoryForWorkspace(workspaceID);
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            manager.createWorkspace(userID, archiveNodeURI);
        } catch(WorkspaceImportException ex) {
            assertEquals("Message different from expected", expectedException.getMessage(), ex.getMessage());
            assertEquals("Workspace ID different from expected", newWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
        
    }
    
    @Test
    public void deleteWorkspaceSuccessfully() throws IOException {
        
        final int workspaceID = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).deleteWorkspace(workspaceID);
            oneOf(mockWorkspaceDirectoryHandler).deleteWorkspaceDirectory(workspaceID);
        }});
        
        manager.deleteWorkspace(workspaceID);
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
        
        try {
            manager.deleteWorkspace(workspaceID);
            fail("should have thrown an exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void openExistingWorkspace() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, IOException {
        
        final int workspaceID = 1;
        
        final Calendar now = Calendar.getInstance();
        final Date firstDate = new Date(now.getTime().getTime());
        final Calendar nowClone = (Calendar) now.clone();
        nowClone.add(Calendar.DATE, numberOfDaysOfInactivityAllowedSinceLastSession);
        final Date secondDate = new Date(nowClone.getTime().getTime());
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            //check if the workspace directory exists
            oneOf(mockWorkspaceDirectoryHandler).workspaceDirectoryExists(mockWorkspace); will(returnValue(Boolean.TRUE));
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(now));
            oneOf(mockWorkspace).setSessionStartDate(firstDate);
            oneOf(mockWorkspace).setSessionEndDate(secondDate);
            
            oneOf(mockWorkspaceDao).updateWorkspaceSessionDates(mockWorkspace);
            //update as well status?

        }});
        
        Workspace result = manager.openWorkspace(workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, mockWorkspace);
    }
    
    @Test
    public void openNonExistingWorkspace() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, IOException {
        final int workspaceID = 1;
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
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            manager.openWorkspace(workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void openExistingWorkspaceWithoutDirectory() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException {
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
        
        final String expectedErrorMessage = "Directory for workpace " + workspaceID + " does not exist";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
            oneOf(mockWorkspaceDirectoryHandler).workspaceDirectoryExists(workspaceToRetrieve); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            manager.openWorkspace(workspaceID);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void submitWorkspaceSuccessful() throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceExportException {

        final int workspaceID = 1;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        
        final WorkspaceStatus submittedStatus = WorkspaceStatus.SUBMITTED;
        final String submittedMessage = "workspace was submitted";
        
        final WorkspaceStatus successfullySubmittedStatus = WorkspaceStatus.DATA_MOVED_SUCCESS;
        final String successfullySubmittedMessage = "data was successfully move to the archive";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = endCalendar.getTime();
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            
            oneOf(mockWorkspace).setStatus(submittedStatus);
            oneOf(mockWorkspace).setMessage(submittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
            
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
//            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(Boolean.TRUE);
            
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            
            oneOf(mockWorkspace).setSessionEndDate(endDate);
            oneOf(mockWorkspace).setEndDate(endDate);
            oneOf(mockWorkspace).setStatus(successfullySubmittedStatus);
            oneOf(mockWorkspace).setMessage(successfullySubmittedMessage);
            oneOf(mockWorkspaceDao).cleanWorkspaceNodesAndLinks(mockWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(mockWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
        }});
        
        
        manager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
    }
    
    @Test
    public void submitWorkspaceThrowsWorkspaceNotFoundException()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException, WorkspaceExportException {
        
        final int workspaceID = 1;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            manager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
            fail("should have thrown exception");
        } catch (WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceThreadInterrupted() throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceExportException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        final Date startDate = startCalendar.getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus initialStatus = WorkspaceStatus.INITIALISED;
        final WorkspaceStatus intermediateStatus = WorkspaceStatus.SUBMITTED;
        final WorkspaceStatus errorSubmittingStatus = WorkspaceStatus.DATA_MOVED_ERROR;
        final String initialMessage = "workspace is in good shape";
        final String intermediateMessage = "workspace was submitted";
        final String errorSubmittingMessage = "there were errors when submitting the workspace";
        final String archiveInfo = "still not sure what this would be";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = new Date(endCalendar.getTime().getTime());
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final Workspace initialWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                initialStatus, initialMessage, archiveInfo);
        
        final Workspace intermediateWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                intermediateStatus, intermediateMessage, archiveInfo);
        
        final Workspace updatedWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, endDate, startDate, endDate, usedStorageSpace, maxStorageSpace,
                errorSubmittingStatus, errorSubmittingMessage, archiveInfo);
        
        final String expectedErrorMessage = "Interruption in thread while submitting workspace " + workspaceID;
        final InterruptedException expectedException = new InterruptedException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(initialWorkspace));
            
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(intermediateWorkspace);
            
            oneOf(mockWorkspaceExportRunner).setWorkspace(initialWorkspace);
//            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(Boolean.TRUE);
            
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(updatedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(updatedWorkspace);
        }});
        
        try {
            manager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void submitWorkspaceExecutionException() throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceExportException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        final Date startDate = startCalendar.getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus initialStatus = WorkspaceStatus.INITIALISED;
        final WorkspaceStatus intermediateStatus = WorkspaceStatus.SUBMITTED;
        final WorkspaceStatus errorSubmittingStatus = WorkspaceStatus.DATA_MOVED_ERROR;
        final String initialMessage = "workspace is in good shape";
        final String intermediateMessage = "workspace was submitted";
        final String errorSubmittingMessage = "there were errors when submitting the workspace";
        final String archiveInfo = "still not sure what this would be";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = new Date(endCalendar.getTime().getTime());
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final Workspace initialWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                initialStatus, initialMessage, archiveInfo);
        
        final Workspace intermediateWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                intermediateStatus, intermediateMessage, archiveInfo);
        
        final Workspace updatedWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, endDate, startDate, endDate, usedStorageSpace, maxStorageSpace,
                errorSubmittingStatus, errorSubmittingMessage, archiveInfo);
        
        final String expectedErrorMessage = "Problem with thread execution while submitting workspace " + workspaceID;
        final ExecutionException expectedException = new ExecutionException("some exception message", null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(initialWorkspace));
            
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(intermediateWorkspace);
            
            oneOf(mockWorkspaceExportRunner).setWorkspace(initialWorkspace);
//            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(Boolean.TRUE);
            
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(updatedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(updatedWorkspace);
        }});
        
        try {
            manager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void submitWorkspaceFails() throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceExportException {
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final URI topNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL topNodeArchiveURL = new URL("file:/archive/folder/someNode.cmdi");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        final Date startDate = startCalendar.getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus initialStatus = WorkspaceStatus.INITIALISED;
        final WorkspaceStatus intermediateStatus = WorkspaceStatus.SUBMITTED;
        final WorkspaceStatus errorSubmittingStatus = WorkspaceStatus.DATA_MOVED_ERROR;
        final String initialMessage = "workspace is in good shape";
        final String intermediateMessage = "workspace was submitted";
        final String errorSubmittingMessage = "there were errors when submitting the workspace";
        final String archiveInfo = "still not sure what this would be";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = endCalendar.getTime();
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final Workspace initialWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                initialStatus, initialMessage, archiveInfo);
        
        final Workspace intermediateWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace,
                intermediateStatus, intermediateMessage, archiveInfo);
        
        final Workspace updatedWorkspace = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, endDate, startDate, endDate, usedStorageSpace, maxStorageSpace,
                errorSubmittingStatus, errorSubmittingMessage, archiveInfo);
        
        final String expectedErrorMessage = "Workspace submission failed for workspace " + workspaceID;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(initialWorkspace));
            
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(intermediateWorkspace);
            
            oneOf(mockWorkspaceExportRunner).setWorkspace(initialWorkspace);
//            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(Boolean.TRUE);
            
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(updatedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(updatedWorkspace);
        }});
        
        try {
            manager.submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
        }
    }
}
