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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceService;
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
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private WorkspaceService service;
    @Mock private WorkspaceAccessChecker mockNodeAccessChecker;
    @Mock private WorkspaceManager mockWorkspaceManager;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceUploader mockWorkspaceUploader;
    @Mock private WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    
    @Mock private WorkspaceNode mockParentNode;
    @Mock private WorkspaceNode mockChildNode;
    
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

    /**
     * 
     */
    @Test
    public void createWorkspaceNoAccess() throws MalformedURLException {
        
        final int archiveNodeID = 10;
        final String userID = "someUser";
        
        context.checking(new Expectations() {{
            oneOf (mockNodeAccessChecker).canCreateWorkspace(userID, archiveNodeID); will(returnValue(Boolean.FALSE));
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeID);
        assertNull("Returned workspace should be null when it cannot be created.", result);
    }
    
    /**
     * 
     */
    @Test
    public void createWorkspaceSuccess() throws MalformedURLException {
        
        final int archiveNodeID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        context.checking(new Expectations() {{
            oneOf (mockNodeAccessChecker).canCreateWorkspace(userID, archiveNodeID); will(returnValue(Boolean.TRUE));
            //allow other calls
            oneOf (mockWorkspaceManager).createWorkspace(userID, archiveNodeID); will(returnValue(newWorkspace));
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeID);
        assertNotNull("Returned workspace should not be null when it can be created", result);
        assertEquals("Returned workspace is different from expected", result, newWorkspace);
    }
    
    @Test
    public void deleteExistingWorkspace() {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceManager).deleteWorkspace(workspaceID);
        }});
        
        service.deleteWorkspace(userID, workspaceID);
    }
    
    @Test
    public void getExistingWorkspace() throws MalformedURLException {
        
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final int topNodeArchiveID = 2;
        final URL topNodeArchiveURL = new URL("http://some/url/node.cmdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveID, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = service.getWorkspace(workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
    }
    
    @Test
    public void listUserWorkspaces() throws MalformedURLException {
        
        final String userID = "someUser";
        final Date date = Calendar.getInstance().getTime();
        Workspace workspace1 = new LamusWorkspace(1, userID, 10, 0, new URL("http://some/url/node.cmdi"),
                date, null, date, null, 0L, 10000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        Workspace workspace2 = new LamusWorkspace(2, userID, 11, 1, new URL("http://someother/url/node.cmdi"),
                date, null, date, null, 0L, 1000000L, WorkspaceStatus.INITIALISED, "workspace is in good shape", "still not sure what this would be");
        final Collection<Workspace> expectedList = new ArrayList<Workspace>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).listWorkspacesForUser(userID); will(returnValue(expectedList));
        }});
        
        Collection<Workspace> result = service.listUserWorkspaces(userID);
        
        assertEquals("Retrieved list differenc from expected", expectedList, result);
    }
    
    @Test
    public void openExistingWorkspace() throws MalformedURLException {
        
        final int workspaceID = 1;
        final String userID = "someUser";
        final int topNodeID = 1;
        final int topNodeArchiveID = 2;
        final URL topNodeArchiveURL = new URL("http://some/url/node.cmdi");
        final Date startDate = Calendar.getInstance().getTime();
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final WorkspaceStatus status = WorkspaceStatus.INITIALISED;
        final String message = "workspace is in good shape";
        final String archiveInfo = "still not sure what this would be";
        final Workspace workspaceToRetrieve = new LamusWorkspace(workspaceID, userID, topNodeID, topNodeArchiveID, topNodeArchiveURL,
                startDate, null, startDate, null, usedStorageSpace, maxStorageSpace, status, message, archiveInfo);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceManager).openWorkspace(userID, workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = service.openWorkspace(userID, workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
    }
    
    @Test
    public void submitWorkspaceNoAccess() {
        final int workspaceID = 1;
        final String userID = "testUser";
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = service.submitWorkspace(userID, workspaceID/*, keepUnlinkedFiles*/);
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void submitWorkspaceFail() {
        final int workspaceID = 1;
        final String userID = "testUser";
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceManager).submitWorkspace(workspaceID/*, keepUnlinkedFiles*/); will(returnValue(Boolean.FALSE));
        }});
        
        boolean result = service.submitWorkspace(userID, workspaceID/*, keepUnlinkedFiles*/);
        assertFalse("Result should be false", result);
    }
        
    @Test
    public void submitWorkspaceSuccess() {
        final int workspaceID = 1;
        final String userID = "testUser";
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceManager).submitWorkspace(workspaceID/*, keepUnlinkedFiles*/); will(returnValue(Boolean.TRUE));
        }});
        
        boolean result = service.submitWorkspace(userID, workspaceID/*, keepUnlinkedFiles*/);
        assertTrue("Result should be true", result);
    }
    
    
    @Test
    public void getExistingNode() throws URISyntaxException {
        
        final int nodeID = 1;
        final int workspaceID = 1;
        final int archiveNodeID = 10;
        URI profileSchemaURI = null;
        String name = "node_name";
        String title = "node_title";
        WorkspaceNodeType type = WorkspaceNodeType.METADATA;
        URL wsURL = null;
        URL archiveURL = null;
        URL originURL = null;
        WorkspaceNodeStatus status = WorkspaceNodeStatus.NODE_ISCOPY;
        String pid = UUID.randomUUID().toString();
        String format = "cmdi";
        final WorkspaceNode nodeToRetrieve = new LamusWorkspaceNode(nodeID, workspaceID, archiveNodeID, profileSchemaURI, name, title, type, wsURL, archiveURL, originURL, status, pid, format);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(returnValue(nodeToRetrieve));
        }});
        
        WorkspaceNode result = service.getNode(nodeID);
        assertNotNull("Returned node should not be null", result);
        assertEquals("Returned node is different from expected", result, nodeToRetrieve);
    }
    
    @Test
    public void getExistingChildNodes() {
        
        final int nodeID = 1;
        final Collection<WorkspaceNode> expectedChildNodes = new ArrayList<WorkspaceNode>();
        final WorkspaceNode childNode = new LamusWorkspaceNode(2, 1, 20, null, "name", "title", WorkspaceNodeType.RESOURCE_MR, null, null, null, WorkspaceNodeStatus.NODE_VIRTUAL, "pid", "jpeg");
        expectedChildNodes.add(childNode);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(expectedChildNodes));
        }});
        
        Collection<WorkspaceNode> retrievedChildNodes = service.getChildNodes(nodeID);
        assertNotNull("Returned list of nodes should not be null", retrievedChildNodes);
        assertEquals("Returned list of nodes is different from expected", expectedChildNodes, retrievedChildNodes);
    }
    
    @Test
    public void uploadFileIntoWorkspaceWithAccess() {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        final Collection<FileItem> fileItems = new ArrayList<FileItem>();
        
        context.checking(new Expectations() {{
            
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceUploader).uploadFiles(workspaceID, fileItems);
        }});
        
        service.uploadFilesIntoWorkspace(userID, workspaceID, fileItems);
    }
    
    //TODO uploadFileIntoWorkspaceWithoutAccess
    
    @Test
    public void linkNodesWithAccess() {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceNodeLinkManager).linkNodes(mockParentNode, mockChildNode);
        }});
        
        service.linkNodes(userID, mockParentNode, mockChildNode);
    }
    
    //TODO linkNodesWithoutAccess
    
    @Test
    public void unlinkNodesWithAccess() {
        
        final int workspaceID = 1;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockParentNode, mockChildNode);
        }});
        
        service.unlinkNodes(userID, mockParentNode, mockChildNode);
    }
    
    //TODO unlinkNodesWithoutAccess
    
    @Test
    public void deleteNodeWithAccess() {
        
        final int workspaceID = 1;
        final int nodeID = 4;
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNodeAccessChecker).hasAccessToWorkspace(userID, workspaceID); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockChildNode);
            oneOf(mockChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockChildNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID);
        }});
        
        service.deleteNode(userID, mockChildNode);
    }
    
    //TODO deleteNodeWithoutAccess

}
