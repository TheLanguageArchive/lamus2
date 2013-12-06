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
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.UnknownNodeException;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.importing.NodeDataRetriever;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.apache.commons.io.FilenameUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
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
public class AddedNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock CorpusStructureBridge mockCorpusStructureBridge;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock SearchClientBridge mockSearchClientBridge;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
//    @Mock AmsBridge mockAmsBridge;
    @Mock NodeDataRetriever mockNodeDataRetriever;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    
    
    @Mock CMDIDocument mockChildCmdiDocument;
    @Mock CMDIDocument mockParentCmdiDocument;
    @Mock StreamResult mockStreamResult;
    @Mock ResourceProxy mockResourceProxy;
    
    @Mock CorpusNode mockParentCorpusNode;
    
    private NodeExporter addedNodeExporter;
    private Workspace testWorkspace;
    
    private final String metadataExtension = "cmdi";
    private final String resourceExtension = "pdf";
    
    
    public AddedNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        addedNodeExporter = new AddedNodeExporter(mockArchiveFileLocationProvider, mockWorkspaceFileHandler,
                mockMetadataAPI, mockCorpusStructureBridge, mockWorkspaceDao, mockSearchClientBridge,
                mockWorkspaceTreeExporter, mockNodeDataRetriever, mockCorpusStructureProvider, mockNodeResolver);
        
        testWorkspace = new LamusWorkspace(1, "someUser", -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "archiveInfo/something");
        addedNodeExporter.setWorkspace(testWorkspace);
    }
    
    @After
    public void tearDown() {
    }

    
    /**
     * Test of exportNode method, of class AddedNodeExporter.
     */
    @Test
    public void exportUploadedResourceNode()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, UnknownNodeException, WorkspaceExportException {
        
        final URI nodeNewArchiveURI = new URI(UUID.randomUUID().toString());

        final WorkspaceNode currentNode = getCurrentResourceNode();
        final WorkspaceNode intermediateCurrentNode = getIntermediateCurrentResourceNode(currentNode);
        final WorkspaceNode updatedCurrentNode = getUpdatedCurrentResourceNode(intermediateCurrentNode, nodeNewArchiveURI);
        final WorkspaceNode parentNode = getParentNode();
        
        final String currentNodeFilename = currentNode.getName() + FilenameUtils.EXTENSION_SEPARATOR_STR + resourceExtension;
        final File nextAvailableFile = new File("/archive/root/somenode/" + currentNodeFilename);
        final File nodeWsFile = new File(currentNode.getWorkspaceURL().getPath());
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getAvailableFile(
                    parentNode.getArchiveURL().getPath(), currentNodeFilename, currentNode.getType());
                will(returnValue(nextAvailableFile));
            
                //TODO GENERATE URID
            oneOf(mockNodeDataRetriever).getNewArchiveURI(); will(returnValue(nodeNewArchiveURI));
                
            oneOf(mockWorkspaceDao).updateNodeArchiveUriUrl(updatedCurrentNode);
            
            oneOf(mockWorkspaceFileHandler).copyFile(nodeWsFile, nextAvailableFile);
                
            //ONLY THIS IS NEEDED...? BECAUSE THE CRAWLER CREATES THE OTHER CONNECTIONS? WHAT ABOUT LINKING IN THE DB?
            
//            oneOf(mockCorpusStructureBridge).ensureChecksum(newNodeArchiveID, nextAvailableFile.toURI().toURL());
            //add node to searchdb
            //calculate urid
            //set urid in db(?) and metadata
            //close searchdb
            
            oneOf(mockSearchClientBridge).isFormatSearchable(currentNode.getFormat()); will(returnValue(Boolean.TRUE));
            oneOf(mockSearchClientBridge).addNode(nodeNewArchiveURI);
            
            //TODO something missing?...
            
            //TODO Remove workspace from filesystem
            //TODO Keep workspace information in DB?
            
        }});
        
        addedNodeExporter.exportNode(parentNode, currentNode);
    }
    
    
    
    @Test
    public void exportUploadedMetadataNode()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        //TODO WHEN METADATA, CALL (RECURSIVELY) exploreTree FOR CHILDREN IN THE BEGINNING
            // this way child files would have the pids calculated in advance,
                // so the references in the parent can be set before the files are copied to their archive location

        //TODO but if now the PIDs are eventually assigned beforehand (when file is uploaded or added?)
            //maybe this doesn't make such a big difference...
        
        final URI nodeNewArchiveURI = new URI(UUID.randomUUID().toString());
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode intermediateCurrentNode = getIntermediateCurrentMetadataNode(currentNode);
        final WorkspaceNode updatedCurrentNode = getUpdatedCurrentMetadataNode(intermediateCurrentNode, nodeNewArchiveURI);
        final WorkspaceNode parentNode = getParentNode();
        
        final String currentNodeFilename = currentNode.getName() + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final File nextAvailableFile = new File("/archive/root/somenode/" + currentNodeFilename);
        final File nodeWsFile = new File(currentNode.getWorkspaceURL().getPath());
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getAvailableFile(
                    parentNode.getArchiveURL().getPath(), currentNodeFilename, currentNode.getType());
                will(returnValue(nextAvailableFile));
            
                //TODO GENERATE URID
            oneOf(mockNodeDataRetriever).getNewArchiveURI(); will(returnValue(nodeNewArchiveURI));
                
            oneOf(mockWorkspaceDao).updateNodeArchiveUriUrl(updatedCurrentNode);
            
            oneOf(mockWorkspaceTreeExporter).explore(testWorkspace, updatedCurrentNode);
            
            oneOf(mockMetadataAPI).getMetadataDocument(currentNode.getWorkspaceURL()); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nextAvailableFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
            
            //ONLY THIS IS NEEDED...? BECAUSE THE CRAWLER CREATES THE OTHER CONNECTIONS? WHAT ABOUT LINKING IN THE DB?
            
//            oneOf(mockCorpusStructureBridge).ensureChecksum(newNodeArchiveID, nextAvailableFile.toURI().toURL());
            //add node to searchdb
            //calculate urid
            //set urid in db(?) and metadata
            //close searchdb
            
            oneOf(mockSearchClientBridge).isFormatSearchable(currentNode.getFormat()); will(returnValue(Boolean.TRUE));
            oneOf(mockSearchClientBridge).addNode(nodeNewArchiveURI);
            
            //TODO something missing?...
            
            //TODO Remove workspace from filesystem
            //TODO Keep workspace information in DB?
            
        }});
        
        addedNodeExporter.exportNode(parentNode, currentNode);
    }
    
    @Test
    public void exportNodeNullWorkspace()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        addedNodeExporter.setWorkspace(null);
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode parentNode = getParentNode();
        
        try {
            addedNodeExporter.exportNode(parentNode, currentNode);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void exportNodeThrowsIOException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode intermediateCurrentNode = getIntermediateCurrentMetadataNode(currentNode);
        final WorkspaceNode parentNode = getParentNode();
        
        final String currentNodeFilename = currentNode.getName() + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        
        final String expectedErrorMessage = "Error getting new file for node " + intermediateCurrentNode.getWorkspaceURL();
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getAvailableFile(
                    parentNode.getArchiveURL().getPath(), currentNodeFilename, currentNode.getType());
                will(throwException(expectedException));
        }});
        
        try {
            addedNodeExporter.exportNode(parentNode, currentNode);
            fail("should have thrown an exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportNodeThrowsMetadataException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        final URI nodeNewArchiveURI = new URI(UUID.randomUUID().toString());
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode intermediateCurrentNode = getIntermediateCurrentMetadataNode(currentNode);
        final WorkspaceNode updatedCurrentNode = getUpdatedCurrentMetadataNode(intermediateCurrentNode, nodeNewArchiveURI);
        final WorkspaceNode parentNode = getParentNode();
        
        final String currentNodeFilename = currentNode.getName() + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final File nextAvailableFile = new File("/archive/root/somenode/" + currentNodeFilename);
        
        final String expectedErrorMessage = "Error getting Metadata Document for node " + currentNode.getWorkspaceURL();
        final MetadataException expectedException = new MetadataException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getAvailableFile(
                    parentNode.getArchiveURL().getPath(), currentNodeFilename, currentNode.getType());
                will(returnValue(nextAvailableFile));
            
                //TODO GENERATE URID
            oneOf(mockNodeDataRetriever).getNewArchiveURI(); will(returnValue(nodeNewArchiveURI));
                
            oneOf(mockWorkspaceDao).updateNodeArchiveUriUrl(updatedCurrentNode);
            
            oneOf(mockWorkspaceTreeExporter).explore(testWorkspace, updatedCurrentNode);
                    
            oneOf(mockMetadataAPI).getMetadataDocument(currentNode.getWorkspaceURL()); will(throwException(expectedException));
        }});
        
        try {
            addedNodeExporter.exportNode(parentNode, currentNode);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportNodeThrowsTransformerException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        final URI nodeNewArchiveURI = new URI(UUID.randomUUID().toString());
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode intermediateCurrentNode = getIntermediateCurrentMetadataNode(currentNode);
        final WorkspaceNode updatedCurrentNode = getUpdatedCurrentMetadataNode(intermediateCurrentNode, nodeNewArchiveURI);
        final WorkspaceNode parentNode = getParentNode();
        
        final String currentNodeFilename = currentNode.getName() + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final File nextAvailableFile = new File("/archive/root/somenode/" + currentNodeFilename);
        final File nodeWsFile = new File(currentNode.getWorkspaceURL().getPath());
        
        final String expectedErrorMessage = "Error writing file for node " + currentNode.getWorkspaceURL();
        final TransformerException expectedException = new TransformerException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getAvailableFile(
                    parentNode.getArchiveURL().getPath(), currentNodeFilename, currentNode.getType());
                will(returnValue(nextAvailableFile));
            
                //TODO GENERATE URID
            oneOf(mockNodeDataRetriever).getNewArchiveURI(); will(returnValue(nodeNewArchiveURI));
                
            oneOf(mockWorkspaceDao).updateNodeArchiveUriUrl(updatedCurrentNode);
            
            oneOf(mockWorkspaceTreeExporter).explore(testWorkspace, updatedCurrentNode);
            
            oneOf(mockMetadataAPI).getMetadataDocument(currentNode.getWorkspaceURL()); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nextAvailableFile); will(returnValue(mockStreamResult));
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
                will(throwException(expectedException));
        }});
        
        try {
            addedNodeExporter.exportNode(parentNode, currentNode);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    
    private WorkspaceNode getParentNode() throws MalformedURLException, URISyntaxException {
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "parentNode";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final URL parentNodeWsURL = new URL("file:/workspace" + testWorkspace.getWorkspaceID() + File.separator + parentFilename);
        final URL parentNodeOriginURL = new URL("file:/archive/somewhere/" + parentFilename);
        final URL parentNodeArchiveURL = parentNodeOriginURL;
        final URI parentNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final WorkspaceNodeType parentNodeType = WorkspaceNodeType.METADATA;
        final WorkspaceNodeStatus parentNodeStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final String parentNodeFormat = "text/cmdi";
        final URI nodeSchemaLocation = new URI("http://some.location");
        
        return new LamusWorkspaceNode(parentNodeWsID, testWorkspace.getWorkspaceID(), nodeSchemaLocation,
                parentNodeName, "", parentNodeType, parentNodeWsURL, parentNodeArchiveURI, parentNodeArchiveURL, parentNodeOriginURL, parentNodeStatus, parentNodeFormat);
    }
    
    
    private WorkspaceNode getCurrentMetadataNode() throws MalformedURLException, URISyntaxException {
        return getCurrentNode(metadataExtension, WorkspaceNodeType.METADATA, "text/cmdi");
    }
    
    private WorkspaceNode getCurrentResourceNode() throws MalformedURLException, URISyntaxException {
        return getCurrentNode(resourceExtension, WorkspaceNodeType.RESOURCE_WR, "application/pdf");
    }
    
    private WorkspaceNode getCurrentNode(String fileExtension, WorkspaceNodeType type, String format) throws MalformedURLException, URISyntaxException {
        
        final int nodeWsID = 10;
        final String nodeName = "Node";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + fileExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + testWorkspace.getWorkspaceID() + "/" + nodeFilename);
        final URL nodeOriginURL = new URL("file:/localdirectory/" + nodeFilename);
        
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_UPLOADED;
        final URI nodeSchemaLocation = new URI("http://some.location");
        
        return new LamusWorkspaceNode(nodeWsID, testWorkspace.getWorkspaceID(), nodeSchemaLocation,
                nodeName, "", type, nodeWsURL, null, null, nodeOriginURL, nodeStatus, format);
    }
    
    
    private WorkspaceNode getIntermediateCurrentMetadataNode(WorkspaceNode currentNode) throws MalformedURLException {
        return getIntermediateCurrentNode(currentNode, metadataExtension);
    }
    
    private WorkspaceNode getIntermediateCurrentResourceNode(WorkspaceNode currentNode) throws MalformedURLException {
        return getIntermediateCurrentNode(currentNode, resourceExtension);
    }
    
    private WorkspaceNode getIntermediateCurrentNode(WorkspaceNode currentNode, String fileExtension) throws MalformedURLException {
        
        final String nodeFilename = currentNode.getName() + FilenameUtils.EXTENSION_SEPARATOR_STR + fileExtension;
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeFilename);
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        
        return new LamusWorkspaceNode(currentNode.getWorkspaceNodeID(), testWorkspace.getWorkspaceID(), currentNode.getProfileSchemaURI(),
                currentNode.getName(), currentNode.getTitle(), currentNode.getType(), currentNode.getWorkspaceURL(),
                null, nodeNewArchiveURL, currentNode.getOriginURL(), currentNode.getStatus(), currentNode.getFormat());
    }
    
    private WorkspaceNode getUpdatedCurrentMetadataNode(WorkspaceNode intermediateCurrentNode, URI nodeNewArchiveURI) throws URISyntaxException {
        return getUpdatedCurrentNode(intermediateCurrentNode, nodeNewArchiveURI, metadataExtension);
    }
    
    private WorkspaceNode getUpdatedCurrentResourceNode(WorkspaceNode intermediateCurrentNode, URI nodeNewArchiveURI) throws URISyntaxException {
        return getUpdatedCurrentNode(intermediateCurrentNode, nodeNewArchiveURI, resourceExtension);
    }
    
    private WorkspaceNode getUpdatedCurrentNode(WorkspaceNode intermediateCurrentNode, URI nodeNewArchiveURI, String fileExtension) throws URISyntaxException {
        
        return new LamusWorkspaceNode(intermediateCurrentNode.getWorkspaceNodeID(), testWorkspace.getWorkspaceID(),
                intermediateCurrentNode.getProfileSchemaURI(), intermediateCurrentNode.getName(), intermediateCurrentNode.getTitle(),
                intermediateCurrentNode.getType(), intermediateCurrentNode.getWorkspaceURL(),
                nodeNewArchiveURI, intermediateCurrentNode.getArchiveURL(), intermediateCurrentNode.getOriginURL(),
                intermediateCurrentNode.getStatus(), intermediateCurrentNode.getFormat());
    }
}