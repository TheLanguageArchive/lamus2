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
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
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
        FileUtils.cleanDirectory(workspaceBaseDirectory);
        workspaceBaseDirectory.setWritable(true);
    }
    
    @After
    public void tearDown() throws IOException {
        FileUtils.cleanDirectory(workspaceBaseDirectory);
    }

    /**
     * Test of createWorkspaceDirectory method, of class WorkspaceFilesystemUtils.
     */
    @Test
    public void workspaceDirectoryHasToBeCreated() throws FailedToCreateWorkspaceDirectoryException {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        
        workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace);
        
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
    }
    
    /**
     * Test of createWorkspaceDirectory method, of class WorkspaceFilesystemUtils.
     */
    @Test
    public void workspaceDirectoryAlreadyExists() throws FailedToCreateWorkspaceDirectoryException {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);

        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        
        workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace);
        
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
    }
    
    @Test
    public void throwsExceptionWhenWorkspaceDirectoryCreationFails() throws FailedToCreateWorkspaceDirectoryException{
        
//        testFolder.delete();
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        workspaceBaseDirectory.setWritable(false);
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        String errorMessage = "Directory for workspace " + testWorkspace.getWorkspaceID() + " could not be created";
        
        try {
            workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace);
            fail("Exception was not thrown");
        } catch(FailedToCreateWorkspaceDirectoryException ex) {
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(errorMessage, ex.getMessage());
        }

        assertFalse("Workspace directory shouldn't have been created, since there should be no permissions for that.", workspaceDirectory.exists());
    }
    
    @Test
    public void workspaceDirectoryExists() {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);

        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        
        boolean result = workspaceDirectoryHandler.workspaceDirectoryExists(testWorkspace);

        assertTrue("Workspace directory wasn't created", result);

    }
    
    @Test public void workspaceDirectoryDoesNotExist() {
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        File workspaceDirectory = new File(workspaceBaseDirectory, "" + testWorkspace.getWorkspaceID());
        boolean isDirectoryCreated = false;
        assertFalse("Workspace directory was not supposed to be created.", isDirectoryCreated);

        assertFalse("Workspace directory wasn't supposed to be created", workspaceDirectory.exists());
        
        boolean result = workspaceDirectoryHandler.workspaceDirectoryExists(testWorkspace);
        
        assertFalse("Workspace directory wasn't supposed to be created", workspaceDirectory.exists());
    }
    
}
