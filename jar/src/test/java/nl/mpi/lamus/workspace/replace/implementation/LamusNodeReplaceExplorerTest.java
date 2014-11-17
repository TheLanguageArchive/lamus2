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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerFactory;
import nl.mpi.lamus.workspace.replace.NodeReplaceExplorer;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.DeleteNodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import nl.mpi.lamus.workspace.replace.action.implementation.RemoveArchiveUriReplaceAction;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.jmock.Expectations;
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
public class LamusNodeReplaceExplorerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeReplaceCheckerFactory mockNodeReplaceCheckerFactory;
    @Mock ReplaceActionFactory mockReplaceActionFactory;
    @Mock ReplaceActionManager mockReplaceActionManager;
    
    @Mock WorkspaceNode mockOldNode;
    @Mock WorkspaceNode mockNewNode;
    
    @Mock WorkspaceNode mockOldNodeFirstChild;
    @Mock WorkspaceNode mockOldNodeSecondChild;
    
    @Mock WorkspaceNode mockNewNodeFirstChild;
    @Mock WorkspaceNode mockNewNodeSecondChild;
    
    @Mock ReferencingMetadataDocument mockNewNodeDocument;
    @Mock MetadataDocument mockNewNodeFirstChildDocument;
    @Mock Reference mockNewNodeFirstChildReference;
    
    @Mock File mockNewNodeFile;
    @Mock StreamResult mockStreamResult;
    
    @Mock ResourceNodeReplaceChecker mockResourceNodeReplaceChecker;
    @Mock MetadataNodeReplaceChecker mockMetadataNodeReplaceChecker;
    
    @Mock DeleteNodeReplaceAction mockDeleteNodeAction;
    @Mock RemoveArchiveUriReplaceAction mockRemoveArchiveUriAction;
    
    
    private NodeReplaceExplorer nodeReplaceExplorer;
    
    
    public LamusNodeReplaceExplorerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeReplaceExplorer = new LamusNodeReplaceExplorer(mockWorkspaceDao,
                mockNodeReplaceCheckerFactory, mockReplaceActionFactory,
                mockReplaceActionManager);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void exploreReplace_WithoutChildren() throws ProtectedNodeException {
        
        final List<NodeReplaceAction> actions = new ArrayList<>();
        
        final int oldNodeId = 10;
        final int newNodeId = 20;
        
        final Collection<WorkspaceNode> oldNodeChildren = new ArrayList<>();
        final Collection<WorkspaceNode> newNodeChildren = new ArrayList<>();
        
        // getting children of old and new node
        context.checking(new Expectations() {{
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oldNodeId); will(returnValue(oldNodeChildren));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(newNodeId); will(returnValue(newNodeChildren));
        }});
        
        //since there are no children, nothing needs to be done
        
        nodeReplaceExplorer.exploreReplace(mockOldNode, mockNewNode, actions);
    }
    
//    @Test
//    public void exploreReplace_OldNodeWithChild_NullHandle() {
//        
//        //TODO DOES IT MAKE SENSE TO HANDLE THIS SITUATION?
//        
//        fail("not tested yet");
//    }
    
    @Test
    public void exploreReplace_OnlyNewNodeHasChildren() throws URISyntaxException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final List<NodeReplaceAction> actions = new ArrayList<>();
        
        final int oldNodeId = 10;
        final int newNodeId = 20;
        
        final Collection<WorkspaceNode> oldNodeChildren = new ArrayList<>();
        
        final Collection<WorkspaceNode> newNodeChildren = new ArrayList<>();
        newNodeChildren.add(mockNewNodeFirstChild);
        
        // getting children of old and new node
        context.checking(new Expectations() {{
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oldNodeId); will(returnValue(oldNodeChildren));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(newNodeId); will(returnValue(newNodeChildren));
        }});
        
        // since there are no old children, all new children have no match
        // the new nodes without match (in this case it's the only child) should have their handles removed and should be treated as normal newly added nodes
        // the handle should be removed from the database and from the link in the parent; if the child is metadata, its self handle should be removed as well
        
        context.checking(new Expectations() {{
            
            //check first if archiveURI and URL are there?
            
            oneOf(mockReplaceActionFactory).getRemoveArchiveUriAction(mockNewNodeFirstChild, mockNewNode); will(returnValue(mockRemoveArchiveUriAction));
            oneOf(mockReplaceActionManager).addActionToList(mockRemoveArchiveUriAction, actions);
            
            //since the child is a resource, there's no self handle to remove (it should be removed if the child is metadata)
        }});
        
//        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(mockNewNodeFile);
        
        
        nodeReplaceExplorer.exploreReplace(mockOldNode, mockNewNode, actions);
    }
    
    @Test
    public void exploreReplace_WithChild_Resource_NotMatching() throws URISyntaxException, MalformedURLException, IOException, MetadataException, TransformerException, ProtectedNodeException {
        
        final List<NodeReplaceAction> actions = new ArrayList<>();
        
        final int oldNodeId = 10;
        final int newNodeId = 20;
        
        // handles do not match
        final URI oldNodeFirstChildHandle = new URI(UUID.randomUUID().toString());
        final URI newNodeFirstChildHandle = new URI(UUID.randomUUID().toString());
        
        final Collection<WorkspaceNode> oldNodeChildren = new ArrayList<>();
        oldNodeChildren.add(mockOldNodeFirstChild);
        
        final Collection<WorkspaceNode> newNodeChildren = new ArrayList<>();
        newNodeChildren.add(mockNewNodeFirstChild);
        
        // getting children of old and new node
        context.checking(new Expectations() {{
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oldNodeId); will(returnValue(oldNodeChildren));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(newNodeId); will(returnValue(newNodeChildren));
        }});
        
        // traverse children of old node and look for a match among the children of the new node
            // in this case there is only one child in each
        context.checking(new Expectations() {{
            oneOf(mockOldNodeFirstChild).getArchiveURI(); will(returnValue(oldNodeFirstChildHandle));
            // for loop - children of new node
            oneOf(mockNewNodeFirstChild).getArchiveURI(); will(returnValue(newNodeFirstChildHandle));
        }});
        
        // no match was found, so the old child should just be marked as deleted (??)
        // the new nodes without match (in this case it's the only child) should have their handles removed and should be treated as normal newly added nodes
        // the handle should be removed from the database and from the link in the parent; if the child is metadata, its self handle should be removed as well
        
        context.checking(new Expectations() {{

            oneOf(mockReplaceActionFactory).getDeleteAction(mockOldNodeFirstChild); will(returnValue(mockDeleteNodeAction));
            oneOf(mockReplaceActionManager).addActionToList(mockDeleteNodeAction, actions);
            
            oneOf(mockReplaceActionFactory).getRemoveArchiveUriAction(mockNewNodeFirstChild, mockNewNode); will(returnValue(mockRemoveArchiveUriAction));
            oneOf(mockReplaceActionManager).addActionToList(mockRemoveArchiveUriAction, actions);
            
            //since the child is a resource, there's no self handle to remove (it should be removed if the child is metadata)
        }});
        
        
        nodeReplaceExplorer.exploreReplace(mockOldNode, mockNewNode, actions);
    }
    
    @Test
    public void exploreReplace_WithChild_Resource_Matching() throws URISyntaxException, ProtectedNodeException {

        final List<NodeReplaceAction> actions = new ArrayList<>();
        
        final int oldNodeId = 10;
        final int newNodeId = 20;
        
        // handles match
        final URI oldNodeFirstChildHandle = new URI(UUID.randomUUID().toString());
        final URI newNodeFirstChildHandle = oldNodeFirstChildHandle;
        
        final Collection<WorkspaceNode> oldNodeChildren = new ArrayList<>();
        oldNodeChildren.add(mockOldNodeFirstChild);
        
        final Collection<WorkspaceNode> newNodeChildren = new ArrayList<>();
        newNodeChildren.add(mockNewNodeFirstChild);
        
        // getting children of old and new node
        context.checking(new Expectations() {{
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oldNodeId); will(returnValue(oldNodeChildren));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(newNodeId); will(returnValue(newNodeChildren));
        }});
        
        // traverse children of old node and look for a match among the children of the new node
            // in this case there is only one child in each
        context.checking(new Expectations() {{
            oneOf(mockOldNodeFirstChild).getArchiveURI(); will(returnValue(oldNodeFirstChildHandle));
            // for loop - children of new node
            oneOf(mockNewNodeFirstChild).getArchiveURI(); will(returnValue(newNodeFirstChildHandle));
        }});
        
        // get NodeReplaceChecker for current child and call it, passing also the matching new node
        
        context.checking(new Expectations() {{
            oneOf(mockNodeReplaceCheckerFactory).getReplaceCheckerForNode(mockOldNodeFirstChild); will(returnValue(mockResourceNodeReplaceChecker));
            oneOf(mockResourceNodeReplaceChecker).decideReplaceActions(mockOldNodeFirstChild, mockNewNodeFirstChild, mockNewNode, Boolean.TRUE, actions);
        }});
        
        
        nodeReplaceExplorer.exploreReplace(mockOldNode, mockNewNode, actions);
    }
    
    @Test
    public void exploreReplace_WithChild_Resource_Matching_ProtectedNode() throws URISyntaxException, ProtectedNodeException {

        final List<NodeReplaceAction> actions = new ArrayList<>();
        
        final int workspaceID = 1;
        final int oldNodeId = 10;
        final int newNodeId = 20;
        
        // handles match
        final URI oldNodeFirstChildHandle = new URI(UUID.randomUUID().toString());
        final URI newNodeFirstChildHandle = oldNodeFirstChildHandle;
        
        final Collection<WorkspaceNode> oldNodeChildren = new ArrayList<>();
        oldNodeChildren.add(mockOldNodeFirstChild);
        
        final Collection<WorkspaceNode> newNodeChildren = new ArrayList<>();
        newNodeChildren.add(mockNewNodeFirstChild);
        
        final ProtectedNodeException expectedException = new ProtectedNodeException("some exception message", oldNodeFirstChildHandle, workspaceID);
        
        // getting children of old and new node
        context.checking(new Expectations() {{
            oneOf(mockOldNode).getWorkspaceNodeID(); will(returnValue(oldNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(oldNodeId); will(returnValue(oldNodeChildren));
            oneOf(mockNewNode).getWorkspaceNodeID(); will(returnValue(newNodeId));
            oneOf(mockWorkspaceDao).getChildWorkspaceNodes(newNodeId); will(returnValue(newNodeChildren));
        }});
        
        // traverse children of old node and look for a match among the children of the new node
            // in this case there is only one child in each
        context.checking(new Expectations() {{
            oneOf(mockOldNodeFirstChild).getArchiveURI(); will(returnValue(oldNodeFirstChildHandle));
            // for loop - children of new node
            oneOf(mockNewNodeFirstChild).getArchiveURI(); will(returnValue(newNodeFirstChildHandle));
        }});
        
        // get NodeReplaceChecker for current child and call it, passing also the matching new node
        
        context.checking(new Expectations() {{
            oneOf(mockNodeReplaceCheckerFactory).getReplaceCheckerForNode(mockOldNodeFirstChild); will(returnValue(mockResourceNodeReplaceChecker));
            oneOf(mockResourceNodeReplaceChecker).decideReplaceActions(mockOldNodeFirstChild, mockNewNodeFirstChild, mockNewNode, Boolean.TRUE, actions);
                will(throwException(expectedException));
        }});
        
        try {
            nodeReplaceExplorer.exploreReplace(mockOldNode, mockNewNode, actions);
            fail("should have thrown exception");
        } catch(ProtectedNodeException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}