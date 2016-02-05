/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import javax.xml.transform.TransformerException;
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.lamus.archive.ArchiveHandleHelper;
import nl.mpi.lamus.archive.CorpusStructureBridge;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class UnlinkedNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    private NodeExporter unlinkedNodeExporter;
    
    @Mock VersioningHandler mockVersioningHandler;
    @Mock ArchiveHandleHelper mockArchiveHandleHelper;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock NodeUtil mockNodeUtil;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockNode;
    @Mock WorkspaceNode mockParentNode;
    @Mock WorkspaceNode mockSomeOtherNode;
    @Mock CorpusNode mockCorpusNode;
    @Mock ReferencingMetadataDocument mockParentDocument;
    @Mock Reference mockReference;
    
    private final int wsID = 1;
    private final int nodeWsID = 10;
    
    public UnlinkedNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        unlinkedNodeExporter = new UnlinkedNodeExporter();
        ReflectionTestUtils.setField(unlinkedNodeExporter, "versioningHandler", mockVersioningHandler);
        ReflectionTestUtils.setField(unlinkedNodeExporter, "archiveHandleHelper", mockArchiveHandleHelper);
        ReflectionTestUtils.setField(unlinkedNodeExporter, "workspaceTreeExporter", mockWorkspaceTreeExporter);
        ReflectionTestUtils.setField(unlinkedNodeExporter, "metadataApiBridge", mockMetadataApiBridge);
        ReflectionTestUtils.setField(unlinkedNodeExporter, "metadataAPI", mockMetadataAPI);
        ReflectionTestUtils.setField(unlinkedNodeExporter, "workspaceDao", mockWorkspaceDao);
        ReflectionTestUtils.setField(unlinkedNodeExporter, "nodeUtil", mockNodeUtil);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void exportNode_NullWorkspace()
            throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        try {
            unlinkedNodeExporter.exportNode(null, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void export_ExportPhaseTree()
            throws WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        try {
            unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "This exporter should only be used when exporting unlinked nodes, not for the tree";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void exportProtectedNode()
            throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final boolean isNodeProtected = Boolean.TRUE;
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        initialExpectations(isNodeProtected);
        
        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void export_DoNotKeepUnlinkedFiles_DeleteSubmissionType()
            throws WorkspaceExportException {
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;

        initialExpectations(isNodeProtected);
            
        //do nothing else - given that the workspace is being deleted, a node cannot be moved or copied from the archive to the "sessions" folder
        
        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportLocalResource_DoNotKeepUnlinkedFiles_SubmitSubmissionType()
            throws WorkspaceExportException {
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        initialExpectations(isNodeProtected);
        
        //do nothing else - given that the workspace is being deleted, a node cannot be moved or copied from the archive to the "sessions" folder
        
        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportArchiveResource_DoNotKeepUnlinkedFiles_SubmitSubmissionType()
            throws MalformedURLException, WorkspaceExportException, HandleException, IOException, TransformerException, MetadataException {
        
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/initial/location/node.txt");
        
        final String nodeVersionArchivePath = "file:/trash/location/r_node.txt";
        final URI nodeVersionArchivePathURI = URI.create(nodeVersionArchivePath);
        final URL nodeVersionArchiveURL = nodeVersionArchivePathURI.toURL();
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockNode).getArchiveURL(); will(returnValue(nodeArchiveURL));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockNode); will(returnValue(nodeVersionArchiveURL));
            
            oneOf(mockArchiveHandleHelper).deleteArchiveHandleFromServerAndFile(mockNode, nodeArchiveURL);
            
            oneOf(mockNode).setArchiveURL(nodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockNode);
        }});
        
        setNodeAsDeletedInDb();

        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportArchiveResourceWithParent_DoNotKeepUnlinkedFiles_SubmitSubmissionType()
            throws MalformedURLException, WorkspaceExportException,
            IOException, MetadataException, TransformerException, HandleException {
        
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/initial/location/node.txt");
        
        final String nodeVersionArchivePath = "file:/trash/location/r_node.txt";
        final URI nodeVersionArchivePathURI = URI.create(nodeVersionArchivePath);
        final URL nodeVersionArchiveURL = nodeVersionArchivePathURI.toURL();
        final String nodeVersionArchiveFile = nodeVersionArchiveURL.getFile();
        final URI nodeVersionArchiveFileUri = URI.create(nodeVersionArchiveFile);
        
        final URL parentWsUrl = new URL("file:/location/workspace/parent.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockNode).getArchiveURL(); will(returnValue(nodeArchiveURL));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockNode); will(returnValue(nodeVersionArchiveURL));
            
            oneOf(mockArchiveHandleHelper).deleteArchiveHandleFromServerAndFile(mockNode, nodeArchiveURL);
            
            oneOf(mockNode).setArchiveURL(nodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockNode);
        }});
        
        setNodeAsDeletedInDb();
        
        updateReferenceInParent(parentWsUrl, nodeArchiveURI, nodeVersionArchivePathURI, nodeVersionArchiveFileUri, null);

        unlinkedNodeExporter.exportNode(mockWorkspace, mockParentNode, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportArchiveMetadata_DoNotKeepUnlinkedFiles_SubmitSubmissionType()
            throws MalformedURLException, WorkspaceExportException, HandleException, IOException, TransformerException, MetadataException {
        
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("file:/initial/location/node.txt");
        
        final String nodeVersionArchivePath = "file:/trash/location/r_node.txt";
        final URI nodeVersionArchivePathURI = URI.create(nodeVersionArchivePath);
        final URL nodeVersionArchiveURL = nodeVersionArchivePathURI.toURL();
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        final String currentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.TRUE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{

            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockNode).getArchiveURL(); will(returnValue(nodeArchiveURL));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            oneOf(mockWorkspaceTreeExporter).explore(mockWorkspace, mockNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockVersioningHandler).moveFileToTrashCanFolder(mockNode); will(returnValue(nodeVersionArchiveURL));
            
            oneOf(mockArchiveHandleHelper).deleteArchiveHandleFromServerAndFile(mockNode, nodeArchiveURL);
            
            oneOf(mockNode).setArchiveURL(nodeVersionArchiveURL);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockNode);
        }});
        
        setNodeAsDeletedInDb();

        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportLocalResource_KeepUnlinkedFiles_SubmitSubmissionType()
            throws MalformedURLException, WorkspaceExportException {
        
        final String nodeFilename = "node.txt";
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockNode); will(returnValue(newNodeLocation));
        }});
        
        updateNodeWorkspaceUrlInDb(newNodeLocation);
        
        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportLocalResourceWithParent_KeepUnlinkedFiles_SubmitSubmissionType()
            throws MalformedURLException, WorkspaceExportException,
            IOException, MetadataException, TransformerException {
        
        final String nodeFilename = "node.txt";
        final URI nodeFilenameUri = URI.create(nodeFilename);
        final URL nodeWsLocation = new URL("file:/workspace/location/" + nodeFilename);
        final URI nodeWsLocationUri = URI.create(nodeWsLocation.toString());
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        final URI newNodeLocationUri = URI.create(newNodeLocation.toString());
        
        final URL parentWsUrl = new URL("file:/location/workspace/parent.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockNode); will(returnValue(newNodeLocation));
            
            oneOf(mockNode).getWorkspaceURL(); will(returnValue(nodeWsLocation));
        }});
        
        updateReferenceInParent(parentWsUrl, nodeWsLocationUri, newNodeLocationUri, nodeFilenameUri, null);
        
        updateNodeWorkspaceUrlInDb(newNodeLocation);
        
        unlinkedNodeExporter.exportNode(mockWorkspace, mockParentNode, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportLocalMetadataWithParent_KeepUnlinkedFiles_SubmitSubmissionType()
            throws MalformedURLException, WorkspaceExportException,
            IOException, MetadataException, TransformerException {
        
        final String nodeFilename = "node.cmdi";
        final URI nodeFilenameUri = URI.create(nodeFilename);
        final URL nodeWsLocation = new URL("file:/workspace/location/" + nodeFilename);
        final URI nodeWsLocationUri = URI.create(nodeWsLocation.toString());
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        final URI newNodeLocationUri = URI.create(newNodeLocation.toString());
        final String currentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final URL parentWsUrl = new URL("file:/location/workspace/parent.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.TRUE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            oneOf(mockWorkspaceTreeExporter).explore(mockWorkspace, mockNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockNode); will(returnValue(newNodeLocation));
            
            oneOf(mockNode).getWorkspaceURL(); will(returnValue(nodeWsLocation));
        }});
        
        updateReferenceInParent(parentWsUrl, nodeWsLocationUri, newNodeLocationUri, nodeFilenameUri, null);
        
        updateNodeWorkspaceUrlInDb(newNodeLocation);
        
        unlinkedNodeExporter.exportNode(mockWorkspace, mockParentNode, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportArchiveResource_KeepUnlinkedFiles_SubmitSubmissionType()
            throws MalformedURLException, HandleException, IOException,
            TransformerException, MetadataException, WorkspaceExportException {
        
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        
        final String nodeFilename = "node.txt";
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        final URL mockUrl = new URL("file:/archive/some/location/sessions/" + nodeWsID + "_" + nodeFilename);
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
        
            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockNode); will(returnValue(newNodeLocation));
            
            oneOf(mockArchiveHandleHelper).deleteArchiveHandleFromServerAndFile(mockNode, newNodeLocation);
        }});

        updateNodeWorkspaceUrlInDb(newNodeLocation);
        
        updateNodeArchiveUrlInDb(mockUrl);
        
        setNodeAsDeletedInDb();
        
        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportLocalResource_KeepUnlinkedFiles_DeleteSubmissionType()
            throws MalformedURLException, WorkspaceExportException {
        
        final String nodeFilename = "node.txt";
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            oneOf(mockNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockNode); will(returnValue(newNodeLocation));
        }});
        
        updateNodeWorkspaceUrlInDb(newNodeLocation);

        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportLocalMetadataWithParent_KeepUnlinkedFiles_DeleteSubmissionType()
            throws MalformedURLException, WorkspaceExportException, IOException,
            MetadataException, TransformerException {
        
        final String nodeFilename = "node.cmdi";
        final URI nodeFilenameUri = URI.create(nodeFilename);
        final URL nodeWsLocation = new URL("file:/workspace/location/" + nodeFilename);
        final URI nodeWsLocationUri = URI.create(nodeWsLocation.toString());
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        final URI newNodeLocationUri = URI.create(newNodeLocation.toString());
        final String currentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final URL parentWsUrl = new URL("file:/location/workspace/parent.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.TRUE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            oneOf(mockWorkspaceTreeExporter).explore(mockWorkspace, mockNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockNode); will(returnValue(newNodeLocation));
            oneOf(mockNode).getWorkspaceURL(); will(returnValue(nodeWsLocation));
        }});
        
        updateReferenceInParent(parentWsUrl, nodeWsLocationUri, newNodeLocationUri, nodeFilenameUri, null);
        
        updateNodeWorkspaceUrlInDb(newNodeLocation);

        unlinkedNodeExporter.exportNode(mockWorkspace, mockParentNode, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportArchiveNode_KeepUnlinkedFiles_DeleteSubmissionType()
            throws MalformedURLException, WorkspaceExportException, HandleException,
            IOException, TransformerException, MetadataException {
        
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
        }});

        unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
        public void exportArchiveNodeWithParent_KeepUnlinkedFiles_DeleteSubmissionType()
            throws MalformedURLException, WorkspaceExportException, HandleException,
            IOException, TransformerException, MetadataException {
        
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        
        final URL parentWsUrl = new URL("file:/location/workspace/parent.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
        }});
        
        updateReferenceInParent(parentWsUrl, nodeArchiveURI, nodeArchiveURI, null, null);

        unlinkedNodeExporter.exportNode(mockWorkspace, mockParentNode, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportArchiveMetadata_DoNotKeepUnlinkedFiles_SubmitSubmissionType_throwsException()
            throws MalformedURLException, WorkspaceExportException {
        
        final URI nodeArchiveURI = URI.create(UUID.randomUUID().toString());
        
        final String nodeVersionArchivePath = "file:/trash/location/r_node.txt";
        final URI nodeVersionArchivePathURI = URI.create(nodeVersionArchivePath);
        final URL nodeVersionArchiveURL = nodeVersionArchivePathURI.toURL();
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        final String currentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.TRUE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", wsID, null);
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{

            oneOf(mockNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            oneOf(mockWorkspaceTreeExporter).explore(mockWorkspace, mockNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
                will(throwException(expectedException));
        }});

        try {
            unlinkedNodeExporter.exportNode(mockWorkspace, null, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
        public void exportLocalResourceWithParent_KeepUnlinkedFiles_SubmitSubmissionType_throwsException()
            throws MalformedURLException, WorkspaceExportException, HandleException,
            IOException, TransformerException, MetadataException {
        
        final String nodeFilename = "node.txt";
        final URI nodeFilenameUri = URI.create(nodeFilename);
        final URL nodeWsLocation = new URL("file:/workspace/location/" + nodeFilename);
        final URI nodeWsLocationUri = URI.create(nodeWsLocation.toString());
        final URL newNodeLocation = new URL("file:/archive/some/location/sessions/" + nodeFilename);
        final URI newNodeLocationUri = URI.create(newNodeLocation.toString());
        
        final URL parentWsUrl = new URL("file:/location/workspace/parent.cmdi");
        final String parentCorpusNamePathToClosestTopNode = CorpusStructureBridge.IGNORE_CORPUS_PATH;
        
        final boolean isNodeProtected = Boolean.FALSE;
        final boolean isNodeMetadata = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.TRUE;
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        final MetadataException expectedCause = new MetadataException("some exception message");
        final String expectedMessage = "Error writing file (updating child reference) for node " + parentWsUrl;
        
        initialExpectations(isNodeProtected);
        
        context.checking(new Expectations() {{
            
            oneOf(mockNode).getArchiveURI(); will(returnValue(null));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockNode); will(returnValue(isNodeMetadata));
            
            oneOf(mockVersioningHandler).moveFileToOrphansFolder(mockWorkspace, mockNode); will(returnValue(newNodeLocation));
            
            oneOf(mockNode).getWorkspaceURL(); will(returnValue(nodeWsLocation));
        }});
        
        updateReferenceInParent(parentWsUrl, nodeWsLocationUri, newNodeLocationUri, nodeFilenameUri, expectedCause);

        try {
            unlinkedNodeExporter.exportNode(mockWorkspace, mockParentNode, parentCorpusNamePathToClosestTopNode, mockNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown an exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
            assertEquals("Exception cause different from expected", expectedCause, ex.getCause());
        }
    }
    
    
    private void initialExpectations(final boolean isNodeProtected) {
        context.checking(new Expectations() {{
            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(wsID));
            allowing(mockNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
            
            oneOf(mockNode).isProtected(); will(returnValue(isNodeProtected));
        }});
    }
    
    private void updateReferenceInParent(final URL parentWsUrl, final URI oldNodeLocationUri,
            final URI newNodeLocationUri, final URI newNodeLocationFilenameUri,
            final Exception saveDocumentException)
                throws IOException, MetadataException, TransformerException {
        context.checking(new Expectations() {{
            allowing(mockParentNode).getWorkspaceURL(); will(returnValue(parentWsUrl));
            oneOf(mockNodeUtil).isNodeMetadata(mockParentNode); will(returnValue(Boolean.TRUE));
            oneOf(mockMetadataAPI).getMetadataDocument(parentWsUrl); will(returnValue(mockParentDocument));
            oneOf(mockParentDocument).getDocumentReferenceByLocation(oldNodeLocationUri); will(returnValue(mockReference));
            
            if(newNodeLocationFilenameUri == null) {
                oneOf(mockParentDocument).removeDocumentReference(mockReference);
            } else {
                oneOf(mockReference).setURI(newNodeLocationFilenameUri);
                oneOf(mockReference).setLocation(newNodeLocationUri);
                
                if(saveDocumentException == null) {
                    oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentWsUrl);
                } else {
                    oneOf(mockMetadataApiBridge).saveMetadataDocument(mockParentDocument, parentWsUrl);
                        will(throwException(saveDocumentException));
                }
            }
        }});
    }
    
    private void updateNodeWorkspaceUrlInDb(final URL newNodeLocation) {
        
        context.checking(new Expectations() {{
            oneOf(mockNode).setWorkspaceURL(newNodeLocation);
            oneOf(mockWorkspaceDao).updateNodeWorkspaceURL(mockNode);
            
            allowing(mockNode).getWorkspaceURL(); will(returnValue(newNodeLocation));
        }});
    }
    
    private void updateNodeArchiveUrlInDb(final URL mockUrl) {
        
        context.checking(new Expectations() {{
            oneOf(mockNode).setArchiveURL(mockUrl);
            oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockNode);
        }});
    }
    
    private void setNodeAsDeletedInDb() {
        
        final boolean isExternal = Boolean.FALSE;
        
        context.checking(new Expectations() {{
            allowing(mockNode).isExternal(); will(returnValue(isExternal));
            oneOf(mockWorkspaceDao).setWorkspaceNodeAsDeleted(wsID, nodeWsID, Boolean.FALSE);
        }});
    }
}