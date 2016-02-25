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
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.metadata.api.MetadataException;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class ReplacedOrDeletedNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private NodeExporter replacedOrDeletedNodeExporter;
    private Workspace testWorkspace;
    
    @Mock VersioningHandler mockVersioningHandler;
    @Mock HandleManager mockHandleManager;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    @Mock ArchiveHandleHelper mockArchiveHandleHelper;
    @Mock NodeUtil mockNodeUtil;
    @Mock WorkspaceDao mockWorkspaceDao;
    
    @Mock WorkspaceNode mockParentWsNode;
    @Mock WorkspaceNode mockChildWsNode;
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
        
        replacedOrDeletedNodeExporter = new ReplacedOrDeletedNodeExporter();
        ReflectionTestUtils.setField(replacedOrDeletedNodeExporter, "versioningHandler", mockVersioningHandler);
        ReflectionTestUtils.setField(replacedOrDeletedNodeExporter, "handleManager", mockHandleManager);
        ReflectionTestUtils.setField(replacedOrDeletedNodeExporter, "archiveFileLocationProvider", mockArchiveFileLocationProvider);
        ReflectionTestUtils.setField(replacedOrDeletedNodeExporter, "workspaceTreeExporter", mockWorkspaceTreeExporter);
        ReflectionTestUtils.setField(replacedOrDeletedNodeExporter, "archiveHandleHelper", mockArchiveHandleHelper);
        ReflectionTestUtils.setField(replacedOrDeletedNodeExporter, "nodeUtil", mockNodeUtil);
        ReflectionTestUtils.setField(replacedOrDeletedNodeExporter, "workspaceDao", mockWorkspaceDao);
        
        testWorkspace = new LamusWorkspace(1, "someUser",  -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "");
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void exportReplacedNode_SubmissionTypeDelete() throws WorkspaceExportException {
        
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.REPLACED;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        context.checking(new Expectations() {{
            allowing(mockChildWsNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        try {
            replacedOrDeletedNodeExporter.exportNode(testWorkspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "This exporter (for nodes with status " + nodeStatus.name() + ") should only be used when submitting the workspace, not when deleting";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }

    @Test
    public void exportDeletedNode_SubmissionTypeDelete() throws WorkspaceExportException {
        
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.DELETED;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        context.checking(new Expectations() {{
            allowing(mockChildWsNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        try {
            replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "This exporter (for nodes with status " + nodeStatus.name() + ") should only be used when submitting the workspace, not when deleting";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void exportDeletedNode_ExportPhaseTree() throws WorkspaceExportException {
        
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.DELETED;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        context.checking(new Expectations() {{
            allowing(mockChildWsNode).getStatus(); will(returnValue(nodeStatus));
        }});
        
        try {
            replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "This exporter (for nodes with status " + nodeStatus.name() + ") should only be used when exporting unlinked nodes, not for the tree";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void exportDeletedResourceNodeWithArchiveURI()
            throws MalformedURLException, URISyntaxException, WorkspaceExportException,
            HandleException, IOException, TransformerException, MetadataException {
        
        final int testWorkspaceNodeID = 10;
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.DELETED;
        final boolean isNodeProtected = Boolean.FALSE;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.FALSE));
            
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockChildWsNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).setArchiveURL(testNodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildWsNode);
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveHandleHelper).deleteArchiveHandleFromServerAndFile(mockChildWsNode, testNodeVersionArchiveURL);
            
        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportDeletedMetadataNodeWithArchiveURI() throws MalformedURLException, URISyntaxException, WorkspaceExportException, HandleException, IOException, MetadataException, TransformerException {
        
        final int testWorkspaceNodeID = 10;
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.DELETED;
        final boolean isNodeProtected = Boolean.FALSE;
        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockChildWsNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).setArchiveURL(testNodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildWsNode);
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveHandleHelper).deleteArchiveHandleFromServerAndFile(mockChildWsNode, testNodeVersionArchiveURL);
            
        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportDeletedExternalNode() throws WorkspaceExportException {
        
        final int testWorkspaceNodeID = 10;
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.DELETED;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            exactly(2).of(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.TRUE));
        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportDeletedNodeWithoutArchiveURI() throws WorkspaceExportException {
     
        final int testWorkspaceNodeID = 10;
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.DELETED;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            exactly(2).of(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));

            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            //node without archiveURL - was never in the archive, so it can just be skipped and will eventually be deleted together with the whole workspace folder
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(null));
        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportProtectedNode() throws WorkspaceExportException, URISyntaxException {
     
        final int testWorkspaceNodeID = 10;
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final boolean isNodeProtected = Boolean.TRUE;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.DELETED;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));

            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportReplacedResourceNodeWithArchiveURI() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI testNodeArchiveURIWithoutHdl = new URI(testNodeArchiveURI.getSchemeSpecificPart());
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.REPLACED;
        final boolean isNodeProtected = Boolean.FALSE;
        final URL testNodeVersionArchiveURL = new URL("file:/versioning/location/r_node.txt");
        final String testNodeVersionArchivePath = "/versioning/location/r_node.txt";
        final File testNodeVersionArchiveFile = new File(testNodeVersionArchivePath);
        final URL testNodeVersionArchiveHttpsUrl = new URL("https:/remote/archive/version_folder/r_node.txt");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.FALSE));
            
            oneOf(mockVersioningHandler).moveFileToVersioningFolder(mockChildWsNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).setArchiveURL(testNodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildWsNode);
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveFileLocationProvider).getUriWithHttpsRoot(testNodeVersionArchiveURL.toURI()); will(returnValue(testNodeVersionArchiveHttpsUrl.toURI()));
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockHandleManager).updateHandle(testNodeVersionArchiveFile, testNodeArchiveURIWithoutHdl, testNodeVersionArchiveHttpsUrl.toURI());
            
        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportReplacedMetadataNodeWithArchiveURI() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI testNodeArchiveURIWithoutHdl = new URI(testNodeArchiveURI.getSchemeSpecificPart());
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.REPLACED;
        final boolean isNodeProtected = Boolean.FALSE;
        final URL testNodeVersionArchiveURL = new URL("file:/versioning/location/r_node.cmdi");
        final String testNodeVersionArchivePath = "/versioning/location/r_node.cmdi";
        final File testNodeVersionArchiveFile = new File(testNodeVersionArchivePath);
        final URL testNodeVersionArchiveHttpsUrl = new URL("https:/remote/archive/version_folder/r_node.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceTreeExporter).explore(testWorkspace, mockChildWsNode, parentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockVersioningHandler).moveFileToVersioningFolder(mockChildWsNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).setArchiveURL(testNodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildWsNode);
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveFileLocationProvider).getUriWithHttpsRoot(testNodeVersionArchiveURL.toURI()); will(returnValue(testNodeVersionArchiveHttpsUrl.toURI()));
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockHandleManager).updateHandle(testNodeVersionArchiveFile, testNodeArchiveURIWithoutHdl, testNodeVersionArchiveHttpsUrl.toURI());
            
        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportNodeWithDifferentStatus() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.CREATED;
        final boolean isNodeProtected = Boolean.FALSE;
        final String expectedExceptionMessage = "This exporter only supports deleted or replaced nodes. Current node status: " + testNodeStatus.toString();
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getStatusAsString(); will(returnValue(testNodeStatus.toString()));
        }});
        
        try {
            replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown an exception");
        } catch(IllegalStateException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void exportDeletedResourceNodeThrowsHandleException() throws MalformedURLException, URISyntaxException, WorkspaceExportException, HandleException, IOException, TransformerException, MetadataException {
        
        final int testWorkspaceNodeID = 10;
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.DELETED;
        final boolean isNodeProtected = Boolean.FALSE;
        final URL testNodeVersionArchiveURL = new URL("file:/trash/location/r_node.txt");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final HandleException expectedException = new HandleException(HandleException.CANNOT_CONNECT_TO_SERVER, "some exception message");
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.FALSE));
            
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockChildWsNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).setArchiveURL(testNodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildWsNode);
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveHandleHelper).deleteArchiveHandleFromServerAndFile(mockChildWsNode, testNodeVersionArchiveURL); will(throwException(expectedException));
            //logger
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));

        }});
        
        replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportReplacedResourceNodeThrowsIOException() throws MalformedURLException, URISyntaxException, WorkspaceExportException, WorkspaceNodeNotFoundException, HandleException, IOException {
        
        final int testWorkspaceNodeID = 10;
        final String testBaseName = "node.txt";
        final URI testNodeArchiveURI = new URI("hdl:" + UUID.randomUUID().toString());
        final URI testNodeArchiveURIWithoutHdl = new URI(testNodeArchiveURI.getSchemeSpecificPart());
        final URL testNodeOriginURL = new URL("file:/lat/corpora/archive/folder/" + testBaseName);
        final WorkspaceNodeStatus testNodeStatus = WorkspaceNodeStatus.REPLACED;
        final boolean isNodeProtected = Boolean.FALSE;
        final URL testNodeVersionArchiveURL = new URL("file:/versioning/location/r_node.txt");
        final String testNodeVersionArchivePath = "/versioning/location/r_node.txt";
        final File testNodeVersionArchiveFile = new File(testNodeVersionArchivePath);
        final URL testNodeVersionArchiveHttpsUrl = new URL("https:/remote/archive/version_folder/r_node.txt");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final URL testNewNodeArchiveURL = testNodeOriginURL;
        
        final IOException expectedExceptionCause = new IOException("Error updating handle for node " + testNewNodeArchiveURL);
        final String expectedExceptionMessage = "Error updating handle for node " + testNewNodeArchiveURL;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        context.checking(new Expectations() {{
            
            allowing(mockChildWsNode).getStatus(); will(returnValue(testNodeStatus));
            
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(testWorkspaceNodeID));
            
            oneOf(mockChildWsNode).isExternal(); will(returnValue(Boolean.FALSE));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.FALSE));
            
            oneOf(mockVersioningHandler).moveFileToVersioningFolder(mockChildWsNode); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).setArchiveURL(testNodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildWsNode);

            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockArchiveFileLocationProvider).getUriWithHttpsRoot(testNodeVersionArchiveURL.toURI()); will(returnValue(testNodeVersionArchiveHttpsUrl.toURI()));
            
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNodeVersionArchiveURL));
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(testNodeArchiveURI));
            oneOf(mockHandleManager).updateHandle(testNodeVersionArchiveFile, testNodeArchiveURIWithoutHdl, testNodeVersionArchiveHttpsUrl.toURI());
                will(throwException(expectedExceptionCause));
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(testNewNodeArchiveURL));
            
        }});
        
        try {
            replacedOrDeletedNodeExporter.exportNode(testWorkspace, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown an exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedExceptionCause, ex.getCause());
        }
    }
    
    @Test
    public void exportNodeNullWorkspace() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        try {
            replacedOrDeletedNodeExporter.exportNode(null, null, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
}