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
import java.util.Calendar;
import java.util.Date;
import javax.sql.DataSource;
import nl.mpi.lamus.dao.LamusJdbcWorkspaceDaoTestBeans;
import nl.mpi.lamus.spring.EmbeddedDatabaseBeans;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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
//@ActiveProfiles("testing")
public class LamusJdbcWorkspaceDaoTest extends AbstractTransactionalJUnit4SpringContextTests {
    
    @Configuration
    @ComponentScan("nl.mpi.lamus.dao")
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
//    @ComponentScan({"nl.mpi.lamus.dao", "nl.mpi.lamus.spring"})
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
    LamusJdbcWorkspaceDao workspaceDao;
    
    public LamusJdbcWorkspaceDaoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
        
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {

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
        
        Workspace insertedWorkspace = new LamusWorkspace(0, null, 0, null, null, null, null, null, 0, 0, WorkspaceStatus.REFUSED, null, null);
        
        try {
            workspaceDao.addWorkspace(insertedWorkspace);
            fail("An exception should have been thrown.");
        } catch(DataAccessException ex) {
            assertTrue("Cause of exception is not of the expected type.", ex.getCause() instanceof SQLException);
            assertEquals("Column was added to the workspace table.", initialNumberOfRows, countRowsInTable("workspace"));
        }
    }
    
    /**
     * 
     */
    @Test
    public void updateWorkspaceTopNode() {
        
        Workspace expectedWorkspace = insertTestWorkspaceIntoDB();
        WorkspaceNode topNode = insertTestWorkspaceNodeIntoDB(expectedWorkspace);
        int expectedTopNodeID = topNode.getWorkspaceNodeID();
        URL expectedTopNodeURL = topNode.getArchiveURL();
        
        expectedWorkspace.setTopNodeID(topNode.getWorkspaceNodeID());
        expectedWorkspace.setTopNodeArchiveURL(topNode.getArchiveURL());
        
        workspaceDao.updateWorkspaceTopNode(expectedWorkspace);
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(expectedWorkspace.getWorkspaceID());
        
        assertEquals("Workspace object retrieved from the database is different from expected.", expectedWorkspace, retrievedWorkspace);
        assertEquals("Top node archive ID of the workspace was not updated in the database.", expectedTopNodeID, retrievedWorkspace.getTopNodeID());
        assertEquals("Top node archive URL of the workspace was not updated in the database.", expectedTopNodeURL, retrievedWorkspace.getTopNodeArchiveURL());
    }
    
    /**
     * 
     */
    @Test
    public void updateWorkspaceStatus() {
        
        Workspace expectedWorkspace = insertTestWorkspaceIntoDB();
        
        WorkspaceStatus expectedStatus = WorkspaceStatus.INITIALISING;
        String expectedMessage = "test message";
        
        expectedWorkspace.setStatus(expectedStatus);
        expectedWorkspace.setMessage(expectedMessage);
        
        workspaceDao.updateWorkspaceStatusMessage(expectedWorkspace);
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(expectedWorkspace.getWorkspaceID());
        
        assertEquals("Workspace object retrieved from the database is different from expected.", expectedWorkspace, retrievedWorkspace);
        assertEquals("Status of the workspace was not updated in the database.", expectedStatus, retrievedWorkspace.getStatus());
        assertEquals("Status of the workspace was not updated in the database.", expectedMessage, retrievedWorkspace.getMessage());
    }
    
    @Test
    public void updateWorkspaceStatusWithNullStatus() {
        
        Workspace expectedWorkspace = insertTestWorkspaceIntoDB();
        Workspace changedWorkspace = copyWorkspace(expectedWorkspace);

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
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(expectedWorkspace.getWorkspaceID());

        assertEquals("Workspace object retrieved from the database is different from expected.", expectedWorkspace, retrievedWorkspace);
        assertEquals("Status of the workspace was not updated in the database.", expectedWorkspace.getStatus(), retrievedWorkspace.getStatus());
        assertEquals("Status of the workspace was not updated in the database.", expectedWorkspace.getMessage(), retrievedWorkspace.getMessage());
    }
    
    @Test
    public void updateWorkspaceStatusWithNullMessage() {
        
        Workspace expectedWorkspace = insertTestWorkspaceIntoDB();
        Workspace changedWorkspace = copyWorkspace(expectedWorkspace);

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
        
        Workspace retrievedWorkspace = getWorkspaceFromDB(expectedWorkspace.getWorkspaceID());

        assertEquals("Workspace object retrieved from the database is different from expected.", expectedWorkspace, retrievedWorkspace);
        assertEquals("Status of the workspace was not updated in the database.", expectedWorkspace.getStatus(), retrievedWorkspace.getStatus());
        assertEquals("Status of the workspace was not updated in the database.", expectedWorkspace.getMessage(), retrievedWorkspace.getMessage());
    }

    /**
     * Test of getWorkspace method, of class JdbcWorkspaceDao.
     */
    @Test
    public void getWorkspaceWithNullEndDates() {

        
        Workspace expectedWorkspace = insertTestWorkspaceIntoDB();
        
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceWithNonNullEndDates() {

        
        Workspace expectedWorkspace = insertTestWorkspaceIntoDB();
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(expectedWorkspace.getWorkspaceID());
        
        assertNotNull("Null retrieved from workspace table, using ID = " + expectedWorkspace.getWorkspaceID(), retrievedWorkspace);
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", expectedWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceWithTopNodeID() {

        
        Workspace expectedWorkspace = insertTestWorkspaceIntoDB();
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

    /**
     * Tests the method {@link JdbcWorkspaceDao#isNodeLocked(int)}
     * by checking for a node ID that exists in the database
     */
    @Test
    public void nodeIsLocked() throws MalformedURLException {
        
        int archiveNodeIdToCheck = 10;
        int archiveNodeIdToBeInsertedInTheDb = archiveNodeIdToCheck;
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
        insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, archiveNodeIdToBeInsertedInTheDb);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeIdToCheck);
        
        assertTrue("Node should be locked (should exist in the database).", result);
    }
    
    /**
     * Tests the method {@link JdbcWorkspaceDao#isNodeLocked(int)}
     * by checking for a node ID that doesn't exist in the database
     */
    @Test
    public void nodeIsNotLocked() throws MalformedURLException {
        
        int archiveNodeIdToCheck = 13;
        int archiveNodeIdToBeInsertedInTheDb = 10;
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
        insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, archiveNodeIdToBeInsertedInTheDb);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeIdToCheck);
        
        assertFalse("Node should not be locked (should not exist in the database).", result);
    }
    
/**
     * Tests the method {@link JdbcWorkspaceDao#isNodeLocked(int)}
     * by checking for a node ID that doesn't exist in the database
     */
    @Test
    public void nodeIsLockedMoreThanOnce() throws MalformedURLException {
        
        int archiveNodeIdToCheck = 13;
        int archiveNodeIdToBeInsertedInTheDb = archiveNodeIdToCheck;
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
        insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, archiveNodeIdToBeInsertedInTheDb);
        insertTestWorkspaceNodeWithArchiveIDIntoDB(testWorkspace, archiveNodeIdToBeInsertedInTheDb);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeIdToCheck);
        
        assertTrue("Node should be locked (should exist in the database).", result);
    }
    
    @Test
    public void addWorkspaceNode() throws URISyntaxException, MalformedURLException {

        int initialNumberOfRows = countRowsInTable("node");
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
        
        WorkspaceNode insertedNode = new LamusWorkspaceNode();
        insertedNode.setWorkspaceID(testWorkspace.getWorkspaceID());
        insertedNode.setName("testNode");
        insertedNode.setType(WorkspaceNodeType.METADATA);
        insertedNode.setStatus(WorkspaceNodeStatus.NODE_ISCOPY);
        insertedNode.setFormat("someFormat");
        insertedNode.setProfileSchemaURI(new URI("http://test.node.uri"));
        insertedNode.setArchiveURL(new URL("http://test.node.uri"));
        insertedNode.setOriginURL(new URL("http://test,node.uri"));
        insertedNode.setWorkspaceURL(new URL("http://test.node.uri"));
        
        this.workspaceDao.addWorkspaceNode(insertedNode);
        
        assertEquals("Column was not added to the node table.", initialNumberOfRows + 1, countRowsInTable("node"));
    }
    
    @Test
    public void addWorkspaceNodeWithWrongParameters() {
        
        int initialNumberOfRows = countRowsInTable("node");
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
        
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
    public void addWorkspaceNodeLink() throws URISyntaxException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
        WorkspaceNode testParentNode = insertTestWorkspaceNodeIntoDB(testWorkspace);
        WorkspaceNode testChildNode = insertTestWorkspaceNodeIntoDB(testWorkspace);

        URI someResourceProxyURI = new URI("resource.proxy.id");
        
        WorkspaceNodeLink insertedLink = 
                new LamusWorkspaceNodeLink(testParentNode.getWorkspaceNodeID(), testChildNode.getWorkspaceNodeID(), someResourceProxyURI);
        
        this.workspaceDao.addWorkspaceNodeLink(insertedLink);
        
        assertEquals("Column was not added to the node table.", initialNumberOfRows + 1, countRowsInTable("node_link"));
    }

    @Test
    public void addWorkspaceNodeLinkWhenParentDoesNotExist() throws URISyntaxException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
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
    public void addWorkspaceNodeLinkWhenChildDoesNotExist() throws URISyntaxException {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
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
    public void addWorkspaceNodeLinkWhenChildResourceProxyIsNull() {
        
        int initialNumberOfRows = countRowsInTable("node_link");
        
        Workspace testWorkspace = insertTestWorkspaceIntoDB();
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
    
    
    private Workspace insertTestWorkspaceIntoDB() {
        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setArchiveInfo("/blabla/blabla");
        Date now = Calendar.getInstance().getTime();
        testWorkspace.setEndDate(now);
        testWorkspace.setSessionEndDate(now);
        
        String insertWorkspaceSql = "INSERT INTO workspace (user_id, start_date, end_date, session_start_date, session_end_date, used_storage_space, max_storage_space, status, message, archive_info)" +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        simpleJdbcTemplate.update(insertWorkspaceSql,
                testWorkspace.getUserID(),
                new Timestamp(testWorkspace.getStartDate().getTime()),
                new Timestamp(testWorkspace.getEndDate().getTime()),
                new Timestamp(testWorkspace.getSessionStartDate().getTime()),
                new Timestamp(testWorkspace.getSessionEndDate().getTime()),
                testWorkspace.getUsedStorageSpace(),
                testWorkspace.getMaxStorageSpace(),
                testWorkspace.getStatus(),
                testWorkspace.getMessage(),
                testWorkspace.getArchiveInfo());

        int workspaceID = getIdentityFromDB();
        testWorkspace.setWorkspaceID(workspaceID);
        
        return testWorkspace;
    }
    
    private WorkspaceNode insertTestWorkspaceNodeIntoDB(Workspace workspace) {
        
        WorkspaceNode testWorkspaceNode = new LamusWorkspaceNode();
        testWorkspaceNode.setWorkspaceID(workspace.getWorkspaceID());
        testWorkspaceNode.setName("someNode");
        testWorkspaceNode.setType(WorkspaceNodeType.METADATA);
        testWorkspaceNode.setFormat("someFormat");
        testWorkspaceNode.setStatus(WorkspaceNodeStatus.NODE_CREATED);
        
        String insertNodeSql = "INSERT INTO node (workspace_id, name, type, format, status) values (?, ?, ?, ?, ?)";
        simpleJdbcTemplate.update(insertNodeSql, testWorkspaceNode.getWorkspaceID(),
                testWorkspaceNode.getName(), testWorkspaceNode.getType(),
                testWorkspaceNode.getFormat(), testWorkspaceNode.getStatus());
        
        int workspaceNodeID = getIdentityFromDB();
        testWorkspaceNode.setWorkspaceNodeID(workspaceNodeID);
        
        return testWorkspaceNode;
    }
    
    private WorkspaceNode insertTestWorkspaceNodeWithArchiveIDIntoDB(Workspace workspace, int archiveNodeID) throws MalformedURLException {
        
        WorkspaceNode testWorkspaceNode = new LamusWorkspaceNode();
        testWorkspaceNode.setWorkspaceID(workspace.getWorkspaceID());
        testWorkspaceNode.setArchiveNodeID(archiveNodeID);
        testWorkspaceNode.setArchiveURL(new URL ("http://some.url"));
        testWorkspaceNode.setName("someNode");
        testWorkspaceNode.setType(WorkspaceNodeType.METADATA);
        testWorkspaceNode.setFormat("someFormat");
        testWorkspaceNode.setStatus(WorkspaceNodeStatus.NODE_CREATED);
        
        String insertNodeSql = "INSERT INTO node (workspace_id, archive_node_id, name, type, format, status) values (?, ?, ?, ?, ?, ?)";
        simpleJdbcTemplate.update(insertNodeSql, testWorkspaceNode.getWorkspaceID(),
                testWorkspaceNode.getArchiveNodeID(), testWorkspaceNode.getName(),
                testWorkspaceNode.getType(), testWorkspaceNode.getFormat(), testWorkspaceNode.getStatus());
        
        int workspaceNodeID = getIdentityFromDB();
        testWorkspaceNode.setWorkspaceNodeID(workspaceNodeID);
        
        return testWorkspaceNode;
    }    
    
    private void setNodeAsWorkspaceTopNodeInDB(Workspace workspace, WorkspaceNode topNode) {
        
        workspace.setTopNodeID(topNode.getWorkspaceNodeID());
        String topNodeArchiveURLStr = null;
        if(topNode.getArchiveURL() != null) {
            topNodeArchiveURLStr = topNode.getArchiveURL().toString();
        }
        String updateWorkspaceSql = "UPDATE workspace SET top_node_id = ?, top_node_archive_url = ? WHERE workspace_id = ?";
        simpleJdbcTemplate.update(updateWorkspaceSql, workspace.getTopNodeID(), topNodeArchiveURLStr, workspace.getWorkspaceID());
    }
    
    private int getIdentityFromDB() {
        
        String identitySql = "CALL IDENTITY();";
        int id = simpleJdbcTemplate.queryForInt(identitySql);
        return id;
    }
    
    private Workspace getWorkspaceFromDB(int workspaceID) {
        
        String selectSql = "SELECT * FROM workspace WHERE workspace_id = ?";
        Workspace workspace = (Workspace) simpleJdbcTemplate.queryForObject(selectSql, new WorkspaceRowMapper(), workspaceID);
        return workspace;
    }

    private Workspace copyWorkspace(Workspace ws) {
        Workspace copiedWs = new LamusWorkspace(
                ws.getWorkspaceID(), ws.getUserID(), ws.getTopNodeID(), ws.getTopNodeArchiveURL(), ws.getStartDate(), ws.getEndDate(),
                ws.getSessionStartDate(), ws.getSessionEndDate(), ws.getUsedStorageSpace(), ws.getMaxStorageSpace(),
                ws.getStatus(), ws.getMessage(), ws.getArchiveInfo());
        return copiedWs;
    }
    
}
class WorkspaceRowMapper implements RowMapper {

    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        
        URL topNodeArchiveURL = null;
        if (rs.getString("top_node_archive_url") != null) {
            try {
                topNodeArchiveURL = new URL(rs.getString("top_node_archive_url"));
            } catch (MalformedURLException ex) {
                fail("malformed URL for top_node_archive_url");
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
