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
import nl.mpi.lamus.filesystem.WorkspaceFilesystemHandler;
import nl.mpi.lamus.workspace.LamusWorkspace;
import nl.mpi.lamus.workspace.Workspace;
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
public class LamusWorkspaceFilesystemHandlerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();
    @Mock private Configuration mockConfiguration;
    private WorkspaceFilesystemHandler workspaceFilesystemHandler;
    
    public LamusWorkspaceFilesystemHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        workspaceFilesystemHandler = new LamusWorkspaceFilesystemHandler(mockConfiguration);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createWorkspaceDirectory method, of class WorkspaceFilesystemUtils.
     */
    @Test
    public void testCreateWorkspaceDirectory() {
        
        // get path of the base directory for lamus workspaces
        // create the directory for this particular workspace (named after the workspace id)
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setWorkspaceID(1);
        final File baseDirectory = testFolder.newFolder("workspace_base_directory");
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getWorkspaceBaseDirectory(); will(returnValue(baseDirectory.getAbsolutePath()));
        }});
        
        File result = workspaceFilesystemHandler.createWorkspaceDirectory(testWorkspace);
        
        assertTrue("Workspace directory wasn't created", new File(baseDirectory, "" + testWorkspace.getWorkspaceID()).exists());
        assertNotNull("Returned workspace should not be null.", result);
    }
}
