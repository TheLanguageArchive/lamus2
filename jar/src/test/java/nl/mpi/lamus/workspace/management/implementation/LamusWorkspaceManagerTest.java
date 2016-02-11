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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import nl.mpi.lamus.archive.permissions.PermissionAdjuster;
import nl.mpi.lamus.archive.permissions.implementation.PermissionAdjusterScope;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.metadata.validation.WorkspaceFileValidator;
import nl.mpi.lamus.metadata.validation.implementation.MetadataValidationIssue;
import nl.mpi.lamus.metadata.validation.implementation.MetadataValidationIssueSeverity;
import nl.mpi.lamus.typechecking.testing.ValidationIssueCollectionMatcher;
import nl.mpi.lamus.util.CalendarHelper;
import nl.mpi.lamus.workspace.exporting.WorkspaceExportRunnerFactory;
import nl.mpi.lamus.workspace.exporting.implementation.WorkspaceExportRunner;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunnerFactory;
import nl.mpi.lamus.workspace.importing.implementation.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
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
        setThreadingPolicy(new Synchroniser());
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
    @Mock private WorkspaceFileValidator mockWorkspaceFileValidator;
    @Mock private PermissionAdjuster mockPermissionAdjuster;
    
    @Mock private WorkspaceImportRunnerFactory mockWorkspaceImportRunnerFactory;
    @Mock private WorkspaceExportRunnerFactory mockWorkspaceExportRunnerFactory;
    
    @Mock private Future<Boolean> mockFuture;
    @Mock private Workspace mockWorkspace;
    @Mock private Workspace mockSubmittedWorkspace;
    @Mock private MetadataValidationIssue mockValidationIssue1;
    @Mock private MetadataValidationIssue mockValidationIssue2;

    @Factory
    public static Matcher<Collection<MetadataValidationIssue>> equivalentValidationIssueCollection(Collection<MetadataValidationIssue> collection) {
        return new ValidationIssueCollectionMatcher(collection);
    }
    
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
                mockWorkspaceDirectoryHandler, mockCalendarHelper,
                mockWorkspaceFileValidator, mockPermissionAdjuster,
                mockWorkspaceImportRunnerFactory, mockWorkspaceExportRunnerFactory);
        
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
            oneOf(mockWorkspaceImportRunnerFactory).getNewImportRunner(); will(returnValue(mockWorkspaceImportRunner));
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(expectedWorkspace));
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeURI);
        assertNotNull("Returned workspace should not be null when object, database and directory are successfully created.", result);
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
            oneOf(mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
            oneOf(mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf(mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace.getWorkspaceID());
                will(throwException(expectedException));
            oneOf(mockWorkspaceDao).unlockAllNodesOfWorkspace(newWorkspace.getWorkspaceID());
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
            oneOf(mockWorkspaceImportRunnerFactory).getNewImportRunner(); will(returnValue(mockWorkspaceImportRunner));
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            oneOf(mockWorkspaceDao).unlockAllNodesOfWorkspace(workspaceID);
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
            oneOf(mockWorkspaceImportRunnerFactory).getNewImportRunner(); will(returnValue(mockWorkspaceImportRunner));
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            oneOf(mockWorkspaceDao).unlockAllNodesOfWorkspace(workspaceID);
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
            oneOf(mockWorkspaceImportRunnerFactory).getNewImportRunner(); will(returnValue(mockWorkspaceImportRunner));
            oneOf(mockWorkspaceImportRunner).setWorkspace(newWorkspace);
            oneOf(mockWorkspaceImportRunner).setTopNodeArchiveURI(archiveNodeURI);
            oneOf(mockExecutorService).submit(mockWorkspaceImportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceDao).unlockAllNodesOfWorkspace(workspaceID);
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
            oneOf(mockWorkspaceImportRunnerFactory).getNewImportRunner(); will(returnValue(mockWorkspaceImportRunner));
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
    public void deleteWorkspaceSuccessfully() throws WorkspaceNotFoundException, WorkspaceExportException, InterruptedException, ExecutionException, IOException {
        
        final int workspaceID = 1;
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.DELETE_WORKSPACE);
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceDao).deleteWorkspace(mockWorkspace);
            oneOf(mockWorkspaceDirectoryHandler).deleteWorkspaceDirectory(workspaceID);
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.UNLINKED_NODES_ONLY);
        }});
        
        manager.deleteWorkspace(workspaceID, keepUnlinkedFiles);
    }
    
    @Test
    public void deleteWorkspaceThrowsWorkspaceNotFoundException() throws WorkspaceNotFoundException, WorkspaceExportException, IOException {
        
        final int workspaceID = 1;
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            manager.deleteWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch (WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteWorkspaceThreadInterrupted() throws WorkspaceNotFoundException, InterruptedException, ExecutionException, IOException {
        
        final int workspaceID = 1;
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final String expectedErrorMessage = "Interruption in thread while deleting workspace " + workspaceID;
        final InterruptedException expectedException = new InterruptedException("some exception message");
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.DELETE_WORKSPACE);
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.UNLINKED_NODES_ONLY);
        }});
        
        try {
            manager.deleteWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void deleteWorkspaceExecutionException() throws WorkspaceNotFoundException, InterruptedException, ExecutionException, IOException {
        
        final int workspaceID = 1;
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final String expectedErrorMessage = "Problem with thread execution while deleting workspace " + workspaceID;
        final ExecutionException expectedException = new ExecutionException("some exception message", null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.DELETE_WORKSPACE);
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.UNLINKED_NODES_ONLY);
        }});
        
        try {
            manager.deleteWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown an exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void deleteWorkspaceFails() throws WorkspaceNotFoundException, InterruptedException, ExecutionException, IOException {
        
        final int workspaceID = 1;
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final String expectedErrorMessage = "Workspace deletion failed for workspace " + workspaceID;
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.DELETE_WORKSPACE);
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockFuture).get(); will(returnValue(Boolean.FALSE));
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.UNLINKED_NODES_ONLY);
        }});
        
        try {
            manager.deleteWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
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
        final String crawlerID = "";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, expectedUserID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, crawlerID);
        
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
        final String crawlerID = "";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveURI, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, crawlerID);
        
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
    public void submitWorkspaceSuccessful()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException,
            WorkspaceExportException, MetadataValidationException {

        final int workspaceID = 1;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        
        final WorkspaceStatus submittedStatus = WorkspaceStatus.SUBMITTED;
        final String submittedMessage = "workspace was submitted";
        
        final WorkspaceStatus successfullySubmittedStatus = WorkspaceStatus.UPDATING_ARCHIVE;
        final String successfullySubmittedMessage = "Data was successfully moved to the archive. It is now being updated in the database.\nAn email will be sent after this process is finished (it can take a while, depending on the size of the workspace).";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = endCalendar.getTime();
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            
            oneOf(mockWorkspaceFileValidator).triggerSchematronValidationForMetadataFilesInWorkspace(workspaceID);
            
            oneOf(mockWorkspace).setStatus(submittedStatus);
            oneOf(mockWorkspace).setMessage(submittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);

            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.SUBMIT_WORKSPACE);
            
            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockSubmittedWorkspace));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            
            oneOf(mockSubmittedWorkspace).setSessionEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setStatus(successfullySubmittedStatus);
            oneOf(mockSubmittedWorkspace).setMessage(successfullySubmittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(mockSubmittedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSubmittedWorkspace);
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.ALL_NODES);
        }});
        
        
        manager.submitWorkspace(workspaceID, keepUnlinkedFiles);
    }
    
    @Test
    public void submitWorkspace_ValidationIssues_Error()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException, WorkspaceExportException, MetadataValidationException {
        
        final int workspaceID = 1;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final String filename = "file.cmdi";
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        issues.add(mockValidationIssue1);
        issues.add(mockValidationIssue2);
        
        final String assertionErrorMessage1 = "[CMDI Archive Restriction] the CMD profile of this record is not allowed in the archive.";
        final String assertionErrorMessage2 = "[CMDI Archive Restriction] Something completely different went wrong.";
        final String validationIssuesString = "Validation issue for file '" + filename + "' - " + MetadataValidationIssueSeverity.ERROR.toString() + ": " + assertionErrorMessage1 + ".\n" +
                "Validation issue for file '" + filename + "' - " + MetadataValidationIssueSeverity.ERROR.toString() + ": " + assertionErrorMessage2 + ".\n";
        
        final MetadataValidationException expectedException = new MetadataValidationException(validationIssuesString, workspaceID, null);
        expectedException.addValidationIssues(issues);
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            
            oneOf(mockWorkspaceFileValidator).triggerSchematronValidationForMetadataFilesInWorkspace(workspaceID); will(throwException(expectedException));
            oneOf(mockWorkspaceFileValidator).validationIssuesToString(with(equivalentValidationIssueCollection(issues))); will(returnValue(validationIssuesString));
            oneOf(mockWorkspaceFileValidator).validationIssuesContainErrors(with(equivalentValidationIssueCollection(issues))); will(returnValue(Boolean.TRUE));
        }});
        
        try {
            manager.submitWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(MetadataValidationException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspace_ValidationIssues_Warning()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException, WorkspaceExportException, MetadataValidationException {
        
        final int workspaceID = 1;
        
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        
        final WorkspaceStatus submittedStatus = WorkspaceStatus.SUBMITTED;
        final String submittedMessage = "workspace was submitted";
        
        final WorkspaceStatus successfullySubmittedStatus = WorkspaceStatus.UPDATING_ARCHIVE;
        final String successfullySubmittedMessage = "Data was successfully moved to the archive. It is now being updated in the database.\nAn email will be sent after this process is finished (it can take a while, depending on the size of the workspace).";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = endCalendar.getTime();
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final String filename = "file.cmdi";
        
        final Collection<MetadataValidationIssue> issues = new ArrayList<>();
        issues.add(mockValidationIssue1);
        
        final String assertionErrorMessage = "[CMDI Best Practice] /cmd:CMD/cmd:Components/*/cmd:Title shouldn't be empty.";
        final String validationIssuesString = "Validation issue for file '" + filename + "' - " + MetadataValidationIssueSeverity.WARN.toString() + ": " + assertionErrorMessage + ".\n";
        
        final MetadataValidationException expectedException = new MetadataValidationException(validationIssuesString, workspaceID, null);
        expectedException.addValidationIssues(issues);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            
            oneOf(mockWorkspaceFileValidator).triggerSchematronValidationForMetadataFilesInWorkspace(workspaceID); will(throwException(expectedException));
            oneOf(mockWorkspaceFileValidator).validationIssuesToString(with(equivalentValidationIssueCollection(issues))); will(returnValue(validationIssuesString));
            oneOf(mockWorkspaceFileValidator).validationIssuesContainErrors(with(equivalentValidationIssueCollection(issues))); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspace).setStatus(submittedStatus);
            oneOf(mockWorkspace).setMessage(submittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
            
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.SUBMIT_WORKSPACE);

            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockSubmittedWorkspace));
            oneOf(mockFuture).get(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            
            oneOf(mockSubmittedWorkspace).setSessionEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setStatus(successfullySubmittedStatus);
            oneOf(mockSubmittedWorkspace).setMessage(successfullySubmittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(mockSubmittedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSubmittedWorkspace);
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.ALL_NODES);
        }});
        
        manager.submitWorkspace(workspaceID, keepUnlinkedFiles);
    }
    
    @Test
    public void submitWorkspaceThrowsWorkspaceNotFoundException()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException,
            WorkspaceExportException, MetadataValidationException {
        
        final int workspaceID = 1;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            manager.submitWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch (WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceThreadInterrupted()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException,
            WorkspaceExportException, MetadataValidationException {
        
        final int workspaceID = 1;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        
        final WorkspaceStatus submittedStatus = WorkspaceStatus.SUBMITTED;
        final String submittedMessage = "workspace was submitted";
        
        final WorkspaceStatus errorSubmittingStatus = WorkspaceStatus.ERROR_MOVING_DATA;
        final String errorSubmittingMessage = "There were errors when submitting the workspace. Please contact the corpus management team.";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = new Date(endCalendar.getTime().getTime());
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final String expectedErrorMessage = "Interruption in thread while submitting workspace " + workspaceID;
        final InterruptedException expectedException = new InterruptedException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            
            oneOf(mockWorkspaceFileValidator).triggerSchematronValidationForMetadataFilesInWorkspace(workspaceID);
            
            oneOf(mockWorkspace).setStatus(submittedStatus);
            oneOf(mockWorkspace).setMessage(submittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
            
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.SUBMIT_WORKSPACE);

            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockSubmittedWorkspace));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            
            oneOf(mockSubmittedWorkspace).setSessionEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setStatus(errorSubmittingStatus);
            oneOf(mockSubmittedWorkspace).setMessage(errorSubmittingMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(mockSubmittedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSubmittedWorkspace);
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.ALL_NODES);
        }});
        
        try {
            manager.submitWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void submitWorkspaceExecutionException()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException,
            WorkspaceExportException, MetadataValidationException {
        
        final int workspaceID = 1;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        
        final WorkspaceStatus submittedStatus = WorkspaceStatus.SUBMITTED;
        final String submittedMessage = "workspace was submitted";
        
        final WorkspaceStatus errorSubmittingStatus = WorkspaceStatus.ERROR_MOVING_DATA;
        final String errorSubmittingMessage = "There were errors when submitting the workspace. Please contact the corpus management team.";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = new Date(endCalendar.getTime().getTime());
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final String expectedErrorMessage = "Problem with thread execution while submitting workspace " + workspaceID;
        final ExecutionException expectedException = new ExecutionException("some exception message", null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            
            oneOf(mockWorkspaceFileValidator).triggerSchematronValidationForMetadataFilesInWorkspace(workspaceID);
            
            oneOf(mockWorkspace).setStatus(submittedStatus);
            oneOf(mockWorkspace).setMessage(submittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
            
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.SUBMIT_WORKSPACE);

            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockSubmittedWorkspace));
            oneOf(mockFuture).get(); will(throwException(expectedException));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            
            oneOf(mockSubmittedWorkspace).setSessionEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setStatus(errorSubmittingStatus);
            oneOf(mockSubmittedWorkspace).setMessage(errorSubmittingMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(mockSubmittedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSubmittedWorkspace);
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.ALL_NODES);
        }});
        
        try {
            manager.submitWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void submitWorkspaceFails()
            throws InterruptedException, ExecutionException, URISyntaxException,
            MalformedURLException, WorkspaceNotFoundException,
            WorkspaceExportException, MetadataValidationException {
        
        final int workspaceID = 1;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.DAY_OF_MONTH, -2);
        
        final WorkspaceStatus submittedStatus = WorkspaceStatus.SUBMITTED;
        final String submittedMessage = "workspace was submitted";
        
        final WorkspaceStatus errorSubmittingStatus = WorkspaceStatus.ERROR_MOVING_DATA;
        final String errorSubmittingMessage = "There were errors when submitting the workspace. Please contact the corpus management team.";
        
        final Calendar endCalendar = Calendar.getInstance();
        final Date endDate = endCalendar.getTime();
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final String expectedErrorMessage = "Workspace submission failed for workspace " + workspaceID;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
            
            oneOf(mockWorkspaceFileValidator).triggerSchematronValidationForMetadataFilesInWorkspace(workspaceID);
            
            oneOf(mockWorkspace).setStatus(submittedStatus);
            oneOf(mockWorkspace).setMessage(submittedMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
            
            oneOf(mockWorkspaceExportRunnerFactory).getNewExportRunner(); will(returnValue(mockWorkspaceExportRunner));
            oneOf(mockWorkspaceExportRunner).setWorkspace(mockWorkspace);
            oneOf(mockWorkspaceExportRunner).setKeepUnlinkedFiles(keepUnlinkedFiles);
            oneOf(mockWorkspaceExportRunner).setSubmissionType(WorkspaceSubmissionType.SUBMIT_WORKSPACE);

            oneOf(mockExecutorService).submit(mockWorkspaceExportRunner); will(returnValue(mockFuture));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockSubmittedWorkspace));
            oneOf(mockFuture).get(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockCalendarHelper).getCalendarInstance(); will(returnValue(endCalendar));
            
            oneOf(mockSubmittedWorkspace).setSessionEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setEndDate(endDate);
            oneOf(mockSubmittedWorkspace).setStatus(errorSubmittingStatus);
            oneOf(mockSubmittedWorkspace).setMessage(errorSubmittingMessage);
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(mockSubmittedWorkspace);
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockSubmittedWorkspace);
            oneOf(mockPermissionAdjuster).adjustPermissions(workspaceID, PermissionAdjusterScope.ALL_NODES);
        }});
        
        try {
            manager.submitWorkspace(workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspaceID, ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
        }
    }
}
