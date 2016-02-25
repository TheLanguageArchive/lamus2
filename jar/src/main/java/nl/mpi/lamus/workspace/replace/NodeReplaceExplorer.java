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
package nl.mpi.lamus.workspace.replace;

import java.util.List;
import nl.mpi.lamus.exception.IncompatibleNodesException;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;
import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;

/**
 * Class used to explore the child nodes and call the correct replace checker
 * according to their type.
 * 
 * @author guisil
 */
public interface NodeReplaceExplorer {
   
    /**
     * Explores the current node's children.
     * 
     * @param oldNode node to be replaced
     * @param newNode new version od the node
     * @param actions List where the actions to be performed should be added
     */
    public void exploreReplace(WorkspaceNode oldNode, WorkspaceNode newNode, List<NodeReplaceAction> actions)
            throws ProtectedNodeException, IncompatibleNodesException;
}
