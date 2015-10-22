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
import nl.mpi.lamus.workspace.replace.action.ReplaceActionFactory;
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
public class LamusReplaceActionFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock WorkspaceNode mockAffectedNode;
    @Mock WorkspaceNode mockNewNode;
    @Mock WorkspaceNode mockParentNode;
    
    private ReplaceActionFactory replaceActionFactory;
    
    
    public LamusReplaceActionFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        replaceActionFactory = new LamusReplaceActionFactory();
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getDeleteAction() {
        
        DeleteNodeReplaceAction retrievedAction = replaceActionFactory.getDeleteAction(mockAffectedNode);
        
        assertEquals("Action affected node different from expected", mockAffectedNode, retrievedAction.getAffectedNode());
    }

    @Test
    public void getLinkAction() {
        
        LinkNodeReplaceAction retrievedAction = replaceActionFactory.getLinkAction(mockAffectedNode, mockParentNode);
        
        assertEquals("Action affected node different from expected", mockAffectedNode, retrievedAction.getAffectedNode());
        assertEquals("Action parent node different from expected", mockParentNode, retrievedAction.getParentNode());
    }

    @Test
    public void getMoveLinkLocationAction() {
        
        MoveLinkLocationNodeReplaceAction retrievedAction = replaceActionFactory.getMoveLinkLocationAction(mockAffectedNode, mockParentNode);
        
        assertEquals("Action affected node different from expected", mockAffectedNode, retrievedAction.getAffectedNode());
        assertEquals("Action parent node different from expected", mockParentNode, retrievedAction.getParentNode());
    }

    @Test
    public void getReplaceAction() {
        
        ReplaceNodeReplaceAction retrievedAction = replaceActionFactory.getReplaceAction(mockAffectedNode, mockParentNode, mockNewNode, Boolean.TRUE);
        
        assertEquals("Action affected node different from expected", mockAffectedNode, retrievedAction.getAffectedNode());
        assertEquals("Action parent node different from expected", mockParentNode, retrievedAction.getParentNode());
        assertEquals("Action new node different from expected", mockNewNode, retrievedAction.getNewNode());
    }

    @Test
    public void getUnlinkAction() {
        
        UnlinkNodeReplaceAction retrievedAction = replaceActionFactory.getUnlinkAction(mockAffectedNode, mockParentNode);
        
        assertEquals("Action affected node different from expected", mockAffectedNode, retrievedAction.getAffectedNode());
        assertEquals("Action parent node different from expected", mockParentNode, retrievedAction.getParentNode());
    }
    
    @Test
    public void getRemoveArchiveUriAction() {
        
        RemoveArchiveUriReplaceAction retrievedAction = replaceActionFactory.getRemoveArchiveUriAction(mockAffectedNode, mockParentNode);
        
        assertEquals("Action affected node different from expected", mockAffectedNode, retrievedAction.getAffectedNode());
        assertEquals("Action parent node different from expected", mockParentNode, retrievedAction.getParentNode());
    }
    
    @Test
    public void getUnlinkFromOldParentAction() {
        
        UnlinkNodeFromReplacedParentReplaceAction retrievedAction = replaceActionFactory.getUnlinkFromOldParentAction(mockAffectedNode, mockParentNode);
        
        assertEquals("Action affected node different from expected", mockAffectedNode, retrievedAction.getAffectedNode());
        assertEquals("Action new parent node different from expected", mockParentNode, retrievedAction.getNewParentNode());
    }
}