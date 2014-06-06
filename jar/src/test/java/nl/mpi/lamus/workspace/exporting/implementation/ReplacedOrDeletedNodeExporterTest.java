/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.workspace.exporting.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class ReplacedOrDeletedNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private NodeExporter replacedOrDeletedNodeExporter;
    private Workspace testWorkspace;
    
    @Mock VersioningHandler mockVersioningHandler;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock HandleManager mockHandleManager;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    
    @Mock WorkspaceNode mockWorkspaceNode;
    @Mock CorpusNode mockCorpusNode;
    
    public ReplacedOrDeletedNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        replacedOrDeletedNodeExporter = new ReplacedOrDeletedNodeExporter(
                mockVersioningHandler, mockWorkspaceDao,
                mockHandleManager, mockArchiveFileLocationProvider);
        
        testWorkspace = new LamusWorkspace(1, "someUser",  -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "archiveInfo/something");
        replacedOrDeletedNodeExporter.setWorkspace(testWorkspace);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void exportDeletedResourceNodeWithArchiveURL() throws MalformedURLException, URISyntaxException, WorkspaceExportException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final String testBaseName = "node.txt";
        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI testNodeArchiveURIWithoutHdl = new URI(testNodeArchiveURI.getSchemeSpecificPart());
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNodeArchiveURL = testNodeOriginURL;
        
        final String testNodeDisplayValue = "node";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.RESOURCE; //TODO change this
        final String testNodeFormat = "text/plain";
        final URI testNodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.NODE_DELETED;

        final WorkspaceNode testNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, testNodeStatus, testNodeFormat);
        
        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeArchiveURL));
            
            oneOf(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);
            
//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//            oneOf(mockSearchClientBridge).removeNode(testNodeArchiveURI);
            
            oneOf(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockHandleManager).deleteHandle(testNodeArchiveURIWithoutHdl);
            oneOf(mockWorkspaceNode).setArchiveURI(null);
            oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockWorkspaceNode);
            
            oneOf(mockWorkspaceNode).isMetadata(); will(returnValue(Boolean.FALSE));
            
        }});
        
        //TODO Handle external nodes (those can't be deleted, just unlinked)
        
        
        //retire version
        //move to trash
        //update csdb to point to the trash location
        
        //remove node from searchDB????
        
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        replacedOrDeletedNodeExporter.exportNode(null, mockWorkspaceNode);
        
    }
    
    @Test
    public void exportDeletedNodeWithoutArchiveURL() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
     
        context.checking(new Expectations() {{

            //node without archiveURL - was never in the archive, so it can just be skipped and will eventually be deleted together with the whole workspace folder
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(null));
        }});
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        replacedOrDeletedNodeExporter.exportNode(null, mockWorkspaceNode);
        
    }
    
    @Test
    public void exportReplacedResourceNodeWithArchiveURL() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final String testBaseName = "node.txt";
        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI testNodeArchiveURIWithoutHdl = new URI(testNodeArchiveURI.getSchemeSpecificPart());
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNodeArchiveURL = testNodeOriginURL;
        
        final String testNodeDisplayValue = "node";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.RESOURCE; //TODO change this
        final String testNodeFormat = "text/plain";
        final URI testNodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.NODE_REPLACED;
        
        final URL testNodeVersionArchiveURL = new URL("file:/versioning/location/r_node.txt");
        final String testNodeVersionArchivePath = "/versioning/location/r_node.txt";
        final File testNodeVersionArchiveFile = new File(testNodeVersionArchivePath);
        final URL testNodeVersionArchiveHttpUrl = new URL("http:/remote/archive/version_folder/r_node.txt");
        

        final WorkspaceNode testOldNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, testNodeStatus, testNodeFormat);
        
        final int testNewWorkspaceNodeID = 20;
        final String testNewBaseName = "node.txt";
        final URL testNewNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNewNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNewNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNewNodeArchiveURL = testNodeOriginURL;
        
        final String testNewNodeDisplayValue = "node";
        final WorkspaceNodeType testNewNodeType = WorkspaceNodeType.RESOURCE; //TODO change this
        final String testNewNodeFormat = "text/plain";
        final URI testNewNodeSchemaLocation = new URI("http://some.location");
        
        final WorkspaceNode testNewNode = new LamusWorkspaceNode(testNewWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNewNodeSchemaLocation,
                testNewNodeDisplayValue, "", testNewNodeType, testNewNodeWsURL, testNewNodeArchiveURI, testNewNodeArchiveURL, testNewNodeOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, testNewNodeFormat);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeArchiveURL));
            
            exactly(2).of(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockVersioningHandler).moveFileToVersioningFolder(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);
            
//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//            oneOf(mockSearchClientBridge).removeNode(testNodeArchiveURI);
            
            exactly(2).of(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveFileLocationProvider).getUriWithHttpRoot(testNodeVersionArchiveURL.toURI()); will(returnValue(testNodeVersionArchiveHttpUrl.toURI()));
            
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockHandleManager).updateHandle(testNodeVersionArchiveFile, testNodeArchiveURIWithoutHdl, testNodeVersionArchiveHttpUrl.toURI());
            
        }});
        
        //TODO Handle external nodes (those can't be deleted, just unlinked)
        
        
        //retire version
        //move to trash
        //update csdb to point to the trash location
        
        //remove node from searchDB????
        
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        replacedOrDeletedNodeExporter.exportNode(null, mockWorkspaceNode);
        
    }
    
    @Test
    public void exportReplacedMetadataNodeWithArchiveURL() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        fail("not implemented/tested yet");
    }
    
    //TODO EXCEPTIONS...
    //TODO EXCEPTIONS...
    //TODO EXCEPTIONS...
    //TODO EXCEPTIONS...
    //TODO EXCEPTIONS...
    //TODO EXCEPTIONS...
    
    @Test
    public void exportNodeWithDifferentStatus() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        final String testBaseName = "node.txt";
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNodeArchiveURL = testNodeOriginURL;
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.NODE_CREATED;
        
        final String expectedExceptionMessage = "This exporter only supports deleted or replaced nodes. Current node status: " + testNodeStatus.toString();
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeArchiveURL));
            
            exactly(2).of(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            
            oneOf(mockWorkspaceNode).getStatusAsString(); will(returnValue(testNodeStatus.toString()));
        }});
        
        try {
            replacedOrDeletedNodeExporter.exportNode(null, mockWorkspaceNode);
            fail("should have thrown an exception");
        } catch(IllegalStateException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
        
    }
    
    @Test
    public void exportDeletedResourceNodeThrowsHandleException() throws MalformedURLException, URISyntaxException, WorkspaceExportException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final String testBaseName = "node.txt";
        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI testNodeArchiveURIWithoutHdl = new URI(testNodeArchiveURI.getSchemeSpecificPart());
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNodeArchiveURL = testNodeOriginURL;
        
        final String testNodeDisplayValue = "node";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.RESOURCE; //TODO change this
        final String testNodeFormat = "text/plain";
        final URI testNodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.NODE_DELETED;

        final WorkspaceNode testNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, testNodeStatus, testNodeFormat);

        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
        
        final HandleException expectedExceptionCause = new HandleException(HandleException.CANNOT_CONNECT_TO_SERVER, "some exception message");
        final String expectedExceptionMessage = "Error deleting handle for node " + testNodeVersionArchiveURL;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeArchiveURL));
            
            oneOf(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);
            
//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//            oneOf(mockSearchClientBridge).removeNode(testNodeArchiveURI);
            
            oneOf(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockHandleManager).deleteHandle(testNodeArchiveURIWithoutHdl); will(throwException(expectedExceptionCause));
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));

        }});
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        try {
            replacedOrDeletedNodeExporter.exportNode(null, mockWorkspaceNode);
            fail("should have thrown an exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedExceptionCause, ex.getCause());
        }
        
    }
    
    @Test
    public void exportReplacedResourceNodeThrowsIOException() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final String testBaseName = "node.txt";
        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI testNodeArchiveURIWithoutHdl = new URI(testNodeArchiveURI.getSchemeSpecificPart());
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNodeArchiveURL = testNodeOriginURL;
        
        final String testNodeDisplayValue = "node";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.RESOURCE; //TODO change this
        final String testNodeFormat = "text/plain";
        final URI testNodeSchemaLocation = new URI("http://some.location");
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.NODE_REPLACED;
        
        final URL testNodeVersionArchiveURL = new URL("file:/versioning/location/r_node.txt");
        final String testNodeVersionArchivePath = "/versioning/location/r_node.txt";
        final File testNodeVersionArchiveFile = new File(testNodeVersionArchivePath);
        final URL testNodeVersionArchiveHttpUrl = new URL("http:/remote/archive/version_folder/r_node.txt");
        

        final WorkspaceNode testOldNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, testNodeStatus, testNodeFormat);
        
        final int testNewWorkspaceNodeID = 20;
        final String testNewBaseName = "node.txt";
        final URL testNewNodeWsURL = new URL("file:/workspace/" + testBaseName);
        final URI testNewNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNewNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final URL testNewNodeArchiveURL = testNodeOriginURL;
        
        final String testNewNodeDisplayValue = "node";
        final WorkspaceNodeType testNewNodeType = WorkspaceNodeType.RESOURCE; //TODO change this
        final String testNewNodeFormat = "text/plain";
        final URI testNewNodeSchemaLocation = new URI("http://some.location");
        
        final WorkspaceNode testNewNode = new LamusWorkspaceNode(testNewWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNewNodeSchemaLocation,
                testNewNodeDisplayValue, "", testNewNodeType, testNewNodeWsURL, testNewNodeArchiveURI, testNewNodeArchiveURL, testNewNodeOriginURL, WorkspaceNodeStatus.NODE_UPLOADED, testNewNodeFormat);
        
        final IOException expectedExceptionCause = new IOException("Error updating handle for node " + testNewNodeArchiveURL);
        final String expectedExceptionMessage = "Error updating handle for node " + testNewNodeArchiveURL;
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeArchiveURL));
            
            exactly(2).of(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockVersioningHandler).moveFileToVersioningFolder(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);

//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//            oneOf(mockSearchClientBridge).removeNode(testNodeArchiveURI);
            
            exactly(2).of(mockWorkspaceNode).getStatus(); will(returnValue(testNodeStatus));
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveFileLocationProvider).getUriWithHttpRoot(testNodeVersionArchiveURL.toURI()); will(returnValue(testNodeVersionArchiveHttpUrl.toURI()));
            
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockHandleManager).updateHandle(testNodeVersionArchiveFile, testNodeArchiveURIWithoutHdl, testNodeVersionArchiveHttpUrl.toURI());
                will(throwException(expectedExceptionCause));
            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNewNodeArchiveURL));
            
        }});
        
        //TODO DO NOT USE NULL - THAT WOULD MEAN DELETING THE TOP NODE - THAT WOULD INVOLVE MESSING WITH THE PARENT OF THE TOP NODE (OUTSIDE OF THE SCOPE OF THE WORKSPACE)
        try {
            replacedOrDeletedNodeExporter.exportNode(null, mockWorkspaceNode);
            fail("should have thrown an exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedExceptionCause, ex.getCause());
        }
        
    }
    
    
//    @Test
//    public void exportUnknownNode() throws MalformedURLException, URISyntaxException, UnknownNodeException, WorkspaceExportException {
//        
//        final int testWorkspaceNodeID = 10;
//        final String testBaseName = "node.txt";
//        final URL testNodeWsURL = new URL("file:/workspace/" + testBaseName);
//        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
//        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
//        final URL testNodeArchiveURL = testNodeOriginURL;
//        
//        final String testNodeDisplayValue = "node";
//        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
//        final String testNodeFormat = "text/plain";
//        final URI testNodeSchemaLocation = new URI("http://some.location");
//
//        final WorkspaceNode testNode = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspace.getWorkspaceID(), testNodeSchemaLocation,
//                testNodeDisplayValue, "", testNodeType, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL, testNodeOriginURL, WorkspaceNodeStatus.NODE_DELETED, testNodeFormat);
//        
//        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
//        
//        final String expectedErrorMessage = "Node not found in archive database for URI " + testNode.getArchiveURI();
//        final UnknownNodeException expectedException = new UnknownNodeException("some exception message");
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockWorkspaceNode).getArchiveURL(); will(returnValue(testNodeArchiveURL));
//            
//            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockWorkspaceNode); will(returnValue(testNodeVersionArchiveURL));
//            oneOf(mockWorkspaceNode).setArchiveURL(testNodeVersionArchiveURL);
//            
//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//            oneOf(mockCorpusStructureProvider).getNode(testNodeArchiveURI); will(throwException(expectedException));
//
//            //exception caught
//            oneOf(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
//        }});
//        
//        try {
//            deletedNodeExporter.exportNode(null, mockWorkspaceNode);
//            fail("should have thrown exception");
//        } catch(WorkspaceExportException ex) {
//            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
//            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
//            assertEquals("Cause different from expected", expectedException, ex.getCause());
//        }
//    }
    
    @Test
    public void exportNodeNullWorkspace() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        replacedOrDeletedNodeExporter.setWorkspace(null);
        
        try {
            replacedOrDeletedNodeExporter.exportNode(null, mockWorkspaceNode);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
}