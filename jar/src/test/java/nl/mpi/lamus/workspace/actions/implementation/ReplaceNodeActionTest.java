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
package nl.mpi.lamus.workspace.actions.implementation;

import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
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
public class ReplaceNodeActionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceTreeNode mockParentNode;
    @Mock WorkspaceTreeNode mockOldChildNode;
    @Mock WorkspaceTreeNode mockNewChildNode;
    @Mock WorkspaceTreeNode mockOtherChildNode;
    
    private WsTreeNodesAction replaceNodesAction;
    
    private String expectedActionName = "replace_node_action";
    private String userID = "testUser";
    
    public ReplaceNodeActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        replaceNodesAction = new ReplaceNodesAction();
        ReflectionTestUtils.setField(replaceNodesAction, "name", expectedActionName);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getName() {
        
        String retrievedActionName = replaceNodesAction.getName();
        
        assertEquals("Retrieved name different from expected", expectedActionName, retrievedActionName);
    }

    @Test
    public void executeWithNullTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        String expectedExceptionMessage = "Action for replacing nodes requires exactly one tree node; currently null";
        
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);

        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithNullUnlinkedNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        String expectedExceptionMessage = "Action for replacing nodes requires exactly one selected unlinked node; currently null";
        
        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);

        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithEmptyTreeNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        String expectedExceptionMessage = "Action for replacing nodes requires exactly one tree node; currently selected " + selectedTreeNodes.size();
        
        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);

        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithEmptyUnlinkedNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        String expectedExceptionMessage = "Action for replacing nodes requires exactly one selected unlinked node; currently selected " + selectedUnlinkedNodes.size();
        
        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);

        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithMultipleUnlinkedNodeCollection() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        selectedUnlinkedNodes.add(mockOtherChildNode);
        String expectedExceptionMessage = "Action for replacing nodes requires exactly one selected unlinked node; currently selected " + selectedUnlinkedNodes.size();
        
        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);

        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void executeWithNullService() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        String expectedExceptionMessage = "WorkspaceService should have been set";
        
        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);

        try {
            replaceNodesAction.execute(userID, null);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void execute() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        
        context.checking(new Expectations() {{
            oneOf(mockOldChildNode).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).replaceTree(userID, mockOldChildNode, mockNewChildNode, mockParentNode);
        }});
        
        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);

        replaceNodesAction.execute(userID, mockWorkspaceService);
    }
    
    @Test
    public void executeActionWorkspaceNotFoundException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockOldChildNode).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).replaceTree(userID, mockOldChildNode, mockNewChildNode, mockParentNode);
                will(throwException(expectedException));
        }});

        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);
        
        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceAccessException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockOldChildNode).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).replaceTree(userID, mockOldChildNode, mockNewChildNode, mockParentNode);
                will(throwException(expectedException));
        }});
        
        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);

        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final int workspaceID = 10;
        Collection<WorkspaceTreeNode> selectedTreeNodes = new ArrayList<WorkspaceTreeNode>();
        selectedTreeNodes.add(mockOldChildNode);
        Collection<WorkspaceTreeNode> selectedUnlinkedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedUnlinkedNodes.add(mockNewChildNode);
        
        final WorkspaceException expectedException = new WorkspaceException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockOldChildNode).getParent(); will(returnValue(mockParentNode));
            oneOf(mockWorkspaceService).replaceTree(userID, mockOldChildNode, mockNewChildNode, mockParentNode);
                will(throwException(expectedException));
        }});

        replaceNodesAction.setSelectedTreeNodes(selectedTreeNodes);
        replaceNodesAction.setSelectedUnlinkedNodes(selectedUnlinkedNodes);
        
        try {
            replaceNodesAction.execute(userID, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}