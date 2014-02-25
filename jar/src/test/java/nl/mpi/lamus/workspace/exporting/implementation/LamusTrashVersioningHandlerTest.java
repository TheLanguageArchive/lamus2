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
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.exporting.TrashVersioningHandler;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusTrashVersioningHandlerTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    private TrashVersioningHandler versioningHandler;
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    
    private File trashCanBaseDirectory = new File("/lat/corpora/version_archive/");
    
    public LamusTrashVersioningHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        versioningHandler = new LamusTrashVersioningHandler(mockArchiveFileHelper);
        
        ReflectionTestUtils.setField(versioningHandler, "trashCanBaseDirectory", trashCanBaseDirectory);
    }
    
    @After
    public void tearDown() {
    }


//    @Test
//    public void retireVersionSucceeds() throws MalformedURLException, URISyntaxException {
//        
//        final int testArchiveNodeID = 100;
//        final WorkspaceNode testNode = getTestNode(testArchiveNodeID);
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockVersioningAPI).setVersionStatus(NodeIdUtils.TONODEID(testArchiveNodeID), true); will(returnValue(true));
//        }});
//        
//        
//        boolean result = versioningHandler.retireNodeVersion(testNode);
//        
//        assertTrue("Result should be true", result);
//    }
//    
//    @Test
//    public void retireVersionFails() throws MalformedURLException, URISyntaxException {
//        
//        final int testArchiveNodeID = 100;
//        final WorkspaceNode testNode = getTestNode(testArchiveNodeID);
//        
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockVersioningAPI).setVersionStatus(NodeIdUtils.TONODEID(testArchiveNodeID), true); will(returnValue(false));
//        }});
//        
//        
//        boolean result = versioningHandler.retireNodeVersion(testNode);
//        
//        assertFalse("Result should be false", result);
//    }
//    
//    @Test
//    public void retireVersionInvalidNodeID() throws MalformedURLException, URISyntaxException {
//        
//        final int testArchiveNodeID = -1;
//        final WorkspaceNode testNode = getTestNode(testArchiveNodeID);
//        
//        context.checking(new Expectations() {{
//            
//            never(mockVersioningAPI).setVersionStatus(NodeIdUtils.TONODEID(testArchiveNodeID), true);
//        }});
//        
//        
//        boolean result = versioningHandler.retireNodeVersion(testNode);
//        
//        assertFalse("Result should be false", result);
//    }

    @Test
    public void getDirectoryForNodeVersion() {
        
        int workspaceID = 10;
        
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        
        StringBuilder directoryName = new StringBuilder();
        directoryName.append(year);
        directoryName.append("-");
        if(month < 10) {
            directoryName.append("0");
        }
        directoryName.append(month);
        File subDirectory = new File(trashCanBaseDirectory, directoryName.toString());
        File expectedDirectory = new File(subDirectory, "" + workspaceID);

        
        File result = versioningHandler.getDirectoryForNodeVersion(workspaceID);
        
        assertEquals("Target version sub-directory is different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getTargetFileForNodeVersion() throws MalformedURLException, URISyntaxException {
        
        final URI testArchiveNodeURI = new URI(UUID.randomUUID().toString());
        final URL testNodeArchiveURL = new URL("file:/lat/corpora/archive/node.cmdi");
        final File testNodeFile = new File(testNodeArchiveURL.getPath());
        final String fileBaseName = "node.cmdi";
        
        final String versionDirectoryName = "2013-05";
        final File versionFullDirectory = new File(trashCanBaseDirectory, versionDirectoryName);
        
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append("v").append(testArchiveNodeURI).append("__.").append(fileBaseName);
        File expectedTargetFile = new File(versionFullDirectory, fileNameBuilder.toString());
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).getFileBasename(testNodeFile.getPath()); will(returnValue(fileBaseName));
        }});
        
        File result = versioningHandler.getTargetFileForNodeVersion(versionFullDirectory, testArchiveNodeURI, testNodeArchiveURL);
        
        assertEquals("Returned file name different from expected", expectedTargetFile, result);
    }
    
    @Test
    public void canWriteExistingTargetDirectory() throws IOException {
        
        File targetDirectory = testFolder.newFolder("/lat/corpora/versions/trash/2013-05/1644");
        targetDirectory.mkdirs();
        
        boolean result = versioningHandler.canWriteTargetDirectory(targetDirectory);
        
        assertTrue("Target directory should be writable", result);
    }
    
    @Test
    public void canWriteNonExistingTargetDirectory() throws IOException {
        
        File targetDirectory = testFolder.newFolder("/lat/corpora/versions/trash/2013-05/1644");
        
        boolean result = versioningHandler.canWriteTargetDirectory(targetDirectory);
        
        assertTrue("Target directory should have been created and be writable", result);
    }
    
    @Test
    public void cannotWriteTargetDirectory() throws IOException {
        
        File targetDirectory = testFolder.newFolder("/lat/corpora/versions/trash/2013-05/1644");
        targetDirectory.mkdirs();
        targetDirectory.setReadOnly();
        
        boolean result = versioningHandler.canWriteTargetDirectory(targetDirectory);
        
        assertFalse("Target directory should not be writable", result);
    }
    
    @Test
    public void targetDirectoryIsNotDirectory() throws IOException {
        
        File someFile = testFolder.newFile("someFile");
        someFile.createNewFile();
        
        boolean result = versioningHandler.canWriteTargetDirectory(someFile);
        
        assertFalse("Target directory is not a directory, therefore it should fail", result);
    }
    
    @Test
    public void moveFileToTargetLocationSucceeds() throws IOException {
        
        File currentFolder = testFolder.newFolder("/lat/corpora/archive/somefolder");
        currentFolder.mkdirs();
        File currentFile = new File(currentFolder, "file");
        currentFile.createNewFile();
        File targetFolder = testFolder.newFolder("/lat/corpora/versions/trash/2013-05/1644");
        targetFolder.mkdirs();
        File targetFile = new File(targetFolder, "v100__.file");
        
        boolean result = versioningHandler.moveFileToTargetLocation(currentFile, targetFile);
        
        assertTrue("File moving result should be true", result);
        assertFalse("File shouldn't exist in its old location", currentFile.exists());
        assertTrue("File should exist in its target location", targetFile.exists());
    }
    
    @Test
    public void moveFileToTargetLocationFailsReadOnly() throws IOException {
        
        File currentFolder = testFolder.newFolder("/lat/corpora/archive/somefolder");
        currentFolder.mkdirs();
        File currentFile = new File(currentFolder, "file");
        currentFile.createNewFile();
        File targetFolder = testFolder.newFolder("/lat/corpora/versions/trash/2013-05/1644");
        targetFolder.mkdirs();
        targetFolder.setReadOnly();
        File targetFile = new File(targetFolder, "v100__.file");
        
        boolean result = versioningHandler.moveFileToTargetLocation(currentFile, targetFile);
        
        assertFalse("File moving result should be false", result);
        assertTrue("File shouldn't exist in its old location", currentFile.exists());
        assertFalse("File should exist in its target location", targetFile.exists());
    }
    
    @Test
    public void moveFileToTargetLocationFailsFileAlreadyExists() throws IOException {
        
        File currentFolder = testFolder.newFolder("/lat/corpora/archive/somefolder");
        currentFolder.mkdirs();
        File currentFile = new File(currentFolder, "file");
        currentFile.createNewFile();
        File targetFolder = testFolder.newFolder("/lat/corpora/versions/trash/2013-05/1644");
        targetFolder.mkdirs();
        targetFolder.setReadOnly();
        File targetFile = new File(targetFolder, "v100__.file");
        
        boolean result = versioningHandler.moveFileToTargetLocation(currentFile, targetFile);
        
        assertFalse("File moving result should be false", result);
        assertTrue("File shouldn't exist in its old location", currentFile.exists());
        assertFalse("File should exist in its target location", targetFile.exists());
    }
    
    
    private WorkspaceNode getTestNode() throws MalformedURLException, URISyntaxException {
        
        final URI testArchiveNodeURI = new URI(UUID.randomUUID().toString());
        return getTestNode(testArchiveNodeURI);
    }
    
    private WorkspaceNode getTestNode(URI archiveNodeURI) throws MalformedURLException, URISyntaxException {
        
        final URL testNodeArchiveURL = new URL("file:/lat/corpora/archive/node.cmdi");
        return getTestNode(archiveNodeURI, testNodeArchiveURL);
    }
    
    private WorkspaceNode getTestNode(URI archiveNodeURI, URL archiveNodeURL) throws MalformedURLException, URISyntaxException {
        
        final int testWorkspaceID = 1;
        
        final int testWorkspaceNodeID = 10;
        final URL testNodeWsURL = new URL("file:/workspace/folder/node.cmdi");
        final String testDisplayValue = "someName";
        final WorkspaceNodeType testNodeType = WorkspaceNodeType.METADATA; //TODO change this
        final String testNodeFormat = "";
        final URI testSchemaLocation = new URI("http://some.location");
        final WorkspaceNode nodeToReturn = new LamusWorkspaceNode(testWorkspaceNodeID, testWorkspaceID, testSchemaLocation,
                testDisplayValue, "", testNodeType, testNodeWsURL, archiveNodeURI, archiveNodeURL, archiveNodeURL, WorkspaceNodeStatus.NODE_ISCOPY, testNodeFormat);
        
        return nodeToReturn;
    }
    
}