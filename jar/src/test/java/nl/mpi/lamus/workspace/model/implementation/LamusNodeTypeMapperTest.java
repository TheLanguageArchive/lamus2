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
package nl.mpi.lamus.workspace.model.implementation;

import nl.mpi.lamus.workspace.model.NodeTypeMapper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
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
public class LamusNodeTypeMapperTest {
    
    private NodeTypeMapper typeMapper;
    
    
    public LamusNodeTypeMapperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        typeMapper = new LamusNodeTypeMapper();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getNodeTypeForTextFile() {
        
        String mimetype = "text/plain";
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_WR;
        
        WorkspaceNodeType retrievedType = typeMapper.getNodeTypeForMimetype(mimetype);
        assertEquals("Retrieved type different from expected", expectedType, retrievedType);
    }
    
    @Test
    public void getNodeTypeForPdf() {
        
        String mimetype = "application/pdf";
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_WR;
        
        WorkspaceNodeType retrievedType = typeMapper.getNodeTypeForMimetype(mimetype);
        assertEquals("Retrieved type different from expected", expectedType, retrievedType);
    }
}