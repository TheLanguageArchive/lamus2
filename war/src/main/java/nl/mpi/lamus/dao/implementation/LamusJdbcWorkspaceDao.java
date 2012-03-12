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
import javax.sql.DataSource;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.Workspace;
import nl.mpi.lamus.workspace.WorkspaceNode;
import nl.mpi.lamus.workspace.WorkspaceNodeImpl;
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
                .usingGeneratedKeyColumns("workspaceid")
                .usingColumns(
                    "userid",
                    "topnodeid",
                    "startdate",
                    "enddate",
                    "sessionstartdate",
                    "sessionenddate",
                    "usedstoragespace",
                    "maxstoragespace",
                    "status",
                    "message",
                    "archiveinfo");
    }
    

    public void addWorkspace(Workspace workspace) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("user_id", workspace.getUserID())
                .addValue("top_node_id", workspace.getTopNodeID())
                .addValue("start_date", new Timestamp(workspace.getStartDate().getTime()))
                .addValue("end_date", new Timestamp(workspace.getEndDate().getTime()))
                .addValue("session_start_date", new Timestamp(workspace.getSessionStartDate().getTime()))
                .addValue("session_end_date", new Timestamp(workspace.getSessionEndDate().getTime()))
                .addValue("used_storage_space", workspace.getUsedStorageSpace())
                .addValue("max_storage_space", workspace.getMaxStorageSpace())
                .addValue("status", workspace.getStatus().toString())
                .addValue("message", workspace.getMessage())
                .addValue("archive_info", workspace.getArchiveInfo());
            Number newID = this.insertWorkspace.executeAndReturnKey(parameters);
        workspace.setWorkspaceID(newID.intValue());
    }

    public Workspace getWorkspace(int workspaceID) {
        throw new UnsupportedOperationException("Not supported yet.");
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
