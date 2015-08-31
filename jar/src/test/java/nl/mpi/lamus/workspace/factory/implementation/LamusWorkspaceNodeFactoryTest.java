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
package nl.mpi.lamus.workspace.factory.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.model.HandleCarrier;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import org.apache.commons.io.FilenameUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceNodeFactoryTest {
    
    private WorkspaceNodeFactory factory;
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    @Mock TestReferencingMetadataDocumentHandleCarrier mockTestReferencingMetadataDocumentHandleCarrier;
    @Mock MetadataDocumentType mockMetadataDocumentType;
    @Mock TestReferenceHandleCarrier mockTestReferenceHandleCarrier;
    
    @Mock CorpusNode mockCorpusNode;
    
    public LamusWorkspaceNodeFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        factory = new LamusWorkspaceNodeFactory(mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNewWorkspaceNode method, of class LamusWorkspaceNodeFactory.
     */
    @Test
    public void workspaceNodeCorrectlyInitialised() throws URISyntaxException, MalformedURLException {

        int testWorkspaceID = 10;
        URI testArchiveNodeURI = URI.create(UUID.randomUUID().toString());
        URL testArchiveNodeURL = new URL("file:/archive/folder/node.cmdi");
        
        WorkspaceNode testWorkspaceNode = factory.getNewWorkspaceNode(testWorkspaceID, testArchiveNodeURI, testArchiveNodeURL);
        
        assertEquals(testWorkspaceID, testWorkspaceNode.getWorkspaceID());
        assertEquals(testArchiveNodeURI, testWorkspaceNode.getArchiveURI());
    }
    
    @Test
    public void workspaceMetadataNodeCorrectlyInitialised() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/archive/folder/node.cmdi");
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String nodeFormat = "text/x-cmdi+xml";
        final URI schemaLocation = URI.create("http://some.location");
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.ARCHIVE_COPY;
        final boolean onSite = Boolean.TRUE;
        final boolean isProtected = Boolean.FALSE;
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode(workspaceID, nodeArchiveURI, nodeArchiveURL);
        expectedNode.setName(nodeName);
        expectedNode.setTitle(nodeName);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeFormat);
        expectedNode.setProfileSchemaURI(schemaLocation);
        expectedNode.setStatus(WorkspaceNodeStatus.ARCHIVE_COPY);
        expectedNode.setProtected(isProtected);

        context.checking(new Expectations() {{
            oneOf(mockTestReferencingMetadataDocumentHandleCarrier).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf(mockMetadataDocumentType).getSchemaLocation(); will(returnValue(schemaLocation));
        }});
        
        WorkspaceNode retrievedNode =
                factory.getNewWorkspaceMetadataNode(workspaceID, nodeArchiveURI, nodeArchiveURL, mockTestReferencingMetadataDocumentHandleCarrier, nodeName, onSite, isProtected);
        
        assertEquals("Retrieved workspace node is different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void workspaceExternalMetadataNodeCorrectlyInitialised() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/archive/folder/node.cmdi");
        final String nodeName = "someName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String nodeFormat = "text/x-cmdi+xml";
        final URI schemaLocation = URI.create("http://some.location");
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.EXTERNAL;
        final boolean onSite = Boolean.FALSE;
        final boolean isProtected = Boolean.TRUE;
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode(workspaceID, nodeArchiveURI, nodeArchiveURL);
        expectedNode.setName(nodeName);
        expectedNode.setTitle(nodeName);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeFormat);
        expectedNode.setProfileSchemaURI(schemaLocation);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);

        context.checking(new Expectations() {{
            oneOf(mockTestReferencingMetadataDocumentHandleCarrier).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf(mockMetadataDocumentType).getSchemaLocation(); will(returnValue(schemaLocation));
        }});
        
        WorkspaceNode retrievedNode =
                factory.getNewWorkspaceMetadataNode(workspaceID, nodeArchiveURI, nodeArchiveURL, mockTestReferencingMetadataDocumentHandleCarrier, nodeName, onSite, isProtected);
        
        assertEquals("Retrieved workspace node is different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void workspaceResourceNodeCorrectlyInitialised() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/archive/folder/node.cmdi");
        final String nodeName = "SomeName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String nodeMimetype = "text/plain";
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean onSite = Boolean.TRUE;
        final boolean isProtected = Boolean.FALSE;
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode(workspaceID, nodeArchiveURI, nodeArchiveURL);
        expectedNode.setName(nodeName);
        expectedNode.setTitle(nodeName);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeMimetype);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode =
                factory.getNewWorkspaceNode(workspaceID, nodeArchiveURI,
                        nodeArchiveURL, mockTestReferenceHandleCarrier,
                        nodeMimetype, nodeType, nodeName, onSite, isProtected);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void workspaceResourceNodeCorrectlyInitialised_NullName() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/archive/folder/node.cmdi");
        final String nameToUse = FilenameUtils.getName(nodeArchiveURL.getPath());
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String nodeMimetype = "text/plain";
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.VIRTUAL;
        final boolean onSite = Boolean.TRUE;
        final boolean isProtected = Boolean.FALSE;
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode(workspaceID, nodeArchiveURI, nodeArchiveURL);
        expectedNode.setName(nameToUse);
        expectedNode.setTitle(nameToUse);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeMimetype);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode =
                factory.getNewWorkspaceNode(workspaceID, nodeArchiveURI,
                        nodeArchiveURL, mockTestReferenceHandleCarrier,
                        nodeMimetype, nodeType, null, onSite, isProtected);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void workspaceExternalResourceNodeCorrectlyInitialised() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/archive/folder/node.cmdi");
        final String nodeName = "SomeName";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String nodeMimetype = "text/plain";
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.EXTERNAL;
        final boolean onSite = Boolean.FALSE;
        final boolean isProtected = Boolean.FALSE;
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode(workspaceID, nodeArchiveURI, nodeArchiveURL);
        expectedNode.setName(nodeName);
        expectedNode.setTitle(nodeName);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeMimetype);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode =
                factory.getNewWorkspaceNode(workspaceID, nodeArchiveURI,
                        nodeArchiveURL, mockTestReferenceHandleCarrier,
                        nodeMimetype, nodeType, nodeName, onSite, isProtected);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void resourceNodeFromFile() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI originURI = URI.create("file:/local/folder/file.txt");
        final URL workspaceURL = new URL("file:/workspace/folder/file.txt");
        final String nameToUse = FilenameUtils.getName(workspaceURL.getPath());
        final String nodeName = null;
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String nodeMimetype = "text/plain";
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.UPLOADED;
        final boolean isProtected = Boolean.FALSE;
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode();
        expectedNode.setWorkspaceID(workspaceID);
        expectedNode.setArchiveURI(null);
        expectedNode.setName(nameToUse);
        expectedNode.setTitle(nameToUse);
        expectedNode.setOriginURI(originURI);
        expectedNode.setWorkspaceURL(workspaceURL);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeMimetype);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode = factory.getNewWorkspaceNodeFromFile(
                workspaceID, null, originURI, workspaceURL, null, nodeName,
                nodeMimetype, nodeType, nodeStatus, isProtected);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void metadataNodeFromFile() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI archiveURI = URI.create(UUID.randomUUID().toString());
        final URI originURI = URI.create("file:/local/folder/file.cmdi");
        final URL workspaceURL = new URL("file:/workspace/folder/file.cmdi");
        final URI schemaLocation = new URI("http:/some/location/schema.xsd");
        final String nodeName = "File";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String nodeMimetype = "text/x-cmdi+xml";
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.UPLOADED;
        final boolean isProtected = Boolean.FALSE;
        
        final WorkspaceNode expectedNode = new LamusWorkspaceNode();
        expectedNode.setWorkspaceID(workspaceID);
        expectedNode.setName(nodeName);
        expectedNode.setTitle(nodeName);
        expectedNode.setArchiveURI(archiveURI);
        expectedNode.setOriginURI(originURI);
        expectedNode.setWorkspaceURL(workspaceURL);
        expectedNode.setProfileSchemaURI(schemaLocation);
        expectedNode.setType(nodeType);
        expectedNode.setFormat(nodeMimetype);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode = factory.getNewWorkspaceNodeFromFile(
                workspaceID, archiveURI, originURI, workspaceURL, schemaLocation, nodeName,
                nodeMimetype, nodeType, nodeStatus, isProtected);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void newExternalNode() throws MalformedURLException {
        
        final int workspaceID = 10;
        final String filename = "file.txt";
        final URI originURI = URI.create("http:/remote/folder/" + filename);
        final WorkspaceNodeType nodeType = WorkspaceNodeType.UNKNOWN;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.EXTERNAL;
        final boolean isProtected = Boolean.FALSE;
        
        WorkspaceNode expectedNode = new LamusWorkspaceNode();
        expectedNode.setWorkspaceID(workspaceID);
        expectedNode.setName(filename);
        expectedNode.setTitle(filename);
        expectedNode.setOriginURI(originURI);
        expectedNode.setType(nodeType);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode = factory.getNewExternalNode(workspaceID, originURI);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void newExternalNode_NoFilename() throws MalformedURLException {
        
        final int workspaceID = 10;
        final String lastFolderName = "something";
        final URI originURI = URI.create("http:/remote/folder/" + lastFolderName + "/");
        final WorkspaceNodeType nodeType = WorkspaceNodeType.UNKNOWN;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.EXTERNAL;
        final boolean isProtected = Boolean.FALSE;
        
        WorkspaceNode expectedNode = new LamusWorkspaceNode();
        expectedNode.setWorkspaceID(workspaceID);
        expectedNode.setName(lastFolderName);
        expectedNode.setTitle(lastFolderName);
        expectedNode.setOriginURI(originURI);
        expectedNode.setType(nodeType);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode = factory.getNewExternalNode(workspaceID, originURI);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void newExternalNodeFromArchiveWithPid() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI archiveURI = URI.create("node:001");
        final URI archivePID = URI.create("hdl:" + UUID.randomUUID().toString());
        final String filename = "node.cmdi";
        final URL archiveURL = new URL("file:/archive/folder/" + filename);
        final WorkspaceNodeType nodeType = WorkspaceNodeType.UNKNOWN;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.EXTERNAL;
        final boolean isProtected = Boolean.FALSE;
        
        WorkspaceNode expectedNode = new LamusWorkspaceNode();
        expectedNode.setWorkspaceID(workspaceID);
        expectedNode.setName(filename);
        expectedNode.setTitle(filename);
        expectedNode.setArchiveURI(archivePID);
        expectedNode.setArchiveURL(archiveURL);
        expectedNode.setOriginURI(archiveURL.toURI());
        expectedNode.setType(nodeType);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        WorkspaceNode retrievedNode = factory.getNewExternalNodeFromArchive(workspaceID, mockCorpusNode, archivePID, archiveURL);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    @Test
    public void newExternalNodeFromArchiveWithoutPid() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URI archiveURI = URI.create("node:001");
        final String filename = "node.cmdi";
        final URL archiveURL = new URL("file:/archive/folder/" + filename);
        final WorkspaceNodeType nodeType = WorkspaceNodeType.UNKNOWN;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.EXTERNAL;
        final boolean isProtected = Boolean.FALSE;
        
        WorkspaceNode expectedNode = new LamusWorkspaceNode();
        expectedNode.setWorkspaceID(workspaceID);
        expectedNode.setName(filename);
        expectedNode.setTitle(filename);
        expectedNode.setArchiveURI(archiveURI);
        expectedNode.setArchiveURL(archiveURL);
        expectedNode.setOriginURI(archiveURL.toURI());
        expectedNode.setType(nodeType);
        expectedNode.setStatus(nodeStatus);
        expectedNode.setProtected(isProtected);
        
        context.checking(new Expectations() {{
            oneOf(mockCorpusNode).getNodeURI(); will(returnValue(archiveURI));
        }});
        
        WorkspaceNode retrievedNode = factory.getNewExternalNodeFromArchive(workspaceID, mockCorpusNode, null, archiveURL);
        
        assertEquals("Retrieved node different from expected", expectedNode, retrievedNode);
    }
    
    interface TestReferencingMetadataDocumentHandleCarrier extends ReferencingMetadataDocument, HandleCarrier {

    }
    
    interface TestReferenceHandleCarrier extends Reference, HandleCarrier {
        
    }
}
