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
package nl.mpi.lamus.workspace.tree.implementation;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
import nl.mpi.lamus.workspace.model.WorkspaceNodeReplacement;
import nl.mpi.lamus.workspace.model.WorkspaceNodeType;
import nl.mpi.lamus.workspace.model.WorkspaceReplacedNodeUrlUpdate;
import nl.mpi.lamus.workspace.tree.WorkspaceDaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Proxy for the WorkspaceDao, which uses the WorkspaceDaoFactory
 * to get the appropriate DAO object.
 * @see WorkspaceDao
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Repository
public class WorkspaceDaoProxy implements WorkspaceDao, Serializable {

    private final static Logger logger = LoggerFactory.getLogger(WorkspaceDaoProxy.class);
    
    @Autowired
    private WorkspaceDaoFactory workspaceDaoFactory;
    
    private transient WorkspaceDao workspaceDao;
    
    
    private synchronized WorkspaceDao getWorkspaceDao() {
        if (this.workspaceDao == null) {
	    logger.debug("Requesting new WorkspaceDao");
	    this.workspaceDao = workspaceDaoFactory.createWorkspaceDao();
	}
	return this.workspaceDao;
    }
    
    /**
     * @see WorkspaceDao#addWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void addWorkspace(Workspace workspace) {
        this.getWorkspaceDao().addWorkspace(workspace);
    }
    
    /**
     * @see WorkspaceDao#deleteWorkspace(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void deleteWorkspace(Workspace workspace) {
        this.getWorkspaceDao().deleteWorkspace(workspace);
    }

    /**
     * @see WorkspaceDao#updateWorkspaceTopNode(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceTopNode(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceTopNode(workspace);
    }

    /**
     * @see WorkspaceDao#updateWorkspaceSessionDates(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceSessionDates(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceSessionDates(workspace);
    }
    
    /**
     * @see WorkspaceDao#updateWorkspaceEndDates(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceEndDates(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceEndDates(workspace);
    }

    /**
     * @see WorkspaceDao#updateWorkspaceStorageSpace(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceStorageSpace(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceStorageSpace(workspace);
    }

    /**
     * @see WorkspaceDao#updateWorkspaceCrawlerID(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceCrawlerID(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceCrawlerID(workspace);
    }

    /**
     * @see WorkspaceDao#updateWorkspaceStatusMessage(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void updateWorkspaceStatusMessage(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceStatusMessage(workspace);
    }

    /**
     * @see WorkspaceDao#getWorkspace(int)
     */
    @Override
    public Workspace getWorkspace(int workspaceID) throws WorkspaceNotFoundException {
        return this.getWorkspaceDao().getWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceDao#getWorkspacesForUser(java.lang.String)
     */
    @Override
    public Collection<Workspace> getWorkspacesForUser(String userID) {
        return this.getWorkspaceDao().getWorkspacesForUser(userID);
    }

    /**
     * @see WorkspaceDao#getWorkspacesInFinalStage()
     */
    @Override
    public Collection<Workspace> getWorkspacesInFinalStage() {
        return this.getWorkspaceDao().getWorkspacesInFinalStage();
    }

    /**
     * @see WorkspaceDao#getAllWorkspaces()
     */
    @Override
    public List<Workspace> getAllWorkspaces() {
        return this.getWorkspaceDao().getAllWorkspaces();
    }

    /**
     * @see WorkspaceDao#preLockNode(java.net.URI)
     */
    @Override
    public void preLockNode(URI nodeURI) {
        this.getWorkspaceDao().preLockNode(nodeURI);
    }

    /**
     * @see WorkspaceDao#removeNodePreLock(java.net.URI)
     */
    @Override
    public void removeNodePreLock(URI nodeURI) {
        this.getWorkspaceDao().removeNodePreLock(nodeURI);
    }

    /**
     * @see WorkspaceDao#isAnyOfNodesPreLocked(java.util.List)
     */
    @Override
    public boolean isAnyOfNodesPreLocked(List<String> nodeURIs) {
        return this.getWorkspaceDao().isAnyOfNodesPreLocked(nodeURIs);
    }
    
    /**
     * @see WorkspaceDao#isNodeLocked(java.net.URI)
     */
    @Override
    public boolean isNodeLocked(URI archiveNodeURI) {
        return this.getWorkspaceDao().isNodeLocked(archiveNodeURI);
    }

    /**
     * @see WorkspaceDao#lockNode(java.net.URI, int)
     */
    @Override
    public void lockNode(URI uriToLock, int workspaceID) {
        this.getWorkspaceDao().lockNode(uriToLock, workspaceID);
    }

    /**
     * @see WorkspaceDao#unlockNode(java.net.URI)
     */
    @Override
    public void unlockNode(URI uriToUnlock) {
        this.getWorkspaceDao().unlockNode(uriToUnlock);
    }

    /**
     * @see WorkspaceDao#unlockAllNodesOfWorkspace(int)
     */
    @Override
    public void unlockAllNodesOfWorkspace(int workspaceID) {
        this.getWorkspaceDao().unlockAllNodesOfWorkspace(workspaceID);
    }
    
    /**
     * @see WorkspaceDao#getWorkspaceNodeByArchiveURI(java.net.URI)
     */
    @Override
    public Collection<WorkspaceNode> getWorkspaceNodeByArchiveURI(URI archiveNodeURI) {
        return this.getWorkspaceDao().getWorkspaceNodeByArchiveURI(archiveNodeURI);
    }

    /**
     * @see WorkspaceDao#addWorkspaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void addWorkspaceNode(WorkspaceNode node) {
        this.getWorkspaceDao().addWorkspaceNode(node);
    }
    
    /**
     * @see WorkspaceDao#setWorkspaceNodeAsDeleted(int, int, boolean)
     */
    @Override
    public void setWorkspaceNodeAsDeleted(int workspaceID, int nodeID, boolean isExternal) {
        this.getWorkspaceDao().setWorkspaceNodeAsDeleted(workspaceID, nodeID, isExternal);
    }

    /**
     * @see WorkspaceDao#deleteWorkspaceNode(int, int)
     */
    @Override
    public void deleteWorkspaceNode(int workspaceID, int nodeID) {
        this.getWorkspaceDao().deleteWorkspaceNode(workspaceID, nodeID);
    }

    /**
     * @see WorkspaceDao#getWorkspaceNode(int)
     */
    @Override
    public WorkspaceNode getWorkspaceNode(int workspaceNodeID) throws WorkspaceNodeNotFoundException {
        return this.getWorkspaceDao().getWorkspaceNode(workspaceNodeID);
    }
    
    /**
     * @see WorkspaceDao#getWorkspaceTopNode(int)
     */
    @Override
    public WorkspaceNode getWorkspaceTopNode(int workspaceID) throws WorkspaceNodeNotFoundException {
        return this.getWorkspaceDao().getWorkspaceTopNode(workspaceID);
    }
    
    /**
     * @see WorkspaceDao#getWorkspaceTopNodeID(int)
     */
    @Override
    public int getWorkspaceTopNodeID(int workspaceID) {
        return this.getWorkspaceDao().getWorkspaceTopNodeID(workspaceID);
    }

    /**
     * @see WorkspaceDao#isTopNodeOfWorkspace(int, int)
     */
    @Override
    public boolean isTopNodeOfWorkspace(int workspaceID, int workspaceNodeID) {
        return this.getWorkspaceDao().isTopNodeOfWorkspace(workspaceID, workspaceNodeID);
    }
    
    /**
     * @see WorkspaceDao#getNodesForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceNode> getNodesForWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getNodesForWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceDao#getMetadataNodesInTreeForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceNode> getMetadataNodesInTreeForWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getMetadataNodesInTreeForWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceDao#getChildWorkspaceNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID) {
        return this.getWorkspaceDao().getChildWorkspaceNodes(workspaceNodeID);
    }

    /**
     * @see WorkspaceDao#getDescendantWorkspaceNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getDescendantWorkspaceNodes(int workspaceNodeID) {
        return this.getWorkspaceDao().getDescendantWorkspaceNodes(workspaceNodeID);
    }

    /**
     * @see WorkspaceDao#getDescendantWorkspaceNodesByType(int, nl.mpi.lamus.workspace.model.WorkspaceNodeType)
     */
    @Override
    public Collection<WorkspaceNode> getDescendantWorkspaceNodesByType(int workspaceNodeID, WorkspaceNodeType nodeType) {
        return this.getWorkspaceDao().getDescendantWorkspaceNodesByType(workspaceNodeID, nodeType);
    }
    
    /**
     * @see WorkspaceDao#getParentWorkspaceNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getParentWorkspaceNodes(int workspaceNodeID) {
        return this.getWorkspaceDao().getParentWorkspaceNodes(workspaceNodeID);
    }
    
    /**
     * @see WorkspaceDao#getUnlinkedAndDeletedTopNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getUnlinkedAndDeletedTopNodes(int workspaceID) {
        return this.getWorkspaceDao().getUnlinkedAndDeletedTopNodes(workspaceID);
    }

    /**
     * @see WorkspaceDao#getUnlinkedNodes(int)
     */
    @Override
    public List<WorkspaceNode> getUnlinkedNodes(int workspaceID) {
        return this.getWorkspaceDao().getUnlinkedNodes(workspaceID);
    }

    /**
     * @see WorkspaceDao#getUnlinkedNodesAndDescendants(int)
     */
    @Override
    public Collection<WorkspaceNode> getUnlinkedNodesAndDescendants(int workspaceID) {
        return this.getWorkspaceDao().getUnlinkedNodesAndDescendants(workspaceID);
    }
    
    /**
     * @see WorkspaceDao#updateNodeWorkspaceURL(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeWorkspaceURL(WorkspaceNode node) {
        this.getWorkspaceDao().updateNodeWorkspaceURL(node);
    }
    
    /**
     * @see WorkspaceDao#updateNodeArchiveUri(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeArchiveUri(WorkspaceNode node) {
        this.getWorkspaceDao().updateNodeArchiveUri(node);
    }
    
    /**
     * @see WorkspaceDao#updateNodeArchiveUrl(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeArchiveUrl(WorkspaceNode node) {
        this.getWorkspaceDao().updateNodeArchiveUrl(node);
    }

    /**
     * @see WorkspaceDao#updateNodeType(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeType(WorkspaceNode node) {
        this.getWorkspaceDao().updateNodeType(node);
    }

    /**
     * @see WorkspaceDao#addWorkspaceNodeLink(nl.mpi.lamus.workspace.model.WorkspaceNodeLink)
     */
    @Override
    public void addWorkspaceNodeLink(WorkspaceNodeLink nodeLink) {
        this.getWorkspaceDao().addWorkspaceNodeLink(nodeLink);
    }
    
    /**
     * @see WorkspaceDao#deleteWorkspaceNodeLink(int, int, int)
     */
    @Override
    public void deleteWorkspaceNodeLink(int workspaceID, int parentNodeID, int childNodeID) {
        this.getWorkspaceDao().deleteWorkspaceNodeLink(workspaceID, parentNodeID, childNodeID);
    }

    /**
     * @see WorkspaceDao#cleanWorkspaceNodesAndLinks(nl.mpi.lamus.workspace.model.Workspace)
     */
    @Override
    public void cleanWorkspaceNodesAndLinks(Workspace workspace) {
        this.getWorkspaceDao().cleanWorkspaceNodesAndLinks(workspace);
    }

    /**
     * @see WorkspaceDao#getOlderVersionOfNode(int, int)
     */
    @Override
    public WorkspaceNode getOlderVersionOfNode(int workspaceID, int workspaceNodeID) throws WorkspaceNodeNotFoundException {
        return this.getWorkspaceDao().getOlderVersionOfNode(workspaceID, workspaceNodeID);
    }

    /**
     * @see WorkspaceDao#getNewerVersionOfNode(int, int)
     */
    @Override
    public WorkspaceNode getNewerVersionOfNode(int workspaceID, int workspaceNodeID) throws WorkspaceNodeNotFoundException {
        return this.getWorkspaceDao().getNewerVersionOfNode(workspaceID, workspaceNodeID);
    }
    
    /**
     * @see WorkspaceDao#replaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode, nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void replaceNode(WorkspaceNode oldNode, WorkspaceNode newNode) {
        this.getWorkspaceDao().replaceNode(oldNode, newNode);
    }

    /**
     * @see WorkspaceDao#getAllNodeReplacements()
     */
    @Override
    public Collection<WorkspaceNodeReplacement> getAllNodeReplacements() {
        return this.getWorkspaceDao().getAllNodeReplacements();
    }

    /**
     * @see WorkspaceDao#getNodeReplacementsForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceNodeReplacement> getNodeReplacementsForWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getNodeReplacementsForWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceDao#getReplacedNodeUrlsToUpdateForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceReplacedNodeUrlUpdate> getReplacedNodeUrlsToUpdateForWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getReplacedNodeUrlsToUpdateForWorkspace(workspaceID);
    }
}
