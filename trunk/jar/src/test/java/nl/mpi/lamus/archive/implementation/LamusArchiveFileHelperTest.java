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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;
import nl.mpi.archiving.corpusstructure.core.FileInfo;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.cmdi.profile.AllowedCmdiProfiles;
import nl.mpi.lamus.cmdi.profile.CmdiProfile;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.util.Checksum;
import nl.mpi.util.OurURL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Checksum.class})
public class LamusArchiveFileHelperTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Autowired
    private ArchiveFileHelper testArchiveFileHelper;
    @Mock File mockFile;
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Mock File mockChildFile;
    @Mock File mockChildAbsoluteFile;
    @Mock File mockParentFile;
    
    @Mock WorkspaceNode mockNode;
    
    @Mock FileInfo mockArchiveFileInfo;
    @Mock File mockWorkspaceFile;
    
    @Mock AllowedCmdiProfiles mockAllowedCmdiProfiles;
    @Mock CmdiProfile mockCmdiProfile;
    
    private final int maxDirectoryNameLength = 50;
    private final long typeRecheckSizeLimitInBytes = 8L * 1024 * 1024;
    
    private final String corpusstructureDirectoryName = "Corpusstructure";
    private final String metadataDirectoryName = "Metadata";
    private final String annotationsDirectoryName = "Annotations";
    private final String mediaDirectoryName = "Media";
    private final String infoDirectoryName = "Info";
    
    private final File trashCanBaseDirectory = new File("/lat/corpora/trashcan/");
    private final File versioningBaseDirectory = new File("/lat/corpora/versioning/");
    
    private File existingTempFile;
    private File existingTempDirectory;
    private File existingNonWritableTempDirectory;
    private File nonExistingTempDirectory;
    
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
        ReflectionTestUtils.setField(testArchiveFileHelper, "typeRecheckSizeLimitInBytes", typeRecheckSizeLimitInBytes);
        
        ReflectionTestUtils.setField(testArchiveFileHelper, "corpusstructureDirectoryName", corpusstructureDirectoryName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "metadataDirectoryName", metadataDirectoryName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "annotationsDirectoryName", annotationsDirectoryName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "mediaDirectoryName", mediaDirectoryName);
        ReflectionTestUtils.setField(testArchiveFileHelper, "infoDirectoryName", infoDirectoryName);
        
        ReflectionTestUtils.setField(testArchiveFileHelper, "trashCanBaseDirectory", trashCanBaseDirectory);
        ReflectionTestUtils.setField(testArchiveFileHelper, "versioningBaseDirectory", versioningBaseDirectory);
        
        ReflectionTestUtils.setField(testArchiveFileHelper, "allowedCmdiProfiles", mockAllowedCmdiProfiles);
    }
    
    @After
    public void tearDown() throws IOException {
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
    public void getFinalFileNonExistingName() throws IOException {
        
        final String fileName = "file.cmdi";
        prepareExistingTempDirectory();
        
        File expectedFile = new File(existingTempDirectory, fileName);
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(existingTempDirectory, fileName);
        
        assertEquals("Retrieved file different from expected", expectedFile, retrievedFile);
    }
    
    @Test
    public void getFinalFileExistingOneName() throws IOException {
        
        final String fileName = "file.cmdi";
        final String expectedName = "file_2.cmdi";
        prepareExistingTempDirectory();
        
        File file = new File(existingTempDirectory, fileName);
        FileUtils.touch(file);
        File expectedFile = new File(existingTempDirectory, expectedName);
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(existingTempDirectory, fileName);
        
        assertEquals("Retrieved file different from expected", expectedFile, retrievedFile);
    }
    
    @Test
    public void getFinalFileExistingSeveralNames() throws IOException {
        
        final String fileName = "file.cmdi";
        prepareExistingTempDirectory();
        
        File file = new File(existingTempDirectory, fileName);
        FileUtils.touch(file);
        
        for(int suffix = 1; suffix < 11; suffix++) {
            String currentFileName = FilenameUtils.getBaseName(fileName) + "_" + suffix + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(fileName);
            File currentFile = new File(existingTempDirectory, currentFileName);
            FileUtils.touch(currentFile);
        }
        
        String expectedName = "file_11.cmdi";
        File expectedFile = new File(existingTempDirectory, expectedName);
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(existingTempDirectory, fileName);
        
        assertEquals("Retrieved file different from expected", expectedFile, retrievedFile);
    }
    
    
    //TODO Does it make sense to have this 10000 sufix limit? How likely is it to happen?
    @Test
    public void getFinalFileExistingAllNames() throws IOException {
        
        final String fileName = "file.cmdi";
        prepareExistingTempDirectory();
        
        File file = new File(existingTempDirectory, fileName);
        FileUtils.touch(file);
        
        for(int suffix = 1; suffix < 10000; suffix++) {
            String currentFileName = FilenameUtils.getBaseName(fileName) + "_" + suffix + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(fileName);
            File currentFile = new File(existingTempDirectory, currentFileName);
            FileUtils.touch(currentFile);
        }
        
        File retrievedFile = testArchiveFileHelper.getFinalFile(existingTempDirectory, fileName);
        
        //TODO Some Exception instead?
        
        assertNull("Retrieved file should be null when all suffixes exist already", retrievedFile);
    }
    
    @Test
    public void createFileAndDirectoriesBothNonExistingYet() throws IOException {
        
        final String fileName = "file.cmdi";
        prepareExistingTempDirectory();
        
        final File file = new File(existingTempDirectory, fileName);
        
        testArchiveFileHelper.createFileAndDirectories(file);
        
        assertTrue("Directory should have been created", existingTempDirectory.exists());
        assertTrue("File should have been created", file.exists());
    }
    
    @Test
    public void createFileAndDirectoriesFileNonExistingYet() throws IOException {
        
        final String fileName = "file.cmdi";
        prepareExistingTempDirectory();
        
        final File file = new File(existingTempDirectory, fileName);
        
        testArchiveFileHelper.createFileAndDirectories(file);
        
        assertTrue("File should have been created", file.exists());
    }
    
    @Test
    public void createFileAndDirectoriesDirectoryExistingAlready() throws IOException {
        
        final String fileName = "file.cmdi";
        prepareExistingTempDirectory();
        
        final File file = new File(existingTempDirectory, fileName);
        
        testArchiveFileHelper.createFileAndDirectories(file);
        
        assertTrue("File should have been created", file.exists());
    }
    
    @Test
    public void createFileAndDirectoriesBothExistingAlready() throws IOException {
        
        final String fileName = "file.cmdi";
        prepareExistingTempDirectory();
        
        final File file = new File(existingTempDirectory, fileName);
        FileUtils.touch(file);
        
        testArchiveFileHelper.createFileAndDirectories(file);
        
        assertTrue("File should have been created", file.exists());
    }
    
    @Test
    public void getDirectoryForCorpus() {
        
        // Since the functionality to add a top node is still not in place,
            // this means always a corpus node which is not a top node,
                // which in turn will always have its location in the same folder as the parent (Corpusstructure)
        
        final String parentPath = "/archive/root/TopNode/Corpusstructure/parent.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/Parent";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String expectedDirectory = "/archive/root/TopNode/" + corpusstructureDirectoryName;
        final URI profileSchemaURI = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712064/xsd"); //lat-corpus
        final String translateType = "corpus";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
            oneOf(mockNode).getProfileSchemaURI(); will(returnValue(profileSchemaURI));
            oneOf(mockAllowedCmdiProfiles).getProfile(profileSchemaURI.toString()); will(returnValue(mockCmdiProfile));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue(translateType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForSession_ChildOfTopNode() {
        
        final String parentPath = "/archive/root/TopNode/Corpusstructure/topnode.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String expectedDirectory = "/archive/root/TopNode/" + metadataDirectoryName;
        final URI profileSchemaURI = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035/xsd"); //lat-session
        final String translateType = "session";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
            oneOf(mockNode).getProfileSchemaURI(); will(returnValue(profileSchemaURI));
            oneOf(mockAllowedCmdiProfiles).getProfile(profileSchemaURI.toString()); will(returnValue(mockCmdiProfile));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue(translateType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForSession_ChildOfNormalNode() {
        
        final String parentPath = "/archive/root/TopNode/Corpusstructure/normalnode.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/NormalNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String expectedDirectory = "/archive/root/TopNode/NormalNode/" + metadataDirectoryName;
        final URI profileSchemaURI = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035/xsd"); //lat-session
        final String translateType = "session";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
            oneOf(mockNode).getProfileSchemaURI(); will(returnValue(profileSchemaURI));
            oneOf(mockAllowedCmdiProfiles).getProfile(profileSchemaURI.toString()); will(returnValue(mockCmdiProfile));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue(translateType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForSession_ChildOfNormalNode_SeveralLevels() {
        
        final String parentPath = "/archive/root/TopNode/Corpusstructure/normalnode.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/ChildNode/AnotherChildNode/YetAnotherChildNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String expectedDirectory = "/archive/root/TopNode/ChildNode/AnotherChildNode/YetAnotherChildNode/" + metadataDirectoryName;
        final URI profileSchemaURI = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035/xsd"); //lat-session
        final String translateType = "session";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
            oneOf(mockNode).getProfileSchemaURI(); will(returnValue(profileSchemaURI));
            oneOf(mockAllowedCmdiProfiles).getProfile(profileSchemaURI.toString()); will(returnValue(mockCmdiProfile));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue(translateType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForSession_ChildOfNormalNode_WithSpaces() {
        
        final String parentPath = "/archive/root/TopNode/Corpusstructure/normalnodewithspaces.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/Normal_Node_With_Spaces";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.METADATA;
        final String expectedDirectory = "/archive/root/TopNode/Normal_Node_With_Spaces/" + metadataDirectoryName;
        final URI profileSchemaURI = URI.create("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1407745712035/xsd"); //lat-session
        final String translateType = "session";
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
            oneOf(mockNode).getProfileSchemaURI(); will(returnValue(profileSchemaURI));
            oneOf(mockAllowedCmdiProfiles).getProfile(profileSchemaURI.toString()); will(returnValue(mockCmdiProfile));
            allowing(mockCmdiProfile).getTranslateType(); will(returnValue(translateType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForWrittenResource_WithinTopFolder() {
        
        final String parentPath = "/archive/root/TopNode/Metadata/parent.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String expectedDirectory = "/archive/root/TopNode/" + annotationsDirectoryName;
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForWrittenResource_WithinOtherFolder() {

        final String parentPath = "/archive/root/TopNode/OtherNode/Metadata/parent.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/OtherNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_WRITTEN;
        final String expectedDirectory = "/archive/root/TopNode/OtherNode/" + annotationsDirectoryName;
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForMediaResource_WithinTopFolder() {
        
        final String parentPath = "/archive/root/TopNode/Metadata/parent.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_VIDEO;
        final String expectedDirectory = "/archive/root/TopNode/" + mediaDirectoryName;
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForMediaResource_WithinOtherFolder() {
        
        final String parentPath = "/archive/root/TopNode/OtherNode/Metadata/parent.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/OtherNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_VIDEO;
        final String expectedDirectory = "/archive/root/TopNode/OtherNode/" + mediaDirectoryName;
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForInfoFile_WithinTopFolder() {
        
        final String parentPath = "/archive/root/TopNode/Metadata/parent.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_INFO;
        final String expectedDirectory = "/archive/root/TopNode/" + infoDirectoryName;
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForInfoFile_WithinOtherFolder() {
        
        final String parentPath = "/archive/root/TopNode/OtherNode/Metadata/parent.cmdi";
        final String parentCorpusNamePathToClosestTopNode = "TopNode/OtherNode";
        final WorkspaceNodeType nodeType = WorkspaceNodeType.RESOURCE_INFO;
        final String expectedDirectory = "/archive/root/TopNode/OtherNode/" + infoDirectoryName;
        
        context.checking(new Expectations() {{
            allowing(mockNode).getType(); will(returnValue(nodeType));
        }});
        
        String result = testArchiveFileHelper.getDirectoryForNode(parentPath, parentCorpusNamePathToClosestTopNode, mockNode);
        
        assertEquals("Returned directory different from expected", expectedDirectory, result);
    }
    
    @Test
    public void fileChangedDifferentSize() {
        
        final long archiveFileSize = 10;
        final long workspaceFileSize = 20;
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileInfo).getSize(); will(returnValue(archiveFileSize));
            oneOf(mockWorkspaceFile).length(); will(returnValue(workspaceFileSize));
        }});
        
        boolean result = testArchiveFileHelper.hasArchiveFileChanged(mockArchiveFileInfo, mockWorkspaceFile);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void fileChangedDifferentChecksum() {
        
        final long archiveFilesize = 10;
        final long workspaceFilesize = 10;
        
        final String archiveChecksum = "123456789";
        final String workspaceChecksum = "987654321";
        
        final String workspaceFilePath = "/workspace/path/file.cmdi";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileInfo).getSize(); will(returnValue(archiveFilesize));
            oneOf(mockWorkspaceFile).length(); will(returnValue(workspaceFilesize));
            oneOf(mockWorkspaceFile).getPath(); will(returnValue(workspaceFilePath));
            oneOf(mockArchiveFileInfo).getChecksum(); will(returnValue(archiveChecksum));
        }});
        
        stub(method(Checksum.class, "create", String.class)).toReturn(workspaceChecksum);
        
        boolean result = testArchiveFileHelper.hasArchiveFileChanged(mockArchiveFileInfo, mockWorkspaceFile);
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void fileDidNotChange() {
        
        final long archiveFilesize = 10;
        final long workspaceFilesize = 10;
        
        final String archiveChecksum = "123456789";
        final String workspaceChecksum = "123456789";
        
        final String workspaceFilePath = "/workspace/path/file.cmdi";
        
        context.checking(new Expectations() {{
            
            oneOf(mockArchiveFileInfo).getSize(); will(returnValue(archiveFilesize));
            oneOf(mockWorkspaceFile).length(); will(returnValue(workspaceFilesize));
            oneOf(mockWorkspaceFile).getPath(); will(returnValue(workspaceFilePath));
            oneOf(mockArchiveFileInfo).getChecksum(); will(returnValue(archiveChecksum));
        }});
        
        stub(method(Checksum.class, "create", String.class)).toReturn(workspaceChecksum);
        
        boolean result = testArchiveFileHelper.hasArchiveFileChanged(mockArchiveFileInfo, mockWorkspaceFile);
        
        assertFalse("Result should be false", result);
    }
    
    
    @Test
    public void getDirectoryForDeletedNode() {
        
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

        
        File result = testArchiveFileHelper.getDirectoryForDeletedNode(workspaceID);
        
        assertEquals("Target trashcan sub-directory is different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getDirectoryForReplacedNode() {
        
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
        File subDirectory = new File(versioningBaseDirectory, directoryName.toString());
        File expectedDirectory = new File(subDirectory, "" + workspaceID);

        
        File result = testArchiveFileHelper.getDirectoryForReplacedNode(workspaceID);
        
        assertEquals("Target versioning sub-directory is different from expected", expectedDirectory, result);
    }
    
    @Test
    public void getTargetFileForDeletedNode() throws MalformedURLException, URISyntaxException {
        
        final String testArchiveNodeUriStr = UUID.randomUUID().toString();
        final String testNodeArchivePath = "file:/lat/corpora/archive/node.cmdi";
        final URL testNodeArchiveURL = new URL(testNodeArchivePath);
        final File testNodeFile = new File(URI.create(testNodeArchivePath));
        final String fileBaseName = "node.cmdi";
        
        final String versionDirectoryName = "2013-05";
        final File versionFullDirectory = new File(trashCanBaseDirectory, versionDirectoryName);
        
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append("v_").append(testArchiveNodeUriStr).append("__.").append(fileBaseName);
        File expectedTargetFile = new File(versionFullDirectory, fileNameBuilder.toString());
        
        context.checking(new Expectations() {{
            
//            oneOf(mockArchiveFileHelper).getFileBasename(testNodeFile.getPath()); will(returnValue(fileBaseName));
        }});
        
        File result = testArchiveFileHelper.getTargetFileForReplacedOrDeletedNode(versionFullDirectory, testArchiveNodeUriStr, testNodeFile);
        
        assertEquals("Returned file name different from expected", expectedTargetFile, result);
    }
    
    @Test
    public void canWriteExistingTargetDirectory() throws IOException {
        
        prepareExistingTempDirectory();
        
        boolean result = testArchiveFileHelper.canWriteTargetDirectory(existingTempDirectory);
        
        assertTrue("Target directory should be writable", result);
    }
    
    @Test
    public void canWriteNonExistingTargetDirectory() throws IOException {
        
        prepareNonExistingTempDirectory();
        
        boolean result = testArchiveFileHelper.canWriteTargetDirectory(nonExistingTempDirectory);
        
        assertTrue("Target directory should have been created and be writable", result);
    }
    
    @Test
    public void cannotWriteTargetDirectory() throws IOException {
        
        prepareExistingNonWritableTempDirectory();
        
        boolean result = testArchiveFileHelper.canWriteTargetDirectory(existingNonWritableTempDirectory);
        
        assertFalse("Target directory should not be writable", result);
    }
    
    @Test
    public void targetDirectoryIsNotDirectory() throws IOException {
        
        prepareExistingTempFile();
        
        boolean result = testArchiveFileHelper.canWriteTargetDirectory(existingTempFile);
        
        assertFalse("Target directory is not a directory, therefore it should fail", result);
    }
    
    
    private void prepareExistingTempFile() throws IOException {
        existingTempFile = testFolder.newFile();
        assertTrue("Temp file wasn't created.", existingTempFile.exists());
    }
    
    private void prepareExistingTempDirectory() throws IOException {
        existingTempDirectory = testFolder.newFolder();
        assertTrue("Temp directory wasn't created.", existingTempDirectory.exists());
    }
    
    private void prepareExistingNonWritableTempDirectory() throws IOException {
        existingNonWritableTempDirectory = testFolder.newFolder();
        assertTrue("Temp directory (non-writable) wasn't created.", existingNonWritableTempDirectory.exists());
        existingNonWritableTempDirectory.setWritable(Boolean.FALSE);
        assertFalse("Temp directory (non-writable) wasn't set as non-writable", existingNonWritableTempDirectory.canWrite());
    }
    
    private void prepareNonExistingTempDirectory() throws IOException {
        File someDirectory = testFolder.newFolder();
        nonExistingTempDirectory = new File(someDirectory, "nonExistingFolder");
        assertFalse("Temp directory shouldn't have been created.", nonExistingTempDirectory.exists());
    }
}
