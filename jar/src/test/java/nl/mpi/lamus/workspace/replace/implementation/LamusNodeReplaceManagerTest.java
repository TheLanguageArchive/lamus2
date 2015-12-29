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

import java.util.ArrayList;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerAssigner;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;

/**
 *
 * @author guisil
 */
public class LamusNodeReplaceManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock NodeReplaceCheckerAssigner mockNodeReplaceCheckerFactory;
    @Mock ReplaceActionManager mockReplaceActionManager;
    
    @Mock NodeReplaceChecker mockNodeReplaceChecker;
    
    @Mock WorkspaceNode mockOldNode;
    @Mock WorkspaceNode mockNewNode;
    @Mock WorkspaceNode mockParentNode;
    
    private LamusNodeReplaceManager nodeReplaceManager;
    
    
    public LamusNodeReplaceManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        nodeReplaceManager = new LamusNodeReplaceManager(mockNodeReplaceCheckerFactory, mockReplaceActionManager);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void replace() throws WorkspaceException, ProtectedNodeException {
        
        final int workspaceID = 1;
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(workspaceID));
            allowing(mockNewNode).getWorkspaceID(); will(returnValue(workspaceID));
            
            oneOf(mockNodeReplaceCheckerFactory).getReplaceCheckerForNode(mockOldNode);
                will(returnValue(mockNodeReplaceChecker));
            oneOf(mockNodeReplaceChecker).decideReplaceActions(
                    with(same(mockOldNode)), with(same(mockNewNode)),
                    with(same(mockParentNode)), with(equal(Boolean.FALSE)),
                    with(equal(new ArrayList<NodeReplaceAction>())));
            
            oneOf(mockReplaceActionManager).applyActions(with(any(ArrayList.class)));
        }});
        
        nodeReplaceManager.replaceTree(mockOldNode, mockNewNode, mockParentNode);
        
        
        
        //TODO TEST CALLING OF ACTIONS IN THE LIST... RETURN LIST INSTEAD?
        
    }
    
    @Test
    public void replace_differentWorkspaces() throws WorkspaceException, ProtectedNodeException {
        
        final int oldWorkspaceID = 1;
        final int newWorkspaceID = 2;
        final String expectedMessage = "Old node and new node belong to different workspaces.";
        
        context.checking(new Expectations() {{
            
            allowing(mockOldNode).getWorkspaceID(); will(returnValue(oldWorkspaceID));
            allowing(mockNewNode).getWorkspaceID(); will(returnValue(newWorkspaceID));
        }});
        
        try {
            nodeReplaceManager.replaceTree(mockOldNode, mockNewNode, mockParentNode);
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    
    //TODO exceptional cases?
}