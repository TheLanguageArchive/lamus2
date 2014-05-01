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
import java.util.UUID;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.exporting.VersioningHandler;
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
@PrepareForTest({FileUtils.class})
public class LamusVersioningHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    
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
        
        versioningHandler = new LamusVersioningHandler(mockArchiveFileHelper, mockArchiveFileLocationProvider);
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void moveFileToTrashCanSucceeds() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URL testNodeWsURL = new URL("file:/workspace/folder/node.cmdi");
        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/node.cmdi");
        final URL testNodeLocalArchiveURL = new URL("file:/lat/corpora/archive/somefolder/node.cmdi");
        
        final String fileBaseName = "node.cmdi";
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testNodeArchiveURI).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File trashedDirectory = new File("/lat/corpora/trashcan/2013-05/10");
        final File trashedFile = new File(trashedDirectory, fileNameBuilder.toString());
        final URL trashedURL = trashedFile.toURI().toURL();
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL);
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getUriWithLocalRoot(testNodeArchiveURL.toURI());
                will(returnValue(testNodeLocalArchiveURL.toURI()));
            
//            oneOf(mockArchiveFileHelper).getArchiveLocationForNodeID(testArchiveNodeID); will(returnValue(archiveFile));
            oneOf(mockArchiveFileHelper).getDirectoryForDeletedNode(workspaceID); will(returnValue(trashedDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(trashedDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveFileHelper).getTargetFileForReplacedOrDeletedNode(trashedDirectory, testNodeArchiveURI, testNodeArchiveURL); will(returnValue(trashedFile));
//            oneOf(mockArchiveFileHelper).moveFileToTargetLocation(archiveFile, trashedFile); will(returnValue(Boolean.TRUE));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(archiveFile);
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        
        URL result = versioningHandler.moveFileToTrashCanFolder(testNode);
        
        assertEquals("Version URL different from expected", trashedURL, result);
    }
    
    @Test
    public void moveFileToVersioningSucceeds() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URL testNodeWsURL = new URL("file:/workspace/folder/node.cmdi");
        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/node.cmdi");
        final URL testNodeLocalArchiveURL = new URL("file:/lat/corpora/archive/somefolder/node.cmdi");
        
        final String fileBaseName = "node.cmdi";
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testNodeArchiveURI).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File versioningDirectory = new File("/lat/corpora/versioning/2013-05/10");
        final File versioningFile = new File(versioningDirectory, fileNameBuilder.toString());
        final URL versioningURL = versioningFile.toURI().toURL();
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL);
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getUriWithLocalRoot(testNodeArchiveURL.toURI());
                will(returnValue(testNodeLocalArchiveURL.toURI()));
            
//            oneOf(mockArchiveFileHelper).getArchiveLocationForNodeID(testArchiveNodeID); will(returnValue(archiveFile));
            oneOf(mockArchiveFileHelper).getDirectoryForReplacedNode(workspaceID); will(returnValue(versioningDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(versioningDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveFileHelper).getTargetFileForReplacedOrDeletedNode(versioningDirectory, testNodeArchiveURI, testNodeArchiveURL); will(returnValue(versioningFile));
//            oneOf(mockArchiveFileHelper).moveFileToTargetLocation(archiveFile, versioningFile); will(returnValue(Boolean.TRUE));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(archiveFile);
        suppress(method(FileUtils.class, "moveFile", File.class, File.class));
        
        URL result = versioningHandler.moveFileToVersioningFolder(testNode);
        
        assertEquals("Version URL different from expected", versioningURL, result);
    }
    
    @Test
    public void moveFileToTrashCanFailsTargetLocation() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URL testNodeWsURL = new URL("file:/workspace/folder/node.cmdi");
        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/node.cmdi");
        final URL testNodeLocalArchiveURL = new URL("file:/lat/corpora/archive/somefolder/node.cmdi");

        final String fileBaseName = "node.cmdi";
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testNodeArchiveURI).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File trashedDirectory = new File("/lat/corpora/trashcan/2013-05/10");
        final File trashedFile = new File(trashedDirectory, fileNameBuilder.toString());
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL);
        
        final IOException expectedException = new IOException("some exception message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getUriWithLocalRoot(testNodeArchiveURL.toURI());
                will(returnValue(testNodeLocalArchiveURL.toURI()));
            
//            oneOf(mockArchiveFileHelper).getArchiveLocationForNodeID(testArchiveNodeID); will(returnValue(archiveFile));
            oneOf(mockArchiveFileHelper).getDirectoryForDeletedNode(workspaceID); will(returnValue(trashedDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(trashedDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockArchiveFileHelper).getTargetFileForReplacedOrDeletedNode(trashedDirectory, testNodeArchiveURI, testNodeArchiveURL); will(returnValue(trashedFile));
//            oneOf(mockArchiveFileHelper).moveFileToTargetLocation(archiveFile, trashedFile); will(returnValue(Boolean.FALSE));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(archiveFile);
        stub(method(FileUtils.class, "moveFile", File.class, File.class)).toThrow(expectedException);
        
        URL result = versioningHandler.moveFileToTrashCanFolder(testNode);
        
        assertNull("Result should be null", result);
    }
    
    @Test
    public void moveFileToTrashCanFailsWriteTargetDirectory() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final URL testNodeWsURL = new URL("file:/workspace/folder/node.cmdi");
        final URI testNodeArchiveURI = new URI(UUID.randomUUID().toString());
        final URL testNodeArchiveURL = new URL("http:/remote/folder/archive/somefolder/node.cmdi");
        final URL testNodeLocalArchiveURL = new URL("file:/lat/corpora/archive/somefolder/node.cmdi");
        
        final String fileBaseName = "node.something";
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testNodeArchiveURI).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File trashedDirectory = new File("/lat/corpora/trashcan/2013-05/10");
        final File trashedFile = new File(trashedDirectory, fileNameBuilder.toString());
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testNodeWsURL, testNodeArchiveURI, testNodeArchiveURL);
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileLocationProvider).getUriWithLocalRoot(testNodeArchiveURL.toURI());
                will(returnValue(testNodeLocalArchiveURL.toURI()));
            
//            oneOf(mockArchiveFileHelper).getArchiveLocationForNodeID(testArchiveNodeID); will(returnValue(archiveFile));
            oneOf(mockArchiveFileHelper).getDirectoryForDeletedNode(workspaceID); will(returnValue(trashedDirectory));
            oneOf(mockArchiveFileHelper).canWriteTargetDirectory(trashedDirectory); will(returnValue(Boolean.FALSE));
        }});
        
        stub(method(FileUtils.class, "toFile", URL.class)).toReturn(archiveFile);
        
        URL result = versioningHandler.moveFileToTrashCanFolder(testNode);
        
        assertNull("Result should be null", result);
    }
    
    
    private WorkspaceNode getTestNode(int wsID, URL nodeWsURL, URI nodeArchiveURI, URL nodeArchiveURL) throws URISyntaxException {
        
        final int wsNodeID = 15;
        final URI nodeSchemaURI = new URI("http://some.location");
        final String nodeName = "some_name";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_DELETED;
        final String nodeFormat = "";
        
        return new LamusWorkspaceNode(wsNodeID, wsID, nodeSchemaURI,
                nodeName, "", nodeType, nodeWsURL, nodeArchiveURI, nodeArchiveURL, nodeArchiveURL, nodeStatus, nodeFormat);
    }
}