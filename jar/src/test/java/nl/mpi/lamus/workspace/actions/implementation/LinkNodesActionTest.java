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
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.actions.WsTwoNodesAction;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
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
public class LinkNodesActionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceService mockWorkspaceService;
    @Mock WorkspaceNode mockSelectedNode;
    @Mock WorkspaceNode mockWorkspaceNodeOne;
    @Mock WorkspaceNode mockWorkspaceNodeTwo;
    
    private WsTwoNodesAction linkNodesAction;
    
    private String expectedActionName = "Link";
    
    public LinkNodesActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        linkNodesAction = new LinkNodesAction(mockWorkspaceService);
        ReflectionTestUtils.setField(linkNodesAction, "name", expectedActionName);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getName() {
        
        String retrievedActionName = linkNodesAction.getName();
        
        assertEquals("Retrieved name different from expected", expectedActionName, retrievedActionName);
    }
    
    @Test
    public void executeOneAction() {

        final String userID = "testUser";
        Collection<WorkspaceNode> nodes = new ArrayList<WorkspaceNode>();
        nodes.add(mockWorkspaceNodeOne);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).linkNodes(userID, mockSelectedNode, mockWorkspaceNodeOne);
        }});

        linkNodesAction.execute(userID, mockSelectedNode, nodes);
    }
    
    @Test
    public void executeTwoActions() {

        final String userID = "testUser";
        Collection<WorkspaceNode> nodes = new ArrayList<WorkspaceNode>();
        nodes.add(mockWorkspaceNodeOne);
        nodes.add(mockWorkspaceNodeTwo);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspaceService).linkNodes(userID, mockSelectedNode, mockWorkspaceNodeOne);
            oneOf(mockWorkspaceService).linkNodes(userID, mockSelectedNode, mockWorkspaceNodeTwo);
        }});

        linkNodesAction.execute(userID, mockSelectedNode, nodes);
    }
}