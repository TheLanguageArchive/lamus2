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
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.exception.TypeCheckerException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.apache.commons.fileupload.FileItem;
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
public class LamusWorkspaceServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private WorkspaceService service;
    @Mock private WorkspaceAccessChecker mockNodeAccessChecker;
    @Mock private WorkspaceManager mockWorkspaceManager;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceUploader mockWorkspaceUploader;
    @Mock private WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    
    @Mock private WorkspaceNode mockParentNode;
    @Mock private WorkspaceNode mockChildNode;
    @Mock private File mockWorkspaceUploadDirectory;
    @Mock private List<WorkspaceNode> mockUnlinkedNodesList;
    @Mock private InputStream mockInputStream;
    @Mock private Collection<File> mockUploadedFiles;

    
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
                mockNodeAccessChecker, mockWorkspaceManager, mockWorkspaceDao,
                mockWorkspaceUploader, mockWorkspaceNodeLinkManager);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void createWorkspaceThrowsUnknownNodeException()
            throws MalformedURLException, URISyntaxException, NodeAccessException, UnknownNodeException, WorkspaceImportException {
        
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        final UnknownNodeException expectedException = new UnknownNodeException("node not found");
        
        context.checking(new Expectations() {{
            oneOf(mockNodeAccessChecker).ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
                will(throwException(expectedException));
        }});
        
        try {
            service.createWorkspace(userID, archiveNodeURI);
            fail("should have thrown exception");
        } catch(UnknownNodeException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void createWorkspaceThrowsNodeAccessException()
            throws MalformedURLException, URISyntaxException, NodeAccessException, UnknownNodeException, WorkspaceImportException {
        
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        final NodeAccessException expectedException = new NodeAccessException("access problem", archiveNodeURI, null);
        
        context.checking(new Expectations() {{
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
            throws MalformedURLException, URISyntaxException, NodeAccessException, UnknownNodeException, WorkspaceImportException {
        
        final int workspaceID = 10;
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        final WorkspaceImportException expectedException = new WorkspaceImportException("some problem", workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeAccessChecker).ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            //allow other calls
            oneOf(mockWorkspaceManager).createWorkspace(userID, archiveNodeURI); will(throwException(expectedException));
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
            throws MalformedURLException, URISyntaxException, UnknownNodeException, NodeAccessException, WorkspaceImportException {
        
        final URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeAccessChecker).ensureWorkspaceCanBeCreated(userID, archiveNodeURI);
            //allow other calls
            oneOf(mockWorkspaceManager).createWorkspace(userID, archiveNodeURI); will(returnValue(newWorkspace));
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeURI);
        assertNotNull("Returned workspace should not be null when it can be created", result);
        assertEquals("Returned workspace is different from expected", result, newWorkspace);
    }
    
    @Test
    public void deleteExistingWorkspace() throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).deleteWorkspace(workspaceID);
        }});
        
        service.deleteWorkspace(userID, workspaceID);
    }
    
    @Test
    public void deleteNonExistingWorkspace() throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.deleteWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteInaccessibleWorkspace() throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.deleteWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void deleteWorkspaceFails() throws WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).deleteWorkspace(workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.deleteWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(IOException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void getExistingWorkspace() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException {
        
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
        }});
        
        Workspace result = service.getWorkspace(workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
    }
    
    @Test
    public void getNonExistingWorkspace() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException {
        
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
        
        final String userID = "someUser";
        final Date date = Calendar.getInstance().getTime();
        Workspace workspace1 = new LamusWorkspace(1, userID, 1, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node1.cmdi"),
                date, null, date, null, 0L, 10000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        Workspace workspace2 = new LamusWorkspace(2, userID, 2, new URI(UUID.randomUUID().toString()), new URL("file:/archive/folder/node2.cmdi"),
                date, null, date, null, 0L, 1000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        final Collection<Workspace> expectedList = new ArrayList<Workspace>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).listWorkspacesForUser(userID); will(returnValue(expectedList));
        }});
        
        Collection<Workspace> result = service.listUserWorkspaces(userID);
        
        assertEquals("Retrieved list different from expected", expectedList, result);
    }
    
    @Test
    public void openExistingWorkspace()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
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
    public void openInaccessibleWorkspace()
            throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException, WorkspaceAccessException, IOException {
        
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
    public void submitWorkspaceNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException {
        final int workspaceID = 1;
        final String userID = "testUser";
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.submitWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException {
        final int workspaceID = 1;
        final String userID = "testUser";
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(throwException(expectedException));
        }});
        
        try {
            service.submitWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void submitWorkspaceFails() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException {
        final int workspaceID = 1;
        final String userID = "testUser";
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).submitWorkspace(workspaceID/*, keepUnlinkedFiles*/); will(throwException(expectedException));
        }});
        
        try {
            service.submitWorkspace(userID, workspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
        
    @Test
    public void submitWorkspaceSuccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceExportException {
        final int workspaceID = 1;
        final String userID = "testUser";
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceManager).submitWorkspace(workspaceID/*, keepUnlinkedFiles*/);
        }});
        
        service.submitWorkspace(userID, workspaceID);
    }
    
    
    @Test
    public void getExistingNode() throws URISyntaxException, WorkspaceNodeNotFoundException {
        
        final int nodeID = 1;
        final int workspaceID = 1;
        URI profileSchemaURI = null;
        String name = "node_name";
        String title = "node_title";
        WorkspaceNodeType type = WorkspaceNodeType.METADATA;
        URL wsURL = null;
        URI archiveURI = null;
        URL archiveURL = null;
        URL originURL = null;
        WorkspaceNodeStatus status = WorkspaceNodeStatus.NODE_ISCOPY;
        String format = "cmdi";
        final WorkspaceNode nodeToRetrieve = new LamusWorkspaceNode(nodeID, workspaceID, profileSchemaURI, name, title, type, wsURL, archiveURI, archiveURL, originURL, status, format);
        
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
        final int workspaceID = 1;
        URI profileSchemaURI = null;
        String name = "node_name";
        String title = "node_title";
        WorkspaceNodeType type = WorkspaceNodeType.METADATA;
        URL wsURL = null;
        URI archiveURI = null;
        URL archiveURL = null;
        URL originURL = null;
        WorkspaceNodeStatus status = WorkspaceNodeStatus.NODE_ISCOPY;
        String format = "cmdi";
        final WorkspaceNode nodeToRetrieve = new LamusWorkspaceNode(nodeID, workspaceID, profileSchemaURI, name, title, type, wsURL, archiveURI, archiveURL, originURL, status, format);
        
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
        final Collection<WorkspaceNode> expectedChildNodes = new ArrayList<WorkspaceNode>();
        final WorkspaceNode childNode = new LamusWorkspaceNode(2, 1, null, "name", "title", WorkspaceNodeType.RESOURCE, null, null, null, null, WorkspaceNodeStatus.NODE_VIRTUAL, "jpeg");
        expectedChildNodes.add(childNode);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(expectedChildNodes));
        }});
        
        Collection<WorkspaceNode> retrievedChildNodes = service.getChildNodes(nodeID);
        assertNotNull("Returned list of nodes should not be null", retrievedChildNodes);
        assertEquals("Returned list of nodes is different from expected", expectedChildNodes, retrievedChildNodes);
    }
    
//    @Test
//    public void uploadFileIntoWorkspaceWithAccess() {
//        
//        final int workspaceID = 1;
//        final String userID = "testUser";
//        
//        final Collection<FileItem> fileItems = new ArrayList<FileItem>();
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
//            oneOf(mockWorkspaceUploader).uploadFiles(workspaceID, fileItems);
//        }});
//        
//        service.uploadFilesIntoWorkspace(userID, workspaceID, fileItems);
//    }
    
    //TODO uploadFileIntoWorkspaceWithoutAccess
    
    @Test
    public void getWorkspaceUploadDirectory() {
        
        final int workspaceID = 1;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).getWorkspaceUploadDirectory(workspaceID);
                will(returnValue(mockWorkspaceUploadDirectory));
        }});
        
        File result = service.getWorkspaceUploadDirectory(workspaceID);
        
        assertEquals("Retrieved directory different from expected", mockWorkspaceUploadDirectory, result);
    }
    
    @Test
    public void uploadFileIntoWorkspace() throws IOException, TypeCheckerException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        final String filename = "someFile.cmdi";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
        }});
        
        service.uploadFileIntoWorkspace(userID, workspaceID, mockInputStream, filename);
    }
    
    @Test
    public void uploadFileIntoWorkspaceThrowsIOException() throws IOException, TypeCheckerException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        final String filename = "someFile.cmdi";
        final IOException ioException = new IOException("some error message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
                will(throwException(ioException));
        }});
        
        try {
            service.uploadFileIntoWorkspace(userID, workspaceID, mockInputStream, filename);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            assertEquals("Exception thrown different frome expected", ioException, ex);
        }
    }
    
    @Test
    public void uploadFileIntoWorkspaceThrowsTypeCheckerException() throws IOException, TypeCheckerException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        final String filename = "someFile.cmdi";
        final TypeCheckerException typeCheckerException = new TypeCheckerException("some error message", null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
                will(throwException(typeCheckerException));
        }});
        
        try {
            service.uploadFileIntoWorkspace(userID, workspaceID, mockInputStream, filename);
            fail("An exception should have been thrown");
        } catch(TypeCheckerException ex) {
            assertEquals("Exception thrown different from expected", typeCheckerException, ex);
        }
    }
    
    @Test
    public void uploadFileIntoWorkspaceThrowsWorkspaceException() throws IOException, TypeCheckerException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        final String filename = "someFile.cmdi";
        final WorkspaceException workspaceException = new WorkspaceException("some error message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceUploader).uploadFileIntoWorkspace(workspaceID, mockInputStream, filename);
                will(throwException(workspaceException));
        }});
        
        try {
            service.uploadFileIntoWorkspace(userID, workspaceID, mockInputStream, filename);
            fail("An exception should have been thrown");
        } catch(WorkspaceException ex) {
            assertEquals("Exception thrown different from expected", workspaceException, ex);
        }
    }
    
    @Test
    public void addNodeWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void addNodeNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void addNodeSuccessfully() throws WorkspaceNotFoundException, WorkspaceAccessException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceDao).addWorkspaceNode(mockChildNode);
        }});
        
        service.addNode(userID, mockChildNode);
    }
    
    @Test
    public void linkNodesWithAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode);
        }});
        
        service.linkNodes(userID, mockParentNode, mockChildNode);
    }
    
    @Test
    public void linkNodesWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void linkNodesNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void linkNodesWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void unlinkNodesWithAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockParentNode, mockChildNode);
        }});
        
        service.unlinkNodes(userID, mockParentNode, mockChildNode);
    }
    
    @Test
    public void unlinkNodesWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void unlinkNodesNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void unlinkNodesWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void deleteNodeWithAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final int nodeID = 4;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockChildNode);
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID);
        }});
        
        service.deleteNode(userID, mockChildNode);
    }
    
    @Test
    public void deleteNodeWorkspaceNotFound() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void deleteNodeNoAccess() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
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
    public void deleteNodeWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final int workspaceID = 1;
        final int nodeID = 4;
        final String userID = "testUser";
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).ensureUserHasAccessToWorkspace(userID, workspaceID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            service.deleteNode(userID, mockChildNode);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void listUnlinkedNodes() {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).listUnlinkedNodes(workspaceID); will(returnValue(mockUnlinkedNodesList));
        }});
        
        service.listUnlinkedNodes(userID, workspaceID);
    }

}
