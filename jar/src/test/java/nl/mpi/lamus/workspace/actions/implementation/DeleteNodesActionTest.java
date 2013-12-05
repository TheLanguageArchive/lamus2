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
public class DeleteNodesActionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceTreeNode mockTreeNodeOne;
    @Mock WorkspaceTreeNode mockTreeNodeTwo;
    
    private WsTreeNodesAction deleteNodesAction;
    
    private String expectedActionName = "Delete";
    
    
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
    public void executeOneAction() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final String userID = "testUser";
        Collection<WorkspaceTreeNode> selectedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedNodes.add(mockTreeNodeOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
        }});

        //TODO is there an advantage on passing the parent node in this particular type of action?
        deleteNodesAction.execute(userID, selectedNodes, mockWorkspaceService);
    }
    
    @Test
    public void executeTwoActions() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final String userID = "testUser";
        Collection<WorkspaceTreeNode> selectedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedNodes.add(mockTreeNodeOne);
        selectedNodes.add(mockTreeNodeTwo);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeTwo);
        }});

        //TODO is there an advantage on passing the parent node in this particular type of action?
        deleteNodesAction.execute(userID, selectedNodes, mockWorkspaceService);
    }
    
    @Test
    public void executeActionWorkspaceNotFoundException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final int workspaceID = 10;
        final String userID = "testUser";
        Collection<WorkspaceTreeNode> selectedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedNodes.add(mockTreeNodeOne);
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
                will(throwException(expectedException));
        }});

        try {
            deleteNodesAction.execute(userID, selectedNodes, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceAccessException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final int workspaceID = 10;
        final String userID = "testUser";
        Collection<WorkspaceTreeNode> selectedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedNodes.add(mockTreeNodeOne);
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
                will(throwException(expectedException));
        }});

        try {
            deleteNodesAction.execute(userID, selectedNodes, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeActionWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {

        final int workspaceID = 10;
        final String userID = "testUser";
        Collection<WorkspaceTreeNode> selectedNodes = new ArrayList<WorkspaceTreeNode>();
        selectedNodes.add(mockTreeNodeOne);
        
        final WorkspaceException expectedException = new WorkspaceException(userID, workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).deleteNode(userID, mockTreeNodeOne);
                will(throwException(expectedException));
        }});

        try {
            deleteNodesAction.execute(userID, selectedNodes, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}