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
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.NodeUtil;
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
public class DeleteNodesActionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeUtil mockNodeUtil;
    @Mock WorkspaceTreeNode mockTreeNodeOne;
    @Mock WorkspaceTreeNode mockTreeNodeTwo;
    
    private WsTreeNodesAction deleteNodesAction;
    
    private final String expectedActionName = "delete_node_action";
    private final String userID = "testUser";
    
    
    public DeleteNodesActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        deleteNodesAction = new DeleteNodesAction();
        ReflectionTestUtils.setField(deleteNodesAction, "name", expectedActionName);
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void getName() {
        
        String retrievedActionName = deleteNodesAction.getName();
        
        assertEquals("Retrieved name different from expected", expectedActionName, retrievedActionName);
    }
    
    @Test
    public void executeWithNullTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        String expectedExceptionMessage = "Action for deleting nodes requires at least one tree node; currently null";
        
        try {
            deleteNodesAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithEmptyTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        
        String expectedExceptionMessage = "Action for deleting nodes requires at least one tree node; currently selected " + selectedTreeNodes.size();
        
        deleteNodesAction.setSelectedTreeNodes(selectedTreeNodes);

        try {
            deleteNodesAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithNullService() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockTreeNodeOne);
        
        String expectedExceptionMessage = "WorkspaceService should have been set";
        
        deleteNodesAction.setSelectedTreeNodes(selectedTreeNodes);

        try {
            deleteNodesAction.execute(userID, null, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeOneAction() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockTreeNodeOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
        }});

        deleteNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        deleteNodesAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
    }
    
    @Test
    public void executeTwoActions() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockTreeNodeOne);
        selectedTreeNodes.add(mockTreeNodeTwo);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeTwo);
        }});

        deleteNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        deleteNodesAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
    }
    
    @Test
    public void executeActionWorkspaceNotFoundException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockTreeNodeOne);
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
                will(throwException(expectedException));
        }});

        deleteNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        try {
            deleteNodesAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceAccessException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockTreeNodeOne);
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
                will(throwException(expectedException));
        }});

        deleteNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        try {
            deleteNodesAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockTreeNodeOne);
        
        final WorkspaceException expectedException = new WorkspaceException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
                will(throwException(expectedException));
        }});

        deleteNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        try {
            deleteNodesAction.execute(userID, mockWorkspaceService, mockWorkspaceDao, mockNodeUtil);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}