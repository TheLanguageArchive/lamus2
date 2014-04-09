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
package nl.mpi.lamus.workspace.exporting.implementation;

import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author guisil
 */
public class ReplacedNodeExporterTest {
    
    public ReplacedNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getWorkspace method, of class ReplacedNodeExporter.
     */
    @Test
    public void testGetWorkspace() {
        System.out.println("getWorkspace");
        ReplacedNodeExporter instance = new ReplacedNodeExporter();
        Workspace expResult = null;
        Workspace result = instance.getWorkspace();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWorkspace method, of class ReplacedNodeExporter.
     */
    @Test
    public void testSetWorkspace() {
        System.out.println("setWorkspace");
        Workspace workspace = null;
        ReplacedNodeExporter instance = new ReplacedNodeExporter();
        instance.setWorkspace(workspace);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of exportNode method, of class ReplacedNodeExporter.
     */
    @Test
    public void testExportNode() {
        System.out.println("exportNode");
        WorkspaceNode parentNode = null;
        WorkspaceNode currentNode = null;
        ReplacedNodeExporter instance = new ReplacedNodeExporter();
        instance.exportNode(parentNode, currentNode);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}