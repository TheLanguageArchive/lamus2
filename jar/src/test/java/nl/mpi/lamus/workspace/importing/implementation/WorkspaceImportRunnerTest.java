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
package nl.mpi.lamus.workspace.importing.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceImportException;
import nl.mpi.lamus.workspace.importing.NodeImporter;
import nl.mpi.lamus.workspace.importing.OrphanNodesImportHandler;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.metadata.api.MetadataException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author guisil
 */
public class WorkspaceImportRunnerTest {
    
    Synchroniser synchroniser = new Synchroniser();
    Mockery context = new JUnit4Mockery() {{
        setThreadingPolicy(synchroniser);
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceImportRunner workspaceImportRunner;
    private final WorkspaceDao mockWorkspaceDao = context.mock(WorkspaceDao.class);
    private final TopNodeImporter mockTopNodeImporter = context.mock(TopNodeImporter.class);
    private final OrphanNodesImportHandler mockOrphanNodesImportHandler = context.mock(OrphanNodesImportHandler.class);
    
    private final Workspace mockWorkspace = context.mock(Workspace.class, "initialWorkspace");
    private final Workspace mockUpdatedWorkspace = context.mock(Workspace.class, "updatedWorkspace");
    private final URI topNodeArchiveURI;
    private final int workspaceID = 1;

    public WorkspaceImportRunnerTest() throws URISyntaxException {
        topNodeArchiveURI = new URI(UUID.randomUUID().toString());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        workspaceImportRunner = new WorkspaceImportRunner(mockWorkspaceDao, mockTopNodeImporter, mockOrphanNodesImportHandler);
        workspaceImportRunner.setWorkspace(mockWorkspace);
        workspaceImportRunner.setTopNodeArchiveURI(topNodeArchiveURI);
    }
    
    @After
    public void tearDown() {
    }


    @Test
    public void workspaceNotSet() throws InterruptedException {
        
        workspaceImportRunner.setWorkspace(null);
        final String expectedMessage = "Workspace not set";
        
        try {
            executeRunner();
            fail("should have thrown an exception");
        } catch(ExecutionException ex) {
            //expected exception thrown
            assertTrue("Exception cause different from expected", ex.getCause() instanceof IllegalStateException);
            assertEquals("Exception message different from expected", expectedMessage, ex.getCause().getMessage());
        }
    }
    
    @Test
    public void topNodeNotSet() throws InterruptedException {
        
        workspaceImportRunner.setTopNodeArchiveURI(null);
        final String expectedMessage = "Top node URI not set";
        
        try {
            executeRunner();
            fail("should have thrown an exception");
        } catch(ExecutionException ex) {
            //expected exception thrown
            assertTrue("Exception cause different from expected", ex.getCause() instanceof IllegalStateException);
            assertEquals("Exception message different from expected", expectedMessage, ex.getCause().getMessage());
        }
    }
    
    @Test
    public void runsSuccessfully() throws Exception {
        
        final States importing = context.states("importing");
        final Collection<ImportProblem> problems = new ArrayList<>();
        
        context.checking(new Expectations() {{

            allowing(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
                when(importing.isNot("finished"));
            
            oneOf(mockTopNodeImporter).importNode(mockWorkspace, topNodeArchiveURI);
                when(importing.isNot("finished"));
            oneOf(mockWorkspace).setStatusMessageInitialised();
                when(importing.isNot("finished"));
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
                when(importing.isNot("finished"));
            oneOf(mockWorkspaceDao).getWorkspace(workspaceID); will(returnValue(mockUpdatedWorkspace));
                when(importing.isNot("finished"));
            oneOf(mockOrphanNodesImportHandler).exploreOrphanNodes(mockUpdatedWorkspace); will(returnValue(problems));
                then(importing.is("finished"));
        }});
        
        
        boolean result = executeRunner();
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(importing.is("finished"), timeoutInMs);
        
        assertTrue("Execution result should have been successful (true)", result);
    }
    
    @Test
    public void throwsWorkspaceImportException() throws Exception {
        
        final Class<? extends NodeImporter> expectedImporterType = MetadataNodeImporter.class;
        final String expectedExceptionMessage = "this is a test message for the exception";
        final Throwable expectedExceptionCauseCause = new MetadataException("this is a test message for the exception cause");
        final Exception expectedExceptionCause =
                new WorkspaceImportException(expectedExceptionMessage, workspaceID, expectedExceptionCauseCause);
        
        final States importing = context.states("importing");
        
        context.checking(new Expectations() {{

            oneOf(mockWorkspace).getWorkspaceID(); will(returnValue(workspaceID));
                when(importing.isNot("finished"));
            
            oneOf(mockTopNodeImporter).importNode(mockWorkspace, topNodeArchiveURI);
                will(throwException(expectedExceptionCause));
            
            oneOf (mockWorkspace).setStatusMessageErrorDuringInitialisation();
                when(importing.isNot("finished"));
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
                then(importing.is("finished"));
        }});
        
        try {
            executeRunner();
            fail("An exception should have been thrown");
        } catch(ExecutionException ex) {
            assertNotNull(ex);
            assertEquals("Exception cause different from expected", expectedExceptionCause, ex.getCause());
        }
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(importing.is("finished"), timeoutInMs);
    }
    
    
    private boolean executeRunner() throws InterruptedException, ExecutionException {
        
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executorService.submit(workspaceImportRunner);
        return result.get();
    }
}
