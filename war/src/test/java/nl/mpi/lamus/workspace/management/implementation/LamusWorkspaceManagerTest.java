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
package nl.mpi.lamus.workspace.management.implementation;

import java.util.concurrent.Executor;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceDirectoryHandler;
import nl.mpi.lamus.workspace.exception.FailedToCreateWorkspaceDirectoryException;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.importing.WorkspaceImportRunner;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.management.implementation.LamusWorkspaceManager;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceManagerTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private WorkspaceManager manager;
    @Mock private Executor mockExecutor;
    @Mock private WorkspaceFactory mockWorkspaceFactory;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceDirectoryHandler mockWorkspaceDirectoryHandler;
    
    public LamusWorkspaceManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        this.manager = new LamusWorkspaceManager(mockExecutor, mockWorkspaceFactory, mockWorkspaceDao, mockWorkspaceDirectoryHandler);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createWorkspace method, of class LamusWorkspaceManager.
     */
    @Test
    public void testCreateWorkspace() throws FailedToCreateWorkspaceDirectoryException {
        final int archiveNodeID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeID); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace);
            oneOf (mockExecutor).execute(with(any(WorkspaceImportRunner.class)));
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeID);
        assertNotNull("Returned workspace should not be null when object, database and directory are successfully created.", result);
    }
    
    /**
     * Test of createWorkspace method, of class LamusWorkspaceManager.
     */
    @Test
    public void creationOfWorkspaceDirectoryFails() throws FailedToCreateWorkspaceDirectoryException {
        final int archiveNodeID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        final String errorMessage = "Directory for workspace " + newWorkspace.getWorkspaceID() + " could not be created";
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeID); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceDirectoryHandler).createWorkspaceDirectory(newWorkspace); will(throwException(new FailedToCreateWorkspaceDirectoryException(errorMessage, newWorkspace, null)));
        }});
        
        Workspace result = manager.createWorkspace(userID, archiveNodeID);
        assertNull("Returned workspace should be null when the directory creation fails.", result);
    }
}
