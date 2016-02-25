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

import java.util.Collection;
import java.util.List;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceAccessException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.exception.WorkspaceNodeNotFoundException;
import nl.mpi.lamus.exception.WorkspaceNotFoundException;
import nl.mpi.lamus.workspace.tree.WorkspaceTreeNode;

/**
 * Collection of service methods that are used to retrieve nodes with the
 * intent of representing them graphically (using a WorkspaceTreeNode instead of
 * just a WorkspaceNode)
 * 
 * @author guisil
 */
public interface WorkspaceTreeService extends WorkspaceService {
    
    /**
     * Retrieves a workspace tree node with the given ID.
     * 
     * @param nodeID ID of the node to retrieve
     * @param parentTreeNode WorkspaceTreeNode object of the parent node
     * @return corresponding workspace tree node
     */
    public WorkspaceTreeNode getTreeNode(int nodeID, WorkspaceTreeNode parentTreeNode)
            throws WorkspaceNodeNotFoundException;
    
    /**
     * Retrieves the nodes in the workspace which are not linked to the tree.
     * 
     * @param userID ID of the user
     * @param workspaceID ID of the workspace
     * @return list of unlinked nodes
     */
    public List<WorkspaceTreeNode> listUnlinkedTreeNodes(String userID, int workspaceID);
    
    /**
     * Deletes the given collection of nodes.
     * 
     * @param userID ID of the user
     * @param nodesToDelete Collection of nodes to delete
     */
    public void deleteTreeNodes(String userID, Collection<WorkspaceTreeNode> nodesToDelete)
            throws WorkspaceNotFoundException, WorkspaceAccessException, WorkspaceException, ProtectedNodeException;
}
