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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
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
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author guisil
 */
public class LamusArchiveFileLocationProviderTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    
    private final String dbHttpsRoot = "https://some.server/archive/";
    private final String dbHttpRoot = "http://some.server/archive/";
    private final String dbLocalRoot = "file:/some/loca/folder/archive/";
    
    private final String corpusstructureDirectoryName = "Corpusstructure";
    private final String orphansDirectoryName = "sessions";
    
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock File mockFile;
    
    @Mock WorkspaceNode mockNode;
    
    private File tempDirectory;
    
    
    public LamusArchiveFileLocationProviderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        archiveFileLocationProvider = new LamusArchiveFileLocationProvider(mockArchiveFileHelper);
        ReflectionTestUtils.setField(archiveFileLocationProvider, "dbHttpsRoot", dbHttpsRoot);
        ReflectionTestUtils.setField(archiveFileLocationProvider, "dbHttpRoot", dbHttpRoot);
        ReflectionTestUtils.setField(archiveFileLocationProvider, "dbLocalRoot", dbLocalRoot);
        
        ReflectionTestUtils.setField(archiveFileLocationProvider, "corpusstructureDirectoryName", corpusstructureDirectoryName);
        ReflectionTestUtils.setField(archiveFileLocationProvider, "orphansDirectoryName", orphansDirectoryName);
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void getAvailableFile() throws IOException {
        
        final String parentPath = "/archive/root/TopNode/Corpusstructure/normalnode.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/NormalNode";
        final String parentDirname = FilenameUtils.getFullPath(parentPath);
        final String filenameAttempt = "resource.pdf";
        final String baseDirectoryForFileType = "/archive/root/TopNode/NormalNode/Annotations";
        final File baseDirectoryForFileTypeFile = new File(baseDirectoryForFileType);
        final String filePathAttempt = parentDirname + File.separator + filenameAttempt;
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).correctPathElement(FilenameUtils.getName(filenameAttempt), "getAvailableFile");
                will(returnValue(filenameAttempt));
            oneOf(mockArchiveFileHelper).getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode); will(returnValue(baseDirectoryForFileType));
            oneOf(mockArchiveFileHelper).getFinalFile(baseDirectoryForFileTypeFile, filenameAttempt); will(returnValue(mockFile));
            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockFile);
        }});
        
        File retrievedFile = archiveFileLocationProvider.getAvailableFile(parentPath, parentCorpusNamePathToClosestTopNode, mockNode, filenameAttempt);
        
        assertEquals("Retrieved file different from expected", mockFile, retrievedFile);
    }
    
    @Test
    public void getAvailableFile_ThrowsException() throws IOException {
        
        final String parentPath = "/archive/root/TopNode/Corpusstructure/normalnode.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/NormalNode";
        final String parentDirname = FilenameUtils.getFullPath(parentPath);
        final String filenameAttempt = "resource.pdf";
        final String baseDirectoryForFileType = "/archive/root/TopNode/NormalNode/Annotations";
        final File baseDirectoryForFileTypeFile = new File(baseDirectoryForFileType);
        final String filePathAttempt = parentDirname + File.separator + filenameAttempt;
        
        final Exception ioException = new IOException("some error message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).correctPathElement(FilenameUtils.getName(filenameAttempt), "getAvailableFile");
                will(returnValue(filenameAttempt));
            oneOf(mockArchiveFileHelper).getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode); will(returnValue(baseDirectoryForFileType));
            oneOf(mockArchiveFileHelper).getFinalFile(baseDirectoryForFileTypeFile, filenameAttempt); will(returnValue(mockFile));
            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockFile); will(throwException(ioException));
        }});
        
        try {
            archiveFileLocationProvider.getAvailableFile(parentPath, parentCorpusNamePathToClosestTopNode, mockNode, filenameAttempt);
            fail("An exception should have been thrown");
        } catch(IOException ex) {
            assertNotNull(ex);
            assertEquals("Exception different from expected", ioException, ex);
        }
    }
    
    @Test
    public void getRelativePath_ChildPathIsInChildDirectoryOfParent() {
        
        final File parentFile = new File(URI.create("file:/archive/path/parentdir/parent.cmdi"));
        final File childFile = new File(URI.create("file:/archive/path/parentdir/childdir/child.cmdi"));
        final String expectedRelativePath = "childdir/child.cmdi";
        
        String retrievedRelativePath = archiveFileLocationProvider.getChildPathRelativeToParent(parentFile, childFile);
        
        assertEquals("Retrieved relative path different from expected", expectedRelativePath, retrievedRelativePath);
    }
    
    @Test
    public void getRelativePath_ChildPathIsNotInChildDirectoryOfParent() {
        
        final File parentFile = new File(URI.create("file:/archive/path/parentdir/parent.cmdi"));
        final File childFile = new File(URI.create("file:/archive/path/otherparentdir/childdir/child.cmdi"));
        final String expectedRelativePath = "../otherparentdir/childdir/child.cmdi";
        
        String retrievedRelativePath = archiveFileLocationProvider.getChildPathRelativeToParent(parentFile, childFile);
        
        assertEquals("Retrieved relative path different from expected", expectedRelativePath, retrievedRelativePath);
    }
    
    @Test
    public void getRelativePath_ChildPathIsInSameDirectoryOfParent() {
        
        final File parentFile = new File(URI.create("file:/archive/path/parentdir/parent.cmdi"));
        final File childFile = new File(URI.create("file:/archive/path/parentdir/child.cmdi"));
        final String expectedRelativePath = "child.cmdi";
        
        String retrievedRelativePath = archiveFileLocationProvider.getChildPathRelativeToParent(parentFile, childFile);
        
        assertEquals("Retrieved relative path different from expected", expectedRelativePath, retrievedRelativePath);
    }
    
    @Test
    public void getRelativePath_ChildPathEqualsParentPath() {
        
        final File parentFile = new File(URI.create("file:/archive/path/parentdir/parent.cmdi"));
        final File childFile = new File(URI.create("file:/archive/path/parentdir/parent.cmdi"));
        
        final String expectedExceptionMessage = "Parent and child files should be different";
        
        try {
            archiveFileLocationProvider.getChildPathRelativeToParent(parentFile, childFile);
            fail("should have thrown exception");
        } catch(IllegalStateException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
        }
    }
    
    @Test
    public void getHttpUriContainingAlreadyHttpRoot() throws URISyntaxException {
        
        String fileRelativePath = "anotherFolder/file.cmdi";
        URI initialLocation = new URI(dbHttpsRoot + fileRelativePath);
        URI expectedLocation = initialLocation;
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithHttpsRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
    
    @Test
    public void getHttpUriContainingLocalRoot() throws URISyntaxException {
        
        String fileRelativePath = "anotherFolder/file.cmdi";
        URI initialLocation = new URI(dbLocalRoot + fileRelativePath);
        URI expectedLocation = new URI(dbHttpsRoot + fileRelativePath);
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithHttpsRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
    
    @Test
    public void getHttpUriContainingDifferentRoot() throws URISyntaxException {
        
        String fileAbsolutePath = "https://alternative/root/anotherFolder/file.cmdi";
        URI initialLocation = new URI(fileAbsolutePath);
        URI expectedLocation = initialLocation;
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithHttpsRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
    
    @Test
    public void getLocalUriContainingAlreadyLocalRoot() throws URISyntaxException {
        
        String fileRelativePath = "anotherFolder/file.cmdi";
        URI initialLocation = new URI(dbLocalRoot + fileRelativePath);
        URI expectedLocation = initialLocation;
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithLocalRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
    
    @Test
    public void getLocalUriContainingHttpsRoot() throws URISyntaxException {
        
        String fileRelativePath = "anotherFolder/file.cmdi";
        URI initialLocation = new URI(dbHttpsRoot + fileRelativePath);
        URI expectedLocation = new URI(dbLocalRoot + fileRelativePath);
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithLocalRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
    
    @Test
    public void getLocalUriContainingHttpRoot() throws URISyntaxException {
        
        String fileRelativePath = "anotherFolder/file.cmdi";
        URI initialLocation = new URI(dbHttpRoot + fileRelativePath);
        URI expectedLocation = new URI(dbLocalRoot + fileRelativePath);
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithLocalRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
    
    @Test
    public void getLocalUriContainingDifferentRoot() throws URISyntaxException {
        
        String fileAbsolutePath = "http://alternative/root/anotherFolder/file.cmdi";
        URI initialLocation = new URI(fileAbsolutePath);
        URI expectedLocation = initialLocation;
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithLocalRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
    
    @Test
    public void getOrphansDirectory_WsTopNodeInCorpusstructureDirectory() throws MalformedURLException {
        
        File pathPrefix = new File("/some/url/with/");
        File corpusstructureDirectoryFullPath = new File(pathPrefix, corpusstructureDirectoryName);
        File fullPath = new File(corpusstructureDirectoryFullPath, "blabla.cmdi");
        
        URI testURI = fullPath.toURI();
        
        File expectedOrphansDirectory = new File(pathPrefix, orphansDirectoryName);
        File retrievedOrphansDirectory = archiveFileLocationProvider.getOrphansDirectory(testURI);
        
        assertEquals("Retrieved orphans directory different from expected", expectedOrphansDirectory.getAbsolutePath(), retrievedOrphansDirectory.getAbsolutePath());
    }
    
    @Test
    public void getOrphansDirectory_WsTopNodeInMetadataDirectoryInSameLevel() throws MalformedURLException, IOException {
        
        prepareExistingTempDirectory();
        File corpusstructureFolder = new File(tempDirectory, corpusstructureDirectoryName);
        FileUtils.forceMkdir(corpusstructureFolder);
        assertTrue(corpusstructureFolder.exists());

        File fullFilePath = new File(tempDirectory, "Metadata/blabla.cmdi");
        
        URI testURI = fullFilePath.toURI();
        
        File expectedOrphansDirectory = new File(tempDirectory, orphansDirectoryName);
        File retrievedOrphansDirectory = archiveFileLocationProvider.getOrphansDirectory(testURI);
        
        assertEquals("Retrieved orphans directory different from expected", expectedOrphansDirectory.getAbsolutePath(), retrievedOrphansDirectory.getAbsolutePath());
    }
    
    @Test
    public void getOrphansDirectory_WsTopNodeInMetadataDirectoryInDifferentLevel() throws MalformedURLException, IOException {
        
        prepareExistingTempDirectory();
        File corpusstructureFolder = new File(tempDirectory, corpusstructureDirectoryName);
        FileUtils.forceMkdir(corpusstructureFolder);
        assertTrue(corpusstructureFolder.exists());

        File fullFilePath = new File(tempDirectory, "SomeNode/Metadata/blabla.cmdi");
        
        URI testURI = fullFilePath.toURI();
        
        File expectedOrphansDirectory = new File(tempDirectory, orphansDirectoryName);
        File retrievedOrphansDirectory = archiveFileLocationProvider.getOrphansDirectory(testURI);
        
        assertEquals("Retrieved orphans directory different from expected", expectedOrphansDirectory.getAbsolutePath(), retrievedOrphansDirectory.getAbsolutePath());
    }
    
    @Test
    public void fileIsInOrphansDirectory() {
        File testFile = new File("file:/bla/bla/sessions");
        
        boolean isFileInOrphansDirectory = archiveFileLocationProvider.isFileInOrphansDirectory(testFile);
        assertTrue("Result should be true", isFileInOrphansDirectory);
    }
    
    @Test
    public void fileIsNotInOrphansDirectory() {
        File testFile = new File("file:/bla/bla/notthere");
        
        boolean isFileInOrphansDirectory = archiveFileLocationProvider.isFileInOrphansDirectory(testFile);
        assertFalse("Result should be false", isFileInOrphansDirectory);
    }
    
    @Test
    public void getFolderNameBeforeCorpusstructure_wCorpusstructure() {
        final String testPath = "/archive/root/TopNode/" + corpusstructureDirectoryName + "/something";
        final String expectedName = "TopNode";
        
        String result = archiveFileLocationProvider.getFolderNameBeforeCorpusstructure(testPath);
        		
        assertEquals("Corpusstructure folder different from expected", result, expectedName);
    }
    
    
    private void prepareExistingTempDirectory() throws IOException {
        tempDirectory = testFolder.newFolder();
        assertTrue("Temp directory wasn't created.", tempDirectory.exists());
    }
}