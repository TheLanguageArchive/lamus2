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
    @Mock WorkspaceTreeNode mockWorkspaceNodeResourceOne;
    @Mock WorkspaceTreeNode mockWorkspaceNodeMetadataOne;
    @Mock WorkspaceTreeNode mockWorkspaceNodeExternal;
    @Mock WorkspaceTreeNode mockWorkspaceNodeProtected;
    
    private WsNodeActionsProvider wsNodeActionsProvider;
    
    private List<WsTreeNodesAction> expectedResourceNodeActions;
    private List<WsTreeNodesAction> expectedMetadataNodeActions;
    private List<WsTreeNodesAction> expectedExternalNodeActions;
    private List<WsTreeNodesAction> expectedProtectedNodeActions;

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
        
        expectedResourceNodeActions = new ArrayList<>();
        expectedResourceNodeActions.add(new DeleteNodesAction());
        expectedResourceNodeActions.add(new UnlinkNodesAction());
        expectedResourceNodeActions.add(new ReplaceNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "resourcesActions", expectedResourceNodeActions);
        
        expectedMetadataNodeActions = new ArrayList<>();
        expectedMetadataNodeActions.add(new DeleteNodesAction());
        expectedMetadataNodeActions.add(new UnlinkNodesAction());
        expectedMetadataNodeActions.add(new LinkNodesAction());
        expectedMetadataNodeActions.add(new ReplaceNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "metadataActions", expectedMetadataNodeActions);
        
        //external - either resource or metadata
        expectedExternalNodeActions = new ArrayList<>();
        expectedExternalNodeActions.add(new DeleteNodesAction());
        expectedExternalNodeActions.add(new UnlinkNodesAction());
        expectedExternalNodeActions.add(new ReplaceNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "externalActions", expectedExternalNodeActions);
        
        expectedProtectedNodeActions = new ArrayList<>();
        expectedProtectedNodeActions.add(new UnlinkNodesAction());
        ReflectionTestUtils.setField(wsNodeActionsProvider, "protectedActions", expectedProtectedNodeActions);
        
        expectedMultipleNodesActions = new ArrayList<>();
        ReflectionTestUtils.setField(wsNodeActionsProvider, "multipleNodesActions", expectedMultipleNodesActions);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void getActionsEmptyNodesList() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<>();
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertTrue("Retrieved node actions list should be empty", retrievedNodeActions.isEmpty());
    }
    
    @Test
    public void getActionsOneResource() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<>();
        nodes.add(mockWorkspaceNodeResourceOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeResourceOne).isProtected(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceNodeResourceOne).isExternal(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceNodeResourceOne).isMetadata(); will(returnValue(Boolean.FALSE));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", expectedResourceNodeActions, retrievedNodeActions);
    }
    
    @Test
    public void getActionsOneMetadata() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<>();
        nodes.add(mockWorkspaceNodeMetadataOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeMetadataOne).isProtected(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceNodeMetadataOne).isExternal(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceNodeMetadataOne).isMetadata(); will(returnValue(Boolean.TRUE));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", expectedMetadataNodeActions, retrievedNodeActions);
    }
    
    @Test
    public void getActionsOneExternal() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<>();
        nodes.add(mockWorkspaceNodeExternal);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeExternal).isProtected(); will(returnValue(Boolean.FALSE));
            oneOf(mockWorkspaceNodeExternal).isExternal(); will(returnValue(Boolean.TRUE));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", expectedExternalNodeActions, retrievedNodeActions);
    }
    
    @Test
    public void getActionsOneProtected() {
        
        Collection<WorkspaceTreeNode> nodes = new ArrayList<>();
        nodes.add(mockWorkspaceNodeProtected);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceNodeProtected).isProtected(); will(returnValue(Boolean.TRUE));
        }});
        
        List<WsTreeNodesAction> retrievedNodeActions = wsNodeActionsProvider.getActions(nodes);
        
        assertEquals("Retrieved node actions different from expected", expectedProtectedNodeActions, retrievedNodeActions);
    }
    
    //TODO Other types
}