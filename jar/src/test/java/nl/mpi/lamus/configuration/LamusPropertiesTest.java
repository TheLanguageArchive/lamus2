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
package nl.mpi.lamus.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import nl.mpi.lamus.configuration.LamusProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@ActiveProfiles("testing")
public class LamusPropertiesTest {
    
    @Autowired
    private long defaultMaxStorageSpaceInBytes;
    @Autowired
    private int numberOfDaysOfInactivityAllowedSinceLastSession;
    @Autowired
    private int totalNumberOfDaysAllowedUntilExpiry;
    @Autowired
    private int numberOfDaysOfInactivityAllowedSinceLastWarningEmail;
    @Autowired
    private long typeRecheckSizeLimitInBytes;
    @Autowired
    private int maxDirectoryNameLength;
    @Autowired
    private String corpusDirectoryBaseName;
    @Autowired
    private String orphansDirectoryBaseName;
    @Autowired
    private File workspaceBaseDirectory;
    @Resource
    private Map<File, File> customTypecheckerFolderToConfigFileMap;
    
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
        
        String expectedWorkspaceBaseDirectoryPath = "/lat/corpora/lamus/LAMS_WORKSPACE";
        assertEquals(expectedWorkspaceBaseDirectoryPath, workspaceBaseDirectory.getPath());
        
        File typecheckerFolder1 = new File("folder1");
        File typecheckerFolder2 = new File("folder2");
        File typecheckerConfigFile1 = new File("config_file1");
        File typecheckerFolder3 = new File("folder3");
        File typecheckerFolder4 = new File("folder4");
        File typecheckerConfigFile2 = new File("config_file2");
        
        Map<File, File> expectedCustomTypecheckerConfigMap = new HashMap<File, File>();
        expectedCustomTypecheckerConfigMap.put(typecheckerFolder1, typecheckerConfigFile1);
        expectedCustomTypecheckerConfigMap.put(typecheckerFolder2, typecheckerConfigFile1);
        expectedCustomTypecheckerConfigMap.put(typecheckerFolder3, typecheckerConfigFile2);
        expectedCustomTypecheckerConfigMap.put(typecheckerFolder4, typecheckerConfigFile2);
        assertEquals(expectedCustomTypecheckerConfigMap, customTypecheckerFolderToConfigFileMap);
    }
}