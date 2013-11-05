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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceParentNodeReference;
import nl.mpi.metadata.cmdi.api.model.DataResourceProxy;
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
//    @ComponentScan("nl.mpi.lamus.dao")
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
        standardArchiveUriForNode = new URI(UUID.randomUUID().toString());
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


    /**
     * Test of addWorkspace method, of class JdbcWorkspaceDao.
     */
    @Test
    public void addWorkspace() {

        int initialNumberOfRows = countRowsInTable("workspace");
        
        Workspace insertedWorkspace = new LamusWorkspace("testUser", 0L, 10000L);
        insertedWorkspace.setArchiveInfo("/blabla/blabla");
        
        workspaceDao.addWorkspace(insertedWorkspace);
        
        assertEquals("Column was not added to the workspace table.", initialNumberOfRows + 1, countRowsInTable("workspace"));
    }
    
    /**
     * 
     */
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
    
    /**
     * 
     */
    @Test
    public void updateWorkspaceTopNode() throws URISyntaxException, MalformedURLException {
        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = new URI(UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(expectedWorkspace, topURI, topURL, true);
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
        assertEquals("Status of the workspace was not updated in the database.", expectedMessage, retrievedWorkspace.getMessage());
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
        assertEquals("Status of the workspace was not updated in the database.", workspace.getMessage(), retrievedWorkspace.getMessage());
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
        assertEquals("Status of the workspace was not updated in the database.", workspace.getMessage(), retrievedWorkspace.getMessage());
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
    public void getWorkspaceWithNullEndDates() {

        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.FALSE);
        
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceWithNonNullEndDates() {

        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceWithTopNodeID() throws URISyntaxException, MalformedURLException {
        
        Workspace expectedWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testWorkspaceNode = insertTestWorkspaceNodeIntoDB(expectedWorkspace);
        setNodeAsWorkspaceTopNodeInDB(expectedWorkspace, testWorkspaceNode);
        
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceThatDoesntExist() {
        
        Workspace workspaceThatDoesntExist = workspaceDao.getWorkspace(1564);
        
        assertNull("Retrieved workspace should not exist", workspaceThatDoesntExist);
    }
    
    @Test
    public void listExistingWorkspacesForUser() {
        
        String userID = "user1";
        
        Workspace workspace1 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        Workspace workspace2 = insertTestWorkspaceWithGivenUserIntoDB(userID, Boolean.TRUE);
        Collection<Workspace> expectedList = new ArrayList<Workspace>();
        expectedList.add(workspace1);
        expectedList.add(workspace2);
        
        Collection<Workspace> retrievedList = workspaceDao.listWorkspacesForUser(userID);
        
        assertEquals("Retrieved list is different from expected", expectedList, retrievedList);
    }
    
    @Test
    public void listNonExistinWorkspacesForUser() {
        
        String userID = "user1";
        
        Collection<Workspace> retrievedList = workspaceDao.listWorkspacesForUser(userID);
        
        assertEquals("Retrieved list should be empty", 0, retrievedList.size());
    }

    /**
     * Tests the method {@link JdbcWorkspaceDao#isNodeLocked(int)}
     * by checking for a node ID that exists in the database
     */
    @Test
    public void nodeIsLocked() throws MalformedURLException, URISyntaxException {
        
        URI archiveNodeUriToCheck = new URI(UUID.randomUUID().toString());
        URI archiveNodeUriToBeInsertedInTheDb = archiveNodeUriToCheck;
        URL nodeURL = new URL("file:/archive/folder/node.cmdi");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, nodeURL, Boolean.TRUE);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeUriToCheck);
        
        assertTrue("Node should be locked (should exist in the database).", result);
    }
    
    /**
     * Tests the method {@link JdbcWorkspaceDao#isNodeLocked(int)}
     * by checking for a node ID that doesn't exist in the database
     */
    @Test
    public void nodeIsNotLocked() throws MalformedURLException, URISyntaxException {
        
        URI archiveNodeUriToCheck = new URI(UUID.randomUUID().toString());
        URI archiveNodeUriToBeInsertedInTheDb = new URI(UUID.randomUUID().toString());
        URL nodeURL = new URL("file:/archive/folder/node.cmdi");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, nodeURL, Boolean.TRUE);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeUriToCheck);
        
        assertFalse("Node should not be locked (should not exist in the database).", result);
    }
    
    /**
     * Tests the method {@link JdbcWorkspaceDao#isNodeLocked(int)}
     * by checking for a node ID that doesn't exist in the database
     */
    @Test
    public void nodeIsLockedMoreThanOnce() throws MalformedURLException, URISyntaxException {
        
        URI archiveNodeUriToCheck = new URI(UUID.randomUUID().toString());
        URL firstURL = new URL("file:/archive/folder/first.cmdi");
        URI archiveNodeUriToBeInsertedInTheDb = archiveNodeUriToCheck;
        URL secondURL = new URL("file:/archive/folder/second.cmdi");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, firstURL, Boolean.TRUE);
        insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, archiveNodeUriToBeInsertedInTheDb, secondURL, Boolean.TRUE);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeUriToCheck);
        
        assertTrue("Node should be locked (should exist in the database).", result);
    }
    
    
    
    //TODO TESTS NODE LOCKED WITH ONLY URL, NOT PID...
    
    
    
    
    @Test
    public void addWorkspaceNode() throws URISyntaxException, MalformedURLException {

        int initialNumberOfRows = countRowsInTable("node");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        WorkspaceNode insertedNode = new LamusWorkspaceNode();
        insertedNode.setWorkspaceID(testWorkspace.getWorkspaceID());
        insertedNode.setName("testNode");
        insertedNode.setType(WorkspaceNodeType.METADATA);
        insertedNode.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);
        insertedNode.setFormat("someFormat");
        insertedNode.setProfileSchemaURI(new URI("http://test.node.uri"));
        insertedNode.setArchiveURI(new URI(UUID.randomUUID().toString()));
        insertedNode.setOriginURL(new URL("http://test,node.uri"));
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
    public void updateNodeWorkspaceURL() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);
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
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);

        testNode.setWorkspaceURL(null);
        
        workspaceDao.updateNodeWorkspaceURL(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Workspace URL of the node was not updated in the database.", null, retrievedNode.getWorkspaceURL());
    }
    
    @Test
    public void updateNodeArchiveURI() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);
        URI expectedURI = new URI(UUID.randomUUID().toString());
        URL expectedURL = new URL("file:/archive/folder/different_test.cmdi");

        testNode.setArchiveURI(expectedURI);
        testNode.setArchiveURL(expectedURL);
        
        workspaceDao.updateNodeArchiveUriUrl(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Archive URI of the node was not updated in the database.", expectedURI, retrievedNode.getArchiveURI());
        assertEquals("Archive URL of the node was not updated in the database.", expectedURL, retrievedNode.getArchiveURL());
    }
    
    @Test
    public void updateNodeWithNullArchiveURI() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);

        testNode.setArchiveURI(null);
        
        workspaceDao.updateNodeArchiveUriUrl(testNode);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        
        assertEquals("WorkspaceNode object retrieved from the database is different from expected.", testNode, retrievedNode);
        assertEquals("Archive URI of the node was not updated in the database.", null, retrievedNode.getArchiveURI());
    }
    
    @Test
    public void addWorkspaceNodeLink() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testParentNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        WorkspaceNode testChildNode = insertTestWorkspaceNodeIntoDB(testWorkspace);

        URI someResourceProxyURI = new URI("resource.proxy.id");
        
        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(testParentNode.getWorkspaceNodeID(), testChildNode.getWorkspaceNodeID(), someResourceProxyURI);
        
        this.workspaceDao.addWorkspaceNodeLink(insertedLink);
        
        assertEquals("Column was not added to the node table.", initialNumberOfRows + 1, countRowsInTable("node_link"));
    }

    @Test
    public void addWorkspaceNodeLinkWhenParentDoesNotExist() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testChildNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        int fakeNodeID = 100;

        URI someResourceProxyURI = new URI("resource.proxy.id");
        
        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(fakeNodeID, testChildNode.getWorkspaceNodeID(), someResourceProxyURI);
        
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

        URI someResourceProxyURI = new URI("resource.proxy.id");
        
        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(testParentNode.getWorkspaceNodeID(), fakeNodeID, someResourceProxyURI);
        
        try {
            this.workspaceDao.addWorkspaceNodeLink(insertedLink);
            fail("An exception should have been thrown.");
        } catch(DataIntegrityViolationException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        assertEquals("Column was added to the node table.", initialNumberOfRows, countRowsInTable("node_link"));
    }
    
    @Test
    public void addWorkspaceNodeLinkWhenChildResourceProxyIsNull() throws URISyntaxException, MalformedURLException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        WorkspaceNode testParentNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        WorkspaceNode testChildNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        
        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(testParentNode.getWorkspaceNodeID(), testChildNode.getWorkspaceNodeID(), null);
        
        try {
            this.workspaceDao.addWorkspaceNodeLink(insertedLink);
            fail("An exception should have been thrown.");
        } catch(DataIntegrityViolationException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
        }
        
        assertEquals("Column was added to the node table.", initialNumberOfRows, countRowsInTable("node_link"));
    }
    
    @Test
    public void getExistingNode() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceNode(testNode.getWorkspaceNodeID());
        
        assertNotNull("Returned node should not be null", result);
    }
    
    @Test
    public void getNonExistingNode() {
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceNode(100);
        
        assertNull("Retrieved node should be null", result);
    }
    
    @Test
    public void getNodeWithNullProfileSchema() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.FALSE);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceNode(testNode.getWorkspaceNodeID());
        
        assertNotNull("Returned node should not be null", result);
    }
    
    @Test
    public void getNodeWithNullURLs() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceNode(testNode.getWorkspaceNodeID());
        
        assertNotNull("Returned node should not be null", result);
    }
    
//    @Test
//    public void getExistingTreeNodeWithoutParent() throws MalformedURLException, URISyntaxException {
//        
//        int archiveNodeIdToBeInsertedInTheDb = 10;
//        
//        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
//        WorkspaceNode testNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, archiveNodeIdToBeInsertedInTheDb, Boolean.TRUE, Boolean.TRUE);
//        WorkspaceTreeNode expectedTreeNode = new LamusWorkspaceTreeNode(testNode, null, workspaceDao);
//        
//        WorkspaceTreeNode result = this.workspaceDao.getWorkspaceTreeNode(testNode.getWorkspaceNodeID(), null);
//        
//        assertNotNull("Returned tree node should not be null", result);
//        assertEquals("Returned tree node is different from expected", expectedTreeNode, result);
//        assertNull("Returned tree node should have a null parent tree node", result.getParent());
//    }
    
//    @Test
//    public void getExistingTreeNodeWithParent() throws MalformedURLException, URISyntaxException {
//        
//        int archiveNodeIdToBeInsertedInTheDb = 10;
//        int parentArchiveNodeIdToBeInsertedInTheDb = 5;
//        
//        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
//        WorkspaceNode testNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, archiveNodeIdToBeInsertedInTheDb, Boolean.TRUE, Boolean.TRUE);
//        WorkspaceNode testParentNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, parentArchiveNodeIdToBeInsertedInTheDb, Boolean.TRUE, Boolean.TRUE);
//        setNodeAsParentAndInsertLinkIntoDatabase(testParentNode, testNode);
//        WorkspaceTreeNode parentTreeNode = new LamusWorkspaceTreeNode(testParentNode, null, workspaceDao);
//        WorkspaceTreeNode expectedTreeNode = new LamusWorkspaceTreeNode(testNode, parentTreeNode, workspaceDao);
//        
//        WorkspaceTreeNode result = this.workspaceDao.getWorkspaceTreeNode(testNode.getWorkspaceNodeID(), parentTreeNode);
//        
//        assertNotNull("Returned tree node should not be null", result);
//        assertEquals("Returned tree node is different from expected", expectedTreeNode, result);
//        assertNotNull("Returned tree node should have a null parent tree node", result.getParent());
//        assertEquals("Returned tree node has a parent tree node that is different from expected", parentTreeNode, result.getParent());
//    }
    
//    @Test
//    public void getExistingTreeNodeWithIllegalParent() throws MalformedURLException, URISyntaxException {
//        
//        int archiveNodeIdToBeInsertedInTheDb = 10;
//        int parentArchiveNodeIdToBeInsertedInTheDb = 5;
//        
//        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
//        WorkspaceNode testNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, archiveNodeIdToBeInsertedInTheDb, Boolean.TRUE, Boolean.TRUE);
//        WorkspaceNode testNotParentNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, parentArchiveNodeIdToBeInsertedInTheDb, Boolean.TRUE, Boolean.TRUE);
//        
//        WorkspaceTreeNode notParentTreeNode = new LamusWorkspaceTreeNode(testNotParentNode, null, workspaceDao);
//        WorkspaceTreeNode treeNode = new LamusWorkspaceTreeNode(testNode, null, workspaceDao);
//        
//        String expectedErrorMessage = "Node with ID " + notParentTreeNode.getWorkspaceNodeID() +
//                " is passed as parent of node with ID " + treeNode.getWorkspaceNodeID() +
//                " but these nodes are not linked in the database.";
//        
//        WorkspaceTreeNode result;
//        try {
//            result = this.workspaceDao.getWorkspaceTreeNode(testNode.getWorkspaceNodeID(), notParentTreeNode);
//            fail("An IllegalArgumentException should have been thrown.");
//        } catch(IllegalArgumentException ex) {
//            assertEquals("Exception message different from expected", expectedErrorMessage, ex.getMessage());
//        }
//    }
    
//    @Test
//    public void getNonExistingTreeNode() {
//        
//        WorkspaceTreeNode result = this.workspaceDao.getWorkspaceTreeNode(100, null);
//        
//        assertNull("Retrieved tree node should be null", result);
//    }
    
    @Test
    public void getWorkspaceTopNode() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);
        testWorkspace.setTopNodeID(testNode.getWorkspaceNodeID());
//        testWorkspace.setTopNodeArchiveID(testNode.getArchiveNodeID());
        testWorkspace.setTopNodeArchiveURI(testNode.getArchiveURI());
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, testNode);
        
        WorkspaceNode result = this.workspaceDao.getWorkspaceTopNode(testWorkspace.getWorkspaceID());
        
        assertNotNull("Returned node should not be null", result);
        assertEquals("Returned node is different from expected", testNode, result);
    }
    
    @Test
    public void getExistingNodesForWorkspace() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = new URI(UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, Boolean.TRUE);
        URI childURI = new URI(UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, Boolean.TRUE);
        
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
    public void getExistingChildNodes() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = new URI(UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, Boolean.TRUE);
        URI childURI = new URI(UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, Boolean.TRUE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getChildWorkspaceNodes(parentNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(childNode));
    }

    @Test
    public void getNonExistingChildNodes() throws URISyntaxException, MalformedURLException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = new URI(UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, Boolean.TRUE);
        
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
    
//    @Test
//    public void getExistingChildTreeNodes() throws URISyntaxException, MalformedURLException {
//        
//        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
//        WorkspaceNode parentNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, 1, Boolean.TRUE, Boolean.TRUE);
//        WorkspaceNode childNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, 2, Boolean.TRUE, Boolean.TRUE);
//        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
//        WorkspaceTreeNode parentTreeNode = new LamusWorkspaceTreeNode(parentNode, null, workspaceDao);
//        WorkspaceTreeNode childTreeNode = new LamusWorkspaceTreeNode(childNode, parentTreeNode, workspaceDao);
//        
//        List<WorkspaceTreeNode> result = this.workspaceDao.getChildWorkspaceTreeNodes(parentTreeNode);
//        
//        assertNotNull("The returned list of tree nodes should not be null", result);
//        assertEquals("Size of the returned list of tree nodes is different from expected", 1, result.size());
//        assertTrue("The returned list of tree nodes does not contain the expected tree node", result.contains(childTreeNode));
//    }

//    @Test
//    public void getNonExistingChildTreeNodes() throws URISyntaxException, MalformedURLException {
//        
//        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
//        WorkspaceNode parentNode = insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, 1, Boolean.TRUE, Boolean.TRUE);
//        WorkspaceTreeNode parentTreeNode = new LamusWorkspaceTreeNode(parentNode, null, workspaceDao);
//        
//        List<WorkspaceTreeNode> result = this.workspaceDao.getChildWorkspaceTreeNodes(parentTreeNode);
//        
//        assertNotNull("The returned list of treenodes should not be null", result);
//        assertEquals("Returned list of treenodes should be empty", 0, result.size());
//    }

//    @Test
//    public void getChildTreeNodesFromNonExistingParent() throws URISyntaxException, MalformedURLException {
//        
//        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
//        WorkspaceNode nonExistingParentNode = createWorkspaceNode(testWorkspace, -1, Boolean.FALSE, Boolean.FALSE);
//        WorkspaceTreeNode nonExistingParentTreeNode = new LamusWorkspaceTreeNode(nonExistingParentNode, null, workspaceDao);
//        
//        List<WorkspaceTreeNode> result = this.workspaceDao.getChildWorkspaceTreeNodes(nonExistingParentTreeNode);
//        
//        assertNotNull("The returned list of tree nodes should not be null", result);
//        assertEquals("Returned list of tree nodes should be empty", 0, result.size());
//    }
    
    @Test
    public void getExistingParentNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = new URI(UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, Boolean.TRUE);
        URI childURI = new URI(UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, Boolean.TRUE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getParentWorkspaceNodes(childNode.getWorkspaceNodeID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(parentNode));
    }
    
    @Test
    public void getNonExistingParentNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI childURI = new URI(UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, Boolean.TRUE);
        
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
        URI testURI = new URI(UUID.randomUUID().toString());
        URL testURL = new URL("file:/archive/folder/test.cmdi");
        WorkspaceNode testNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, testURI, testURL, Boolean.TRUE);
        setNodeAsDeleted(testNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(testNode));
    }
    
    @Test
    public void getDeletedNodeWithChildren() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI parentURI = new URI(UUID.randomUUID().toString());
        URL parentURL = new URL("file:/archive/folder/parent.cmdi");
        WorkspaceNode parentNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, parentURI, parentURL, Boolean.TRUE);
        URI childURI = new URI(UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/child.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, Boolean.TRUE);
        setNodeAsParentAndInsertLinkIntoDatabase(parentNode, childNode);
        setNodeAsDeleted(parentNode);
        setNodeAsDeleted(childNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 1, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(parentNode));
    }
    
    @Test
    public void getSeveralDeletedNodes() throws MalformedURLException, URISyntaxException {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI firstURI = new URI(UUID.randomUUID().toString());
        URL firstURL = new URL("file:/archive/folder/first.cmdi");
        WorkspaceNode firstNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, firstURI, firstURL, Boolean.TRUE);
        URI secondURI = new URI(UUID.randomUUID().toString());
        URL secondURL = new URL("file:/archive/folder/second.cmdi");
        WorkspaceNode secondNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, secondURI, secondURL, Boolean.TRUE);
        setNodeAsDeleted(firstNode);
        setNodeAsDeleted(secondNode);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertEquals("Size of the returned list of nodes is different from expected", 2, result.size());
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(firstNode));
        assertTrue("The returned list of nodes does not contain the expected node", result.contains(secondNode));
    }
    
    @Test
    public void getNonExistingDeletedTopNodes() {
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        
        Collection<WorkspaceNode> result = this.workspaceDao.getDeletedTopNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("The returned list of nodes should not be null", result);
        assertTrue("Returned list of nodes should be empty", result.isEmpty());
    }
    
    @Test
    public void listUnlinkedNodesOneNode() throws URISyntaxException, MalformedURLException {
        
        //TODO get all nodes that have no parent link in the node_link table
            // THIS HAS TO EXCLUDE THE TOP NODE
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = new URI(UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, Boolean.TRUE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = new URI(UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, Boolean.TRUE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        URI unlinkedURI = new URI(UUID.randomUUID().toString());
        URL unlinkedURL = new URL("file:/archive/folder/unlinkednode.cmdi");
        WorkspaceNode unlinkedNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, unlinkedURI, unlinkedURL, Boolean.TRUE);
        
        List<WorkspaceNode> result = this.workspaceDao.listUnlinkedNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("List of unlinked nodes should not be null", result);
        assertTrue("List of unlinked nodes has a different size than what was expected", result.size() == 1);
        assertEquals("Node in the list of unlinked nodes different from expected", unlinkedNode, result.iterator().next());
    }

    @Test
    public void listUnlinkedNodesZeroNodes() throws URISyntaxException, MalformedURLException {
        
        //TODO get all nodes that have no parent link in the node_link table
            // THIS HAS TO EXCLUDE THE TOP NODE
        
        Workspace testWorkspace = insertTestWorkspaceWithDefaultUserIntoDB(Boolean.TRUE);
        URI topURI = new URI(UUID.randomUUID().toString());
        URL topURL = new URL("file:/archive/folder/topnode.cmdi");
        WorkspaceNode topNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, topURI, topURL, Boolean.TRUE);
        setNodeAsWorkspaceTopNodeInDB(testWorkspace, topNode);
        URI childURI = new URI(UUID.randomUUID().toString());
        URL childURL = new URL("file:/archive/folder/childnode.cmdi");
        WorkspaceNode childNode = insertTestWorkspaceNodeWithUriIntoDB(testWorkspace, childURI, childURL, Boolean.TRUE);
        setNodeAsParentAndInsertLinkIntoDatabase(topNode, childNode);
        
        List<WorkspaceNode> result = this.workspaceDao.listUnlinkedNodes(testWorkspace.getWorkspaceID());
        
        assertNotNull("List of unlinked nodes should not be null", result);
        assertTrue("List of unlinked nodes has a different size than what was expected", result.isEmpty());
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
                testWorkspace.getWorkspaceID(), testNode.getWorkspaceNodeID());
        int finalNumberOfRows = countRowsInTable("node");
        
        assertTrue("Node should not be deleted from the database, just set as deleted", finalNumberOfRows == intermediateNumberOfRows);
        
        WorkspaceNode retrievedNode = getNodeFromDB(testNode.getWorkspaceNodeID());
        assertNotNull("Node should exist in the database", retrievedNode);
        assertEquals("Not should be set as deleted", WorkspaceNodeStatus.NODE_DELETED, retrievedNode.getStatus());
    }
    

    private Workspace insertTestWorkspaceWithDefaultUserIntoDB(boolean withEndDates) {
        return insertTestWorkspaceWithGivenUserIntoDB("someUser", withEndDates);
    }
    
    private Workspace insertTestWorkspaceWithGivenUserIntoDB(String userID, boolean withEndDates) {
        
        Workspace testWorkspace = new LamusWorkspace(userID, 0L, 10000000L);
        testWorkspace.setArchiveInfo("/blabla/blabla");
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
        
        String insertWorkspaceSql = "INSERT INTO workspace (user_id, start_date, end_date, session_start_date, session_end_date, used_storage_space, max_storage_space, status, message, archive_info)" +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertWorkspaceSql,
                testWorkspace.getUserID(),
                new Timestamp(testWorkspace.getStartDate().getTime()),
                endDate,
                new Timestamp(testWorkspace.getSessionStartDate().getTime()),
                sessionEndDate,
                testWorkspace.getUsedStorageSpace(),
                testWorkspace.getMaxStorageSpace(),
                testWorkspace.getStatus(),
                testWorkspace.getMessage(),
                testWorkspace.getArchiveInfo());

        int workspaceID = getIdentityFromDB();
        testWorkspace.setWorkspaceID(workspaceID);
        
        return testWorkspace;
    }
    
    private WorkspaceNode insertTestWorkspaceNodeIntoDB(Workspace workspace) throws URISyntaxException, MalformedURLException {
        
        WorkspaceNode testWorkspaceNode = createWorkspaceNode(workspace, null, null, Boolean.FALSE);
        
        String insertNodeSql = "INSERT INTO node (workspace_id, name, type, format, status) values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertNodeSql, testWorkspaceNode.getWorkspaceID(),
                testWorkspaceNode.getName(), testWorkspaceNode.getType(),
                testWorkspaceNode.getFormat(), testWorkspaceNode.getStatus());
        
        int workspaceNodeID = getIdentityFromDB();
        testWorkspaceNode.setWorkspaceNodeID(workspaceNodeID);
        
        return testWorkspaceNode;
    }
    
    private WorkspaceNode insertTestWorkspaceNodeWithUriIntoDB(Workspace workspace, URI archiveURI, URL archiveURL, boolean withProfileSchemaURI) throws MalformedURLException, URISyntaxException {
        
        WorkspaceNode testWorkspaceNode = createWorkspaceNode(workspace, archiveURI, archiveURL, withProfileSchemaURI);
        
        String insertNodeSql = "INSERT INTO node (workspace_id, profile_schema_uri, workspace_url, archive_uri, archive_url, origin_url, name, type, format, status) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertNodeSql, testWorkspaceNode.getWorkspaceID(),
                testWorkspaceNode.getProfileSchemaURI(), testWorkspaceNode.getWorkspaceURL(),
                testWorkspaceNode.getArchiveURI(), testWorkspaceNode.getArchiveURL(), testWorkspaceNode.getOriginURL(), testWorkspaceNode.getName(),
                testWorkspaceNode.getType(), testWorkspaceNode.getFormat(), testWorkspaceNode.getStatus());
        
        int workspaceNodeID = getIdentityFromDB();
        testWorkspaceNode.setWorkspaceNodeID(workspaceNodeID);
        
        return testWorkspaceNode;
    }
    
    private WorkspaceNode createWorkspaceNode(Workspace workspace, URI archiveURI, URL archiveURL, boolean withProfileSchemaURI) throws URISyntaxException, MalformedURLException {
        
        WorkspaceNode testWorkspaceNode = new LamusWorkspaceNode();
        testWorkspaceNode.setWorkspaceID(workspace.getWorkspaceID());
        if(withProfileSchemaURI) {
            testWorkspaceNode.setProfileSchemaURI(new URI("http://some.schema.xsd"));
        }

        testWorkspaceNode.setWorkspaceURL(standardWorkspaceUrlForNode);
        testWorkspaceNode.setArchiveURI(archiveURI);
        testWorkspaceNode.setArchiveURL(archiveURL);
        testWorkspaceNode.setOriginURL(archiveURL);

        testWorkspaceNode.setName("someNode");
        testWorkspaceNode.setType(WorkspaceNodeType.METADATA);
        testWorkspaceNode.setFormat("someFormat");
        testWorkspaceNode.setStatus(WorkspaceNodeStatus.NODE_CREATED);
        
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

    private Workspace copyWorkspace(Workspace ws) {
        Workspace copiedWs = new LamusWorkspace(
                ws.getWorkspaceID(), ws.getUserID(), ws.getTopNodeID(), ws.getTopNodeArchiveURI(), ws.getTopNodeArchiveURL(), ws.getStartDate(), ws.getEndDate(),
                ws.getSessionStartDate(), ws.getSessionEndDate(), ws.getUsedStorageSpace(), ws.getMaxStorageSpace(),
                ws.getStatus(), ws.getMessage(), ws.getArchiveInfo());
        return copiedWs;
    }
    
    private void setNodeAsParentAndInsertLinkIntoDatabase(WorkspaceNode parent, WorkspaceNode child) throws URISyntaxException {
        
        URI childResourceProxy = new URI("http://some_uri.jpg");
        
        WorkspaceParentNodeReference parentReference = new LamusWorkspaceParentNodeReference(parent.getWorkspaceNodeID(), new DataResourceProxy("id", childResourceProxy, "jpg"));
        child.addParentNodeReference(parentReference);
        WorkspaceNodeLink link = new LamusWorkspaceNodeLink(parent.getWorkspaceNodeID(), child.getWorkspaceNodeID(), childResourceProxy);
        
        String insertNodeSql = "INSERT INTO node_link (parent_workspace_node_id, child_workspace_node_id, child_uri) "
                + "values (?, ?, ?)";
        jdbcTemplate.update(insertNodeSql, link.getParentWorkspaceNodeID(), link.getChildWorkspaceNodeID(),
                link.getChildURI());
    }
    
    private void setNodeAsDeleted(WorkspaceNode node) {
        
        String updateNodeSql = "UPDATE node SET status = ? WHERE workspace_node_id = ?";
        jdbcTemplate.update(updateNodeSql, WorkspaceNodeStatus.NODE_DELETED.toString(), node.getWorkspaceNodeID());
        node.setStatus(WorkspaceNodeStatus.NODE_DELETED);
    }
    
}

class WorkspaceRowMapper implements RowMapper<Workspace> {

    @Override
    public Workspace mapRow(ResultSet rs, int rowNum) throws SQLException {
        
        URI topNodeArchiveURI = null;
        if (rs.getString("top_node_archive_uri") != null) {
            try {
                topNodeArchiveURI = new URI(rs.getString("top_node_archive_uri"));
            } catch (URISyntaxException ex) {
                fail("top_node_archive_uri is not a valid URI");
            }
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
                rs.getString("archive_info"));
        return workspace;
    }
 
}

class WorkspaceNodeRowMapper implements RowMapper<WorkspaceNode> {

    @Override
    public WorkspaceNode mapRow(ResultSet rs, int rowNum) throws SQLException {
        
            URI profileSchemaURI = null;
            if(rs.getString("profile_schema_uri") != null) {
                try {
                    profileSchemaURI = new URI(rs.getString("profile_schema_uri"));
                } catch (URISyntaxException ex) {
                    fail("Profile Schema URI has invalid syntax; null used instead");
                }
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
                try {
                    archiveURI = new URI(rs.getString("archive_uri"));
                } catch (URISyntaxException ex) {
                    fail("Archive URI is malformed; null used instead");
                }
            }
            URL archiveURL = null;
            if(rs.getString("archive_url") != null) {
                try {
                    archiveURL = new URL(rs.getString("archive_url"));
                } catch (MalformedURLException ex) {
                    fail("Archive URL is malformed; null used instead");
                }
            }
            URL originURL = null;
            if(rs.getString("origin_url") != null) {
                try {
                    originURL = new URL(rs.getString("origin_url"));
                } catch (MalformedURLException ex) {
                    fail("Origin URL is malformed; null used instead");
                }
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
                    originURL,
                    WorkspaceNodeStatus.valueOf(rs.getString("status")),
                    rs.getString("format"));
            return workspaceNode;
    }
}

class WorkspaceNodeLinkRowMapper implements RowMapper<WorkspaceNodeLink> {
        
    @Override
    public WorkspaceNodeLink mapRow(ResultSet rs, int rowNum) throws SQLException {

        URI childURI = null;
        if(rs.getString("child_uri") != null) {
            try {
                childURI = new URI(rs.getString("child_uri"));
            } catch (URISyntaxException ex) {
                fail("Child URI has an invalid syntax; null used instead");
            }
        }

        WorkspaceNodeLink workspaceNodeLink = new LamusWorkspaceNodeLink(
                rs.getInt("parent_workspace_node_id"),
                rs.getInt("child_workspace_node_id"),
                childURI);
        return workspaceNodeLink;
    }
}
