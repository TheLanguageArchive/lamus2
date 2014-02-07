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
import java.util.List;
import java.util.logging.Level;
import javax.sql.DataSource;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
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
                    "top_node_archive_uri",
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
                    "profile_schema_uri",
                    "name",
                    "title",
                    "type",
                    "workspace_url",
                    "archive_uri",
                    "archive_url",
                    "origin_url",
                    "status",
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
    @Override
    public void addWorkspace(Workspace workspace) {
        
        logger.debug("Adding workspace to the database in node " + workspace.getTopNodeArchiveURI());

        String topNodeArchiveUriStr = null;
        if(workspace.getTopNodeArchiveURI() != null) {
            topNodeArchiveUriStr = workspace.getTopNodeArchiveURI().toString();
        }
        String topNodeArchiveUrlStr = null;
        if(workspace.getTopNodeArchiveURL() != null) {
            topNodeArchiveUrlStr = workspace.getTopNodeArchiveURL().toString();
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
                .addValue("top_node_archive_uri", topNodeArchiveUriStr)
                .addValue("top_node_archive_url", topNodeArchiveUrlStr)
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
    @Override
    public void deleteWorkspace(int workspaceID) {
        
        logger.debug("Deleting workspace with ID " + workspaceID);
        
        String deleteNodeLinkSql = "DELETE FROM node_link WHERE parent_workspace_node_id IN (SELECT workspace_node_id FROM node WHERE workspace_id = :workspace_id);";
        String deleteNodeSql = "DELETE FROM node WHERE workspace_ID = :workspace_id;";
        String deleteWorkspaceSql = "DELETE FROM workspace WHERE workspace_id = :workspace_id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        this.namedParameterJdbcTemplate.update(deleteNodeLinkSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodeSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteWorkspaceSql, namedParameters);
        
        logger.info("Workspace with ID " + workspaceID + " deleted");
    }
    
    /**
     * @see WorkspaceDao#updateWorkspaceTopNode(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceTopNode(Workspace workspace) {
        
        logger.debug("Updating workspace with ID: " + workspace.getWorkspaceID()
                + "; setting top node to: workspace ID = " + workspace.getTopNodeID() + "; archive URI = " + workspace.getTopNodeArchiveURI());
        
        String topNodeArchiveUriStr = null;
        if(workspace.getTopNodeArchiveURI() != null) {
            topNodeArchiveUriStr = workspace.getTopNodeArchiveURI().toString();
        }
        String topNodeArchiveUrlStr = null;
        if(workspace.getTopNodeArchiveURL() != null) {
            topNodeArchiveUrlStr = workspace.getTopNodeArchiveURL().toString();
        }
        
        String updateSql = "UPDATE workspace SET top_node_id = :top_node_id,"
                + " top_node_archive_uri = :top_node_archive_uri, top_node_archive_url = :top_node_archive_url"
                + " WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("top_node_id", workspace.getTopNodeID())
                .addValue("top_node_archive_uri", topNodeArchiveUriStr)
                .addValue("top_node_archive_url", topNodeArchiveUrlStr)
                .addValue("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Top node of workspace " + workspace.getWorkspaceID()
                + " updated to: workspace ID = " + workspace.getTopNodeID() + "; archive URI = " + workspace.getTopNodeArchiveURI());
    }
    
    /**
     * @see WorkspaceDao#updateWorkspaceSessionDates(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
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
    @Override
    public void updateWorkspaceStorageSpace(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @see WorkspaceDao#updateWorkspaceStatusMessage(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceStatusMessage(Workspace workspace) {
        
        logger.debug("Updating workspace with ID: " + workspace.getWorkspaceID() + "; setting status to: " + workspace.getStatus() 
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
     * @see WorkspaceDao#updateWorkspaceEndDates(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceEndDates(Workspace workspace) {
        
        logger.debug("Updating workspace with ID: " + workspace.getWorkspaceID() + "; setting end date to: " + workspace.getEndDate()
                + "; setting session end date to: \"" + workspace.getSessionEndDate() + "\"");
        
        String updateSql = "UPDATE workspace SET end_date = :end_date, session_end_date = :session_end_date"
                + " WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("end_date", workspace.getEndDate())
                .addValue("session_end_date", workspace.getSessionEndDate())
                .addValue("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("End date of workspace " + workspace.getWorkspaceID() + " updated to " + workspace.getEndDate()
                + " and session end date updated to " + workspace.getSessionEndDate());

    }

    /**
     * @see WorkspaceDao#getWorkspace(int)
     */
    @Override
    public Workspace getWorkspace(int workspaceID)
            throws WorkspaceNotFoundException {
        
        logger.debug("Retrieving workspace with ID: " + workspaceID);
        
        String queryWorkspaceSql = "SELECT * FROM workspace WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        Workspace workspaceToReturn;
        try {
            workspaceToReturn = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceSql, namedParameters, new WorkspaceMapper());
        } catch(EmptyResultDataAccessException ex) {
            String errorMessage = "Workspace with ID " + workspaceID + " does not exist in the database";
            logger.error(errorMessage, ex);
            throw new WorkspaceNotFoundException(errorMessage, workspaceID, ex);
        }
        
        logger.info("Workspace with ID " + workspaceID + " retrieved from the database");
        
        return workspaceToReturn;
    }
    
    /**
     * @see WorkspaceDao#getWorkspacesForUser(java.lang.String)
     */
    @Override
    public Collection<Workspace> getWorkspacesForUser(String userID) {

        logger.debug("Retrieving list of workspace created by user with ID: " + userID);
        
        String queryWorkspaceListSql = "SELECT * FROM workspace WHERE user_id = :user_id"
                + " AND status NOT IN (:submitted_status, :error_status, :success_status)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("user_id", userID)
                .addValue("submitted_status", WorkspaceStatus.SUBMITTED.toString())
                .addValue("error_status", WorkspaceStatus.DATA_MOVED_ERROR.toString())
                .addValue("success_status", WorkspaceStatus.DATA_MOVED_SUCCESS.toString());
        
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
     * @see WorkspaceDao#isNodeLocked(java.net.URI)
     */
    @Override
    public boolean isNodeLocked(URI archiveNodeURI) {
        
        logger.debug("Checking if node " + archiveNodeURI + " is locked");
        
        String queryNodeSql = "SELECT workspace_node_id FROM node WHERE archive_uri = :uri";
        SqlParameterSource namedParameters = new MapSqlParameterSource("uri", archiveNodeURI.toString());
        
        RowMapper<WorkspaceNode> mapper = new RowMapper<WorkspaceNode>() {
            @Override
            public WorkspaceNode mapRow(ResultSet rs, int rowNum) throws SQLException {
                WorkspaceNode node = new LamusWorkspaceNode();
                node.setWorkspaceNodeID(rs.getInt("workspace_node_id"));
                return node;
            }
        };
        
        try {
            this.namedParameterJdbcTemplate.queryForObject(queryNodeSql, namedParameters, mapper);
        } catch(EmptyResultDataAccessException eex) {
            logger.info("Node " + archiveNodeURI.toString() + " is not locked (there is no existing workspace that contains this node).");
            return false;
        } catch(IncorrectResultSizeDataAccessException iex) {
            logger.warn("Node " + archiveNodeURI.toString() + " is locked more than once.");
            return true;
        }

        logger.info("Node " + archiveNodeURI + " is locked (there is already a workspace that contains this node).");
        return true;
    }

    /**
     * @see WorkspaceDao#getWorkspaceNodeByArchiveURI(java.net.URI)
     */
    @Override
    public Collection<WorkspaceNode> getWorkspaceNodeByArchiveURI(URI archiveNodeURI) {
        
        logger.debug("Retrieving locked node " + archiveNodeURI);
        
        
        String queryWorkspaceNodeListSql = "SELECT * FROM node WHERE archive_uri = :uri";
        SqlParameterSource namedParameters = new MapSqlParameterSource("uri", archiveNodeURI.toString());
        
        Collection<WorkspaceNode> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }

    /**
     * @see WorkspaceDao#addWorkspaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
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
        String archiveUriStr = null;
        if(node.getArchiveURI() != null) {
            archiveUriStr = node.getArchiveURI().toString();
        }
        String archiveUrlStr = null;
        if(node.getArchiveURL() != null) {
            archiveUrlStr = node.getArchiveURL().toString();
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
                .addValue("profile_schema_uri", profileSchemaURIStr)
                .addValue("name", node.getName())
                .addValue("title", node.getTitle())
                .addValue("type", typeStr)
                .addValue("workspace_url", workspaceURLStr)
                .addValue("archive_uri", archiveUriStr)
                .addValue("archive_url", archiveUrlStr)
                .addValue("origin_url", originURLStr)
                .addValue("status", statusStr)
                .addValue("format", node.getFormat());
        Number newID = this.insertWorkspaceNode.executeAndReturnKey(parameters);
        node.setWorkspaceNodeID(newID.intValue());
        
        logger.info("Node added to the database. Node ID: " + node.getWorkspaceNodeID());
    }
    
    /**
     * @see WorkspaceDao#setWorkspaceNodeAsDeleted(int, int)
     */
    @Override
    public void setWorkspaceNodeAsDeleted(int workspaceID, int nodeID) {
        
        logger.debug("Setting node " + nodeID + " in workspace " + workspaceID + " as deleted");
        
        String updateSql = "UPDATE node SET status = :status"
                + " WHERE workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("status", WorkspaceNodeStatus.NODE_DELETED.toString())
                .addValue("workspace_node_id", nodeID);
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Node " + nodeID + " in workspace " + workspaceID + " was set as deleted");
    }

    /**
     * @see WorkspaceDao#getWorkspaceNode(int)
     */
    @Override
    public WorkspaceNode getWorkspaceNode(int workspaceNodeID)
            throws WorkspaceNodeNotFoundException {

        logger.debug("Retrieving workspace node with ID: " + workspaceNodeID);
        
        String queryWorkspaceNodeSql = "SELECT * FROM node WHERE workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_node_id", workspaceNodeID);
        
        WorkspaceNode workspaceNodeToReturn;
        try {
            workspaceNodeToReturn = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceNodeSql, namedParameters, new WorkspaceNodeMapper());
        } catch(EmptyResultDataAccessException ex) {
            String errorMessage = "Workspace Node with ID " + workspaceNodeID + " does not exist in the database";
            logger.error(errorMessage, ex);
            throw new WorkspaceNodeNotFoundException(errorMessage, -1, workspaceNodeID, ex);
        }
        
        logger.info("Workspace Node with ID " + workspaceNodeID + " retrieved from the database");
        
        return workspaceNodeToReturn;
    }
    
    /**
     * @see WorkspaceDao#getWorkspaceTopNode(int)
     */
    @Override
    public WorkspaceNode getWorkspaceTopNode(int workspaceID) throws WorkspaceNodeNotFoundException {

        logger.debug("Retrieving top node of workspace with ID: " + workspaceID);
        
        String queryWorkspaceNodeSql = "SELECT * FROM node WHERE workspace_node_id IN (SELECT top_node_id FROM workspace WHERE workspace_id = :workspace_id)";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        WorkspaceNode topWorkspaceNode;
        try {
            topWorkspaceNode = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceNodeSql, namedParameters, new WorkspaceNodeMapper());
        } catch(EmptyResultDataAccessException ex) {
            String errorMessage = "Top node for workspace with ID " + workspaceID + " does not exist in the database";
            logger.error(errorMessage, ex);
            throw new WorkspaceNodeNotFoundException(errorMessage, workspaceID, -1, ex);
        }
        
        logger.info("Top node for workspace with ID " + workspaceID + " retrieved from the database");
        
        return topWorkspaceNode;
    }
    
    /**
     * @see WorkspaceDao#getNodesForWorkspace(int)
     */
    @Override
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
    @Override
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID) {
        
        logger.debug("Retrieving list containing child nodes of the node with ID: " + workspaceNodeID);
        
        String queryWorkspaceNodeListSql = "SELECT * FROM node WHERE workspace_node_id IN "
                + "(SELECT child_workspace_node_id FROM node_link WHERE parent_workspace_node_id = :parent_workspace_node_id)";
        SqlParameterSource namedParameters = new MapSqlParameterSource("parent_workspace_node_id", workspaceNodeID);
        
        Collection<WorkspaceNode> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }
    
    /**
     * @see WorkspaceDao#getParentWorkspaceNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getParentWorkspaceNodes(int workspaceNodeID) {
        
        logger.debug("Retrieving list containing parent nodes of the node with ID: " + workspaceNodeID);
        
        String queryWorkspaceNodeListSql = "SELECT * FROM node WHERE workspace_node_id IN "
                + "(SELECT parent_workspace_node_id FROM node_link WHERE child_workspace_node_id = :child_workspace_node_id)";
        SqlParameterSource namedParameters = new MapSqlParameterSource("child_workspace_node_id", workspaceNodeID);
        
        Collection<WorkspaceNode> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }
    
    /**
     * @see WorkspaceDao#getUnlinkedAndDeletedTopNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getUnlinkedAndDeletedTopNodes(int workspaceID) {
        
        logger.debug("Retrieving list containing unlinked and deleted top nodes of the workspace with ID: " + workspaceID);
        
        String queryUnlinkedAndDeletedTopNodeListSql = "SELECT * FROM node"
                + " WHERE workspace_node_id NOT IN (SELECT child_workspace_node_id from node_link)"
                + " AND workspace_node_id NOT IN (SELECT top_node_id FROM workspace WHERE workspace_id = :workspace_id);";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        Collection<WorkspaceNode> listToReturn =
                this.namedParameterJdbcTemplate.query(queryUnlinkedAndDeletedTopNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }

    /**
     * @see WorkspaceDao#getUnlinkedNodes(int)
     */
    @Override
    public List<WorkspaceNode> getUnlinkedNodes(int workspaceID) {
        
        logger.debug("Retrieving list containing unlinked nodes of the workspace with ID: " + workspaceID);
        
        String queryUnlinkedNodeListSql = "SELECT * FROM node WHERE workspace_node_id NOT IN (SELECT child_workspace_node_id from node_link)"
                + " AND workspace_id = :workspace_id AND status NOT LIKE :status"
                + " AND workspace_node_id NOT IN (SELECT top_node_id FROM workspace WHERE workspace_id = :workspace_id);";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("workspace_id", workspaceID)
                .addValue("status", WorkspaceNodeStatus.NODE_DELETED.toString());
        
        List<WorkspaceNode> listToReturn =
                this.namedParameterJdbcTemplate.query(queryUnlinkedNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }
    
    
    
    /**
     * @see WorkspaceDao#updateNodeWorkspaceURL(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
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
     * @see WorkspaceDao#updateNodeArchiveUri(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeArchiveUri(WorkspaceNode node) {
        
        logger.debug("Updating archive URI for node with ID: " + node.getWorkspaceNodeID() + "; setting archive URI to: " + node.getArchiveURI());
        
        String nodeArchiveUriStr = null;
        if(node.getArchiveURI() != null) {
            nodeArchiveUriStr = node.getArchiveURI().toString();
        }
        
        String updateSql = "UPDATE node SET archive_uri = :archive_uri"
                + " WHERE workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("archive_uri", nodeArchiveUriStr)
                .addValue("workspace_node_id", node.getWorkspaceNodeID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Archive URI of node " + node.getWorkspaceNodeID() + " updated to " + node.getArchiveURI());
    }
    
    /**
     * @see WorkspaceDao#updateNodeArchiveUrl(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeArchiveUrl(WorkspaceNode node) {
        logger.debug("Updating archive URL for node with ID: " + node.getWorkspaceNodeID() + "; setting archive URL to: " + node.getArchiveURL());
        
        String nodeArchiveUrlStr = null;
        if(node.getArchiveURL() != null) {
            nodeArchiveUrlStr = node.getArchiveURL().toString();
        }
        
        String updateSql = "UPDATE node SET archive_url = :archive_url"
                + " WHERE workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("archive_url", nodeArchiveUrlStr)
                .addValue("workspace_node_id", node.getWorkspaceNodeID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Archive URL of node " + node.getWorkspaceNodeID() + " updated to " + node.getArchiveURL());
    }

    /**
     * @see WorkspaceDao#addWorkspaceNodeLink(nl.mpi.lamus.workspace.model.WorkspaceNodeLink)
     */
    @Override
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
     * @see WorkspaceDao#deleteWorkspaceNodeLink(int, int, int)
     */
    @Override
    public void deleteWorkspaceNodeLink(int workspaceID, int parentNodeID, int childNodeID) {
        
        logger.debug("Deleting link between nodes " + parentNodeID + " and " + childNodeID + ", in workspace " + workspaceID);
        
        String deleteNodeLinkSql =
                "DELETE FROM node_link WHERE parent_workspace_node_id = :parent_node_id AND child_workspace_node_id = :child_node_id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("parent_node_id", parentNodeID)
                .addValue("child_node_id", childNodeID);
        this.namedParameterJdbcTemplate.update(deleteNodeLinkSql, namedParameters);
        
        logger.info("Node link between nodes " + parentNodeID + " and " + childNodeID + ", in workspace " + workspaceID + " was deleted");
    }

    /**
     * @see WorkspaceDao#cleanWorkspaceNodesAndLinks(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void cleanWorkspaceNodesAndLinks(Workspace workspace) {
        
        logger.debug("Cleaning nodes and links belonging to workspace " + workspace.getWorkspaceID());
        
        String deleteLinksSql =
                "DELETE FROM node_link WHERE parent_workspace_node_id in (SELECT workspace_node_id FROM node WHERE workspace_id = :workspace_id);";
        String deleteNodesSql =
                "DELETE FROM node WHERE workspace_id = :workspace_id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(deleteLinksSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodesSql, namedParameters);
        
        logger.info("Nodes and links belonging to workspace " + workspace.getWorkspaceID() + " were deleted");
    }

    
    /**
     * Inner class used to map rows from the workspace table into Workspace objects in queries
     */
    private static final class WorkspaceMapper implements RowMapper<Workspace> {
        
        @Override
        public Workspace mapRow(ResultSet rs, int rowNum) throws SQLException {
            String topNodeArchiveURIStr = rs.getString("top_node_archive_uri");
            URI topNodeArchiveURI = null;
            if(topNodeArchiveURIStr != null && !topNodeArchiveURIStr.isEmpty()) {
                try {
                    topNodeArchiveURI = new URI(topNodeArchiveURIStr);
                } catch (URISyntaxException ex) {
                    logger.warn("Top Node Archive URI is malformed; null used instead", ex);
                }
            }
            String topNodeArchiveURLStr = rs.getString("top_node_archive_url");
            URL topNodeArchiveURL = null;
            if(topNodeArchiveURLStr != null && !topNodeArchiveURLStr.isEmpty()) {
                try {
                    topNodeArchiveURL = new URL(topNodeArchiveURLStr);
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
    
    /**
     * Inner class used to map rows from the node table into WorkspaceNode objects in queries
     */
    private static final class WorkspaceNodeMapper implements RowMapper<WorkspaceNode> {

        @Override
        public WorkspaceNode mapRow(ResultSet rs, int rowNum) throws SQLException {

            String profileSchemaURIStr = rs.getString("profile_schema_uri");
            URI profileSchemaURI = null;
            if(profileSchemaURIStr != null && !profileSchemaURIStr.isEmpty()) {
                try {
                    profileSchemaURI = new URI(profileSchemaURIStr);
                } catch (URISyntaxException ex) {
                    logger.warn("Profile Schema URI has invalid syntax; null used instead", ex);
                }
            }
            String workspaceURLStr = rs.getString("workspace_url");
            URL workspaceURL = null;
            if(workspaceURLStr != null && !workspaceURLStr.isEmpty()) {
                try {
                    workspaceURL = new URL(workspaceURLStr);
                } catch (MalformedURLException ex) {
                    logger.warn("Workspace URL is malformed; null used instead", ex);
                }
            }
            String archiveURIStr = rs.getString("archive_uri");
            URI archiveURI = null;
            if(archiveURIStr != null && !archiveURIStr.isEmpty()) {
                try {
                    archiveURI = new URI(archiveURIStr);
                } catch (URISyntaxException ex) {
                    logger.warn("Archive URI is malformed; null used instead", ex);
                }
            }
            String archiveURLStr = rs.getString("archive_url");
            URL archiveURL = null;
            if(archiveURLStr != null) {
                try {
                    archiveURL = new URL(archiveURLStr);
                } catch (MalformedURLException ex) {
                    logger.warn("Archive URL is malformed; null used instead", ex);
                }
            }
            String originURLStr = rs.getString("origin_url");
            URL originURL = null;
            if(originURLStr != null) {
                try {
                    originURL = new URL(originURLStr);
                } catch (MalformedURLException ex) {
                    logger.warn("Origin URL is malformed; null used instead", ex);
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
    
    /**
     * Inner class used to map rows from the node_link table into WorkspaceNodeLink objects in queries
     */
    private static final class WorkspaceNodeLinkMapper implements RowMapper<WorkspaceNodeLink> {
        
        @Override
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