/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.importing.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class NodeImporterFactoryBeanTest {
    
    private NodeImporterFactoryBean nodeImporterFactoryBean;
    
    public NodeImporterFactoryBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        nodeImporterFactoryBean = new NodeImporterFactoryBean();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getMetadataImporter() throws Exception {
        Reference metadataReference = new MetadataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        nodeImporterFactoryBean.setNodeImporterTypeForReference(metadataReference);
        
        NodeImporter retrievedNodeImporter = nodeImporterFactoryBean.getObject();
        
        assertNotNull(retrievedNodeImporter);
        assertTrue(retrievedNodeImporter instanceof MetadataNodeImporter);
    }

    @Test
    public void getResourceImporter() throws Exception {
        Reference resourceReference = new DataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        nodeImporterFactoryBean.setNodeImporterTypeForReference(resourceReference);
        
        NodeImporter retrievedNodeImporter = nodeImporterFactoryBean.getObject();
        
        assertNotNull(retrievedNodeImporter);
        assertTrue(retrievedNodeImporter instanceof ResourceNodeImporter);
    }
    
    @Test
    public void nodeImporterTypeSetForMetadataReference() throws URISyntaxException {
        Reference metadataReference = new MetadataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        nodeImporterFactoryBean.setNodeImporterTypeForReference(metadataReference);
        
        assertEquals("Retrieved object should be of type MetadataNodeImporter", MetadataNodeImporter.class, nodeImporterFactoryBean.getObjectType());
    }

    @Test
    public void nodeImporterTypeSetForResourceReference() throws URISyntaxException {
        Reference metadataReference = new DataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        nodeImporterFactoryBean.setNodeImporterTypeForReference(metadataReference);
        
        assertEquals("Retrieved object should be of type ResourceNodeImporter", ResourceNodeImporter.class, nodeImporterFactoryBean.getObjectType());
    }
        
    @Test
    public void testIsSingleton() {
        assertFalse("Bean should not be a singleton", nodeImporterFactoryBean.isSingleton());
    }
}
