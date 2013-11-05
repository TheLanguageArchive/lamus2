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
import nl.mpi.lamus.workspace.model.Workspace;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeLink;
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
     * @see WorkspaceDao#deleteWorkspace(int)
     */
    @Override
    public void deleteWorkspace(int workspaceID) {
        this.getWorkspaceDao().deleteWorkspace(workspaceID);
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
    public Workspace getWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceDao#listWorkspacesForUser(java.lang.String)
     */
    @Override
    public Collection<Workspace> listWorkspacesForUser(String userID) {
        return this.getWorkspaceDao().listWorkspacesForUser(userID);
    }

    /**
     * @see WorkspaceDao#isNodeLocked(java.net.URI)
     */
    @Override
    public boolean isNodeLocked(URI archiveNodeURI) {
        return this.getWorkspaceDao().isNodeLocked(archiveNodeURI);
    }

    /**
     * @see WorkspaceDao#addWorkspaceNode(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void addWorkspaceNode(WorkspaceNode node) {
        this.getWorkspaceDao().addWorkspaceNode(node);
    }
    
    /**
     * @see WorkspaceDao#setWorkspaceNodeAsDeleted(int, int)
     */
    @Override
    public void setWorkspaceNodeAsDeleted(int workspaceID, int nodeID) {
        this.getWorkspaceDao().setWorkspaceNodeAsDeleted(workspaceID, nodeID);
    }

    /**
     * @see WorkspaceDao#getWorkspaceNode(int)
     */
    @Override
    public WorkspaceNode getWorkspaceNode(int workspaceNodeID) {
        return this.getWorkspaceDao().getWorkspaceNode(workspaceNodeID);
    }
    
    /**
     * @see WorkspaceDao#getWorkspaceTopNode(int)
     */
    @Override
    public WorkspaceNode getWorkspaceTopNode(int workspaceID) {
        return this.getWorkspaceDao().getWorkspaceTopNode(workspaceID);
    }
    
    /**
     * @see WorkspaceDao#getNodesForWorkspace(int)
     */
    @Override
    public Collection<WorkspaceNode> getNodesForWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getNodesForWorkspace(workspaceID);
    }

    /**
     * @see WorkspaceDao#getChildWorkspaceNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID) {
        return this.getWorkspaceDao().getChildWorkspaceNodes(workspaceNodeID);
    }
    
    /**
     * @see WorkspaceDao#getParentWorkspaceNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getParentWorkspaceNodes(int workspaceNodeID) {
        return this.getWorkspaceDao().getParentWorkspaceNodes(workspaceNodeID);
    }
    
    /**
     * @see WorkspaceDao#getDeletedTopNodes(int)
     */
    @Override
    public Collection<WorkspaceNode> getDeletedTopNodes(int workspaceID) {
        return this.getWorkspaceDao().getDeletedTopNodes(workspaceID);
    }

    /**
     * @see WorkspaceDao#listUnlinkedNodes(int)
     */
    @Override
    public List<WorkspaceNode> listUnlinkedNodes(int workspaceID) {
        return this.getWorkspaceDao().listUnlinkedNodes(workspaceID);
    }

    /**
     * @see WorkspaceDao#updateNodeWorkspaceURL(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeWorkspaceURL(WorkspaceNode node) {
        this.getWorkspaceDao().updateNodeWorkspaceURL(node);
    }
    
    /**
     * @see WorkspaceDao#updateNodeArchiveUriUrl(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void updateNodeArchiveUriUrl(WorkspaceNode node) {
        this.getWorkspaceDao().updateNodeArchiveUriUrl(node);
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
}
