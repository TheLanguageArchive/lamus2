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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import javax.sql.DataSource;
import nl.mpi.lamus.workspace.LamusWorkspace;
import nl.mpi.lamus.workspace.Workspace;
import static org.junit.Assert.*;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@ContextConfiguration
public class LamusJdbcWorkspaceDaoTest extends AbstractTransactionalJUnit4SpringContextTests {
    
    @Autowired
    LamusJdbcWorkspaceDao workspaceDao;

    @Autowired
    DataSource embeddedDataSource;
    
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
    public void testAddWorkspace() throws SQLException {
        System.out.println("addWorkspace");

        int initialNumberOfRows = countRowsInTable("workspace");
        
        Workspace insertedWorkspace = new LamusWorkspace("testUser", 0L, 10000L);
        insertedWorkspace.setTopNodeID(10);
        insertedWorkspace.setArchiveInfo("/blabla/blabla");
        
        workspaceDao.addWorkspace(insertedWorkspace);
        
        assertEquals("Column was not added to the workspace table.", initialNumberOfRows + 1, countRowsInTable("workspace"));
    }

    
    
    
    //TODO test add workspace with some null values (dates...)
    
    //TODO test get workspace with exception (if empty result)
    
    
    
    
    
    /**
     * Test of getWorkspace method, of class JdbcWorkspaceDao.
     */
    @Test
    public void getWorkspaceWithNullEndDates() {

        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setTopNodeID(10);
        testWorkspace.setArchiveInfo("/blabla/blabla");
        
        String insertSql = "INSERT INTO workspace (user_id, top_node_id, start_date, session_start_date, used_storage_space, max_storage_space, status, archive_info)" +
                        "values (?, ?, ?, ?, ?, ?, ?, ?)";
        simpleJdbcTemplate.update(insertSql,
                testWorkspace.getUserID(), testWorkspace.getTopNodeID(),
                new Timestamp(testWorkspace.getStartDate().getTime()),
                new Timestamp(testWorkspace.getSessionStartDate().getTime()),
                testWorkspace.getUsedStorageSpace(), testWorkspace.getMaxStorageSpace(),
                testWorkspace.getStatus(), testWorkspace.getArchiveInfo());
        
        String identitySql = "CALL IDENTITY();";
        int workspaceID = simpleJdbcTemplate.queryForInt(identitySql);
        testWorkspace.setWorkspaceID(workspaceID);
        
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(workspaceID);
        
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", testWorkspace, retrievedWorkspace);
    }
    
    @Test
    public void getWorkspaceWithNonNullEndDates() {

        
        Workspace testWorkspace = new LamusWorkspace("someUser", 0L, 10000000L);
        testWorkspace.setTopNodeID(10);
        testWorkspace.setArchiveInfo("/blabla/blabla");
        Date now = Calendar.getInstance().getTime();
        testWorkspace.setEndDate(now);
        testWorkspace.setSessionEndDate(now);
        
        String insertSql = "INSERT INTO workspace (user_id, top_node_id, start_date, end_date, session_start_date, session_end_date, used_storage_space, max_storage_space, status, archive_info)" +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        simpleJdbcTemplate.update(insertSql,
                testWorkspace.getUserID(), testWorkspace.getTopNodeID(),
                new Timestamp(testWorkspace.getStartDate().getTime()),
                new Timestamp(testWorkspace.getEndDate().getTime()),
                new Timestamp(testWorkspace.getSessionStartDate().getTime()),
                new Timestamp(testWorkspace.getSessionEndDate().getTime()),
                testWorkspace.getUsedStorageSpace(), testWorkspace.getMaxStorageSpace(),
                testWorkspace.getStatus(), testWorkspace.getArchiveInfo());
        
        String identitySql = "CALL IDENTITY();";
        int workspaceID = simpleJdbcTemplate.queryForInt(identitySql);
        testWorkspace.setWorkspaceID(workspaceID);
        
        Workspace retrievedWorkspace = workspaceDao.getWorkspace(workspaceID);
        
        assertEquals("Values retrieved from the workspace table do not match the inserted ones.", testWorkspace, retrievedWorkspace);
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
    public void nodeIsLocked() {
        
        int archiveNodeIdToCheck = 10;
        int archiveNodeIdToBeInsertedInTheDb = archiveNodeIdToCheck;
        
        String insertNodeSQL =
                "insert into node (workspace_node_id, workspace_id, archive_node_id, name, status) "
                + "values (?, ?, ?, ?, ?)";
        this.simpleJdbcTemplate.update(insertNodeSQL, 1, 0, archiveNodeIdToBeInsertedInTheDb, "someNode", 9);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeIdToCheck);
        
        assertTrue("Node should be locked (should exist in the database).", result);
    }
    
    /**
     * Tests the method {@link JdbcWorkspaceDao#isNodeLocked(int)}
     * by checking for a node ID that doesn't exist in the database
     */
    @Test
    public void nodeIsNotLocked() {
        
        int archiveNodeIdToCheck = 13;
        int archiveNodeIdToBeInsertedInTheDb = 10;
        
        String insertNodeSQL =
                "insert into node (workspace_node_id, workspace_id, archive_node_id, name, status) "
                + "values (?, ?, ?, ?, ?)";
        this.simpleJdbcTemplate.update(insertNodeSQL, 1, 0, archiveNodeIdToBeInsertedInTheDb, "someNode", 9);
        
        boolean result = this.workspaceDao.isNodeLocked(archiveNodeIdToCheck);
        
        assertFalse("Node should not be locked (should not exist in the database).", result);
    }
}
