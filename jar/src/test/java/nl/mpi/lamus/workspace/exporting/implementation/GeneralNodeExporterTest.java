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
import nl.mpi.lamus.metadata.MetadataApiBridge;
import nl.mpi.lamus.workspace.exporting.ExporterHelper;
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
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;
import org.apache.commons.io.FileUtils;
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
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FileUtils.class})
public class GeneralNodeExporterTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock MetadataAPI mockMetadataAPI;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    @Mock WorkspaceFileHandler mockWorkspaceFileHandler;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock NodeUtil mockNodeUtil;
    @Mock ExporterHelper mockExporterHelper;
    
    @Mock CMDIDocument mockChildCmdiDocument;
    @Mock CMDIDocument mockParentCmdiDocument;
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
        ReflectionTestUtils.setField(generalNodeExporter, "metadataApiBridge", mockMetadataApiBridge);
        ReflectionTestUtils.setField(generalNodeExporter, "workspaceFileHandler", mockWorkspaceFileHandler);
        ReflectionTestUtils.setField(generalNodeExporter, "workspaceTreeExporter", mockWorkspaceTreeExporter);
        ReflectionTestUtils.setField(generalNodeExporter, "corpusStructureProvider", mockCorpusStructureProvider);
        ReflectionTestUtils.setField(generalNodeExporter, "nodeResolver", mockNodeResolver);
        ReflectionTestUtils.setField(generalNodeExporter, "archiveFileLocationProvider", mockArchiveFileLocationProvider);
        ReflectionTestUtils.setField(generalNodeExporter, "nodeUtil", mockNodeUtil);
        ReflectionTestUtils.setField(generalNodeExporter, "exporterHelper", mockExporterHelper);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void export_SubmissionTypeDelete() throws WorkspaceExportException {
        
        final boolean keepUnlinkedFiles = Boolean.TRUE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.DELETE_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.UNLINKED_NODES_EXPORT;
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
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
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
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
        
        final int nodeWsID = 10;
        final String nodeFilename = "topnode.cmdi";
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https://archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final URL nodeArchiveURL = new URL(nodeArchivePath);
        final String nodeArchiveLocalPath = "file:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        final String currentCorpusNamePathToClosestTopNode = ""; // top node
        
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
            
            oneOf(mockExporterHelper).getNamePathToUseForThisExporter(
                    mockChildWsNode, null, null, Boolean.TRUE, generalNodeExporter.getClass());
                will(returnValue(currentCorpusNamePathToClosestTopNode));
            
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nodeArchiveLocalFile); will(returnValue(mockStreamResult));
            
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
        }});
        
        generalNodeExporter.exportNode(workspace, null, null, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportChangedMetadataNode()
            throws MalformedURLException, URISyntaxException, IOException,
            MetadataException, TransformerException, WorkspaceExportException {
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "TopNode";
        final String metadataExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final String parentNodeWsPath = "file:/workspace" + workspace.getWorkspaceID() + File.separator + parentFilename;
        final URL parentNodeWsURL = new URL(parentNodeWsPath);
        final File parentNodeWsFile = new File(URI.create(parentNodeWsPath));
        final URI parentNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String parentNodeArchiveLocalPath = "file:/archive/location/TopNode/Corpusstructure/" + parentFilename;
        final URL parentNodeArchiveLocalURL = new URL(parentNodeArchiveLocalPath);
        final File parentNodeArchiveLocalFile = new File(URI.create(parentNodeArchiveLocalPath));
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final URL nodeArchiveURL = new URL(nodeArchivePath);
        final String nodeArchiveLocalPath = "file:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        final String currentCorpusNamePathToClosestTopNode = "TopNode";
        
        final String nodePathRelativeToParent = nodeFilename;
        
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
            
            oneOf(mockExporterHelper).getNamePathToUseForThisExporter(
                    mockChildWsNode, mockParentWsNode, parentCorpusNamePathToClosestTopNode, Boolean.TRUE, generalNodeExporter.getClass());
                will(returnValue(currentCorpusNamePathToClosestTopNode));
            
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nodeArchiveLocalFile); will(returnValue(mockStreamResult));
            
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
        }});
        
        checkParentReferenceUpdateInvocations(nodeArchiveURI, parentNodeArchiveURI, parentNodeWsURL, parentNodeWsFile,
                parentNodeArchiveLocalURL, parentNodeArchiveLocalFile, nodeArchiveLocalFile, nodePathRelativeToParent, null);
        
        generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    @Test
    public void exportUnknownMetadataNode() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int parentNodeWsID = 1;
        final String metadataExtension = "cmdi";
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
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
            generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", null, ex.getCause());
        }
    }
    
    @Test
    public void export_ProblemsRetrievingCorpusNamePath() throws MalformedURLException, URISyntaxException {
        
        final int parentNodeWsID = 1;
        final String metadataExtension = "cmdi";
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final URL nodeArchiveURL = new URL(nodeArchivePath);
        final String nodeArchiveLocalPath = "file:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        final String expectedExceptionMessage = "Problems retrieving the corpus name path for node " + nodeArchiveURI;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        checkLoggerInvocations(parentNodeWsID, nodeWsID);
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).isProtected(); will(returnValue(isNodeProtected));
            
            allowing(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
            oneOf(mockCorpusStructureProvider).getNode(nodeArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(nodeArchiveLocalFile));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockChildWsNode); will(returnValue(Boolean.TRUE));
            
            oneOf(mockExporterHelper).getNamePathToUseForThisExporter(
                    mockChildWsNode, mockParentWsNode, parentCorpusNamePathToClosestTopNode, Boolean.TRUE, generalNodeExporter.getClass());
                will(returnValue(null));
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspace.getWorkspaceID(), ex.getWorkspaceID());
            assertNull("Cause should be null", ex.getCause());
        }
    }
    
    
    @Test
    public void exportProtectedNode() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "TopNode";
        final String metadataExtension = "cmdi";
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final String nodeArchivePath = "https:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
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
        
        generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
    }
    
    
    
    @Test
    public void exportNullWorkspace() throws MalformedURLException, URISyntaxException, WorkspaceExportException {
        
        final String metadataExtension = "cmdi";
        
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/TopNode/Corpusstructure/" + nodeFilename);
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        workspace.setTopNodeID(nodeWsID);
        workspace.setTopNodeArchiveURI(nodeArchiveURI);
        workspace.setTopNodeArchiveURL(nodeArchiveURL);
        
        try {
            generalNodeExporter.exportNode(null, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
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
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/TopNode/Corpusstructure/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        final String currentCorpusNamePathToClosestTopNode = "TopNode";
        
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
            
            oneOf(mockExporterHelper).getNamePathToUseForThisExporter(
                    mockChildWsNode, mockParentWsNode, parentCorpusNamePathToClosestTopNode, Boolean.TRUE, generalNodeExporter.getClass());
                will(returnValue(currentCorpusNamePathToClosestTopNode));
                
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(throwException(expectedException));
            //logger
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
            
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
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
        final String parentCorpusNamePathToClosestTopNode = ""; // top node
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/TopNode/Corpusstructure/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        final String currentCorpusNamePathToClosestTopNode = "TopNode";
        
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
            oneOf(mockExporterHelper).getNamePathToUseForThisExporter(
                    mockChildWsNode, mockParentWsNode, parentCorpusNamePathToClosestTopNode, Boolean.TRUE, generalNodeExporter.getClass());
                will(returnValue(currentCorpusNamePathToClosestTopNode));
            
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(throwException(expectedException));
            //logger
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
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
        final String parentCorpusNamePathToClosestTopNode = "";
        
        final int nodeWsID = 10;
        final String nodeName = "SomeNode";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + metadataExtension;
        final URL nodeWsURL = new URL("file:/workspace/" + workspace.getWorkspaceID() + File.separator + nodeFilename);
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/TopNode/Corpusstructure/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/TopNode/Corpusstructure/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        final String currentCorpusNamePathToClosestTopNode = "TopNode";
        
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
            
            oneOf(mockExporterHelper).getNamePathToUseForThisExporter(
                    mockChildWsNode, mockParentWsNode, parentCorpusNamePathToClosestTopNode, Boolean.TRUE, generalNodeExporter.getClass());
                will(returnValue(currentCorpusNamePathToClosestTopNode));
            
            oneOf(mockWorkspaceTreeExporter).explore(workspace, mockChildWsNode, currentCorpusNamePathToClosestTopNode, keepUnlinkedFiles, submissionType, exportPhase);
            
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
            
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nodeArchiveLocalFile); will(returnValue(mockStreamResult));
            
            oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
                will(throwException(expectedException));
            //logger
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeArchiveURI));
        }});
        
        try {
            generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", workspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportResourceNode() throws MalformedURLException, URISyntaxException, WorkspaceExportException, IOException, MetadataException, TransformerException {
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "ParentNode";
        final String metadataExtension = "cmdi";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final URL parentNodeWsURL = new URL("file:/workspace" + workspace.getWorkspaceID() + File.separator + parentFilename);
        final URI parentNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final File parentNodeWsFile = new File(parentNodeWsURL.getPath());
        final String parentNodeArchiveLocalPath = "file:/archive/location/TopNode/SomeNode/Metadata/" + parentFilename;
        final URL parentNodeArchiveLocalUrl = new URL(parentNodeArchiveLocalPath);
        final File parentNodeArchiveLocalFile = new File(URI.create(parentNodeArchiveLocalPath));
        final String parentCorpusNamePathToClosestTopNode = "TopNode/SomeNode";
        
        final int nodeWsID = 10;
        final String nodeName = "SomeResource";
        final String pdfExtension = "pdf";
        final String nodeFilename = nodeName + FilenameUtils.EXTENSION_SEPARATOR_STR  + pdfExtension;
        final URI nodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL nodeArchiveURL = new URL("http:/archive/location/TopNode/SomeNode/Annotations/" + nodeFilename);
        final String nodeArchiveLocalPath = "file:/archive/location/TopNode/SomeNode/Annotations/" + nodeFilename;
        final File nodeArchiveLocalFile = new File(URI.create(nodeArchiveLocalPath));
        final boolean isNodeProtected = Boolean.FALSE;
        
        final boolean keepUnlinkedFiles = Boolean.FALSE; //not used in this exporter
        final WorkspaceSubmissionType submissionType = WorkspaceSubmissionType.SUBMIT_WORKSPACE;
        final WorkspaceExportPhase exportPhase = WorkspaceExportPhase.TREE_EXPORT;
        
        final String nodePathRelativeToParent = "../Annotations/" + nodeFilename;
        
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
        }});
        
        checkParentReferenceUpdateInvocations(nodeArchiveURI, parentNodeArchiveURI, parentNodeWsURL, parentNodeWsFile,
                parentNodeArchiveLocalUrl, parentNodeArchiveLocalFile, nodeArchiveLocalFile, nodePathRelativeToParent, null);
        
        generalNodeExporter.exportNode(workspace, mockParentWsNode, parentCorpusNamePathToClosestTopNode, mockChildWsNode, keepUnlinkedFiles, submissionType, exportPhase);
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
            final URL parentArchiveLocalUrl, final File parentArchiveLocalFile, final File childArchiveLocalFile,
            final String childPathRelativeToParent,
            final Exception expectedException) throws IOException, MetadataException, TransformerException, URISyntaxException {
        
        final URI childUriRelativeToParent = URI.create(childPathRelativeToParent);
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentWsNode).getArchiveURI(); will(returnValue(parentArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(parentArchiveURI); will(returnValue(mockParentCorpusNode));
            oneOf(mockParentWsNode).getArchiveURL(); will(returnValue(parentArchiveLocalUrl));
            
            oneOf(mockNodeUtil).isNodeMetadata(mockParentWsNode); will(returnValue(Boolean.TRUE));
            oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentWsURL);
                will(returnValue(mockParentCmdiDocument));
            
            oneOf(mockArchiveFileLocationProvider).getChildPathRelativeToParent(parentArchiveLocalFile, childArchiveLocalFile);
                will(returnValue(childPathRelativeToParent));
            
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(childArchiveURI));
            oneOf(mockMetadataApiBridge).getDocumentReferenceByDoubleCheckingURI(mockParentCmdiDocument, childArchiveURI);
                will(returnValue(mockResourceProxy));
            oneOf(mockResourceProxy).setLocation(childUriRelativeToParent);
            
            
            oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentWsURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(parentWsFile);
                will(returnValue(mockStreamResult));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(parentArchiveLocalFile);
        
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