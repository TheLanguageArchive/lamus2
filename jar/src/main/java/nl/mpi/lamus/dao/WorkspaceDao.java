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
package nl.mpi.lamus.dao;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;

/**
 * Data access layer for the workspace data.
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceDao {
    
    /**
     * Inserts a workspace into the database
     * 
     * @param workspace Workspace object to insert into the database
     */
    public void addWorkspace(Workspace workspace);
    
    /**
     * Deletes a workspace from the database
     * 
     * @param workspaceID ID of the workspace to delete
     */
    public void deleteWorkspace(int workspaceID);
    
    /**
     * Updates the top node of the given workspace.
     * 
     * @param workspace Workspace object already with the updated top node
     */
    public void updateWorkspaceTopNode(Workspace workspace);
    
    /**
     * Updates the session dates of the given workspace.
     * 
     * @param workspace workspace object already with the updated session dates
     */
    public void updateWorkspaceSessionDates(Workspace workspace);
    
    /**
     * Updates the storage space of the given workspace
     * @param workspace workspace object already with the updated storage space
     */
    public void updateWorkspaceStorageSpace(Workspace workspace);

    /**
     * Updates the status and message of the given workspace.
     * 
     * @param workspace workspace object already with the updated status and message
     */
    public void updateWorkspaceStatusMessage(Workspace workspace);
    
    /**
     * Updates the session end date and the end date of the given workspace
     * in order to finalise it.
     * 
     * @param workspace workspace object already with the updated end dates
     */
    public void updateWorkspaceEndDates(Workspace workspace);
    
    /**
     * Retrieves the workspace with the given ID.
     * 
     * @param workspaceID ID of the workspace to retrieve
     * @return Workspace object with the given ID
     */
    public Workspace getWorkspace(int workspaceID)
            throws WorkspaceNotFoundException;

    /**
     * Retrieves a collection of workspaces created by the given user.
     * 
     * @param userID ID of the user to use in the query
     * @return Collectio of workspaces created by the given user
     */
    public Collection<Workspace> getWorkspacesForUser(String userID);
    
    /**
     * Checks if the archive node with the given ID is locked
     * (is part of any existing workspace)
     * @param archiveNodeURI URI of the archive node to be checked
     * @return true if the given archive node is locked
     */
    public boolean isNodeLocked(URI archiveNodeURI);
    
    /**
     * Gets a list of workspace nodes with the given URI.
     * There should be only one, but in case of failed workspaces
     * that weren't deleted it could be possible to have more.
     * @param archiveNodeURI URI of the archive node
     * @return List of WorkspaceNode objects with the given archive URI
     */
    public Collection<WorkspaceNode> getWorkspaceNodeByArchiveURI(URI archiveNodeURI);
    
    /**
     * Inserts a node into the database.
     * 
     * @param node WorkspaceNode object to insert into the database
     */
    public void addWorkspaceNode(WorkspaceNode node);
    
    /**
     * Sets a node as deleted in the database
     * @param workspaceID ID of the workspace
     * @param nodeID ID of the node
     */
    public void setWorkspaceNodeAsDeleted(int workspaceID, int nodeID);
    
    /**
     * Retrieves the node with the given ID.
     * 
     * @param workspaceNodeID ID of the node to retrieve
     * @return WorkspaceNode object with the given ID
     */
    public WorkspaceNode getWorkspaceNode(int workspaceNodeID)
            throws WorkspaceNodeNotFoundException;
    
    /**
     * Retrieves the top node of the given workspace.
     * 
     * @param workspaceID ID of the workspace
     * @return WorkspaceNode object corresponding to the top node of the workspace
     */
    public WorkspaceNode getWorkspaceTopNode(int workspaceID)
            throws WorkspaceNodeNotFoundException;
    
    /**
     * Retrieves a collection containing all the nodes
     * of the workspace with the given ID.
     * 
     * @param workspaceID ID of the workspace
     * @return Collection of nodes associated with the given workspace
     */
    public Collection<WorkspaceNode> getNodesForWorkspace(int workspaceID);
    
    /**
     * Retrieves a collection containing the child nodes of the node with the
     * given ID.
     * 
     * @param workspaceNodeID ID of the parent node
     * @return Collection of nodes that have the given node as parent
     */
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID);
    
    /**
     * Retrieves a collection containing the parent nodes of the node with the
     * given ID.
     * 
     * @param workspaceNodeID ID of the child node
     * @return Collection of nodes that have the given node as child
     */
    public Collection<WorkspaceNode> getParentWorkspaceNodes(int workspaceNodeID);
    
    /**
     * Retrieves a collection containing the nodes that were unlinked or deleted
     * and have no parent (except the top node of the workspace).
     * @param workspaceNodeID ID of the workspace
     * @return Collection of unlinked or deleted nodes that have no parent
     */
    public Collection<WorkspaceNode> getUnlinkedAndDeletedTopNodes(int workspaceNodeID);
    
    /**
     * Retrieves a collection containing the nodes that have no parent
     * (unlinked nodes).
     * @param workspaceID ID of the workspace
     * @return List of unlinked nodes in the workspace
     */
    public List<WorkspaceNode> getUnlinkedNodes(int workspaceID);
    
    /**
     * Updates the Workspace URL of the given node.
     * 
     * @param node WorkspaceNode object to be updated
     */
    public void updateNodeWorkspaceURL(WorkspaceNode node);
    
    /**
     * Updates the Archive URI of the given node.
     * 
     * @param node WorkspaceNode object to be updated
     */
    public void updateNodeArchiveUri(WorkspaceNode node);
    
    /**
     * Updates the Archive URL of the given node.
     * 
     * @param node WorkspaceNode object to be updated
     */
    public void updateNodeArchiveUrl(WorkspaceNode node);
    
    /**
     * Inserts a link between two nodes (parent and child) into the database.
     * @param link WorkspaceNodeLink object to insert into the database
     */
    public void addWorkspaceNodeLink(WorkspaceNodeLink nodeLink);
    
    /**
     * Deletes a link between two nodes (parent and child) from the database.
     * @param workspaceID ID of the workspace
     * @param parentNodeID ID of the parent node
     * @param childNodeID ID of the child node
     */
    public void deleteWorkspaceNodeLink(int workspaceID, int parentNodeID, int childNodeID);
    
    /**
     * Cleans the information regarding nodes and links of a workspace.
     * @param workspace 
     */
    public void cleanWorkspaceNodesAndLinks(Workspace workspace);
}
