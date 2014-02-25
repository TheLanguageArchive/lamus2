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
import net.handle.hdllib.HandleException;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleManager;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.exception.WorkspaceExportException;
import nl.mpi.lamus.metadata.MetadataApiBridge;
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
import nl.mpi.metadata.api.model.HeaderInfo;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.cmdi.api.CMDIConstants;
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
import static org.jmock.Expectations.returnValue;
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
    @Mock WorkspaceDao mockWorkspaceDao;
    @Mock SearchClientBridge mockSearchClientBridge;
    @Mock WorkspaceTreeExporter mockWorkspaceTreeExporter;
//    @Mock AmsBridge mockAmsBridge;
    @Mock NodeDataRetriever mockNodeDataRetriever;
    
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    
    @Mock HandleManager mockHandleManager;
    @Mock MetadataApiBridge mockMetadataApiBridge;
    
    // initially had these mock objects as CMDIDocument,
    // but the expectations were not being properly matched after the cast (to ReferencingMetadataObject) was made in the code to be tested
    @Mock ReferencingMetadataDocument mockChildCmdiDocument;
    @Mock ReferencingMetadataDocument mockParentCmdiDocument;
    @Mock StreamResult mockStreamResult;
    @Mock ResourceProxy mockResourceProxy;
    
    @Mock CorpusNode mockParentCorpusNode;
    
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockParentWsNode;
    @Mock WorkspaceNode mockChildWsNode;
    
    private NodeExporter addedNodeExporter;
    private Workspace testWorkspace;
    
    private final String metadataExtension = "cmdi";
    private final String resourceExtension = "pdf";
    
    private final String handleHdlPrefix = "hdl:" + "11142" + "/";
    
    
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
                mockMetadataAPI, mockWorkspaceDao, mockSearchClientBridge, mockWorkspaceTreeExporter,
                mockNodeDataRetriever, mockCorpusStructureProvider, mockNodeResolver, mockHandleManager, mockMetadataApiBridge);
        
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
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, WorkspaceExportException, TransformerException, HandleException {
        
        final URI nodeNewArchiveHandle = new URI(UUID.randomUUID().toString());
        final URI preparedNewArchiveHandle = new URI(handleHdlPrefix + nodeNewArchiveHandle.toString());

        final WorkspaceNode currentNode = getCurrentResourceNode();
        final WorkspaceNode parentNode = getParentNode();
        
        final boolean isFileMetadata = Boolean.FALSE;
        final URL nodeWsURL = currentNode.getWorkspaceURL();
        final String nodeWsPath = nodeWsURL.getPath();
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final String nodeWsFilename = FilenameUtils.getName(nodeWsPath);
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeWsFilename);
        final WorkspaceNodeType nodeType = currentNode.getType();
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        final URI nodeNewArchiveUrlToUri = nodeNewArchiveURL.toURI();
        final URI nodeNewArchiveUriToUriHttpRoot = new URI("http://server/archive/root/somenode/" + nodeWsFilename);
        final String nodeFormat = currentNode.getFormat();
        
        final URL parentNodeArchiveURL = parentNode.getArchiveURL();
        final String parentNodeArchivePath = parentNodeArchiveURL.getPath();
        final URL parentNodeWsURL = parentNode.getWorkspaceURL();
        final File parentNodeWsFile = new File(parentNodeWsURL.getPath());
        
        
        checkFirstInvocations(parentNodeArchiveURL, nodeWsURL, nodeType, nodeWsFilename, nextAvailableFile, nodeNewArchiveURL, parentNodeArchivePath, null);
        
        checkExploreInvocations(isFileMetadata, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
        }});
        
        checkRetrieveMetadataDocumentInvocations(isFileMetadata, nodeWsURL, null);
        
        checkHandleAssignmentInvocations(nodeWsURL, nodeNewArchiveURL, nodeWsFile, nodeNewArchiveUrlToUri, nodeNewArchiveUriToUriHttpRoot, nodeNewArchiveHandle, null);
        
        checkUpdateSelfHandleInvocations(isFileMetadata, nodeNewArchiveHandle, preparedNewArchiveHandle, null);
        
        checkFileMoveInvocations(isFileMetadata, nodeWsFile, nextAvailableFile);
        
        checkParentReferenceUpdateInvocations(nodeWsURL, nodeNewArchiveHandle, preparedNewArchiveHandle, parentNodeWsURL, parentNodeWsFile, null);
        
        checkSearchClientInvocations(nodeFormat, nodeNewArchiveHandle);
                
        
        addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
    }
    
    
    
    @Test
    public void exportUploadedMetadataNode()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException, HandleException {
        
        final URI nodeNewArchiveHandle = new URI(UUID.randomUUID().toString().toUpperCase());
        final URI preparedNewArchiveHandle = new URI(handleHdlPrefix + nodeNewArchiveHandle.toString());
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode parentNode = getParentNode();
        
        final boolean isFileMetadata = Boolean.TRUE;
        final URL nodeWsURL = currentNode.getWorkspaceURL();
        final String nodeWsPath = nodeWsURL.getPath();
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final String nodeWsFilename = FilenameUtils.getName(nodeWsPath);
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeWsFilename);
        final WorkspaceNodeType nodeType = currentNode.getType();
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        final URI nodeNewArchiveUrlToUri = nodeNewArchiveURL.toURI();
        final URI nodeNewArchiveUriToUriHttpRoot = new URI("http://server/archive/root/somenode/" + nodeWsFilename);
        final String nodeFormat = currentNode.getFormat();
        
        final URL parentNodeArchiveURL = parentNode.getArchiveURL();
        final String parentNodeArchivePath = parentNodeArchiveURL.getPath();
        final URL parentNodeWsURL = parentNode.getWorkspaceURL();
        final File parentNodeWsFile = new File(parentNodeWsURL.getPath());
        
        final HeaderInfo childSelfLinkHeaderInfo = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, nodeNewArchiveHandle.toString());
        
        
        checkFirstInvocations(parentNodeArchiveURL, nodeWsURL, nodeType, nodeWsFilename, nextAvailableFile, nodeNewArchiveURL, parentNodeArchivePath, null);
        
        checkExploreInvocations(isFileMetadata, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
        }});
        
        checkRetrieveMetadataDocumentInvocations(isFileMetadata, nodeWsURL, null);
        
        checkHandleAssignmentInvocations(nodeWsURL, nodeNewArchiveURL, nodeWsFile, nodeNewArchiveUrlToUri, nodeNewArchiveUriToUriHttpRoot, nodeNewArchiveHandle, null);
        
        checkUpdateSelfHandleInvocations(isFileMetadata, nodeNewArchiveHandle, preparedNewArchiveHandle, childSelfLinkHeaderInfo);
        
        checkFileMoveInvocations(isFileMetadata, nodeWsFile, nextAvailableFile);
        
        checkParentReferenceUpdateInvocations(nodeWsURL, nodeNewArchiveHandle, preparedNewArchiveHandle, parentNodeWsURL, parentNodeWsFile, null);
        
        checkSearchClientInvocations(nodeFormat, nodeNewArchiveHandle);
        
        
        addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
    }
    
    @Test
    public void exportNodeNullWorkspace()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException {
        
        addedNodeExporter.setWorkspace(null);
        
        try {
            addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
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
        final WorkspaceNode parentNode = getParentNode();
        
        final URL nodeWsURL = currentNode.getWorkspaceURL();
        final String nodeWsPath = nodeWsURL.getPath();
        final String nodeWsFilename = FilenameUtils.getName(nodeWsPath);
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeWsFilename);
        final WorkspaceNodeType nodeType = currentNode.getType();
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        
        final URL parentNodeArchiveURL = parentNode.getArchiveURL();
        final String parentNodeArchivePath = parentNodeArchiveURL.getPath();
                
        final String expectedErrorMessage = "Error getting new file for node " + nodeWsURL;
        final IOException expectedException = new IOException("some exception message");
        
        
        checkFirstInvocations(parentNodeArchiveURL, nodeWsURL, nodeType, nodeWsFilename, nextAvailableFile, nodeNewArchiveURL, parentNodeArchivePath, expectedException);
        
        try {
            addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
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
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode parentNode = getParentNode();
        
        final boolean isFileMetadata = Boolean.TRUE;
        final URL nodeWsURL = currentNode.getWorkspaceURL();
        final String nodeWsPath = nodeWsURL.getPath();
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final String nodeWsFilename = FilenameUtils.getName(nodeWsPath);
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeWsFilename);
        final WorkspaceNodeType nodeType = currentNode.getType();
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        
        final URL parentNodeArchiveURL = parentNode.getArchiveURL();
        final String parentNodeArchivePath = parentNodeArchiveURL.getPath();
        
        final String expectedErrorMessage = "Error getting Metadata Document for node " + nodeWsURL;
        final MetadataException expectedException = new MetadataException("some exception message");
        
        
        checkFirstInvocations(parentNodeArchiveURL, nodeWsURL, nodeType, nodeWsFilename, nextAvailableFile, nodeNewArchiveURL, parentNodeArchivePath, null);
        
        checkExploreInvocations(isFileMetadata, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
        }});
        
        checkRetrieveMetadataDocumentInvocations(isFileMetadata, nodeWsURL, expectedException);
        
        try {
            addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportNodeExploreThrowsException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException, HandleException {
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode parentNode = getParentNode();
        
        final boolean isFileMetadata = Boolean.TRUE;
        final URL nodeWsURL = currentNode.getWorkspaceURL();
        final String nodeWsPath = nodeWsURL.getPath();
        final String nodeWsFilename = FilenameUtils.getName(nodeWsPath);
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeWsFilename);
        final WorkspaceNodeType nodeType = currentNode.getType();
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        
        final URL parentNodeArchiveURL = parentNode.getArchiveURL();
        final String parentNodeArchivePath = parentNodeArchiveURL.getPath();
        
        final WorkspaceExportException expectedException = new WorkspaceExportException("some exception message", testWorkspace.getWorkspaceID(), null);
        
        
        checkFirstInvocations(parentNodeArchiveURL, nodeWsURL, nodeType, nodeWsFilename, nextAvailableFile, nodeNewArchiveURL, parentNodeArchivePath, null);
        
        checkExploreInvocations(isFileMetadata, expectedException);

        try {
            addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Exception different from expected", expectedException, ex);
        }
    }
    
    @Test
    public void exportNodeThrowsHandleException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException, HandleException {
        
        final URI nodeNewArchiveHandle = new URI(UUID.randomUUID().toString());
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode parentNode = getParentNode();
        
        final boolean isFileMetadata = Boolean.TRUE;
        final URL nodeWsURL = currentNode.getWorkspaceURL();
        final String nodeWsPath = nodeWsURL.getPath();
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final String nodeWsFilename = FilenameUtils.getName(nodeWsPath);
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeWsFilename);
        final WorkspaceNodeType nodeType = currentNode.getType();
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        final URI nodeNewArchiveUrlToUri = nodeNewArchiveURL.toURI();
        final URI nodeNewArchiveUriToUriHttpRoot = new URI("http://server/archive/root/somenode/" + nodeWsFilename);
        
        final URL parentNodeArchiveURL = parentNode.getArchiveURL();
        final String parentNodeArchivePath = parentNodeArchiveURL.getPath();
        final URL parentNodeWsURL = parentNode.getWorkspaceURL();
        final File parentNodeWsFile = new File(parentNodeWsURL.getPath());
        
        final String expectedErrorMessage = "Error assigning new handle for node " + nodeWsURL;
        final HandleException expectedException = new HandleException(HandleException.INVALID_VALUE, "some exception message");
        
        
        checkFirstInvocations(parentNodeArchiveURL, nodeWsURL, nodeType, nodeWsFilename, nextAvailableFile, nodeNewArchiveURL, parentNodeArchivePath, null);
        
        checkExploreInvocations(isFileMetadata, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
        }});
        
        checkRetrieveMetadataDocumentInvocations(isFileMetadata, nodeWsURL, null);
        
        checkHandleAssignmentInvocations(nodeWsURL, nodeNewArchiveURL, nodeWsFile, nodeNewArchiveUrlToUri, nodeNewArchiveUriToUriHttpRoot, nodeNewArchiveHandle, expectedException);
        
        try {
            addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    @Test
    public void exportNodeThrowsTransformerException()
            throws MalformedURLException, URISyntaxException, IOException, MetadataException, TransformerException, WorkspaceExportException, HandleException {
        
        final URI nodeNewArchiveHandle = new URI(UUID.randomUUID().toString().toUpperCase());
        final URI preparedNewArchiveHandle = new URI(handleHdlPrefix + nodeNewArchiveHandle.toString());
        
        final WorkspaceNode currentNode = getCurrentMetadataNode();
        final WorkspaceNode parentNode = getParentNode();
        
        final boolean isFileMetadata = Boolean.TRUE;
        final URL nodeWsURL = currentNode.getWorkspaceURL();
        final String nodeWsPath = nodeWsURL.getPath();
        final File nodeWsFile = new File(nodeWsURL.getPath());
        final String nodeWsFilename = FilenameUtils.getName(nodeWsPath);
        final File nextAvailableFile = new File("/archive/root/somenode/" + nodeWsFilename);
        final WorkspaceNodeType nodeType = currentNode.getType();
        final URL nodeNewArchiveURL = nextAvailableFile.toURI().toURL();
        final URI nodeNewArchiveUrlToUri = nodeNewArchiveURL.toURI();
        final URI nodeNewArchiveUriToUriHttpRoot = new URI("http://server/archive/root/somenode/" + nodeWsFilename);
        
        final URL parentNodeArchiveURL = parentNode.getArchiveURL();
        final String parentNodeArchivePath = parentNodeArchiveURL.getPath();
        final URL parentNodeWsURL = parentNode.getWorkspaceURL();
        final File parentNodeWsFile = new File(parentNodeWsURL.getPath());
        
        final String expectedErrorMessage = "Error writing file (updating child reference) for node " + parentNodeWsURL;
        final TransformerException expectedException = new TransformerException("some exception message");
        
        final HeaderInfo childSelfLinkHeaderInfo = new HeaderInfo(CMDIConstants.CMD_HEADER_MD_SELF_LINK, nodeNewArchiveHandle.toString());
        
        
        checkFirstInvocations(parentNodeArchiveURL, nodeWsURL, nodeType, nodeWsFilename, nextAvailableFile, nodeNewArchiveURL, parentNodeArchivePath, null);
        
        checkExploreInvocations(isFileMetadata, null);
        
        context.checking(new Expectations() {{
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
        }});
        
        checkRetrieveMetadataDocumentInvocations(isFileMetadata, nodeWsURL, null);
        
        checkHandleAssignmentInvocations(nodeWsURL, nodeNewArchiveURL, nodeWsFile, nodeNewArchiveUrlToUri, nodeNewArchiveUriToUriHttpRoot, nodeNewArchiveHandle, null);
        
        checkUpdateSelfHandleInvocations(isFileMetadata, nodeNewArchiveHandle, preparedNewArchiveHandle, childSelfLinkHeaderInfo);
        
        checkFileMoveInvocations(isFileMetadata, nodeWsFile, nextAvailableFile);
        
        checkParentReferenceUpdateInvocations(nodeWsURL, nodeNewArchiveHandle, preparedNewArchiveHandle, parentNodeWsURL, parentNodeWsFile, expectedException);
        
        try {
            addedNodeExporter.exportNode(mockParentWsNode, mockChildWsNode);
            fail("should have thrown exception");
        } catch(WorkspaceExportException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Cause different from expected", expectedException, ex.getCause());
        }
    }
    
    
    private void checkFirstInvocations(
            final URL parentNodeArchiveURL, final URL nodeWsURL, final WorkspaceNodeType nodeType,
            final String nodeWsFilename, final File nextAvailableFile,
            final URL nodeNewArchiveURL, final String parentNodeArchivePath, final Exception expectedException) throws IOException {
        
        context.checking(new Expectations() {{
            oneOf(mockParentWsNode).getArchiveURL(); will(returnValue(parentNodeArchiveURL));
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockChildWsNode).getType(); will(returnValue(nodeType));
        }});
        
        if(expectedException != null) {
            context.checking(new Expectations() {{
                oneOf(mockArchiveFileLocationProvider).getAvailableFile(
                        parentNodeArchivePath, nodeWsFilename, nodeType);
                    will(throwException(expectedException));
                //during logging of the exception
                oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            }});
        } else {
        
            context.checking(new Expectations() {{
                oneOf(mockArchiveFileLocationProvider).getAvailableFile(
                        parentNodeArchivePath, nodeWsFilename, nodeType);
                    will(returnValue(nextAvailableFile));
                oneOf(mockChildWsNode).setArchiveURL(nodeNewArchiveURL);
                oneOf(mockWorkspaceDao).updateNodeArchiveUrl(mockChildWsNode);
            }});
        }
    }
    
    
    private void checkExploreInvocations(final boolean isMetadata, final Exception expectedException) throws WorkspaceExportException {
        
        if(isMetadata) {
            if(expectedException != null) {
                context.checking(new Expectations() {{
                    oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.TRUE));
                    oneOf(mockWorkspaceTreeExporter).explore(testWorkspace, mockChildWsNode);
                        will(throwException(expectedException));
                }});
            } else {
                context.checking(new Expectations() {{
                    oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.TRUE));
                    oneOf(mockWorkspaceTreeExporter).explore(testWorkspace, mockChildWsNode);
                    oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.TRUE));

                }});
            }
        } else {
            context.checking(new Expectations() {{
                exactly(2).of(mockChildWsNode).isMetadata(); will(returnValue(Boolean.FALSE));
            }});
        }
    }
    
    private void checkRetrieveMetadataDocumentInvocations(final boolean isMetadata, final URL nodeWsURL, final Exception expectedException) throws IOException, MetadataException {
        
        if(isMetadata) {
            if(expectedException != null) {
                context.checking(new Expectations() {{
                    oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(throwException(expectedException));
                    //during logging of the exception
                    oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
                }});
            } else {
                context.checking(new Expectations() {{
                    oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
                    oneOf(mockMetadataAPI).getMetadataDocument(nodeWsURL); will(returnValue(mockChildCmdiDocument));
                }});
            }
        }
    }
    
    private void checkUpdateSelfHandleInvocations(
            final boolean isMetadata, final URI nodeNewArchiveHandle, final URI preparedNodeNewArchiveHandle,
            final HeaderInfo childSelfLinkHeaderInfo) throws MetadataException, URISyntaxException {
        
        if(isMetadata) {
            context.checking(new Expectations() {{
                oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.TRUE));
                oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeNewArchiveHandle));
                oneOf(mockHandleManager).prepareHandleWithHdlPrefix(nodeNewArchiveHandle); will(returnValue(preparedNodeNewArchiveHandle));
                oneOf(mockMetadataApiBridge).getNewSelfHandleHeaderInfo(preparedNodeNewArchiveHandle); will(returnValue(childSelfLinkHeaderInfo));
                oneOf(mockChildCmdiDocument).putHeaderInformation(childSelfLinkHeaderInfo);
            }});
        } else {
            context.checking(new Expectations() {{
                oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.FALSE));
            }});
        }
    }
    
    private void checkFileMoveInvocations(
            boolean isMetadata, final File nodeWsFile,
            final File nextAvailableFile) throws IOException, WorkspaceExportException, MetadataException, TransformerException {
        
        if(isMetadata) {
            context.checking(new Expectations() {{
                oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.TRUE));
                oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(nextAvailableFile); will(returnValue(mockStreamResult));
                oneOf(mockMetadataAPI).writeMetadataDocument(mockChildCmdiDocument, mockStreamResult);
            }});
        } else {
            context.checking(new Expectations() {{
                oneOf(mockChildWsNode).isMetadata(); will(returnValue(Boolean.FALSE));
                oneOf(mockWorkspaceFileHandler).copyFile(nodeWsFile, nextAvailableFile);
            }});
        }
    }
    
    private void checkHandleAssignmentInvocations(
            final URL nodeWsURL, final URL nodeNewArchiveURL, final File nodeWsFile,
            final URI nodeNewArchiveUrlToUri, final URI nodeNewArchiveUriToUriHttpRoot,
            final URI nodeNewArchiveHandle, final Exception expectedException) throws HandleException, IOException, URISyntaxException {
        
        context.checking(new Expectations() {{
            oneOf(mockChildWsNode).getArchiveURL(); will(returnValue(nodeNewArchiveURL));
            oneOf(mockArchiveFileLocationProvider).getUriWithHttpRoot(nodeNewArchiveUrlToUri); will(returnValue(nodeNewArchiveUriToUriHttpRoot));
        }});
        
        if(expectedException != null) {
            context.checking(new Expectations() {{
                oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
                oneOf(mockHandleManager).assignNewHandle(nodeWsFile, nodeNewArchiveUriToUriHttpRoot); will(throwException(expectedException));
                //logging for the exception
                oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            }});
        } else {
            context.checking(new Expectations() {{
                oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
                oneOf(mockHandleManager).assignNewHandle(nodeWsFile, nodeNewArchiveUriToUriHttpRoot); will(returnValue(nodeNewArchiveHandle));
                oneOf(mockChildWsNode).setArchiveURI(nodeNewArchiveHandle);
                oneOf(mockWorkspaceDao).updateNodeArchiveUri(mockChildWsNode);
            }});
        }
    }
    
    private void checkParentReferenceUpdateInvocations(
            final URL nodeWsURL, final URI nodeNewArchiveHandle, final URI preparedNewArchiveHandle,
            final URL parentNodeWsURL, final File parentNodeWsFile, final Exception expectedException) throws IOException, MetadataException, TransformerException, URISyntaxException {
        
        context.checking(new Expectations() {{
            
            oneOf(mockParentWsNode).isMetadata(); will(returnValue(Boolean.TRUE));
            oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentNodeWsURL));
            oneOf(mockMetadataAPI).getMetadataDocument(parentNodeWsURL);
                will(returnValue(mockParentCmdiDocument));
                
            oneOf(mockChildWsNode).getWorkspaceURL(); will(returnValue(nodeWsURL));
            oneOf(mockParentCmdiDocument).getDocumentReferenceByURI(nodeWsURL.toURI());
                will(returnValue(mockResourceProxy));
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeNewArchiveHandle));
            oneOf(mockHandleManager).prepareHandleWithHdlPrefix(nodeNewArchiveHandle); will(returnValue(preparedNewArchiveHandle));
            oneOf(mockResourceProxy).setURI(preparedNewArchiveHandle);
            
            
            oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentNodeWsURL));
            oneOf(mockWorkspaceFileHandler).getStreamResultForNodeFile(parentNodeWsFile);
                will(returnValue(mockStreamResult));
        }});
        
        if(expectedException != null) {
            context.checking(new Expectations() {{
                oneOf(mockMetadataAPI).writeMetadataDocument(mockParentCmdiDocument, mockStreamResult);
                    will(throwException(expectedException));
                //logging for the exception
                oneOf(mockParentWsNode).getWorkspaceURL(); will(returnValue(parentNodeWsURL));
            }});
        } else {
            context.checking(new Expectations() {{
                oneOf(mockMetadataAPI).writeMetadataDocument(mockParentCmdiDocument, mockStreamResult);
            }});
        }
    }
    
    private void checkSearchClientInvocations(final String nodeFormat, final URI nodeNewArchiveHandle) {
        
        context.checking(new Expectations() {{
            
            oneOf(mockChildWsNode).getFormat(); will(returnValue(nodeFormat));
            oneOf(mockSearchClientBridge).isFormatSearchable(nodeFormat); will(returnValue(Boolean.TRUE));
            oneOf(mockChildWsNode).getArchiveURI(); will(returnValue(nodeNewArchiveHandle));
            oneOf(mockSearchClientBridge).addNode(nodeNewArchiveHandle);
        }});
    }
    
    
    
    
    private WorkspaceNode getParentNode() throws MalformedURLException, URISyntaxException {
        
        final int parentNodeWsID = 1;
        final String parentNodeName = "parentNode";
        final String parentFilename = parentNodeName + FilenameUtils.EXTENSION_SEPARATOR_STR + metadataExtension;
        final URL parentNodeWsURL = new URL("file:/workspace/" + testWorkspace.getWorkspaceID() + File.separator + parentFilename);
        final URL parentNodeOriginURL = new URL("file:/archive/somewhere/" + parentFilename);
        final URL parentNodeArchiveURL = parentNodeOriginURL;
        final URI parentNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final WorkspaceNodeType parentNodeType = WorkspaceNodeType.METADATA;
        final WorkspaceNodeStatus parentNodeStatus = WorkspaceNodeStatus.NODE_ISCOPY;
        final String parentNodeFormat = "text/x-cmdi+xml";
        final URI nodeSchemaLocation = new URI("http://some.location");
        
        return new LamusWorkspaceNode(parentNodeWsID, testWorkspace.getWorkspaceID(), nodeSchemaLocation,
                parentNodeName, "", parentNodeType, parentNodeWsURL, parentNodeArchiveURI, parentNodeArchiveURL, parentNodeOriginURL, parentNodeStatus, parentNodeFormat);
    }
    
    
    private WorkspaceNode getCurrentMetadataNode() throws MalformedURLException, URISyntaxException {
        return getCurrentNode(metadataExtension, WorkspaceNodeType.METADATA, "text/x-cmdi+xml");
    }
    
    private WorkspaceNode getCurrentResourceNode() throws MalformedURLException, URISyntaxException {
        return getCurrentNode(resourceExtension, WorkspaceNodeType.RESOURCE, "application/pdf");
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