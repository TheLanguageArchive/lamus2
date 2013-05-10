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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.UnknownNodeException;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinker;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.*;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;
import static org.junit.Assert.*;



/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class MetadataNodeImporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private NodeImporter nodeImporter;
    
    @Mock NodeDataRetriever mockNodeDataRetriever;
    @Mock WorkspaceNodeLinker mockWorkspaceNodeLinker;
    @Mock WorkspaceFileImporter mockWorkspaceFileImporter;
    
    @Mock ArchiveObjectsDB mockArchiveObjectsDB;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceParentNodeReferenceFactory mockWorkspaceParentNodeReferenceFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceNodeExplorer mockWorkspaceNodeExplorer;
    @Mock WorkspaceNode mockParentNode;
    @Mock Reference mockChildLink;
    private Workspace testWorkspace;
    
    @Mock MetadataDocument mockNonReferencingMetadataDocument;
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock TestReferencingMetadataDocumentWithHandle mockTestReferencingMetadataDocumentWithHandle;
    @Mock TestNonReferencingMetadataDocumentWithHandle mockTestNonReferencingMetadataDocumentWithHandle;
    @Mock MetadataDocumentType mockMetadataDocumentType;
    @Mock StreamResult mockStreamResult;
    @Mock List<Reference> mockReferenceList;
    
    public MetadataNodeImporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        testWorkspace = new LamusWorkspace(1, "someUser", -1, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.INITIALISING, "Workspace initialising", "archiveInfo/something");
        nodeImporter = new MetadataNodeImporter(mockArchiveObjectsDB, mockWorkspaceDao, mockMetadataAPI,
                mockNodeDataRetriever, mockWorkspaceNodeLinker, mockWorkspaceFileImporter,
                mockWorkspaceNodeFactory, mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory,
                mockWorkspaceFileHandler, mockWorkspaceNodeExplorer);
        nodeImporter.setWorkspace(testWorkspace);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void importNodeWithNullWorkspace() throws NodeExplorerException {
        NodeImporter testNodeImporter = new MetadataNodeImporter(mockArchiveObjectsDB, mockWorkspaceDao, mockMetadataAPI,
                mockNodeDataRetriever, mockWorkspaceNodeLinker, mockWorkspaceFileImporter,
                mockWorkspaceNodeFactory, mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory,
                mockWorkspaceFileHandler, mockWorkspaceNodeExplorer);
        
        final int testChildArchiveID = 100;
        try {
            testNodeImporter.importNode(null, null, null, testChildArchiveID);
        } catch (NodeImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "MetadataNodeImporter.importNode: workspace not set";
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(null, ex.getWorkspace());
            assertEquals(MetadataNodeImporter.class, ex.getNodeImporterType());
            assertEquals(null, ex.getCause());
        }
    }
    
    /**
     * Test of importNode method, of class MetadataNodeImporter.
     */
    @Test
    public void importTopNodeWithHandleAndLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, NodeImporterException, NodeExplorerException {

        final int testChildWorkspaceNodeID = 10;
        final int testChildArchiveID = 100;
        final OurURL testChildURL = new OurURL("http://some.url/node.something");
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final String testPid = "somePID";
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(testWorkspace, testChildArchiveID, mockTestReferencingMetadataDocumentWithHandle);
                will(returnValue(testChildNode));
            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinker).linkNodes(testWorkspace, null, testChildNode, null);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
        
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentReferences(); will(returnValue(mockReferenceList));
            oneOf (mockWorkspaceNodeExplorer).explore(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle, mockReferenceList);
        }});
        
        nodeImporter.importNode(null, null, null, testChildArchiveID);
    }
    
    @Test
    public void importTopNodeWithHandleAndNoLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, NodeImporterException, NodeExplorerException {

        final int testChildWorkspaceNodeID = 10;
        final int testChildArchiveID = 100;
        final OurURL testChildURL = new OurURL("http://some.url/node.something");
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final String testPid = "somePID";
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(testWorkspace, testChildArchiveID, mockTestNonReferencingMetadataDocumentWithHandle);
                will(returnValue(testChildNode));
            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinker).linkNodes(testWorkspace, null, testChildNode, null);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testWorkspace, testChildNode, mockTestNonReferencingMetadataDocumentWithHandle);
        }});
        
        nodeImporter.importNode(null, null, null, testChildArchiveID);
    }
   
    @Test
    public void importNodeMetadataDocumentThrowsIOException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, NodeExplorerException {

        final int testChildArchiveID = 100;
        
        final IOException expectedException = new IOException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(null, null, null, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(NodeImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Error importing Metadata Document for node with ID " + testChildArchiveID;
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataNodeImporter.class, ex.getNodeImporterType());
            assertEquals(expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importNodeMetadataDocumentThrowsMetadataException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, NodeExplorerException {

        final int testChildArchiveID = 100;
        
        final MetadataException expectedException = new MetadataException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(null, null, null, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(NodeImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Error importing Metadata Document for node with ID " + testChildArchiveID;
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataNodeImporter.class, ex.getNodeImporterType());
            assertEquals(expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importNodeMetadataDocumentThrowsUnknownNodeException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, NodeExplorerException {

        final int testChildArchiveID = 100;
        
        final UnknownNodeException expectedException = new UnknownNodeException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(null, null, null, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(NodeImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Error getting object URL for node ID " + testChildArchiveID;
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataNodeImporter.class, ex.getNodeImporterType());
            assertEquals(expectedException, ex.getCause());
        }
    }
    
    //TODO Test throw NodeExplorerException
    
    @Test
    public void importNormalNodeWithHandleAndLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, NodeImporterException, NodeExplorerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final int testChildArchiveID = 100;
        final URL parentURL = new URL("http://some.uri/filename.cmdi");
        final OurURL testChildURL = new OurURL("http://some.url/node.something");
        final URI testChildURI = new URI("http://some.url/node.something");
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final String testPid = "somePID";
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, "aPid", "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(testWorkspace, testChildArchiveID, mockTestReferencingMetadataDocumentWithHandle);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinker).linkNodes(testWorkspace, testParentNode, testChildNode, testChildReference);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
            
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentReferences(); will(returnValue(mockReferenceList));
            oneOf (mockWorkspaceNodeExplorer).explore(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle, mockReferenceList);
        }});
        
        nodeImporter.importNode(testParentNode, mockReferencingMetadataDocument, testChildReference, testChildArchiveID);
    }

    @Test
    public void importNormalNodeWithHandleAndNoLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, NodeImporterException, NodeExplorerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final int testChildArchiveID = 100;
        final URL parentURL = new URL("http://some.uri/filename.cmdi");
        final OurURL testChildURL = new OurURL("http://some.url/node.something");
        final URI testChildURI = new URI("http://some.url/node.something");
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final String testPid = "somePID";
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, "aPid", "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(testWorkspace, testChildArchiveID, mockTestNonReferencingMetadataDocumentWithHandle);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinker).linkNodes(testWorkspace, testParentNode, testChildNode, testChildReference);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testWorkspace, testChildNode, mockTestNonReferencingMetadataDocumentWithHandle);
        }});
        
        nodeImporter.importNode(testParentNode, mockReferencingMetadataDocument, testChildReference, testChildArchiveID);
    }

    @Test
    public void getNewWorkspaceNodeThrowsMalformedURLException() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, NodeImporterException, NodeExplorerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final int testChildArchiveID = 100;
        final URL parentURL = new URL("http://some.uri/filename.cmdi");
        final OurURL testChildURL = new OurURL("http://some.url/node.something");
        final URI testChildURI = new URI("http://some.url/node.something");
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final String testPid = "somePID";
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, "aPid", "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        final MalformedURLException expectedException = new MalformedURLException("this is an exception thrown by the method 'getNewWorkspaceMetadataNode'");
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(testWorkspace, testChildArchiveID, mockTestReferencingMetadataDocumentWithHandle);
                will(throwException(expectedException));
            oneOf(mockTestReferencingMetadataDocumentWithHandle).getFileLocation(); will(returnValue(testChildURI));
        }});
        
        try {
            nodeImporter.importNode(testParentNode, mockReferencingMetadataDocument, testChildReference, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(NodeImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Error creating workspace node for file with location: " + testChildURI;
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataNodeImporter.class, ex.getNodeImporterType());
            assertEquals(expectedException, ex.getCause());
        }
    }
    
    @Test
    public void getNewWorkspaceNodeThrowsWorkspaceNodeFilesystemException() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, NodeImporterException, NodeExplorerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final int testChildArchiveID = 100;
        final URL parentURL = new URL("http://some.uri/filename.cmdi");
        final OurURL testChildURL = new OurURL("http://some.url/node.something");
        final URI testChildURI = new URI("http://some.url/node.something");
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final String testPid = "somePID";
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), 1, testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentURL, parentURL, parentURL, WorkspaceNodeStatus.NODE_ISCOPY, "aPid", "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        final WorkspaceNodeFilesystemException expectedException =
                new WorkspaceNodeFilesystemException("this is an exception thrown by the method 'getNewWorkspaceMetadataNode'",
                    testWorkspace, testChildNode, null);
        
        context.checking(new Expectations() {{
            oneOf(mockNodeDataRetriever).getArchiveNodeMetadataDocument(testChildArchiveID);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(testWorkspace, testChildArchiveID, mockTestReferencingMetadataDocumentWithHandle);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinker).linkNodes(testWorkspace, testParentNode, testChildNode, testChildReference);
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testParentNode, mockReferencingMetadataDocument, testChildReference, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(NodeImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Failed to create file for workspace node " + testChildNode.getWorkspaceNodeID()
		    + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataNodeImporter.class, ex.getNodeImporterType());
            assertEquals(expectedException, ex.getCause());
        }
    }
    
}
interface TestReferencingMetadataDocumentWithHandle extends ReferencingMetadataDocument, HandleCarrier {
   
}

interface TestNonReferencingMetadataDocumentWithHandle extends MetadataDocument, HandleCarrier {
    
}
