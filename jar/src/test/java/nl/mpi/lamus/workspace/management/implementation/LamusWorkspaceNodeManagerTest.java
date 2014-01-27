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
import static org.junit.Assert.*;
import org.junit.Rule;

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
    public void deleteNodeWithArchiveUriWithoutChildren() throws WorkspaceException, URISyntaxException {
        
        final int workspaceID = 10;
        final int nodeID = 101;
        
        final Collection<WorkspaceNode> childNodes = new ArrayList<WorkspaceNode>();
        
        context.checking(new Expectations() {{
            
            //no children
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(childNodes));
            
            oneOf(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID);
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockNode);
        }});
        
        workspaceNodeManager.deleteNodesRecursively(mockNode);
    }
    
    @Test
    public void deleteNodeWitOneChild() throws WorkspaceException {
        
        final int workspaceID = 10;
        final int nodeID = 101;
        final int childNodeID = 102;
        
        final Collection<WorkspaceNode> childNodes = new ArrayList<WorkspaceNode>();
        childNodes.add(mockOneChildNode);
        final Collection<WorkspaceNode> childChildNodes = new ArrayList<WorkspaceNode>();
        
        context.checking(new Expectations() {{
            
            //one child
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(childNodes));
            
            //recursive call
            //no children
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(childNodeID); will(returnValue(childChildNodes));
            //child is marked as deleted
            oneOf(mockOneChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(childNodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, childNodeID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildNode);

            //back to the top node, which will be marked as deleted too
            oneOf(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID);
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockNode);
        }});
        
        workspaceNodeManager.deleteNodesRecursively(mockNode);
    }
    
    @Test
    public void deleteNodeWitChildrenWithChildren() throws WorkspaceException {
        
        final int workspaceID = 10;
        final int nodeID = 101;
        final int oneChildNodeID = 102;
        final int anotherChildNodeID = 103;
        final int oneChildOneChildNodeID = 104;
        final int oneChildAnotherChildNodeID = 105;
        
        final Collection<WorkspaceNode> childNodes = new ArrayList<WorkspaceNode>();
        childNodes.add(mockOneChildNode);
        childNodes.add(mockAnotherChildNode);
        final Collection<WorkspaceNode> oneChildChildNodes = new ArrayList<WorkspaceNode>();
        oneChildChildNodes.add(mockOneChildOneChildNode);
        oneChildChildNodes.add(mockOneChildAnotherChildNode);
        final Collection<WorkspaceNode> oneChildOneChildChildNodes = new ArrayList<WorkspaceNode>();
        final Collection<WorkspaceNode> oneChildAnotherChildChildNodes = new ArrayList<WorkspaceNode>();
        final Collection<WorkspaceNode> anotherChildChildNodes = new ArrayList<WorkspaceNode>();
        
        context.checking(new Expectations() {{
            
            //two children
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(nodeID); will(returnValue(childNodes));
            
            //recursive call - first child
            //two children
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oneChildNodeID); will(returnValue(oneChildChildNodes));
            
            //another recursive call - first child - first child
            //no children
            oneOf(mockOneChildOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildOneChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oneChildOneChildNodeID); will(returnValue(oneChildOneChildChildNodes));
            //child first child marked as deleted
            oneOf(mockOneChildOneChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildOneChildNodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, oneChildOneChildNodeID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildOneChildNode);
            
            //another recursive call - first child - second child
            //no children
            oneOf(mockOneChildAnotherChildNode).getWorkspaceNodeID(); will(returnValue(oneChildAnotherChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oneChildAnotherChildNodeID); will(returnValue(oneChildAnotherChildChildNodes));
            //child second child marked as deleted
            oneOf(mockOneChildAnotherChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildAnotherChildNode).getWorkspaceNodeID(); will(returnValue(oneChildAnotherChildNodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, oneChildAnotherChildNodeID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildAnotherChildNode);
            
            //back to the first child node - it is marked as deleted too
            oneOf(mockOneChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockOneChildNode).getWorkspaceNodeID(); will(returnValue(oneChildNodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, oneChildNodeID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockOneChildNode);
            
            //recursive call - second child
            //no children
            oneOf(mockAnotherChildNode).getWorkspaceNodeID(); will(returnValue(anotherChildNodeID));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(anotherChildNodeID); will(returnValue(anotherChildChildNodes));
            //second child node is marked as deleted too
            oneOf(mockAnotherChildNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockAnotherChildNode).getWorkspaceNodeID(); will(returnValue(anotherChildNodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, anotherChildNodeID);
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockAnotherChildNode);

            //back to the top node, which will be marked as deleted too
            oneOf(mockNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(workspaceID, nodeID);
            
            oneOf(mockWorkspaceNodeLinkManager).unlinkNodeFromAllParents(mockNode);
        }});
        
        workspaceNodeManager.deleteNodesRecursively(mockNode);
    }
    
    
    //TODO ERRORS / EXCEPTIONS
}