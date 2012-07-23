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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import nl.mpi.bcarchive.typecheck.FileType;
import nl.mpi.lamus.typechecking.*;
import nl.mpi.lamus.workspace.model.TypeMapper;
import nl.mpi.lamus.workspace.model.Workspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusTypecheckingTestProperties.class, LamusTypecheckingTestBeans.class},
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusFileTypeHandlerFactoryTest {
    
    public @Rule JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    @Autowired
    private FileTypeHandlerFactory factory;
    @Resource
    @Qualifier("customTypecheckerFolderToConfigFileMap")
    private Map<String, String> customTypecheckerFolderToConfigFileMap;
    
    private Map<String, String> mapBackup;
    
    @Mock Workspace mockWorkspace;
    @Mock FileTypeFactory mockFileTypeFactory;
    @Mock TypeMapper mockTypeMapper;
    @Mock FileType mockTypeCheckerWithConfigFile;
    @Mock FileType mockDefaultTypeChecker;
    private URL testMatchingArchiveURL;
    private URL testSubArchiveURL;
    private String testMatchingArchiveURLStr = "file:/some/test/path";
    private String testSubArchiveURLStr = "file:/some/test/path/with/more/levels";
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
        
        mapBackup = customTypecheckerFolderToConfigFileMap;
        
        ReflectionTestUtils.setField(factory, "fileTypeFactory", mockFileTypeFactory);
        ReflectionTestUtils.setField(factory, "typeMapper", mockTypeMapper);
        
        testMatchingArchiveURL = new URL(testMatchingArchiveURLStr);
        testSubArchiveURL = new URL(testSubArchiveURLStr);
    }
    
    @After
    public void tearDown() {
        ReflectionTestUtils.setField(factory, "customTypecheckerFolderToConfigFileMap", mapBackup);
    }

    /**
     * Test of getNewFileTypeHandlerForWorkspace method, of class LamusFileTypeHandlerFactory.
     */
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithNullMap() {
        
        ReflectionTestUtils.setField(factory, "customTypecheckerFolderToConfigFileMap", null);
        
        context.checking(new Expectations() {{
            oneOf (mockFileTypeFactory).getNewFileTypeWithDefaultConfigFile(); will(returnValue(mockDefaultTypeChecker));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockDefaultTypeChecker, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithEmptyMap() {
        
        Map<File, File> emptyMap = new HashMap<File, File>();
        ReflectionTestUtils.setField(factory, "customTypecheckerFolderToConfigFileMap", emptyMap);
        
        context.checking(new Expectations() {{
            oneOf (mockFileTypeFactory).getNewFileTypeWithDefaultConfigFile(); will(returnValue(mockDefaultTypeChecker));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockDefaultTypeChecker, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithNullTopNodeURL() {
        
        final Collection<File> relaxedTypeCheckFolders = new ArrayList<File>();
        relaxedTypeCheckFolders.add(new File(testArchivePath));
        
        context.checking(new Expectations() {{
            oneOf (mockWorkspace).getTopNodeArchiveURL(); will(returnValue(null));
            oneOf (mockFileTypeFactory).getNewFileTypeWithDefaultConfigFile(); will(returnValue(mockDefaultTypeChecker));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockDefaultTypeChecker, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithMapContainingMatchingWorkspaceURL() {
        
        Map<String, String> map = new HashMap<String, String>();
//        File matchingFolder = new File(testArchivePath);
        final String configFileStr = "someother_filetypes.txt";
        final File configFile = new File(configFileStr);
        map.put(testArchivePath, configFileStr);
        ReflectionTestUtils.setField(factory, "customTypecheckerFolderToConfigFileMap", map);
        
        context.checking(new Expectations() {{
            exactly(2).of (mockWorkspace).getTopNodeArchiveURL(); will(returnValue(testMatchingArchiveURL));
            oneOf (mockFileTypeFactory).getNewFileTypeWithConfigFile(configFile); will(returnValue(mockTypeCheckerWithConfigFile));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockTypeCheckerWithConfigFile, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithMapContainingParentOfWorkspaceURL() {
        
        Map<String, String> map = new HashMap<String, String>();
//        File matchingFolder = new File(testArchivePath);
        final String configFileStr = "someother_filetypes.txt";
        final File configFile = new File(configFileStr);
        map.put(testArchivePath, configFileStr);
        ReflectionTestUtils.setField(factory, "customTypecheckerFolderToConfigFileMap", map);
        
        context.checking(new Expectations() {{
            exactly(2).of (mockWorkspace).getTopNodeArchiveURL(); will(returnValue(testSubArchiveURL));
            oneOf (mockFileTypeFactory).getNewFileTypeWithConfigFile(configFile); will(returnValue(mockTypeCheckerWithConfigFile));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockTypeCheckerWithConfigFile, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
    
    @Test
    public void getNewFileTypeHandlerForWorkspaceWithRetrievedTypeCheckerNull() {
        
        Map<String, String> map = new HashMap<String, String>();
//        File matchingFolder = new File(testArchivePath);
        final String configFileStr = "someother_filetypes.txt";
        final File configFile = new File(configFileStr);
        map.put(testArchivePath, configFileStr);
        ReflectionTestUtils.setField(factory, "customTypecheckerFolderToConfigFileMap", map);
        
        context.checking(new Expectations() {{
            exactly(2).of (mockWorkspace).getTopNodeArchiveURL(); will(returnValue(testSubArchiveURL));
            oneOf (mockFileTypeFactory).getNewFileTypeWithConfigFile(configFile); will(returnValue(null));
            oneOf (mockFileTypeFactory).getNewFileTypeWithDefaultConfigFile(); will(returnValue(mockDefaultTypeChecker));
        }});
        
        FileTypeHandler retrievedFileTypeHandler = factory.getNewFileTypeHandlerForWorkspace(mockWorkspace);
        
        assertNotNull("FileTypeHandler should not be null", retrievedFileTypeHandler);
        assertEquals("Type checker (FileType object) different from expected", mockDefaultTypeChecker, retrievedFileTypeHandler.getConfiguredTypeChecker());
    }
}
