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
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeExplorer;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.*;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import nl.mpi.metadata.cmdi.api.model.MetadataResourceProxy;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import static org.jmock.Expectations.throwException;
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
    
    @Mock WorkspaceNodeLinkManager mockWorkspaceNodeLinkManager;
    @Mock WorkspaceFileImporter mockWorkspaceFileImporter;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceNodeExplorer mockWorkspaceNodeExplorer;
    @Mock WorkspaceNode mockParentNode;
    @Mock Reference mockReferenceWithoutHandle;
    private Workspace testWorkspace;
    
    @Mock MetadataDocument mockNonReferencingMetadataDocument;
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock TestReferencingMetadataDocumentWithHandle mockTestReferencingMetadataDocumentWithHandle;
    @Mock TestNonReferencingMetadataDocumentWithHandle mockTestNonReferencingMetadataDocumentWithHandle;
    @Mock MetadataDocumentType mockMetadataDocumentType;
    @Mock StreamResult mockStreamResult;
    @Mock List<Reference> mockReferenceList;
    @Mock CorpusNode mockCorpusNode;
    
    private final int workspaceID = 1;
    
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
        testWorkspace = new LamusWorkspace(workspaceID, "someUser", -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.INITIALISING, "Workspace initialising", "archiveInfo/something");
        nodeImporter = new MetadataNodeImporter(
                mockCorpusStructureProvider, mockNodeResolver,
                mockWorkspaceDao, mockMetadataAPI,
                mockWorkspaceNodeLinkManager, mockWorkspaceFileImporter,
                mockWorkspaceNodeFactory, mockWorkspaceNodeExplorer);
    }
    
    @After
    public void tearDown() {
    }
    

    @Test
    public void importNodeWithNullWorkspace() throws URISyntaxException, WorkspaceImportException {
        NodeImporter testNodeImporter = new MetadataNodeImporter(
                mockCorpusStructureProvider, mockNodeResolver,
                mockWorkspaceDao, mockMetadataAPI,
                mockWorkspaceNodeLinkManager, mockWorkspaceFileImporter,
                mockWorkspaceNodeFactory, mockWorkspaceNodeExplorer);
        
        try {
            testNodeImporter.importNode(null, null, null, null);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void importTopNodeWithHandleAndLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int testChildWorkspaceNodeID = 10;
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        testWorkspace.setTopNodeArchiveURI(testChildURI);
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, null, testChildNode, null);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
        
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentReferences(); will(returnValue(mockReferenceList));
            oneOf (mockWorkspaceNodeExplorer).explore(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle, mockReferenceList);
        }});
        
        nodeImporter.importNode(testWorkspace, null, null, null);
    }
    
    @Test
    public void importTopNodeWithHandleAndNoLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int testChildWorkspaceNodeID = 10;
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        testWorkspace.setTopNodeArchiveURI(testChildURI);
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestNonReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf(mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, null, testChildNode, null);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestNonReferencingMetadataDocumentWithHandle);
        }});
        
        nodeImporter.importNode(testWorkspace, null, null, null);
    }
   
    @Test
    public void importNodeMetadataDocumentThrowsIOException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, UnknownNodeException {

        final URL testChildURL = new URL("http://some.url/node.something");
        final URI testURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final boolean testChildOnSite = Boolean.TRUE;
        
        testWorkspace.setTopNodeArchiveURI(testURI);
        
        final IOException expectedException = new IOException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildURL);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, null, null, null);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Error getting Metadata Document for node " + testURI;
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importNodeMetadataDocumentThrowsMetadataException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, UnknownNodeException {

        final URL testChildURL = new URL("http://some.url/node.something");
        final URI testURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final boolean testChildOnSite = Boolean.TRUE;
        
        testWorkspace.setTopNodeArchiveURI(testURI);
        
        final MetadataException expectedException = new MetadataException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildURL);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, null, null, null);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Error getting Metadata Document for node " + testURI;
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals(expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importNodeMetadataDocumentThrowsUnknownNodeException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, UnknownNodeException {

        final URI testURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        
        testWorkspace.setTopNodeArchiveURI(testURI);
        
        final UnknownNodeException expectedException = new UnknownNodeException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testURI); will(throwException(expectedException));
            
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, null, null, null);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Error getting information for node " + testURI;
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importNormalNodeWithHandleAndLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
                
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, testChildReference);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
            
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentReferences(); will(returnValue(mockReferenceList));
            oneOf (mockWorkspaceNodeExplorer).explore(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle, mockReferenceList);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, testChildReference);
    }

    @Test
    public void importNormalNodeWithHandleAndNoLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestNonReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, testChildReference);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestNonReferencingMetadataDocumentWithHandle);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, testChildReference);
    }
    
    @Test
    public void importNormalNodeWithNoHandle() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = testChildArchiveURL.toURI();
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
//        final Reference testChildReference = new DataResourceProxy("childID", testChildURI, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockReferenceWithoutHandle).getURI(); will(returnValue(testChildURI));
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestNonReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, mockReferenceWithoutHandle);
            
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestNonReferencingMetadataDocumentWithHandle);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockReferenceWithoutHandle);
    }
    
    @Test
    public void importExternalNode() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = testChildArchiveURL.toURI();
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_EXTERNAL;
        final boolean testChildOnSite = Boolean.FALSE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
//        final Reference testChildReference = new DataResourceProxy("childID", testChildURI, "cmdi");
        
        context.checking(new Expectations() {{
            
            oneOf(mockReferenceWithoutHandle).getURI(); will(returnValue(testChildURI));
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestNonReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, mockReferenceWithoutHandle);
            
            // external files shouldn't be copied to the workspace, just added as nodes in the database
            
//            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestNonReferencingMetadataDocumentWithHandle);
        }});
        
        nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, mockReferenceWithoutHandle);
    }

    @Test
    public void getNewWorkspaceNodeThrowsMalformedURLException() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        final MalformedURLException expectedException =
                new MalformedURLException("this is an exception thrown by the method 'getNewWorkspaceMetadataNode'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, testChildReference);
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, testChildReference);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to set URL for node " + testChildNode.getArchiveURI()
		    + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
        
    @Test
    public void getNewWorkspaceNodeThrowsIOException() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        final IOException expectedException =
                new IOException("this is an exception thrown by the method 'getNewWorkspaceMetadataNode'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, testChildReference);
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, testChildReference);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to create file for node " + testChildNode.getArchiveURI()
		    + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void getNewWorkspaceNodeThrowsTransformerException() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        final TransformerException expectedException =
                new TransformerException("this is an exception thrown by the method 'getNewWorkspaceMetadataNode'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, testChildReference);
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, testChildReference);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to create file for node " + testChildNode.getArchiveURI()
		    + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void getNewWorkspaceNodeThrowsMetadataException() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceImportException, UnknownNodeException, TransformerException {

        final int parentWorkspaceNodeID = 1;
        final int testChildWorkspaceNodeID = 10;
        final URL parentWsURL = new URL("file:/workspace/folder/filename.cmdi");
        final URL parentOriginURL = new URL("file:/some.uri/filename.cmdi");
        final URL parentArchiveURL = parentOriginURL;
        final URI parentURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000001");
        final WorkspaceNodeStatus parentStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        
        final URL testChildWsURL = new URL("file:/workspace/folder/node.something");
        final URL testChildOriginURL = new URL("file:/some.url/node.something");
        final URL testChildArchiveURL = testChildOriginURL;
        final URI testChildURI = new URI("hdl:11142/00-00000000-0000-0000-0000-000000000010");
        final String testChildName = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA;
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testChildStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final boolean testChildOnSite = Boolean.TRUE;
        
        final WorkspaceNode testParentNode = new LamusWorkspaceNode(parentWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                "parent label", "", WorkspaceNodeType.METADATA, parentWsURL, parentURI, parentArchiveURL, parentOriginURL, parentStatus, "cmdi");
        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testSchemaLocation,
                testChildName, "", testNodeType, testChildWsURL, testChildURI, testChildArchiveURL, testChildOriginURL, testChildStatus, testNodeFormat);
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        
        final MetadataException expectedException =
                new MetadataException("this is an exception thrown by the method 'getNewWorkspaceMetadataNode'");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testChildURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getUrl(mockCorpusNode); will(returnValue(testChildArchiveURL));
            oneOf(mockCorpusNode).getName(); will(returnValue(testChildName));
            oneOf(mockCorpusNode).isOnSite(); will(returnValue(testChildOnSite));
            
            oneOf(mockMetadataAPI).getMetadataDocument(testChildArchiveURL);
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            oneOf(mockWorkspaceNodeFactory).getNewWorkspaceMetadataNode(
                    testWorkspace.getWorkspaceID(), testChildURI, testChildArchiveURL, mockTestReferencingMetadataDocumentWithHandle, testChildName, testChildOnSite);
                will(returnValue(testChildNode));
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            oneOf(mockWorkspaceNodeLinkManager).linkNodesWithReference(testWorkspace, testParentNode, testChildNode, testChildReference);
            oneOf(mockWorkspaceFileImporter).importMetadataFileToWorkspace(testChildArchiveURL, testChildNode, mockTestReferencingMetadataDocumentWithHandle);
                will(throwException(expectedException));
        }});
        
        try {
            nodeImporter.importNode(testWorkspace, testParentNode, mockReferencingMetadataDocument, testChildReference);
            fail("Should have thrown exception");
        } catch(WorkspaceImportException ex) {
            String errorMessage = "Failed to create file for node " + testChildNode.getArchiveURI()
		    + " in workspace " + testWorkspace.getWorkspaceID();
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
}
interface TestReferencingMetadataDocumentWithHandle extends ReferencingMetadataDocument, HandleCarrier {
   
}

interface TestNonReferencingMetadataDocumentWithHandle extends MetadataDocument, HandleCarrier {
    
}
