/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.management.implementation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.filesystem.WorkspaceFileHandler;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.model.WorkspaceNodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeManager
 * @author guisil
 */
@Component
public class LamusWorkspaceNodeManager implements WorkspaceNodeManager {

    private static final Logger logger = LoggerFactory.getLogger(LamusWorkspaceNodeManager.class);
    
    private final WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private final WorkspaceDao workspaceDao;
    private final WorkspaceFileHandler workspaceFileHandler;
    
    @Autowired
    public LamusWorkspaceNodeManager(WorkspaceNodeLinkManager wsNodeLinkManager,
            WorkspaceDao wsDao, WorkspaceFileHandler wsFileHandler) {
        
        this.workspaceNodeLinkManager = wsNodeLinkManager;
        this.workspaceDao = wsDao;
        this.workspaceFileHandler = wsFileHandler;
    }
    
    /**
     * @see WorkspaceNodeManager#deleteNodesRecursively(nl.mpi.lamus.workspace.model.WorkspaceNode)
     */
    @Override
    public void deleteNodesRecursively(WorkspaceNode rootNodeToDelete) throws WorkspaceException, ProtectedNodeException {
        
        if(rootNodeToDelete.isProtected()) {
            String message = "Cannot proceed with deleting because the node (ID = " + rootNodeToDelete.getWorkspaceNodeID() + ") is protected (WS ID = " + rootNodeToDelete.getWorkspaceID() + ").";
            throw new ProtectedNodeException(message, rootNodeToDelete.getArchiveURI(), rootNodeToDelete.getWorkspaceID());
        }
        
        deleteNodesRecursively(rootNodeToDelete, true);
    }
    
    private void deleteNodesRecursively(WorkspaceNode rootNodeToDelete, boolean isFirstRoot) throws WorkspaceException, ProtectedNodeException {
        
        Collection<WorkspaceNode> children = workspaceDao.getChildWorkspaceNodes(rootNodeToDelete.getWorkspaceNodeID());
        
        for(WorkspaceNode child : children) {
            if(child.isProtected()) {
                workspaceNodeLinkManager.unlinkNodes(rootNodeToDelete, child);
            } else {
                deleteNodesRecursively(child, false);
            }
        }
        
        if(WorkspaceNodeStatus.UPLOADED.equals(rootNodeToDelete.getStatus())) {
            workspaceDao.deleteWorkspaceNode(rootNodeToDelete.getWorkspaceID(), rootNodeToDelete.getWorkspaceNodeID());
            File nodeFile = new File(rootNodeToDelete.getWorkspaceURL().getPath());
            try {
                workspaceFileHandler.deleteFile(nodeFile);
            } catch (IOException ex) {
                logger.warn("Error deleting uploaded file (" + nodeFile.getAbsolutePath() + ")", ex);
            }
        } else {
            workspaceDao.setWorkspaceNodeAsDeleted(
                    rootNodeToDelete.getWorkspaceID(),
                    rootNodeToDelete.getWorkspaceNodeID(),
                    rootNodeToDelete.isExternal());
            workspaceNodeLinkManager.unlinkNodeFromAllParents(rootNodeToDelete);
        }
    }
    
}
