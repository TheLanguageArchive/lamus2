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
package nl.mpi.lamus.workspace.importing.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.NodeImporterAssigner;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
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
public class LamusNodeImporterAssignerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock MetadataNodeImporter mockMetadataNodeImporter;
    @Mock ResourceNodeImporter mockResourceNodeImporter;
    
    private NodeImporterAssigner nodeImporterAssigner;
    
    public LamusNodeImporterAssignerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        nodeImporterAssigner = new LamusNodeImporterAssigner();
        
        ReflectionTestUtils.setField(nodeImporterAssigner, "metadataNodeImporter", mockMetadataNodeImporter);
        ReflectionTestUtils.setField(nodeImporterAssigner, "resourceNodeImporter", mockResourceNodeImporter);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void getMetadataImporter() throws URISyntaxException {
        Reference metadataReference = new MetadataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        
        NodeImporter retrievedNodeImporter = nodeImporterAssigner.getImporterForReference(metadataReference);
        
        assertNotNull(retrievedNodeImporter);
        assertTrue(retrievedNodeImporter instanceof MetadataNodeImporter);
    }

    @Test
    public void getResourceImporter() throws URISyntaxException {
        Reference resourceReference = new DataResourceProxy("id", new URI("htto:/lalala.la"), "mimetype");
        
        NodeImporter retrievedNodeImporter = nodeImporterAssigner.getImporterForReference(resourceReference);
        
        assertNotNull(retrievedNodeImporter);
        assertTrue(retrievedNodeImporter instanceof ResourceNodeImporter);
    }
    
    @Test
    public void getImporterForDifferentType() throws URISyntaxException {
        Reference otherReference = new SomeOtherReference();
        
        String expectedExceptionMessage = "Unexpected reference type";
        
        try {
            nodeImporterAssigner.getImporterForReference(otherReference);
            fail("should have thrown exception");
        } catch(IllegalArgumentException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    
    private class SomeOtherReference implements Reference {

        @Override
        public URI getURI() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setURI(URI uri) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public URI getLocation() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setLocation(URI uri) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getMimetype() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setMimeType(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}