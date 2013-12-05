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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.filesystem.LamusFilesystemTestBeans;
import nl.mpi.lamus.filesystem.LamusFilesystemTestProperties;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.cmdi.api.type.CMDITypeException;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.FileUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.xml.sax.SAXException;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusFilesystemTestProperties.class, LamusFilesystemTestBeans.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusWorkspaceFileHandlerTest {
    
    private static Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    private File tempDirectory;
    
    @Mock private MetadataAPI mockMetadataAPI;
    @Mock private MetadataDocument mockMetadataDocument;
    @Mock private StreamResult mockStreamResult;
    
    public LamusWorkspaceFileHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws IOException {
        FileUtils.cleanDirectory(workspaceBaseDirectory);
    }
    
    @After
    public void tearDown() throws IOException {
        FileUtils.cleanDirectory(workspaceBaseDirectory);
        if(tempDirectory != null) {
            FileUtils.cleanDirectory(tempDirectory);
        }
    }

    /**
     * Test of copyMetadataFileToWorkspace method, of class LamusWorkspaceFileHandler.
     */
    @Test
    public void copyMetadataFileToWorkspaceSuccessfully() throws IOException,
        TransformerException, MetadataException,
        CMDITypeException, ParserConfigurationException, SAXException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestMetadataWorkspaceNode(testWorkspace.getWorkspaceID(), nodeFilename);
        File testNodeFile = new File(workspaceDirectory, archiveNodeURL.getFile());
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
        }});
        
        workspaceFileHandler.copyMetadataFile(testWorkspaceNode, mockMetadataAPI,
                mockMetadataDocument, testNodeFile, mockStreamResult);
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsIOException() throws IOException,
        TransformerException, MetadataException, CMDITypeException, ParserConfigurationException,
        SAXException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestMetadataWorkspaceNode(testWorkspace.getWorkspaceID(), nodeFilename);
        File testNodeFile = new File(workspaceDirectory, archiveNodeURL.getFile());
        
        String expectedErrorMessage = "Problem writing file " + testNodeFile.getAbsolutePath();
        final IOException expectedException = new IOException(expectedErrorMessage);
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                will(throwException(expectedException));
        }});
        
        try {
            workspaceFileHandler.copyMetadataFile(testWorkspaceNode, mockMetadataAPI,
                    mockMetadataDocument, testNodeFile, mockStreamResult);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            assertEquals("Exception is different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsTransformerException() throws IOException,
        TransformerException, MetadataException, CMDITypeException, ParserConfigurationException,
        SAXException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestMetadataWorkspaceNode(testWorkspace.getWorkspaceID(), nodeFilename);
        File testNodeFile = new File(workspaceDirectory, archiveNodeURL.getFile());
        
        String expectedErrorMessage = "Problem writing file " + testNodeFile.getAbsolutePath();
        final TransformerException expectedException = new TransformerException(expectedErrorMessage);
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                will(throwException(expectedException));
        }});
        
        try {
            workspaceFileHandler.copyMetadataFile(testWorkspaceNode, mockMetadataAPI,
                    mockMetadataDocument, testNodeFile, mockStreamResult);
            fail("An exception should have been thrown");
        } catch(TransformerException ex) {
            assertEquals("Exception is different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsMetadataException() throws IOException,
        TransformerException, MetadataException, CMDITypeException, ParserConfigurationException,
        SAXException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestMetadataWorkspaceNode(testWorkspace.getWorkspaceID(), nodeFilename);
        File testNodeFile = new File(workspaceDirectory, archiveNodeURL.getFile());
        
        String expectedErrorMessage = "Problem writing file " + testNodeFile.getAbsolutePath();
        final MetadataException expectedException = new MetadataException(expectedErrorMessage);
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                will(throwException(expectedException));
        }});
        
        try {
            workspaceFileHandler.copyMetadataFile(testWorkspaceNode, mockMetadataAPI,
                    mockMetadataDocument, testNodeFile, mockStreamResult);
            fail("An exception should have been thrown");
        } catch(MetadataException ex) {
            assertEquals("Exception is different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void copyResourceFileSuccessfully() throws MalformedURLException, IOException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testNode = createTestResourceWorkspaceNode(testWorkspace.getWorkspaceID(), workspaceDirectory, nodeFilename);
        
        File originFile = new File(testNode.getWorkspaceURL().getPath());
        File destinationFile = new File(workspaceDirectory, "someRandomLocation.txt");
        
        workspaceFileHandler.copyResourceFile(testNode, originFile, destinationFile);
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
    }
    
    @Test
    public void copyResourceFileThrowsException() throws MalformedURLException, IOException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testNode = createTestResourceWorkspaceNode(testWorkspace.getWorkspaceID(), workspaceDirectory, nodeFilename);
        
        File originFile = new File(testNode.getWorkspaceURL().getPath());
        File destinationFile = new File(workspaceDirectory, "someRandomLocation.txt");
        
        destinationFile.createNewFile();
        destinationFile.setReadOnly();
        
        try {
            workspaceFileHandler.copyResourceFile(testNode, originFile, destinationFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            assertTrue("Exception has different type than expected", ex instanceof IOException);
            
            //TODO Should mock the call to FileUtils.copyFile in order to properly compare the exception with an expected one
        }
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
    }
    
    @Test
    public void copyFileSuccessfully() throws IOException {
        
        final int workspaceID = 1;
        final File baseDirectory = createTestBaseDirectory();
        final File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, workspaceID);
        final File uploadDirectory = createTestUploadDirectory(workspaceDirectory);
        
        final File fileToCopy = createFileToCopy();
        
        final File destinationFile = new File(uploadDirectory, FilenameUtils.getName(fileToCopy.getPath()));
        
        workspaceFileHandler.copyFile(workspaceID, fileToCopy, destinationFile);
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
    }
    
    @Test
    public void copyFileThrowsException() throws IOException {
        
        final int workspaceID = 1;
        final File baseDirectory = createTestBaseDirectory();
        final File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, workspaceID);
        final File uploadDirectory = createTestUploadDirectory(workspaceDirectory);
        
        final File fileToCopy = doNotCreateFileToCopy();
        
        final File destinationFile = new File(uploadDirectory, FilenameUtils.getName(fileToCopy.getPath()));
        
        destinationFile.createNewFile();
        destinationFile.setReadOnly();
        
        try {
            workspaceFileHandler.copyFile(workspaceID, fileToCopy, destinationFile);
            fail("An exception should have been thrown");
        } catch (IOException ex) {
            assertTrue("Exception has different type than expected", ex instanceof IOException);
            
            //TODO Should mock the call to FileUtils.copyFile in order to properly compare the exception with an expected one
        }
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
    }
    
    @Test
    public void getStreamResultForWorkspaceNodeFileSuccessfully() throws MalformedURLException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestMetadataWorkspaceNode(testWorkspace.getWorkspaceID(), nodeFilename);
        File testNodeFile = new File(workspaceDirectory, archiveNodeURL.getFile());
        
        StreamResult retrievedStreamResult = 
                workspaceFileHandler.getStreamResultForNodeFile(testNodeFile);
        
        assertNotNull("Resulting StreamResult is null", retrievedStreamResult);
    }
    
    @Test
    public void getFileForWorkspaceNodeSuccessfully() throws IOException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        WorkspaceNode testWorkspaceNode = createTestMetadataWorkspaceNode(testWorkspace.getWorkspaceID(), nodeFilename);
        
        File expectedWorkspaceDirectory = new File(workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
//        String nodeFilename = FilenameUtils.getName(testWorkspaceNode.getArchiveURL().toString());
        File expectedNodeFile = new File(expectedWorkspaceDirectory, nodeFilename);
        
        File retrievedFile = workspaceFileHandler.getFileForImportedWorkspaceNode(archiveNodeURL, testWorkspaceNode);
        
        assertEquals(expectedNodeFile, retrievedFile);
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
        assertTrue("Workspace directory wasn't created.", workspaceDirectory.exists());
        
        return workspaceDirectory;
    }
    
    private File createTestUploadDirectory(File workspaceDirectory) {
        File uploadDirectory = new File(workspaceDirectory, "upload");
        boolean isDirectoryCreated = uploadDirectory.mkdirs();
        
        assertTrue("Upload directory was not successfully created.", isDirectoryCreated);
        assertTrue("Workspace directory wasn't created.", uploadDirectory.exists());
        
        return uploadDirectory;
    }
        
    private WorkspaceNode createTestMetadataWorkspaceNode(int workspaceID, String filename) throws URISyntaxException, MalformedURLException {
        URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        URL archiveNodeURL = new URL("file:/workspace/folder/node.cmdi");
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        node.setName(filename);
        node.setType(WorkspaceNodeType.METADATA);
        node.setFormat("someFormat");
        node.setStatus(WorkspaceNodeStatus.NODE_CREATED);

        return node;
    }
    
    private WorkspaceNode createTestResourceWorkspaceNode(int workspaceID, File workspaceDirectory, String filename) throws IOException, URISyntaxException {
        URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        URL archiveNodeURL = new URL("file:/workspace/folder/node.cmdi");
        WorkspaceNode node = new LamusWorkspaceNode(
                workspaceID, archiveNodeURI, archiveNodeURL);
        
        File workspaceNodeFile = new File(workspaceDirectory, filename);
        workspaceNodeFile.createNewFile();
        node.setWorkspaceURL(workspaceNodeFile.toURI().toURL());
        node.setName(filename);
        node.setType(WorkspaceNodeType.RESOURCE_WR);
        node.setFormat("someFormat");
        node.setStatus(WorkspaceNodeStatus.NODE_CREATED);

        return node;
    }
    
    private File createFileToCopy() throws IOException {
        tempDirectory = testFolder.newFolder("temp_directory");

        assertTrue("Temp directory wasn't created.", tempDirectory.exists());
        
        File tempFile = new File(tempDirectory, "temp_file.txt");
        boolean isFileCreated = tempFile.createNewFile();
        
        assertTrue("Temp file was not successfuly created.", isFileCreated);
        assertTrue("Temp file wasn't created.", tempFile.exists());
        
        return tempFile;
    }
    
    private File doNotCreateFileToCopy() throws IOException {
        tempDirectory = testFolder.newFolder("temp_directory");

        assertTrue("Temp directory wasn't created.", tempDirectory.exists());
        
        File tempFile = new File(tempDirectory, "temp_file.txt");
        
        assertFalse("Temp file should not have been created.", tempFile.exists());
        
        return tempFile;
    }
}
