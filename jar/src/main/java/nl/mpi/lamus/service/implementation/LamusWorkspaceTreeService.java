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
import nl.mpi.lamus.tree.WorkspaceTreeNode;
import nl.mpi.lamus.workspace.management.NodeAccessChecker;
import nl.mpi.lamus.workspace.management.WorkspaceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public class LamusWorkspaceTreeService extends LamusWorkspaceService implements WorkspaceTreeService {

    @Autowired
    public LamusWorkspaceTreeService(NodeAccessChecker accessChecker, WorkspaceManager workspaceManager,
            WorkspaceDao workspaceDao) {
        super(accessChecker, workspaceManager, workspaceDao);
    }
    
    public WorkspaceTreeNode getTreeNode(int nodeID, int parentNodeID) {
        
        return this.workspaceDao.getWorkspaceTreeNode(nodeID, parentNodeID);
    }
    
}
