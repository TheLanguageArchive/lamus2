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
import javax.sql.DataSource;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * Data access layer that uses JDBC from the Spring framework
 * in order to access tha lamus database.
 * 
 * @see WorkspaceDao
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
//@Repository
public class LamusJdbcWorkspaceDao implements WorkspaceDao {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusJdbcWorkspaceDao.class);
    
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private SimpleJdbcInsert insertWorkspace;
    private SimpleJdbcInsert insertWorkspaceNode;
    private SimpleJdbcInsert insertWorkspaceNodeLink;
    
    public LamusJdbcWorkspaceDao(DataSource dataSource) {
        this.setDataSource(dataSource);
    }
    
    /**
     * Setter for the DataSource that will be used by the several
     * JdbcTemplate and JdbcInsert objects.
     * 
     * @param datasource data source to be used for accessing the lamus database
     */
//    @Autowired
//    @Qualifier("lamusDataSource")
    public final void setDataSource(DataSource datasource) {
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
    
    /**
     * @see WorkspaceDao#addWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
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
    
    /**
     * @see WorkspaceDao#deleteWorkspace(int)
     */
    public void deleteWorkspace(int workspaceID) {
        
        logger.debug("Deleting workspace with ID " + workspaceID);
        
        String deleteNodeLinkSql = "DELETE FROM node_link WHERE parent_workspace_node_id IN (SELECT workspace_node_id FROM node WHERE workspace_id = :workspace_id);";
        String deleteNodeSql = "DELETE FROM node WHERE workspace_ID = :workspace_id;";
        String deleteWorkspaceSql = "DELETE FROM workspace WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        this.namedParameterJdbcTemplate.update(deleteNodeLinkSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodeSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteWorkspaceSql, namedParameters);
        
        logger.info("Workspace with ID " + workspaceID + " deleted");
    }
    
    /**
     * @see WorkspaceDao#updateWorkspaceTopNode(nl.mpi.lamus.workspace.model.Workspace)
     */
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
    
    /**
     * @see WorkspaceDao#updateWorkspaceSessionDates(nl.mpi.lamus.workspace.model.Workspace)
     */
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

    /**
     * @see WorkspaceDao#updateWorkspaceStorageSpace(nl.mpi.lamus.workspace.model.Workspace)
     */
    public void updateWorkspaceStorageSpace(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @see WorkspaceDao#updateWorkspaceStatusMessage(nl.mpi.lamus.workspace.model.Workspace)
     */
    public void updateWorkspaceStatusMessage(Workspace workspace) {
        
        logger.debug("Uploading workspace with ID: " + workspace.getWorkspaceID() + "; setting status to: " + workspace.getStatus() 
                + "; setting message to: \"" + workspace.getMessage() + "\"");
        
        String statusStr = null;
        if(workspace.getStatus() != null) {
            statusStr = workspace.getStatus().toString();
        }
        
        String updateSql = "UPDATE workspace SET status = :status, message = :message WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("status", statusStr)
                .addValue("message", workspace.getMessage())
                .addValue("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Status of workspace " + workspace.getWorkspaceID() + " updated to \"" + statusStr
                + "\" and message updated to \"" + workspace.getMessage() + "\"");
    }

    /**
     * @see WorkspaceDao#getWorkspace(int)
     */
    public Workspace getWorkspace(int workspaceID) {
        
        logger.debug("Retrieving workspace with ID: " + workspaceID);
        
        String queryWorkspaceSql = "SELECT * FROM workspace WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        Workspace workspaceToReturn;
        try {
            workspaceToReturn = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceSql, namedParameters, new WorkspaceMapper());
        } catch(EmptyResultDataAccessException ex) {
            logger.warn("Workspace with ID " + workspaceID + " does not exist in the database");
            return null;
        }
        
        logger.info("Workspace with ID " + workspaceID + " retrieved from the database");
        
        return workspaceToReturn;
    }
    
    /**
     * @see WorkspaceDao#listWorkspacesForUser(java.lang.String)
     */
    public Collection<Workspace> listWorkspacesForUser(String userID) {

        logger.debug("Retrieving list of workspace created by user with ID: " + userID);
        
        String queryWorkspaceListSql = "SELECT * FROM workspace WHERE user_id = :user_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("user_id", userID);
        
        Collection<Workspace> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceListSql, namedParameters, new WorkspaceMapper());
        
        return listToReturn;
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

    /**
     * @see WorkspaceDao#isNodeLocked(int)
     */
    public boolean isNodeLocked(int archiveNodeID) {
        
        logger.debug("Checking if node with archive ID " + archiveNodeID + " is locked");
        
        String queryNodeSql = "SELECT workspace_node_id FROM node WHERE archive_node_id = :archive_node_id";
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

    /**
     * @see WorkspaceDao#addWorkspaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    public void addWorkspaceNode(WorkspaceNode node) {
                
        logger.debug("Adding node to the database belonging to workspace with ID: " + node.getWorkspaceID());

        String profileSchemaURIStr = null;
        if(node.getProfileSchemaURI() != null) {
            profileSchemaURIStr = node.getProfileSchemaURI().toString();
        }
        String typeStr = null;
        if(node.getType() != null) {
            typeStr = node.getType().toString();
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
                .addValue("type", typeStr)
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

    /**
     * @see WorkspaceDao#getWorkspaceNode(int)
     */
    public WorkspaceNode getWorkspaceNode(int workspaceNodeID) {

        logger.debug("Retrieving workspace node with ID: " + workspaceNodeID);
        
        String queryWorkspaceNodeSql = "SELECT * FROM node WHERE workspace_node_id = :workspace_node_id";
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
    
    /**
     * @see WorkspaceDao#getNodesForWorkspace(int)
     */
    public Collection<WorkspaceNode> getNodesForWorkspace(int workspaceID) {
        
        logger.debug("Retrieving list containing nodes of the workspace with ID " + workspaceID);
        
        String queryWorkspaceNodeListSql = "SELECT * FROM node WHERE workspace_id = :workspace_id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        Collection<WorkspaceNode> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }
    
    /**
     * @see WorkspaceDao#getChildWorkspaceNodes(int)
     */
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID) {
        
        logger.debug("Retrieving list containing child nodes of the node with ID: " + workspaceNodeID);
        
        String queryWorkspaceNodeListSql = "SELECT * FROM node WHERE workspace_node_id IN "
                + "(SELECT child_workspace_node_id FROM node_link WHERE parent_workspace_node_id = :parent_workspace_node_id)";
        SqlParameterSource namedParameters = new MapSqlParameterSource("parent_workspace_node_id", workspaceNodeID);
        
        Collection<WorkspaceNode> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }
    
    public void updateNodeWorkspaceURL(WorkspaceNode node) {
        
        logger.debug("Updating workspace URL for node with ID: " + node.getWorkspaceNodeID() + "; setting workspace URL to: " + node.getWorkspaceURL());
        
        String nodeWorkspaceURLStr = null;
        if(node.getWorkspaceURL() != null) {
            nodeWorkspaceURLStr = node.getWorkspaceURL().toString();
        }
        
        String updateSql = "UPDATE node SET workspace_url = :workspace_url"
                + " WHERE workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("workspace_url", nodeWorkspaceURLStr)
                .addValue("workspace_node_id", node.getWorkspaceNodeID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Workspace URL of node " + node.getWorkspaceNodeID() + " updated to " + node.getWorkspaceURL());
    }

    /**
     * @see WorkspaceDao#addWorkspaceNodeLink(nl.mpi.lamus.workspace.model.WorkspaceNodeLink)
     */
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

    
    /**
     * Inner class used to map rows from the workspace table into Workspace objects in queries
     */
    private static final class WorkspaceMapper implements RowMapper<Workspace> {
        
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
    }
    
    /**
     * Inner class used to map rows from the node table into WorkspaceNode objects in queries
     */
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
    
    /**
     * Inner class used to map rows from the node_link table into WorkspaceNodeLink objects in queries
     */
    private static final class WorkspaceNodeLinkMapper implements RowMapper<WorkspaceNodeLink> {
        
        public WorkspaceNodeLink mapRow(ResultSet rs, int rowNum) throws SQLException {

            URI childURI = null;
            if(rs.getString("child_uri") != null) {
                try {
                    childURI = new URI(rs.getString("child_uri"));
                } catch (URISyntaxException ex) {
                    logger.warn("Child URI has an invalid syntax; null used instead", ex);
                }
            }
            
            WorkspaceNodeLink workspaceNodeLink = new LamusWorkspaceNodeLink(
                    rs.getInt("parent_workspace_node_id"),
                    rs.getInt("child_workspace_node_id"),
                    childURI);
            return workspaceNodeLink;
        }
    }
}