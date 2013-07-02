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
import java.util.Collection;
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
    
    @Override
    public void addWorkspace(Workspace workspace) {
        this.getWorkspaceDao().addWorkspace(workspace);
    }
    
    @Override
    public void deleteWorkspace(int workspaceID) {
        this.getWorkspaceDao().deleteWorkspace(workspaceID);
    }

    @Override
    public void updateWorkspaceTopNode(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceTopNode(workspace);
    }

    @Override
    public void updateWorkspaceSessionDates(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceSessionDates(workspace);
    }
    
    @Override
    public void updateWorkspaceEndDates(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceEndDates(workspace);
    }

    @Override
    public void updateWorkspaceStorageSpace(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceStorageSpace(workspace);
    }

    @Override
    public void updateWorkspaceStatusMessage(Workspace workspace) {
        this.getWorkspaceDao().updateWorkspaceStatusMessage(workspace);
    }

    @Override
    public Workspace getWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getWorkspace(workspaceID);
    }

    @Override
    public Collection<Workspace> listWorkspacesForUser(String userID) {
        return this.getWorkspaceDao().listWorkspacesForUser(userID);
    }

    @Override
    public boolean isNodeLocked(int archiveNodeID) {
        return this.getWorkspaceDao().isNodeLocked(archiveNodeID);
    }

    @Override
    public void addWorkspaceNode(WorkspaceNode node) {
        this.getWorkspaceDao().addWorkspaceNode(node);
    }

    @Override
    public WorkspaceNode getWorkspaceNode(int workspaceNodeID) {
        return this.getWorkspaceDao().getWorkspaceNode(workspaceNodeID);
    }
    
    @Override
    public WorkspaceNode getWorkspaceTopNode(int workspaceID) {
        return this.getWorkspaceDao().getWorkspaceTopNode(workspaceID);
    }
    
    @Override
    public Collection<WorkspaceNode> getNodesForWorkspace(int workspaceID) {
        return this.getWorkspaceDao().getNodesForWorkspace(workspaceID);
    }

    @Override
    public Collection<WorkspaceNode> getChildWorkspaceNodes(int workspaceNodeID) {
        return this.getWorkspaceDao().getChildWorkspaceNodes(workspaceNodeID);
    }

    @Override
    public void updateNodeWorkspaceURL(WorkspaceNode node) {
        this.getWorkspaceDao().updateNodeWorkspaceURL(node);
    }

    @Override
    public void addWorkspaceNodeLink(WorkspaceNodeLink nodeLink) {
        this.getWorkspaceDao().addWorkspaceNodeLink(nodeLink);
    }
}
