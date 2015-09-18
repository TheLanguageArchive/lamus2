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
package nl.mpi.lamus.dao.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import javax.sql.DataSource;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeReplacement;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;



/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(/*classes = {LamusJdbcWorkspaceDao.class, EmbeddedDatabaseBeans.classLamusJdbcWorkspaceDaoTestBeans.class}, */
        loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("testing")
public class LamusJdbcWorkspaceDaoTest extends AbstractTransactionalJUnit4SpringContextTests {
    
    @Configuration
    @ComponentScan("nl.mpi.lamus.dao")
    @Profile("testing")
    static class DataSourceConfig {
        
        @Bean
        @Qualifier("lamusDataSource")
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .setName("lamus2")
                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_drop.sql")
                .addScript("classpath:nl/mpi/lamus/dao/implementation/hsql_lamus2_create.sql")
                .build();
        }
    }
    
    @Configuration
    static class TransactionManagerConfig {
        
        @Autowired
        @Qualifier("lamusDataSource")
        private DataSource lamusDataSource;
        
        @Bean
        public DataSourceTransactionManager transactionManager() {
            return new DataSourceTransactionManager(lamusDataSource);
        }
    }
    
    @Autowired
    @Qualifier("lamusDataSource")
    DataSource lamusDataSource;
    
    
    private final URI standardArchiveUriForNode;
    private final URL standardWorkspaceUrlForNode;
            
    
//    @Autowired
    LamusJdbcWorkspaceDao workspaceDao;
    
    public LamusJdbcWorkspaceDaoTest() throws MalformedURLException, URISyntaxException {
        standardArchiveUriForNode = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        standardWorkspaceUrlForNode = new URL("http://some.workspace.url");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
        
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
        workspaceDao = new LamusJdbcWorkspaceDao(lamusDataSource);
    }
    
    @After
    public void tearDown() {

    }


    @Test
    public void addWorkspace() {

        int initialNumberOfRows = countRowsInTable("workspace");
        
        Workspace insertedWorkspace = new LamusWorkspace("testUser", 0L, 10000L);
        
        workspaceDao.addWorkspace(insertedWorkspace);
        
        assertEquals("Column was not added to the workspace table.", initialNumberOfRows + 1, countRowsInTable("workspace"));
    }
    
    @Test
    public void addWorkspaceWithWrongParameters() {

        int initialNumberOfRows = countRowsInTable("workspace");
        
        Workspace insertedWorkspace = new LamusWorkspace(0, null, 0, null, null, null, null, null, null, 0, 0, WorkspaceStatus.REFUSED, null, null);
        
        try {
            workspaceDao.addWorkspace(insertedWorkspace);
            fail("An exception should have been thrown.");
        } catch(DataAccessException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
            assertEquals("Column was added to the workspace table.", initialNumberOfRows, countRowsInTable("workspace"));
        }
    }
    
    @Test
    public void deleteWorkspace() {
        
        int initialNumberOfRows = countRowsInTable("workspace");
        
        Workspace insertedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        
        assertTrue("Workspace was not inserted into the database", countRowsInTable("workspace") == initialNumberOfRows + 1);
        
        workspaceDao.deleteWorkspace(insertedWorkspace.getWorkspaceID());
        
        assertTrue("Workspace was not deleted from the database", countRowsInTable("workspace") == initialNumberOfRows);
    }
    
    @Test
    public void deleteWorkspaceIncludingDataInOtherTables() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfWorkspaceRows = countRowsInTable("workspace");
        int initialNumberOfNodeRows = countRowsInTable("node");
        int initialNumberOfNodeLocks = countRowsInTable("node_lock");
        int initialNumberOfLinkRows = countRowsInTable("node_link");
        int initialNumberOfReplacementRows = countRowsInTable("node_replacement");
        
        Workspace insertedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        URI firstNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL firstNodeURL = new URL("https://archive/location/firstNode.cmdi");
        WorkspaceNode firstNode = insertTestWorkspaceNodeWithUriIntoDB(insertedWorkspace, firstNodeURI, firstNodeURL, firstNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(firstNode);
        URI secondNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL secondNodeURL = new URL("https://archive/location/secondNode.cmdi");
        WorkspaceNode secondNode = insertTestWorkspaceNodeWithUriIntoDB(insertedWorkspace, secondNodeURI, secondNodeURL, secondNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(secondNode);
        URI thirdNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL thirdNodeURL = new URL("https://archive/location/thirdNode.cmdi");
        WorkspaceNode thirdNode = insertTestWorkspaceNodeWithUriIntoDB(insertedWorkspace, thirdNodeURI, thirdNodeURL, thirdNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(thirdNode);
        URI fourthNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL fourthNodeURL = new URL("https://archive/location/fourthNode.cmdi");
        WorkspaceNode fourthNode = insertTestWorkspaceNodeWithUriIntoDB(insertedWorkspace, fourthNodeURI, fourthNodeURL, fourthNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(fourthNode);
        setNodeAsParentAndInsertLinkIntoDatabase(firstNode, secondNode);
        setNodeAsReplacedAndAddReplacementInDatabase(thirdNode, fourthNode);
        
        assertTrue("Workspace was not inserted into the database", countRowsInTable("workspace") == initialNumberOfWorkspaceRows + 1);
        assertTrue("Nodes were not inserted into the database", countRowsInTable("node") == initialNumberOfNodeRows + 4);
        assertTrue("Node locks were not inserted into the database", countRowsInTable("node_lock") == initialNumberOfNodeLocks + 4);
        assertTrue("Link was not inserted into the database", countRowsInTable("node_link") == initialNumberOfLinkRows + 1);
        assertTrue("Replacement was not inserted into the database", countRowsInTable("node_replacement") == initialNumberOfReplacementRows + 1);
        
        workspaceDao.deleteWorkspace(insertedWorkspace.getWorkspaceID());
        
        assertTrue("Replacement was not deleted from the database", countRowsInTable("node_replacement") == initialNumberOfReplacementRows);
        assertTrue("Link was not deleted from the database", countRowsInTable("node_link") == initialNumberOfLinkRows);
        assertTrue("Node locks were not deleted from the database", countRowsInTable("node_lock") == initialNumberOfNodeLocks);
        assertTrue("Nodes were not deleted from the database", countRowsInTable("node") == initialNumberOfNodeRows);
        assertTrue("Workspace was not deleted from the database", countRowsInTable("workspace") == initialNumberOfWorkspaceRows);
    }
    
    
    @Test
    public void updateWorkspaceTopNode() throws URISyntaxException, MalformedURLException {
        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(expectedWorkspace, topURI, topURL, null, true, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        int expectedTopNodeID = topNode.getWorkspaceNodeID();
        URI expectedTopNodeURI = topNode.getArchiveURI();
        URL expectedTopNodeURL = topNode.getArchiveURL();
        
        expectedWorkspace.setTopNodeID(topNode.getWorkspaceNodeID());
        expectedWorkspace.setTopNodeArchiveURI(topNode.getArchiveURI());
        expectedWorkspace.setTopNodeArchiveURL(topNode.getArchiveURL());
        
        workspaceDao.updateWorkspaceTopNode(expectedWorkspace);
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(expectedWorkspace.getWorkspaceID());
        
        assertEquals("Workspace object retrieved from the database is different from expected.", expectedWorkspace, retrievedWorkspace);
        assertEquals("Top node ID of the workspace was not updated in the database.", expectedTopNodeID, retrievedWorkspace.getTopNodeID());
        assertEquals("Top node archive URI of the workspace was not updated in the database.", expectedTopNodeURI, retrievedWorkspace.getTopNodeArchiveURI());
        assertEquals("Top node archive URL of the workspace was not updated in the database.", expectedTopNodeURL, retrievedWorkspace.getTopNodeArchiveURL());
    }
    
    /**
     * 
     */
    @Test
    public void updateWorkspaceStatus() {
        
        Workspace workspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        WorkspaceStatus expectedStatus = WorkspaceStatus.SUBMITTED;
        String expectedMessage = "the workspace was submitted and bla bla";
        
        workspace.setStatus(expectedStatus);
        workspace.setMessage(expectedMessage);
        
        workspaceDao.updateWorkspaceStatusMessage(workspace);
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(workspace.getWorkspaceID());
        
        assertEquals("Workspace object retrieved from the database is different from expected.", workspace, retrievedWorkspace);
        assertEquals("Status of the workspace was not updated in the database.", expectedStatus, retrievedWorkspace.getStatus());
        assertEquals("Message of the workspace was not updated in the database.", expectedMessage, retrievedWorkspace.getMessage());
    }
    
    @Test
    public void updateWorkspaceStatusWithNullStatus() {
        
        Workspace workspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        Workspace changedWorkspace = copyWorkspace(workspace);

        WorkspaceStatus changedStatus = null;
        String changedMessage = "test message";
        
        changedWorkspace.setStatus(changedStatus);
        changedWorkspace.setMessage(changedMessage);
        
        try {
            workspaceDao.updateWorkspaceStatusMessage(changedWorkspace);
            fail("An exception should have been thrown.");
        } catch(DataIntegrityViolationException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(workspace.getWorkspaceID());

        assertEquals("Workspace object retrieved from the database is different from expected.", workspace, retrievedWorkspace);
        assertEquals("Status of the workspace was not updated in the database.", workspace.getStatus(), retrievedWorkspace.getStatus());
        assertEquals("Message of the workspace was not updated in the database.", workspace.getMessage(), retrievedWorkspace.getMessage());
    }
    
    @Test
    public void updateWorkspaceStatusWithNullMessage() {
        
        Workspace workspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        Workspace changedWorkspace = copyWorkspace(workspace);

        WorkspaceStatus changedStatus = WorkspaceStatus.INITIALISING;
        String changedMessage = null;
        
        changedWorkspace.setStatus(changedStatus);
        changedWorkspace.setMessage(changedMessage);
        
        try {
            workspaceDao.updateWorkspaceStatusMessage(changedWorkspace);
            fail("An exception should have been thrown.");
        } catch(DataIntegrityViolationException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(workspace.getWorkspaceID());

        assertEquals("Workspace object retrieved from the database is different from expected.", workspace, retrievedWorkspace);
        assertEquals("Status of the workspace was not updated in the database.", workspace.getStatus(), retrievedWorkspace.getStatus());
        assertEquals("Message of the workspace was not updated in the database.", workspace.getMessage(), retrievedWorkspace.getMessage());
    }
    
    @Test
    public void updateCrawlerID() {
        
        Workspace workspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        String expectedCrawlerID = UUID.randomUUID().toString();
        
        workspace.setCrawlerID(expectedCrawlerID);
        
        workspaceDao.updateWorkspaceCrawlerID(workspace);
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(workspace.getWorkspaceID());
        
        assertEquals("Workspace object retrieved from the database is different from expected.", workspace, retrievedWorkspace);
        assertEquals("Crawler ID of the workspace was not updated in the database.", expectedCrawlerID, retrievedWorkspace.getCrawlerID());
    }
    
    @Test
    public void updateWorkspaceEndDates() {
        
        Workspace workspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        Workspace changedWorkspace = copyWorkspace(workspace);
        
        Date expectedEndDate = Calendar.getInstance().getTime();
        
        changedWorkspace.setSessionEndDate(expectedEndDate);
        changedWorkspace.setEndDate(expectedEndDate);
        
        workspaceDao.updateWorkspaceEndDates(changedWorkspace);
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(workspace.getWorkspaceID());
        
        assertEquals("Workspace object retrieved from the database is different from expected.", changedWorkspace, retrievedWorkspace);
        assertEquals("Session End Date of the workspace was not updated in the database.", expectedEndDate, retrievedWorkspace.getSessionEndDate());
        assertEquals("End Date of the workspace was not updated in the database.", expectedEndDate, retrievedWorkspace.getEndDate());
    }
    
    @Test
    public void updateSessionDates() {
        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        Calendar c = Calendar.getInstance();
        Date expectedSessionStartDate = c.getTime();
        c.add(Calendar.DATE, 60);
        Date expectedSessionEndDate = c.getTime();
        
        expectedWorkspace.setSessionStartDate(expectedSessionStartDate);
        expectedWorkspace.setSessionEndDate(expectedSessionEndDate);
        
        this.workspaceDao.updateWorkspaceSessionDates(expectedWorkspace);
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(expectedWorkspace.getWorkspaceID());
        
        assertEquals("Workspace object retrieved from the database is different from expected.", expectedWorkspace, retrievedWorkspace);
        assertEquals("Session start date of the workspace was not updated in the database.", expectedSessionStartDate, retrievedWorkspace.getSessionStartDate());
        assertEquals("Session end date of the workspace was not updated in the database.", expectedSessionEndDate, retrievedWorkspace.getSessionEndDate());
    }

    /**
     * Test of getWorkspace method, of class JdbcWorkspaceDao.
     */
    @Test
    public void getWorkspaceWithNullEndDates() throws WorkspaceNotFoundException {

        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceWithNonNullEndDates() throws WorkspaceNotFoundException {

        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceWithTopNodeID() throws URISyntaxException, MalformedURLException, WorkspaceNotFoundException {
        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testWorkspaceNode = insertTestWorkspaceNodeIntoDB(expectedWorkspace);
        setNodeAsWorkspaceTopNodeInDB(expectedWorkspace, testWorkspaceNode);
        
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceThatDoesntExist() {
        
        int nonExistingWorkspaceID = 1564;
        String errorMessage = "Workspace with ID " + nonExistingWorkspaceID + " does not exist in the database";
        
        try {
            workspaceDao.getWorkspace(nonExistingWorkspaceID);
            fail("should have thrown exception");
        } catch(WorkspaceNotFoundException ex) {
            assertEquals("Message different from expected", errorMessage, ex.getMessage());
            assertEquals("Workspace ID different from expected", nonExistingWorkspaceID, ex.getWorkspaceID());
            assertTrue("Exception cause has different type from expected", ex.getCause() instanceof EmptyResultDataAccessException);
        }
    }
    
    @Test
    public void getExistingWorkspacesForUserBothInitialised() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace1);
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneUninitialised() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.UNINITIALISED);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneInitialising() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.ERROR_INITIALISATION);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneInitialisationError() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.ERROR_INITIALISATION);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneSleeping() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.SLEEPING);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneSubmitted() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.SUBMITTED);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneClosedTimeout() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.TIMEOUT);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneRefused() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.REFUSED);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneSuccess() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.SUCCESS);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOneError() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.ERROR_MOVING_DATA);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getExistingWorkspacesForUserOnePending() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.UPDATING_ARCHIVE);
        updateWorkspaceStatusInDb(workspace1);
        
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getNonExistinWorkspacesForUser() {
        
        String userID = "user1";
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesForUser(userID);
        
        assertTrue("Retrieved list should be empty", retrievedList.isEmpty());
    }
    
    @Test
    public void getWorkspacesInFinalStageOneSubmitted() {
        
        Workspace workspace1 = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace1);
        Workspace workspace2 = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.UPDATING_ARCHIVE);
        workspace2.setCrawlerID(UUID.randomUUID().toString());
        updateWorkspaceStatusInDb(workspace2);
        updateWorkspaceCrawlerIDInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesInFinalStage();
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getWorkspacesInFinalStageTwoSubmitted() {
        
        Workspace workspace1 = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.UPDATING_ARCHIVE);
        workspace1.setCrawlerID(UUID.randomUUID().toString());
        updateWorkspaceStatusInDb(workspace1);
        updateWorkspaceCrawlerIDInDb(workspace1);
        Workspace workspace2 = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.UPDATING_ARCHIVE);
        workspace2.setCrawlerID(UUID.randomUUID().toString());
        updateWorkspaceStatusInDb(workspace2);
        updateWorkspaceCrawlerIDInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesInFinalStage();
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getWorkspacesInFinalStageNoneSubmitted() {
        
        Workspace workspace1 = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        workspace1.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace1);
        Workspace workspace2 = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        workspace2.setStatus(WorkspaceStatus.INITIALISED);
        updateWorkspaceStatusInDb(workspace2);
        
        Collection<Workspace> expectedList = new ArrayList<>();
        
        Collection<Workspace> retrievedList = workspaceDao.getWorkspacesInFinalStage();
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getAllWorkspacesOneExists() {
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB("randomUser", Boolean.FALSE);
        
        List<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        
        List<Workspace> retrievedList = workspaceDao.getAllWorkspaces();
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getAllWorkspacesSeveralExist() {
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB("randomUser", Boolean.FALSE);
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB("anotherRandomUser", Boolean.TRUE);
        Workspace workspace3 = insertTestWorkspaceWithGivenUserIntoDB("yetAnotherRandomUser", Boolean.FALSE);
        workspace3.setStatus(WorkspaceStatus.ERROR_MOVING_DATA);
        updateWorkspaceStatusInDb(workspace3);
        
        List<Workspace> expectedList = new ArrayList<>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        expectedList.add(workspace3);
        
        List<Workspace> retrievedList = workspaceDao.getAllWorkspaces();
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void getAllWorkspacesNoneExists() {
        
        List<Workspace> retrievedList = workspaceDao.getAllWorkspaces();
        
        assertTrue("Retrieved list should be empty", retrievedList.isEmpty());
    }

    @Test
    public void nodeIsLocked() throws MalformedURLException, URISyntaxException {
        
        URI archiveNodeUriToCheck = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URI archiveNodeUriToBeInsertedInTheDb = archiveNodeUriToCheck;
        URL nodeURL = new URL("file:/archive/folder/node.cmdi");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, nodeURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(testNode);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeUriToCheck);
        
        assertTrue("Node should be locked (should exist in the database).", result);
    }
    
    @Test
    public void nodeIsNotLocked() throws MalformedURLException, URISyntaxException {
        
        URI archiveNodeUriToCheck = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URI archiveNodeUriToBeInsertedInTheDb = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL nodeURL = new URL("file:/archive/folder/node.cmdi");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, nodeURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeUriToCheck);
        
        assertFalse("Node should not be locked (should not exist in the database).", result);
    }
    
    @Test
    public void getNodeByArchiveURI() throws URISyntaxException, MalformedURLException {
        
        URI archiveNodeUriToCheck = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URI archiveNodeUriToBeInsertedInTheDb = archiveNodeUriToCheck;
        URL nodeURL = new URL("file:/archive/folder/node.cmdi");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, nodeURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> retrievedNodes = this.workspaceDao.getWorkspaceNodeByArchiveURI(archiveNodeUriToCheck);
        
        assertNotNull("Node should not be null", retrievedNodes);
        assertFalse("List should not be empty", retrievedNodes.isEmpty());
        assertTrue("List should contain one node", retrievedNodes.size() == 1);
        assertEquals("Node is different from expected", testNode, retrievedNodes.iterator().next());
    }
    
    @Test
    public void getNodeByArchiveURI_MoreThanOnce() throws URISyntaxException, MalformedURLException {
        
        URI archiveNodeUriToCheck = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL firstURL = new URL("file:/archive/folder/first.cmdi");
        URI archiveNodeUriToBeInsertedInTheDb = archiveNodeUriToCheck;
        URL secondURL = new URL("file:/archive/folder/second.cmdi");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        WorkspaceNode firstTestNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, firstURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        WorkspaceNode secondTestNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, secondURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        Collection<WorkspaceNode> testNodes = new ArrayList<>();
        testNodes.add(firstTestNode);
        testNodes.add(secondTestNode);
        
        Collection<WorkspaceNode> retrievedNodes = this.workspaceDao.getWorkspaceNodeByArchiveURI(archiveNodeUriToCheck);
        
        assertNotNull("Node should not be null", retrievedNodes);
        assertFalse("List should not be empty", retrievedNodes.isEmpty());
        assertTrue("List should contain two nodes", retrievedNodes.size() == 2);
        assertTrue("Collection doesn't contain all expected nodes", retrievedNodes.containsAll(testNodes));
    }
    
    
    @Test
    public void addWorkspaceNode() throws URISyntaxException, MalformedURLException {

        int initialNumberOfRows = countRowsInTable("node");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        WorkspaceNode insertedNode = new LamusWorkspaceNode();
        insertedNode.setWorkspaceID(testWorkspace.getWorkspaceID());
        insertedNode.setName("testNode");
        insertedNode.setType(WorkspaceNodeType.METADATA);
        insertedNode.setStatus(WorkspaceNodeStatus.ARCHIVE_COPY);
        insertedNode.setFormat("someFormat");
        insertedNode.setProfileSchemaURI(URI.create("http://test.node.uri"));
        insertedNode.setArchiveURI(URI.create("hdl:11142/" + UUID.randomUUID().toString()));
        insertedNode.setOriginURI(URI.create("http://test,node.uri"));
        insertedNode.setWorkspaceURL(new URL("http://test.node.uri"));
        
        this.workspaceDao.addWorkspaceNode(insertedNode);
        
        assertEquals("Column was not added to the node table.", initialNumberOfRows + 1, countRowsInTable("node"));
    }
    
    @Test
    public void addWorkspaceNodeWithWrongParameters() {
        
        int initialNumberOfRows = countRowsInTable("node");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        WorkspaceNode insertedNode = new LamusWorkspaceNode();
        insertedNode.setWorkspaceID(testWorkspace.getWorkspaceID());
        
        try {
            workspaceDao.addWorkspaceNode(insertedNode);
            fail("An exception should have been thrown.");
        } catch(DataAccessException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        assertEquals("Column was added to the node table.", initialNumberOfRows, countRowsInTable("node"));
    }
    
    @Test
    public void addWorkspaceNodeToNonExistingWorkspace() {
        
        int initialNumberOfRows = countRowsInTable("node");
        
        int fakeWorkspaceID = 100;
        
        WorkspaceNode insertedNode = new LamusWorkspaceNode();
        insertedNode.setWorkspaceID(fakeWorkspaceID);
        
        try {
            workspaceDao.addWorkspaceNode(insertedNode);
            fail("An exception should have been thrown.");
        } catch(DataIntegrityViolationException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        assertEquals("Column was added to the node table.", initialNumberOfRows, countRowsInTable("node"));
    }
    
    @Test
    public void lockNode() throws MalformedURLException, URISyntaxException {
        
        Workspace ws = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL nodeURL = new URL("https://archive/location/node.cmdi");
        WorkspaceNode node = insertTestWorkspaceNodeWithUriIntoDB(ws, nodeURI, nodeURL, nodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        int initialNumberOfLocks = countRowsInTable("node_lock");
        
        workspaceDao.lockNode(nodeURI, ws.getWorkspaceID());
        
        int finalNumberOfLocks = countRowsInTable("node_lock");
        
        assertEquals("Node lock wasn't created", finalNumberOfLocks, initialNumberOfLocks + 1);
    }
    
    @Test
    public void unlockNode() throws MalformedURLException, URISyntaxException {
        
        Workspace ws = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI nodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL nodeURL = new URL("https://archive/location/node.cmdi");
        WorkspaceNode node = insertTestWorkspaceNodeWithUriIntoDB(ws, nodeURI, nodeURL, nodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(node);
        
        int initialNumberOfLocks = countRowsInTable("node_lock");
        
        workspaceDao.unlockNode(nodeURI);
        
        int finalNumberOfLocks = countRowsInTable("node_lock");
        
        assertEquals("Node lock wasn't removed", finalNumberOfLocks, initialNumberOfLocks - 1);
    }
    
    @Test
    public void unlockAllNodesOfWorkspace() throws MalformedURLException, URISyntaxException {

        int initialNumberOfWorkspaces = countRowsInTable("workspace");
        int initialNumberOfNodes = countRowsInTable("node");
        int initialNumberOfLinks = countRowsInTable("node_link");
        int initialNumberOfLocks = countRowsInTable("node_lock");
        int initialNumberOfReplacements = countRowsInTable("node_replacement");
        
        Workspace ws = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI firstNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL firstNodeURL = new URL("https://archive/location/firstNode.cmdi");
        WorkspaceNode firstNode = insertTestWorkspaceNodeWithUriIntoDB(ws, firstNodeURI, firstNodeURL, firstNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(firstNode);
        URI secondNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL secondNodeURL = new URL("https://archive/location/secondNode.cmdi");
        WorkspaceNode secondNode = insertTestWorkspaceNodeWithUriIntoDB(ws, secondNodeURI, secondNodeURL, secondNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(secondNode);
        URI thirdNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL thirdNodeURL = new URL("https://archive/location/firstNode.cmdi");
        WorkspaceNode thirdNode = insertTestWorkspaceNodeWithUriIntoDB(ws, thirdNodeURI, thirdNodeURL, thirdNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(thirdNode);
        setNodeAsParentAndInsertLinkIntoDatabase(firstNode, secondNode);
        setNodeAsReplacedAndAddReplacementInDatabase(secondNode, thirdNode);
        
        int intermediateNumberOfWorkspaces = countRowsInTable("workspace");
        int intermediateNumberOfNodes = countRowsInTable("node");
        int intermediateNumberOfLinks = countRowsInTable("node_link");
        int intermediateNumberOfLocks = countRowsInTable("node_lock");
        int intermediateNumberOfReplacements = countRowsInTable("node_replacement");
        
        assertEquals("Intermediate number of workspaces different from expected", initialNumberOfWorkspaces + 1, intermediateNumberOfWorkspaces);
        assertEquals("Intermediate number of nodes different from expected", initialNumberOfNodes + 3, intermediateNumberOfNodes);
        assertEquals("Intermediate number of links different from expected", initialNumberOfLinks + 1, intermediateNumberOfLinks);
        assertEquals("Intermediate number of locks different from expected", initialNumberOfLocks + 3, intermediateNumberOfLocks);
        assertEquals("Intermediate number of replacements different from expected", initialNumberOfReplacements + 1, intermediateNumberOfReplacements);
        
        this.workspaceDao.unlockAllNodesOfWorkspace(ws.getWorkspaceID());
        
        int finalNumberOfWorkspaces = countRowsInTable("workspace");
        int finalNumberOfNodes = countRowsInTable("node");
        int finalNumberOfLinks = countRowsInTable("node_link");
        int finalNumberOfLocks = countRowsInTable("node_lock");
        int finalNumberOfReplacements = countRowsInTable("node_replacement");
        
        assertEquals("Final number of workspaces different from expected", intermediateNumberOfWorkspaces, finalNumberOfWorkspaces);
        assertEquals("Final number of nodes different from expected", intermediateNumberOfNodes, finalNumberOfNodes);
        assertEquals("Final number of links different from expected", intermediateNumberOfLinks, finalNumberOfLinks);
        assertEquals("Final number of locks different from expected", initialNumberOfLocks, finalNumberOfLocks);
        assertEquals("Final number of replacements different from expected", intermediateNumberOfReplacements, finalNumberOfReplacements);
    }
    
    @Test
    public void updateNodeWorkspaceURL() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URL expectedURL = new URL("http:/totally/different.url");

        testNode.setWorkspaceURL(expectedURL);
        
        workspaceDao.updateNodeWorkspaceURL(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Workspace URL of the node was not updated in the database.", expectedURL, retrievedNode.getWorkspaceURL());
    }
    
    @Test
    public void updateNodeWithNullWorkspaceURL() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);

        testNode.setWorkspaceURL(null);
        
        workspaceDao.updateNodeWorkspaceURL(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Workspace URL of the node was not updated in the database.", null, retrievedNode.getWorkspaceURL());
    }
    
    @Test
    public void updateNodeArchiveURI() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI expectedURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());

        testNode.setArchiveURI(expectedURI);
        
        workspaceDao.updateNodeArchiveUri(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Archive URI of the node was not updated in the database.", expectedURI, retrievedNode.getArchiveURI());
    }
    
    @Test
    public void updateNodeWithNullArchiveURI() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);

        testNode.setArchiveURI(null);
        
        workspaceDao.updateNodeArchiveUri(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Archive URI of the node was not updated in the database.", null, retrievedNode.getArchiveURI());
    }
    
    @Test
    public void updateNodeArchiveURL() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URL expectedURL = new URL("file:/archive/folder/different_test.cmdi");

        testNode.setArchiveURL(expectedURL);
        
        workspaceDao.updateNodeArchiveUrl(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Archive URL of the node was not updated in the database.", expectedURL, retrievedNode.getArchiveURL());
    }
    
    @Test
    public void updateNodeWithNullArchiveURL() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);

        testNode.setArchiveURL(null);
        
        workspaceDao.updateNodeArchiveUrl(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Archive URL of the node was not updated in the database.", null, retrievedNode.getArchiveURL());
    }
    
    @Test
    public void updateNodeType() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        setNodeType(testNode, WorkspaceNodeType.UNKNOWN);
        
        WorkspaceNodeType expectedType = WorkspaceNodeType.RESOURCE_INFO;
        testNode.setType(expectedType);
        
        workspaceDao.updateNodeType(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected", testNode, retrievedNode);
        assertEquals("Type of the node was not updated in the database", expectedType, retrievedNode.getType());
    }
    
    @Test
    public void addWorkspaceNodeLink() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testParentNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        WorkspaceNode testChildNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        
        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(testParentNode.getWorkspaceNodeID(), testChildNode.getWorkspaceNodeID());
        
        this.workspaceDao.addWorkspaceNodeLink(insertedLink);
        
        assertEquals("Column was not added to the node table.", initialNumberOfRows + 1, countRowsInTable("node_link"));
    }

    @Test
    public void addWorkspaceNodeLinkWhenParentDoesNotExist() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testChildNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        int fakeNodeID = 100;
        
        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(fakeNodeID, testChildNode.getWorkspaceNodeID());
        
        try {
            this.workspaceDao.addWorkspaceNodeLink(insertedLink);
            fail("An exception should have been thrown.");
        } catch(DataIntegrityViolationException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        assertEquals("Column was added to the node table.", initialNumberOfRows, countRowsInTable("node_link"));
    }
    
    @Test
    public void addWorkspaceNodeLinkWhenChildDoesNotExist() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testParentNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        int fakeNodeID = 100;

        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(testParentNode.getWorkspaceNodeID(), fakeNodeID);
        
        try {
            this.workspaceDao.addWorkspaceNodeLink(insertedLink);
            fail("An exception should have been thrown.");
        } catch(DataIntegrityViolationException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        assertEquals("Column was added to the node table.", initialNumberOfRows, countRowsInTable("node_link"));
    }
    
    @Test
    public void getExistingNode()
            throws MalformedURLException, URISyntaxException, WorkspaceNodeNotFoundException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceNode(testNode.getWorkspaceNodeID());
        
        assertNotNull("Returned node should not be null", result);
    }
    
    @Test
    public void getNonExistingNode()
            throws WorkspaceNodeNotFoundException {
        
        int workspaceNodeID = 100;
        String expectedErrorMessage = "Workspace Node with ID " + workspaceNodeID + " does not exist in the database";
        
        try {
            this.workspaceDao.getWorkspaceNode(workspaceNodeID);
        } catch (WorkspaceNodeNotFoundException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID should not be known", -1, ex.getWorkspaceID());
            assertEquals("Node ID different from expected", workspaceNodeID, ex.getWorkspaceNodeID());
            assertTrue("Cause has a different type from expected", ex.getCause() instanceof EmptyResultDataAccessException);
        }
    }
    
    @Test
    public void getNodeWithNullProfileSchema()
            throws MalformedURLException, URISyntaxException, WorkspaceNodeNotFoundException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.FALSE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceNode(testNode.getWorkspaceNodeID());
        
        assertNotNull("Returned node should not be null", result);
    }
    
    @Test
    public void getNodeWithNullURLs()
            throws MalformedURLException, URISyntaxException, WorkspaceNodeNotFoundException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceNode(testNode.getWorkspaceNodeID());
        
        assertNotNull("Returned node should not be null", result);
    }
    
    @Test
    public void getWorkspaceTopNode()
            throws MalformedURLException, URISyntaxException, WorkspaceNodeNotFoundException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        testWorkspace.setTopNodeID(testNode.getWorkspaceNodeID());
        testWorkspace.setTopNodeArchiveURI(testNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, testNode);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceTopNode(testWorkspace.getWorkspaceID());
        
        assertNotNull("Returned node should not be null", result);
        assertEquals("Returned node is different from expected", testNode, result);
    }
    
    @Test
    public void getNotFoundWorkspaceTopNode()
            throws MalformedURLException, URISyntaxException, WorkspaceNodeNotFoundException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        testWorkspace.setTopNodeArchiveURI(testNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, testNode);
        
        String expectedErrorMessage = "Top node for workspace with ID " + testWorkspace.getWorkspaceID() + " does not exist in the database";
        
        try {
            this.workspaceDao.getWorkspaceTopNode(testWorkspace.getWorkspaceID());
        } catch (WorkspaceNodeNotFoundException ex) {
            assertEquals("Message different from expected", expectedErrorMessage, ex.getMessage());
            assertEquals("Workspace ID should not be known", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertEquals("Node ID should not be known", -1, ex.getWorkspaceNodeID());
            assertTrue("Cause has a different type from expected", ex.getCause() instanceof EmptyResultDataAccessException);
        }
    }
    
    @Test
    public void getWorkspaceTopNodeID() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        testWorkspace.setTopNodeArchiveURI(testNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, testNode);
        
        int retrievedNodeID = this.workspaceDao.getWorkspaceTopNodeID(testWorkspace.getWorkspaceID());
        
        assertEquals("Retrieved node ID different from expected", testNode.getWorkspaceNodeID(), retrievedNodeID);
    }
    
    @Test
    public void nodeIsTopNodeOfWorkspace() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        testWorkspace.setTopNodeArchiveURI(testNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, testNode);
        
        boolean result = this.workspaceDao.isTopNodeOfWorkspace(testWorkspace.getWorkspaceID(), testNode.getWorkspaceNodeID());
        
        assertTrue("Result should be true", result);
    }
    
    @Test
    public void nodeIsNotTopNodeOfWorkspace() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        testWorkspace.setTopNodeArchiveURI(testNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, testNode);
        
        URI anotherTestURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL anotherTestURL = new URL("file:/archive/folder/anothertest.cmdi");
        WorkspaceNode anotherTestNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, anotherTestURI, anotherTestURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        boolean result = this.workspaceDao.isTopNodeOfWorkspace(testWorkspace.getWorkspaceID(), anotherTestNode.getWorkspaceNodeID());
        
        assertFalse("Result should be false", result);
    }
    
    @Test
    public void getExistingNodesForWorkspace() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getNodesForWorkspace(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 2, result.size());
        assertTrue("The returned list of nodes does not contain the expected nodes", result.contains(parentNode) && result.contains(childNode));
    }
    
    @Test
    public void getNonExistingNodesForWorkspace() {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getNodesForWorkspace(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Returned list of nodes should be empty", 0, result.size());

    }
    
    @Test
    public void getExistingMetadataNodesInTreeForWorkspace_workspaceHasNoUnlinkedNodes()
            throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        URI topNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topNodeURL = new URL("file:/archive/folder/top.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topNodeURI, topNodeURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        testWorkspace.setTopNodeArchiveURI(topNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, parentNode);
        
        URI childURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_1 = new URL("file:/archive/folder/child_1.cmdi");
        WorkspaceNode childNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_1, childURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_1);
        URI childURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_2 = new URL("file:/archive/folder/child_2.txt");
        WorkspaceNode childNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_2, childURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_2);
        URI childURI_3 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_3 = new URL("file:/archive/folder/child_3.cmdi");
        WorkspaceNode childNode_3 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_3, childURL_3, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_3);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getMetadataNodesInTreeForWorkspace(testWorkspace.getWorkspaceID());
        
        assertTrue("Top node missing from the result", result.contains(topNode));
        assertTrue("Parent node missing from the result", result.contains(parentNode));
        assertTrue("First child node missing from the result", result.contains(childNode_1));
        assertFalse("Second child shouldn't be part of the result", result.contains(childNode_2));
        assertTrue("Third child node missing from the result", result.contains(childNode_3));
    }
    
    @Test
    public void getExistingMetadataNodesInTreeForWorkspace_workspaceHasUnlinkedNodes()
            throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        URI topNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topNodeURL = new URL("file:/archive/folder/top.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topNodeURI, topNodeURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        testWorkspace.setTopNodeArchiveURI(topNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, parentNode);
        
        URI childURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_1 = new URL("file:/archive/folder/child_1.cmdi");
        WorkspaceNode childNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_1, childURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_1);
        URI childURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_2 = new URL("file:/archive/folder/child_2.cmdi");
        WorkspaceNode childNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_2, childURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_2);
        URI childURI_3 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_3 = new URL("file:/archive/folder/child_3.txt");
        WorkspaceNode childNode_3 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_3, childURL_3, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_3);
        
        
        URI unlinkedNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL unlinkedNodeURL = new URL("file:/archive/folder/unlinked.cmdi");
        WorkspaceNode unlinkedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, unlinkedNodeURI, unlinkedNodeURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        URI deletedNodeURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL deletedNodeURL = new URL("file:/archive/folder/deleted.cmdi");
        WorkspaceNode deletedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, deletedNodeURI, deletedNodeURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsDeleted(deletedNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getMetadataNodesInTreeForWorkspace(testWorkspace.getWorkspaceID());
        
        assertTrue("Top node missing from the result", result.contains(topNode));
        assertTrue("Parent node missing from the result", result.contains(parentNode));
        assertTrue("First child node missing from the result", result.contains(childNode_1));
        assertTrue("Second child node missing from the result", result.contains(childNode_2));
        assertFalse("Third child node shouldn't be part of the result", result.contains(childNode_3));
        assertFalse("Unlinked node shouldn't be part of the result", result.contains(unlinkedNode));
        assertFalse("Deleted node shouldn't be part of the result", result.contains(deletedNode));
    }
    
    @Test
    public void getNonExistingMetadataNodesInTreeForWorkspace() {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getMetadataNodesInTreeForWorkspace(testWorkspace.getWorkspaceID());
        
        assertTrue("Result should be empty", result.isEmpty());
    }
    
    @Test
    public void getExistingChildNodes() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getChildWorkspaceNodes(parentNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(childNode));
    }

    @Test
    public void getNonExistingChildNodes() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getChildWorkspaceNodes(parentNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertTrue("Returned list of nodes should be empty", result.isEmpty());
    }

    @Test
    public void getChildNodesFromNonExistingParent() throws URISyntaxException, MalformedURLException {
        
        Collection<WorkspaceNode> result = this.workspaceDao.getChildWorkspaceNodes(1);
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Returned list of nodes should be empty", 0, result.size());
    }
    
    @Test
    public void getExistingDescendantNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI childURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_1 = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_1, childURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_1);
        URI childURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_2 = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_2, childURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_2);
        URI subChildURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL subChildURL_1 = new URL("file:/archive/folder/subchild.cmdi");
        WorkspaceNode subChildNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, subChildURI_1, subChildURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(childNode_1, subChildNode_1);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDescendantWorkspaceNodes(parentNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 3, result.size());
        assertTrue("The returned list of nodes does not contain one of the expected nodes (child_1)", result.contains(childNode_1));
        assertTrue("The returned list of nodes does not contain one of the expected nodes (child_2)", result.contains(childNode_2));
        assertTrue("The returned list of nodes does not contain one of the expected nodes (subChild_1)", result.contains(subChildNode_1));
    }
    
    @Test
    public void getNonExistingDescendantNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDescendantWorkspaceNodes(parentNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertTrue("Returned list of nodes should be empty", result.isEmpty());
    }
    
    @Test
    public void getDescendantNodesFromNonExistingParent() {
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDescendantWorkspaceNodes(1);
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Returned list of nodes should be empty", 0, result.size());
    }
    
    @Test
    public void getExistingDescendantNodesByType() throws MalformedURLException, URISyntaxException {
        
        WorkspaceNodeType typeToFilter = WorkspaceNodeType.METADATA;
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI childURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_1 = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_1, childURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_1);
        URI childURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL_2 = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI_2, childURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode_2);
        URI subChildURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL subChildURL_1 = new URL("file:/archive/folder/subchild.cmdi");
        WorkspaceNode subChildNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, subChildURI_1, subChildURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(childNode_1, subChildNode_1);
        URI subChildURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL subChildURL_2 = new URL("file:/archive/folder/subchild.txt");
        WorkspaceNode subChildNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, subChildURI_2, subChildURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(childNode_1, subChildNode_2);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDescendantWorkspaceNodesByType(parentNode.getWorkspaceNodeID(), typeToFilter);
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 3, result.size());
        assertTrue("The returned list of nodes does not contain one of the expected nodes (child_1)", result.contains(childNode_1));
        assertTrue("The returned list of nodes does not contain one of the expected nodes (child_2)", result.contains(childNode_2));
        assertTrue("The returned list of nodes does not contain one of the expected nodes (subChild_1)", result.contains(subChildNode_1));
        assertFalse("The returned list of nodes contains an unexpected node (subChild_2)", result.contains(subChildNode_2));
    }
    
    @Test
    public void getNonExistingDescendantNodesByType() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDescendantWorkspaceNodesByType(parentNode.getWorkspaceNodeID(), WorkspaceNodeType.METADATA);
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertTrue("Returned list of nodes should be empty", result.isEmpty());
    }
    
    @Test
    public void getDescendantNodesByTypeFromNonExistingParent() {
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDescendantWorkspaceNodesByType(1, WorkspaceNodeType.METADATA);
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Returned list of nodes should be empty", 0, result.size());
    }
    
    @Test
    public void getExistingParentNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getParentWorkspaceNodes(childNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(parentNode));
    }
    
    @Test
    public void getNonExistingParentNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getParentWorkspaceNodes(childNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertTrue("Returned list of nodes should be empty", result.isEmpty());
    }
    
    @Test
    public void getParentNodesFromNonExistingChild() throws URISyntaxException, MalformedURLException {
        
        Collection<WorkspaceNode> result = this.workspaceDao.getParentWorkspaceNodes(2);
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertTrue("Returned list of nodes should be empty", result.isEmpty());
    }
    
    @Test
    public void getTheOnlyDeletedNode() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsDeleted(testNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedAndDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(testNode));
    }
    
    @Test
    public void getTheOnlyUnlinkedNode() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedAndDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(testNode));
    }
    
    @Test
    public void getDeletedNodeWithChildren() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
        setNodeAsDeleted(parentNode);
        setNodeAsDeleted(childNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedAndDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(parentNode));
    }
    
    @Test
    public void getTheOnlyUnlinkedNodeInCurrentWorkspace() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Workspace anotherTestWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI anotherTestURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL anotherTestURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode anotherTestNode = insertTestWorkspaceNodeWithUriIntoDB(anotherTestWorkspace, anotherTestURI, anotherTestURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedAndDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(testNode));
    }
    
    @Test
    public void getSeveralDeletedNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI firstURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL firstURL = new URL("file:/archive/folder/first.cmdi");
        WorkspaceNode firstNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, firstURI, firstURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI secondURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL secondURL = new URL("file:/archive/folder/second.cmdi");
        WorkspaceNode secondNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, secondURI, secondURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsDeleted(firstNode);
        setNodeAsDeleted(secondNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedAndDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 2, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(firstNode));
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(secondNode));
    }
    
    @Test
    public void getSeveralUnlinkedAndDeletedNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI firstURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL firstURL = new URL("file:/archive/folder/first.cmdi");
        WorkspaceNode firstNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, firstURI, firstURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI secondURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL secondURL = new URL("file:/archive/folder/second.cmdi");
        WorkspaceNode secondNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, secondURI, secondURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsDeleted(firstNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedAndDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 2, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(firstNode));
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(secondNode));
    }
    
    @Test
    public void getNonExistingUnlinkedAndDeletedTopNodes() throws URISyntaxException, MalformedURLException {
        
        //top node of the workspace shouldn't be retrieved
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI firstURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL firstURL = new URL("file:/archive/folder/first.cmdi");
        WorkspaceNode firstNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, firstURI, firstURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, firstNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedAndDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertTrue("Returned list of nodes should be empty", result.isEmpty());
    }
    
    @Test
    public void getUnlinkedNodesOneNode() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        URI unlinkedURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL unlinkedURL = new URL("file:/archive/folder/unlinkednode.cmdi");
        WorkspaceNode unlinkedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, unlinkedURI, unlinkedURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        List<WorkspaceNode> result = this.workspaceDao.getUnlinkedNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("List of unlinked nodes should not be null", result);
        assertTrue("List of unlinked nodes has a different size than what was expected", result.size() == 1);
        assertEquals("Node in the list of unlinked nodes different from expected", unlinkedNode, result.iterator().next());
    }

    @Test
    public void getUnlinkedNodesZeroNodes() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        
        List<WorkspaceNode> result = this.workspaceDao.getUnlinkedNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("List of unlinked nodes should not be null", result);
        assertTrue("List of unlinked nodes has a different size than what was expected", result.isEmpty());
    }
    
    @Test
    public void getUnlinkedNodesDeletedNode() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        URI deletedURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL deletedURL = new URL("file:/archive/folder/deletednode.cmdi");
        WorkspaceNode deletedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, deletedURI, deletedURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsDeleted(deletedNode);
        
        List<WorkspaceNode> result = this.workspaceDao.getUnlinkedNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("List of unlinked nodes should not be null", result);
        assertTrue("List of unlinked nodes has a different size than what was expected", result.isEmpty());
    }
    
    @Test
    public void getUnlinkedNodesExternalDeletedNode() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        URI externalDeletedURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL externalDeletedURL = new URL("file:/archive/folder/deletednode.cmdi");
        WorkspaceNode externalDeletedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, externalDeletedURI, externalDeletedURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsExternalDeleted(externalDeletedNode);
        
        List<WorkspaceNode> result = this.workspaceDao.getUnlinkedNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("List of unlinked nodes should not be null", result);
        assertTrue("List of unlinked nodes has a different size than what was expected", result.isEmpty());
    }
    
    @Test
    public void getUnlinkedNodesReplacedNode() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        URI deletedURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL deletedURL = new URL("file:/archive/folder/deletednode.cmdi");
        WorkspaceNode deletedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, deletedURI, deletedURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsReplaced(deletedNode);
        
        List<WorkspaceNode> result = this.workspaceDao.getUnlinkedNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("List of unlinked nodes should not be null", result);
        assertTrue("List of unlinked nodes has a different size than what was expected", result.isEmpty());
    }
    
    @Test
    public void getUnlinkedNodesAndDescendants() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        
        URI unlinkedURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL unlinkedURL = new URL("file:/archive/folder/unlinkednode.cmdi");
        WorkspaceNode unlinkedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, unlinkedURI, unlinkedURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        URI unlinkedNodeChildURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL unlinkedNodeChildURL_1 = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode unlinkedNodeChildNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, unlinkedNodeChildURI_1, unlinkedNodeChildURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(unlinkedNode, unlinkedNodeChildNode_1);
        URI unlinkedNodeChildURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL unlinkedNodeChildURL_2 = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode unlinkedNodeChildNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, unlinkedNodeChildURI_2, unlinkedNodeChildURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(unlinkedNode, unlinkedNodeChildNode_2);
        URI unlinkedNodeSubChildURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL unlinkedNodeSubChildURL_1 = new URL("file:/archive/folder/subchild.cmdi");
        WorkspaceNode unlinkedNodeSubChildNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, unlinkedNodeSubChildURI_1, unlinkedNodeSubChildURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(unlinkedNodeChildNode_1, unlinkedNodeSubChildNode_1);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getUnlinkedNodesAndDescendants(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 4, result.size());
        assertTrue("The returned list of nodes does not contain one of the expected nodes (unlinkedNode)", result.contains(unlinkedNode));
        assertTrue("The returned list of nodes does not contain one of the expected nodes (child_1)", result.contains(unlinkedNodeChildNode_1));
        assertTrue("The returned list of nodes does not contain one of the expected nodes (child_2)", result.contains(unlinkedNodeChildNode_2));
        assertTrue("The returned list of nodes does not contain one of the expected nodes (subChild_1)", result.contains(unlinkedNodeSubChildNode_1));
    }
    
    @Test
    public void deleteWorkspaceNodeLink() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        WorkspaceNode parentNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        WorkspaceNode childNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
        int intermediateNumberOfRows = countRowsInTable("node_link");
        assertTrue("Node link was not inserted in the database", intermediateNumberOfRows == initialNumberOfRows + 1);
        
        this.workspaceDao.deleteWorkspaceNodeLink(
                testWorkspace.getWorkspaceID(), parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
        int finalNumberOfRows = countRowsInTable("node_link");
        
        assertTrue("Node link was not deleted from the database", finalNumberOfRows == intermediateNumberOfRows - 1);
        
        WorkspaceNodeLink nodeLink = getNodeLinkFromDB(parentNode.getWorkspaceNodeID(), childNode.getWorkspaceNodeID());
        assertNull("Node link should not exist in the database", nodeLink);
    }
    
    @Test
    public void setWorkspaceNodeAsDeleted() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        WorkspaceNode testNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        int intermediateNumberOfRows = countRowsInTable("node");
        assertTrue("Node was not inserted in the database", intermediateNumberOfRows == initialNumberOfRows + 1);
        
        this.workspaceDao.setWorkspaceNodeAsDeleted(
                testWorkspace.getWorkspaceID(), testNode.getWorkspaceNodeID(), Boolean.FALSE);
        int finalNumberOfRows = countRowsInTable("node");
        
        assertTrue("Node should not be deleted from the database, just set as deleted", finalNumberOfRows == intermediateNumberOfRows);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        assertNotNull("Node should exist in the database", retrievedNode);
        assertEquals("Node should be set as deleted", WorkspaceNodeStatus.DELETED, retrievedNode.getStatus());
    }
    
    @Test
    public void setExternalWorkspaceNodeAsDeleted() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        WorkspaceNode testNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        testNode.setStatus(WorkspaceNodeStatus.EXTERNAL);
        int intermediateNumberOfRows = countRowsInTable("node");
        assertTrue("Node was not inserted in the database", intermediateNumberOfRows == initialNumberOfRows + 1);
        
        this.workspaceDao.setWorkspaceNodeAsDeleted(
                testNode.getWorkspaceID(), testNode.getWorkspaceNodeID(), Boolean.TRUE);
        int finalNumberOfRows = countRowsInTable("node");
        
        assertTrue("Node should not be deleted from the database, just set as deleted", finalNumberOfRows == intermediateNumberOfRows);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        assertNotNull("Node should exist in the database", retrievedNode);
        assertEquals("Node should be set as deleted", WorkspaceNodeStatus.EXTERNAL_DELETED, retrievedNode.getStatus());
    }
    
    @Test
    public void deleteWorkspaceNode_withReplacementsAndLinks() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfNodes = countRowsInTable("node");
        int initialNumberOfLinks = countRowsInTable("node_link");
        int initialNumberOfReplacements = countRowsInTable("node_replacement");
        
        Workspace ws = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode node1 = insertTestWorkspaceNodeIntoDB(ws);
        WorkspaceNode node2 = insertTestWorkspaceNodeIntoDB(ws);
        WorkspaceNode node3 = insertTestWorkspaceNodeIntoDB(ws);
        WorkspaceNode node4 = insertTestWorkspaceNodeIntoDB(ws);
        WorkspaceNode node5 = insertTestWorkspaceNodeIntoDB(ws);
        WorkspaceNode node6 = insertTestWorkspaceNodeIntoDB(ws);
        setNodeAsParentAndInsertLinkIntoDatabase(node1, node2);
        setNodeAsParentAndInsertLinkIntoDatabase(node2, node3);
        setNodeAsParentAndInsertLinkIntoDatabase(node2, node4);
        setNodeAsReplacedAndAddReplacementInDatabase(node5, node6);
        setNodeAsParentAndInsertLinkIntoDatabase(node2, node6);
        
        int intermediateNumberOfNodes = countRowsInTable("node");
        int intermediateNumberOfLinks = countRowsInTable("node_link");
        int intermediateNumberOfReplacements = countRowsInTable("node_replacement");
        
        assertEquals("Intermediate number of nodes different from expected", initialNumberOfNodes + 6, intermediateNumberOfNodes);
        assertEquals("Intermediate number of links different from expected", initialNumberOfLinks + 4, intermediateNumberOfLinks);
        assertEquals("Intermediate number of replacements different from expected", initialNumberOfReplacements + 1, intermediateNumberOfReplacements);
        
        this.workspaceDao.deleteWorkspaceNode(node6.getWorkspaceID(), node6.getWorkspaceNodeID());
        
        int finalNumberOfNodes = countRowsInTable("node");
        int finalNumberOfLinks = countRowsInTable("node_link");
        int finalNumberOfReplacements = countRowsInTable("node_replacement");
        
        assertEquals("Final number of nodes different from expected", intermediateNumberOfNodes - 1, finalNumberOfNodes);
        assertEquals("Final number of links different from expected", intermediateNumberOfLinks - 1, finalNumberOfLinks);
        assertEquals("Final number of replacements different from expected", intermediateNumberOfReplacements - 1, finalNumberOfReplacements);
        
        WorkspaceNode retrievedNode = getNodeFromDB(node6.getWorkspaceNodeID());
        assertNull("Node should not exist in the database", retrievedNode);
    }
    
    @Test
    public void cleanWorkspaceNodesAndLinks() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfWorkspaces = countRowsInTable("workspace");
        int initialNumberOfNodes = countRowsInTable("node");
        int initialNumberOfLinks = countRowsInTable("node_link");
        int initialNumberOfLocks = countRowsInTable("node_lock");
        int initialNumberOfReplacements = countRowsInTable("node_replacement");
        
        Workspace ws = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI firstNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL firstNodeURL = new URL("https://archive/location/firstNode.cmdi");
        WorkspaceNode firstNode = insertTestWorkspaceNodeWithUriIntoDB(ws, firstNodeURI, firstNodeURL, firstNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(firstNode);
        URI secondNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL secondNodeURL = new URL("https://archive/location/secondNode.cmdi");
        WorkspaceNode secondNode = insertTestWorkspaceNodeWithUriIntoDB(ws, secondNodeURI, secondNodeURL, secondNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(secondNode);
        URI thirdNodeURI = URI.create("hdl:11111/" + UUID.randomUUID().toString());
        URL thirdNodeURL = new URL("https://archive/location/firstNode.cmdi");
        WorkspaceNode thirdNode = insertTestWorkspaceNodeWithUriIntoDB(ws, thirdNodeURI, thirdNodeURL, thirdNodeURL.toURI(), Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        addNodeLockToDb(thirdNode);
        setNodeAsParentAndInsertLinkIntoDatabase(firstNode, secondNode);
        setNodeAsReplacedAndAddReplacementInDatabase(secondNode, thirdNode);
        
        int intermediateNumberOfWorkspaces = countRowsInTable("workspace");
        int intermediateNumberOfNodes = countRowsInTable("node");
        int intermediateNumberOfLinks = countRowsInTable("node_link");
        int intermediateNumberOfLocks = countRowsInTable("node_lock");
        int intermediateNumberOfReplacements = countRowsInTable("node_replacement");
        
        assertEquals("Intermediate number of workspaces different from expected", initialNumberOfWorkspaces + 1, intermediateNumberOfWorkspaces);
        assertEquals("Intermediate number of nodes different from expected", initialNumberOfNodes + 3, intermediateNumberOfNodes);
        assertEquals("Intermediate number of links different from expected", initialNumberOfLinks + 1, intermediateNumberOfLinks);
        assertEquals("Intermediate number of locks different from expected", initialNumberOfLocks + 3, intermediateNumberOfLocks);
        assertEquals("Intermediate number of replacements different from expected", initialNumberOfReplacements + 1, intermediateNumberOfReplacements);
        
        this.workspaceDao.cleanWorkspaceNodesAndLinks(ws);
        
        int finalNumberOfWorkspaces = countRowsInTable("workspace");
        int finalNumberOfNodes = countRowsInTable("node");
        int finalNumberOfLinks = countRowsInTable("node_link");
        int finalNumberOfLocks = countRowsInTable("node_lock");
        int finalNumberOfReplacements = countRowsInTable("node_replacement");
        
        assertEquals("Final number of workspaces different from expected", intermediateNumberOfWorkspaces, finalNumberOfWorkspaces);
        assertEquals("Final number of nodes different from expected", initialNumberOfNodes, finalNumberOfNodes);
        assertEquals("Final number of links different from expected", initialNumberOfLinks, finalNumberOfLinks);
        assertEquals("Final number of locks different from expected", initialNumberOfLocks, finalNumberOfLocks);
        assertEquals("Final number of replacements different from expected", initialNumberOfReplacements, finalNumberOfReplacements);
    }
    
    @Test
    public void getNewerVersionOfNodeExists() throws URISyntaxException, MalformedURLException, WorkspaceNodeNotFoundException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URI oldURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL oldURL = new URL("file:/archive/folder/oldnode.cmdi");
        WorkspaceNode oldNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, oldURI, oldURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        URI newURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL newURL = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, newURI, newURL, null, Boolean.TRUE, WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode);
        
        setNodeAsReplacedAndAddReplacementInDatabase(oldNode, newNode);
        
        WorkspaceNode retrievedNode = this.workspaceDao.getNewerVersionOfNode(testWorkspace.getWorkspaceID(), oldNode.getWorkspaceNodeID());
        
        assertEquals("Retrieved node different from expected", newNode, retrievedNode);
    }
    
    @Test
    public void getNewerVersionOfNodeDoesNotExist() throws URISyntaxException, MalformedURLException, WorkspaceNodeNotFoundException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URI oldURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL oldURL = new URL("file:/archive/folder/oldnode.cmdi");
        WorkspaceNode oldNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, oldURI, oldURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsReplaced(oldNode);
        
        URI newURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL newURL = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, newURI, newURL, null, Boolean.TRUE, WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode);
        
        String expectedExceptionMessage = "Newer version of node with ID " + oldNode.getWorkspaceNodeID() + " not found in the database";
        
        try {
            this.workspaceDao.getNewerVersionOfNode(testWorkspace.getWorkspaceID(), oldNode.getWorkspaceNodeID());
            fail("should have thrown exception");
        } catch(WorkspaceNodeNotFoundException ex) {
            assertEquals("Exception message different from expected", expectedExceptionMessage, ex.getMessage());
            assertEquals("workspaceID in Exception different from expected", testWorkspace.getWorkspaceID(), ex.getWorkspaceID());
            assertTrue("Cause of exception different from expected", ex.getCause() instanceof EmptyResultDataAccessException);
        }
    }
    
    @Test
    public void replaceNode() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_replacement");
        
        Workspace ws = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        WorkspaceNode node1 = insertTestWorkspaceNodeIntoDB(ws);
        setNodeAsCopy(node1);
        WorkspaceNode node2 = insertTestWorkspaceNodeIntoDB(ws);
        
        this.workspaceDao.replaceNode(node1, node2);

        int finalNumberOfRows = countRowsInTable("node_replacement");
        assertEquals("An entry should have been added in the node_replacement table", finalNumberOfRows, initialNumberOfRows + 1);
        
        int olderNodeID = getOlderNode(node2.getWorkspaceNodeID());
        assertEquals("Older version of node different from expected", node1.getWorkspaceNodeID(), olderNodeID);
        int newerNodeID = getNewerNode(node1.getWorkspaceNodeID());
        assertEquals("Newer version of node different from expected", node2.getWorkspaceNodeID(), newerNodeID);
        
        WorkspaceNode retrievedNode1 = getNodeFromDB(node1.getWorkspaceNodeID());
        assertFalse("Old node should have been changed", node1.equals(retrievedNode1));
        assertEquals("Old node should have been set as replaced", WorkspaceNodeStatus.REPLACED, retrievedNode1.getStatus());
        
        WorkspaceNode retrievedNode2 = getNodeFromDB(node2.getWorkspaceNodeID());
        assertEquals("New node should not have been changed", node2, retrievedNode2);
    }
    
    @Test
    public void replaceNewNode() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_replacement");
        
        Workspace ws = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        WorkspaceNode node1 = insertTestWorkspaceNodeIntoDB(ws);
        WorkspaceNode node2 = insertTestWorkspaceNodeIntoDB(ws);
        
        this.workspaceDao.replaceNode(node1, node2);

        int finalNumberOfRows = countRowsInTable("node_replacement");
        assertEquals("No entry should have been added in the node_replacement table", finalNumberOfRows, initialNumberOfRows);
        
//        int olderNodeID = getOlderNode(node2.getWorkspaceNodeID());
//        assertEquals("Older version of node different from expected", node1.getWorkspaceNodeID(), olderNodeID);
//        int newerNodeID = getNewerNode(node1.getWorkspaceNodeID());
//        assertEquals("Newer version of node different from expected", node2.getWorkspaceNodeID(), newerNodeID);
        
        WorkspaceNode retrievedNode1 = getNodeFromDB(node1.getWorkspaceNodeID());
        assertFalse("Old node should have been changed", node1.equals(retrievedNode1));
        assertEquals("Old node should have been set as deleted", WorkspaceNodeStatus.DELETED, retrievedNode1.getStatus());
        
        WorkspaceNode retrievedNode2 = getNodeFromDB(node2.getWorkspaceNodeID());
        assertEquals("New node should not have been changed", node2, retrievedNode2);
    }
    
    @Test
    public void getAllNodeReplacements() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URI oldURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL oldURL_1 = new URL("file:/archive/folder/oldnode.cmdi");
        WorkspaceNode oldNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, oldURI_1, oldURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        URI oldURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL oldURL_2 = new URL("file:/archive/folder/oldnode.cmdi");
        WorkspaceNode oldNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, oldURI_2, oldURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        URI newURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL newURL_1 = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, newURI_1, newURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode_1);
        
        URI newURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL newURL_2 = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, newURI_2, newURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode_2);
        
        setNodeAsReplacedAndAddReplacementInDatabase(oldNode_1, newNode_1);
        WorkspaceNodeReplacement replacement_1 =
                new LamusWorkspaceNodeReplacement(oldNode_1.getArchiveURI(), newNode_1.getArchiveURI());
        
        setNodeAsReplacedAndAddReplacementInDatabase(oldNode_2, newNode_2);
        WorkspaceNodeReplacement replacement_2 =
                new LamusWorkspaceNodeReplacement(oldNode_2.getArchiveURI(), newNode_2.getArchiveURI());
        
        Collection<WorkspaceNodeReplacement> retrievedCollection = this.workspaceDao.getAllNodeReplacements();
        
        assertTrue("Retrieved collection of replacements has different size from expected", retrievedCollection.size() == 2);
        assertTrue("Not all expected replacements are present in the collection", retrievedCollection.contains(replacement_1) && retrievedCollection.contains(replacement_2));
    }
    
    @Test
    public void getAllNodeReplacementWithoutArchiveURI() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URL oldURL_1 = new URL("file:/archive/folder/oldnode.cmdi");
        WorkspaceNode oldNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, null, oldURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        
        URL newURL_1 = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, null, newURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.UPLOADED, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode_1);
        
        //node replacement should contain the archiveURL, since archiveURI is null
        
        setNodeAsReplacedAndAddReplacementInDatabase(oldNode_1, newNode_1);
        WorkspaceNodeReplacement replacement_1 =
                new LamusWorkspaceNodeReplacement(oldNode_1.getArchiveURL().toURI(), newNode_1.getArchiveURL().toURI());
        
        Collection<WorkspaceNodeReplacement> retrievedCollection = this.workspaceDao.getAllNodeReplacements();
        
        assertTrue("Retrieved collection of replacements has different size from expected", retrievedCollection.size() == 1);
        assertTrue("Not all expected replacements are present in the collection", retrievedCollection.contains(replacement_1));
    }
    
    @Test
    public void getAllNodeReplacementWithExternalNode() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URL oldURL_1 = new URL("file:/archive/folder/oldnode.cmdi");
        WorkspaceNode oldNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, null, null, oldURL_1.toURI(), Boolean.TRUE, WorkspaceNodeStatus.EXTERNAL, Boolean.FALSE);
        oldNode_1.setStatus(WorkspaceNodeStatus.EXTERNAL);
        oldNode_1.setOriginURI(oldURL_1.toURI());
          
        URL newURL_1 = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, null, null, newURL_1.toURI(), Boolean.TRUE, WorkspaceNodeStatus.EXTERNAL, Boolean.FALSE);
        newNode_1.setStatus(WorkspaceNodeStatus.EXTERNAL);
        newNode_1.setOriginURI(newURL_1.toURI());
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode_1);
        
        //node replacements should contain the originURL, since both archiveURI and archiveURL are null
        
        setNodeAsReplacedAndAddReplacementInDatabase(oldNode_1, newNode_1);
        WorkspaceNodeReplacement replacement_1 =
                new LamusWorkspaceNodeReplacement(oldNode_1.getOriginURI(), newNode_1.getOriginURI());
        
        Collection<WorkspaceNodeReplacement> retrievedCollection = this.workspaceDao.getAllNodeReplacements();
        
        assertTrue("Retrieved collection of replacements has different size from expected", retrievedCollection.size() == 1);
        assertTrue("Not all expected replacements are present in the collection", retrievedCollection.contains(replacement_1));
    }
    
    @Test
    public void getAllNodeReplacementsEmpty() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        
        URI newURI_1 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL newURL_1 = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode_1 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, newURI_1, newURL_1, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode_1);
        
        URI newURI_2 = URI.create("hdl:11142/" + UUID.randomUUID().toString());
        URL newURL_2 = new URL("file:/archive/folder/newnode.cmdi");
        WorkspaceNode newNode_2 = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, newURI_2, newURL_2, null, Boolean.TRUE, WorkspaceNodeStatus.ARCHIVE_COPY, Boolean.FALSE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, newNode_2);
        
        Collection<WorkspaceNodeReplacement> retrievedCollection = this.workspaceDao.getAllNodeReplacements();
        
        assertTrue("Retrieved collection of replacements has different size from expected", retrievedCollection.isEmpty());
    }
    

    private Workspace insertTestWorkspaceWithDefaultUserIntoDB(boolean withEndDates) {
        return insertTestWorkspaceWithGivenUserIntoDB("someUser", withEndDates);
    }
    
    private Workspace insertTestWorkspaceWithGivenUserIntoDB(String userID, boolean withEndDates) {
        
        Workspace testWorkspace = new LamusWorkspace(userID, 0L, 10000000L);
        Date now = Calendar.getInstance().getTime();
        if(withEndDates) {
            testWorkspace.setEndDate(now);
            testWorkspace.setSessionEndDate(now);
        }
        
        Timestamp endDate = null;
        if(testWorkspace.getEndDate() != null) {
            endDate = new Timestamp(testWorkspace.getEndDate().getTime());
        }
        Timestamp sessionEndDate = null;
        if(testWorkspace.getSessionEndDate() != null) {
            sessionEndDate = new Timestamp(testWorkspace.getSessionEndDate().getTime());
        }
        
        String insertWorkspaceSql = "INSERT INTO workspace (user_id, start_date, end_date, session_start_date, session_end_date, used_storage_space, max_storage_space, status, message, crawler_id)" +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertWorkspaceSql,
                testWorkspace.getUserID(),
                new Timestamp(testWorkspace.getStartDate().getTime()),
                endDate,
                new Timestamp(testWorkspace.getSessionStartDate().getTime()),
                sessionEndDate,
                testWorkspace.getUsedStorageSpace(),
                testWorkspace.getMaxStorageSpace(),
                testWorkspace.getStatus().name(),
                testWorkspace.getMessage(),
                testWorkspace.getCrawlerID());

        int workspaceID = getIdentityFromDB();
        testWorkspace.setWorkspaceID(workspaceID);
        
        return testWorkspace;
    }
    
    private WorkspaceNode insertTestWorkspaceNodeIntoDB(Workspace workspace) throws URISyntaxException, MalformedURLException {
        
        WorkspaceNode testWorkspaceNode = createWorkspaceNode(workspace, null, null, null, Boolean.FALSE, WorkspaceNodeStatus.CREATED, Boolean.FALSE);
        
        String insertNodeSql = "INSERT INTO node (workspace_id, workspace_url, name, type, format, protected, status) values (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertNodeSql, testWorkspaceNode.getWorkspaceID(),
                testWorkspaceNode.getWorkspaceURL(), testWorkspaceNode.getName(),
                testWorkspaceNode.getType().name(), testWorkspaceNode.getFormat(), testWorkspaceNode.isProtected(), testWorkspaceNode.getStatus().name());
        
        int workspaceNodeID = getIdentityFromDB();
        testWorkspaceNode.setWorkspaceNodeID(workspaceNodeID);
        
        return testWorkspaceNode;
    }
    
    private WorkspaceNode insertTestWorkspaceNodeWithUriIntoDB(Workspace workspace, URI archiveURI, URL archiveURL, URI originURI,
            boolean withProfileSchemaURI, WorkspaceNodeStatus status, boolean isProtected)
                throws MalformedURLException, URISyntaxException {
        
        WorkspaceNode testWorkspaceNode = createWorkspaceNode(workspace, archiveURI, archiveURL, originURI, withProfileSchemaURI, status, isProtected);
        
        String insertNodeSql = "INSERT INTO node (workspace_id, profile_schema_uri, workspace_url, archive_uri, archive_url, origin_url, name, type, format, protected, status) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertNodeSql, testWorkspaceNode.getWorkspaceID(),
                testWorkspaceNode.getProfileSchemaURI(), testWorkspaceNode.getWorkspaceURL(),
                testWorkspaceNode.getArchiveURI(), testWorkspaceNode.getArchiveURL(), testWorkspaceNode.getOriginURI(), testWorkspaceNode.getName(),
                testWorkspaceNode.getType().name(), testWorkspaceNode.getFormat(), testWorkspaceNode.isProtected(), testWorkspaceNode.getStatus().name());
        
        int workspaceNodeID = getIdentityFromDB();
        testWorkspaceNode.setWorkspaceNodeID(workspaceNodeID);
        
        return testWorkspaceNode;
    }
    
    private WorkspaceNode createWorkspaceNode(Workspace workspace, URI archiveURI, URL archiveURL, URI originURI, boolean withProfileSchemaURI, WorkspaceNodeStatus status, boolean isProtected)
            throws URISyntaxException, MalformedURLException {
        
        WorkspaceNode testWorkspaceNode = new LamusWorkspaceNode();
        testWorkspaceNode.setWorkspaceID(workspace.getWorkspaceID());
        if(withProfileSchemaURI) {
            testWorkspaceNode.setProfileSchemaURI(URI.create("http://some.schema.xsd"));
        }

        testWorkspaceNode.setWorkspaceURL(standardWorkspaceUrlForNode);
        testWorkspaceNode.setArchiveURI(archiveURI);
        testWorkspaceNode.setArchiveURL(archiveURL);
        testWorkspaceNode.setOriginURI(originURI);

        testWorkspaceNode.setName("someNode");
        if( (archiveURL != null && archiveURL.toString().endsWith(".cmdi")) || (originURI != null && originURI.toString().endsWith(".cmdi")) ) {
            testWorkspaceNode.setType(WorkspaceNodeType.METADATA);
        } else {
            testWorkspaceNode.setType(WorkspaceNodeType.RESOURCE_OTHER);
        }
        testWorkspaceNode.setFormat("someFormat");
        testWorkspaceNode.setStatus(status);
        testWorkspaceNode.setProtected(isProtected);
        
        return testWorkspaceNode;
    }
    
    private void setNodeAsWorkspaceTopNodeInDB(Workspace workspace, WorkspaceNode topNode) {
        
        workspace.setTopNodeID(topNode.getWorkspaceNodeID());
        String topNodeArchiveUriStr = null;
        if(topNode.getArchiveURI() != null) {
            topNodeArchiveUriStr = topNode.getArchiveURI().toString();
        }
        String topNodeArchiveUrlStr = null;
        if(topNode.getArchiveURL() != null) {
            topNodeArchiveUrlStr = topNode.getArchiveURL().toString();
        }
        String updateWorkspaceSql = "UPDATE workspace SET top_node_id = ?, top_node_archive_uri = ?, top_node_archive_url = ? WHERE workspace_id = ?";
        jdbcTemplate.update(updateWorkspaceSql, workspace.getTopNodeID(), topNodeArchiveUriStr, topNodeArchiveUrlStr, workspace.getWorkspaceID());
    }
    
    private int getIdentityFromDB() {
        
        String identitySql = "CALL IDENTITY();";
        int id = jdbcTemplate.queryForInt(identitySql);
        return id;
    }
    
    private Workspace getWorkspaceFromDB(int workspaceID) {
        
        String selectSql = "SELECT * FROM workspace WHERE workspace_id = ?";
        Workspace workspace;
        try {
            workspace = (Workspace) jdbcTemplate.queryForObject(selectSql, new WorkspaceRowMapper(), workspaceID);
        } catch(EmptyResultDataAccessException ex) {
            workspace = null;
        }
        return workspace;
    }
    
    private WorkspaceNode getNodeFromDB(int nodeID) {
        
        String selectSql = "SELECT * FROM node WHERE workspace_node_id = ?";
        WorkspaceNode node;
        try {
            node = (WorkspaceNode) jdbcTemplate.queryForObject(selectSql, new WorkspaceNodeRowMapper(), nodeID);
        } catch(EmptyResultDataAccessException ex) {
            node = null;
        }
        return node;
    }
    
    private WorkspaceNodeLink getNodeLinkFromDB(int parentID, int childID) {
        
        String selectSql = "SELECT * FROM node_link WHERE parent_workspace_node_id = ? AND child_workspace_node_id = ?";
        WorkspaceNodeLink nodeLink;
        try {
            nodeLink = (WorkspaceNodeLink) jdbcTemplate.queryForObject(selectSql, new WorkspaceNodeLinkRowMapper(), parentID, childID);
        } catch(EmptyResultDataAccessException ex) {
            nodeLink = null;
        }
        return nodeLink;
    }
    
    private int getOlderNode(int newNodeID) {
        
        String selectSql = "SELECT old_node_id FROM node_replacement WHERE new_node_id = ?";
        int olderNodeID = jdbcTemplate.queryForInt(selectSql, newNodeID);
        return olderNodeID;
    }
    
    private int getNewerNode(int oldNodeID) {
        
        String selectSql = "SELECT new_node_id FROM node_replacement WHERE old_node_id = ?";
        int newerNodeID = jdbcTemplate.queryForInt(selectSql, oldNodeID);
        return newerNodeID;
    }

    private Workspace copyWorkspace(Workspace ws) {
        Workspace copiedWs = new LamusWorkspace(
                ws.getWorkspaceID(), ws.getUserID(), ws.getTopNodeID(), ws.getTopNodeArchiveURI(), ws.getTopNodeArchiveURL(), ws.getStartDate(), ws.getEndDate(),
                ws.getSessionStartDate(), ws.getSessionEndDate(), ws.getUsedStorageSpace(), ws.getMaxStorageSpace(),
                ws.getStatus(), ws.getMessage(), ws.getCrawlerID());
        return copiedWs;
    }
    
    private void setNodeAsParentAndInsertLinkIntoDatabase(WorkspaceNode parent, WorkspaceNode child) throws URISyntaxException {
        
        WorkspaceNodeLink link = new LamusWorkspaceNodeLink(parent.getWorkspaceNodeID(), child.getWorkspaceNodeID());
        
        String insertLinkSql = "INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id) "
                + "VALUES (?, ?)";
        jdbcTemplate.update(insertLinkSql, link.getParentWorkspaceNodeID(), link.getChildWorkspaceNodeID());
    }
    
    private void setNodeAsDeleted(WorkspaceNode node) {
        
        String updateNodeSql = "UPDATE node SET status = ? WHERE workspace_node_id = ?";
        jdbcTemplate.update(updateNodeSql, WorkspaceNodeStatus.DELETED.name(), node.getWorkspaceNodeID());
        node.setStatus(WorkspaceNodeStatus.DELETED);
    }
    
    private void setNodeAsExternalDeleted(WorkspaceNode node) {
        
        String updateNodeSql = "UPDATE node SET status = ? WHERE workspace_node_id = ?";
        jdbcTemplate.update(updateNodeSql, WorkspaceNodeStatus.EXTERNAL_DELETED.name(), node.getWorkspaceNodeID());
        node.setStatus(WorkspaceNodeStatus.EXTERNAL_DELETED);
    }
    
    private void setNodeAsReplacedAndAddReplacementInDatabase(WorkspaceNode oldNode, WorkspaceNode newNode) {
        
        setNodeAsReplaced(oldNode);
        addNodeReplacement(oldNode, newNode);
    }
    
    private void setNodeAsReplaced(WorkspaceNode node) {
        
        String updateNodeSql = "UPDATE node SET status = ? WHERE workspace_node_id = ?";
        jdbcTemplate.update(updateNodeSql, WorkspaceNodeStatus.REPLACED.name(), node.getWorkspaceNodeID());
        node.setStatus(WorkspaceNodeStatus.REPLACED);
    }
    
    private void setNodeAsCopy(WorkspaceNode node) {
        
        String updateNodeSql = "UPDATE node SET status = ? WHERE workspace_node_id = ?";
        jdbcTemplate.update(updateNodeSql, WorkspaceNodeStatus.ARCHIVE_COPY.name(), node.getWorkspaceNodeID());
        node.setStatus(WorkspaceNodeStatus.ARCHIVE_COPY);
    }
    
    private void setNodeType(WorkspaceNode node, WorkspaceNodeType newType) {
        
        String updateNodeSql = "UPDATE node SET type = ? WHERE workspace_node_id = ?";
        jdbcTemplate.update(updateNodeSql, newType.name(), node.getWorkspaceNodeID());
        node.setType(newType);
    }
    
    private void addNodeReplacement(WorkspaceNode oldNode, WorkspaceNode newNode) {
        
        String insertReplacementSql = "INSERT INTO node_replacement (old_node_id, new_node_id) VALUES (?, ?)";
        jdbcTemplate.update(insertReplacementSql, oldNode.getWorkspaceNodeID(), newNode.getWorkspaceNodeID());
    }
    
    private void updateWorkspaceStatusInDb(Workspace workspace) {
        
        String updateWorkspaceSql = "UPDATE workspace SET status = ? WHERE workspace_id = ?";
        jdbcTemplate.update(updateWorkspaceSql, workspace.getStatus().name(), workspace.getWorkspaceID());
    }
    
    private void updateWorkspaceCrawlerIDInDb(Workspace workspace) {
        
        String updateWorkspaceSql = "UPDATE workspace SET crawler_id = ? WHERE workspace_id = ?";
        jdbcTemplate.update(updateWorkspaceSql, workspace.getCrawlerID(), workspace.getWorkspaceID());
    }
    
    private void addNodeLockToDb(WorkspaceNode node) {
        
        String insertLockSql = "INSERT INTO node_lock (archive_uri, workspace_id) "
                + "VALUES (?, ?)";
        jdbcTemplate.update(insertLockSql, node.getArchiveURI().toString(), node.getWorkspaceID());
    }
}

class WorkspaceRowMapper implements RowMapper<Workspace> {

    @Override
    public Workspace mapRow(ResultSet rs, int rowNum) throws SQLException {
        
        URI topNodeArchiveURI = null;
        if (rs.getString("top_node_archive_uri") != null) {
            topNodeArchiveURI = URI.create(rs.getString("top_node_archive_uri"));
        }
        URL topNodeArchiveURL = null;
        if(rs.getString("top_node_archive_url") != null) {
            try {
                topNodeArchiveURL = new URL(rs.getString("top_node_archive_url"));
            } catch (MalformedURLException ex) {
                fail("top_node_archive_url is not a valid URL");
            }
        }
        
        Date endDate = null;
        if (rs.getTimestamp("end_date") != null) {
            endDate = new Date(rs.getTimestamp("end_date").getTime());
        }
        Date sessionEndDate = null;
        if (rs.getTimestamp("session_end_date") != null) {
            sessionEndDate = new Date(rs.getTimestamp("session_end_date").getTime());
        }

        Workspace workspace = new LamusWorkspace(
                rs.getInt("workspace_id"),
                rs.getString("user_id"),
                rs.getInt("top_node_id"),
                topNodeArchiveURI,
                topNodeArchiveURL,
                new Date(rs.getTimestamp("start_date").getTime()),
                endDate,
                new Date(rs.getTimestamp("session_start_date").getTime()),
                sessionEndDate,
                rs.getLong("used_storage_space"),
                rs.getLong("max_storage_space"),
                WorkspaceStatus.valueOf(rs.getString("status")),
                rs.getString("message"),
                rs.getString("crawler_id"));
        return workspace;
    }
 
}

class WorkspaceNodeRowMapper implements RowMapper<WorkspaceNode> {

    @Override
    public WorkspaceNode mapRow(ResultSet rs, int rowNum) throws SQLException {
        
            URI profileSchemaURI = null;
            if(rs.getString("profile_schema_uri") != null) {
                profileSchemaURI = URI.create(rs.getString("profile_schema_uri"));
            }
            URL workspaceURL = null;
            if(rs.getString("workspace_url") != null) {
                try {
                    workspaceURL = new URL(rs.getString("workspace_url"));
                } catch (MalformedURLException ex) {
                    fail("Workspace URL is malformed; null used instead");
                }
            }
            URI archiveURI = null;
            if(rs.getString("archive_uri") != null) {
                archiveURI = URI.create(rs.getString("archive_uri"));
            }
            URL archiveURL = null;
            if(rs.getString("archive_url") != null) {
                try {
                    archiveURL = new URL(rs.getString("archive_url"));
                } catch (MalformedURLException ex) {
                    fail("Archive URL is malformed; null used instead");
                }
            }
            URI originURI = null;
            if(rs.getString("origin_url") != null) {
                originURI = URI.create(rs.getString("origin_url"));
            }

            WorkspaceNode workspaceNode = new LamusWorkspaceNode(
                    rs.getInt("workspace_node_id"),
                    rs.getInt("workspace_id"),
                    profileSchemaURI,
                    rs.getString("name"),
                    rs.getString("title"),
                    WorkspaceNodeType.valueOf(rs.getString("type")),
                    workspaceURL,
                    archiveURI,
                    archiveURL,
                    originURI,
                    WorkspaceNodeStatus.valueOf(rs.getString("status")),
                    rs.getBoolean("protected"),
                    rs.getString("format"));
            return workspaceNode;
    }
}

class WorkspaceNodeLinkRowMapper implements RowMapper<WorkspaceNodeLink> {
        
    @Override
    public WorkspaceNodeLink mapRow(ResultSet rs, int rowNum) throws SQLException {

        WorkspaceNodeLink workspaceNodeLink = new LamusWorkspaceNodeLink(
                rs.getInt("parent_workspace_node_id"),
                rs.getInt("child_workspace_node_id"));
        return workspaceNodeLink;
    }
}
