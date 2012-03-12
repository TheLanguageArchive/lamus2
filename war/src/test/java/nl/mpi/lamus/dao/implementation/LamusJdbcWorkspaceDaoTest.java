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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import javax.sql.DataSource;
import nl.mpi.lamus.workspace.LamusWorkspace;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.WorkspaceStatus;
import static org.junit.Assert.*;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LamusJdbcWorkspaceDaoTest extends AbstractTransactionalJUnit4SpringContextTests {
    
//    @Autowired
//    ApplicationContext applicationContext;
    
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
        
        String scriptPath = "nl/mpi/lamus/dao/implementation/hsql_CreateTables.sql";
        executeSqlScript(scriptPath, false);
        
    }
    
    @After
    public void tearDown() {
        
        String scriptPath = "nl/mpi/lamus/dao/implementation/hsql_DropTables.sql";
        executeSqlScript(scriptPath, false);
    }

    /**
     * Test of setDataSource method, of class JdbcWorkspaceDao.
     */
    @Test
    public void testSetDataSource() {
        System.out.println("setDataSource");
        DataSource datasource = null;
        LamusJdbcWorkspaceDao instance = new LamusJdbcWorkspaceDao();
        instance.setDataSource(datasource);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addWorkspace method, of class JdbcWorkspaceDao.
     */
    @Test
    public void testAddWorkspace() throws SQLException {
        System.out.println("addWorkspace");

        Date now = Calendar.getInstance().getTime();
        Workspace insertedWorkspace = new LamusWorkspace("testUser", 0L, 10000L);
//        insertedWorkspace.setArchiveInfo("/blabla/blabla");
        
        workspaceDao.addWorkspace(insertedWorkspace);
        
        assertEquals(1, countRowsInTable("workspace"));

        assertEquals(1, insertedWorkspace.getWorkspaceID());
        
        String sql = "SELECT * FROM workspace WHERE workspace_id = ?";        
//        Workspace queriedWorkspace = simpleJdbcTemplate.queryForObject(sql, Workspace.class, insertedWorkspace.getWorkspaceID());
        
        Workspace queriedWorkspace = simpleJdbcTemplate.queryForObject(
                sql,
                new RowMapper<Workspace>() {
                  public Workspace mapRow(ResultSet rs, int rowNum) throws SQLException {
                      Workspace workspace = new LamusWorkspace();
                      workspace.setWorkspaceID(rs.getInt("workspace_id"));
                      workspace.setUserID(rs.getString("userid"));
                      workspace.setTopNodeID(rs.getInt("topnodeid"));
                      workspace.setStartDate(new Date(rs.getTimestamp("startdate").getTime()));
                      workspace.setEndDate(new Date(rs.getTimestamp("enddate").getTime()));
                      workspace.setSessionStartDate(new Date(rs.getTimestamp("sessionstartdate").getTime()));
                      workspace.setSessionEndDate(new Date(rs.getTimestamp("sessionenddate").getTime()));
                      workspace.setUsedStorageSpace(rs.getLong("used_storage_space"));
                      workspace.setMaxStorageSpace(rs.getLong("max_storage_space"));
                      workspace.setStatus(WorkspaceStatus.valueOf(rs.getString("status")));
                      workspace.setMessage(rs.getString("message"));
                      workspace.setArchiveInfo(rs.getString("archive_info"));
                      return workspace;
                  }
                },
                new Object[] { insertedWorkspace.getWorkspaceID() });
        
//        Workspace queriedWorkspace = simpleJdbcTemplate.queryForObject(
//                sql, ParameterizedBeanPropertyRowMapper.newInstance(Workspace.class), new Object[] { insertedWorkspace.getWorkspaceID() });
        
        
        //TODO implement equals in WorkspaceImpl
        assertEquals(insertedWorkspace, queriedWorkspace);
        
//                throw new UnsupportedOperationException("needs to be updated");
    }

    
    /**
     * Test of getWorkspace method, of class JdbcWorkspaceDao.
     */
    @Test
    public void testGetWorkspace() {
        System.out.println("getWorkspace");
        int workspaceID = 0;
        LamusJdbcWorkspaceDao instance = new LamusJdbcWorkspaceDao();
        Workspace expResult = null;
        Workspace result = instance.getWorkspace(workspaceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
        
        assertTrue(result);
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
        
        assertFalse(result);
    }
}
