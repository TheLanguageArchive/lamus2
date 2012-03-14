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
import java.sql.Timestamp;
import java.util.Date;
import javax.sql.DataSource;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.LamusWorkspace;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.WorkspaceNode;
import nl.mpi.lamus.workspace.WorkspaceNodeImpl;
import nl.mpi.lamus.workspace.WorkspaceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Repository
public class LamusJdbcWorkspaceDao implements WorkspaceDao {
    
//    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private SimpleJdbcInsert insertWorkspace;
    
    @Autowired
    public void setDataSource(DataSource datasource) {
//        this.jdbcTemplate = new JdbcTemplate(datasource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
        this.insertWorkspace = new SimpleJdbcInsert(datasource)
                .withTableName("workspace")
                .usingGeneratedKeyColumns("workspace_id")
                .usingColumns(
                    "user_id",
                    "top_node_id",
                    "start_date",
                    "end_date",
                    "session_start_date",
                    "session_end_date",
                    "used_storage_space",
                    "max_storage_space",
                    "status",
                    "message",
                    "archive_info");
        //TODO Inject table and column names
    }
    

    public void addWorkspace(Workspace workspace) {
        
        Timestamp endDate = null;
        if(workspace.getEndDate() != null) {
            endDate = new Timestamp(workspace.getEndDate().getTime());
        }
        Timestamp sessionEndDate = null;
        if(workspace.getSessionEndDate() != null) {
            sessionEndDate = new Timestamp(workspace.getSessionEndDate().getTime());
        }
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("user_id", workspace.getUserID())
                .addValue("top_node_id", workspace.getTopNodeID())
                .addValue("start_date", new Timestamp(workspace.getStartDate().getTime()))
                .addValue("end_date", endDate)
                .addValue("session_start_date", new Timestamp(workspace.getSessionStartDate().getTime()))
                .addValue("session_end_date", sessionEndDate)
                .addValue("used_storage_space", workspace.getUsedStorageSpace())
                .addValue("max_storage_space", workspace.getMaxStorageSpace())
                .addValue("status", workspace.getStatus().toString())
                .addValue("message", workspace.getMessage())
                .addValue("archive_info", workspace.getArchiveInfo());
            Number newID = this.insertWorkspace.executeAndReturnKey(parameters);
        workspace.setWorkspaceID(newID.intValue());
    }

    public Workspace getWorkspace(int workspaceID) {
        
        String queryWorkspaceSql = "select * from workspace where workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        RowMapper<Workspace> mapper = new RowMapper<Workspace>() {
          public Workspace mapRow(ResultSet rs, int rowNum) throws SQLException {
              Date endDate = null;
              if(rs.getTimestamp("end_date") != null) {
                  endDate = new Date(rs.getTimestamp("end_date").getTime());
              }
              Date sessionEndDate = null;
              if(rs.getTimestamp("session_end_date") != null) {
                  sessionEndDate = new Date(rs.getTimestamp("session_end_date").getTime());
              }
              
              Workspace workspace = new LamusWorkspace(
                      rs.getInt("workspace_id"),
                      rs.getString("user_id"),
                      rs.getInt("top_node_id"),
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
        };
        
        Workspace workspaceToReturn;
        try {
            workspaceToReturn = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceSql, namedParameters, mapper);
        } catch(EmptyResultDataAccessException ex) {
            return null;
        }
        
        return workspaceToReturn;
    }

    public boolean isNodeLocked(int archiveNodeID) {
        
        String queryNodeSql = "select workspace_node_id from node where archive_node_id = :archive_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("archive_node_id", archiveNodeID);
        
        RowMapper<WorkspaceNode> mapper = new RowMapper<WorkspaceNode>() {
            public WorkspaceNode mapRow(ResultSet rs, int rowNum) throws SQLException {
                WorkspaceNode node = new WorkspaceNodeImpl();
                node.setWorkspaceNodeID(rs.getInt("workspace_node_id"));
                return node;
            }
        };
        
        WorkspaceNode retrievedNode;
        try {
            retrievedNode = this.namedParameterJdbcTemplate.queryForObject(queryNodeSql, namedParameters, mapper);
        } catch(EmptyResultDataAccessException ex) {
            return false;
        }

        return (retrievedNode != null);
    }
}
