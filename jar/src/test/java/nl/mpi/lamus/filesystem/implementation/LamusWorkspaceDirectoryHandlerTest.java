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
import nl.mpi.lamus.filesystem.LamusFilesystemTestBeans;
import nl.mpi.lamus.filesystem.LamusFilesystemTestProperties;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.WorkspaceFilesystemException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.codehaus.plexus.util.FileUtils;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusFilesystemTestProperties.class, LamusFilesystemTestBeans.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusWorkspaceDirectoryHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Autowired
    private WorkspaceDirectoryHandler workspaceDirectoryHandler;
    
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    
    @Autowired
    @Qualifier("workspaceUploadDirectoryName")
    private String workspaceUploadDirectoryName;
    
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
    public void workspaceDirectoryHasToBeCreated() throws WorkspaceFilesystemException {
        
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
    public void workspaceDirectoryAlreadyExists() throws WorkspaceFilesystemException {
        
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
    public void throwsExceptionWhenWorkspaceDirectoryCreationFails() throws WorkspaceFilesystemException{
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        this.workspaceBaseDirectory.setWritable(false);
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        String errorMessage = "Directory for workspace " + testWorkspace.getWorkspaceID() + " could not be created";
        
        try {
            this.workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace.getWorkspaceID());
            fail("Exception was not thrown");
        } catch(WorkspaceFilesystemException ex) {
            assertEquals(testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
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
    
    //TODO add more tests to the deleteWorkspaceDirectory method
    
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
    public void uploadDirectoryForWorkspaceNeedsToBeCreated() throws WorkspaceFilesystemException {
        
        int workspaceID = 1;
        File workspaceDirectory = new File(this.workspaceBaseDirectory, "" + workspaceID);
        File workspaceUploadDirectory = new File(workspaceDirectory, this.workspaceUploadDirectoryName);
        
        this.workspaceDirectoryHandler.createUploadDirectoryForWorkspace(workspaceID);
        
        assertTrue("Workspace upload directory wasn't created", workspaceUploadDirectory.exists());
    }
    
    @Test
    public void uploadDirectoryForWorkspaceAlreadyExists() throws WorkspaceFilesystemException {
        
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
        } catch(WorkspaceFilesystemException ex) {
            assertEquals(workspaceID, ex.getWorkspaceID());
            assertEquals(errorMessage, ex.getMessage());
        }

        assertFalse("Workspace upload directory shouldn't have been created,"
                + " since there should be no permissions for that.", workspaceUploadDirectory.exists());
    }
}
