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
package nl.mpi.lamus.workspace.factory.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import nl.mpi.lamus.ams.AmsServiceBridge;
import nl.mpi.lamus.archive.ArchiveFileHelper;
import nl.mpi.lamus.workspace.factory.WorkspaceFactory;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusWorkspaceFactoryTest {
    
    @Configuration
    @ComponentScan("nl.mpi.lamus.workspace.factory")
    @Profile("testing")
    static class ConfigurationContext {
        
        // This bean is not needed here,
        // but because of the "ComponentScan",
        // Spring tries to add the LamusWorkspaceNodeFactory
        // singleton to the application context, and it needs this object then
        @Bean
        public ArchiveFileHelper archiveFileHelper() {
            return null;
        }
        
        @Bean
        public AmsServiceBridge amsBridge() {
            return null;
        }
        
        @Bean
        @Qualifier("defaultMaxStorageSpaceInBytes")
        public long defaultMaxStorageSpaceInBytes() {
            return 10L * 1024 * 1024 * 1024;
        }
    }
    
    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();
    @Mock private AmsServiceBridge mockAmsBridge;
    
    @Autowired
    private WorkspaceFactory factory;
    @Autowired
    @Qualifier("defaultMaxStorageSpaceInBytes")
    private long defaultMaxStorageSpaceInBytes;
    
    private URI archiveTopNodeURI;
    private String userID;
    
    public LamusWorkspaceFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws URISyntaxException {
        archiveTopNodeURI = new URI(UUID.randomUUID().toString());
        userID = "testUser";
        ReflectionTestUtils.setField(factory, "amsBridge", mockAmsBridge);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Tests if a workspace object is created,
     * by calling the method {@link WorkspaceFactory#getNewWorkspace(java.lang.String, int)},
     * while retrieving the storage space values from {@link AmsServiceBridge}
     */
    @Test
    public void workspaceObjectIsCreatedWithRetrievedStorageSpaceValues() {
        
        final long expectedUsedStorageSpace = 10000000L;
        final long expectedMaxStorageSpace = 10000000000L;
        WorkspaceStatus expectedStatus = WorkspaceStatus.UNINITIALISED;
        
        context.checking(new Expectations() {{
            oneOf (mockAmsBridge).getUsedStorageSpace(userID, archiveTopNodeURI);
                will(returnValue(expectedUsedStorageSpace));
            oneOf (mockAmsBridge).getMaxStorageSpace(userID, archiveTopNodeURI);
                will(returnValue(expectedMaxStorageSpace));
        }});
        
        Workspace testWorkspace = factory.getNewWorkspace(userID, archiveTopNodeURI);
        
        assertNotNull("Returned workspace should not be null.", testWorkspace);
        assertTrue("Returned object is not an instance of Workspace.", testWorkspace instanceof LamusWorkspace);

        assertEquals("Status of created workspace is not the expected one.", expectedStatus, testWorkspace.getStatus());
        assertEquals("Value of 'usedStorageSpace' is not the expected one.", expectedUsedStorageSpace, testWorkspace.getUsedStorageSpace());
        assertEquals("Value of 'maxStorageSpace' is not the expected one.", expectedMaxStorageSpace, testWorkspace.getMaxStorageSpace());
    }
    
    /**
     * Tests if a workspace object is created,
     * by calling the method {@link WorkspaceFactory#getNewWorkspace(java.lang.String, int)},
     * while retrieving the max storage space values from {@link AmsServiceBridge}
     * and using the default value for used space value
     */
    @Test
    public void workspaceObjectIsCreatedWithDefaultUsedStorageSpace() {
        
        final long valueNotDefined = -1L;
        final long expectedUsedStorageSpace = 0L;
        final long expectedMaxStorageSpace = 10000000000L;
        WorkspaceStatus expectedStatus = WorkspaceStatus.UNINITIALISED;
        
        context.checking(new Expectations() {{
            oneOf (mockAmsBridge).getUsedStorageSpace(userID, archiveTopNodeURI);
                will(returnValue(valueNotDefined));
            oneOf (mockAmsBridge).getMaxStorageSpace(userID, archiveTopNodeURI);
                will(returnValue(expectedMaxStorageSpace));
        }});
        
        Workspace testWorkspace = factory.getNewWorkspace(userID, archiveTopNodeURI);
        
        assertNotNull("Returned workspace should not be null.", testWorkspace);
        assertTrue("Returned object is not an instance of Workspace.", testWorkspace instanceof LamusWorkspace);

        assertEquals("Status of created workspace is not the expected one.", expectedStatus, testWorkspace.getStatus());
        assertEquals("Value of 'usedStorageSpace' is not the expected one.", expectedUsedStorageSpace, testWorkspace.getUsedStorageSpace());
        assertEquals("Value of 'maxStorageSpace' is not the expected one.", expectedMaxStorageSpace, testWorkspace.getMaxStorageSpace());
    }

    /**
     * Tests if a workspace object is created,
     * by calling the method {@link WorkspaceFactory#getNewWorkspace(java.lang.String, int)},
     * while retrieving the used storage space values from {@link AmsServiceBridge}
     * and using the default value for max space value
     */
    @Test
    public void workspaceObjectIsCreatedWithDefaultMaxStorageSpace() {
        
        final long valueNotDefined = -1L;
        final long expectedUsedStorageSpace = 10000000L;
        final long expectedMaxStorageSpace = defaultMaxStorageSpaceInBytes;
        WorkspaceStatus expectedStatus = WorkspaceStatus.UNINITIALISED;
        
        context.checking(new Expectations() {{
            oneOf (mockAmsBridge).getUsedStorageSpace(userID, archiveTopNodeURI);
                will(returnValue(expectedUsedStorageSpace));
            oneOf (mockAmsBridge).getMaxStorageSpace(userID, archiveTopNodeURI);
                will(returnValue(valueNotDefined));
        }});
        
        Workspace testWorkspace = factory.getNewWorkspace(userID, archiveTopNodeURI);
        
        assertNotNull("Returned workspace should not be null.", testWorkspace);
        assertTrue("Returned object is not an instance of Workspace.", testWorkspace instanceof LamusWorkspace);

        assertEquals("Status of created workspace is not the expected one.", expectedStatus, testWorkspace.getStatus());
        assertEquals("Value of 'usedStorageSpace' is not the expected one.", expectedUsedStorageSpace, testWorkspace.getUsedStorageSpace());
        assertEquals("Value of 'maxStorageSpace' is not the expected one.", expectedMaxStorageSpace, testWorkspace.getMaxStorageSpace());
    }
    
}
