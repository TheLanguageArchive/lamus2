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
package nl.mpi.lamus.workspace.replace.action.implementation;

import java.util.ArrayList;
import java.util.List;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionExecutor;
import nl.mpi.lamus.workspace.replace.action.ReplaceActionManager;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
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
public class LamusReplaceActionManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock ReplaceActionExecutor mockReplaceActionExecutor;
    
    @Mock List<NodeReplaceAction> mockActionList;
    @Mock NodeReplaceAction mockAction;
    
    @Mock NodeReplaceAction mockAction1;
    @Mock NodeReplaceAction mockAction2;
    @Mock NodeReplaceAction mockAction3;
    
    private ReplaceActionManager replaceActionManager;
    
    
    public LamusReplaceActionManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        replaceActionManager = new LamusReplaceActionManager(mockReplaceActionExecutor);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void addActionToList_actionAlreadyExists() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockActionList).contains(mockAction); will(returnValue(Boolean.TRUE));
            never(mockActionList).add(mockAction);
        }});
        
        replaceActionManager.addActionToList(mockAction, mockActionList);
    }
    
    @Test
    public void addActionToList_actionDoesntExistYet() {
        
        context.checking(new Expectations() {{
            
            oneOf(mockActionList).contains(mockAction); will(returnValue(Boolean.FALSE));
            oneOf(mockActionList).add(mockAction);
        }});
        
        replaceActionManager.addActionToList(mockAction, mockActionList);
    }
    
    @Test
    public void applyActionsSuccessful() throws WorkspaceException {
        
        List<NodeReplaceAction> actions = new ArrayList<NodeReplaceAction>();
        actions.add(mockAction1);
        actions.add(mockAction2);
        actions.add(mockAction3);
        
        context.checking(new Expectations() {{
            
            oneOf(mockReplaceActionExecutor).execute(mockAction1);
            oneOf(mockReplaceActionExecutor).execute(mockAction2);
            oneOf(mockReplaceActionExecutor).execute(mockAction3);
        }});
        
        replaceActionManager.applyActions(actions);
    }
    
    @Test
    public void applyActionsUnsuccessful() throws WorkspaceException {
        
        List<NodeReplaceAction> actions = new ArrayList<NodeReplaceAction>();
        actions.add(mockAction1);
        actions.add(mockAction2);
        actions.add(mockAction3);
        
        final int workspaceID = 10;
        final WorkspaceException expectedException = new WorkspaceException("some exception message", workspaceID, null);
        
        context.checking(new Expectations() {{
            oneOf(mockReplaceActionExecutor).execute(mockAction1); will(throwException(expectedException));
        }});
        
        try {
            replaceActionManager.applyActions(actions);
            fail("should have thrown exception");
        } catch(WorkspaceException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
}