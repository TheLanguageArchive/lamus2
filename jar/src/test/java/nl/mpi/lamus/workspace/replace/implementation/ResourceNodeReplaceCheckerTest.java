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
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.DeleteNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.LinkNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.ReplaceNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.UnlinkNodeReplaceAction;
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
public class ResourceNodeReplaceCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
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
        
        nodeReplaceChecker = new ResourceNodeReplaceChecker(
                mockCorpusStructureProvider, mockArchiveFileHelper,
                mockReplaceActionManager, mockReplaceActionFactory);
        actions = new ArrayList<NodeReplaceAction>();
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void decideReplaceActionsOldNodeInArchive_WithoutChange_NotLinked() throws URISyntaxException, MalformedURLException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI archiveNodeHandleURI = new URI(UUID.randomUUID().toString());
        final URL archiveNodeRemoteURL = new URL("http://remote/archive/file.txt");
        final URI archiveNodeRemoteURI = archiveNodeRemoteURL.toURI();
        final URL archiveNodeLocalURL = new URL("file:/local/archive/file.txt");
        final URI archiveNodeLocalURI = archiveNodeLocalURL.toURI();
        
        final URL newNodeWorkspaceURL = new URL("file:/lamus/folder/workspace/" + workspaceID + "/file.txt");
        final File newNodeWorkspaceFile = new File(newNodeWorkspaceURL.getPath());
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            
            //TODO CHECK IF FILE EXISTS, IS A FILE AND IS READABLE?
            
            
            //TODO GET OLD NODE FROM ARCHIVE
                //TODO IF NOT PRESENT THERE, PROBABLY IT'S A NEWLY ADDED FILE - ADD REPLACE ACTION
                //TODO IF PRESENT, COMPARE FILESIZE AND/OR CHECKSUM AND ADD ACTIONS ACCORDINGLY...
            
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(archiveNodeHandleURI));
            
            oneOf(mockCorpusStructureProvider).getNode(archiveNodeHandleURI); will(returnValue(mockOldCorpusNode));
            
            oneOf(mockOldCorpusNode).getFileInfo(); will(returnValue(mockOldCorpusNodeFileInfo));
            oneOf(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
            oneOf(mockArchiveFileHelper).hasArchiveFileChanged(mockOldCorpusNodeFileInfo, newNodeWorkspaceFile); will(returnValue(Boolean.FALSE));
            
            //TODO MULTIPLE PARENTS???
            
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeInArchive_WithoutChange_AlreadyLinked() throws URISyntaxException, MalformedURLException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI archiveNodeHandleURI = new URI(UUID.randomUUID().toString());
        final URL archiveNodeRemoteURL = new URL("http://remote/archive/file.txt");
        final URI archiveNodeRemoteURI = archiveNodeRemoteURL.toURI();
        final URL archiveNodeLocalURL = new URL("file:/local/archive/file.txt");
        final URI archiveNodeLocalURI = archiveNodeLocalURL.toURI();
        
        final URL newNodeWorkspaceURL = new URL("file:/lamus/folder/workspace/" + workspaceID + "/file.txt");
        final File newNodeWorkspaceFile = new File(newNodeWorkspaceURL.getPath());
        
        final boolean newNodeAlreadyLinked = Boolean.TRUE;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            
            //TODO CHECK IF FILE EXISTS, IS A FILE AND IS READABLE?
            
            
            //TODO GET OLD NODE FROM ARCHIVE
                //TODO IF NOT PRESENT THERE, PROBABLY IT'S A NEWLY ADDED FILE - ADD REPLACE ACTION
                //TODO IF PRESENT, COMPARE FILESIZE AND/OR CHECKSUM AND ADD ACTIONS ACCORDINGLY...
            
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
            
            //TODO MULTIPLE PARENTS???
            
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeInArchive_WithChange_NotLinked() throws URISyntaxException, MalformedURLException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URI oldNodeArchiveHandleURI = new URI(UUID.randomUUID().toString());
        final URL oldNodeArchiveRemoteURL = new URL("http://remote/archive/file.txt");
        final URI oldNodeArchiveRemoteURI = oldNodeArchiveRemoteURL.toURI();
        final URL oldNodeArchiveLocalURL = new URL("file:/local/archive/file.txt");
        final URI oldNodeArchiveLocalURI = oldNodeArchiveLocalURL.toURI();
        
        final URL newNodeWorkspaceURL = new URL("file:/lamus/folder/workspace/" + workspaceID + "/file.txt");
        final File newNodeWorkspaceFile = new File(newNodeWorkspaceURL.getPath());
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            //TODO CHECK IF FILE EXISTS, IS A FILE AND IS READABLE?
            
            
            //TODO GET OLD NODE FROM ARCHIVE
                //TODO IF NOT PRESENT THERE, PROBABLY IT'S A NEWLY ADDED FILE - ADD REPLACE ACTION
                //TODO IF PRESENT, COMPARE FILESIZE AND/OR CHECKSUM AND ADD ACTIONS ACCORDINGLY...
            
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(oldNodeArchiveHandleURI));
            oneOf(mockCorpusStructureProvider).getNode(oldNodeArchiveHandleURI); will(returnValue(mockOldCorpusNode));
            
            oneOf(mockOldCorpusNode).getFileInfo(); will(returnValue(mockOldCorpusNodeFileInfo));
            oneOf(mockNewNode).getWorkspaceURL(); will(returnValue(newNodeWorkspaceURL));
            oneOf(mockArchiveFileHelper).hasArchiveFileChanged(mockOldCorpusNodeFileInfo, newNodeWorkspaceFile); will(returnValue(Boolean.TRUE));
            
            oneOf(mockReplaceActionFactory).getReplaceAction(mockOldNode, mockParentNode, mockNewNode, newNodeAlreadyLinked); will(returnValue(mockReplaceAction));
            oneOf(mockReplaceActionManager).addActionToList(mockReplaceAction, actions);
            
            //TODO MULTIPLE PARENTS???
            
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActionsOldNodeNotInArchive() throws MalformedURLException {
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final URL newNodeWorkspaceURL = new URL("file:/lamus/folder/workspace/" + workspaceID + "/file.txt");
        final File newNodeWorkspaceFile = new File(newNodeWorkspaceURL.getPath());
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            //old node not in archive, which means it was newly added in this workspace
                // it should be replaced normally, the dao layer will take care of setting it as deleted instead or replaced
            
            oneOf(mockOldNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockCorpusStructureProvider).getNode(null); will(returnValue(null));
            
            oneOf(mockReplaceActionFactory).getReplaceAction(mockOldNode, mockParentNode, mockNewNode, newNodeAlreadyLinked); will(returnValue(mockReplaceAction));
            oneOf(mockReplaceActionManager).addActionToList(mockReplaceAction, actions);
            
            //TODO MULTIPLE PARENTS???
            
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    
    //TODO other tests
}