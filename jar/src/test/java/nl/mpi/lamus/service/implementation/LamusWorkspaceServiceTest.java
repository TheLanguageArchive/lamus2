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
package nl.mpi.lamus.service.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;
import nl.mpi.archiving.corpusstructure.core.NodeNotFoundException;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.exception.MetadataValidationException;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.typechecking.TypecheckedResults;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.replace.implementation.LamusNodeReplaceManager;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import nl.mpi.lamus.workspace.importing.implementation.ImportProblem;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceService service;
    @Mock private WorkspaceAccessChecker mockNodeAccessChecker;
    @Mock private ArchiveHandleHelper mockArchiveHandleHelper;
    @Mock private WorkspaceManager mockWorkspaceManager;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceUploader mockWorkspaceUploader;
    @Mock private WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    @Mock private WorkspaceNodeManager mockWorkspaceNodeManager;
    @Mock private LamusNodeReplaceManager mockNodeReplaceManager;
    
    @Mock private WorkspaceNode mockParentNode;
    @Mock private WorkspaceNode mockChildNode;
    @Mock private WorkspaceNode mockOldNode;
    @Mock private WorkspaceNode mockNewNode;
    @Mock private File mockWorkspaceUploadDirectory;
    @Mock private List<WorkspaceNode> mockUnlinkedNodesList;
    @Mock private InputStream mockInputStream;
    @Mock private File mockFile;
    @Mock private Collection<File> mockUploadedFiles;
    @Mock private Collection<ImportProblem> mockFailedUploads;
    @Mock private TypecheckedResults mockTypecheckedResults;
    @Mock private ZipInputStream mockZipInputStream;

    private final int workspaceID = 1;
    private final String userID = "testUser";
    
    
    public LamusWorkspaceServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        service = new LamusWorkspaceService(
                mockNodeAccessChecker, mockArchiveHandleHelper,
                mockWorkspaceManager, mockWorkspaceDao,
                mockWorkspaceUploader, mockWorkspaceNodeLinkManager,
                mockWorkspaceNodeManager, mockNodeReplaceManager);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void createWorkspaceNullUri()
            throws MalformedURLException, URISyntaxException, NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        final URI archiveNodeURI = null;
        final String expectedMessage = "Both userID and archiveNodeURI should not be null";
        
        try {
            service.createWorkspace(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void createWorkspaceNullUser()
            throws MalformedURLException, URISyntaxException, NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String expectedMessage = "Both userID and archiveNodeURI should not be null";
        
        try {
            service.createWorkspace(null, archiveNodeURI);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void createWorkspaceThrowsNodeNotFoundException()
            throws MalformedURLException, URISyntaxException, NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        final URI archiveNodeURI = new URI("node:001");
        final NodeNotFoundException expectedException = new NodeNotFoundException(archiveNodeURI, "access problem");
        
        context.checking(new Expectations() {{
            oneOf(mockArchiveHandleHelper).getArchiveHandleForNode(archiveNodeURI);
                will(throwException(expectedException));
        }});
        
        try {
            service.createWorkspace(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(NodeNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void createWorkspaceThrowsNodeAccessException()
            throws MalformedURLException, URISyntaxException, NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        final URI archiveNodeURI = new URI("node:001");
        final URI archiveNodePid = new URI("hdl:" + UUID.randomUUID().toString());
        final NodeAccessException expectedException = new NodeAccessException("access problem", archiveNodeURI, null);
        
        context.checking(new Expectations() {{
            oneOf(mockArchiveHandleHelper).getArchiveHandleForNode(archiveNodeURI); will(returnValue(archiveNodePid));
            oneOf(mockNodeAccessChecker).ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
                will(throwException(expectedException));
        }});
        
        try {
            service.createWorkspace(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(NodeAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void createWorkspaceThrowsWorkspaceImportException()
            throws MalformedURLException, URISyntaxException, NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        final URI archiveNodeURI = new URI("node:001");
        final URI archiveNodePid = new URI("hdl:" + UUID.randomUUID().toString());
        final WorkspaceImportException expectedException = new WorkspaceImportException("some problem", workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockArchiveHandleHelper).getArchiveHandleForNode(archiveNodeURI); will(returnValue(archiveNodePid));
            oneOf(mockNodeAccessChecker).ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            //allow other calls
            oneOf(mockWorkspaceManager).createWorkspace(userID, archiveNodePid); will(throwException(expectedException));
        }});
        
        try {
            service.createWorkspace(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(WorkspaceImportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void createWorkspaceSuccess()
            throws MalformedURLException, URISyntaxException, NodeAccessException, WorkspaceImportException, NodeNotFoundException {
        
        final URI archiveNodeURI = new URI("node:001");
        final URI archiveNodePid = new URI("hdl:" + UUID.randomUUID().toString());
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        context.checking(new Expectations() {{
            oneOf(mockArchiveHandleHelper).getArchiveHandleForNode(archiveNodeURI); will(returnValue(archiveNodePid));
            oneOf(mockNodeAccessChecker).ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            //allow other calls
            oneOf(mockWorkspaceManager).createWorkspace(userID, archiveNodePid); will(returnValue(newWorkspace));
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeURI);
        assertNotNull("Returned workspace should not be null when it can be created", result);
        assertEquals("Returned workspace is different from expected", result, newWorkspace);
    }
    
    @Test
    public void deleteExistingWorkspace() throws WorkspaceNotFoundException, WorkspaceExportException, WorkspaceAccessException, IOException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserCanDeleteWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).deleteWorkspace(workspaceID, keepUnlinkedFiles);
        }});
        
        service.deleteWorkspace(userID, workspaceID, keepUnlinkedFiles);
    }
    
    @Test
    public void deleteNonExistingWorkspace() throws WorkspaceNotFoundException, WorkspaceExportException, WorkspaceAccessException, IOException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserCanDeleteWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.deleteWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteInaccessibleWorkspace() throws WorkspaceNotFoundException, WorkspaceExportException, WorkspaceAccessException, IOException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserCanDeleteWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.deleteWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteWorkspaceExportException() throws WorkspaceNotFoundException, WorkspaceExportException, WorkspaceAccessException, IOException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserCanDeleteWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).deleteWorkspace(workspaceID, keepUnlinkedFiles); will(throwException(expectedException));
        }});
        
        try {
            service.deleteWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteWorkspaceFails() throws WorkspaceNotFoundException, WorkspaceExportException, WorkspaceAccessException, IOException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserCanDeleteWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).deleteWorkspace(workspaceID, keepUnlinkedFiles); will(throwException(expectedException));
        }});
        
        try {
            service.deleteWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void getExistingWorkspace() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException {
        
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
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = service.getWorkspace(workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
    }
    
    @Test
    public void getNonExistingWorkspace() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException {
        
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
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.getWorkspace(workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void listUserWorkspaces() throws URISyntaxException, MalformedURLException {
        
        final Date date = Calendar.getInstance().getTime();
        Workspace workspace1 = new LamusWorkspace(1, userID, 1, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node1.cmdi"),
                date, null, date, null, 0L, 10000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        Workspace workspace2 = new LamusWorkspace(2, userID, 2, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node2.cmdi"),
                date, null, date, null, 0L, 1000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        final Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesForUser(userID); will(returnValue(expectedList));
        }});
        
        Collection<Workspace> result = service.listUserWorkspaces(userID);
        
        assertEquals("Retrieved list different from expected", expectedList, result);
    }
    
    @Test
    public void listAllWorkspaces() throws URISyntaxException, MalformedURLException {
        
        final Date date = Calendar.getInstance().getTime();
        Workspace workspace1 = new LamusWorkspace(1, userID, 1, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node1.cmdi"),
                date, null, date, null, 0L, 10000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        Workspace workspace2 = new LamusWorkspace(2, userID, 2, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node2.cmdi"),
                date, null, date, null, 0L, 1000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        final List<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getAllWorkspaces(); will(returnValue(expectedList));
        }});
        
        List<Workspace> result = service.listAllWorkspaces();
        
        assertEquals("Retrieved list different from expected", expectedList, result);
    }
    
    @Test
    public void userHasActiveWorkspaces() throws URISyntaxException, MalformedURLException {
        
        final Date date = Calendar.getInstance().getTime();
        Workspace workspace1 = new LamusWorkspace(1, userID, 1, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node1.cmdi"),
                date, null, date, null, 0L, 10000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        Workspace workspace2 = new LamusWorkspace(2, userID, 2, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node2.cmdi"),
                date, null, date, null, 0L, 1000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        final Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesForUser(userID); will(returnValue(expectedList));
        }});
        
        boolean result = service.userHasWorkspaces(userID);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void userHasActiveAndInactiveWorkspaces() throws URISyntaxException, MalformedURLException {
        
        final Date date = Calendar.getInstance().getTime();
        Workspace workspace1 = new LamusWorkspace(1, userID, 1, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node1.cmdi"),
                date, null, date, null, 0L, 10000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        Workspace workspace2 = new LamusWorkspace(2, userID, 2, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node2.cmdi"),
                date, null, date, null, 0L, 1000000L, WorkspaceStatus.UNINITIALISED, "workspace is in good shape", "still not sure what this would be");
        final Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesForUser(userID); will(returnValue(expectedList));
        }});
        
        boolean result = service.userHasWorkspaces(userID);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void userHasNoActiveWorkspaces() throws URISyntaxException, MalformedURLException {
        
        final Date date = Calendar.getInstance().getTime();
        Workspace workspace1 = new LamusWorkspace(1, userID, 1, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node1.cmdi"),
                date, null, date, null, 0L, 10000000L, WorkspaceStatus.UNINITIALISED, "workspace is in good shape", "still not sure what this would be");
        Workspace workspace2 = new LamusWorkspace(2, userID, 2, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node2.cmdi"),
                date, null, date, null, 0L, 1000000L, WorkspaceStatus.ERROR_INITIALISATION, "workspace is in good shape", "still not sure what this would be");
        final Collection<Workspace> expectedList = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspacesForUser(userID); will(returnValue(expectedList));
        }});
        
        boolean result = service.userHasWorkspaces(userID);
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void openExistingWorkspace()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
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
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).openWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = service.openWorkspace(userID, workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
    }
    
    @Test
    public void openNonExistingWorkspace()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
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
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.openWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void openWorkspaceNullUser()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        final String expectedMessage = "userID should not be null";
        
        try {
            service.openWorkspace(null, workspaceID);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void openInaccessibleWorkspace()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
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
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.openWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void openWorkspaceThrowsIOException()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
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
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).openWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.openWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceNoAccess() throws WorkspaceNotFoundException,
            WorkspaceAccessException, WorkspaceExportException, MetadataValidationException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.submitWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceNotFound() throws WorkspaceNotFoundException,
            WorkspaceAccessException, WorkspaceExportException, MetadataValidationException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.submitWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceFails() throws WorkspaceNotFoundException,
            WorkspaceAccessException, WorkspaceExportException, MetadataValidationException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).submitWorkspace(workspaceID, keepUnlinkedFiles); will(throwException(expectedException));
        }});
        
        try {
            service.submitWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceMetadataValidationIssues() throws WorkspaceNotFoundException,
            WorkspaceAccessException, WorkspaceExportException, MetadataValidationException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final MetadataValidationException expectedException = new MetadataValidationException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).submitWorkspace(workspaceID, keepUnlinkedFiles); will(throwException(expectedException));
        }});
        
        try {
            service.submitWorkspace(userID, workspaceID, keepUnlinkedFiles);
            fail("should have thrown exception");
        } catch(MetadataValidationException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
        
    @Test
    public void submitWorkspaceSuccess() throws WorkspaceNotFoundException,
            WorkspaceAccessException, WorkspaceExportException, MetadataValidationException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).submitWorkspace(workspaceID, keepUnlinkedFiles);
        }});
        
        service.submitWorkspace(userID, workspaceID, keepUnlinkedFiles);
    }
    
    
    @Test
    public void getExistingNode() throws URISyntaxException, WorkspaceNodeNotFoundException {
        
        final int nodeID = 1;
        URI profileSchemaURI = null;
        String name = "node_name";
        String title = "node_title";
        WorkspaceNodeType type = WorkspaceNodeType.METADATA;
        URL wsURL = null;
        URI archiveURI = null;
        URL archiveURL = null;
        URI originURI = null;
        WorkspaceNodeStatus status = WorkspaceNodeStatus.ARCHIVE_COPY;
        boolean isProtected = Boolean.FALSE;
        String format = "cmdi";
        final WorkspaceNode nodeToRetrieve = new LamusWorkspaceNode(
                nodeID, workspaceID, profileSchemaURI, name, title, type, wsURL,
                archiveURI, archiveURL, originURI, status, isProtected, format);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(returnValue(nodeToRetrieve));
        }});
        
        WorkspaceNode result = service.getNode(nodeID);
        assertNotNull("Returned node should not be null", result);
        assertEquals("Returned node is different from expected", result, nodeToRetrieve);
    }
    
    @Test
    public void getNonExistingNode() throws URISyntaxException, WorkspaceNodeNotFoundException {
        
        final int nodeID = 1;
        URI profileSchemaURI = null;
        String name = "node_name";
        String title = "node_title";
        WorkspaceNodeType type = WorkspaceNodeType.METADATA;
        URL wsURL = null;
        URI archiveURI = null;
        URL archiveURL = null;
        URI originURI = null;
        WorkspaceNodeStatus status = WorkspaceNodeStatus.ARCHIVE_COPY;
        boolean isProtected = Boolean.FALSE;
        String format = "cmdi";
        final WorkspaceNode nodeToRetrieve = new LamusWorkspaceNode(
                nodeID, workspaceID, profileSchemaURI, name, title, type, wsURL,
                archiveURI, archiveURL, originURI, status, isProtected, format);
        
        final WorkspaceNodeNotFoundException expectedException = new WorkspaceNodeNotFoundException("some exception message", -1, nodeID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(throwException(expectedException));
        }});
        
        try {
            service.getNode(nodeID);
            fail("should have thrown exception");
        } catch(WorkspaceNodeNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void getExistingChildNodes() {
        
        final int nodeID = 1;
        final Collection<WorkspaceNode> expectedChildNodes = new ArrayList<>();
        final WorkspaceNode childNode = new LamusWorkspaceNode(
                2, 1, null, "name", "title", WorkspaceNodeType.RESOURCE_IMAGE, null,
                null, null, null, WorkspaceNodeStatus.VIRTUAL, Boolean.FALSE, "jpeg");
        expectedChildNodes.add(childNode);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(expectedChildNodes));
        }});
        
        Collection<WorkspaceNode> retrievedChildNodes = service.getChildNodes(nodeID);
        assertNotNull("Returned list of nodes should not be null", retrievedChildNodes);
        assertEquals("Returned list of nodes is different from expected", expectedChildNodes, retrievedChildNodes);
    }
    
    @Test
    public void getWorkspaceUploadDirectory() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).getWorkspaceUploadDirectory(workspaceID);
                will(returnValue(mockWorkspaceUploadDirectory));
        }});
        
        File result = service.getWorkspaceUploadDirectory(workspaceID);
        
        assertEquals("Retrieved directory different from expected", mockWorkspaceUploadDirectory, result);
    }
    
    @Test
    public void uploadFileIntoWorkspace() throws IOException, DisallowedPathException {
        
        final String filename = "someFile.cmdi";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
                will(returnValue(mockFile));
        }});
        
        File result = service.uploadFileIntoWorkspace(userID, workspaceID, mockInputStream, filename);
        
        assertEquals("Result different from expected", mockFile, result);
    }
    
    @Test
    public void uploadFileIntoWorkspace_throwsException() throws IOException, DisallowedPathException {
        
        final String filename = "someFile.cmdi";
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
                will(throwException(expectedException));
        }});
        
        try {
            service.uploadFileIntoWorkspace(userID, workspaceID, mockInputStream, filename);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void uploadZipFileIntoWorkspace() throws IOException, DisallowedPathException {
        
        final String filename = "someFile.cmdi";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
                will(returnValue(mockUploadedFiles));
        }});
        
        Collection<File> result = service.uploadZipFileIntoWorkspace(userID, workspaceID, mockZipInputStream, filename);
        
        assertEquals("Result different from expected", mockUploadedFiles, result);
    }
    
    @Test
    public void uploadZipFileIntoWorkspace_throwsException() throws IOException, DisallowedPathException {
        
        final String filename = "someFile.cmdi";
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadZipFileIntoWorkspace(workspaceID, mockZipInputStream);
                will(throwException(expectedException));
        }});
        
        try {
             service.uploadZipFileIntoWorkspace(userID, workspaceID, mockZipInputStream, filename);
             fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void processUploadedFiles() throws IOException, WorkspaceException, TypeCheckerException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).processUploadedFiles(workspaceID, mockUploadedFiles);
                will(returnValue(mockFailedUploads));
        }});
        
        Collection<ImportProblem> result = service.processUploadedFiles(userID, workspaceID, mockUploadedFiles);
        
        assertEquals("Resulting map different from expected", mockFailedUploads, result);
    }
    
    @Test
    public void processUploadedFilesThrowsWorkspaceException() throws IOException, WorkspaceException, TypeCheckerException {
        
        final WorkspaceException workspaceException = new WorkspaceException("some error message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).processUploadedFiles(workspaceID, mockUploadedFiles);
                will(throwException(workspaceException));
        }});
        
        try {
            service.processUploadedFiles(userID, workspaceID, mockUploadedFiles);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", workspaceException, ex);
        }
    }
    
    @Test
    public void addNodeWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, MalformedURLException {
        
        final URL nodeURL = new URL("file:/workspace/node.cmdi");
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(nodeURL));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.addNode(userID, mockChildNode);
            fail("should have thrown an exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different frome expected", expectedException, ex);
        }
    }
    
    @Test
    public void addNodeNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, MalformedURLException {
        
        final URL nodeURL = new URL("file:/workspace/node.cmdi");
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(nodeURL));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.addNode(userID, mockChildNode);
            fail("should have thrown an exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different frome expected", expectedException, ex);
        }
    }
    
    @Test
    public void addNodeSuccessfully() throws WorkspaceNotFoundException, WorkspaceAccessException, MalformedURLException {
        
        final URL nodeURL = new URL("file:/workspace/node.cmdi");
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceURL(); will(returnValue(nodeURL));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceDao).addWorkspaceNode(mockChildNode);
        }});
        
        service.addNode(userID, mockChildNode);
    }
    
    @Test
    public void linkNodesWithAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode);
        }});
        
        service.linkNodes(userID, mockParentNode, mockChildNode);
    }
    
    @Test
    public void linkNodesWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.linkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void linkNodesNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.linkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void linkNodesWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            service.linkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void linkNodesProtectedNodeException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException, URISyntaxException {
        
        final int parentNodeID = 10;
        final URI parentNodeURI = new URI(UUID.randomUUID().toString());
        final int childNodeID = 20;
        
        final ProtectedNodeException expectedException = new ProtectedNodeException("some exception message", parentNodeURI, workspaceID);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            service.linkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void unlinkNodesWithAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockParentNode, mockChildNode);
        }});
        
        service.unlinkNodes(userID, mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkNodesWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.unlinkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void unlinkNodesNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.unlinkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void unlinkNodesWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int parentNodeID = 10;
        final int childNodeID = 20;
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockParentNode, mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            service.unlinkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void unlinkNodesProtectedNodeException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException, URISyntaxException {
        
        final int parentNodeID = 10;
        final URI parentNodeURI = new URI(UUID.randomUUID().toString());
        final int childNodeID = 20;
        
        final ProtectedNodeException expectedException = new ProtectedNodeException("some exception message", parentNodeURI, workspaceID);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockParentNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockParentNode, mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            service.unlinkNodes(userID, mockParentNode, mockChildNode);
            fail("should have thrown exception");
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteNodeWithAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int nodeID = 4;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeManager).deleteNodesRecursively(mockChildNode);
        }});
        
        service.deleteNode(userID, mockChildNode);
    }
    
    @Test
    public void deleteNodeWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int nodeID = 4;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});

        try {
            service.deleteNode(userID, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteNodeNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int nodeID = 4;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});

        try {
            service.deleteNode(userID, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteNodeWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int nodeID = 4;
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeManager).deleteNodesRecursively(mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            service.deleteNode(userID, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteNodeProtectedNodeException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException, URISyntaxException {
        
        final int nodeID = 4;
        final URI nodeURI = new URI(UUID.randomUUID().toString());
        
        final ProtectedNodeException expectedException = new ProtectedNodeException("some exception message", nodeURI, workspaceID);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeManager).deleteNodesRecursively(mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            service.deleteNode(userID, mockChildNode);
            fail("should have thrown exception");
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void replaceNodeWithAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int oldNodeID = 10;
        final int newNodeID = 20;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockNodeReplaceManager).replaceTree(mockOldNode, mockNewNode, mockParentNode);
        }});
        
        service.replaceTree(userID, mockOldNode, mockNewNode, mockParentNode);
    }
    
    @Test
    public void replaceNodeWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int oldNodeID = 10;
        final int newNodeID = 20;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockNodeReplaceManager).replaceTree(mockOldNode, mockNewNode, mockParentNode);
        }});
        
        try {
            service.replaceTree(userID, mockOldNode, mockNewNode, mockParentNode);
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void replaceNodeNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int oldNodeID = 10;
        final int newNodeID = 20;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockNodeReplaceManager).replaceTree(mockOldNode, mockNewNode, mockParentNode);
        }});
        
        try {
            service.replaceTree(userID, mockOldNode, mockNewNode, mockParentNode);
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void replaceNodeWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        final int oldNodeID = 10;
        final int newNodeID = 20;
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockNodeReplaceManager).replaceTree(mockOldNode, mockNewNode, mockParentNode);
        }});
        
        try {
            service.replaceTree(userID, mockOldNode, mockNewNode, mockParentNode);
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void replaceNodeProtectedNodeException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException, URISyntaxException {
        
        final int oldNodeID = 10;
        final URI oldNodeURI = new URI(UUID.randomUUID().toString());
        final int newNodeID = 20;
        
        final ProtectedNodeException expectedException = new ProtectedNodeException("some exception message", oldNodeURI, workspaceID);
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockNodeReplaceManager).replaceTree(mockOldNode, mockNewNode, mockParentNode);
        }});
        
        try {
            service.replaceTree(userID, mockOldNode, mockNewNode, mockParentNode);
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void listUnlinkedNodes() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getUnlinkedNodes(workspaceID); will(returnValue(mockUnlinkedNodesList));
        }});
        
        service.listUnlinkedNodes(userID, workspaceID);
    }

}
