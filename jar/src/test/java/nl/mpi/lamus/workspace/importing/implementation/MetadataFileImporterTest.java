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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.exception.FileExplorerException;
import nl.mpi.lamus.workspace.exception.FileImporterException;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceNodeLinkFactory;
import nl.mpi.lamus.workspace.factory.WorkspaceParentNodeReferenceFactory;
import nl.mpi.lamus.workspace.importing.FileImporter;
import nl.mpi.lamus.workspace.importing.WorkspaceFileExplorer;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceParentNodeReference;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataElementException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.events.MetadataDocumentListener;
import nl.mpi.metadata.api.events.MetadataElementListener;
import nl.mpi.metadata.api.model.*;
import nl.mpi.metadata.api.type.ContainedMetadataElementType;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import nl.mpi.metadata.api.type.MetadataElementType;
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
public class MetadataFileImporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private FileImporter fileImporter;
    @Mock ArchiveObjectsDB mockArchiveObjectsDB;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceNodeFactory mockWorkspaceNodeFactory;
    @Mock WorkspaceParentNodeReferenceFactory mockWorkspaceParentNodeReferenceFactory;
    @Mock WorkspaceNodeLinkFactory mockWorkspaceNodeLinkFactory;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceFileExplorer mockWorkspaceFileExplorer;
    @Mock WorkspaceNode mockParentNode;
    @Mock Reference mockChildLink;
    private Workspace testWorkspace;
    
    @Mock MetadataDocument mockNonReferencingMetadataDocument;
    @Mock ReferencingMetadataDocument mockReferencingMetadataDocument;
    @Mock TestReferencingMetadataDocumentWithHandle mockTestReferencingMetadataDocumentWithHandle;
    @Mock TestNonReferencingMetadataDocumentWithHandle mockTestNonReferencingMetadataDocumentWithHandle;
    @Mock MetadataDocumentType mockMetadataDocumentType;
    @Mock StreamResult mockStreamResult;
    @Mock List<Reference> mockReferenceCollection;
    
    public MetadataFileImporterTest() {
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
        fileImporter = new MetadataFileImporter(mockArchiveObjectsDB, mockWorkspaceDao, mockMetadataAPI,
                mockWorkspaceNodeFactory, mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory,
                mockWorkspaceFileHandler, mockWorkspaceFileExplorer);
        fileImporter.setWorkspace(testWorkspace);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void importNodeWithNullWorkspace() throws FileExplorerException {
        FileImporter testFileImporter = new MetadataFileImporter(mockArchiveObjectsDB, mockWorkspaceDao, mockMetadataAPI,
                mockWorkspaceNodeFactory, mockWorkspaceParentNodeReferenceFactory, mockWorkspaceNodeLinkFactory,
                mockWorkspaceFileHandler, mockWorkspaceFileExplorer);
        
        final int testChildArchiveID = 100;
        try {
            testFileImporter.importFile(null, null, null, testChildArchiveID);
        } catch (FileImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "MetadataFileImporter.importFile: workspace not set";
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(null, ex.getWorkspace());
            assertEquals(MetadataFileImporter.class, ex.getFileImporterType());
            assertEquals(null, ex.getCause());
        }
        
    }
    
    /**
     * Test of importFile method, of class MetadataFileImporter.
     */
    @Test
    public void importTopNodeFileWithHandleAndLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, FileImporterException, FileExplorerException {

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
        final File testChildNodeFile = new File("this/is/a/test.file");
        
        context.checking(new Expectations() {{
            
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
            
            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL()); will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            
            exactly(2).of (mockTestReferencingMetadataDocumentWithHandle).getDisplayValue(); will(returnValue(testDisplayValue));
            //TODO get type
            //TODO get format
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf (mockMetadataDocumentType).getSchemaLocation(); will(returnValue(testSchemaLocation));
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getHandle(); will(returnValue(testPid));
            
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            
            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(null, null); will(returnValue(null));
        }});
        
        testChildNode.setWorkspaceNodeID(testChildWorkspaceNodeID);
        testWorkspace.setTopNodeID(testChildNode.getWorkspaceNodeID());
        testWorkspace.setTopNodeArchiveURL(testChildNode.getArchiveURL());
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceTopNode(testWorkspace);
        }});
        
        testWorkspace.setStatus(WorkspaceStatus.INITIALISING);
        testWorkspace.setMessage("Workspace initialising");
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(testWorkspace);
        }});
        
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceFileHandler).getFileForWorkspaceNode(testChildNode); will(returnValue(testChildNodeFile));
            oneOf (mockWorkspaceFileHandler).getStreamResultForWorkspaceNodeFile(testChildNodeFile);
                will(returnValue(mockStreamResult));
            oneOf (mockWorkspaceFileHandler).copyMetadataFileToWorkspace(testWorkspace, testChildNode, mockMetadataAPI,
                    mockTestReferencingMetadataDocumentWithHandle, testChildNodeFile, mockStreamResult);
            oneOf (mockWorkspaceDao).updateNodeWorkspaceURL(testChildNode);
        }});
        
        context.checking(new Expectations() {{
            
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentReferences(); will(returnValue(mockReferenceCollection));
            oneOf (mockWorkspaceFileExplorer).explore(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle, mockReferenceCollection);
        }});
        
        
        fileImporter.importFile(null, null, null, testChildArchiveID);
        
    }
    
    @Test
    public void importTopNodeFileWithHandleAndNoLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, FileImporterException, FileExplorerException {

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
        final File testChildNodeFile = new File("this/is/a/test.file");
        
        context.checking(new Expectations() {{
            
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
            
            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL()); will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            
            exactly(2).of (mockTestNonReferencingMetadataDocumentWithHandle).getDisplayValue(); will(returnValue(testDisplayValue));
            //TODO get type
            //TODO get format
            oneOf (mockTestNonReferencingMetadataDocumentWithHandle).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf (mockMetadataDocumentType).getSchemaLocation(); will(returnValue(testSchemaLocation));
            oneOf (mockTestNonReferencingMetadataDocumentWithHandle).getHandle(); will(returnValue(testPid));
            
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            
            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(null, null); will(returnValue(null));
        }});
        
        testChildNode.setWorkspaceNodeID(testChildWorkspaceNodeID);
        testWorkspace.setTopNodeID(testChildNode.getWorkspaceNodeID());
        testWorkspace.setTopNodeArchiveURL(testChildNode.getArchiveURL());
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceTopNode(testWorkspace);
        }});
        
        testWorkspace.setStatus(WorkspaceStatus.INITIALISING);
        testWorkspace.setMessage("Workspace initialising");
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(testWorkspace);
        }});
        
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceFileHandler).getFileForWorkspaceNode(testChildNode); will(returnValue(testChildNodeFile));
            oneOf (mockWorkspaceFileHandler).getStreamResultForWorkspaceNodeFile(testChildNodeFile);
                will(returnValue(mockStreamResult));
            oneOf (mockWorkspaceFileHandler).copyMetadataFileToWorkspace(testWorkspace, testChildNode, mockMetadataAPI,
                    mockTestNonReferencingMetadataDocumentWithHandle, testChildNodeFile, mockStreamResult);
            oneOf (mockWorkspaceDao).updateNodeWorkspaceURL(testChildNode);
        }});
        
        fileImporter.importFile(null, null, null, testChildArchiveID);
        
    }

    
    @Test
    public void importNodeFileWithHandleAndLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, FileImporterException, FileExplorerException {

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
        final File testChildNodeFile = new File("this/is/a/test.file");
        
        context.checking(new Expectations() {{
            
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
            
            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL()); will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            
            exactly(2).of (mockTestReferencingMetadataDocumentWithHandle).getDisplayValue(); will(returnValue(testDisplayValue));
            //TODO get type
            //TODO get format
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf (mockMetadataDocumentType).getSchemaLocation(); will(returnValue(testSchemaLocation));
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getHandle(); will(returnValue(testPid));
            
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            
            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(null, null); will(returnValue(null));
        }});
        
        testChildNode.setWorkspaceNodeID(testChildWorkspaceNodeID);
        testWorkspace.setTopNodeID(testChildNode.getWorkspaceNodeID());
        testWorkspace.setTopNodeArchiveURL(testChildNode.getArchiveURL());
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceTopNode(testWorkspace);
        }});
        
        testWorkspace.setStatus(WorkspaceStatus.INITIALISING);
        testWorkspace.setMessage("Workspace initialising");
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(testWorkspace);
        }});
        
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceFileHandler).getFileForWorkspaceNode(testChildNode); will(returnValue(testChildNodeFile));
            oneOf (mockWorkspaceFileHandler).getStreamResultForWorkspaceNodeFile(testChildNodeFile);
                will(returnValue(mockStreamResult));
            oneOf (mockWorkspaceFileHandler).copyMetadataFileToWorkspace(testWorkspace, testChildNode, mockMetadataAPI,
                    mockTestReferencingMetadataDocumentWithHandle, testChildNodeFile, mockStreamResult);
            oneOf (mockWorkspaceDao).updateNodeWorkspaceURL(testChildNode);
        }});
        
        context.checking(new Expectations() {{
            
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentReferences(); will(returnValue(mockReferenceCollection));
            oneOf (mockWorkspaceFileExplorer).explore(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle, mockReferenceCollection);
        }});
        
        
        fileImporter.importFile(null, null, null, testChildArchiveID);
        
    }
    
    @Test
    public void importNodeUrlFromArchiveReturnsNull() throws MalformedURLException, URISyntaxException, FileExplorerException {

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
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(null));
        }});
        
        try {
            fileImporter.importFile(null, null, null, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(FileImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Error getting object URL for node ID " + testChildArchiveID;
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataFileImporter.class, ex.getFileImporterType());
            assertNull(ex.getCause());
        }
    }
    
    @Test
    public void importNodeMetadataDocumentThrowsIOException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, FileExplorerException {

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
        
        final IOException expectedException = new IOException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
            
            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL()); will(throwException(expectedException));
        }});
        
        try {
            fileImporter.importFile(null, null, null, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(FileImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Error importing Metadata Document " + testChildNode.getArchiveURL();
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataFileImporter.class, ex.getFileImporterType());
            assertEquals(expectedException, ex.getCause());
        }
    }
    
    @Test
    public void importNodeMetadataDocumentThrowsMetadataException() throws MalformedURLException, URISyntaxException,
        IOException, MetadataException, FileExplorerException {

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
        
        final MetadataException expectedException = new MetadataException("this is an exception thrown by the method 'getMetadataDocument'");
        
        context.checking(new Expectations() {{
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
            
            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL()); will(throwException(expectedException));
        }});
        
        try {
            fileImporter.importFile(null, null, null, testChildArchiveID);
            fail("Should have thrown exception");
        } catch(FileImporterException ex) {
            assertNotNull(ex);
            String errorMessage = "Error importing Metadata Document " + testChildNode.getArchiveURL();
            assertEquals(errorMessage, ex.getMessage());
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(MetadataFileImporter.class, ex.getFileImporterType());
            assertEquals(expectedException, ex.getCause());
        }
    }
//    
//    @Test
//    public void importNodeWorkspaceFileHandlerThrowsException() throws MalformedURLException, URISyntaxException,
//        IOException, MetadataException, FailedToCreateWorkspaceNodeFileException, FileExplorerException {
//
//        final int testChildWorkspaceNodeID = 10;
//        final int testChildArchiveID = 100;
//        final OurURL testChildURL = new OurURL("http://some.url/node.something");
//        final String testDisplayValue = "someName";
//        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
//        final String testNodeFormat = "";
//        final URI testSchemaLocation = new URI("http://some.location");
//        final String testPid = "somePID";
//        final WorkspaceNode testChildNode = new LamusWorkspaceNode(testChildWorkspaceNodeID, testWorkspace.getWorkspaceID(), testChildArchiveID, testSchemaLocation,
//                testDisplayValue, "", testNodeType, testChildURL.toURL(), testChildURL.toURL(), testChildURL.toURL(), WorkspaceNodeStatus.NODE_ISCOPY, testPid, testNodeFormat);
//        final File testChildNodeFile = new File("this/is/a/test.file");
//        
//        final FailedToCreateWorkspaceNodeFileException expectedException = 
//                new FailedToCreateWorkspaceNodeFileException(
//                    "this is an exception thrown by the method 'getOutputStreamForWorkspaceNodeFile'",
//                    testWorkspace, testChildNode, new FileNotFoundException("this is a cause"));
//        
//        context.checking(new Expectations() {{
//            
//            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
//            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
//            
//            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL()); will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
//            
//            exactly(2).of (mockTestReferencingMetadataDocumentWithHandle).getDisplayValue(); will(returnValue(testDisplayValue));
//            //TODO get type
//            //TODO get format
//            oneOf (mockTestReferencingMetadataDocumentWithHandle).getType(); will(returnValue(mockMetadataDocumentType));
//            oneOf (mockMetadataDocumentType).getSchemaLocation(); will(returnValue(testSchemaLocation));
//            oneOf (mockTestReferencingMetadataDocumentWithHandle).getHandle(); will(returnValue(testPid));
//            
//            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
//            
//            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(null, null); will(returnValue(null));
//        }});
//        
//        testChildNode.setWorkspaceNodeID(testChildWorkspaceNodeID);
//        testWorkspace.setTopNodeID(testChildNode.getWorkspaceNodeID());
//        testWorkspace.setTopNodeArchiveURL(testChildNode.getArchiveURL());
//        
//        context.checking(new Expectations() {{
//            
//            oneOf (mockWorkspaceDao).updateWorkspaceTopNode(testWorkspace);
//        }});
//        
//        testWorkspace.setStatus(WorkspaceStatus.INITIALISING);
//        testWorkspace.setMessage("Workspace initialising");
//        
//        context.checking(new Expectations() {{
//            
//            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(testWorkspace);
//        }});
//        
//        
//        context.checking(new Expectations() {{
//            
//            oneOf (mockWorkspaceFileHandler).getFileForWorkspaceNode(testChildNode); will(returnValue(testChildNodeFile));
//            oneOf (mockWorkspaceFileHandler).getOutputStreamForWorkspaceNodeFile(testWorkspace, testChildNode, testChildNodeFile);
//                will(throwException(expectedException));
//        }});
//
//        
//        try {
//            fileImporter.importFile(null, null, null, testChildArchiveID);
//            fail("Should have thrown exception");
//        } catch(FileImporterException ex) {
//            assertNotNull(ex);
//            String errorMessage = "Failed to create file for workspace node " + testChildNode.getWorkspaceNodeID()
//                    + " in workspace " + testWorkspace.getWorkspaceID();
//            assertEquals(errorMessage, ex.getMessage());
//            assertEquals(testWorkspace, ex.getWorkspace());
//            assertEquals(MetadataFileImporter.class, ex.getFileImporterType());
//            assertEquals(expectedException, ex.getCause());
//        }
//    }
    
    //TODO Test throw FileExplorerException
    
    @Test
    public void importNormalNodeFileWithHandleAndLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, FileImporterException, FileExplorerException {

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
        final File testChildNodeFile = new File("this/is/a/test.file");
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        final WorkspaceParentNodeReference testParentNodeReference = 
                new LamusWorkspaceParentNodeReference(parentWorkspaceNodeID, testChildReference);
        final WorkspaceNodeLink testNodeLink = 
                new LamusWorkspaceNodeLink(parentWorkspaceNodeID, testChildWorkspaceNodeID, testChildReference.getURI());
        
        context.checking(new Expectations() {{
            
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
            
            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL());
                will(returnValue(mockTestReferencingMetadataDocumentWithHandle));
            
            exactly(2).of (mockTestReferencingMetadataDocumentWithHandle).getDisplayValue(); will(returnValue(testDisplayValue));
            //TODO get type
            //TODO get format
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf (mockMetadataDocumentType).getSchemaLocation(); will(returnValue(testSchemaLocation));
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getHandle(); will(returnValue(testPid));
            
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            
            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(testParentNode, testChildReference);
                will(returnValue(testParentNodeReference));
        }});
        
        testChildNode.setWorkspaceNodeID(testChildWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, testChildWorkspaceNodeID, testChildURI);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
        }});
        
        testWorkspace.setStatus(WorkspaceStatus.INITIALISING);
        testWorkspace.setMessage("Workspace initialising");
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(testWorkspace);
        }});
        
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceFileHandler).getFileForWorkspaceNode(testChildNode); will(returnValue(testChildNodeFile));
            oneOf (mockWorkspaceFileHandler).getStreamResultForWorkspaceNodeFile(testChildNodeFile);
                will(returnValue(mockStreamResult));
            oneOf (mockWorkspaceFileHandler).copyMetadataFileToWorkspace(testWorkspace, testChildNode, mockMetadataAPI,
                    mockTestReferencingMetadataDocumentWithHandle, testChildNodeFile, mockStreamResult);
            oneOf (mockWorkspaceDao).updateNodeWorkspaceURL(testChildNode);
        }});
        
        context.checking(new Expectations() {{
            
            oneOf (mockTestReferencingMetadataDocumentWithHandle).getDocumentReferences(); will(returnValue(mockReferenceCollection));
            oneOf (mockWorkspaceFileExplorer).explore(testWorkspace, testChildNode, mockTestReferencingMetadataDocumentWithHandle, mockReferenceCollection);
        }});
        
        
        fileImporter.importFile(testParentNode, mockReferencingMetadataDocument, testChildReference, testChildArchiveID);
        
    }

    @Test
    public void importNormalNodeFileWithHandleAndNoLinks() throws MalformedURLException, IOException, MetadataException, URISyntaxException,
        WorkspaceNodeFilesystemException, FileImporterException, FileExplorerException {

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
        final File testChildNodeFile = new File("this/is/a/test.file");
        
        final Reference testChildReference = new MetadataResourceProxy("childID", testChildURI, "cmdi");
        final WorkspaceParentNodeReference testParentNodeReference = 
                new LamusWorkspaceParentNodeReference(parentWorkspaceNodeID, testChildReference);
        final WorkspaceNodeLink testNodeLink = 
                new LamusWorkspaceNodeLink(parentWorkspaceNodeID, testChildWorkspaceNodeID, testChildReference.getURI());
        
        context.checking(new Expectations() {{
            
            oneOf (mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(testChildArchiveID), ArchiveAccessContext.getFileUrlContext()); will(returnValue(testChildURL));
            oneOf (mockWorkspaceNodeFactory).getNewWorkspaceNode(testWorkspace.getWorkspaceID(), testChildArchiveID, testChildURL.toURL()); will(returnValue(testChildNode));
            
            oneOf (mockMetadataAPI).getMetadataDocument(testChildNode.getArchiveURL());
                will(returnValue(mockTestNonReferencingMetadataDocumentWithHandle));
            
            exactly(2).of (mockTestNonReferencingMetadataDocumentWithHandle).getDisplayValue(); will(returnValue(testDisplayValue));
            //TODO get type
            //TODO get format
            oneOf (mockTestNonReferencingMetadataDocumentWithHandle).getDocumentType(); will(returnValue(mockMetadataDocumentType));
            oneOf (mockMetadataDocumentType).getSchemaLocation(); will(returnValue(testSchemaLocation));
            oneOf (mockTestNonReferencingMetadataDocumentWithHandle).getHandle(); will(returnValue(testPid));
            
            oneOf (mockWorkspaceDao).addWorkspaceNode(testChildNode);
            
            oneOf (mockWorkspaceParentNodeReferenceFactory).getNewWorkspaceParentNodeReference(testParentNode, testChildReference);
                will(returnValue(testParentNodeReference));
        }});
        
        testChildNode.setWorkspaceNodeID(testChildWorkspaceNodeID);
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceNodeLinkFactory).getNewWorkspaceNodeLink(parentWorkspaceNodeID, testChildWorkspaceNodeID, testChildURI);
                will(returnValue(testNodeLink));
            oneOf (mockWorkspaceDao).addWorkspaceNodeLink(testNodeLink);
        }});
        
        testWorkspace.setStatus(WorkspaceStatus.INITIALISING);
        testWorkspace.setMessage("Workspace initialising");
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(testWorkspace);
        }});
        
        
        context.checking(new Expectations() {{
            
            oneOf (mockWorkspaceFileHandler).getFileForWorkspaceNode(testChildNode); will(returnValue(testChildNodeFile));
            oneOf (mockWorkspaceFileHandler).getStreamResultForWorkspaceNodeFile(testChildNodeFile);
                will(returnValue(mockStreamResult));
            oneOf (mockWorkspaceFileHandler).copyMetadataFileToWorkspace(testWorkspace, testChildNode, mockMetadataAPI,
                    mockTestNonReferencingMetadataDocumentWithHandle, testChildNodeFile, mockStreamResult);
            oneOf (mockWorkspaceDao).updateNodeWorkspaceURL(testChildNode);
        }});
        
        fileImporter.importFile(testParentNode, mockReferencingMetadataDocument, testChildReference, testChildArchiveID);
        
    }

}


interface TestReferencingMetadataDocumentWithHandle extends ReferencingMetadataDocument, HandleCarrier {
   
}

interface TestNonReferencingMetadataDocumentWithHandle extends MetadataDocument, HandleCarrier {
    
}
