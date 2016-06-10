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
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Resource;
import nl.mpi.lamus.exception.DisallowedPathException;
import nl.mpi.lamus.filesystem.LamusFilesystemTestBeans;
import nl.mpi.lamus.filesystem.LamusFilesystemTestProperties;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.exception.WorkspaceFilesystemException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.apache.commons.io.FileUtils;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author guisil
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusFilesystemTestProperties.class, LamusFilesystemTestBeans.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusWorkspaceDirectoryHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    
    @Autowired
    private WorkspaceDirectoryHandler workspaceDirectoryHandler;
    
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    @Autowired
    @Qualifier("workspaceUploadDirectoryName")
    private String workspaceUploadDirectoryName;
    
    @Autowired
    @Qualifier("orphansDirectoryName")
    private String orphansDirectoryName;
    
    @Resource
    @Qualifier("disallowedFolderNamesWorkspace")
    private Collection<String> disallowedFolderNamesWorkspace;
    
    public LamusWorkspaceDirectoryHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws IOException {
        FileUtils.cleanDirectory(this.workspaceBaseDirectory);
        this.workspaceBaseDirectory.setWritable(true);
    }
    
    @After
    public void tearDown() throws IOException {
        FileUtils.cleanDirectory(this.workspaceBaseDirectory);
    }

    /**
     * Test of createWorkspaceDirectory method, of class WorkspaceFilesystemUtils.
     */
    @Test
    public void workspaceDirectoryHasToBeCreated() throws IOException {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        
        this.workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace.getWorkspaceID());
        
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
    }
    
    /**
     * Test of createWorkspaceDirectory method, of class WorkspaceFilesystemUtils.
     */
    @Test
    public void workspaceDirectoryAlreadyExists() throws IOException {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);

        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        
        this.workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace.getWorkspaceID());
        
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
    }
    
    @Test
    public void throwsExceptionWhenWorkspaceDirectoryCreationFails() throws IOException {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        this.workspaceBaseDirectory.setWritable(false);
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        String errorMessage = "Directory for workspace " + testWorkspace.getWorkspaceID() + " could not be created";
        
        try {
            this.workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace.getWorkspaceID());
            fail("Exception was not thrown");
        } catch(IOException ex) {
            assertEquals(errorMessage, ex.getMessage());
        }

        assertFalse("Workspace directory shouldn't have been created, since there should be no permissions for that.", workspaceDirectory.exists());
    }
    
    @Test
    public void workspaceEmptyDirectoryDeleted() throws IOException {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);

        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        
        this.workspaceDirectoryHandler.deleteWorkspaceDirectory(workspaceID);
        
        assertFalse("Workspace directory wasn't deleted", workspaceDirectory.exists());
    }
    
    @Test
    public void workspaceNonEmptyDirectoryDeleted() throws IOException {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File innerDirectory = new File(workspaceDirectory, "0");
        boolean areDirectoriesCreated = innerDirectory.mkdirs();
        assertTrue("Workspace directories were not successfuly created.", areDirectoriesCreated);
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        assertTrue("Workspace inner directory weasn't created", innerDirectory.exists());
        
        this.workspaceDirectoryHandler.deleteWorkspaceDirectory(workspaceID);
        
        assertFalse("Workspace inner directory wasn't deleted", innerDirectory.exists());
        assertFalse("Workspace directory wasn't deleted", workspaceDirectory.exists());
    }
    
    @Test
    public void workspaceDirectoryExists() {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);

        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        
        boolean result = this.workspaceDirectoryHandler.workspaceDirectoryExists(testWorkspace);

        assertTrue("Workspace directory wasn't created", result);

    }
    
    @Test
    public void workspaceDirectoryDoesNotExist() {
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        boolean isDirectoryCreated = false;
        assertFalse("Workspace directory was not supposed to be created.", isDirectoryCreated);

        assertFalse("Workspace directory wasn't supposed to be created", workspaceDirectory.exists());
        
        boolean result = this.workspaceDirectoryHandler.workspaceDirectoryExists(testWorkspace);
        
        assertFalse("Workspace directory wasn't supposed to be created", workspaceDirectory.exists());
    }
    
    @Test
    public void getDirectoryForWorkspace() {
        
        int workspaceID = 1;
        File expectedDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File retrievedDirectory = this.workspaceDirectoryHandler.getDirectoryForWorkspace(workspaceID);
        
        assertEquals("Retrieved directory different from expected", expectedDirectory, retrievedDirectory);
    }
    
    @Test
    public void getUploadDirectoryForWorkspace() {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File expectedDirectory = new File(workspaceDirectory, "upload");
        File retrievedDirectory = this.workspaceDirectoryHandler.getUploadDirectoryForWorkspace(workspaceID);
        
        assertEquals("Retrieved directory different from expected", expectedDirectory, retrievedDirectory);
    }
    
    @Test
    public void getOrphansDirectoryInWorkspace() {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File expectedDirectory = new File(workspaceDirectory, orphansDirectoryName);
        File retrievedDirectory = this.workspaceDirectoryHandler.getOrphansDirectoryInWorkspace(workspaceID);
        
        assertEquals("Retrieved directory different from expected", expectedDirectory, retrievedDirectory);
    }
    
    @Test
    public void uploadDirectoryForWorkspaceNeedsToBeCreated() throws IOException {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File workspaceUploadDirectory = new File(workspaceDirectory, this.workspaceUploadDirectoryName);
        
        this.workspaceDirectoryHandler.createUploadDirectoryForWorkspace(workspaceID);
        
        assertTrue("Workspace upload directory wasn't created", workspaceUploadDirectory.exists());
    }
    
    @Test
    public void uploadDirectoryForWorkspaceAlreadyExists() throws IOException {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File workspaceUploadDirectory = new File(workspaceDirectory, this.workspaceUploadDirectoryName);
        boolean isDirectoryCreated = workspaceUploadDirectory.mkdirs();
        assertTrue("Workspace upload directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Workspace upload directory wasn't created", workspaceUploadDirectory.exists());
        
        this.workspaceDirectoryHandler.createUploadDirectoryForWorkspace(workspaceID);
        
        assertTrue("Workspace upload directory wasn't created", workspaceUploadDirectory.exists());
    }
    
    @Test
    public void throwsExceptionWhenUploadDirectoryForWorkspaceCreationFails() throws WorkspaceFilesystemException{
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        workspaceDirectory.setWritable(false);
        File workspaceUploadDirectory = new File(workspaceDirectory, this.workspaceUploadDirectoryName);

        String errorMessage = "Upload directory for workspace " + workspaceID + " could not be created";
        
        try {
            this.workspaceDirectoryHandler.createUploadDirectoryForWorkspace(workspaceID);
            fail("Exception was not thrown");
        } catch(IOException ex) {
            assertEquals(errorMessage, ex.getMessage());
        }

        assertFalse("Workspace upload directory shouldn't have been created,"
                + " since there should be no permissions for that.", workspaceUploadDirectory.exists());
    }
    
    @Test
    public void orphansDirectoryInWorkspaceNeedsToBeCreated() throws IOException {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File workspaceOrphansDirectory = new File(workspaceDirectory, this.orphansDirectoryName);
        
        this.workspaceDirectoryHandler.createOrphansDirectoryInWorkspace(workspaceID);
        
        assertTrue("Workspace orphans directory wasn't created", workspaceOrphansDirectory.exists());
    }
    
    @Test
    public void orphansDirectoryInWorkspaceAlreadyExists() throws IOException {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File workspaceOrphansDirectory = new File(workspaceDirectory, this.orphansDirectoryName);
        boolean isDirectoryCreated = workspaceOrphansDirectory.mkdirs();
        assertTrue("Workspace orphans directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Workspace orphans directory wasn't created", workspaceOrphansDirectory.exists());
        
        this.workspaceDirectoryHandler.createOrphansDirectoryInWorkspace(workspaceID);
        
        assertTrue("Workspace orphans directory wasn't created", workspaceOrphansDirectory.exists());
    }
    
    @Test
    public void throwsExceptionWhenOrphansDirectoryInWorkspaceCreationFails() throws WorkspaceFilesystemException{
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        workspaceDirectory.setWritable(false);
        File workspaceOrphansDirectory = new File(workspaceDirectory, this.orphansDirectoryName);

        String errorMessage = "Orphans directory in workspace " + workspaceID + " could not be created";
        
        try {
            this.workspaceDirectoryHandler.createOrphansDirectoryInWorkspace(workspaceID);
            fail("Exception was not thrown");
        } catch(IOException ex) {
            assertEquals(errorMessage, ex.getMessage());
        }

        assertFalse("Workspace orphans directory shouldn't have been created,"
                + " since there should be no permissions for that.", workspaceOrphansDirectory.exists());
    }
    
    @Test
    public void createDirectoryInWorkspace_Allowed() throws IOException {
        
        int workspaceID = 1;
        String intendedDirectoryName = "someDirectory/";
        
        File workspaceDirectory = createTestWorkspaceDirectory(workspaceID);
        File uploadDirectory = createTestWorkspaceUploadDirectory(workspaceDirectory);
        File expectedDirectory = new File(uploadDirectory, intendedDirectoryName);
        
        this.workspaceDirectoryHandler.createDirectoryInWorkspace(workspaceID, intendedDirectoryName);
        
        assertTrue("Directory '" + expectedDirectory.getName() + "' should have been created in the workspace upload directory", expectedDirectory.exists());
    }
    
    @Test
    public void pathIsAllowed_oneLevel() throws DisallowedPathException {
        
        final String path = "file.txt";
        workspaceDirectoryHandler.ensurePathIsAllowed(path);
    }
    
    @Test
    public void pathIsAllowed_twoLevels() throws DisallowedPathException {
        
        final String path = "folder/file.txt";
        workspaceDirectoryHandler.ensurePathIsAllowed(path);
    }
    
    @Test
    public void pathIsNotAllowed_oneLevel() {
        
        assertDisallowedPathException(".svn", ".svn");
        
        assertDisallowedPathException("DesktopFolderDB", "DesktopFolderDB");
        
        assertDisallowedPathException("temp", "temp");
        
        assertDisallowedPathException("tmp", "tmp");
    }
    
    @Test
    public void pathIsNotAllowed_twoLevels() {
        
        assertDisallowedPathException(".svn", ".svn/file.txt");
        
        assertDisallowedPathException("DesktopFolderDB", "DesktopFolderDB/file.txt");
        
        assertDisallowedPathException("temp", "temp/file.txt");
        
        assertDisallowedPathException("tmp", "tmp/file.txt");
    }
    
    
    private File createTestWorkspaceDirectory(int workspaceID) {
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + workspaceID);
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Workspace directory wasn't created.", workspaceDirectory.exists());
        
        return workspaceDirectory;
    }
    
    private File createTestWorkspaceUploadDirectory(File workspaceDirectory) {
        File uploadDirectory = new File(workspaceDirectory, workspaceUploadDirectoryName);
        boolean isDirectoryCreated = uploadDirectory.mkdirs();
        
        assertTrue("Upload directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Upload directory wasn't created.", uploadDirectory.exists());
        
        return uploadDirectory;
    }
    
    private File createTestDirectoryInUploadDirectory(File uploadDirectory, String directoryName) {
        File testDirectory = new File(uploadDirectory, directoryName);
        boolean isDirectoryCreated = testDirectory.mkdirs();
        
        assertTrue("Test directory was not successfuly created.", isDirectoryCreated);
        assertTrue("Test directory wasn't created.", testDirectory.exists());
        
        return testDirectory;
    }
    
    private void assertDisallowedPathException(String disallowedName, String path) {
        
        String expectedMessage = "The path [" + path + "] contains a disallowed file/folder name (" + disallowedName + ")";
        try {
            workspaceDirectoryHandler.ensurePathIsAllowed(path);
            fail("should have thrown exception");
        } catch(DisallowedPathException ex) {
            assertEquals("Exception problematic path different from expected", path, ex.getProblematicPath());
            assertEquals("Exception message different from expected", expectedMessage, ex.getMessage());
        }
    }
}
