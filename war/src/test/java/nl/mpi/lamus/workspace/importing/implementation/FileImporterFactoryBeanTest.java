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
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class FileImporterFactoryBeanTest {
    
    private FileImporterFactoryBean fileImporterFactoryBean;
    
    public FileImporterFactoryBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        fileImporterFactoryBean = new FileImporterFactoryBean();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getMetadataImporter() throws Exception {
        Reference metadataReference = new MetadataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        fileImporterFactoryBean.setFileImporterTypeForReference(metadataReference);
        
        FileImporter retrievedFileImporter = fileImporterFactoryBean.getObject();
        
        assertNotNull(retrievedFileImporter);
        assertTrue(retrievedFileImporter instanceof MetadataFileImporter);
    }

    @Test
    public void getResourceImporter() throws Exception {
        Reference resourceReference = new DataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        fileImporterFactoryBean.setFileImporterTypeForReference(resourceReference);
        
        FileImporter retrievedFileImporter = fileImporterFactoryBean.getObject();
        
        assertNotNull(retrievedFileImporter);
        assertTrue(retrievedFileImporter instanceof ResourceFileImporter);
    }
    
    @Test
    public void fileImporterTypeSetForMetadataReference() throws URISyntaxException {

        Reference metadataReference = new MetadataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        fileImporterFactoryBean.setFileImporterTypeForReference(metadataReference);
        
        assertEquals("Retrieved object should be of type MetadataFileImporter", MetadataFileImporter.class, fileImporterFactoryBean.getObjectType());
    }

    @Test
    public void fileImporterTypeSetForResourceReference() throws URISyntaxException {

        Reference metadataReference = new DataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        fileImporterFactoryBean.setFileImporterTypeForReference(metadataReference);
        
        assertEquals("Retrieved object should be of type ResourceFileImporter", ResourceFileImporter.class, fileImporterFactoryBean.getObjectType());
    }
        
    @Test
    public void testIsSingleton() {
        assertFalse("Bean should not be a singleton", fileImporterFactoryBean.isSingleton());
    }
}
