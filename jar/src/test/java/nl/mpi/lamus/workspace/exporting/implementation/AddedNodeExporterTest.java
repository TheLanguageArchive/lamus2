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
import javax.xml.transform.stream.StreamResult;
import nl.mpi.corpusstructure.AccessInfo;
import nl.mpi.lamus.ams.AmsBridge;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceNodeFilesystemException;
import nl.mpi.lamus.workspace.exporting.CorpusStructureBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.SearchClientBridge;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
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
    @Mock AmsBridge mockAmsBridge;
    
    @Mock CMDIDocument mockChildCmdiDocument;
    @Mock CMDIDocument mockParentCmdiDocument;
    @Mock StreamResult mockStreamResult;
    @Mock ResourceProxy mockResourceProxy;
    @Mock AccessInfo mockAccessInfo;
    
    private NodeExporter addedNodeExporter;
    private Workspace testWorkspace;
    
    
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
                mockMetadataAPI, mockCorpusStructureBridge, mockWorkspaceDao, mockSearchClientBridge, mockWorkspaceTreeExporter);
        
        testWorkspace = new LamusWorkspace(1, "someUser", -1, -1, null,
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
            throws MalformedURLException, URISyntaxException, WorkspaceNodeFilesystemException, IOException, MetadataException {
        
        final int parentNodeWsID = 1;
        final int parentNodeArchiveID = 50;
        final String parentNodeName = "parentNode";
        final String parentExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + parentExtension;
        final URL parentNodeWsURL = new URL("file:/workspace" + testWorkspace.getWorkspaceID() + File.separator + parentFilename);
        final URL parentNodeArchiveURL = new URL("file:/archive/root/somenode/" + parentFilename);
        final WorkspaceNodeType parentNodeType = WorkspaceNodeType.METADATA;
        final WorkspaceNodeStatus parentNodeStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final String parentNodePid = UUID.randomUUID().toString();
        final String parentNodeFormat = "text/cmdi";
        
        final int nodeWsID = 10;
        final int nodeArchiveID = -1;
        final String nodeName = "Node";
        final String nodeExtension = "pdf";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + nodeExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + testWorkspace.getWorkspaceID() + "/" + nodeFilename);
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final URL nodeOriginURL = new URL("file:/localdirectory/" + nodeFilename);
        final URL nodeNewArchiveURL = new URL("file:/archive/root/somenode/" + parentNodeName + File.separator +
                nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + nodeExtension);
        final File nodeOriginFile = new File(nodeOriginURL.getPath());
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WR; //TODO change this
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_UPLOADED;
        final String nodeFormat = "application/pdf";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final String nodePid = UUID.randomUUID().toString();
        final WorkspaceNode currentNode = new LamusWorkspaceNode(nodeWsID, testWorkspace.getWorkspaceID(), nodeArchiveID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeNewArchiveURL, nodeOriginURL, nodeStatus, nodePid, nodeFormat);
        
        final WorkspaceNode parentNode = new LamusWorkspaceNode(parentNodeWsID, testWorkspace.getWorkspaceID(), parentNodeArchiveID, nodeSchemaLocation,
                parentNodeName, "", parentNodeType, parentNodeWsURL, parentNodeArchiveURL, parentNodeArchiveURL, parentNodeStatus, parentNodePid, parentNodeFormat);
        
        final File nextAvailableFile = new File("/archive/root/somenode/node.pdf");
        final String resourceType = nodeFormat; //"WRITTEN_RESOURCE"; //TODO What should be this like? Is it needed?
        
        final int newNodeArchiveID = 100;
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getAvailableFile(parentNodeArchiveURL.getPath(), nodeFilename, nodeType);
                will(returnValue(nextAvailableFile));
            
            oneOf(mockWorkspaceDao).updateNodeArchiveURL(currentNode);
            
            oneOf(mockCorpusStructureBridge).addNewNodeToCorpusStructure(nextAvailableFile.toURI().toURL(), nodePid, testWorkspace.getUserID());
                will(returnValue(newNodeArchiveID));
            
            oneOf(mockWorkspaceFileHandler).copyResourceFile(currentNode, nodeWsFile, nextAvailableFile);
                
            //TODO new node is added and linked in database

            //ONLY THIS IS NEEDED...? BECAUSE THE CRAWLER CREATES THE OTHER CONNECTIONS? WHAT ABOUT LINKING IN THE DB?
            
            oneOf(mockCorpusStructureBridge).ensureChecksum(newNodeArchiveID, nextAvailableFile.toURI().toURL());
            //add node to searchdb
            //calculate urid
            //set urid in db(?) and metadata
            //close searchdb
            
            oneOf(mockSearchClientBridge).isFormatSearchable(nodeFormat); will(returnValue(Boolean.TRUE));
            oneOf(mockSearchClientBridge).addNode(newNodeArchiveID);
            
            //TODO something missing?...
            
            //TODO Remove workspace from filesystem
            //TODO Keep workspace information in DB?
            
        }});
        
        
        addedNodeExporter.exportNode(parentNode, currentNode);
        
    }
    
    
    
    @Test
    public void exportUploadedMetadataNode()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, WorkspaceNodeFilesystemException {
        
        //TODO WHEN METADATA, CALL (RECURSIVELY) exploreTree FOR CHILDREN IN THE BEGINNING
            // this way child files would have the pids calculated in advance,
                // so the references in the parent can be set before the files are copied to their archive location

        //TODO but if now the PIDs are eventually assigned beforehand (when file is uploaded or added?)
            //maybe this doesn't make such a big difference...
        
        
        
        final int parentNodeWsID = 1;
        final int parentNodeArchiveID = 50;
        final String parentNodeName = "parentNode";
        final String metadataExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final URL parentNodeWsURL = new URL("file:/workspace" + testWorkspace.getWorkspaceID() + File.separator + parentFilename);
        final URL parentNodeArchiveURL = new URL("file:/archive/root/somenode/" + parentFilename);
        final WorkspaceNodeType parentNodeType = WorkspaceNodeType.METADATA;
        final WorkspaceNodeStatus parentNodeStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final String parentNodePid = UUID.randomUUID().toString();
        final String parentNodeFormat = "text/cmdi";
        
        final int nodeWsID = 10;
        final int nodeArchiveID = -1;
        final String nodeName = "Node";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + testWorkspace.getWorkspaceID() + "/" + nodeFilename);
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final URL nodeOriginURL = new URL("file:/localdirectory/" + nodeFilename);
        final URL nodeNewArchiveURL = new URL("file:/archive/root/somenode/" + parentNodeName + File.separator +
                nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension);
        final File nodeOriginFile = new File(nodeOriginURL.getPath());
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA; //TODO change this
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_UPLOADED;
        final String nodeFormat = "text/cmdi";
        final URI nodeSchemaLocation = new URI("http://some.location");
        final String nodePid = UUID.randomUUID().toString();
        final WorkspaceNode currentNode = new LamusWorkspaceNode(nodeWsID, testWorkspace.getWorkspaceID(), nodeArchiveID, nodeSchemaLocation,
                nodeName, "", nodeType, nodeWsURL, nodeNewArchiveURL, nodeOriginURL, nodeStatus, nodePid, nodeFormat);
        
        final WorkspaceNode parentNode = new LamusWorkspaceNode(parentNodeWsID, testWorkspace.getWorkspaceID(), parentNodeArchiveID, nodeSchemaLocation,
                parentNodeName, "", parentNodeType, parentNodeWsURL, parentNodeArchiveURL, parentNodeArchiveURL, parentNodeStatus, parentNodePid, parentNodeFormat);
        
        final File nextAvailableFile = new File("/archive/root/somenode/node.cmdi");
        final String resourceType = nodeFormat; //"WRITTEN_RESOURCE"; //TODO What should be this like? Is it needed?
        
        final int newNodeArchiveID = 100;
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getAvailableFile(parentNodeArchiveURL.getPath(), nodeFilename, nodeType);
                will(returnValue(nextAvailableFile));
            
            oneOf(mockWorkspaceDao).updateNodeArchiveURL(currentNode);
            
            oneOf(mockWorkspaceTreeExporter).explore(testWorkspace, currentNode);
                    
            oneOf(mockCorpusStructureBridge).addNewNodeToCorpusStructure(nextAvailableFile.toURI().toURL(), nodePid, testWorkspace.getUserID());
                will(returnValue(newNodeArchiveID));
            
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nextAvailableFile); will(returnValue(mockStreamResult));
            oneOf(mockWorkspaceFileHandler).copyMetadataFile(currentNode, mockMetadataAPI, mockChildCmdiDocument, nodeWsFile, mockStreamResult);
            
            //TODO new node is added and linked in database

            //ONLY THIS IS NEEDED...? BECAUSE THE CRAWLER CREATES THE OTHER CONNECTIONS? WHAT ABOUT LINKING IN THE DB?
            
            oneOf(mockCorpusStructureBridge).ensureChecksum(newNodeArchiveID, nextAvailableFile.toURI().toURL());
            //add node to searchdb
            //calculate urid
            //set urid in db(?) and metadata
            //close searchdb
            
            oneOf(mockSearchClientBridge).isFormatSearchable(nodeFormat); will(returnValue(Boolean.TRUE));
            oneOf(mockSearchClientBridge).addNode(newNodeArchiveID);
            
            //TODO something missing?...
            
            //TODO Remove workspace from filesystem
            //TODO Keep workspace information in DB?
            
        }});
        
        
        addedNodeExporter.exportNode(parentNode, currentNode);
    }
    
}