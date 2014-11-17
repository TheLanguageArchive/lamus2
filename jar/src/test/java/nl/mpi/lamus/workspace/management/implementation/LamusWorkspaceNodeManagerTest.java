/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.management.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;

/**
 *
 * @author guisil
 */
public class LamusWorkspaceNodeManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceNodeManager workspaceNodeManager;
    
    @Mock private WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    @Mock private WorkspaceDao mockWorkspaceDao;
    
    @Mock private WorkspaceNode mockNode;
    @Mock private WorkspaceNode mockOneChildNode;
    @Mock private WorkspaceNode mockAnotherChildNode;
    @Mock private WorkspaceNode mockOneChildOneChildNode;
    @Mock private WorkspaceNode mockOneChildAnotherChildNode;
    
    public LamusWorkspaceNodeManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspaceNodeManager = new LamusWorkspaceNodeManager(mockWorkspaceNodeLinkManager, mockWorkspaceDao);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void deleteNodeWithArchiveUriWithoutChildren() throws WorkspaceException, URISyntaxException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final int nodeID = 101;
        final boolean isExternal = Boolean.FALSE;
        final boolean isProtected = Boolean.FALSE;
        
        final Collection<WorkspaceNode> childNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            //node not protected - delete recursively
            oneOf(mockNode).isProtected(); will(returnValue(isProtected));
            
            //no children
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(childNodes));
            
            oneOf(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockNode).isExternal(); will(returnValue(isExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID, isExternal);
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockNode);
        }});
        
        workspaceNodeManager.deleteNodesRecursively(mockNode);
    }
    
    @Test
    public void deleteNodeWitOneChild() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final int nodeID = 101;
        final boolean isNodeExternal = Boolean.FALSE;
        final boolean isNodeProtected = Boolean.FALSE;
        final int childNodeID = 102;
        final boolean isChildNodeExternal = Boolean.FALSE;
        final boolean isChildNodeProtected = Boolean.FALSE;
        
        final Collection<WorkspaceNode> childNodes = new ArrayList<>();
        childNodes.add(mockOneChildNode);
        final Collection<WorkspaceNode> childChildNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            //node not protected - delete recursively
            oneOf(mockNode).isProtected(); will(returnValue(isNodeProtected));
            
            //one child
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(childNodes));

            //child not protected - delete recursively
            oneOf(mockOneChildNode).isProtected(); will(returnValue(isChildNodeProtected));
            //recursive call
            //no children
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(childNodeID); will(returnValue(childChildNodes));
            //child is marked as deleted
            oneOf(mockOneChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockOneChildNode).isExternal(); will(returnValue(isChildNodeExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, childNodeID, isChildNodeExternal);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildNode);

            //back to the top node, which will be marked as deleted too
            oneOf(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockNode).isExternal(); will(returnValue(isNodeExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID, isNodeExternal);
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockNode);
        }});
        
        workspaceNodeManager.deleteNodesRecursively(mockNode);
    }
    
    @Test
    public void deleteNodeWithChildren() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 10;
        final int nodeID = 101;
        final boolean isNodeExternal = Boolean.FALSE;
        final boolean isNodeProtected = Boolean.FALSE;
        final int oneChildNodeID = 102;
        final boolean isOneChildExternal = Boolean.FALSE;
        final boolean isOneChildProtected = Boolean.FALSE;
        final int anotherChildNodeID = 103;
        final boolean isAnotherChildExternal = Boolean.FALSE;
        final boolean isAnotherChildProtected = Boolean.FALSE;
        final int oneChildOneChildNodeID = 104;
        final boolean isOneChildOneChildExternal = Boolean.FALSE;
        final boolean isOneChildOneChildProtected = Boolean.FALSE;
        final int oneChildAnotherChildNodeID = 105;
        final boolean isOneChildAnotherChildExternal = Boolean.FALSE;
        final boolean isOneChildAnotherChildProtected = Boolean.FALSE;
        
        final Collection<WorkspaceNode> childNodes = new ArrayList<>();
        childNodes.add(mockOneChildNode);
        childNodes.add(mockAnotherChildNode);
        final Collection<WorkspaceNode> oneChildChildNodes = new ArrayList<>();
        oneChildChildNodes.add(mockOneChildOneChildNode);
        oneChildChildNodes.add(mockOneChildAnotherChildNode);
        final Collection<WorkspaceNode> oneChildOneChildChildNodes = new ArrayList<>();
        final Collection<WorkspaceNode> oneChildAnotherChildChildNodes = new ArrayList<>();
        final Collection<WorkspaceNode> anotherChildChildNodes = new ArrayList<>();
        
        context.checking(new Expectations() {{
            
            //node not protected - delete recursively
            oneOf(mockNode).isProtected(); will(returnValue(isNodeProtected));
            
            //two children
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(childNodes));
            
            //first child not protected - delete recursively
            oneOf(mockOneChildNode).isProtected(); will(returnValue(isOneChildProtected));
            //recursive call - first child
            //two children
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oneChildNodeID); will(returnValue(oneChildChildNodes));
            
            //first child - first child not protected - delete recursively
            oneOf(mockOneChildOneChildNode).isProtected(); will(returnValue(isOneChildOneChildProtected));
            //another recursive call - first child - first child
            //no children
            oneOf(mockOneChildOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildOneChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oneChildOneChildNodeID); will(returnValue(oneChildOneChildChildNodes));
            //child first child marked as deleted
            oneOf(mockOneChildOneChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildOneChildNodeID));
            oneOf(mockOneChildOneChildNode).isExternal(); will(returnValue(isOneChildOneChildExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, oneChildOneChildNodeID, isOneChildOneChildExternal);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildOneChildNode);
            
            //first child - second child not protected - delete recursively
            oneOf(mockOneChildAnotherChildNode).isProtected(); will(returnValue(isOneChildAnotherChildProtected));
            //another recursive call - first child - second child
            //no children
            oneOf(mockOneChildAnotherChildNode).getWorkspaceNodeID(); will(returnValue(oneChildAnotherChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oneChildAnotherChildNodeID); will(returnValue(oneChildAnotherChildChildNodes));
            //child second child marked as deleted
            oneOf(mockOneChildAnotherChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildAnotherChildNode).getWorkspaceNodeID(); will(returnValue(oneChildAnotherChildNodeID));
            oneOf(mockOneChildAnotherChildNode).isExternal(); will(returnValue(isOneChildAnotherChildExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, oneChildAnotherChildNodeID, isOneChildAnotherChildExternal);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildAnotherChildNode);
            
            //back to the first child node - it is marked as deleted too
            oneOf(mockOneChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildNodeID));
            oneOf(mockOneChildNode).isExternal(); will(returnValue(isOneChildExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, oneChildNodeID, isOneChildExternal);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildNode);
            
            //second child not protected - delete recursively
            oneOf(mockAnotherChildNode).isProtected(); will(returnValue(isAnotherChildProtected));
            //recursive call - second child
            //no children
            oneOf(mockAnotherChildNode).getWorkspaceNodeID(); will(returnValue(anotherChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(anotherChildNodeID); will(returnValue(anotherChildChildNodes));
            //second child node is marked as deleted too
            oneOf(mockAnotherChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockAnotherChildNode).getWorkspaceNodeID(); will(returnValue(anotherChildNodeID));
            oneOf(mockAnotherChildNode).isExternal(); will(returnValue(isAnotherChildExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, anotherChildNodeID, isAnotherChildExternal);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockAnotherChildNode);

            //back to the top node, which will be marked as deleted too
            oneOf(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockNode).isExternal(); will(returnValue(isNodeExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID, isNodeExternal);
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockNode);
        }});
        
        workspaceNodeManager.deleteNodesRecursively(mockNode);
    }
    
    @Test
    public void deleteNode_DoNotAllowDeletionOfProtectedNode() throws WorkspaceException {
        
        // if the node is protected, it shouldn't be possible to delete it
        
        final int workspaceID = 10;
        final int nodeID = 101;
        final URI nodeURI = URI.create(UUID.randomUUID().toString());
        final boolean isNodeProtected = Boolean.TRUE;
        
        final String expectedExceptionMessage = "Cannot proceed with deleting because the node (ID = " + nodeID + ") is protected (WS ID = " + workspaceID + ").";
        
        context.checking(new Expectations() {{
            
            //node protected - do not allow deletion to proceed
            oneOf(mockNode).isProtected(); will(returnValue(isNodeProtected));
            
            //log
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            exactly(2).of(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeURI));
        }});
        
        try {
            workspaceNodeManager.deleteNodesRecursively(mockNode);
            fail("should have thrown exception");
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", nodeURI, ex.getNodeURI());
            assertEquals("Exception workspace ID different from expected", workspaceID, ex.getWorkspaceID());
        }
    }
    
    @Test
    public void deleteNode_ProtectedChildNodes() throws WorkspaceException, ProtectedNodeException {
        
        // if the node has protected children, deletion should be allowed, but making sure that the protected children would just be unlinked, not deleted
        
        final int workspaceID = 10;
        final int nodeID = 101;
        final boolean isNodeExternal = Boolean.FALSE;
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isOneChildNodeProtected = Boolean.TRUE;
        
        final Collection<WorkspaceNode> childNodes = new ArrayList<>();
        childNodes.add(mockOneChildNode);
        
        context.checking(new Expectations() {{
            
            //node not protected - delete recursively
            oneOf(mockNode).isProtected(); will(returnValue(isNodeProtected));
            
            //one child
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(childNodes));
            
            //child protected - only unlink instead of recursively delete
            oneOf(mockOneChildNode).isProtected(); will(returnValue(isOneChildNodeProtected));
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodes(mockNode, mockOneChildNode);
            
            //top node marked as deleted
            oneOf(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockNode).isExternal(); will(returnValue(isNodeExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID, isNodeExternal);
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockNode);
        }});
        
        workspaceNodeManager.deleteNodesRecursively(mockNode);
    }
    
    
    //TODO ERRORS / EXCEPTIONS
}