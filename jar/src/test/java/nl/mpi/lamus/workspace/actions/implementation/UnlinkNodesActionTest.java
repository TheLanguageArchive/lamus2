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
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTreeNodesAction;
import nl.mpi.lamus.exception.WorkspaceException;
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
public class UnlinkNodesActionTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceTreeNode mockChildNodeOne;
    @Mock WorkspaceTreeNode mockChildNodeTwo;
    @Mock WorkspaceTreeNode mockParentNode;
    
    private WsTreeNodesAction unlinkNodesAction;
    
    private String expectedActionName = "unlink_node_action";
    private String userID = "testUser";

    
    public UnlinkNodesActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        unlinkNodesAction = new UnlinkNodesAction();
        ReflectionTestUtils.setField(unlinkNodesAction, "name", expectedActionName);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getName() {
        
        String retrievedActionName = unlinkNodesAction.getName();
        
        assertEquals("Retrieved name different from expected", expectedActionName, retrievedActionName);
    }
    
    @Test
    public void executeWithNullTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        String expectedExceptionMessage = "Action for unlinking nodes requires at least one tree node; currently null";
        
        try {
            unlinkNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithEmptyTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        String expectedExceptionMessage = "Action for unlinking nodes requires at least one tree node; currently selected " + selectedTreeNodes.size();
                
        unlinkNodesAction.setSelectedTreeNodes(selectedTreeNodes);

        try {
            unlinkNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithNullService() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockChildNodeOne);
        String expectedExceptionMessage = "WorkspaceService should have been set";

        unlinkNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        try {
            unlinkNodesAction.execute(userID, null);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeOneAction() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {
        
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockChildNodeOne);
        
        context.checking(new Expectations() {{
            oneOf(mockChildNodeOne).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).unlinkNodes(userID, mockParentNode, mockChildNodeOne);
        }});

        unlinkNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        unlinkNodesAction.execute(userID, mockWorkspaceService);
    }
    
    @Test
    public void executeTwoActions() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockChildNodeOne);
        selectedTreeNodes.add(mockChildNodeTwo);
        
        context.checking(new Expectations() {{
            oneOf(mockChildNodeOne).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).unlinkNodes(userID, mockParentNode, mockChildNodeOne);
            oneOf(mockChildNodeTwo).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).unlinkNodes(userID, mockParentNode, mockChildNodeTwo);
        }});

        unlinkNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        unlinkNodesAction.execute(userID, mockWorkspaceService);
    }
    
    @Test
    public void executeActionWorkspaceNotFoundException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockChildNodeOne);
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildNodeOne).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).unlinkNodes(userID, mockParentNode, mockChildNodeOne);
                will(throwException(expectedException));
        }});

        unlinkNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        try {
            unlinkNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceAccessException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockChildNodeOne);
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildNodeOne).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).unlinkNodes(userID, mockParentNode, mockChildNodeOne);
                will(throwException(expectedException));
        }});

        unlinkNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        try {
            unlinkNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<>();
        selectedTreeNodes.add(mockChildNodeOne);
        
        final WorkspaceException expectedException = new WorkspaceException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildNodeOne).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).unlinkNodes(userID, mockParentNode, mockChildNodeOne);
                will(throwException(expectedException));
        }});

        unlinkNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        
        try {
            unlinkNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}