/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.lamus.filesystem.implementation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.exception.NodeAccessException;
import nl.mpi.lamus.filesystem.LamusFilesystemTestProperties;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.filesystem.implementation.LamusWorkspaceFileHandlerTest.LamusWorkspaceFileHandlerTestBeans;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusFilesystemTestProperties.class, LamusWorkspaceFileHandlerTestBeans.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
@PrepareForTest({FileUtils.class, Files.class})
public class LamusWorkspaceFileHandlerTest {
    
    @Configuration
    @ComponentScan(basePackages = {"nl.mpi.lamus.filesystem"})
    @Profile("testing")
    static class LamusWorkspaceFileHandlerTestBeans {
        
        @Bean
        public ArchiveFileLocationProvider archiveFileLocationProvider() {
            return mockArchiveFileLocationProvider;
        }
        
        @Bean
        public WorkspaceAccessChecker workspaceAccessChecker() {
            return mockWorkspaceAccessChecker;
        }
    }
    
    private static Logger logger = LoggerFactory.getLogger(LamusWorkspaceFileHandler.class);
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Mock static ArchiveFileLocationProvider mockArchiveFileLocationProvider;
    @Mock static WorkspaceAccessChecker mockWorkspaceAccessChecker;
    
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Autowired
    private WorkspaceFileHandler workspaceFileHandler;
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    private File tempDirectory;
    private File anotherTempDirectory;
    
    @Mock private File mockArchiveFile;
    @Mock private WorkspaceNode mockWorkspaceNode;
    @Mock private Workspace mockWorkspace;
    @Mock private File mockOrphansDirectory;
    @Mock private File mockOrphan1;
    @Mock private File mockOrphan2;
    @Mock private File mockInnerDirectory;
    
    public LamusWorkspaceFileHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws IOException {
        FileUtils.cleanDirectory(workspaceBaseDirectory);
        
        ReflectionTestUtils.setField(workspaceFileHandler, "archiveFileLocationProvider", mockArchiveFileLocationProvider);
        ReflectionTestUtils.setField(workspaceFileHandler, "workspaceAccessChecker", mockWorkspaceAccessChecker);
    }
    
    @After
    public void tearDown() throws IOException {
        FileUtils.cleanDirectory(workspaceBaseDirectory);
        if(tempDirectory != null) {
            tempDirectory.setWritable(true);
            FileUtils.cleanDirectory(tempDirectory);
        }
        if(anotherTempDirectory != null) {
            anotherTempDirectory.setWritable(true);
            FileUtils.cleanDirectory(anotherTempDirectory);
        }
    }

    
    @Test
    public void copyFileSuccessfully() throws MalformedURLException, IOException, URISyntaxException {
        
        File originFile = createFileToCopy();
        File destinationFile = createTargetDirectory_retrieveTargetFileLocation();
        
        workspaceFileHandler.copyFile(originFile, destinationFile);
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
    }
    
    @Test
    public void copyFile_originFileDoesntExist() throws IOException {
        
        File originFile = doNotCreateFileToCopy();
        File destinationFile = createTargetDirectory_retrieveTargetFileLocation();
        
        try {
            workspaceFileHandler.copyFile(originFile, destinationFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            //expected exception is thrown
        }
        
        assertFalse("File shouldn't exist in the origin location", originFile.exists());
        assertFalse("File shouldn't exist in the destination location", destinationFile.exists());
    }
    
    @Test
    public void copyFile_targetDirectoryDoesntExist() throws IOException {
        
        File originFile = createFileToCopy();
        File destinationFile = doNotCreateTargetDirectory_retrieveTargetFileLocation();
        
        try {
            workspaceFileHandler.copyFile(originFile, destinationFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            //expected exception is thrown
        }
        
        assertFalse("File shouldn't exist in the destination location", destinationFile.exists());
    }
    
    @Test
    public void copyFileThrowsException() throws MalformedURLException, IOException, URISyntaxException {
        
        File originFile = createFileToCopy();
        File destinationFile = createTargetDirectory_retrieveTargetFileLocation();
        anotherTempDirectory.setReadOnly();
        
        try {
            workspaceFileHandler.copyFile(originFile, destinationFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            //expected exception is thrown
        }
        
        assertFalse("File shouldn't exist in the destination location", destinationFile.exists());
    }
    
    @Test
    public void moveFileSuccessfully() throws MalformedURLException, IOException, URISyntaxException {
        
        File originFile = createFileToCopy();
        File destinationFile = createTargetDirectory_retrieveTargetFileLocation();
        
        workspaceFileHandler.moveFile(originFile, destinationFile);
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
        assertFalse("File shouldn't exist anymore in its original location", originFile.exists());
    }
    
    @Test
    public void moveFileSuccessfully_FileAlreadyExists() throws MalformedURLException, IOException, URISyntaxException {
        
        File originFile = createFileToCopy();
        File destinationFile = createTargetDirectory_retrieveTargetFileLocation();
        destinationFile.createNewFile();
        
        workspaceFileHandler.moveFile(originFile, destinationFile);
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
        assertFalse("File shouldn't exist anymore in its original location", originFile.exists());
    }
    
    @Test
    public void moveFile_originFileDoesntExist() throws IOException {
        
        File originFile = doNotCreateFileToCopy();
        File destinationFile = createTargetDirectory_retrieveTargetFileLocation();
        
        try {
            workspaceFileHandler.moveFile(originFile, destinationFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            //expected exception is thrown
        }
        
        assertFalse("File shouldn't exist in the origin location", originFile.exists());
        assertFalse("File shouldn't exist in the destination location", destinationFile.exists());
    }
    
    @Test
    public void moveFile_targetDirectoryDoesntExist() throws IOException {
        
        File originFile = createFileToCopy();
        File destinationFile = doNotCreateTargetDirectory_retrieveTargetFileLocation();
        
        try {
            workspaceFileHandler.moveFile(originFile, destinationFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            //expected exception is thrown
        }
        
        assertFalse("File shouldn't exist in the destination location", destinationFile.exists());
    }
    
    @Test
    public void moveFileThrowsException() throws MalformedURLException, IOException, URISyntaxException {
        
        File originFile = createFileToCopy();
        File destinationFile = createTargetDirectory_retrieveTargetFileLocation();
        anotherTempDirectory.setReadOnly();
        
        try {
            workspaceFileHandler.moveFile(originFile, destinationFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            //expected exception is thrown
        }
        
        assertFalse("File shouldn't exist in its expected final location", destinationFile.exists());
        assertTrue("File should still exist in its original location", originFile.exists());
    }
    
    @Test
    public void deleteFileSuccessfully() throws MalformedURLException, IOException, URISyntaxException {
        
        File originFile = createFileToCopy();
        
        workspaceFileHandler.deleteFile(originFile);
        
        assertFalse("File shouldn't exist anymore in its original location", originFile.exists());
    }
    
    @Test
    public void deleteFile_doesntExist() throws IOException {
        
        File originFile = doNotCreateFileToCopy();
        
        workspaceFileHandler.deleteFile(originFile);
        //shouldn't cause any error if the file doesn't exist
        
        assertFalse("File shouldn't exist in the origin location", originFile.exists());
    }
    
    @Test
    public void deleteFileThrowsException() throws MalformedURLException, IOException, URISyntaxException {
        
        File originFile = createFileToCopy();
        tempDirectory.setReadOnly();
        
        try {
            workspaceFileHandler.deleteFile(originFile);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            //expected exception is thrown
        }
        
        assertTrue("File should still exist in its original location", originFile.exists());
    }
    
    @Test
    public void getStreamResultForWorkspaceNodeFileSuccessfully() throws MalformedURLException, URISyntaxException, IOException {
        
        String nodeFilename = "someNode.cmdi";
        URL archiveNodeURL = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        Workspace testWorkspace = createTestWorkspace();
        File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        File testNodeFile = new File(workspaceDirectory, archiveNodeURL.getFile());
        
        StreamResult retrievedStreamResult = 
                workspaceFileHandler.getStreamResultForNodeFile(testNodeFile);
        
        assertNotNull("Resulting StreamResult is null", retrievedStreamResult);
    }
    
    @Test
    public void getFileForWorkspaceNodeSuccessfully() throws IOException, URISyntaxException {
        
        final int workspaceID = 1;
        final String nodeFilename = "someNode.cmdi";
        final String archiveNodePath = "file:/somewhere/in/the/archive/" + nodeFilename;
        
        File expectedWorkspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        File expectedNodeFile = new File(expectedWorkspaceDirectory, nodeFilename);
        
        context.checking(new Expectations() {{
            
            oneOf(mockWorkspaceNode).getWorkspaceID(); will(returnValue(workspaceID));
            oneOf(mockArchiveFile).getPath(); will(returnValue(archiveNodePath));
        }});
        
        File retrievedFile = workspaceFileHandler.getFileForImportedWorkspaceNode(mockArchiveFile, mockWorkspaceNode);
        
        assertEquals(expectedNodeFile, retrievedFile);
    }

    @Test
    public void copyInputStreamToTargetFile() throws MalformedURLException, IOException, URISyntaxException {
        
        String nodeFilename = "someNode.cmdi";
        Workspace testWorkspace = createTestWorkspace();
        final File baseDirectory = createTestBaseDirectory();
        File workspaceDirectory = createTestWorkspaceDirectory(baseDirectory, testWorkspace.getWorkspaceID());
        WorkspaceNode testNode = createTestResourceWorkspaceNode(testWorkspace.getWorkspaceID(), workspaceDirectory, nodeFilename);
        
        File originFile = new File(testNode.getWorkspaceURL().getPath());
        InputStream originInputStream = new FileInputStream(originFile);
        File destinationFile = new File(workspaceDirectory, "someRandomLocation.txt");
        
        workspaceFileHandler.copyInputStreamToTargetFile(originInputStream, destinationFile);
        
        assertTrue("File doesn't exist in its expected final location", destinationFile.exists());
    }
    
    @Test
    public void getFilesInOrphanDirectory_NoneLocked() throws MalformedURLException, URISyntaxException, NodeAccessException {
        
        final String nodeFilename = "someNode.cmdi";
        final URL archiveNodeUrl = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        final URI archiveNodeUrlUri = archiveNodeUrl.toURI();
        
        final URI orphan1_Uri = URI.create("file:/somewhere/in/the/archive/sessions/orphan1.cmdi");
        final URI orphan2_Uri = URI.create("file:/somewhere/in/the/archive/sessions/orphan2.cmdi");
        
        final Collection<File> fileCollection = new ArrayList<>();
        fileCollection.add(mockOrphan1);
        fileCollection.add(mockOrphan2);
        
        final Collection<File> expectedFiles = new ArrayList<>();
        expectedFiles.add(mockOrphan1);
        expectedFiles.add(mockOrphan2);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(archiveNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(archiveNodeUrlUri); will(returnValue(mockOrphansDirectory));
            
            //first iteration
            oneOf(mockOrphan1).isFile(); will(returnValue(Boolean.TRUE));
            oneOf(mockOrphan1).toURI(); will(returnValue(orphan1_Uri));
            oneOf(mockWorkspaceAccessChecker).ensureNodeIsNotLocked(orphan1_Uri);
            
            //second iteration
            oneOf(mockOrphan2).isFile(); will(returnValue(Boolean.TRUE));
            oneOf(mockOrphan2).toURI(); will(returnValue(orphan2_Uri));
            oneOf(mockWorkspaceAccessChecker).ensureNodeIsNotLocked(orphan2_Uri);
        }});
        
        stub(method(FileUtils.class, "listFiles", File.class, String[].class, boolean.class)).toReturn(fileCollection);
        
        Collection<File> retrivedFiles = workspaceFileHandler.getFilesInOrphanDirectory(mockWorkspace);
        
        assertEquals("Retrieved collection different from expected", expectedFiles, retrivedFiles);
    }
    
    @Test
    public void getFilesInOrphanDirectory_NoDirectory() throws MalformedURLException, URISyntaxException, NodeAccessException {
        
        final String nodeFilename = "someNode.cmdi";
        final URL archiveNodeUrl = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        final URI archiveNodeUrlUri = archiveNodeUrl.toURI();
        
        final Collection<File> expectedFiles = new ArrayList<>();
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(archiveNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(archiveNodeUrlUri); will(returnValue(mockOrphansDirectory));
        }});
        
        stub(method(FileUtils.class, "listFiles", File.class, String[].class, boolean.class)).toReturn(null);
        
        Collection<File> retrivedFiles = workspaceFileHandler.getFilesInOrphanDirectory(mockWorkspace);
        
        assertEquals("Retrieved collection different from expected", expectedFiles, retrivedFiles);
    }
    
    @Test
    public void getFilesInOrphanDirectory_NoFiles() throws MalformedURLException, URISyntaxException, NodeAccessException {
        
        final String nodeFilename = "someNode.cmdi";
        final URL archiveNodeUrl = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        final URI archiveNodeUrlUri = archiveNodeUrl.toURI();
        
        final Collection<File> fileCollection = new ArrayList<>();
        
        final Collection<File> expectedFiles = new ArrayList<>();
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(archiveNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(archiveNodeUrlUri); will(returnValue(mockOrphansDirectory));
        }});
        
        stub(method(FileUtils.class, "listFiles", File.class, String[].class, boolean.class)).toReturn(fileCollection);
        
        Collection<File> retrivedFiles = workspaceFileHandler.getFilesInOrphanDirectory(mockWorkspace);
        
        assertEquals("Retrieved collection different from expected", expectedFiles, retrivedFiles);
    }
    
    @Test
    public void getFilesInOrphanDirectory_InnerDirectory() throws MalformedURLException, URISyntaxException, NodeAccessException {
        
        final String nodeFilename = "someNode.cmdi";
        final URL archiveNodeUrl = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        final URI archiveNodeUrlUri = archiveNodeUrl.toURI();
        
        //file is directory, so it shouldn't be added to the resulting list
        
        final Collection<File> fileCollection = new ArrayList<>();
        fileCollection.add(mockInnerDirectory);
        
        final Collection<File> expectedFiles = new ArrayList<>();
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(archiveNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(archiveNodeUrlUri); will(returnValue(mockOrphansDirectory));
            
            //first iteration
            oneOf(mockInnerDirectory).isFile(); will(returnValue(Boolean.FALSE)); //it's a directory, so it doesn't go further
        }});
        
        stub(method(FileUtils.class, "listFiles", File.class, String[].class, boolean.class)).toReturn(fileCollection);
        
        Collection<File> retrivedFiles = workspaceFileHandler.getFilesInOrphanDirectory(mockWorkspace);
        
        assertEquals("Retrieved collection different from expected", expectedFiles, retrivedFiles);
    }
    
    @Test
    public void getFilesInOrphanDirectory_OneLocked() throws MalformedURLException, URISyntaxException, NodeAccessException {
        
        final String nodeFilename = "someNode.cmdi";
        final URL archiveNodeUrl = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        final URI archiveNodeUrlUri = archiveNodeUrl.toURI();
        
        final String orphan1Filename = "orphan1.cmdi";
        final URI orphan1_Uri = URI.create("file:/somewhere/in/the/archive/sessions/" + orphan1Filename);
        final URI orphan2_Uri = URI.create("file:/somewhere/in/the/archive/sessions/orphan2.cmdi");
        
        final Collection<File> fileCollection = new ArrayList<>();
        fileCollection.add(mockOrphan1);
        fileCollection.add(mockOrphan2);
        
        final Collection<File> expectedFiles = new ArrayList<>();
        expectedFiles.add(mockOrphan2);
        
        final NodeAccessException exceptionToThrow = new NodeAccessException(orphan1Filename, orphan1_Uri, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(archiveNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(archiveNodeUrlUri); will(returnValue(mockOrphansDirectory));
            
            //first iteration
            oneOf(mockOrphan1).isFile(); will(returnValue(Boolean.TRUE));
            oneOf(mockOrphan1).toURI(); will(returnValue(orphan1_Uri));
            oneOf(mockWorkspaceAccessChecker).ensureNodeIsNotLocked(orphan1_Uri); will(throwException(exceptionToThrow));
            
            //second iteration
            oneOf(mockOrphan2).isFile(); will(returnValue(Boolean.TRUE));
            oneOf(mockOrphan2).toURI(); will(returnValue(orphan2_Uri));
            oneOf(mockWorkspaceAccessChecker).ensureNodeIsNotLocked(orphan2_Uri);
        }});
        
        stub(method(FileUtils.class, "listFiles", File.class, String[].class, boolean.class)).toReturn(fileCollection);
        
        Collection<File> retrivedFiles = workspaceFileHandler.getFilesInOrphanDirectory(mockWorkspace);
        
        assertEquals("Retrieved collection different from expected", expectedFiles, retrivedFiles);
    }
    
    @Test
    public void getFilesInOrphanDirectory_BothLocked() throws MalformedURLException, URISyntaxException, NodeAccessException {
        
        final String nodeFilename = "someNode.cmdi";
        final URL archiveNodeUrl = new URL("file:/somewhere/in/the/archive/" + nodeFilename);
        final URI archiveNodeUrlUri = archiveNodeUrl.toURI();
        
        final String orphan1Filename = "orphan1.cmdi";
        final URI orphan1_Uri = URI.create("file:/somewhere/in/the/archive/sessions/" + orphan1Filename);
        final String orphan2Filename = "orphan2.cmdi";
        final URI orphan2_Uri = URI.create("file:/somewhere/in/the/archive/sessions/" + orphan2Filename);
        
        final Collection<File> fileCollection = new ArrayList<>();
        fileCollection.add(mockOrphan1);
        fileCollection.add(mockOrphan2);
        
        final NodeAccessException exceptionToThrow1 = new NodeAccessException(orphan1Filename, orphan1_Uri, null);
        final NodeAccessException exceptionToThrow2 = new NodeAccessException(orphan2Filename, orphan2_Uri, null);
        
        context.checking(new Expectations() {{
            oneOf(mockWorkspace).getTopNodeArchiveURL(); will(returnValue(archiveNodeUrl));
            oneOf(mockArchiveFileLocationProvider).getOrphansDirectory(archiveNodeUrlUri); will(returnValue(mockOrphansDirectory));
            
            //first iteration
            oneOf(mockOrphan1).isFile(); will(returnValue(Boolean.TRUE));
            oneOf(mockOrphan1).toURI(); will(returnValue(orphan1_Uri));
            oneOf(mockWorkspaceAccessChecker).ensureNodeIsNotLocked(orphan1_Uri); will(throwException(exceptionToThrow1));
            
            //second iteration
            oneOf(mockOrphan2).isFile(); will(returnValue(Boolean.TRUE));
            oneOf(mockOrphan2).toURI(); will(returnValue(orphan2_Uri));
            oneOf(mockWorkspaceAccessChecker).ensureNodeIsNotLocked(orphan2_Uri); will(throwException(exceptionToThrow2));
        }});
        
        stub(method(FileUtils.class, "listFiles", File.class, String[].class, boolean.class)).toReturn(fileCollection);
        
        Collection<File> retrivedFiles = workspaceFileHandler.getFilesInOrphanDirectory(mockWorkspace);
        
        assertTrue("Retrieved collection should be empty", retrivedFiles.isEmpty());
    }
    
    
    private Workspace createTestWorkspace() {
        Workspace workspace = new LamusWorkspace("someUser", 0L, 10000000L);
        workspace.setWorkspaceID(1);
        
        return workspace;
    }
    
    private File createTestBaseDirectory() throws IOException {
        File baseDirectory = testFolder.newFolder("workspace_base_directory");
        return baseDirectory;
    }
    
    private File createTestWorkspaceDirectory(File baseDirectory, int workspaceID) {
        File workspaceDirectory = new File(baseDirectory, "" + workspaceID);
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Workspace directory wasn't created.", workspaceDirectory.exists());
        
        return workspaceDirectory;
    }
    
    private File createTestUploadDirectory(File workspaceDirectory) {
        File uploadDirectory = new File(workspaceDirectory, "upload");
        boolean isDirectoryCreated = uploadDirectory.mkdirs();
        
        assertTrue("Upload directory was not successfully created.", isDirectoryCreated);
        assertTrue("Workspace directory wasn't created.", uploadDirectory.exists());
        
        return uploadDirectory;
    }
        
    private WorkspaceNode createTestMetadataWorkspaceNode(int workspaceID, String filename) throws URISyntaxException, MalformedURLException {
        URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        URL archiveNodeURL = new URL("file:/workspace/folder/node.cmdi");
        WorkspaceNode node = new LamusWorkspaceNode(workspaceID, archiveNodeURI, archiveNodeURL);
        node.setName(filename);
        node.setType(WorkspaceNodeType.METADATA);
        node.setFormat("someFormat");
        node.setStatus(WorkspaceNodeStatus.CREATED);

        return node;
    }
    
    private WorkspaceNode createTestResourceWorkspaceNode(int workspaceID, File workspaceDirectory, String filename) throws IOException, URISyntaxException {
        URI archiveNodeURI = new URI(UUID.randomUUID().toString());
        URL archiveNodeURL = new URL("file:/workspace/folder/node.cmdi");
        WorkspaceNode node = new LamusWorkspaceNode(
                workspaceID, archiveNodeURI, archiveNodeURL);
        
        File workspaceNodeFile = new File(workspaceDirectory, filename);
        workspaceNodeFile.createNewFile();
        node.setWorkspaceURL(workspaceNodeFile.toURI().toURL());
        node.setName(filename);
        node.setType(WorkspaceNodeType.RESOURCE_OTHER);
        node.setFormat("someFormat");
        node.setStatus(WorkspaceNodeStatus.CREATED);

        return node;
    }
    
    private void prepareTempDirectory() {
        tempDirectory = testFolder.newFolder("temp_directory");
        assertTrue("Temp directory wasn't created.", tempDirectory.exists());
    }
    
    private void prepareAnotherTempDirectory() {
        anotherTempDirectory = testFolder.newFolder("another_temp_directory");
        assertTrue("Another temp directory wasn't created.", anotherTempDirectory.exists());
    }
    
    private File createFileWithNameInTempDirectory(String filename) throws IOException {
        prepareTempDirectory();
        
        File tempFile = new File(tempDirectory, filename);
        boolean isFileCreated = tempFile.createNewFile();
        
        assertTrue("Temp file was not successfuly created.", isFileCreated);
        assertTrue("Temp file wasn't created.", tempFile.exists());
        
        return tempFile;
    }
    
    private File createFileToCopy() throws IOException {
        prepareTempDirectory();
        
        File tempFile = new File(tempDirectory, "temp_file.txt");
        boolean isFileCreated = tempFile.createNewFile();
        
        assertTrue("Temp file was not successfuly created.", isFileCreated);
        assertTrue("Temp file wasn't created.", tempFile.exists());
        
        return tempFile;
    }
    
    private File doNotCreateFileToCopy() throws IOException {
        prepareTempDirectory();
        
        File tempFile = new File(tempDirectory, "temp_file.txt");
        
        assertFalse("Temp file should not have been created.", tempFile.exists());
        
        return tempFile;
    }
    
    private File createTargetDirectory_retrieveTargetFileLocation() {
        prepareAnotherTempDirectory();
        
        File targetLocation = new File(anotherTempDirectory, "target_temp_file.txt");
        
        return targetLocation;
    }
    
    private File doNotCreateTargetDirectory_retrieveTargetFileLocation() {
        prepareAnotherTempDirectory();
        
        File targetDirectory = new File(anotherTempDirectory, "nonexistingfolder");
        
        assertFalse("Target directory should not have been created.", targetDirectory.exists());
        
        File targetLocation = new File(targetDirectory, "target_temp_file.txt");
        
        return targetLocation;
    }
}
