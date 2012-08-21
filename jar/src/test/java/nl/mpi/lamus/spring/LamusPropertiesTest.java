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
package nl.mpi.lamus.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import nl.mpi.lamus.spring.LamusProperties;
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

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LamusProperties.class}, loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("production")
public class LamusPropertiesTest {
    
    @Autowired
    @Qualifier("defaultMaxStorageSpaceInBytes")
    private long defaultMaxStorageSpaceInBytes;
    @Autowired
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastSession")
    private int numberOfDaysOfInactivityAllowedSinceLastSession;
    @Autowired
    @Qualifier("totalNumberOfDaysAllowedUntilExpiry")
    private int totalNumberOfDaysAllowedUntilExpiry;
    @Autowired
    @Qualifier("numberOfDaysOfInactivityAllowedSinceLastWarningEmail")
    private int numberOfDaysOfInactivityAllowedSinceLastWarningEmail;
    @Autowired
    @Qualifier("typeRecheckSizeLimitInBytes")
    private long typeRecheckSizeLimitInBytes;
    @Autowired
    @Qualifier("maxDirectoryNameLength")
    private int maxDirectoryNameLength;
    @Autowired
    @Qualifier("corpusDirectoryBaseName")
    private String corpusDirectoryBaseName;
    @Autowired
    @Qualifier("orphansDirectoryBaseName")
    private String orphansDirectoryBaseName;
    @Autowired
    @Qualifier("workspaceBaseDirectory")
    private File workspaceBaseDirectory;
    @Resource
    @Qualifier("customTypecheckerFolderToConfigFileMap")
    private Map<String, String> customTypecheckerFolderToConfigFileMap;
    
    public LamusPropertiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getTestProperty method, of class LamusProperties.
     */
    @Test
    public void testPropertiesAreLoaded() {
        
        long expectedDefaultMaxStorageSpaceInBytes = 10L * 1024 * 1024 * 1024;
        assertEquals(expectedDefaultMaxStorageSpaceInBytes, defaultMaxStorageSpaceInBytes);
        
        int expectedNumberOfDaysOfInactivityAllowedSinceLastSession = 60;
        assertEquals(expectedNumberOfDaysOfInactivityAllowedSinceLastSession, numberOfDaysOfInactivityAllowedSinceLastSession);
        
        int expectedTotalNumberOfDaysAllowedUntilExpiry = 180;
        assertEquals(expectedTotalNumberOfDaysAllowedUntilExpiry, totalNumberOfDaysAllowedUntilExpiry);
        
        int expectedNumberOfDaysOfInactivityAllowedSinceLastWarningEmail = 30;
        assertEquals(expectedNumberOfDaysOfInactivityAllowedSinceLastWarningEmail, numberOfDaysOfInactivityAllowedSinceLastWarningEmail);
        
        long expectedTypeRecheckSizeLimitInBytes = 8L * 1024 * 1024;
        assertEquals(expectedTypeRecheckSizeLimitInBytes, typeRecheckSizeLimitInBytes);
        
        int expectedMaxDirectoryNameLength = 100;
        assertEquals(expectedMaxDirectoryNameLength, maxDirectoryNameLength);
        
        String expectedCorpusDirectoryBaseName = "Corpusstructure";
        assertEquals(expectedCorpusDirectoryBaseName, corpusDirectoryBaseName);
        
        String expectedOrphansDirectoryBaseName = "sessions";
        assertEquals(expectedOrphansDirectoryBaseName, orphansDirectoryBaseName);
        
        String expectedWorkspaceBaseDirectoryPath = "/lat/corpora/lamus/workspaces";
        assertEquals(expectedWorkspaceBaseDirectoryPath, workspaceBaseDirectory.getPath());
        
//        File typecheckerFolder1 = new File("folder1");
//        File typecheckerFolder2 = new File("folder2");
//        File typecheckerConfigFile1 = new File("config_file1");
//        File typecheckerFolder3 = new File("folder3");
//        File typecheckerFolder4 = new File("folder4");
//        File typecheckerConfigFile2 = new File("config_file2");
        
        Map<String, String> expectedCustomTypecheckerConfigMap = new HashMap<String, String>();
        expectedCustomTypecheckerConfigMap.put("folder1", "config_file1");
        expectedCustomTypecheckerConfigMap.put("folder2", "config_file1");
        expectedCustomTypecheckerConfigMap.put("folder3", "config_file2");
        expectedCustomTypecheckerConfigMap.put("folder4", "config_file2");
        assertEquals(expectedCustomTypecheckerConfigMap, customTypecheckerFolderToConfigFileMap);
    }
}
