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
package nl.mpi.lamus.workspace.replace.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.IncompatibleNodesException;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.DeleteNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.LinkNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.ReplaceNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.UnlinkNodeFromReplacedParentReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.UnlinkNodeReplaceAction;
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
import org.junit.Rule;
import static org.junit.Assert.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class MetadataNodeReplaceCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock ReplaceActionManager mockReplaceActionManager;
    @Mock ReplaceActionFactory mockReplaceActionFactory;
    @Mock NodeReplaceExplorer mockNodeReplaceExplorer;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock HandleParser mockHandleParser;
    
    @Mock WorkspaceNode mockOldNode;
    @Mock WorkspaceNode mockNewNode;
    @Mock WorkspaceNode mockParentNode;
    @Mock CorpusNode mockOldCorpusNode;
    
    @Mock UnlinkNodeReplaceAction mockUnlinkAction;
    @Mock UnlinkNodeFromReplacedParentReplaceAction mockUnlinkFromReplacedParentAction;
    @Mock DeleteNodeReplaceAction mockDeleteAction;
    @Mock LinkNodeReplaceAction mockLinkAction;
    @Mock ReplaceNodeReplaceAction mockReplaceAction;
    
    private List<NodeReplaceAction> actions;
    
    
    private NodeReplaceChecker nodeReplaceChecker;
    
    public MetadataNodeReplaceCheckerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeReplaceChecker = new MetadataNodeReplaceChecker();
        ReflectionTestUtils.setField(nodeReplaceChecker, "replaceActionManager", mockReplaceActionManager);
        ReflectionTestUtils.setField(nodeReplaceChecker, "replaceActionFactory", mockReplaceActionFactory);
        ReflectionTestUtils.setField(nodeReplaceChecker, "nodeReplaceExplorer", mockNodeReplaceExplorer);
        ReflectionTestUtils.setField(nodeReplaceChecker, "workspaceDao", mockWorkspaceDao);
        ReflectionTestUtils.setField(nodeReplaceChecker, "handleParser", mockHandleParser);
        
        actions = new ArrayList<>();
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void decideReplaceActions_NotLinked() throws URISyntaxException, MalformedURLException, ProtectedNodeException, IncompatibleNodesException {
        
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final URI newNodeURI = oldNodeURI;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockHandleParser).areHandlesEquivalent(oldNodeURI, newNodeURI); will(returnValue(Boolean.TRUE));
            
            oneOf(mockReplaceActionFactory).getReplaceAction(mockOldNode, mockParentNode, mockNewNode, newNodeAlreadyLinked); will(returnValue(mockReplaceAction));
            oneOf(mockReplaceActionManager).addActionToList(mockReplaceAction, actions);
            
            oneOf(mockNodeReplaceExplorer).exploreReplace(mockOldNode, mockNewNode, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActions_NotLinked_DifferentHandles() throws URISyntaxException, MalformedURLException, ProtectedNodeException, IncompatibleNodesException {
        
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final URI newNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockHandleParser).areHandlesEquivalent(oldNodeURI, newNodeURI); will(returnValue(Boolean.FALSE));
            
            oneOf(mockReplaceActionFactory).getDeleteAction(mockOldNode); will(returnValue(mockDeleteAction));
            oneOf(mockReplaceActionManager).addActionToList(mockDeleteAction, actions);
            oneOf(mockReplaceActionFactory).getLinkAction(mockNewNode, mockParentNode); will(returnValue(mockLinkAction));
            oneOf(mockReplaceActionManager).addActionToList(mockLinkAction, actions);
            
            oneOf(mockNodeReplaceExplorer).exploreReplace(mockOldNode, mockNewNode, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActions_ProtectedNode() throws IncompatibleNodesException {
        
        // if a protected node is found in the tree (a descendant of the top node to replace),
        // the replacement should not go ahead
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final int newNodeID = 200;
        final URI newNodeURI = oldNodeURI;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.TRUE;
        
        final String expectedExceptionMessage = "Cannot proceed with replacement because old node (ID = " + oldNodeID + ") is protected (WS ID = " + workspaceID + ").";
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
        }});
        
        try {
            nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
            fail("should have thrown exception");
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception node URI different from expected", oldNodeURI, ex.getNodeURI());
            assertEquals("Exception workspace ID different from expected", workspaceID, ex.getWorkspaceID());
        }
    }
    
    @Test
    public void decideReplaceActions_SameNode() throws MalformedURLException, URISyntaxException, ProtectedNodeException, IncompatibleNodesException {
        
        final int oldNodeID = 100;
        final int newNodeID = 100;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final URI newNodeURI = oldNodeURI;
        
        final boolean newNodeAlreadyLinked = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));

            oneOf(mockReplaceActionFactory).getUnlinkFromOldParentAction(mockOldNode, mockParentNode); will(returnValue(mockUnlinkFromReplacedParentAction));
            oneOf(mockReplaceActionManager).addActionToList(mockUnlinkFromReplacedParentAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActions_TopNode_NodesCompatible() throws ProtectedNodeException, IncompatibleNodesException, MalformedURLException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final URL oldNodeArchiveURL = new URL("file:/archive/location/node.cmdi");
        final int newNodeID = 200;
        final URI newNodeURI = oldNodeURI;
        final URL newNodeWorkspaceURL = new URL("file:/workspace/location/node.cmdi");
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldNodeID); will(returnValue(Boolean.TRUE));
            
            oneOf(mockHandleParser).areHandlesEquivalent(oldNodeURI, newNodeURI); will(returnValue(Boolean.TRUE));
            
            allowing(mockOldNode).getArchiveURL(); will(returnValue(oldNodeArchiveURL));
            allowing(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockReplaceActionFactory).getReplaceAction(mockOldNode, null, mockNewNode, newNodeAlreadyLinked); will(returnValue(mockReplaceAction));
            oneOf(mockReplaceActionManager).addActionToList(mockReplaceAction, actions);
            
            oneOf(mockNodeReplaceExplorer).exploreReplace(mockOldNode, mockNewNode, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, null, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceAction_TopNode_NodesIncompatible_InvalidHandle() throws MalformedURLException, ProtectedNodeException {
        
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final int newNodeID = 200;
        final URI newNodeURI = null;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        final IllegalArgumentException exceptionToThrow = new IllegalArgumentException("Invalid handle or something");
        final String expectedExceptionMessage = "Incompatible top nodes (different handles). Old: " + oldNodeURI + "; New: " + newNodeURI;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldNodeID); will(returnValue(Boolean.TRUE));
            
            oneOf(mockHandleParser).areHandlesEquivalent(oldNodeURI, newNodeURI); will(throwException(exceptionToThrow));
        }});
        
        try {
            nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, null, newNodeAlreadyLinked, actions);
            fail("should have thrown exception");
        } catch(IncompatibleNodesException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception workspaceID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Exception oldNodeID different from expected", oldNodeID, ex.getOldNodeID());
            assertEquals("Exception newNodeID different from expected", newNodeID, ex.getNewNodeID());
        }
    }
    
    @Test
    public void decideReplaceAction_TopNode_NodesIncompatible_DifferentHandle() throws MalformedURLException, ProtectedNodeException {
        
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final int newNodeID = 200;
        final URI newNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        final String expectedExceptionMessage = "Incompatible top nodes (different handles). Old: " + oldNodeURI + "; New: " + newNodeURI;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldNodeID); will(returnValue(Boolean.TRUE));
            
            oneOf(mockHandleParser).areHandlesEquivalent(oldNodeURI, newNodeURI); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, null, newNodeAlreadyLinked, actions);
            fail("should have thrown exception");
        } catch(IncompatibleNodesException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception workspaceID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Exception oldNodeID different from expected", oldNodeID, ex.getOldNodeID());
            assertEquals("Exception newNodeID different from expected", newNodeID, ex.getNewNodeID());
        }
    }
    
    @Test
    public void decideReplaceAction_TopNode_NodesIncompatible_DifferentFilename() throws MalformedURLException, ProtectedNodeException {
        
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final String oldNodeFilename = "node.cmdi";
        final URL oldNodeArchiveURL = new URL("file:/archive/location/" + oldNodeFilename);
        final int newNodeID = 200;
        final URI newNodeURI = oldNodeURI;
        final String newNodeFilename = "other_node.cmdi";
        final URL newNodeWorkspaceURL = new URL("file:/workspace/location/" + newNodeFilename);
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        final String expectedExceptionMessage = "Incompatible top nodes (different filename). Old: " + oldNodeFilename + "; New: " + newNodeFilename;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldNodeID); will(returnValue(Boolean.TRUE));
            
            oneOf(mockHandleParser).areHandlesEquivalent(oldNodeURI, newNodeURI); will(returnValue(Boolean.TRUE));
            
            allowing(mockOldNode).getArchiveURL(); will(returnValue(oldNodeArchiveURL));
            allowing(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
        }});
        
        try {
            nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, null, newNodeAlreadyLinked, actions);
            fail("should have thrown exception");
        } catch(IncompatibleNodesException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception workspaceID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Exception oldNodeID different from expected", oldNodeID, ex.getOldNodeID());
            assertEquals("Exception newNodeID different from expected", newNodeID, ex.getNewNodeID());
        }
    }
    
    @Test
    public void decideReplaceAction_TopNode_NodesIncompatible_NullUrl() throws MalformedURLException, ProtectedNodeException {
        
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final URL oldNodeArchiveURL = null;
        final int newNodeID = 200;
        final URI newNodeURI = oldNodeURI;
        final String newNodeFilename = "other_node.cmdi";
        final URL newNodeWorkspaceURL = new URL("file:/workspace/location/" + newNodeFilename);
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        final String expectedExceptionMessage = "Couldn't verify filename compatibility. Old node Archive URL: " + oldNodeArchiveURL + "; New node Workspace URL: " + newNodeWorkspaceURL;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldNodeID); will(returnValue(Boolean.TRUE));
            
            oneOf(mockHandleParser).areHandlesEquivalent(oldNodeURI, newNodeURI); will(returnValue(Boolean.TRUE));
            
            allowing(mockOldNode).getArchiveURL(); will(returnValue(oldNodeArchiveURL));
            allowing(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
        }});
        
        try {
            nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, null, newNodeAlreadyLinked, actions);
            fail("should have thrown exception");
        } catch(IncompatibleNodesException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception workspaceID different from expected", workspaceID, ex.getWorkspaceID());
            assertEquals("Exception oldNodeID different from expected", oldNodeID, ex.getOldNodeID());
            assertEquals("Exception newNodeID different from expected", newNodeID, ex.getNewNodeID());
        }
    }
    
    @Test
    public void decideReplaceActions_NullParentButNotTopNode() throws ProtectedNodeException, IncompatibleNodesException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        final URI oldNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        final URI newNodeURI = oldNodeURI;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        final String expectedExceptionMessage = "Parent node was passed as null but node to replace is not top node of the workspace";
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            allowing(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));
            allowing(mockNewNode).getArchiveURI(); will(returnValue(newNodeURI));
            
            oneOf(mockWorkspaceDao).isTopNodeOfWorkspace(workspaceID, oldNodeID); will(returnValue(Boolean.FALSE));
        }});
        
        try {
            nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, null, newNodeAlreadyLinked, actions);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
}