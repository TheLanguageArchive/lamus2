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

import nl.mpi.lamus.workspace.model.WorkspaceNode;
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
public class LinkNodeReplaceActionTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceNode mockAffectedNode;
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockOtherNode;
    
    private LinkNodeReplaceAction action;
    
    public LinkNodeReplaceActionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        action = new LinkNodeReplaceAction(mockAffectedNode, mockParentNode);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void actionsAreEqual() {
        
        LinkNodeReplaceAction actionToCompare = new LinkNodeReplaceAction(mockAffectedNode, mockParentNode);
        
        assertEquals("Actions should be considered equal", actionToCompare, action);
    }
    
    @Test
    public void actionsAreNotEqual() {
        
        LinkNodeReplaceAction actionToCompare = new LinkNodeReplaceAction(mockAffectedNode, mockOtherNode);
        
        assertFalse("Actions should not be considered equal", action.equals(actionToCompare));
    }
}