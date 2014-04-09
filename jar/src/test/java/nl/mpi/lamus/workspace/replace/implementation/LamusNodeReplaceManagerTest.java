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
import java.util.List;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.NodeReplaceChecker;
import nl.mpi.lamus.workspace.replace.NodeReplaceCheckerFactory;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
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

/**
 *
 * @author guisil
 */
public class LamusNodeReplaceManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock NodeReplaceCheckerFactory mockNodeReplaceCheckerFactory;
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
    public void replace() throws WorkspaceException {
        
        context.checking(new Expectations() {{
            
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
    
    //TODO exceptional cases?
}