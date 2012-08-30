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

import java.net.URI;
import java.net.URL;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.tree.implementation.LamusWorkspaceTreeNode;
import nl.mpi.lamus.workspace.management.NodeAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTreeServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private WorkspaceTreeService service;
    @Mock private NodeAccessChecker mockNodeAccessChecker;
    @Mock private WorkspaceManager mockWorkspaceManager;
    @Mock private WorkspaceDao mockWorkspaceDao;
    
    public LamusWorkspaceTreeServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        service = new LamusWorkspaceTreeService(mockNodeAccessChecker, mockWorkspaceManager, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getTreeNode method, of class LamusWorkspaceTreeService.
     */
    @Test
    public void testGetTreeNodeWithoutParent() {

        final int nodeID = 1;
        final int workspaceID = 1;
        int archiveNodeID = 10;
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
        
        final WorkspaceNode node = new LamusWorkspaceNode(
                nodeID, workspaceID, archiveNodeID, profileSchemaURI,
                name, title, type, wsURL, archiveURL, originURL,
                status, pid, format);
        
        final WorkspaceTreeNode treeNodeToRetrieve = new LamusWorkspaceTreeNode(
                node, null, mockWorkspaceDao);
        
        context.checking(new Expectations() {{
            
//            oneOf(mockWorkspaceDao).getWorkspaceTreeNode(nodeID, null); will(returnValue(treeNodeToRetrieve));
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(returnValue(node));
        }});
        
        WorkspaceTreeNode result = service.getTreeNode(nodeID, null);
        assertNotNull("Returned tree node should not be null", result);
        assertEquals("Returned tree node is different from expected", result, treeNodeToRetrieve);
        assertNull("Returned tree node should have a null parent tree node.", result.getParent());
    }
    
    /**
     * Test of getTreeNode method, of class LamusWorkspaceTreeService.
     */
    @Test
    public void testGetTreeNodeWithParent() {

        final int parentNodeID = 0;
        final int workspaceID = 1;
        final int parentArchiveNodeID = 5;
        String parentName = "parent_name";
        String parentTitle = "parent_title";
        String parentPid = "some:parent-fake-pid";
                
        final int nodeID = 1;
        int archiveNodeID = 10;
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

        final WorkspaceTreeNode parentTreeNode = new LamusWorkspaceTreeNode(
                parentNodeID, workspaceID, parentArchiveNodeID, profileSchemaURI,
                parentName, parentTitle, type, originURL, archiveURL, originURL,
                status, pid, format, null, mockWorkspaceDao);
        
        final WorkspaceNode node = new LamusWorkspaceNode(
                nodeID, workspaceID, archiveNodeID, profileSchemaURI,
                name, title, type, wsURL, archiveURL, originURL,
                status, pid, format);
        
        final WorkspaceTreeNode treeNodeToRetrieve = new LamusWorkspaceTreeNode(
                node, parentTreeNode, mockWorkspaceDao);
        
        context.checking(new Expectations() {{
            
//            oneOf(mockWorkspaceDao).getWorkspaceTreeNode(nodeID, parentTreeNode); will(returnValue(treeNodeToRetrieve));
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(returnValue(node));
        }});
        
        WorkspaceTreeNode result = service.getTreeNode(nodeID, parentTreeNode);
        assertNotNull("Returned tree node should not be null", result);
        assertEquals("Returned tree node is different from expected", treeNodeToRetrieve, result);
        assertNotNull("Returned tree node should have a null parent tree node.", result.getParent());
        assertEquals("The parent tree node of the returned tree node is different from expected", parentTreeNode, result.getParent());
    }
}
