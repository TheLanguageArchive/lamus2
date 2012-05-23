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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
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
import nl.mpi.metadata.cmdi.api.dom.CMDIDocumentReader;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.type.CMDIProfileContainer;
import nl.mpi.metadata.cmdi.api.type.CMDIProfileReader;
import nl.mpi.metadata.cmdi.api.type.CMDITypeException;
import nl.mpi.metadata.cmdi.util.CMDIEntityResolver;
import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceFileHandlerTest {
    
    private static Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    private WorkspaceFileHandler workspaceFileHandler;
    @Mock private Configuration mockConfiguration;
    @Mock private MetadataAPI mockMetadataAPI;
    @Mock private MetadataDocument mockMetadataDocument;
    @Mock private File mockNodeFile;
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
        TransformerException, MetadataException, FailedToCreateWorkspaceNodeFileException,
        CMDITypeException, ParserConfigurationException, SAXException, URISyntaxException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        File testNodeFile = new File(workspaceDirectory, testWorkspaceNode.getArchiveURL().getFile());
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
        }});
        
        workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI,
                mockMetadataDocument, testNodeFile, mockStreamResult);
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsIOException() throws IOException,
        TransformerException, MetadataException, CMDITypeException, ParserConfigurationException,
        SAXException, URISyntaxException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        File testNodeFile = new File(workspaceDirectory, testWorkspaceNode.getArchiveURL().getFile());
        
        final IOException expectedExceptionCause = new IOException("something bla bla");
        String expectedErrorMessage = "Problem writing file " + testNodeFile.getAbsolutePath();
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                will(throwException(expectedExceptionCause));
        }});
        
        try {
            workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI,
                    mockMetadataDocument, testNodeFile, mockStreamResult);
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
        TransformerException, MetadataException, CMDITypeException, ParserConfigurationException,
        SAXException, URISyntaxException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        File testNodeFile = new File(workspaceDirectory, testWorkspaceNode.getArchiveURL().getFile());
        
        final TransformerException expectedExceptionCause = new TransformerException("something bla bla");
        String expectedErrorMessage = "Problem writing file " + testNodeFile.getAbsolutePath();
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                will(throwException(expectedExceptionCause));
        }});
        
        try {
            workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI,
                    mockMetadataDocument, testNodeFile, mockStreamResult);
            fail("An exception should have been thrown");
        } catch(FailedToCreateWorkspaceNodeFileException fwsex) {
            assertEquals("Exception cause is different from expected", fwsex.getCause(), expectedExceptionCause);
            assertEquals("Exception error message is different from expected", fwsex.getMessage(), expectedErrorMessage);
            assertEquals("Workspace associated with exception is different from expected", fwsex.getWorkspace(), testWorkspace);
            assertEquals("Workspace Node associated with exception is different from expected", fwsex.getWorkspaceNode(), testWorkspaceNode);
        }
    }
    
    @Test
    public void copyMetadataFileToWorkspaceThrowsMetadataException() throws IOException,
        TransformerException, MetadataException, CMDITypeException, ParserConfigurationException,
        SAXException, URISyntaxException {
        
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        File testNodeFile = new File(workspaceDirectory, testWorkspaceNode.getArchiveURL().getFile());
        
        final MetadataException expectedExceptionCause = new MetadataException("something bla bla");
        String expectedErrorMessage = "Problem writing file " + testNodeFile.getAbsolutePath();
        
        context.checking(new Expectations() {{
            oneOf (mockMetadataAPI).writeMetadataDocument(mockMetadataDocument, mockStreamResult);
                will(throwException(expectedExceptionCause));
        }});
        
        try {
            workspaceFileHandler.copyMetadataFileToWorkspace(testWorkspace, testWorkspaceNode, mockMetadataAPI,
                    mockMetadataDocument, testNodeFile, mockStreamResult);
            fail("An exception should have been thrown");
        } catch(FailedToCreateWorkspaceNodeFileException fwsex) {
            assertEquals("Exception cause is different from expected", fwsex.getCause(), expectedExceptionCause);
            assertEquals("Exception error message is different from expected", fwsex.getMessage(), expectedErrorMessage);
            assertEquals("Workspace associated with exception is different from expected", fwsex.getWorkspace(), testWorkspace);
            assertEquals("Workspace Node associated with exception is different from expected", fwsex.getWorkspaceNode(), testWorkspaceNode);
        }
    }
    
    @Test
    public void getStreamResultForWorkspaceNodeFileSuccessfully() throws MalformedURLException {
        
        Workspace testWorkspace = createTestWorkspace();
        File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testWorkspaceNode = createTestWorkspaceNode(testWorkspace.getWorkspaceID());
        File testNodeFile = new File(workspaceDirectory, testWorkspaceNode.getArchiveURL().getFile());
        
        StreamResult retrievedStreamResult = 
                workspaceFileHandler.getStreamResultForWorkspaceNodeFile(testWorkspace, testWorkspaceNode, testNodeFile);
        
        assertNotNull("Resulting StreamResult is null", retrievedStreamResult);
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
        URL archiveNodeURL = new URL("http://some.url/someNode.cmdi");
        WorkspaceNode node = new LamusWorkspaceNode(
                workspaceID, archiveNodeID, archiveNodeURL, archiveNodeURL);
        node.setName("someNode");
        node.setType(WorkspaceNodeType.METADATA);
        node.setFormat("someFormat");
        node.setStatus(WorkspaceNodeStatus.NODE_CREATED);

        return node;
    }
    
}
