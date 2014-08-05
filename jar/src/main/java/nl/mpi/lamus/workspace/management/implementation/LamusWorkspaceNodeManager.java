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

import java.util.Collection;
import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.management.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.management.WorkspaceNodeManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @see WorkspaceNodeManager
 * @author guisil
 */
@Component
public class LamusWorkspaceNodeManager implements WorkspaceNodeManager {

    private WorkspaceNodeLinkManager workspaceNodeLinkManager;
    private WorkspaceDao workspaceDao;
    
    @Autowired
    public LamusWorkspaceNodeManager(WorkspaceNodeLinkManager wsNodeLinkManager,
            WorkspaceDao wsDao) {
        
        this.workspaceNodeLinkManager = wsNodeLinkManager;
        this.workspaceDao = wsDao;
    }
    
    @Override
    public void deleteNodesRecursively(WorkspaceNode rootNodeToDelete) throws WorkspaceException {
        
        Collection<WorkspaceNode> children = workspaceDao.getChildWorkspaceNodes(rootNodeToDelete.getWorkspaceNodeID());
        
        for(WorkspaceNode child : children) {
            deleteNodesRecursively(child);
        }
        
        workspaceDao.setWorkspaceNodeAsDeleted(
                rootNodeToDelete.getWorkspaceID(),
                rootNodeToDelete.getWorkspaceNodeID(),
                rootNodeToDelete.isExternal());
        
        workspaceNodeLinkManager.unlinkNodeFromAllParents(rootNodeToDelete);
    }
    
}
