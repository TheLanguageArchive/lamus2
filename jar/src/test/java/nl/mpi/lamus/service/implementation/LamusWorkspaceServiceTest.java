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

import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.management.NodeAccessChecker;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
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
    @Mock private NodeAccessChecker mockNodeAccessChecker;
    @Mock private WorkspaceManager mockWorkspaceManager;
    @Mock private WorkspaceDao mockWorkspaceDao;
    
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
        service = new LamusWorkspaceService(mockNodeAccessChecker, mockWorkspaceManager, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * 
     */
    @Test
    public void returnNullWorkspaceIfCannotBeCreated() throws MalformedURLException {
        
        final int archiveNodeID = 10;
        final String userID = "someUser";
        
        context.checking(new Expectations() {{
            oneOf (mockNodeAccessChecker).canCreateWorkspace(userID, archiveNodeID); will(returnValue(false));
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeID);
        assertNull("Returned workspace should be null when it cannot be created.", result);
    }
    
    /**
     * 
     */
    @Test
    public void triggersWorkspaceCreationIfCanBeCreated() throws MalformedURLException {
        
        final int archiveNodeID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        context.checking(new Expectations() {{
            oneOf (mockNodeAccessChecker).canCreateWorkspace(userID, archiveNodeID); will(returnValue(true));
            //allow other calls
            oneOf (mockWorkspaceManager).createWorkspace(userID, archiveNodeID); will(returnValue(newWorkspace));
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeID);
        assertNotNull("Returned workspace should not be null when it can be created", result);
        assertEquals("Returned workspace is different from expected", result, newWorkspace);
                
    }
    
    @Test
    public void getExistingWorkspace() throws MalformedURLException {
        
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
        }});
        
        Workspace result = service.getWorkspace(workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
    }
    
    @Test
    public void openExistingWorkspace() throws MalformedURLException {
        
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
            
            oneOf(mockWorkspaceManager).openWorkspace(userID, workspaceID); will(returnValue(workspaceToRetrieve));
        }});
        
        Workspace result = service.openWorkspace(userID, workspaceID);
        assertNotNull("Returned workspace should not be null", result);
        assertEquals("Returned workspace is different from expected", result, workspaceToRetrieve);
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
        String pid = "some:fake-pid";
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
}
