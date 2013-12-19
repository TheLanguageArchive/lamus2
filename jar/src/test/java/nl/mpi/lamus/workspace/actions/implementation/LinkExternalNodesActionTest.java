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

import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsParentSingleChildNodeAction;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
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
public class LinkExternalNodesActionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceTreeNode mockParentNode;
    @Mock WorkspaceNode mockChildNode;
    
    private WsParentSingleChildNodeAction linkExternalNodesAction;
    
    private String expectedActionName = "Link External Node";
    
    public LinkExternalNodesActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        linkExternalNodesAction = new LinkExternalNodesAction();
        ReflectionTestUtils.setField(linkExternalNodesAction, "name", expectedActionName);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getName() {
        
        String retrievedActionName = linkExternalNodesAction.getName();
        
        assertEquals("Retrieved name different from expected", expectedActionName, retrievedActionName);
    }
    
    @Test
    public void execute() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final String userID = "testUser";
        
        context.checking(new Expectations() {{
 
            oneOf(mockWorkspaceService).addNode(userID, mockChildNode);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNode);
        }});
        
        linkExternalNodesAction.execute(userID, mockParentNode, mockChildNode, mockWorkspaceService);
    }
    
    @Test
    public void executeWorkspaceNotFoundException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final String userID = "testUser";
        final int workspaceID = 10;
        
        final WorkspaceNotFoundException expectedException = new WorkspaceNotFoundException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
 
            oneOf(mockWorkspaceService).addNode(userID, mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            linkExternalNodesAction.execute(userID, mockParentNode, mockChildNode, mockWorkspaceService);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void executeWorkspaceAccessException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final String userID = "testUser";
        final int workspaceID = 10;
        
        final WorkspaceAccessException expectedException = new WorkspaceAccessException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
 
            oneOf(mockWorkspaceService).addNode(userID, mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            linkExternalNodesAction.execute(userID, mockParentNode, mockChildNode, mockWorkspaceService);
        } catch(WorkspaceAccessException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }

    }
    
    @Test
    public void executeWorkspaceException() throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException {
        
        final String userID = "testUser";
        final int workspaceID = 10;
        
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
 
            oneOf(mockWorkspaceService).addNode(userID, mockChildNode);
            oneOf(mockWorkspaceService).linkNodes(userID, mockParentNode, mockChildNode); will(throwException(expectedException));
        }});
        
        try {
            linkExternalNodesAction.execute(userID, mockParentNode, mockChildNode, mockWorkspaceService);
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}