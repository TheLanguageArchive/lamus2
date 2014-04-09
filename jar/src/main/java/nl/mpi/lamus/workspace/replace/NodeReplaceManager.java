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

import nl.mpi.lamus.exception.WorkspaceException;
import nl.mpi.lamus.workspace.model.WorkspaceNode;

/**
 * Class used to trigger the appropriate methods in order to perform
 * a tree / node replacement.
 * 
 * @author guisil
 */
public interface NodeReplaceManager {

    /**
     * Replaces a tree / node.
     * 
     * @param oldNode node (or top node of tree) to be replaced
     * @param newNode new version of the node (or top node of tree)
     * @param parentNode parent node
     */
    public void replaceTree(WorkspaceNode oldNode, WorkspaceNode newNode, WorkspaceNode parentNode) throws WorkspaceException;
}
