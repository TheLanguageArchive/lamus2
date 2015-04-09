/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
import nl.mpi.archiving.corpusstructure.core.FileInfo;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.workspace.exporting.NodeExporter;
import nl.mpi.lamus.workspace.exporting.WorkspaceTreeExporter;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceExportPhase;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.WorkspaceSubmissionType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.apache.commons.io.FilenameUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
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
public class GeneralNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock MetadataAPI mockMetadataAPI;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock NodeUtil mockNodeUtil;
    
    @Mock ReferencingMetadataDocument mockChildCmdiDocument;
    @Mock ReferencingMetadataDocument mockParentCmdiDocument;
    @Mock ResourceProxy mockResourceProxy;
    @Mock StreamResult mockStreamResult;
    @Mock CorpusNode mockCorpusNode;
    @Mock FileInfo mockFileInfo;
    @Mock CorpusNode mockParentCorpusNode;
    
    @Mock WorkspaceNode mockParentWsNode;
    @Mock WorkspaceNode mockChildWsNode;
    
    private NodeExporter generalNodeExporter;
    private Workspace workspace;
    
    public GeneralNodeExporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        workspace = new LamusWorkspace(1, "someUser", -1, null, null,
                Calendar.getInstance().getTime(), null, Calendar.getInstance().getTime(), null,
                0L, 10000L, WorkspaceStatus.SUBMITTED, "Workspace submitted", "");

        generalNodeExporter = new GeneralNodeExporter();
        ReflectionTestUtils.setField(generalNodeExporter, "metadataAPI", mockMetadataAPI);
        ReflectionTestUtils.setField(generalNodeExporter, "workspaceFileHandler", mockWorkspaceFileHandler);
        ReflectionTestUtils.setField(generalNodeExporter, "workspaceTreeExporter", mockWorkspaceTreeExporter);
        ReflectionTestUtils.setField(generalNodeExporter, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(generalNodeExporter, "nodeResolver", mockNodeResolver);
        ReflectionTestUtils.setField(generalNodeExporter, "archiveFileLocationProvider", mockArchiveFileLocationProvider);
        ReflectionTestUtils.setField(generalNodeExporter, "nodeUtil", mockNodeUtil);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void export_SubmissionTypeDelete() throws WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "This exporter should only be used when submitting the workspace, not when deleting";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void export_ExportPhaseUnlinkedNodes() throws WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "This exporter should only be used when exporting the tree, not for unlinked nodes";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }

    @Test
    public void exportChangedTopNode()
            throws MalformedURLException, URISyntaxException, IOException,
            MetadataException, TransformerException, WorkspaceExportException {
    
        /*
         * File already exists in the archive.
         * Should it be copied anyway?
         * If not, there should be some indication about it being unchanged.
         * But maybe there is a risk that afterwards the file will be moved (because of something changing in the parent, for instance) - but still the file can be moved as it is...
         * 
         * option 1 - copy always in any case
         * option 2 - find a way of checking efficiently for differences, and copy only if they exist
         */
        
        final int nodeWsID = 10;
        final String nodeFilename = "someNode.cmdi";
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https://archive/location/" + nodeFilename;
        final URL nodeArchiveURL = new URL(nodeArchivePath);
        final String nodeArchiveLocalPath = "file:/archive/location/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        checkLoggerInvocations(-1, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(nodeArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nodeArchiveLocalFile); will(returnValue(mockStreamResult));
            
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
        }});
        
        generalNodeExporter.exportNode(workspace, null, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportChangedMetadataNode()
            throws MalformedURLException, URISyntaxException, IOException,
            MetadataException, TransformerException, WorkspaceExportException {
        
        /*
         * File already exists in the archive.
         * Should it be copied anyway?
         * If not, there should be some indication about it being unchanged.
         * But maybe there is a risk that afterwards the file will be moved (because of something changing in the parent, for instance) - but still the file can be moved as it is...
         * 
         * option 1 - copy always in any case
         * option 2 - find a way of checking efficiently for differences, and copy only if they exist
         */
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "parentNode";
        final String metadataExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final String parentNodeWsPath = "file:/workspace" + workspace.getWorkspaceID() + File.separator + parentFilename;
        final URL parentNodeWsURL = new URL(parentNodeWsPath);
        final File parentNodeWsFile = new File(URI.create(parentNodeWsPath));
        final URI parentNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String parentNodeArchivePath = "https:/archive/location/" + parentFilename;
        final URL parentNodeArchiveURL = new URL(parentNodeArchivePath);
        final String parentNodeArchiveLocalPath = "file:/archive/location/" + parentFilename;
        final File parentNodeArchiveLocalFile = new File(URI.create(parentNodeArchiveLocalPath));
        final URL parentNodeArchiveLocalURL = new URL(parentNodeArchiveLocalPath);
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https:/archive/location/child/" + nodeFilename;
        final URL nodeArchiveURL = new URL(nodeArchivePath);
        final String nodeArchiveLocalPath = "file:/archive/location/child/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final String nodePathRelativeToParent = "child/" + nodeFilename;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(nodeArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nodeArchiveLocalFile); will(returnValue(mockStreamResult));
            
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
        }});
        
        checkParentReferenceUpdateInvocations(nodeArchiveURI, parentNodeArchiveURI, parentNodeWsURL, parentNodeWsFile,
                parentNodeArchiveLocalFile, nodeArchiveLocalFile, nodePathRelativeToParent, null);
        
        generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    
    
    //AT THE MOMENT (due to the localURI being edited in the import process),
        // METADATA FILES END UP ALWAYS BEING CHANGED
    
//    @Test
//    public void exportUnchangedMetadataNode()
//            throws MalformedURLException, URISyntaxException, WorkspaceExportException,
//            IOException, MetadataException, TransformerException {
//        
//        final String parentNodeName = "parentNode";
//        final String metadataExtension = "cmdi";
//        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
//        final URL parentNodeWsURL = new URL("file:/workspace" + workspace.getWorkspaceID() + File.separator + parentFilename);
//        final File parentNodeWsFile = new File(parentNodeWsURL.getPath());
//        final URL parentNodeArchiveURL = new URL("http:/archive/root/somenode/" + parentFilename);
//        final URL parentNodeArchiveLocalURL = new URL("file:/archive/location/" + parentFilename);
//        final String parentNodeArchiveLocalPath = parentNodeArchiveLocalURL.toURI().getSchemeSpecificPart();
//        
//        final int nodeWsID = 10;
//        final String nodeName = "someNode";
//        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
//        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
//        final File nodeWsFile = new File(nodeWsURL.getPath());
//        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
//        final URL nodeArchiveURL = new URL("http:/archive/location/" + nodeFilename);
//        final URL nodeArchiveLocalURL = new URL("file:/archive/location/child/" + nodeFilename);
//        final String nodeArchiveLocalPath = nodeArchiveLocalURL.toURI().getSchemeSpecificPart();
//        final File nodeArchiveLocalFile = new File(nodeArchiveLocalPath);
//        
//        final String nodePathRelativeToParent = "child/" + nodeFilename;
//        final URL nodeUrlRelativeToParent = new URL(parentNodeArchiveLocalURL, nodePathRelativeToParent);
//        
//        workspace.setTopNodeID(nodeWsID);
//        workspace.setTopNodeArchiveURI(nodeArchiveURI);
//        workspace.setTopNodeArchiveURL(nodeArchiveURL);
//        
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.TRUE));
//            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode);
//            
//            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
//            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
//            oneOf(mockCorpusNode).getFileInfo(); will(returnValue(mockFileInfo));
//            // Files are similar, so export will return
//            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
//            oneOf(mockArchiveFileHelper).hasArchiveFileChanged(mockFileInfo, nodeWsFile); will(returnValue(Boolean.FALSE));
//            
//        }});
//        
//        checkParentReferenceUpdateInvocations(nodeArchiveURI, parentNodeWsURL, parentNodeWsFile,
//                parentNodeArchiveURL, parentNodeArchiveLocalURL, parentNodeArchiveLocalPath,
//                nodeArchiveLocalPath, nodePathRelativeToParent, nodeUrlRelativeToParent, null);
//        
//        generalNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
//    }
    
    @Test
    public void exportUnknownMetadataNode() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "parentNode";
        final String metadataExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https:/archive/location/" + nodeFilename;
        final URL nodeArchiveURL = new URL(nodeArchivePath);
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        final String expectedErrorMessage = "Node not found in archive database for URI " + nodeArchiveURI;
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(null));
            //logger
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", null, ex.getCause());
        }
    }
    
    //TODO TEST WHEN NODE RESOLVER RETURNS NULL
    
    
    @Test
    public void exportProtectedNode() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "parentNode";
        final String metadataExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https:/archive/location/" + nodeFilename;
        final URL nodeArchiveURL = new URL(nodeArchivePath);
        final boolean isNodeProtected = Boolean.TRUE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            //logger
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(nodeWsID));
        }});
        
        generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    
    
    @Test
    public void exportNullWorkspace() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final String metadataExtension = "cmdi";
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/" + nodeFilename);
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        try {
            generalNodeExporter.exportNode(null, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch (IllegalArgumentException ex) {
            String errorMessage = "Workspace not set";
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    @Test
    public void exportChangedMetadataNodeIOException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        final String metadataExtension = "cmdi";
        
        final int parentNodeWsID = 1;
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/child/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        final String expectedErrorMessage = "Error getting Metadata Document for node " + nodeArchiveURI;
        final IOException expectedException = new IOException("some exception message");
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(nodeArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(throwException(expectedException));
            //logger
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportChangedMetadataNodeMetadataException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        final String metadataExtension = "cmdi";
        
        final int parentNodeWsID = 1;
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/child/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        final String expectedErrorMessage = "Error getting Metadata Document for node " + nodeArchiveURI;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(nodeArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(throwException(expectedException));
            //logger
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportChangedMetadataNodeTransformerException() throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        final String metadataExtension = "cmdi";
        
        final int parentNodeWsID = 1;
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        final String expectedErrorMessage = "Error writing file for node " + nodeArchiveURI;
        final TransformerException expectedException = new TransformerException("some exception message");
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(nodeArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nodeArchiveLocalFile); will(returnValue(mockStreamResult));
            
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
                will(throwException(expectedException));
            //logger
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportResourceNode() throws MalformedURLException, URISyntaxException, WorkspaceExportException, IOException, MetadataException, TransformerException {
        
        /*
         * File already exists in the archive.
         * Should it be copied anyway?
         * If not, there should be some indication about it being unchanged.
         * But maybe there is a risk that afterwards the file will be moved (because of something changing in the parent, for instance) - but still the file can be moved as it is...
         * 
         * option 1 - copy always in any case
         * option 2 - find a way of checking efficiently for differences, and copy only if they exist
         */
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "parentNode";
        final String metadataExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final URL parentNodeWsURL = new URL("file:/workspace" + workspace.getWorkspaceID() + File.separator + parentFilename);
        final URI parentNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final File parentNodeWsFile = new File(parentNodeWsURL.getPath());
        final String parentNodeArchiveLocalPath = "file:/archive/location/" + parentFilename;
        final File parentNodeArchiveLocalFile = new File(URI.create(parentNodeArchiveLocalPath));
        
        final int nodeWsID = 10;
        final String nodeName = "someNode";
        final String pdfExtension = "pdf";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + pdfExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/child/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        final String nodePathRelativeToParent = "child/" + nodeFilename;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(nodeArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.FALSE));
            
            //TODO what to expect here?
                // resources are not copied to the workspace, so if they need to be copied back, it means they are replacements,
                    // and therefore don't belong in this exporter...
        }});
        
        checkParentReferenceUpdateInvocations(nodeArchiveURI, parentNodeArchiveURI, parentNodeWsURL, parentNodeWsFile,
                parentNodeArchiveLocalFile, nodeArchiveLocalFile, nodePathRelativeToParent, null);
        
        generalNodeExporter.exportNode(workspace, mockParentWsNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    
    private void checkLoggerInvocations(final int parentNodeID, final int currentNodeID) {
        
        context.checking(new Expectations() {{
            
            if(parentNodeID > -1) {
                oneOf(mockParentWsNode).getWorkspaceNodeID(); will(returnValue(parentNodeID));
            }
            oneOf(mockChildWsNode).getWorkspaceNodeID(); will(returnValue(currentNodeID));
        }});
    }
    
    private void checkParentReferenceUpdateInvocations(
            final URI childArchiveURI, final URI parentArchiveURI, final URL parentWsURL, final File parentWsFile,
            final File parentArchiveLocalFile, final File childArchiveLocalFile,
            final String childPathRelativeToParent,
            final Exception expectedException) throws IOException, MetadataException, TransformerException, URISyntaxException {
        
        final URI childUriRelativeToParent = URI.create(childPathRelativeToParent);
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentWsNode).getArchiveURI(); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockParentCorpusNode); will(returnValue(parentArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentWsURL);
                will(returnValue(mockParentCmdiDocument));
            
            oneOf(mockArchiveFileLocationProvider).getChildPathRelativeToParent(parentArchiveLocalFile, childArchiveLocalFile);
                will(returnValue(childPathRelativeToParent));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockParentCmdiDocument).getDocumentReferenceByURI(childArchiveURI);
                will(returnValue(mockResourceProxy));
            oneOf(mockResourceProxy).setLocation(childUriRelativeToParent);
            
            
            oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentWsURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(parentWsFile);
                will(returnValue(mockStreamResult));
        }});
        
        if(expectedException != null) {
            context.checking(new Expectations() {{
                oneOf(mockMetadataAPI).writeMetadataDocument(mockParentCmdiDocument, mockStreamResult);
                    will(throwException(expectedException));
                //logging for the exception
                oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentWsURL));
            }});
        } else {
            context.checking(new Expectations() {{
                oneOf(mockMetadataAPI).writeMetadataDocument(mockParentCmdiDocument, mockStreamResult);
            }});
        }
    }
}