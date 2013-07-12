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
import java.net.URL;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.ArchiveFileLocationProvider;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusArchiveFileLocationProviderTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private ArchiveFileLocationProvider archiveFileLocationProvider;
    
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
    }
    
    @After
    public void tearDown() {
    }


//    @Test
//    public void getNextAvailableMetadataFileForNodeCreatedFromScratch_ParentFollowingMpiRules() throws MalformedURLException, IOException {
//        
//        final String parentFileNameWithoutExtension = "1-2";
//        final String parentBasename = parentFileNameWithoutExtension + ".cmdi";
//        final String parentDirname = "/archive/some/url";
//        final URL parentArchiveURL = new URL("file:/archive/some/url/" + parentBasename);
//        final String childNodeName = "childnode";
////        final String childBasename = parentFileNameWithoutExtension + "-01.cmdi";
//        
//        context.checking(new Expectations() {{
//            
//            // when originURL of the child node is null, use archiveURL of parent as the base for the name
//            oneOf(mockArchiveFileHelper).fileNameMatchesMpiRules(parentArchiveURL.getPath()); will(returnValue(Boolean.TRUE));
//            
////            oneOf(mockArchiveFileHelper).correctPathElement(childNodeName, "getNextAvailableMetadataFile"); will(returnValue(childNodeName));
//            oneOf(mockArchiveFileHelper).getFinalFile(parentDirname, parentBasename); will(returnValue(mockChildFile));
//            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockChildFile);
//        }});
//        
//        File retrievedFile = archiveFileLocationProvider.getNextAvailableMetadataFile(parentArchiveURL, childNodeName, null);
//        
//        assertEquals("Retrieved file different from expected", mockChildFile, retrievedFile);
//    }
    
//    @Test
//    public void getNextAvailableMetadataFileForNodeCreatedFromScratch_ParentNotFollowingMpiRules() throws MalformedURLException, IOException {
//        
//        final String parentFileNameWithoutExtension = "parentnode";
//        final String parentBasename = parentFileNameWithoutExtension + ".cmdi";
//        final String parentDirname = "/archive/some/url";
//        final URL parentArchiveURL = new URL("file:/archive/some/url/" + parentBasename);
//        final String childNodeName = "childnode";
//        final String childBasename = childNodeName + ".cmdi";
//        
//        context.checking(new Expectations() {{
//            
//            // when originURL of the child node is null, use archiveURL of parent as the base for the name
//            oneOf(mockArchiveFileHelper).fileNameMatchesMpiRules(parentArchiveURL.getPath()); will(returnValue(Boolean.FALSE));
//            
//            oneOf(mockArchiveFileHelper).correctPathElement(childNodeName, "getNextAvailableMetadataFile"); will(returnValue(childNodeName));
//            oneOf(mockArchiveFileHelper).getFinalFile(parentDirname, childBasename); will(returnValue(mockChildFile));
//            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockChildFile);
//        }});
//        
//        File retrievedFile = archiveFileLocationProvider.getNextAvailableMetadataFile(parentArchiveURL, childNodeName, null);
//        
//        assertEquals("Retrieved file different from expected", mockChildFile, retrievedFile);
//    }
    
//    @Test
//    public void getNextAvailableMetadataFileForNodeUploaded_FollowingMpiRules() throws MalformedURLException, IOException {
//        
//        final String parentBasename = "parentnode";
//        final String parentFilenameWithExtension = parentBasename + ".cmdi";
//        final String parentDirname = "/archive/some/url";
//        final URL parentArchiveURL = new URL("file:/archive/some/url/" + parentFilenameWithExtension);
//        final String childNodeName = "1-3";
//        final String childFilename = "1-3.cmdi";
////        final String expectedChildFilename = "1-3-01.cmdi";
//        
////        final String childFilename = "childnode";
////        final String childFilenameWithExtension = childFilename + ".cmdi";
//        final URL childOriginURL = new URL("file:/local/folder/" + childFilename);
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockArchiveFileHelper).fileNameMatchesMpiRules(childOriginURL.getPath()); will(returnValue(Boolean.TRUE));
//            
////            oneOf(mockArchiveFileHelper).correctPathElement(childNodeName, "getNextAvailableMetadataFile"); will(returnValue(childNodeName));
//            oneOf(mockArchiveFileHelper).getFinalFile(parentDirname, childFilename); will(returnValue(mockChildFile));
//            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockChildFile);
//        }});
//        
//        File retrievedFile = archiveFileLocationProvider.getNextAvailableMetadataFile(parentArchiveURL, childNodeName, childOriginURL);
//        
//        assertEquals("Retrieved file different from expected", mockChildFile, retrievedFile);
//    }
    
//    @Test
//    public void getNextAvailableMetadataFileForNodeUploaded_NotFollowingMpiRules() throws MalformedURLException, IOException {
//        
//        final String parentBasename = "parentnode";
//        final String parentFilenameWithExtension = parentBasename + ".cmdi";
//        final String parentDirname = "/archive/some/url";
//        final URL parentArchiveURL = new URL("file:/archive/some/url/" + parentFilenameWithExtension);
//        final String childNodeName = "CHILDNODE";
//        final String expectedChildFilename = "CHILDNODE.cmdi";
//        
//        final String childFilename = "childnode";
//        final String childFilenameWithExtension = childFilename + ".cmdi";
//        final URL childOriginURL = new URL("file:/local/folder/" + childFilenameWithExtension);
//        
//        context.checking(new Expectations() {{
//            
//            oneOf(mockArchiveFileHelper).fileNameMatchesMpiRules(childOriginURL.getPath()); will(returnValue(Boolean.FALSE));
//            
//            oneOf(mockArchiveFileHelper).correctPathElement(childNodeName, "getNextAvailableMetadataFile"); will(returnValue(childNodeName));
//            oneOf(mockArchiveFileHelper).getFinalFile(parentDirname, expectedChildFilename); will(returnValue(mockChildFile));
//            oneOf(mockArchiveFileHelper).createFileAndDirectories(mockChildFile);
//        }});
//        
//        File retrievedFile = archiveFileLocationProvider.getNextAvailableMetadataFile(parentArchiveURL, childNodeName, childOriginURL);
//        
//        assertEquals("Retrieved file different from expected", mockChildFile, retrievedFile);
//    }
    
    
    @Test
    public void getAvailableFile() throws IOException {
        
        final String parentPath = "/archive/some/url/parent.cmdi";
        final String parentDirname = FilenameUtils.getFullPath(parentPath);
        final String filenameAttempt = "resource.pdf";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WR;
        final String baseDirectoryForFileType = parentDirname + File.separator + "Annotations";
        final String filePathAttempt = parentDirname + File.separator + filenameAttempt;
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).correctPathElement(FilenameUtils.getName(filenameAttempt), "getAvailableFile");
                will(returnValue(filenameAttempt));
            oneOf(mockArchiveFileHelper).getDirectoryForFileType(parentDirname, nodeType); will(returnValue(baseDirectoryForFileType));
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
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WR;
        final String baseDirectoryForFileType = parentDirname + File.separator + "Annotations";
        final String filePathAttempt = parentDirname + File.separator + filenameAttempt;
        
        final Exception ioException = new IOException("some error message");
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileHelper).correctPathElement(FilenameUtils.getName(filenameAttempt), "getAvailableFile");
                will(returnValue(filenameAttempt));
            oneOf(mockArchiveFileHelper).getDirectoryForFileType(parentDirname, nodeType); will(returnValue(baseDirectoryForFileType));
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
}