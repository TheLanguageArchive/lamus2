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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
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
import nl.mpi.lamus.workspace.replace.action.implementation.UnlinkNodeReplaceAction;
import org.jmock.Expectations;
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
public class MetadataNodeReplaceCheckerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock ReplaceActionManager mockReplaceActionManager;
    @Mock ReplaceActionFactory mockReplaceActionFactory;
    @Mock NodeReplaceExplorer mockNodeReplaceExplorer;
    
    @Mock WorkspaceNode mockOldNode;
    @Mock WorkspaceNode mockNewNode;
    @Mock WorkspaceNode mockParentNode;
    @Mock CorpusNode mockOldCorpusNode;
    
    @Mock UnlinkNodeReplaceAction mockUnlinkAction;
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
        
        nodeReplaceChecker = new MetadataNodeReplaceChecker(
                mockReplaceActionManager, mockReplaceActionFactory,
                mockNodeReplaceExplorer);
        actions = new ArrayList<>();
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void decideReplaceActionsNotLinked() throws URISyntaxException, MalformedURLException, ProtectedNodeException {
        
        final int oldNodeID = 100;
        final int newNodeID = 200;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            //TODO CHECK IF FILE EXISTS, IS A FILE AND IS READABLE?
            
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            
            oneOf(mockReplaceActionFactory).getReplaceAction(mockOldNode, mockParentNode, mockNewNode, newNodeAlreadyLinked); will(returnValue(mockReplaceAction));
            oneOf(mockReplaceActionManager).addActionToList(mockReplaceAction, actions);
            
            oneOf(mockNodeReplaceExplorer).exploreReplace(mockOldNode, mockNewNode, actions);
            
        }});
        
        nodeReplaceChecker.decideReplaceActions(mockOldNode, mockNewNode, mockParentNode, newNodeAlreadyLinked, actions);
    }
    
    @Test
    public void decideReplaceActions_ProtectedNode() {
        
        // if a protected node is found in the tree (a descendant of the top node to replace),
        // the replacement should not go ahead
        
        final int workspaceID = 10;
        final int oldNodeID = 100;
        final URI oldNodeURI = URI.create(UUID.randomUUID().toString());
        final int newNodeID = 200;
        
        final boolean newNodeAlreadyLinked = Boolean.FALSE;
        final boolean isOldNodeProtected = Boolean.TRUE;
        
        final String expectedExceptionMessage = "Cannot proceed with replacement because old node (ID = " + oldNodeID + ") is protected (WS ID = " + workspaceID + ").";
        
        context.checking(new Expectations() {{
            
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeID));
            
            //TODO CHECK IF FILE EXISTS, IS A FILE AND IS READABLE?
            
            
            oneOf(mockOldNode).isProtected(); will(returnValue(isOldNodeProtected));
            //logger
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeID));
            exactly(2).of(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
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
}