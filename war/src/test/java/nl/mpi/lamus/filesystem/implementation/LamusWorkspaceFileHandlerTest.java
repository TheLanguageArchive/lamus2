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
package nl.mpi.lamus.filesystem.implementation;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.transform.TransformerException;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceNodeFileException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFileHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    private WorkspaceFileHandler workspaceFileHandler;
    @Mock private Configuration mockConfiguration;
    @Mock private MetadataAPI mockMetadataAPI;
    @Mock private MetadataDocument mockMetadataDocument;
    @Mock private OutputStream mockOutputStream;
    
    public LamusWorkspaceFileHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        workspaceFileHandler = new LamusWorkspaceFileHandler(mockConfiguration);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of copyMetadataFileToWorkspace method, of class LamusWorkspaceFileHandler.
     */
    @Test
    public void copyMetadataFileToWorkspaceSuccessfully() throws IOException,
        TransformerException, MetadataException, FailedToCreateWorkspaceNodeFileException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory));
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockOutputStream);
            oneOf (mockOutputStream).close();
        }});
        
        workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI, mockMetadataDocument, mockOutputStream);
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsIOException() throws IOException,
        TransformerException, MetadataException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        
        File workspaceNodeFile = new File(baseDirectory, "" + testWorkspaceNode.getWorkspaceNodeID());
        
        final IOException expectedExceptionCause = new IOException("something bla bla");
        String expectedErrorMessage = "Problem writing file " + workspaceNodeFile.getAbsolutePath();
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory));
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockOutputStream); will(throwException(expectedExceptionCause));
            oneOf (mockOutputStream).close();
        }});
        
        try {
            workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI, mockMetadataDocument, mockOutputStream);
            fail("An exception should have been thrown");
        } catch(FailedToCreateWorkspaceNodeFileException fwsex) {
            assertEquals("Exception cause is different from expected", fwsex.getCause(), expectedExceptionCause);
            assertEquals("Exception error message is different from expected", fwsex.getMessage(), expectedErrorMessage);
            assertEquals("Workspace associated with exception is different from expected", fwsex.getWorkspace(), testWorkspace);
            assertEquals("Workspace Node associated with exception is different from expected", fwsex.getWorkspaceNode(), testWorkspaceNode);
        }
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsTransformerException() throws IOException,
        TransformerException, MetadataException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        
        File workspaceNodeFile = new File(baseDirectory, "" + testWorkspaceNode.getWorkspaceNodeID());
        
        final TransformerException expectedExceptionCause = new TransformerException("something bla bla");
        String expectedErrorMessage = "Problem writing file " + workspaceNodeFile.getAbsolutePath();
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory));
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockOutputStream); will(throwException(expectedExceptionCause));
            oneOf (mockOutputStream).close();
        }});
        
        try {
            workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI, mockMetadataDocument, mockOutputStream);
            fail("An exception should have been thrown");
        } catch(FailedToCreateWorkspaceNodeFileException fwsex) {
            assertEquals("Exception cause is different from expected", fwsex.getCause(), expectedExceptionCause);
            assertEquals("Exception error message is different from expected", fwsex.getMessage(), expectedErrorMessage);
            assertEquals("Workspace associated with exception is different from expected", fwsex.getWorkspace(), testWorkspace);
            assertEquals("Workspace Node associated with exception is different from expected", fwsex.getWorkspaceNode(), testWorkspaceNode);
        }
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsMetadataException() throws IOException, TransformerException, MetadataException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        
        File workspaceNodeFile = new File(baseDirectory, "" + testWorkspaceNode.getWorkspaceNodeID());
        
        final MetadataException expectedExceptionCause = new MetadataException("something bla bla");
        String expectedErrorMessage = "Problem writing file " + workspaceNodeFile.getAbsolutePath();
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory));
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockOutputStream); will(throwException(expectedExceptionCause));
            oneOf (mockOutputStream).close();
        }});
        
        try {
            workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI, mockMetadataDocument, mockOutputStream);
            fail("An exception should have been thrown");
        } catch(FailedToCreateWorkspaceNodeFileException fwsex) {
            assertEquals("Exception cause is different from expected", fwsex.getCause(), expectedExceptionCause);
            assertEquals("Exception error message is different from expected", fwsex.getMessage(), expectedErrorMessage);
            assertEquals("Workspace associated with exception is different from expected", fwsex.getWorkspace(), testWorkspace);
            assertEquals("Workspace Node associated with exception is different from expected", fwsex.getWorkspaceNode(), testWorkspaceNode);
        }
    }
    
    @Test
    public void getOutputStreamForWorkspaceNodeFileSuccessfully() throws IOException, FailedToCreateWorkspaceNodeFileException {
        
        Workspace testWorkspace = createTestWorkspace();
        File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        
        File workspaceNodeFile = new File(workspaceDirectory, "" + testWorkspaceNode.getWorkspaceNodeID());
        
        OutputStream retrievedOutputStream = 
                workspaceFileHandler.getOutputStreamForWorkspaceNodeFile(testWorkspace, testWorkspaceNode, workspaceNodeFile);
        
        assertNotNull("Resulting OutputStream is null", retrievedOutputStream);
        assertTrue("Resulting OutputStream is not of the expected type (FileOutputStream).", retrievedOutputStream instanceof FileOutputStream);
    }
    
    @Test
    public void getOutputStreamForWorkspaceNodeFileThrowsFileNotFoundException() throws IOException {
        
        Workspace testWorkspace = createTestWorkspace();
        
        File baseDirectory = createTestBaseDirectory();
        createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        baseDirectory.setWritable(false);
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        
        File workspaceNodeFile = new File(baseDirectory, "" + testWorkspaceNode.getWorkspaceNodeID());
        
        String expectedErrorMessage = "Problem with file " + workspaceNodeFile.getAbsolutePath();
        
        try {
            workspaceFileHandler.getOutputStreamForWorkspaceNodeFile(testWorkspace, testWorkspaceNode, workspaceNodeFile);
            fail("An exception should have been thrown");
        } catch(FailedToCreateWorkspaceNodeFileException fwsex) {
            assertTrue("Exception cause is different from expected", fwsex.getCause() instanceof FileNotFoundException);
            assertEquals("Exception error message is different from expected", fwsex.getMessage(), expectedErrorMessage);
            assertEquals("Workspace associated with exception is different from expected", fwsex.getWorkspace(), testWorkspace);
            assertEquals("Workspace Node associated with exception is different from expected", fwsex.getWorkspaceNode(), testWorkspaceNode);
        }
    }
    
    @Test
    public void getFileForWorkspaceNodeSuccessfully() throws IOException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = new File("workspace_base_directory");
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        
        File expectedFile = new File(baseDirectory, "" + testWorkspaceNode.getWorkspaceNodeID());
        
        context.checking(new Expectations() {{
            
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory));
        }});
        
        File retrievedFile = workspaceFileHandler.getFileForWorkspaceNode(testWorkspaceNode);
        
        assertEquals(expectedFile, retrievedFile);
    }

    
    private Workspace createTestWorkspace() {
        Workspace workspace = new LamusWorkspace("someUser", 0L, 10000000L);
        workspace.setWorkspaceID(1);
        
        return workspace;
    }
    
    private File createTestBaseDirectory() {
        File baseDirectory = testFolder.newFolder("workspace_base_directory");
        return baseDirectory;
    }
    
    private File createTestWorkspaceDirectory(File baseDirectory, int workspaceID) {
        File workspaceDirectory = new File(baseDirectory, "" + workspaceID);
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        
        return workspaceDirectory;
    }
        
    private WorkspaceNode createTestWorkspaceNode(int workspaceID) throws MalformedURLException {
        int archiveNodeID = 100;
        URL archiveNodeURL = new URL("http://some.url");
        WorkspaceNode node = new LamusWorkspaceNode(
                workspaceID, archiveNodeID, archiveNodeURL, archiveNodeURL);
        node.setName("someNode");
        node.setType(WorkspaceNodeType.METADATA);
        node.setFormat("someFormat");
        node.setStatus(WorkspaceNodeStatus.NODE_CREATED);

        return node;
    }

}
