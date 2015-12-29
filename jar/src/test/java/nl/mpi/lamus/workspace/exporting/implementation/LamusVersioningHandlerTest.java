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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.CorpusNode;
import nl.mpi.archiving.corpusstructure.core.service.NodeResolver;
import nl.mpi.archiving.corpusstructure.provider.CorpusStructureProvider;
import nl.mpi.handle.util.HandleParser;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
import nl.mpi.lamus.workspace.model.NodeUtil;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.apache.commons.io.FileUtils;
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
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FileUtils.class, Files.class})
public class LamusVersioningHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock HandleParser mockHandleParser;
    @Mock CorpusStructureProvider mockCorpusStructureProvider;
    @Mock NodeResolver mockNodeResolver;
    @Mock NodeUtil mockNodeUtil;
    
    @Mock CorpusNode mockCorpusNode;
    @Mock Workspace mockWorkspace;
    @Mock WorkspaceNode mockWorkspaceNode;
    
    private VersioningHandler versioningHandler;
    
    public LamusVersioningHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        versioningHandler = new LamusVersioningHandler(mockArchiveFileHelper, mockArchiveFileLocationProvider,
                mockHandleParser, mockCorpusStructureProvider, mockNodeResolver, mockNodeUtil);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void moveFileToTrashCanSucceeds() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final String testNodeStrippedHandle = UUID.randomUUID().toString();
        final String testNodeFullHandle = "hdl:12345/" + testNodeStrippedHandle;
        final URI testNodeFullArchiveURI = URI.create(testNodeFullHandle);
        
        final String fileBaseName = "node.cmdi";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/" + fileBaseName);
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testNodeStrippedHandle).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File trashedDirectory = new File("/lat/corpora/trashcan/2013-05/10");
        final File trashedFile = new File(trashedDirectory, fileNameBuilder.toString());
        final URL trashedURL = trashedFile.toURI().toURL();
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeFullArchiveURI, testNodeArchiveURL);
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testNodeFullArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(archiveFile));
            
            oneOf(mockArchiveFileHelper).getDirectoryForDeletedNode(workspaceID); will(returnValue(trashedDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(trashedDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockHandleParser).stripAndValidateHandleIfPrefixIsKnown(testNodeFullArchiveURI); will(returnValue(testNodeStrippedHandle));
            oneOf(mockArchiveFileHelper).getTargetFileForReplacedOrDeletedNode(trashedDirectory, testNodeStrippedHandle, archiveFile); will(returnValue(trashedFile));
        }});
        
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        
        URL result = versioningHandler.moveFileToTrashCanFolder(testNode);
        
        assertEquals("Version URL different from expected", trashedURL, result);
    }
    
    @Test
    public void moveFileToVersioningSucceeds() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final String testNodeStrippedHandle = UUID.randomUUID().toString();
        final String testNodeFullHandle = "hdl:12345/" + testNodeStrippedHandle;
        final URI testNodeFullArchiveURI = URI.create(testNodeFullHandle);
        
        final String fileBaseName = "node.cmdi";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/" + fileBaseName);
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testNodeStrippedHandle).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File versioningDirectory = new File("/lat/corpora/versioning/2013-05/10");
        final File versioningFile = new File(versioningDirectory, fileNameBuilder.toString());
        final URL versioningURL = versioningFile.toURI().toURL();
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeFullArchiveURI, testNodeArchiveURL);
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testNodeFullArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(archiveFile));
            
            oneOf(mockArchiveFileHelper).getDirectoryForReplacedNode(workspaceID); will(returnValue(versioningDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(versioningDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockHandleParser).stripAndValidateHandleIfPrefixIsKnown(testNodeFullArchiveURI); will(returnValue(testNodeStrippedHandle));
            oneOf(mockArchiveFileHelper).getTargetFileForReplacedOrDeletedNode(versioningDirectory, testNodeStrippedHandle, archiveFile); will(returnValue(versioningFile));
        }});
        
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        
        URL result = versioningHandler.moveFileToVersioningFolder(testNode);
        
        assertEquals("Version URL different from expected", versioningURL, result);
    }
    
    @Test
    public void moveFileToTrashCanFailsTargetLocation() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final String testNodeStrippedHandle = UUID.randomUUID().toString();
        final String testNodeFullHandle = "hdl:12345/" + testNodeStrippedHandle;
        final URI testNodeFullArchiveURI = URI.create(testNodeFullHandle);
        
        final String fileBaseName = "node.cmdi";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/" + fileBaseName);
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testNodeStrippedHandle).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File trashedDirectory = new File("/lat/corpora/trashcan/2013-05/10");
        final File trashedFile = new File(trashedDirectory, fileNameBuilder.toString());
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeFullArchiveURI, testNodeArchiveURL);
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testNodeFullArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(archiveFile));
            
            oneOf(mockArchiveFileHelper).getDirectoryForDeletedNode(workspaceID); will(returnValue(trashedDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(trashedDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockHandleParser).stripAndValidateHandleIfPrefixIsKnown(testNodeFullArchiveURI); will(returnValue(testNodeStrippedHandle));
            oneOf(mockArchiveFileHelper).getTargetFileForReplacedOrDeletedNode(trashedDirectory, testNodeStrippedHandle, archiveFile); will(returnValue(trashedFile));
        }});
        
        stub(method(FileUtils.class, "moveFile", File.class, File.class)).toThrow(expectedException);
        
        URL result = versioningHandler.moveFileToTrashCanFolder(testNode);
        
        assertNull("Result should be null", result);
    }
    
    @Test
    public void moveFileToTrashCanFailsWriteTargetDirectory() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final String testNodeStrippedHandle = UUID.randomUUID().toString();
        final String testNodeFullHandle = "hdl:12345/" + testNodeStrippedHandle;
        final URI testNodeFullArchiveURI = URI.create(testNodeFullHandle);
        
        final String fileBaseName = "node.cmdi";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/" + fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File trashedDirectory = new File("/lat/corpora/trashcan/2013-05/10");
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeFullArchiveURI, testNodeArchiveURL);
        
        context.checking(new Expectations() {{
            
            oneOf(mockCorpusStructureProvider).getNode(testNodeFullArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(archiveFile));
            
            oneOf(mockArchiveFileHelper).getDirectoryForDeletedNode(workspaceID); will(returnValue(trashedDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(trashedDirectory); will(returnValue(Boolean.FALSE));
        }});
        
        URL result = versioningHandler.moveFileToTrashCanFolder(testNode);
        
        assertNull("Result should be null", result);
    }
    
    @Test
    public void moveFileToOrphansFolder_ResourceNodeWithArchiveURI() throws MalformedURLException, URISyntaxException {
        
        final URL wsTopNodeUrl = new URL("file:/workspace/folder/topnode.cmdi");
        final URI wsTopNodeUrlToUri = wsTopNodeUrl.toURI();
        final String testNodeStrippedHandle = UUID.randomUUID().toString();
        final String testNodeFullHandle = "hdl:12345/" + testNodeStrippedHandle;
        final URI testNodeFullArchiveURI = URI.create(testNodeFullHandle);
        
        final String fileBaseName = "node.txt";
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File fileToMove = new File(archiveDirectory, fileBaseName);
        final File orphansDirectory = new File("/lat/corpora/corpora/archive/somefolder/sessions");
        
        final File finalFile = new File(orphansDirectory, fileBaseName);
        final URL expectedURL = finalFile.toURI().toURL();
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeFullArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(testNodeFullArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(fileToMove));
            oneOf(mockNodeUtil).isNodeMetadata(mockWorkspaceNode); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(wsTopNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(wsTopNodeUrlToUri); will(returnValue(orphansDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(orphansDirectory); will(returnValue(Boolean.TRUE));
            
            oneOf(mockArchiveFileHelper).getFinalFile(orphansDirectory, fileBaseName); will(returnValue(finalFile));
        }});
        
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        suppress(method(Files.class, "deleteIfExists", Path.class));
        
        URL result = versioningHandler.moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode);
        
        assertEquals("Result different from expected", expectedURL, result);
    }
    
    @Test
    public void moveFileToOrphansFolder_ResourceNodeWithArchiveURI_FileNull() throws MalformedURLException, URISyntaxException {
        
        final String testNodeStrippedHandle = UUID.randomUUID().toString();
        final String testNodeFullHandle = "hdl:12345/" + testNodeStrippedHandle;
        final URI testNodeFullArchiveURI = URI.create(testNodeFullHandle);
        
        final String expectedMessage = "No valid file location was found.";
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeFullArchiveURI));
            oneOf(mockNodeUtil).isNodeMetadata(mockWorkspaceNode); will(returnValue(Boolean.FALSE));
            oneOf(mockCorpusStructureProvider).getNode(testNodeFullArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(null));
        }});
        
        try {
            versioningHandler.moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode);
            fail("should have thrown exception");
        } catch(IllegalStateException ex) {
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
    
    @Test
    public void moveFileToOrphansFolder_ResourceNodeWithoutArchiveURI() throws MalformedURLException, URISyntaxException {
        
        final URL wsTopNodeUrl = new URL("file:/workspace/folder/topnode.cmdi");
        final URI wsTopNodeUrlToUri = wsTopNodeUrl.toURI();
        
        final String fileBaseName = "node.txt";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final File fileToMove = new File(testNodeWsURL.getPath());
        final File orphansDirectory = new File("/lat/corpora/corpora/archive/somefolder/sessions");
        
        final File finalFile = new File(orphansDirectory, fileBaseName);
        final URL expectedURL = finalFile.toURI().toURL();
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspaceNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockWorkspaceNode).getWorkspaceURL(); will(returnValue(testNodeWsURL));
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(fileToMove); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(wsTopNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(wsTopNodeUrlToUri); will(returnValue(orphansDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(orphansDirectory); will(returnValue(Boolean.TRUE));
            
            oneOf(mockArchiveFileHelper).getFinalFile(orphansDirectory, fileBaseName); will(returnValue(finalFile));
        }});
        
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        
        URL result = versioningHandler.moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode);
        
        assertEquals("Result different from expected", expectedURL, result);
    }
    
    @Test
    public void moveFileToOrphansFolder_MetadataWithoutArchiveURI() throws MalformedURLException, URISyntaxException {
        
        final URL wsTopNodeUrl = new URL("file:/workspace/folder/topnode.cmdi");
        final URI wsTopNodeUrlToUri = wsTopNodeUrl.toURI();
        
        final String fileBaseName = "node.cmdi";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final File fileToMove = new File(testNodeWsURL.getPath());
        final File orphansDirectory = new File("/lat/corpora/corpora/archive/somefolder/sessions");
        
        final File finalFile = new File(orphansDirectory, fileBaseName);
        final URL expectedURL = finalFile.toURI().toURL();
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspaceNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockWorkspaceNode).getWorkspaceURL(); will(returnValue(testNodeWsURL));
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(fileToMove); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(wsTopNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(wsTopNodeUrlToUri); will(returnValue(orphansDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(orphansDirectory); will(returnValue(Boolean.TRUE));
            
            oneOf(mockArchiveFileHelper).getFinalFile(orphansDirectory, fileBaseName); will(returnValue(finalFile));
        }});
        
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        
        URL result = versioningHandler.moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode);
        
        assertEquals("Result different from expected", expectedURL, result);
    }
    
    @Test
    public void moveFileToOrphansFolder_MetadataWithArchiveURI() throws MalformedURLException, URISyntaxException {
        
        final URL wsTopNodeUrl = new URL("file:/workspace/folder/topnode.cmdi");
        final URI wsTopNodeUrlToUri = wsTopNodeUrl.toURI();
        final String testNodeStrippedHandle = UUID.randomUUID().toString();
        final String testNodeFullHandle = "hdl:12345/" + testNodeStrippedHandle;
        final URI testNodeFullArchiveURI = new URI(testNodeFullHandle);
        
        final String fileBaseName = "node.cmdi";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final File fileToMove = new File(testNodeWsURL.getPath());
        final File orphansDirectory = new File("/lat/corpora/corpora/archive/somefolder/sessions");
        
        final File finalFile = new File(orphansDirectory, fileBaseName);
        final URL expectedURL = finalFile.toURI().toURL();
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspaceNode).getArchiveURI(); will(returnValue(testNodeFullArchiveURI));
            oneOf(mockCorpusStructureProvider).getNode(testNodeFullArchiveURI); will(returnValue(mockCorpusNode));
            oneOf(mockNodeResolver).getLocalFile(mockCorpusNode); will(returnValue(fileToMove));
            oneOf(mockNodeUtil).isNodeMetadata(mockWorkspaceNode); will(returnValue(Boolean.TRUE));
            oneOf(mockWorkspaceNode).getWorkspaceURL(); will(returnValue(testNodeWsURL));
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(fileToMove); will(returnValue(Boolean.FALSE));
            
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(wsTopNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(wsTopNodeUrlToUri); will(returnValue(orphansDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(orphansDirectory); will(returnValue(Boolean.TRUE));
            
            oneOf(mockArchiveFileHelper).getFinalFile(orphansDirectory, fileBaseName); will(returnValue(finalFile));
        }});
        
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        suppress(method(Files.class, "deleteIfExists", Path.class));
        
        URL result = versioningHandler.moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode);
        
        assertEquals("Result different from expected", expectedURL, result);
    }
    
    @Test
    public void moveFileToOrphansFolder_WithoutArchiveURI_AlreadyInOrphansFolder() throws MalformedURLException, URISyntaxException {
        
        final String fileBaseName = "node.cmdi";
        final URL testNodeWsURL = new URL("file:/workspace/folder/" + fileBaseName);
        final File fileToMove = new File(testNodeWsURL.getPath());
        
        context.checking(new Expectations() {{
            
            allowing(mockWorkspaceNode).getArchiveURI(); will(returnValue(null));
            oneOf(mockWorkspaceNode).getWorkspaceURL(); will(returnValue(testNodeWsURL));
            oneOf(mockArchiveFileLocationProvider).isFileInOrphansDirectory(fileToMove); will(returnValue(Boolean.TRUE));
        }});
        
        URL result = versioningHandler.moveFileToOrphansFolder(mockWorkspace, mockWorkspaceNode);
        
        assertEquals("Result different from expected", testNodeWsURL, result);
    }
    
    
    private WorkspaceNode getTestNode(int wsID, URL nodeWsURL, URI nodeArchiveURI, URL nodeArchiveURL) throws URISyntaxException {
        
        final int wsNodeID = 15;
        final URI nodeSchemaURI = new URI("http://some.location");
        final String nodeName = "some_name";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.DELETED;
        final boolean nodeProtected = Boolean.FALSE;
        final String nodeFormat = "";
        
        return new LamusWorkspaceNode(wsNodeID, wsID, nodeSchemaURI,
                nodeName, "", nodeType, nodeWsURL, nodeArchiveURI, nodeArchiveURL, nodeArchiveURL.toURI(), nodeStatus, nodeProtected, nodeFormat);
    }
}