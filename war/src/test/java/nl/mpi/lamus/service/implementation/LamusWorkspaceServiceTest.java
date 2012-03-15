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
package nl.mpi.lamus.service.implementation;

import java.net.MalformedURLException;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.filesystem.WorkspaceFilesystemHandler;
import nl.mpi.lamus.service.WorkspaceService;
import nl.mpi.lamus.workspace.LamusWorkspace;
import nl.mpi.lamus.workspace.NodeAccessChecker;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.WorkspaceFactory;
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
public class LamusWorkspaceServiceTest {
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    private WorkspaceService service;
    @Mock private NodeAccessChecker mockNodeAccessChecker;
    @Mock private WorkspaceFactory mockWorkspaceFactory;
    @Mock private WorkspaceDao mockWorkspaceDao;
    @Mock private WorkspaceFilesystemHandler mockWorkspaceFilesystemHandler;
    
    
    public LamusWorkspaceServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        service = new LamusWorkspaceService(mockNodeAccessChecker, mockWorkspaceFactory, mockWorkspaceDao, mockWorkspaceFilesystemHandler);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * 
     */
    @Test
    public void returnNullWorkspaceIfCannotBeCreated() throws MalformedURLException {
        
        final int archiveNodeID = 10;
        final String userID = "someUser";
        
        context.checking(new Expectations() {{
            oneOf (mockNodeAccessChecker).canCreateWorkspace(userID, archiveNodeID); will(returnValue(false));
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeID);
        assertNull("Returned workspace should be null when it cannot be created.", result);
    }
    
    /**
     * 
     */
    @Test
    public void triggersWorkspaceCreationIfCanBeCreated() throws MalformedURLException {
        
        final int archiveNodeID = 10;
        final String userID = "someUser";
        final long usedStorageSpace = 0L;
        final long maxStorageSpace = 10000000L;
        final Workspace newWorkspace = new LamusWorkspace(userID, usedStorageSpace, maxStorageSpace);
        
        context.checking(new Expectations() {{
            oneOf (mockNodeAccessChecker).canCreateWorkspace(userID, archiveNodeID); will(returnValue(true));
            //allow other calls
            oneOf (mockWorkspaceFactory).getNewWorkspace(userID, archiveNodeID); will(returnValue(newWorkspace));
            oneOf (mockWorkspaceDao).addWorkspace(newWorkspace);
            oneOf (mockWorkspaceFilesystemHandler).createWorkspaceDirectory(newWorkspace);
        }});
        
        Workspace result = service.createWorkspace(userID, archiveNodeID);
        assertNotNull("Returned workspace should not be null when it can be created", result);
    }
    
    
    
}
