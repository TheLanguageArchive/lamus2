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
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import javax.sql.DataSource;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceStatus;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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
    
    private static final Logger logger = LoggerFactory.getLogger(LamusJdbcWorkspaceDao.class);
    
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private SimpleJdbcInsert insertWorkspace;
    private SimpleJdbcInsert insertWorkspaceNode;
    private SimpleJdbcInsert insertWorkspaceNodeLink;
    
    @Autowired
    @Qualifier("lamusDataSource")
    public void setDataSource(DataSource datasource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(datasource);
        this.insertWorkspace = new SimpleJdbcInsert(datasource)
                .withTableName("workspace")
                .usingGeneratedKeyColumns("workspace_id")
                .usingColumns(
                    "user_id",
                    "top_node_id",
                    "top_node_archive_url",
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
        
        this.insertWorkspaceNode = new SimpleJdbcInsert(datasource)
                .withTableName("node")
                .usingGeneratedKeyColumns("workspace_node_id")
                .usingColumns(
                    "workspace_id",
                    "archive_node_id",
                    "profile_schema_uri",
                    "name",
                    "title",
                    "type",
                    "workspace_url",
                    "archive_url",
                    "origin_url",
                    "status",
                    "pid",
                    "format");
        //TODO Inject table and column names
        
        this.insertWorkspaceNodeLink = new SimpleJdbcInsert(datasource)
                .withTableName("node_link")
                .usingColumns(
                    "parent_workspace_node_id",
                    "child_workspace_node_id",
                    "child_uri");
        //TODO Inject table and column names
        
    }
    

    public void addWorkspace(Workspace workspace) {
        
        logger.debug("Adding workspace to the database in node with ID: " + workspace.getTopNodeID());

        String topNodeArchiveURLStr = null;
        if(workspace.getTopNodeArchiveURL() != null) {
            topNodeArchiveURLStr = workspace.getTopNodeArchiveURL().toString();
        }
        
        //TODO end dates are null when adding a workspace, which makes sense; is there any case where it would be different?
        // if dates are null, let the database throw the errors
        Timestamp startDate = null;
        if(workspace.getStartDate() != null) {
            startDate = new Timestamp(workspace.getStartDate().getTime());
        }
        Timestamp sessionStartDate = null;
        if(workspace.getSessionStartDate() != null) {
            sessionStartDate = new Timestamp(workspace.getSessionStartDate().getTime());
        }
        String statusStr = null;
        if(workspace.getStatus() != null) {
            statusStr = workspace.getStatus().toString();
        }
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("user_id", workspace.getUserID())
                .addValue("top_node_id", workspace.getTopNodeID())
                .addValue("top_node_archive_url", topNodeArchiveURLStr)
                .addValue("start_date", startDate)
                .addValue("session_start_date", sessionStartDate)
                .addValue("used_storage_space", workspace.getUsedStorageSpace())
                .addValue("max_storage_space", workspace.getMaxStorageSpace())
                .addValue("status", statusStr)
                .addValue("message", workspace.getMessage())
                .addValue("archive_info", workspace.getArchiveInfo());
        Number newID = this.insertWorkspace.executeAndReturnKey(parameters);
        workspace.setWorkspaceID(newID.intValue());
        
        logger.info("Workspace added to the database. Workspace ID: " + workspace.getWorkspaceID());
    }
    
    
    public void updateWorkspaceTopNode(Workspace workspace) {
        
        logger.debug("Updating workspace with ID: " + workspace.getWorkspaceID() + "; setting top node to: " + workspace.getTopNodeID());
        
        String topNodeArchiveURLStr = null;
        if(workspace.getTopNodeArchiveURL() != null) {
            topNodeArchiveURLStr = workspace.getTopNodeArchiveURL().toString();
        }
        
        String updateSql = "UPDATE workspace SET top_node_id = :top_node_id, top_node_archive_url = :top_node_archive_url"
                + " WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("top_node_id", workspace.getTopNodeID())
                .addValue("top_node_archive_url", topNodeArchiveURLStr)
                .addValue("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Top node of workspace " + workspace.getWorkspaceID() + " updated to node " + workspace.getTopNodeID());
    }
    
    public void updateWorkspaceSessionDates(Workspace workspace) {
                
        logger.debug("Updating workspace with ID: " + workspace.getWorkspaceID() + "; setting session start date to: "
                + workspace.getSessionStartDate() + " and session end date to: " + workspace.getSessionEndDate());
        
        String updateSql = "UPDATE workspace SET session_start_date = :session_start_date, session_end_date = :session_end_date"
                + " WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("session_start_date", workspace.getSessionStartDate())
                .addValue("session_end_date", workspace.getSessionEndDate())
                .addValue("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Session start date of workspace " + workspace.getWorkspaceID() + " updated to " + workspace.getSessionStartDate()
                + " and session end date updated to " + workspace.getSessionEndDate());
    }

    public void updateWorkspaceStorageSpace(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateWorkspaceStatusMessage(Workspace workspace) {
        
        logger.debug("Uploading workspace with ID: " + workspace.getWorkspaceID() + "; setting status to: " + workspace.getStatus() 
                + "; setting message to: \"" + workspace.getMessage() + "\"");
        
        String updateSql = "UPDATE workspace SET status = :status, message = :message WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("status", workspace.getStatus())
                .addValue("message", workspace.getMessage())
                .addValue("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Status of workspace " + workspace.getWorkspaceID() + " updated to \"" + workspace.getStatus()
                + "\" and message updated to \"" + workspace.getMessage() + "\"");
    }

    public Workspace getWorkspace(int workspaceID) {
        
        logger.debug("Retrieving workspace with ID: " + workspaceID);
        
        String queryWorkspaceSql = "select * from workspace where workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        RowMapper<Workspace> mapper = new RowMapper<Workspace>() {
          public Workspace mapRow(ResultSet rs, int rowNum) throws SQLException {
              URL topNodeArchiveURL = null;
              if(rs.getString("top_node_archive_url") != null) {
                    try {
                        topNodeArchiveURL = new URL(rs.getString("top_node_archive_url"));
                    } catch (MalformedURLException ex) {
                        logger.warn("Top Node Archive URL is malformed; null used instead", ex);
                    }
              }
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
        };
        
        Workspace workspaceToReturn;
        try {
            workspaceToReturn = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceSql, namedParameters, mapper);
        } catch(EmptyResultDataAccessException ex) {
            logger.warn("Workspace with ID " + workspaceID + " does not exist in the database");
            return null;
        }
        
        logger.info("Workspace with ID " + workspaceID + " retrieved from the database");
        
        return workspaceToReturn;
    }
    
//    public void updateWorkspaceEndDates(Workspace workspace) {
//
//        Timestamp endDateTimestamp = null;
//        if (workspace.getEndDate() != null) {
//            endDateTimestamp = new Timestamp(workspace.getEndDate().getTime());
//        }
//        Date sessionEndDateTimestamp = null;
//        if (workspace.getSessionEndDate() != null) {
//            sessionEndDateTimestamp = new Timestamp(workspace.getSessionEndDate().getTime());
//        }
//
//        String updateWorkspaceSql = "update workspace set end_date = :end_date, session_end_date = :session_end_date "
//                + "where workspace_id = " + workspace.getWorkspaceID();
//
//        SqlParameterSource parameters = new MapSqlParameterSource().addValue("end_date", endDateTimestamp).addValue("session_end_date", sessionEndDateTimestamp);
//
//        namedParameterJdbcTemplate.update(updateWorkspaceSql, parameters);
//    }

    public boolean isNodeLocked(int archiveNodeID) {
        
        logger.debug("Checking if node with archive ID " + archiveNodeID + " is locked");
        
        String queryNodeSql = "select workspace_node_id from node where archive_node_id = :archive_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("archive_node_id", archiveNodeID);
        
        RowMapper<WorkspaceNode> mapper = new RowMapper<WorkspaceNode>() {
            public WorkspaceNode mapRow(ResultSet rs, int rowNum) throws SQLException {
                WorkspaceNode node = new LamusWorkspaceNode();
                node.setWorkspaceNodeID(rs.getInt("workspace_node_id"));
                return node;
            }
        };
        
        try {
            this.namedParameterJdbcTemplate.queryForObject(queryNodeSql, namedParameters, mapper);
        } catch(EmptyResultDataAccessException eex) {
            logger.info("Node with archive ID " + archiveNodeID + " is not locked (there is no existing workspace that contains this node).");
            return false;
        } catch(IncorrectResultSizeDataAccessException iex) {
            logger.warn("Node with archive ID " + archiveNodeID + " is locked more than once.");
            return true;
        }

        logger.info("Node with archive ID " + archiveNodeID + " is locked (there is already a workspace that contains this node).");
        return true;
    }

    public void addWorkspaceNode(WorkspaceNode node) {
                
        logger.debug("Adding node to the database belonging to workspace with ID: " + node.getWorkspaceID());

        String profileSchemaURIStr = null;
        if(node.getProfileSchemaURI() != null) {
            profileSchemaURIStr = node.getProfileSchemaURI().toString();
        }
        String workspaceURLStr = null;
        if(node.getWorkspaceURL() != null) {
            workspaceURLStr = node.getWorkspaceURL().toString();
        }
        String archiveURLStr = null;
        if(node.getArchiveURL() != null) {
            archiveURLStr = node.getArchiveURL().toString();
        }
        String originURLStr = null;
        if(node.getOriginURL() != null) {
            originURLStr = node.getOriginURL().toString();
        }
        String statusStr = null;
        if(node.getStatus() != null) {
            statusStr = node.getStatus().toString();
        }
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("workspace_id", node.getWorkspaceID())
                .addValue("archive_node_id", node.getArchiveNodeID())
                .addValue("profile_schema_uri", profileSchemaURIStr)
                .addValue("name", node.getName())
                .addValue("title", node.getTitle())
                .addValue("type", node.getType())
                .addValue("workspace_url", workspaceURLStr)
                .addValue("archive_url", archiveURLStr)
                .addValue("origin_url", originURLStr)
                .addValue("status", statusStr)
                .addValue("pid", node.getPid())
                .addValue("format", node.getFormat());
        Number newID = this.insertWorkspaceNode.executeAndReturnKey(parameters);
        node.setWorkspaceNodeID(newID.intValue());
        
        logger.info("Node added to the database. Node ID: " + node.getWorkspaceNodeID());
    }

    public WorkspaceNode getWorkspaceNode(int workspaceNodeID) {

        logger.debug("Retrieving workspace node with ID: " + workspaceNodeID);
        
        String queryWorkspaceNodeSql = "select * from node where workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_node_id", workspaceNodeID);
        
        WorkspaceNode workspaceNodeToReturn;
        try {
            workspaceNodeToReturn = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceNodeSql, namedParameters, new WorkspaceNodeMapper());
        } catch(EmptyResultDataAccessException ex) {
            logger.warn("Workspace Node with ID " + workspaceNodeID + " does not exist in the database");
            return null;
        }
        
        logger.info("Workspace Node with ID " + workspaceNodeID + " retrieved from the database");
        
        return workspaceNodeToReturn;
    }
    
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID) {
        
        logger.debug("Retrieving list containing child nodes of the node with ID: " + workspaceNodeID);
        
        String queryWorkspaceNodeListSql = "select * from node where workspace_node_id in "
                + "(select child_workspace_node_id from node_link where parent_workspace_node_id = :parent_workspace_node_id)";
        SqlParameterSource namedParameters = new MapSqlParameterSource("parent_workspace_node_id", workspaceNodeID);
        
        //TODO check for exception?
        
        Collection<WorkspaceNode> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        
        return listToReturn;
    }

    public void addWorkspaceNodeLink(WorkspaceNodeLink nodeLink) {
        
        logger.debug("Adding to the database a link between node with ID: " + nodeLink.getParentWorkspaceNodeID()
                + " and node with ID: " + nodeLink.getChildWorkspaceNodeID());
        
        String childResourceProxyURIStr = null;
        if(nodeLink.getChildURI() != null) {
            childResourceProxyURIStr = nodeLink.getChildURI().toString();
        }
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("parent_workspace_node_id", nodeLink.getParentWorkspaceNodeID())
                .addValue("child_workspace_node_id", nodeLink.getChildWorkspaceNodeID())
                .addValue("child_uri", childResourceProxyURIStr);
        this.insertWorkspaceNodeLink.execute(parameters);

        logger.info("Link added to the database. Parent node ID: " + nodeLink.getParentWorkspaceNodeID()
                + "; Child node ID: " + nodeLink.getChildWorkspaceNodeID());
    }

    
    private static final class WorkspaceNodeMapper implements RowMapper<WorkspaceNode> {

        public WorkspaceNode mapRow(ResultSet rs, int rowNum) throws SQLException {

            int archiveNodeID = -1;
            if(rs.getString("archive_node_id") != null) {
                archiveNodeID = rs.getInt("archive_node_id");
            }
            URI profileSchemaURI = null;
            if(rs.getString("profile_schema_uri") != null) {
                try {
                    profileSchemaURI = new URI(rs.getString("profile_schema_uri"));
                } catch (URISyntaxException ex) {
                    logger.warn("Profile Schema URI has invalid syntax; null used instead", ex);
                }
            }
            URL workspaceURL = null;
            if(rs.getString("workspace_url") != null) {
                try {
                    workspaceURL = new URL(rs.getString("workspace_url"));
                } catch (MalformedURLException ex) {
                    logger.warn("Workspace URL is malformed; null used instead", ex);
                }
            }
            URL archiveURL = null;
            if(rs.getString("archive_url") != null) {
                try {
                    archiveURL = new URL(rs.getString("archive_url"));
                } catch (MalformedURLException ex) {
                    logger.warn("Archive URL is malformed; null used instead", ex);
                }
            }
            URL originURL = null;
            if(rs.getString("origin_url") != null) {
                try {
                    originURL = new URL(rs.getString("origin_url"));
                } catch (MalformedURLException ex) {
                    logger.warn("Origin URL is malformed; null used instead", ex);
                }
            }

            WorkspaceNode workspaceNode = new LamusWorkspaceNode(
                    rs.getInt("workspace_node_id"),
                    rs.getInt("workspace_id"),
                    archiveNodeID,
                    profileSchemaURI,
                    rs.getString("name"),
                    rs.getString("title"),
                    WorkspaceNodeType.valueOf(rs.getString("type")),
                    workspaceURL,
                    archiveURL,
                    originURL,
                    WorkspaceNodeStatus.valueOf(rs.getString("status")),
                    rs.getString("pid"),
                    rs.getString("format"));
            return workspaceNode;
        }

    }
}