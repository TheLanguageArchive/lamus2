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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.exception.FileImporterInitialisationException;
import nl.mpi.lamus.workspace.importing.implementation.MetadataFileImporter;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class WorkspaceImportRunnerTest {
    
    Synchroniser synchroniser = new Synchroniser();
    Mockery context = new JUnit4Mockery() {{
        setThreadingPolicy(synchroniser);
    }};
    
    private Runnable workspaceImportRunner;
    private final WorkspaceDao mockWorkspaceDao = context.mock(WorkspaceDao.class);
//    @Mock private Workspace mockWorkspace;
    private final Workspace mockWorkspace = context.mock(Workspace.class);
    private int topNodeArchiveID = 10;
//    @Mock private FileImporterFactory mockFileImporterFactory;
    private final FileImporterFactory mockFileImporterFactory = context.mock(FileImporterFactory.class);
//    @Mock private Class<? extends FileImporter> mockFileImporterType;
//    @Mock private FileImporter mockFileImporter;
    private final FileImporter mockFileImporter = context.mock(FileImporter.class);
    
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
        workspaceImportRunner = new WorkspaceImportRunner(mockWorkspaceDao, mockWorkspace, topNodeArchiveID, mockFileImporterFactory);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class WorkspaceImportRunner.
     */
    @Test
    public void runsSuccessfully() throws FileImporterInitialisationException, InterruptedException {
        
        final Class<? extends FileImporter> testImporterType = MetadataFileImporter.class;
        
        final States importing = context.states("importing");
        
        context.checking(new Expectations() {{
            oneOf (mockFileImporterFactory).getFileImporterTypeForTopNode();
                will(returnValue(testImporterType));
                when(importing.isNot("finished"));
            oneOf (mockFileImporterFactory).getNewFileImporterOfType(testImporterType);
                will(returnValue(mockFileImporter));
                when(importing.isNot("finished"));
            oneOf (mockFileImporter).importFile(null, topNodeArchiveID);
                then(importing.is("finished"));
        }});
        
        
        executeRunner();
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(importing.is("finished"), timeoutInMs);
    }
    
    /**
     * Test of run method, of class WorkspaceImportRunner.
     */
    @Test
    public void throwsFileImporterInitialisationException() throws FileImporterInitialisationException, InterruptedException {
        
        final Class<? extends FileImporter> expectedImporterType = MetadataFileImporter.class;
        final String expectedExceptionMessage = "this is a test message for the exception";
        final Throwable expectedExceptionCause = new NoSuchMethodException("this is a test message for the exception cause");
        
        final States importing = context.states("importing");
        
        context.checking(new Expectations() {{
            oneOf (mockFileImporterFactory).getFileImporterTypeForTopNode();
                will(returnValue(expectedImporterType));
                when(importing.isNot("finished"));
            oneOf (mockFileImporterFactory).getNewFileImporterOfType(expectedImporterType);
                will(throwException(new FileImporterInitialisationException(
                        expectedExceptionMessage, mockWorkspace, expectedImporterType, expectedExceptionCause)));
                when(importing.isNot("finished"));
            never (mockFileImporter).importFile(null, topNodeArchiveID);
            
            oneOf (mockWorkspace).setStatusMessageErrorDuringInitialisation();
            oneOf (mockWorkspaceDao).updateWorkspaceStatusMessage(mockWorkspace);
                then(importing.is("finished"));
            
            //TODO expect a call to a listener indicating failure
        }});
        
        
        executeRunner();
        
        long timeoutInMs = 2000L;
        synchroniser.waitUntil(importing.is("finished"), timeoutInMs);
    }
    
    private void executeRunner() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(workspaceImportRunner);
    }
}
