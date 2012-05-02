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
package nl.mpi.lamus.typechecking.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.configuration.Configuration;
import nl.mpi.lamus.typechecking.FileTypeFactory;
import nl.mpi.lamus.typechecking.FileTypeHandler;
import nl.mpi.lamus.typechecking.FileTypeHandlerFactory;
import nl.mpi.lamus.workspace.model.TypeMapper;
import nl.mpi.lamus.workspace.model.Workspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusFileTypeHandlerFactoryTest {
    
    public @Rule JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private FileTypeHandlerFactory factory;
    @Mock Configuration mockConfiguration;
    @Mock Workspace mockWorkspace;
    @Mock FileTypeFactory mockFileTypeFactory;
    @Mock TypeMapper mockTypeMapper;
    @Mock FileType mockTypeCheckerWithConfigFile;
    @Mock FileType mockDefaultTypeChecker;
    private URL testArchiveURL;
    private String testArchiveURLStr = "file:/some/test/path";
    private String testArchivePath = "/some/test/path";
    
    public LamusFileTypeHandlerFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws MalformedURLException {
        
        factory = new LamusFileTypeHandlerFactory(mockConfiguration, mockFileTypeFactory, mockTypeMapper);
        testArchiveURL = new URL(testArchiveURLStr);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNewFileTypeHandlerForWorkspace method, of class LamusFileTypeHandlerFactory.
     */
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithNullRelaxedTypeCheckFolders() {
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getRelaxedTypeCheckFolders(); will(returnValue(null));
            oneOf (mockFileTypeFactory).getNewFileTypeWithDefaultConfigFile(); will(returnValue(mockDefaultTypeChecker));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockDefaultTypeChecker, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithEmptyRelaxedTypeCheckFolders() {
        
        final Collection<File> emptyRelaxedTypeCheckFolders = new ArrayList<File>();
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getRelaxedTypeCheckFolders(); will(returnValue(emptyRelaxedTypeCheckFolders));
            oneOf (mockFileTypeFactory).getNewFileTypeWithDefaultConfigFile(); will(returnValue(mockDefaultTypeChecker));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockDefaultTypeChecker, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithRelaxedTypeCheckFoldersMatchingWorkspaceURL() {
        
        final Collection<File> relaxedTypeCheckFolders = new ArrayList<File>();
        relaxedTypeCheckFolders.add(new File(testArchivePath));
        final File relaxedTypeCheckConfigFile = new File("someother_filetypes.txt");
        
        context.checking(new Expectations() {{
            oneOf (mockConfiguration).getRelaxedTypeCheckFolders(); will(returnValue(relaxedTypeCheckFolders));
            exactly(2).of (mockWorkspace).getTopNodeArchiveURL(); will(returnValue(testArchiveURL));
            oneOf (mockConfiguration).getRelaxedTypeCheckConfigFile(); will(returnValue(relaxedTypeCheckConfigFile));
            oneOf (mockFileTypeFactory).getNewFileTypeWithConfigFile(relaxedTypeCheckConfigFile); will(returnValue(mockTypeCheckerWithConfigFile));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockTypeCheckerWithConfigFile, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    //TODO test when URL is null
}
