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
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceReplacedNodeUrlUpdate;

/**
 * Data access layer for the workspace data.
 * 
 * @author guisil
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
     * @param workspace workspace to delete
     */
    public void deleteWorkspace(Workspace workspace);
    
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
     * Updates the ID of the crawler being executed for the workspace.
     * 
     * @param workspace workspace object already with the updated crawler ID
     */
    public void updateWorkspaceCrawlerID(Workspace workspace);
    
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
     * @return Collection of workspaces created by the given user
     */
    public Collection<Workspace> getWorkspacesForUser(String userID);
    
    /**
     * Retrieves a collection of workspaces which have been
     * submitted, but are yet to be finalised
     * (i.e. are currently waiting for the result of the crawler).
     * @return Collection of submitted but unfinished workspaces.
     */
    public Collection<Workspace> getWorkspacesInFinalStage();
    
    /**
     * Retrieves a list containing all workspaces in the database.
     * @return List with all the workspaces
     */
    public List<Workspace> getAllWorkspaces();
    
    /**
     * Pre-locks a node.
     * @param nodeURI URI of the node to pre-lock
     */
    public void preLockNode(URI nodeURI);
    
    /**
     * Removes the pre-lock from a node
     * @param nodeURI URI of the node to remove the pre-lock from
     */
    public void removeNodePreLock(URI nodeURI);
    
    /**
     * Checks if any of the given nodes is pre-locked
     * @param nodeURIs list of URIs (as strings) of the nodes to check
     * @return true if any of the nodes in the list is pre-locked
     */
    public boolean isAnyOfNodesPreLocked(List<String> nodeURIs);
    
    /**
     * Checks if the archive node with the given ID is locked
     * (is part of any existing workspace).
     * Protected nodes don't count as locked.
     * @param archiveNodeURI URI of the archive node to be checked
     * @return true if the given archive node is locked
     */
    public boolean isNodeLocked(URI archiveNodeURI);
    
    /**
     * Adds a lock on the given node.
     * @param uriToLock Archive URI of the node to lock
     * @param workspaceID ID of the workspace where the node is being locked
     */
    public void lockNode(URI uriToLock, int workspaceID);
    
    /**
     * Removes a lock on the given node.
     * @param uriToUnlock Archive URI of the node to be unlocked
     */
    public void unlockNode(URI uriToUnlock);
    
    /**
     * Removes all locks on nodes of the given workspace.
     * @param workspaceID ID of the workspace from which to unlock the nodes
     */
    public void unlockAllNodesOfWorkspace(int workspaceID);
    
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
     * @param isExternal true if node is external
     */
    public void setWorkspaceNodeAsDeleted(int workspaceID, int nodeID, boolean isExternal);
    
    /**
     * Completely deletes a node from the workspace.
     * @param workspaceID ID of the workspace
     * @param nodeID ID of the node
     */
    public void deleteWorkspaceNode(int workspaceID, int nodeID);
    
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
     * Retrieves the ID of the top node of the given workspace.
     * 
     * @param workspaceID ID of the workspace
     * @return ID of the top node of the workspace
     */
    public int getWorkspaceTopNodeID(int workspaceID);
    
    /**
     * @param workspaceID ID of the workspace
     * @param workspaceNodeID ID of the node
     * @return true if the given node is the top node of the given workspace
     */
    public boolean isTopNodeOfWorkspace(int workspaceID, int workspaceNodeID);
    
    /**
     * Retrieves a collection containing all the nodes
     * of the workspace with the given ID.
     * 
     * @param workspaceID ID of the workspace
     * @return Collection of nodes associated with the given workspace
     */
    public Collection<WorkspaceNode> getNodesForWorkspace(int workspaceID);
    
    /**
     * Retrieves a collection containing all the metadata nodes present in the tree
     * (top node and descendants) of the workspace with the given ID.
     * @param workspaceID ID of the workspace
     * @return Collection of nodes associated with the given workspace
     */
    public Collection<WorkspaceNode> getMetadataNodesInTreeForWorkspace(int workspaceID);
    
    /**
     * Retrieves a collection containing the child nodes of the node with the
     * given ID.
     * 
     * @param workspaceNodeID ID of the parent node
     * @return Collection of nodes that have the given node as parent
     */
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID);
    
    /**
     * Retrieves a collection containing the descendant nodes of the node with
     * the given ID.
     * @param workspaceNodeID ID of the parent node
     * @return Collection of nodes that have the given node as ancestor
     */
    public Collection<WorkspaceNode> getDescendantWorkspaceNodes(int workspaceNodeID);
    
    /**
     * Retrieves a collection containing the descendant nodes of the node with
     * the given ID, filtering them by the given node type.
     * @param workspaceNodeID ID of the parent node
     * @param nodeType type to filter the descendants; if UNKNOWN is passed, all nodes will be returned
     * @return Collection of nodes, of the given type, that have the given node as ancestor
     */
    public Collection<WorkspaceNode> getDescendantWorkspaceNodesByType(int workspaceNodeID, WorkspaceNodeType nodeType);
    
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
     * @param workspaceID ID of the workspace
     * @return Collection of unlinked or deleted nodes that have no parent
     */
    public Collection<WorkspaceNode> getUnlinkedAndDeletedTopNodes(int workspaceID);
    
    /**
     * Retrieves a collection containing the nodes that have no parent
     * (unlinked nodes).
     * @param workspaceID ID of the workspace
     * @return List of unlinked nodes in the workspace
     */
    public List<WorkspaceNode> getUnlinkedNodes(int workspaceID);
    
    /**
     * Retrieves a collection containing the nodes that have no parent
     * (unlinked nodes) and their descendants.
     * @param workspaceID ID of the workspace
     * @return Collection of unlinked nodes in the workspace
     */
    public Collection<WorkspaceNode> getUnlinkedNodesAndDescendants(int workspaceID);
    
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
     * Updates the type of the given node.
     * @param node  WorkspaceNode object to be updated
     */
    public void updateNodeType(WorkspaceNode node);
    
    /**
     * Inserts a link between two nodes (parent and child) into the database.
     * @param nodeLink WorkspaceNodeLink object to insert into the database
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
    
    /**
     * Retrieves the node which was replaced by the given node.
     * @param workspaceID ID of the workspace
     * @param workspaceNodeID ID of the node which replaced the one to retrieve
     * @return WorkspaceNode object corresponding to the older version
     */
    public WorkspaceNode getOlderVersionOfNode(int workspaceID, int workspaceNodeID)
            throws WorkspaceNodeNotFoundException;
    
    /**
     * Retrieves the node which replaced the given node.
     * @param workspaceID ID of the workspace
     * @param workspaceNodeID ID of the node which was replaced
     * @return WorkspaceNode object corresponding to the newer version
     */
    public WorkspaceNode getNewerVersionOfNode(int workspaceID, int workspaceNodeID)
            throws WorkspaceNodeNotFoundException;
    
    /**
     * Replaces a node by a its newer version.
     * @param oldNode
     * @param newNode
     */
    public void replaceNode(WorkspaceNode oldNode, WorkspaceNode newNode);
    
    /**
     * Retrieves a collection of node replacements, each indicating the
     * node to be replaced and its new version.
     * @return Collection of node replacements
     */
    public Collection<WorkspaceNodeReplacement> getAllNodeReplacements();
    
    /**
     * Retrieves a collection of node replacements belonging to the given workspace.
     * @param workspaceID ID of the workspace
     * @return Collection of node replacements
     */
    public Collection<WorkspaceNodeReplacement> getNodeReplacementsForWorkspace(int workspaceID);
    
    /**
     * Retrieves a collection of node URL updated belonging to the given workspace.
     * @param workspaceID ID of the workspace
     * @return Collection of node URL updates
     */
    public Collection<WorkspaceReplacedNodeUrlUpdate> getReplacedAndDeletedNodeUrlsToUpdateForWorkspace(int workspaceID);
}
