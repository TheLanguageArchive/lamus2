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
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Mock File mockChildFile;
    @Mock File mockChildAbsoluteFile;
    @Mock File mockParentFile;
    
    private final int maxDirectoryNameLength = 50;
    private final String corpusDirectoryBaseName = "Corpusstructure";
    private String orphansDirectoryBaseName = "sessions";
    private long typeRecheckSizeLimitInBytes = 8L * 1024 * 1024;
    
    private final String metadataDirectoryName = "Metadata";
    private final String resourcesDirectoryName = "Resources";
    
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
        testArchiveFileHelper = new LamusArchiveFileHelper();
        ReflectionTestUtils.setField(testArchiveFileHelper, "maxDirectoryNameLength", maxDirectoryNameLength);
        ReflectionTestUtils.setField(testArchiveFileHelper, "corpusDirectoryBaseName", corpusDirectoryBaseName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "orphansDirectoryBaseName", orphansDirectoryBaseName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "typeRecheckSizeLimitInBytes", typeRecheckSizeLimitInBytes);
        
        ReflectionTestUtils.setField(testArchiveFileHelper, "metadataDirectoryName", metadataDirectoryName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "resourcesDirectoryName", resourcesDirectoryName);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void getFileBasenamePathHasSlashes() {
        
        String expectedName = "baseName.txt";
        String fullName = "something/with/some/slashes/" + expectedName;
        
        String retrievedName = testArchiveFileHelper.getFileBasename(fullName);
        
        assertEquals("Retrieved basename different from expected", expectedName, retrievedName);
    }
    
    @Test
    public void getFileBasenamePathHasNoSlashes() {
        
        String expectedName = "something_without__slashes.txt";
        
        String retrievedName = testArchiveFileHelper.getFileBasename(expectedName);
        
        assertEquals("Retrieved basename different from expected", expectedName, retrievedName);
    }
    
    @Test
    public void getFileBasenameWithoutExtensionPathHasSlashes() {
        
        String expectedName = "baseName";
        String fullName = "something/with/some/slashes/" + expectedName + ".txt";
        
        String retrievedName = testArchiveFileHelper.getFileBasenameWithoutExtension(fullName);
        
        assertEquals("Retrieved basename (without extension) different from expected", expectedName, retrievedName);
    }
    
    @Test
    public void getFileBasenameWithoutExtensionPathHasNoSlashes() {
        
        String expectedName = "something_without__slashes";
        String fullName = expectedName + ".txt";
        
        String retrievedName = testArchiveFileHelper.getFileBasenameWithoutExtension(fullName);
        
        assertEquals("Retrieved basename (without extension) different from expected", expectedName, retrievedName);
    }
    
    @Test
    public void getFileTitlePathHasBaseName() {
        
        String expectedTitle = "baseName.txt";
        String fullName = "something/with/slashes/and/" + expectedTitle;
        
        String retrievedTitle = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals("Retrieved title different from expected", expectedTitle, retrievedTitle);
    }
    
    @Test
    public void getFileTitlePathHasNoBaseName() {
        
        String expectedTitle = "something";
        String fullName = expectedTitle + "/with/slashes/";
        
        String retrievedTitle = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals("Retrieved title different from expected", expectedTitle, retrievedTitle);
    }
    
    @Test
    public void getFileTitlePathHasUrlName() {
        
        String expectedTitle = "mpi";
        String fullName = "file:/" + expectedTitle + "/with/slashes/";
        
        String retrievedTitle = testArchiveFileHelper.getFileTitle(fullName);
        assertEquals("Retrieved title different from expected", expectedTitle, retrievedTitle);
    }
    
    @Test
    public void getFileTitlePathHasNoSlashes() {
        
        String expectedTitle = "no_slashes";
        
        String retrievedTitle = testArchiveFileHelper.getFileTitle(expectedTitle);
        assertEquals("Retrieved title different from expected", expectedTitle, retrievedTitle);
    }

    @Test
    public void correctPathElementWithInvalidCharacters() {
        
        String someReason = "because";
        String input = "this#file&has**invalid@characters.txt";
        String expectedOutput = "this_file_has_invalid_characters.txt";
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals("Actual output different from expected", expectedOutput, actualOutput);
    }
    
    @Test
    public void correctPathElementWithoutInvalidCharacters() {
        
        String someReason = "because";
        String input = "this_file_has_no_invalid_characters.txt";
        String expectedOutput = input;
        
        String actualOutput = testArchiveFileHelper.correctPathElement(input, someReason);
        
        assertEquals("Actual output different from expected", expectedOutput, actualOutput);
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
        
        assertEquals("Actual output different from expected", expectedOutput, actualOutput);
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
        
        assertEquals("Retrieved orphans directory different from expected", expectedOrphansDirectory.getAbsolutePath(), retrievedOrphansDirectory.getAbsolutePath());
    }
    
    /**
     * Test of getOrphansDirectory method, of class LamusArchiveFileHelper.
     */
    @Test
    public void getOrphansDirectoryWithoutCorpusDirectory() throws MalformedURLException, IOException {
        
        String pathPrefix = "/some/url/with/";
        File pathPrefixFile = testFolder.newFolder(pathPrefix);
        File corpusFolder = testFolder.newFolder(pathPrefix + corpusDirectoryBaseName);
        FileUtils.forceMkdir(corpusFolder);
        assertTrue(corpusFolder.exists());

        File fullFilePath = new File(pathPrefixFile, "metadata/blabla.cmdi");
        
        URI testURI = fullFilePath.toURI();
        
        File expectedOrphansDirectory = new File(pathPrefixFile, orphansDirectoryBaseName);
        File retrievedOrphansDirectory = testArchiveFileHelper.getOrphansDirectory(testURI);
        
        assertEquals("Retrieved orphans directory different from expected", expectedOrphansDirectory.getAbsolutePath(), retrievedOrphansDirectory.getAbsolutePath());
    }

    /**
     * Test of isFileSizeAboveTypeReCheckSizeLimit method, of class LamusArchiveFileHelper.
     */
    @Test
    public void fileSizeIsAboveTypeReCheckSizeLimit() {
        final long actualFileSize = typeRecheckSizeLimitInBytes + 1;
        
        context.checking(new Expectations() {{
            oneOf (mockFile).length(); will(returnValue(actualFileSize));
        }});
        
        boolean isSizeAboveLimit = testArchiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(mockFile);
        
        assertTrue("Result should be true", isSizeAboveLimit);
    }
    
    @Test
    public void fileSizeIsBelowTypeReCheckSizeLimit() {

        final long actualFileSize = typeRecheckSizeLimitInBytes - 1;
        
        context.checking(new Expectations() {{
            oneOf (mockFile).length(); will(returnValue(actualFileSize));
        }});
        
        boolean isSizeAboveLimit = testArchiveFileHelper.isFileSizeAboveTypeReCheckSizeLimit(mockFile);
        
        assertFalse("Result should be false", isSizeAboveLimit);
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
    public void getFinalFileNonExistingName() {
        
        final String dirPath = "/some/path";
        final String fileName = "file.cmdi";
        
        File dir = testFolder.newFolder(dirPath);
        File expectedFile = new File(dir, fileName);
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(dir.getPath(), fileName);
        
        assertEquals("Retrieved file different from expected", expectedFile, retrievedFile);
    }
    
    @Test
    public void getFinalFileExistingOneName() throws IOException {
        
        final String dirPath = "/some/path";
        final String fileName = "file.cmdi";
        final String expectedName = "file_2.cmdi";
        
        File dir = testFolder.newFolder(dirPath);
        FileUtils.forceMkdir(dir);
        File file = new File(dir, fileName);
        FileUtils.touch(file);
        File expectedFile = new File(dir, expectedName);
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(dir.getPath(), fileName);
        
        assertEquals("Retrieved file different from expected", expectedFile, retrievedFile);
    }
    
    @Test
    public void getFinalFileExistingSeveralNames() throws IOException {
        
        final String dirPath = "/some/path";
        final String fileName = "file.cmdi";
        File dir = testFolder.newFolder(dirPath);
        FileUtils.forceMkdir(dir);
        File file = new File(dir, fileName);
        FileUtils.touch(file);
        
        for(int suffix = 1; suffix < 11; suffix++) {
            String currentFileName = FilenameUtils.getBaseName(fileName) + "_" + suffix + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(fileName);
            File currentFile = new File(dir, currentFileName);
            FileUtils.touch(currentFile);
        }
        
        String expectedName = "file_11.cmdi";
        File expectedFile = new File(dir, expectedName);
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(dir.getPath(), fileName);
        
        assertEquals("Retrieved file different from expected", expectedFile, retrievedFile);
    }
    
    
    //TODO Does it make sense to have this 10000 sufix limit? How likely is it to happen?
    @Test
    public void getFinalFileExistingAllNames() throws IOException {
        
        final String dirPath = "/some/path";
        final String fileName = "file.cmdi";
        File dir = testFolder.newFolder(dirPath);
        FileUtils.forceMkdir(dir);
        File file = new File(dir, fileName);
        FileUtils.touch(file);
        
        for(int suffix = 1; suffix < 10000; suffix++) {
            String currentFileName = FilenameUtils.getBaseName(fileName) + "_" + suffix + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(fileName);
            File currentFile = new File(dir, currentFileName);
            FileUtils.touch(currentFile);
        }
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(dir.getPath(), fileName);
        
        //TODO Some Exception instead?
        
        assertNull("Retrieved file should be null when all suffixes exist already", retrievedFile);
    }
    
    @Test
    public void createFileAndDirectoriesBothNonExistingYet() throws IOException {
        
        final String dirPath = "/some/path";
        final String fileName = "file.cmdi";
        
        final File dir = testFolder.newFolder(dirPath);
        final File file = new File(dir, fileName);
        
        testArchiveFileHelper.createFileAndDirectories(file);
        
        assertTrue("Directory should have been created", dir.exists());
        assertTrue("File should have been created", file.exists());
    }
    
    @Test
    public void createFileAndDirectoriesFileNonExistingYet() throws IOException {
        
        final String dirPath = "/some/path";
        final String fileName = "file.cmdi";
        
        final File dir = testFolder.newFolder(dirPath);
        FileUtils.forceMkdir(dir);
        assertTrue("Directory should have been created", dir.exists());
        final File file = new File(dir, fileName);
        
        testArchiveFileHelper.createFileAndDirectories(file);
        
        assertTrue("File should have been created", file.exists());
    }
    
    @Test
    public void createFileAndDirectoriesBothExistingAlready() throws IOException {
        
        final String dirPath = "/some/path";
        final String fileName = "file.cmdi";
        
        final File dir = testFolder.newFolder(dirPath);
        FileUtils.forceMkdir(dir);
        assertTrue("Directory should have been created", dir.exists());
        final File file = new File(dir, fileName);
        FileUtils.touch(file);
        
        testArchiveFileHelper.createFileAndDirectories(file);
        
        assertTrue("File should have been created", file.exists());
    }
    
    @Test
    public void getDirectoryForResource() {
        
        final String parentpath = "/some/path/parent.cmdi";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE;
        final String expectedDirectory = "/some/path/" + resourcesDirectoryName;
        
        String result = testArchiveFileHelper.getDirectoryForFileType(parentpath, nodeType);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForMetadata() {
        
        final String parentPath = "/some/path/parent.cmdi";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String expectedDirectory = "/some/path/" + metadataDirectoryName;
        
        String result = testArchiveFileHelper.getDirectoryForFileType(parentPath, nodeType);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
}
