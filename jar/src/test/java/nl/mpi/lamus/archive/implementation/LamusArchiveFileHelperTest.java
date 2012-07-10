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
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.archive.LamusArchiveTestBeans;
import nl.mpi.lamus.archive.LamusArchiveTestProperties;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusArchiveTestProperties.class, LamusArchiveTestBeans.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusArchiveFileHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Autowired
    private ArchiveFileHelper testArchiveFileHelper;
    @Mock File mockFile;
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Autowired
    private int maxDirectoryNameLength;
    @Autowired
    private String corpusDirectoryBaseName;
    @Autowired
    private String orphansDirectoryBaseName;
    @Autowired
    private long typeRecheckSizeLimitInBytes;
    
    
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
}
