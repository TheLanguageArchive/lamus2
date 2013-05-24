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
package nl.mpi.lamus.archive.implementation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import nl.mpi.corpusstructure.ArchiveAccessContext;
import nl.mpi.corpusstructure.ArchiveObjectsDB;
import nl.mpi.corpusstructure.NodeIdUtils;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.util.OurURL;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusArchiveFileHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Autowired
    private ArchiveFileHelper testArchiveFileHelper;
    @Mock File mockFile;
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    private final int maxDirectoryNameLength = 50;
    private final String corpusDirectoryBaseName = "Corpusstructure";
    private String orphansDirectoryBaseName = "sessions";
    private long typeRecheckSizeLimitInBytes = 8L * 1024 * 1024;
    
    @Mock ArchiveObjectsDB mockArchiveObjectsDB;
    
    public LamusArchiveFileHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        testArchiveFileHelper = new LamusArchiveFileHelper(mockArchiveObjectsDB);
        ReflectionTestUtils.setField(testArchiveFileHelper, "maxDirectoryNameLength", maxDirectoryNameLength);
        ReflectionTestUtils.setField(testArchiveFileHelper, "corpusDirectoryBaseName", corpusDirectoryBaseName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "orphansDirectoryBaseName", orphansDirectoryBaseName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "typeRecheckSizeLimitInBytes", typeRecheckSizeLimitInBytes);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getFileBasenameWithSlashes() {
        
        String baseName = "baseName.txt";
        String fullName = "something/with/some/slashes/" + baseName;
        
        String retrievedName = testArchiveFileHelper.getFileBasename(fullName);
        
        assertEquals(baseName, retrievedName);
        
    }
    
    @Test
    public void getFileBasenameWithoutSlashes() {
        
        String fullName = "something_without__slashes.txt";
        
        String retrievedName = testArchiveFileHelper.getFileBasename(fullName);
        
        assertEquals(fullName, retrievedName);
        
    }
    
    @Test
    public void getFileDirNameWithSlashes() {
        
        String dirName = "something/with/some/slashes";
        String fullName = dirName + "/baseName.txt";
        
        String retrievedName = testArchiveFileHelper.getFileDirname(fullName);
        
        assertEquals(dirName, retrievedName);
    }
    
    @Test
    public void getFileDirNameWithoutSlashes() {
        
        String fullName = "something_without__slashes.txt";
        
        String retrievedName = testArchiveFileHelper.getFileDirname(fullName);
        
        assertEquals("", retrievedName);
    }
    
    @Test
    public void getFileTitleWithBaseName() {
        
        String baseName = "baseName.txt";
        String fullName = "something/with/slashes/and/" + baseName;
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(baseName, retrievedName);
    }
    
    @Test
    public void getFileTitleWithoutBaseName() {
        
        String nameBeforeFirstSlash = "something";
        String fullName = nameBeforeFirstSlash + "/with/slashes/";
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(nameBeforeFirstSlash, retrievedName);
    }
    
    @Test
    public void getFileTitleWithUrlName() {
        
        String domainName = "mpi";
        String fullName = "file:/" + domainName + "/with/slashes/";
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(domainName, retrievedName);
    }
    
    @Test
    public void getFileTitleWithoutSlashes() {
        
        String fullName = "no_slashes";
        
        String retrievedName = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals(fullName, retrievedName);
    }

    @Test
    public void correctPathElementWithInvalidCharacters() {
        
        String someReason = "because";
        String input = "this#file&has**invalid@characters.txt";
        String expectedOutput = "this_file_has_invalid_characters.txt";
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    public void correctPathElementWithoutInvalidCharacters() {
        
        String someReason = "because";
        String input = "this_file_has_no_invalid_characters.txt";
        String expectedOutput = input;
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    @Test
    public void correctPathElementAboveMaxDirLength() {
        
        String someReason = "because";
        String firstMaxNumberMinusSevenCharacters = "this_has_several_characters_and_they_are_re";
        String lastCharacters = "peated_and_they_are_repeated_and_they_are_repeated";
        String extension = ".txt";
        String threePoints = "...";
        String input = firstMaxNumberMinusSevenCharacters + lastCharacters + extension;
        String expectedOutput = firstMaxNumberMinusSevenCharacters + threePoints + extension;
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals(expectedOutput, actualOutput);
    }

    /**
     * Test of getOrphansDirectory method, of class LamusArchiveFileHelper.
     */
    @Test
    public void getOrphansDirectoryWithCorpusDirectory() throws MalformedURLException {
        
        File pathPrefix = new File("/some/url/with/");
        File corpusDirectoryFullPath = new File(pathPrefix, corpusDirectoryBaseName);
        File fullPath = new File(corpusDirectoryFullPath, "blabla.cmdi");
        
        URI testURI = fullPath.toURI();
        
        File expectedOrphansDirectory = new File(pathPrefix, orphansDirectoryBaseName);
        File retrievedOrphansDirectory = testArchiveFileHelper.getOrphansDirectory(testURI);
        
        assertEquals(expectedOrphansDirectory.getAbsolutePath(), retrievedOrphansDirectory.getAbsolutePath());
    }
    
    /**
     * Test of getOrphansDirectory method, of class LamusArchiveFileHelper.
     */
    @Test
    public void getOrphansDirectoryWithoutCorpusDirectory() throws MalformedURLException {
        
        String pathPrefix = "/some/url/with/";
        File pathPrefixFile = testFolder.newFolder(pathPrefix);
        File corpusFolder = testFolder.newFolder(pathPrefix + corpusDirectoryBaseName);
        corpusFolder.mkdirs();
        assertTrue(corpusFolder.exists());

        File fullFilePath = new File(pathPrefixFile, "metadata/blabla.cmdi");
        
        URI testURI = fullFilePath.toURI();
        
        File expectedOrphansDirectory = new File(pathPrefixFile, orphansDirectoryBaseName);
        File retrievedOrphansDirectory = testArchiveFileHelper.getOrphansDirectory(testURI);
        
        assertEquals(expectedOrphansDirectory.getAbsolutePath(), retrievedOrphansDirectory.getAbsolutePath());
    }

    /**
     * Test of isFileSizeAboveTypeReCheckSizeLimit method, of class LamusArchiveFileHelper.
     */
    @Test
    public void fileSizeIsAboveTypeReCheckSizeLimit() throws IOException {
        final long actualFileSize = typeRecheckSizeLimitInBytes + 1;
        
        context.checking(new Expectations() {{
            oneOf (mockFile).length(); will(returnValue(actualFileSize));
        }});
        
        boolean isSizeAboveLimit = testArchiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(mockFile);
        
        assertTrue(isSizeAboveLimit);
    }
    
    @Test
    public void fileSizeIsBelowTypeReCheckSizeLimit() {

        final long actualFileSize = typeRecheckSizeLimitInBytes - 1;
        
        context.checking(new Expectations() {{
            oneOf (mockFile).length(); will(returnValue(actualFileSize));
        }});
        
        boolean isSizeAboveLimit = testArchiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(mockFile);
        
        assertFalse(isSizeAboveLimit);
    }
    
    @Test
    public void fileIsInOrphansDirectory() {
        File testFile = new File("file:/bla/bla/sessions");
        
        boolean isFileInOrphansDirectory = testArchiveFileHelper.isFileInOrphansDirectory(testFile);
        assertTrue("Result should be true", isFileInOrphansDirectory);
    }
    
    @Test
    public void fileIsNotInOrphansDirectory() {
        File testFile = new File("file:/bla/bla/notthere");
        
        boolean isFileInOrphansDirectory = testArchiveFileHelper.isFileInOrphansDirectory(testFile);
        assertFalse("Result should be false", isFileInOrphansDirectory);
    }

    @Test
    public void urlHasLocalProtocol() throws MalformedURLException {
        OurURL testUrl = new OurURL("file:/bla/bla");
        
        boolean isUrlLocal = testArchiveFileHelper.isUrlLocal(testUrl);
        assertTrue("Result should be true", isUrlLocal);
    }
    
    @Test
    public void urlHasRemoteProtocol() throws MalformedURLException {
        OurURL testUrl = new OurURL("http://bla/bla");
        
        boolean isUrlLocal = testArchiveFileHelper.isUrlLocal(testUrl);
        assertFalse("Result should be false", isUrlLocal);
    }
    
    @Test
    public void getArchiveLocationForNodeID() throws MalformedURLException {
        
        final int archiveNodeID = 100;
        final OurURL nodeURL = new OurURL("http://some.url");
        
        final File expectedFile = new File(nodeURL.getPath());
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveObjectsDB).getObjectURL(NodeIdUtils.TONODEID(archiveNodeID), ArchiveAccessContext.getFileUrlContext());
                will(returnValue(nodeURL));
        }});
        
        File result = testArchiveFileHelper.getArchiveLocationForNodeID(archiveNodeID);
        
        assertEquals("Location different from expected", expectedFile, result);
    }
}
