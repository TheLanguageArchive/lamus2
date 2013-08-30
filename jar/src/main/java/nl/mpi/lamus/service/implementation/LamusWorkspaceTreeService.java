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
package nl.mpi.lamus.service.implementation;

import nl.mpi.lamus.dao.WorkspaceDao;
import nl.mpi.lamus.service.WorkspaceTreeService;
import nl.mpi.lamus.workspace.importing.WorkspaceNodeLinkManager;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.management.WorkspaceAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.tree.implementation.LamusWorkspaceTreeNode;
import nl.mpi.lamus.workspace.upload.WorkspaceUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@Service
public class LamusWorkspaceTreeService extends LamusWorkspaceService implements WorkspaceTreeService {

    @Autowired
    public LamusWorkspaceTreeService(WorkspaceAccessChecker aChecker, WorkspaceManager wsManager,
            WorkspaceDao wsDao, WorkspaceUploader wsUploader, WorkspaceNodeLinkManager wsnLinkManager) {
        super(aChecker, wsManager, wsDao, wsUploader, wsnLinkManager);
    }
    
    /**
     * @see WorkspaceTreeService#getTreeNode(int, int)
     */
    @Override
    public WorkspaceTreeNode getTreeNode(int nodeID, WorkspaceTreeNode parentTreeNode) {
        
        WorkspaceNode child = this.workspaceDao.getWorkspaceNode(nodeID);
        
        WorkspaceTreeNode treeNode = new LamusWorkspaceTreeNode(
                        child.getWorkspaceNodeID(), child.getWorkspaceID(), child.getArchiveNodeID(),
                        child.getProfileSchemaURI(), child.getName(), child.getTitle(),
                        child.getType(), child.getWorkspaceURL(), child.getArchiveURL(),
                        child.getOriginURL(), child.getStatus(), child.getPid(),
                        child.getFormat(), parentTreeNode, this.workspaceDao);
        
        return treeNode;
    }
}
