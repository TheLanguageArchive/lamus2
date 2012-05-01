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

import java.lang.reflect.InvocationTargetException;
import nl.mpi.lamus.workspace.exception.FileImporterInitialisationException;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.FileImporterFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.metadata.api.model.MetadataReference;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ResourceReference;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceFileImporterFactoryTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private FileImporterFactory fileImporterFactory;
    @Mock private Workspace mockWorkspace;
    
    public WorkspaceFileImporterFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        this.fileImporterFactory = new WorkspaceFileImporterFactory(mockWorkspace);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getFileImporterTypeForReference method, of class WorkspaceFileImporterFactory.
     */
    @Test
    public void getMetadataFileImporterTypeForMetadataReference() {

        Class<? extends FileImporter> expectedImporterType = MetadataFileImporter.class;
        Class<? extends FileImporter> retrievedImporterType = fileImporterFactory.getFileImporterTypeForReference(MetadataReference.class);
        assertTrue("Not the expected type of importer", expectedImporterType == retrievedImporterType);
    }
    
    /**
     * Test of getFileImporterTypeForReference method, of class WorkspaceFileImporterFactory.
     */
    @Test
    public void getResourceFileImporterTypeForResourceReference() {

        Class<? extends FileImporter> expectedImporterType = ResourceFileImporter.class;
        Class<? extends FileImporter> retrievedImporterType = fileImporterFactory.getFileImporterTypeForReference(ResourceReference.class);
        assertTrue("Not the expected type of importer", expectedImporterType == retrievedImporterType);
    }

    /**
     * Test of getFileImporterTypeForTopNode method, of class WorkspaceFileImporterFactory.
     */
    @Test
    public void getMetadataFileImporterTypeForTopNode() {
        
        Class<? extends FileImporter> expectedImporterType = MetadataFileImporter.class;
        Class<? extends FileImporter> retrievedImporterType = fileImporterFactory.getFileImporterTypeForTopNode();
        assertTrue("Not the expected type of importer.", expectedImporterType == retrievedImporterType);
    }
    
    /**
     * Test of getNewFileImporterOfType method, of class WorkspaceFileImporterFactory
     */
    @Test
    public void getNewFileImporterOfType() throws FileImporterInitialisationException {
        
        Class<? extends FileImporter> expectedImporterType = MetadataFileImporter.class;
        FileImporter retrievedImporter = fileImporterFactory.getNewFileImporterOfType(expectedImporterType);
        assertNotNull("Retrieved importer should not be null.", retrievedImporter);
        assertTrue("Not the expected type of importer.", expectedImporterType == retrievedImporter.getClass());
    }
    
    /**
     * Test of getNewFileImporterOfType method, of class WorkspaceFileImporterFactory
     */
    @Test
    public void fileImporterDoesntHaveDefaultConstructor() {
        
        Class<? extends FileImporter> testImporterType = FileImporterWithoutDefaultConstructor.class;
        try {
            fileImporterFactory.getNewFileImporterOfType(testImporterType);
        } catch(FileImporterInitialisationException ex) {
            assertNotNull("Exception cause should not be null.", ex.getCause());
            assertTrue("Exception cause is not of the expected type.", ex.getCause() instanceof NoSuchMethodException);
            assertEquals("Exception message is not the expected one.",
                    "FileImporter subtype does not have a default constructor.", ex.getMessage());
        }
    }
    
    //TODO When is a SecurityException thrown in Class.getDeclaredConstructor?
    
    /**
     * Test of getNewFileImporterOfType method, of class WorkspaceFileImporterFactory
     */
    @Test
    public void fileImporterIsAnAbstractClass() {
        
        Class<? extends FileImporter> testImporterType = AbstractFileImporter.class;
        try {
            fileImporterFactory.getNewFileImporterOfType(testImporterType);
        } catch(FileImporterInitialisationException ex) {
            assertNotNull("Exception cause should not be null.", ex.getCause());
            assertTrue("Exception cause is not of the expected type.", ex.getCause() instanceof InstantiationException);
            assertEquals("Exception message is not the expected one.",
                    "FileImporter subtype could not be instantiated because it is an abstract class.", ex.getMessage());
        }
    }
    
    /**
     * Test of getNewFileImporterOfType method, of class WorkspaceFileImporterFactory
     */
    @Test
    public void fileImporterHaveInaccessibleDefaultConstructor() {
        
        Class<? extends FileImporter> testImporterType = FileImporterWithInaccessibleDefaultConstructor.class;
        try {
            fileImporterFactory.getNewFileImporterOfType(testImporterType);
        } catch(FileImporterInitialisationException ex) {
            assertNotNull("Exception cause should not be null.", ex.getCause());
            assertTrue("Exception cause is not of the expected type.", ex.getCause() instanceof IllegalAccessException);
            assertEquals("Exception message is not the expected one.",
                    "FileImporter subtype's constructor is inaccessible.", ex.getMessage());
        }
    }
    
    //TODO IllegalArgumentException only happens if the constructor has a different parameter
        // this is not happening at the moment because only the default constructor (without parameters) is called
    
    /**
     * Test of getNewFileImporterOfType method, of class WorkspaceFileImporterFactory
     */
    @Test
    public void fileImporterWithConstructorThrowingException() {
        
        Class<? extends FileImporter> testImporterType = FileImporterWithExceptionThrowingConstructor.class;
        try {
            fileImporterFactory.getNewFileImporterOfType(testImporterType);
        } catch(FileImporterInitialisationException ex) {
            assertNotNull("Exception cause should not be null.", ex.getCause());
            assertTrue("Exception cause is not of the expected type.", ex.getCause() instanceof InvocationTargetException);
            assertEquals("Exception message is not the expected one.",
                    "FileImporter subtype's constructor threw an exception.", ex.getMessage());
        }
    }
    
}

class FileImporterWithoutDefaultConstructor implements FileImporter {

    FileImporterWithoutDefaultConstructor(String someArgument) {
        
    }
    
    public void importFile(WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument, Reference childLink, int childNodeArchiveID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
}

abstract class AbstractFileImporter implements FileImporter {

    AbstractFileImporter() {
        
    }
    
    public void importFile(WorkspaceNode parentNode, Reference childLink, int childNodeArchiveID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

class FileImporterWithInaccessibleDefaultConstructor implements FileImporter {

    private FileImporterWithInaccessibleDefaultConstructor() {
        
    }
    
    public void importFile(WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument, Reference childLink, int childNodeArchiveID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

class FileImporterWithExceptionThrowingConstructor implements FileImporter {
    
    FileImporterWithExceptionThrowingConstructor() throws Exception {
        throw new Exception();
    }

    public void importFile(WorkspaceNode parentNode, ReferencingMetadataDocument parentDocument, Reference childLink, int childNodeArchiveID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}