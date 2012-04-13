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
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.LamusWorkspace;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceDirectoryHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    @Mock private Configuration mockConfiguration;
    private WorkspaceDirectoryHandler workspaceDirectoryHandler;
    
    public LamusWorkspaceDirectoryHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        workspaceDirectoryHandler = new LamusWorkspaceDirectoryHandler(mockConfiguration);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createWorkspaceDirectory method, of class WorkspaceFilesystemUtils.
     */
    @Test
    public void workspaceDirectoryHasToBeCreated() throws FailedToCreateWorkspaceDirectoryException {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        final File baseDirectory = testFolder.newFolder("workspace_base_directory");
        File workspaceDirectory = new File(baseDirectory, "" + testWorkspace.getWorkspaceID());
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory.getAbsolutePath()));
        }});
        
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
        final File baseDirectory = testFolder.newFolder("workspace_base_directory");
        File workspaceDirectory = new File(baseDirectory, "" + testWorkspace.getWorkspaceID());
        boolean isDirectoryCreated = workspaceDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);

        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory.getAbsolutePath()));
        }});
        
        workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace);
        
        assertTrue("Workspace directory wasn't created", workspaceDirectory.exists());
    }
    
    @Test
    public void throwsExceptionWhenWorkspaceDirectoryCreationFails() throws FailedToCreateWorkspaceDirectoryException{
        
        testFolder.delete();
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        final File baseDirectory = testFolder.newFolder("workspace_base_directory");
        boolean isDirectoryCreated = baseDirectory.mkdirs();
        assertTrue("Workspace directory was not successfuly created.", isDirectoryCreated);
        baseDirectory.setWritable(false);
        File workspaceDirectory = new File(baseDirectory, "" + testWorkspace.getWorkspaceID());
        String errorMessage = "Directory for workspace " + testWorkspace.getWorkspaceID() + " could not be created";
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory.getAbsolutePath()));
        }});
        
        try {
            workspaceDirectoryHandler.createWorkspaceDirectory(testWorkspace);
            fail("Exception was not thrown");
        } catch(FailedToCreateWorkspaceDirectoryException ex) {
            assertEquals(testWorkspace, ex.getWorkspace());
            assertEquals(errorMessage, ex.getMessage());
        }

        assertFalse("Workspace directory shouldn't have been created, since there should be no permissions for that.", workspaceDirectory.exists());
    }
    
}
