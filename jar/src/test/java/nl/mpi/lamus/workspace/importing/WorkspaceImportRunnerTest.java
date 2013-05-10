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
package nl.mpi.lamus.workspace.importing;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.exception.NodeExplorerException;
import nl.mpi.lamus.workspace.exception.NodeImporterException;
import nl.mpi.lamus.workspace.importing.implementation.NodeImporterFactoryBean;
import nl.mpi.lamus.workspace.importing.implementation.MetadataNodeImporter;
import nl.mpi.lamus.workspace.importing.implementation.TopNodeImporter;
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
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceImportRunnerTest {
    
    Synchroniser synchroniser = new Synchroniser();
    Mockery context = new JUnit4Mockery() {{
        setThreadingPolicy(synchroniser);
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private WorkspaceImportRunner workspaceImportRunner;
    private final WorkspaceDao mockWorkspaceDao = context.mock(WorkspaceDao.class);
    private final WorkspaceNodeExplorer mockWorkspaceFileExplorer = context.mock(WorkspaceNodeExplorer.class);
    private final TopNodeImporter mockTopNodeImporter = context.mock(TopNodeImporter.class);
    private final NodeImporterFactoryBean mockFileImporterFactoryBean = context.mock(NodeImporterFactoryBean.class);
    
    private final Workspace mockWorkspace = context.mock(Workspace.class);
    private int topNodeArchiveID = 10;

    public WorkspaceImportRunnerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        workspaceImportRunner = new WorkspaceImportRunner(mockWorkspaceDao, mockWorkspaceFileExplorer,
                mockFileImporterFactoryBean, mockTopNodeImporter);
        workspaceImportRunner.setWorkspace(mockWorkspace);
        workspaceImportRunner.setTopNodeArchiveID(topNodeArchiveID);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class WorkspaceImportRunner.
     */
    @Test
    public void runsSuccessfully() throws Exception {
        
        final States importing = context.states("importing");
        
        context.checking(new Expectations() {{
            
            oneOf(mockTopNodeImporter).setWorkspace(mockWorkspace);
                when(importing.isNot("finished"));
            oneOf(mockTopNodeImporter).importNode(topNodeArchiveID);
                when(importing.isNot("finished"));
            oneOf(mockWorkspace).setStatusMessageInitialised();
                when(importing.isNot("finished"));
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
                then(importing.is("finished"));
        }});
        
        
        boolean result = executeRunner();
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(importing.is("finished"), timeoutInMs);
        
        assertTrue("Execution result should have been successful (true)", result);
    }
    
    /**
     * Test of run method, of class WorkspaceImportRunner.
     */
//    @Test
//    public void throwsException() throws Exception {
//        
//        final String someExceptionMessage = "some exception message";
//        final Exception expectedExceptionCause = new Exception(someExceptionMessage);
//        
//        final States importing = context.states("importing");
//        
//        context.checking(new Expectations() {{
//
//            oneOf (mockFileImporterFactoryBean).setNodeImporterTypeForReference(null);
//            oneOf (mockFileImporterFactoryBean).getObject(); will(throwException(expectedExceptionCause));
//            oneOf (mockTopNodeImporter).setWorkspace(mockWorkspace);
//                when(importing.isNot("finished"));
//            never (mockTopNodeImporter).importNode(null, null, null, topNodeArchiveID);
//            oneOf (mockWorkspace).setStatusMessageErrorDuringInitialisation();
//            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
//                then(importing.is("finished"));
//            
//            //TODO expect a call to a listener indicating failure
//        }});
//        
//        try {
//            executeRunner();
//            fail("An exception should have been thrown");
//        } catch(ExecutionException ex) {
//            assertNotNull(ex);
//            assertEquals("Exception cause different from expected", expectedExceptionCause, ex.getCause());
//        }
//        
//        long timeoutInMs = 2000L;
//        synchroniser.waitUntil(importing.is("finished"), timeoutInMs);
//    }
    
    /**
     * Test of run method, of class WorkspaceImportRunner.
     */
    @Test
    public void throwsFileImporterException() throws Exception {
        
        final Class<? extends NodeImporter> expectedImporterType = MetadataNodeImporter.class;
        final String expectedExceptionMessage = "this is a test message for the exception";
        final Throwable expectedExceptionCauseCause = new MetadataException("this is a test message for the exception cause");
        final Exception expectedExceptionCause = new NodeImporterException(expectedExceptionMessage, mockWorkspace, expectedImporterType, expectedExceptionCauseCause);
        
        final States importing = context.states("importing");
        
        context.checking(new Expectations() {{

            oneOf(mockTopNodeImporter).setWorkspace(mockWorkspace);
                when(importing.isNot("finished"));
            oneOf(mockTopNodeImporter).importNode(topNodeArchiveID);
                will(throwException(expectedExceptionCause));
            
            oneOf (mockWorkspace).setStatusMessageErrorDuringInitialisation();
                when(importing.isNot("finished"));
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
                then(importing.is("finished"));
            
            //TODO expect a call to a listener indicating failure
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
    
    /**
     * Test of run method, of class WorkspaceImportRunner.
     */
    @Test
    public void throwsFileExplorerException() throws Exception {
        
        final String expectedExceptionMessage = "this is a test message for the exception";
        final Throwable expectedExceptionCauseCause = null;
        final Exception expectedExceptionCause = new NodeExplorerException(expectedExceptionMessage, mockWorkspace, expectedExceptionCauseCause);
        
        final States importing = context.states("importing");
        
        context.checking(new Expectations() {{

            oneOf(mockTopNodeImporter).setWorkspace(mockWorkspace);
                when(importing.isNot("finished"));
            oneOf(mockTopNodeImporter).importNode(topNodeArchiveID);
                will(throwException(expectedExceptionCause));
            
            oneOf(mockWorkspace).setStatusMessageErrorDuringInitialisation();
            oneOf(mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
                then(importing.is("finished"));
            
            //TODO expect a call to a listener indicating failure
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
        
//        TaskExecutor executor = new SimpleAsyncTaskExecutor();
//        executor.execute(workspaceImportRunner);
        
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executorService.submit(workspaceImportRunner);
        return result.get();
    }
}
