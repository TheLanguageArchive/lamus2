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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
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
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusArchiveFileLocationProviderTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    
    private final String dbHttpsRoot = "https://some.server/archive/";
    private final String dbHttpRoot = "http://some.server/archive/";
    private final String dbLocalRoot = "file:/some/loca/folder/archive/";
    
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Mock ArchiveFileHelper mockArchiveFileHelper;
    @Mock File mockFile;
    
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
    }
    
    @After
    public void tearDown() {
    }
    
    
    @Test
    public void getAvailableFile() throws IOException {
        
        final String parentPath = "/archive/some/url/parent.cmdi";
        final String parentDirname = FilenameUtils.getFullPath(parentPath);
        final String filenameAttempt = "resource.pdf";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE;
        final String baseDirectoryForFileType = parentDirname + File.separator + "Annotations";
        final String filePathAttempt = parentDirname + File.separator + filenameAttempt;
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).correctPathElement(FilenameUtils.getName(filenameAttempt), "getAvailableFile");
                will(returnValue(filenameAttempt));
            oneOf(mockArchiveFileHelper).getDirectoryForFileType(parentPath, nodeType); will(returnValue(baseDirectoryForFileType));
            oneOf(mockArchiveFileHelper).getFinalFile(baseDirectoryForFileType, filenameAttempt); will(returnValue(mockFile));
            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockFile);
            
        
        //correct path element
        
        //if filename in that path already exists, rename it (_0001?) until filename is new (up to count = 10000 ??)
        
        //create directories if necessary, and empty file
        }});
        
        File retrievedFile = archiveFileLocationProvider.getAvailableFile(parentPath, filenameAttempt, nodeType);
        
        assertEquals("Retrieved file different from expected", mockFile, retrievedFile);
    }
    
    @Test
    public void getAvailableFile_ThrowsException() throws IOException {
        
        final String parentPath = "/archive/some/url/parent.cmdi";
        final String parentDirname = FilenameUtils.getFullPath(parentPath);
        final String filenameAttempt = "resource.pdf";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE;
        final String baseDirectoryForFileType = parentDirname + File.separator + "Annotations";
        final String filePathAttempt = parentDirname + File.separator + filenameAttempt;
        
        final Exception ioException = new IOException("some error message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).correctPathElement(FilenameUtils.getName(filenameAttempt), "getAvailableFile");
                will(returnValue(filenameAttempt));
            oneOf(mockArchiveFileHelper).getDirectoryForFileType(parentPath, nodeType); will(returnValue(baseDirectoryForFileType));
            oneOf(mockArchiveFileHelper).getFinalFile(baseDirectoryForFileType, filenameAttempt); will(returnValue(mockFile));
            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockFile); will(throwException(ioException));
            
        
        //correct path element
        
        //if filename in that path already exists, rename it (_0001?) until filename is new (up to count = 10000 ??)
        
        //create directories if necessary, and empty file
        }});
        
        try {
            archiveFileLocationProvider.getAvailableFile(parentPath, filenameAttempt, nodeType);
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
    public void getHttpUriContainingDifferentRoot() throws URISyntaxException { //TODO Just return the same file? Or throw some error? What could cause this?
        
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
    public void getLocalUriContainingDifferentRoot() throws URISyntaxException { //TODO Just return the same file? Or throw some error? What could cause this?
        
        String fileAbsolutePath = "http://alternative/root/anotherFolder/file.cmdi";
        URI initialLocation = new URI(fileAbsolutePath);
        URI expectedLocation = initialLocation;
        
        URI retrievedFile = archiveFileLocationProvider.getUriWithLocalRoot(initialLocation);
        
        assertEquals("Retrieved file different from expected", expectedLocation, retrievedFile);
    }
}