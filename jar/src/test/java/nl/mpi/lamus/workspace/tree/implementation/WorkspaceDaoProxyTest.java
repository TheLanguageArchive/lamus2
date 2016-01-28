/*
 * Copyright (C) 2016 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.tree.implementation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceReplacedNodeUrlUpdate;
import nl.mpi.lamus.workspace.tree.WorkspaceDaoFactory;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class WorkspaceDaoProxyTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock WorkspaceDaoFactory mockWorkspaceDaoFactory;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    @Mock Workspace mockWorkspace;
    @Mock List<Workspace> mockWorkspaceCollection;
    @Mock WorkspaceNode mockWorkspaceNode;
    @Mock WorkspaceNode mockOtherWorkspaceNode;
    @Mock List<WorkspaceNode> mockWorkspaceNodeCollection;
    @Mock WorkspaceNodeLink mockWorkspaceNodeLink;
    @Mock List<WorkspaceNodeLink> mockWorkspaceNodeLinkCollection;
    @Mock List<WorkspaceNodeReplacement> mockWorkspaceNodeReplacementCollection;
    @Mock List<WorkspaceReplacedNodeUrlUpdate> mockWorkspaceReplacedNodeUrlUpdateCollection;
    
    private WorkspaceDaoProxy workspaceDaoProxy;
    
    
    public WorkspaceDaoProxyTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        workspaceDaoProxy = new WorkspaceDaoProxy();
        
        ReflectionTestUtils.setField(workspaceDaoProxy, "workspaceDaoFactory", mockWorkspaceDaoFactory);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void testAddWorkspace() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).addWorkspace(mockWorkspace);
        }});
        workspaceDaoProxy.addWorkspace(mockWorkspace);
    }

    @Test
    public void testDeleteWorkspace() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).deleteWorkspace(mockWorkspace);
        }});
        workspaceDaoProxy.deleteWorkspace(mockWorkspace);
    }

    @Test
    public void testUpdateWorkspaceTopNode() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateWorkspaceTopNode(mockWorkspace);
        }});
        workspaceDaoProxy.updateWorkspaceTopNode(mockWorkspace);
    }

    @Test
    public void testUpdateWorkspaceSessionDates() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateWorkspaceSessionDates(mockWorkspace);
        }});
        workspaceDaoProxy.updateWorkspaceSessionDates(mockWorkspace);
    }

    @Test
    public void testUpdateWorkspaceEndDates() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateWorkspaceEndDates(mockWorkspace);
        }});
        workspaceDaoProxy.updateWorkspaceEndDates(mockWorkspace);
    }

    @Test
    public void testUpdateWorkspaceStorageSpace() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateWorkspaceStorageSpace(mockWorkspace);
        }});
        workspaceDaoProxy.updateWorkspaceStorageSpace(mockWorkspace);
    }

    @Test
    public void testUpdateWorkspaceCrawlerID() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateWorkspaceCrawlerID(mockWorkspace);
        }});
        workspaceDaoProxy.updateWorkspaceCrawlerID(mockWorkspace);
    }

    @Test
    public void testUpdateWorkspaceStatusMessage() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
        }});
        workspaceDaoProxy.updateWorkspaceStatusMessage(mockWorkspace);
    }

    @Test
    public void testGetWorkspace() throws Exception {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockWorkspace));
        }});
        assertEquals(mockWorkspace, workspaceDaoProxy.getWorkspace(workspaceID));
    }

    @Test
    public void testGetWorkspacesForUser() {
        final String userID = "testuser";
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspacesForUser(userID); will(returnValue(mockWorkspaceCollection));
        }});
        assertEquals(mockWorkspaceCollection, workspaceDaoProxy.getWorkspacesForUser(userID));
    }

    @Test
    public void testGetWorkspacesInFinalStage() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(mockWorkspaceCollection));
        }});
        assertEquals(mockWorkspaceCollection, workspaceDaoProxy.getWorkspacesInFinalStage());
    }

    @Test
    public void testGetAllWorkspaces() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspacesInFinalStage(); will(returnValue(mockWorkspaceCollection));
        }});
        assertEquals(mockWorkspaceCollection, workspaceDaoProxy.getWorkspacesInFinalStage());
    }

    @Test
    public void testPreLockNode() {
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).preLockNode(nodeURI);
        }});
        workspaceDaoProxy.preLockNode(nodeURI);
    }

    @Test
    public void testRemoveNodePreLock() {
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).removeNodePreLock(nodeURI);
        }});
        workspaceDaoProxy.removeNodePreLock(nodeURI);
    }

    @Test
    public void testIsAnyOfNodesPreLocked() {
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final List<String> nodeURIs = new ArrayList<>();
        nodeURIs.add(nodeURI.toString());
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).isAnyOfNodesPreLocked(nodeURIs); will(returnValue(Boolean.TRUE));
        }});
        assertTrue(workspaceDaoProxy.isAnyOfNodesPreLocked(nodeURIs));
    }

    @Test
    public void testIsNodeLocked() {
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).isNodeLocked(nodeURI); will(returnValue(Boolean.TRUE));
        }});
        assertTrue(workspaceDaoProxy.isNodeLocked(nodeURI));
    }

    @Test
    public void testLockNode() {
        final int workspaceID = 10;
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).lockNode(nodeURI, workspaceID);
        }});
        workspaceDaoProxy.lockNode(nodeURI, workspaceID);
    }

    @Test
    public void testUnlockNode() {
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).unlockNode(nodeURI);
        }});
        workspaceDaoProxy.unlockNode(nodeURI);
    }

    @Test
    public void testUnlockAllNodesOfWorkspace() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).unlockAllNodesOfWorkspace(workspaceID);
        }});
        workspaceDaoProxy.unlockAllNodesOfWorkspace(workspaceID);
    }

    @Test
    public void testGetWorkspaceNodeByArchiveURI() {
        final URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspaceNodeByArchiveURI(nodeURI); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getWorkspaceNodeByArchiveURI(nodeURI));
    }

    @Test
    public void testAddWorkspaceNode() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).addWorkspaceNode(mockWorkspaceNode);
        }});
        workspaceDaoProxy.addWorkspaceNode(mockWorkspaceNode);
    }

    @Test
    public void testSetWorkspaceNodeAsDeleted() {
        final int workspaceID = 10;
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID, Boolean.FALSE);
        }});
        workspaceDaoProxy.setWorkspaceNodeAsDeleted(workspaceID, nodeID, Boolean.FALSE);
    }

    @Test
    public void testDeleteWorkspaceNode() {
        final int workspaceID = 10;
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).deleteWorkspaceNode(workspaceID, nodeID);
        }});
        workspaceDaoProxy.deleteWorkspaceNode(workspaceID, nodeID);
    }

    @Test
    public void testGetWorkspaceNode() throws Exception {
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspaceNode(nodeID); will(returnValue(mockWorkspaceNode));
        }});
        assertEquals(mockWorkspaceNode, workspaceDaoProxy.getWorkspaceNode(nodeID));
    }

    @Test
    public void testGetWorkspaceTopNode() throws Exception {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspaceTopNode(workspaceID); will(returnValue(mockWorkspaceNode));
        }});
        assertEquals(mockWorkspaceNode, workspaceDaoProxy.getWorkspaceTopNode(workspaceID));
    }

    @Test
    public void testGetWorkspaceTopNodeID() {
        final int workspaceID = 10;
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getWorkspaceTopNodeID(workspaceID); will(returnValue(nodeID));
        }});
        assertEquals(nodeID, workspaceDaoProxy.getWorkspaceTopNodeID(workspaceID));
    }

    @Test
    public void testIsTopNodeOfWorkspace() {
        final int workspaceID = 10;
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, nodeID); will(returnValue(Boolean.TRUE));
        }});
        assertTrue(workspaceDaoProxy.isTopNodeOfWorkspace(workspaceID, nodeID));
    }

    @Test
    public void testGetNodesForWorkspace() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getNodesForWorkspace(workspaceID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getNodesForWorkspace(workspaceID));
    }

    @Test
    public void testGetMetadataNodesInTreeForWorkspace() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getMetadataNodesInTreeForWorkspace(workspaceID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getMetadataNodesInTreeForWorkspace(workspaceID));
    }

    @Test
    public void testGetChildWorkspaceNodes() {
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getChildWorkspaceNodes(nodeID));
    }

    @Test
    public void testGetDescendantWorkspaceNodes() {
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getDescendantWorkspaceNodes(nodeID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getDescendantWorkspaceNodes(nodeID));
    }

    @Test
    public void testGetDescendantWorkspaceNodesByType() {
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getDescendantWorkspaceNodesByType(nodeID, WorkspaceNodeType.RESOURCE_AUDIO); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getDescendantWorkspaceNodesByType(nodeID, WorkspaceNodeType.RESOURCE_AUDIO));
    }

    @Test
    public void testGetParentWorkspaceNodes() {
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getParentWorkspaceNodes(nodeID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getParentWorkspaceNodes(nodeID));
    }

    @Test
    public void testGetUnlinkedAndDeletedTopNodes() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getUnlinkedAndDeletedTopNodes(workspaceID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getUnlinkedAndDeletedTopNodes(workspaceID));
    }

    @Test
    public void testGetUnlinkedNodes() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getUnlinkedNodes(workspaceID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getUnlinkedNodes(workspaceID));
    }

    @Test
    public void testGetUnlinkedNodesAndDescendants() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getUnlinkedNodesAndDescendants(workspaceID); will(returnValue(mockWorkspaceNodeCollection));
        }});
        assertEquals(mockWorkspaceNodeCollection, workspaceDaoProxy.getUnlinkedNodesAndDescendants(workspaceID));
    }

    @Test
    public void testUpdateNodeWorkspaceURL() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateNodeWorkspaceURL(mockWorkspaceNode);
        }});
        workspaceDaoProxy.updateNodeWorkspaceURL(mockWorkspaceNode);
    }

    @Test
    public void testUpdateNodeArchiveUri() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockWorkspaceNode);
        }});
        workspaceDaoProxy.updateNodeArchiveUri(mockWorkspaceNode);
    }

    @Test
    public void testUpdateNodeArchiveUrl() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockWorkspaceNode);
        }});
        workspaceDaoProxy.updateNodeArchiveUrl(mockWorkspaceNode);
    }

    @Test
    public void testUpdateNodeType() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).updateNodeType(mockWorkspaceNode);
        }});
        workspaceDaoProxy.updateNodeType(mockWorkspaceNode);
    }

    @Test
    public void testAddWorkspaceNodeLink() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).addWorkspaceNodeLink(mockWorkspaceNodeLink);
        }});
        workspaceDaoProxy.addWorkspaceNodeLink(mockWorkspaceNodeLink);
    }

    @Test
    public void testDeleteWorkspaceNodeLink() {
        final int workspaceID = 10;
        final int parentNodeID = 100;
        final int childNodeID = 200;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
        }});
        workspaceDaoProxy.deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
    }

    @Test
    public void testCleanWorkspaceNodesAndLinks() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).cleanWorkspaceNodesAndLinks(mockWorkspace);
        }});
        workspaceDaoProxy.cleanWorkspaceNodesAndLinks(mockWorkspace);
    }

    @Test
    public void testGetOlderVersionOfNode() throws Exception {
        final int workspaceID = 10;
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getOlderVersionOfNode(workspaceID, nodeID); will(returnValue(mockWorkspaceNode));
        }});
        assertEquals(mockWorkspaceNode, workspaceDaoProxy.getOlderVersionOfNode(workspaceID, nodeID));
    }

    @Test
    public void testGetNewerVersionOfNode() throws Exception {
        final int workspaceID = 10;
        final int nodeID = 100;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getNewerVersionOfNode(workspaceID, nodeID); will(returnValue(mockWorkspaceNode));
        }});
        assertEquals(mockWorkspaceNode, workspaceDaoProxy.getNewerVersionOfNode(workspaceID, nodeID));
    }

    @Test
    public void testReplaceNode() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).replaceNode(mockWorkspaceNode, mockOtherWorkspaceNode);
        }});
        workspaceDaoProxy.replaceNode(mockWorkspaceNode, mockOtherWorkspaceNode);
    }

    @Test
    public void testGetAllNodeReplacements() {
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getAllNodeReplacements(); will(returnValue(mockWorkspaceNodeReplacementCollection));
        }});
        assertEquals(mockWorkspaceNodeReplacementCollection, workspaceDaoProxy.getAllNodeReplacements());
    }

    @Test
    public void testGetNodeReplacementsForWorkspace() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getNodeReplacementsForWorkspace(workspaceID); will(returnValue(mockWorkspaceNodeReplacementCollection));
        }});
        assertEquals(mockWorkspaceNodeReplacementCollection, workspaceDaoProxy.getNodeReplacementsForWorkspace(workspaceID));
    }

    @Test
    public void testGetReplacedNodeUrlsToUpdateForWorkspace() {
        final int workspaceID = 10;
        allowCallToDaoFactory();
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceDao).getReplacedNodeUrlsToUpdateForWorkspace(workspaceID); will(returnValue(mockWorkspaceReplacedNodeUrlUpdateCollection));
        }});
        assertEquals(mockWorkspaceReplacedNodeUrlUpdateCollection, workspaceDaoProxy.getReplacedNodeUrlsToUpdateForWorkspace(workspaceID));
    }
    
    
    private void allowCallToDaoFactory() {
        context.checking(new Expectations() {{
            allowing(mockWorkspaceDaoFactory).createWorkspaceDao(); will(returnValue(mockWorkspaceDao));
        }});
    }
}
