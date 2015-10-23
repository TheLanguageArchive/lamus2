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
package nl.mpi.lamus.workspace.management;

import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Provides some node managing functionality.
 * @author guisil
 */
public interface WorkspaceNodeManager {
    
    /**
     * Deletes a node and recursively deletes its descendants.
     * Note that the nodes are not immediately deleted, but unlinked
     * from their parents and marked in the database as deleted.
     * The action to follow will be taken care of during the workspace submission.
     * 
     * @param rootNodeToDelete node to delete, along with its descendants
     */
    public void deleteNodesRecursively(WorkspaceNode rootNodeToDelete) throws WorkspaceException, ProtectedNodeException;
}
