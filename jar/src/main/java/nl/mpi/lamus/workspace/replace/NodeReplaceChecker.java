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

import nl.mpi.lamus.workspace.replace.action.implementation.NodeReplaceAction;
import java.util.List;
import nl.mpi.lamus.exception.ProtectedNodeException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Class used to check the current node and decide which are the
 * necessary actions in order to perform a replacement.
 * 
 * @author guisil
 */
public interface NodeReplaceChecker {
    
    /**
     * Decides which are the necessary actions for this specific node replacement
     * and adds them to the actions list.
     * 
     * @param oldNode node to be replaced
     * @param newNode new version of the node
     * @param parentNode parent node
     * @param newNodeAlreadyLinked true if the new node is already linked to the parent
     * @param actions list of actions
     */
    public void decideReplaceActions(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode, boolean newNodeAlreadyLinked, List<NodeReplaceAction> actions)
            throws ProtectedNodeException;
}
