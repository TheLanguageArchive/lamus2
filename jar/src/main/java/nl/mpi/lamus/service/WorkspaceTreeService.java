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
package nl.mpi.lamus.service;

import java.util.List;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Collection of service methods that are used to retrieve nodes with the
 * intent of representing them graphically (using a WorkspaceTreeNode instead of
 * just a WorkspaceNode)
 * 
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
public interface WorkspaceTreeService extends WorkspaceService {
    
    /**
     * Retrieves a workspace tree node with the given ID.
     * 
     * @param nodeID ID of the node to retrieve
     * @param parentNode WorkspaceTreeNode object of the parent node
     * @return corresponding workspace tree node
     */
    public WorkspaceTreeNode getTreeNode(int nodeID, WorkspaceTreeNode parentNode);
}
