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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.FileInfo;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.exception.IncompatibleNodesException;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.DeleteNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.LinkNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.ReplaceNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.UnlinkNodeFromReplacedParentReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.UnlinkNodeReplaceAction;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
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
public class ResourceNodeReplaceCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock ReplaceActionManager mockReplaceActionManager;
    @Mock ReplaceActionFactory mockReplaceActionFactory;
    
    @Mock WorkspaceNode mockOldNode;
    @Mock WorkspaceNode mockNewNode;
    @Mock WorkspaceNode mockParentNode;
    @Mock CorpusNode mockOldCorpusNode;
    @Mock FileInfo mockOldCorpusNodeFileInfo;
    
    @Mock UnlinkNodeReplaceAction mockUnlinkAction;
    @Mock UnlinkNodeFromReplacedParentReplaceAction mockUnlinkFromReplacedParentAction;
    @Mock DeleteNodeReplaceAction mockDeleteAction;
    @Mock LinkNodeReplaceAction mockLinkAction;
    @Mock ReplaceNodeReplaceAction mockReplaceAction;
    
    private List<NodeReplaceAction> actions;
    
    
    private NodeReplaceChecker nodeReplaceChecker;
    
    public ResourceNodeReplaceCheckerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeReplaceChecker = new ResourceNodeReplaceChecker();
        ReflectionTestUtils.setField(nodeReplaceChecker, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(nodeReplaceChecker, "archiveFileHelper", mockArchiveFileHelper);
        ReflectionTestUtils.setField(nodeReplaceChecker, "replaceActionManager", mockReplaceActionManager);
        ReflectionTestUtils.setField(nodeReplaceChecker, "replaceActionFactory", mockReplaceActionFactory);
        
        actions = new ArrayList<>();
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void decideReplaceActionsOldNodeInArchive_WithoutChange_NotLinked() throws URISyntaxException, MalformedURLException, ProtectedNodeException, IncompatibleNodesException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI archiveNodeHandleURI = new URI(UUID.randomUUID().toString());
        
        final URL newNodeWorkspaceURL = new URL("file:/lamus/folder/workspace/" + workspaceID + "/file.txt");
        final File newNodeWorkspaceFile = new File(newNodeWorkspaceURL.getPath());
        
        final String oldNodeFormat = "text/plain";
        final String newNodeFormat = "text/plain";
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockOldNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockOldNode).getFormat(); will(returnValue(oldNodeFormat));
            oneOf(mockNewNode).getFormat(); will(returnValue(newNodeFormat));
            
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(archiveNodeHandleURI));
            
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeHandleURI); will(returnValue(mockOldCorpusNode));
            
            oneOf(mockOldCorpusNode).getFileInfo(); will(returnValue(mockOldCorpusNodeFileInfo));
            oneOf(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
            oneOf(mockArchiveFileHelper).hasArchiveFileChanged(mockOldCorpusNodeFileInfo, newNodeWorkspaceFile); will(returnValue(Boolean.FALSE));
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeInArchive_WithoutChange_AlreadyLinked() throws URISyntaxException, MalformedURLException, ProtectedNodeException, IncompatibleNodesException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI archiveNodeHandleURI = new URI(UUID.randomUUID().toString());
        
        final URL newNodeWorkspaceURL = new URL("file:/lamus/folder/workspace/" + workspaceID + "/file.txt");
        final File newNodeWorkspaceFile = new File(newNodeWorkspaceURL.getPath());
        
        final String oldNodeFormat = "text/plain";
        final String newNodeFormat = "text/plain";
        
        final boolean newNodeAlreadyLinked = Boolean.TRUE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockOldNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockOldNode).getFormat(); will(returnValue(oldNodeFormat));
            oneOf(mockNewNode).getFormat(); will(returnValue(newNodeFormat));
            
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(archiveNodeHandleURI));
            
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeHandleURI); will(returnValue(mockOldCorpusNode));
            
            oneOf(mockOldCorpusNode).getFileInfo(); will(returnValue(mockOldCorpusNodeFileInfo));
            oneOf(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
            oneOf(mockArchiveFileHelper).hasArchiveFileChanged(mockOldCorpusNodeFileInfo, newNodeWorkspaceFile); will(returnValue(Boolean.FALSE));
            
            oneOf(mockReplaceActionFactory).getUnlinkAction(mockNewNode, mockParentNode); will(returnValue(mockUnlinkAction));
            oneOf(mockReplaceActionManager).addActionToList(mockUnlinkAction, actions);
            oneOf(mockReplaceActionFactory).getDeleteAction(mockNewNode); will(returnValue(mockDeleteAction));
            oneOf(mockReplaceActionManager).addActionToList(mockDeleteAction, actions);
            oneOf(mockReplaceActionFactory).getLinkAction(mockOldNode, mockParentNode); will(returnValue(mockLinkAction));
            oneOf(mockReplaceActionManager).addActionToList(mockLinkAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeInArchive_WithChange_NotLinked() throws URISyntaxException, MalformedURLException, ProtectedNodeException, IncompatibleNodesException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI oldNodeArchiveHandleURI = new URI(UUID.randomUUID().toString());
        
        final URL newNodeWorkspaceURL = new URL("file:/lamus/folder/workspace/" + workspaceID + "/file.txt");
        final File newNodeWorkspaceFile = new File(newNodeWorkspaceURL.getPath());
        
        final String oldNodeFormat = "text/plain";
        final String newNodeFormat = "text/plain";
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockOldNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockOldNode).getFormat(); will(returnValue(oldNodeFormat));
            oneOf(mockNewNode).getFormat(); will(returnValue(newNodeFormat));
            
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(oldNodeArchiveHandleURI));
            oneOf(mockCorpusStructureProvider).getNode(oldNodeArchiveHandleURI); will(returnValue(mockOldCorpusNode));
            
            oneOf(mockOldCorpusNode).getFileInfo(); will(returnValue(mockOldCorpusNodeFileInfo));
            oneOf(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
            oneOf(mockArchiveFileHelper).hasArchiveFileChanged(mockOldCorpusNodeFileInfo, newNodeWorkspaceFile); will(returnValue(Boolean.TRUE));
            
            oneOf(mockReplaceActionFactory).getReplaceAction(mockOldNode, mockParentNode, mockNewNode, newNodeAlreadyLinked); will(returnValue(mockReplaceAction));
            oneOf(mockReplaceActionManager).addActionToList(mockReplaceAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeNotInArchive() throws MalformedURLException, ProtectedNodeException, IncompatibleNodesException {
        
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final String oldNodeFormat = "text/plain";
        final String newNodeFormat = "text/plain";
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockOldNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockOldNode).getFormat(); will(returnValue(oldNodeFormat));
            oneOf(mockNewNode).getFormat(); will(returnValue(newNodeFormat));
            
            //old node not in archive, which means it was newly added in this workspace
                // it should be replaced normally, the dao layer will take care of setting it as deleted instead or replaced
            
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockCorpusStructureProvider).getNode(null); will(returnValue(null));
            
            oneOf(mockReplaceActionFactory).getReplaceAction(mockOldNode, mockParentNode, mockNewNode, newNodeAlreadyLinked); will(returnValue(mockReplaceAction));
            oneOf(mockReplaceActionManager).addActionToList(mockReplaceAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsNodesWithDifferentFormats() throws MalformedURLException, URISyntaxException, ProtectedNodeException, IncompatibleNodesException {
        
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final String oldNodeFormat = "text/plain";
        final String newNodeFormat = "image/jpeg";
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockOldNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockOldNode).getFormat(); will(returnValue(oldNodeFormat));
            oneOf(mockNewNode).getFormat(); will(returnValue(newNodeFormat));
            
            oneOf(mockReplaceActionFactory).getUnlinkAction(mockOldNode, mockParentNode); will(returnValue(mockUnlinkAction));
            oneOf(mockReplaceActionManager).addActionToList(mockUnlinkAction, actions);
            oneOf(mockReplaceActionFactory).getDeleteAction(mockOldNode); will(returnValue(mockDeleteAction));
            oneOf(mockReplaceActionManager).addActionToList(mockDeleteAction, actions);
            oneOf(mockReplaceActionFactory).getLinkAction(mockNewNode, mockParentNode); will(returnValue(mockLinkAction));
            oneOf(mockReplaceActionManager).addActionToList(mockLinkAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeExternal() throws MalformedURLException, URISyntaxException, ProtectedNodeException, IncompatibleNodesException {
        
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockOldNode).isExternal(); will(returnValue(Boolean.TRUE));
            
            oneOf(mockReplaceActionFactory).getUnlinkAction(mockOldNode, mockParentNode); will(returnValue(mockUnlinkAction));
            oneOf(mockReplaceActionManager).addActionToList(mockUnlinkAction, actions);
            oneOf(mockReplaceActionFactory).getDeleteAction(mockOldNode); will(returnValue(mockDeleteAction));
            oneOf(mockReplaceActionManager).addActionToList(mockDeleteAction, actions);
            oneOf(mockReplaceActionFactory).getLinkAction(mockNewNode, mockParentNode); will(returnValue(mockLinkAction));
            oneOf(mockReplaceActionManager).addActionToList(mockLinkAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeProtected() throws MalformedURLException, URISyntaxException, ProtectedNodeException, IncompatibleNodesException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create(UUID.randomUUID().toString());
        final int newNodeID = 200;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.TRUE;
        
        final String expectedExceptionMessage = "Cannot proceed with replacement because old node (ID = " + oldNodeID + ") is protected (WS ID = " + workspaceID + ").";
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(oldNodeURI));            
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
    public void decideReplaceActions_OldNodeProtected_ButNotActuallyBeingReplaced() throws MalformedURLException, URISyntaxException, ProtectedNodeException, IncompatibleNodesException {
        
        final int nodeID = 100;
        
        final boolean newNodeAlreadyLinked = Boolean.TRUE;
        final boolean isOldNodeProtected = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            oneOf(mockReplaceActionFactory).getUnlinkFromOldParentAction(mockOldNode, mockParentNode); will(returnValue(mockUnlinkFromReplacedParentAction));
            oneOf(mockReplaceActionManager).addActionToList(mockUnlinkFromReplacedParentAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsNodesAreTheSame() throws ProtectedNodeException, IncompatibleNodesException {
        
        final int nodeID = 100;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            allowing(mockNewNode).getWorkspaceNodeID(); will(returnValue(nodeID));
            
            oneOf(mockReplaceActionFactory).getUnlinkFromOldParentAction(mockOldNode, mockParentNode); will(returnValue(mockUnlinkFromReplacedParentAction));
            oneOf(mockReplaceActionManager).addActionToList(mockUnlinkFromReplacedParentAction, actions);
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
}