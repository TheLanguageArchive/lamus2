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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.exporting.TrashCanHandler;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusTrashCanHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Mock TrashVersioningHandler mockTrashVersioningHandler;
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    private TrashCanHandler trashCanHandler;
    
    public LamusTrashCanHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        trashCanHandler = new LamusTrashCanHandler(mockTrashVersioningHandler, mockArchiveFileHelper);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of moveFileToTrashCan method, of class LamusTrashCanHandler.
     */
    @Test
    public void moveFileToTrashCanSucceeds() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final int testArchiveNodeID = 100;
        final URL testNodeURL = new URL("http://some.url/node.something");
        
        final String fileBaseName = "node.something";
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testArchiveNodeID).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File versionDirectory = new File("/lat/corpora/version-archive/trash/2013-05/10");
        final File versionFile = new File(versionDirectory, fileNameBuilder.toString());
        final URL versionURL = versionFile.toURI().toURL();
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testArchiveNodeID, testNodeURL);
        
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).getArchiveLocationForNodeID(testArchiveNodeID); will(returnValue(archiveFile));
            oneOf(mockTrashVersioningHandler).getDirectoryForNodeVersion(workspaceID); will(returnValue(versionDirectory));
            oneOf(mockTrashVersioningHandler).canWriteTargetDirectory(versionDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockTrashVersioningHandler).getTargetFileForNodeVersion(versionDirectory, testArchiveNodeID, testNodeURL); will(returnValue(versionFile));
            oneOf(mockTrashVersioningHandler).moveFileToTargetLocation(archiveFile, versionFile); will(returnValue(Boolean.TRUE));
        }});
        
        URL result = trashCanHandler.moveFileToTrashCan(testNode);
        
        assertEquals("Version URL different from expected", versionURL, result);
    }
    
    @Test
    public void moveFileToTrashCanFailsTargetLocation() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final int testArchiveNodeID = 100;
        final URL testNodeURL = new URL("http://some.url/node.something");

        final String fileBaseName = "node.something";
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testArchiveNodeID).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File versionDirectory = new File("/lat/corpora/version-archive/trash/2013-05/10");
        final File versionFile = new File(versionDirectory, fileNameBuilder.toString());
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testArchiveNodeID, testNodeURL);
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).getArchiveLocationForNodeID(testArchiveNodeID); will(returnValue(archiveFile));
            oneOf(mockTrashVersioningHandler).getDirectoryForNodeVersion(workspaceID); will(returnValue(versionDirectory));
            oneOf(mockTrashVersioningHandler).canWriteTargetDirectory(versionDirectory); will(returnValue(Boolean.TRUE));
            oneOf(mockTrashVersioningHandler).getTargetFileForNodeVersion(versionDirectory, testArchiveNodeID, testNodeURL); will(returnValue(versionFile));
            oneOf(mockTrashVersioningHandler).moveFileToTargetLocation(archiveFile, versionFile); will(returnValue(Boolean.FALSE));
        }});
        
        URL result = trashCanHandler.moveFileToTrashCan(testNode);
        
        assertNull("Result should be null", result);
    }
    
    @Test
    public void moveFileToTrashCanFailsWriteTargetDirectory() throws MalformedURLException, URISyntaxException {
        
        final int workspaceID = 10;
        final int testArchiveNodeID = 100;
        final URL testNodeURL = new URL("http://some.url/node.something");
        
        final String fileBaseName = "node.something";
        final StringBuilder fileNameBuilder = new StringBuilder().append("v").append(testArchiveNodeID).append("__.").append(fileBaseName);
        final File archiveDirectory = new File("/lat/corpora/archive/somefolder");
        final File archiveFile = new File(archiveDirectory, fileBaseName);
        final File versionDirectory = new File("/lat/corpora/version-archive/trash/2013-05/10");
        final File versionFile = new File(versionDirectory, fileNameBuilder.toString());
        
        final WorkspaceNode testNode = getTestNode(workspaceID, testArchiveNodeID, testNodeURL);
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).getArchiveLocationForNodeID(testArchiveNodeID); will(returnValue(archiveFile));
            oneOf(mockTrashVersioningHandler).getDirectoryForNodeVersion(workspaceID); will(returnValue(versionDirectory));
            oneOf(mockTrashVersioningHandler).canWriteTargetDirectory(versionDirectory); will(returnValue(Boolean.FALSE));
        }});
        
        URL result = trashCanHandler.moveFileToTrashCan(testNode);
        
        assertNull("Result should be null", result);
    }
    
    
    private WorkspaceNode getTestNode(int wsID, int archiveNodeID, URL nodeURL) throws URISyntaxException {
        
        final int wsNodeID = 15;
        final URI nodeSchemaURI = new URI("http://some.location");
        final String nodeName = "some_name";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final WorkspaceNodeStatus nodeStatus = WorkspaceNodeStatus.NODE_DELETED;
        final String nodePid = "somepid";
        final String nodeFormat = "";
        
        return new LamusWorkspaceNode(wsNodeID, wsID, archiveNodeID, nodeSchemaURI,
                nodeName, "", nodeType, nodeURL, nodeURL, nodeURL, nodeStatus, nodePid, nodeFormat);
    }
}