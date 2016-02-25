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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.model.*;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspace;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNode;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.implementation.LamusWorkspaceReplacedNodeUrlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * Data access layer that uses JDBC from the Spring framework
 * in order to access tha lamus database.
 * 
 * @see WorkspaceDao
 * 
 * @author guisil
 */
public class LamusJdbcWorkspaceDao implements WorkspaceDao {
    
    private static final Logger logger = LoggerFactory.getLogger(LamusJdbcWorkspaceDao.class);
    
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private SimpleJdbcInsert insertWorkspace;
    private SimpleJdbcInsert insertPreLock;
    private SimpleJdbcInsert insertWorkspaceNode;
    private SimpleJdbcInsert insertWorkspaceNodeLock;
    private SimpleJdbcInsert insertWorkspaceNodeLink;
    private SimpleJdbcInsert insertNodeReplacement;
    
    public LamusJdbcWorkspaceDao(DataSource dataSource) {
        this.setDataSource(dataSource);
    }
    
    /**
     * Setter for the DataSource that will be used by the several
     * JdbcTemplate and JdbcInsert objects.
     * 
     * @param datasource data source to be used for accessing the lamus database
     */
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
                    "crawler_id");
        
        this.insertPreLock = new SimpleJdbcInsert(datasource)
                .withTableName("pre_lock")
                .usingColumns("archive_uri");
        
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
                    "protected",
                    "format");
        
        this.insertWorkspaceNodeLock = new SimpleJdbcInsert(datasource)
                .withTableName("node_lock")
                .usingColumns(
                        "archive_uri",
                        "workspace_id");
        
        this.insertWorkspaceNodeLink = new SimpleJdbcInsert(datasource)
                .withTableName("node_link")
                .usingColumns(
                    "parent_workspace_node_id",
                    "child_workspace_node_id");
        
        this.insertNodeReplacement = new SimpleJdbcInsert(datasource)
                .withTableName("node_replacement")
                .usingColumns(
                    "old_node_id",
                    "new_node_id");
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
            statusStr = workspace.getStatus().name();
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
                .addValue("crawler_id", workspace.getCrawlerID());
        Number newID = this.insertWorkspace.executeAndReturnKey(parameters);
        workspace.setWorkspaceID(newID.intValue());
        
        logger.info("Workspace added to the database. Workspace ID: " + workspace.getWorkspaceID());
    }
    
    /**
     * @see WorkspaceDao#deleteWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void deleteWorkspace(Workspace workspace) {
        
        logger.debug("Deleting workspace with ID " + workspace.getWorkspaceID());
        
        String deleteNodeReplacementSql = "DELETE FROM node_replacement WHERE old_node_id IN (SELECT workspace_node_id FROM node WHERE workspace_id = :workspace_id);";
        String deleteNodeLinkSql = "DELETE FROM node_link WHERE parent_workspace_node_id IN (SELECT workspace_node_id FROM node WHERE workspace_id = :workspace_id);";
        String deleteNodeLockSql = "DELETE FROM node_lock WHERE workspace_id = :workspace_id";
        String deleteNodeSql = "DELETE FROM node WHERE workspace_ID = :workspace_id;";
        String deletePreLockSql = "DELETE FROM pre_lock WHERE archive_uri = :archive_uri";
        String deleteWorkspaceSql = "DELETE FROM workspace WHERE workspace_id = :workspace_id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("workspace_id", workspace.getWorkspaceID())
                .addValue("archive_uri", workspace.getTopNodeArchiveURI().toString());
        this.namedParameterJdbcTemplate.update(deleteNodeReplacementSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodeLinkSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodeLockSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodeSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deletePreLockSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteWorkspaceSql, namedParameters);
        
        logger.info("Workspace with ID " + workspace.getWorkspaceID() + " deleted");
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
        
        String statusStr = null;
        if(workspace.getStatus() != null) {
            statusStr = workspace.getStatus().name();
        }
        
        logger.debug("Updating workspace with ID: " + workspace.getWorkspaceID() + "; setting status to: " + statusStr
                + "; setting message to: \"" + workspace.getMessage() + "\"");
        
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
     * @see WorkspaceDao#updateWorkspaceCrawlerID(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceCrawlerID(Workspace workspace) {
        
        logger.debug("Updating workspace with ID: " + workspace.getWorkspaceID() + "; setting crawler ID to: " + workspace.getCrawlerID());
        
        String updateSql = "UPDATE workspace SET crawler_id = :crawler_id WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("crawler_id", workspace.getCrawlerID())
                .addValue("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Crawler ID of workspace " + workspace.getWorkspaceID() + " updated to \"" + workspace.getCrawlerID());
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
                + " AND status IN (:initialised_status, :sleeping_status)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("user_id", userID)
                .addValue("initialised_status", WorkspaceStatus.INITIALISED.name())
                .addValue("sleeping_status", WorkspaceStatus.SLEEPING.name());
        
        Collection<Workspace> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceListSql, namedParameters, new WorkspaceMapper());
        
        return listToReturn;
    }

    /**
     * @see WorkspaceDao#getWorkspacesInFinalStage()
     */
    @Override
    public Collection<Workspace> getWorkspacesInFinalStage() {
        
        logger.debug("Retrieving list of workspace in final stage (submitted but still waiting for result of crawler)");
        
        String queryWorkspaceListSql = "SELECT * FROM workspace WHERE status LIKE :pending_db_update_status";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("pending_db_update_status", WorkspaceStatus.UPDATING_ARCHIVE.name());
        
        Collection<Workspace> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceListSql, namedParameters, new WorkspaceMapper());
        
        return listToReturn;
    }

    /**
     * @see WorkspaceDao#getAllWorkspaces()
     */
    @Override
    public List<Workspace> getAllWorkspaces() {
        
        logger.debug("Retrieving list of all workspaces");
        
        String queryWorkspaceListSql = "SELECT * FROM workspace";
        SqlParameterSource namedParameters = new MapSqlParameterSource();
        
        List<Workspace> listToReturn = this.namedParameterJdbcTemplate.query(queryWorkspaceListSql, namedParameters, new WorkspaceMapper());
        
        return listToReturn;
    }

    /**
     * @see WorkspaceDao#preLockNode(java.net.URI)
     */
    @Override
    public void preLockNode(URI nodeURI) {
        
        logger.debug("Adding to the database a pre lock for node " + nodeURI);
        
        if(nodeURI == null) {
            throw new IllegalArgumentException("Archive URI to pre lock should not be null");
        }
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("archive_uri", nodeURI.toString());
        this.insertPreLock.execute(parameters);

        logger.info("Pre Lock added to the database. Archive URI: " + nodeURI);
    }

    /**
     * @see WorkspaceDao#removeNodePreLock(java.net.URI)
     */
    @Override
    public void removeNodePreLock(URI nodeURI) {

        logger.debug("Deleting pre lock on node " + nodeURI);
        
        if(nodeURI == null) {
            throw new IllegalArgumentException("Archive URI to unlock should not be null");
        }
        
        String deleteNodePreLockSql =
                "DELETE FROM pre_lock WHERE archive_uri = :archive_uri;";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("archive_uri", nodeURI.toString());
        this.namedParameterJdbcTemplate.update(deleteNodePreLockSql, namedParameters);
        
        logger.info("Pre Lock on node " + nodeURI + " was deleted");
    }

    /**
     * @see WorkspaceDao#isAnyOfNodesPreLocked(java.util.List)
     */
    @Override
    public boolean isAnyOfNodesPreLocked(List<String> nodeURIs) {

        logger.debug("Checking if any of the nodes in the collection is locked");
        
        if(nodeURIs == null || nodeURIs.isEmpty()) {
            return false;
        }
        
        String queryPreLockSql = "SELECT * FROM pre_lock WHERE archive_uri in (:uris)";
        SqlParameterSource namedParameters;
        
        int from = 0;
        int to = nodeURIs.size() <= 100 ? nodeURIs.size() : 100;
        
        while(from < nodeURIs.size()) {
            
            List<String> someURIs = nodeURIs.subList(from, to);
            namedParameters = new MapSqlParameterSource().addValue("uris", someURIs);
            List<Map<String, Object>> list = this.namedParameterJdbcTemplate.queryForList(queryPreLockSql, namedParameters);

            if(!list.isEmpty()) {
                return true;
            }
            from = to;
            to = nodeURIs.size() <= from + 100 ? nodeURIs.size() : from + 100;
        }

        logger.info("None of the nodes in the collection is pre locked (there is no workspace being created in any of the nodes).");
        return false;
    }
    
    /**
     * @see WorkspaceDao#isNodeLocked(java.net.URI)
     */
    @Override
    public boolean isNodeLocked(URI archiveNodeURI) {
        
        logger.debug("Checking if node " + archiveNodeURI + " is locked");
        
        if(archiveNodeURI == null) {
            throw new IllegalArgumentException("URI to check should not be null");
        }
        
        String queryNodeSql = "SELECT * FROM node_lock WHERE (archive_uri = :uri)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("uri", archiveNodeURI.toString());
        
        SqlRowSet result = this.namedParameterJdbcTemplate.queryForRowSet(queryNodeSql, namedParameters);
        
        if(!result.first()) {
            logger.info("Node " + archiveNodeURI.toString() + " is not locked (there is no existing workspace that contains this node).");
            return false;
        }

        logger.info("Node " + archiveNodeURI + " is locked (there is already a workspace that contains this node).");
        return true;
    }
    
    /**
     * @see WorkspaceDao#lockNode(java.net.URI, int)
     */
    @Override
    public void lockNode(URI uriToLock, int workspaceID) {
        
        logger.debug("Adding to the database a lock for node " + uriToLock
                + " in workspace " + workspaceID);
        
        if(uriToLock == null) {
            throw new IllegalArgumentException("Archive URI to lock should not be null");
        }
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("archive_uri", uriToLock.toString())
                .addValue("workspace_id", workspaceID);
        this.insertWorkspaceNodeLock.execute(parameters);

        logger.info("Lock added to the database. Archive URI: " + uriToLock
                + "; Workspace ID: " + workspaceID);
    }

    /**
     * @see WorkspaceDao#unlockNode(java.net.URI)
     */
    @Override
    public void unlockNode(URI uriToUnlock) {
        
        logger.debug("Deleting lock on node " + uriToUnlock);
        
        if(uriToUnlock == null) {
            throw new IllegalArgumentException("Archive URI to unlock should not be null");
        }
        
        String deleteNodeLockSql =
                "DELETE FROM node_lock WHERE archive_uri = :archive_uri;";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("archive_uri", uriToUnlock.toString());
        this.namedParameterJdbcTemplate.update(deleteNodeLockSql, namedParameters);
        
        logger.info("Lock on node " + uriToUnlock + " was deleted");
    }

    /**
     * @see WorkspaceDao#unlockAllNodesOfWorkspace(int)
     */
    @Override
    public void unlockAllNodesOfWorkspace(int workspaceID) {

        logger.debug("Unlocking nodes belonging to workspace " + workspaceID);
        
        String deleteLocksSql =
                "DELETE FROM node_lock WHERE workspace_id = :workspace_id;";

        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        this.namedParameterJdbcTemplate.update(deleteLocksSql, namedParameters);
        
        logger.info("Node locks belonging to workspace " + workspaceID + " were deleted");
    }

    /**
     * @see WorkspaceDao#getWorkspaceNodeByArchiveURI(java.net.URI)
     */
    @Override
    public Collection<WorkspaceNode> getWorkspaceNodeByArchiveURI(URI archiveNodeURI) {
        
        logger.debug("Retrieving node(s) with archive URI " + archiveNodeURI);
        
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
            typeStr = node.getType().name();
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
        if(node.getOriginURI() != null) {
            originURLStr = node.getOriginURI().toString();
        }
        String statusStr = null;
        if(node.getStatus() != null) {
            statusStr = node.getStatus().name();
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
                .addValue("protected", node.isProtected())
                .addValue("format", node.getFormat());
        Number newID = this.insertWorkspaceNode.executeAndReturnKey(parameters);
        node.setWorkspaceNodeID(newID.intValue());
        
        logger.info("Node added to the database. Node ID: " + node.getWorkspaceNodeID());
    }
    
    /**
     * @see WorkspaceDao#setWorkspaceNodeAsDeleted(int, int, boolean)
     */
    @Override
    public void setWorkspaceNodeAsDeleted(int workspaceID, int nodeID, boolean isExternal) {
        
        logger.debug("Setting node " + nodeID + " in workspace " + workspaceID + " as deleted");
        
        String updateSql = "UPDATE node SET status = :status"
                + " WHERE workspace_node_id = :workspace_node_id";
        
        WorkspaceNodeStatus deletedStatus = isExternal ?
                WorkspaceNodeStatus.EXTERNAL_DELETED :
                WorkspaceNodeStatus.DELETED;
        
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("status", deletedStatus.name())
                .addValue("workspace_node_id", nodeID);
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Node " + nodeID + " in workspace " + workspaceID + " was set as deleted");
    }

    /**
     * @see WorkspaceDao#deleteWorkspaceNode(int, int)
     */
    @Override
    public void deleteWorkspaceNode(int workspaceID, int nodeID) {

        logger.debug("Deleting node " + nodeID + " in workspace " + workspaceID);
        
        String deleteReplacementsSql = "DELETE FROM node_replacement" +
                " WHERE old_node_id = :node_id OR new_node_id = :node_id";
        String deleteLinksSql = "DELETE FROM node_link" +
                " WHERE parent_workspace_node_id = :node_id OR child_workspace_node_id = :node_id";
        String deleteNodeSql = "DELETE from node" +
                " WHERE workspace_node_id = :node_id";
        
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("node_id", nodeID);
        this.namedParameterJdbcTemplate.update(deleteReplacementsSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteLinksSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodeSql, namedParameters);
        
        logger.info("Node " + nodeID + " in workspace " + workspaceID + " has been deleted");
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
     * @see WorkspaceDao#getWorkspaceTopNodeID(int)
     */
    @Override
    public int getWorkspaceTopNodeID(int workspaceID) {
        
        logger.debug("Retrieving top node ID of workspace with ID: " + workspaceID);
        
        String queryWorkspaceNodeSql = "SELECT top_node_id FROM workspace WHERE workspace_id = :workspace_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        int topWorkspaceNodeID = -1;
        try {
            topWorkspaceNodeID = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceNodeSql, namedParameters, Integer.class);
        } catch(EmptyResultDataAccessException ex) {
            String errorMessage = "Top node for workspace with ID " + workspaceID + " does not exist in the database";
            logger.error(errorMessage, ex);
        }
        
        logger.info("Top node for workspace with ID " + workspaceID + " retrieved from the database");
        
        return topWorkspaceNodeID;
    }

    /**
     * @see WorkspaceDao#isTopNodeOfWorkspace(int, int)
     */
    @Override
    public boolean isTopNodeOfWorkspace(int workspaceID, int workspaceNodeID) {
        
        logger.debug("Checking if given node (" + workspaceNodeID + ") is top node of workspace " + workspaceID);
        
        if(workspaceNodeID == getWorkspaceTopNodeID(workspaceID)) {
            return true;
        }
        
        return false;
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
     * @see WorkspaceDao#getMetadataNodesInTreeForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceNode> getMetadataNodesInTreeForWorkspace(int workspaceID) {
        
        logger.debug("Retrieving list containing nodes which are part of the tree of the workspace with ID " + workspaceID);
        
        Collection<WorkspaceNode> listToReturn = new ArrayList<>();
        WorkspaceNode topNode;
        try {
            topNode = getWorkspaceTopNode(workspaceID);
            listToReturn.add(topNode);
        } catch(WorkspaceNodeNotFoundException ex) {
            logger.warn("Top node not found for workspace " + workspaceID);
            return listToReturn;
        }
        Collection<WorkspaceNode> topNodeMetadataDescendants = getDescendantWorkspaceNodesByType(topNode.getWorkspaceNodeID(), WorkspaceNodeType.METADATA);
        listToReturn.addAll(topNodeMetadataDescendants);
        
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
     * @see WorkspaceDao#getDescendantWorkspaceNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getDescendantWorkspaceNodes(int workspaceNodeID) {
        
        logger.debug("Retrieving list containing descendant nodes of the node with ID: " + workspaceNodeID);
        
        return getDescendantWorkspaceNodesByType(workspaceNodeID, WorkspaceNodeType.UNKNOWN);
    }

    /**
     * @see WorkspaceDao#getDescendantWorkspaceNodesByType(int, nl.mpi.lamus.workspace.model.WorkspaceNodeType)
     */
    @Override
    public Collection<WorkspaceNode> getDescendantWorkspaceNodesByType(int workspaceNodeID, WorkspaceNodeType nodeType) {
        
        logger.debug("Retrieving list containing descendant nodes (filtered by type " + nodeType.name() + ") of the node with ID: " + workspaceNodeID);
        
        Collection<WorkspaceNode> allDescendantsOfType = new ArrayList<>();
        
        Collection<WorkspaceNode> children = getChildWorkspaceNodes(workspaceNodeID);
        
        for(WorkspaceNode child : children) {
            if(WorkspaceNodeType.UNKNOWN.equals(nodeType) || child.getType().equals(nodeType)) {
                allDescendantsOfType.add(child);
            }
            allDescendantsOfType.addAll(getDescendantWorkspaceNodesByType(child.getWorkspaceNodeID(), nodeType));
        }
        
        return allDescendantsOfType;
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
                + " AND workspace_node_id NOT IN (SELECT top_node_id FROM workspace WHERE workspace_id = :workspace_id)"
                + " AND workspace_id = :workspace_id;";
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
                + " AND workspace_id = :workspace_id AND status NOT LIKE :status_deleted AND status NOT LIKE :status_external_deleted AND status NOT LIKE :status_replaced"
                + " AND workspace_node_id NOT IN (SELECT top_node_id FROM workspace WHERE workspace_id = :workspace_id);";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("workspace_id", workspaceID)
                .addValue("status_deleted", WorkspaceNodeStatus.DELETED.name())
                .addValue("status_external_deleted", WorkspaceNodeStatus.EXTERNAL_DELETED.name())
                .addValue("status_replaced", WorkspaceNodeStatus.REPLACED.name());
        
        List<WorkspaceNode> listToReturn =
                this.namedParameterJdbcTemplate.query(queryUnlinkedNodeListSql, namedParameters, new WorkspaceNodeMapper());
        
        return listToReturn;
    }

    /**
     * @see WorkspaceDao#getUnlinkedNodesAndDescendants(int)
     */
    @Override
    public Collection<WorkspaceNode> getUnlinkedNodesAndDescendants(int workspaceID) {
        
        logger.debug("Retrieving list containing unlinked nodes and descendants for workspace with ID: " + workspaceID);
        
        Collection<WorkspaceNode> unlinkedNodesAndDescendants = new ArrayList<>();
        
        List<WorkspaceNode> unlinkedNodes = getUnlinkedNodes(workspaceID);
        
        for(WorkspaceNode node : unlinkedNodes) {
            unlinkedNodesAndDescendants.add(node);
            unlinkedNodesAndDescendants.addAll(getDescendantWorkspaceNodes(node.getWorkspaceNodeID()));
        }
        
        return unlinkedNodesAndDescendants;
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
     * @see WorkspaceDao#updateNodeType(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeType(WorkspaceNode node) {
        logger.debug("Updating type for node with ID: " + node.getWorkspaceNodeID() + "; setting type to: " + node.getType());
        
        String updateSql = "UPDATE node SET type = :type"
                + " WHERE workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("type", node.getType().name())
                .addValue("workspace_node_id", node.getWorkspaceNodeID());
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Type of node " + node.getWorkspaceNodeID() + " updated to " + node.getType());
    }

    /**
     * @see WorkspaceDao#addWorkspaceNodeLink(nl.mpi.lamus.workspace.model.WorkspaceNodeLink)
     */
    @Override
    public void addWorkspaceNodeLink(WorkspaceNodeLink nodeLink) {
        
        logger.debug("Adding to the database a link between node with ID: " + nodeLink.getParentWorkspaceNodeID()
                + " and node with ID: " + nodeLink.getChildWorkspaceNodeID());
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("parent_workspace_node_id", nodeLink.getParentWorkspaceNodeID())
                .addValue("child_workspace_node_id", nodeLink.getChildWorkspaceNodeID());
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
        
        String deleteReplacementsSql =
                "DELETE FROM node_replacement WHERE old_node_id in (SELECT workspace_node_id FROM node WHERE workspace_id = :workspace_id);";
        String deleteLinksSql =
                "DELETE FROM node_link WHERE parent_workspace_node_id in (SELECT workspace_node_id FROM node WHERE workspace_id = :workspace_id);";
        String deleteLocksSql =
                "DELETE FROM node_lock WHERE workspace_id = :workspace_id;";
        String deleteNodesSql =
                "DELETE FROM node WHERE workspace_id = :workspace_id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspace.getWorkspaceID());
        this.namedParameterJdbcTemplate.update(deleteReplacementsSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteLinksSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteLocksSql, namedParameters);
        this.namedParameterJdbcTemplate.update(deleteNodesSql, namedParameters);
        
        logger.info("Nodes and links belonging to workspace " + workspace.getWorkspaceID() + " were deleted");
    }

    /**
     * @see WorkspaceDao#getOlderVersionOfNode(int, int)
     */
    @Override
    public WorkspaceNode getOlderVersionOfNode(int workspaceID, int workspaceNodeID)
            throws WorkspaceNodeNotFoundException {
        
        logger.debug("Retrieving older version of node " + workspaceNodeID);
        
        String queryWorkspaceNodeSql = "SELECT * FROM node WHERE workspace_node_id IN (SELECT old_node_id FROM node_replacement WHERE new_node_id = :node_id)";
        SqlParameterSource namedParameters = new MapSqlParameterSource("node_id", workspaceNodeID);
        
        WorkspaceNode olderVersion;
        try {
            olderVersion = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceNodeSql, namedParameters, new WorkspaceNodeMapper());
        } catch(EmptyResultDataAccessException ex) {
            String errorMessage = "Older version of node with ID " + workspaceNodeID + " not found in the database";
            logger.error(errorMessage, ex);
            throw new WorkspaceNodeNotFoundException(errorMessage, workspaceID, -1, ex);
        }
        
        logger.info("Older version of node with ID " + workspaceNodeID + " retrieved from the database");
        
        return olderVersion;
    }
    
    /**
     * @see WorkspaceDao#getNewerVersionOfNode(int, int)
     */
    @Override
    public WorkspaceNode getNewerVersionOfNode(int workspaceID, int workspaceNodeID)
            throws WorkspaceNodeNotFoundException {
        
        logger.debug("Retrieving newer version of node " + workspaceNodeID);
        
        String queryWorkspaceNodeSql = "SELECT * FROM node WHERE workspace_node_id IN (SELECT new_node_id FROM node_replacement WHERE old_node_id = :node_id)";
        SqlParameterSource namedParameters = new MapSqlParameterSource("node_id", workspaceNodeID);
        
        WorkspaceNode newerVersion;
        try {
            newerVersion = this.namedParameterJdbcTemplate.queryForObject(queryWorkspaceNodeSql, namedParameters, new WorkspaceNodeMapper());
        } catch(EmptyResultDataAccessException ex) {
            String errorMessage = "Newer version of node with ID " + workspaceNodeID + " not found in the database";
            logger.error(errorMessage, ex);
            throw new WorkspaceNodeNotFoundException(errorMessage, workspaceID, -1, ex);
        }
        
        logger.info("Newer version of node with ID " + workspaceNodeID + " retrieved from the database");
        
        return newerVersion;
    }
    
    /**
     * @see WorkspaceDao#replaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void replaceNode(WorkspaceNode oldNode, WorkspaceNode newNode) {
        
        logger.debug("Replacing node " + oldNode.getWorkspaceNodeID() + " by node " + newNode.getWorkspaceNodeID());
        
        if(WorkspaceNodeStatus.CREATED.equals(oldNode.getStatus()) ||
                WorkspaceNodeStatus.UPLOADED.equals(oldNode.getStatus())) {
            
            setWorkspaceNodeAsDeleted(oldNode.getWorkspaceID(), oldNode.getWorkspaceNodeID(), oldNode.isExternal());
        } else {
            setWorkspaceNodeAsReplaced(oldNode.getWorkspaceID(), oldNode.getWorkspaceNodeID());
            createNodeVersion(oldNode, newNode);
        }
    }

    /**
     * @see WorkspaceDao#getAllNodeReplacements()
     */
    @Override
    public Collection<WorkspaceNodeReplacement> getAllNodeReplacements() {
        
        logger.debug("Retrieving collection containing all node replacements present in the database");
        
        String queryNodeReplacementsSql =
                "SELECT coalesce(a.archive_uri, a.archive_url, a.origin_url) old_node_uri,"
                + " coalesce(b.archive_uri, b.archive_url, b.origin_url) new_node_uri FROM"
                + " (SELECT workspace_node_id, archive_uri, archive_url, origin_url from node) a,"
                + " (SELECT workspace_node_id, archive_uri, archive_url, origin_url from node) b,"
                + " (SELECT old_node_id, new_node_id from node_replacement) c"
                + " WHERE c.old_node_id = a.workspace_node_id and c.new_node_id = b.workspace_node_id;";
        
        SqlParameterSource namedParameters = new MapSqlParameterSource();
        
        Collection<WorkspaceNodeReplacement> collectionToReturn =
                this.namedParameterJdbcTemplate.query(queryNodeReplacementsSql, namedParameters, new WorkspaceNodeReplacementMapper());
        
        return collectionToReturn;
    }

    /**
     * @see WorkspaceDao#getNodeReplacementsForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceNodeReplacement> getNodeReplacementsForWorkspace(int workspaceID) {
        
        logger.debug("Retrieving collection containing node replacements belonging to workspace " + workspaceID);
        
        String queryNodeReplacementsSql =
                "SELECT coalesce(a.archive_uri, a.archive_url, a.origin_url) old_node_uri,"
                + " coalesce(b.archive_uri, b.archive_url, b.origin_url) new_node_uri FROM"
                + " (SELECT workspace_id, workspace_node_id, archive_uri, archive_url, origin_url from node) a,"
                + " (SELECT workspace_id, workspace_node_id, archive_uri, archive_url, origin_url from node) b,"
                + " (SELECT old_node_id, new_node_id from node_replacement) c"
                + " WHERE c.old_node_id = a.workspace_node_id and c.new_node_id = b.workspace_node_id"
                + " AND a.workspace_id = b.workspace_id AND a.workspace_id = :workspace_id;";
        
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        
        Collection<WorkspaceNodeReplacement> collectionToReturn =
                this.namedParameterJdbcTemplate.query(queryNodeReplacementsSql, namedParameters, new WorkspaceNodeReplacementMapper());
        
        return collectionToReturn;
    }

    /**
     * @see WorkspaceDao#getReplacedAndDeletedNodeUrlsToUpdateForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceReplacedNodeUrlUpdate> getReplacedAndDeletedNodeUrlsToUpdateForWorkspace(int workspaceID) {
        
        logger.debug("Retrieving collection containing replaced and deleted node URL updates belonging to workspace " + workspaceID);
        
        String queryReplacedNodeUrlUpdatesSql =
                "SELECT coalesce(a.archive_uri, a.archive_url, a.origin_url) node_uri,"
                + " a.archive_url updated_uri FROM"
                + " (SELECT workspace_id, workspace_node_id, archive_uri, archive_url, origin_url from node) a,"
                + " (SELECT old_node_id, new_node_id from node_replacement) c"
                + " WHERE c.old_node_id = a.workspace_node_id"
                + " AND a.workspace_id = :workspace_id;";
        
        SqlParameterSource namedParameters = new MapSqlParameterSource("workspace_id", workspaceID);
        Collection<WorkspaceReplacedNodeUrlUpdate> replacedNodes =
                this.namedParameterJdbcTemplate.query(queryReplacedNodeUrlUpdatesSql, namedParameters, new WorkspaceReplacedNodeUrlUpdateMapper());
        
        String queryDeletedNodeUrlUpdatedSql =
                "SELECT coalesce(archive_uri, archive_url, origin_url) node_uri,"
                + " archive_url updated_uri FROM node"
                + " WHERE status LIKE :status_deleted"
                + " AND (archive_uri IS NOT NULL OR archive_url IS NOT NULL)"
                + " AND workspace_id = :workspace_id;";
        
        namedParameters = new MapSqlParameterSource()
                .addValue("workspace_id", workspaceID)
                .addValue("status_deleted", WorkspaceNodeStatus.DELETED.name());
        
        Collection<WorkspaceReplacedNodeUrlUpdate> deletedNodes =
                this.namedParameterJdbcTemplate.query(queryDeletedNodeUrlUpdatedSql, namedParameters, new WorkspaceReplacedNodeUrlUpdateMapper());
        
        Collection<WorkspaceReplacedNodeUrlUpdate> collectionToReturn = new ArrayList<>();
        collectionToReturn.addAll(replacedNodes);
        collectionToReturn.addAll(deletedNodes);
        
        return collectionToReturn;
    }
    
    
    private void setWorkspaceNodeAsReplaced(int workspaceID, int nodeID) {
        
        logger.debug("Setting node " + nodeID + " in workspace " + workspaceID + " as replaced");
        
        String updateSql = "UPDATE node SET status = :status"
                + " WHERE workspace_node_id = :workspace_node_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("status", WorkspaceNodeStatus.REPLACED.name())
                .addValue("workspace_node_id", nodeID);
        this.namedParameterJdbcTemplate.update(updateSql, namedParameters);
        
        logger.info("Node " + nodeID + " in workspace " + workspaceID + " was set as replaced");
    }
    
    private void createNodeVersion(WorkspaceNode oldNode, WorkspaceNode newNode) {
        
        logger.debug("Adding to the database a replacement link between old node with ID: " + oldNode.getWorkspaceNodeID()
                + " and new node with ID: " + newNode.getWorkspaceNodeID());
        
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("old_node_id", oldNode.getWorkspaceNodeID())
                .addValue("new_node_id", newNode.getWorkspaceNodeID());
        this.insertNodeReplacement.execute(parameters);

        logger.info("Replacement link added to the database. Old node ID: " + oldNode.getWorkspaceNodeID()
                + "; New node ID: " + newNode.getWorkspaceNodeID());
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
                    rs.getString("crawler_id"));
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
            String originURIStr = rs.getString("origin_url");
            URI originURI = null;
            if(originURIStr != null) {
                originURI = URI.create(originURIStr);
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
    
    /**
     * Inner class used to map rows from the node_link table into WorkspaceNodeLink objects in queries
     */
    private static final class WorkspaceNodeLinkMapper implements RowMapper<WorkspaceNodeLink> {
        
        @Override
        public WorkspaceNodeLink mapRow(ResultSet rs, int rowNum) throws SQLException {
            
            WorkspaceNodeLink workspaceNodeLink = new LamusWorkspaceNodeLink(
                    rs.getInt("parent_workspace_node_id"),
                    rs.getInt("child_workspace_node_id"));
            return workspaceNodeLink;
        }
    }
    
    /**
     * Inner class used to map rows from the node table into WorkspaceNodeReplacement objects in queries
     */
    private static final class WorkspaceNodeReplacementMapper implements RowMapper<WorkspaceNodeReplacement> {

        @Override
        public WorkspaceNodeReplacement mapRow(ResultSet rs, int i) throws SQLException {
            
            String oldNodeURIStr = rs.getString("old_node_uri");
            URI oldNodeURI = null;
            if(oldNodeURIStr != null && !oldNodeURIStr.isEmpty()) {
                try {
                    oldNodeURI = new URI(oldNodeURIStr);
                } catch (URISyntaxException ex) {
                    logger.warn("Archive URI is malformed; null used instead", ex);
                }
            }
            
            String newNodeURIStr = rs.getString("new_node_uri");
            URI newNodeURI = null;
            if(newNodeURIStr != null && !newNodeURIStr.isEmpty()) {
                try {
                    newNodeURI = new URI(newNodeURIStr);
                } catch (URISyntaxException ex) {
                    logger.warn("Archive URI is malformed; null used instead", ex);
                }
            }
            
            WorkspaceNodeReplacement nodeReplacement = new LamusWorkspaceNodeReplacement(
                    oldNodeURI,
                    newNodeURI);
            return nodeReplacement;
        }
    }
    
    /**
     * Inner class used to map rows from the node table into WorkspaceReplacedNodeUrlUpdate objects in queries
     */
    private static final class WorkspaceReplacedNodeUrlUpdateMapper implements RowMapper<WorkspaceReplacedNodeUrlUpdate> {

        @Override
        public WorkspaceReplacedNodeUrlUpdate mapRow(ResultSet rs, int i) throws SQLException {
            
            String nodeUriStr = rs.getString("node_uri");
            URI nodeUri = null;
            if(nodeUriStr != null && !nodeUriStr.isEmpty()) {
                try {
                    nodeUri = new URI(nodeUriStr);
                } catch(URISyntaxException ex) {
                    logger.warn("Archive URI is malformed; null used instead", ex);
                }
            }
            
            String updatedUriStr = rs.getString("updated_uri");
            URI updatedUri = null;
            if(updatedUriStr != null && !updatedUriStr.isEmpty()) {
                try {
                    updatedUri = new URI(updatedUriStr);
                } catch(URISyntaxException ex) {
                    logger.warn("Updated URL is malformed (not a URI); null used instead", ex);
                }
            }
            
            WorkspaceReplacedNodeUrlUpdate replacedNodeUrlUpdate = new LamusWorkspaceReplacedNodeUrlUpdate(
                    nodeUri,
                    updatedUri);
            return replacedNodeUrlUpdate;
        }
    }
}