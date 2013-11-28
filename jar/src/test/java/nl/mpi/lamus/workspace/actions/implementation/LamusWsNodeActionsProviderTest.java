/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.actions.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsNodeActionsProvider;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
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
public class LamusWsNodeActionsProviderTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceTreeNode mockWorkspaceNodeWrittenResourceOne;
    @Mock WorkspaceTreeNode mockWorkspaceNodeMediaResourceOne;
    @Mock WorkspaceTreeNode mockWorkspaceNodeLexicalResourceOne;
    @Mock WorkspaceTreeNode mockWorkspaceNodeMetadataOne;
    @Mock WorkspaceTreeNode mockWorkspaceNodeMetadataCollectionOne;
    
    
    
    private WsNodeActionsProvider wsNodeActionsProvider;
    
    private List<WsTreeNodesAction> expectedWrittenResourceNodeActions;
    private List<WsTreeNodesAction> expectedMediaResourceNodeActions;
    private List<WsTreeNodesAction> expectedLexicalResourceNodeActions;
    
    private List<WsTreeNodesAction> expectedMetadataNodeActions;
    
    private List<WsTreeNodesAction> expectedMetadataCollectionNodeActions;
    
    private List<WsTreeNodesAction> expectedMultipleNodesActions;
    
    
    public LamusWsNodeActionsProviderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        wsNodeActionsProvider = new LamusWsNodeActionsProvider();
        
        expectedWrittenResourceNodeActions = new ArrayList<WsTreeNodesAction>();
        expectedWrittenResourceNodeActions.add(new DeleteNodesAction());
        expectedWrittenResourceNodeActions.add(new UnlinkNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "writtenResourcesActions", expectedWrittenResourceNodeActions);
        
        expectedMediaResourceNodeActions = new ArrayList<WsTreeNodesAction>();
        expectedMediaResourceNodeActions.add(new DeleteNodesAction());
        expectedMediaResourceNodeActions.add(new UnlinkNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "mediaResourcesActions", expectedMediaResourceNodeActions);
        
        expectedLexicalResourceNodeActions = new ArrayList<WsTreeNodesAction>();
        expectedLexicalResourceNodeActions.add(new DeleteNodesAction());
        expectedLexicalResourceNodeActions.add(new UnlinkNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "lexicalResourcesActions", expectedLexicalResourceNodeActions);
        
        expectedMetadataNodeActions = new ArrayList<WsTreeNodesAction>();
        expectedMetadataNodeActions.add(new DeleteNodesAction());
        expectedMetadataNodeActions.add(new UnlinkNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "metadataActions", expectedMetadataNodeActions);
        
        expectedMetadataCollectionNodeActions = new ArrayList<WsTreeNodesAction>();
        ReflectionTestUtils.setField(wsNodeActionsProvider, "metadataCollectionActions", expectedMetadataCollectionNodeActions);
        
        expectedMultipleNodesActions = new ArrayList<WsTreeNodesAction>();
        ReflectionTestUtils.setField(wsNodeActionsProvider, "multipleNodesActions", expectedMultipleNodesActions);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getActionsEmptyNodesList() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<WorkspaceTreeNode>();
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertTrue("Retrieved node actions list should be empty", retrievedNodeActions.isEmpty());
    }
    
    @Test
    public void getActionsOneWrittenResource() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<WorkspaceTreeNode>();
        nodes.add(mockWorkspaceNodeWrittenResourceOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeWrittenResourceOne).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_WR));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", retrievedNodeActions, expectedWrittenResourceNodeActions);
    }
    
    @Test
    public void getActionsOneMediaResource() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<WorkspaceTreeNode>();
        nodes.add(mockWorkspaceNodeMediaResourceOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeMediaResourceOne).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_MR));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", retrievedNodeActions, expectedMediaResourceNodeActions);
    }
    
    @Test
    public void getActionsOneLexicalResource() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<WorkspaceTreeNode>();
        nodes.add(mockWorkspaceNodeLexicalResourceOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeLexicalResourceOne).getType(); will(returnValue(WorkspaceNodeType.RESOURCE_LEX));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", retrievedNodeActions, expectedLexicalResourceNodeActions);
    }
    
    @Test
    public void getActionsOneMetadata() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<WorkspaceTreeNode>();
        nodes.add(mockWorkspaceNodeMetadataOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeMetadataOne).getType(); will(returnValue(WorkspaceNodeType.METADATA));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", retrievedNodeActions, expectedMetadataNodeActions);
    }
    
    //TODO Other types
}